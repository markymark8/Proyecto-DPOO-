package club.pruebas;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.tienda.Articulo;

public class ArticuloTest {

    private Articulo articulo;

    @BeforeEach
    public void setUp() {
        articulo = new Articulo("Balon", "Deportes", 50000, 10);
    }

    @Test
    public void testReducirStock() {
        boolean resultado = articulo.reducirStock(3);

        assertTrue(resultado);
        assertEquals(7, articulo.getCantidadDisponible());
    }

    @Test
    public void testReducirStockInsuficiente() {
        boolean resultado = articulo.reducirStock(15);

        assertFalse(resultado);
        assertEquals(10, articulo.getCantidadDisponible());
    }

    @Test
    public void testAumentarStock() {
        boolean resultado = articulo.aumentarStock(5);

        assertTrue(resultado);
        assertEquals(15, articulo.getCantidadDisponible());
    }

    @Test
    public void testCantidadNegativa() {
        boolean resultado = articulo.reducirStock(-2);

        assertFalse(resultado);
        assertEquals(10, articulo.getCantidadDisponible());
    }
}