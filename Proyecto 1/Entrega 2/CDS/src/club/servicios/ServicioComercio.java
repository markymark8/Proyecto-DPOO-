
package club.servicios;

import java.util.ArrayList;

import modelo.tienda.Articulo;
import modelo.cafeteria.ProductoCafeteria;
import modelo.ventas.venta;
import modelo.ventas.VentaTienda;
import club.modelo.usuarios.Comprador;
import club.modelo.usuarios.Empleado;
import modelo.ventas.VentaCafeteria;

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
    
    public void registrarVenta(venta nuevaVenta) {

        if (nuevaVenta == null) {
            throw new ReglaDeNegocioException(
                "La venta no puede ser nula");
        }

        if (ventas.contains(nuevaVenta)) {
            throw new ReglaDeNegocioException(
                "Esta venta ya fue registrada");
        }

        ventas.add(nuevaVenta);
    }
    

    public void realizarCompraTienda(
        VentaTienda ventaTienda,
        Comprador comprador,
        String codigoDescuento,
        ArrayList<Empleado> empleados,
        int puntosUsar) {

    if (ventaTienda == null) {
        throw new ReglaDeNegocioException(
            "La venta de tienda no puede ser nula");
    }

    if (ventas.contains(ventaTienda)) {
        throw new ReglaDeNegocioException(
            "Esta venta ya fue registrada");
    }

    ventaTienda.confirmarVenta(
        comprador, codigoDescuento, empleados, puntosUsar);

    registrarVenta(ventaTienda);
}
    
    public void realizarCompraCafeteria(
            VentaCafeteria ventaCafeteria,
            Comprador comprador,
            int puntosUsar) {

        if (ventaCafeteria == null) {
            throw new ReglaDeNegocioException(
                "La venta de cafeteria no puede ser nula");
        }

        if (ventas.contains(ventaCafeteria)) {
            throw new ReglaDeNegocioException(
                "Esta venta ya fue registrada");
        }

        ventaCafeteria.confirmarVenta(comprador, puntosUsar);

        registrarVenta(ventaCafeteria);
    }

}

