package com.ProgettoISA.WMS.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "RUOLI")
public class Ruoli {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome_ruolo;

    @OneToMany(mappedBy = "ruolo")
    private List<Utenti> lista_ruolo_utenti = new ArrayList<>();

    public Ruoli() {
    }

    public Ruoli(String nome_ruolo) {
        this.nome_ruolo = nome_ruolo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome_ruolo() {
        return nome_ruolo;
    }

    public void setNome_ruolo(String nome_ruolo) {
        this.nome_ruolo = nome_ruolo;
    }

    public List<Utenti> getLista_ruolo_utenti() {
        return lista_ruolo_utenti;
    }
}