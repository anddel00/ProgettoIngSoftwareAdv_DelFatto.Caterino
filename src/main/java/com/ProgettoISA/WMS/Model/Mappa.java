package com.ProgettoISA.WMS.Model;

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
@Table(name = "\"MAPPA\"")
public class Mappa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"Id_Reparto\"", nullable = false)
    private Reparti reparto;

    @ManyToOne
    @JoinColumn(name = "\"Id_Scaffale\"", nullable = false)
    private Scaffali scaffale;

    @Column(name = "\"X\"", nullable = false)
    private int x;

    @Column(name = "\"Y\"", nullable = false)
    private int y;

    @Column(name = "\"OrientamentoScaffale\"", nullable = false)
    private String orientamentoScaffale;

    @OneToMany(mappedBy = "mappa")
    private List<BatchScaffale> listabatch = new ArrayList<>();

    @OneToMany(mappedBy = "scaffale_inizio")
    private List<Task> task_scaffali_inizio = new ArrayList<>();

    @OneToMany(mappedBy = "scaffale_fine")
    private List<Task> task_scaffali_fine = new ArrayList<>();

    public Mappa() {
    }

    public Mappa(Reparti reparto, Scaffali scaffale, int x, int y, String orientamentoScaffale) {
        this.reparto = reparto;
        this.scaffale = scaffale;
        this.x = x;
        this.y = y;
        this.orientamentoScaffale = orientamentoScaffale;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Reparti getReparto() {
        return reparto;
    }

    public void setReparto(Reparti reparto) {
        this.reparto = reparto;
    }

    public Scaffali getScaffale() {
        return scaffale;
    }

    public void setScaffale(Scaffali scaffale) {
        this.scaffale = scaffale;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getOrientamentoScaffale() {
        return orientamentoScaffale;
    }

    public void setOrientamentoScaffale(String orientamentoScaffale) {
        this.orientamentoScaffale = orientamentoScaffale;
    }

    public List<Task> getTask_scaffali_inizio() {
        return task_scaffali_inizio;
    }

    public List<Task> getTask_scaffali_fine() {
        return task_scaffali_fine;
    }
}