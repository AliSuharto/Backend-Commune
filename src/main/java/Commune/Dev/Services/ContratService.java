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
        place.setIsOccuped(true);
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
                     marchand.getNom());
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
                Halls hall = place.getHall();
                Zone zone = place.getZone();
                Marchee marchee = place.getMarchee();

                PlaceDTOmarchands placeDTO = new PlaceDTOmarchands();

                placeDTO.setId(place.getId());
                placeDTO.setNom(safe(place.getNom()));

                String salleName = "";
                String zoneName = "";
                String marcheeName = "";

                // =========================== HALL EXISTE ===========================
                if (hall != null) {

                    salleName = safe(hall.getNom());

                    if (hall.getZone() != null) {

                        zone = hall.getZone(); // overwrite zone

                        zoneName = safe(zone.getNom());

                        if (zone.getMarchee() != null) {
                            marchee = zone.getMarchee();
                            marcheeName = safe(marchee.getNom());
                        } else if (hall.getMarchee() != null) {
                            marchee = hall.getMarchee();
                            marcheeName = safe(marchee.getNom());
                        } else if (place.getMarchee() != null) {
                            marcheeName = safe(place.getMarchee().getNom());
                        }

                    } else {
                        // hall existe mais hall.zone n'existe pas
                        if (hall.getMarchee() != null) {
                            marcheeName = safe(hall.getMarchee().getNom());
                        } else if (place.getMarchee() != null) {
                            marcheeName = safe(place.getMarchee().getNom());
                        }
                    }

                }

                // ======================= PAS DE HALL MAIS ZONE EXISTE ========================
                else if (zone != null) {

                    zoneName = safe(zone.getNom());

                    if (zone.getMarchee() != null) {
                        marcheeName = safe(zone.getMarchee().getNom());
                    } else if (place.getMarchee() != null) {
                        marcheeName = safe(place.getMarchee().getNom());
                    }

                }

                // ======================= NI HALL NI ZONE ========================
                else if (marchee != null) {
                    marcheeName = safe(marchee.getNom());
                }

                placeDTO.setSalleName(salleName);
                placeDTO.setZoneName(zoneName);
                placeDTO.setMarcheeName(marcheeName);

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
                    if (p.getQuittance() != null) {
                        paiementDTO.setRecuNumero(safe(p.getQuittance().getNom()));
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


    public MarchandDetailsDTO getMarchandByCIN(String cin) {

        Marchands marchand = marchandsRepository.findByNumCIN(cin);

        if (marchand == null) {
            throw new RuntimeException("Aucun marchand trouvé avec le CIN : " + cin);
        }
        MarchandDetailsDTO dto = new MarchandDetailsDTO();
        dto.setId(marchand.getId());
        dto.setNom(safe(marchand.getNom()));
        dto.setCin(safe(marchand.getNumCIN()));
        dto.setTelephone(safe(marchand.getNumTel1()));
        dto.setStatut(safe(String.valueOf(marchand.getStatut())));
        dto.setActivite(safe(marchand.getActivite()));

        // =================== CONTRATS ACTIFS ===================
        List<Contrat> contrats = contratRepository.findContratsActifsAvecTout()
                .stream()
                .filter(c -> c.getMarchand().getId().equals(marchand.getId()))
                .toList();

        dto.setDebutContrat(
                contrats.stream()
                        .map(Contrat::getDateOfStart)
                        .filter(Objects::nonNull)
                        .min(LocalDate::compareTo)
                        .orElse(null)
        );

        // =================== PLACES ===================
        for (Contrat contrat : contrats) {
            if (contrat.getPlace() != null) {

                Place place = contrat.getPlace();
                Halls hall = place.getHall();
                Zone zone = place.getZone();
                Marchee marchee = place.getMarchee();

                PlaceDTOmarchands placeDTO = new PlaceDTOmarchands();

                placeDTO.setId(place.getId());
                placeDTO.setNom(safe(place.getNom()));

                String salleName = "";
                String zoneName = "";
                String marcheeName = "";

                // =========================== HALL EXISTE ===========================
                if (hall != null) {

                    salleName = safe(hall.getNom());

                    if (hall.getZone() != null) {

                        zone = hall.getZone(); // overwrite zone

                        zoneName = safe(zone.getNom());

                        if (zone.getMarchee() != null) {
                            marchee = zone.getMarchee();
                            marcheeName = safe(marchee.getNom());
                        } else if (hall.getMarchee() != null) {
                            marchee = hall.getMarchee();
                            marcheeName = safe(marchee.getNom());
                        } else if (place.getMarchee() != null) {
                            marcheeName = safe(place.getMarchee().getNom());
                        }

                    } else {
                        // hall existe mais hall.zone n'existe pas
                        if (hall.getMarchee() != null) {
                            marcheeName = safe(hall.getMarchee().getNom());
                        } else if (place.getMarchee() != null) {
                            marcheeName = safe(place.getMarchee().getNom());
                        }
                    }

                }

                // ======================= PAS DE HALL MAIS ZONE EXISTE ========================
                else if (zone != null) {

                    zoneName = safe(zone.getNom());

                    if (zone.getMarchee() != null) {
                        marcheeName = safe(zone.getMarchee().getNom());
                    } else if (place.getMarchee() != null) {
                        marcheeName = safe(place.getMarchee().getNom());
                    }

                }

                // ======================= NI HALL NI ZONE ========================
                else if (marchee != null) {
                    marcheeName = safe(marchee.getNom());
                }

                placeDTO.setSalleName(salleName);
                placeDTO.setZoneName(zoneName);
                placeDTO.setMarcheeName(marcheeName);

                dto.getPlaces().add(placeDTO);
            }
        }

        // =================== PAIEMENTS ===================
        LocalDateTime dernierPaiement = marchand.getPaiements()
                .stream()
                .map(Paiement::getDatePaiement)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        for (Paiement p : marchand.getPaiements()) {
            PaiementDTO pd = new PaiementDTO();

            pd.setMotif(safe(p.getMotif()));
            pd.setMontant(p.getMontant() != null ? p.getMontant() : BigDecimal.ZERO);
            pd.setDatePaiement(p.getDatePaiement());
            pd.setDernierePaiement(dernierPaiement);

            // ------- RECU -------
            pd.setRecuNumero(
                    p.getQuittance() != null ? safe(p.getQuittance().getNom()) : ""
            );

            // ------- AGENT -------
            if (p.getAgent() != null) {
                pd.setIdAgent(
                        p.getAgent().getId() != null ? Math.toIntExact(p.getAgent().getId()) : null
                );
                pd.setNomAgent(safe(p.getAgent().getNom()));
            } else {
                pd.setIdAgent(null);
                pd.setNomAgent("");
            }

            dto.getPaiements().add(pd);
        }

        return dto;
    }



    private String safe(String value) {
        return value != null ? value : "";
    }







}
