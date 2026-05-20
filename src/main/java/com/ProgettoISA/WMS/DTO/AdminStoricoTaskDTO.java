package com.ProgettoISA.WMS.DTO;

public class AdminStoricoTaskDTO {
    private Long idTask;
    private String descrizione;
    private String tipoTask;
    private int quantita;
    private String nomeDipendente;

    // Campi per scaffali e coordinate (percorso del task)
    private Long idScaffaleInizio;
    private Long idScaffaleFine;
    private String nomeReparto;     // ricavato da scaffale_inizio.reparto (o scaffale_fine)
    private int vecchiaX;
    private int vecchiaY;
    private int vecchiaZ;
    private int nuovaX;
    private int nuovaY;
    private int nuovaZ;

    public AdminStoricoTaskDTO(Long idTask, String descrizione, String tipoTask, int quantita, String nomeDipendente) {
        this.idTask = idTask;
        this.descrizione = descrizione;
        this.tipoTask = tipoTask;
        this.quantita = quantita;
        this.nomeDipendente = nomeDipendente;
    }

    // Getters
    public Long getIdTask() { return idTask; }
    public String getDescrizione() { return descrizione; }
    public String getTipoTask() { return tipoTask; }
    public int getQuantita() { return quantita; }
    public String getNomeDipendente() { return nomeDipendente; }

    public Long getIdScaffaleInizio() { return idScaffaleInizio; }
    public void setIdScaffaleInizio(Long idScaffaleInizio) { this.idScaffaleInizio = idScaffaleInizio; }

    public Long getIdScaffaleFine() { return idScaffaleFine; }
    public void setIdScaffaleFine(Long idScaffaleFine) { this.idScaffaleFine = idScaffaleFine; }

    public String getNomeReparto() { return nomeReparto; }
    public void setNomeReparto(String nomeReparto) { this.nomeReparto = nomeReparto; }

    public int getVecchiaX() { return vecchiaX; }
    public void setVecchiaX(int vecchiaX) { this.vecchiaX = vecchiaX; }

    public int getVecchiaY() { return vecchiaY; }
    public void setVecchiaY(int vecchiaY) { this.vecchiaY = vecchiaY; }

    public int getVecchiaZ() { return vecchiaZ; }
    public void setVecchiaZ(int vecchiaZ) { this.vecchiaZ = vecchiaZ; }

    public int getNuovaX() { return nuovaX; }
    public void setNuovaX(int nuovaX) { this.nuovaX = nuovaX; }

    public int getNuovaY() { return nuovaY; }
    public void setNuovaY(int nuovaY) { this.nuovaY = nuovaY; }

    public int getNuovaZ() { return nuovaZ; }
    public void setNuovaZ(int nuovaZ) { this.nuovaZ = nuovaZ; }
}