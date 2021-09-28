package br.com.zupacademy.thiago.pix.lista

import br.com.zupacademy.thiago.*
import br.com.zupacademy.thiago.pix.repository.ChavePixRepository
import br.com.zupacademy.thiago.shared.handler.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.ZoneId
import java.util.*


@ErrorHandler
@Singleton
class ListaChavesEndpoint(@Inject private val repository : ChavePixRepository)
    : KeymanagerListaServiceGrpc.KeymanagerListaServiceImplBase() {

    override fun lista(
        request: ListaChavesPixRequest,
        responseObserver: StreamObserver<ListaChavesPixResponse>
    ) {

        if (request.clienteId.isNullOrBlank()){
            throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio")
        }

        val clienteId = UUID.fromString(request.clienteId)
        val chaves = repository.findAllByClienteId(clienteId).map {
            ListaChavesPixResponse.ChavePix.newBuilder()
                .setPixId(it.id.toString())
                .setTipo(TipoDeChave.valueOf(it.tipoChave.name))
                .setChave(it.chave)
                .setTipoDeConta(TipoDeConta.valueOf(it.conta.tipoConta.name))
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                           .build()
                })
                .build()
        }
        responseObserver.onNext(ListaChavesPixResponse.newBuilder()
            .setClienteId(clienteId.toString())
            .addAllChaves(chaves)
            .build())

        responseObserver.onCompleted()
    }
}