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
@Table(name = "\"ETPROD\"")
public class EtProd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"Id_Prodotto\"", nullable = false)
    private Prodotti prodotto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"Id_Etichetta\"", nullable = false)
    private Etichette etichetta;

    public EtProd() {
    }

    public EtProd(Prodotti prodotto, Etichette etichetta) {
        this.prodotto = prodotto;
        this.etichetta = etichetta;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Prodotti getProdotto() {
        return prodotto;
    }

    public void setProdotto(Prodotti prodotto) {
        this.prodotto = prodotto;
    }

    public Etichette getEtichetta() {
        return etichetta;
    }

    public void setEtichetta(Etichette etichetta) {
        this.etichetta = etichetta;
    }
}