package Commune.Dev.Dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarcheeDTO {

    private Integer id;
    private String nom;
    private String adresse;
    private Integer nbrPlace;

    // Statistiques calculées
    private Long actualTotalPlaces;
    private Long occupiedPlaces;
    private Long availablePlaces;
    private Long totalZones;
    private Long totalSalles;
    private Double occupationRate;
    private Double capacityUtilization;
    private Boolean isOverCapacity;
    private Boolean isUnderUtilized;
}

// Classe pour les requêtes de création/mise à jour
@Data
@NoArgsConstructor
@AllArgsConstructor
class MarcheeCreateRequest {

    private String nom;
    private String adresse;
    private Integer nbrPlace;
}

// Classe pour les réponses avec pagination
@Data
@NoArgsConstructor
@AllArgsConstructor
class MarcheePageResponse {

    private java.util.List<MarcheeDTO> marchees;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
}

// Classe pour les statistiques détaillées d'un marché
@Data
@NoArgsConstructor
@AllArgsConstructor
class MarcheeDetailedStats {

    private Integer marcheeId;
    private String marcheeName;
    private String marcheeAddress;
    private Integer declaredCapacity;

    // Statistiques des zones
    private Long totalZones;
    private java.util.List<String> zoneNames;

    // Statistiques des places
    private Long actualTotalPlaces;
    private Long occupiedPlaces;
    private Long availablePlaces;
    private Double occupationRate;
    private Double capacityUtilization;

    // Statistiques des salles
    private Long totalSalles;

    // Analyses
    private Boolean isOverCapacity;
    private Boolean isUnderUtilized;
    private Boolean hasZones;
    private Boolean hasSalles;
    private String capacityStatus; // "OVER", "UNDER", "OPTIMAL"
    private String utilizationLevel; // "LOW", "MEDIUM", "HIGH"
}

// Classe pour les filtres de recherche
@Data
@NoArgsConstructor
@AllArgsConstructor
class MarcheeSearchFilter {

    private String nom;
    private String adresse;
    private Integer minCapacity;
    private Integer maxCapacity;
    private Boolean hasZones;
    private Boolean hasSalles;
    private Boolean hasAvailablePlaces;
    private Double minOccupationRate;
    private Double maxOccupationRate;
    private String sortBy; // "nom", "capacity", "occupationRate"
    private String sortDirection; // "ASC", "DESC"
}

// Classe pour les statistiques globales
@Data
@NoArgsConstructor
@AllArgsConstructor
class GlobalMarcheeStats {

    private Long totalMarchees;
    private Integer totalDeclaredCapacity;
    private Long totalActualPlaces;
    private Long totalOccupiedPlaces;
    private Long totalAvailablePlaces;
    private Double globalOccupationRate;
    private Integer averageCapacityPerMarchee;
    private Long averageActualPlacesPerMarchee;

    // Marché remarquables
    private MarcheeDTO biggestMarchee;
    private MarcheeDTO smallestMarchee;
    private MarcheeDTO bestOccupationRate;
    private MarcheeDTO worstOccupationRate;
}

// Classe pour la comparaison de marchés
@Data
@NoArgsConstructor
@AllArgsConstructor
class MarcheeComparison {

    private MarcheeDetailedStats marchee1;
    private MarcheeDetailedStats marchee2;

    // Comparaisons
    private String biggerMarche;
    private String betterOccupationRate;
    private Long placeDifference;
    private Double occupationDifference;

    // Recommandations
    private String recommendation;
    private java.util.List<String> advantages1; // Avantages du marché 1
    private java.util.List<String> advantages2; // Avantages du marché 2
}

// Classe pour les rapports de performance
@Data
@NoArgsConstructor
@AllArgsConstructor
class MarcheePerformanceReport {

    private Integer marcheeId;
    private String marcheeName;
    private String reportDate;

    // Métriques de performance
    private Double occupationRate;
    private Double capacityUtilization;
    private Long totalRevenue; // Si disponible
    private Integer customerSatisfaction; // Si disponible

    // Tendances (comparaison avec période précédente)
    private String occupationTrend; // "UP", "DOWN", "STABLE"
    private Double occupationChange; // Pourcentage de changement

    // Recommandations d'amélioration
    private java.util.List<String> recommendations;
    private String priorityLevel; // "HIGH", "MEDIUM", "LOW"
}

// Classe pour les alertes sur les marchés
@Data
@NoArgsConstructor
@AllArgsConstructor
class MarcheeAlert {

    private Integer marcheeId;
    private String marcheeName;
    private String alertType; // "OVERCAPACITY", "UNDERUTILIZED", "NO_ZONES", "NO_AVAILABLE_PLACES"
    private String alertLevel; // "CRITICAL", "WARNING", "INFO"
    private String alertMessage;
    private String actionRequired;
    private java.time.LocalDateTime alertDate;
}