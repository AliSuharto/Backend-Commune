package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDTO {

    private Integer id;
    private String nom;
    private String adresse;
    private Boolean isOccuped;
    private LocalDateTime dateDebutOccupation;
    private LocalDateTime dateFinOccupation;

    // IDs des entités liées (pour éviter la sérialisation complète)
    private Integer marcheeId;
    private String marcheeName;
    private Integer marchandId;
    private String marchandName;
    private Integer salleId;
    private String salleName;
    private Integer categorieId;
    private String categorieName;

    // NOUVELLE RELATION AVEC ZONE
    private Integer zoneId;
    private String zoneName;
}

// Classe pour les requêtes de création/mise à jour
@Data
@NoArgsConstructor
@AllArgsConstructor
class PlaceCreateRequest {

    private String nom;
    private String adresse;
    private Boolean isOccuped;
    private LocalDateTime dateDebutOccupation;
    private LocalDateTime dateFinOccupation;
    private Integer marcheeId;
    private Integer marchandId;
    private Integer salleId;
    private Integer categorieId;
    private Integer zoneId; // NOUVEAU CHAMP
}

// Classe pour les réponses avec pagination
@Data
@NoArgsConstructor
@AllArgsConstructor
class PlacePageResponse {

    private java.util.List<PlaceDTO> places;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
}

// Classe pour les statistiques des places
@Data
@NoArgsConstructor
@AllArgsConstructor
class PlaceStatsResponse {

    private long totalPlaces;
    private long occupiedPlaces;
    private long availablePlaces;
    private long expiredOccupations;
    private double occupationRate;
}

// Classe pour les filtres de recherche
@Data
@NoArgsConstructor
@AllArgsConstructor
class PlaceSearchFilter {

    private String nom;
    private String adresse;
    private Boolean isOccuped;
    private Integer marcheeId;
    private Integer marchandId;
    private Integer salleId;
    private Integer categorieId;
    private Integer zoneId; // NOUVEAU FILTRE
    private LocalDateTime dateDebutFrom;
    private LocalDateTime dateDebutTo;
    private LocalDateTime dateFinFrom;
    private LocalDateTime dateFinTo;
}