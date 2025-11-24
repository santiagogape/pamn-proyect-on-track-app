Perfecto, ya tienes lo básico del calendario.  
Ahora quieres **el siguiente paso: integración con tus ViewModels → mostrar tareas por fecha**, algo muy típico en apps de productividad.

Vamos a construir _una arquitectura final limpia_ para tu caso:

---

# ✔️ Arquitectura recomendada

**Tu pipeline debería ser:**

```
TasksViewModel (StateFlow<Map<LocalDate, List<Task>>>)
          ↓
    CalendarScreen(tasksByDate)
          ↓
       DayCell(tieneEvento = tasksByDate[date] != null)
          ↓
  onDayClick(date)  → navegar o abrir detalles
```

---

# ✔️ 1. El ViewModel debe exponer tareas por fecha

Supongamos que tienes tareas así:

```kotlin
data class Task(
    val id: String,
    val name: String,
    val date: LocalDate
)
```

Entonces en tu `TasksViewModel`:

```kotlin
class TasksViewModel : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    // Agrupado por fecha para el calendario
    val tasksByDate: StateFlow<Map<LocalDate, List<Task>>> =
        tasks.map { list ->
            list.groupBy { it.date }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyMap()
        )

    init {
        // MOCK
        _tasks.value = listOf(
            Task("1", "task1", LocalDate.now()),
            Task("2", "task2", LocalDate.now().plusDays(1)),
            Task("3", "task3", LocalDate.now().plusDays(2))
        )
    }
}
```

### ✔️ Ahora tu ViewModel produce:

- Todas las tareas
    
- Y también un mapa **fecha → lista de tareas**
    

Perfecto para el calendario.

---

# ✔️ 2. CalendarScreen recibe ese flujo

```kotlin
@Composable
fun CalendarScreen(
    viewModel: TasksViewModel = viewModel(),
    onDayClick: (LocalDate) -> Unit = {}
) {
    val tasksByDate by viewModel.tasksByDate.collectAsStateWithLifecycle()

    CalendarView(
        tasksByDate = tasksByDate,
        onDayClick = onDayClick
    )
}
```

---

# ✔️ 3. CalendarView (tu calendario) acepta tareas por fecha

Cambiamos el composable del calendario:

```kotlin
@Composable
fun CalendarView(
    tasksByDate: Map<LocalDate, List<Task>>,
    onDayClick: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()

    Column {
        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
        )

        Spacer(Modifier.height(8.dp))

        DaysOfWeekRow()

        Spacer(Modifier.height(8.dp))

        MonthGrid(
            month = currentMonth,
            today = today,
            tasksByDate = tasksByDate,
            onDayClick = onDayClick
        )
    }
}
```

---

# ✔️ 4. MonthGrid: detecta si un día tiene tareas

Nueva versión:

```kotlin
@Composable
fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstOfMonth = month.atDay(1)
    val shift = ((firstOfMonth.dayOfWeek.value - 1) + 7) % 7
    val daysInMonth = month.lengthOfMonth()

    val days: List<LocalDate?> = buildList {
        repeat(shift) { add(null) }
        repeat(daysInMonth) { day -> add(month.atDay(day + 1)) }
    }

    LazyVerticalGrid(columns = GridCells.Fixed(7)) {
        items(days) { date ->
            DayCell(
                date = date,
                hasEvents = date != null && !tasksByDate[date].isNullOrEmpty(),
                isToday = date == today,
                onClick = onDayClick
            )
        }
    }
}
```

---

# ✔️ 5. DayCell muestra un punto si hay tareas

Muy típico en calendarios.

```kotlin
@Composable
fun DayCell(
    date: LocalDate?,
    hasEvents: Boolean,
    isToday: Boolean,
    onClick: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = date != null) {
                date?.let(onClick)
            }
            .background(
                if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${date.dayOfMonth}")

                if (hasEvents) {
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}
```

Ahora cada celda:  
✔ muestra el número  
✔ marca con un puntito si hay tareas  
✔ resalta el día actual  
✔ es clickable

---

# ✔️ 6. Qué pasa cuando haces click en un día

Tú decides:

### → Mostrar lista de tareas del día

### → Abrir un Activity con detalles

### → Filtrar tu lista de ExpandableCards

Ejemplo para navegar dentro del NavHost:

```kotlin
onDayClick = { date ->
    navController.navigate("tasks/${date.toString()}")
}
```

---

# ⭐ Resultado: un calendario real, rápido, 100% Compose

- Control total de animación, estilo, colores
    
- Integrado con tus ViewModels y tus listas ExpandableCard
    
- Extensible a eventos complejos
    

---

## Si quieres, te puedo dar:

✔ versión con semanas expandibles  
✔ versión tipo Google Calendar (scroll vertical infinito por meses)  
✔ versión con selección de rango (para búsqueda)  
✔ versión con eventos coloreados (punto → varias tareas con colores)

Dime qué estilo quieres y te lo armo.