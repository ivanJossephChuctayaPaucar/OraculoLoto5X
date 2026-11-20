# Oráculo Loto 5X (v3.0.0)

> **Aplicación Android Nativa de Predicción Hermética y Estadística para La Tinka (Perú)**  
> **Versión:** 3.0.0 (Build 7) · **Framework:** Kotlin + Jetpack Compose Material 3  
> **Operador:** Iván Josseph Chuctaya Páucar (Sicuani, Cusco, Perú)  

---

## 🌟 Novedades de la Versión 3.0.0

* **Pantalla Única Minimalista:** Navegación unificada que elimina la barra inferior de pestañas y centra la experiencia en un asistente conversacional fluido.
* **Hero Card Auto-Colapsable:** Muestra las 6 bolillas más probables de La Tinka, semáforo del día, fase lunar y hora planetaria en Cusco. Se contrae automáticamente a 1 sola línea al escribir o hacer scroll, dejando libre más del 80% de la pantalla para el chat.
* **Chatbot Hermético Multi-Turno:** Conectado a Google AI Studio (`gemini-2.5-flash` con respaldo automático a `gemini-3.8-flash` y `gemini-2.5-pro`) con ventana deslizante de memoria (últimos 6 mensajes).
* **RAG Local con Citas PDF Clickeables:** Genera chips interactivos de libros (`📖 Cheiro · Pág. 82`, `📖 Millman · Pág. 144`) con lista blanca estricta anti-alucinaciones, permitiendo abrir la página exacta del tratado en el visor nativo sin consumir internet.
* **Auto-Sincronización de La Tinka:** Workflow de GitHub Actions que corre cada miércoles y domingo a las 23:15 hora Perú (`cron: '15 4 * * 1,4'`) para registrar los sorteos oficiales en `data/tinka_history.json`. La aplicación Android descarga este JSON (~2 KB) y aplica automáticamente el filtro de enfriamiento (-15.0 pts al último sorteo) y backtesting.

---

## 🏛️ Motor Hermético y Matemático

1. **Simulaciones Monte Carlo (10,000 iteraciones)** con filtrado por campana de Gauss histórica ($130 \le \text{suma} \le 195$).
2. **Perfil Astronómico Natal:**
   - Ubicación: Sicuani, Cusco ($-14.2667, -71.2333$).
   - Hora Sideral Natal: $22\text{h } 01\text{m}$ (Ascendente $27^\circ$ Tauro).
   - Casa 5 (*Agathe Tuche*): Regida por Mercurio (bolilla 5) y Venus (bolilla 6).
   - Fase Lunar Natal: Cuarto Creciente (50% de iluminación).
3. **Los 5 Tratados Herméticos (embebidos en `assets/books/`):**
   - **Cheiro:** Serie 1-4, números compuestos (19, 23, 37) y Cap. XXX de azar y apuestas.
   - **Dan Millman:** Sendero de Vida 37/10 y leyes de la manifestación.
   - **Dr. David Phillips:** Cuadrícula Pitagórica $3 \times 3$, Flecha 4-5-6 y el 7 aislado.
   - **W. W. Westcott:** Números perfectos (6, 28) y horas planetarias.
   - **Glynis McCants:** Tríada de afinidad natural 1-5-7 y Lucky Lotto Index.

---

## 🚀 Compilación y Descargas

* El proyecto compila automáticamente mediante GitHub Actions en cada commit y tag:
  - **APK Debug oficial:** Disponible en la sección [Releases](https://github.com/ivanJossephChuctayaPaucar/OraculoLoto5X/releases/tag/v3.0.0).
