package br.com.zupacademy.thiago.pix.model

import io.micronaut.data.annotation.Embeddable

@Embeddable
class Titular(
    val nomeTitular: String,
    val cpfTitular: String
)