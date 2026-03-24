package com.ProgettoISA.WMS.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "\"SCAFFALI\"")
public class Scaffali {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private Integer id;

    @Column(name = "\"MaxRighe\"", nullable = false)
    private Integer maxRighe;

    @Column(name = "\"MaxColonne\"", nullable = false)
    private Integer maxColonne;

    @Column(name = "\"MaxAltezza\"", nullable = false)
    private Integer maxAltezza;

    @Column(name = "\"MaxPeso\"", nullable = false)
    private Integer maxPeso;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMaxRighe() {
        return maxRighe;
    }

    public void setMaxRighe(Integer maxRighe) {
        this.maxRighe = maxRighe;
    }

    public Integer getMaxColonne() {
        return maxColonne;
    }

    public void setMaxColonne(Integer maxColonne) {
        this.maxColonne = maxColonne;
    }

    public Integer getMaxAltezza() {
        return maxAltezza;
    }

    public void setMaxAltezza(Integer maxAltezza) {
        this.maxAltezza = maxAltezza;
    }

    public Integer getMaxPeso() {
        return maxPeso;
    }

    public void setMaxPeso(Integer maxPeso) {
        this.maxPeso = maxPeso;
    }

}