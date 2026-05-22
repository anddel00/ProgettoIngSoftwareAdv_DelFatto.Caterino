package com.ProgettoISA.WMS.DTO;

import java.time.LocalDate;
import java.util.List;

public class CatalogoBatchDTO {
    private Long idLotto;
    private String nomeProdotto;
    private Integer quantitaDisponibile;
    private LocalDate dataScadenza;
    private List<String> posizioni;

    public CatalogoBatchDTO(Long idLotto, String nomeProdotto, Integer quantitaDisponibile, LocalDate dataScadenza, List<String> posizioni) {
        this.idLotto = idLotto;
        this.nomeProdotto = nomeProdotto;
        this.quantitaDisponibile = quantitaDisponibile;
        this.dataScadenza = dataScadenza;
        this.posizioni = posizioni;
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
}
