package br.com.zupacademy.thiago.pix.lista

import br.com.zupacademy.thiago.KeymanagerListaServiceGrpc
import br.com.zupacademy.thiago.ListaChavesPixRequest
import br.com.zupacademy.thiago.TipoDeChave
import br.com.zupacademy.thiago.pix.model.ChavePix
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
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavesEndpointTest(
    val repository : ChavePixRepository,
    val grpcClient : KeymanagerListaServiceGrpc.KeymanagerListaServiceBlockingStub
) {
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
        repository.save(ChavePix(tipoChave = TipoChave.ALEATORIA, chave = "random-key", clienteId = UUID.randomUUID(), conta = contaResponse.toModel()))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves do cliente`() {

        val clienteId = CLIENTE_ID.toString()

        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClienteId(clienteId)
            .build())

        with (response.chavesList) {
            assertEquals(2, this.size)
            assertTrue(this.map { Pair(it.tipo, it.chave) }.contains(Pair(TipoDeChave.EMAIL, "zupperson@zup.com.br")))
            assertTrue(this.map { Pair(it.tipo, it.chave) }.contains(Pair(TipoDeChave.CPF, "63657520325")))
        }
    }

    @Test
    fun `nao deve listar chaves quando cliente nao possuir chaves`() {
        val clienteSemChaves = UUID.randomUUID().toString()

        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClienteId(clienteSemChaves)
            .build())

        assertEquals(0, response.chavesCount)
    }

    @Test
    fun `nao deve listar chaves quando clienteId for invalido`() {
        val clienteIdInvalido = ""

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException>{
         grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClienteId(clienteIdInvalido)
            .build())
        }
        with (error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Factory
    class ListaChavesGrpcClient {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerListaServiceGrpc.KeymanagerListaServiceBlockingStub{
            return KeymanagerListaServiceGrpc.newBlockingStub(channel)
        }
    }
}