
package club.persistencia;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import club.servicios.ClubSeneca;
import club.servicios.ServicioComercio;

import modelo.tienda.Articulo;
import modelo.cafeteria.ProductoCafeteria;
import modelo.cafeteria.Bebida;
import modelo.cafeteria.Snack;

public class RepositorioComercio {

    private static final String ARCHIVO_ARTICULOS =
            "articulos.csv";

    private static final String ARCHIVO_CAFETERIA =
            "cafeteria.csv";

    private static final String[] ENC_ARTICULOS = {
        "nombre", "categoria", "precio", "cantidadDisponible"
    };

    private static final String[] ENC_CAFETERIA = {
        "tipo", "nombre", "precio",
        "cantidadDisponible", "detalle"
    };

    private RepositorioComercio() {
    }

    public static void guardar(Path carpeta, ClubSeneca club) {

        ServicioComercio servicio = club.getServicioComercio();

        guardarArticulos(
            carpeta.resolve(ARCHIVO_ARTICULOS), servicio
        );

        guardarCafeteria(
            carpeta.resolve(ARCHIVO_CAFETERIA), servicio
        );
    }

    public static void cargar(Path carpeta, ClubSeneca club) {

        ServicioComercio servicio = club.getServicioComercio();

        cargarArticulos(
            carpeta.resolve(ARCHIVO_ARTICULOS), servicio
        );

        cargarCafeteria(
            carpeta.resolve(ARCHIVO_CAFETERIA), servicio
        );
    }

    private static void guardarArticulos(
            Path archivo, ServicioComercio servicio) {

        List<String[]> filas = new ArrayList<>();

        for (Articulo articulo : servicio.getArticulos()) {

            filas.add(new String[] {
                articulo.getNombre(),
                articulo.getCategoria(),
                String.valueOf(articulo.getPrecio()),
                String.valueOf(articulo.getCantidadDisponible())
            });
        }

        Csv.escribir(archivo, ENC_ARTICULOS, filas);
    }

    private static void cargarArticulos(
            Path archivo, ServicioComercio servicio) {

        List<String[]> filas = Csv.leer(archivo);

        for (String[] fila : filas) {

            if (fila.length != 4) {
                throw new IllegalArgumentException(
                    "Fila invalida en articulos.csv"
                );
            }

            String nombre = fila[0];
            String categoria = fila[1];
            double precio = Double.parseDouble(fila[2]);
            int cantidad = Integer.parseInt(fila[3]);

            Articulo articulo = new Articulo(
                nombre, categoria, precio, cantidad
            );

            servicio.registrarArticulo(articulo);
        }
    }

    private static void guardarCafeteria(
            Path archivo, ServicioComercio servicio) {

        List<String[]> filas = new ArrayList<>();

        for (ProductoCafeteria producto :
                servicio.getProductosCafeteria()) {

            String tipo = "Producto";
            String detalle = "";

            if (producto instanceof Bebida) {
                tipo = "Bebida";
                detalle = ((Bebida) producto).getTemperatura();

            } else if (producto instanceof Snack) {
                tipo = "Snack";
                detalle = ((Snack) producto).getAlergenos();
            }

            filas.add(new String[] {
                tipo,
                producto.getNombre(),
                String.valueOf(producto.getPrecio()),
                String.valueOf(producto.getCantidadDisponible()),
                detalle
            });
        }

        Csv.escribir(archivo, ENC_CAFETERIA, filas);
    }

    private static void cargarCafeteria(
            Path archivo, ServicioComercio servicio) {

        List<String[]> filas = Csv.leer(archivo);

        for (String[] fila : filas) {

            if (fila.length != 5) {
                throw new IllegalArgumentException(
                    "Fila invalida en cafeteria.csv"
                );
            }

            String tipo = fila[0];
            String nombre = fila[1];
            double precio = Double.parseDouble(fila[2]);
            int cantidad = Integer.parseInt(fila[3]);
            String detalle = fila[4];

            ProductoCafeteria producto;

            if (tipo.equals("Bebida")) {

                producto = new Bebida(
                    nombre, precio, cantidad, detalle
                );

            } else if (tipo.equals("Snack")) {

                producto = new Snack(
                    nombre, precio, cantidad, detalle
                );

            } else if (tipo.equals("Producto")) {

                producto = new ProductoCafeteria(
                    nombre, precio, cantidad
                );

            } else {
                throw new IllegalArgumentException(
                    "Tipo de producto desconocido: " + tipo
                );
            }

            servicio.registrarProductoCafeteria(producto);
        }
    }
}
