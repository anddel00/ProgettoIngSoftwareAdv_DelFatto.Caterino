package com.ProgettoISA.WMS.Model;

import java.util.Date;

import javax.swing.text.TabableView;

import jakarta.persistence.*;

@Entity
@Table(name = "TURNIDIP")
public class TurniDip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "Id.Dipendente")
    private Utenti dipendente;

    @ManyToOne
    @JoinColumn(name = "Id.Turno")
    private Turni turno;


    public TurniDip(Utenti dipendente, Turni turno) {
        this.dipendente = dipendente;
        this.turno = turno;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utenti getDipendente() {
        return dipendente;
    }

    public void setDipendente(Utenti dipendente) {
        this.dipendente = dipendente;
    }

    public Turni getTurno() {
        return turno;
    }

    public void setTurno(Turni turno) {
        this.turno = turno;
    }
}
