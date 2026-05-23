package com.ProgettoISA.WMS.DTO;

import java.util.List;

public class OrdineDTO {
    private Long id;
    private String dataCreazione;
    private String statoOrdine;
    private List<RigaOrdineDTO> righe;

    public OrdineDTO() {}

    public OrdineDTO(Long id, String dataCreazione, String statoOrdine, List<RigaOrdineDTO> righe) {
        this.id = id;
        this.dataCreazione = dataCreazione;
        this.statoOrdine = statoOrdine;
        this.righe = righe;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(String dataCreazione) { this.dataCreazione = dataCreazione; }
    
    public String getStatoOrdine() { return statoOrdine; }
    public void setStatoOrdine(String statoOrdine) { this.statoOrdine = statoOrdine; }
    
    public List<RigaOrdineDTO> getRighe() { return righe; }
    public void setRighe(List<RigaOrdineDTO> righe) { this.righe = righe; }
}
