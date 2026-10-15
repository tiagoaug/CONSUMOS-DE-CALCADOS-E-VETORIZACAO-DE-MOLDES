package com.musgo.vendaseproducao.moldevetor.ui.medida

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musgo.vendaseproducao.moldevetor.domain.Contorno
import com.musgo.vendaseproducao.moldevetor.domain.DimensoesContorno
import com.musgo.vendaseproducao.moldevetor.domain.ResultadoDesvio
import com.musgo.vendaseproducao.moldevetor.domain.ValidacaoDesvio

/** Validação por medida real de referência — espelha a especificação, seção 4, passo 7: "o
 * usuário informa uma medida real (ex.: comprimento medido com paquímetro) e o app compara com
 * o valor que calculou, alertando se o desvio passar do limite configurável" (1%, confirmado com
 * o Tiago). Compara o COMPRIMENTO (maior dimensão do contorno, ver [DimensoesContorno]), não a
 * área — um paquímetro mede distância, não área. Esta validação é opcional: o usuário pode
 * pular direto pra Salvar se não tiver um paquímetro à mão agora. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedidaReferenciaScreen(
    contorno: Contorno,
    salvando: Boolean,
    erro: String?,
    onSalvar: () -> Unit,
    onBack: () -> Unit,
    onAplicarCorrecao: (Contorno) -> Unit,
) {
    val dimensoes = remember(contorno) { DimensoesContorno.calcular(contorno) }
    var medidaTexto by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<ResultadoDesvio?>(null) }
    var erroEntrada by remember { mutableStateOf<String?>(null) }
    var correcaoAplicada by remember { mutableStateOf(false) }

    fun validar() {
        val medida = medidaTexto.replace(",", ".").toDoubleOrNull()
        if (medida == null || medida <= 0.0) {
            erroEntrada = "Informe uma medida válida, em milímetros."
            resultado = null
            return
        }
        erroEntrada = null
        resultado = ValidacaoDesvio.validar(valorCalculado = dimensoes.comprimentoMm, valorReferencia = medida)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validar medida (opcional)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = "Comprimento calculado pela foto: ${"%.1f".format(dimensoes.comprimentoMm)} mm")
            Text(text = "Largura calculada pela foto: ${"%.1f".format(dimensoes.larguraMm)} mm")
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Meça a faca real com um paquímetro (maior comprimento) e informe o valor abaixo pra conferir se a vetorização ficou precisa.")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = medidaTexto,
                onValueChange = { medidaTexto = it; resultado = null },
                label = { Text("Comprimento medido com paquímetro (mm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            if (erroEntrada != null) {
                Text(text = erroEntrada ?: "", color = Color(0xFFB91C1C), modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { validar() }, modifier = Modifier.fillMaxWidth()) {
                Text("Conferir desvio")
            }

            resultado?.let { r ->
                Spacer(modifier = Modifier.height(16.dp))
                val cor = if (r.dentroDoLimite) Color(0xFF15803D) else Color(0xFFB91C1C)
                Text(
                    text = if (r.dentroDoLimite) {
                        "Dentro do limite: desvio de ${"%.2f".format(r.desvioPercentual)}% (limite 1%)."
                    } else {
                        "Atenção: desvio de ${"%.2f".format(r.desvioPercentual)}%, acima do limite de 1%. Considere refazer a captura ou ajustar o contorno na etapa anterior."
                    },
                    color = cor,
                )
                if (r.desvioPercentual > 0.05 && !correcaoAplicada) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "A detecção do marcador tem uma pequena margem de erro própria de cada foto — isso vira \"variação de tamanho\" ao comparar peças de fotos diferentes no cálculo de consumo. Aplicar a correção ajusta TODO o contorno (incluindo blocos) pra bater exatamente com a medida do paquímetro.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val fator = r.valorReferencia / r.valorCalculado
                            onAplicarCorrecao(contorno.escalar(fator))
                            correcaoAplicada = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Aplicar correção ao contorno") }
                }
                if (correcaoAplicada) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Correção aplicada — o contorno já foi ajustado pra essa medida.", fontSize = 12.sp, color = Color(0xFF15803D))
                }
            }

            if (erro != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = erro, color = Color(0xFFB91C1C))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSalvar,
                enabled = !salvando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (salvando) "Aguarde..." else "Continuar para o encaixe")
            }
        }
    }
}
