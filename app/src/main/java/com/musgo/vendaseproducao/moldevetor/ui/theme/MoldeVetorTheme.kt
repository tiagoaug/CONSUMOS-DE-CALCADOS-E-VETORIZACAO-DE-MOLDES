package com.musgo.vendaseproducao.moldevetor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Mesma paleta neutra em cinza (slate) do Print Studio (PrintStudioTheme.kt) — consistência
// visual entre os módulos nativos do app, nenhum dos dois usa o roxo/lilás padrão do Material3.
private val AppColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF0F766E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0FDFA),
    onSecondaryContainer = Color(0xFF134E4A),
    background = Color.White,
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp),
)

@Composable
fun MoldeVetorTheme(content: @Composable () -> Unit) {
    // O editor usa uma interface clara fixa: superfícies brancas, cinzas neutros e azul como
    // destaque. Isso evita que o tema do sistema reintroduza fundos escuros ou tons lilás.
    MaterialTheme(colorScheme = AppColors, shapes = AppShapes, content = content)
}
