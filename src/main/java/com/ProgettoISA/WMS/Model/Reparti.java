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
    private Long maxX;

    @Column(name = "\"MaxY\"")
    private Long maxY;

    @Column(name = "\"Temperatura\"")
    private Long temperatura;
    
    @Column(name = "\"Nome\"")
    private String nome;

    @OneToMany(mappedBy = "reparto")
    private List<Mappa> mappe = new ArrayList<>();

    public Reparti() {
    }

    public Reparti(Long MaxX, Long MaxY, Long Temperatura, String Nome) {
        this.maxX = MaxX;
        this.maxY = MaxY;
        this.temperatura = Temperatura;
        this.nome = Nome;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaxX() {
        return maxX;
    }

    public void setMaxX(Long maxX) {
        maxX = maxX;
    }

    public Long getMaxY() {
        return maxY;
    }

    public void setMaxY(Long maxY) {
        this.maxY = maxY;
    }

    public Long getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Long temperatura) {
        this.temperatura = temperatura;
    }

    public List<Mappa> getMappe() {
        return mappe;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}

