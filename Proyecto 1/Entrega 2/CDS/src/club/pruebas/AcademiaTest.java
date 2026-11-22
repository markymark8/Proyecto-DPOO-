package club.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import club.modelo.individual.Academia;
import club.modelo.individual.Disciplina;
import club.modelo.individual.InscripcionAcademia;
import club.modelo.individual.Nivel;
import club.modelo.usuarios.Socio;
import club.pruebas.Datos;

//PASO 1: Clase TEST en paquete TEST
public class AcademiaTest {

	//PASO 2: Atributos
	private Academia academia;
	private Socio ana;
	private Socio luis;
	private InscripcionAcademia inscripcionAna;

	//PASO 3: Setup (solo ana está inscrita)
	public void setup() {
		academia = new Academia(Disciplina.TENIS, 2);
		ana = Datos.socio("ana");
		luis = Datos.socio("luis");
		inscripcionAna = new InscripcionAcademia(ana, academia, Nivel.PRINCIPIANTE, 1000, LocalDate.of(2026, 11, 1));
		academia.agregarInscripcion(inscripcionAna);
	}

	//PARTE 4: Las pruebas

	@Test
	public void estaInscritoTest() {
		setup();
		assertTrue(academia.estaInscrito(ana), "Ana debe estar inscrita");
		assertFalse(academia.estaInscrito(luis), "Luis no está inscrito");
		assertTrue(academia.estaInscrito(Datos.socio("ana")), "Se compara por login, no por objeto");
	}

	@Test
	public void getInscripcionDeTest() {
		setup();
		assertEquals(inscripcionAna, academia.getInscripcionDe(ana), "Debe retornar la inscripción de Ana");
		assertNull(academia.getInscripcionDe(luis), "Luis no tiene inscripción");
	}

}
