package com.ProgettoISA.WMS.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ProgettoISA.WMS.DTO.ProdottiDTO;
import com.ProgettoISA.WMS.Model.Prodotti;
import com.ProgettoISA.WMS.Repository.ProdottiRepository;

@Service
public class ProdottiService {

    private final ProdottiRepository prodottiRepository;
    public ProdottiService(ProdottiRepository prodottiRepository) {
        this.prodottiRepository = prodottiRepository;
    }

    public List<ProdottiDTO> getTuttiIProdotti() {
        List<Prodotti> prodotti = prodottiRepository.findAll();
        return prodotti.stream().map(p -> new ProdottiDTO(
                    p.getId(), 
                    p.getNome(), 
                    p.getSpazioUnitario(),
                    p.getPrezzo(),
                    p.getMinTemperatura(), 
                    p.getMaxTemperatura(),  
                    p.getPesoUnitario() 
                    )
                ).toList();
    }
}
