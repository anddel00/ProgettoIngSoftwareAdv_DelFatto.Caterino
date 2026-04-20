package com.ProgettoISA.WMS.DTO;

public class CreaTaskDTO {

    private String descrizione;
    private String tipoTask;      // es. "PRELIEVO", "DEPOSITO", "SPOSTAMENTO"
    private int quantita;
    private String emailDipendente; // A quale dipendente assegnare il task

    // Posizioni opzionali (default 0 se non specificate)
    private int vecchiaX;
    private int vecchiaY;
    private int nuovaX;
    private int nuovaY;

    public CreaTaskDTO() {}

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getTipoTask() { return tipoTask; }
    public void setTipoTask(String tipoTask) { this.tipoTask = tipoTask; }

    public int getQuantita() { return quantita; }
    public void setQuantita(int quantita) { this.quantita = quantita; }

    public String getEmailDipendente() { return emailDipendente; }
    public void setEmailDipendente(String emailDipendente) { this.emailDipendente = emailDipendente; }

    public int getVecchiaX() { return vecchiaX; }
    public void setVecchiaX(int vecchiaX) { this.vecchiaX = vecchiaX; }

    public int getVecchiaY() { return vecchiaY; }
    public void setVecchiaY(int vecchiaY) { this.vecchiaY = vecchiaY; }

    public int getNuovaX() { return nuovaX; }
    public void setNuovaX(int nuovaX) { this.nuovaX = nuovaX; }

    public int getNuovaY() { return nuovaY; }
    public void setNuovaY(int nuovaY) { this.nuovaY = nuovaY; }
}