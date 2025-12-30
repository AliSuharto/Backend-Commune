package Commune.Dev.Services;

import Commune.Dev.Dtos.MultiplePaiementRequestDTO;
import Commune.Dev.Dtos.PaiementDTO;
import Commune.Dev.Dtos.PaiementRequestDTO;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final MarchandsRepository marchandsRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final SessionRepository sessionRepository;
    private final ContratRepository contratRepository;
    private final QuittanceRepository quittanceRepository;


    /**
     * Effectuer plusieurs paiements en même temps
     */

    @Transactional
    public List<PaiementDTO> effectuerMultiplePaiements(MultiplePaiementRequestDTO request) {
        return request.getPaiements().stream().map(this::effectuerPaiement).collect(Collectors.toList());
    }

    /**
     * Récupérer tous les paiements
     */

    public List<PaiementDTO> getAllPaiements() {
        return paiementRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Récupérer un paiement par ID
     */

    public PaiementDTO getPaiementById(Integer id) {
        Paiement paiement = paiementRepository.findById(id).orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
        return convertToDTO(paiement);
    }

    /**
     * Récupérer les paiements par marchand
     */

    public List<PaiementDTO> getPaiementsByMarchand(Integer idMarchand) {
        List<Paiement> paiements = paiementRepository.findByMarchandId(idMarchand);
        return paiements.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements par place
     */

    public List<PaiementDTO> getPaiementsByPlace(Integer idPlace) {
        List<Paiement> paiements = paiementRepository.findByPlaceId(idPlace);
        return paiements.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements par session
     */

    public List<PaiementDTO> getPaiementsBySession(Integer sessionId) {
        List<Paiement> paiements = paiementRepository.findBySessionId(sessionId);
        return paiements.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements par date
     */

    public List<PaiementDTO> getPaiementsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Paiement> paiements = paiementRepository.findByDatePaiementBetween(startOfDay, endOfDay);
        return paiements.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements entre deux dates
     */

    public List<PaiementDTO> getPaiementsByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime startDateTime = dateDebut.atStartOfDay();

        LocalDateTime endDateTime = dateFin.atTime(LocalTime.MAX);

        List<Paiement> paiements = paiementRepository.findByDatePaiementBetween(startDateTime, endDateTime);
        return paiements.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements par agent
     */
    public List<PaiementDTO> getPaiementsByAgent(Integer idAgent) {
        List<Paiement> paiements = paiementRepository.findByAgentId(idAgent);
        return paiements.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements par mode de paiement
     */

    public List<PaiementDTO> getPaiementsByModePaiement(String modePaiement) {
        Paiement.ModePaiement mode = Paiement.ModePaiement.valueOf(modePaiement);
        List<Paiement> paiements = paiementRepository.findByModePaiement(mode);
        return paiements.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements d'un marchand pour un mois spécifique
     */

    public List<PaiementDTO> getPaiementsByMarchandAndMois(Integer idMarchand, String mois) {
        List<Paiement> paiements = paiementRepository.findByMarchandIdAndMoisdePaiement(idMarchand, mois);
        return paiements.stream().map(this::convertToDTO).collect(Collectors.toList());

    }

    /**
     * Supprimer un paiement
     */

    @Transactional
    public void deletePaiement(Integer id) {
        if (!paiementRepository.existsById(id)) {
            throw new RuntimeException("Paiement non trouvé");
        }
        paiementRepository.deleteById(id);
    }


    // =====================================================
//           EFFECTUER UN PAIEMENT
// =====================================================
    @Transactional
    public PaiementDTO effectuerPaiement(PaiementRequestDTO request) {

        // 🔍 1. Vérification session
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));

        // 🔍 2. La session doit être OUVERTE
        if (session.getStatus() != Session.SessionStatus.OUVERTE) {
            throw new IllegalStateException("La session n'est pas ouverte. Paiement refusé.");
        }

        Quittance quittance = quittanceRepository.findByNom(request.getNumeroQuittance())
                .orElseThrow(() -> new RuntimeException("Numéro de quittance introuvable"));

        if (quittance.getEtat() != StatusQuittance.DISPONIBLE) {
            throw new IllegalStateException("Ce numéro de quittance est déjà utilisé");
        }


        // 🔍 3. Vérification agent
        User agent = userRepository.findById(request.getIdAgent())
                .orElseThrow(() -> new RuntimeException("Agent non trouvé"));

        // 🔍 4. Vérifier que l'agent correspond au user_id de la session
        if (!session.getUser().getId().equals(agent.getId())) {
            throw new IllegalStateException(
                    "Cet agent n'est pas autorisé à effectuer des paiements pour cette session."
            );
        }

        Paiement paiement = new Paiement();
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setModePaiement(Paiement.ModePaiement.cash);
        paiement.setSession(session);
        paiement.setAgent(agent);
        paiement.setQuittance(quittance);
        quittance.setDateUtilisation(LocalDateTime.now());
        quittance.setEtat(StatusQuittance.UTILISE);



        // =====================================================
        //    CAS 1 : MARCHAND ENREGISTRÉ
        // =====================================================
        if (request.getIdMarchand() != null) {

            Marchands marchand = marchandsRepository.findById(request.getIdMarchand())
                    .orElseThrow(() -> new RuntimeException("Marchand non trouvé"));

            paiement.setMarchand(marchand);
            paiement.setNomMarchands(marchand.getNom());

            Contrat contrat = contratRepository
                    .findTopByIdMarchandOrderByDateOfStartDesc(request.getIdMarchand())
                    .orElseThrow(() -> new RuntimeException("Contrat non trouvé pour ce marchand"));

            // =====================================================
            //    VÉRIFICATION DU TYPE DE PAIEMENT
            // =====================================================

            if (request.getTypePaiement() == Paiement.Typepaiement.droit_annuel) {
                // ========== PAIEMENT DROIT ANNUEL ==========

                if (contrat.getDroitAnnuel() == null) {
                    throw new RuntimeException("Droit annuel non défini pour ce contrat");
                }

                // Trouver le dernier paiement de droit annuel
                Paiement dernierPaiementAnnuel = paiementRepository
                        .findTopByMarchandIdAndTypePaiementOrderByDatePaiementDesc(
                                marchand.getId(), Paiement.Typepaiement.droit_annuel)
                        .orElse(null);

                int annee = calculerAnneeProchainDroitAnnuel(contrat, dernierPaiementAnnuel);

                paiement.setMontant(contrat.getDroitAnnuel().getMontant());
                paiement.setMoisdePaiement("Année " + annee);
                paiement.setMotif("Droit annuel " + annee);
                paiement.setAnneePaye(Year.of(annee));
                paiement.setTypePaiement(Paiement.Typepaiement.droit_annuel);
                paiement.setPlace(contrat.getPlace());

                // Dates pour droit annuel : du 1er janvier au 31 décembre de l'année
                paiement.setDateDebut(LocalDate.of(annee, 1, 1));
                paiement.setDateFin(LocalDate.of(annee, 12, 31));

            } else if (request.getTypePaiement() == Paiement.Typepaiement.droit_place) {
                // ========== PAIEMENT DROIT DE PLACE ==========

                FrequencePaiement frequence = contrat.getFrequencePaiement();

                Paiement dernierPaiement = paiementRepository
                        .findTopByMarchandIdAndTypePaiementOrderByDatePaiementDesc(
                                marchand.getId(), Paiement.Typepaiement.droit_place)
                        .orElse(null);

                // ---------- CALCUL PROCHAINE PERIODE ----------
                PeriodePaiement prochainePeriode = calculerProchainePeriode(contrat, dernierPaiement);

                // ---------- ON REMPLIT LE PAIEMENT ----------
                paiement.setMontant(prochainePeriode.montant);
                paiement.setMoisdePaiement(prochainePeriode.labelPeriode);
                paiement.setMotif(prochainePeriode.motif);
                paiement.setTypePaiement(Paiement.Typepaiement.droit_place);
                paiement.setPlace(contrat.getPlace());

                // ✅ AJOUT DES DATES DEBUT ET FIN
                paiement.setDateDebut(prochainePeriode.dateDebut);
                paiement.setDateFin(prochainePeriode.dateFin);

            } else {
                throw new RuntimeException("Type de paiement invalide. Utilisez 'droit_annuel' ou 'droit_place'");
            }
        }

        // =====================================================
        //    CAS 2 : MARCHAND AMBULANT
        // =====================================================
        else {
            if (request.getNomMarchands() == null || request.getNomMarchands().isBlank()) {
                throw new RuntimeException("Nom du marchand ambulant obligatoire");
            }

            paiement.setNomMarchands(request.getNomMarchands());
            paiement.setMontant(request.getMontant());

            paiement.setMotif(request.getMotif());
            paiement.setMoisdePaiement(request.getMoisdePaiement());
            paiement.setTypePaiement(request.getTypePaiement());
        }

        // =====================================================
        //       PLACE MANUELLE SI FOURNIE
        // =====================================================
        if (request.getIdPlace() != null) {
            Place place = placeRepository.findById(request.getIdPlace())
                    .orElseThrow(() -> new RuntimeException("Place non trouvée"));
            paiement.setPlace(place);
        }

        // =========================
        //  MISE À JOUR DU TOTAL
        // =========================
        session.addToTotal(paiement.getMontant());
        sessionRepository.save(session);

        Paiement saved = paiementRepository.save(paiement);
        return convertToDTO(saved);
    }


    // =====================================================
//   CALCULER L'ANNÉE DU PROCHAIN DROIT ANNUEL
// =====================================================
    private int calculerAnneeProchainDroitAnnuel(Contrat contrat, Paiement dernierPaiementAnnuel) {
        LocalDate dateDebut = contrat.getDateOfStart();
        int anneeDebut = dateDebut.getYear();

        if (dernierPaiementAnnuel == null) {
            // Premier paiement : année de début du contrat
            return anneeDebut;
        }

        // Extraire l'année du dernier paiement et ajouter 1
        try {
            String motif = dernierPaiementAnnuel.getMotif();
            // Extraire l'année depuis "Droit annuel 2025"
            int derniereAnnee = Integer.parseInt(motif.replaceAll("\\D+", ""));
            return derniereAnnee + 1;
        } catch (Exception e) {
            // Si erreur, calculer depuis la date de début
            return anneeDebut + 1;
        }
    }


    // =====================================================
//      CALCUL PROCHAINE PERIODE PAIEMENT
// =====================================================
    private PeriodePaiement calculerProchainePeriode(Contrat contrat, Paiement dernierPaiement) {

        LocalDate dateDebut;
        FrequencePaiement freq = contrat.getFrequencePaiement();
        BigDecimal montant = contrat.getCategorie().getMontant();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);

        // =====================================================
        //  CALCUL DE LA DATE DE DÉBUT DE LA PROCHAINE PÉRIODE
        // =====================================================
        if (dernierPaiement != null && dernierPaiement.getDateFin() != null) {
            // ✅ Si on a un dernier paiement avec dateFin, on commence le jour suivant
            dateDebut = dernierPaiement.getDateFin().plusDays(1);
        } else {
            // ✅ Sinon, on commence à la date de début du contrat
            dateDebut = contrat.getDateOfStart();
        }

        // =====================================================
        //  CALCUL DE LA DATE DE FIN SELON LA FRÉQUENCE
        // =====================================================
        LocalDate dateFin;
        String labelPeriode;
        int index = 1;

        if (dernierPaiement != null) {
            index = extraireIndexDernierePeriode(dernierPaiement.getMoisdePaiement()) + 1;
        }

        switch (freq) {

            case MENSUEL: {
                dateFin = dateDebut.plusMonths(1).minusDays(1);
                labelPeriode = "Mois " + index;

                String motif = "Paiement du " + index + "ᵉ mois (" +
                        dateDebut.format(formatter) + " - " + dateFin.format(formatter) + ")";

                return new PeriodePaiement(labelPeriode, motif, montant, dateDebut, dateFin);
            }

            case HEBDOMADAIRE: {
                dateFin = dateDebut.plusWeeks(1).minusDays(1);
                labelPeriode = "Semaine " + index;

                String motif = "Paiement de la " + index + "ᵉ semaine (" +
                        dateDebut.format(formatter) + " - " + dateFin.format(formatter) + ")";

                return new PeriodePaiement(labelPeriode, motif, montant, dateDebut, dateFin);
            }

            case JOURNALIER: {
                dateFin = dateDebut;
                labelPeriode = "Jour " + index;

                String motif = "Paiement du jour " + index + " (" + dateDebut.format(formatter) + ")";

                return new PeriodePaiement(labelPeriode, motif, montant, dateDebut, dateFin);
            }

            default:
                throw new RuntimeException("Fréquence non gérée");
        }
    }

    // Extraction du numéro de période depuis "Mois 2", "Semaine 4", etc.
    private int extraireIndexDernierePeriode(String label) {
        try {
            return Integer.parseInt(label.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return 1;
        }
    }

    // DTO interne
    private static class PeriodePaiement {
        String labelPeriode;
        String motif;
        BigDecimal montant;
        LocalDate dateDebut;
        LocalDate dateFin;

        public PeriodePaiement(String labelPeriode, String motif, BigDecimal montant,
                               LocalDate dateDebut, LocalDate dateFin) {
            this.labelPeriode = labelPeriode;
            this.motif = motif;
            this.montant = montant;
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
        }
    }


    // =====================================================
// CONVERSION DTO
// =====================================================
    private PaiementDTO convertToDTO(Paiement paiement) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(paiement.getId());
        dto.setMontant(paiement.getMontant());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setModePaiement(paiement.getModePaiement().name());
        dto.setMoisdePaiement(paiement.getMoisdePaiement());
        dto.setMotif(paiement.getMotif());
        dto.setRecuNumero(paiement.getQuittance().getNom());
        dto.setNomMarchands(paiement.getNomMarchands());
        dto.setTypePaiement(paiement.getTypePaiement());
        dto.setNomAgent(paiement.getAgent().getNom());
        if (paiement.getMarchand() != null) dto.setIdMarchand(paiement.getMarchand().getId());
        if (paiement.getAgent() != null) dto.setIdAgent(Math.toIntExact(paiement.getAgent().getId()));
        if (paiement.getPlace() != null) dto.setIdPlace(paiement.getPlace().getId());
        if (paiement.getSession() != null) dto.setSessionId(Math.toIntExact(paiement.getSession().getId()));

        return dto;
    }

}
