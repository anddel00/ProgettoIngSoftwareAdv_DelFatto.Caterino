package com.ProgettoISA.WMS.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "TASK")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String descrizione;
    private String tipo_task;
    
    @ManyToOne
    @JoinColumn(name = "Id.Batch")
    private BatchProdotti batch_prodotti;

    @ManyToOne
    @JoinColumn(name = "Id.ScaffaleInizio")
    private Scaffali scaffale_inizio;

    @ManyToOne
    @JoinColumn(name = "Id.ScaffaleFine") 
    private Scaffali scaffale_fine;

    private int vecchia_x;
    private int vecchia_y;
    private int nuova_x;
    private int nuova_y;
    private String stato_task;
    private int qta_spostata;

    @OneToMany(mappedBy = "task")
    private List<TaskDip> taskDip = new ArrayList<>();

    public Task() {
    }

    public Task(String descrizione, String tipo_task, BatchProdotti batch_prodotti, Scaffali scaffale_inizio, Scaffali scaffale_fine, int vecchia_x, int vecchia_y, int nuova_x, int nuova_y, String stato_task, int qta_spostata) {
        this.descrizione = descrizione;
        this.tipo_task = tipo_task;
        this.batch_prodotti = batch_prodotti;
        this.scaffale_inizio = scaffale_inizio;
        this.scaffale_fine = scaffale_fine;
        this.vecchia_x = vecchia_x;
        this.vecchia_y = vecchia_y;
        this.nuova_x = nuova_x;
        this.nuova_y = nuova_y;
        this.stato_task = stato_task;
        this.qta_spostata = qta_spostata;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getTipo_task() {
        return tipo_task;
    }

    public void setTipo_task(String tipo_task) {
        this.tipo_task = tipo_task;
    }

    public BatchProdotti getBatch_prodotti() {
        return batch_prodotti;
    }

    public void setBatch_prodotti(BatchProdotti batch_prodotti) {
        this.batch_prodotti = batch_prodotti;
    }

    public Scaffali getScaffale_inizio() {
        return scaffale_inizio;
    }

    public void setScaffale_inizio(Scaffali scaffale_inizio) {
        this.scaffale_inizio = scaffale_inizio;
    }

    public Scaffali getScaffale_fine() {
        return scaffale_fine;
    }

    public void setScaffale_fine(Scaffali scaffale_fine) {
        this.scaffale_fine = scaffale_fine;
    }

    public int getVecchia_x() {
        return vecchia_x;
    }

    public void setVecchia_x(int vecchia_x) {
        this.vecchia_x = vecchia_x;
    }

    public int getVecchia_y() {
        return vecchia_y;
    }

    public void setVecchia_y(int vecchia_y) {
        this.vecchia_y = vecchia_y;
    }

    public int getNuova_x() {
        return nuova_x;
    }

    public void setNuova_x(int nuova_x) {
        this.nuova_x = nuova_x;
    }

    public int getNuova_y() {
        return nuova_y;
    }

    public void setNuova_y(int nuova_y) {
        this.nuova_y = nuova_y;
    }

    public String getStato_task() {
        return stato_task;
    }

    public void setStato_task(String stato_task) {
        this.stato_task = stato_task;
    }

    public int getQta_spostata() {
        return qta_spostata;
    }

    public void setQta_spostata(int qta_spostata) {
        this.qta_spostata = qta_spostata;
    }

    public List<TaskDip> getTaskDip() {
        return taskDip;
    }
}
