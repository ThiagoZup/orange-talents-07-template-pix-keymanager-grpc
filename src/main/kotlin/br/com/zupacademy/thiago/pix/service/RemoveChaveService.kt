package br.com.zupacademy.thiago.pix.service

import br.com.zupacademy.thiago.integration.bcb.BancoCentralClient
import br.com.zupacademy.thiago.integration.bcb.DeletePixKeyRequest
import br.com.zupacademy.thiago.pix.exception.ChavePixNaoEncontradaException
import br.com.zupacademy.thiago.pix.repository.ChavePixRepository
import br.com.zupacademy.thiago.pix.validation.ValidUUID
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChaveService(
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BancoCentralClient
) {

    @Transactional
    fun remove(
        @NotBlank @ValidUUID("clienteId inválido") clienteId: String?,
        @NotBlank @ValidUUID("pixId inválido") pixId: String?
    ){

        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString(clienteId)

        val chave = repository.findByIdAndClienteId(uuidPixId, uuidClienteId)
            .orElseThrow {ChavePixNaoEncontradaException("Chave pix não encontrada ou não pertence ao cliente") }

        repository.deleteById(uuidPixId)

        val bcbRequest = DeletePixKeyRequest(chave.chave)
        val bcbResponse = bcbClient.delete(key = chave.chave, request = bcbRequest)
        if(bcbResponse.status != HttpStatus.OK){
            throw IllegalStateException("Erro ao remover chave Pix no Banco Central do Brasil")
        }
    }
}