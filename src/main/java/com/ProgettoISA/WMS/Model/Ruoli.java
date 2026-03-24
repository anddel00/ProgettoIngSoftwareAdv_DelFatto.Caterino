package com.ProgettoISA.WMS.Model;

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
    private List<Utenti> lista_ruolo_utenti;
}