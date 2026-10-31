package cl.duoc.productos.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record Producto(
        Long id,
        @NotBlank String nombre,
        @Positive double precio,
        @Min(0) int stock
) {
    public Producto conId(Long nuevoId) {
        return new Producto(nuevoId, nombre, precio, stock);
    }

    public Producto conStock(int nuevoStock) {
        return new Producto(id, nombre, precio, nuevoStock);
    }
}
