package com.musgo.vendaseproducao.moldevetor.data

import com.musgo.vendaseproducao.moldevetor.domain.EncaixeConsumo
import com.musgo.vendaseproducao.moldevetor.domain.InstanciaEncaixe
import com.musgo.vendaseproducao.moldevetor.domain.TipoModuloEncaixe
import org.json.JSONArray
import org.json.JSONObject

object EncaixeJsonMapper {
    fun paraJson(encaixe: EncaixeConsumo): String = JSONObject().apply {
        put("tipo", encaixe.tipo.name)
        put("larguraMm", encaixe.larguraMm)
        put("alturaMm", encaixe.alturaMm)
        put("deslocamentoMm", encaixe.deslocamentoMm)
        put("afastamentoHorizontalMm", encaixe.afastamentoHorizontalMm)
        put("afastamentoVerticalMm", encaixe.afastamentoVerticalMm)
        put("instancias", JSONArray().apply {
            encaixe.instancias.forEach { item ->
                put(JSONObject().apply {
                    put("blocoIndice", item.blocoIndice)
                    put("xMm", item.xMm)
                    put("yMm", item.yMm)
                    put("rotacaoGraus", item.rotacaoGraus)
                    put("espelhadaHorizontal", item.espelhadaHorizontal)
                    put("invertidaVertical", item.invertidaVertical)
                    put("travada", item.travada)
                })
            }
        })
    }.toString()

    fun deJson(texto: String): EncaixeConsumo {
        val json = JSONObject(texto)
        val itens = json.optJSONArray("instancias") ?: JSONArray()
        return EncaixeConsumo(
            tipo = runCatching { TipoModuloEncaixe.valueOf(json.optString("tipo")) }.getOrDefault(TipoModuloEncaixe.RETANGULO),
            larguraMm = json.optDouble("larguraMm", 0.0),
            alturaMm = json.optDouble("alturaMm", 0.0),
            deslocamentoMm = json.optDouble("deslocamentoMm", 0.0),
            // Compatível com arquivos .bloco salvos antes deste campo virar dois (pedido do
            // Tiago: "afastamento... deve ter valor para as laterais e acima e abaixo") — se não
            // achar os novos campos, usa o antigo "afastamentoMm" único como valor inicial dos
            // dois eixos; se nem esse existir, cai no padrão de 5mm de sempre.
            afastamentoHorizontalMm = json.optDouble("afastamentoHorizontalMm", json.optDouble("afastamentoMm", 5.0)),
            afastamentoVerticalMm = json.optDouble("afastamentoVerticalMm", json.optDouble("afastamentoMm", 5.0)),
            instancias = (0 until itens.length()).map { indice ->
                val item = itens.getJSONObject(indice)
                InstanciaEncaixe(
                    blocoIndice = item.optInt("blocoIndice", 0),
                    xMm = item.optDouble("xMm", 0.0),
                    yMm = item.optDouble("yMm", 0.0),
                    rotacaoGraus = item.optDouble("rotacaoGraus", 0.0),
                    espelhadaHorizontal = item.optBoolean("espelhadaHorizontal", false),
                    invertidaVertical = item.optBoolean("invertidaVertical", false),
                    travada = item.optBoolean("travada", false),
                )
            },
        )
    }
}
