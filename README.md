# Oráculo Loto 5X (v3.0.1)

> **Aplicación Android Nativa de Predicción Hermética y Estadística para La Tinka (Perú)**  
> **Versión:** 3.0.1 (Build 8) · **Framework:** Kotlin + Jetpack Compose Material 3  
> **Operador:** Iván Josseph Chuctaya Páucar (Sicuani, Cusco, Perú)  
> **Repositorio Oficial:** [ivanJossephChuctayaPaucar/OraculoLoto5X](https://github.com/ivanJossephChuctayaPaucar/OraculoLoto5X)

---

## 🌟 Novedades de la Versión 3.0.1

* **Corrección de Timeout y Optimización Thinking:** Conectado a Google AI Studio (`gemini-2.5-flash` principal con `thinkingBudget = 0` y respaldo a `gemini-2.0-flash`). Tiempos de espera ampliados a 20s de conexión y 45s de lectura para redes móviles.
* **Snapshot Completo de Telemetría (~2 KB):** Inyección en `system_instruction` de todo el contexto calculado: perfil natal de Iván, ciclos personales, semáforo, iluminación lunar, ventanas de Sol y Luna restantes hoy, radiografía completa de las 6 bolillas con puntajes y motivos por autor, métricas gaussianas, rueda de 9 bolillas, cooldown del último sorteo oficial, top 5 calientes/frías y backtesting local.
* **Diagnóstico Preciso de Errores:** Mensajes claros ante timeout, falta de conexión, cuota 429 y clave inválida o restringida (HTTP 400/403).
* **Lista Blanca Estricta de Citas PDF:** Corrección de citas de Glynis McCants a `[PDF:mccants:4]` y `[PDF:mccants:6]`.
* **Pantalla Única Minimalista:** Navegación unificada que elimina la barra inferior de pestañas y centra la experiencia en un asistente conversacional fluido.
* **Hero Card Auto-Colapsable:** Muestra las 6 bolillas más probables de La Tinka, semáforo del día, fase lunar y hora planetaria en Cusco. Se contrae automáticamente a 1 sola línea al escribir o hacer scroll, dejando libre más del 80% de la pantalla para el chat.
* **RAG Local con Citas PDF Clickeables:** Genera chips interactivos de libros (`📖 Cheiro · Pág. 82`, `📖 Millman · Pág. 144`) con lista blanca estricta anti-alucinaciones, permitiendo abrir la página exacta del tratado en el visor nativo sin consumir internet.
* **Auto-Sincronización de La Tinka:** Workflow de GitHub Actions que corre cada miércoles y domingo a las 23:15 hora Perú (`cron: '15 4 * * 1,4'`) para registrar los sorteos oficiales en `data/tinka_history.json`. La aplicación Android descarga este JSON (~2 KB) y aplica automáticamente el filtro de enfriamiento (-15.0 pts al último sorteo) y backtesting.

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

1. **Simulaciones Monte Carlo (10,000 iteraciones)** con filtrado por campana de Gauss histórica ($130 \le \text{suma} \le 195$).
2. **Perfil Astronómico Natal:**
   - Ubicación: Sicuani, Cusco ($-14.2667, -71.2333$).
   - Hora Sideral Natal: $22\text{h } 01\text{m}$ (Ascendente $27^\circ$ Tauro).
   - Casa 5 (*Agathe Tuche*): Regida por Mercurio (bolilla 5) y Venus (bolilla 6).
   - Fase Lunar Natal: Cuarto Creciente (50% de iluminación).
3. **Los 5 Tratados Herméticos (embebidos en `assets/books/`):**
   - **Cheiro (Book of Numbers):** Serie 1-4, números compuestos afortunados (10 al 52, con énfasis en 19, 23, 37) y reglas de azar del Cap. XXX.
   - **Dan Millman (The Life You Were Born to Live):** Sendero de Vida 37/10 y leyes de manifestación cósmica.
   - **Dr. David Phillips (The Complete Book of Numerology):** Cuadrícula Pitagórica $3 \times 3$, Flecha de la Frustración 4-5-6 y flechas de fuerza.
   - **W. W. Westcott (Numbers: Their Occult Power and Mystic Virtues):** Números perfectos (6, 28) y cálculo de horas planetarias en orden caldeo.
   - **Glynis McCants (Glynis Has Your Number):** Tríada de afinidad natural 1-5-7 y Lucky Lotto Index.

---

## 📱 Instalación en Android

1. Descarga el APK desde el enlace directo en tu celular.
2. Abre la notificación o el explorador de archivos y pulsa sobre `app-debug.apk` (o `OraculoLoto5X-v3.0.1.apk`).
3. Si el sistema lo solicita, autoriza *"Instalar aplicaciones de fuentes desconocidas"*.
4. Abre la app. Al iniciar, verificará la base de datos local SQLite y cargará de inmediato la radiografía hermética del día.
