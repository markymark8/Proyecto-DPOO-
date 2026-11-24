package club.pruebas;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import club.modelo.individual.Nivel;

public class NivelTest {

	

	@Test
	public void sonCompatiblesTest() {
		assertTrue(Nivel.sonCompatibles(Nivel.INTERMEDIO, Nivel.INTERMEDIO), "Niveles iguales son compatibles");
		assertTrue(Nivel.sonCompatibles(Nivel.PRINCIPIANTE, Nivel.INTERMEDIO), "Niveles adyacentes son compatibles");
		assertTrue(Nivel.sonCompatibles(Nivel.AVANZADO, Nivel.INTERMEDIO), "Es simétrico: AVANZADO e INTERMEDIO son compatibles");
		assertFalse(Nivel.sonCompatibles(Nivel.PRINCIPIANTE, Nivel.AVANZADO), "PRINCIPIANTE y AVANZADO no son compatibles");

		try {
			Nivel.sonCompatibles(null, Nivel.AVANZADO);
			fail("Debió lanzar IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
		}
	}

}
