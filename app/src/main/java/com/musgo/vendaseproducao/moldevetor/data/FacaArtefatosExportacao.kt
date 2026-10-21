package com.musgo.vendaseproducao.moldevetor.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfDocument
import com.musgo.vendaseproducao.moldevetor.data.db.FacaMoldeEntity
import com.musgo.vendaseproducao.moldevetor.domain.Contorno
import com.musgo.vendaseproducao.moldevetor.domain.EncaixeConsumo
import com.musgo.vendaseproducao.moldevetor.domain.InstanciaEncaixe
import com.musgo.vendaseproducao.moldevetor.domain.Ponto
import com.musgo.vendaseproducao.moldevetor.domain.TipoModuloEncaixe
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

internal data class ArtefatosExportacaoFaca(
    val capaVetorPng: ByteArray? = null,
    val encaixePdf: ByteArray? = null,
)

/**
 * Gera os arquivos visuais portáteis incluídos no pacote `.bloco` v2.
 *
 * A capa é uma imagem quadrada e limpa do vetor completo. O PDF usa as dimensões físicas do
 * módulo de encaixe e repete exatamente posição, rotação e espelhamento das instâncias salvas.
 */
internal object FacaArtefatosExportacao {
    private const val TAMANHO_CAPA_PX = 1200
    private const val MARGEM_CAPA_PX = 90f
    private const val PONTOS_POR_MM = 72f / 25.4f

    fun gerar(faca: FacaMoldeEntity): ArtefatosExportacaoFaca {
        val contorno = faca.contornoJson
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { ContornoJsonMapper.deJson(it) }.getOrNull() }
            ?: return ArtefatosExportacaoFaca()

        val capa = runCatching { gerarCapaPng(contorno, faca.corCapaArgb) }.getOrNull()
        val encaixe = faca.encaixeJson
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { EncaixeJsonMapper.deJson(it) }.getOrNull() }
        val pdf = encaixe?.let { runCatching { gerarEncaixePdf(contorno, it) }.getOrNull() }
        return ArtefatosExportacaoFaca(capaVetorPng = capa, encaixePdf = pdf)
    }

    private fun gerarCapaPng(contorno: Contorno, corCapaArgb: Int): ByteArray? {
        val externos = (listOf(contorno.externo) + contorno.blocos).filter { it.size >= 3 }
        if (externos.isEmpty()) return null
        val todos = externos.flatten()
        val minX = todos.minOf { it.xMm }
        val maxX = todos.maxOf { it.xMm }
        val minY = todos.minOf { it.yMm }
        val maxY = todos.maxOf { it.yMm }
        val largura = (maxX - minX).coerceAtLeast(1.0)
        val altura = (maxY - minY).coerceAtLeast(1.0)
        val escala = min(
            (TAMANHO_CAPA_PX - 2f * MARGEM_CAPA_PX) / largura.toFloat(),
            (TAMANHO_CAPA_PX - 2f * MARGEM_CAPA_PX) / altura.toFloat(),
        )
        val deslocamentoX = (TAMANHO_CAPA_PX - largura * escala).toFloat() / 2f
        val deslocamentoY = (TAMANHO_CAPA_PX - altura * escala).toFloat() / 2f
        fun mapear(ponto: Ponto) = android.graphics.PointF(
            deslocamentoX + ((ponto.xMm - minX) * escala).toFloat(),
            deslocamentoY + ((ponto.yMm - minY) * escala).toFloat(),
        )

        val bitmap = Bitmap.createBitmap(TAMANHO_CAPA_PX, TAMANHO_CAPA_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val corSolida = corCapaArgb or -16777216
        val preenchimento = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = corSolida
            style = Paint.Style.FILL
        }
        val contornoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = corSolida
            style = Paint.Style.STROKE
            strokeWidth = 7f
            strokeJoin = Paint.Join.ROUND
        }

        val raiz = caminho(contorno.externo, ::mapear).apply {
            fillType = Path.FillType.EVEN_ODD
            contorno.furos.filter { it.size >= 3 }.forEach { adicionarAnel(this, it, ::mapear) }
        }
        canvas.drawPath(raiz, preenchimento)
        canvas.drawPath(raiz, contornoPaint)
        contorno.blocos.filter { it.size >= 3 }.forEach { bloco ->
            val path = caminho(bloco, ::mapear)
            canvas.drawPath(path, preenchimento)
            canvas.drawPath(path, contornoPaint)
        }

        return ByteArrayOutputStream().use { saida ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)) { "Não foi possível gerar a capa PNG." }
            bitmap.recycle()
            saida.toByteArray()
        }
    }

    private fun gerarEncaixePdf(contorno: Contorno, encaixe: EncaixeConsumo): ByteArray? {
        if (encaixe.larguraMm <= 0.0 || encaixe.alturaMm <= 0.0 || encaixe.instancias.isEmpty()) return null
        val bases = (listOf(contorno.externo) + contorno.blocos)
            .filter { it.size >= 3 }
            .map(::normalizar)
        if (bases.isEmpty()) return null

        val deslocamento = if (encaixe.tipo == TipoModuloEncaixe.PARALELOGRAMO) encaixe.deslocamentoMm else 0.0
        val larguraVisualMm = encaixe.larguraMm + abs(deslocamento)
        val origemX = -min(0.0, deslocamento)
        val larguraPagina = ceil(larguraVisualMm * PONTOS_POR_MM).roundToInt().coerceIn(72, 14_400)
        val alturaPagina = ceil(encaixe.alturaMm * PONTOS_POR_MM).roundToInt().coerceIn(72, 14_400)
        val escalaX = larguraPagina / larguraVisualMm.toFloat()
        val escalaY = alturaPagina / encaixe.alturaMm.toFloat()

        val documento = PdfDocument()
        try {
            val pagina = documento.startPage(PdfDocument.PageInfo.Builder(larguraPagina, alturaPagina, 1).create())
            val canvas = pagina.canvas
            canvas.drawColor(Color.WHITE)
            val fundoModulo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(248, 250, 252)
                style = Paint.Style.FILL
            }
            val bordaModulo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(79, 70, 229)
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
            val preenchimento = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(167, 227, 192)
                style = Paint.Style.FILL
            }
            val bordaBloco = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(22, 163, 74)
                style = Paint.Style.STROKE
                strokeWidth = 1f
                strokeJoin = Paint.Join.ROUND
            }
            fun paraPagina(ponto: PontoDuplo) = android.graphics.PointF(
                ((ponto.x + origemX) * escalaX).toFloat(),
                (ponto.y * escalaY).toFloat(),
            )

            val moldura = Path().apply {
                moveTo((origemX * escalaX).toFloat(), 0f)
                lineTo(((origemX + encaixe.larguraMm) * escalaX).toFloat(), 0f)
                lineTo(((origemX + encaixe.larguraMm + deslocamento) * escalaX).toFloat(), alturaPagina.toFloat())
                lineTo(((origemX + deslocamento) * escalaX).toFloat(), alturaPagina.toFloat())
                close()
            }
            canvas.drawPath(moldura, fundoModulo)
            canvas.drawPath(moldura, bordaModulo)

            encaixe.instancias.forEach { instancia ->
                val base = bases.getOrNull(instancia.blocoIndice) ?: return@forEach
                val externos = base.pontos.map { transformar(it, base, instancia) }
                val path = caminhoDuplo(externos, ::paraPagina)
                canvas.drawPath(path, preenchimento)
                canvas.drawPath(path, bordaBloco)

                if (instancia.blocoIndice == 0) {
                    contorno.furos.filter { it.size >= 3 }.forEach { furo ->
                        val normalizado = furo.map { PontoDuplo(it.xMm - base.minXOriginal, it.yMm - base.minYOriginal) }
                        val transformado = normalizado.map { transformar(it, base, instancia) }
                        val caminhoFuro = caminhoDuplo(transformado, ::paraPagina)
                        canvas.drawPath(caminhoFuro, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL })
                        canvas.drawPath(caminhoFuro, bordaBloco)
                    }
                }
            }

            documento.finishPage(pagina)
            return ByteArrayOutputStream().use { saida ->
                documento.writeTo(saida)
                saida.toByteArray()
            }
        } finally {
            documento.close()
        }
    }

    private data class PontoDuplo(val x: Double, val y: Double)
    private data class BlocoNormalizado(
        val pontos: List<PontoDuplo>,
        val largura: Double,
        val altura: Double,
        val minXOriginal: Double,
        val minYOriginal: Double,
    )

    private fun normalizar(pontos: List<Ponto>): BlocoNormalizado {
        val minX = pontos.minOf { it.xMm }
        val maxX = pontos.maxOf { it.xMm }
        val minY = pontos.minOf { it.yMm }
        val maxY = pontos.maxOf { it.yMm }
        return BlocoNormalizado(
            pontos = pontos.map { PontoDuplo(it.xMm - minX, it.yMm - minY) },
            largura = (maxX - minX).coerceAtLeast(1.0),
            altura = (maxY - minY).coerceAtLeast(1.0),
            minXOriginal = minX,
            minYOriginal = minY,
        )
    }

    private fun transformar(ponto: PontoDuplo, base: BlocoNormalizado, item: InstanciaEncaixe): PontoDuplo {
        val centroX = base.largura / 2.0
        val centroY = base.altura / 2.0
        val dxOriginal = ponto.x - centroX
        val dyOriginal = ponto.y - centroY
        val dx = if (item.espelhadaHorizontal) -dxOriginal else dxOriginal
        val dy = if (item.invertidaVertical) -dyOriginal else dyOriginal
        val rad = Math.toRadians(item.rotacaoGraus)
        return PontoDuplo(
            x = item.xMm + centroX + dx * cos(rad) - dy * sin(rad),
            y = item.yMm + centroY + dx * sin(rad) + dy * cos(rad),
        )
    }

    private fun caminho(pontos: List<Ponto>, mapear: (Ponto) -> android.graphics.PointF): Path =
        Path().also { adicionarAnel(it, pontos, mapear) }

    private fun adicionarAnel(path: Path, pontos: List<Ponto>, mapear: (Ponto) -> android.graphics.PointF) {
        pontos.forEachIndexed { indice, ponto ->
            val mapeado = mapear(ponto)
            if (indice == 0) path.moveTo(mapeado.x, mapeado.y) else path.lineTo(mapeado.x, mapeado.y)
        }
        path.close()
    }

    private fun caminhoDuplo(
        pontos: List<PontoDuplo>,
        mapear: (PontoDuplo) -> android.graphics.PointF,
    ): Path = Path().apply {
        pontos.forEachIndexed { indice, ponto ->
            val mapeado = mapear(ponto)
            if (indice == 0) moveTo(mapeado.x, mapeado.y) else lineTo(mapeado.x, mapeado.y)
        }
        close()
    }
}
