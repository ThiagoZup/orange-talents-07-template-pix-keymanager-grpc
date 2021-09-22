package br.com.zupacademy.thiago.pix.registra

import br.com.zupacademy.thiago.*
import br.com.zupacademy.thiago.pix.model.enums.TipoChave
import br.com.zupacademy.thiago.pix.model.enums.TipoConta
import br.com.zupacademy.thiago.pix.repository.ChavePixRepository
import br.com.zupacademy.thiago.pix.service.ContaResponse
import br.com.zupacademy.thiago.pix.service.ContasDeClientesNoItauClient
import br.com.zupacademy.thiago.pix.service.InstituicaoResponse
import br.com.zupacademy.thiago.pix.service.TitularResponse
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerRegistraServiceGrpc.KeymanagerRegistraServiceBlockingStub
){

    @Inject
    lateinit var itauClient: ContasDeClientesNoItauClient
    lateinit var contaResponse: ContaResponse

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
        contaResponse = ContaResponse(
            tipo = TipoConta.CONTA_CORRENTE,
            instituicao = InstituicaoResponse( nome = "UNIBANCO ITAU SA", ispb = "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse(id = "c56dfef4-7901-44fb-84e2-a2cefb157890", nome = "Rafael M C Ponte", cpf = "12345678901")
        )
    }

    @Test
    fun `deve registrar chave pix com email valido`() {
        //cenário

        Mockito.`when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(contaResponse)

        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("zupperson@zup.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build())

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), this.clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar pix com email invalido`() {
        //cenário

        Mockito.`when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(contaResponse)

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("zupperson.zup.com")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve registrar chave pix com cpf valido`() {
        //cenário

        Mockito.`when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(contaResponse)

        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.CPF)
            .setChave("12345678901")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build())

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), this.clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar chave pix repetida`() {
        Mockito.`when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(contaResponse)

        val contaAssociada = contaResponse.toModel()

        var request = RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("zupperson@zup.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val chavePix = NovaChaveRequest(clienteId = CLIENTE_ID.toString(), tipo = TipoChave.EMAIL, chave = request.chave, TipoConta.CONTA_CORRENTE)
            .toModel(contaAssociada)

        repository.save(chavePix)

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }
        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
    }

    @Test
    fun `nao deve registrar pix com cpf invalido`() {
        //cenário

        Mockito.`when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(contaResponse)

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("123")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve registrar chave pix com celular valido`() {
        //cenário

        Mockito.`when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(contaResponse)

        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.CELULAR)
            .setChave("+5585988714077")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build())

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), this.clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar pix com celular invalido`() {
        //cenário

        Mockito.`when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(contaResponse)

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.CELULAR)
                    .setChave("123")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve registrar com chave nula quando tipo ALEATORIA `() {
        //cenário

        Mockito.`when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(contaResponse)

        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.ALEATORIA)
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build())

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), this.clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar com chave nula quando tipo diferente de ALEATORIA`() {
        //cenário

        Mockito.`when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
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
        return Mockito.mock(ContasDeClientesNoItauClient::class.java)
    }

}