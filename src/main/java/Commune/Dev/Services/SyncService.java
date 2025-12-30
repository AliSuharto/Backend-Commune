package Commune.Dev.Services;

import Commune.Dev.Dtos.SyncDataResponse;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final UserRepository userRepository;
    private final MarcheeRepository marcheeRepository;
    private final ZoneRepository zoneRepository;
    private final HallsRepository hallsRepository;
    private final PlaceRepository placeRepository;
    private final MarchandsRepository marchandRepository;
    private final PaiementRepository paiementRepository;

    @Transactional(readOnly = true)
    public SyncDataResponse getSyncDataForUser(Long userId) {
        log.info("🔄 Début de synchronisation pour l'utilisateur ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        SyncDataResponse response = new SyncDataResponse();
        response.setSyncTimestamp(LocalDateTime.now());

        // 1. Données de l'utilisateur
        response.setUser(mapUserToSyncData(user));

        // 2. Récupérer tous les marchés, zones et halls de l'utilisateur
        Set<Long> marcheeIds = new HashSet<>();
        Set<Long> zoneIds = new HashSet<>();
        Set<Long> hallIds = new HashSet<>();

        if (user.getMarchees() != null) {
            marcheeIds.addAll(user.getMarchees().stream()
                    .map(Marchee::getId)
                    .collect(Collectors.toSet()));
        }

        if (user.getZones() != null) {
            zoneIds.addAll(user.getZones().stream()
                    .map(Zone::getId)
                    .collect(Collectors.toSet()));
        }

        if (user.getHalls() != null) {
            hallIds.addAll(user.getHalls().stream()
                    .map(Halls::getId)
                    .collect(Collectors.toSet()));
        }

        log.info("📊 Marchés: {}, Zones: {}, Halls: {}", marcheeIds.size(), zoneIds.size(), hallIds.size());

        // 3. Charger les données des marchés
        List<Integer> marcheeIdsInt = marcheeIds.stream()
                .map(Long::intValue)
                .collect(Collectors.toList());

        List<Marchee> marchees = new ArrayList<>();
        if (!marcheeIdsInt.isEmpty()) {
            marchees = marcheeRepository.findAllById(marcheeIdsInt);
        }
        response.setMarchees(marchees.stream()
                .map(this::mapMarcheeToData)
                .collect(Collectors.toList()));

        // 4. Charger les données des zones
        List<Integer> zoneIdsInt = zoneIds.stream()
                .map(Long::intValue)
                .collect(Collectors.toList());

        List<Zone> zones = new ArrayList<>();
        if (!zoneIdsInt.isEmpty()) {
            zones = zoneRepository.findAllById(zoneIdsInt);
        }
        response.setZones(zones.stream()
                .map(this::mapZoneToData)
                .collect(Collectors.toList()));

        // 5. Charger les données des halls
        List<Integer> hallIdsInt = hallIds.stream()
                .map(Long::intValue)
                .collect(Collectors.toList());

        List<Halls> halls = new ArrayList<>();
        if (!hallIdsInt.isEmpty()) {
            halls = hallsRepository.findAllById(hallIdsInt);
        }
        response.setHalls(halls.stream()
                .map(this::mapHallToData)
                .collect(Collectors.toList()));

        // 6. Charger toutes les places liées à ces marchés/zones/halls
        Set<Integer> placeIds = new HashSet<>();
        List<Place> places = new ArrayList<>();

        if (!marcheeIds.isEmpty()) {
            places.addAll(placeRepository.findByMarcheeIdIn(new ArrayList<>(marcheeIds)));
        }
        if (!zoneIds.isEmpty()) {
            places.addAll(placeRepository.findByZoneIdIn(new ArrayList<>(zoneIds)));
        }
        if (!hallIds.isEmpty()) {
            places.addAll(placeRepository.findByHallIdIn(new ArrayList<>(hallIds)));
        }

        // Dédupliquer les places
        places = places.stream().distinct().collect(Collectors.toList());
        placeIds = places.stream().map(Place::getId).collect(Collectors.toSet());

        response.setPlaces(places.stream()
                .map(this::mapPlaceToData)
                .collect(Collectors.toList()));

        log.info("📍 Places trouvées: {}", places.size());

        // 7. Charger tous les marchands liés à ces places
        Set<Integer> marchandIds = places.stream()
                .map(Place::getMarchands)
                .filter(m -> m != null)
                .map(Marchands::getId)
                .collect(Collectors.toSet());

        List<Marchands> marchands = new ArrayList<>();
        if (!marchandIds.isEmpty()) {
            marchands = marchandRepository.findAllById(marchandIds);
        }
        response.setMarchands(marchands.stream()
                .map(this::mapMarchandToData)
                .collect(Collectors.toList()));

        log.info("👥 Marchands trouvés: {}", marchands.size());

        // 8. Charger tous les paiements liés à ces places et marchands
        List<Paiement> paiements = new ArrayList<>();
        if (!placeIds.isEmpty()) {
            paiements.addAll(paiementRepository.findByPlaceIdIn(new ArrayList<>(placeIds)));
        }
        if (!marchandIds.isEmpty()) {
            paiements.addAll(paiementRepository.findByMarchandIdIn(new ArrayList<>(marchandIds)));
        }

        // Dédupliquer les paiements
        paiements = paiements.stream().distinct().collect(Collectors.toList());

        response.setPaiements(paiements.stream()
                .map(this::mapPaiementToData)
                .collect(Collectors.toList()));

        log.info("💰 Paiements trouvés: {}", paiements.size());
        log.info("✅ Synchronisation terminée pour l'utilisateur ID: {}", userId);

        return response;
    }

    // Méthodes de mapping
    private SyncDataResponse.UserSyncData mapUserToSyncData(User user) {
        return new SyncDataResponse.UserSyncData(
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getRole().toString(),
                user.getTelephone()
        );
    }

    private SyncDataResponse.MarcheeData mapMarcheeToData(Marchee marchee) {
        return new SyncDataResponse.MarcheeData(
                marchee.getId(),
                marchee.getNom(),
                marchee.getDescription(),
                marchee.getAdresse()

        );
    }

    private SyncDataResponse.ZoneData mapZoneToData(Zone zone) {
        return new SyncDataResponse.ZoneData(
                zone.getId(),
                zone.getNom(),
                zone.getDescription(),
                zone.getMarchee() != null ? zone.getMarchee().getId() : null,
                zone.getMarchee() != null ? zone.getMarchee().getNom() : null
        );
    }

    private SyncDataResponse.HallData mapHallToData(Halls hall) {
        return new SyncDataResponse.HallData(
                hall.getId(),
                hall.getNom(),
                hall.getNumero(),
                hall.getDescription(),
                hall.getCodeUnique(),
                hall.getNbrPlace(),
                hall.getMarchee() != null ? hall.getMarchee().getId() : null,
                hall.getZone() != null ? hall.getZone().getId() : null
        );
    }

    private SyncDataResponse.PlaceData mapPlaceToData(Place place) {
        return new SyncDataResponse.PlaceData(
                place.getId(),
                place.getNom(),
                place.getIsOccuped() != null ? place.getIsOccuped().toString() : null,
                place.getDroitAnnuel().getMontant(),
                place.getCategorie().getMontant(),
                place.getHall() != null ? place.getHall().getId() : null,
                place.getZone() != null ? place.getZone().getId() : null,
                place.getMarchee() != null ? place.getMarchee().getId() : null,
                place.getMarchands() != null ? place.getMarchands().getId() : null
        );
    }

    private SyncDataResponse.MarchandData mapMarchandToData(Marchands marchand) {
        return new SyncDataResponse.MarchandData(
                marchand.getId(),
                marchand.getNom(),
                marchand.getPrenom(),
                marchand.getNumTel1(),
                marchand.getNumCIN(),
                marchand.getNIF(),
                marchand.getSTAT(),
                marchand.getActivite(),
                marchand.getDateEnregistrement()
        );
    }

    private SyncDataResponse.PaiementData mapPaiementToData(Paiement paiement) {
        return new SyncDataResponse.PaiementData(
                paiement.getId(),
                paiement.getQuittance().getNom(),
                paiement.getMontant(),
                paiement.getTypePaiement() != null ? paiement.getTypePaiement().toString() : null,
                paiement.getDatePaiement(),
                paiement.getMarchand() != null ? paiement.getMarchand().getId() : null,
                paiement.getPlace() != null ? paiement.getPlace().getId() : null,
                paiement.getAgent() != null ? paiement.getAgent().getId() : null,
                paiement.getDateDebut() != null ? paiement.getDateDebut().toString() : null,
                paiement.getDateFin() != null ? paiement.getDateFin().toString() : null
        );
    }
}
