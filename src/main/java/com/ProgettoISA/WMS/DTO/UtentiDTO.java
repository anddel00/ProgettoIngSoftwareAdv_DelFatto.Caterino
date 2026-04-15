package com.ProgettoISA.WMS.DTO;

public class UtentiDTO {
    
    private long id;
    private String nome;
    private String cognome;
    private String email;
    private String ruolo;

    public UtentiDTO() {}
    public UtentiDTO(long id, String nome, String cognome, String email, String ruolo) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
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
    public String getEmail() {
        return email;
    }
    public String getRuolo() {
        return ruolo;
    }

    public void setId(long id) {
        this.id = id;
    }
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
}
