package com.ProgettoISA.WMS.Model;

import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.persistence.Table;

@Entity
@Table(name = "REPARTI")
public class Reparti {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long MaxX;
    private Long MaxY;
    private Long Temperature;

    @OneToMany(mappedBy = "reparto")
    private List<Mappa> mappe = new ArrayList<>();

    public Reparti() {
    }

    public Reparti(Long MaxX, Long MaxY, Long Temperature) {
        this.MaxX = MaxX;
        this.MaxY = MaxY;
        this.Temperature = Temperature;
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
        return Temperature;
    }

    public void setTemperature(Long temperature) {
        Temperature = temperature;
    }

    public List<Mappa> getMappe() {
        return mappe;
    }
}

