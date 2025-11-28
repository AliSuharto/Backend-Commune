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
import java.time.LocalTime;
import java.util.List;
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
//    private final QuittanceRepository quittanceRepository;

    /**
     * Effectuer un paiement unique
     */
    @Transactional
    public PaiementDTO effectuerPaiement(PaiementRequestDTO request) {
        // Validation de la session
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));

        // Validation de l'agent
        User agent = userRepository.findById(request.getIdAgent())
                .orElseThrow(() -> new RuntimeException("Agent non trouvé"));

        Paiement paiement = new Paiement();
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setModePaiement(Paiement.ModePaiement.valueOf(request.getModePaiement()));
        paiement.setSession(session);
        paiement.setAgent(agent);

        // ✅ Si c'est un marchand enregistré
        if (request.getIdMarchand() != null) {
            Marchands marchand = marchandsRepository.findById(request.getIdMarchand())
                    .orElseThrow(() -> new RuntimeException("Marchand non trouvé"));
            paiement.setMarchand(marchand);
            paiement.setNomMarchands(marchand.getNom());

            // 🔍 Récupérer le dernier paiement de ce marchand
            Paiement dernierPaiement = paiementRepository
                    .findTopByMarchandIdOrderByDatePaiementDesc(request.getIdMarchand())
                    .orElse(null);

            if (dernierPaiement != null) {
                // ✅ Marchand déjà payé avant → mois = mois suivant
                String dernierMois = dernierPaiement.getMoisdePaiement();
                String moisSuivant = calculerMoisSuivant(dernierMois);

                paiement.setMoisdePaiement(moisSuivant);
                paiement.setMontant(dernierPaiement.getMontant());
                paiement.setPlace(dernierPaiement.getPlace());

            } else {
                // 🚀 Premier paiement → chercher le contrat

                Contrat contrat = contratRepository
                        .findTopByIdMarchandOrderByDateOfStartDesc(request.getIdMarchand())
                        .orElseThrow(() -> new RuntimeException("Contrat non trouvé pour ce marchand"));

                    // 🗓️ Date effective du premier paiement (31 jours après le début du contrat)
                LocalDate datePremierPaiement = contrat.getDateOfStart().plusDays(31);

                   // 🏷️ Mois de paiement = mois de début du contrat (et non le mois du paiement)
                String moisPaiement = capitalizeFirstLetter(
                        contrat.getDateOfStart().getMonth().name().toLowerCase()
                );

                      // 🔧 Enregistrement
                paiement.setMoisdePaiement(moisPaiement);
                paiement.setPlace(contrat.getPlace());

                  // Montant à partir du contrat (si applicable)
                paiement.setMontant(request.getMontant() != null ? request.getMontant() : contrat.getCategorie().getMontant());
            }

        } else {
            // 🚶‍♂️ Marchand ambulant
            if (request.getNomMarchands() == null || request.getNomMarchands().isBlank()) {
                throw new RuntimeException("Le nom du marchand est obligatoire pour un marchand ambulant");
            }
            paiement.setNomMarchands(request.getNomMarchands());
            paiement.setMontant(request.getMontant());
            paiement.setMoisdePaiement(request.getMoisdePaiement());
        }

        // ✅ Place manuelle si fournie (priorité)
        if (request.getIdPlace() != null) {
            Place place = placeRepository.findById(request.getIdPlace())
                    .orElseThrow(() -> new RuntimeException("Place non trouvée"));
            paiement.setPlace(place);
        }

        Paiement saved = paiementRepository.save(paiement);
        return convertToDTO(saved);
    }


/**
 * Calculer le mois suivant à partir d’un mois (en lettres)
 */
private String calculerMoisSuivant(String moisActuel) {
    if (moisActuel == null) return "Janvier";

    // Liste des mois en français (pour affichage propre)
    List<String> mois = List.of(
            "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    );

    int index = mois.indexOf(capitalizeFirstLetter(moisActuel.toLowerCase()));
    if (index == -1) return "Janvier";

    return (index == 11) ? mois.get(0) : mois.get(index + 1);
}

/**
 * Met une majuscule à la première lettre d’un mot
 */
private String capitalizeFirstLetter(String str) {
    if (str == null || str.isEmpty()) return str;
    return str.substring(0, 1).toUpperCase() + str.substring(1);
}




    /**
     * Effectuer plusieurs paiements en même temps
     */
    @Transactional
    public List<PaiementDTO> effectuerMultiplePaiements(MultiplePaiementRequestDTO request) {
        return request.getPaiements().stream()
                .map(this::effectuerPaiement)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer tous les paiements
     */
    public List<PaiementDTO> getAllPaiements() {
        return paiementRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un paiement par ID
     */
    public PaiementDTO getPaiementById(Integer id) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
        return convertToDTO(paiement);
    }

    /**
     * Récupérer les paiements par marchand
     */
    public List<PaiementDTO> getPaiementsByMarchand(Integer idMarchand) {
        List<Paiement> paiements = paiementRepository.findByMarchandId(idMarchand);
        return paiements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements par place
     */
    public List<PaiementDTO> getPaiementsByPlace(Integer idPlace) {
        List<Paiement> paiements = paiementRepository.findByPlaceId(idPlace);
        return paiements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements par session
     */
    public List<PaiementDTO> getPaiementsBySession(Integer sessionId) {
        List<Paiement> paiements = paiementRepository.findBySessionId(sessionId);
        return paiements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements par date
     */
    public List<PaiementDTO> getPaiementsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Paiement> paiements = paiementRepository.findByDatePaiementBetween(startOfDay, endOfDay);
        return paiements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements entre deux dates
     */
    public List<PaiementDTO> getPaiementsByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime startDateTime = dateDebut.atStartOfDay();
        LocalDateTime endDateTime = dateFin.atTime(LocalTime.MAX);
        List<Paiement> paiements = paiementRepository.findByDatePaiementBetween(startDateTime, endDateTime);
        return paiements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements par agent
     */
    public List<PaiementDTO> getPaiementsByAgent(Integer idAgent) {
        List<Paiement> paiements = paiementRepository.findByAgentId(idAgent);
        return paiements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements par mode de paiement
     */
    public List<PaiementDTO> getPaiementsByModePaiement(String modePaiement) {
        Paiement.ModePaiement mode = Paiement.ModePaiement.valueOf(modePaiement);
        List<Paiement> paiements = paiementRepository.findByModePaiement(mode);
        return paiements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements d'un marchand pour un mois spécifique
     */
    public List<PaiementDTO> getPaiementsByMarchandAndMois(Integer idMarchand, String mois) {
        List<Paiement> paiements = paiementRepository.findByMarchandIdAndMoisdePaiement(idMarchand, mois);
        return paiements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

    /**
     * Convertir une entité Paiement en DTO
     */
    private PaiementDTO convertToDTO(Paiement paiement) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(paiement.getId());
        dto.setMontant(paiement.getMontant());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setModePaiement(paiement.getModePaiement().name());
        dto.setMoisdePaiement(paiement.getMoisdePaiement());
        dto.setNomMarchands(paiement.getNomMarchands());

        if (paiement.getMarchand() != null) {
            dto.setIdMarchand(paiement.getMarchand().getId());
        }

        if (paiement.getAgent() != null) {
            dto.setIdAgent(Math.toIntExact(paiement.getAgent().getId()));
            dto.setNomAgent(paiement.getAgent().getNom());
        }

        if (paiement.getPlace() != null) {
            dto.setIdPlace(paiement.getPlace().getId());
            dto.setNomPlace(paiement.getPlace().getNom());
        }

        if (paiement.getSession() != null) {
            dto.setSessionId(Math.toIntExact(paiement.getSession().getId()));
        }

        if (paiement.getQuittance() != null) {
            dto.setQuittanceId(paiement.getQuittance().getId());
        }

        return dto;
    }
}
