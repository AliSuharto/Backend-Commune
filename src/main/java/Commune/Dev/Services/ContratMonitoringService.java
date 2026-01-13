package Commune.Dev.Services;

import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;

@Service
public class ContratMonitoringService {

    private final ContratRepository contratRepository;
    private final PaiementRepository paiementRepository;
    private final MarchandsRepository marchandRepository;

    public ContratMonitoringService(ContratRepository contratRepository,
                                    PaiementRepository paiementRepository,
                                    MarchandsRepository marchandRepository) {
        this.contratRepository = contratRepository;
        this.paiementRepository = paiementRepository;
        this.marchandRepository = marchandRepository;
    }

    /**
     * Analyse l'ensemble des contrats actifs et met à jour le statut paiement des marchands.
     */
    @Transactional
    public void analyserContrats() {
        List<Contrat> contrats = contratRepository.findAllActifs();

        for (Contrat contrat : contrats) {
            if (contrat.getMarchand() == null) continue;

            // Analyser TOUTES les périodes échues depuis le début du contrat
            ResultatAnalyse resultat = analyserHistoriquePaiements(contrat);

            // Mise à jour du marchand
            Marchands marchand = contrat.getMarchand();
            marchand.setStatut(resultat.statut);
            marchand.setEstEndette(resultat.estEndette);
            marchandRepository.save(marchand);
        }
    }

    /**
     * Analyse l'historique complet des paiements pour un contrat.
     * Vérifie toutes les périodes échues depuis le début du contrat.
     * NOTE: On vérifie uniquement les paiements de type DROIT_PLACE (paiements récurrents).
     */
    private ResultatAnalyse analyserHistoriquePaiements(Contrat contrat) {
        LocalDate startDate = contrat.getDateOfStart();
        LocalDate today = LocalDate.now();

        if (startDate == null || startDate.isAfter(today)) {
            // Contrat pas encore commencé ou date invalide
            return new ResultatAnalyse(StatutMarchands.A_JOUR, false);
        }

        // Générer toutes les périodes échues
        List<Periode> periodesEchues = genererPeriodesEchues(startDate, today, contrat.getFrequencePaiement());

        if (periodesEchues.isEmpty()) {
            // Aucune période échue = marchand à jour
            return new ResultatAnalyse(StatutMarchands.A_JOUR, false);
        }

        // Vérifier chaque période échue
        Periode premiereNonPayee = null;
        int nombrePeriodesNonPayees = 0;

        for (Periode periode : periodesEchues) {
            // IMPORTANT: Vérifier uniquement les paiements de type DROIT_PLACE
            boolean paiementEffectue = paiementRepository.existsByMarchandIdAndTypePaiementAndDatePaiementBetween(
                    Long.valueOf(contrat.getMarchand().getId()),
                    Paiement.Typepaiement.droit_place,
                    periode.start,
                    periode.end
            );

            if (!paiementEffectue) {
                nombrePeriodesNonPayees++;
                if (premiereNonPayee == null) {
                    premiereNonPayee = periode;
                }
            }
        }

        // Si toutes les périodes sont payées
        if (nombrePeriodesNonPayees == 0) {
            return new ResultatAnalyse(StatutMarchands.A_JOUR, false);
        }

        // Il y a des périodes non payées = marchand endetté
        StatutMarchands statut = determinerStatutSelonRetard(
                premiereNonPayee.end.toLocalDate(),
                nombrePeriodesNonPayees,
                contrat.getFrequencePaiement()
        );

        return new ResultatAnalyse(statut, true);
    }

    /**
     * Génère toutes les périodes ÉCHUES (terminées) depuis le début du contrat jusqu'à aujourd'hui.
     */
    private List<Periode> genererPeriodesEchues(LocalDate startDate, LocalDate today, FrequencePaiement frequence) {
        List<Periode> periodes = new ArrayList<>();
        LocalDate currentPeriodStart = startDate;

        while (true) {
            LocalDate currentPeriodEnd = calculerFinPeriode(currentPeriodStart, frequence);

            // Si la fin de période est dans le futur, on arrête
            if (currentPeriodEnd.isAfter(today)) {
                break;
            }

            // Ajouter cette période échue
            periodes.add(new Periode(
                    currentPeriodStart.atStartOfDay(),
                    currentPeriodEnd.atTime(LocalTime.MAX)
            ));

            // Passer à la période suivante
            currentPeriodStart = calculerDebutPeriodeSuivante(currentPeriodStart, frequence);

            // Sécurité : éviter boucle infinie
            if (currentPeriodStart.isAfter(today)) {
                break;
            }
        }

        return periodes;
    }

    /**
     * Calcule la date de fin d'une période donnée selon la fréquence.
     */
    private LocalDate calculerFinPeriode(LocalDate debut, FrequencePaiement frequence) {
        switch (frequence) {
            case JOURNALIER:
                return debut; // Même jour

            case HEBDOMADAIRE:
                return debut.plusDays(6); // 7 jours (0-6)

            case MENSUEL:
                // Fin du mois de la date de début
                return debut.plusMonths(1).minusDays(1);

            default:
                return debut;
        }
    }

    /**
     * Calcule le début de la période suivante.
     */
    private LocalDate calculerDebutPeriodeSuivante(LocalDate debutActuel, FrequencePaiement frequence) {
        switch (frequence) {
            case JOURNALIER:
                return debutActuel.plusDays(1);

            case HEBDOMADAIRE:
                return debutActuel.plusWeeks(1);

            case MENSUEL:
                return debutActuel.plusMonths(1);

            default:
                return debutActuel.plusDays(1);
        }
    }

    /**
     * Détermine le statut en fonction du retard et du nombre de périodes manquées.
     */
    private StatutMarchands determinerStatutSelonRetard(
            LocalDate finPremierePeriodeNonPayee,
            int nombrePeriodesNonPayees,
            FrequencePaiement frequence) {

        long joursRetard = ChronoUnit.DAYS.between(finPremierePeriodeNonPayee, LocalDate.now());

        // Logique basée sur le nombre de périodes manquées ET les jours de retard
        switch (frequence) {
            case MENSUEL:
                if (nombrePeriodesNonPayees == 1 && joursRetard <= 30) {
                    return StatutMarchands.RETARD_LEGER;
                } else if (nombrePeriodesNonPayees <= 2 || joursRetard <= 60) {
                    return StatutMarchands.RETARD_SIGNIFICATIF;
                } else if (nombrePeriodesNonPayees <= 3 || joursRetard <= 90) {
                    return StatutMarchands.RETARD_CRITIQUE;
                } else {
                    return StatutMarchands.RETARD_PROLONGER;
                }

            case HEBDOMADAIRE:
                if (nombrePeriodesNonPayees == 1 && joursRetard <= 7) {
                    return StatutMarchands.RETARD_LEGER;
                } else if (nombrePeriodesNonPayees <= 2 || joursRetard <= 14) {
                    return StatutMarchands.RETARD_SIGNIFICATIF;
                } else if (nombrePeriodesNonPayees <= 3 || joursRetard <= 21) {
                    return StatutMarchands.RETARD_CRITIQUE;
                } else {
                    return StatutMarchands.RETARD_PROLONGER;
                }

            case JOURNALIER:
                if (nombrePeriodesNonPayees == 1 || joursRetard <= 1) {
                    return StatutMarchands.RETARD_LEGER;
                } else if (nombrePeriodesNonPayees <= 3 || joursRetard <= 3) {
                    return StatutMarchands.RETARD_SIGNIFICATIF;
                } else if (nombrePeriodesNonPayees <= 7 || joursRetard <= 7) {
                    return StatutMarchands.RETARD_CRITIQUE;
                } else {
                    return StatutMarchands.RETARD_PROLONGER;
                }

            default:
                if (joursRetard <= 5) return StatutMarchands.RETARD_LEGER;
                if (joursRetard <= 15) return StatutMarchands.RETARD_SIGNIFICATIF;
                if (joursRetard <= 30) return StatutMarchands.RETARD_CRITIQUE;
                return StatutMarchands.RETARD_PROLONGER;
        }
    }

    /**
     * Classe interne pour représenter une période.
     */
    private static class Periode {
        private final LocalDateTime start;
        private final LocalDateTime end;

        Periode(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Classe interne pour le résultat de l'analyse.
     */
    private static class ResultatAnalyse {
        private final StatutMarchands statut;
        private final boolean estEndette;

        ResultatAnalyse(StatutMarchands statut, boolean estEndette) {
            this.statut = statut;
            this.estEndette = estEndette;
        }
    }
}