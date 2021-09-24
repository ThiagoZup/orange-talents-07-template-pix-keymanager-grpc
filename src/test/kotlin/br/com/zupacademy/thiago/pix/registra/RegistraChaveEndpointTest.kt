package br.com.zupacademy.thiago.pix.registra

import br.com.zupacademy.thiago.*
import br.com.zupacademy.thiago.integration.bcb.*
import br.com.zupacademy.thiago.pix.model.ChavePix
import br.com.zupacademy.thiago.pix.model.ContaAssociada
import br.com.zupacademy.thiago.pix.model.enums.TipoChave
import br.com.zupacademy.thiago.pix.model.enums.TipoConta
import br.com.zupacademy.thiago.pix.repository.ChavePixRepository
import br.com.zupacademy.thiago.pix.service.ContaResponse
import br.com.zupacademy.thiago.pix.service.ContasDeClientesNoItauClient
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerRegistraServiceGrpc.KeymanagerRegistraServiceBlockingStub
){

    @Inject
    lateinit var itauClient: ContasDeClientesNoItauClient
    @Inject
    lateinit var bcbClient: BancoCentralClient

    companion object {
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
        repository.deleteAll()
    }

    @Test
    fun `deve registrar chave pix com email valido`() {

        val chavePix = NovaChaveRequest(
            clienteId = CLIENTE_ID.toString(),
            tipo = TipoChave.EMAIL,
            chave = "zupperson@zup.com",
            tipoConta = TipoConta.CONTA_CORRENTE
        ).toModel(contaResponse.toModel())

        val bcbRequest = CreatePixKeyRequest.of(chavePix)
        val bcbResponse = createPixKeyResponse(chavePix)

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = chavePix.conta.tipoConta.name))
            .thenReturn(contaResponse)

        `when`(bcbClient.create(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val grpcResponse = grpcClient.registra(registraChavePixRequest(chavePix))

        // validação
        with(grpcResponse) {
            assertEquals(CLIENTE_ID.toString(), this.clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar pix com email invalido`() {

        val chavePix = NovaChaveRequest(
            clienteId = CLIENTE_ID.toString(),
            tipo = TipoChave.EMAIL,
            chave = "zupperson",
            tipoConta = TipoConta.CONTA_CORRENTE
        ).toModel(contaResponse.toModel())

        val bcbRequest = CreatePixKeyRequest.of(chavePix)
        val bcbResponse = createPixKeyResponse(chavePix)

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = chavePix.conta.tipoConta.name))
            .thenReturn(contaResponse)

        `when`(bcbClient.create(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registra(registraChavePixRequest(chavePix))
        }
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve registrar chave pix com cpf valido`() {

        val chavePix = NovaChaveRequest(
            clienteId = CLIENTE_ID.toString(),
            tipo = TipoChave.CPF,
            chave = "12345678901",
            tipoConta = TipoConta.CONTA_CORRENTE
        ).toModel(contaResponse.toModel())

        val bcbRequest = CreatePixKeyRequest.of(chavePix)
        val bcbResponse = createPixKeyResponse(chavePix)

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = chavePix.conta.tipoConta.name))
            .thenReturn(contaResponse)

        `when`(bcbClient.create(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val response = grpcClient.registra(registraChavePixRequest(chavePix))

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), this.clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar chave pix repetida`() {

        val chavePix = NovaChaveRequest(
            clienteId = CLIENTE_ID.toString(),
            tipo = TipoChave.EMAIL,
            chave = "zupperson@zup.com",
            tipoConta = TipoConta.CONTA_CORRENTE
        ).toModel(contaResponse.toModel())

        val bcbRequest = CreatePixKeyRequest.of(chavePix)
        val bcbResponse = createPixKeyResponse(chavePix)

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = chavePix.conta.tipoConta.name))
            .thenReturn(contaResponse)

        `when`(bcbClient.create(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val contaAssociada = contaResponse.toModel()

        var request = registraChavePixRequest(chavePix)

        repository.save(chavePix)

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }
        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
    }

    @Test
    fun `nao deve registrar pix com cpf invalido`() {

        val chavePix = NovaChaveRequest(
            clienteId = CLIENTE_ID.toString(),
            tipo = TipoChave.CPF,
            chave = "123",
            tipoConta = TipoConta.CONTA_CORRENTE
        ).toModel(contaResponse.toModel())

        val bcbRequest = CreatePixKeyRequest.of(chavePix)
        val bcbResponse = createPixKeyResponse(chavePix)

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = chavePix.conta.tipoConta.name))
            .thenReturn(contaResponse)

        `when`(bcbClient.create(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registra(registraChavePixRequest(chavePix))
        }
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve registrar chave pix com celular valido`() {
        val chavePix = NovaChaveRequest(
            clienteId = CLIENTE_ID.toString(),
            tipo = TipoChave.CELULAR,
            chave = "+5585988714077",
            tipoConta = TipoConta.CONTA_CORRENTE
        ).toModel(contaResponse.toModel())

        val bcbRequest = CreatePixKeyRequest.of(chavePix)
        val bcbResponse = createPixKeyResponse(chavePix)

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = chavePix.conta.tipoConta.name))
            .thenReturn(contaResponse)

        `when`(bcbClient.create(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val response = grpcClient.registra(registraChavePixRequest(chavePix))

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), this.clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar pix com celular invalido`() {

        val chavePix = NovaChaveRequest(
            clienteId = CLIENTE_ID.toString(),
            tipo = TipoChave.CELULAR,
            chave = "123",
            tipoConta = TipoConta.CONTA_CORRENTE
        ).toModel(contaResponse.toModel())

        val bcbRequest = CreatePixKeyRequest.of(chavePix)
        val bcbResponse = createPixKeyResponse(chavePix)

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = chavePix.conta.tipoConta.name))
            .thenReturn(contaResponse)

        `when`(bcbClient.create(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registra(registraChavePixRequest(chavePix))
        }
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `nao deve registrar com chave nula quando tipo diferente de ALEATORIA`() {
        //cenário

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(contaResponse)

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.CELULAR)
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @MockBean(ContasDeClientesNoItauClient::class)
    fun itauClient(): ContasDeClientesNoItauClient? {
        return mock(ContasDeClientesNoItauClient::class.java)
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }

    fun createPixKeyResponse(chavePix: ChavePix): CreatePixKeyResponse{
        return CreatePixKeyResponse(
            keyType = PixKeyType.by(chavePix.tipoChave),
            key = chavePix.chave,
            bankAccount = BankAccount(
                participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                branch = chavePix.conta.agencia,
                accountNumber = chavePix.conta.numero,
                accountType = BankAccount.AccountType.by(chavePix.conta.tipoConta)
            ),
            owner = Owner(
                type = Owner.OwnerType.NATURAL_PERSON,
                name = chavePix.conta.nomeTitular,
                taxIdNumber = chavePix.conta.cpfTitular
            ),
            createdAt = LocalDateTime.now()
        )
    }

    fun registraChavePixRequest(chavePix: ChavePix): RegistraChavePixRequest{
        return RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.valueOf(chavePix.tipoChave.name))
            .setChave(chavePix.chave)
            .setTipoDeConta(TipoDeConta.valueOf(chavePix.conta.tipoConta.name))
            .build()
    }
}

@Factory
class RegistraChaveGrpcClient {

    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRegistraServiceGrpc.KeymanagerRegistraServiceBlockingStub{
        return KeymanagerRegistraServiceGrpc.newBlockingStub(channel)
    }
}