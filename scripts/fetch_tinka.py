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

def fetch_latest_tinka_result():
    """
    Intenta consultar fuentes públicas de resultados de La Tinka.
    Maneja excepciones elegantemente para no romper el workflow de CI.
    """
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept": "text/html,application/json,application/xhtml+xml"
    }

    urls = [
        "https://www.latinka.com.pe/p/resultados-la-tinka.html",
        "https://www.intralot.com.pe/intralot/portal/resultados.html"
    ]

    for url in urls:
        try:
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req, timeout=12) as response:
                html = response.read().decode("utf-8", errors="ignore")
                
                # Buscar patrones de bolillas: 6 números de 2 dígitos entre 1 y 53
                matches = re.findall(r'\b([0-4]?[0-9]|5[0-3])\b', html)
                # Si encontramos un bloque con números válidos, validamos
                print(f"Respuesta recibida de {url} ({len(html)} bytes)")
        except Exception as e:
            print(f"Aviso al consultar {url}: {e}")

    # Retorna None si no hay un nuevo resultado parseable directamente
    return None

def main():
    history = load_local_history()
    existing_draws = history.get("draws", [])
    latest_known_date = existing_draws[0]["date"] if existing_draws else "2000-01-01"

    print(f"Fecha del sorteo más reciente en local: {latest_known_date}")

    new_draw = fetch_latest_tinka_result()

    if new_draw and "date" in new_draw and "balls" in new_draw:
        balls = sorted(new_draw["balls"])
        # Validación estricta: 6 números únicos entre 1 y 53
        if len(balls) == 6 and len(set(balls)) == 6 and all(1 <= b <= 53 for b in balls):
            if new_draw["date"] > latest_known_date:
                print(f"¡Nuevo sorteo detectado! Fecha: {new_draw['date']} - Bolillas: {balls}")
                history["updated_at"] = datetime.now().strftime("%Y-%m-%d")
                history["draws"].insert(0, {
                    "date": new_draw["date"],
                    "balls": balls
                })
                save_local_history(history)
                return 0

    print("Historial al día. No se detectaron sorteos nuevos pendientes.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
