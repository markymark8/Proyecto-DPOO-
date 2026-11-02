
package club.servicios;

import java.util.ArrayList;

import club.modelo.tienda.Articulo;
import modelo.cafeteria.ProductoCafeteria;
import modelo.ventas.venta;

public class ServicioComercio {

    private ArrayList<Articulo> articulos;
    private ArrayList<ProductoCafeteria> productosCafeteria;
    private ArrayList<venta> ventas;

    public ServicioComercio() {
        this.articulos = new ArrayList<>();
        this.productosCafeteria = new ArrayList<>();
        this.ventas = new ArrayList<>();
    }

    public void registrarArticulo(Articulo articulo) {

        if (articulo == null) {
            throw new ReglaDeNegocioException(
                "El articulo no puede ser nulo");
        }

        articulos.add(articulo);
    }

    public void registrarProductoCafeteria(
            ProductoCafeteria producto) {

        if (producto == null) {
            throw new ReglaDeNegocioException(
                "El producto no puede ser nulo");
        }

        productosCafeteria.add(producto);
    }

    public ArrayList<Articulo> getArticulos() {
        return new ArrayList<>(articulos);
    }

    public ArrayList<ProductoCafeteria> getProductosCafeteria() {
        return new ArrayList<>(productosCafeteria);
    }

    public ArrayList<venta> getVentas() {
        return new ArrayList<>(ventas);
    }
}

