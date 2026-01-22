package Commune.Dev.Services;

import Commune.Dev.Dtos.RegisseurDashboardDTO;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegisseurDashboardService {

    private final PlaceRepository placeRepository;
    private final MarchandsRepository marchandsRepository;
    private final SessionRepository sessionRepository;
    private final QuittanceRepository quittanceRepository;
    private final PaiementRepository paiementRepository;

    @Transactional(readOnly = true)
    public RegisseurDashboardDTO getDashboardData(Integer userId) {
        RegisseurDashboardDTO dashboard = new RegisseurDashboardDTO();

        // Nombre total de places (tous types confondus)
        dashboard.setNbrPlaces(placeRepository.countAllPlaces());

        // Nombre de marchands
        dashboard.setNbrMarchands((int) marchandsRepository.count());

        // Statistiques des marchands
        calculerStatistiquesMarchands(dashboard);

        // Sessions de l'utilisateur
        List<Session> sessions = sessionRepository.findByUserId(userId);
        dashboard.setNbrSession(sessions.size());

        // Nombre de sessions validées
        dashboard.setNbrSessionValide((int) sessions.stream()
                .filter(s -> s.getStatus() == Session.SessionStatus.VALIDEE)
                .count());

        // Nombre de sessions en validation
        dashboard.setNbrSessionEnvalidation((int) sessions.stream()
                .filter(s -> s.getStatus() == Session.SessionStatus.EN_VALIDATION)
                .count());

        // Nom de la session ouverte
        sessions.stream()
                .filter(s -> s.getStatus() == Session.SessionStatus.OUVERTE)
                .findFirst()
                .ifPresent(s -> dashboard.setNomSessionOuvert(s.getNomSession()));

        // Mapper les sessions
        dashboard.setSessions(sessions.stream()
                .map(this::mapToSessionRegisseur)
                .collect(Collectors.toList()));

        // Quittances de l'utilisateur
        List<Quittance> quittances = quittanceRepository.findByPercepteurId(Long.valueOf(userId));
        dashboard.setNbrQuittancesUtilise((int) quittances.stream()
                .filter(q -> q.getEtat() == StatusQuittance.UTILISE)
                .count());

        dashboard.setNbrQuittancesLibre((int) quittances.stream()
                .filter(q -> q.getEtat() == StatusQuittance.DISPONIBLE)
                .count());

        // Mapper les quittances
        dashboard.setQuittances(quittances.stream()
                .map(this::mapToQuittanceRegisseur)
                .collect(Collectors.toList()));

        return dashboard;
    }

    private void calculerStatistiquesMarchands(RegisseurDashboardDTO dashboard) {
        List<Marchands> allMarchands = marchandsRepository.findAll();

        int endettes = 0;
        int ajour = 0;
        int retardLeger = 0;
        int retardSignificatif = 0;
        int retardCritique = 0;
        int retardProlonge = 0;

        for (Marchands m : allMarchands) {
            if (Boolean.TRUE.equals(m.getEstEndette())) {
                endettes++;
            } else {
                ajour++;
            }

            // Compter selon le statut du marchand
            if (m.getStatut() != null) {
                switch (m.getStatut()) {
                    case RETARD_LEGER:
                        retardLeger++;
                        break;
                    case RETARD_SIGNIFICATIF:
                        retardSignificatif++;
                        break;
                    case RETARD_CRITIQUE:
                        retardCritique++;
                        break;
                    case RETARD_PROLONGER:
                        retardProlonge++;
                        break;
                    default:
                        break;
                }
            }
        }

        dashboard.setNbrMarchandsEndette(endettes);
        dashboard.setNbrMarchandsAjour(ajour);
        dashboard.setNbrMarchandsRetardLeger(retardLeger);
        dashboard.setNbrMarchandsRetardSignificatif(retardSignificatif);
        dashboard.setNbrMarchandsRetardCritique(retardCritique);
        dashboard.setNbrMarchandsRetardProlonger(retardProlonge);
    }

    private RegisseurDashboardDTO.SessionRegisseur mapToSessionRegisseur(Session session) {
        RegisseurDashboardDTO.SessionRegisseur sessionDTO = new RegisseurDashboardDTO.SessionRegisseur();

        sessionDTO.setId(session.getId().intValue());
        sessionDTO.setNomSession(session.getNomSession());
        sessionDTO.setMontant(session.getTotalCollected() != null ? session.getTotalCollected() : BigDecimal.ZERO);
        sessionDTO.setStatut(session.getStatus() != null ? session.getStatus().name() : null);
        sessionDTO.setDateValidation(session.getValidation_date());
        sessionDTO.setDateOuverture(session.getStartTime());
        sessionDTO.setDateFermeture(session.getEndTime());

        // Mapper les paiements de la session
        if (session.getPaiements() != null) {
            sessionDTO.setPaiements(session.getPaiements().stream()
                    .map(this::mapToPaiementRegisseur)
                    .collect(Collectors.toList()));
        }

        return sessionDTO;
    }

    private RegisseurDashboardDTO.SessionRegisseur.PaiementRegisseur mapToPaiementRegisseur(Paiement paiement) {
        RegisseurDashboardDTO.SessionRegisseur.PaiementRegisseur paiementDTO =
                new RegisseurDashboardDTO.SessionRegisseur.PaiementRegisseur();

        paiementDTO.setId(paiement.getId());
        paiementDTO.setMotif(paiement.getMotif());
        paiementDTO.setNomAgent(paiement.getAgent() != null ? paiement.getAgent().getUsername() : null);
        paiementDTO.setMontant(paiement.getMontant() != null ? paiement.getMontant().toString() : "0");
        paiementDTO.setDatePaiement(paiement.getDatePaiement());
        paiementDTO.setNomMarchands(paiement.getNomMarchands() != null ?
                paiement.getNomMarchands() :
                (paiement.getMarchand() != null ? paiement.getMarchand().getNom() : null));
        paiementDTO.setIdSession(paiement.getSession() != null ? paiement.getSession().getId().intValue() : null);

        return paiementDTO;
    }

    private RegisseurDashboardDTO.QuittanceRegisseur mapToQuittanceRegisseur(Quittance quittance) {
        RegisseurDashboardDTO.QuittanceRegisseur quittanceDTO = new RegisseurDashboardDTO.QuittanceRegisseur();

        quittanceDTO.setId(quittance.getId().intValue());
        quittanceDTO.setNom(quittance.getNom());
        quittanceDTO.setStatut(quittance.getEtat() != null ? quittance.getEtat().name() : null);

        // Montant du paiement associé
        if (quittance.getPaiement() != null && quittance.getPaiement().getMontant() != null) {
            quittanceDTO.setMontant(quittance.getPaiement().getMontant().intValue());
        } else {
            quittanceDTO.setMontant(0);
        }

        // Session associée via le paiement
        if (quittance.getPaiement() != null && quittance.getPaiement().getSession() != null) {
            quittanceDTO.setIdSession(quittance.getPaiement().getSession().getId().intValue());
        }

        quittanceDTO.setDateUtilisation(quittance.getDateUtilisation());

        return quittanceDTO;
    }
}