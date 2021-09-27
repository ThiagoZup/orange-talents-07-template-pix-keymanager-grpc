package br.com.zupacademy.thiago.pix.carrega

import br.com.zupacademy.thiago.pix.model.ChavePix
import br.com.zupacademy.thiago.pix.model.ContaAssociada
import br.com.zupacademy.thiago.pix.model.enums.TipoChave
import br.com.zupacademy.thiago.pix.model.enums.TipoConta
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipo: TipoChave,
    val chave: String,
    val tipoConta: TipoConta,
    val conta: ContaAssociada,
    val criadaEm: LocalDateTime = LocalDateTime.now()
){
    companion object {
        fun of(chave: ChavePix): ChavePixInfo{
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clienteId,
                tipo = chave.tipoChave,
                chave = chave.chave,
                tipoConta = chave.conta.tipoConta,
                conta = chave.conta,
                criadaEm = chave.criadaEm
            )
        }
    }
}