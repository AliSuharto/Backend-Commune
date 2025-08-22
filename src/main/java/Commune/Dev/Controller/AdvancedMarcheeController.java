//package Commune.Dev.Controllers;
//
//import Commune.Dev.Models.Marchee;
//import Commune.Dev.Services.MarcheeService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/marchees/advanced")
//@CrossOrigin(origins = "*")
//public class AdvancedMarcheeController {
//
//    @Autowired
//    private MarcheeService marcheeService;
//
//    // Endpoints pour analyses avancées
//
//    // Marchés avec places disponibles
//    @GetMapping("/with-available-places")
//    public ResponseEntity<List<Marchee>> getMarcheesWithAvailablePlaces() {
//        try {
//            List<Marchee> marchees = marcheeService.findMarcheesWithAvailablePlaces();
//            return ResponseEntity.ok(marchees);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Marchés sans zones
//    @GetMapping("/without-zones")
//    public ResponseEntity<List<Marchee>> getMarcheesWithoutZones() {
//        try {
//            List<Marchee> marchees = marcheeService.findMarcheesWithoutZones();
//            return ResponseEntity.ok(marchees);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Recherche multicritère
//    @PostMapping("/search-advanced")
//    public ResponseEntity<List<Marchee>> advancedSearch(@RequestBody Map<String, Object> criteria) {
//        try {
//            String nom = (String) criteria.get("nom");
//            String adresse = (String) criteria.get("adresse");
//            Integer minCapacity = (Integer) criteria.get("minCapacity");
//            Integer maxCapacity = (Integer) criteria.get("maxCapacity");
//
//            List<Marchee> marchees = marcheeService.findByMultipleCriteria(nom, adresse, minCapacity, maxCapacity);
//            return ResponseEntity.ok(marchees);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Alertes sur les marchés
//    @GetMapping("/alerts")
//    public ResponseEntity<List<Map<String, Object>>> getMarcheeAlerts() {
//        try {
//            List<Map<String, Object>> alerts = marcheeService.generateAlerts();
//            return ResponseEntity.ok(alerts);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Rapport de performance
//    @GetMapping("/{id}/performance-report")
//    public ResponseEntity<Map<String, Object>> getPerformanceReport(@PathVariable Integer id) {
//        try {
//            Map<String, Object> report = marcheeService.generatePerformanceReport(id);
//            if (report.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//            return ResponseEntity.ok(report);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Top marchés par critère
//    @GetMapping("/top")
//    public ResponseEntity<Map<String, Object>> getTopMarchees(
//            @RequestParam(defaultValue = "capacity") String criteria,
//            @RequestParam(defaultValue = "5") Integer limit) {
//        try {
//            Map<String, Object> topMarchees = marcheeService.getTopMarchees(criteria, limit);
//            return ResponseEntity.ok(topMarchees);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Analyse de tendances (simulation)
//    @GetMapping("/{id}/trends")
//    public ResponseEntity<Map<String, Object>> getTrendAnalysis(@PathVariable Integer id) {
//        try {
//            Map<String, Object> trends = marcheeService.analyzeTrends(id);
//            if (trends.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//            return ResponseEntity.ok(trends);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Recommandations d'amélioration
//    @GetMapping("/{id}/recommendations")
//    public ResponseEntity<Map<String, Object>> getRecommendations(@PathVariable Integer id) {
//        try {
//            Map<String, Object> recommendations = marcheeService.generateRecommendations(id);
//            if (recommendations.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//            return ResponseEntity.ok(recommendations);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Tableau de bord exécutif
//    @GetMapping("/dashboard")
//    public ResponseEntity<Map<String, Object>> getExecutiveDashboard() {
//        try {
//            Map<String, Object> dashboard = marcheeService.generateExecutiveDashboard();
//            return ResponseEntity.ok(dashboard);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Export des données (simulation JSON)
//    @GetMapping("/export")
//    public ResponseEntity<Map<String, Object>> exportMarcheeData(
//            @RequestParam(defaultValue = "json") String format) {
//        try {
//            Map<String, Object> exportData = marcheeService.exportAllData(format);
//            return ResponseEntity.ok(exportData);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    // Prévisions de capacité (simulation)
//    @GetMapping("/{id}/capacity-forecast")
//    public ResponseEntity<Map<String, Object>> getCapacityForecast(@PathVariable Integer id) {
//        try {
//            Map<String, Object> forecast = marcheeService.forecastCapacity(id);
//            if (forecast.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//            return ResponseEntity.ok(forecast);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//}