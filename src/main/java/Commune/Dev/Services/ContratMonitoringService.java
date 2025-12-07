package Commune.Dev.Services;

import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
     * Analyse l'ensemble des contrats actifs et met a jour le statut paiement des marchands.
     */
    @Transactional
    public void analyserContrats() {
        List<Contrat> contrats = contratRepository.findAllActifs();

        for (Contrat contrat : contrats) {
            if (contrat.getMarchand() == null) continue; // securite si relation lazy non chargee

            // Calcule la fenetre temporelle attendue pour le paiement courant
            Periodne periode = calculerFenetreCourante(contrat);

            StatutMarchands statut;
            boolean estEndette;

            // 1. NOUVEAU CHECK : Si la periode de paiement n'est pas encore terminee
            if (periode.end.toLocalDate().isAfter(LocalDate.now())) {

                // La date de fin est dans le futur (la période est toujours ouverte)
                statut = StatutMarchands.A_JOUR;
                estEndette = false;

            } else {
                // La periode de paiement est terminee (aujourd'hui ou dans le passé)

                // Verification existence paiement
                boolean paiementOk = paiementRepository.existsByMarchandIdAndDatePaiementBetween(
                        contrat.getMarchand().getId(),
                        periode.start,
                        periode.end
                );

                if (paiementOk) {
                    statut = StatutMarchands.A_JOUR;
                    estEndette = false;
                } else {
                    // Determine le statut en fonction du retard et de la frequence
                    statut = determinerStatutEnFonctionDuRetard(
                            periode.end.toLocalDate(),
                            contrat.getFrequencePaiement()
                    );
                    // Si aucun paiement trouvé pour une période échue, le marchand est endetté
                    estEndette = true;
                }
            }

            // Mise a jour du marchand
            Marchands marchand = contrat.getMarchand();
            marchand.setStatut(statut);
            // Assurez-vous que la classe Marchands a bien un setter pour le champ transient
            marchand.setEstEndette(estEndette);
            marchandRepository.save(marchand);
        }
    }

    /**
     * Calcule la fenetre de paiement (start, end) en LocalDateTime pour la periode courante
     * en fonction de la frequence et de la date de debut du contrat.
     */
    private Periodne calculerFenetreCourante(Contrat contrat) {
        LocalDate startDate = contrat.getDateOfStart();
        LocalDate today = LocalDate.now();

        if (startDate == null) {
            // si pas de date, on considere la periode comme aujourd'hui (securite)
            LocalDateTime s = today.atStartOfDay();
            LocalDateTime e = today.atTime(LocalTime.MAX);
            return new Periodne(s, e);
        }

        switch (contrat.getFrequencePaiement()) {
            case JOURNALIER:
                // periode courante = aujourd'hui
                LocalDateTime sJ = today.atStartOfDay();
                LocalDateTime eJ = today.atTime(LocalTime.MAX);
                return new Periodne(sJ, eJ);

            case HEBDOMADAIRE:
                // calculer le nombre de semaines completes depuis startDate jusqu'a today
                long weeks = ChronoUnit.WEEKS.between(startDate, today);
                LocalDate periodStartWeek = startDate.plusWeeks(weeks);
                LocalDate periodEndWeek = periodStartWeek.plusDays(6); // 7 jours - 1
                return new Periodne(periodStartWeek.atStartOfDay(), periodEndWeek.atTime(LocalTime.MAX));

            case MENSUEL:
                // calculer le nombre de mois complets depuis startDate jusqu'a today
                long months = ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), today.withDayOfMonth(1));
                LocalDate periodStartMonth = startDate.plusMonths(months);
                LocalDate periodEndMonth = periodStartMonth.plusMonths(1).minusDays(1);
                return new Periodne(periodStartMonth.atStartOfDay(), periodEndMonth.atTime(LocalTime.MAX));

            default:
                // fallback = journee courante
                LocalDateTime sDef = today.atStartOfDay();
                LocalDateTime eDef = today.atTime(LocalTime.MAX);
                return new Periodne(sDef, eDef);
        }
    }

    /**
     * Determine le statut selon le nombre de jours de retard (depuis la fin de la periode)
     * en utilisant une logique differente selon la frequence de paiement.
     */
    private StatutMarchands determinerStatutEnFonctionDuRetard(LocalDate finPeriode, FrequencePaiement frequence) {
        long joursRetard = ChronoUnit.DAYS.between(finPeriode, LocalDate.now());

        if (joursRetard <= 1) return StatutMarchands.A_JOUR;

        switch (frequence) {
            case MENSUEL:
                // 1 mois ≈ 30 jours
                if (joursRetard <= 30) return StatutMarchands.RETARD_LEGER;
                // 2 mois ≈ 60 jours
                if (joursRetard <= 60) return StatutMarchands.RETARD_SIGNIFICATIF;
                // 3 mois ≈ 90 jours
                if (joursRetard <= 90) return StatutMarchands.RETARD_CRITIQUE;
                return StatutMarchands.RETARD_PROLONGER;

            case HEBDOMADAIRE:
                // Basé sur des semaines
                if (joursRetard <= 7) return StatutMarchands.RETARD_LEGER;
                if (joursRetard <= 14) return StatutMarchands.RETARD_SIGNIFICATIF;
                if (joursRetard <= 21) return StatutMarchands.RETARD_CRITIQUE;
                return StatutMarchands.RETARD_PROLONGER;

            case JOURNALIER:
                // Basé sur des jours très courts
                if (joursRetard <= 1) return StatutMarchands.RETARD_LEGER;
                if (joursRetard <= 3) return StatutMarchands.RETARD_SIGNIFICATIF;
                if (joursRetard <= 7) return StatutMarchands.RETARD_CRITIQUE;
                return StatutMarchands.RETARD_PROLONGER;

            default:
                // Logique par defaut si la frequence n'est pas geree
                if (joursRetard <= 5) return StatutMarchands.RETARD_LEGER;
                if (joursRetard <= 15) return StatutMarchands.RETARD_SIGNIFICATIF;
                if (joursRetard <= 30) return StatutMarchands.RETARD_CRITIQUE;
                return StatutMarchands.RETARD_PROLONGER;
        }
    }

    /**
     * Simple DTO interne pour representer une fenetre start-end en LocalDateTime.
     */
    private static class Periodne {
        private final LocalDateTime start;
        private final LocalDateTime end;

        Periodne(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }
}