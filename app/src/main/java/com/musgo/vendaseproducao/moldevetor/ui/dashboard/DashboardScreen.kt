package com.musgo.vendaseproducao.moldevetor.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musgo.vendaseproducao.moldevetor.data.FacaMoldeRepository
import com.musgo.vendaseproducao.moldevetor.data.db.FacaMoldeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: FacaMoldeRepository,
    onAbrirBlocos: () -> Unit,
    onAbrirGrupos: () -> Unit,
    onNovaFaca: () -> Unit,
    onBack: () -> Unit,
) {
    val facas by repository.observeSummaries().collectAsState(initial = emptyList())
    val grupos by repository.observeGruposModelos().collectAsState(initial = emptyList())
    val ativas = facas.count { it.status == FacaMoldeEntity.STATUS_ATIVA }
    val vetorizadas = facas.count { it.contornoJson != null }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Fechar")
                    }
                },
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Button(
                    onClick = onNovaFaca,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Nova faca", modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Text("Visão geral", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Acompanhe seus blocos e organize as facas por modelo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricaCard(
                    titulo = "Blocos ativos",
                    valor = ativas.toString(),
                    icone = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                )
                MetricaCard(
                    titulo = "Vetorizados",
                    valor = vetorizadas.toString(),
                    icone = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricaCard(
                    titulo = "Grupos de modelos",
                    valor = grupos.size.toString(),
                    icone = { Icon(Icons.Filled.Folder, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                )
                MetricaCard(
                    titulo = "Total cadastrado",
                    valor = facas.size.toString(),
                    icone = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                "Acesso rápido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 26.dp, bottom = 10.dp),
            )
            MenuDashboardCard(
                titulo = "Blocos e facas",
                descricao = "Cadastre, vetorize, edite e exporte seus blocos.",
                icone = Icons.Filled.Dashboard,
                onClick = onAbrirBlocos,
            )
            Spacer(Modifier.height(10.dp))
            MenuDashboardCard(
                titulo = "Configurações",
                descricao = "Crie e organize os grupos de facas de modelos.",
                icone = Icons.Filled.Settings,
                onClick = onAbrirGrupos,
            )
        }
    }
}

@Composable
private fun MetricaCard(
    titulo: String,
    valor: String,
    icone: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides Color(0xFF2563EB),
                    content = icone,
                )
            }
            Text(valor, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
            Text(titulo, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MenuDashboardCard(
    titulo: String,
    descricao: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(Color(0xFFEFF6FF), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icone, contentDescription = null, tint = Color(0xFF2563EB))
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(titulo, fontWeight = FontWeight.SemiBold)
                Text(descricao, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF64748B))
        }
    }
}
