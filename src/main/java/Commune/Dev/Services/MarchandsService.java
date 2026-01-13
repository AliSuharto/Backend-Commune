package Commune.Dev.Services;

import Commune.Dev.Dtos.*;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarchandsService {

    private static final Logger logger = LoggerFactory.getLogger(MarchandsService.class);

    @Autowired
    private MarchandsRepository marchandsRepository;

    @Autowired
    private ContratRepository contratRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private DroitannuelRepository droitannuelRepository;

    @Autowired
    private CategorieRepository categorieRepository;

    @Autowired
    private HallsRepository hallRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private MarcheeRepository marcheeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.photo.upload.dir:uploads/photos/marchands}")
    private String photoUploadDirectory;

    // ============================
    // MÉTHODES DE GESTION FICHIERS
    // ============================

    private void createDirectoryIfNotExists() {
        try {
            Path path = Paths.get(photoUploadDirectory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la création du dossier photos", e);
        }
    }

    public String savePhoto(MultipartFile photo) throws IOException {
        if (photo == null || photo.isEmpty()) return null;

        createDirectoryIfNotExists();

        String originalFilename = photo.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID() + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                extension;

        Path filePath = Paths.get(photoUploadDirectory, filename);
        Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public void deletePhoto(String photoFilename) {
        if (photoFilename != null && !photoFilename.isEmpty()) {
            try {
                Files.deleteIfExists(Paths.get(photoUploadDirectory, photoFilename));
            } catch (IOException e) {
                System.err.println("Erreur suppression photo: " + e.getMessage());
            }
        }
    }

    // ============================
    // MÉTHODES CRUD MARCHANDS
    // ============================

    @Transactional
    public Marchands saveMarchandWithPhoto(Marchands marchand, MultipartFile photo) throws IOException {
        if (marchandsRepository.existsByNumCIN(marchand.getNumCIN())) {
            throw new IllegalArgumentException("Un marchand avec ce numéro CIN existe déjà");
        }

        if (photo != null && !photo.isEmpty()) {
            marchand.setPhoto(savePhoto(photo));
        }

        return marchandsRepository.save(marchand);
    }

    @Transactional
    // ===============================
// MÉTHODE PRINCIPALE D'IMPORT
// ===============================
    public ImportResult importMarchandsAndContrats(MultipartFile excelFile) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Chaque ligne a sa propre transaction isolée
                    processRowInTransaction(row, i + 1, result);
                } catch (Exception e) {
                    // L'erreur a déjà été loggée et gérée dans processRowInTransaction
                    // Continue avec la ligne suivante
                    logger.debug("Ligne {}: Passage à la ligne suivante après erreur", i + 1);
                }
            }
        }

        logger.info("========== FIN IMPORT ==========");
        logger.info("Marchands créés: {}", result.marchandsSaved.size());
        logger.info("Contrats créés: {}", result.contratsSaved.size());
        logger.info("Erreurs: {}", result.errors.size());

        return result;
    }

    // ===============================
// TRAITEMENT D'UNE LIGNE (TRANSACTION ISOLÉE)
// ===============================
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRowInTransaction(Row row, int lineNumber, ImportResult result) {
        logger.info("========== TRAITEMENT LIGNE {} ==========", lineNumber);

        try {
            // ===============================
            // 1. LECTURE DONNÉES MARCHAND
            // ===============================
            String nom = getCellValue(row.getCell(0));
            String numCIN = getCellValue(row.getCell(1));
            String tel1= getCellValue(row.getCell(2));
            String activite = getCellValue(row.getCell(3));
            String nif = getCellValue(row.getCell(4));
            String stat = getCellValue(row.getCell(5));


            logger.debug("Données marchand lues - Nom: {}, CIN: {}", nom, numCIN);

            // Validation obligatoires
            if (nom == null  || numCIN == null) {
                logger.warn("Ligne {}: Données obligatoires manquantes", lineNumber);
                result.errors.add("Ligne " + lineNumber + ": Nom, Prénom, CIN obligatoires.");
                return;
            }

            // Vérification CIN existant AVANT création
            if (marchandsRepository.existsByNumCIN(numCIN)) {
                logger.warn("Ligne {}: CIN '{}' déjà existant", lineNumber, numCIN);
                result.errors.add("Ligne " + lineNumber + ": CIN '" + numCIN + "' déjà existant.");
                return;
            }

            // ===============================
            // 2. CRÉATION MARCHAND
            // ===============================
            logger.info("Ligne {}: Création du marchand {} ", lineNumber, nom);

            Marchands marchand = new Marchands();
            marchand.setNom(nom);
            marchand.setNumCIN(numCIN);
            marchand.setNumTel1(tel1);
            marchand.setActivite(activite);
            marchand.setNIF(nif);
            marchand.setSTAT(stat);
            marchand.setIsCarteGenerer(false);

            // SAUVEGARDE IMMÉDIATE + FLUSH pour obtenir l'ID
            Marchands savedMarchand = marchandsRepository.saveAndFlush(marchand);

            if (savedMarchand.getId() == null) {
                logger.error("Ligne {}: Impossible de générer l'ID du marchand", lineNumber);
                result.errors.add("Ligne " + lineNumber + ": Impossible de générer l'ID du marchand.");
                throw new RuntimeException("ID du marchand null après saveAndFlush");
            }

            logger.info("Ligne {}: Marchand créé avec succès - ID: {}", lineNumber, savedMarchand.getId());
            result.marchandsSaved.add(savedMarchand);

            // ===============================
            // 3. LECTURE DONNÉES CONTRAT
            // ===============================
            String categorieName = getCellValue(row.getCell(6));
            String marcheeName = getCellValue(row.getCell(7));
            String zoneName = getCellValue(row.getCell(8));
            String hallName = getCellValue(row.getCell(9));
            String placeName = getCellValue(row.getCell(10));
            BigDecimal droitAnnuel = getCellValueAsBigDecimal(row.getCell(11));

            logger.debug("Ligne {}: Données contrat - Place: {}, Hall: {}, Zone: {}, Marché: {}, Catégorie: {}, Droit: {}",
                    lineNumber, placeName, hallName, zoneName, marcheeName, categorieName, droitAnnuel);

            // Pas de place ⇒ marchand créé mais pas de contrat
            if (placeName == null || placeName.isEmpty()) {
                logger.info("Ligne {}: Pas de place spécifiée, marchand créé sans contrat", lineNumber);
                return;
            }

            // ===============================
            // 4. RECHERCHE PLACE PAR HIÉRARCHIE
            // ===============================
            Place place = findPlace(lineNumber, placeName, hallName, zoneName, marcheeName, result);
            if (place == null) {
                // Erreur déjà ajoutée dans findPlace()
                return;
            }

            // ===============================
            // 5. VÉRIFICATION PLACE LIBRE
            // ===============================
            logger.debug("Ligne {}: Vérification si la place ID: {} est libre", lineNumber, place.getId());
            Optional<Contrat> contratExistant = contratRepository.findContratByPlace(place.getId());

            if (contratExistant.isPresent()) {
                Contrat contrat = contratExistant.get();
                Optional<Marchands> marchandOccupant = marchandsRepository.findById(contrat.getIdMarchand());

                String marchandInfo = marchandOccupant.isPresent()
                        ? marchandOccupant.get().getNom()
                        : "ID: " + contrat.getIdMarchand();

                logger.warn("Ligne {}: Place '{}' (ID: {}) déjà occupée par {} (Contrat ID: {})",
                        lineNumber, placeName, place.getId(), marchandInfo, contrat.getId());

                result.errors.add("Ligne " + lineNumber + ": Place '" + placeName +
                        "' déjà occupée par " + marchandInfo + ". Marchand créé mais sans contrat.");
                return;
            }

            logger.info("Ligne {}: Place '{}' (ID: {}) est libre", lineNumber, placeName, place.getId());

            // ===============================
            // 6. VALIDATION CATÉGORIE
            // ===============================
            Categorie categorie = validateCategorie(lineNumber, categorieName, result);
            if (categorie == null) {
                return;
            }

            // ===============================
            // 7. VALIDATION DROIT ANNUEL
            // ===============================
            DroitAnnuel droitAnnuelEntity = validateDroitAnnuel(lineNumber, droitAnnuel, result);
            if (droitAnnuelEntity == null) {
                return;
            }

            // ===============================
            // 8. CRÉATION CONTRAT
            // ===============================
            logger.info("Ligne {}: Création du contrat - Marchand ID: {}, Place ID: {}, Catégorie ID: {}, Droit ID: {}",
                    lineNumber, savedMarchand.getId(), place.getId(), categorie.getId(), droitAnnuelEntity.getId());

            Contrat contrat = new Contrat();
            contrat.setIdMarchand(savedMarchand.getId());
            contrat.setIdPlace(place.getId());
            contrat.setCategorieId(categorie.getId());
            contrat.setDroitAnnuelId(droitAnnuelEntity.getId());
            contrat.setDateOfStart(LocalDate.now());
            contrat.setDateOfCreation(LocalDateTime.now());
            contrat.setIsActif(true);
            contrat.setFrequencePaiement(FrequencePaiement.MENSUEL);

            // Mise à jour de la place
            place.setMarchands(savedMarchand);
            place.setIsOccuped(true);
            place.setDateDebutOccupation(LocalDateTime.now());
            place.setDateFinOccupation(null);

            contrat = contratRepository.saveAndFlush(contrat);
            placeRepository.saveAndFlush(place);

            result.contratsSaved.add(contrat);

            logger.info("Ligne {}: ✅ CONTRAT CRÉÉ AVEC SUCCÈS - Contrat ID: {}", lineNumber, contrat.getId());

        } catch (Exception e) {
            logger.error("Ligne {}: ❌ EXCEPTION LORS DU TRAITEMENT", lineNumber, e);
            result.errors.add("Ligne " + lineNumber + ": ERREUR - " + e.getMessage());

            // Propager l'exception pour déclencher le rollback automatique
            throw new RuntimeException("Erreur lors du traitement de la ligne " + lineNumber, e);
        }
    }

// ===============================
// MÉTHODES UTILITAIRES
// ===============================

    private Place findPlace(int lineNumber, String placeName, String hallName,
                            String zoneName, String marcheeName, ImportResult result) {
        logger.info("Ligne {}: Début recherche de la place '{}'", lineNumber, placeName);

        // CAS 1: Recherche par HALL
        if (hallName != null && !hallName.isEmpty()) {
            logger.debug("Ligne {}: Recherche par HALL - hallName: '{}'", lineNumber, hallName);

            Optional<Halls> hallOpt = hallRepository.findByNom(hallName);
            if (hallOpt.isEmpty()) {
                logger.warn("Ligne {}: Hall '{}' introuvable", lineNumber, hallName);
                result.errors.add("Ligne " + lineNumber + ": Hall '" + hallName +
                        "' introuvable. Marchand créé mais sans contrat.");
                return null;
            }

            Long idHall = hallOpt.get().getId();
            logger.debug("Ligne {}: Hall trouvé - ID: {}", lineNumber, idHall);

            Optional<Place> placeOpt = placeRepository.findByNomAndHallId(placeName, Math.toIntExact(idHall));
            if (placeOpt.isEmpty()) {
                logger.warn("Ligne {}: Place '{}' introuvable dans hall '{}' (ID: {})",
                        lineNumber, placeName, hallName, idHall);
                result.errors.add("Ligne " + lineNumber + ": Place '" + placeName +
                        "' introuvable dans le hall '" + hallName + "'. Marchand créé mais sans contrat.");
                return null;
            }

            Place place = placeOpt.get();
            logger.info("Ligne {}: Place trouvée - ID: {}, Nom: '{}', Hall: '{}'",
                    lineNumber, place.getId(), place.getNom(), hallName);
            return place;
        }

        // CAS 2: Recherche par ZONE
        else if (zoneName != null && !zoneName.isEmpty()) {
            logger.debug("Ligne {}: Recherche par ZONE - zoneName: '{}'", lineNumber, zoneName);

            Optional<Zone> zoneOpt = zoneRepository.findByNom(zoneName);
            if (zoneOpt.isEmpty()) {
                logger.warn("Ligne {}: Zone '{}' introuvable", lineNumber, zoneName);
                result.errors.add("Ligne " + lineNumber + ": Zone '" + zoneName +
                        "' introuvable. Marchand créé mais sans contrat.");
                return null;
            }

            Long idZone = zoneOpt.get().getId();
            logger.debug("Ligne {}: Zone trouvée - ID: {}", lineNumber, idZone);

            Optional<Place> placeOpt = placeRepository.findByNomAndZoneId(placeName, Math.toIntExact(idZone));
            if (placeOpt.isEmpty()) {
                logger.warn("Ligne {}: Place '{}' introuvable dans zone '{}' (ID: {})",
                        lineNumber, placeName, zoneName, idZone);
                result.errors.add("Ligne " + lineNumber + ": Place '" + placeName +
                        "' introuvable dans la zone '" + zoneName + "'. Marchand créé mais sans contrat.");
                return null;
            }

            Place place = placeOpt.get();
            logger.info("Ligne {}: Place trouvée - ID: {}, Nom: '{}', Zone: '{}'",
                    lineNumber, place.getId(), place.getNom(), zoneName);
            return place;
        }

        // CAS 3: Recherche par MARCHÉ
        else if (marcheeName != null && !marcheeName.isEmpty()) {
            logger.debug("Ligne {}: Recherche par MARCHÉ - marcheeName: '{}'", lineNumber, marcheeName);

            Optional<Marchee> marcheeOpt = marcheeRepository.findByNom(marcheeName);
            if (marcheeOpt.isEmpty()) {
                logger.warn("Ligne {}: Marché '{}' introuvable", lineNumber, marcheeName);
                result.errors.add("Ligne " + lineNumber + ": Marché '" + marcheeName +
                        "' introuvable. Marchand créé mais sans contrat.");
                return null;
            }

            Long idMarchee = marcheeOpt.get().getId();
            logger.debug("Ligne {}: Marché trouvé - ID: {}", lineNumber, idMarchee);

            Optional<Place> placeOpt = placeRepository.findByNomAndMarcheeId(placeName, Math.toIntExact(idMarchee));
            if (placeOpt.isEmpty()) {
                logger.warn("Ligne {}: Place '{}' introuvable dans marché '{}' (ID: {})",
                        lineNumber, placeName, marcheeName, idMarchee);
                result.errors.add("Ligne " + lineNumber + ": Place '" + placeName +
                        "' introuvable dans le marché '" + marcheeName + "'. Marchand créé mais sans contrat.");
                return null;
            }

            Place place = placeOpt.get();
            logger.info("Ligne {}: Place trouvée - ID: {}, Nom: '{}', Marché: '{}'",
                    lineNumber, place.getId(), place.getNom(), marcheeName);
            return place;
        }

        // CAS 4: Aucune hiérarchie fournie
        else {
            logger.error("Ligne {}: Aucune hiérarchie fournie (Hall, Zone, Marché tous vides)", lineNumber);
            result.errors.add("Ligne " + lineNumber + ": Impossible de localiser la place '" + placeName +
                    "' sans hall, zone ou marché. Marchand créé mais sans contrat.");
            return null;
        }
    }

    private Categorie validateCategorie(int lineNumber, String categorieName, ImportResult result) {
        logger.debug("Ligne {}: Validation catégorie '{}'", lineNumber, categorieName);

        if (categorieName == null || categorieName.isEmpty()) {
            logger.warn("Ligne {}: Catégorie manquante", lineNumber);
            result.errors.add("Ligne " + lineNumber + ": Catégorie manquante. Marchand créé mais sans contrat.");
            return null;
        }

        Categorie.CategorieNom categorieNomEnum;
        try {
            categorieNomEnum = Categorie.CategorieNom.valueOf(categorieName.trim().toUpperCase());
            logger.debug("Ligne {}: Catégorie enum convertie: {}", lineNumber, categorieNomEnum);
        } catch (IllegalArgumentException e) {
            logger.error("Ligne {}: Catégorie '{}' invalide (enum inexistant)", lineNumber, categorieName);
            result.errors.add("Ligne " + lineNumber + ": Catégorie '" + categorieName +
                    "' invalide. Marchand créé mais sans contrat.");
            return null;
        }

        Optional<Categorie> categOpt = categorieRepository.findByNom(categorieNomEnum);
        if (categOpt.isEmpty()) {
            logger.error("Ligne {}: Catégorie '{}' introuvable en base de données", lineNumber, categorieName);
            result.errors.add("Ligne " + lineNumber + ": Catégorie '" + categorieName +
                    "' introuvable en base. Marchand créé mais sans contrat.");
            return null;
        }

        logger.info("Ligne {}: Catégorie validée - ID: {}, Nom: {}",
                lineNumber, categOpt.get().getId(), categOpt.get().getNom());
        return categOpt.get();
    }

    private DroitAnnuel validateDroitAnnuel(int lineNumber, BigDecimal droitAnnuel, ImportResult result) {
        logger.debug("Ligne {}: Validation droit annuel: {}", lineNumber, droitAnnuel);

        if (droitAnnuel == null) {
            logger.warn("Ligne {}: Droit annuel manquant", lineNumber);
            result.errors.add("Ligne " + lineNumber + ": Droit annuel manquant. Marchand créé mais sans contrat.");
            return null;
        }

        Optional<DroitAnnuel> daOpt = droitannuelRepository.findByMontant(droitAnnuel);
        if (daOpt.isEmpty()) {
            logger.error("Ligne {}: Droit annuel '{}' introuvable en base de données", lineNumber, droitAnnuel);
            result.errors.add("Ligne " + lineNumber + ": Droit annuel '" + droitAnnuel +
                    "' introuvable. Marchand créé mais sans contrat.");
            return null;
        }

        logger.info("Ligne {}: Droit annuel validé - ID: {}, Montant: {}",
                lineNumber, daOpt.get().getId(), daOpt.get().getMontant());
        return daOpt.get();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> {
                String value = cell.getStringCellValue().trim();
                yield value.isEmpty() ? null : value;
            }
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        yield String.valueOf((long) cell.getNumericCellValue());
                    } catch (Exception ex) {
                        yield null;
                    }
                }
            }
            default -> null;
        };
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return null;

        try {
            return switch (cell.getCellType()) {
                case STRING -> {
                    String value = cell.getStringCellValue().trim().replace(",", ".");
                    yield value.isEmpty() ? null : new BigDecimal(value);
                }
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                case FORMULA -> {
                    try {
                        yield BigDecimal.valueOf(cell.getNumericCellValue());
                    } catch (Exception e) {
                        String value = cell.getStringCellValue().trim().replace(",", ".");
                        yield value.isEmpty() ? null : new BigDecimal(value);
                    }
                }
                default -> null;
            };
        } catch (Exception e) {
            throw new RuntimeException("Valeur invalide pour BigDecimal à la cellule : '" + cell.getAddress() + "'", e);
        }
    }

    @Transactional(readOnly = true)
    public List<MarchandDTO> getAllMarchands() {
        List<Marchands> marchands = marchandsRepository.findAll();
        return marchands.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<MarchandDTO> getMarchandById(Integer id) {
        return marchandsRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<MarchandsPaiementDTO> getMarchandsById(Integer id) {
        return marchandsRepository.findById(id)
                .map(this::convertMarchandToDTO);
    }

    @Transactional(readOnly = true)
    public List<MarchandDTO> searchMarchands(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return marchandsRepository.findByNomOrPrenomContaining(searchTerm.trim())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Marchands updateMarchand(Integer id, Marchands marchandDetails, MultipartFile newPhoto) throws IOException {
        Marchands existing = marchandsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marchand non trouvé avec l'ID: " + id));

        if (!existing.getNumCIN().equals(marchandDetails.getNumCIN()) &&
                marchandsRepository.existsByNumCIN(marchandDetails.getNumCIN())) {
            throw new IllegalArgumentException("CIN déjà utilisé par un autre marchand");
        }

        existing.setNom(marchandDetails.getNom());

        existing.setNumCIN(marchandDetails.getNumCIN());
        existing.setNumTel1(marchandDetails.getNumTel1());
        existing.setNumTel2(marchandDetails.getNumTel2());
        existing.setAdress(marchandDetails.getAdress());
        existing.setDescription(marchandDetails.getDescription());

        if (newPhoto != null && !newPhoto.isEmpty()) {
            String oldPhoto = existing.getPhoto();
            String newPhotoPath = savePhoto(newPhoto);
            existing.setPhoto(newPhotoPath);
            // Supprimer l'ancienne photo seulement après la sauvegarde réussie
            deletePhoto(oldPhoto);
        }

        return marchandsRepository.save(existing);
    }

    @Transactional
    public void deleteMarchand(Integer id) {
        Marchands marchand = marchandsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marchand non trouvé avec l'ID: " + id));

        String photoToDelete = marchand.getPhoto();
        marchandsRepository.delete(marchand);
        entityManager.flush(); // Force la suppression immédiate
        deletePhoto(photoToDelete);
    }

    public Path getPhotoPath(String photoFilename) {
        if (photoFilename == null || photoFilename.isEmpty()) {
            return null;
        }
        Path path = Paths.get(photoUploadDirectory, photoFilename);
        return Files.exists(path) ? path : null;
    }

    // ============================
    // MAPPING DTO
    // ============================

    private MarchandDTO convertToDTO(Marchands marchand) {
        MarchandDTO dto = new MarchandDTO();
        dto.setId(marchand.getId());
        dto.setNom(marchand.getNom());

        dto.setAdress(marchand.getAdress());
        dto.setDescription(marchand.getDescription());
        dto.setNumCIN(marchand.getNumCIN());
        dto.setActivite(marchand.getActivite());
        dto.setPhoto(marchand.getPhoto());
        dto.setNumTel1(marchand.getNumTel1());
        dto.setNumTel2(marchand.getNumTel2());
        dto.setDateEnregistrement(marchand.getDateEnregistrement());
        dto.setEstEndette(marchand.getEstEndette());

        List<PlaceDTOmarchands> places = Optional.ofNullable(marchand.getPlaces())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::convertPlaceToDTO)
                .collect(Collectors.toList());

        dto.setPlaces(places);
        dto.setHasPlace(!places.isEmpty());

        return dto;
    }

    private PlaceDTOmarchands convertPlaceToDTO(Place place) {
        PlaceDTOmarchands dto = new PlaceDTOmarchands();

        dto.setId(place.getId());
        dto.setNom(place.getNom());
        dto.setDateDebutOccupation(place.getDateDebutOccupation());
        dto.setDateFinOccupation(place.getDateFinOccupation());

        // Catégorie
        if (place.getCategorie() != null) {
            dto.setCategorieId(place.getCategorie().getId());
            dto.setCategorieName(String.valueOf(place.getCategorie().getNom()));
        }

        // --- HALL ---
        Halls hall = place.getHall();
        if (hall != null) {
            dto.setSalleName(hall.getNom());

            // Hall → Zone
            Zone zoneFromHall = hall.getZone();
            if (zoneFromHall != null) {
                dto.setZoneName(zoneFromHall.getNom());

                // Zone → Marchée
                if (zoneFromHall.getMarchee() != null) {
                    dto.setMarcheeName(zoneFromHall.getMarchee().getNom());
                    return dto;
                }
            }

            // Hall → Marchée directement
            if (hall.getMarchee() != null) {
                dto.setMarcheeName(hall.getMarchee().getNom());
                return dto;
            }
        }

        // --- ZONE DIRECTE ---
        if (place.getZone() != null) {
            dto.setZoneName(place.getZone().getNom());

            if (place.getZone().getMarchee() != null) {
                dto.setMarcheeName(place.getZone().getMarchee().getNom());
                return dto;
            }
        }

        // --- MARCHEE DIRECTE ---
        if (place.getMarchee() != null) {
            dto.setMarcheeName(place.getMarchee().getNom());
        }

        return dto;
    }

    // ceci est appeler avant de faire une paiement pour juste la visibilite cote front, mais pour lancer un payement
    // il faut


    @Transactional(readOnly = true)
    public Optional<Marchands> getMarchandsByCIN(String cin) {
        if (cin == null || cin.trim().isEmpty()) return Optional.empty();
        return Optional.ofNullable(marchandsRepository.findByNumCIN(cin.trim()));
    }

    public MarchandsPaiementDTO convertMarchandToDTO(Marchands marchand) {
        MarchandsPaiementDTO dto = new MarchandsPaiementDTO();

        // ------------------------------
        // 1. Informations de base
        // ------------------------------
        dto.setNom(marchand.getNom());
        dto.setId(marchand.getId());
        dto.setCin(marchand.getNumCIN());
        dto.setActivite(marchand.getActivite());
        dto.setTelephone(marchand.getNumTel1());
        dto.setNif(marchand.getNIF());
        dto.setStat(marchand.getSTAT());
        dto.setPlace(construireNomCompletPlace(marchand.getPlaces().getLast()));

        if (marchand.getContrats() == null || marchand.getContrats().isEmpty()) {
            dto.setStatut("Sans contrat");
            dto.setFrequencePaiement("N/A");
            dto.setMontantPlace("0.00");
            dto.setMontantAnnuel("0.00");
            dto.setMotifPaiementPlace("Aucun contrat actif");
            dto.setMotifPaiementAnnuel("Aucun contrat actif");
            return dto;
        }

        Contrat contrat = marchand.getContrats().get(marchand.getContrats().size() - 1);

        dto.setStatut(String.valueOf(marchand.getStatut()));
        dto.setFrequencePaiement(
                contrat.getFrequencePaiement() != null ?
                        contrat.getFrequencePaiement().toString() : "NON_DÉFINI"
        );

        BigDecimal montantPlace = contrat.getCategorie() != null ? contrat.getCategorie().getMontant() : BigDecimal.ZERO;
        BigDecimal montantAnnuel = contrat.getDroitAnnuel() != null ? contrat.getDroitAnnuel().getMontant() : BigDecimal.ZERO;

        dto.setMontantPlace(montantPlace.toPlainString());
        dto.setMontantAnnuel(montantAnnuel.toPlainString());

        // ------------------------------
        // 2. Calcule des deux motifs séparés
        // ------------------------------
        dto.setMotifPaiementPlace(
                calculerMotifParType(marchand, contrat, Paiement.Typepaiement.droit_place)
        );

        dto.setMotifPaiementAnnuel(
                calculerMotifParType(marchand, contrat, Paiement.Typepaiement.droit_annuel)
        );

        return dto;
    }



    private String calculerMotifParType(Marchands marchand, Contrat contrat, Paiement.Typepaiement type) {

        // =====================================================
        //    CAS 1 : DROIT ANNUEL
        // =====================================================
        if (type == Paiement.Typepaiement.droit_annuel) {

            // Récupérer le dernier paiement annuel
            Paiement dernierPaiementAnnuel = marchand.getPaiements().stream()
                    .filter(p -> p.getTypePaiement() == Paiement.Typepaiement.droit_annuel)
                    .filter(p -> p.getDatePaiement() != null)
                    .max(Comparator.comparing(Paiement::getDatePaiement))
                    .orElse(null);

            int anneeProchaine;

            if (dernierPaiementAnnuel == null) {
                // Premier paiement : année de début du contrat
                anneeProchaine = contrat.getDateOfStart() != null ?
                        contrat.getDateOfStart().getYear() : LocalDate.now().getYear();
            } else {
                // Extraire l'année du dernier paiement et ajouter 1
                try {
                    String motif = dernierPaiementAnnuel.getMotif();
                    // Extraire l'année depuis "Droit annuel 2025"
                    int derniereAnnee = Integer.parseInt(motif.replaceAll("\\D+", ""));
                    anneeProchaine = derniereAnnee + 1;
                } catch (Exception e) {
                    // Si erreur, prendre l'année actuelle
                    anneeProchaine = LocalDate.now().getYear();
                }
            }

            return "Droit annuel " + anneeProchaine;
        }

        // =====================================================
        //    CAS 2 : DROIT DE PLACE
        // =====================================================
        else if (type == Paiement.Typepaiement.droit_place) {

            // Récupérer le dernier paiement de place
            Paiement lastPayment = marchand.getPaiements().stream()
                    .filter(p -> p.getTypePaiement() == Paiement.Typepaiement.droit_place)
                    .filter(p -> p.getDatePaiement() != null)
                    .max(Comparator.comparing(Paiement::getDatePaiement))
                    .orElse(null);

            LocalDate nextStart;
            LocalDate nextEnd;
            int index = 1;

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
            FrequencePaiement freq = contrat.getFrequencePaiement();

            // -----------------------------------
            // Calcul de la prochaine période
            // -----------------------------------
            if (lastPayment != null && lastPayment.getDateFin() != null) {
                // On commence le jour après la fin du dernier paiement
                nextStart = lastPayment.getDateFin().plusDays(1);

                // Extraire l'index du dernier paiement
                try {
                    index = Integer.parseInt(lastPayment.getMoisdePaiement().replaceAll("\\D+", "")) + 1;
                } catch (Exception e) {
                    index = 1;
                }
            } else {
                // Aucun paiement → commencer à la date du contrat
                nextStart = contrat.getDateOfStart() != null ?
                        contrat.getDateOfStart() : LocalDate.now();
            }

            // Calculer la date de fin selon la fréquence
            switch (freq) {
                case MENSUEL:
                    nextEnd = nextStart.plusMonths(1).minusDays(1);
                    return "Paiement du " + index + "ᵉ mois (" +
                            nextStart.format(fmt) + " - " + nextEnd.format(fmt) + ")";

                case HEBDOMADAIRE:
                    nextEnd = nextStart.plusWeeks(1).minusDays(1);
                    return "Paiement de la " + index + "ᵉ semaine (" +
                            nextStart.format(fmt) + " - " + nextEnd.format(fmt) + ")";

                case JOURNALIER:
                    nextEnd = nextStart;
                    return "Paiement du jour " + index + " (" + nextStart.format(fmt) + ")";

                default:
                    return "Fréquence non définie";
            }
        }

        return "Type de paiement non reconnu";
    }
    private String construireNomCompletPlace(Place place) {

        List<String> segments = new ArrayList<>();

        // 1️⃣ Place
        if (place.getNom() != null) {
            segments.add(place.getNom());
        }

        // 2️⃣ Hall
        if (place.getHall() != null && place.getHall().getNom() != null) {
            segments.add(place.getHall().getNom());
        }

        // 3️⃣ Zone
        Zone zone = null;

        // Zone via Hall
        if (place.getHall() != null && place.getHall().getZone() != null) {
            zone = place.getHall().getZone();
        }

        // Zone directe
        if (zone == null && place.getZone() != null) {
            zone = place.getZone();
        }

        if (zone != null && zone.getNom() != null) {
            segments.add(zone.getNom());
        }

        // 4️⃣ Marchée
        Marchee marchee = null;

        // Marchée via Zone
        if (zone != null && zone.getMarchee() != null) {
            marchee = zone.getMarchee();
        }

        // Marchée via Hall
        if (marchee == null && place.getHall() != null && place.getHall().getMarchee() != null) {
            marchee = place.getHall().getMarchee();
        }

        // Marchée directe
        if (marchee == null && place.getMarchee() != null) {
            marchee = place.getMarchee();
        }

        if (marchee != null && marchee.getNom() != null) {
            segments.add(marchee.getNom());
        }

        // Assemblage final
        return String.join("/", segments);
    }


}