package club.pruebas;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import club.modelo.usuarios.Empleado;
import club.modelo.usuarios.Entrenador;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ServicioIndividual;


public class Datos {

	/** Carpeta (desde la raíz del proyecto) donde están los CSV de prueba. */
	public static final String CARPETA = "./datosIndividual";

	// ---------------- CSV ----------------

	/** Lee un CSV de la carpeta de pruebas: una fila por línea, sin el encabezado. */
	public static List<String[]> leerCsv(String archivo) {
		List<String[]> filas = new ArrayList<String[]>();
		try {
			List<String> lineas = Files.readAllLines(Paths.get(CARPETA + archivo));
			for (int i = 1; i < lineas.size(); i++) { // la línea 0 es el encabezado
				if (!lineas.get(i).trim().isEmpty()) {
					filas.add(lineas.get(i).trim().split(","));
				}
			}
		}
		catch (IOException e) {
			fail("No se pudo leer " + CARPETA + archivo);
		}
		return filas;
	}

	/** socios.csv (login,tipo): tipo es socio, entrenador (socio que es entrenador) o baja. */
	public static Map<String, Socio> cargarSocios() {
		Map<String, Socio> socios = new HashMap<String, Socio>();
		for (String[] fila : leerCsv("socios.csv")) {
			if (fila[1].equals("entrenador")) {
				socios.put(fila[0], socioEntrenador(fila[0]));
			}
			else if (fila[1].equals("baja")) {
				socios.put(fila[0], socioDadoDeBaja(fila[0]));
			}
			else {
				socios.put(fila[0], socio(fila[0]));
			}
		}
		return socios;
	}

	/** instalaciones.csv (codigo,nombre,disciplina,capacidad,techada): las registra en el servicio. */
	public static void registrarInstalaciones(ServicioRaqueta servicio) {
		for (String[] f : leerCsv("instalaciones.csv")) {
			servicio.registrarInstalacion(Integer.parseInt(f[0]), f[1], f[2], Integer.parseInt(f[3]), Boolean.parseBoolean(f[4]));
		}
	}

	// ---------------- Usuarios ----------------

	/** Socio activo, sin bebida caliente. */
	public static Socio socio(String login) {
		return new Socio(login, login, Usuario.hash("clave"), LocalDate.of(2000, 1, 1));
	}

	/** Socio para el cual esEntrenador() es true (juega sin la regla de niveles). */
	public static Socio socioEntrenador(String login) {
		Socio socio = socio(login);
		try {
			// Socio.setEmpleado no es público (solo se puede usar dentro de su paquete)
			Method vincular = Socio.class.getDeclaredMethod("setEmpleado", Empleado.class);
			vincular.setAccessible(true);
			vincular.invoke(socio, entrenador(login));
		}
		catch (Exception e) {
			fail("No se pudo convertir al socio " + login + " en entrenador");
		}
		return socio;
	}

	/** Socio dado de baja: isActivo() es false. */
	public static Socio socioDadoDeBaja(String login) {
		Socio socio = socio(login);
		socio.setActivo(false);
		return socio;
	}

	/** Usuario para el cual esAdministrador() es true. */
	public static Usuario administrador(String login) {
		return new Usuario(login, login, Usuario.hash("clave")) {
			@Override
			public boolean esAdministrador() {
				return true;
			}
		};
	}

	public static Entrenador entrenador(String login) {
		return new Entrenador(login, login, Usuario.hash("clave"), "DESC10", 1);
	}

}
