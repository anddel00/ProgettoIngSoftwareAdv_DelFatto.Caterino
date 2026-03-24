package com.ProgettoISA.WMS.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "TASKDIP")
public class TaskDip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "Id.Dipendente")
    private Utenti dipendente;

    @ManyToOne
    @JoinColumn(name = "Id.Task")
    private Task task;

    
    public TaskDip(Utenti dipendente, Task task) {
        this.dipendente = dipendente;
        this.task = task;
    }

    public long getId() {
        return id;
    }

    public Utenti getDipendente() {
        return dipendente;
    }

    public Task getTask() {
        return task;
    }
    
}
