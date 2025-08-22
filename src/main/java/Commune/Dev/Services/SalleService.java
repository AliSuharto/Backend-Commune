package Commune.Dev.Services;

import Commune.Dev.Dtos.*;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

public interface SalleService {

    // Basic CRUD
    SalleResponseDTO create(SalleCreateDTO createDTO);
    Optional<SalleResponseDTO> findById(Integer id);
    SalleResponseDTO update(Integer id, SalleUpdateDTO updateDTO);
    void deleteById(Integer id);

    // Advanced operations
    Page<SalleResponseDTO> findAllWithFilters(SalleFilterDTO filterDTO);
    List<SalleResponseDTO> findAll();
    List<SalleResponseDTO> findAllByMarchee(Integer marcheeId); // Toutes les salles du marché
    List<SalleResponseDTO> findDirectlyByMarchee(Integer marcheeId); // Salles directement dans le marché
    List<SalleResponseDTO> findByZone(Integer zoneId);
    Optional<SalleResponseDTO> findByIdWithPlaces(Integer id);

    // Batch operations
    List<SalleResponseDTO> createBatch(List<SalleCreateDTO> createDTOs);
    void deleteBatch(List<Integer> ids);

    // Utility methods
    boolean existsById(Integer id);
    boolean existsByNom(String nom);
    long countPlaces(Integer salleId);

    // Business operations
    List<SalleResponseDTO> searchByNom(String nom);
}
