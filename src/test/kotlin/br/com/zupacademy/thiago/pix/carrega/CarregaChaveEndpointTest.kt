package br.com.zupacademy.thiago.pix.carrega

import br.com.zupacademy.thiago.CarregaChavePixRequest
import br.com.zupacademy.thiago.KeymanagerCarregaServiceGrpc
import br.com.zupacademy.thiago.integration.bcb.*
import br.com.zupacademy.thiago.pix.model.ChavePix
import br.com.zupacademy.thiago.pix.model.ContaAssociada
import br.com.zupacademy.thiago.pix.model.enums.TipoChave
import br.com.zupacademy.thiago.pix.model.enums.TipoConta
import br.com.zupacademy.thiago.pix.repository.ChavePixRepository
import br.com.zupacademy.thiago.pix.service.ContaResponse
import br.com.zupacademy.thiago.pix.service.InstituicaoResponse
import br.com.zupacademy.thiago.pix.service.TitularResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class CarregaChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerCarregaServiceGrpc.KeymanagerCarregaServiceBlockingStub
){
    @Inject
    lateinit var bcbClient: BancoCentralClient

    companion object{
        val CLIENTE_ID = UUID.randomUUID()
        val contaResponse = ContaResponse(
            tipo = TipoConta.CONTA_CORRENTE,
            instituicao = InstituicaoResponse( nome = "UNIBANCO ITAU SA", ispb = "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse(id = "c56dfef4-7901-44fb-84e2-a2cefb157890", nome = "Rafael M C Ponte", cpf = "12345678901")
        )
    }

    @BeforeEach
    fun setup() {
        repository.save(ChavePix(tipoChave = TipoChave.EMAIL, chave = "zupperson@zup.com.br", clienteId = CLIENTE_ID, conta = contaResponse.toModel()))
        repository.save(ChavePix(tipoChave = TipoChave.CPF, chave = "63657520325", clienteId = CLIENTE_ID, conta = contaResponse.toModel()))
        repository.save(ChavePix(tipoChave = TipoChave.ALEATORIA, chave = "random-key", clienteId = CLIENTE_ID, conta = contaResponse.toModel()))
        repository.save(ChavePix(tipoChave = TipoChave.CELULAR, chave = "+551155554321", clienteId = CLIENTE_ID, conta = contaResponse.toModel()))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve carregar chave por pixId e clienteId`() {

        val chaveExistente = repository.findByChave("+551155554321").get()

        val response = grpcClient.carrega(CarregaChavePixRequest.newBuilder()
            .setPixId(CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                .setPixId(chaveExistente.id.toString())
                .setClienteId(chaveExistente.clienteId.toString())
                .build()
            )
            .build())

        with(response) {
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.clienteId.toString(), this.clienteId)
            assertEquals(chaveExistente.tipoChave.name, this.chave.tipo.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `nao deve carregar por pixId e clienteId quanto filtro invalido`() {
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .setPixId(CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setPixId("")
                    .setClienteId("")
                    .build()
                )
                .build())
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando chave nao existir`() {
        val pixIdNaoExistente = UUID.randomUUID().toString()
        val clienteIdNaoExistente = UUID.randomUUID().toString()
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .setPixId(CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setPixId(pixIdNaoExistente)
                    .setClienteId(clienteIdNaoExistente)
                    .build()
                )
                .build())
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro existir localmente`() {

        val chaveExistente = repository.findByChave("zupperson@zup.com.br").get()

        val response = grpcClient.carrega(CarregaChavePixRequest.newBuilder()
            .setChave(chaveExistente.chave)
            .build())

        with(response) {
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.clienteId.toString(), this.clienteId)
            assertEquals(chaveExistente.tipoChave.name, this.chave.tipo.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro nao existir localmente`() {
        val bcbResponse = pixKeyDetailResponse()
        `when`(bcbClient.findByKey(key = "zupperson2@zup.com.br"))
            .thenReturn(HttpResponse.ok(bcbResponse))

        val response = grpcClient.carrega(CarregaChavePixRequest.newBuilder()
            .setChave("zupperson2@zup.com.br")
            .build())

        with(response) {
            assertEquals("", this.pixId)
            assertEquals(bcbResponse.keyType.name, this.chave.tipo.name)
            assertEquals(bcbResponse.key, this.chave.chave)
        }

    }

    @Test
    fun `nao deve carregar chave por valor da chave quando registro nao existir localmente nem no BCB`() {
        val bcbResponse = pixKeyDetailResponse()
        `when`(bcbClient.findByKey(key = "chave.nao.existente@zup.com.br"))
            .thenReturn(HttpResponse.notFound())

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .setChave("chave.nao.existente@zup.com.br")
                .build())
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quanto filtro invalido`() {
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .setChave("")
                .build())
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `nao deve carregar chave quanto filtro invalido`() {
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .build())
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }

    @Factory
    class CarregaChaveGrpcClient {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerCarregaServiceGrpc.KeymanagerCarregaServiceBlockingStub{
            return KeymanagerCarregaServiceGrpc.newBlockingStub(channel)
        }
    }

    fun  pixKeyDetailResponse(): PixKeyDetailResponse {
        return PixKeyDetailResponse(
            keyType = PixKeyType.EMAIL,
            key = "zupperson2@zup.com.br",
            bankAccount = BankAccount(
                participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                branch = "0001",
                accountNumber = "123",
                accountType = BankAccount.AccountType.CACC
            ),
            owner = Owner(
                type = Owner.OwnerType.NATURAL_PERSON,
                name = "Zupperson da Silva",
                taxIdNumber = "63657520000"
            ),
            createdAt = LocalDateTime.now()
        )
    }
}

