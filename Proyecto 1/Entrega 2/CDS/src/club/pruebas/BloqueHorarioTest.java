package club.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import club.modelo.individual.BloqueHorario;


public class BloqueHorarioTest {

	
	private BloqueHorario bloque;
	private LocalDate dia;

	//Pongo como prueba la fehca 1 de diciembre de 10 am a 11:30 am
	public void setup() {
		dia = LocalDate.of(2026, 12, 1);
		bloque = new BloqueHorario(dia, LocalTime.of(10, 0), LocalTime.of(11, 30));
	}

	

	@Test
	public void seSolapaConTest() {
		setup();
		assertTrue(bloque.seSolapaCon(new BloqueHorario(dia, LocalTime.of(11, 0), LocalTime.of(12, 0))), "Un bloque horario que se cruza con otro tiene conflicto ");
		assertTrue(bloque.seSolapaCon(new BloqueHorario(dia, LocalTime.of(10, 15), LocalTime.of(10, 45))), "Un bloque horario contenido dentor de otro tiene conflicto");
		assertFalse(bloque.seSolapaCon(new BloqueHorario(dia, LocalTime.of(11, 30), LocalTime.of(12, 30))), "Si un bloque empieza cuando el otro termina no debe habler conflicto");
		assertFalse(bloque.seSolapaCon(new BloqueHorario(dia.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 30))), "El mismo bloque en distinto día no tiene conflicto");
	}

	@Test
	public void contieneTest() {
		setup();
		assertTrue(bloque.contiene(LocalDateTime.of(dia, LocalTime.of(10, 0))), "El inicio está incluido");
		assertTrue(bloque.contiene(LocalDateTime.of(dia, LocalTime.of(11, 29))), "Un instante dentro del bloque está contenido");
		assertFalse(bloque.contiene(LocalDateTime.of(dia, LocalTime.of(11, 30))), "El fin está excluido");
		assertFalse(bloque.contiene(LocalDateTime.of(dia, LocalTime.of(9, 59))), "Antes del inicio no está contenido");
	}

	@Test
	public void getDuracionMinutosTest() {
		setup();
		assertEquals(90, bloque.getDuracionMinutos(), "La duración debe ser 90 minutos");
	}

}
