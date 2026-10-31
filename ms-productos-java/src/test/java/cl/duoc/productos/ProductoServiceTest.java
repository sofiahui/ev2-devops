package cl.duoc.productos;

import cl.duoc.productos.model.Producto;
import cl.duoc.productos.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoServiceTest {

    private ProductoService service;

    @BeforeEach
    void setUp() {
        service = new ProductoService();
    }

    @Test
    void listarDevuelveDatosIniciales() {
        assertEquals(3, service.listar().size());
    }

    @Test
    void crearAsignaIdNuevo() {
        Producto creado = service.crear(new Producto(null, "Audifonos", 29990, 8));
        assertEquals(4L, creado.id());
        assertTrue(service.buscar(4L).isPresent());
    }

    @Test
    void buscarInexistenteDevuelveVacio() {
        assertTrue(service.buscar(99L).isEmpty());
    }

    @Test
    void descontarStockRestaCantidad() {
        Producto actualizado = service.descontarStock(1L, 3);
        assertEquals(7, actualizado.stock());
    }

    @Test
    void descontarStockInsuficienteLanzaError() {
        assertThrows(IllegalStateException.class, () -> service.descontarStock(3L, 100));
    }

    @Test
    void descontarProductoInexistenteLanzaError() {
        assertThrows(IllegalArgumentException.class, () -> service.descontarStock(99L, 1));
    }

    @Test
    void descontarCantidadInvalidaLanzaError() {
        assertThrows(IllegalArgumentException.class, () -> service.descontarStock(1L, 0));
    }
}
