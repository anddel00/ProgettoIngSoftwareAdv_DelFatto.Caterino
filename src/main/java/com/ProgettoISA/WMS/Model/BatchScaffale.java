package com.ProgettoISA.WMS.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "\"BATCHSCAFFALE\"")
public class BatchScaffale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "\"Id.Scaffale\"", nullable = false)
    private Scaffali scaffale;

    @ManyToOne
    @JoinColumn(name = "\"Id.Batch\"", nullable = false)
    private BatchProdotti batch_prodotti;

    @Column(name = "\"Colonna\"", nullable = false)
    private Integer colonna;

    @Column(name = "\"Riga\"", nullable = false)
    private Integer riga;

    @Column(name = "\"Altezza\"", nullable = false)
    private Integer altezza;

    @Column(name = "\"Qta\"", nullable = false)
    private Integer qta;

    public BatchScaffale() {
    }

    public BatchScaffale(Scaffali scaffale, BatchProdotti batch_prodotti, Integer colonna, Integer riga, Integer altezza, Integer qta) {
        this.scaffale = scaffale;
        this.batch_prodotti = batch_prodotti;
        this.colonna = colonna;
        this.riga = riga;
        this.altezza = altezza;
        this.qta = qta;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Scaffali getScaffale() {
        return scaffale;
    }

    public void setScaffale(Scaffali scaffale) {
        this.scaffale = scaffale;
    }

    public BatchProdotti getBatch_prodotti() {
        return batch_prodotti;
    }

    public void setBatch_prodotti(BatchProdotti batch_prodotti) {
        this.batch_prodotti = batch_prodotti;
    }

    public Integer getColonna() {
        return colonna;
    }

    public void setColonna(Integer colonna) {
        this.colonna = colonna;
    }

    public Integer getRiga() {
        return riga;
    }

    public void setRiga(Integer riga) {
        this.riga = riga;
    }

    public Integer getAltezza() {
        return altezza;
    }

    public void setAltezza(Integer altezza) {
        this.altezza = altezza;
    }

    public Integer getQta() {
        return qta;
    }

    public void setQta(Integer qta) {
        this.qta = qta;
    }

}