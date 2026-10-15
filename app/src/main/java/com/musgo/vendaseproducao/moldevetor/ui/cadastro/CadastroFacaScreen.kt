package com.musgo.vendaseproducao.moldevetor.ui.cadastro

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.musgo.vendaseproducao.moldevetor.data.BlocoImportacaoConflitoException
import com.musgo.vendaseproducao.moldevetor.data.FacaMoldeCodigoDuplicadoException
import com.musgo.vendaseproducao.moldevetor.data.FacaMoldeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Criar/editar uma Faca (Fase 1 — só os campos de identificação; foto/contorno/área entram
 * nas Fases 2-4). [facaId] null = criação; não-null = edição, carrega os dados existentes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroFacaScreen(
    repository: FacaMoldeRepository,
    facaId: Long?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    onVetorizar: (Long) -> Unit,
    onCriarManual: (Long, larguraMm: Double, alturaMm: Double) -> Unit,
    somenteImportacao: Boolean = false,
    onImportado: (Long) -> Unit = {},
) {
    var codigo by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var referenciaModelo by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var areaM2Atual by remember { mutableStateOf<Double?>(null) }
    var consumoEncaixeM2Atual by remember { mutableStateOf<Double?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var salvando by remember { mutableStateOf(false) }
    var mostrarDialogoManual by remember { mutableStateOf(false) }
    var importando by remember { mutableStateOf(false) }
    var uriImportacaoConflito by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(facaId) {
        if (facaId != null) {
            repository.getById(facaId)?.let { faca ->
                codigo = faca.codigo
                nome = faca.nome
                referenciaModelo = faca.referenciaModelo ?: ""
                observacoes = faca.observacoes ?: ""
                areaM2Atual = faca.areaM2
                consumoEncaixeM2Atual = faca.consumoEncaixeM2
            }
        }
    }

    fun salvar() {
        if (codigo.isBlank() || nome.isBlank()) {
            erro = "Código e nome são obrigatórios."
            return
        }
        salvando = true
        erro = null
        scope.launch {
            try {
                repository.salvar(
                    id = facaId,
                    codigo = codigo,
                    nome = nome,
                    referenciaModelo = referenciaModelo,
                    observacoes = observacoes,
                )
                onSaved()
            } catch (e: FacaMoldeCodigoDuplicadoException) {
                erro = e.message
            } catch (e: Exception) {
                erro = "Erro ao salvar: ${e.message}"
            } finally {
                salvando = false
            }
        }
    }

    fun importarProjeto(uri: Uri, sobrescrever: Boolean) {
        importando = true
        erro = null
        scope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { entrada ->
                        repository.importarBloco(entrada, sobrescrever)
                    } ?: throw IllegalStateException("Não foi possível abrir o arquivo da faca.")
                }
                uriImportacaoConflito = null
                onImportado(resultado.id)
            } catch (conflito: BlocoImportacaoConflitoException) {
                uriImportacaoConflito = uri
                erro = "Já existe uma faca com o código ${conflito.codigo}. Confirme para substituir."
            } catch (e: Exception) {
                erro = "Falha ao importar a faca: ${e.message}"
            } finally {
                importando = false
            }
        }
    }

    val launcherImportar = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importarProjeto(uri, sobrescrever = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (facaId == null) "Nova Faca" else "Editar Faca") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (somenteImportacao && facaId == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp,
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Importar do Editor de Facas", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Crie o contorno e calcule o consumo no aplicativo Editor de Facas. Depois importe aqui o arquivo .bloco finalizado.",
                            color = Color(0xFF64748B),
                        )
                        OutlinedButton(
                            onClick = { launcherImportar.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                            enabled = !importando,
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                        ) {
                            Icon(Icons.Filled.FileOpen, contentDescription = null)
                            Text(if (importando) "  Importando..." else "  Escolher arquivo .bloco")
                        }
                    }
                }
                erro?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(it, color = Color(0xFFB91C1C))
                }
            } else {
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text("Código da faca *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome / descrição *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = referenciaModelo,
                onValueChange = { referenciaModelo = it },
                label = { Text("Modelo ou referência de calçado (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = observacoes,
                onValueChange = { observacoes = it },
                label = { Text("Observações") },
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                minLines = 3,
            )
            if (erro != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = erro ?: "", color = androidx.compose.ui.graphics.Color(0xFFB91C1C))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { salvar() },
                enabled = !salvando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (salvando) "Salvando..." else "Salvar")
            }
            // Vetorização (Fase 3) só fica disponível depois de a faca já existir no banco —
            // o contorno/área precisam de um id pra serem gravados (FacaMoldeRepository.
            // salvarResultadoVetorizacao). Numa faca nova, peça pra salvar antes.
            if (facaId != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (areaM2Atual != null) {
                        "Área vetorizada: ${"%.4f".format(areaM2Atual)} m²"
                    } else {
                        "Esta faca ainda não foi vetorizada."
                    },
                )
                consumoEncaixeM2Atual?.let { consumo ->
                    Text(
                        text = "Consumo pelo encaixe: ${"%.4f".format(consumo)} m²/bloco",
                        color = androidx.compose.ui.graphics.Color(0xFF047857),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (!somenteImportacao) {
                    OutlinedButton(
                        onClick = { onVetorizar(facaId) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) {
                        Text(if (areaM2Atual != null) "Vetorizar novamente" else "Vetorizar foto (Beta)")
                    }
                // Pedido do Tiago: "opção de criar geometrias quadradas ou retangulares sem
                // precisar de capturas de fotografias, inserindo seu lado 1 e lado 2" — pula
                // Captura inteiramente, gera um retângulo pelos lados digitados e abre direto no
                // editor de contorno (mesmas ferramentas de ajuste, só sem foto de fundo).
                    OutlinedButton(
                        onClick = { mostrarDialogoManual = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) {
                        Text("Criar geometria manual (sem foto)")
                    }
                }
            }
            }
        }
    }

    if (mostrarDialogoManual && facaId != null) {
        DialogoGeometriaManual(
            onCancelar = { mostrarDialogoManual = false },
            onCriar = { larguraMm, alturaMm ->
                mostrarDialogoManual = false
                onCriarManual(facaId, larguraMm, alturaMm)
            },
        )
    }

    uriImportacaoConflito?.let { uri ->
        AlertDialog(
            onDismissRequest = { uriImportacaoConflito = null },
            title = { Text("Código já cadastrado") },
            text = { Text("Deseja substituir o cadastro atual pelo contorno, consumo, encaixe e foto recebidos do Editor de Facas?") },
            confirmButton = {
                TextButton(onClick = { importarProjeto(uri, sobrescrever = true) }) { Text("Substituir") }
            },
            dismissButton = {
                TextButton(onClick = { uriImportacaoConflito = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun DialogoGeometriaManual(onCancelar: () -> Unit, onCriar: (larguraMm: Double, alturaMm: Double) -> Unit) {
    var lado1Texto by remember { mutableStateOf("") }
    var lado2Texto by remember { mutableStateOf("") }
    val lado1 = lado1Texto.replace(',', '.').toDoubleOrNull()
    val lado2 = lado2Texto.replace(',', '.').toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nova geometria retangular") },
        text = {
            Column {
                Text("Informe os dois lados do retângulo (ou valores iguais pra um quadrado). Você pode ajustar os vértices depois no editor.")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = lado1Texto,
                    onValueChange = { lado1Texto = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label = { Text("Lado 1 (mm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lado2Texto,
                    onValueChange = { lado2Texto = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label = { Text("Lado 2 (mm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = lado1 != null && lado1 > 0.0 && lado2 != null && lado2 > 0.0,
                onClick = { onCriar(lado1!!, lado2!!) },
            ) { Text("Criar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
}
