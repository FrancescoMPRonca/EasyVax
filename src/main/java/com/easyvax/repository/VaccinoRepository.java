package com.easyvax.repository;

import com.easyvax.DTO.VaccinoDTO;
import com.easyvax.model.Vaccino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccinoRepository extends JpaRepository<Vaccino,Long> {

    List<VaccinoDTO>  findVaccinoByCasaFarmaceutica(String casaFarmaceutica);
    List<VaccinoDTO> findByCasaFarmaceutica(String casaFarmaceutica);

    boolean existsByNome(String nome);
    boolean existsByNomeAndCasaFarmaceutica(String nome, String casaFarmaceutica);
    boolean existsById(Long id);
    boolean existsByCasaFarmaceutica(String casaFarmaceutica);
}
