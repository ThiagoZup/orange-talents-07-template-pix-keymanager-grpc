package br.com.zupacademy.thiago.pix.service

import br.com.zupacademy.thiago.pix.model.ContaAssociada
import br.com.zupacademy.thiago.pix.model.enums.TipoConta
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "http://localhost:9091/api/v1/clientes")
interface ContasDeClientesNoItauClient {

    @Get(value = "/{clienteId}/contas")
    fun buscaContaPorTipo(@PathVariable(value = "clienteId") clienteId: String,
                          @QueryValue(value = "tipo") tipo: String ): ContaResponse
}

@Introspected
data class ContaResponse(
    val tipo: TipoConta,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    fun toModel(): ContaAssociada {
        return ContaAssociada(TipoConta.valueOf(tipo.name), instituicao.nome, instituicao.ispb, agencia, numero, titular.nome, titular.cpf)
    }
}

@Introspected
data class InstituicaoResponse(
    val nome: String,
    val ispb: String
)

@Introspected
data class TitularResponse(
    val id: String,
    val nome: String,
    val cpf: String
)