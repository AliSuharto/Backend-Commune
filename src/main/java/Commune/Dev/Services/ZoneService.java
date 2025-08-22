package Commune.Dev.Services;

import Commune.Dev.Models.Zone;
import Commune.Dev.Repositories.ZoneRepository;
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
    private PlaceService placeService;

    // CREATE operations
    public Zone save(Zone zone) {
        return zoneRepository.save(zone);
    }

    public List<Zone> saveAll(List<Zone> zones) {
        return zoneRepository.saveAll(zones);
    }

    // READ operations
    public List<Zone> findAll() {
        return zoneRepository.findAll();
    }

    public Optional<Zone> findById(Integer id) {
        return zoneRepository.findById(id);
    }

    public List<Zone> findAllById(List<Integer> ids) {
        return zoneRepository.findAllById(ids);
    }

    public List<Zone> findByIdMarchee(Integer idMarchee) {
        return zoneRepository.findByIdMarchee(idMarchee);
    }

    public List<Zone> findByNomContainingIgnoreCase(String nom) {
        return zoneRepository.findByNomContainingIgnoreCase(nom);
    }

    public List<Zone> findByIdMarcheeAndNomContainingIgnoreCase(Integer idMarchee, String nom) {
        return zoneRepository.findByIdMarcheeAndNomContainingIgnoreCase(idMarchee, nom);
    }

    // COUNT operations
    public long count() {
        return zoneRepository.count();
    }

    public long countByIdMarchee(Integer idMarchee) {
        return zoneRepository.countByIdMarchee(idMarchee);
    }

    public boolean existsById(Integer id) {
        return zoneRepository.existsById(id);
    }

    // DELETE operations
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

    public void deleteByIdMarchee(Integer idMarchee) {
        zoneRepository.deleteByIdMarchee(idMarchee);
    }

    public void deleteAll() {
        zoneRepository.deleteAll();
    }

    // Custom business methods
    public Map<String, Object> getZoneStatistics(Integer zoneId) {
        Map<String, Object> stats = new HashMap<>();

        Optional<Zone> zoneOpt = findById(zoneId);
        if (zoneOpt.isPresent()) {
            Zone zone = zoneOpt.get();

            // Statistiques de base
            stats.put("zoneId", zoneId);
            stats.put("zoneName", zone.getNom());
            stats.put("zoneDescription", zone.getDescription());
            stats.put("marcheeId", zone.getIdMarchee());

            // Statistiques des places dans cette zone
            long totalPlaces = placeService.countPlacesByZoneId(zoneId);
            long availablePlaces = placeService.countAvailablePlacesByZoneId(zoneId);
            long occupiedPlaces = totalPlaces - availablePlaces;

            double occupationRate = totalPlaces > 0 ?
                    (double) occupiedPlaces / totalPlaces * 100 : 0.0;

            stats.put("totalPlaces", totalPlaces);
            stats.put("availablePlaces", availablePlaces);
            stats.put("occupiedPlaces", occupiedPlaces);
            stats.put("occupationRate", Math.round(occupationRate * 100.0) / 100.0);

            // Nombre de salles dans cette zone (si relation existe)
            stats.put("totalSalles", zone.getSalles() != null ? zone.getSalles().size() : 0);
        }

        return stats;
    }

    public List<Zone> getZonesByMarcheeWithPlaceCount(Integer idMarchee) {
        return zoneRepository.findByIdMarcheeWithPlaceCount(idMarchee);
    }

    public boolean hasPlaces(Integer zoneId) {
        return placeService.countPlacesByZoneId(zoneId) > 0;
    }

    public boolean hasSalles(Integer zoneId) {
        Optional<Zone> zone = findById(zoneId);
        return zone.isPresent() && zone.get().getSalles() != null && !zone.get().getSalles().isEmpty();
    }
}