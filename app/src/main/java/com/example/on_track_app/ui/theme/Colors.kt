package com.example.on_track_app.ui.theme
// app/src/main/java/com/example/on_track_app/ui/theme/Color.kt

import androidx.compose.ui.graphics.Color

// === Paleta base migrada desde res/values/colors.xml ===
// source:
//  purple_200 = #FFBB86FC
//  purple_500 = #FF6200EE
//  purple_700 = #FF3700B3
//  teal_200   = #FF03DAC5
//  teal_700   = #FF018786
//  black      = #FF000000
//  white      = #FFFFFFFF

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)

val Teal200   = Color(0xFF03DAC5)
val Teal700   = Color(0xFF018786)

val Black     = Color(0xFF000000)
val White     = Color(0xFFFFFFFF)

// (Opcional) Aliases para facilitar el paso a Material3 en Theme.kt
// Los usaremos como primary/secondary en esquemas claro/oscuro.
val PrimaryLight        = Purple500
val PrimaryLightVariant = Purple700
val SecondaryLight      = Teal200
val SecondaryLightVariant = Teal700
val OnPrimaryLight      = White
val OnSecondaryLight    = Black

val PrimaryDark         = Purple200
val PrimaryDarkVariant  = Purple700
val SecondaryDark       = Teal200
val SecondaryDarkVariant = Teal700
val OnPrimaryDark       = Black
val OnSecondaryDark     = Black
