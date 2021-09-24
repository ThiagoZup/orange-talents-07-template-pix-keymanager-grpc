package br.com.zupacademy.thiago.pix.service

import br.com.zupacademy.thiago.integration.bcb.BancoCentralClient
import br.com.zupacademy.thiago.integration.bcb.CreatePixKeyRequest
import br.com.zupacademy.thiago.pix.exception.ClienteNaoEncontradoException
import br.com.zupacademy.thiago.pix.model.ChavePix
import br.com.zupacademy.thiago.pix.registra.NovaChaveRequest
import br.com.zupacademy.thiago.pix.repository.ChavePixRepository
import io.micronaut.http.HttpStatus
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException
import javax.validation.Valid

@Singleton
open class RegistraChaveService(
    @Inject val itauClient: ContasDeClientesNoItauClient,
    @Inject val bcbClient: BancoCentralClient,
    @Inject val repository: ChavePixRepository
) {

    @Transactional
    open fun registra(@Valid novaChaveRequest: NovaChaveRequest): ChavePix {


        // Busca dados da conta no ERP do ITAU
        val response = itauClient.buscaContaPorTipo(
            novaChaveRequest.clienteId!!,
            novaChaveRequest.tipoConta!!.name
        )
        val conta = response?.toModel() ?: throw ClienteNaoEncontradoException("Cliente com id ${novaChaveRequest.clienteId} e ${novaChaveRequest.tipoConta} não encontrado")

        // Grava chave no banco de dados
        val chave = novaChaveRequest.toModel(conta)
        if(!chave.valida()){
            throw IllegalArgumentException("Chave inválida")
        }
        repository.save(chave)

        // Registra no BCB
        val bcbRequest = CreatePixKeyRequest.of(chave)

        val bcbResponse = bcbClient.create(bcbRequest)

        if(bcbResponse.status != HttpStatus.CREATED){
            throw IllegalStateException("Erro ao registrar chave Pix no Banco Central")
        }
        chave.atualiza(bcbResponse.body()!!.key)

        return chave
    }
}