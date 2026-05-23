package com.ProgettoISA.WMS.DTO;

public class RigaOrdineDTO {
    private Long id;
    private Long idProdotto;
    private String nomeProdotto;
    private Integer quantitaRichiesta;

    public RigaOrdineDTO() {}

    public RigaOrdineDTO(Long id, Long idProdotto, String nomeProdotto, Integer quantitaRichiesta) {
        this.id = id;
        this.idProdotto = idProdotto;
        this.nomeProdotto = nomeProdotto;
        this.quantitaRichiesta = quantitaRichiesta;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getIdProdotto() { return idProdotto; }
    public void setIdProdotto(Long idProdotto) { this.idProdotto = idProdotto; }
    
    public String getNomeProdotto() { return nomeProdotto; }
    public void setNomeProdotto(String nomeProdotto) { this.nomeProdotto = nomeProdotto; }
    
    public Integer getQuantitaRichiesta() { return quantitaRichiesta; }
    public void setQuantitaRichiesta(Integer quantitaRichiesta) { this.quantitaRichiesta = quantitaRichiesta; }
}
