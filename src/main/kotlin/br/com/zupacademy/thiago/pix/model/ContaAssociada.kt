package br.com.zupacademy.thiago.pix.model

import br.com.zupacademy.thiago.pix.model.enums.TipoConta
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotNull

@Embeddable
class ContaAssociada (
    @field:NotNull
    @Column(nullable = false)
    val tipoConta: TipoConta,
    val instituicao: String,
    val ispb: String,
    val agencia: String,
    val numero: String,
    val nomeTitular: String,
    val cpfTitular: String
)