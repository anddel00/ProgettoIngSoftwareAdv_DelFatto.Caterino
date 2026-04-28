package com.ProgettoISA.WMS.Service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ProgettoISA.WMS.DTO.MappaDTO;
import com.ProgettoISA.WMS.Model.Mappa;
import com.ProgettoISA.WMS.Model.Reparti;
import com.ProgettoISA.WMS.Model.Scaffali;
import com.ProgettoISA.WMS.Repository.MappaRepository;

@ExtendWith(MockitoExtension.class)
public class MappaServiceTest {

    @Mock
    private MappaRepository mappaRepository;

    @InjectMocks 
    MappaService mappaService;

    Reparti reparto1;
    Scaffali scaffale1;
    Mappa mappa1, mappa2;

    @BeforeEach
    void setUp() {
        reparto1 = new Reparti(10L, 10L, -15L, "Reparto A");
        reparto1.setId(1L);
        
        scaffale1 = new Scaffali(5, 2, 2, 2000);
        scaffale1.setId(1L);

        // Simuliamo due postazioni fisse create dal DatabaseTestRunner
        mappa1 = new Mappa(reparto1, scaffale1, 0, 0, "ORIZZONTALE");
        mappa1.setId(1L);

        mappa2 = new Mappa(reparto1, scaffale1, 8, 0, "ORIZZONTALE");
        mappa2.setId(2L);
    }

    @Test
    void getTuttaLaMappa_ritornaListaConvertitaCorrettamente() {
        // --- ARRANGE ---
        when(mappaRepository.findAll()).thenReturn(List.of(mappa1, mappa2));

        // --- ACT ---
        List<MappaDTO> result = mappaService.getTuttaLaMappa();

        // --- ASSERT ---
        assertEquals(2, result.size(), "Deve ritornare esattamente 2 DTO");
        
        // Verifichiamo che il mapping Entità -> DTO sia avvenuto con i campi giusti
        assertEquals(1L, result.get(0).getId());
        assertEquals(0, result.get(0).getCoordinataX());
        assertEquals(8, result.get(1).getCoordinataX());
        assertEquals(1L, result.get(0).getIdReparto());

        // Verifichiamo che il repository sia stato interrogato una sola volta
        verify(mappaRepository, times(1)).findAll();
        
        System.out.println("✅ UNIT TEST: Traduzione Mappa -> MappaDTO funzionante!");
    }
}