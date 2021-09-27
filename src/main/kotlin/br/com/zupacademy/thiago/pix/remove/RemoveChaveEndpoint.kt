package br.com.zupacademy.thiago.pix.remove

import br.com.zupacademy.thiago.*
import br.com.zupacademy.thiago.pix.exception.ChavePixNaoEncontradaException
import br.com.zupacademy.thiago.pix.service.RemoveChaveService
import io.grpc.Status
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class RemoveChaveEndpoint(@Inject private val service: RemoveChaveService): KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceImplBase() {

    override fun remove (
        request: RemoveChavePixRequest?,
        responseObserver: StreamObserver<RemoveChavePixResponse>?) {

        try {
            service.remove(clienteId = request?.clienteId, pixId = request?.pixId)

            responseObserver?.onNext(RemoveChavePixResponse.newBuilder()
                .setClientId(request?.clienteId)
                .setPixId(request?.pixId)
                .build())
        } catch(e: ChavePixNaoEncontradaException){
            responseObserver?.onError(
                Status.NOT_FOUND
                    .withDescription(e.message)
                    .asRuntimeException()
            )
            return
        } catch (e: ConstraintViolationException) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                .withDescription("Dados de entrada inválidos")
                .asRuntimeException())
            return
        } catch (e: IllegalStateException) {
            responseObserver?.onError(
                Status.UNAVAILABLE
                    .withDescription(e.message)
                    .asRuntimeException())
            return
        }
        responseObserver!!.onCompleted()
    }

}