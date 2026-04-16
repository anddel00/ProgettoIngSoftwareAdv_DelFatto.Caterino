package com.ProgettoISA.WMS.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"REPARTI\"")
public class Reparti {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Long id;

    @Column(name = "\"MaxX\"")
    private Long MaxX;

    @Column(name = "\"MaxY\"")
    private Long MaxY;

    @Column(name = "\"Temperatura\"")
    private Long Temperatura;

    @OneToMany(mappedBy = "reparto")
    private List<Mappa> mappe = new ArrayList<>();

    public Reparti() {
    }

    public Reparti(Long MaxX, Long MaxY, Long Temperatura) {
        this.MaxX = MaxX;
        this.MaxY = MaxY;
        this.Temperatura = Temperatura;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaxX() {
        return MaxX;
    }

    public void setMaxX(Long maxX) {
        MaxX = maxX;
    }

    public Long getMaxY() {
        return MaxY;
    }

    public void setMaxY(Long maxY) {
        MaxY = maxY;
    }

    public Long getTemperature() {
        return Temperatura;
    }

    public void setTemperature(Long temperature) {
        Temperatura = temperature;
    }

    public List<Mappa> getMappe() {
        return mappe;
    }
}

