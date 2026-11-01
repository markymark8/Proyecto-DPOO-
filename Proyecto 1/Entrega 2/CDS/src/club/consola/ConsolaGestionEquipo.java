package club.consola;

import java.time.LocalDate;

import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.IntegranteEquipo;
import club.modelo.conjunto.Posicion;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ReglaDeNegocioException;

public class ConsolaGestionEquipo {

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

    private static Socio socio(String login, int anioNacimiento) {
        return new Socio(login, "Jugador " + login, Usuario.hash("1234"), LocalDate.of(anioNacimiento, 3, 15));
    }

    private static IntegranteEquipo de(Equipo equipo, Socio socio) {
        for (IntegranteEquipo i : equipo.getIntegrantes()) {
            if (i.getSocio() == socio) {
                return i;
            }
        }
        return null;
    }

    private static void imprimir(Equipo e) {
        System.out.println("--- " + e.getNombre() + " | " + e.getDeporte().getNombre() + " | " + e.getCategoria()
                + " | cupo " + e.getCupoMaximo() + " | " + (e.isActivo() ? "activo" : "dado de baja") + " ---");
        for (IntegranteEquipo i : e.getIntegrantes()) {
            System.out.println("  #" + i.getNumeroCamiseta() + " " + i.getSocio().getNombre() + " | " + i.getPosicion()
                    + " | " + (i.isActivo() ? "activo" : "retirado")
                    + (i.isSuspendidoInterno() ? " | SUSPENDIDO" : "")
                    + " | amarillas: " + i.getAmarillasAcumuladasInterno());
        }
    }

    public static void main(String[] args) {
        Deporte futbol = new Deporte("FUTBOL", 7, 3, 1, 0, true, false, true);

        Socio[] j = new Socio[9];
        for (int i = 1; i <= 8; i++) {
            j[i] = socio("jug" + i, 2000);
        }
        Socio menor = socio("menor", 2011); // 15 anios

        Posicion[] posiciones = { null, Posicion.PORTERO, Posicion.DEFENSA, Posicion.DEFENSA, Posicion.DEFENSA,
                Posicion.MEDIOCAMPISTA, Posicion.MEDIOCAMPISTA, Posicion.DELANTERO, Posicion.DELANTERO };

        // ---------------------------------------------------------------
        System.out.println("=== Equipo pequeno (cupo 3): llenado ===");
        Equipo chico = new Equipo("EQ-001", "Chicos FC", futbol, Categoria.MAYORES, 3);
        chico.agregarIntegrante(j[1], Posicion.PORTERO, 1);
        chico.agregarIntegrante(j[2], Posicion.DEFENSA, 2);
        verificar("Con 2 de 3 no esta lleno", !chico.estaLleno());
        chico.agregarIntegrante(j[3], Posicion.DELANTERO, 3);
        verificar("Con 3 de 3 esta lleno", chico.estaLleno());
        debeRechazar("Agregar un cuarto jugador", () -> chico.agregarIntegrante(j[4], Posicion.DEFENSA, 4));
        verificar("Sigue con 3 activos tras el rechazo", chico.getIntegrantesActivos().size() == 3);
        imprimir(chico);

        // ---------------------------------------------------------------
        System.out.println("=== Equipo grande (cupo 14): reglas de ingreso ===");
        Equipo eq = new Equipo("EQ-002", "Seneca Mayores", futbol, Categoria.MAYORES, 14);
        eq.agregarIntegrante(j[1], Posicion.PORTERO, 1);
        verificar("Primer integrante agregado", eq.getIntegrantes().size() == 1);
        verificar("Camiseta 1 ya no esta disponible", !eq.camisetaDisponible(1));
        verificar("Camiseta 2 esta disponible", eq.camisetaDisponible(2));
        debeRechazar("Camiseta repetida", () -> eq.agregarIntegrante(j[2], Posicion.DEFENSA, 1));
        debeRechazar("Posicion de otro deporte (BASE en futbol)", () -> eq.agregarIntegrante(j[2], Posicion.BASE, 2));
        debeRechazar("Posicion nula", () -> eq.agregarIntegrante(j[2], null, 2));
        debeRechazar("Socio de 15 anios en MAYORES", () -> eq.agregarIntegrante(menor, Posicion.DEFENSA, 2));
        debeRechazar("Socio que ya pertenece al equipo", () -> eq.agregarIntegrante(j[1], Posicion.DEFENSA, 9));
        debeRechazar("Socio nulo", () -> eq.agregarIntegrante(null, Posicion.DEFENSA, 9));
        debeRechazar("Camiseta no positiva", () -> eq.agregarIntegrante(j[2], Posicion.DEFENSA, 0));
        verificar("Solo hay 1 integrante tras los rechazos", eq.getIntegrantes().size() == 1);

        // ---------------------------------------------------------------
        System.out.println("=== Minimo de jugadores habilitados (futbol: 7) ===");
        for (int i = 2; i <= 6; i++) {
            eq.agregarIntegrante(j[i], posiciones[i], i);
        }
        verificar("Con 6 habilitados NO cumple el minimo", !eq.tieneMinimoHabilitados());
        eq.agregarIntegrante(j[7], posiciones[7], 7);
        verificar("Con 7 habilitados cumple el minimo", eq.tieneMinimoHabilitados());

        // ---------------------------------------------------------------
        System.out.println("=== Sanciones internas ===");
        IntegranteEquipo i3 = de(eq, j[3]);
        i3.registrarAmarilla();
        verificar("Una amarilla: sigue habilitado", i3.isHabilitado());
        verificar("Una amarilla: el equipo aun cumple el minimo", eq.tieneMinimoHabilitados());
        i3.registrarAmarilla();
        verificar("Dos amarillas: queda suspendido", i3.isSuspendidoInterno() && !i3.isHabilitado());
        verificar("Habilitados bajan a 6", eq.getHabilitadosInterno().size() == 6);
        verificar("Activos siguen en 7", eq.getIntegrantesActivos().size() == 7);
        verificar("Con un suspendido ya no cumple el minimo", !eq.tieneMinimoHabilitados());
        i3.levantarSuspensionInterno();
        verificar("Suspension levantada: habilitado", i3.isHabilitado());
        verificar("Suspension levantada: amarillas en 0", i3.getAmarillasAcumuladasInterno() == 0);
        verificar("Vuelve a cumplir el minimo", eq.tieneMinimoHabilitados());

        IntegranteEquipo i4 = de(eq, j[4]);
        i4.registrarRoja();
        verificar("Roja: suspension inmediata", i4.isSuspendidoInterno());
        i4.levantarSuspensionInterno();

        // ---------------------------------------------------------------
        System.out.println("=== Retiros ===");
        eq.retirarIntegrante(j[7]);
        verificar("Activos bajan a 6", eq.getIntegrantesActivos().size() == 6);
        verificar("El retirado queda en el historial (7)", eq.getIntegrantes().size() == 7);
        verificar("Su camiseta queda libre", eq.camisetaDisponible(7));
        verificar("Con 6 ya no cumple el minimo", !eq.tieneMinimoHabilitados());
        debeRechazar("Retirar otra vez al mismo socio", () -> eq.retirarIntegrante(j[7]));
        debeRechazar("Retirar a un socio que no es del equipo", () -> eq.retirarIntegrante(j[8]));
        debeRechazar("Amarilla a un integrante retirado", () -> de(eq, j[7]).registrarAmarilla());
        debeRechazar("Retirar dos veces al mismo integrante", () -> de(eq, j[7]).retirar());

        // ---------------------------------------------------------------
        System.out.println("=== Baja del equipo ===");
        imprimir(eq);
        eq.darDeBaja();
        verificar("El equipo queda inactivo", !eq.isActivo());
        verificar("No quedan integrantes activos", eq.getIntegrantesActivos().isEmpty());
        verificar("El historial se conserva (7)", eq.getIntegrantes().size() == 7);
        debeRechazar("Agregar a un equipo dado de baja", () -> eq.agregarIntegrante(j[8], Posicion.DELANTERO, 20));
        debeRechazar("Dar de baja dos veces", () -> eq.darDeBaja());
        imprimir(eq);

        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : "HAY " + fallas + " FALLA(S): revisa las lineas marcadas");
    }
}