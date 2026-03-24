package com.ProgettoISA.WMS.Model;

import jakarta.persistence.Entity;

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
    private List<Mappa> mappe;

    public Reparti(Long id, Long MaxX, Long MaxY, Long Temperature) {
        this.id = id;
        this.MaxX = MaxX;
        this.MaxY = MaxY;
        this.Temperature = Temperature;
    }

    public Long getId() {
        return id;
    }
    public Long getMaxX() {
        return MaxX;
    }
    public Long getMaxY() {
        return MaxY;
    }
    public Long getTemperature() {
        return Temperature;
    }
}

