package com.ProgettoISA.WMS.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.sql.Date;

public class RegistrazioneRequestDTO {

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String cognome;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Formato email non valido")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    private String password;

    @NotBlank(message = "L'username è obbligatorio")
    private String username;

    private String data_nascita; // as string for easier parsing or can use java.sql.Date

    @NotBlank(message = "Il ruolo è obbligatorio")
    private String ruolo;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getData_nascita() { return data_nascita; }
    public void setData_nascita(String data_nascita) { this.data_nascita = data_nascita; }
    
    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }
}
