
package club.pruebas;

import static org.junit.jupiter.api.Assertions.*;
import modelo.cafeteria.ProductoCafeteria;
import modelo.cafeteria.Snack;
import modelo.cafeteria.Bebida;
import java.util.Date;
import club.modelo.usuarios.Socio;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.ventas.VentaCafeteria;
import club.servicios.ReglaDeNegocioException;

public class VentaCafeteriaTest {

    private VentaCafeteria venta;
    private Snack snack;
    private Bebida bebida;
    
    @BeforeEach
    public void setUp() {
        venta = new VentaCafeteria(
            new Date(), 0, 0, 0, 0.08, 0.10,
            null, null);

        snack = new Snack("Sandwich", 15000, 10, "Gluten");
        bebida = new Bebida("Cafe", 5000, 20, "Caliente");
    }

    @Test
    public void testAgregarProductoNulo() {
        assertThrows(ReglaDeNegocioException.class, () -> {
            venta.agregarProducto(null, 2);
        });

        assertEquals(0, venta.calcularSubtotal(), 0.001);
    }

    @Test
    public void testSubtotalVentaVacia() {
        assertEquals(0, venta.calcularSubtotal(), 0.001);
    }

    @Test
    public void testImpuestoVentaVacia() {
        assertEquals(0, venta.calcularImpuestoConsumo(), 0.001);
    }

    @Test
    public void testPropinaNegativa() {
        assertThrows(ReglaDeNegocioException.class, () -> {
            venta.setPropina(-0.10);
        });
    }

    @Test
    public void testConfirmarVentaSinComprador() {
        assertThrows(ReglaDeNegocioException.class, () -> {
            venta.confirmarVenta(null, 0);
        });

        assertFalse(venta.isConfirmada());
    }
    

    @Test
    public void testAgregarProducto() {
    	venta.agregarProducto(snack, 2);

    	assertEquals(30000, venta.calcularSubtotal(), 0.001);
}

    @Test
    public void testCalcularSubtotal() {
    	venta.agregarProducto(snack, 2);
    	venta.agregarProducto(bebida, 2);

    	assertEquals(40000, venta.calcularSubtotal(), 0.001);
}

    @Test
    public void testCalcularImpuestoConsumo() {
    	venta.agregarProducto(snack, 2);
    	venta.agregarProducto(bebida, 2);

    	assertEquals(3200, venta.calcularImpuestoConsumo(), 0.001);
}

    @Test
    public void testCalcularPropina() {
    	venta.agregarProducto(snack, 2);
    	venta.agregarProducto(bebida, 2);

    	assertEquals(4000, venta.calcularPropina(), 0.001);
}

    @Test
    public void testCalcularTotal() {
    	venta.agregarProducto(snack, 2);
    	venta.agregarProducto(bebida, 2);

    	assertEquals(47200, venta.calcularTotal(), 0.001);
}

    @Test
    public void testInventarioInsuficiente() {
    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.agregarProducto(snack, 15);
    });

    	assertEquals(10, snack.getCantidadDisponible());
    	assertEquals(0, venta.calcularSubtotal(), 0.001);
}

    @Test
    public void testInventarioAcumuladoInsuficiente() {
    	venta.agregarProducto(snack, 6);

    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.agregarProducto(snack, 5);
    });

    	assertEquals(90000, venta.calcularSubtotal(), 0.001);
    	assertEquals(10, snack.getCantidadDisponible());
}
    

    @Test
    public void testConsultarAlergenos() {
    	venta.agregarProducto(snack, 2);

    	String informacion = venta.consultarAlergenos();

    	assertTrue(informacion.contains("Sandwich"));
    	assertTrue(informacion.contains("Gluten"));
}
    

    @Test
    public void testConfirmarVentaSinInformarAlergenos() {

    	Socio socio = new Socio(
    			"socio01", "Maria", "hash123",
    			LocalDate.of(2000, 5, 10));

    		venta.agregarProducto(snack, 2);

    		assertThrows(ReglaDeNegocioException.class, () -> {
    			venta.confirmarVenta(socio, 0);
    });

    		assertFalse(venta.isConfirmada());
    		assertEquals(10, snack.getCantidadDisponible());
    		assertEquals(0, socio.getVentas().size());
}
    

    @Test
    public void testConfirmarVentaExitosa() {

    	Socio socio = new Socio(
    			"socio01", "Maria", "hash123",
    			LocalDate.of(2000, 5, 10));

    	venta.agregarProducto(snack, 2);
    	venta.confirmarInformacionAlergenos();

    	venta.confirmarVenta(socio, 0);
    
    	assertTrue(venta.isConfirmada());

    	assertEquals(8, snack.getCantidadDisponible());

    	assertEquals(1, socio.getVentas().size());

    	assertEquals(35400, venta.getTotal(), 0.001);

    	assertEquals(708, socio.getPuntosFidelidad());
}
    

    @Test
    public void testConfirmarVentaDosVeces() {

    	Socio socio = new Socio(
    			"socio01", "Maria", "hash123",
    			LocalDate.of(2000, 5, 10));

    	venta.agregarProducto(snack, 2);
    	venta.confirmarInformacionAlergenos();

    	venta.confirmarVenta(socio, 0);

    	assertThrows(ReglaDeNegocioException.class, () -> {
    		venta.confirmarVenta(socio, 0);
    });

    	assertTrue(venta.isConfirmada());
    	assertEquals(8, snack.getCantidadDisponible());
    	assertEquals(1, socio.getVentas().size());
    	assertEquals(708, socio.getPuntosFidelidad());
    	
    	
}


    @Test
    public void testRedimirPuntosFidelidad() {

    	Socio socio = new Socio(
    			"socio01", "Maria", "hash123",
    			LocalDate.of(2000, 5, 10));

    	socio.sumarPuntos(5000);

    	venta.agregarProducto(snack, 2);
    	venta.confirmarInformacionAlergenos();

    	venta.confirmarVenta(socio, 3000);

    	assertTrue(venta.isConfirmada());

    	assertEquals(32400, venta.getTotal(), 0.001);

    	assertEquals(2648, socio.getPuntosFidelidad());

    	assertEquals(8, snack.getCantidadDisponible());

    	assertEquals(1, socio.getVentas().size());
}
    
    

    @Test
    public void testContieneBebidaCaliente() {

    	venta.agregarProducto(bebida, 2);

    	assertTrue(venta.contieneBebidaCaliente());
}
    

    @Test
    public void testSinBebidaCaliente() {

    	venta.agregarProducto(snack, 2);

    	assertFalse(venta.contieneBebidaCaliente());
    }
    	

    @Test
    public void testModificarPropinaVentaConfirmada() {

    Socio socio = new Socio(
        "socio01", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    venta.agregarProducto(snack, 2);
    venta.confirmarInformacionAlergenos();
    venta.confirmarVenta(socio, 0);

    assertThrows(ReglaDeNegocioException.class, () -> {
        venta.setPropina(0.15);
    });

    assertTrue(venta.isConfirmada());


}


    @Test
    public void testAgregarProductoVentaConfirmada() {

    Socio socio = new Socio(
        "socio01", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    venta.agregarProducto(snack, 2);
    venta.confirmarInformacionAlergenos();
    venta.confirmarVenta(socio, 0);

    assertThrows(ReglaDeNegocioException.class, () -> {
        venta.agregarProducto(bebida, 2);
    });

    assertTrue(venta.isConfirmada());
    assertEquals(20, bebida.getCantidadDisponible());
    assertEquals(30000, venta.calcularSubtotal(), 0.001);
}


@Test
public void testInformarAlergenosVentaConfirmada() {

    Socio socio = new Socio(
        "socio01", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    venta.agregarProducto(snack, 2);
    venta.confirmarInformacionAlergenos();
    venta.confirmarVenta(socio, 0);

    assertThrows(ReglaDeNegocioException.class, () -> {
        venta.confirmarInformacionAlergenos();
    });

    assertTrue(venta.isConfirmada());
    assertEquals(8, snack.getCantidadDisponible());
}


	@Test
	public void testPuntosInsuficientes() {

    Socio socio = new Socio(
        "socio01", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    socio.sumarPuntos(500);

    venta.agregarProducto(snack, 2);
    venta.confirmarInformacionAlergenos();

    assertThrows(ReglaDeNegocioException.class, () -> {
        venta.confirmarVenta(socio, 1000);
    });

    assertFalse(venta.isConfirmada());
    assertEquals(500, socio.getPuntosFidelidad());
    assertEquals(10, snack.getCantidadDisponible());
    assertEquals(0, socio.getVentas().size());
}
	

	@Test
	public void testPuntosNegativos() {

    Socio socio = new Socio(
        "socio01", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    venta.agregarProducto(snack, 2);
    venta.confirmarInformacionAlergenos();

    assertThrows(ReglaDeNegocioException.class, () -> {
        venta.confirmarVenta(socio, -100);
    });

    assertFalse(venta.isConfirmada());
    assertEquals(0, socio.getPuntosFidelidad());
    assertEquals(10, snack.getCantidadDisponible());
    assertEquals(0, socio.getVentas().size());
}
	

	@Test
	public void testCantidadNegativa() {

    assertThrows(ReglaDeNegocioException.class, () -> {
        venta.agregarProducto(snack, -2);
    });

    assertEquals(0, venta.calcularSubtotal(), 0.001);
    assertEquals(10, snack.getCantidadDisponible());
    assertFalse(venta.isConfirmada());
}
	
	

	@Test
	public void testCantidadCero() {

    assertThrows(ReglaDeNegocioException.class, () -> {
        venta.agregarProducto(snack, 0);
    });

    assertEquals(0, venta.calcularSubtotal(), 0.001);
    assertEquals(10, snack.getCantidadDisponible());
    assertFalse(venta.isConfirmada());
}
	

	@Test
	public void testConfirmarVentaVacia() {

    Socio socio = new Socio(
        "socio01", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    assertThrows(ReglaDeNegocioException.class, () -> {
        venta.confirmarVenta(socio, 0);
    });

    assertFalse(venta.isConfirmada());
    assertEquals(0, venta.calcularSubtotal(), 0.001);
    assertEquals(0, socio.getVentas().size());
    assertEquals(0, socio.getPuntosFidelidad());
}


	@Test
	public void testPuntosSuperanValorCompra() {

    Socio socio = new Socio(
        "socio01", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    socio.sumarPuntos(50000);

    venta.agregarProducto(snack, 2);
    venta.confirmarInformacionAlergenos();

    assertThrows(ReglaDeNegocioException.class, () -> {
        venta.confirmarVenta(socio, 40000);
    });

    assertFalse(venta.isConfirmada());
    assertEquals(50000, socio.getPuntosFidelidad());
    assertEquals(10, snack.getCantidadDisponible());
    assertEquals(0, socio.getVentas().size());
}


	@Test
	public void testModificarPropinaAntesDeConfirmar() {

    venta.agregarProducto(snack, 2);

    venta.setPropina(0.15);

    assertEquals(4500, venta.calcularPropina(), 0.001);
    assertEquals(36900, venta.calcularTotal(), 0.001);
    assertEquals(10, snack.getCantidadDisponible());
    assertFalse(venta.isConfirmada());
}
	

	@Test
	public void testConfirmarVentaSoloBebidas() {

    Socio socio = new Socio(
        "socio01", "Maria", "hash123",
        LocalDate.of(2000, 5, 10));

    venta.agregarProducto(bebida, 2);

    venta.confirmarVenta(socio, 0);

    assertTrue(venta.isConfirmada());
    assertEquals(18, bebida.getCantidadDisponible());
    assertEquals(1, socio.getVentas().size());
    assertEquals(11800, venta.getTotal(), 0.001);
    assertEquals(236, socio.getPuntosFidelidad());
}







}
