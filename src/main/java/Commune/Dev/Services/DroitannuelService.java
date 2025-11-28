package Commune.Dev.Services;

import Commune.Dev.Dtos.CreateDroitAnnuelRequest;
import Commune.Dev.Dtos.DroitannuelDTO;
import Commune.Dev.Models.DroitAnnuel;
import Commune.Dev.Repositories.DroitannuelRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DroitannuelService {
    private final DroitannuelRepository repository;

    public DroitannuelService(DroitannuelRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DroitAnnuel create(CreateDroitAnnuelRequest request) {
        DroitAnnuel droitAnnuel = new DroitAnnuel();
        droitAnnuel.setDescription(request.getDescription());
        droitAnnuel.setMontant(request.getMontant());
        droitAnnuel.setDateCreation(LocalDateTime.now());
        return repository.save(droitAnnuel);
    }

    @Transactional
    public List<DroitannuelDTO> getAll() {
        return repository.findAll().stream()
                .map(d -> new DroitannuelDTO(
                        d.getId(),
                        d.getDescription(),
                        d.getDateCreation(),
                        d.getMontant(),
                        d.getPlaces() != null ? d.getPlaces().size() : 0
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public DroitAnnuel getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DroitAnnuel non trouvé avec l'id: " + id));
    }
}

