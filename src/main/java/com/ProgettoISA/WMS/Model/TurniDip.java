package com.ProgettoISA.WMS.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    // --- NUOVI CAMPI PER LA LOGICA DEI TURNI ---
    @Column
            (name = "\"OraInizioReale\"")
    private LocalDateTime oraInizioReale;

    @Column(name = "\"OraFineReale\"")
    private LocalDateTime oraFineReale;

    public TurniDip() {
    }


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

    public LocalDateTime getOraInizioReale() {
        return oraInizioReale;
    }

    public void setOraInizioReale(LocalDateTime oraInizioReale) {
        this.oraInizioReale = oraInizioReale;
    }

    public LocalDateTime getOraFineReale() {
        return oraFineReale;
    }

    public void setOraFineReale(LocalDateTime oraFineReale) {
        this.oraFineReale = oraFineReale;
    }


}
