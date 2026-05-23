package com.ProgettoISA.WMS.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"RIGA_ORDINE\"")
public class RigaOrdine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"Id_Ordine\"", nullable = false)
    private Ordine ordine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"Id_Prodotto\"", nullable = false)
    private Prodotti prodotto;

    @Column(name = "\"QuantitaRichiesta\"", nullable = false)
    private Integer quantitaRichiesta;

    public RigaOrdine() {}

    public RigaOrdine(Ordine ordine, Prodotti prodotto, Integer quantitaRichiesta) {
        this.ordine = ordine;
        this.prodotto = prodotto;
        this.quantitaRichiesta = quantitaRichiesta;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ordine getOrdine() {
        return ordine;
    }

    public void setOrdine(Ordine ordine) {
        this.ordine = ordine;
    }

    public Prodotti getProdotto() {
        return prodotto;
    }

    public void setProdotto(Prodotti prodotto) {
        this.prodotto = prodotto;
    }

    public Integer getQuantitaRichiesta() {
        return quantitaRichiesta;
    }

    public void setQuantitaRichiesta(Integer quantitaRichiesta) {
        this.quantitaRichiesta = quantitaRichiesta;
    }
}
