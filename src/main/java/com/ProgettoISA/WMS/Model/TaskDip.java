package com.ProgettoISA.WMS.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"TASKDIP\"")
public class TaskDip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "\"Id_Dipendente\"")
    private Utenti dipendente;

    @ManyToOne
    @JoinColumn(name = "\"Id_Task\"")
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
