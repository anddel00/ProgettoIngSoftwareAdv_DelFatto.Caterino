package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.Model.Turni;
import com.ProgettoISA.WMS.Model.TurniDip;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.TurniDipRepository;
import com.ProgettoISA.WMS.Repository.TurniRepository;
import com.ProgettoISA.WMS.Repository.UtentiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TurniService {

    @Autowired
    private TurniDipRepository turniDipRepository;

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private TurniRepository turniRepository;

    public void iniziaTurno(String email) {
        // 1. Troviamo il dipendente GESTENDO L'OPTIONAL
        Utenti dipendente = utentiRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato con email: " + email));

        // 2. Controllo sicurezza: il dipendente ha già un turno attivo?
        List<Utenti> attivi = turniDipRepository.findDipendentiAttualmenteInTurno();
        if (attivi.contains(dipendente)) {
            throw new IllegalStateException("Hai già un turno in corso!");
        }

        // 3. Fallback per i Turni (visto che l'Admin non li ha ancora creati)
        Turni turnoAttuale;
        List<Turni> tuttiTurni = turniRepository.findAll();
        if (tuttiTurni.isEmpty()) {
            // Se il database è vuoto, creiamo un Turno generico di default
            turnoAttuale = new Turni("00:00", "23:59");
            turniRepository.save(turnoAttuale);
        } else {
            // Altrimenti prendiamo il primo disponibile
            turnoAttuale = tuttiTurni.get(0);
        }

        TurniDip nuovaTimbratura = new TurniDip(dipendente, turnoAttuale);
        nuovaTimbratura.setOraInizioReale(LocalDateTime.now());

        turniDipRepository.save(nuovaTimbratura);
    }
}