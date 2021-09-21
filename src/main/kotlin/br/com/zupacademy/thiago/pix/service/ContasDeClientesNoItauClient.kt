package br.com.zupacademy.thiago.pix.service

import br.com.zupacademy.thiago.pix.model.ContaAssociada
import br.com.zupacademy.thiago.pix.model.Instituicao
import br.com.zupacademy.thiago.pix.model.Titular
import br.com.zupacademy.thiago.pix.model.enums.TipoConta
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.net.http.HttpResponse

@Client(value = "http://localhost:9091/api/v1/clientes")
interface ContasDeClientesNoItauClient {

    @Get(value = "/{clienteId}/contas")
    fun buscaContaPorTipo(@PathVariable(value = "clienteId") clienteId: String,
                          @QueryValue(value = "tipo") tipo: String ): ContaResponse
}

@Introspected
data class ContaResponse(
    val tipo: TipoConta,
    val instituicao: InstituicaoRequest,
    val agencia: String,
    val numero: String,
    val titular: TitularRequest
) {
    fun toModel(): ContaAssociada {
        return ContaAssociada(TipoConta.valueOf(tipo.name), instituicao.toModel(), agencia, numero, titular.toModel())
    }
}

@Introspected
data class InstituicaoRequest(
    val nome: String,
    val ispb: String
) {
    fun toModel(): Instituicao{
        return Instituicao(nome, ispb)
    }
}

@Introspected
data class TitularRequest(
    val id: String,
    val nome: String,
    val cpf: String
) {
    fun toModel(): Titular {
        return Titular(nome, cpf)
    }
}