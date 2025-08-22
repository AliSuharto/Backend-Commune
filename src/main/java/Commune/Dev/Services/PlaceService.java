package Commune.Dev.Services;

import Commune.Dev.Models.Place;
import Commune.Dev.Repositories.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlaceService {

    @Autowired
    private PlaceRepository placeRepository;

    // CREATE operations
    public Place save(Place place) {
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