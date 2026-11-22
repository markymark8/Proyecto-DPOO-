package club.pruebas;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import club.modelo.individual.EstadisticaTenisPadel;
import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;
import club.pruebas.Datos;


public class EstadisticaTenisPadelTest {

	private Socio ana;

	
	public void setup() {
		ana = Datos.socio("ana");
	}

	@Test
	public void validarValoresTest() {
		setup();
		try {
			new EstadisticaTenisPadel(ana, null, 2, 12, 3, 5).validarValores();
		}
		catch (ReglaDeNegocioException e) {
			fail("No debió lanzar excepción con valores válidos");
		}
		
	}

	@Test
	public void validarValoresNegativosTest() {
		setup();
		try {
			new EstadisticaTenisPadel(ana, null, -1, 12, 3, 5).validarValores();
			fail("Debió lanzar excepción: sets negativos");
		}
		catch (ReglaDeNegocioException e) {
		}

		try {
			new EstadisticaTenisPadel(ana, null, 2, 12, -3, 5).validarValores();
			fail("Debió lanzar excepción: aces negativos");
		}
		catch (ReglaDeNegocioException e) {
		}
	}

}
