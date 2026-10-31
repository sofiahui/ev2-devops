import os
import socket
from datetime import datetime, timezone

import httpx
from fastapi import Depends, FastAPI, HTTPException
from pydantic import BaseModel, Field

PRODUCTOS_URL = os.getenv("PRODUCTOS_URL", "http://localhost:8081")

app = FastAPI(title="ms-pedidos", version="1.0.0")

# Almacenamiento en memoria (suficiente para la evaluacion)
pedidos: list[dict] = []


class PedidoIn(BaseModel):
    producto_id: int = Field(gt=0)
    cantidad: int = Field(gt=0)


def get_client():
    with httpx.Client(base_url=PRODUCTOS_URL, timeout=5.0) as client:
        yield client


@app.get("/health")
def health():
    # hostname permite ver que replica respondio cuando se escala el servicio
    return {"status": "UP", "servicio": "ms-pedidos", "instancia": socket.gethostname()}


@app.get("/pedidos")
def listar_pedidos():
    return pedidos


@app.post("/pedidos", status_code=201)
def crear_pedido(pedido: PedidoIn, client: httpx.Client = Depends(get_client)):
    try:
        resp = client.post(
            f"/api/productos/{pedido.producto_id}/descontar",
            params={"cantidad": pedido.cantidad},
        )
    except httpx.HTTPError:
        raise HTTPException(status_code=503, detail="ms-productos no disponible")

    if resp.status_code == 404:
        raise HTTPException(status_code=404, detail="Producto no existe")
    if resp.status_code == 409:
        raise HTTPException(status_code=409, detail="Stock insuficiente")
    if resp.status_code != 200:
        raise HTTPException(status_code=502, detail="Error en ms-productos")

    producto = resp.json()
    nuevo = {
        "id": len(pedidos) + 1,
        "producto_id": pedido.producto_id,
        "producto": producto["nombre"],
        "cantidad": pedido.cantidad,
        "total": producto["precio"] * pedido.cantidad,
        "fecha": datetime.now(timezone.utc).isoformat(),
    }
    pedidos.append(nuevo)
    return nuevo
