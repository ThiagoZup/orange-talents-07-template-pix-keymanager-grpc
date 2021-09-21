package br.com.zupacademy.thiago.pix.service

import br.com.zupacademy.thiago.pix.model.ChavePix
import br.com.zupacademy.thiago.pix.registra.NovaChavePix
import br.com.zupacademy.thiago.pix.repository.ChavePixRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Singleton
open class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ContasDeClientesNoItauClient) {

    open fun registra(@Valid novaChaveRequest: NovaChavePix): ChavePix {

        println(2.1)
        // 1. verifica se chave já existe no sistema
        if(repository.existsByChave(novaChaveRequest.chave)){

            throw IllegalStateException("Chave Pix '${novaChaveRequest.chave}' já existe")
        }
        println("2.2")
        // 2.busca dados da conta no ERP do ITAU
        val response = itauClient.buscaContaPorTipo(
            novaChaveRequest.clienteId!!,
            novaChaveRequest.tipoConta!!.name
        )
        println("2.3")
        val conta = response.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itaú")
        println("2.4")
        // 3. grava no banco de dados
        val chave = novaChaveRequest.toModel(conta)
        println("2.5")
        return chave
    }

}