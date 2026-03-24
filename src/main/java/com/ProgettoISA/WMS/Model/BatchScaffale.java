package com.ProgettoISA.WMS.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "\"BATCHSCAFFALE\"")
public class BatchScaffale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private Integer id;

    @Column(name = "\"Colonna\"", nullable = false)
    private Integer colonna;

    @Column(name = "\"Riga\"", nullable = false)
    private Integer riga;

    @Column(name = "\"Altezza\"", nullable = false)
    private Integer altezza;

    @Column(name = "\"Qta\"", nullable = false)
    private Integer qta;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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