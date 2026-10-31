package club.modelo.conjunto;

import java.time.LocalDate;

import club.modelo.usuarios.Socio;

public class IntegranteEquipo {

    private final Socio socio;
    private Posicion posicion;
    private int numeroCamiseta;
    private LocalDate fechaIngreso;
    private boolean activo;
    private int amarillasAcumuladasInterno;
    private boolean suspendidoInterno;

    public IntegranteEquipo(Socio socio, Posicion posicion, int numeroCamiseta, LocalDate fechaIngreso) {
        this.socio = socio;
        this.posicion = posicion;
        this.numeroCamiseta = numeroCamiseta;
        this.fechaIngreso = fechaIngreso;
        this.activo = true;
        this.amarillasAcumuladasInterno = 0;
        this.suspendidoInterno = false;
    }

    public Socio getSocio() { return socio; }
    public Posicion getPosicion() { return posicion; }
    public int getNumeroCamiseta() { return numeroCamiseta; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public boolean isActivo() { return activo; }
    public int getAmarillasAcumuladasInterno() { return amarillasAcumuladasInterno; }
    public boolean isSuspendidoInterno() { return suspendidoInterno; }
    
    public boolean isHabilitado() {
        return activo && !suspendidoInterno;
    }
}

