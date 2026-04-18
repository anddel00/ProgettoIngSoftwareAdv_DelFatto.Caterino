package com.ProgettoISA.WMS.DTO;

public class TaskDTO {
    private Long id;
    private String descrizione;
    private String tipoTask;
    private String statoTask;
    private int quantita;
    private String nomeDipendente;

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
}
