package com.ProgettoISA.WMS.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"UTENTI\"")
public class Utenti {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Long id;

    @Column(name = "\"Nome\"")
    private String nome;

    @Column(name = "\"Cognome\"")
    private String cognome;

    @Column(name = "\"DataNasc\"")
    private Date data_nascita;

    @Column(name = "\"Email\"")
    private String email;

    @Column(name = "\"Password\"")
    private String password;

    @ManyToOne
    @JoinColumn(name = "\"Id_Ruolo\"") 
    private Ruoli ruolo;

    @JsonIgnore // MAGIA: Evita il loop infinito del JSON!
    @OneToMany(mappedBy = "dipendente")
    private List<TurniDip> turniDip = new ArrayList<>();

    @JsonIgnore // MAGIA: Evita il loop infinito del JSON!
    @OneToMany(mappedBy = "dipendente")
    private List<TaskDip> taskDip = new ArrayList<>();

    public Utenti() {}

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

    public Ruoli getRuolo() { //era ancora getId_ruolo -> cambiato in getRuolo
        return ruolo;
    }

    public void setRuolo(Ruoli ruolo) {
        this.ruolo = ruolo;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setData_nascita(Date data_nascita) {
        this.data_nascita = data_nascita;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<TurniDip> getTurniDip() {
        return turniDip;
    }

    public List<TaskDip> getTaskDip() {
        return taskDip;
    }



}
