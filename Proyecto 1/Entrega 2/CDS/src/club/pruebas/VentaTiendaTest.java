
package club.pruebas;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.ArrayList;

import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Empleado;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.tienda.Articulo;
import modelo.ventas.VentaTienda;
import club.servicios.ReglaDeNegocioException;

public class VentaTiendaTest {

    private VentaTienda venta;
    private Articulo articulo;

    @BeforeEach
    public void setUp() {
        venta = new VentaTienda(
            new Date(), 0, 0, 0, 0.19, null, null);

        articulo = new Articulo(
            "Balon", "Deportes", 50000, 10);
    }

    @Test
    public void testAgregarArticulo() {
        venta.agregarArticulo(articulo, 2);

        assertEquals(100000, venta.calcularSubtotal(), 0.001);
    }

    @Test
    public void testAgregarArticuloSinInventario() {
        assertThrows(ReglaDeNegocioException.class, () -> {
            venta.agregarArticulo(articulo, 15);
        });

        assertEquals(10, articulo.getCantidadDisponible());
    }

    @Test
    public void testAgregarCantidadNegativa() {
        assertThrows(ReglaDeNegocioException.class, () -> {
            venta.agregarArticulo(articulo, -3);
        });
    }

    @Test
    public void testCalcularIVA() {
        venta.agregarArticulo(articulo, 2);

        assertEquals(19000, venta.calcularIVA(), 0.001);
    }

    @Test
    public void testCalcularTotalConIVA() {
        venta.agregarArticulo(articulo, 2);

        assertEquals(119000, venta.calcularTotalConIVA(), 0.001);
    }
    

    @Test
    public void testAgregarArticuloNulo() {
    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.agregarArticulo(null, 2);
    });

    	assertEquals(0, venta.calcularSubtotal(), 0.001);
}

    @Test
    public void testInventarioAcumuladoInsuficiente() {
    	venta.agregarArticulo(articulo, 6);

    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.agregarArticulo(articulo, 5);
    });

    	assertEquals(300000, venta.calcularSubtotal(), 0.001);
    	assertEquals(10, articulo.getCantidadDisponible());
}

    @Test
    public void testCalcularTotalCompradorNulo() {
    	venta.agregarArticulo(articulo, 2);

    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.calcularTotalFinal(null, null, null, 0);
    });
}
    

    @Test
    public void testDescuentoEmpleado() {
    	Empleado empleado = new Empleado(
    			"empleado1", "Carlos", "hash123", "DESC08");

    	venta.agregarArticulo(articulo, 2);

    	double descuento = venta.calcularDescuento(
    			empleado, null, new ArrayList<Empleado>());

    	assertEquals(15000, descuento, 0.001);
}

    @Test
    public void testDescuentoSocioConCodigo() {
    	Socio socio = new Socio(
    			"socio1", "Maria", "hash123",
    	LocalDate.of(2000, 5, 10));

    	Empleado empleado = new Empleado(
    			"empleado1", "Carlos", "hash123", "DESC08");

    	ArrayList<Empleado> empleados = new ArrayList<>();
    		empleados.add(empleado);

    		venta.agregarArticulo(articulo, 2);

    		double descuento = venta.calcularDescuento(
        socio, "DESC08", empleados);

    		assertEquals(8000, descuento, 0.001);
}

    @Test
    public void testRechazarDescuentosAcumulados() {
    	Empleado empleado = new Empleado(
    			"empleado1", "Carlos", "hash123", "DESC08");

    	venta.agregarArticulo(articulo, 2);

    	assertThrows(ReglaDeNegocioException.class, () -> {
        venta.calcularDescuento(
            empleado, "DESC08", new ArrayList<Empleado>());
    });
}

    @Test
    public void testSocioSinCodigoDescuento() {
    	Socio socio = new Socio(
    			"socio1", "Maria", "hash123",
    		LocalDate.of(2000, 5, 10));

    	venta.agregarArticulo(articulo, 2);

    	double descuento = venta.calcularDescuento(
    			socio, null, new ArrayList<Empleado>());

    	assertEquals(0, descuento, 0.001);
}
    

    @Test
    public void testCalcularTotalConPuntos() {
    	Socio socio = new Socio(
    			"socio1", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    	socio.sumarPuntos(1000);
    	venta.agregarArticulo(articulo, 2);

    	double total = venta.calcularTotalFinal(
    			socio, null, new ArrayList<Empleado>(), 1000);

    	assertEquals(118000, total, 0.001);
}

    @Test
    public void testPuntosInsuficientes() {
    	Socio socio = new Socio(
    			"socio1", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    	socio.sumarPuntos(500);
    	venta.agregarArticulo(articulo, 2);

    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.calcularTotalFinal(
    				socio, null, new ArrayList<Empleado>(), 1000);
    });
}

    @Test
    public void testPuntosNegativos() {
    	Socio socio = new Socio(
    			"socio1", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    	venta.agregarArticulo(articulo, 2);

    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.calcularTotalFinal(
    				socio, null, new ArrayList<Empleado>(), -5);
    });
}

    @Test
    public void testPuntosSuperanTotal() {
    	Socio socio = new Socio(
    			"socio1", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    	socio.sumarPuntos(200000);
    	venta.agregarArticulo(articulo, 2);

    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.calcularTotalFinal(
    				socio, null, new ArrayList<Empleado>(), 150000);
    });
}

    @Test
    public void testConfirmarVentaExitosa() {
    	Socio socio = new Socio(
    			"socio1", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    	venta.agregarArticulo(articulo, 2);

    	venta.confirmarVenta(
    			socio, null, new ArrayList<Empleado>(), 0);

    	assertTrue(venta.isConfirmada());

    	assertEquals(8, articulo.getCantidadDisponible());

    	assertEquals(119000, venta.getTotal(), 0.001);

    	assertEquals(2380, socio.getPuntosFidelidad());

    	assertTrue(socio.getVentas().contains(venta));
}
    
    

    @Test
    public void testConfirmarVentaVacia() {
    	Socio socio = new Socio(
    			"socio1", "Maria", "hash123",
    			LocalDate.of(2000, 5, 10));

    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.confirmarVenta(
    				socio, null, new ArrayList<Empleado>(), 0);
    });

    	assertFalse(venta.isConfirmada());
    	assertEquals(10, articulo.getCantidadDisponible());
}

    @Test
    public void testConfirmarVentaDosVeces() {
    	Socio socio = new Socio(
    			"socio1", "Maria", "hash123",
    			LocalDate.of(2000, 5, 10));

    	venta.agregarArticulo(articulo, 2);

    	venta.confirmarVenta(
    			socio, null, new ArrayList<Empleado>(), 0);

    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.confirmarVenta(
    				socio, null, new ArrayList<Empleado>(), 0);
    });

    	assertEquals(8, articulo.getCantidadDisponible());
    	assertEquals(1, socio.getVentas().size());
    	assertEquals(2380, socio.getPuntosFidelidad());
}

    @Test
    public void testAgregarArticuloVentaConfirmada() {
    	Socio socio = new Socio(
    			"socio1", "Maria", "hash123",
    			LocalDate.of(2000, 5, 10));

    	venta.agregarArticulo(articulo, 2);

    	venta.confirmarVenta(
    			socio, null, new ArrayList<Empleado>(), 0);

    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.agregarArticulo(articulo, 1);
    });

    	assertEquals(8, articulo.getCantidadDisponible());
    	assertEquals(100000, venta.calcularSubtotal(), 0.001);
}




}
