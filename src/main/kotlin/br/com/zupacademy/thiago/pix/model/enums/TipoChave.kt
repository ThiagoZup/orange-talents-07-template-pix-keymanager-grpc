package br.com.zupacademy.thiago.pix.model.enums

import javax.annotation.MatchesPattern

enum class TipoChave {
    UNKNOWN_TIPO_CHAVE {
        override fun valida(chave: String): Boolean {
            return false
        }
    },
    CPF {
        override fun valida(chave: String): Boolean {
            return chave.matches("^[0-9]{11}\$".toRegex())
        }
    },
    CELULAR{
        override fun valida(chave: String): Boolean {
            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun valida(chave: String): Boolean {
            return chave.matches("^[a-z0-9.]+@[a-z0-9]+\\.[a-z]+(\\.[a-z]+)?\$".toRegex())
        }
    },
    ALEATORIA {
        override fun valida(chave: String): Boolean {
            return true
        }
    };

    abstract fun valida(chave: String): Boolean
}