package club.pruebas;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import club.modelo.individual.Instalacion;
import club.modelo.individual.Modalidad;


public class InstalacionTest {


	private Instalacion tenis;
	private Instalacion padel;
	private Instalacion mesaTechada;
	private Instalacion mesaAbierta;


	public void setup() {
		tenis = new Instalacion(1, "Cancha 1", "TENIS", 4, false, true);
		padel = new Instalacion(2, "Padel 1", "PADEL", 4, false, true);
		mesaTechada = new Instalacion(3, "Mesa 1", "TENIS_DE_MESA", 4, true, true);
		mesaAbierta = new Instalacion(4, "Mesa 2", "TENIS_DE_MESA", 4, false, true);
	}

	

	@Test
	public void admiteTenisTest() {
		setup();
		assertTrue(tenis.admite(2, Modalidad.SENCILLOS), "Tenis sencillos admite 2 jugadores");
		assertTrue(tenis.admite(4, Modalidad.DOBLES), "Tenis dobles admite 4 jugadores");
		assertFalse(tenis.admite(4, Modalidad.SENCILLOS), "Sencillos con 4 jugadores no es válido");
		assertFalse(tenis.admite(3, Modalidad.DOBLES), "Dobles con 3 jugadores no es válido");
		assertFalse(tenis.admite(2, null), "Sin modalidad no es válido");

		Instalacion pequena = new Instalacion(5, "Cancha 2", "TENIS", 2, false, true);
		assertFalse(pequena.admite(4, Modalidad.DOBLES), "No puede superar la capacidad");
	}

	@Test
	public void admitePadelTest() {
		setup();
		assertTrue(padel.admite(4, Modalidad.DOBLES), "Padel solo admite dobles con 4");
		assertFalse(padel.admite(2, Modalidad.SENCILLOS), "Padel no admite sencillos");
	}

	

}