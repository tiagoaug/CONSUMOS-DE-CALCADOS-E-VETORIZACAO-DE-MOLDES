package com.musgo.vendaseproducao.moldevetor

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.musgo.vendaseproducao.moldevetor.ui.MoldeVetorApp
import com.musgo.vendaseproducao.moldevetor.ui.theme.MoldeVetorTheme
import com.musgo.vendaseproducao.moldevetor.visao.MoldeVetorIA
import org.opencv.android.OpenCVLoader

class MoldeVetorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Repassado pela JS ao abrir o módulo (ver MoldeVetorPlugin.kt) — necessário pro
        // contorno por IA poder chamar a Cloud Function autenticada (ver MoldeVetorIA.kt).
        MoldeVetorIA.tokenJsAtual = intent.getStringExtra("idToken")
        // initLocal() carrega a lib nativa do OpenCV embutida no próprio APK (sem depender do
        // app "OpenCV Manager" separado, que não existe mais nessa versão) — precisa rodar
        // antes de qualquer uso de ArucoMarkerDetector/PerspectiveCorrector/ContornoExtractor.
        val opencvCarregado = OpenCVLoader.initLocal()
        Log.i("MoldeVetorActivity", "OpenCV carregado: $opencvCarregado")
        setContent {
            MoldeVetorTheme {
                MoldeVetorApp(
                    onClose = {
                        finish()
                        // Mesmo motivo do overridePendingTransition em MoldeVetorPlugin.open —
                        // sem isso, a animação padrão de saída deixa a WebView (MainActivity)
                        // visível por baixo por um instante ao fechar o módulo.
                        overridePendingTransition(0, 0)
                    },
                )
            }
        }
    }
}
