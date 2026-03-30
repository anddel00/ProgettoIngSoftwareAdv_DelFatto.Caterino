package com.ProgettoISA.WMS.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "TASKDIP")
public class TaskDip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "\"Id.Dipendente\"") // <- Aggiungi \" \"
    private Utenti dipendente;

    @ManyToOne
    @JoinColumn(name = "\"Id.Task\"")       // <- Aggiungi \" \"
    private Task task;

    public TaskDip() {
    }

    public TaskDip(Utenti dipendente, Task task) {
        this.dipendente = dipendente;
        this.task = task;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utenti getDipendente() {
        return dipendente;
    }

    public void setDipendente(Utenti dipendente) {
        this.dipendente = dipendente;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

}
