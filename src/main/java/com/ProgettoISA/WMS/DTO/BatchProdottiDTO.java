package com.ProgettoISA.WMS.DTO;

public class BatchProdottiDTO {
    private Long id;
    private Long idProdotto;
    private Integer quantita;
    private String scadenza;

    public BatchProdottiDTO() {
    }

    public BatchProdottiDTO(Long id, Long idProdotto, Integer quantita, String scadenza) {
        this.id = id;
        this.idProdotto = idProdotto;
        this.quantita = quantita;
        this.scadenza = scadenza;
    }

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdProdotto() {
        return idProdotto;
    }

    public void setIdProdotto(Long idProdotto) {
        this.idProdotto = idProdotto;
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

