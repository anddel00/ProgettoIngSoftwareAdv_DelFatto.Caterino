package com.ProgettoISA.WMS.Model;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "TURNI")
public class Turni {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome_turno;
    private String orario_inizio;
    private String orario_fine;

    @OneToMany(mappedBy = "turno")
    private List<TurniDip> turniDip;

    public Turni() {
    }

    public Turni(String nome_turno, String orario_inizio, String orario_fine) {
        this.nome_turno = nome_turno;
        this.orario_inizio = orario_inizio;
        this.orario_fine = orario_fine;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome_turno() {
        return nome_turno;
    }

    public void setNome_turno(String nome_turno) {
        this.nome_turno = nome_turno;
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
