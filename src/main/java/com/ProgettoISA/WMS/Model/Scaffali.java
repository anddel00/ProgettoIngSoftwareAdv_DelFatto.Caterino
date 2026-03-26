package com.ProgettoISA.WMS.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "SCAFFALI")
public class Scaffali {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   
    private int max_righe;
    private int max_colonne;
    private int max_altezza;
    private int max_peso;

    @OneToMany(mappedBy = "scaffale_inizio")
    private List<Task> task_scaffali_inizio = new ArrayList<>();

    @OneToMany(mappedBy = "scaffale_fine")
    private List<Task> task_scaffali_fine = new ArrayList<>();

    @OneToMany(mappedBy = "scaffale")
    private List<BatchScaffale> batch_scaffali = new ArrayList<>();

    @OneToMany(mappedBy = "scaffale")
    private List<Mappa> mappa_scaffali = new ArrayList<>();

    public Scaffali() {
    }

    public Scaffali(int max_righe, int max_colonne, int max_altezza, int max_peso) {
        this.max_righe = max_righe;
        this.max_colonne = max_colonne;
        this.max_altezza = max_altezza;
        this.max_peso = max_peso;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMax_righe() {
        return max_righe;
    }

    public void setMax_righe(int max_righe) {
        this.max_righe = max_righe;
    }

    public int getMax_colonne() {
        return max_colonne;
    }

    public void setMax_colonne(int max_colonne) {
        this.max_colonne = max_colonne;
    }

    public int getMax_altezza() {
        return max_altezza;
    }

    public void setMax_altezza(int max_altezza) {
        this.max_altezza = max_altezza;
    }

    public int getMax_peso() {
        return max_peso;
    }

    public void setMax_peso(int max_peso) {
        this.max_peso = max_peso;
    }

    public List<Task> getTask_scaffali_inizio() {
        return task_scaffali_inizio;
    }

    public List<Task> getTask_scaffali_fine() {
        return task_scaffali_fine;
    }

    public List<BatchScaffale> getBatch_scaffali() {
        return batch_scaffali;
    }

    public List<Mappa> getMappa_scaffali() {
        return mappa_scaffali;
    }

}
