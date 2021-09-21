package br.com.zupacademy.thiago.pix.service

import br.com.zupacademy.thiago.pix.exception.ClienteNaoEncontradoException
import br.com.zupacademy.thiago.pix.model.ChavePix
import br.com.zupacademy.thiago.pix.registra.NovaChavePixRequest
import br.com.zupacademy.thiago.pix.repository.ChavePixRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.Valid

@Singleton
open class NovaChavePixService(
    @Inject val itauClient: ContasDeClientesNoItauClient) {

    open fun registra(@Valid novaChaveRequest: NovaChavePixRequest): ChavePix {

        try {
            val response = itauClient.buscaContaPorTipo(
                novaChaveRequest.clienteId!!,
                novaChaveRequest.tipoConta!!.name
            )
            val conta = response.toModel()
            val chave = novaChaveRequest.toModel(conta)

            return chave
        } catch (e: Exception) {
            throw ClienteNaoEncontradoException("Cliente com id ${novaChaveRequest.clienteId} e ${novaChaveRequest.tipoConta} não encontrado")
        }
    }
}