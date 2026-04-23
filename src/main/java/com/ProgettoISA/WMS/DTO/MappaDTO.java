package com.ProgettoISA.WMS.DTO;

public class MappaDTO {
    private Long id;
    private int coordinataX;
    private int coordinataY;
    private Long idScaffale;
    private Long idReparto;
    private String orientamentoScaffale;

    public MappaDTO(Long id, int coordinataX, int coordinataY, Long idScaffale, Long idReparto, String orientamentoScaffale) {
        this.id = id;
        this.coordinataX = coordinataX;
        this.coordinataY = coordinataY;
        this.idScaffale = idScaffale;
        this.idReparto = idReparto;
        this.orientamentoScaffale = orientamentoScaffale;
    }

    // Getter e Setter
    public Long getId() { return id; }
    public int getCoordinataX() { return coordinataX; }
    public int getCoordinataY() { return coordinataY; }
    public Long getIdScaffale() { return idScaffale; }
    public Long getIdReparto() { return idReparto; }
    public String getOrientamentoScaffale() { return orientamentoScaffale; }

    public void setId(Long id) { this.id = id; }
    public void setCoordinataX(int coordinataX) { this.coordinataX = coordinataX; }
    public void setCoordinataY(int coordinataY) { this.coordinataY = coordinataY; }
    public void setIdScaffale(Long idScaffale) { this.idScaffale = idScaffale; }
    public void setIdReparto(Long idReparto) { this.idReparto = idReparto; }   
    public void setOrientamentoScaffale(String orientamentoScaffale) { this.orientamentoScaffale = orientamentoScaffale; }
}