package Commune.Dev.Services;

import Commune.Dev.Dtos.ZoneResponse;
import Commune.Dev.Models.Marchee;
import Commune.Dev.Models.Zone;
import Commune.Dev.Repositories.MarcheeRepository;
import Commune.Dev.Repositories.ZoneRepository;
import Commune.Dev.Request.ZoneRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ZoneService {

    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private MarcheeRepository marcheeRepository;
    @Autowired
    private PlaceService placeService;

    // =================== CREATE ===================
    public Zone save(Zone zone) {
        return zoneRepository.save(zone);
    }

    public ZoneResponse createZone(ZoneRequest request) {
        // Vérifier que le marché existe
        Marchee marchee = marcheeRepository.findById(request.getMarcheeId())
                .orElseThrow(() -> new RuntimeException("Marché introuvable"));

        // Créer l’entité Zone
        Zone zone = new Zone();
        zone.setNom(request.getNom());
        zone.setDescription(request.getDescription());
        zone.setMarchee(marchee);

        Zone savedZone = zoneRepository.save(zone);

        // Convertir en réponse
        ZoneResponse response = new ZoneResponse();
        response.setId(Math.toIntExact(savedZone.getId()));
        response.setNom(savedZone.getNom());
        response.setDescription(savedZone.getDescription());
        response.setMarcheeId(Math.toIntExact(marchee.getId()));
        response.setMarcheeNom(marchee.getNom());

        return response;
    }

    public List<Zone> saveAll(List<Zone> zones) {
        return zoneRepository.saveAll(zones);
    }

    // =================== READ ===================
    public List<Zone> findAll() {
        return zoneRepository.findAll();
    }

    public Optional<Zone> findById(Integer id) {
        return zoneRepository.findById(id);
    }

    public List<Zone> findAllById(List<Integer> ids) {
        return zoneRepository.findAllById(ids);
    }

    public List<Zone> findByMarcheeId(Integer marcheeId) {
        return zoneRepository.findByMarcheeId(marcheeId);
    }

    public List<Zone> findByNomContainingIgnoreCase(String nom) {
        return zoneRepository.findByNomContainingIgnoreCase(nom);
    }

    public List<Zone> findByMarcheeIdAndNomContainingIgnoreCase(Integer marcheeId, String nom) {
        return zoneRepository.findByMarcheeIdAndNomContainingIgnoreCase(marcheeId, nom);
    }

    // =================== COUNT ===================
    public long count() {
        return zoneRepository.count();
    }

    public long countByMarcheeId(Integer marcheeId) {
        return zoneRepository.countByMarcheeId(marcheeId);
    }

    public boolean existsById(Integer id) {
        return zoneRepository.existsById(id);
    }

    // =================== DELETE ===================
    public void deleteById(Integer id) {
        zoneRepository.deleteById(id);
    }

    public void delete(Zone zone) {
        zoneRepository.delete(zone);
    }

    public void deleteAll(List<Zone> zones) {
        zoneRepository.deleteAll(zones);
    }

    public void deleteAllById(List<Integer> ids) {
        zoneRepository.deleteAllById(ids);
    }

    public void deleteByMarcheeId(Integer marcheeId) {
        zoneRepository.deleteByMarcheeId(marcheeId);
    }

    public void deleteAll() {
        zoneRepository.deleteAll();
    }

    // =================== CUSTOM BUSINESS METHODS ===================
    public Map<String, Object> getZoneStatistics(Integer zoneId) {
        Map<String, Object> stats = new HashMap<>();

        Optional<Zone> zoneOpt = findById(zoneId);
        if (zoneOpt.isPresent()) {
            Zone zone = zoneOpt.get();

            // Infos de base
            stats.put("zoneId", zoneId);
            stats.put("zoneName", zone.getNom());
            stats.put("zoneDescription", zone.getDescription());
            stats.put("marcheeId", zone.getMarchee() != null ? zone.getMarchee().getId() : null);

            // Stats des places
            long totalPlaces = placeService.countPlacesByZoneId(zoneId);
            long availablePlaces = placeService.countAvailablePlacesByZoneId(zoneId);
            long occupiedPlaces = totalPlaces - availablePlaces;
            double occupationRate = totalPlaces > 0 ? (double) occupiedPlaces / totalPlaces * 100 : 0.0;

            stats.put("totalPlaces", totalPlaces);
            stats.put("availablePlaces", availablePlaces);
            stats.put("occupiedPlaces", occupiedPlaces);
            stats.put("occupationRate", Math.round(occupationRate * 100.0) / 100.0);

            // Nombre de halls
            stats.put("totalSalles", zone.getHalls() != null ? zone.getHalls().size() : 0);
        }

        return stats;
    }

    public List<Zone> getZonesByMarcheeWithPlaceCount(Integer marcheeId) {
        return zoneRepository.findByMarcheeIdWithPlaceCount(marcheeId);
    }

    public boolean hasPlaces(Integer zoneId) {
        return placeService.countPlacesByZoneId(zoneId) > 0;
    }

    public boolean hasSalles(Integer zoneId) {
        Optional<Zone> zone = findById(zoneId);
        return zone.isPresent() && zone.get().getHalls() != null && !zone.get().getHalls().isEmpty();
    }
}
