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

    public Prodotti() {
    }

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

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getSpazioUnitario() {
        return SpazioUnitario;
    }

    public void setSpazioUnitario(Long spazioUnitario) {
        SpazioUnitario = spazioUnitario;
    }

    public Float getPrezzo() {
        return Prezzo;
    }

    public void setPrezzo(Float prezzo) {
        Prezzo = prezzo;
    }

    public Long getMinTemperatura() {
        return MinTemperatura;
    }

    public void setMinTemperatura(Long minTemperatura) {
        MinTemperatura = minTemperatura;
    }

    public Long getMaxTemperatura() {
        return MaxTemperatura;
    }

    public void setMaxTemperatura(Long maxTemperatura) {
        MaxTemperatura = maxTemperatura;
    }

    public java.util.List<BatchProdotti> getBatchProdotti() {
        return BatchProdotti;
    }

    public java.util.List<EtProd> getEtProd() {
        return EtProd;
    }

}
