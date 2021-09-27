package br.com.zupacademy.thiago.pix.carrega

import br.com.zupacademy.thiago.CarregaChavePixResponse
import br.com.zupacademy.thiago.TipoDeChave
import br.com.zupacademy.thiago.TipoDeConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregaChavePixResponseConverter {

    fun convert(chaveInfo: ChavePixInfo): CarregaChavePixResponse {
        return CarregaChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId.toString())
            .setPixId(chaveInfo.pixId?.toString() ?: "")
            .setChave(CarregaChavePixResponse.ChavePix.newBuilder()
                .setTipo(TipoDeChave.valueOf(chaveInfo.tipo.name))
                .setChave(chaveInfo.chave)
                .setConta(CarregaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                    .setTipo(TipoDeConta.valueOf(chaveInfo.tipoConta.name))
                    .setInstituicao(chaveInfo.conta.instituicao)
                    .setNomeDoTitular(chaveInfo.conta.nomeTitular)
                    .setCpfDoTitular(chaveInfo.conta.cpfTitular)
                    .setAgencia(chaveInfo.conta.agencia)
                    .setNumeroDaConta(chaveInfo.conta.numero)
                    .build())
                .setCriadaEm(chaveInfo.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build())
            .build()
    }
}