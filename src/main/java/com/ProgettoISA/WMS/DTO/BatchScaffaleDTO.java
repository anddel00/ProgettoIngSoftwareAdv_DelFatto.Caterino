package com.ProgettoISA.WMS.DTO;

public class BatchScaffaleDTO {
    
    private Long id;
    private Long idMappa;
    private Long idBatchProdotti;
    private Integer colonna;
    private Integer riga;
    private Integer altezza;
    private Integer qta;

    public BatchScaffaleDTO(Long id, Long idMappa, Long idBatchProdotti, Integer colonna, Integer riga, Integer altezza, Integer qta) {
        this.id = id;
        this.idMappa = idMappa;
        this.idBatchProdotti = idBatchProdotti;
        this.colonna = colonna;
        this.riga = riga;
        this.altezza = altezza;
        this.qta = qta;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdMappa() { return idMappa; }
    public void setIdMappa(Long idMappa) { this.idMappa = idMappa; }
    public Long getIdBatchProdotti() { return idBatchProdotti; }
    public void setIdBatchProdotti(Long idBatchProdotti) { this.idBatchProdotti = idBatchProdotti; }
    public Integer getColonna() { return colonna; }
    public void setColonna(Integer colonna) { this.colonna = colonna; }
    public Integer getRiga() { return riga; }
    public void setRiga(Integer riga) { this.riga = riga; }
    public Integer getAltezza() { return altezza; }
    public void setAltezza(Integer altezza) { this.altezza = altezza; }
    public Integer getQta() { return qta; }
    public void setQta(Integer qta) { this.qta = qta; }

}