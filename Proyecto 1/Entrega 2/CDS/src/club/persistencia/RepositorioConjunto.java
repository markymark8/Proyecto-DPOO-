package club.persistencia;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.IntegranteEquipo;
import club.modelo.conjunto.Posicion;
import club.modelo.usuarios.Socio;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.servicios.ServicioConjunto;
import java.time.format.DateTimeParseException;

//se divide en cargar y guardar (deportes,equipos,integrantes)

public class RepositorioConjunto {

    private static final String ARCHIVO_DEPORTES = "deportes.csv";
    private static final String ARCHIVO_EQUIPOS = "equipos.csv";
    private static final String ARCHIVO_INTEGRANTES = "integrantes.csv";

    private static final String[] ENC_DEPORTES = {
        "nombre", "minJugadores", "puntosVictoria", "puntosEmpate",
        "puntosDerrota", "permiteEmpate", "partidoPorSets", "usaTarjetas"
    };
    private static final String[] ENC_EQUIPOS = {
        "id", "nombre", "deporte", "categoria", "cupoMaximo", "activo"
    };
    private static final String[] ENC_INTEGRANTES = {
        "idEquipo", "loginSocio", "posicion", "numeroCamiseta",
        "fechaIngreso", "activo", "amarillas", "suspendido"
    };

    private RepositorioConjunto() {
    }


    public static void cargar(Path carpeta, ClubSeneca club) {
        ServicioConjunto servicio = club.getServicioConjunto();
        cargarDeportes(carpeta.resolve(ARCHIVO_DEPORTES), servicio);
        cargarEquipos(carpeta.resolve(ARCHIVO_EQUIPOS), servicio);
        cargarIntegrantes(carpeta.resolve(ARCHIVO_INTEGRANTES), club);
    }


private static void cargarDeportes(Path archivo, ServicioConjunto servicio) {

    List<String[]> filas = Csv.leer(archivo);

    int numeroFila = 1;

    for (String[] fila : filas) {
        numeroFila++;

        if (fila.length != 8) {
            throw new ReglaDeNegocioException(
                "Error en deportes.csv, fila " + numeroFila
                + ": se esperaban 8 columnas"
            );
        }

        try {
            String nombre = fila[0];
            int minJugadores = Integer.parseInt(fila[1]);
            int puntosVictoria = Integer.parseInt(fila[2]);
            int puntosEmpate = Integer.parseInt(fila[3]);
            int puntosDerrota = Integer.parseInt(fila[4]);

            boolean permiteEmpate = Boolean.parseBoolean(fila[5]);
            boolean partidoPorSets = Boolean.parseBoolean(fila[6]);
            boolean usaTarjetas = Boolean.parseBoolean(fila[7]);

            Deporte deporte = new Deporte(
                nombre,
                minJugadores,
                puntosVictoria,
                puntosEmpate,
                puntosDerrota,
                permiteEmpate,
                partidoPorSets,
                usaTarjetas
            );

            servicio.registrarDeporte(deporte);

        } catch (NumberFormatException e) {
            throw new ReglaDeNegocioException(
                "Error en deportes.csv, fila " + numeroFila
                + ": uno de los valores numéricos no es válido"
            );
        }
    }
}


private static void cargarEquipos(Path archivo, ServicioConjunto servicio) {

    List<String[]> filas = Csv.leer(archivo);

    int numeroFila = 1;

    for (String[] fila : filas) {
        numeroFila++;

        if (fila.length != 6) {
            throw new ReglaDeNegocioException(
                "Error en equipos.csv, fila " + numeroFila
                + ": se esperaban 6 columnas"
            );
        }

        try {
            String id = fila[0];
            String nombre = fila[1];
            Deporte deporte = servicio.getDeporte(fila[2]);

            if (deporte == null) {
                throw new ReglaDeNegocioException(
                    "Error en equipos.csv, fila " + numeroFila
                    + ": no existe el deporte " + fila[2]
                );
            }
            Categoria categoria = Categoria.valueOf(fila[3]);
            int cupoMaximo = Integer.parseInt(fila[4]);
            boolean activo = Boolean.parseBoolean(fila[5]);
            Equipo equipo = new Equipo(
                id,
                nombre,
                deporte,
                categoria,
                cupoMaximo
            );
            equipo.setActivoCargado(activo);
            servicio.agregarEquipoCargado(equipo);

        } catch (IllegalArgumentException e) {
            throw new ReglaDeNegocioException(
                "Error en equipos.csv, fila " + numeroFila
                + ": categoría o valor numérico inválido"
            );
        }
    }
}


private static void cargarIntegrantes(Path archivo, ClubSeneca club) {


    List<String[]> filas = Csv.leer(archivo);
    int numeroFila = 1;

    for (String[] fila : filas) {
        numeroFila++;

        if (fila.length != 8) {
            throw new ReglaDeNegocioException(
                "Error en integrantes.csv, fila " + numeroFila
                + ": se esperaban 8 columnas"
            );
        }
        
        try {
            Equipo equipo = club.getServicioConjunto().getEquipo(fila[0]);

            if (equipo == null) {
                throw new ReglaDeNegocioException("Error en integrantes.csv, fila " + numeroFila + ": no existe el equipo " + fila[0]);
            }
            Socio socio = club.getSocio(fila[1]);
            Posicion posicion = Posicion.valueOf(fila[2]);
            int numeroCamiseta = Integer.parseInt(fila[3]);
            LocalDate fechaIngreso = LocalDate.parse(fila[4]);

            boolean activo = Boolean.parseBoolean(fila[5]);
            int amarillas = Integer.parseInt(fila[6]);
            boolean suspendido = Boolean.parseBoolean(fila[7]);
            IntegranteEquipo integrante = new IntegranteEquipo(
                socio,
                posicion,
                numeroCamiseta,
                fechaIngreso,
                activo,
                amarillas,
                suspendido
            );
            equipo.agregarIntegranteCargado(integrante);

        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new ReglaDeNegocioException( "Error en integrantes.csv, fila " + numeroFila + ": posición, fecha o valor numérico inválido"
            );
        }
    }
}


    public static void guardar(Path carpeta, ClubSeneca club) {
        ServicioConjunto servicio = club.getServicioConjunto();
        guardarDeportes(carpeta.resolve(ARCHIVO_DEPORTES), servicio);
        guardarEquipos(carpeta.resolve(ARCHIVO_EQUIPOS), servicio);
        guardarIntegrantes(carpeta.resolve(ARCHIVO_INTEGRANTES), servicio);
    }


private static void guardarDeportes(Path archivo, ServicioConjunto servicio) {

    List<String[]> filas = new ArrayList<>();

    for (Deporte deporte : servicio.getDeportes()) {

        String[] fila = {
            deporte.getNombre(),
            String.valueOf(deporte.getMinJugadores()),
            String.valueOf(deporte.getPuntosVictoria()),
            String.valueOf(deporte.getPuntosEmpate()),
            String.valueOf(deporte.getPuntosDerrota()),
            String.valueOf(deporte.permiteEmpate()),
            String.valueOf(deporte.usaSets()),
            String.valueOf(deporte.usaTarjetas())
        };

        filas.add(fila);
    }

    Csv.escribir(archivo, ENC_DEPORTES, filas);
}



private static void guardarEquipos(Path archivo, ServicioConjunto servicio) {

    List<String[]> filas = new ArrayList<>();

    for (Equipo equipo : servicio.getEquipos()) {

        String[] fila = {
            equipo.getId(),
            equipo.getNombre(),
            equipo.getDeporte().getNombre(),
            equipo.getCategoria().name(),
            String.valueOf(equipo.getCupoMaximo()),
            String.valueOf(equipo.isActivo())
        };

        filas.add(fila);
    }

    Csv.escribir(archivo, ENC_EQUIPOS, filas);
}



private static void guardarIntegrantes(Path archivo, ServicioConjunto servicio) {

    List<String[]> filas = new ArrayList<>();
    for (Equipo equipo : servicio.getEquipos()) {
        for (IntegranteEquipo integrante : equipo.getIntegrantes()) {

            String[] fila = {
                equipo.getId(),
                integrante.getSocio().getLogin(),
                integrante.getPosicion().name(),
                String.valueOf(integrante.getNumeroCamiseta()),
                integrante.getFechaIngreso().toString(),
                String.valueOf(integrante.isActivo()),
                String.valueOf(integrante.getAmarillasAcumuladasInterno()),
                String.valueOf(integrante.isSuspendidoInterno())
            };

            filas.add(fila);
        }
    }
    Csv.escribir(archivo, ENC_INTEGRANTES, filas);
}

}