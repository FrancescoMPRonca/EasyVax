package com.easyvax.service.service;

import com.easyvax.DTO.SomministrazioneDTO;
import com.easyvax.model.Somministrazione;

import java.util.List;

public interface SomministrazioneService {

    SomministrazioneDTO insertSomministrazione(SomministrazioneDTO somministrazione);
    SomministrazioneDTO updateSomministrazione(SomministrazioneDTO somministrazione);
    SomministrazioneDTO getDetails(Long id);
    List<SomministrazioneDTO> findAll();
    List<SomministrazioneDTO> findByUtente(String cf);
    SomministrazioneDTO findByCod(String cod);


    List<SomministrazioneDTO> deletePrenotazione(Long id);


}
