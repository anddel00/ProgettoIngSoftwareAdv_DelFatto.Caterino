package com.ProgettoISA.WMS.Model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"TURNI\"")
public class Turni {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Long id;

    @Column(name = "\"OrarioInizio\"")
    private String orario_inizio;
    @Column(name = "\"OrarioFine\"")
    private String orario_fine;

    @OneToMany(mappedBy = "turno")
    private List<TurniDip> turniDip = new java.util.ArrayList<>();

    public Turni() {
    }

    public Turni(String orario_inizio, String orario_fine) {
        this.orario_inizio = orario_inizio;
        this.orario_fine = orario_fine;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrario_inizio() {
        return orario_inizio;
    }

    public void setOrario_inizio(String orario_inizio) {
        this.orario_inizio = orario_inizio;
    }

    public String getOrario_fine() {
        return orario_fine;
    }

    public void setOrario_fine(String orario_fine) {
        this.orario_fine = orario_fine;
    }

    public List<TurniDip> getTurniDip() {
        return turniDip;
    }
}
