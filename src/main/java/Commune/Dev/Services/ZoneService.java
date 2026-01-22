package Commune.Dev.Services;

import Commune.Dev.Dtos.ZoneDTO;
import Commune.Dev.Dtos.ZoneMapper;
import Commune.Dev.Dtos.ZoneResponse;
import Commune.Dev.Models.Halls;
import Commune.Dev.Models.Marchee;
import Commune.Dev.Models.Place;
import Commune.Dev.Models.Zone;
import Commune.Dev.Repositories.MarcheeRepository;
import Commune.Dev.Repositories.ZoneRepository;
import Commune.Dev.Request.ZoneRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ZoneService {

    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private MarcheeRepository marcheeRepository;
    @Autowired
    private PlaceService placeService;
    @Autowired
    private  ZoneMapper zoneMapper;

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
        response.setMarcheeId(Math.toIntExact(marchee.getId()));
        response.setMarcheeNom(marchee.getNom());

        return response;
    }

    public List<Zone> saveAll(List<Zone> zones) {
        return zoneRepository.saveAll(zones);
    }

    // =================== READ ===================
    @Transactional
    public List<ZoneDTO> findAll() {
        // Récupérer toutes les zones avec leur marché
        List<Zone> zones = zoneRepository.findAllWithMarchee();

        // Mapper chaque zone en DTO avec tous les calculs
        return zones.stream()
                .map(this::mapZoneToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mappe une Zone en ZoneDTO avec tous les calculs métier
     */
    private ZoneDTO mapZoneToDTO(Zone zone) {
        // Calcul du nombre de halls
        int nbrHall = calculateNombreHalls(zone);

        // Récupération de toutes les places (zone + halls)
        List<Place> toutesLesPlaces = getAllPlaces(zone);

        // Calculs des places
        int nbrPlaceTotal = toutesLesPlaces.size();
        int nbrPlaceLibre = calculatePlacesLibres(toutesLesPlaces);
        int nbrPlaceOccupee = calculatePlacesOccupees(toutesLesPlaces);

        // Mapping via le mapper
        return zoneMapper.toDTO(
                zone,
                nbrHall,
                nbrPlaceTotal,
                nbrPlaceLibre,
                nbrPlaceOccupee
        );
    }

    /**
     * Calcule le nombre de halls d'une zone
     */
    private int calculateNombreHalls(Zone zone) {
        return zone.getHalls() != null ? zone.getHalls().size() : 0;
    }

    /**
     * Récupère toutes les places d'une zone (directes + dans les halls)
     */
    private List<Place> getAllPlaces(Zone zone) {
        List<Place> toutesLesPlaces = new ArrayList<>();

        // Ajouter les places directes de la zone
        if (zone.getPlaces() != null) {
            toutesLesPlaces.addAll(zone.getPlaces());
        }

        // Ajouter les places des halls
        if (zone.getHalls() != null) {
            for (Halls hall : zone.getHalls()) {
                if (hall.getPlaces() != null) {
                    toutesLesPlaces.addAll(hall.getPlaces());
                }
            }
        }

        return toutesLesPlaces;
    }

    /**
     * Calcule le nombre de places libres
     */
    private int calculatePlacesLibres(List<Place> places) {
        return (int) places.stream()
                .filter(place -> !place.getIsOccuped())
                .count();
    }

    /**
     * Calcule le nombre de places occupées
     */
    private int calculatePlacesOccupees(List<Place> places) {
        return (int) places.stream()
                .filter(Place::getIsOccuped)
                .count();
    }

//    @Transactional
//    public ZoneDTO create(Zone zone) {
//        Zone savedZone = zoneRepository.save(zone);
//        return zoneMapper.toDTO(savedZone);
//    }

//    @Transactional
//    public ZoneDTO update(Long id, Zone zoneDetails) {
//        Zone zone = zoneRepository.findById(Math.toIntExact(id))
//                .orElseThrow(() -> new RuntimeException("Zone non trouvée avec l'id: " + id));
//
//        zone.setNom(zoneDetails.getNom());
//        zone.setDescription(zoneDetails.getDescription());
//
//        Zone updatedZone = zoneRepository.save(zone);
//        return zoneMapper.toDTO(updatedZone);
//    }

    @Transactional
    public void delete(Long id) {
        if (!zoneRepository.existsById(Math.toIntExact(id))) {
            throw new RuntimeException("Zone non trouvée avec l'id: " + id);
        }
        zoneRepository.deleteById(Math.toIntExact(id));
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
