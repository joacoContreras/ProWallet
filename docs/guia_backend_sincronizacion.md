# Guía de Implementación del Backend de Sincronización

La aplicación móvil de **ProWallet** ya está completamente configurada para realizar la sincronización asíncrona (offline-first) a través del archivo `SyncWorker.kt`. 

Para completar el flujo, ahora debes implementar el endpoint en tu servidor. Aquí tienes los detalles técnicos del contrato y la lógica que debe seguir el backend.

---

## 1. El Endpoint de Sincronización

*   **Ruta**: `POST /api/sync`
*   **Encabezados**: `Authorization: Bearer <token_de_usuario>` (opcional si identificas por el email del body, pero recomendado para producción).
*   **Cuerpo (JSON Request)**:
    ```json
    {
      "lastSyncTime": 1719323400000,
      "userEmail": "usuario@undef.edu.ar",
      "modifiedPurchases": [...],
      "modifiedCategories": [...],
      "modifiedAccounts": [...],
      "modifiedFixedExpenses": [...]
    }
    ```

---

## 2. Lógica que debe realizar el Servidor (Algoritmo de Sync)

Cuando el servidor recibe la petición, debe realizar los siguientes **tres pasos en una transacción de base de datos** para asegurar la consistencia:

### Paso A: Procesar Subidas (Upsert local -> remoto)
Para cada una de las listas recibidas (`modifiedPurchases`, `modifiedCategories`, `modifiedAccounts`, `modifiedFixedExpenses`):
1.  **Buscar el registro** en la base de datos del servidor por su `id` (filtrando por `user_email`).
2.  **Si el registro no existe**: Crearlo en la base de datos del servidor.
3.  **Si el registro existe**: Comparar el timestamp `updated_at`. 
    *   Si `body.updated_at > db.updated_at`, **sobreescribir** el registro en la base de datos con los datos que envió el celular (*Last-Write-Wins*).
    *   Si `is_deleted` viene en `true` (1), marcarlo como eliminado en la base de datos del servidor (puedes hacer borrado físico o mantener una columna lógica `is_deleted`).

### Paso B: Recopilar Bajadas (Download remoto -> local)
Para armar la respuesta, el servidor debe buscar todos los registros en su base de datos que:
1.  Pertenezcan al `user_email` especificado.
2.  Tengan un `updated_at` **mayor** al `lastSyncTime` enviado por el cliente.
3.  *Opcional*: Excluir los registros que acaban de ser actualizados/creados por el propio cliente en esta llamada (para no devolverle al celular los mismos datos que acaba de subir).

### Paso C: Responder al Cliente
El servidor debe obtener su marca de tiempo actual del reloj del sistema (en milisegundos Unix) y responder con estado `200 OK` y el siguiente JSON:
```json
{
  "serverTime": 1719325800000,
  "remotePurchases": [...],
  "remoteCategories": [...],
  "remoteAccounts": [...],
  "remoteFixedExpenses": [...]
}
```

---

## 3. Servidor de Pruebas (Mock Backend en Python)

Si quieres probar la sincronización de inmediato de manera local, puedes crear un archivo temporal en Python (por ejemplo, `mock_server.py`) y ejecutar un servidor FastAPI con persistencia en memoria.

### Código del Servidor Mock (`mock_server.py`):

```python
import time
from typing import List, Dict, Any
from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel

app = FastAPI(title="ProWallet Sync Backend Mock")

# Simulación de Base de Datos en memoria
DB: Dict[str, Dict[str, List[Dict[str, Any]]]] = {
    "purchases": [],
    "categories": [],
    "accounts": [],
    "fixed_expenses": []
}

class ProductDto(BaseModel):
    id: str
    code: str
    name: str
    description: str
    price: float

class PurchaseDto(BaseModel):
    id: str
    storeName: str
    date: str
    time: str
    totalAmount: float
    category: str
    products: List[ProductDto] = []
    ticketImageUri: str = None
    timestampMs: int = 0
    latitude: float = None
    longitude: float = None

# Modelos Room reflejados en JSON
class CategoryEntity(BaseModel):
    id: int
    name: str
    user_email: str
    updated_at: int
    is_dirty: bool
    is_deleted: bool

class AccountEntity(BaseModel):
    id: int
    name: str
    type: str
    last_four: str
    is_primary: bool
    user_email: str
    updated_at: int
    is_dirty: bool
    is_deleted: bool

class FixedExpenseEntity(BaseModel):
    id: int
    name: str
    amount: float
    category: str
    frequency: str
    user_email: str
    updated_at: int
    is_dirty: bool
    is_deleted: bool

class SyncRequest(BaseModel):
    lastSyncTime: int
    userEmail: str
    modifiedPurchases: List[PurchaseDto] = []
    modifiedCategories: List[CategoryEntity] = []
    modifiedAccounts: List[AccountEntity] = []
    modifiedFixedExpenses: List[FixedExpenseEntity] = []

@app.post("/api/sync")
def sync_data(request: SyncRequest, authorization: str = Header(None)):
    server_time = int(time.time() * 1000)
    email = request.userEmail
    
    # --- PROCESAR SUBIDAS (UPSERT) ---
    # Cuentas
    for remote in request.modifiedAccounts:
        existing = next((x for x in DB["accounts"] if x["id"] == remote.id and x["user_email"] == email), None)
        if not existing:
            DB["accounts"].append(remote.dict())
        elif remote.updated_at > existing["updated_at"]:
            DB["accounts"].remove(existing)
            DB["accounts"].append(remote.dict())

    # Categorías
    for remote in request.modifiedCategories:
        existing = next((x for x in DB["categories"] if x["id"] == remote.id and x["user_email"] == email), None)
        if not existing:
            DB["categories"].append(remote.dict())
        elif remote.updated_at > existing["updated_at"]:
            DB["categories"].remove(existing)
            DB["categories"].append(remote.dict())

    # Gastos Fijos
    for remote in request.modifiedFixedExpenses:
        existing = next((x for x in DB["fixed_expenses"] if x["id"] == remote.id and x["user_email"] == email), None)
        if not existing:
            DB["fixed_expenses"].append(remote.dict())
        elif remote.updated_at > existing["updated_at"]:
            DB["fixed_expenses"].remove(existing)
            DB["fixed_expenses"].append(remote.dict())

    # Compras (Domain models)
    for remote in request.modifiedPurchases:
        existing = next((x for x in DB["purchases"] if x["id"] == remote.id), None)
        # Nota: Usamos timestampMs como proxy de updated_at para compras en este ejemplo
        if not existing:
            # Añadimos email al diccionario
            data = remote.dict()
            data["user_email"] = email
            DB["purchases"].append(data)
        elif remote.timestampMs > existing.get("timestampMs", 0):
            DB["purchases"].remove(existing)
            data = remote.dict()
            data["user_email"] = email
            DB["purchases"].append(data)

    # --- RECOPILAR BAJADAS (DOWNLOAD) ---
    # Buscamos elementos que tengan un timestamp superior a lastSyncTime
    response_accounts = [x for x in DB["accounts"] if x["user_email"] == email and x["updated_at"] > request.lastSyncTime]
    response_categories = [x for x in DB["categories"] if x["user_email"] == email and x["updated_at"] > request.lastSyncTime]
    response_fixed = [x for x in DB["fixed_expenses"] if x["user_email"] == email and x["updated_at"] > request.lastSyncTime]
    response_purchases = [x for x in DB["purchases"] if x["user_email"] == email and x.get("timestampMs", 0) > request.lastSyncTime]

    return {
        "serverTime": server_time,
        "remotePurchases": response_purchases,
        "remoteCategories": response_categories,
        "remoteAccounts": response_accounts,
        "remoteFixedExpenses": response_fixed
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

### Instrucciones de ejecución:
1. Instalar dependencias: `pip install fastapi uvicorn pydantic`
2. Correr el backend: `python mock_server.py`
3. En la app Android, durante el desarrollo local, puedes cambiar la base URL en `RetrofitClient.kt` por tu dirección IP local (ej. `http://10.0.2.2:8000/` si usas el emulador oficial de Android Studio).
