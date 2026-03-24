package com.ProgettoISA.WMS.Model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "\"BATCHPRODOTTI\"")
public class Batchprodotti {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"Id.Prodotto\"", nullable = false)
    private Prodotti prodotto;

    @Column(name = "\"Qta\"", nullable = false)
    private Integer qta;

    @Column(name = "\"Scadenza\"", nullable = false)
    private LocalDate scadenza;

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

    public Integer getQta() {
        return qta;
    }

    public void setQta(Integer qta) {
        this.qta = qta;
    }

    public LocalDate getScadenza() {
        return scadenza;
    }

    public void setScadenza(LocalDate scadenza) {
        this.scadenza = scadenza;
    }

}