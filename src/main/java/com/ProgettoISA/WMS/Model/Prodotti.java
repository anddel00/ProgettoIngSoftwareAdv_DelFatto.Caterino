package com.ProgettoISA.WMS.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "PRODOTTI")
public class Prodotti {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private Long SpazioUnitario;
    private Float Prezzo;
    private Long MinTemperatura;
    private Long MaxTemperatura;

    @OneToMany(mappedBy="prodotto")
    private java.util.List<BatchProdotti> BatchProdotti;
    
    @OneToMany(mappedBy="prodotto")
    private java.util.List<EtProd> EtProd;

    public Prodotti(String nome, Long SpazioUnitario, Float Prezzo, Long MinTemperatura, Long MaxTemperatura) {
        this.nome = nome;
        this.SpazioUnitario = SpazioUnitario;
        this.Prezzo = Prezzo;
        this.MinTemperatura = MinTemperatura;
        this.MaxTemperatura = MaxTemperatura;
    }

    public Long getId() {
        return id;
    }
    public String getNome() {
        return nome;
    }
    public Long getSpazioUnitario() {
        return SpazioUnitario;
    }
    public Float getPrezzo() {
        return Prezzo;
    }
    public Long getMinTemperatura() {
        return MinTemperatura;
    }
    public Long getMaxTemperatura() {
        return MaxTemperatura;
    }



}
