package com.ProgettoISA.WMS.DTO;

import java.util.Date;

public class UtentiDTO {
    
    private long id;
    private String username; // AGGIUNGI QUESTO
    private String nome;
    private String cognome;
    private String email;
    private String ruolo;
    private Date data_nascita;

    public UtentiDTO() {}
    public UtentiDTO(long id,String username,  String nome, String cognome, String email, String ruolo, Date data_nascita) {
        this.id = id;
        this.username = username;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.ruolo = ruolo;
        this.data_nascita = data_nascita;
    }

    public long getId() {
        return id;
    }
    public String getUsername() { return username; }
    public String getNome() {
        return nome;
    }
    public String getCognome() {
        return cognome; 
    }
    public String getEmail() {
        return email;
    }
    public String getRuolo() {
        return ruolo;
    }
    public Date getData_nascita() {
        return data_nascita;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setUsername(String username) { this.username = username; }
    public void setNome(String nome) {
        this.nome = nome;   
    }
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }
    public void setData_nascita(Date data_nascita) {
        this.data_nascita = data_nascita;
    }
}
