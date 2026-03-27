package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.Model.Ruoli;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.RuoliRepository;
import com.ProgettoISA.WMS.Repository.UtentiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtentiService {

    @Autowired
    private UtentiRepository utentiRepository;
    @Autowired
    private RuoliRepository ruoliRepository;

    // Inizializziamo BCrypt per criptare le password
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==========================================
    // 1. REGISTRAZIONE UTENTE
    // ==========================================
    public Utenti registraUtente(Utenti nuovoUtente, String nomeRuolo) {

        if (utentiRepository.existsByEmail(nuovoUtente.getEmail())) {  //Controlliamo se l'email esiste già nel database
            throw new IllegalArgumentException("Errore: L'email inserita è già in uso!");
        }

        // 2. Cerchiamo il ruolo specificato (es. "Admin" o "Dipendente")
        Ruoli ruoloAssegnato = ruoliRepository.findByNomeRuolo(nomeRuolo)
                .orElseThrow(() -> new IllegalArgumentException("Errore: Ruolo '" + nomeRuolo + "' non trovato nel sistema!"));

        nuovoUtente.setRuolo(ruoloAssegnato);

        // 3. logica di hashing tramite BCrypt
        String passwordCriptata = passwordEncoder.encode(nuovoUtente.getPassword());
        nuovoUtente.setPassword(passwordCriptata);

        // 4. Salvo nuovo utente nel DB
        return utentiRepository.save(nuovoUtente);
    }

    // 2. LOGIN DIFFERENZIATO
    public Utenti effettuaLogin(String email, String passwordInChiaro) {
        // 1. Troviamo l'utente partendo dalla sua email
        Utenti utenteTrovato = utentiRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Errore: Nessun utente trovato con questa email."));

        // 2. Verifichiamo la password
        // Il metodo .matches() di BCrypt fa la magia: capisce se la password in chiaro corrisponde all'hash nel DB
        if (!passwordEncoder.matches(passwordInChiaro, utenteTrovato.getPassword())) {
            throw new IllegalArgumentException("Errore: Password errata!");
        }

        // Se le credenziali sono giuste, restituiamo l'utente (che conterrà anche il suo Ruolo)
        return utenteTrovato;
    }

    // ==========================================
    // 3. OTTIENI TUTTI GLI UTENTI
    // ==========================================
    public List<Utenti> ottieniTuttiUtenti() {
        return utentiRepository.findAll();
    }

    // ==========================================
    // 4. MODIFICA UTENTE
    // ==========================================
    public Utenti modificaUtente(Long id, Utenti datiAggiornati) {
        // Cerchiamo l'utente nel DB
        Utenti utenteEsistente = utentiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Errore: Utente non trovato!"));

        // Aggiorniamo i dati base
        utenteEsistente.setNome(datiAggiornati.getNome());
        utenteEsistente.setCognome(datiAggiornati.getCognome());
        utenteEsistente.setEmail(datiAggiornati.getEmail());

        // Se l'admin ha inserito una nuova password, la criptiamo e la salviamo.
        // Altrimenti, lasciamo quella vecchia intatta.
        if (datiAggiornati.getPassword() != null && !datiAggiornati.getPassword().isEmpty()) {
            utenteEsistente.setPassword(passwordEncoder.encode(datiAggiornati.getPassword()));
        }

        // Salviamo le modifiche nel database
        return utentiRepository.save(utenteEsistente);
    }

    // ==========================================
    // 5. ELIMINA UTENTE
    // ==========================================
    public void eliminaUtente(Long id) {
        if (!utentiRepository.existsById(id)) {
            throw new IllegalArgumentException("Errore: Impossibile eliminare, utente non trovato!");
        }
        utentiRepository.deleteById(id);
    }
}