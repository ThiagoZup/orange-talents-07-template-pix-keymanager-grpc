package br.com.zupacademy.thiago.shared.handler

import br.com.zupacademy.thiago.pix.exception.ChavePixExistenteException
import br.com.zupacademy.thiago.pix.exception.ClienteNaoEncontradoException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorHandler::class)
class ErrorHandlerInterceptor : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>?): Any? {
        try {
            return context!!.proceed()
        } catch (e : Exception) {
            val responseObserver = context!!.parameterValues[1] as StreamObserver<*>

            val status = when(e) {
                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                                                        .withCause(e)
                                                        .withDescription(e.message)

                is IllegalArgumentException -> Status.INVALID_ARGUMENT
                                                        .withCause(e)
                                                        .withDescription(e.message)

                is ClienteNaoEncontradoException -> Status.NOT_FOUND
                                                        .withCause(e)
                                                        .withDescription(e.message)

                is IllegalStateException -> Status.UNAVAILABLE
                                                .withCause(e)
                                                .withDescription(e.message)

                is ChavePixExistenteException -> Status.ALREADY_EXISTS
                                                    .withCause(e)
                                                    .withDescription(e.message)

                is ClienteNaoEncontradoException -> Status.NOT_FOUND
                                                        .withCause(e)
                                                        .withDescription(e.message)

                else -> Status.UNKNOWN
                            .withCause(e)
                            .withDescription("Ocorreu um erro inesperado")
            }

            responseObserver.onError(status.asRuntimeException())
        }
        return null
    }
}