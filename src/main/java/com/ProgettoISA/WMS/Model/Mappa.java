package com.ProgettoISA.WMS.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "\"MAPPA\"")
public class Mappa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"Id.Reparto\"", nullable = false)
    private Reparti reparto;

    @Column(name = "\"X\"", nullable = false)
    private Integer x;

    @Column(name = "\"Y\"", nullable = false)
    private Integer y;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Reparti getReparto() {
        return reparto;
    }

    public void setReparto(Reparti reparto) {
        this.reparto = reparto;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

}