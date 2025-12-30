package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncDataResponse {
    private UserSyncData user;
    private List<MarcheeData> marchees;
    private List<ZoneData> zones;
    private List<HallData> halls;
    private List<PlaceData> places;
    private List<MarchandData> marchands;
    private List<PaiementData> paiements;
    private LocalDateTime syncTimestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSyncData {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String role;
        private String telephone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarcheeData {
        private Integer id;
        private String nom;
        private String description;
        private String adresse;
        private String codeUnique;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZoneData {
        private Long id;
        private String nom;
        private String description;
        private String codeUnique;
        private Long marcheeId;
        private String marcheeName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HallData {
        private Long id;
        private String nom;
        private Integer numero;
        private String description;
        private String codeUnique;
        private Long nbrPlace;
        private Long marcheeId;
        private Long zoneId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceData {
        private Integer id;
        private Integer numero;
        private String codeUnique;
        private String statut;
        private Double droitannuel;
        private Double categorie;
        private Long hallId;
        private Long zoneId;
        private Long marcheeId;
        private Long marchandId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarchandData {
        private Long id;
        private String nom;
        private String prenom;
        private String telephone;
        private String cin;
        private String email;
        private String adresse;
        private String numeroPatente;
        private String typeActivite;
        private LocalDateTime dateInscription;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaiementData {
        private Long id;
        private String numeroQuittance;
        private Double montant;
        private String typePaiement;
        private LocalDateTime datePaiement;
        private String statut;
        private Long marchandId;
        private Long placeId;
        private Long agentId;
        private String periodeDebut;
        private String periodeFin;
    }
}
