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
@Table(name = "\"MAPPA\"")
public class Mappa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"Id_Reparto\"", nullable = false)
    private Reparti reparto;

    @ManyToOne
    @JoinColumn(name = "\"Id_Scaffale\"", nullable = false)
    private Scaffali scaffale;

    @Column(name = "\"X\"", nullable = false)
    private int x;

    @Column(name = "\"Y\"", nullable = false)
    private int y;

    public Mappa() {
    }

    public Mappa(Reparti reparto, Scaffali scaffale, int x, int y) {
        this.reparto = reparto;
        this.scaffale = scaffale;
        this.x = x;
        this.y = y;
    }

    public long getId() {
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

    public Scaffali getScaffale() {
        return scaffale;
    }

    public void setScaffale(Scaffali scaffale) {
        this.scaffale = scaffale;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

}