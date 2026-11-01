package club.modelo.conjunto;

import java.time.LocalDate;

import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;

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
    
    public void retirar() {
        if (!activo) {
            throw new ReglaDeNegocioException("El integrante ya está retirado");
        }
        activo = false;
    }
    public void registrarAmarilla() {
        if (!activo) {
            throw new ReglaDeNegocioException("El integrante no está activo");
        }
        amarillasAcumuladasInterno++;

        if (amarillasAcumuladasInterno >= 2) {
            suspendidoInterno = true;
        }
    }
    
    public void registrarRoja() {
        if (!activo) {
            throw new ReglaDeNegocioException("El integrante no está activo");
        }
        suspendidoInterno = true;
    }
    
    public void levantarSuspensionInterno() {
        suspendidoInterno = false;
        amarillasAcumuladasInterno = 0;
    }
}

