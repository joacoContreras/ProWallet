"""
Script para procesar el dataset real del SEPA y generar productos_sepa.json
Uso:
  1. Descargá cualquier ZIP de https://www.datos.gob.ar/dataset/produccion-precios-claros---base-sepa
  2. Descomprimilo (obtendrás un .csv o carpeta con CSVs)
  3. Corré: python generar_json_sepa.py ruta/al/archivo.csv
"""

import sys
import csv
import json
from collections import defaultdict

def procesar_sepa(ruta_csv: str, max_productos: int = 250):
    precios_por_producto = defaultdict(list)

    with open(ruta_csv, encoding="utf-8", errors="replace") as f:
        # El separador del SEPA es pipe |
        reader = csv.reader(f, delimiter="|")
        for fila in reader:
            try:
                # Columnas típicas SEPA: id_comercio|id_bandera|id_sucursal|id_producto|productos_descripcion|precio
                # O también: id|codigo_barras|?|nombre|cantidad|precio
                if len(fila) < 4:
                    continue
                nombre = fila[3].strip() if len(fila) > 3 else None
                precio_str = fila[-1].strip()  # El precio suele ser la última columna
                if not nombre or not precio_str:
                    continue
                precio = float(precio_str.replace(",", "."))
                if precio > 0:
                    precios_por_producto[nombre].append(precio)
            except (ValueError, IndexError):
                continue

    # Calcular promedio por producto
    productos = []
    for nombre, precios in precios_por_producto.items():
        if len(precios) >= 2:  # Al menos 2 comercios para que el promedio tenga sentido
            promedio = round(sum(precios) / len(precios), 2)
            productos.append({
                "nombre": nombre,
                "descripcion": "",
                "precio_promedio": promedio,
                "_count": len(precios)
            })

    # Ordenar por cantidad de registros (más representativos primero) y tomar los mejores
    top_raw = sorted(productos, key=lambda x: x["_count"], reverse=True)[:max_productos]
    top = [{k: v for k, v in p.items() if k != "_count"} for p in top_raw]

    resultado = {"productos": top}
    with open("productos_sepa.json", "w", encoding="utf-8") as f:
        json.dump(resultado, f, ensure_ascii=False, indent=2)

    print(f"✓ Generados {len(top)} productos en productos_sepa.json")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python generar_json_sepa.py ruta/al/archivo.csv")
        sys.exit(1)
    procesar_sepa(sys.argv[1])
