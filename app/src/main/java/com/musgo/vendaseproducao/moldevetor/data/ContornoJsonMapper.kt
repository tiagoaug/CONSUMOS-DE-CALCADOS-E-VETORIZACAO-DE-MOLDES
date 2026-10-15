package com.musgo.vendaseproducao.moldevetor.data

import com.musgo.vendaseproducao.moldevetor.domain.Contorno
import com.musgo.vendaseproducao.moldevetor.domain.Ponto
import org.json.JSONArray
import org.json.JSONObject

/** (De)serialização de [Contorno] pra gravar em [FacaMoldeEntity.contornoJson]/[areaAnteriorJson]
 * — usa org.json (já usado em expedicaobling/data/ShippingOrderMapper.kt neste projeto), não
 * kotlinx.serialization, exatamente pra manter domain/Contorno.kt livre de anotações e 100%
 * testável em JUnit puro (org.json só existe no runtime Android, não no classpath de teste
 * unitário puro). */
object ContornoJsonMapper {

    fun paraJson(contorno: Contorno): String {
        val json = JSONObject()
        json.put("externo", pontosParaJsonArray(contorno.externo))
        val furosJson = JSONArray()
        contorno.furos.forEach { furosJson.put(pontosParaJsonArray(it)) }
        json.put("furos", furosJson)
        val blocosJson = JSONArray()
        contorno.blocos.forEach { blocosJson.put(pontosParaJsonArray(it)) }
        json.put("blocos", blocosJson)
        return json.toString()
    }

    fun deJson(texto: String): Contorno {
        val json = JSONObject(texto)
        val externo = jsonArrayParaPontos(json.getJSONArray("externo"))
        val furosJson = json.optJSONArray("furos") ?: JSONArray()
        val furos = (0 until furosJson.length()).map { i -> jsonArrayParaPontos(furosJson.getJSONArray(i)) }
        val blocosJson = json.optJSONArray("blocos") ?: JSONArray()
        val blocos = (0 until blocosJson.length()).map { i -> jsonArrayParaPontos(blocosJson.getJSONArray(i)) }
        return Contorno(externo = externo, furos = furos, blocos = blocos)
    }

    private fun pontosParaJsonArray(pontos: List<Ponto>): JSONArray {
        val array = JSONArray()
        pontos.forEach { p ->
            val obj = JSONObject()
            obj.put("x", p.xMm)
            obj.put("y", p.yMm)
            array.put(obj)
        }
        return array
    }

    private fun jsonArrayParaPontos(array: JSONArray): List<Ponto> =
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Ponto(xMm = obj.getDouble("x"), yMm = obj.getDouble("y"))
        }
}
