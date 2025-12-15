En Kotlin, un **sealed class** y un **sealed interface** sirven para modelar **jerarquías cerradas de tipos**.
Son *la herramienta clave* para **modelar estados, resultados y eventos de forma segura**, y encajan **perfecto** con ViewModels, reducers, y flujos como el que tienes (auth, sync, UI).

Voy paso a paso, de simple a profundo, con ejemplos **muy conectados a tu proyecto**.

---

# 🧩 ¿Qué significa “sealed”?

> **“Sealed” = sellado**
> El compilador **conoce TODAS las subclases posibles**.

Eso permite:

* `when` **exhaustivo** (sin `else`)
* Seguridad en tiempo de compilación
* Modelar *estados reales*, no flags sueltos

---

# 1️⃣ Sealed class (clásico)

```kotlin
sealed class AuthResult {
    object Success : AuthResult()
    object Cancelled : AuthResult()
    data class Error(val throwable: Throwable) : AuthResult()
}
```

### Qué es cada cosa

| Elemento     | Qué representa              |
| ------------ | --------------------------- |
| `AuthResult` | El **tipo raíz**            |
| `object`     | Un estado único (singleton) |
| `data class` | Un estado con datos         |

---

## Uso correcto con `when`

```kotlin
when (result) {
    AuthResult.Success -> showHome()
    AuthResult.Cancelled -> showLogin()
    is AuthResult.Error -> showError(result.throwable)
}
```

✔ **NO necesitas `else`**
✔ Si agregas un nuevo estado → **el compilador te avisa**

---

# 2️⃣ Sealed interface (moderno, recomendado)

Desde Kotlin 1.5+, muchas veces es **mejor que sealed class**.

```kotlin
sealed interface AuthResult

object AuthSuccess : AuthResult
object AuthCancelled : AuthResult
data class AuthError(val cause: Throwable) : AuthResult
```

### Diferencias clave

| Sealed class         | Sealed interface             |
| -------------------- | ---------------------------- |
| Solo herencia simple | Permite herencia múltiple    |
| Puede tener estado   | No tiene constructor         |
| Más “OOP”            | Más “algebraico / funcional” |

👉 Para **resultados y estados** → sealed interface suele ser mejor

---

# 3️⃣ ¿Por qué NO usar Boolean / enum?

### ❌ Boolean (malo)

```kotlin
fun signIn(): Boolean
```

Problemas:

* ¿`false` es error?
* ¿cancelación?
* ¿sin conexión?

---

### ❌ Enum (limitado)

```kotlin
enum class AuthState {
    SUCCESS, ERROR
}
```

No puede transportar datos (`Throwable`, mensaje, etc.)

---

### ✅ Sealed (correcto)

```kotlin
sealed interface AuthResult {
    object Success : AuthResult
    object Cancelled : AuthResult
    data class Error(val reason: Throwable) : AuthResult
}
```

✔ Tipado fuerte
✔ Expresivo
✔ Extensible sin romper código

---

# 4️⃣ Ejemplo aplicado a TU `GoogleAuthClient`

### Antes

```kotlin
suspend fun signIn(activityContext: Context): Boolean
```

---

### Después (mejor)

```kotlin
sealed interface AuthResult {
    object Success : AuthResult
    object Cancelled : AuthResult
    data class Error(val cause: Throwable) : AuthResult
}
```

```kotlin
suspend fun signIn(activityContext: Context): AuthResult {
    return try {
        ...
        AuthResult.Success
    } catch (e: CancellationException) {
        AuthResult.Cancelled
    } catch (e: Exception) {
        AuthResult.Error(e)
    }
}
```

---

## Uso en ViewModel

```kotlin
viewModelScope.launch {
    when (val result = authClient.signIn(context)) {
        AuthResult.Success -> {
            authClient.ensureUserExists()
            startSync()
        }
        AuthResult.Cancelled -> showLoginAgain()
        is AuthResult.Error -> showError(result.cause)
    }
}
```

---

# 5️⃣ Sealed + StateFlow (patrón ideal)

Muy alineado con lo que ya usas (`StateFlow`, `SyncEngine`).

```kotlin
sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Authenticated : AuthUiState
    data class Error(val message: String) : AuthUiState
}
```

```kotlin
private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
val state: StateFlow<AuthUiState> = _state
```

---

# 6️⃣ Sealed y arquitectura (regla mental)

Usa **sealed** cuando:

✔ Hay **un número finito de estados**
✔ Cada estado **significa algo distinto**
✔ No quieres `null`, `Boolean`, `else`

Ejemplos ideales:

* AuthResult
* SyncState
* UiState
* NetworkResult
* LoadResult<T>

---

# 7️⃣ Sealed class vs sealed interface — decisión rápida

| Caso                       | Usa              |
| -------------------------- | ---------------- |
| Estados / Resultados       | sealed interface |
| Jerarquía con lógica común | sealed class     |
| Necesitas constructor      | sealed class     |
| Modelado puro              | sealed interface |

---

# 🧠 Conexión con tu SyncEngine

Tu diseño ya es muy “sealed-friendly”:

* `SynchronizationState` → podría ser sealed
* `AuthResult` → sealed
* `SyncPhase` (Idle / Pull / Push / Conflict) → sealed
* `ReferenceResolutionResult` → sealed

👉 Te daría **menos bugs y más garantías en compile-time**

---

## 🔚 Resumen

* **sealed** = jerarquía cerrada
* Permite `when` exhaustivo
* Reemplaza `Boolean`, `enum`, `null`
* Es clave para arquitecturas reactivas modernas
* Encaja perfecto con tu proyecto actual

Si quieres, el siguiente paso lógico sería:

* Diseñar **AuthUiState sealed**
* Diseñar **SyncState sealed**
* O refactorizar `SynchronizationState` a sealed + data

Dime cuál y lo hacemos paso a paso 👌
