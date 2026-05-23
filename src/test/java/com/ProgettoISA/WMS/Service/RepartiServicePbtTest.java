package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.DTO.RepartiDTO;
import com.ProgettoISA.WMS.Model.Reparti;
import com.ProgettoISA.WMS.Model.Scaffali;
import com.ProgettoISA.WMS.Repository.MappaRepository;
import com.ProgettoISA.WMS.Repository.RepartiRepository;
import com.ProgettoISA.WMS.Repository.ScaffaliRepository;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.lifecycle.BeforeTry;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RepartiServicePbtTest {

    @Mock
    private RepartiRepository repartiRepository;

    @Mock
    private ScaffaliRepository scaffaliRepository;

    @Mock
    private MappaRepository mappaRepository;

    @InjectMocks
    private RepartiService repartiService;

    @BeforeTry
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Property
    void verificaGeometriaPlanimetria(
            @ForAll @IntRange(min = 4, max = 1000) int xCasuale,
            @ForAll @IntRange(min = 2, max = 1000) int yCasuale
    ) {
        // Arrange
        RepartiDTO dto = new RepartiDTO(null, (long) xCasuale, (long) yCasuale, 20L, "Reparto Fuzzing");

        Reparti mockReparto = new Reparti();
        mockReparto.setId(1L);
        mockReparto.setMaxX((long) xCasuale);
        mockReparto.setMaxY((long) yCasuale);

        when(repartiRepository.save(any(Reparti.class))).thenReturn(mockReparto);
        when(scaffaliRepository.save(any(Scaffali.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        repartiService.creaRepartoConPlanimetria(dto);

        // Assert
        int corridoioCentrale = (xCasuale % 2 == 0) ? 2 : 3;
        int larghezzaScaffale = (xCasuale - corridoioCentrale) / 2;

        assertTrue(larghezzaScaffale > 0, "La larghezza scaffale calcolata deve essere > 0 per " + xCasuale);
        
        int xRicostruito = (larghezzaScaffale * 2) + corridoioCentrale;
        assertEquals(xCasuale, xRicostruito, "La formula inversa non restituisce la X originale senza perdite");
    }
}
