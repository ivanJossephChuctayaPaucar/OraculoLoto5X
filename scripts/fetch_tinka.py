#!/usr/bin/env python3
"""
fetch_tinka.py
Script ligero para obtener el último resultado oficial de La Tinka (Perú).
Se ejecuta periódicamente en GitHub Actions y actualiza data/tinka_history.json.
"""

import json
import os
import re
import sys
import urllib.request
from datetime import datetime

DATA_PATH = os.path.join(os.path.dirname(__file__), "..", "data", "tinka_history.json")

# Fuentes públicas para consultar resultados de La Tinka (Perú)
# latinka.com.pe es la fuente oficial, se consulta primero
TINKA_URLS = [
    "https://www.latinka.com.pe/p/resultados-la-tinka.html",
    "https://www.tinkaresultados.com/",
    "https://www.intralot.com.pe/intralot/portal/resultados.html",
]

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Accept": "text/html,application/json,application/xhtml+xml",
}

DATE_PATTERNS = [
    r"(\d{1,2})/(\d{1,2})/(\d{4})",  # 18/11/2026
    r"(\d{4})-(\d{1,2})-(\d{1,2})",   # 2026-11-18
]

# Mapa de meses abreviados en español
MES_ES = {
    "ene": 1, "feb": 2, "mar": 3, "abr": 4, "may": 5, "jun": 6,
    "jul": 7, "ago": 8, "sep": 9, "oct": 10, "nov": 11, "dic": 12,
}


def normalize_date(day, month, year):
    """Convierte componentes de fecha a formato ISO YYYY-MM-DD."""
    return f"{int(year):04d}-{int(month):02d}-{int(day):02d}"


def parse_latinka_date(date_text, reference_year=None):
    """
    Convierte texto tipo '18 Nov' a YYYY-MM-DD.
    reference_year: año de referencia (por defecto el año actual).
    """
    if reference_year is None:
        reference_year = datetime.now().year

    match = re.match(r"\s*(\d{1,2})\s+([a-záéíóúñ]{3})\s*", date_text.strip(), re.IGNORECASE)
    if not match:
        return None

    day = int(match.group(1))
    mes_abbr = match.group(2).lower()[:3]
    month = MES_ES.get(mes_abbr)
    if month is None:
        return None

    return f"{reference_year:04d}-{month:02d}-{day:02d}"


def extract_draws_from_latinka_html(html, reference_year=None):
    """
    Extrae sorteos de la página oficial latinka.com.pe.
    Estructura esperada:
      <div class="last-results">
        <span class="text-results">RESULTADOS <span id="results-date-tinka">18 Nov</span></span>
        <span class="ball-results" id="results-tinka">
          <span class="result-ball"><span class="ball">41</span></span>
          ... (6 bolillas normales, opcionalmente una 7ª especial)
        </span>
    Retorna lista de dicts con 'date' y 'balls'.
    """
    draws = []

    # Buscar la fecha en <span id="results-date-tinka">18 Nov</span>
    date_match = re.search(
        r'id="results-date-tinka"\s*[^>]*>\s*([^<]+)\s*</span>',
        html,
        re.IGNORECASE,
    )
    if not date_match:
        return draws

    date_str = parse_latinka_date(date_match.group(1), reference_year)
    if date_str is None:
        return draws

    # Buscar el bloque de bolillas dentro de #results-tinka
    tinka_block_match = re.search(
        r'id="results-tinka"\s*[^>]*>(.*?)</span>\s*</span>',
        html,
        re.IGNORECASE | re.DOTALL,
    )
    if not tinka_block_match:
        # Intento alternativo sin el cierre de span anidado estricto
        tinka_block_match = re.search(
            r'id="results-tinka"[^>]*>(.*?)(?=</span>\s*</span>|$)',
            html,
            re.IGNORECASE | re.DOTALL,
        )

    if not tinka_block_match:
        return draws

    tinka_html = tinka_block_match.group(1)

    # Extraer bolillas normales (clase "ball", ignorando result-ball-special)
    ball_matches = re.findall(
        r'<span\s+class="ball">\s*(\d{1,2})\s*</span>',
        tinka_html,
    )

    balls = [int(b) for b in ball_matches]

    # Tomar solo las primeras 6 bolillas normales (ignorar boliyapa/special)
    if len(balls) >= 6:
        main_balls = balls[:6]
        if len(set(main_balls)) == 6 and all(1 <= b <= 53 for b in main_balls):
            draws.append({"date": date_str, "balls": sorted(main_balls)})

    return draws


def load_local_history():
    try:
        with open(DATA_PATH, "r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as e:
        print(f"Aviso: No se pudo leer {DATA_PATH}: {e}")
        return {"updated_at": datetime.now().strftime("%Y-%m-%d"), "lottery": "LA_TINKA", "draws": []}


def save_local_history(data):
    os.makedirs(os.path.dirname(DATA_PATH), exist_ok=True)
    with open(DATA_PATH, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(f"Sincronización guardada exitosamente en {DATA_PATH}")


def extract_draws_from_html(html, reference_year=None):
    """
    Intenta extraer sorteos de La Tinka desde HTML.
    Primero intenta el parser oficial de latinka.com.pe,
    luego los patrones genéricos para otras fuentes.
    Retorna una lista de dicts con keys 'date' y 'balls'.
    """
    draws = []

    # 1. Intento parser oficial de latinka.com.pe
    latinka_draws = extract_draws_from_latinka_html(html, reference_year)
    if latinka_draws:
        return latinka_draws

    # 2. Patrón para páginas tipo tinkaresultados.com
    pattern = re.compile(
        r"(?:Fecha|Sorteo|Tinka)\s*(?:\d+)?\s*[,:]?\s*(\d{1,2})/(\d{1,2})/(\d{4})[^\n]{0,200}?"
        r"Jugada\s*ganadora[^\n]{0,200}?\b(\d{1,2})\s+(\d{1,2})\s+(\d{1,2})\s+(\d{1,2})\s+(\d{1,2})\s+(\d{1,2})\b",
        re.IGNORECASE | re.DOTALL,
    )

    for m in pattern.finditer(html):
        day, month, year = m.group(1), m.group(2), m.group(3)
        balls = [int(m.group(i)) for i in range(4, 10)]
        date_str = normalize_date(day, month, year)

        if len(balls) == 6 and len(set(balls)) == 6 and all(1 <= b <= 53 for b in balls):
            draws.append({"date": date_str, "balls": sorted(balls)})

    # 3. Patrón genérico: fecha seguida de 6 números
    if not draws:
        generic = re.compile(
            r"(\d{1,2})/(\d{1,2})/(\d{4})[^\n]{0,300}?"
            r"\b(\d{1,2})\s+(\d{1,2})\s+(\d{1,2})\s+(\d{1,2})\s+(\d{1,2})\s+(\d{1,2})\b",
            re.DOTALL,
        )
        for m in generic.finditer(html):
            day, month, year = m.group(1), m.group(2), m.group(3)
            balls = [int(m.group(i)) for i in range(4, 10)]
            date_str = normalize_date(day, month, year)

            if len(balls) == 6 and len(set(balls)) == 6 and all(1 <= b <= 53 for b in balls):
                draws.append({"date": date_str, "balls": sorted(balls)})

    return draws


def fetch_tinka_history():
    """
    Consulta fuentes públicas y retorna los sorteos más recientes encontrados.
    """
    all_draws = []
    reference_year = datetime.now().year

    for url in TINKA_URLS:
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=15) as response:
                html = response.read().decode("utf-8", errors="ignore")
                print(f"Respuesta recibida de {url} ({len(html)} bytes)")

                page_draws = extract_draws_from_html(html, reference_year)
                if page_draws:
                    print(f"Sorteos detectados en {url}: {len(page_draws)}")
                    all_draws.extend(page_draws)
                    break  # Si una fuente funciona, usamos sus datos
        except Exception as e:
            print(f"Aviso al consultar {url}: {e}")

    # Deduplicar por fecha, manteniendo la más reciente
    unique = {}
    for d in all_draws:
        date_str = d["date"]
        if date_str not in unique:
            unique[date_str] = d

    return sorted(unique.values(), key=lambda x: x["date"], reverse=True)


def main():
    history = load_local_history()
    existing_draws = history.get("draws", [])
    existing_dates = {d["date"] for d in existing_draws}
    latest_known_date = existing_draws[0]["date"] if existing_draws else "2000-01-01"

    print(f"Fecha del sorteo más reciente en local: {latest_known_date}")

    remote_draws = fetch_tinka_history()

    if not remote_draws:
        print("No se pudieron obtener sorteos remotos. Historial sin cambios.")
        return 0

    new_draws = [d for d in remote_draws if d["date"] not in existing_dates]

    if not new_draws:
        print("Historial al día. No se detectaron sorteos nuevos pendientes.")
        return 0

    # Insertar sorteos nuevos al principio (más recientes primero)
    for draw in new_draws:
        print(f"Nuevo sorteo detectado: {draw['date']} - Bolillas: {draw['balls']}")
        history["draws"].insert(0, draw)

    history["updated_at"] = datetime.now().strftime("%Y-%m-%d")
    save_local_history(history)
    return 0


if __name__ == "__main__":
    sys.exit(main())
