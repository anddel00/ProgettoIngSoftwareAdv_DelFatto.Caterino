package com.ProgettoISA.WMS.DTO;

public class TaskDTO {
    private Long id;
    private String descrizione;
    private String tipoTask;
    private String statoTask;
    private int quantita;
    private String nomeDipendente;
    
    // Campi aggiuntivi per la mappa
    private Long idBatch;
    private Long idScaffaleInizio;
    private Long idScaffaleFine;
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

    public Long getIdBatch() { return idBatch; }
    public void setIdBatch(Long idBatch) { this.idBatch = idBatch; }

    public Long getIdScaffaleInizio() { return idScaffaleInizio; }
    public void setIdScaffaleInizio(Long idScaffaleInizio) { this.idScaffaleInizio = idScaffaleInizio; }

    public Long getIdScaffaleFine() { return idScaffaleFine; }
    public void setIdScaffaleFine(Long idScaffaleFine) { this.idScaffaleFine = idScaffaleFine; }

    public int getNuovaX() { return nuovaX; }
    public void setNuovaX(int nuovaX) { this.nuovaX = nuovaX; }

    public int getNuovaY() { return nuovaY; }
    public void setNuovaY(int nuovaY) { this.nuovaY = nuovaY; }

    public int getNuovaZ() { return nuovaZ; }
    public void setNuovaZ(int nuovaZ) { this.nuovaZ = nuovaZ; }
}
