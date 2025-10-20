package Commune.Dev.Services;

import Commune.Dev.Dtos.AttributionResponse;
import Commune.Dev.Dtos.MarchandDTO;
import Commune.Dev.Dtos.PlaceAttrDTO;
import Commune.Dev.Models.Marchands;
import Commune.Dev.Models.Place;
import Commune.Dev.Repositories.MarchandsRepository;
import Commune.Dev.Repositories.PlaceRepository;
import Commune.Dev.Request.AttributionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttributionService {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private MarchandsRepository marchandsRepository;

    /**
     * Attribuer une place à un marchand
     */
    @Transactional
    public AttributionResponse attribuerPlace(AttributionRequest request) {
        try {
            // Vérifier que le marchand existe
            Marchands marchand = marchandsRepository.findById(request.getMarchandId())
                    .orElseThrow(() -> new RuntimeException("Marchand introuvable avec l'ID: " + request.getMarchandId()));

            // Vérifier que la place existe
            Place place = placeRepository.findById(request.getPlaceId())
                    .orElseThrow(() -> new RuntimeException("Place introuvable avec l'ID: " + request.getPlaceId()));

            // Vérifier que la place n'est pas déjà occupée
            if (place.getIsOccuped()) {
                return new AttributionResponse(false,
                        "Cette place est déjà occupée par " +
                                (place.getMarchands() != null ?
                                        place.getMarchands().getPrenom() + " " + place.getMarchands().getNom() :
                                        "un autre marchand"),
                        null, null);
            }

            // Vérifier que le marchand n'a pas déjà une place
            List<Place> placesExistantes = placeRepository.findAll().stream()
                    .filter(p -> p.getMarchands() != null && p.getMarchands().getId().equals(marchand.getId()) && p.getIsOccuped())
                    .collect(Collectors.toList());

            if (!placesExistantes.isEmpty()) {
                return new AttributionResponse(false,
                        "Ce marchand a déjà une place attribuée: " + placesExistantes.get(0).getNom(),
                        null, null);
            }

            // Effectuer l'attribution
            place.setMarchands(marchand);
            place.setIsOccuped(true);
            place.setDateDebutOccupation(LocalDateTime.now());
            place.setDateFinOccupation(null); // Pas de date de fin pour le moment

            Place savedPlace = placeRepository.save(place);

            // Préparer la réponse
            PlaceAttrDTO placeAttrDTO = convertToPlaceDTO(savedPlace);
            MarchandDTO marchandDTO = convertToMarchandDTO(marchand);

            return new AttributionResponse(true,
                    "Place " + place.getNom() + " attribuée avec succès à " + marchand.getPrenom() + " " + marchand.getNom(),
                    placeAttrDTO, marchandDTO);

        } catch (Exception e) {
            return new AttributionResponse(false, "Erreur lors de l'attribution: " + e.getMessage(), null, null);
        }
    }

    /**
     * Libérer une place (retirer l'attribution)
     */
    @Transactional
    public AttributionResponse libererPlace(Integer placeId) {
        try {
            Place place = placeRepository.findById(placeId)
                    .orElseThrow(() -> new RuntimeException("Place introuvable avec l'ID: " + placeId));

            if (!place.getIsOccuped()) {
                return new AttributionResponse(false, "Cette place n'est pas occupée", null, null);
            }

            Marchands ancienMarchand = place.getMarchands();

            // Libérer la place
            place.setMarchands(null);
            place.setIsOccuped(false);
            place.setDateFinOccupation(LocalDateTime.now());

            Place savedPlace = placeRepository.save(place);

            return new AttributionResponse(true,
                    "Place " + place.getNom() + " libérée avec succès" +
                            (ancienMarchand != null ? " (était occupée par " + ancienMarchand.getPrenom() + " " + ancienMarchand.getNom() + ")" : ""),
                    convertToPlaceDTO(savedPlace),
                    ancienMarchand != null ? convertToMarchandDTO(ancienMarchand) : null);

        } catch (Exception e) {
            return new AttributionResponse(false, "Erreur lors de la libération: " + e.getMessage(), null, null);
        }
    }

    /**
     * Obtenir la liste des marchands sans place
     */
    public List<MarchandDTO> getMarchandsSansPlace() {
        return marchandsRepository.findMarchandsSansPlace().stream()
                .map(this::convertToMarchandDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir la liste des places disponibles
     */
    public List<PlaceAttrDTO> getPlacesDisponibles() {
        return placeRepository.findByIsOccupedFalse().stream()
                .map(this::convertToPlaceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir la liste des places occupées
     */
    public List<PlaceAttrDTO> getPlacesOccupees() {
        return placeRepository.findByIsOccupedTrue().stream()
                .map(this::convertToPlaceDTO)
                .collect(Collectors.toList());
    }

    // Méthodes utilitaires de conversion
    private MarchandDTO convertToMarchandDTO(Marchands marchand) {
        MarchandDTO dto = new MarchandDTO();
        dto.setId(marchand.getId());
        dto.setNom(marchand.getNom());
        dto.setPrenom(marchand.getPrenom());
        dto.setNumCIN(marchand.getNumCIN());
        dto.setNumTel1(marchand.getNumTel1());
        dto.setAdress(marchand.getAdress());

        // Vérifier si le marchand a une place
        boolean hasPlace = placeRepository.findAll().stream()
                .anyMatch(p -> p.getMarchands() != null &&
                        p.getMarchands().getId().equals(marchand.getId()) &&
                        p.getIsOccuped());
        dto.setHasPlace(hasPlace);

        return dto;
    }

    private PlaceAttrDTO convertToPlaceDTO(Place place) {
        PlaceAttrDTO dto = new PlaceAttrDTO();
        dto.setId(place.getId());
        dto.setNom(place.getNom());
        dto.setAdresse(place.getAdresse());
        dto.setIsOccuped(place.getIsOccuped());

        if (place.getZone() != null) {
            dto.setZoneName(place.getZone().getNom()); // Assumant que Zone a un champ 'nom'
        }

        if (place.getHall() != null) {
            dto.setHallName(place.getHall().getNom()); // Assumant que Halls a un champ 'nom'
        }

        if (place.getMarchands() != null) {
            dto.setMarchand(convertToMarchandDTO(place.getMarchands()));
        }

        return dto;
    }
}
