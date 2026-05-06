package com.ProgettoISA.WMS.DTO;

public class ProdottiDTO {
    
    private Long id;
    private String nome;
    private long spazioUnitario;
    private float prezzo;
    private long minTemperatura;
    private long maxTemperatura;
    private float pesoUnitario;

    public ProdottiDTO(Long id, String nome, long spazioUnitario, float prezzo, long minTemperatura, long maxTemperatura, float pesoUnitario) {
        this.id = id;
        this.nome = nome;
        this.spazioUnitario = spazioUnitario;
        this.prezzo = prezzo;
        this.minTemperatura = minTemperatura;
        this.maxTemperatura = maxTemperatura;
        this.pesoUnitario = pesoUnitario;
    }

    //getter and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public long getSpazioUnitario() { return spazioUnitario; }
    public void setSpazioUnitario(long spazioUnitario) { this.spazioUnitario = spazioUnitario; }
    public float getPrezzo() { return prezzo; }
    public void setPrezzo(float prezzo) { this.prezzo = prezzo; }
    public long getMinTemperatura() { return minTemperatura; }
    public void setMinTemperatura(long minTemperatura) { this.minTemperatura = minTemperatura; }
    public long getMaxTemperatura() { return maxTemperatura; }
    public void setMaxTemperatura(long maxTemperatura) { this.maxTemperatura = maxTemperatura; }
    public float getPesoUnitario() { return pesoUnitario; }
    public void setPesoUnitario(float pesoUnitario) { this.pesoUnitario = pesoUnitario; }
}
