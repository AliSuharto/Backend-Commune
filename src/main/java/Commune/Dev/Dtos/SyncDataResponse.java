package Commune.Dev.Dtos;

import Commune.Dev.Models.Paiement;
import Commune.Dev.Models.QuittancePlage;
import Commune.Dev.Models.StatusQuittance;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private List<QuittanceData> quittances;
    private List<SessionData> sessions;
    private LocalDateTime syncTimestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSyncData {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String password;
        private String role;
        private String telephone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarcheeData {
        private Long id;
        private String nom;
        private String description;
        private String adresse;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZoneData {
        private Long id;
        private String nom;
        private String description;
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
        private String nom;
        private String statut;
        private LocalDateTime dateDebutOccupation;
        private BigDecimal droitannuel;
        private BigDecimal categorie;
        private Long hallId;
        private Long zoneId;
        private Long marcheeId;
        private Integer marchandId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarchandData {
        private Integer id;
        private String nom;
        private String prenom;
        private String statutDePaiement;
        private String etat;
        private String telephone;
        private String cin;
        private String Nif;
        private String Stat;
        private String typeActivite;
        private LocalDateTime dateInscription;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaiementData {
        private Integer id;
//        private String numeroQuittance;
        private BigDecimal montant;
        private String typePaiement;
        private LocalDateTime datePaiement;
        private String motif;
        private Integer marchandId;
        private Integer placeId;
        private Integer sessionId;
        private Long agentId;
        private String dateDebut;
        private String dateFin;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuittanceData {

        private Long id;

        private LocalDateTime creationDate;

//        private Long percepteurId; //celui qui peut utiliser le quittance.

        private LocalDateTime dateUtilisation;

        private String nom;

        private String etat;

        private Integer QuittancePlageId;

        private Integer paiementId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionData{
        private Integer Id;
        private String nom;
        private BigDecimal montant;
        private LocalDateTime DateOuverture;
        private LocalDateTime DateFermeture;
        private String statut;
        private Integer RegisseurPrincipalId;
        private LocalDateTime validation_date;
    }
}
