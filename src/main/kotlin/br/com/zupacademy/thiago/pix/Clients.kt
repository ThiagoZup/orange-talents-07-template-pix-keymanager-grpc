package br.com.zupacademy.thiago.pix

import br.com.zupacademy.thiago.KeymanagerRegistraServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import jakarta.inject.Singleton

@Factory
class Clients {

    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRegistraServiceGrpc.KeymanagerRegistraServiceBlockingStub{
        return KeymanagerRegistraServiceGrpc.newBlockingStub(channel)
    }
}