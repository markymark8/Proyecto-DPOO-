package club.consola;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.List;
import club.modelo.torneos.InscripcionTorneo;
import club.modelo.torneos.Modalidad;
import club.modelo.torneos.Torneo;
import club.modelo.usuarios.Empleado;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.modelo.conjunto.Equipo;
import club.modelo.torneos.InscripcionEquipo;
import club.modelo.torneos.InscripcionIndividual;
import club.modelo.torneos.TorneoCompetitivo;
import club.modelo.conjunto.IntegranteEquipo;

public class ConsolaTorneos {

    private final ClubSeneca club;
    private final Scanner in;

    public ConsolaTorneos(ClubSeneca club, Scanner in) {
        this.club = club;
        this.in = in;
    }

    public void ejecutar(Usuario usuario) {
        boolean salir = false;
        while (!salir) {
            mostrarMenu(usuario);
            String opcion = in.nextLine().trim();
            try {
                salir = procesar(opcion, usuario);
            } catch (ReglaDeNegocioException e) {
                System.out.println(">> RECHAZADO: " + e.getMessage());
            } catch (IllegalArgumentException | DateTimeParseException e) {
                System.out.println(">> DATO INVALIDO: " + e.getMessage());
            }
        }
    }

    // MENU
    
    // Aqui se ejecutan en consola los requerimientos que se evidencian a través de la historias de usuario de la documentación
    // Se marcan abajo y se pueden comparar con la documentación
    // También abajo están divididos los roles: primero ADMINISTRADOR y luego SOCIO y EMPLEADO
    

    private void mostrarMenu(Usuario usuario) {
        System.out.println();
        System.out.println("=== TORNEOS (" + usuario.getNombre() + ") ===");
        System.out.println("1. Listar torneos");                    // T7
        System.out.println("2. Ver detalle de un torneo");          // T7
        if (usuario.esAdministrador()) {
            System.out.println("3. Crear torneo");                  // T1
            System.out.println("4. Ver cupos de un torneo");        // T9
            System.out.println("5. Liberar cupos preferenciales");  // T9
            System.out.println("6. Iniciar torneo");
            System.out.println("7. Finalizar torneo");
        } else if (usuario instanceof Socio) {
            System.out.println("3. Inscribirme en un torneo individual");   // T2, T6
            System.out.println("4. Inscribir una pareja (dobles)");         // T3
            System.out.println("5. Inscribir un equipo");                   // T3
            System.out.println("6. Retirarme de un torneo");               // T4
        } else if (usuario instanceof Empleado) {
            System.out.println("3. Inscribirme en un torneo individual");   // T5
            System.out.println("4. Retirarme de un torneo");               // T4
        }
        System.out.println("0. Volver");
        System.out.print("Opcion: ");
    }

    private boolean procesar(String opcion, Usuario usuario) {
        if (opcion.equals("0")) {
            return true;
        }
        if (opcion.equals("1")) {
            listarTorneos();
        } else if (opcion.equals("2")) {
            verDetalle();
        } else if (usuario.esAdministrador()) {
            procesarAdministrador(opcion);
        } else if (usuario instanceof Socio) {
            procesarSocio(opcion, usuario);
        } else if (usuario instanceof Empleado) {
            procesarEmpleado(opcion, usuario);
        } else {
            System.out.println("Opcion invalida.");
        }
        return false;
    }

    private void procesarAdministrador(String opcion) {
        switch (opcion) {
            case "3": crearTorneo(); break;
            case "4": verCupos(); break;
            case "5": liberarPreferenciales(); break;
            case "6": iniciarTorneo(); break;
            case "7": finalizarTorneo(); break;
            default: System.out.println("Opcion invalida.");
        }
    }

    private void procesarSocio(String opcion, Usuario usuario) {
        switch (opcion) {
            case "3": inscribirIndividual(usuario); break;
            case "4": inscribirPareja(usuario); break;
            case "5": inscribirEquipo(usuario); break;
            case "6": retirarse(usuario); break;
            default: System.out.println("Opcion invalida.");
        }
    }

    private void procesarEmpleado(String opcion, Usuario usuario) {
        switch (opcion) {
            case "3": inscribirIndividual(usuario); break;
            case "4": retirarse(usuario); break;
            default: System.out.println("Opcion invalida.");
        }
    }

    // LECTURA DE DATOS

    private String leer(String mensaje) {
        System.out.print(mensaje + ": ");
        return in.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        return Integer.parseInt(leer(mensaje));
    }

    // CONSULTA

    private void listarTorneos() {
        List<Torneo> torneos = club.getServicioTorneos().getTorneos();

        if (torneos.isEmpty()) {
            System.out.println("No hay torneos.");
            return;
        }

        for (Torneo torneo : torneos) {
            describir(torneo);
        }
    }

    private void verDetalle() {
        String id = leer("Id del torneo");
        Torneo torneo = club.getServicioTorneos().exigirTorneo(id);

        describir(torneo);

        System.out.println("\n--- INSCRIPCIONES ---");

        List<InscripcionTorneo> inscripciones = torneo.getInscripciones();

        if (inscripciones.isEmpty()) {
            System.out.println("No hay participantes inscritos.");
            return;
        }

        for (InscripcionTorneo inscripcion : inscripciones) {
            System.out.println("\nParticipante: " + inscripcion.getNombre());
            System.out.println("Identificador: " + inscripcion.getIdentificador());

            System.out.print("Integrantes: ");
            for (Usuario participante : inscripcion.getParticipantes()) {
                System.out.print(participante.getLogin() + " ");
            }

            System.out.println();
            System.out.println("Preferencial: "
                    + (inscripcion.isPreferencial() ? "Sí" : "No"));
            System.out.println("Puntaje de ranking: "
                    + inscripcion.getPuntajeRanking());
        }
    }

    private void describir(Torneo t) {
        String tipo = (t instanceof TorneoCompetitivo)
                ? "COMPETITIVO" : "RECREATIVO";
        
        if (t instanceof TorneoCompetitivo) {
            TorneoCompetitivo competitivo = (TorneoCompetitivo) t;
            System.out.println("Tarifa de inscripción: "
                    + competitivo.getTarifaInscripcion());
        }

        System.out.println("----------------------------------------");
        System.out.println("ID: " + t.getId());
        System.out.println("Nombre: " + t.getNombre());
        System.out.println("Tipo: " + tipo);
        System.out.println("Modalidad: " + t.getModalidad());
        System.out.println("Disciplina: " + t.getDisciplina());
        System.out.println("Categoría: " + t.getCategoria());
        System.out.println("Fecha: " + t.getFecha());
        System.out.println("Estado: " + t.getEstado());
        System.out.println("Inscritos: " + t.getInscripciones().size()
                + "/" + t.getCupos());
        System.out.println("----------------------------------------");
    }

    // ADMIN

    private void crearTorneo() {
        String nombre = leer("Nombre del torneo");

        String tipo = leer("Tipo (R: Recreativo / C: Competitivo)")
                .toUpperCase();

        Modalidad modalidad = Modalidad.valueOf(
                leer("Modalidad (CONJUNTO, INDIVIDUAL, DOBLES)").toUpperCase());

        String disciplina = leer("Disciplina");
        String categoria = leer("Categoría");
        int cupos = leerEntero("Cupos disponibles");
        LocalDate fecha = LocalDate.parse(
                leer("Fecha (yyyy-MM-dd)"));

        Torneo torneo;

        if (tipo.equals("R")) {
            torneo = club.getServicioTorneos().crearTorneoRecreativo(
                    nombre, modalidad, disciplina, categoria, cupos, fecha);

        } else if (tipo.equals("C")) {
            int tarifa = leerEntero("Tarifa de inscripción");

            torneo = club.getServicioTorneos().crearTorneoCompetitivo(
                    nombre, modalidad, disciplina, categoria, cupos, fecha, tarifa);

        } else {
            throw new ReglaDeNegocioException(
                    "El tipo debe ser R (recreativo) o C (competitivo)");
        }

        System.out.println(">> Torneo creado: " + torneo.getId());
    }

    private void verCupos() {
        String id = leer("Id del torneo");
        Torneo torneo = club.getServicioTorneos().exigirTorneo(id);

        int ocupados = torneo.getInscripciones().size();
        int libres = torneo.getCupos() - ocupados;

        int preferencialesOcupados = 0;

        for (InscripcionTorneo inscripcion : torneo.getInscripciones()) {
            if (inscripcion.isPreferencial()) {
                preferencialesOcupados++;
            }
        }

        int regularesOcupados = ocupados - preferencialesOcupados;

        System.out.println("\n--- DISPONIBILIDAD DE CUPOS ---");
        System.out.println("Torneo: " + torneo.getNombre());
        System.out.println("Cupos totales: " + torneo.getCupos());

        System.out.println("\nPreferenciales:");
        System.out.println("  Capacidad: " + torneo.getCuposPreferenciales());
        System.out.println("  Ocupados: " + preferencialesOcupados);

        System.out.println("\nRegulares:");
        System.out.println("  Capacidad: " + torneo.getCuposRegulares());
        System.out.println("  Ocupados: " + regularesOcupados);

        System.out.println("\nTotal ocupados: " + ocupados);
        System.out.println("Total libres: " + libres);
        System.out.println("Preferenciales liberados: "
                + (torneo.isPreferencialesLiberados() ? "Sí" : "No"));
    }

    private void liberarPreferenciales() {
        String id = leer("Id del torneo");

        club.getServicioInscripcion().liberarCuposPreferenciales(id);

        System.out.println(">> Cupos preferenciales liberados para el torneo " + id);
    }

    private void iniciarTorneo() {
        String id = leer("Id del torneo");

        club.getServicioTorneos().iniciarTorneo(id);

        System.out.println(">> Torneo " + id + " iniciado correctamente.");
    }

    private void finalizarTorneo() {
        String id = leer("Id del torneo");

        club.getServicioTorneos().finalizarTorneo(id);

        System.out.println(">> Torneo " + id + " finalizado correctamente.");
    }
    // SOCIO Y EMPLEADO

    private void inscribirIndividual(Usuario usuario) {
        String id = leer("Id del torneo");

        InscripcionIndividual inscripcion = club.getServicioInscripcion()
                .inscribirJugador(id, usuario);

        System.out.println(">> " + usuario.getNombre()
                + " inscrito en el torneo.");

        System.out.println("Preferencial: "
                + (inscripcion.isPreferencial() ? "Sí" : "No"));
    }

    private void inscribirPareja(Usuario usuario) {
        String id = leer("Id del torneo");
        String login = leer("Login de la pareja");

        Usuario otro = club.buscarUsuario(login);

        if (otro == null) {
            throw new ReglaDeNegocioException(
                    "No existe el usuario " + login);
        }

        InscripcionIndividual inscripcion = club.getServicioInscripcion()
                .inscribirPareja(id, usuario, otro);

        System.out.println(">> Pareja inscrita en el torneo.");
        System.out.println("Participantes: " + inscripcion.getNombre());
        System.out.println("Preferencial: "
                + (inscripcion.isPreferencial() ? "Sí" : "No"));
    }

    private void inscribirEquipo(Usuario usuario) {
        String id = leer("Id del torneo");
        String idEquipo = leer("Id del equipo");

        Equipo equipo = club.getServicioConjunto().getEquipo(idEquipo);

        if (equipo == null) {
            throw new ReglaDeNegocioException(
                    "No existe el equipo " + idEquipo);
        }
        
        boolean esIntegrante = false;
        for (IntegranteEquipo integrante : equipo.getIntegrantesActivos()) {
            if (integrante.getSocio().getLogin().equals(usuario.getLogin())) {
                esIntegrante = true;
            }
        }
        if (!esIntegrante) {
            throw new ReglaDeNegocioException(
                "Solo un integrante activo puede inscribir al equipo " + idEquipo);
        }

        InscripcionEquipo inscripcion = club.getServicioInscripcion()
                .inscribirEquipo(id, equipo);

        Torneo torneo = club.getServicioTorneos().exigirTorneo(id);

        System.out.println(">> Equipo " + equipo.getNombre()
                + " inscrito en el torneo.");
        System.out.println("Integrantes inscritos: "
                + inscripcion.getJugadores().size());
        System.out.println("Cupos restantes: "
                + (torneo.getCupos() - torneo.getInscripciones().size()));
    }

    private void retirarse(Usuario usuario) {
        String idTorneo = leer("Id del torneo");

        System.out.println("\n¿Qué inscripción deseas retirar?");
        System.out.println("1. Inscripción individual");
        System.out.println("2. Inscripción de pareja");
        System.out.println("3. Inscripción de equipo");

        String opcion = leer("Opción");

        String identificador;

        switch (opcion) {
            case "1":
                identificador = usuario.getLogin();
                break;

            case "2":
                String loginPareja = leer("Login de tu pareja");

                Usuario pareja = club.buscarUsuario(loginPareja);

                if (pareja == null) {
                    throw new ReglaDeNegocioException(
                            "No existe el usuario " + loginPareja);
                }

                if (loginPareja.equals(usuario.getLogin())) {
                    throw new ReglaDeNegocioException(
                            "No puedes seleccionar tu propio login como pareja");
                }
                if (usuario.getLogin().compareTo(loginPareja) < 0) {
                    identificador = usuario.getLogin() + "-" + loginPareja;
                } else {
                    identificador = loginPareja + "-" + usuario.getLogin();
                }
                break;

            case "3":
                if (!(usuario instanceof Socio)) {
                    throw new ReglaDeNegocioException(
                            "Solo los socios pueden retirar una inscripción de equipo");
                }

                String idEquipo = leer("Id del equipo");
                Equipo equipo = club.getServicioConjunto().getEquipo(idEquipo);

                if (equipo == null) {
                    throw new ReglaDeNegocioException(
                            "No existe el equipo " + idEquipo);
                }

                boolean esIntegrante = false;

                for (IntegranteEquipo integrante
                        : equipo.getIntegrantesActivos()) {

                    if (integrante.getSocio().getLogin()
                            .equals(usuario.getLogin())) {
                        esIntegrante = true;
                        break;
                    }
                }

                if (!esIntegrante) {
                    throw new ReglaDeNegocioException(
                            "No perteneces al equipo " + idEquipo);
                }

                identificador = equipo.getId();
                break;

            default:
                throw new ReglaDeNegocioException(
                        "Opción de inscripción inválida");
        }

        club.getServicioInscripcion().retirarInscripcion(
                idTorneo, identificador);

        System.out.println(">> Inscripción retirada correctamente.");
    }
}
