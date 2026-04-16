package com.ProgettoISA.WMS.Model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"RUOLI\"")
public class Ruoli {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Long id;

    @Column(name = "\"Nome\"")
    private String nomeRuolo;

    @JsonIgnore // MAGIA: Evita il loop infinito del JSON!
    @OneToMany(mappedBy = "ruolo")
    private List<Utenti> lista_ruolo_utenti = new ArrayList<>();

    public Ruoli() {
    }

    public Ruoli(String nomeRuolo) {
        this.nomeRuolo = nomeRuolo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeRuolo() {
        return nomeRuolo;
    }

    public void setNomeRuolo(String nomeRuolo) {
        this.nomeRuolo = nomeRuolo;
    }

    public List<Utenti> getLista_ruolo_utenti() {
        return lista_ruolo_utenti;
    }
}