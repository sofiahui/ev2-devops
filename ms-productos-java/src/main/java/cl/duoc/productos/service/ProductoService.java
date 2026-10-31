package cl.duoc.productos.service;

import cl.duoc.productos.model.Producto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductoService {

    private final Map<Long, Producto> productos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    public ProductoService() {
        // Datos iniciales para que el servicio sea usable apenas arranca
        crear(new Producto(null, "Teclado mecanico", 45990, 10));
        crear(new Producto(null, "Mouse inalambrico", 19990, 25));
        crear(new Producto(null, "Monitor 24 pulgadas", 129990, 5));
    }

    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>(productos.values());
        lista.sort(Comparator.comparing(Producto::id));
        return lista;
    }

    public Optional<Producto> buscar(Long id) {
        return Optional.ofNullable(productos.get(id));
    }

    public Producto crear(Producto producto) {
        Producto nuevo = producto.conId(secuencia.incrementAndGet());
        productos.put(nuevo.id(), nuevo);
        return nuevo;
    }

    /**
     * Descuenta stock de forma atomica.
     * Lanza IllegalArgumentException si no existe y IllegalStateException si no alcanza el stock.
     */
    public synchronized Producto descontarStock(Long id, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        Producto actual = buscar(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto " + id + " no existe"));
        if (actual.stock() < cantidad) {
            throw new IllegalStateException("Stock insuficiente para el producto " + id);
        }
        Producto actualizado = actual.conStock(actual.stock() - cantidad);
        productos.put(id, actualizado);
        return actualizado;
    }
}
