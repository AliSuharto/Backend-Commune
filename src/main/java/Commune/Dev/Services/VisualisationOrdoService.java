package Commune.Dev.Services;

import Commune.Dev.Dtos.*;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.MarchandsRepository;
import Commune.Dev.Repositories.MarcheeRepository;
import Commune.Dev.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisualisationOrdoService {

    private final MarcheeRepository marcheeRepository;
    private final MarchandsRepository marchandRepository;
    private final UserRepository userRepository;

    public VisualisationOrdoDTO getVisualisationData() {
        VisualisationOrdoDTO dto = new VisualisationOrdoDTO();

        // Récupération des données
        List<MarcheeDTO> marchees = getAllMarchees();
        List<MarchandDTO> marchands = getAllMarchands();
        List<UserResponse> users = getAllUsers();

        // Calcul des statistiques
        dto.setNbr_marchee(marchees.size());
        dto.setNbr_marchands(marchands.size());
        dto.setNbr_user(users.size());
        dto.setNbr_marchands_endettee(
                (int) marchands.stream()
                        .filter(m -> m.getEstEndette() != null && m.getEstEndette())
                        .count()
        );

        // Assignation des listes
        dto.setMarchees(marchees);
        dto.setMarchands(marchands);
        dto.setUsers(users);

        return dto;
    }

    private List<MarcheeDTO> getAllMarchees() {
        return marcheeRepository.findAll().stream()
                .map(this::convertToMarcheeDTO)
                .collect(Collectors.toList());
    }

    private List<MarchandDTO> getAllMarchands() {
        return marchandRepository.findAll().stream()
                .map(this::convertToMarchandDTO)
                .collect(Collectors.toList());
    }

    private List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    private MarcheeDTO convertToMarcheeDTO(Marchee marchee) {
        MarcheeDTO dto = new MarcheeDTO();
        dto.setId(Math.toIntExact(marchee.getId()));
        dto.setNom(marchee.getNom());
        dto.setAdresse(marchee.getAdresse());
        dto.setNbrPlace(marchee.getNbrPlace() != null ? marchee.getNbrPlace() : 0);

        // Calcul des statistiques
        Long totalPlaces = calculateTotalPlaces(marchee);
        Long occupiedPlaces = calculateOccupiedPlaces(marchee);
        Long availablePlaces = totalPlaces - occupiedPlaces;

        dto.setActualTotalPlaces(totalPlaces);
        dto.setOccupiedPlaces(occupiedPlaces);
        dto.setAvailablePlaces(availablePlaces);
        dto.setTotalZones(calculateTotalZones(marchee));
        dto.setTotalSalles(calculateTotalSalles(marchee));

        double occupationRate = totalPlaces > 0 ?
                (occupiedPlaces.doubleValue() / totalPlaces.doubleValue()) * 100 : 0.0;
        dto.setOccupationRate(Math.round(occupationRate * 100.0) / 100.0);

        Integer nbrPlace = marchee.getNbrPlace() != null ? marchee.getNbrPlace() : 0;
        double capacityUtilization = nbrPlace > 0 ?
                (totalPlaces.doubleValue() / nbrPlace) * 100 : 0.0;
        dto.setCapacityUtilization(Math.round(capacityUtilization * 100.0) / 100.0);

        dto.setIsOverCapacity(totalPlaces > nbrPlace);
        dto.setIsUnderUtilized(occupationRate < 50.0);

        return dto;
    }

    private MarchandDTO convertToMarchandDTO(Marchands marchand) {
        MarchandDTO dto = new MarchandDTO();
        dto.setId(marchand.getId());
        dto.setNom(marchand.getNom());
//        dto.setPrenom(marchand.getPrenom());
        dto.setAdress(marchand.getAdress());
        dto.setDescription(marchand.getDescription());
        dto.setActivite(marchand.getActivite());
        dto.setNumCIN(marchand.getNumCIN());
        dto.setPhoto(marchand.getPhoto());
        dto.setNumTel1(marchand.getNumTel1());
        dto.setNumTel2(marchand.getNumTel2());
        dto.setDateEnregistrement(marchand.getDateEnregistrement());
        dto.setEstEndette(marchand.getEstEndette());
        dto.setHasPlace(marchand.getPlaces() != null && !marchand.getPlaces().isEmpty());
        dto.setPlaces(convertPlaces(marchand.getPlaces()));
        return dto;
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setNom(user.getNom());
        response.setPrenom(user.getPrenom());
        response.setPseudo(user.getPseudo());
        response.setPhotoUrl(user.getPhotoUrl());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setMustChangePassword(user.getMustChangePassword());
        response.setTelephone(user.getTelephone());
        response.setCreatedByName(user.getCreatedBy() != null ?
                user.getCreatedBy().getNom() + " " + user.getCreatedBy().getPrenom() : null);
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    private PlaceDTOmarchands convertSinglePlace(Place place) {
        PlaceDTOmarchands dto = new PlaceDTOmarchands();

        dto.setId(place.getId());
        dto.setNom(safe(place.getNom()));
        dto.setDateDebutOccupation(place.getDateDebutOccupation());
        dto.setDateFinOccupation(place.getDateFinOccupation());

        if (place.getCategorie() != null) {
            dto.setCategorieId(place.getCategorie().getId());
            dto.setCategorieName(safe(String.valueOf(place.getCategorie().getNom())));
            dto.setMontant(place.getCategorie().getMontant());
        }

        // ======================
        // HALL / ZONE / MARCHEE
        // ======================
        Halls hall = place.getHall();
        Zone zone = place.getZone();
        Marchee marchee = place.getMarchee();

        String salleName = "";
        String zoneName = "";
        String marcheeName = "";

        // ========== SI HALL EXISTE ==========
        if (hall != null) {

            salleName = safe(hall.getNom());

            if (hall.getZone() != null) {
                zone = hall.getZone();
                zoneName = safe(zone.getNom());

                if (zone.getMarchee() != null) {
                    marchee = zone.getMarchee();
                    marcheeName = safe(marchee.getNom());
                } else if (hall.getMarchee() != null) {
                    marchee = hall.getMarchee();
                    marcheeName = safe(marchee.getNom());
                } else if (place.getMarchee() != null) {
                    marcheeName = safe(place.getMarchee().getNom());
                }

            } else {
                if (hall.getMarchee() != null) {
                    marcheeName = safe(hall.getMarchee().getNom());
                } else if (place.getMarchee() != null) {
                    marcheeName = safe(place.getMarchee().getNom());
                }
            }
        }

        // ========== PAS DE HALL MAIS ZONE EXISTE ==========
        else if (zone != null) {

            zoneName = safe(zone.getNom());

            if (zone.getMarchee() != null) {
                marcheeName = safe(zone.getMarchee().getNom());
            } else if (place.getMarchee() != null) {
                marcheeName = safe(place.getMarchee().getNom());
            }
        }

        // ========== NI HALL NI ZONE MAIS MARCHEE EXISTE ==========
        else if (marchee != null) {
            marcheeName = safe(marchee.getNom());
        }

        dto.setSalleName(salleName);
        dto.setZoneName(zoneName);
        dto.setMarcheeName(marcheeName);

        return dto;
    }


    private Long calculateTotalPlaces(Marchee marchee) {
        return (long) collectAllPlaces(marchee).size();
    }

    private Long calculateOccupiedPlaces(Marchee marchee) {
        return collectAllPlaces(marchee).stream()
                .filter(place -> place.getMarchands() != null)
                .count();
    }

    private Long calculateTotalZones(Marchee marchee) {
        return marchee.getZones() != null ? (long) marchee.getZones().size() : 0L;
    }

    private Long calculateTotalSalles(Marchee marchee) {
        long hallsInMarche = marchee.getHalls() != null ? marchee.getHalls().size() : 0;
        long hallsInZones = 0;

        if (marchee.getZones() != null) {
            hallsInZones = marchee.getZones().stream()
                    .filter(zone -> zone.getHalls() != null)
                    .mapToLong(zone -> zone.getHalls().size())
                    .sum();
        }

        return hallsInMarche + hallsInZones;
    }

    /**
     * Collecte toutes les places d'un marché en parcourant sa hiérarchie complète
     * Hiérarchie: Marchee -> Places directes | Zones -> Places | Halls -> Places
     */
    private List<Place> collectAllPlaces(Marchee marchee) {
        List<Place> allPlaces = new ArrayList<>();

        // 1. Places directement rattachées au marché
        if (marchee.getPlaces() != null && !marchee.getPlaces().isEmpty()) {
            allPlaces.addAll(marchee.getPlaces());
        }

        // 2. Places dans les halls directement rattachés au marché
        if (marchee.getHalls() != null) {
            marchee.getHalls().forEach(hall -> {
                if (hall.getPlaces() != null && !hall.getPlaces().isEmpty()) {
                    allPlaces.addAll(hall.getPlaces());
                }
            });
        }

        // 3. Places dans les zones et leurs halls
        if (marchee.getZones() != null) {
            marchee.getZones().forEach(zone -> {
                // Places directement dans la zone
                if (zone.getPlaces() != null && !zone.getPlaces().isEmpty()) {
                    allPlaces.addAll(zone.getPlaces());
                }

                // Places dans les halls de la zone
                if (zone.getHalls() != null) {
                    zone.getHalls().forEach(hall -> {
                        if (hall.getPlaces() != null && !hall.getPlaces().isEmpty()) {
                            allPlaces.addAll(hall.getPlaces());
                        }
                    });
                }
            });
        }

        return allPlaces;
    }



    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
    private List<PlaceDTOmarchands> convertPlaces(List<Place> places) {
        if (places == null) return null;

        return places.stream()
                .map(this::convertSinglePlace)
                .collect(Collectors.toList());
    }

}