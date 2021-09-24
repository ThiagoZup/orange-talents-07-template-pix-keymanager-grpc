package br.com.zupacademy.thiago.pix.remove

import br.com.zupacademy.thiago.*
import br.com.zupacademy.thiago.integration.bcb.BancoCentralClient
import br.com.zupacademy.thiago.integration.bcb.DeletePixKeyRequest
import br.com.zupacademy.thiago.integration.bcb.DeletePixKeyResponse
import br.com.zupacademy.thiago.pix.model.ChavePix
import br.com.zupacademy.thiago.pix.model.ContaAssociada
import br.com.zupacademy.thiago.pix.model.enums.TipoChave
import br.com.zupacademy.thiago.pix.model.enums.TipoConta
import br.com.zupacademy.thiago.pix.registra.RegistraChaveEndpointTest
import br.com.zupacademy.thiago.pix.repository.ChavePixRepository
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceBlockingStub
){

    @Inject
    lateinit var bcbClient: BancoCentralClient
    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup() {
        repository.deleteAll()
        CHAVE_EXISTENTE = repository.save(
            ChavePix(
                clienteId = UUID.randomUUID(),
                tipoChave = TipoChave.EMAIL,
                chave = "zupperson@zup.com",
                conta = ContaAssociada(
                    tipoConta = TipoConta.CONTA_CORRENTE,
                    instituicao = "UNIBANCO ITAU SA",
                    ispb = "60701190",
                    agencia = "0001",
                    numero = "291900",
                    nomeTitular = "zupperson",
                    cpfTitular = "12345678901"
                )
            )
        )
    }

    @Test
    fun `deve remover chave pix`() {

        `when`(bcbClient.delete(CHAVE_EXISTENTE.chave, DeletePixKeyRequest(CHAVE_EXISTENTE.chave)))
            .thenReturn(HttpResponse.ok(DeletePixKeyResponse(key = CHAVE_EXISTENTE.chave,
                    participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                    deletedAt = LocalDateTime.now()))
        )

        val response = grpcClient.remove(
            RemoveChavePixRequest.newBuilder()
            .setClienteId(RegistraChaveEndpointTest.CLIENTE_ID.toString())
            .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
            .setPixId(CHAVE_EXISTENTE.id.toString())
            .build())

        with(response) {
            assertEquals(CHAVE_EXISTENTE.clienteId.toString(), this.clientId)
        }
    }

    @Test
    fun `nao deve remover chave pix quando clienteId nao corresponse`() {

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(RegistraChaveEndpointTest.CLIENTE_ID.toString())
                    .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                    .setPixId(UUID.randomUUID().toString())
                    .build()
            )
        }
        assertEquals(Status.NOT_FOUND.code, error.status.code)
    }

    @Test
    fun `nao deve remover chave pix quando pixId nao existe`() {

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(RegistraChaveEndpointTest.CLIENTE_ID.toString())
                    .setClienteId(UUID.randomUUID().toString())
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .build()
            )
        }
        assertEquals(Status.NOT_FOUND.code, error.status.code)
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }
}

@Factory
class RemoveChaveGrpcClient {

    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceBlockingStub{
        return KeymanagerRemoveServiceGrpc.newBlockingStub(channel)
    }
}