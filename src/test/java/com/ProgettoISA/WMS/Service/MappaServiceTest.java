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

    // Rinominato per coincidere col nome della variabile in MappaService (scaffaliRepository)
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
        mappa1.setId(1L); // FONDAMENTALE per il check delle collisioni

        // Lo scaffale "Kamikaze" che vogliamo salvare (Coordinate X=0, Y=0)
        mappa2 = new Mappa(reparto1, scaffale2, 0, 0, "ORIZZONTALE");
        mappa2.setId(2L); // FONDAMENTALE

        // I DTO di input
        mappa1DTO = new MappaDTO(1L, 0, 0, scaffale1.getId(), reparto1.getId(), "VERTICALE");
        mappa2DTO = new MappaDTO(2L, 0, 0, scaffale2.getId(), reparto1.getId(), "ORIZZONTALE");
    }

    @Test
    void seScaffaleSiSovrappone_lanciaEccezione() {
        // --- 1. ARRANGE ---
        
        // A. Il Service deve recuperare il record che stiamo cercando di modificare (mappa2DTO ha id=2L)
        when(mappaRepository.findById(2L)).thenReturn(Optional.of(mappa2));

        // B. Il Service deve recuperare Reparto e Scaffale associati
        when(repartiRepository.findById(1L)).thenReturn(Optional.of(reparto1));
        when(scaffaliRepository.findById(2L)).thenReturn(Optional.of(scaffale2));

        // C. Il finto DB dice al Service che nel Reparto A c'è già "mappa1"
        when(mappaRepository.findByRepartoId(1L)).thenReturn(List.of(mappa1));

        // Creiamo il payload in ingresso
        List<MappaDTO> listaDaSalvare = List.of(mappa2DTO);

        // --- 2. ACT & ASSERT (Mettiamo alla prova l'algoritmo AABB) ---
        
        // Eseguiamo il salvataggio e ci aspettiamo un'eccezione perché sia mappa1 che mappa2 sono in (0,0)
        RuntimeException eccezione = assertThrows(RuntimeException.class, () -> {
            mappaService.salvaPosizioni(listaDaSalvare);
        });

        // Verifichiamo che l'eccezione sia scattata ESATTAMENTE per il nostro errore di sovrapposizione 
        // e non per un'altra NullPointerException casuale
        assertTrue(eccezione.getMessage().contains("sovrappone a un altro scaffale"));

        // Verifichiamo che l'algoritmo abbia bloccato tutto e il DB finto non abbia mai ricevuto il comando "saveAll"
        verify(mappaRepository, never()).saveAll(any());
    }
}