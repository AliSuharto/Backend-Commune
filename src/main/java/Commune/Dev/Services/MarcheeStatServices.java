package Commune.Dev.Services;

import Commune.Dev.Dtos.MarcheStatDTO;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarcheeStatServices {

    private final MarcheeRepository marcheeRepository;
    private final PlaceRepository placeRepository;
    private final ContratRepository contratRepository;
    private final PaiementRepository paiementRepository;

    // Constantes pour la conversion mensuelle
    private static final double SEMAINES_PAR_MOIS = 4.33;
    private static final int JOURS_PAR_MOIS = 30;

    /**
     * Récupère les statistiques pour tous les marchés
     */
    @Transactional(readOnly = true)
    public List<MarcheStatDTO> getAllMarcheesStats() {
        List<Marchee> marchees = marcheeRepository.findAll();
        return marchees.stream()
                .map(this::buildMarcheStatDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les statistiques pour un marché spécifique
     */
    @Transactional(readOnly = true)
    public MarcheStatDTO getMarcheStatById(Long marcheeId) {
        Marchee marchee = marcheeRepository.findById(Math.toIntExact(marcheeId))
                .orElseThrow(() -> new RuntimeException("Marché non trouvé"));
        return buildMarcheStatDTO(marchee);
    }

    /**
     * Construit le DTO avec toutes les statistiques
     */
    private MarcheStatDTO buildMarcheStatDTO(Marchee marchee) {
        MarcheStatDTO dto = new MarcheStatDTO();

        // Informations de base
        dto.setId(marchee.getId());
        dto.setNom(marchee.getNom());
        dto.setAdresse(marchee.getAdresse());

        // Calculer le nombre total de halls (directs + dans les zones)
        int nbrHallsDirects = marchee.getHalls() != null ? marchee.getHalls().size() : 0;
        int nbrHallsDansZones = marchee.getZones() != null ?
                marchee.getZones().stream()
                        .mapToInt(zone -> zone.getHalls() != null ? zone.getHalls().size() : 0)
                        .sum() : 0;
        int nbrHallsTotal = nbrHallsDirects + nbrHallsDansZones;
        dto.setNbrHall(nbrHallsTotal);

        dto.setNbrZone(marchee.getZones() != null ? marchee.getZones().size() : 0);

        // Récupérer TOUTES les places du marché selon la hiérarchie complète
        List<Place> places = placeRepository.findAllByMarcheeIdRecursive(marchee.getId());

        // Calculer le nombre total de places dynamiquement
        int nbrPlaceTotal = places.size();
        dto.setNbrPlace(nbrPlaceTotal);

        // Statistiques des places
        long placesOccupees = places.stream().filter(p -> Boolean.TRUE.equals(p.getIsOccuped())).count();
        dto.setNbrPlaceOccupee((int) placesOccupees);
        dto.setNbrPlaceLibre(nbrPlaceTotal - (int) placesOccupees);

        // Taux d'occupation
        if (nbrPlaceTotal > 0) {
            dto.setTauxOccupation((int) Math.round((placesOccupees * 100.0) / nbrPlaceTotal));
        } else {
            dto.setTauxOccupation(0);
        }

        // Récupérer les contrats actifs pour toutes les places du marché
        List<Integer> placeIds = places.stream()
                .map(Place::getId)
                .collect(Collectors.toList());

        List<Contrat> contratsActifs = placeIds.isEmpty() ?
                new ArrayList<>() :
                contratRepository.findByIdPlaceInAndIsActif(placeIds, true);
        dto.setNbrMarchands(contratsActifs.size());

        // Calculer la répartition par catégorie et fréquence
        List<MarcheStatDTO.CategorieFrequenceDTO> repartition = calculerRepartitionCategorieFrequence(contratsActifs, marchee.getId());
        dto.setRepartitionPaiements(repartition);

        // Calculer les montants globaux
        BigDecimal montantEstime = repartition.stream()
                .map(MarcheStatDTO.CategorieFrequenceDTO::getMontantEstimeTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setMontantEstimeParMois(montantEstime);

        BigDecimal montantPercu = repartition.stream()
                .map(MarcheStatDTO.CategorieFrequenceDTO::getMontantPercu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setMontantPercuMoisDernier(montantPercu);

        dto.setMontantRestant(montantEstime.subtract(montantPercu));

        // Taux de perception global
        if (montantEstime.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tauxPerception = montantPercu
                    .multiply(BigDecimal.valueOf(100))
                    .divide(montantEstime, 0, RoundingMode.HALF_UP);
            dto.setTauxPerception(tauxPerception.intValue());
        } else {
            dto.setTauxPerception(0);
        }

        // Statistiques des halls
        dto.setHalls(calculerStatsHalls(marchee));

        // Statistiques des zones
        dto.setZones(calculerStatsZones(marchee));

        return dto;
    }

    /**
     * Calcule la répartition par catégorie et fréquence de paiement
     */
    private List<MarcheStatDTO.CategorieFrequenceDTO> calculerRepartitionCategorieFrequence(
            List<Contrat> contratsActifs, Long marcheeId) {

        // Grouper par catégorie et fréquence
        Map<String, List<Contrat>> groupes = contratsActifs.stream()
                .filter(c -> c.getCategorie() != null && c.getFrequencePaiement() != null)
                .collect(Collectors.groupingBy(c ->
                        c.getCategorie().getNom() + "|" + c.getFrequencePaiement().toString()
                ));

        List<MarcheStatDTO.CategorieFrequenceDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<Contrat>> entry : groupes.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String categorieNom = parts[0];
            String frequence = parts[1];
            List<Contrat> contrats = entry.getValue();

            MarcheStatDTO.CategorieFrequenceDTO dto = new MarcheStatDTO.CategorieFrequenceDTO();

            // Informations de base
            dto.setCategorie(categorieNom);
            dto.setFrequence(frequence);
            dto.setNbrMarchands(contrats.size());

            // Tarif de base (prendre le tarif de la catégorie du premier contrat)
            Categorie categorie = contrats.get(0).getCategorie();
            BigDecimal tarifBase = categorie.getMontant();
            dto.setTarifBase(tarifBase);

            // Facteur de conversion selon la fréquence
            double facteur = getFacteurConversion(frequence);
            dto.setFacteurConversion(facteur);

            // Montant mensuel unitaire
            BigDecimal montantMensuelUnitaire = tarifBase.multiply(BigDecimal.valueOf(facteur))
                    .setScale(0, RoundingMode.HALF_UP);
            dto.setMontantMensuelUnitaire(montantMensuelUnitaire);

            // Estimation totale
            BigDecimal montantEstime = montantMensuelUnitaire
                    .multiply(BigDecimal.valueOf(contrats.size()));
            dto.setMontantEstimeTotal(montantEstime);

            // Montant perçu le mois dernier
            BigDecimal montantPercu = calculerMontantPercuMoisDernier(contrats, marcheeId);
            dto.setMontantPercu(montantPercu);

            // Taux de perception
            if (montantEstime.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal tauxPerception = montantPercu
                        .multiply(BigDecimal.valueOf(100))
                        .divide(montantEstime, 0, RoundingMode.HALF_UP);
                dto.setTauxPerception(tauxPerception.intValue());
            } else {
                dto.setTauxPerception(0);
            }

            result.add(dto);
        }

        // Trier par catégorie puis fréquence
        result.sort(Comparator.comparing(MarcheStatDTO.CategorieFrequenceDTO::getCategorie)
                .thenComparing(MarcheStatDTO.CategorieFrequenceDTO::getFrequence));

        return result;
    }

    /**
     * Retourne le facteur de conversion pour l'estimation mensuelle
     */
    private double getFacteurConversion(String frequence) {
        switch (frequence) {
            case "MENSUEL":
                return 1.0;
            case "HEBDOMADAIRE":
                return SEMAINES_PAR_MOIS;
            case "JOURNALIER":
                return JOURS_PAR_MOIS;
            default:
                return 1.0;
        }
    }

    /**
     * Calcule le montant perçu le mois dernier pour une liste de contrats
     */
    private BigDecimal calculerMontantPercuMoisDernier(List<Contrat> contrats, Long marcheeId) {
        // Mois dernier
        YearMonth moisDernier = YearMonth.now().minusMonths(1);
        LocalDateTime debutMois = moisDernier.atDay(1).atStartOfDay();
        LocalDateTime finMois = moisDernier.atEndOfMonth().atTime(23, 59, 59);

        // Récupérer les IDs des marchands
        List<Integer> marchandIds = contrats.stream()
                .map(Contrat::getIdMarchand)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (marchandIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Récupérer les paiements du mois dernier
        List<Paiement> paiements = paiementRepository.findByMarchandIdInAndDatePaiementBetween(
                marchandIds, debutMois, finMois);

        // Sommer les montants
        return paiements.stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcule les statistiques pour chaque hall
     */
    private List<MarcheStatDTO.HallDTO> calculerStatsHalls(Marchee marchee) {
        if (marchee.getHalls() == null || marchee.getHalls().isEmpty()) {
            return new ArrayList<>();
        }

        return marchee.getHalls().stream()
                .map(hall -> {
                    MarcheStatDTO.HallDTO dto = new MarcheStatDTO.HallDTO();
                    dto.setId(hall.getId());
                    dto.setNom(hall.getNom());

                    List<Place> placesHall = placeRepository.findByHallId(hall.getId());
                    dto.setNbrPlace(placesHall.size());

                    long placesOccupees = placesHall.stream()
                            .filter(p -> Boolean.TRUE.equals(p.getIsOccuped()))
                            .count();
                    dto.setNbrPlaceOccupee((int) placesOccupees);
                    dto.setNbrPlaceLibre(placesHall.size() - (int) placesOccupees);

                    // Taux d'occupation
                    if (placesHall.size() > 0) {
                        dto.setTauxOccupation((int) Math.round((placesOccupees * 100.0) / placesHall.size()));
                    } else {
                        dto.setTauxOccupation(0);
                    }

                    // Contrats actifs dans ce hall
                    List<Integer> placeIds = placesHall.stream()
                            .map(Place::getId)
                            .collect(Collectors.toList());

                    List<Contrat> contratsHall = contratRepository.findByIdPlaceInAndIsActif(placeIds, true);
                    dto.setNbrMarchands(contratsHall.size());

                    // Estimation et montants
                    BigDecimal montantEstime = calculerEstimationMensuelle(contratsHall);
                    dto.setMontantEstime(montantEstime);

                    BigDecimal montantPercu = calculerMontantPercuMoisDernier(contratsHall, marchee.getId());
                    dto.setMontantPercu(montantPercu);
                    dto.setMontantRestant(montantEstime.subtract(montantPercu));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcule les statistiques pour chaque zone
     */
    private List<MarcheStatDTO.ZoneDTO> calculerStatsZones(Marchee marchee) {
        if (marchee.getZones() == null || marchee.getZones().isEmpty()) {
            return new ArrayList<>();
        }

        return marchee.getZones().stream()
                .map(zone -> {
                    MarcheStatDTO.ZoneDTO dto = new MarcheStatDTO.ZoneDTO();
                    dto.setId(zone.getId());
                    dto.setNom(zone.getNom());
                    dto.setNbrHalls(zone.getHalls() != null ? zone.getHalls().size() : 0);

                    // Récupérer TOUTES les places de la zone (directes + dans halls)
                    List<Place> placesZone = placeRepository.findAllByZoneIdRecursive(zone.getId());
                    dto.setNbrPlace(placesZone.size());

                    long placesOccupees = placesZone.stream()
                            .filter(p -> Boolean.TRUE.equals(p.getIsOccuped()))
                            .count();
                    dto.setNbrPlaceOccupee((int) placesOccupees);
                    dto.setNbrPlaceLibre(placesZone.size() - (int) placesOccupees);

                    // Taux d'occupation
                    if (placesZone.size() > 0) {
                        dto.setTauxOccupation((int) Math.round((placesOccupees * 100.0) / placesZone.size()));
                    } else {
                        dto.setTauxOccupation(0);
                    }

                    // Contrats actifs dans cette zone
                    List<Integer> placeIds = placesZone.stream()
                            .map(Place::getId)
                            .collect(Collectors.toList());

                    List<Contrat> contratsZone = placeIds.isEmpty() ?
                            new ArrayList<>() :
                            contratRepository.findByIdPlaceInAndIsActif(placeIds, true);
                    dto.setNbrMarchands(contratsZone.size());

                    // Estimation et montants
                    BigDecimal montantEstime = calculerEstimationMensuelle(contratsZone);
                    dto.setMontantEstimeMois(montantEstime);

                    BigDecimal montantPercu = calculerMontantPercuMoisDernier(contratsZone, marchee.getId());
                    dto.setMontantPercu(montantPercu);
                    dto.setMontantRestant(montantEstime.subtract(montantPercu));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcule l'estimation mensuelle pour une liste de contrats
     */
    private BigDecimal calculerEstimationMensuelle(List<Contrat> contrats) {
        return contrats.stream()
                .filter(c -> c.getCategorie() != null && c.getFrequencePaiement() != null)
                .map(contrat -> {
                    BigDecimal tarifBase = contrat.getCategorie().getMontant();
                    double facteur = getFacteurConversion(contrat.getFrequencePaiement().toString());
                    return tarifBase.multiply(BigDecimal.valueOf(facteur))
                            .setScale(0, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}