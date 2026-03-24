package com.ProgettoISA.WMS.Model;

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
    private List<Task> task_scaffali_inizio;

    @OneToMany(mappedBy = "scaffale_fine")
    private List<Task> task_scaffali_fine;

    @OneToMany(mappedBy = "scaffale")
    private List<BatchScaffale> batch_scaffali;

    @OneToMany(mappedBy = "scaffale")
    private List<Mappa> mappa_scaffali;

    
    public Scaffali(int max_righe, int max_colonne, int max_altezza, int max_peso) {
        this.max_righe = max_righe;
        this.max_colonne = max_colonne;
        this.max_altezza = max_altezza;
        this.max_peso = max_peso;
    }

    public long getId() {
        return id;
    }
    public int getMax_righe() {
        return max_righe;
    }
    public int getMax_colonne() {
        return max_colonne;
    }
    public int getMax_altezza() {
        return max_altezza;
    }
    public int getMax_peso() {
        return max_peso;
    }
    

}
