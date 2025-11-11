package com.example.on_track_app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// === Formas globales ===
// Se aplican a componentes por defecto como botones, tarjetas, menús, diálogos.
// Equivalente a shapeAppearance en temas XML.

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),   // Ej: botones pequeños, chips
    medium = RoundedCornerShape(8.dp),  // Ej: tarjetas, menús
    large = RoundedCornerShape(12.dp)   // Ej: diálogos, contenedores grandes
)
