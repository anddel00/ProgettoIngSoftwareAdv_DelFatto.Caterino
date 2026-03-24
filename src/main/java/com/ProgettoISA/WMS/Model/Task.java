package com.ProgettoISA.WMS.Model;

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
    private List<TaskDip> taskDip;


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
    public String getDescrizione() {
        return descrizione;
    }
    public String getTipo_task() {
        return tipo_task;
    }
    public BatchProdotti getBatch_prodotti() {
        return batch_prodotti;
    }
    public Scaffali getScaffale_inizio() {
        return scaffale_inizio;
    }
    public Scaffali getScaffale_fine() {
        return scaffale_fine;
    }
    public int getVecchia_x() {
        return vecchia_x;
    }
    public int getVecchia_y() {
        return vecchia_y;
    }
    public int getNuova_x() {
        return nuova_x;
    }
    public int getNuova_y() {
        return nuova_y;
    }
    public String getStato_task() {
        return stato_task;
    }
    public int getQta_spostata() {
        return qta_spostata;
    }
}
