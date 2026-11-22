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

	
	public static final String CARPETA = "./datos";

	
	public static List<String[]> leerCsv(String archivo) {
		List<String[]> filas = new ArrayList<String[]>();
		try {
			List<String> lineas = Files.readAllLines(Paths.get(CARPETA + archivo));
			for (int i = 1; i < lineas.size(); i++) { 
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


	public static void registrarInstalaciones(ServicioIndividual servicio) {
		for (String[] f : leerCsv("instalaciones.csv")) {
			servicio.registrarInstalacion(Integer.parseInt(f[0]), f[1], f[2], Integer.parseInt(f[3]), Boolean.parseBoolean(f[4]));
		}
	}

	
	public static Socio socio(String login) {
		return new Socio(login, login, Usuario.hash("clave"), LocalDate.of(2000, 1, 1));
	}

	public static Socio socioEntrenador(String login) {
		Socio socio = socio(login);
		try {
			
			Method vincular = Socio.class.getDeclaredMethod("setEmpleado", Empleado.class);
			vincular.setAccessible(true);
			vincular.invoke(socio, entrenador(login));
		}
		catch (Exception e) {
			fail("No se pudo convertir al socio " + login + " en entrenador");
		}
		return socio;
	}

	
	public static Socio socioDadoDeBaja(String login) {
		Socio socio = socio(login);
		socio.setActivo(false);
		return socio;
	}

	
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
