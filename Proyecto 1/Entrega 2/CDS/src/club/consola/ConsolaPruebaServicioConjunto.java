package club.consola;

import java.time.LocalDate;

import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.Posicion;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ReglaDeNegocioException;
import club.servicios.ServicioConjunto;
import club.servicios.ClubSeneca;

public class ConsolaPruebaServicioConjunto {

    private static int fallas = 0;

    private static void verificar(String descripcion, boolean condicion) {
        System.out.println((condicion ? "OK     " : "FALLA  ") + descripcion);
        if (!condicion) {
            fallas++;
        }
    }

    private static void debeRechazar(String descripcion, Runnable accion) {
        try {
            accion.run();
            verificar(descripcion + " (debia rechazar y no lo hizo)", false);
        } catch (ReglaDeNegocioException e) {
            verificar(descripcion + " -> " + e.getMessage(), true);
        }
    }

    private static Socio socio(String login) {
        return new Socio(login, "Jugador " + login, Usuario.hash("1234"), LocalDate.of(2000, 3, 15));
    }

    public static void main(String[] args) {
        ServicioConjunto servicio = new ServicioConjunto();
        Deporte futbol = new Deporte("FUTBOL", 7, 3, 1, 0, true, false, true);
        Deporte basket = new Deporte("BASKET", 5, 2, 0, 0, false, false, false);

        Socio j1 = socio("jug1");
        Socio j2 = socio("jug2");
        Socio j3 = socio("jug3");
        Socio j4 = socio("jug4");

        System.out.println("=== Deportes ===");
        servicio.registrarDeporte(futbol);
        servicio.registrarDeporte(basket);
        verificar("Hay 2 deportes", servicio.getDeportes().size() == 2);
        verificar("getDeporte(FUTBOL) lo encuentra", servicio.getDeporte("FUTBOL") == futbol);
        verificar("getDeporte de uno inexistente da null", servicio.getDeporte("TENIS") == null);
        debeRechazar("Registrar un deporte nulo", () -> servicio.registrarDeporte(null));

        System.out.println("=== Crear equipos ===");
        Equipo fc1 = servicio.crearEquipo("Seneca A", futbol, Categoria.MAYORES, 14);
        Equipo bk = servicio.crearEquipo("Seneca Baloncesto", basket, Categoria.MAYORES, 10);
        Equipo fc3 = servicio.crearEquipo("Seneca B", futbol, Categoria.MAYORES, 14);
        verificar("Primer id es EQ-001", fc1.getId().equals("EQ-001"));
        verificar("Segundo id es EQ-002", bk.getId().equals("EQ-002"));
        verificar("Tercer id es EQ-003", fc3.getId().equals("EQ-003"));
        verificar("El servicio tiene 3 equipos", servicio.getEquipos().size() == 3);
        debeRechazar("Repetir nombre, deporte y categoria",
                () -> servicio.crearEquipo("Seneca A", futbol, Categoria.MAYORES, 14));
        debeRechazar("Repetir ignorando mayusculas",
                () -> servicio.crearEquipo("SENECA a", futbol, Categoria.MAYORES, 14));
        Equipo fc4 = servicio.crearEquipo("Seneca A", futbol, Categoria.SUB_17, 14);
        verificar("Mismo nombre con otra categoria si se permite", fc4.getId().equals("EQ-004"));
        debeRechazar("Nombre nulo", () -> servicio.crearEquipo(null, futbol, Categoria.MAYORES, 14));
        debeRechazar("Deporte nulo", () -> servicio.crearEquipo("X", null, Categoria.MAYORES, 14));
        debeRechazar("Cupo en cero", () -> servicio.crearEquipo("Y", futbol, Categoria.MAYORES, 0));

        System.out.println("=== Buscar por id ===");
        verificar("getEquipo(EQ-001) devuelve fc1", servicio.getEquipo("EQ-001") == fc1);
        verificar("getEquipo(EQ-999) devuelve null", servicio.getEquipo("EQ-999") == null);

        System.out.println("=== Inscribir jugadores ===");
        servicio.inscribirJugador(fc1, j1, Posicion.PORTERO, 1);
        verificar("j1 queda en fc1", fc1.getIntegrantesActivos().size() == 1);
        debeRechazar("j1 en otro equipo de futbol", () -> servicio.inscribirJugador(fc3, j1, Posicion.DEFENSA, 2));
        debeRechazar("j1 otra vez en el mismo equipo", () -> servicio.inscribirJugador(fc1, j1, Posicion.DEFENSA, 2));
        servicio.inscribirJugador(bk, j1, Posicion.BASE, 5);
        verificar("j1 si puede estar en un equipo de otro deporte", bk.getIntegrantesActivos().size() == 1);
        servicio.inscribirJugador(fc3, j2, Posicion.DEFENSA, 2);
        debeRechazar("Equipo nulo", () -> servicio.inscribirJugador(null, j3, Posicion.DEFENSA, 3));
        debeRechazar("Socio nulo", () -> servicio.inscribirJugador(fc1, null, Posicion.DEFENSA, 3));
        debeRechazar("Posicion nula", () -> servicio.inscribirJugador(fc1, j3, null, 3));
        Equipo ajeno = new Equipo("EQ-777", "Ajeno", futbol, Categoria.MAYORES, 14);
        debeRechazar("Equipo que no es del servicio", () -> servicio.inscribirJugador(ajeno, j3, Posicion.DEFENSA, 3));

        System.out.println("=== Consultas ===");
        verificar("j1 esta en 2 equipos", servicio.getEquiposDe(j1).size() == 2);
        verificar("j2 esta en 1 equipo", servicio.getEquiposDe(j2).size() == 1);
        verificar("j3 no esta en ninguno", servicio.getEquiposDe(j3).isEmpty());
        verificar("j1 tiene equipo activo", servicio.socioTieneEquipoActivo(j1));
        verificar("j3 no tiene equipo activo", !servicio.socioTieneEquipoActivo(j3));
        debeRechazar("Consultar equipos de un socio nulo", () -> servicio.getEquiposDe(null));

        System.out.println("=== Retirar jugador ===");
        servicio.retirarJugador(fc3, j2);
        verificar("j2 ya no tiene equipo activo", !servicio.socioTieneEquipoActivo(j2));
        verificar("fc3 conserva el historial (1)", fc3.getIntegrantes().size() == 1);
        debeRechazar("Retirar a alguien que no esta", () -> servicio.retirarJugador(fc3, j2));
        debeRechazar("Retirar con equipo nulo", () -> servicio.retirarJugador(null, j1));

        System.out.println("=== Retirar de todos los equipos (baja de socio) ===");
        servicio.retirarSocioDeTodosLosEquipos(j1);
        verificar("j1 ya no esta en ningun equipo", servicio.getEquiposDe(j1).isEmpty());
        verificar("fc1 sin activos", fc1.getIntegrantesActivos().isEmpty());
        verificar("bk sin activos", bk.getIntegrantesActivos().isEmpty());
        servicio.retirarSocioDeTodosLosEquipos(j4);
        verificar("Retirar a quien no tiene equipos no falla", true);
        servicio.inscribirJugador(fc3, j1, Posicion.DELANTERO, 9);
        verificar("Tras salir, j1 puede unirse a otro equipo de futbol", servicio.socioTieneEquipoActivo(j1));

        System.out.println("=== Baja de equipos ===");
        servicio.darDeBajaEquipo(fc3);
        verificar("fc3 queda inactivo", !fc3.isActivo());
        verificar("Un equipo de baja no cuenta para el socio", servicio.getEquiposDe(j1).isEmpty());
        debeRechazar("Inscribir en un equipo dado de baja", () -> servicio.inscribirJugador(fc3, j4, Posicion.DEFENSA, 7));
        debeRechazar("Dar de baja dos veces", () -> servicio.darDeBajaEquipo(fc3));
        debeRechazar("Dar de baja un equipo ajeno", () -> servicio.darDeBajaEquipo(ajeno));

        System.out.println("=== Equipos cargados desde CSV ===");
        servicio.agregarEquipoCargado(new Equipo("EQ-010", "Cargado FC", futbol, Categoria.SUB_15, 10));
        Equipo siguiente = servicio.crearEquipo("Nuevo", basket, Categoria.SUB_15, 8);
        verificar("El contador sigue despues de lo cargado (EQ-011)", siguiente.getId().equals("EQ-011"));
        debeRechazar("Cargar un id repetido",
                () -> servicio.agregarEquipoCargado(new Equipo("EQ-010", "Otro", futbol, Categoria.SUB_13, 10)));

        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : "HAY " + fallas + " FALLA(S): revisa las lineas marcadas");
    }
}