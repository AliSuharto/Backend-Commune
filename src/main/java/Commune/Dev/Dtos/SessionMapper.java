package Commune.Dev.Dtos;

import Commune.Dev.Models.*;
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


        // 🔹 Création du builder DTO
        SessionDTO.SessionDTOBuilder builder = SessionDTO.builder()
                .id(session.getId())
                .nomSession(session.getNomSession())
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
        dto.setMotif(paiement.getMotif());
        dto.setTypePaiement(paiement.getTypePaiement());

        if (paiement.getMarchand() != null) {
            dto.setNomMarchands(paiement.getMarchand().getNom());
            dto.setIdMarchand(paiement.getMarchand().getId() != null
                    ? paiement.getMarchand().getId().intValue() : null);
            dto.setActiviteMarchands(paiement.getMarchand().getActivite() !=null
                     ?paiement.getMarchand().getActivite().toString():null);
        }

        dto.setNomMarchands(paiement.getNomMarchands());

        if (paiement.getAgent() != null) {
            dto.setNomAgent(paiement.getAgent().getNom());
            dto.setIdAgent(paiement.getAgent().getId() != null
                    ? paiement.getAgent().getId().intValue() : null);
        }

        if (paiement.getPlace() != null) {
            Place place = paiement.getPlace();
            dto.setNomPlace(place.getNom());
            dto.setIdPlace(place.getId() != null ? place.getId().intValue() : null);

            // Construction du nom complet selon la hiérarchie
            StringBuilder nomComplet = new StringBuilder(place.getNom());

            Halls hall = place.getHall();
            Zone zone = null;
            Marchee marchee = null;

            if (hall != null) {
                // La place est dans un hall
                nomComplet.append(" / ").append(hall.getNom());

                // Vérifier si le hall est dans une zone
                zone = hall.getZone();
                if (zone != null) {
                    nomComplet.append(" / ").append(zone.getNom());
                    // Si zone existe, récupérer son marché
                    marchee = zone.getMarchee();
                } else {
                    // Le hall est directement dans un marché
                    marchee = hall.getMarchee();
                }
            } else {
                // La place est directement dans une zone ou un marché
                zone = place.getZone();
                if (zone != null) {
                    nomComplet.append(" / ").append(zone.getNom());
                    marchee = zone.getMarchee();
                } else {
                    // La place est directement dans un marché
                    marchee = place.getMarchee();
                }
            }

            // Ajouter le marché à la fin si il existe
            if (marchee != null) {
                nomComplet.append(" / ").append(marchee.getNom());
            }

            dto.setNomPlaceComplet(nomComplet.toString());
        }

        if (paiement.getSession() != null) {
            dto.setSessionId(paiement.getSession().getId() != null
                    ? paiement.getSession().getId().intValue() : null);
        }

        if (paiement.getQuittance() != null) {
            dto.setQuittanceId(paiement.getQuittance().getId() != null
                    ? paiement.getQuittance().getId().intValue() : null);
            dto.setRecuNumero(paiement.getQuittance().getNom()!=null
            ?paiement.getQuittance().getNom().toString():null);
        }

        return dto;
    }
}
