package com.ProgettoISA.WMS.Integration;

import com.ProgettoISA.WMS.DTO.RepartiDTO;
import com.ProgettoISA.WMS.Model.Reparti;
import com.ProgettoISA.WMS.Repository.MappaRepository;
import com.ProgettoISA.WMS.Service.RepartiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class RepartiServiceIT {

    @Autowired
    private RepartiService repartiService;

    @Autowired
    private MappaRepository mappaRepository;

    @Test
    void testCreazionePlanimetria() {
        // Arrange
        RepartiDTO dto = new RepartiDTO(null, 20L, 20L, 22L, "Reparto IT Test");

        long countPrima = mappaRepository.count();

        // Act
        Reparti salvato = repartiService.creaRepartoConPlanimetria(dto);

        // Assert
        long countDopo = mappaRepository.count();

        // Formula: 
        // corridoioCentrale = (20 % 2 == 0) ? 2 : 3 = 2
        // larghezzaScaffale = (20 - 2) / 2 = 9
        // Ciclo su Y da 0 a 19 (y+=2) -> y = 0, 2, 4, 6, 8, 10, 12, 14, 16, 18 (10 iterazioni)
        // Per ogni iterazione, 2 scaffali (uno a destra e uno a sinistra)
        // Totale mappe previste: 10 * 2 = 20
        assertEquals(countPrima + 20, countDopo, "Dovrebbero essere stati inseriti 20 record per la planimetria");
    }
}
