package club.consola;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.IntegranteEquipo;
import club.modelo.conjunto.Posicion;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.persistencia.RepositorioConjunto;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.servicios.ServicioConjunto;

public class PruebaRepositorioConjunto {

    private static int fallas = 0;

    private static final String[] LOGINS = {
        "jug01", "jug02", "jug03", "jug04", "jug05", "jug06", "jug07", "jug08", "jug09", "jug10"
    };
    private static final LocalDate[] NACIMIENTOS = {
        LocalDate.of(2000, 3, 15), LocalDate.of(1999, 7, 22), LocalDate.of(2001, 11, 2),
        LocalDate.of(1998, 1, 30), LocalDate.of(2002, 5, 9), LocalDate.of(2003, 9, 18),
        LocalDate.of(2010, 4, 12), LocalDate.of(2010, 8, 25), LocalDate.of(1995, 12, 5),
        LocalDate.of(1997, 6, 14)
    };

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

    private static ClubSeneca clubConSocios() {
        ClubSeneca club = new ClubSeneca();
        for (int i = 0; i < LOGINS.length; i++) {
            club.registrarUsuario(new Socio(LOGINS[i], "Jugador " + LOGINS[i],
                    Usuario.hash("1234"), NACIMIENTOS[i]));
        }
        return club;
    }

    private static IntegranteEquipo buscar(Equipo equipo, String login) {
        for (IntegranteEquipo i : equipo.getIntegrantes()) {
            if (i.getSocio().getLogin().equals(login) && i.isActivo()) {
                return i;
            }
        }
        return null;
    }

    private static boolean iguales(Equipo a, Equipo b) {
        if (b == null
                || !a.getNombre().equals(b.getNombre())
                || !a.getDeporte().getNombre().equals(b.getDeporte().getNombre())
                || a.getCategoria() != b.getCategoria()
                || a.getCupoMaximo() != b.getCupoMaximo()
                || a.isActivo() != b.isActivo()
                || a.getIntegrantes().size() != b.getIntegrantes().size()) {
            return false;
        }
        for (int k = 0; k < a.getIntegrantes().size(); k++) {
            IntegranteEquipo x = a.getIntegrantes().get(k);
            IntegranteEquipo y = b.getIntegrantes().get(k);
            if (!x.getSocio().getLogin().equals(y.getSocio().getLogin())
                    || x.getPosicion() != y.getPosicion()
                    || x.getNumeroCamiseta() != y.getNumeroCamiseta()
                    || !x.getFechaIngreso().equals(y.getFechaIngreso())
                    || x.isActivo() != y.isActivo()
                    || x.getAmarillasAcumuladasInterno() != y.getAmarillasAcumuladasInterno()
                    || x.isSuspendidoInterno() != y.isSuspendidoInterno()) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException {

        // ---------- 1. Armar un club con casos variados ----------
        ClubSeneca club1 = clubConSocios();
        ServicioConjunto s1 = club1.getServicioConjunto();

        Deporte futbol = new Deporte("FUTBOL", 7, 3, 1, 0, true, false, true);
        Deporte basket = new Deporte("BASKET", 5, 2, 0, 0, false, false, false);
        Deporte voley = new Deporte("VOLEIBOL", 6, 3, 0, 0, false, true, false);
        s1.registrarDeporte(futbol);
        s1.registrarDeporte(basket);
        s1.registrarDeporte(voley);

        Equipo fc = s1.crearEquipo("Seneca FC", futbol, Categoria.MAYORES, 14);
        Equipo bk = s1.crearEquipo("Seneca Basket", basket, Categoria.MAYORES, 10);
        Equipo vl = s1.crearEquipo("Seneca Voley", voley, Categoria.MAYORES, 12);
        Equipo juv = s1.crearEquipo("Seneca Juvenil", futbol, Categoria.SUB_17, 14);
        Equipo vet = s1.crearEquipo("Veteranos FC", futbol, Categoria.MAYORES, 14);

        s1.inscribirJugador(fc, club1.getSocio("jug01"), Posicion.PORTERO, 1);
        s1.inscribirJugador(fc, club1.getSocio("jug02"), Posicion.DEFENSA, 2);
        s1.inscribirJugador(fc, club1.getSocio("jug03"), Posicion.DEFENSA, 3);
        s1.inscribirJugador(fc, club1.getSocio("jug04"), Posicion.MEDIOCAMPISTA, 8);
        s1.inscribirJugador(fc, club1.getSocio("jug05"), Posicion.DELANTERO, 9);
        s1.inscribirJugador(bk, club1.getSocio("jug01"), Posicion.BASE, 5);
        s1.inscribirJugador(bk, club1.getSocio("jug06"), Posicion.ALERO, 7);
        s1.inscribirJugador(vl, club1.getSocio("jug02"), Posicion.LIBERO, 4);
        s1.inscribirJugador(juv, club1.getSocio("jug07"), Posicion.PORTERO, 1);
        s1.inscribirJugador(juv, club1.getSocio("jug08"), Posicion.DELANTERO, 9);
        s1.inscribirJugador(vet, club1.getSocio("jug09"), Posicion.PORTERO, 1);
        s1.inscribirJugador(vet, club1.getSocio("jug10"), Posicion.DEFENSA, 2);

        buscar(fc, "jug02").registrarAmarilla();
        buscar(fc, "jug03").registrarAmarilla();
        buscar(fc, "jug03").registrarAmarilla();   // 2 amarillas -> suspendido
        s1.retirarJugador(fc, club1.getSocio("jug05"));
        s1.darDeBajaEquipo(vet);

        // ---------- 2. Guardar ----------
        System.out.println("=== Guardar ===");
        Path carpeta1 = Files.createTempDirectory("conjunto1");
        RepositorioConjunto.guardar(carpeta1, club1);
        verificar("Se creo deportes.csv", Files.exists(carpeta1.resolve("deportes.csv")));
        verificar("Se creo equipos.csv", Files.exists(carpeta1.resolve("equipos.csv")));
        verificar("Se creo integrantes.csv", Files.exists(carpeta1.resolve("integrantes.csv")));

        // ---------- 3. Cargar en un club nuevo y comparar ----------
        System.out.println("=== Cargar y comparar ===");
        ClubSeneca club2 = clubConSocios();
        RepositorioConjunto.cargar(carpeta1, club2);
        ServicioConjunto s2 = club2.getServicioConjunto();

        verificar("3 deportes cargados", s2.getDeportes().size() == 3);
        verificar("Puntos de victoria de futbol = 3", s2.getDeporte("FUTBOL").getPuntosVictoria() == 3);
        verificar("Futbol usa tarjetas", s2.getDeporte("FUTBOL").usaTarjetas());
        verificar("Voleibol usa sets", s2.getDeporte("VOLEIBOL").usaSets());
        verificar("Basket no permite empate", !s2.getDeporte("BASKET").permiteEmpate());
        verificar("5 equipos cargados", s2.getEquipos().size() == 5);
        for (Equipo original : s1.getEquipos()) {
            verificar("Equipo " + original.getId() + " igual tras cargar",
                    iguales(original, s2.getEquipo(original.getId())));
        }

        // ---------- 4. Guardar lo cargado debe dar exactamente los mismos archivos ----------
        System.out.println("=== Estabilidad (guardar -> cargar -> guardar) ===");
        Path carpeta2 = Files.createTempDirectory("conjunto2");
        RepositorioConjunto.guardar(carpeta2, club2);
        for (String nombre : new String[] { "deportes.csv", "equipos.csv", "integrantes.csv" }) {
            verificar(nombre + " idéntico en la segunda ronda",
                    Files.readString(carpeta1.resolve(nombre)).equals(Files.readString(carpeta2.resolve(nombre))));
        }

        // ---------- 5. Las reglas siguen funcionando con lo cargado ----------
        System.out.println("=== Reglas sobre datos cargados ===");
        Equipo fc2 = s2.getEquipo("EQ-001");
        Equipo juv2 = s2.getEquipo("EQ-004");
        Equipo vet2 = s2.getEquipo("EQ-005");

        verificar("El suspendido sigue suspendido", !buscar(fc2, "jug03").isHabilitado());
        verificar("Las amarillas se conservan", buscar(fc2, "jug02").getAmarillasAcumuladasInterno() == 1);
        verificar("El equipo dado de baja sigue inactivo", !vet2.isActivo());
        verificar("El retirado no cuenta como activo", fc2.getIntegrantesActivos().size() == 4);
        verificar("jug09 (equipo de baja) no tiene equipo activo",
                !s2.socioTieneEquipoActivo(club2.getSocio("jug09")));
        verificar("EQ-001 sigue sin el minimo de habilitados", !fc2.tieneMinimoHabilitados());

        debeRechazar("jug01 ya esta en un equipo de futbol",
                () -> s2.inscribirJugador(juv2, club2.getSocio("jug01"), Posicion.DEFENSA, 2));
        debeRechazar("Inscribir en equipo dado de baja",
                () -> s2.inscribirJugador(vet2, club2.getSocio("jug04"), Posicion.DEFENSA, 5));

        s2.inscribirJugador(fc2, club2.getSocio("jug05"), Posicion.DELANTERO, 9);
        verificar("El retirado puede volver y el historial se conserva", fc2.getIntegrantes().size() == 6);

        Equipo nuevo = s2.crearEquipo("Nuevo", s2.getDeporte("BASKET"), Categoria.SUB_15, 8);
        verificar("El contador sigue despues de lo cargado (EQ-006)", nuevo.getId().equals("EQ-006"));

        // ---------- 6. Archivos ausentes o dañados ----------
        System.out.println("=== Archivos ausentes o danados ===");
        ClubSeneca vacio = clubConSocios();
        RepositorioConjunto.cargar(carpeta1.resolve("noexiste"), vacio);
        verificar("Carpeta sin archivos carga vacia", vacio.getServicioConjunto().getEquipos().isEmpty());

        Path malo = Files.createTempDirectory("conjuntoMalo");
        Files.copy(carpeta1.resolve("deportes.csv"), malo.resolve("deportes.csv"));
        Files.copy(carpeta1.resolve("equipos.csv"), malo.resolve("equipos.csv"));
        String encabezado = "idEquipo;loginSocio;posicion;numeroCamiseta;fechaIngreso;activo;amarillas;suspendido\n";

        Files.writeString(malo.resolve("integrantes.csv"),
                encabezado + "EQ-001;jug01;PORTERO;1;31-10-2026;true;0;false\n");
        debeRechazar("Fecha mal escrita", () -> RepositorioConjunto.cargar(malo, clubConSocios()));

        Files.writeString(malo.resolve("integrantes.csv"), encabezado + "EQ-001;jug01\n");
        debeRechazar("Fila con columnas faltantes", () -> RepositorioConjunto.cargar(malo, clubConSocios()));

        Files.writeString(malo.resolve("integrantes.csv"),
                encabezado + "EQ-001;jug01;ARQUERO;1;2026-09-01;true;0;false\n");
        debeRechazar("Posicion inexistente", () -> RepositorioConjunto.cargar(malo, clubConSocios()));

        Files.writeString(malo.resolve("integrantes.csv"),
                encabezado + "EQ-999;jug01;PORTERO;1;2026-09-01;true;0;false\n");
        debeRechazar("Equipo inexistente", () -> RepositorioConjunto.cargar(malo, clubConSocios()));

        Files.writeString(malo.resolve("integrantes.csv"),
                encabezado + "EQ-001;fantasma;PORTERO;1;2026-09-01;true;0;false\n");
        debeRechazar("Socio inexistente", () -> RepositorioConjunto.cargar(malo, clubConSocios()));

        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : "HAY " + fallas + " FALLA(S): revisa las lineas marcadas");
    }
}