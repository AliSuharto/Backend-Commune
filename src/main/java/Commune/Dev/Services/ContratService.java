package Commune.Dev.Services;

import Commune.Dev.Dtos.MarchandDetailsDTO;
import Commune.Dev.Dtos.PaiementDTO;
import Commune.Dev.Dtos.PlaceDTOmarchands;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ContratService {

    @Autowired
    private ContratRepository contratRepository;

    @Autowired
    private MarchandsRepository marchandsRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private DroitannuelRepository droitannuelRepository;

    @Autowired
    private CategorieRepository categorieRepository;

    // Récupérer tous les contrats
    public List<Contrat> getAllContrats() {
        return contratRepository.findAllWithRelations();
    }



    // Récupérer un contrat par ID
    public Optional<Contrat> getContratById(Integer id) {
        return contratRepository.findById(id);
    }

    // Récupérer les contrats d'un marchand
    public List<Contrat> getContratsByMarchand(Integer idMarchand) {
        return contratRepository.findByIdMarchand(idMarchand);
    }

    // Récupérer le contrat d'une place
    public Optional<Contrat> getContratByPlace(Integer idPlace) {
        return contratRepository.findContratByPlace(idPlace);
    }

    // Récupérer les contrats par catégorie
    public List<Contrat> getContratsByCategorie(Integer categorieId) {
        return contratRepository.findByCategorieId(categorieId);
    }

    public List<Contrat> getContratsByDroitAnnuel(Integer droitAnnuelId) {
        return contratRepository.findByDroitAnnuelId(droitAnnuelId);
    }


    @Transactional
    public Contrat createContrat(Contrat contrat) {

        if (contrat.getIdMarchand() == null
                || contrat.getIdPlace() == null
                || contrat.getCategorieId() == null
                || contrat.getDroitAnnuelId() == null) {
            throw new IllegalArgumentException("Marchand, place, catégorie et droit annuel sont obligatoires");
        }

        Marchands marchand = marchandsRepository.findById(contrat.getIdMarchand())
                .orElseThrow(() -> new IllegalArgumentException("Marchand non trouvé"));

        Place place = placeRepository.findById(contrat.getIdPlace())
                .orElseThrow(() -> new IllegalArgumentException("Place non trouvée"));

        Categorie categorie = categorieRepository.findById(contrat.getCategorieId())
                .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée"));

        System.out.println("Droit Annuel ID avant save: " + contrat.getDroitAnnuelId());
        DroitAnnuel droitAnnuel = droitannuelRepository.findById(contrat.getDroitAnnuelId())
                .orElseThrow(() -> new IllegalArgumentException("Droit annuel non trouvé"));

        if (contratRepository.findContratByPlace(contrat.getIdPlace()).isPresent()) {
            throw new IllegalStateException("Un contrat existe déjà pour cette place");
        }

        // =========================
        // MAPPING VIA ID (COHÉRENT)
        // =========================

        contrat.setCategorieId(categorie.getId());
        contrat.setDroitAnnuelId(droitAnnuel.getId());
        contrat.setIdMarchand(marchand.getId());
        contrat.setIdPlace(place.getId());


        // =========================
        // GESTION PLACE
        // =========================
        place.setCategorie(categorie);

        // =========================
        // DATES ET ETAT
        // =========================

        if (contrat.getDateOfStart() == null) {
            contrat.setDateOfStart(LocalDate.now());
        }

        contrat.setDateOfCreation(LocalDateTime.now());
        contrat.setIsActif(true);

        if (contrat.getDateOfStart() == null) {
            contrat.setDateOfStart(LocalDate.from(LocalDate.now().atStartOfDay()));
        }

        // =========================
        // NOM AUTOMATIQUE
        // =========================

        if (contrat.getNom() == null || contrat.getNom().isEmpty()) {
            contrat.setNom("Contrat " + place.getNom() + " - " +
                    marchand.getPrenom() + " " + marchand.getNom());
        }

        return contratRepository.save(contrat);
    }



    // Mettre à jour un contrat
    @Transactional
    public Contrat updateContrat(Integer id, Contrat contratDetails) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé avec l'ID: " + id));

        // Mise à jour des champs
        if (contratDetails.getNom() != null) {
            contrat.setNom(contratDetails.getNom());
        }
        if (contratDetails.getDescription() != null) {
            contrat.setDescription(contratDetails.getDescription());
        }
        if (contratDetails.getDateOfStart() != null) {
            contrat.setDateOfStart(contratDetails.getDateOfStart());
        }
        if (contratDetails.getCategorieId() != null) {
            contrat.setCategorieId(contratDetails.getCategorieId());
        }

        return contratRepository.save(contrat);
    }

    // Supprimer un contrat
    @Transactional
    public void deleteContrat(Integer id) {
        if (!contratRepository.existsById(id)) {
            throw new IllegalArgumentException("Contrat non trouvé avec l'ID: " + id);
        }
        contratRepository.deleteById(id);
    }

    // Vérifier si un marchand a un contrat actif
    public boolean marchandHasContrat(Integer idMarchand) {
        List<Contrat> contrats = contratRepository.findByIdMarchand(idMarchand);
        return !contrats.isEmpty();
    }

    // Vérifier si une place a un contrat actif
    public boolean placeHasContrat(Integer idPlace) {
        Optional<Contrat> contrat = contratRepository.findContratByPlace(idPlace);
        return contrat.isPresent();
    }

    public List<MarchandDetailsDTO> getMarchandsAvecContratActif() {

        List<Contrat> contrats = contratRepository.findContratsActifsAvecTout();
        Map<Integer, MarchandDetailsDTO> map = new HashMap<>();

        for (Contrat contrat : contrats) {

            Marchands marchand = contrat.getMarchand();

            MarchandDetailsDTO dto = map.computeIfAbsent(marchand.getId(), id -> {
                MarchandDetailsDTO m = new MarchandDetailsDTO();
                m.setId(marchand.getId());
                m.setDebutContrat(contrat.getDateOfStart());
                m.setNom(safe(marchand.getNom()));
                m.setStatut(safe(String.valueOf(marchand.getStatut())));
                m.setActivite(safe(marchand.getActivite()));
                m.setTelephone(safe(marchand.getNumTel1()));
                m.setCin(safe(marchand.getNumCIN()));
                return m;
            });

            // ================== PLACES ==================
            if (contrat.getPlace() != null) {
                Place place = contrat.getPlace();


                PlaceDTOmarchands placeDTO = new PlaceDTOmarchands();
                placeDTO.setNom(safe(place.getNom()));

                placeDTO.setId(Integer.valueOf(safe(String.valueOf(place.getId()))));

                placeDTO.setSalleName(
                        place.getHall() != null ? safe(place.getHall().getNom()) : ""

                );

                placeDTO.setZoneName(
                        place.getZone() != null ? safe(place.getZone().getNom()) : ""
                );

                placeDTO.setMarcheeName(
                        place.getMarchee() != null ? safe(place.getMarchee().getNom()) : ""
                );

                dto.getPlaces().add(placeDTO);
            }

            // ================== PAIEMENTS ==================

            LocalDateTime dateDernierPaiement = null;

            if (marchand.getPaiements() != null && !marchand.getPaiements().isEmpty()) {
                dateDernierPaiement = marchand.getPaiements()
                        .stream()
                        .map(Paiement::getDatePaiement)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
            }

            if (marchand.getPaiements() != null && !marchand.getPaiements().isEmpty()) {

                for (Paiement p : marchand.getPaiements()) {

                    PaiementDTO paiementDTO = new PaiementDTO();

                    paiementDTO.setMotif(safe(p.getMotif()));
                    paiementDTO.setMontant(p.getMontant() != null ? p.getMontant() : BigDecimal.ZERO);
                    paiementDTO.setDatePaiement(p.getDatePaiement());

                    // ✅ Date réelle du dernier paiement du marchand
                    paiementDTO.setDernierePaiement(dateDernierPaiement);

                    // ---------- RECUS ----------
                    if (p.getRecus() != null) {
                        paiementDTO.setRecuNumero(safe(p.getRecus().getNumero()));
                    } else {
                        paiementDTO.setRecuNumero("");
                    }

                    // ---------- AGENT ----------
                    if (p.getAgent() != null) {
                        paiementDTO.setIdAgent(
                                p.getAgent().getId() != null ? Math.toIntExact(p.getAgent().getId()) : null
                        );
                        paiementDTO.setNomAgent(safe(p.getAgent().getNom()));
                    } else {
                        paiementDTO.setIdAgent(null);
                        paiementDTO.setNomAgent("");
                    }

                    dto.getPaiements().add(paiementDTO);
                };
            }
        }

        return new ArrayList<>(map.values());
    }




    private String safe(String value) {
        return value != null ? value : "";
    }







}
