package com.musgo.vendaseproducao.moldevetor.ui.cadastro

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.musgo.vendaseproducao.moldevetor.data.BlocoImportacaoConflitoException
import com.musgo.vendaseproducao.moldevetor.data.FacaMoldeCodigoDuplicadoException
import com.musgo.vendaseproducao.moldevetor.data.FacaMoldeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Cadastro e edição dos dados do bloco, incluindo o grupo de modelo ao qual ele pertence. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroFacaScreen(
    repository: FacaMoldeRepository,
    facaId: Long?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    onGerenciarGrupos: () -> Unit,
    onVetorizar: (Long) -> Unit,
    onEditarVetorizacao: (Long) -> Unit,
    onConferirDesvio: (Long) -> Unit,
    onEditarEncaixe: (Long) -> Unit,
    onCriarManual: (Long, larguraMm: Double, alturaMm: Double) -> Unit,
    somenteImportacao: Boolean = false,
    onImportado: (Long) -> Unit = {},
) {
    val grupos by repository.observeGruposModelos().collectAsState(initial = emptyList())
    var codigo by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var grupoModeloId by remember { mutableStateOf<Long?>(null) }
    var grupoExpandido by remember { mutableStateOf(false) }
    var referenciaModelo by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var areaM2Atual by remember { mutableStateOf<Double?>(null) }
    var consumoEncaixeM2Atual by remember { mutableStateOf<Double?>(null) }
    var temContorno by remember { mutableStateOf(false) }
    var temEncaixe by remember { mutableStateOf(false) }
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
                grupoModeloId = faca.grupoModeloId
                referenciaModelo = faca.referenciaModelo ?: ""
                observacoes = faca.observacoes ?: ""
                areaM2Atual = faca.areaM2
                consumoEncaixeM2Atual = faca.consumoEncaixeM2
                temContorno = !faca.contornoJson.isNullOrBlank()
                temEncaixe = !faca.encaixeJson.isNullOrBlank()
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
                    grupoModeloId = grupoModeloId,
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
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (facaId == null) "Nova faca" else "Editar faca", fontWeight = FontWeight.SemiBold)
                        Text("Cadastro do bloco", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (somenteImportacao && facaId == null) {
                SecaoCadastro(titulo = "Importar do Editor de Facas") {
                    Text(
                        "Selecione um arquivo .bloco finalizado para importar o cadastro, o contorno e o consumo.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            } else {
                SecaoCadastro(titulo = "Identificação") {
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it; erro = null },
                        label = { Text("Código da faca *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it; erro = null },
                        label = { Text("Nome / descrição *") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )

                    Text(
                        "Grupo de facas do modelo",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp),
                    )
                    ExposedDropdownMenuBox(
                        expanded = grupoExpandido,
                        onExpandedChange = { grupoExpandido = it },
                    ) {
                        val grupoSelecionado = grupos.firstOrNull { it.id == grupoModeloId }
                        OutlinedTextField(
                            value = grupoSelecionado?.nome ?: "Sem grupo",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Filled.Folder, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(grupoExpandido) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = grupoExpandido,
                            onDismissRequest = { grupoExpandido = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sem grupo") },
                                onClick = { grupoModeloId = null; grupoExpandido = false },
                            )
                            grupos.forEach { grupo ->
                                DropdownMenuItem(
                                    text = { Text(grupo.nome) },
                                    onClick = { grupoModeloId = grupo.id; grupoExpandido = false },
                                )
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onGerenciarGrupos) {
                            Text(if (grupos.isEmpty()) "Criar grupo de modelos" else "Gerenciar grupos")
                        }
                    }

                    OutlinedTextField(
                        value = referenciaModelo,
                        onValueChange = { referenciaModelo = it },
                        label = { Text("Referência complementar (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )
                }

                SecaoCadastro(titulo = "Observações") {
                    OutlinedTextField(
                        value = observacoes,
                        onValueChange = { observacoes = it },
                        label = { Text("Notas sobre esta faca") },
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                    )
                }

                if (facaId != null) {
                    SecaoCadastro(titulo = "Etapas do projeto") {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.Straighten, contentDescription = null, tint = Color(0xFF2563EB))
                            Column {
                                Text(
                                    areaM2Atual?.let { "Área vetorizada: ${"%.4f".format(it)} m²" }
                                        ?: "Esta faca ainda não foi vetorizada.",
                                    fontWeight = FontWeight.Medium,
                                )
                                consumoEncaixeM2Atual?.let { consumo ->
                                    Text(
                                        "Consumo pelo encaixe: ${"%.4f".format(consumo)} m²/bloco",
                                        color = Color(0xFF047857),
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        }
                        if (!somenteImportacao) {
                            Spacer(Modifier.height(14.dp))
                            EtapaProjetoCard(
                                numero = 1,
                                titulo = "Vetorização",
                                legenda = "Capture a foto ou edite os pontos do vetor já salvo.",
                                concluida = temContorno,
                                habilitada = true,
                                acao = if (temContorno) "Editar vetor" else "Iniciar vetorização",
                                onClick = { if (temContorno) onEditarVetorizacao(facaId) else onVetorizar(facaId) },
                            )
                            EtapaProjetoCard(
                                numero = 2,
                                titulo = "Conferência de desvio",
                                legenda = "Confira a medida real e aplique a correção sem alterar o encaixe salvo.",
                                concluida = temContorno && areaM2Atual != null,
                                habilitada = temContorno,
                                acao = "Conferir desvio",
                                onClick = { onConferirDesvio(facaId) },
                            )
                            EtapaProjetoCard(
                                numero = 3,
                                titulo = "Encaixe e consumo",
                                legenda = "Edite a disposição salva dos blocos, dimensões, rotações e afastamentos.",
                                concluida = temEncaixe,
                                habilitada = temContorno,
                                acao = if (temEncaixe) "Editar encaixe salvo" else "Montar encaixe",
                                onClick = { onEditarEncaixe(facaId) },
                            )
                            if (temContorno) {
                                OutlinedButton(
                                    onClick = { onVetorizar(facaId) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                ) {
                                    Text("Fazer nova captura da faca")
                                }
                            }
                            OutlinedButton(
                                onClick = { mostrarDialogoManual = true },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            ) {
                                Text("Criar geometria manual (sem foto)")
                            }
                        }
                    }
                }

                erro?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp) }
                Button(
                    onClick = { salvar() },
                    enabled = !salvando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (salvando) "Salvando..." else "Salvar cadastro")
                }
            }
            if (somenteImportacao) {
                erro?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp) }
            }
            Spacer(Modifier.height(12.dp))
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
            text = { Text("Deseja substituir o cadastro atual pelo contorno, consumo, encaixe e foto recebidos?") },
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
private fun EtapaProjetoCard(
    numero: Int,
    titulo: String,
    legenda: String,
    concluida: Boolean,
    habilitada: Boolean,
    acao: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, if (concluida) Color(0xFFA7F3D0) else Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "$numero.",
                    color = Color(0xFF4F46E5),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 1.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text("Etapa $numero · $titulo", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text(legenda, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 3.dp))
                    Text(
                        if (concluida) "Salva no projeto" else if (habilitada) "Pendente" else "Conclua a etapa anterior",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (concluida) Color(0xFF047857) else Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
            OutlinedButton(
                onClick = onClick,
                enabled = habilitada,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            ) {
                Text(acao)
            }
        }
    }
}

@Composable
private fun SecaoCadastro(titulo: String, conteudo: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            conteudo()
        }
    }
}

@Composable
private fun DialogoGeometriaManual(
    onCancelar: () -> Unit,
    onCriar: (larguraMm: Double, alturaMm: Double) -> Unit,
) {
    var lado1Texto by remember { mutableStateOf("") }
    var lado2Texto by remember { mutableStateOf("") }
    val lado1 = lado1Texto.replace(',', '.').toDoubleOrNull()
    val lado2 = lado2Texto.replace(',', '.').toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nova geometria retangular") },
        text = {
            Column {
                Text("Informe os dois lados do retângulo. Use valores iguais para criar um quadrado.")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = lado1Texto,
                    onValueChange = { lado1Texto = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label = { Text("Lado 1 (mm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lado2Texto,
                    onValueChange = { lado2Texto = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label = { Text("Lado 2 (mm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
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
