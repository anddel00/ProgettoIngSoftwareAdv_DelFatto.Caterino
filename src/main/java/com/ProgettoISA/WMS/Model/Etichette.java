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
@Table(name = "\"ETICHETTE\"")
public class Etichette {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private Integer id;

    @Column(name = "\"Nome\"", nullable = false, length = 32)
    private String nome;

    @OneToMany(mappedBy = "etichetta")
    private List<EtProd> etProd = new ArrayList<>();

    public Etichette() {
    }

    public Etichette(String nome) {
        this.nome = nome;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public java.util.List<EtProd> getEtProd() {
        return etProd;
    }

}