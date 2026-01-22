package Commune.Dev.Services;

import Commune.Dev.Dtos.*;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SalleServiceImpl implements SalleService {

    private final HallsRepository salleRepository;
    private final MarcheeRepository marcheeRepository;
    private final ZoneRepository zoneRepository;

    @Override
    public SalleResponseDTO create(SalleCreateDTO createDTO) {
        log.info("Creating new Salle with name: {}", createDTO.getCodeUnique());

        // Validate business rule: soit marché direct, soit zone
        if (!createDTO.isValid()) {
            throw new RuntimeException("Une salle doit être soit directement dans un marché, soit dans une zone, mais pas les deux");
        }

        // Validate unique name
        if (salleRepository.existsByCodeUniqueIgnoreCase(createDTO.getCodeUnique())) {
            throw new RuntimeException("Une salle avec ce nom existe déjà");
        }

        Halls halls = new Halls();
        halls.setNom(createDTO.getNom());
        halls.setDescription(createDTO.getDescription());

        // Set emplacement selon la logique métier
        if (createDTO.getMarcheeId() != null) {
            // Salle directement dans le marché
            Marchee marchee = marcheeRepository.findById(createDTO.getMarcheeId())
                    .orElseThrow(() -> new RuntimeException("Marché non trouvé"));
            halls.setMarchee(marchee);
            halls.setZone(null); // Explicitement null
        } else if (createDTO.getZoneId() != null) {
            // Salle dans une zone
            Zone zone = zoneRepository.findById(Math.toIntExact(Long.valueOf(createDTO.getZoneId())))
                    .orElseThrow(() -> new RuntimeException("Zone non trouvée"));
            halls.setZone(zone);
            halls.setMarchee(null); // Explicitement null - le marché sera accessible via zone.marchee
        }

        Halls savedHalls = salleRepository.save(halls);
        return mapToResponseDTO(savedHalls);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalleResponseDTO> findById(Integer id) {
        return salleRepository.findById(id)
                .map(this::mapToResponseDTO);
    }

    @Override
    public SalleResponseDTO update(Integer id, SalleUpdateDTO updateDTO) {
        log.info("Updating Salle with id: {}", id);

        Halls halls = salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée"));

        // Update fields if provided
        if (updateDTO.getNom() != null && !updateDTO.getNom().equals(halls.getNom())) {
            if (salleRepository.existsByCodeUniqueIgnoreCase(updateDTO.getCodeUnique())) {
                throw new RuntimeException("Une salle avec ce nom existe déjà");
            }
            halls.setNom(updateDTO.getNom());
        }

        if (updateDTO.getDescription() != null) {
            halls.setDescription(updateDTO.getDescription());
        }

        // Gestion du changement d'emplacement
        if (updateDTO.getMoveToMarche() != null && updateDTO.getMoveToMarche()) {
            // Déplacer vers un marché direct
            if (updateDTO.getMarcheeId() == null) {
                throw new RuntimeException("ID du marché requis pour déplacer la salle vers un marché direct");
            }
            Marchee marchee = marcheeRepository.findById(updateDTO.getMarcheeId())
                    .orElseThrow(() -> new RuntimeException("Marché non trouvé"));
            halls.setMarchee(marchee);
            halls.setZone(null); // Retirer de la zone
        } else if (updateDTO.getMoveToZone() != null && updateDTO.getMoveToZone()) {
            // Déplacer vers une zone
            if (updateDTO.getZoneId() == null) {
                throw new RuntimeException("ID de la zone requis pour déplacer la salle vers une zone");
            }
            Zone zone = zoneRepository.findById(Math.toIntExact(Long.valueOf(updateDTO.getZoneId())))
                    .orElseThrow(() -> new RuntimeException("Zone non trouvée"));
            halls.setZone(zone);
            halls.setMarchee(null); // Retirer du marché direct
        } else {
            // Mise à jour simple sans changement d'emplacement
            if (updateDTO.getMarcheeId() != null && halls.getZone() == null) {
                // Mise à jour du marché pour une salle déjà dans un marché direct
                Marchee marchee = marcheeRepository.findById(updateDTO.getMarcheeId())
                        .orElseThrow(() -> new RuntimeException("Marché non trouvé"));
                halls.setMarchee(marchee);
            } else if (updateDTO.getZoneId() != null && halls.getMarchee() == null) {
                // Mise à jour de la zone pour une salle déjà dans une zone
                Zone zone = zoneRepository.findById(Math.toIntExact(Long.valueOf(updateDTO.getZoneId())))
                        .orElseThrow(() -> new RuntimeException("Zone non trouvée"));
                halls.setZone(zone);
            }
        }

        Halls updatedHalls = salleRepository.save(halls);
        return mapToResponseDTO(updatedHalls);
    }

    @Override
    public void deleteById(Integer id) {
        log.info("Deleting Salle with id: {}", id);

        if (!salleRepository.existsById(id)) {
            throw new RuntimeException("Salle non trouvée");
        }

        // Check if salle has places
        long placesCount = salleRepository.countPlacesBySalleId(id);
        if (placesCount > 0) {
            throw new RuntimeException("Impossible de supprimer: la salle contient " + placesCount + " places");
        }

        salleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalleResponseDTO> findAllWithFilters(SalleFilterDTO filterDTO) {
        Specification<Halls> spec = createSpecification(filterDTO);

        Sort sort = Sort.by(
                filterDTO.getSortDirection().equalsIgnoreCase("DESC") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                filterDTO.getSortBy()
        );

        Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize(), sort);

        Page<Halls> sallesPage = salleRepository.findAll(spec, pageable);

        return sallesPage.map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalleResponseDTO> findAll() {
        return salleRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalleResponseDTO> findAllByMarchee(Integer marcheeId) {
        return salleRepository.findAllByMarcheeId(marcheeId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalleResponseDTO> findDirectlyByMarchee(Integer marcheeId) {
        return salleRepository.findByMarcheeIdDirectly(marcheeId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalleResponseDTO> findByZone(Integer zoneId) {
        return salleRepository.findByZoneId(zoneId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalleResponseDTO> findByIdWithPlaces(Integer id) {
        return salleRepository.findByIdWithPlaces(id)
                .map(this::mapToResponseDTO);
    }

    @Override
    public List<SalleResponseDTO> createBatch(List<SalleCreateDTO> createDTOs) {
        log.info("Creating batch of {} salles", createDTOs.size());

        List<Halls> halls = new ArrayList<>();

        for (SalleCreateDTO createDTO : createDTOs) {
            // Validate business rule
            if (!createDTO.isValid()) {
                throw new RuntimeException("Salle '" + createDTO.getNom() + "': doit être soit directement dans un marché, soit dans une zone");
            }

            if (salleRepository.existsByCodeUniqueIgnoreCase(createDTO.getNom())) {
                throw new RuntimeException("Une salle avec le nom '" + createDTO.getNom() + "' existe déjà");
            }

            Halls halls1 = new Halls();
            halls1.setNom(createDTO.getNom());
            halls1.setDescription(createDTO.getDescription());

            if (createDTO.getMarcheeId() != null) {
                // Salle directement dans le marché
                Marchee marchee = marcheeRepository.findById(createDTO.getMarcheeId())
                        .orElseThrow(() -> new RuntimeException("Marché non trouvé: " + createDTO.getMarcheeId()));
                halls1.setMarchee(marchee);
                halls1.setZone(null);
            } else if (createDTO.getZoneId() != null) {
                // Salle dans une zone
                Zone zone = zoneRepository.findById(Math.toIntExact(Long.valueOf(createDTO.getZoneId())))
                        .orElseThrow(() -> new RuntimeException("Zone non trouvée: " + createDTO.getZoneId()));
                halls1.setZone(zone);
                halls1.setMarchee(null);
            }

            halls.add(halls1);
        }

        List<Halls> savedHalls = salleRepository.saveAll(halls);
        return savedHalls.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        log.info("Deleting batch of {} salles", ids.size());

        // Verify all salles exist and can be deleted
        for (Integer id : ids) {
            if (!salleRepository.existsById(id)) {
                throw new RuntimeException("Salle non trouvée: " + id);
            }

            long placesCount = salleRepository.countPlacesBySalleId(id);
            if (placesCount > 0) {
                throw new RuntimeException("Impossible de supprimer la salle " + id + ": elle contient " + placesCount + " places");
            }
        }

        salleRepository.deleteAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return salleRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNom(String codeUnique) {
        return salleRepository.existsByCodeUniqueIgnoreCase(codeUnique);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPlaces(Integer salleId) {
        return salleRepository.countPlacesBySalleId(salleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalleResponseDTO> searchByNom(String nom) {
        return salleRepository.findByNomContainingIgnoreCase(nom).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private Specification<Halls> createSpecification(SalleFilterDTO filterDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDTO.getNom() != null && !filterDTO.getNom().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nom")),
                        "%" + filterDTO.getNom().toLowerCase() + "%"
                ));
            }

            if (filterDTO.getMarcheeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("marchee").get("id"), filterDTO.getMarcheeId()));
            }

            if (filterDTO.getZoneId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("zone").get("id"), filterDTO.getZoneId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private SalleResponseDTO mapToResponseDTO(Halls halls) {
        SalleResponseDTO dto = new SalleResponseDTO();
        dto.setId(Math.toIntExact(halls.getId()));
        dto.setNom(halls.getNom());
        dto.setDescription(halls.getDescription());

        // Déterminer le type d'emplacement et les informations
        if (halls.getMarchee() != null && halls.getZone() == null) {
            // Salle directement dans un marché
            dto.setEmplacementType("MARCHE_DIRECT");
            dto.setMarcheeId(Math.toIntExact(halls.getMarchee().getId()));
            dto.setMarcheeNom(halls.getMarchee().getNom());
        } else if (halls.getZone() != null && halls.getMarchee() == null) {
            // Salle dans une zone
            dto.setEmplacementType("ZONE");
            dto.setZoneId(Math.toIntExact(halls.getZone().getId()));
            dto.setZoneNom(halls.getZone().getNom());
            // Le marché est accessible via la zone
            if (halls.getZone().getMarchee() != null) {
                dto.setMarcheeId(Math.toIntExact(halls.getZone().getMarchee().getId()));
                dto.setMarcheeNom(halls.getZone().getMarchee().getNom());
            }
        }

        // Count places if available
        if (halls.getPlaces() != null) {
            dto.setNbrPlaces((long) halls.getPlaces().size());
        } else {
            dto.setNbrPlaces(salleRepository.countPlacesBySalleId(Math.toIntExact(halls.getId())));
        }

        return dto;
    }
}