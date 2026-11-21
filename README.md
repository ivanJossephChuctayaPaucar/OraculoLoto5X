# Oráculo Loto 5X (v3.0.1)

> **Aplicación Android Nativa de Predicción Hermética y Estadística para La Tinka (Perú)**  
> **Versión:** 3.0.1 (Build 8) · **Framework:** Kotlin + Jetpack Compose Material 3  
> **Operador:** Iván Josseph Chuctaya Páucar (Sicuani, Cusco, Perú)  
> **Repositorio Oficial:** [ivanJossephChuctayaPaucar/OraculoLoto5X](https://github.com/ivanJossephChuctayaPaucar/OraculoLoto5X)

---

## 🌟 Novedades de la Versión 3.0.1

* **Seguridad y Privacidad de API Key:** Eliminada la clave de Google AI Studio embebida en el código fuente (`DEFAULT_KEY_B64`). Ahora el usuario debe ingresar su propia clave gratuita desde el formulario de configuración de la app (obtenible en [Google AI Studio](https://aistudio.google.com/)). Esto evita exposición de credenciales y uso indebido de cuota ajena.
* **Modelos de Gemini Oficiales:** Conectividad optimizada con `gemini-3.5-flash` como modelo principal (alta velocidad y respuesta en ~2s), con respaldo automático hacia `gemini-flash-latest`, `gemini-2.0-flash` y `gemini-1.5-flash` para garantizar compatibilidad total sin interrupciones.
* **Motor de Estadísticas Offline (`StatisticsEngine.kt`):** Nueva clase que analiza los sorteos históricos guardados en SQLite y calcula: frecuencia por bolilla (1-53), top 5 calientes, top 5 fríos, top 5 sobrepasados (más días sin salir), paridad par/impar, campana de Gauss (suma mínima, máxima, promedio, mediana), distribución por décadas y cooldown del último sorteo. Estos datos se inyectan como ajustes de peso en `LotteryFusionEngine` para la generación de jugadas Monte Carlo.
* **Integración de Pesos Estadísticos en `LotteryFusionEngine`:** El constructor acepta ahora `weightAdjustments: Map<Int, Double>` que multiplican los pesos base de cada bolilla en la simulación de 10,000 iteraciones, favoreciendo calientes y sobrepasados, y enfriando las bolillas del sorteo inmediato anterior.
* **Corrección del Sorteo Oficial 18/11/2026:** Actualizadas las bolillas en `data/tinka_history.json` con los números oficiales de latinka.com.pe: `[1, 4, 8, 24, 41, 42]`.
* **Script `fetch_tinka.py` Arreglado:** El script de sincronización automática ahora parsea correctamente el HTML de `tinkaresultados.com` (y `latinka.com.pe` como fuente oficial) para extraer fecha y 6 bolillas. Solo inserta sorteos nuevos (sin duplicados) y valida que sean 6 números únicos entre 1 y 53.
* **Perfil de Usuario Sin Datos Hardcodeados:** `UserProfile` ya no trae nombre, fecha de nacimiento ni coordenadas personales por defecto. Los valores son neutros (`fullName=""`, `lat=0.0`, `lon=0.0`, `birthDate=2000-01-01`) y se completan desde el formulario de perfil de la app.
* **Diagnóstico Preciso de Errores de IA:** Mensajes claros ante timeout, falta de conexión, cuota 429 y clave inválida o restringida (HTTP 400/403).
* **Auto-Sincronización de La Tinka:** Workflow de GitHub Actions que corre cada miércoles y domingo a las 23:15 hora Perú (`cron: '15 4 * * 1,4'`) para registrar los sorteos oficiales en `data/tinka_history.json`. La aplicación Android descarga este JSON (~2 KB) y aplica automáticamente el filtro de enfriamiento (-15.0 pts al último sorteo) y backtesting.
* **Pantalla Única Minimalista:** Navegación unificada que elimina la barra inferior de pestañas y centra la experiencia en un asistente conversacional fluido.
* **Hero Card Auto-Colapsable:** Muestra las 6 bolillas más probables de La Tinka, semáforo del día, fase lunar y hora planetaria en Cusco. Se contrae automáticamente a 1 sola línea al escribir o hacer scroll, dejando libre más del 80% de la pantalla para el chat.
* **RAG Local con Citas PDF Clickeables:** Genera chips interactivos de libros (`📖 Cheiro · Pág. 82`, `📖 Millman · Pág. 144`) con lista blanca estricta anti-alucinaciones, permitiendo abrir la página exacta del tratado en el visor nativo sin consumir internet.

---

## 🔄 Pipeline CI/CD, Compilación y Distribución Continua

El proyecto cuenta con un flujo completamente automatizado en **GitHub Actions** configurado en [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml), diseñado para compilar la aplicación en la nube sin requerir la instalación local del Android SDK.

```
       [Código Fuente] 
              │
         git push main
              ▼
   ┌──────────────────────────────────────────┐
   │ GitHub Actions: ubuntu-latest            │
   │ ├─ Setup JDK 17 (Temurin + Native Cache) │
   │ ├─ Gradle Daemonless Multi-Core (4GB RAM)│
   │ ├─ ./gradlew assembleDebug --parallel    │
   │ └─ Build Cache & Desugar Android         │
   └──────────────────┬───────────────────────┘
                      │
           ┌──────────┴──────────┐
           ▼                     ▼
┌─────────────────────┐ ┌──────────────────────────────────────┐
│  GitHub Artifacts   │ │  GitHub Releases (tag: latest)       │
│  (OraculoLoto5X-    │ │  app-debug.apk                       │
│   Debug-APK.zip)    │ │  (Enlace directo 1-Click sin .zip)   │
└─────────────────────┘ └──────────────────┬───────────────────┘
                                           │
                                  Descarga Automática
                                  o Navegador Móvil
                                           ▼
                            [Celular Android / PC]
```

### 1. Optimizaciones de Velocidad del Pipeline
* **Caché Nativa de Dependencias:** Uso de `actions/setup-java@v4` con parámetro `cache: 'gradle'`, eliminando intermediarios lentos o inestables de Develocity y reduciendo el tiempo de preparación de dependencias de minutos a pocos segundos.
* **Asignación de Memoria y Recolección Paralela:** Ajuste en `gradle.properties` a `-Xmx4096m -XX:+UseParallelGC`, permitiendo que el compilador de Kotlin y el dexer utilicen plenamente los recursos de la máquina virtual de GitHub Actions.
* **Compilación en Paralelo y Build Cache:** Ejecución bajo `./gradlew assembleDebug --no-daemon --parallel --build-cache` para procesar tareas de recursos, manifiesto y compilación Kotlin de forma simultánea.
* **Compresión Rápida de Artefactos:** `compression-level: 1` en `upload-artifact@v4` para evitar demoras innecesarias al empaquetar un archivo `.apk` que ya contiene compresión interna.

### 2. Descarga Directa del APK (1-Click en Celular)
En cada push a la rama `main`, el pipeline actualiza automáticamente el release `latest`. Esto genera una **URL pública directa y permanente** para descargar el archivo instalable sin necesidad de iniciar sesión en GitHub ni descomprimir archivos `.zip`:

🔗 **Enlace directo al instalador:**  
[Descargar OraculoLoto5X APK (Última Versión)](https://github.com/ivanJossephChuctayaPaucar/OraculoLoto5X/releases/download/latest/app-debug.apk)

### 3. Script Local de Automatización (`descargar_apk.ps1`)
El repositorio incluye el script [`descargar_apk.ps1`](descargar_apk.ps1) para desarrolladores en Windows. Al ejecutarlo en PowerShell:
1. Detecta automáticamente la última ejecución en curso del workflow en GitHub.
2. Monitorea el estado en tiempo real hasta que la compilación termine con éxito.
3. Descarga el APK compilado y lo copia automáticamente a:
   * Carpeta del proyecto: `C:\Proyectos\OraculoLoto5X\OraculoLoto5X-v3.0.1.apk`
   * Carpeta de Descargas de Windows: `C:\Users\Ivan Josseph\Downloads\OraculoLoto5X-v3.0.1.apk`

```powershell
# Ejecutar desde PowerShell en la carpeta del proyecto
powershell -ExecutionPolicy Bypass -File .\descargar_apk.ps1
```

---

## 🏛️ Motor Hermético y Matemático

### 1. Simulaciones Monte Carlo (10,000 iteraciones)
Con filtrado por campana de Gauss histórica ( \le \text{suma} \le 195$), anti-consecutivos (3 o más bolillas seguidas descartadas), paridad equilibrada y distribución por decenas.

### 2. Motor de Estadísticas Offline (StatisticsEngine.kt)
Analiza los sorteos históricos locales y genera:

| Métrica | Descripción |
|---------|-------------|
| **Frecuencia por bolilla** (1-53) | Veces que salió cada número en el historial |
| **Calientes** | Top 5 bolillas más frecuentes |
| **Fríos** | Top 5 bolillas menos frecuentes (≥1 aparición) |
| **Sobrepasados** | Top 5 bolillas con más días sin salir (candidatos "deuda") |
| **Paridad** | % par vs % impar en el histórico |
| **Suma (Gauss)** | Mín, máx, promedio, mediana y suma más común |
| **Décadas** | Distribución por rangos (1-10, 11-20, ..., 51-53) |
| **Cooldown** | Bolillas del último sorteo (penalización -15.0 pts) |

Estos ajustes se aplican como multiplicadores de peso en el motor de generación de jugadas.

### 3. Perfil Astronómico Natal
- Ubicación: Sicuani, Cusco ($-14.2667, -71.2333$).
- Hora Sideral Natal: \text{h } 01\text{m}$ (Ascendente ^\circ$ Tauro).
- Casa 5 (*Agathe Tuche*): Regida por Mercurio (bolilla 5) y Venus (bolilla 6).
- Fase Lunar Natal: Cuarto Creciente (50% de iluminación).

### 4. Los 5 Tratados Herméticos (embebidos en ssets/books/)
- **Cheiro (Book of Numbers):** Serie 1-4, números compuestos afortunados (10 al 52, con énfasis en 19, 23, 37) y reglas de azar del Cap. XXX.
- **Dan Millman (The Life You Were Born to Live):** Sendero de Vida 37/10 y leyes de manifestación cósmica.
- **Dr. David Phillips (The Complete Book of Numerology):** Cuadrícula Pitagórica  \times 3$, Flecha de la Frustración 4-5-6 and flechas de fuerza.
- **W. W. Westcott (Numbers: Their Occult Power and Mystic Virtues):** Números perfectos (6, 28) and cálculo de horas planetarias en orden caldeo.
- **Glynis McCants (Glynis Has Your Number):** Tríada de afinidad natural 1-5-7 and Lucky Lotto Index.

## 📱 Instalación en Android

1. Descarga el APK desde el enlace directo en tu celular.
2. Abre la notificación o el explorador de archivos y pulsa sobre `app-debug.apk` (o `OraculoLoto5X-v3.0.1.apk`).
3. Si el sistema lo solicita, autoriza *"Instalar aplicaciones de fuentes desconocidas"*.
4. Abre la app. Al iniciar, verificará la base de datos local SQLite y cargará de inmediato la radiografía hermética del día.

---

## 🔑 Configuración Inicial Obligatoria

Antes de usar la función de chat con IA:

1. Obtén una clave gratuita en [Google AI Studio](https://aistudio.google.com/).
2. Abre la app y ve al menú de configuración (Perfil y coordenadas).
3. Ingresa tu API Key en el campo correspondiente y selecciona un modelo (recomendado: `gemini-3.5-flash`).
4. Guarda los cambios. La app usará tu clave guardada localmente en `SharedPreferences` (nunca se envía al repositorio).

Si no configuras una clave, el chat de IA estará deshabilitado pero el motor hermético y estadístico seguirá funcionando 100% offline.

