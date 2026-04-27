package com.ProgettoISA.WMS.DTO;

public class RepartiDTO {
    private Long id;
    private Long MaxX;
    private Long MaxY;
    private Long Temperatura;
    private String Nome;

    public RepartiDTO(Long id, Long maxX, Long maxY, Long temperatura, String nome) {
        this.id = id;
        MaxX = maxX;
        MaxY = maxY;
        Temperatura = temperatura;
        Nome = nome;
    }

    // Getter
    public Long getId() { return id; }
    public Long getMaxX() { return MaxX; }
    public Long getMaxY() { return MaxY; }
    public Long getTemperatura() { return Temperatura; }
    public String getNome() { return Nome; }

    // Setter
    public void setId(Long id) { this.id = id; }
    public void setMaxX(Long maxX) { this.MaxX = maxX; }
    public void setMaxY(Long maxY) { this.MaxY = maxY; }
    public void setTemperatura(Long temperatura) { this.Temperatura = temperatura; }
    public void setNome(String nome) { this.Nome = nome; }
}