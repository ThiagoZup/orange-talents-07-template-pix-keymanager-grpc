package br.com.zupacademy.thiago.pix.model

import io.micronaut.data.annotation.Embeddable

@Embeddable
class Instituicao(
    val nome: String,
    val ispb: String
)