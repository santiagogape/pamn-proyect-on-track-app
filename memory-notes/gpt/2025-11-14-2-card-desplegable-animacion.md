Perfecto 🔥 — si el rectángulo será un **item de lista** que **se expande al pulsarlo**, entonces **`Card` de Material 3** es *la mejor opción*, porque:

* maneja elevación animada automáticamente
* integra bien `clickable` + `interactionSource`
* respeta el theming
* puedes combinarlo con `animateContentSize()` para expandir/colapsar suavemente

Aquí te doy un **patrón completo y moderno** para un “expandable card list item”.

---

# ✅ Ejemplo completo: **ExpandableCardItem**

Este composable:

* muestra una card flotante estilo la imagen que enviaste
* se expande al tocarla
* retrae si se vuelve a tocar
* ajusta la altura **automáticamente**
* tiene animación de tamaño suavizada
* maneja elevación cambiando al presionar

```kotlin
@Composable
fun ExpandableCardItem(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .animateContentSize(), // ⭐ animación automática de expansión
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (expanded) 8.dp else 4.dp
        ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {

            // Título siempre visible
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            // Espacio cuando expandido
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
```

---

# 🧠 ¿Qué hace cada parte?

### 👉 `var expanded by remember { mutableStateOf(false) }`

Estado local: controla si la card está abierta o cerrada.

### 👉 `Card(onClick = { expanded = !expanded })`

Card como elemento clicable.

### 👉 `animateContentSize()`

Animación automática al cambiar el tamaño del contenido interno (`Column`).

✨ **No necesitas animaciones manuales**, Material 3 maneja el rebote y el suavizado.

### 👉 Elevación animada

```kotlin
defaultElevation = if (expanded) 8.dp else 4.dp
```

Cuando se expande, parece que “sube un poco”.

### 👉 Contenido condicional

```kotlin
if (expanded) {
    // contenido extra
}
```

---

# 📌 Ejemplo de uso dentro de una lista (`LazyColumn`)

```kotlin
LazyColumn {
    items(listItems) { item ->
        ExpandableCardItem(
            title = item.title,
            content = item.description
        )
    }
}
```

---

# 🎨 ¿Quieres una versión visual más cercana a tu captura?

Si quieres que el borde sea **más redondeado**, **sombra más suave** o **color de fondo rosado**, puedo ajustar:

* shape (`RoundedCornerShape(20.dp)` o más)
* elevation
* container color
* bordes
* animaciones de alpha o icono que rota (“chevron expand/collapse”)

¿Quieres eso?
