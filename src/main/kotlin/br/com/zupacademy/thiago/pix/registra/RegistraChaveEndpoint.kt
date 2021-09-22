package br.com.zupacademy.thiago.pix.registra

import br.com.zupacademy.thiago.*
import br.com.zupacademy.thiago.pix.exception.ClienteNaoEncontradoException
import br.com.zupacademy.thiago.pix.model.enums.TipoChave
import br.com.zupacademy.thiago.pix.model.enums.TipoConta
import br.com.zupacademy.thiago.pix.repository.ChavePixRepository
import br.com.zupacademy.thiago.pix.service.RegistraChaveService
import io.grpc.Status
import io.grpc.stub.StreamObserver

import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class RegistraChaveEndpoint(
    @Inject val service: RegistraChaveService,
    @Inject val repository: ChavePixRepository
) : KeymanagerRegistraServiceGrpc.KeymanagerRegistraServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest?,
        responseObserver: StreamObserver<RegistraChavePixResponse>?
    ) {

        if(repository.existsByChave(request?.chave)){
            responseObserver?.onError(
                Status.ALREADY_EXISTS
                    .withDescription("Chave ${request?.chave} já registrada")
                    .asRuntimeException()
            )
        }

        try {
            val novaChaveRequest = request!!.toModel()
            val chaveCriada = service.registra(novaChaveRequest)
            if(!chaveCriada.valida()){
                responseObserver?.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("Chave inválida")
                        .asRuntimeException()
                )
                return
            }

            repository.save(chaveCriada)

            responseObserver!!.onNext(RegistraChavePixResponse.newBuilder()
                .setClientId(chaveCriada.clienteId.toString())
                .setPixId(chaveCriada.id.toString())
                .build())

        } catch(e: ClienteNaoEncontradoException){
            responseObserver?.onError(
                Status.NOT_FOUND
                    .withDescription(e.message)
                    .asRuntimeException()
            )
            return
        } catch (e: ConstraintViolationException) {
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("Dados de entrada inválidos")
                .asRuntimeException())
            return
        }
        responseObserver!!.onCompleted()
    }

}

fun RegistraChavePixRequest.toModel() : NovaChaveRequest {
    return NovaChaveRequest(
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