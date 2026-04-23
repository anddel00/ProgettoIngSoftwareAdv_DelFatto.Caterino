package com.ProgettoISA.WMS.DTO;

public class ScaffaliDTO{
    private long id;
    private int max_righe;
    private int max_colonne;
    private int max_altezza;
    private int max_peso;

    public ScaffaliDTO(long id, int max_righe, int max_colonne, int max_altezza, int max_peso) {
        this.id = id;
        this.max_righe = max_righe;
        this.max_colonne = max_colonne;
        this.max_altezza = max_altezza;
        this.max_peso = max_peso;
    }

    // Getters
    public long getId() { return id; }
    public int getMax_righe() { return max_righe; }
    public int getMax_colonne() { return max_colonne; }
    public int getMax_altezza() { return max_altezza; }
    public int getMax_peso() { return max_peso; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setMax_righe(int max_righe) { this.max_righe = max_righe; }
    public void setMax_colonne(int max_colonne) { this.max_colonne = max_colonne; }
    public void setMax_altezza(int max_altezza) { this.max_altezza = max_altezza; }
    public void setMax_peso(int max_peso) { this.max_peso = max_peso; }

}