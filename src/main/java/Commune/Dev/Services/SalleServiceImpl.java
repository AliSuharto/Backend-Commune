package Commune.Dev.Services;

import Commune.Dev.Dtos.*;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import Commune.Dev.Services.SalleService;
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

    private final SalleRepository salleRepository;
    private final MarcheeRepository marcheeRepository;
    private final ZoneRepository zoneRepository;

    @Override
    public SalleResponseDTO create(SalleCreateDTO createDTO) {
        log.info("Creating new Salle with name: {}", createDTO.getNom());

        // Validate business rule: soit marché direct, soit zone
        if (!createDTO.isValid()) {
            throw new RuntimeException("Une salle doit être soit directement dans un marché, soit dans une zone, mais pas les deux");
        }

        // Validate unique name
        if (salleRepository.existsByNomIgnoreCase(createDTO.getNom())) {
            throw new RuntimeException("Une salle avec ce nom existe déjà");
        }

        Salle salle = new Salle();
        salle.setNom(createDTO.getNom());
        salle.setDescription(createDTO.getDescription());

        // Set emplacement selon la logique métier
        if (createDTO.getMarcheeId() != null) {
            // Salle directement dans le marché
            Marchee marchee = marcheeRepository.findById(createDTO.getMarcheeId())
                    .orElseThrow(() -> new RuntimeException("Marché non trouvé"));
            salle.setMarchee(marchee);
            salle.setZone(null); // Explicitement null
        } else if (createDTO.getZoneId() != null) {
            // Salle dans une zone
            Zone zone = zoneRepository.findById(createDTO.getZoneId())
                    .orElseThrow(() -> new RuntimeException("Zone non trouvée"));
            salle.setZone(zone);
            salle.setMarchee(null); // Explicitement null - le marché sera accessible via zone.marchee
        }

        Salle savedSalle = salleRepository.save(salle);
        return mapToResponseDTO(savedSalle);
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

        Salle salle = salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée"));

        // Update fields if provided
        if (updateDTO.getNom() != null && !updateDTO.getNom().equals(salle.getNom())) {
            if (salleRepository.existsByNomIgnoreCase(updateDTO.getNom())) {
                throw new RuntimeException("Une salle avec ce nom existe déjà");
            }
            salle.setNom(updateDTO.getNom());
        }

        if (updateDTO.getDescription() != null) {
            salle.setDescription(updateDTO.getDescription());
        }

        // Gestion du changement d'emplacement
        if (updateDTO.getMoveToMarche() != null && updateDTO.getMoveToMarche()) {
            // Déplacer vers un marché direct
            if (updateDTO.getMarcheeId() == null) {
                throw new RuntimeException("ID du marché requis pour déplacer la salle vers un marché direct");
            }
            Marchee marchee = marcheeRepository.findById(updateDTO.getMarcheeId())
                    .orElseThrow(() -> new RuntimeException("Marché non trouvé"));
            salle.setMarchee(marchee);
            salle.setZone(null); // Retirer de la zone
        } else if (updateDTO.getMoveToZone() != null && updateDTO.getMoveToZone()) {
            // Déplacer vers une zone
            if (updateDTO.getZoneId() == null) {
                throw new RuntimeException("ID de la zone requis pour déplacer la salle vers une zone");
            }
            Zone zone = zoneRepository.findById(updateDTO.getZoneId())
                    .orElseThrow(() -> new RuntimeException("Zone non trouvée"));
            salle.setZone(zone);
            salle.setMarchee(null); // Retirer du marché direct
        } else {
            // Mise à jour simple sans changement d'emplacement
            if (updateDTO.getMarcheeId() != null && salle.getZone() == null) {
                // Mise à jour du marché pour une salle déjà dans un marché direct
                Marchee marchee = marcheeRepository.findById(updateDTO.getMarcheeId())
                        .orElseThrow(() -> new RuntimeException("Marché non trouvé"));
                salle.setMarchee(marchee);
            } else if (updateDTO.getZoneId() != null && salle.getMarchee() == null) {
                // Mise à jour de la zone pour une salle déjà dans une zone
                Zone zone = zoneRepository.findById(updateDTO.getZoneId())
                        .orElseThrow(() -> new RuntimeException("Zone non trouvée"));
                salle.setZone(zone);
            }
        }

        Salle updatedSalle = salleRepository.save(salle);
        return mapToResponseDTO(updatedSalle);
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
        Specification<Salle> spec = createSpecification(filterDTO);

        Sort sort = Sort.by(
                filterDTO.getSortDirection().equalsIgnoreCase("DESC") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                filterDTO.getSortBy()
        );

        Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize(), sort);

        Page<Salle> sallesPage = salleRepository.findAll(spec, pageable);

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

        List<Salle> salles = new ArrayList<>();

        for (SalleCreateDTO createDTO : createDTOs) {
            // Validate business rule
            if (!createDTO.isValid()) {
                throw new RuntimeException("Salle '" + createDTO.getNom() + "': doit être soit directement dans un marché, soit dans une zone");
            }

            if (salleRepository.existsByNomIgnoreCase(createDTO.getNom())) {
                throw new RuntimeException("Une salle avec le nom '" + createDTO.getNom() + "' existe déjà");
            }

            Salle salle = new Salle();
            salle.setNom(createDTO.getNom());
            salle.setDescription(createDTO.getDescription());

            if (createDTO.getMarcheeId() != null) {
                // Salle directement dans le marché
                Marchee marchee = marcheeRepository.findById(createDTO.getMarcheeId())
                        .orElseThrow(() -> new RuntimeException("Marché non trouvé: " + createDTO.getMarcheeId()));
                salle.setMarchee(marchee);
                salle.setZone(null);
            } else if (createDTO.getZoneId() != null) {
                // Salle dans une zone
                Zone zone = zoneRepository.findById(createDTO.getZoneId())
                        .orElseThrow(() -> new RuntimeException("Zone non trouvée: " + createDTO.getZoneId()));
                salle.setZone(zone);
                salle.setMarchee(null);
            }

            salles.add(salle);
        }

        List<Salle> savedSalles = salleRepository.saveAll(salles);
        return savedSalles.stream()
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
    public boolean existsByNom(String nom) {
        return salleRepository.existsByNomIgnoreCase(nom);
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

    private Specification<Salle> createSpecification(SalleFilterDTO filterDTO) {
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

    private SalleResponseDTO mapToResponseDTO(Salle salle) {
        SalleResponseDTO dto = new SalleResponseDTO();
        dto.setId(salle.getId());
        dto.setNom(salle.getNom());
        dto.setDescription(salle.getDescription());

        // Déterminer le type d'emplacement et les informations
        if (salle.getMarchee() != null && salle.getZone() == null) {
            // Salle directement dans un marché
            dto.setEmplacementType("MARCHE_DIRECT");
            dto.setMarcheeId(salle.getMarchee().getId());
            dto.setMarcheeNom(salle.getMarchee().getNom());
        } else if (salle.getZone() != null && salle.getMarchee() == null) {
            // Salle dans une zone
            dto.setEmplacementType("ZONE");
            dto.setZoneId(salle.getZone().getId());
            dto.setZoneNom(salle.getZone().getNom());
            // Le marché est accessible via la zone
            if (salle.getZone().getMarchee() != null) {
                dto.setMarcheeId(salle.getZone().getMarchee().getId());
                dto.setMarcheeNom(salle.getZone().getMarchee().getNom());
            }
        }

        // Count places if available
        if (salle.getPlaces() != null) {
            dto.setNbPlaces((long) salle.getPlaces().size());
        } else {
            dto.setNbPlaces(salleRepository.countPlacesBySalleId(salle.getId()));
        }

        return dto;
    }
}