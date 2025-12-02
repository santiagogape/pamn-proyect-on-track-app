# prompt
>lo que me interesa son los xml relacionados con las vistas de la app. quiero usar Jetpack Compose, con material design de android y tener mas controls sobre la app desarrollandola declarativamente.
> antes que nada, analizalos para enteder la app, y hablame del ciclo de vida que tiene actualmente y los elementos de la arquitectura android que usa como Fragment, Activity, ...

¡Perfecto! Ya vi tu `build.gradle.kts`, el `AndroidManifest.xml`, los **Kotlin** y la **carpeta de recursos**. Te resumo cómo está armada la app hoy y qué ciclo de vida/arquitectura usa.

# Qué tienes hoy (estructura y arquitectura)

* **Single-Activity + Navigation Component.**
  `MainActivity` infla un layout con:

    * un `NavHostFragment` (el contenedor de destinos),
    * un `BottomNavigationView` con 3 secciones (Home, Dashboard, Notifications),
    * y configura `AppBarConfiguration` para que esas 3 secciones sean “top-level” (sin botón back).

* **Tres Fragments “vacíos” con ViewModel propio:**

    * `HomeFragment` + `HomeViewModel` (muestra un `TextView` observando un `LiveData<String>`).
    * `DashboardFragment` + `DashboardViewModel`.
    * `NotificationsFragment` + `NotificationsViewModel`.
      En todos, usas **ViewBinding** para inflar el XML del fragment y observar el `LiveData` con `viewLifecycleOwner`. Al destruir la vista hacen `_binding = null` para evitar memory leaks.

* **Recursos (res.zip):**

    * `layout/` con `activity_main.xml` (coordinator + toolbar/navhost + bottom nav) y 3 `fragment_*.xml` con un `TextView`.
    * `menu/bottom_nav_menu.xml` con 3 items.
    * `navigation/mobile_navigation.xml` con los 3 destinos que apuntan a tus Fragments.
    * `values/*` (themes, strings, etc.).
      *(Todo esto encaja con lo que configura `MainActivity` y lo que observan los Fragments).*

* **Dependencias y flags:**
  `compose = true` está activado, y tienes libs de **Compose + Material3**, pero **aún no se usan** en las pantallas (están hechas con XML + ViewBinding). También mantienes `viewBinding = true`.

# Ciclo de vida actual (qué pasa en runtime)

1. **Arranque (Activity):**

    * Android lanza `MainActivity` (declarada como `LAUNCHER` en el manifest).
    * `onCreate()` infla `ActivityMainBinding`, busca el `NavController` del `NavHostFragment`, configura la ActionBar con `AppBarConfiguration` y conecta el `BottomNavigationView` al `NavController`. Desde aquí, la navegación entre tabs la maneja el Navigation Component.

2. **Navegación a cada tab (Fragment Lifecycle + ViewBinding):**

    * En cada `Fragment`, `onCreateView()`:

        * infla `Fragment*Binding`,
        * obtiene el `*ViewModel` con `ViewModelProvider(this)`,
        * conecta el `TextView` a `LiveData` usando `observe(viewLifecycleOwner)`.
    * En `onDestroyView()` se limpia el binding (`_binding = null`).
      Esto significa que el **ciclo de vida de la vista** del fragment está bien separado del **ciclo de vida del fragment**: si la vista se destruye (p. ej. por navegación o por volver al tab), no filtras la referencia a la vista.

3. **ViewModel y LiveData (scopes):**

    * Cada `ViewModel` está **scopeado al Fragment** (usas `ViewModelProvider(this)`), por lo que:

        * Sobrevive a **cambios de configuración** *mientras el fragment siga en el back stack*.
        * **No** es compartido entre tabs ni con la Activity (no usas `activityViewModels()` ni `navGraphViewModels()`).
    * `LiveData` emite el texto y la UI se actualiza automáticamente al observar con `viewLifecycleOwner`.

4. **Back stack y barras:**

    * Al ser top-level destinations, la AppBar no muestra el botón “up” en los 3 tabs. La back stack la gestiona el Navigation Component según cambias de tab o navegas a destinos internos (que hoy no hay).

# Elementos de la arquitectura Android presentes

* **Activity (1)**: orquesta navegación y toolbar.
* **Fragments (3)**: una pantalla por tab, cada una con su propio ciclo de vista.
* **Navigation Component**: `NavHostFragment` + `NavController` + `Graph` XML + `BottomNavigationView` sincronizada.
* **MVVM ligero**: `Fragment` (View) + `ViewModel` + `LiveData`. Aún **no hay** repositorio, use-cases, ni capa de datos.
* **ViewBinding**: en Activity y Fragments.
* **Material Components** (tema XML actual).
* **Compose**: habilitado pero **no utilizado aún** en las pantallas.

# Implicaciones para migrar a Jetpack Compose (preview rápido)

No te cambio nada aún—solo te dejo claro el terreno para el paso siguiente:

* Puedes migrar **por etapas**:

    1. **Híbrido**: dentro de cada `Fragment` mete un `ComposeView` y empieza a reemplazar el layout XML por composables (sigue sirviéndote del `NavController` y del bottom nav existente).
    2. **Full Compose**: sustituye `activity_main.xml` por un `setContent { Scaffold(...) }`, usa **Navigation Compose** y `NavigationBar` (Material3) en vez de `BottomNavigationView`, y mueve cada pantalla a un `@Composable`.
* Tus `ViewModel` actuales funcionan igual con Compose (observas `LiveData` como `state` usando `collectAsStateWithLifecycle` si cambias a Flow o `observeAsState()` para LiveData).

Si quieres, en el siguiente paso te preparo:

* el **mapa 1:1** `fragment_*.xml` → `@Composable`,
* un **Scaffold + NavigationBar (M3)**,
* y la **navegación Compose** equivalente a tu `mobile_navigation.xml`.

¿Seguimos con la opción híbrida o te paso directamente la plantilla full Compose con Material3 y Navigation Compose?
