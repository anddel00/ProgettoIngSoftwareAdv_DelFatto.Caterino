package com.ProgettoISA.WMS.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "\"ETPROD\"")
public class Etprod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"Id.Prodotto\"", nullable = false)
    private Prodotti prodotto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"Id.Etichetta\"", nullable = false)
    private Etichette etichetta;

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