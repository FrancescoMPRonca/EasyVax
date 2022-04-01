package com.easyvax.service.impl;


import com.easyvax.dto.SomministrazioneDTO;
import com.easyvax.exception.enums.SomministrazioneEnum;
import com.easyvax.exception.handler.ApiRequestException;
import com.easyvax.model.*;
import com.easyvax.repository.*;
import com.easyvax.service.service.SomministrazioneService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.data.relational.core.sql.FalseCondition;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor

public class SomministrazioneServiceImpl implements SomministrazioneService {

    private final SomministrazioneRepository somministrazioneRepository;
    private final PersonaleRepository personaleRepository;
    private final VaccinoRepository vaccinoRepository;
    private final CentroVaccinaleRepository centroVaccinaleRepository;
    private final UtenteRepository utenteRepository;
    private static SomministrazioneEnum somministrazioneEnum;
    private JavaMailSender mailSender;

    @Override
    public SomministrazioneDTO insertSomministrazione(SomministrazioneDTO somministrazioneDTO) {

        if (somministrazioneRepository.findByUtente_IdAndVaccino_IdAndDataSomministrazione(somministrazioneDTO.getIdUtente(), somministrazioneDTO.getIdVaccino(), somministrazioneDTO.getData()) == 0) {

            Somministrazione somministrazione = new Somministrazione(somministrazioneDTO);

            Utente utente = utenteRepository.findById(somministrazioneDTO.getIdUtente()).get();
            Vaccino vaccino = vaccinoRepository.findById(somministrazioneDTO.getIdVaccino()).get();
            CentroVaccinale cv = centroVaccinaleRepository.findById(somministrazioneDTO.getIdCentro()).get();

            if (utenteRepository.existsById(utente.getId()) && centroVaccinaleRepository.existsById(cv.getId()) && vaccinoRepository.existsById(vaccino.getId()) && utenteRepository.existsById(somministrazioneDTO.idUtente)) { //da aggiungere se è valid l'utente

                String randomCode = RandomString.make(12);
                if (!somministrazioneRepository.existsByCodiceSomm(randomCode))
                    somministrazione.setCodiceSomm(randomCode);
                else {
                    somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_AE");
                    throw new ApiRequestException(somministrazioneEnum.getMessage());
                }
                LocalDate today = LocalDate.now();
                LocalDate giornoSomm = somministrazione.getDataSomministrazione();

                if (ChronoUnit.DAYS.between(today, giornoSomm) >= 2 && today.isBefore(somministrazione.getDataSomministrazione())) {

                    somministrazione.setDataSomministrazione(somministrazioneDTO.getData());
                    somministrazione.setOraSomministrazione(somministrazioneDTO.getOra());
                    somministrazione.setUtente(utente);
                    somministrazione.setCentro(cv);
                    somministrazione.setVaccino(vaccino);
                    somministrazione.setInAttesa(Boolean.FALSE);
                    somministrazione = somministrazioneRepository.save(somministrazione);
                }else {
                    somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_IE");
                    throw new ApiRequestException(somministrazioneEnum.getMessage());
                }

                try {
                    sendEmail(somministrazione.getCodiceSomm(), utente);
                } catch (MessagingException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return new SomministrazioneDTO(somministrazione);

            } else {
                somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_IE");
                throw new ApiRequestException(somministrazioneEnum.getMessage());
            }
        } else {
            somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_AE");
            throw new ApiRequestException(somministrazioneEnum.getMessage());
        }
    }

    private void sendEmail(String cod, Utente utente) throws MessagingException, UnsupportedEncodingException {
        String toAddress = utente.getEmail();
        String fromAddress = "easyVaxNOREPLY@gmail.com";
        String senderName = "EasyVax";
        String subject = "I dettagli della tua prenotazione";
        String content = "Caro [[name]],<br>"
                + "eccco il codice della tua prenotazione [[codice]]<br>"
                + "Con questo codice potrai scaricare la ricevuta cliccando sul seguente link<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">DETTAGLI</a></h3>"
                + " Saluti,<br>"
                + "EasyVax.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", utente.getNome_Cognome());
        content = content.replace("[[codice]]", cod);

        helper.setText(content, true);

        mailSender.send(message);
    }


    @Override
    public SomministrazioneDTO updateSomministrazione(String code, SomministrazioneDTO somministrazioneDTO) {

        if (somministrazioneRepository.existsByCodiceSomm(code) && somministrazioneDTO.inAttesa != Boolean.TRUE) {
            Somministrazione somministrazione = somministrazioneRepository.findByCodiceSomm(code);

            if (somministrazioneDTO.getData() != somministrazione.getDataSomministrazione() || somministrazioneDTO.getOra() != somministrazione.getOraSomministrazione()) {

                LocalDate today = LocalDate.now();
                LocalDate giornoSomm = somministrazioneDTO.getData();

                if (ChronoUnit.DAYS.between(today, giornoSomm) >= 2 && today.isBefore(somministrazione.getDataSomministrazione())) {
                    somministrazione.setDataSomministrazione(somministrazioneDTO.getData());
                    somministrazione.setOraSomministrazione(somministrazioneDTO.getOra());

                    somministrazione = somministrazioneRepository.save(somministrazione);

                    return new SomministrazioneDTO(somministrazione);
                } else {
                    somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_DE");
                    throw new ApiRequestException(somministrazioneEnum.getMessage());
                }
            } else {
                somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_DE");
                throw new ApiRequestException(somministrazioneEnum.getMessage());
            }
        } else {
            somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_NF");
            throw new ApiRequestException(somministrazioneEnum.getMessage());
        }
    }

    @Override
    public SomministrazioneDTO getDetails(Long id) {
        if (somministrazioneRepository.existsById(id)) {
            return new SomministrazioneDTO(somministrazioneRepository.getById(id));
        } else {
            somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_NF");
            throw new ApiRequestException(somministrazioneEnum.getMessage());
        }
    }

    @Override
    public List<SomministrazioneDTO> findAll() {

        if (!somministrazioneRepository.findAll().isEmpty()) {
            return somministrazioneRepository.findAll().stream().map(SomministrazioneDTO::new).collect(Collectors.toList());
        } else {
            somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_IDNE");
            throw new ApiRequestException(somministrazioneEnum.getMessage());
        }
    }

    @Override
    public List<SomministrazioneDTO> findByUtente(String cf) {
        if (cf != null && utenteRepository.existsByCodFiscale(cf)) {
            return somministrazioneRepository.findbyUtente(cf).stream().map(SomministrazioneDTO::new).collect(Collectors.toList());
        } else {
            somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_IDNE");
            throw new ApiRequestException(somministrazioneEnum.getMessage());
        }
    }

    @Override
    public SomministrazioneDTO findByCod(String cod) {
        if (cod != null && somministrazioneRepository.existsByCodiceSomm(cod)) {
            return new SomministrazioneDTO(somministrazioneRepository.findByCodiceSomm(cod));
        } else {
            somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_IDNE");
            throw new ApiRequestException(somministrazioneEnum.getMessage());
        }
    }

    @Override
    public List<SomministrazioneDTO> deletePrenotazione(Long id) {
        if (somministrazioneRepository.existsById(id)) {
            Somministrazione somministrazione = somministrazioneRepository.findById(id).get();
            if (somministrazione.getInAttesa() != Boolean.TRUE)
                somministrazioneRepository.deleteById(id);
            return somministrazioneRepository.findAll().stream().map(SomministrazioneDTO::new).collect(Collectors.toList());
        } else {
            somministrazioneEnum = SomministrazioneEnum.getSomministrazioneEnumByMessageCode("SOMM_DLE");
            throw new ApiRequestException(somministrazioneEnum.getMessage());
        }
    }


}
