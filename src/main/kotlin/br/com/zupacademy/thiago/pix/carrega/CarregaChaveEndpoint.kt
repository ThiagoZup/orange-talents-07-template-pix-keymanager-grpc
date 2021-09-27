package br.com.zupacademy.thiago.pix.carrega

import br.com.zupacademy.thiago.CarregaChavePixRequest
import br.com.zupacademy.thiago.CarregaChavePixResponse
import br.com.zupacademy.thiago.KeymanagerCarregaServiceGrpc
import br.com.zupacademy.thiago.integration.bcb.BancoCentralClient
import br.com.zupacademy.thiago.pix.exception.ChavePixNaoEncontradaException
import br.com.zupacademy.thiago.pix.repository.ChavePixRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@Singleton
class CarregaChaveEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BancoCentralClient,
    @Inject private val validator: Validator,
): KeymanagerCarregaServiceGrpc.KeymanagerCarregaServiceImplBase() {

    override fun carrega(
        request: CarregaChavePixRequest,
        responseObserver: StreamObserver<CarregaChavePixResponse>
    ) {

        try {
            val filtro = request.toModel(validator)
            val chaveInfo = filtro.filtra(
                repository = repository,
                bcbClient = bcbClient
            )

            responseObserver.onNext(CarregaChavePixResponseConverter().convert(chaveInfo))
            responseObserver.onCompleted()
        } catch(e: ChavePixNaoEncontradaException){
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(e.message)
                    .asRuntimeException()
            )
            return
        } catch (e: IllegalArgumentException) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(e.message)
                    .asRuntimeException())
            return
        } catch (e: ConstraintViolationException) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(e.message)
                    .asRuntimeException())
            return
        }
    }
}