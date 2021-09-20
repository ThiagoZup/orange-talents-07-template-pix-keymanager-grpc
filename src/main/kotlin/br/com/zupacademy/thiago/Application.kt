package br.com.zupacademy.thiago

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zupacademy.thiago")
		.start()
}

