package club.pruebas;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import club.modelo.individual.EstadisticaTenisDeMesa;
import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;
import club.pruebas.Datos;

public class EstadisticaTenisDeMesaTest {

	
	private Socio ana;

	
	public void setup() {
		ana = Datos.socio("ana");
	}


	@Test
	public void validarValoresTest() {
		setup();
		try {
			new EstadisticaTenisDeMesa(ana, null, 3, 45).validarValores();
		}
		catch (ReglaDeNegocioException e) {
			fail("No debió lanzar excepción con valores válidos");
		}
	}

	@Test
	public void validarValoresNegativosTest() {
		setup();
		try {
			new EstadisticaTenisDeMesa(ana, null, -1, 45).validarValores();
			fail("Debió lanzar excepción: sets negativos");
		}
		catch (ReglaDeNegocioException e) {
		}

		try {
			new EstadisticaTenisDeMesa(ana, null, 3, -5).validarValores();
			fail("Debió lanzar excepción: puntos negativos");
		}
		catch (ReglaDeNegocioException e) {
		}
	}

}
