package com.musgo.vendaseproducao.moldevetor.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

/** Preview da câmera ao vivo via CameraX (use case Preview) + captura sob demanda (use case
 * ImageCapture) — espelha o passo 2 da especificação ("Captura guiada: o app mostra uma guia
 * visual ao vivo pra alinhar o marcador e a faca"). [onCapturaPronta] devolve o [ImageCapture] já
 * vinculado ao ciclo de vida, pro chamador (CapturaScreen) disparar a captura quando o usuário
 * tocar no botão — ver [capturarComoBitmap]. */
@Composable
fun CameraXPreview(
    modifier: Modifier = Modifier,
    onCapturaPronta: (ImageCapture) -> Unit,
    onErro: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    // MAXIMIZE_QUALITY: a precisão do contorno/área depende diretamente da
                    // nitidez da foto — prioriza qualidade sobre velocidade de captura.
                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture,
                    )
                    onCapturaPronta(imageCapture)
                } catch (e: Exception) {
                    Log.e("CameraXPreview", "Falha ao iniciar câmera", e)
                    onErro("Não foi possível iniciar a câmera: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
    )
}

/** Dispara uma captura e devolve o resultado já como [Bitmap] (corrigido de rotação), pronto
 * pra virar um Mat do OpenCV — ver ContornoExtractor/PerspectiveCorrector. Sem salvar em
 * arquivo: o [ImageCapture.OnImageCapturedCallback] (variante em memória) evita um arquivo
 * temporário desnecessário, já que a foto final só é persistida depois de confirmada. */
fun capturarComoBitmap(
    imageCapture: ImageCapture,
    executor: java.util.concurrent.Executor,
    onSucesso: (Bitmap) -> Unit,
    onErro: (String) -> Unit,
) {
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val bitmap = imageProxyParaBitmap(image)
                    onSucesso(bitmap)
                } catch (e: Exception) {
                    onErro("Falha ao processar a foto capturada: ${e.message}")
                } finally {
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onErro("Falha ao capturar a foto: ${exception.message}")
            }
        },
    )
}

private fun imageProxyParaBitmap(image: ImageProxy): Bitmap {
    // Captura em memória (sem OutputFileOptions) sai em JPEG numa única plane — decodifica
    // direto, sem precisar lidar com os 3 planos do formato YUV_420_888 da preview.
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmapBruto = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalStateException("Não foi possível decodificar a imagem capturada.")

    val rotacao = image.imageInfo.rotationDegrees
    if (rotacao == 0) return bitmapBruto

    val matrix = Matrix().apply { postRotate(rotacao.toFloat()) }
    return Bitmap.createBitmap(bitmapBruto, 0, 0, bitmapBruto.width, bitmapBruto.height, matrix, true)
}
