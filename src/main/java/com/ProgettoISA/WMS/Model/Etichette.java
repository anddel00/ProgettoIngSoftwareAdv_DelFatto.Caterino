package com.ProgettoISA.WMS.Model;

import jakarta.persistence.*;

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
    private java.util.List<EtProd> etProd;

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

}