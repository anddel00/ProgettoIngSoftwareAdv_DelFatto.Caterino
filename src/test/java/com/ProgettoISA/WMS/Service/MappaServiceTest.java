package com.ProgettoISA.WMS.Service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ProgettoISA.WMS.DTO.MappaDTO;
import com.ProgettoISA.WMS.Model.Mappa;
import com.ProgettoISA.WMS.Model.Reparti;
import com.ProgettoISA.WMS.Model.Scaffali;
import com.ProgettoISA.WMS.Repository.MappaRepository;
import com.ProgettoISA.WMS.Repository.RepartiRepository;
import com.ProgettoISA.WMS.Repository.ScaffaliRepository;

@ExtendWith(MockitoExtension.class)
public class MappaServiceTest {

    @Mock
    private MappaRepository mappaRepository;

    @Mock
    private ScaffaliRepository scaffaliRepository; 

    @Mock
    private RepartiRepository repartiRepository;

    @InjectMocks 
    MappaService mappaService;

    Reparti reparto1;
    Scaffali scaffale1, scaffale2;
    Mappa mappa1, mappa2;
    MappaDTO mappa1DTO, mappa2DTO;

    @BeforeEach
    void setUp() {
        // Inizializza i finti repository di Mockito prima di ogni test
        reparto1 = new Reparti(10L, 10L, -15L, "Reparto A");
        reparto1.setId(1L);
        
        scaffale1 = new Scaffali(5, 2, 2, 2000);
        scaffale1.setId(1L);
        
        scaffale2 = new Scaffali(5, 2, 2, 2000);
        scaffale2.setId(2L);

        // Lo scaffale "Ostacolo" già presente (Coordinate X=0, Y=0)
        mappa1 = new Mappa(reparto1, scaffale1, 0, 0, "VERTICALE");
        mappa1.setId(1L);

        // Lo scaffale "Kamikaze" che vogliamo salvare (Coordinate X=0, Y=0)
        mappa2 = new Mappa(reparto1, scaffale2, 0, 0, "ORIZZONTALE");
        mappa2.setId(2L);

        // I DTO di input
        mappa1DTO = new MappaDTO(1L, 0, 0, scaffale1.getId(), reparto1.getId(), "VERTICALE");
        mappa2DTO = new MappaDTO(2L, 0, 0, scaffale2.getId(), reparto1.getId(), "ORIZZONTALE");
    }

    @Test
    void seScaffaleSiSovrappone_lanciaEccezione() {
        // --- ARRANGE ---
        when(mappaRepository.findById(2L)).thenReturn(Optional.of(mappa2));
        when(repartiRepository.findById(1L)).thenReturn(Optional.of(reparto1));
        when(scaffaliRepository.findById(2L)).thenReturn(Optional.of(scaffale2));
        when(mappaRepository.findByRepartoId(1L)).thenReturn(List.of(mappa1));

        List<MappaDTO> listaDaSalvare = List.of(mappa2DTO);

        // --- ACT & ASSERT ---
        RuntimeException eccezione = assertThrows(RuntimeException.class, () -> {
            mappaService.salvaPosizioni(listaDaSalvare);
        });

        assertTrue(eccezione.getMessage().contains("sovrappone a un altro scaffale"));
        verify(mappaRepository, never()).saveAll(any());

        // IL NOSTRO FEEDBACK VISIVO
        System.out.println("✅ TEST COLLISIONI SUPERATO: Il sistema ha bloccato la sovrapposizione degli scaffali!");
    }

    @Test
    void seScaffaleEsceDaiBordi_lanciaEccezione() {
        // --- ARRANGE ---
        // Proviamo a mettere lo scaffale (lungo 5) alla coordinata X=9. 
        // 9 + 5 = 14. Il reparto finisce a 10. Deve esplodere!
        MappaDTO dtoFuoriBordo = new MappaDTO(2L, 9, 2, scaffale2.getId(), reparto1.getId(), "ORIZZONTALE");
        
        when(mappaRepository.findById(2L)).thenReturn(Optional.of(mappa2));
        when(repartiRepository.findById(1L)).thenReturn(Optional.of(reparto1));
        when(scaffaliRepository.findById(2L)).thenReturn(Optional.of(scaffale2));
        
        // NOTA: Non ci serve mockare 'findByRepartoId' qui, perché l'eccezione dei bordi 
        // bloccherà il codice prima ancora che il Service cerchi di leggere gli altri scaffali!

        List<MappaDTO> listaDaSalvare = List.of(dtoFuoriBordo);

        // --- ACT & ASSERT ---
        RuntimeException eccezione = assertThrows(RuntimeException.class, () -> {
            mappaService.salvaPosizioni(listaDaSalvare);
        });

        assertTrue(eccezione.getMessage().contains("esce dai bordi"));
        verify(mappaRepository, never()).saveAll(any());

        //FEEDBACK VISIVO
        System.out.println("✅ TEST BORDI SUPERATO: Il sistema ha impedito allo scaffale di uscire dalla mappa!");
    }

    @Test
    void salvaPosizioni_successo() {
        // --- ARRANGE ---
        // Spostiamo lo scaffale 2 in una posizione libera (5,5)
        MappaDTO dtoValido = new MappaDTO(2L, 5, 5, scaffale2.getId(), reparto1.getId(), "ORIZZONTALE");
        
        when(mappaRepository.findById(2L)).thenReturn(Optional.of(mappa2));
        when(repartiRepository.findById(reparto1.getId())).thenReturn(Optional.of(reparto1));
        when(scaffaliRepository.findById(scaffale2.getId())).thenReturn(Optional.of(scaffale2));
        // Mockiamo una lista vuota: non ci sono ostacoli in (5,5)
        when(mappaRepository.findByRepartoId(reparto1.getId())).thenReturn(List.of(mappa1));

        // --- ACT ---
        mappaService.salvaPosizioni(List.of(dtoValido));

        // --- ASSERT ---
        // Verifichiamo che il repository abbia ricevuto il comando di salvare
        verify(mappaRepository, times(1)).saveAll(any());
        System.out.println("✅ TEST SUCCESS: Scaffale spostato correttamente in zona libera!");
    }

    @Test
    void salvaPosizioni_mappaNonTrovata_lanciaEccezione() {
        // ARRANGE: ID inesistente nel DB
        MappaDTO dtoFantasma = new MappaDTO(999L, 0, 0, 1L, 1L, "ORIZZONTALE");
        when(mappaRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            mappaService.salvaPosizioni(List.of(dtoFantasma));
        });
        System.out.println("✅ TEST NOT FOUND: Gestito correttamente il caso di record inesistente!");
    }
    
}