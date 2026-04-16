package com.ProgettoISA.WMS.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"TURNIDIP\"")
public class TurniDip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "\"Id_Dipendente\"")
    private Utenti dipendente;

    @ManyToOne
    @JoinColumn(name = "\"Id_Turno\"")
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
