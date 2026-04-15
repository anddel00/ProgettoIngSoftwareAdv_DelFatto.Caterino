package com.ProgettoISA.WMS.DTO;

public class AdminStoricoTaskDTO {
    private Long idTask;
    private String descrizione;
    private String tipoTask;
    private int quantita;
    private String nomeDipendente; // <-- L'informazione cruciale per l'Admin!

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
}