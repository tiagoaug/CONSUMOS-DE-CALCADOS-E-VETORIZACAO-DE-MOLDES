package com.musgo.vendaseproducao.moldevetor.ui.lista

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musgo.vendaseproducao.moldevetor.data.BlocoImportacaoConflitoException
import com.musgo.vendaseproducao.moldevetor.data.BlocoImportadoResultado
import com.musgo.vendaseproducao.moldevetor.data.ContornoJsonMapper
import com.musgo.vendaseproducao.moldevetor.data.FacaMoldeRepository
import com.musgo.vendaseproducao.moldevetor.data.FacaMoldeSummary
import com.musgo.vendaseproducao.moldevetor.data.db.FacaMoldeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

private enum class FiltroStatus(val label: String) {
    TODAS("Todas"),
    ATIVAS("Ativas"),
    INATIVAS("Inativas"),
}

/** Lista/consulta de facas — busca por código e nome, filtro por status (ver especificação,
 * seção 3 "Consultar/listar"). Exclusão lógica é a ação padrão do botão de lixeira; exclusão
 * física fica atrás de uma segunda confirmação explícita, restrita por ora a quem acessa este
 * módulo (o PDF pede "apenas para administradores" — não há papéis de usuário neste módulo
 * nativo local, então a segunda confirmação é a barreira prática nesta Fase 1). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaFacasScreen(
    repository: FacaMoldeRepository,
    onFacaSelecionada: (id: Long) -> Unit,
    onVisualizarBloco: (id: Long) -> Unit,
    onEditarBloco: (id: Long) -> Unit,
    onNovaFaca: () -> Unit,
    onBack: () -> Unit,
) {
    val summaries by repository.observeSummaries().collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busca by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf(FiltroStatus.ATIVAS) }
    var pendingExcluirLogico by remember { mutableStateOf<FacaMoldeSummary?>(null) }
    var pendingExcluirFisico by remember { mutableStateOf<FacaMoldeSummary?>(null) }
    var pendingExportar by remember { mutableStateOf<FacaMoldeSummary?>(null) }
    var pendingArquivar by remember { mutableStateOf<FacaMoldeSummary?>(null) }
    var uriImportacaoConflito by remember { mutableStateOf<Uri?>(null) }
    var blocoImportadoDisponivel by remember { mutableStateOf<BlocoImportadoResultado?>(null) }
    var mensagemArquivo by remember { mutableStateOf<String?>(null) }
    var mensagemArquivoErro by remember { mutableStateOf(false) }
    var operacaoArquivoEmAndamento by remember { mutableStateOf(false) }

    fun importarBloco(uri: Uri, sobrescrever: Boolean) {
        operacaoArquivoEmAndamento = true
        mensagemArquivo = null
        scope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { entrada ->
                        repository.importarBloco(entrada, sobrescrever)
                    } ?: throw IllegalStateException("Não foi possível abrir o arquivo BLOCO.")
                }
                mensagemArquivoErro = false
                mensagemArquivo = "BLOCO ${resultado.codigo} — ${resultado.nome} importado com foto e dados completos."
                blocoImportadoDisponivel = resultado
                uriImportacaoConflito = null
            } catch (conflito: BlocoImportacaoConflitoException) {
                uriImportacaoConflito = uri
                mensagemArquivoErro = true
                mensagemArquivo = "O código ${conflito.codigo} já existe. Confirme para substituí-lo."
            } catch (erro: Exception) {
                mensagemArquivoErro = true
                mensagemArquivo = "Falha ao importar BLOCO: ${erro.message}"
            } finally {
                operacaoArquivoEmAndamento = false
            }
        }
    }

    val launcherImportar = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importarBloco(uri, sobrescrever = false)
    }
    val launcherExportar = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        val faca = pendingExportar
        pendingExportar = null
        if (uri != null && faca != null) {
            operacaoArquivoEmAndamento = true
            mensagemArquivo = null
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri, "w")?.use { saida ->
                            repository.exportarBloco(faca.id, saida)
                        } ?: throw IllegalStateException("Não foi possível criar o arquivo BLOCO.")
                    }
                    mensagemArquivoErro = false
                    mensagemArquivo = "Arquivo ${faca.codigo}.bloco salvo com foto e projeto completo."
                    pendingArquivar = faca
                } catch (erro: Exception) {
                    mensagemArquivoErro = true
                    mensagemArquivo = "Falha ao exportar BLOCO: ${erro.message}"
                } finally {
                    operacaoArquivoEmAndamento = false
                }
            }
        }
    }

    val filtradas = summaries
        .filter { faca ->
            when (filtro) {
                FiltroStatus.TODAS -> true
                FiltroStatus.ATIVAS -> faca.status == FacaMoldeEntity.STATUS_ATIVA
                FiltroStatus.INATIVAS -> faca.status == FacaMoldeEntity.STATUS_INATIVA
            }
        }
        .filter { faca ->
            busca.isBlank() ||
                faca.codigo.contains(busca, ignoreCase = true) ||
                faca.nome.contains(busca, ignoreCase = true)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editor de Facas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { launcherImportar.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                        enabled = !operacaoArquivoEmAndamento,
                    ) {
                        Text(if (operacaoArquivoEmAndamento) "Aguarde..." else "Importar .bloco")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNovaFaca) {
                Icon(Icons.Filled.Add, contentDescription = "Nova faca")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Conclua o contorno e o consumo. Depois use o ícone de exportação no card para enviar o arquivo .bloco ao aplicativo principal.",
                color = Color(0xFF475569),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                label = { Text("Buscar por código ou nome") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (busca.isNotEmpty()) {
                        IconButton(onClick = { busca = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpar busca")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FiltroStatus.entries.forEach { opcao ->
                    FilterChip(
                        selected = filtro == opcao,
                        onClick = { filtro = opcao },
                        label = { Text(opcao.label) },
                    )
                }
            }

            mensagemArquivo?.let { mensagem ->
                Text(
                    text = mensagem,
                    color = if (mensagemArquivoErro) Color(0xFFB91C1C) else Color(0xFF047857),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }

            if (filtradas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Text(
                        text = if (summaries.isEmpty()) {
                            "Nenhuma faca cadastrada ainda."
                        } else {
                            "Nenhuma faca encontrada com esse filtro/busca."
                        },
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                    items(filtradas, key = { it.id }) { faca ->
                        // Pedido do Tiago: "organizar melhor esse card com cores neutras em
                        // branco puro, um thumbnail do bloco representativo como miniatura, card
                        // menor com as opções disponíveis" — branco puro + borda fina em vez da
                        // elevação cinza padrão do Material3, miniatura do contorno à esquerda, e
                        // as ações viram ícones compactos numa linha só, em vez de 5 botões de
                        // texto empilhados.
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.Top) {
                                    MiniaturaFaca(faca.contornoJson)
                                    Column(
                                        modifier = Modifier.weight(1f).padding(start = 10.dp).clickable { onFacaSelecionada(faca.id) },
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text("${faca.codigo} — ${faca.nome}", fontSize = 14.sp, color = Color(0xFF1E293B), maxLines = 1)
                                            if (faca.status == FacaMoldeEntity.STATUS_INATIVA) {
                                                AssistChip(onClick = {}, label = { Text("Inativa", fontSize = 10.sp) }, modifier = Modifier.size(width = 64.dp, height = 22.dp))
                                            }
                                        }
                                        faca.referenciaModelo?.let { Text(it, fontSize = 11.sp, color = Color(0xFF64748B)) }
                                        Text(
                                            text = faca.areaM2?.let { "Área: %.4f m²".format(it) } ?: "Área ainda não calculada",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                        )
                                        faca.consumoEncaixeM2?.let {
                                            Text("Consumo: %.4f m²/bloco".format(it), fontSize = 11.sp, color = Color(0xFF047857))
                                        }
                                        Text(formatUpdatedAt(faca.updatedAt), fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    IconButton(onClick = { onVisualizarBloco(faca.id) }, modifier = Modifier.size(34.dp)) {
                                        Icon(Icons.Filled.Visibility, contentDescription = "Visualizar bloco", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { onEditarBloco(faca.id) }, modifier = Modifier.size(34.dp)) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar bloco", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            pendingExportar = faca
                                            launcherExportar.launch("${nomeArquivoSeguro(faca.codigo)}.bloco")
                                        },
                                        enabled = !operacaoArquivoEmAndamento,
                                        modifier = Modifier.size(34.dp),
                                    ) {
                                        Icon(Icons.Filled.FileUpload, contentDescription = "Exportar", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            if (faca.status == FacaMoldeEntity.STATUS_ATIVA) {
                                                pendingExcluirLogico = faca
                                            } else {
                                                pendingExcluirFisico = faca
                                            }
                                        },
                                        modifier = Modifier.size(34.dp),
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Excluir faca", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingExcluirLogico?.let { faca ->
        AlertDialog(
            onDismissRequest = { pendingExcluirLogico = null },
            title = { Text("Excluir faca") },
            text = { Text("Marcar \"${faca.codigo} — ${faca.nome}\" como inativa? Ela some da lista padrão, mas continua preservada (registros já exportados/usados não são afetados).") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.excluirLogico(faca.id) }
                    pendingExcluirLogico = null
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { pendingExcluirLogico = null }) { Text("Cancelar") }
            },
        )
    }

    // Exclusão FÍSICA — só oferecida numa faca JÁ inativa (um passo extra antes de chegar
    // aqui), com texto de confirmação explicitamente mais grave que o da exclusão lógica.
    pendingExcluirFisico?.let { faca ->
        AlertDialog(
            onDismissRequest = { pendingExcluirFisico = null },
            title = { Text("Excluir definitivamente") },
            text = { Text("Excluir \"${faca.codigo} — ${faca.nome}\" PERMANENTEMENTE? Essa ação não pode ser desfeita e remove o histórico de área desta faca.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.excluirFisico(faca.id) }
                    pendingExcluirFisico = null
                }) { Text("Excluir definitivamente") }
            },
            dismissButton = {
                TextButton(onClick = { pendingExcluirFisico = null }) { Text("Cancelar") }
            },
        )
    }

    uriImportacaoConflito?.let { uri ->
        AlertDialog(
            onDismissRequest = { uriImportacaoConflito = null },
            title = { Text("Código já cadastrado") },
            text = {
                Text("Já existe um BLOCO com esse código. Deseja substituir o cadastro local, o contorno, as camadas, o encaixe e a foto pelos dados do arquivo?")
            },
            confirmButton = {
                TextButton(onClick = { importarBloco(uri, sobrescrever = true) }) {
                    Text("Substituir")
                }
            },
            dismissButton = {
                TextButton(onClick = { uriImportacaoConflito = null }) { Text("Cancelar") }
            },
        )
    }

    blocoImportadoDisponivel?.let { bloco ->
        AlertDialog(
            onDismissRequest = { blocoImportadoDisponivel = null },
            title = { Text("BLOCO importado") },
            text = {
                Text("${bloco.codigo} — ${bloco.nome} está disponível. Deseja somente visualizar ou abrir agora no editor de facas?")
            },
            confirmButton = {
                TextButton(onClick = {
                    blocoImportadoDisponivel = null
                    onEditarBloco(bloco.id)
                }) { Text("Editar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    blocoImportadoDisponivel = null
                    onVisualizarBloco(bloco.id)
                }) { Text("Visualizar") }
            },
        )
    }

    pendingArquivar?.let { faca ->
        AlertDialog(
            onDismissRequest = { pendingArquivar = null },
            title = { Text("BLOCO exportado") },
            text = {
                Text("O arquivo completo foi salvo. Deseja remover agora a cópia local e a foto de \"${faca.codigo} — ${faca.nome}\" para liberar espaço? Depois, basta importar o arquivo .bloco.")
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.excluirFisico(faca.id)
                        mensagemArquivoErro = false
                        mensagemArquivo = "Cópia local removida. O projeto permanece no arquivo .bloco exportado."
                    }
                    pendingArquivar = null
                }) { Text("Remover e liberar espaço") }
            },
            dismissButton = {
                TextButton(onClick = { pendingArquivar = null }) { Text("Manter no aparelho") }
            },
        )
    }
}

/** Miniatura do contorno externo da faca — pedido do Tiago: "um thumbnail do bloco
 * representativo como miniatura". Neutra (cinza claro) quando ainda não tem contorno
 * vetorizado, igual ao resto do card (branco puro + cinza neutro). */
@Composable
private fun MiniaturaFaca(contornoJson: String?) {
    val pontos = remember(contornoJson) {
        contornoJson?.let { runCatching { ContornoJsonMapper.deJson(it).externo }.getOrNull() }
    }
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
    ) {
        if (pontos != null && pontos.size >= 3) {
            Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                val minX = pontos.minOf { it.xMm }
                val maxX = pontos.maxOf { it.xMm }
                val minY = pontos.minOf { it.yMm }
                val maxY = pontos.maxOf { it.yMm }
                val largura = (maxX - minX).toFloat().coerceAtLeast(1f)
                val altura = (maxY - minY).toFloat().coerceAtLeast(1f)
                val escala = min(size.width / largura, size.height / altura)
                val origemX = (size.width - largura * escala) / 2f
                val origemY = (size.height - altura * escala) / 2f
                val caminho = Path().apply {
                    pontos.forEachIndexed { indice, ponto ->
                        val x = origemX + (ponto.xMm.toFloat() - minX.toFloat()) * escala
                        val y = origemY + (ponto.yMm.toFloat() - minY.toFloat()) * escala
                        if (indice == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(caminho, color = Color(0xFF2563EB).copy(alpha = 0.12f))
                drawPath(caminho, color = Color(0xFF2563EB), style = Stroke(width = 2.5f))
            }
        }
    }
}

private fun nomeArquivoSeguro(codigo: String): String =
    codigo.trim().replace(Regex("[^A-Za-z0-9._-]+"), "_").ifBlank { "bloco" }

private fun formatUpdatedAt(epochMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date(epochMillis))
