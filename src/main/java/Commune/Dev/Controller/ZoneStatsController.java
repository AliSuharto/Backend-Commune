//package Commune.Dev.Controller;
//
//import Commune.Dev.Services.PlaceService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/zones")
//@CrossOrigin(origins = "*")
//public class ZoneStatsController {
//
//    @Autowired
//    private PlaceService placeService;
//
//    // Statistiques d'une zone spécifique
//    @GetMapping("/{zoneId}/stats")
//    public ResponseEntity<Map<String, Object>> getZoneStatistics(@PathVariable Integer zoneId) {
//        try {
//            Map<String, Object> stats = new HashMap<>();
//
//            long totalPlaces = placeService.countPlacesByZoneId(zoneId);
//            long availablePlaces = placeService.countAvailablePlacesByZoneId(zoneId);
//            long occupiedPlaces = totalPlaces - availablePlaces;
//
//            double occupationRate = totalPlaces > 0 ?
//                    (double) occupiedPlaces / totalPlaces * 100 : 0.0;
//
//            stats.put("zoneId", zoneId);
//            stats.put("totalPlaces", totalPlaces);
//            stats.put("occupiedPlaces", occupiedPlaces);
//            stats.put("availablePlaces", availablePlaces);
//            stats.put("occupationRate", Math.round(occupationRate * 100.0) / 100.0);
//
//            return ResponseEntity.ok(stats);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Compter les places totales dans une zone
//    @GetMapping("/{zoneId}/count")
//    public ResponseEntity<Long> countPlacesInZone(@PathVariable Integer zoneId) {
//        try {
//            long count = placeService.countPlacesByZoneId(zoneId);
//            return ResponseEntity.ok(count);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Compter les places disponibles dans une zone
//    @GetMapping("/{zoneId}/count/available")
//    public ResponseEntity<Long> countAvailablePlacesInZone(@PathVariable Integer zoneId) {
//        try {
//            long count = placeService.countAvailablePlacesByZoneId(zoneId);
//            return ResponseEntity.ok(count);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Compter les places occupées dans une zone
//    @GetMapping("/{zoneId}/count/occupied")
//    public ResponseEntity<Long> countOccupiedPlacesInZone(@PathVariable Integer zoneId) {
//        try {
//            long totalPlaces = placeService.countPlacesByZoneId(zoneId);
//            long availablePlaces = placeService.countAvailablePlacesByZoneId(zoneId);
//            long occupiedPlaces = totalPlaces - availablePlaces;
//            return ResponseEntity.ok(occupiedPlaces);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//}
