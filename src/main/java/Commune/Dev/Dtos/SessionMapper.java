package Commune.Dev.Dtos;

import Commune.Dev.Models.Paiement;
import Commune.Dev.Models.Session;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SessionMapper {

    public SessionDTO toDTO(Session session) {
        if (session == null) {
            return null;
        }

        // 🔹 Conversion des paiements en DTO
        List<PaiementDTO> paiementDTOs = null;
        BigDecimal montantCollecte = BigDecimal.ZERO;

        if (session.getPaiements() != null && !session.getPaiements().isEmpty()) {
            paiementDTOs = session.getPaiements().stream()
                    .map(this::toPaiementDTO)
                    .collect(Collectors.toList());

            // Calcul du montant total collecté
            montantCollecte = session.getPaiements().stream()
                    .map(Paiement::getMontant)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // 🔹 Génération du nom de session
        String nomSession = generateNomSession(session);

        // 🔹 Création du builder DTO
        SessionDTO.SessionDTOBuilder builder = SessionDTO.builder()
                .id(session.getId())
                .nomSession(nomSession)
                .type(session.getType())
                .dateSession(session.getStartTime())
                .status(session.getStatus())
                .montantCollecte(montantCollecte)
                .isValid(session.getIsValid())
                .paiements(paiementDTOs)
                .notes(session.getNotes());

        // 🔹 Mapper uniquement les infos essentielles de l'utilisateur
        if (session.getUser() != null) {
            builder.user(new UserSummaryDTO(
                    session.getUser().getId(),
                    session.getUser().getNom(),
                    session.getUser().getPrenom()
            ));
        }

        return builder.build();
    }

    private String generateNomSession(Session session) {
        // Format: id-sessionId-userId-ddMMyyyy
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        String dateFormatted = session.getStartTime() != null
                ? session.getStartTime().format(formatter)
                : "N/A";

        return String.format("%d-%d-%s",
                session.getId() != null ? session.getId() : 0,
                session.getUser() != null ? session.getUser().getId() : 0,
                dateFormatted);
    }

    public List<SessionDTO> toDTOList(List<Session> sessions) {
        return sessions.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 🔹 Méthode pour convertir un Paiement -> PaiementDTO
    private PaiementDTO toPaiementDTO(Paiement paiement) {
        if (paiement == null) return null;

        PaiementDTO dto = new PaiementDTO();
        dto.setId(paiement.getId() != null ? paiement.getId().intValue() : null);
        dto.setMontant(paiement.getMontant());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setModePaiement(String.valueOf(paiement.getModePaiement()));
        dto.setMoisdePaiement(paiement.getMoisdePaiement());

        if (paiement.getMarchand() != null) {
            dto.setNomMarchands(paiement.getMarchand().getNom());
            dto.setIdMarchand(paiement.getMarchand().getId() != null
                    ? paiement.getMarchand().getId().intValue() : null);
        }

        if (paiement.getAgent() != null) {
            dto.setNomAgent(paiement.getAgent().getNom());
            dto.setIdAgent(paiement.getAgent().getId() != null
                    ? paiement.getAgent().getId().intValue() : null);
        }

        if (paiement.getPlace() != null) {
            dto.setNomPlace(paiement.getPlace().getNom());
            dto.setIdPlace(paiement.getPlace().getId() != null
                    ? paiement.getPlace().getId().intValue() : null);
        }

        if (paiement.getSession() != null) {
            dto.setSessionId(paiement.getSession().getId() != null
                    ? paiement.getSession().getId().intValue() : null);
        }

        if (paiement.getQuittance() != null) {
            dto.setQuittanceId(paiement.getQuittance().getId() != null
                    ? paiement.getQuittance().getId().intValue() : null);
        }

        return dto;
    }
}
