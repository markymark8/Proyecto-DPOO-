package club.consola;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.time.LocalDate;
import java.util.ArrayList;

import club.servicios.ClubSeneca;
import club.persistencia.RepositorioComercio;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Empleado;

import modelo.cafeteria.Bebida;
import modelo.ventas.VentaCafeteria;
import modelo.tienda.Articulo;
import modelo.ventas.VentaTienda;

import java.nio.file.Files;
import java.io.IOException;

public class PruebaPersistenciaComercio {
	
	public static void main(String[] args) {


		Path carpeta = Paths.get("datos_prueba_comercio");
		
		try {
		    Files.createDirectories(carpeta);
		} catch (IOException e) {
		    throw new RuntimeException(
		        "No se pudo crear la carpeta de prueba", e
		    );
		}

	    ClubSeneca club1 = new ClubSeneca();

	    Socio socio = new Socio(
	        "socio1", "Mariana", "1234",
	        LocalDate.of(2004, 5, 10)
	    );

	    club1.registrarUsuario(socio);

	    Articulo raqueta = new Articulo(
	        "Raqueta", "Deportes", 100000, 10
	    );

	    club1.getServicioComercio().registrarArticulo(raqueta);

	    VentaTienda venta = new VentaTienda(
	        new Date(), 0, 0, 0, 0.19, socio, null
	    );

	    venta.agregarArticulo(raqueta, 2);

	    club1.getServicioComercio().realizarCompraTienda(
	        venta,
	        socio,
	        null,
	        new ArrayList<Empleado>(),
	        0
	    );

	    System.out.println("=== ANTES DE GUARDAR ===");
	    System.out.println("Inventario: "
	        + raqueta.getCantidadDisponible());


	    RepositorioComercio.guardar(carpeta, club1);


	    ClubSeneca club2 = new ClubSeneca();

	    RepositorioComercio.cargar(carpeta, club2);

	    Articulo articuloCargado =
	        club2.getServicioComercio().getArticulos().get(0);

	    System.out.println("=== DESPUES DE CARGAR ===");
	    System.out.println("Articulo: "
	        + articuloCargado.getNombre());

	    System.out.println("Inventario: "
	        + articuloCargado.getCantidadDisponible());
	


	Bebida cafe = new Bebida(
	    "Cafe", 10000, 10, "Caliente"
	);

	club1.getServicioComercio().registrarProductoCafeteria(cafe);

	VentaCafeteria ventaCafe = new VentaCafeteria(
	    new Date(), 0, 0, 0, 0.08, 0.10, socio, null
	);

	ventaCafe.agregarProducto(cafe, 2);

	club1.getServicioComercio().realizarCompraCafeteria(
	    ventaCafe, socio, 0
	);

	System.out.println("=== CAFETERIA ANTES DE GUARDAR ===");
	System.out.println("Inventario: "
	    + cafe.getCantidadDisponible());


	RepositorioComercio.guardar(carpeta, club1);


	ClubSeneca club3 = new ClubSeneca();

	RepositorioComercio.cargar(carpeta, club3);

	Bebida cafeCargado = (Bebida)
	    club3.getServicioComercio()
	         .getProductosCafeteria().get(0);

	System.out.println("=== CAFETERIA DESPUES DE CARGAR ===");
	System.out.println("Producto: "
	    + cafeCargado.getNombre());

	System.out.println("Inventario: "
	    + cafeCargado.getCantidadDisponible());

	System.out.println("Temperatura: "
	    + cafeCargado.getTemperatura());
	
	

}
}
