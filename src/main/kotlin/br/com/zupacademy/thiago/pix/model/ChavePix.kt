package br.com.zupacademy.thiago.pix.model

import br.com.zupacademy.thiago.pix.model.enums.TipoChave
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
    @field:NotNull
    @Column(nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @field:NotBlank
    @field:Size(max = 77)
    @Column(unique = true, nullable = false)
    var chave: String,

    @field:Valid
    @Embedded
    val conta: ContaAssociada

) {

    @Id
    @GeneratedValue
    var id: UUID? = null

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    fun valida() : Boolean {
        return this.tipoChave.valida(this.chave)
    }

    fun atualiza(chave: String){
        if(this.tipoChave == TipoChave.ALEATORIA){
            this.chave = chave
        }
    }
}