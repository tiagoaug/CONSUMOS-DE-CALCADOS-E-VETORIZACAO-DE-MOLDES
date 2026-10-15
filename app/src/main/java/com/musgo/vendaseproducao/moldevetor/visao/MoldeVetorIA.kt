package com.musgo.vendaseproducao.moldevetor.visao

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Contorno por IA (Claude, via Cloud Function "moldeVetorContornoIA") — alternativa opcional ao
 * pipeline OpenCV (ver [ContornoExtractor.refinarDentroDoGuia]), pra peças complexas onde a
 * detecção por borda tem dificuldade (textura muito marcada, baixo contraste, furos internos que
 * confundem o threshold). Pedido do Tiago: "módulo IA para ajudar a criar o contorno de peças
 * complexas... podendo escolher usar IA ou não" — SEMPRE opcional, nunca substitui o fluxo
 * existente, só oferece uma segunda opção quando o usuário escolher.
 */
object MoldeVetorIA {

    /** Token de ID do Firebase do usuário logado, repassado pela JS ao abrir o módulo (ver
     * MoldeVetorPlugin.kt / MoldeVetorActivity.kt / src/lib/moldeVetorizacao.ts). A sessão deste
     * app vive SÓ no Firebase JS SDK — o login nativo (`FirebaseAuth.getInstance()`) é
     * deliberadamente pulado com `skipNativeAuth: true` em signInWithGoogle/signInWithApple (ver
     * src/lib/firebase.ts, resolvendo bugs de nonce duplicado), então o SDK nativo de Functions
     * NUNCA teria um usuário pra autenticar a chamada sozinho — chamar a Cloud Function via HTTP
     * puro com esse token no header Authorization contorna isso por completo. */
    var tokenJsAtual: String? = null

    private const val URL_FUNCAO = "https://us-central1-app-vendas-e-producao.cloudfunctions.net/moldeVetorContornoIA"

    /** Recorta [bitmap] pela caixa delimitadora de [guiaPx] (com margem), envia o recorte pro
     * Claude e devolve os pontos do contorno já convertidos de volta pras coordenadas PIXEL da
     * imagem COMPLETA (não do recorte) — prontos pra virar [verticesPx] direto, no mesmo formato
     * de retorno que [ContornoExtractor.refinarDentroDoGuia]. */
    suspend fun extrairContorno(bitmap: Bitmap, guiaPx: List<Offset>): List<Offset>? {
        if (guiaPx.isEmpty()) return null
        val token = tokenJsAtual
            ?: throw IllegalStateException("Sessão não encontrada — feche e reabra o módulo de Vetorização de Facas.")

        val margemFracao = 0.25f
        val minX = guiaPx.minOf { it.x }
        val maxX = guiaPx.maxOf { it.x }
        val minY = guiaPx.minOf { it.y }
        val maxY = guiaPx.maxOf { it.y }
        val margemX = (maxX - minX) * margemFracao
        val margemY = (maxY - minY) * margemFracao
        val left = (minX - margemX).toInt().coerceIn(0, bitmap.width - 1)
        val top = (minY - margemY).toInt().coerceIn(0, bitmap.height - 1)
        val right = (maxX + margemX).toInt().coerceIn(left + 1, bitmap.width)
        val bottom = (maxY + margemY).toInt().coerceIn(top + 1, bitmap.height)
        val recorte = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)

        val saida = ByteArrayOutputStream()
        recorte.compress(Bitmap.CompressFormat.JPEG, 90, saida)
        val imagemBase64 = Base64.encodeToString(saida.toByteArray(), Base64.NO_WRAP)

        val respostaTexto = withContext(Dispatchers.IO) {
            val conexao = URL(URL_FUNCAO).openConnection() as HttpURLConnection
            conexao.requestMethod = "POST"
            conexao.setRequestProperty("Content-Type", "application/json")
            conexao.setRequestProperty("Authorization", "Bearer $token")
            conexao.doOutput = true
            conexao.connectTimeout = 15000
            conexao.readTimeout = 50000
            val corpo = JSONObject().put(
                "data",
                JSONObject().put("imageBase64", imagemBase64).put("mediaType", "image/jpeg"),
            )
            conexao.outputStream.use { it.write(corpo.toString().toByteArray(Charsets.UTF_8)) }
            val codigo = conexao.responseCode
            val fluxo = if (codigo in 200..299) conexao.inputStream else conexao.errorStream
            val texto = fluxo.bufferedReader().use { it.readText() }
            if (codigo !in 200..299) {
                val mensagem = runCatching { JSONObject(texto).optJSONObject("error")?.optString("message") }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() }
                throw IllegalStateException(mensagem ?: "Falha ao consultar a IA (HTTP $codigo).")
            }
            texto
        }

        val resultado = JSONObject(respostaTexto).optJSONObject("result") ?: return null
        val pontosNormalizados = resultado.optJSONArray("pontos") ?: return null
        val larguraRecorte = (right - left).toFloat()
        val alturaRecorte = (bottom - top).toFloat()
        return (0 until pontosNormalizados.length())
            .mapNotNull { indice ->
                val ponto = pontosNormalizados.optJSONObject(indice) ?: return@mapNotNull null
                if (!ponto.has("x") || !ponto.has("y")) return@mapNotNull null
                val xNorm = ponto.optDouble("x").toFloat()
                val yNorm = ponto.optDouble("y").toFloat()
                Offset(left + xNorm * larguraRecorte, top + yNorm * alturaRecorte)
            }
            .takeIf { it.size >= 3 }
    }
}
