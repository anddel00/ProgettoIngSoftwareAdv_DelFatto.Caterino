package com.ProgettoISA.WMS.DTO;

public class TaskDTO {
    private Long id;
    private String descrizione;
    private String tipoTask;
    private String statoTask;
    private int quantita;
    private String nomeDipendente;
    private String idMissione;
    
    // Campi aggiuntivi per la mappa
    private Long idBatch;
    private Long idLottoOrigine;
    private Long idScaffaleInizio;
    private Long idScaffaleFine;
    private String nomeReparto;     // ricavato da scaffale_inizio.reparto (o scaffale_fine)
    private int vecchiaX;
    private int vecchiaY;
    private int vecchiaZ;
    private int nuovaX;
    private int nuovaY;
    private int nuovaZ;

    public TaskDTO(Long id, String descrizione, String tipoTask, String statoTask, int quantita) {
        this.id = id;
        this.descrizione = descrizione;
        this.tipoTask = tipoTask;
        this.statoTask = statoTask;
        this.quantita = quantita;
        this.nomeDipendente = "Non assegnato";
    }
    // 2. Nuovo Costruttore Completo (Per la tabella dell'Admin)
    public TaskDTO(Long id, String descrizione, String tipoTask, String statoTask, int quantita, String nomeDipendente) {
        this.id = id;
        this.descrizione = descrizione;
        this.tipoTask = tipoTask;
        this.statoTask = statoTask;
        this.quantita = quantita;
        this.nomeDipendente = nomeDipendente;
    }

    // Getters
    public Long getId() { return id; }
    public String getDescrizione() { return descrizione; }
    public String getTipoTask() { return tipoTask; }
    public String getStatoTask() { return statoTask; }
    public int getQuantita() { return quantita; }
    public String getNomeDipendente() { return nomeDipendente; }

    public String getIdMissione() { return idMissione; }
    public void setIdMissione(String idMissione) { this.idMissione = idMissione; }

    public Long getIdBatch() { return idBatch; }
    public void setIdBatch(Long idBatch) { this.idBatch = idBatch; }

    public Long getIdLottoOrigine() { return idLottoOrigine; }
    public void setIdLottoOrigine(Long idLottoOrigine) { this.idLottoOrigine = idLottoOrigine; }

    public Long getIdScaffaleInizio() { return idScaffaleInizio; }
    public void setIdScaffaleInizio(Long idScaffaleInizio) { this.idScaffaleInizio = idScaffaleInizio; }

    public Long getIdScaffaleFine() { return idScaffaleFine; }
    public void setIdScaffaleFine(Long idScaffaleFine) { this.idScaffaleFine = idScaffaleFine; }

    public String getNomeReparto() { return nomeReparto; }
    public void setNomeReparto(String nomeReparto) { this.nomeReparto = nomeReparto; }

    public int getNuovaX() { return nuovaX; }
    public void setNuovaX(int nuovaX) { this.nuovaX = nuovaX; }

    public int getNuovaY() { return nuovaY; }
    public void setNuovaY(int nuovaY) { this.nuovaY = nuovaY; }

    public int getNuovaZ() { return nuovaZ; }
    public void setNuovaZ(int nuovaZ) { this.nuovaZ = nuovaZ; }
    
    public int getVecchiaX() { return vecchiaX; }
    public void setVecchiaX(int vecchiaX) { this.vecchiaX = vecchiaX; }

    public int getVecchiaY() { return vecchiaY; }
    public void setVecchiaY(int vecchiaY) { this.vecchiaY = vecchiaY; }

    public int getVecchiaZ() { return vecchiaZ; }
    public void setVecchiaZ(int vecchiaZ) { this.vecchiaZ = vecchiaZ; }
}
