
package club.consola;

import java.util.Scanner;

import club.servicios.ClubSeneca;
import club.servicios.ServicioComercio;
import club.modelo.usuarios.Comprador;
import modelo.tienda.Articulo;
import java.util.Date;
import java.util.ArrayList;
import modelo.cafeteria.ProductoCafeteria;
import modelo.ventas.VentaCafeteria;
import modelo.ventas.venta;

import modelo.ventas.VentaTienda;
import club.modelo.usuarios.Empleado;
import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;

public class ConsolaComercio {

    private ClubSeneca club;
    private ServicioComercio servicio;
    private Comprador comprador;
    private Scanner scanner;

    public ConsolaComercio(ClubSeneca club,
                           Comprador comprador,
                           Scanner scanner) {

        this.club = club;
        this.servicio = club.getServicioComercio();
        this.comprador = comprador;
        this.scanner = scanner;
    }
    
    

    private void consultarProductosCafeteria() {

    	ArrayList<ProductoCafeteria> productos =
    			servicio.getProductosCafeteria();

    	System.out.println("\n=== PRODUCTOS DE CAFETERIA ===");

    	if (productos.isEmpty()) {
    		System.out.println("No hay productos registrados");
    		return;
    	}

    	for (int i = 0; i < productos.size(); i++) {

    		ProductoCafeteria producto = productos.get(i);

    		System.out.println(
    				(i + 1) + ". " + producto.getNombre()
    				+ " | Precio: $" + producto.getPrecio()
    				+ " | Disponibles: "
    				+ producto.getCantidadDisponible()
    				);
    	}
    }

    

    private void consultarArticulos() {

    	ArrayList<Articulo> articulos = servicio.getArticulos();

    	System.out.println("\n=== ARTICULOS DE TIENDA ===");

    	if (articulos.isEmpty()) {
    		System.out.println("No hay articulos registrados");
    		return;
    	}

    	for (int i = 0; i < articulos.size(); i++) {

    		Articulo articulo = articulos.get(i);

    		System.out.println(
    				(i + 1) + ". " + articulo.getNombre()
    				+ " | Categoria: " + articulo.getCategoria()
    				+ " | Precio: $" + articulo.getPrecio()
    				+ " | Disponibles: "
    				+ articulo.getCantidadDisponible()
    				);
    	}
}
    
    
	private void comprarTienda() {
		
	    ArrayList<Articulo> articulos = servicio.getArticulos();
	
	    if (articulos.isEmpty()) {
	        System.out.println("No hay articulos disponibles");
	        return;
	    }
	
	    VentaTienda nuevaVenta = new VentaTienda(
	        new Date(), 0, 0, 0, 0.19, null, null
	    );
	
	    try {
	
	        boolean seguirComprando = true;
	        boolean tieneArticulos = false;
	
	        while (seguirComprando) {
	
	            consultarArticulos();
	
	            System.out.print("Seleccione el numero del articulo: ");
	            int indice = Integer.parseInt(scanner.nextLine()) - 1;
	
	            if (indice >= 0 && indice < articulos.size()) {
	
	                Articulo articulo = articulos.get(indice);
	
	                System.out.print("Ingrese la cantidad: ");
	                int cantidad = Integer.parseInt(scanner.nextLine());
	
	                nuevaVenta.agregarArticulo(articulo, cantidad);
	                tieneArticulos = true;
	
	                System.out.println("Articulo agregado correctamente");
	
	            } else {
	                System.out.println("Articulo invalido");
	            }
	
	            System.out.print("Desea agregar otro articulo? (S/N): ");
	            String respuesta = scanner.nextLine();
	
	            if (respuesta.equalsIgnoreCase("N")) {
	                seguirComprando = false;
	
	            } else if (!respuesta.equalsIgnoreCase("S")) {
	                System.out.println("Respuesta invalida");
	            }
	        }
	
	        if (!tieneArticulos) {
	            System.out.println("No se agregaron articulos a la compra");
	            return;
	        }
	
	        System.out.println(
	            "Subtotal: $" + nuevaVenta.calcularSubtotal()
	        );
	
	        System.out.println(
	            "Total con IVA: $" + nuevaVenta.calcularTotalConIVA()
	        );
	
	        System.out.println(
	            "Puntos disponibles: " + comprador.getPuntosFidelidad()
	        );
	
	        System.out.print("Cuantos puntos desea utilizar: ");
	        int puntosUsar = Integer.parseInt(scanner.nextLine());
	
	        String codigoDescuento = null;
	
	        if (comprador instanceof Socio) {
	
	            System.out.print(
	                "Ingrese codigo de descuento (Enter si no tiene): "
	            );
	
	            codigoDescuento = scanner.nextLine();
	
	            if (codigoDescuento.isEmpty()) {
	                codigoDescuento = null;
	            }
	        }
	
	        ArrayList<Empleado> empleados = club.getEmpleados();
	
	        double totalFinal = nuevaVenta.calcularTotalFinal(
	            comprador, codigoDescuento, empleados, puntosUsar
	        );
	
	        System.out.println("Total final: $" + totalFinal);
	
	        System.out.print("Confirmar compra? (S/N): ");
	        String confirmacion = scanner.nextLine();
	
	        if (confirmacion.equalsIgnoreCase("S")) {
	
	            servicio.realizarCompraTienda(
	                nuevaVenta,
	                comprador,
	                codigoDescuento,
	                empleados,
	                puntosUsar
	            );
	
	            System.out.println("Compra realizada exitosamente");
	
	        } else {
	            System.out.println("Compra cancelada");
	        }
	
	    } catch (NumberFormatException e) {
	        System.out.println("Debe ingresar un numero valido");
	
	    } catch (ReglaDeNegocioException e) {
	        System.out.println(e.getMessage());
	    }
	}
	

	private void comprarCafeteria() {

    ArrayList<ProductoCafeteria> productos =
            servicio.getProductosCafeteria();

    if (productos.isEmpty()) {
        System.out.println("No hay productos disponibles");
        return;
    }

    VentaCafeteria nuevaVenta = new VentaCafeteria(
        new Date(), 0, 0, 0, 0.08, 0.10, null, null
    );

    boolean seguirComprando = true;

    try {

        while (seguirComprando) {

            consultarProductosCafeteria();

            System.out.print("Seleccione el numero del producto: ");
            int indice = Integer.parseInt(scanner.nextLine()) - 1;

            if (indice >= 0 && indice < productos.size()) {

                ProductoCafeteria producto = productos.get(indice);

                System.out.print("Ingrese la cantidad: ");
                int cantidad = Integer.parseInt(scanner.nextLine());

                nuevaVenta.agregarProducto(producto, cantidad);

                System.out.println("Producto agregado correctamente");

            } else {
                System.out.println("Producto invalido");
            }

            System.out.print("Desea agregar otro producto? (S/N): ");
            String respuesta = scanner.nextLine();

            if (respuesta.equalsIgnoreCase("N")) {
                seguirComprando = false;
            }
        }

        System.out.println(
            "Subtotal: $" + nuevaVenta.calcularSubtotal()
        );

        System.out.println(
            "Impuesto al consumo: $" +
            nuevaVenta.calcularImpuestoConsumo()
        );

        System.out.println(
            "Propina: $" + nuevaVenta.calcularPropina()
        );
        


String alergenos = nuevaVenta.consultarAlergenos();

if (!alergenos.isEmpty()) {

    System.out.println("\n=== INFORMACION DE ALERGENOS ===");
    System.out.println(alergenos);

    System.out.print(
        "Confirma que recibio la informacion de alergenos? (S/N): "
    );

    String respuesta = scanner.nextLine();

    if (respuesta.equalsIgnoreCase("S")) {

        nuevaVenta.confirmarInformacionAlergenos();
        System.out.println("Informacion de alergenos confirmada");

    } else {

        System.out.println(
            "No se puede continuar sin confirmar los alergenos"
        );
        return;
    }
}

	System.out.println(
			"Puntos disponibles: " + comprador.getPuntosFidelidad()
			);

	System.out.print("Cuantos puntos desea utilizar: ");
	int puntosUsar = Integer.parseInt(scanner.nextLine());

	if (puntosUsar < 0 ||
			puntosUsar > comprador.getPuntosFidelidad()) {

		System.out.println("Cantidad de puntos invalida");
		return;
	}

	System.out.println(
			"Total antes de redimir puntos: $"
					+ nuevaVenta.calcularTotal()
			);

	System.out.print("Confirmar compra? (S/N): ");
	String confirmacion = scanner.nextLine();

	if (confirmacion.equalsIgnoreCase("S")) {

		servicio.realizarCompraCafeteria(
				nuevaVenta, comprador, puntosUsar
				);

		System.out.println("Compra realizada exitosamente");

	} else {

		System.out.println("Compra cancelada");
	}


    	} catch (NumberFormatException e) {
        System.out.println("Debe ingresar un numero valido");

    	} catch (ReglaDeNegocioException e) {
    		System.out.println(e.getMessage());
    	}
	}
	

	private void consultarPuntos() {

		System.out.println("\n=== PUNTOS DE FIDELIDAD ===");

		System.out.println(
				"Puntos disponibles: " + comprador.getPuntosFidelidad()
				);
	}
	
	

	private void consultarHistorialCompras() {

    ArrayList<venta> ventas = servicio.getVentas();

    System.out.println("\n=== HISTORIAL DE COMPRAS ===");

    boolean tieneCompras = false;

    for (venta compra : ventas) {

        boolean esDelComprador = false;

        if (comprador instanceof Socio) {
            esDelComprador = compra.getSocio() == comprador;

        } else if (comprador instanceof Empleado) {
            esDelComprador = compra.getEmpleado() == comprador;
        }

        if (esDelComprador) {

            tieneCompras = true;

            System.out.println("-------------------------");
            System.out.println("Fecha: " + compra.getFecha());
            System.out.println("Total: $" + compra.getTotal());
            System.out.println("Descuento: $" + compra.getDescuento());
            System.out.println("Puntos: " + compra.getPuntos());
        }
    }

    if (!tieneCompras) {
        System.out.println("No tienes compras registradas");
    }
}
  

	    public void mostrarMenu() {

	    	int opcion = -1;

	    	while (opcion != 0) {

        System.out.println("\n=== TIENDA Y CAFETERIA ===");
        System.out.println("1. Consultar articulos de tienda");
        System.out.println("2. Comprar en tienda");
        System.out.println("3. Consultar productos de cafeteria");
        System.out.println("4. Comprar en cafeteria");
        System.out.println("5. Consultar puntos de fidelidad");
        System.out.println("6. Consultar historial de compras");
        System.out.println("0. Volver al menu principal");
        System.out.print("Seleccione una opcion: ");

        String entrada = scanner.nextLine();

        try {

            opcion = Integer.parseInt(entrada);

            if (opcion == 1) {
                consultarArticulos();

            } else if (opcion == 2) {
                comprarTienda();

            } else if (opcion == 3) {
                consultarProductosCafeteria();

            } else if (opcion == 4) {
                comprarCafeteria();

            } else if (opcion == 5) {
                consultarPuntos();

            } else if (opcion == 6) {
                consultarHistorialCompras();
                
            } else if (opcion == 0) {
                System.out.println("Volviendo al menu principal...");

            } else {
                System.out.println("Opcion invalida");
            }

        } catch (NumberFormatException e) {
            System.out.println("Debe ingresar un numero valido");
        }
    }


	}
	
}