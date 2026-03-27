package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.Model.Ruoli;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.RuoliRepository;
import com.ProgettoISA.WMS.Repository.UtentiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
    public Utenti registraUtente(Utenti nuovoUtente, String nome_ruolo) {

        if (utentiRepository.existsByEmail(nuovoUtente.getEmail())) {  //Controlliamo se l'email esiste già nel database
            throw new IllegalArgumentException("Errore: L'email inserita è già in uso!");
        }

        // 2. Cerchiamo il ruolo specificato (es. "Admin" o "Dipendente")
        Ruoli ruoloAssegnato = ruoliRepository.findByNome(nome_ruolo)
                .orElseThrow(() -> new IllegalArgumentException("Errore: Ruolo '" + nome_ruolo + "' non trovato nel sistema!"));

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
}