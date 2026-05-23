package com.ProgettoISA.WMS.DTO;

import java.time.LocalDate;
import java.util.List;

public class CatalogoBatchDTO {
    private Long idLotto;
    private String nomeProdotto;
    private Integer quantitaDisponibile;
    private LocalDate dataScadenza;
    private List<String> posizioni;
    private Long idLottoOrigine;
    private Long idOrdineVendita;

    public CatalogoBatchDTO(Long idLotto, String nomeProdotto, Integer quantitaDisponibile, LocalDate dataScadenza, List<String> posizioni, Long idLottoOrigine, Long idOrdineVendita) {
        this.idLotto = idLotto;
        this.nomeProdotto = nomeProdotto;
        this.quantitaDisponibile = quantitaDisponibile;
        this.dataScadenza = dataScadenza;
        this.posizioni = posizioni;
        this.idLottoOrigine = idLottoOrigine;
        this.idOrdineVendita = idOrdineVendita;
    }

    public Long getIdLotto() {
        return idLotto;
    }

    public void setIdLotto(Long idLotto) {
        this.idLotto = idLotto;
    }

    public String getNomeProdotto() {
        return nomeProdotto;
    }

    public void setNomeProdotto(String nomeProdotto) {
        this.nomeProdotto = nomeProdotto;
    }

    public Integer getQuantitaDisponibile() {
        return quantitaDisponibile;
    }

    public void setQuantitaDisponibile(Integer quantitaDisponibile) {
        this.quantitaDisponibile = quantitaDisponibile;
    }

    public LocalDate getDataScadenza() {
        return dataScadenza;
    }

    public void setDataScadenza(LocalDate dataScadenza) {
        this.dataScadenza = dataScadenza;
    }

    public List<String> getPosizioni() {
        return posizioni;
    }

    public void setPosizioni(List<String> posizioni) {
        this.posizioni = posizioni;
    }

    public Long getIdLottoOrigine() {
        return idLottoOrigine;
    }

    public void setIdLottoOrigine(Long idLottoOrigine) {
        this.idLottoOrigine = idLottoOrigine;
    }

    public Long getIdOrdineVendita() {
        return idOrdineVendita;
    }

    public void setIdOrdineVendita(Long idOrdineVendita) {
        this.idOrdineVendita = idOrdineVendita;
    }
}
