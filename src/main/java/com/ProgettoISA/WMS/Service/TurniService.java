package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.Model.Turni;
import com.ProgettoISA.WMS.Model.TurniDip;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import com.ProgettoISA.WMS.Repository.TurniDipRepository;
import com.ProgettoISA.WMS.Repository.TurniRepository;
import com.ProgettoISA.WMS.Repository.UtentiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private TaskRepository taskRepository;

    // ==========================================
    // INIZIA TURNO
    // ==========================================
    @Transactional
    public void iniziaTurno(String email) {
        Utenti dipendente = utentiRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + email));

        // Blocca se ha già un turno aperto (oraFineReale IS NULL)
        List<Utenti> attivi = turniDipRepository.findDipendentiAttualmenteInTurno();
        if (attivi.stream().anyMatch(u -> u.getEmail().equals(email))) {
            throw new IllegalStateException("Hai già un turno in corso!");
        }

        // Fallback: crea un turno generico se non ne esistono
        Turni turnoAttuale;
        List<Turni> tuttiTurni = turniRepository.findAll();
        if (tuttiTurni.isEmpty()) {
            turnoAttuale = new Turni("00:00", "23:59");
            turniRepository.save(turnoAttuale);
        } else {
            turnoAttuale = tuttiTurni.get(0);
        }

        TurniDip nuovaTimbratura = new TurniDip(dipendente, turnoAttuale);
        nuovaTimbratura.setOraInizioReale(LocalDateTime.now());
        turniDipRepository.save(nuovaTimbratura);
    }

    // ==========================================
    // TERMINA TURNO
    // Regola: non si può chiudere il turno con task attivi
    // ==========================================
    @Transactional
    public void terminaTurno(String email) {
        Utenti dipendente = utentiRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + email));

        long taskAttivi = taskRepository.countTaskAttiviPerDipendente(email);
        if (taskAttivi > 0) {
            throw new IllegalStateException(
                    "Impossibile terminare il turno: hai ancora " + taskAttivi +
                            " task attivo/i. Completali prima di uscire."
            );
        }

        // Usiamo la LISTA per evitare crash. Troviamo tutti i turni rimasti appesi
        List<TurniDip> turniAperti = turniDipRepository.findTurniApertiByEmail(email);

        if (turniAperti.isEmpty()) {
            throw new IllegalStateException("Nessun turno attivo trovato per: " + email);
        }

        // Chiudiamo tutti i turni aperti (Sanificazione del DB dai vecchi test)
        for (TurniDip turno : turniAperti) {
            turno.setOraFineReale(LocalDateTime.now());
            turniDipRepository.save(turno);
        }
    }
}