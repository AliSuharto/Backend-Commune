package Commune.Dev.Services;

import Commune.Dev.Dtos.PlaceParentInfo;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.HallsRepository;
import Commune.Dev.Repositories.MarcheeRepository;
import Commune.Dev.Repositories.PlaceRepository;
import Commune.Dev.Repositories.ZoneRepository;
import Commune.Dev.Request.PlaceRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlaceService {
    @Autowired
    private HallsRepository hallRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private PlaceRepository placeRepository;
    @Autowired
    private MarcheeRepository marcheeRepository;

    // CREATE operations
    public Place save(Place place) {
        return placeRepository.save(place);
    }

        public Place createPlace(PlaceRequest request) {
            // Validation des données d'entrée
            validatePlaceRequest(request);

            Place place = new Place();
            place.setNom(request.getNom());
            place.setAdresse(request.getAdresse());
            place.setIsOccuped(request.isOccuped());

            // Déterminer et assigner le parent
            setPlaceParent(place, request);

            return placeRepository.save(place);
        }

        private void validatePlaceRequest(PlaceRequest request) {
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom de la place est obligatoire");
            }

            // Compter combien de parents sont spécifiés
            int parentCount = 0;
            if (request.getMarcheeId() != null) parentCount++;
            if (request.getHallId() != null) parentCount++;
            if (request.getZoneId() != null) parentCount++;

            if (parentCount == 0) {
                throw new IllegalArgumentException("Une place doit appartenir à un marché, un hall ou une zone");
            }

            if (parentCount > 1) {
                throw new IllegalArgumentException("Une place ne peut appartenir qu'à un seul parent (marché OU hall OU zone)");
            }
        }

        private void setPlaceParent(Place place, PlaceRequest request) {
            if (request.getMarcheeId() != null) {
                Marchee marchee = marcheeRepository.findById(Math.toIntExact(request.getMarcheeId()))
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Marché introuvable avec l'ID: " + request.getMarcheeId()));
                place.setMarchee(marchee);
                place.setHall(null);
                place.setZone(null);

            } else if (request.getHallId() != null) {
                Halls hall = hallRepository.findById(Math.toIntExact(request.getHallId()))
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Hall introuvable avec l'ID: " + request.getHallId()));
                place.setHall(hall);
                place.setMarchee(null);
                place.setZone(null);

            } else if (request.getZoneId() != null) {
                Zone zone = zoneRepository.findById(Math.toIntExact(request.getZoneId()))
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Zone introuvable avec l'ID: " + request.getZoneId()));
                place.setZone(zone);
                place.setMarchee(null);
                place.setHall(null);
            }
        }

        // Méthodes de lecture
        public List<Place> findPlacesByMarchee(Long marcheeId) {
            if (marcheeId == null) {
                throw new IllegalArgumentException("L'ID du marché ne peut pas être null");
            }
            return placeRepository.findByMarcheeId(marcheeId);
        }

        public List<Place> findPlacesByHall(Long hallId) {
            if (hallId == null) {
                throw new IllegalArgumentException("L'ID du hall ne peut pas être null");
            }
            return placeRepository.findByHallId(hallId);
        }

        public List<Place> findPlacesByZone(Long zoneId) {
            if (zoneId == null) {
                throw new IllegalArgumentException("L'ID de la zone ne peut pas être null");
            }
            return placeRepository.findByZoneId(zoneId);
        }

        public Place findPlaceById(Long id) {
            return placeRepository.findById(Math.toIntExact(id))
                    .orElseThrow(() -> new EntityNotFoundException("Place introuvable avec l'ID: " + id));
        }

        // Méthode pour obtenir le type et nom du parent
        public PlaceParentInfo getPlaceParentInfo(Place place) {
            if (place.getMarchee() != null) {
                return new PlaceParentInfo("MARCHEE", place.getMarchee().getId(), place.getMarchee().getNom());
            } else if (place.getHall() != null) {
                return new PlaceParentInfo("HALL", place.getHall().getId(), place.getHall().getNom());
            } else if (place.getZone() != null) {
                return new PlaceParentInfo("ZONE", place.getZone().getId(), place.getZone().getNom());
            }
            return new PlaceParentInfo("AUCUN", null, "Aucun parent");
        }

        // Méthode pour mettre à jour une place
        public Place updatePlace(Long placeId, PlaceRequest request) {
            Place existingPlace = findPlaceById(placeId);
            validatePlaceRequest(request);

            existingPlace.setNom(request.getNom());
            existingPlace.setAdresse(request.getAdresse());
            existingPlace.setIsOccuped(request.isOccuped());

            setPlaceParent(existingPlace, request);

            return placeRepository.save(existingPlace);
        }

        // Méthode pour supprimer une place
        public void deletePlace(Long placeId) {
            Place place = findPlaceById(placeId);
            placeRepository.delete(place);
        }

        // Méthode pour changer le statut d'occupation
        public Place toggleOccupationStatus(Long placeId) {
            Place place = findPlaceById(placeId);
            place.setIsOccuped(!place.getIsOccuped());
            return placeRepository.save(place);
        }

    public List<Place> saveAll(List<Place> places) {
        return placeRepository.saveAll(places);
    }

    // READ operations
    public List<Place> findAll() {
        return placeRepository.findAll();
    }

    public Optional<Place> findById(Integer id) {
        return placeRepository.findById(id);
    }

    public List<Place> findAllById(List<Integer> ids) {
        return placeRepository.findAllById(ids);
    }

    public List<Place> findByNomContainingIgnoreCase(String nom) {
        return placeRepository.findByNomContainingIgnoreCase(nom);
    }

    public List<Place> findByIsOccuped(Boolean isOccuped) {
        return placeRepository.findByIsOccuped(isOccuped);
    }

    public List<Place> findByAdresseContainingIgnoreCase(String adresse) {
        return placeRepository.findByAdresseContainingIgnoreCase(adresse);
    }

    // COUNT operations
    public long count() {
        return placeRepository.count();
    }

    public boolean existsById(Integer id) {
        return placeRepository.existsById(id);
    }

    // DELETE operations
    public void deleteById(Integer id) {
        placeRepository.deleteById(id);
    }

    public void delete(Place place) {
        placeRepository.delete(place);
    }

    public void deleteAll(List<Place> places) {
        placeRepository.deleteAll(places);
    }

    public void deleteAllById(List<Integer> ids) {
        placeRepository.deleteAllById(ids);
    }

    public void deleteAll() {
        placeRepository.deleteAll();
    }

    // Custom business methods
    public List<Place> findAvailablePlaces() {
        return placeRepository.findByIsOccuped(false);
    }

    public List<Place> findOccupiedPlaces() {
        return placeRepository.findByIsOccuped(true);
    }

    public long countAvailablePlaces() {
        return placeRepository.countByIsOccuped(false);
    }

    public long countOccupiedPlaces() {
        return placeRepository.countByIsOccuped(true);
    }

    public List<Place> findByZoneId(Integer zoneId) {
        return placeRepository.findByZoneId(zoneId);
    }


    public List<Place> findByZoneIdAndIsOccuped(Integer zoneId, Boolean isOccuped) {
        return placeRepository.findByZoneIdAndIsOccuped(zoneId, isOccuped);
    }

    public long countPlacesByZoneId(Integer zoneId) {
        return placeRepository.countByZoneId(zoneId);
    }

    public long countAvailablePlacesByZoneId(Integer zoneId) {
        return placeRepository.countByZoneIdAndIsOccuped(zoneId, false);
    }



}