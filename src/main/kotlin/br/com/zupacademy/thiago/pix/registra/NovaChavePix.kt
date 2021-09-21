package br.com.zupacademy.thiago.pix.registra

import br.com.zupacademy.thiago.pix.model.ChavePix
import br.com.zupacademy.thiago.pix.model.ContaAssociada
import br.com.zupacademy.thiago.pix.model.enums.TipoChave
import br.com.zupacademy.thiago.pix.model.enums.TipoConta
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class NovaChavePix(
    @field:NotBlank
    val clienteId: String?,
    @field:NotNull
    val tipo: TipoChave?,
    @field:Size(max = 77)
    val chave: String?,
    @field:NotNull
    val tipoConta: TipoConta?
) {
    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipoChave = TipoChave.valueOf(this.tipo!!.name),
            chave = if(this.tipo == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            conta = conta
        )
    }
}