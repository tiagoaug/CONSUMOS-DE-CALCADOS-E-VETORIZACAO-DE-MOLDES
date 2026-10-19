package com.musgo.vendaseproducao.moldevetor.ui.grupos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musgo.vendaseproducao.moldevetor.data.FacaMoldeRepository
import com.musgo.vendaseproducao.moldevetor.data.GrupoModeloNomeDuplicadoException
import com.musgo.vendaseproducao.moldevetor.data.db.GrupoModeloEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GruposModelosScreen(repository: FacaMoldeRepository, onBack: () -> Unit) {
    val grupos by repository.observeGruposModelos().collectAsState(initial = emptyList())
    val facas by repository.observeSummaries().collectAsState(initial = emptyList())
    var grupoEmEdicao by remember { mutableStateOf<GrupoModeloEntity?>(null) }
    var mostrarEditor by remember { mutableStateOf(false) }
    var grupoParaExcluir by remember { mutableStateOf<GrupoModeloEntity?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Grupos de modelos", fontWeight = FontWeight.SemiBold)
                        Text("Configurações", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { grupoEmEdicao = null; mostrarEditor = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Novo grupo")
            }
        },
    ) { padding ->
        if (grupos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                ) {
                    Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Folder, contentDescription = null, tint = Color(0xFF2563EB))
                        Text("Nenhum grupo criado", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                        Text(
                            "Crie um grupo para organizar as facas que pertencem ao mesmo modelo.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        TextButton(onClick = { grupoEmEdicao = null; mostrarEditor = true }) {
                            Text("Criar primeiro grupo")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text(
                        "Use os grupos para reunir todos os blocos de um mesmo modelo.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                items(grupos, key = { it.id }) { grupo ->
                    val quantidade = facas.count { it.grupoModeloId == grupo.id }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            grupoEmEdicao = grupo
                            mostrarEditor = true
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Folder, contentDescription = null, tint = Color(0xFF2563EB))
                            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                                Text(grupo.nome, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (quantidade == 1) "1 bloco vinculado" else "$quantidade blocos vinculados",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                )
                                grupo.descricao?.let {
                                    Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = { grupoParaExcluir = grupo }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Excluir grupo", tint = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarEditor) {
        EditorGrupoDialog(
            grupo = grupoEmEdicao,
            repository = repository,
            onDismiss = { mostrarEditor = false },
        )
    }

    grupoParaExcluir?.let { grupo ->
        val quantidade = facas.count { it.grupoModeloId == grupo.id }
        AlertDialog(
            onDismissRequest = { grupoParaExcluir = null },
            title = { Text("Excluir grupo") },
            text = {
                Text(
                    if (quantidade == 0) {
                        "Deseja excluir o grupo \"${grupo.nome}\"?"
                    } else {
                        "O grupo \"${grupo.nome}\" possui $quantidade bloco(s). Eles continuarão cadastrados, mas ficarão sem grupo."
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.excluirGrupoModelo(grupo.id) }
                    grupoParaExcluir = null
                }) { Text("Excluir", color = Color(0xFFDC2626)) }
            },
            dismissButton = { TextButton(onClick = { grupoParaExcluir = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun EditorGrupoDialog(
    grupo: GrupoModeloEntity?,
    repository: FacaMoldeRepository,
    onDismiss: () -> Unit,
) {
    var nome by remember(grupo) { mutableStateOf(grupo?.nome.orEmpty()) }
    var descricao by remember(grupo) { mutableStateOf(grupo?.descricao.orEmpty()) }
    var salvando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (grupo == null) "Novo grupo de modelos" else "Editar grupo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Dê um nome que identifique o modelo ao qual os blocos pertencem.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it; erro = null },
                    label = { Text("Nome do grupo *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição (opcional)") },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                erro?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = nome.isNotBlank() && !salvando,
                onClick = {
                    salvando = true
                    scope.launch {
                        try {
                            repository.salvarGrupoModelo(grupo?.id, nome, descricao)
                            onDismiss()
                        } catch (e: GrupoModeloNomeDuplicadoException) {
                            erro = e.message
                        } catch (e: Exception) {
                            erro = "Não foi possível salvar: ${e.message}"
                        } finally {
                            salvando = false
                        }
                    }
                },
            ) { Text(if (salvando) "Salvando..." else "Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
