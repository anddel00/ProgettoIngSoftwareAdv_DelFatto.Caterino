package com.ProgettoISA.WMS.DTO;

public class EtProdDTO {
    
    private long id;
    private long idProdotto;
    private long idEtichetta;

    public EtProdDTO() {
    }

    public EtProdDTO(long id, long idProdotto, long idEtichetta) {
        this.id = id;
        this.idProdotto = idProdotto;
        this.idEtichetta = idEtichetta;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdProdotto() {
        return idProdotto;
    }

    public void setIdProdotto(long idProdotto) {
        this.idProdotto = idProdotto;
    }

    public long getIdEtichetta() {
        return idEtichetta;
    }

    public void setIdEtichetta(long idEtichetta) {
        this.idEtichetta = idEtichetta;
    }
}
