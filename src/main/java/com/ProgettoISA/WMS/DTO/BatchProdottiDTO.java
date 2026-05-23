package com.ProgettoISA.WMS.DTO;

public class BatchProdottiDTO {
    private Long id;
    private Long idProdotto;
    private Integer quantita;
    private String scadenza;
    private Long idLottoOrigine;

    public BatchProdottiDTO() {
    }

    public BatchProdottiDTO(Long id, Long idProdotto, Integer quantita, String scadenza, Long idLottoOrigine) {
        this.id = id;
        this.idProdotto = idProdotto;
        this.quantita = quantita;
        this.scadenza = scadenza;
        this.idLottoOrigine = idLottoOrigine;
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

    public Long getIdLottoOrigine() {
        return idLottoOrigine;
    }

    public void setIdLottoOrigine(Long idLottoOrigine) {
        this.idLottoOrigine = idLottoOrigine;
    }
}

