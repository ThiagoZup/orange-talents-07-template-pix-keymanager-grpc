package br.com.zupacademy.thiago.pix.registra

import br.com.zupacademy.thiago.*
import br.com.zupacademy.thiago.pix.model.enums.TipoChave
import br.com.zupacademy.thiago.pix.model.enums.TipoConta
import br.com.zupacademy.thiago.pix.service.NovaChavePixService
import io.grpc.stub.StreamObserver

import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class RegistraChaveEndpoint(
    @Inject val service: NovaChavePixService) : KeymanagerRegistraServiceGrpc.KeymanagerRegistraServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest?,
        responseObserver: StreamObserver<RegistraChavePixResponse>?
    ) {
        println("1")

        val novaChaveRequest = request!!.toModel()
        println("2")
        val chaveCriada = service.registra(novaChaveRequest)
        println("3")

        responseObserver!!.onNext(RegistraChavePixResponse.newBuilder()
            .setClientId(chaveCriada.clienteId.toString())
            .setPixId(chaveCriada.id.toString())
            .build())

        responseObserver!!.onCompleted()
    }

}

fun RegistraChavePixRequest.toModel() : NovaChavePix {
    return NovaChavePix(
        clienteId = clienteId,
        tipo = when (tipoDeChave) {
            TipoDeChave.UNKNOWN_TIPO_CHAVE -> null
            else -> TipoChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoConta = when (tipoDeConta) {
            TipoDeConta.UNKNOWN_TIPO_CONTA -> null
            else -> TipoConta.valueOf(tipoDeConta.name)
        }
    )
}