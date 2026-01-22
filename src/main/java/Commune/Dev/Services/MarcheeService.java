package Commune.Dev.Services;

import Commune.Dev.Dtos.ApiResponse;
import Commune.Dev.Dtos.MarcheeInfoDTO;
import Commune.Dev.Dtos.MarcheeResponseDTO;
import Commune.Dev.Exception.MarcheeNotEmptyException;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.HallsRepository;
import Commune.Dev.Repositories.MarcheeRepository;
import Commune.Dev.Repositories.PlaceRepository;
import Commune.Dev.Repositories.ZoneRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarcheeService {

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private HallsRepository hallRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private MarcheeRepository marcheeRepository;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private PlaceService placeService;
    private static final Logger logger = LoggerFactory.getLogger(MarcheeService.class);

    // CREATE operations
    @Transactional
    public ResponseEntity<ApiResponse<Marchee>> save(Marchee marchee) {
        try {
            logger.info("Début de l'enregistrement du marché : nom={}, adresse={}",
                    marchee.getNom(), marchee.getAdresse());

            // Vérifier si le marché existe déjà
            if (marcheeRepository.existsByNomAndAdresse(marchee.getNom(), marchee.getAdresse())) {
                String message = "Ce marché existe déjà avec le nom '" + marchee.getNom() +
                        "' et l'adresse '" + marchee.getAdresse() + "'";
                logger.warn(message);
                return ResponseEntity
                        .badRequest()
                        .body(ApiResponse.error(message));
            }

            // ===============================
            // Préparation des relations avant sauvegarde
            // ===============================
            if (marchee.getZones() != null) {
                for (Zone zone : marchee.getZones()) {
                    zone.setMarchee(marchee);

                    if (zone.getHalls() != null) {
                        for (Halls hall : zone.getHalls()) {
                            hall.setMarchee(marchee);
                            hall.setZone(zone);

                            if (hall.getPlaces() != null) {
                                for (Place place : hall.getPlaces()) {
                                    place.setMarchee(marchee);
                                    place.setZone(zone);
                                    place.setHall(hall);
                                }
                            }
                        }
                    }

                    if (zone.getPlaces() != null) {
                        for (Place place : zone.getPlaces()) {
                            place.setMarchee(marchee);
                            place.setZone(zone);
                        }
                    }
                }
            }

            if (marchee.getHalls() != null) {
                for (Halls hall : marchee.getHalls()) {
                    hall.setMarchee(marchee);

                    if (hall.getPlaces() != null) {
                        for (Place place : hall.getPlaces()) {
                            place.setMarchee(marchee);
                            place.setHall(hall);
                        }
                    }
                }
            }

            if (marchee.getPlaces() != null) {
                for (Place place : marchee.getPlaces()) {
                    place.setMarchee(marchee);
                }
            }

            // ===============================
            // Sauvegarde principale
            // ===============================
            Marchee savedMarchee = marcheeRepository.save(marchee);

            logger.info("Enregistrement du marché '{}' terminé avec succès.", savedMarchee.getNom());

            // ✅ Retourne un JSON bien structuré
            return ResponseEntity.ok(ApiResponse.success("Marché enregistré avec succès.", savedMarchee));

        } catch (Exception ex) {
            logger.error("Erreur lors de l'enregistrement du marché '{}': {}",
                    marchee.getNom(), ex.getMessage(), ex);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error("Erreur lors de l'enregistrement du marché : " + ex.getMessage()));
        }
    }


    public ResponseEntity<Map<String, Object>> saveAll(List<Marchee> marchees) {
        List<Marchee> marcheesToSave = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();
        List<String> saved = new ArrayList<>();

        for (Marchee marchee : marchees) {
            // Vérifier chaque marché individuellement
            if (marcheeRepository.existsByNomAndAdresse(marchee.getNom(), marchee.getAdresse())) {
                duplicates.add("Marché '" + marchee.getNom() + "' à l'adresse '" +
                        marchee.getAdresse() + "' existe déjà");
            } else {
                marcheesToSave.add(marchee);
                saved.add("Marché '" + marchee.getNom() + "' à l'adresse '" +
                        marchee.getAdresse() + "'");
            }
        }

        if (!marcheesToSave.isEmpty()) {
            marcheeRepository.saveAll(marcheesToSave);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("nouveauxEnregistres", saved.size());
        response.put("duplicatesIgnores", duplicates.size());
        response.put("detailsNouveaux", saved);
        response.put("detailsDuplicates", duplicates);

        String message = String.format(
                "Opération terminée: %d nouveaux marchés enregistrés, %d doublons ignorés",
                saved.size(), duplicates.size()
        );
        response.put("message", message);

        return ResponseEntity.ok(response);
    }

    // READ operations
    public List<Marchee> findAll()
    {
        return marcheeRepository.findAll();
    }

    @Transactional
    public MarcheeInfoDTO getMarcheeInfo(Long marcheeId) {
        Marchee marchee = marcheeRepository.findById(Math.toIntExact(marcheeId))
                .orElseThrow(() -> new RuntimeException("Marché non trouvé avec l'ID: " + marcheeId));

        return convertToDTOS(marchee);
    }

    @Transactional
    public List<MarcheeInfoDTO> getAllMarcheesInfo() {
        return marcheeRepository.findAll()
                .stream()
                .map(this::convertToDTOS)
                .collect(Collectors.toList());
    }


    private MarcheeInfoDTO convertToDTOS(Marchee marchee) {

        MarcheeInfoDTO dto = new MarcheeInfoDTO();

        // ===== Infos de base =====
        dto.setId(marchee.getId().intValue());
        dto.setNom(marchee.getNom());
        dto.setAdresse(marchee.getAdresse());

        // ===== Calcul agrégé des places =====
        List<Place> allPlaces = getAllPlacesOfMarchee(marchee);

        long placeOccupe = countOccupied(allPlaces);
        long placeLibre  = countFree(allPlaces);
        long totalPlaces = allPlaces.size();

        dto.setNbrPlace((int) totalPlaces);
        dto.setPlaceOccupe(placeOccupe);
        dto.setPlaceLibre(placeLibre);

        double occupationRate = totalPlaces > 0
                ? (placeOccupe * 100.0) / totalPlaces
                : 0.0;

        dto.setOccupationRate(Math.round(occupationRate * 100.0) / 100.0);

        // ===== Totaux structurels =====
        dto.setTotalZones((long) marchee.getZones().size());
        dto.setTotalHalls((long) marchee.getHalls().size());

        // ===== Zones =====
        dto.setZone(marchee.getZones()
                .stream()
                .map(this::convertZoneToDTO)
                .collect(Collectors.toList()));

        // ===== Halls sans zone =====
        dto.setHall(marchee.getHalls()
                .stream()
                .filter(h -> h.getZone() == null)
                .map(this::convertHallToDTO)
                .collect(Collectors.toList()));

        // ===== Places directement rattachées au marché =====
        dto.setPlace(marchee.getPlaces()
                .stream()
                .map(this::convertPlaceToDTO)
                .collect(Collectors.toList()));

        // ===== Utilisateurs =====
        dto.setUser(marchee.getUsers() != null
                ? marchee.getUsers().stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList())
                : List.of());

        return dto;
    }


    private MarcheeInfoDTO.ZoneDto convertZoneToDTO(Zone zone) {

        MarcheeInfoDTO.ZoneDto dto = new MarcheeInfoDTO.ZoneDto();

        dto.setId(zone.getId());
        dto.setNom(zone.getNom());

        List<Place> allPlaces = getAllPlacesOfZone(zone);

        dto.setNbrPlace(allPlaces.size());
        dto.setPlaceOccupe(countOccupied(allPlaces));
        dto.setPlaceLibre(countFree(allPlaces));

        dto.setNbrHall(zone.getHalls().size());

        // ===== Halls de la zone =====
        dto.setHall(zone.getHalls()
                .stream()
                .map(this::convertHallToDTO)
                .collect(Collectors.toList()));

        // ===== Places directement dans la zone =====
        dto.setPlace(zone.getPlaces()
                .stream()
                .filter(p -> p.getHall() == null)
                .map(this::convertPlaceToDTO)
                .collect(Collectors.toList()));

        // ===== Utilisateurs =====
        dto.setUser(zone.getUsers() != null
                ? zone.getUsers().stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList())
                : List.of());

        return dto;
    }


    private MarcheeInfoDTO.HallDto convertHallToDTO(Halls hall) {

        MarcheeInfoDTO.HallDto dto = new MarcheeInfoDTO.HallDto();

        dto.setId(hall.getId());
        dto.setNom(hall.getNom());

        List<Place> allPlaces = hall.getPlaces();

        dto.setNbrPlace(allPlaces.size());
        dto.setPlaceOccupe(countOccupied(allPlaces));
        dto.setPlaceLibre(countFree(allPlaces));

        dto.setZoneId(hall.getZone() != null ? hall.getZone().getId().intValue() : null);
        dto.setMarcheeId(hall.getMarchee() != null ? hall.getMarchee().getId().intValue() : null);

        dto.setPlace(allPlaces
                .stream()
                .map(this::convertPlaceToDTO)
                .collect(Collectors.toList()));

        dto.setUser(hall.getUsers() != null
                ? hall.getUsers().stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList())
                : List.of());

        return dto;
    }

    private MarcheeInfoDTO.PlaceDto convertPlaceToDTO(Place place) {

        MarcheeInfoDTO.PlaceDto dto = new MarcheeInfoDTO.PlaceDto();

        dto.setId(place.getId().longValue());
        dto.setNom(place.getNom());
        dto.setStatut(Boolean.TRUE.equals(place.getIsOccuped()) ? "Occupée" : "Libre");

        dto.setHallId(place.getHall() != null ? place.getHall().getId().intValue() : null);
        dto.setZoneId(place.getZone() != null ? place.getZone().getId().intValue() : null);
        dto.setMarcheeId(place.getMarchee() != null ? place.getMarchee().getId().intValue() : null);
        dto.setNomMarchand(
                place.getMarchands() != null
                        ? place.getMarchands().getNom()
                        : null
        );
        dto.setStatutMarchand(
                place.getMarchands() != null
                        ? String.valueOf(place.getMarchands().getStatut())
                        : null
        );
        return dto;
    }


    private MarcheeInfoDTO.UserInfoDto convertUserToDTO(User user) {

        MarcheeInfoDTO.UserInfoDto dto = new MarcheeInfoDTO.UserInfoDto();

        dto.setId(user.getId());
        dto.setNom(user.getNom() + " " + user.getPrenom());
        dto.setPhoneNumber(user.getTelephone());
        dto.setMail(user.getEmail());

        return dto;
    }
    private List<Place> getAllPlacesOfMarchee(Marchee marchee) {

        List<Place> places = new ArrayList<>();

        // Places directement dans le marché
        places.addAll(marchee.getPlaces());

        // Halls sans zone
        marchee.getHalls().stream()
                .filter(h -> h.getZone() == null)
                .forEach(h -> places.addAll(h.getPlaces()));

        // Zones + halls des zones
        for (Zone zone : marchee.getZones()) {
            places.addAll(getAllPlacesOfZone(zone));
        }

        return places;
    }
    private List<Place> getAllPlacesOfZone(Zone zone) {

        List<Place> places = new ArrayList<>();

        // Places directement dans la zone
        places.addAll(zone.getPlaces());

        // Places dans les halls de la zone
        for (Halls hall : zone.getHalls()) {
            places.addAll(hall.getPlaces());
        }

        return places;
    }
    private long countOccupied(List<Place> places) {
        return places.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsOccuped()))
                .count();
    }

    private long countFree(List<Place> places) {
        return places.stream()
                .filter(p -> p.getIsOccuped() == null || !p.getIsOccuped())
                .count();
    }







    public Optional<Marchee> findById(Integer id) {
        return marcheeRepository.findById(id);
    }

    public List<Marchee> findAllById(List<Integer> ids) {
        return marcheeRepository.findAllById(ids);
    }

    public List<Marchee> findByNomContainingIgnoreCase(String nom) {
        return marcheeRepository.findByNomContainingIgnoreCase(nom);
    }

    public List<Marchee> findByAdresseContainingIgnoreCase(String adresse) {
        return marcheeRepository.findByAdresseContainingIgnoreCase(adresse);
    }

    public List<Marchee> findByNbrPlaceGreaterThanEqual(Integer nbrPlace) {
        return marcheeRepository.findByNbrPlaceGreaterThanEqual(nbrPlace);
    }

    public List<Marchee> findAllOrderByNbrPlaceAsc() {
        return marcheeRepository.findAllByOrderByNbrPlaceAsc();
    }

    public List<Marchee> findAllOrderByNbrPlaceDesc() {
        return marcheeRepository.findAllByOrderByNbrPlaceDesc();
    }

    // COUNT operations
    public long count() {
        return marcheeRepository.count();
    }

    public boolean existsById(Integer id) {
        return marcheeRepository.existsById(id);
    }

    // DELETE operations
    @Transactional
    public void deleteById(Long id) {
        Marchee marchee = marcheeRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new RuntimeException("Marché non trouvé avec l'ID: " + id));

        // Vérifier si le marché est vide
        if (!isMarcheeEmpty(marchee)) {
            throw new MarcheeNotEmptyException(
                    "Le marché '" + marchee.getNom() + "' n'est pas vide. " +
                            "Veuillez supprimer d'abord tous les zones, halls et places associés."
            );
        }

        marcheeRepository.deleteById(Math.toIntExact(id));
    }

    /**
     * Vérifie si un marché est vide (sans zones, halls, ni places)
     */
    private boolean isMarcheeEmpty(Marchee marchee) {
        boolean hasNoZones = marchee.getZones() == null || marchee.getZones().isEmpty();
        boolean hasNoHalls = marchee.getHalls() == null || marchee.getHalls().isEmpty();
        boolean hasNoPlaces = marchee.getPlaces() == null || marchee.getPlaces().isEmpty();

        return hasNoZones && hasNoHalls && hasNoPlaces;
    }

    /**
     * Méthode optionnelle pour obtenir des détails sur ce qui empêche la suppression
     */
    public String getMarcheeDeleteBlockingInfo(Long id) {
        Marchee marchee = marcheeRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new RuntimeException("Marché non trouvé avec l'ID: " + id));

        List<String> blockingElements = new ArrayList<>();

        if (marchee.getZones() != null && !marchee.getZones().isEmpty()) {
            blockingElements.add(marchee.getZones().size() + " zone(s)");
        }
        if (marchee.getHalls() != null && !marchee.getHalls().isEmpty()) {
            blockingElements.add(marchee.getHalls().size() + " hall(s)");
        }
        if (marchee.getPlaces() != null && !marchee.getPlaces().isEmpty()) {
            blockingElements.add(marchee.getPlaces().size() + " place(s)");
        }

        if (blockingElements.isEmpty()) {
            return "Le marché est vide et peut être supprimé";
        }

        return "Le marché contient : " + String.join(", ", blockingElements);
    }

    public void delete(Marchee marchee) {
        marcheeRepository.delete(marchee);
    }

    public void deleteAll(List<Marchee> marchees) {
        marcheeRepository.deleteAll(marchees);
    }

    public void deleteAllById(List<Integer> ids) {
        marcheeRepository.deleteAllById(ids);
    }

    public void deleteAll() {
        marcheeRepository.deleteAll();
    }

    // Custom business methods
    public Map<String, Object> getMarcheeStatistics(Integer marcheeId) {
        Map<String, Object> stats = new HashMap<>();

        Optional<Marchee> marcheeOpt = findById(marcheeId);
        if (marcheeOpt.isPresent()) {
            Marchee marchee = marcheeOpt.get();

            // Informations de base du marché
            stats.put("marcheeId", marcheeId);
            stats.put("marcheeName", marchee.getNom());
            stats.put("marcheeAddress", marchee.getAdresse());
            stats.put("declaredCapacity", marchee.getNbrPlace());

            // Statistiques des zones
            long totalZones = zoneService.countByMarcheeId(marcheeId);
            stats.put("totalZones", totalZones);

            // Statistiques des places
            List<Place> places = marchee.getPlaces();
            long totalPlaces = places != null ? places.size() : 0;
            long occupiedPlaces = places != null ?
                    places.stream().mapToLong(p -> p.getIsOccuped() != null && p.getIsOccuped() ? 1 : 0).sum() : 0;
            long availablePlaces = totalPlaces - occupiedPlaces;

            double occupationRate = totalPlaces > 0 ? (double) occupiedPlaces / totalPlaces * 100 : 0.0;
            double capacityUtilization = marchee.getNbrPlace() != null && marchee.getNbrPlace() > 0 ?
                    (double) totalPlaces / marchee.getNbrPlace() * 100 : 0.0;

            stats.put("actualTotalPlaces", totalPlaces);
            stats.put("occupiedPlaces", occupiedPlaces);
            stats.put("availablePlaces", availablePlaces);
            stats.put("occupationRate", Math.round(occupationRate * 100.0) / 100.0);
            stats.put("capacityUtilization", Math.round(capacityUtilization * 100.0) / 100.0);

            // Statistiques des salles
            List<Halls> halls = marchee.getHalls();
            long totalSalles = halls != null ? halls.size() : 0;
            stats.put("totalSalles", totalSalles);

            // Analyse comparative
            stats.put("isOverCapacity", totalPlaces > (marchee.getNbrPlace() != null ? marchee.getNbrPlace() : 0));
            stats.put("isUnderUtilized", occupationRate < 50.0);
            stats.put("hasZones", totalZones > 0);
            stats.put("hasHalls", totalSalles > 0);
        }

        return stats;
    }

    public List<Zone> getZonesByMarcheeId(Integer marcheeId) {
        return zoneService.findByMarcheeId(marcheeId);
    }

    public List<Place> getPlacesByMarcheeId(Integer marcheeId) {
        Optional<Marchee> marchee = findById(marcheeId);
        return marchee.map(Marchee::getPlaces).orElse(List.of());
    }

    public List<Halls> getSallesByMarcheeId(Integer marcheeId) {
        Optional<Marchee> marchee = findById(marcheeId);
        return marchee.map(Marchee::getHalls).orElse(List.of());
    }

    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Marchee> allMarchees = findAll();

        // Statistiques globales
        stats.put("totalMarchees", allMarchees.size());

        int totalDeclaredCapacity = allMarchees.stream()
                .mapToInt(m -> m.getNbrPlace() != null ? m.getNbrPlace() : 0)
                .sum();

        long totalActualPlaces = allMarchees.stream()
                .mapToLong(m -> m.getPlaces() != null ? m.getPlaces().size() : 0)
                .sum();

        long totalOccupiedPlaces = allMarchees.stream()
                .mapToLong(m -> m.getPlaces() != null ?
                        m.getPlaces().stream().mapToLong(p -> p.getIsOccuped() != null && p.getIsOccuped() ? 1 : 0).sum() : 0)
                .sum();

        long totalAvailablePlaces = totalActualPlaces - totalOccupiedPlaces;

        double globalOccupationRate = totalActualPlaces > 0 ?
                (double) totalOccupiedPlaces / totalActualPlaces * 100 : 0.0;

        stats.put("totalDeclaredCapacity", totalDeclaredCapacity);
        stats.put("totalActualPlaces", totalActualPlaces);
        stats.put("totalOccupiedPlaces", totalOccupiedPlaces);
        stats.put("totalAvailablePlaces", totalAvailablePlaces);
        stats.put("globalOccupationRate", Math.round(globalOccupationRate * 100.0) / 100.0);

        // Statistiques par marché
        stats.put("averageCapacityPerMarchee", allMarchees.size() > 0 ? totalDeclaredCapacity / allMarchees.size() : 0);
        stats.put("averageActualPlacesPerMarchee", allMarchees.size() > 0 ? totalActualPlaces / allMarchees.size() : 0);

        // Marché le plus grand
        Optional<Marchee> biggestMarchee = allMarchees.stream()
                .max((m1, m2) -> Integer.compare(
                        m1.getPlaces() != null ? m1.getPlaces().size() : 0,
                        m2.getPlaces() != null ? m2.getPlaces().size() : 0
                ));

        if (biggestMarchee.isPresent()) {
            Map<String, Object> biggest = new HashMap<>();
            biggest.put("id", biggestMarchee.get().getId());
            biggest.put("nom", biggestMarchee.get().getNom());
            biggest.put("actualPlaces", biggestMarchee.get().getPlaces() != null ? biggestMarchee.get().getPlaces().size() : 0);
            stats.put("biggestMarchee", biggest);
        }

        return stats;
    }

    public Map<String, Object> compareMarchees(Integer id1, Integer id2) {
        Map<String, Object> comparison = new HashMap<>();

        Optional<Marchee> marchee1Opt = findById(id1);
        Optional<Marchee> marchee2Opt = findById(id2);

        if (marchee1Opt.isPresent() && marchee2Opt.isPresent()) {
            Marchee marchee1 = marchee1Opt.get();
            Marchee marchee2 = marchee2Opt.get();

            Map<String, Object> stats1 = getMarcheeStatistics(id1);
            Map<String, Object> stats2 = getMarcheeStatistics(id2);

            comparison.put("marchee1", stats1);
            comparison.put("marchee2", stats2);

            // Comparaisons
            long places1 = (Long) stats1.get("actualTotalPlaces");
            long places2 = (Long) stats2.get("actualTotalPlaces");

            double occupation1 = (Double) stats1.get("occupationRate");
            double occupation2 = (Double) stats2.get("occupationRate");

            comparison.put("biggerMarche", places1 > places2 ? marchee1.getNom() : marchee2.getNom());
            comparison.put("betterOccupationRate", occupation1 > occupation2 ? marchee1.getNom() : marchee2.getNom());
            comparison.put("placeDifference", Math.abs(places1 - places2));
            comparison.put("occupationDifference", Math.abs(occupation1 - occupation2));
        }

        return comparison;
    }

    // Méthodes utilitaires
    public boolean hasZones(Integer marcheeId) {
        return zoneService.countByMarcheeId(marcheeId) > 0;
    }

    public boolean hasAvailablePlaces(Integer marcheeId) {
        Optional<Marchee> marchee = findById(marcheeId);
        if (marchee.isPresent() && marchee.get().getPlaces() != null) {
            return marchee.get().getPlaces().stream()
                    .anyMatch(p -> p.getIsOccuped() == null || !p.getIsOccuped());
        }
        return false;
    }

    public List<Marchee> findMarcheesWithAvailablePlaces() {
        return marcheeRepository.findMarcheesWithAvailablePlaces();
    }

    public List<Marchee> findMarcheesWithoutZones() {
        return marcheeRepository.findMarcheesWithoutZones();
    }

    public List<MarcheeResponseDTO> getAllMarcheeStats() {
        List<Marchee> marchees = marcheeRepository.findAll();

        return marchees.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MarcheeResponseDTO convertToDTO(Marchee marchee) {
        List<Place> allPlaces = collectAllPlaces(marchee);

        // Filtrer les places null
        long totalPlaces = allPlaces.stream()
                .filter(Objects::nonNull)  // ← Ajoutez ceci
                .count();

        long placesOccupees = allPlaces.stream()
                .filter(Objects::nonNull)  // ← Ajoutez ceci
                .filter(place -> place.getIsOccuped() != null && place.getIsOccuped())
                .count();

        double tauxOccupation = totalPlaces > 0
                ? (placesOccupees * 100.0 / totalPlaces)
                : 0.0;

        return new MarcheeResponseDTO(
                marchee.getId(),
                marchee.getNom(),
                marchee.getAdresse(),
                totalPlaces,
                placesOccupees,
                Math.round(tauxOccupation * 100.0) / 100.0
        );
    }

    /**
     * Récupère toutes les places rattachées à ce marché,
     * qu'elles soient directement liées ou via les zones/halls imbriqués.
     */
    private List<Place> collectAllPlaces(Marchee marchee) {
        List<Place> allPlaces = new ArrayList<>();

        // 1. Places directement dans le marché
        if (marchee.getPlaces() != null) {
            marchee.getPlaces().stream()
                    .filter(Objects::nonNull)  // ← Filtre les null
                    .forEach(allPlaces::add);
        }

        // 2. Places dans chaque zone
        if (marchee.getZones() != null) {
            marchee.getZones().stream()
                    .filter(Objects::nonNull)  // ← Filtre les zones null
                    .forEach(zone -> {
                        if (zone.getPlaces() != null) {
                            zone.getPlaces().stream()
                                    .filter(Objects::nonNull)
                                    .forEach(allPlaces::add);
                        }

                        // 3. Places dans les halls des zones
                        if (zone.getHalls() != null) {
                            zone.getHalls().stream()
                                    .filter(Objects::nonNull)
                                    .forEach(hall -> {
                                        if (hall.getPlaces() != null) {
                                            hall.getPlaces().stream()
                                                    .filter(Objects::nonNull)
                                                    .forEach(allPlaces::add);
                                        }
                                    });
                        }
                    });
        }

        // 4. Halls directement rattachés au marché
        if (marchee.getHalls() != null) {
            marchee.getHalls().stream()
                    .filter(Objects::nonNull)
                    .forEach(hall -> {
                        if (hall.getPlaces() != null) {
                            hall.getPlaces().stream()
                                    .filter(Objects::nonNull)
                                    .forEach(allPlaces::add);
                        }
                    });
        }

        return allPlaces;
    }








}