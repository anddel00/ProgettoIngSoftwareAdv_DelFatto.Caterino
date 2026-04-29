package com.ProgettoISA.WMS.DTO;

public class BatchProdottiDTO {
    private Integer id; // Nel tuo Model è Integer, non Long
    private Long idProdotto;
    private String nomeProdotto; // Aggiunto per il frontend
    private Integer quantita;
    private String scadenza;

    public BatchProdottiDTO() {
    }

    public BatchProdottiDTO(Integer id, Long idProdotto, String nomeProdotto, Integer quantita, String scadenza) {
        this.id = id;
        this.idProdotto = idProdotto;
        this.nomeProdotto = nomeProdotto;
        this.quantita = quantita;
        this.scadenza = scadenza;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getIdProdotto() {
        return idProdotto;
    }

    public void setIdProdotto(Long idProdotto) {
        this.idProdotto = idProdotto;
    }

    public String getNomeProdotto() {
        return nomeProdotto;
    }

    public void setNomeProdotto(String nomeProdotto) {
        this.nomeProdotto = nomeProdotto;
    }

    public Integer getQuantita() {
        return quantita;
    }

    public void setQuantita(Integer quantita) {
        this.quantita = quantita;
    }

    public String getScadenza() {
        return scadenza;
    }

    public void setScadenza(String scadenza) {
        this.scadenza = scadenza;
    }
}