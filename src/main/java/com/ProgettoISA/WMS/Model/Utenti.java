package com.ProgettoISA.WMS.Model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "UTENTI")
public class Utenti {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String cognome;
    private Date data_nascita;
    private String email;
    private String password;

    @ManyToOne
    @JoinColumn(name = "Id.Ruolo") 
    private Ruoli ruolo;

    @OneToMany(mappedBy = "turni_dipendente")
    private List<TurniDip> turniDip;    

    @OneToMany(mappedBy = "task_dipendente")
    private List<TaskDip> taskDip;


    public Utenti(String nome, String cognome, Date data_nascita, String email, String password, Ruoli ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.data_nascita = data_nascita;
        this.email = email;
        this.password = password;
        this.ruolo = ruolo;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public Date getData_nascita() {
        return data_nascita;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Ruoli getId_ruolo() {
        return ruolo;
    }

}
