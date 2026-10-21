package com.musgo.vendaseproducao.moldevetor.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.musgo.vendaseproducao.moldevetor.data.db.FacaMoldeEntity
import com.musgo.vendaseproducao.moldevetor.domain.EncaixeConsumo
import com.musgo.vendaseproducao.moldevetor.domain.InstanciaEncaixe
import com.musgo.vendaseproducao.moldevetor.domain.TipoModuloEncaixe
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FacaArtefatosExportacaoInstrumentedTest {
    @Test
    fun encaixeJsonMantemDisposicaoCompletaAposReabertura() {
        val original = EncaixeConsumo(
            tipo = TipoModuloEncaixe.PARALELOGRAMO,
            larguraMm = 812.5,
            alturaMm = 473.25,
            deslocamentoMm = 38.0,
            afastamentoHorizontalMm = 4.5,
            afastamentoVerticalMm = 7.25,
            margemModuloMm = 12.0,
            instancias = listOf(
                InstanciaEncaixe(0, 21.75, 35.5, 45.0, espelhadaHorizontal = true, travada = true),
                InstanciaEncaixe(1, 308.25, 119.75, 270.0, invertidaVertical = true),
            ),
        )

        val restaurado = EncaixeJsonMapper.deJson(EncaixeJsonMapper.paraJson(original))

        assertEquals(original, restaurado)
        assertEquals(21.75, restaurado.instancias.first().xMm, 0.0)
        assertEquals(270.0, restaurado.instancias.last().rotacaoGraus, 0.0)
        assertTrue(restaurado.instancias.first().espelhadaHorizontal)
        assertTrue(restaurado.instancias.first().travada)
        assertTrue(restaurado.instancias.last().invertidaVertical)
        assertEquals(12.0, restaurado.margemModuloMm, 0.0)
    }

    @Test
    fun geraCapaEPdfRenderizavel() {
        val faca = FacaMoldeEntity(
            codigo = "VALIDACAO-001",
            nome = "Validação visual",
            contornoJson = """{"externo":[{"x":0,"y":0},{"x":70,"y":0},{"x":62,"y":180},{"x":8,"y":180}],"furos":[[{"x":25,"y":60},{"x":45,"y":60},{"x":45,"y":90},{"x":25,"y":90}]],"blocos":[]}""",
            encaixeJson = """{"tipo":"RETANGULO","larguraMm":220,"alturaMm":400,"deslocamentoMm":0,"afastamentoHorizontalMm":5,"afastamentoVerticalMm":5,"instancias":[{"blocoIndice":0,"xMm":15,"yMm":10,"rotacaoGraus":0,"espelhadaHorizontal":false,"invertidaVertical":false},{"blocoIndice":0,"xMm":110,"yMm":205,"rotacaoGraus":180,"espelhadaHorizontal":true,"invertidaVertical":false}]}""",
            consumoEncaixeM2 = 0.044,
            corCapaArgb = android.graphics.Color.rgb(220, 38, 38),
            createdAt = 1,
            updatedAt = 1,
        )

        val artefatos = FacaArtefatosExportacao.gerar(faca)
        val capa = artefatos.capaVetorPng
        val pdf = artefatos.encaixePdf
        assertNotNull(capa)
        assertNotNull(pdf)
        assertTrue(capa!!.take(8).toByteArray().contentEquals(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)))
        assertTrue(pdf!!.decodeToString(0, 5) == "%PDF-")

        val bitmapCapa = BitmapFactory.decodeByteArray(capa, 0, capa.size)
        assertEquals(1200, bitmapCapa.width)
        assertEquals(1200, bitmapCapa.height)
        assertEquals(android.graphics.Color.rgb(220, 38, 38), bitmapCapa.getPixel(600, 180))

        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val pasta = requireNotNull(contexto.getExternalFilesDir(null))
        val arquivoPdf = File(pasta, "validacao-encaixe.pdf").apply { writeBytes(pdf) }
        File(pasta, "validacao-vetor-capa.png").writeBytes(capa)

        ParcelFileDescriptor.open(arquivoPdf, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                assertEquals(1, renderer.pageCount)
                renderer.openPage(0).use { pagina ->
                    val escala = 0.45f
                    val render = Bitmap.createBitmap(
                        (pagina.width * escala).toInt().coerceAtLeast(1),
                        (pagina.height * escala).toInt().coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888,
                    )
                    pagina.render(render, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    File(pasta, "validacao-encaixe-render.png").outputStream().use { saida ->
                        assertTrue(render.compress(Bitmap.CompressFormat.PNG, 100, saida))
                    }
                    render.recycle()
                }
            }
        }
        bitmapCapa.recycle()
    }
}
