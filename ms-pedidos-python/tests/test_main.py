import httpx
import pytest
from fastapi.testclient import TestClient

from app import main
from app.main import app, get_client


def cliente_falso(handler):
    """Reemplaza la llamada real a ms-productos por una respuesta simulada."""
    def _override():
        with httpx.Client(base_url="http://productos", transport=httpx.MockTransport(handler)) as c:
            yield c
    return _override


@pytest.fixture(autouse=True)
def limpiar():
    main.pedidos.clear()
    yield
    app.dependency_overrides.clear()


client = TestClient(app)


def test_health():
    r = client.get("/health")
    assert r.status_code == 200
    assert r.json()["status"] == "UP"


def test_listar_vacio():
    assert client.get("/pedidos").json() == []


def test_crear_pedido_ok():
    def handler(request):
        assert request.url.path == "/api/productos/1/descontar"
        assert request.url.params["cantidad"] == "2"
        return httpx.Response(200, json={"id": 1, "nombre": "Teclado", "precio": 1000, "stock": 8})

    app.dependency_overrides[get_client] = cliente_falso(handler)
    r = client.post("/pedidos", json={"producto_id": 1, "cantidad": 2})

    assert r.status_code == 201
    assert r.json()["total"] == 2000
    assert len(client.get("/pedidos").json()) == 1


@pytest.mark.parametrize("codigo_productos,codigo_esperado", [(404, 404), (409, 409), (500, 502)])
def test_errores_de_productos(codigo_productos, codigo_esperado):
    app.dependency_overrides[get_client] = cliente_falso(lambda req: httpx.Response(codigo_productos))
    r = client.post("/pedidos", json={"producto_id": 1, "cantidad": 1})
    assert r.status_code == codigo_esperado


def test_productos_caido_devuelve_503():
    def handler(request):
        raise httpx.ConnectError("sin conexion")

    app.dependency_overrides[get_client] = cliente_falso(handler)
    r = client.post("/pedidos", json={"producto_id": 1, "cantidad": 1})
    assert r.status_code == 503


def test_validacion_cantidad():
    r = client.post("/pedidos", json={"producto_id": 1, "cantidad": 0})
    assert r.status_code == 422


def test_get_client_usa_url_configurada():
    gen = get_client()
    c = next(gen)
    assert str(c.base_url).startswith(main.PRODUCTOS_URL)
    gen.close()
