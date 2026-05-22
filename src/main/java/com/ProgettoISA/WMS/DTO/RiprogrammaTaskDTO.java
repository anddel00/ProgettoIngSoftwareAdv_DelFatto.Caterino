package com.ProgettoISA.WMS.DTO;

public class RiprogrammaTaskDTO {

    private Long idTaskOriginale;
    private Long idNuovoScaffale;
    private Integer nuovaX;
    private Integer nuovaY;
    private Integer nuovaZ;

    public RiprogrammaTaskDTO() {}

    public RiprogrammaTaskDTO(Long idTaskOriginale, Long idNuovoScaffale, Integer nuovaX, Integer nuovaY, Integer nuovaZ) {
        this.idTaskOriginale = idTaskOriginale;
        this.idNuovoScaffale = idNuovoScaffale;
        this.nuovaX = nuovaX;
        this.nuovaY = nuovaY;
        this.nuovaZ = nuovaZ;
    }

    public Long getIdTaskOriginale() {
        return idTaskOriginale;
    }

    public void setIdTaskOriginale(Long idTaskOriginale) {
        this.idTaskOriginale = idTaskOriginale;
    }

    public Long getIdNuovoScaffale() {
        return idNuovoScaffale;
    }

    public void setIdNuovoScaffale(Long idNuovoScaffale) {
        this.idNuovoScaffale = idNuovoScaffale;
    }

    public Integer getNuovaX() {
        return nuovaX;
    }

    public void setNuovaX(Integer nuovaX) {
        this.nuovaX = nuovaX;
    }

    public Integer getNuovaY() {
        return nuovaY;
    }

    public void setNuovaY(Integer nuovaY) {
        this.nuovaY = nuovaY;
    }

    public Integer getNuovaZ() {
        return nuovaZ;
    }

    public void setNuovaZ(Integer nuovaZ) {
        this.nuovaZ = nuovaZ;
    }
}
