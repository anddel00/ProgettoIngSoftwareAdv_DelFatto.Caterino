package com.ProgettoISA.WMS.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"BATCHPRODOTTI\"")

public class BatchProdotti {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"Id_Prodotto\"", nullable = false)
    private Prodotti prodotto;

    @OneToMany(mappedBy="batch_prodotti")
    private List<BatchScaffale> batch_scaffali = new ArrayList<>();

    @OneToMany(mappedBy="batch_prodotti")
    private List<Task> task = new ArrayList<>();

    @Column(name = "\"Qta\"", nullable = false)
    private Integer qta;

    @Column(name = "\"Scadenza\"", nullable = false)
    private LocalDate scadenza;

    @Column(name = "\"statoLotto\"", nullable = false, columnDefinition = "varchar(255) default 'IN_ATTESA'")
    private String statoLotto = "IN_ATTESA";

    @Column(name = "\"Id_Ordine_Vendita\"")
    private Long idOrdineVendita;

    @Column(name = "\"Id_Lotto_Origine\"")
    private Long idLottoOrigine;

    public BatchProdotti() {}
    public BatchProdotti(Prodotti prodotto, Integer qta, LocalDate scadenza) {
        this.prodotto = prodotto;
        this.qta = qta;
        this.scadenza = scadenza;
        this.statoLotto = "IN_ATTESA";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prodotti getProdotto() {
        return prodotto;
    }

    public void setProdotto(Prodotti prodotto) {
        this.prodotto = prodotto;
    }

    public Integer getQta() {
        return qta;
    }

    public void setQta(Integer qta) {
        this.qta = qta;
    }

    public LocalDate getScadenza() {
        return scadenza;
    }

    public void setScadenza(LocalDate scadenza) {
        this.scadenza = scadenza;
    }

    public List<BatchScaffale> getBatch_scaffali() {
        return batch_scaffali;
    }

    public List<Task> getTask() {
        return task;
    }

    public String getStatoLotto() {
        return statoLotto;
    }

    public void setStatoLotto(String statoLotto) {
        this.statoLotto = statoLotto;
    }

    public Long getIdOrdineVendita() {
        return idOrdineVendita;
    }

    public void setIdOrdineVendita(Long idOrdineVendita) {
        this.idOrdineVendita = idOrdineVendita;
    }

    public Long getIdLottoOrigine() {
        return idLottoOrigine;
    }

    public void setIdLottoOrigine(Long idLottoOrigine) {
        this.idLottoOrigine = idLottoOrigine;
    }
}