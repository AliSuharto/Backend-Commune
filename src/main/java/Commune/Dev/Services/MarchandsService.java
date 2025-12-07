package Commune.Dev.Services;

import Commune.Dev.Dtos.*;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
    public ImportResult importMarchandsAndContrats(MultipartFile excelFile) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Variables pour rollback partiel
                Marchands savedMarchand = null;
                String photoPath = null;

                try {
                    logger.info("========== TRAITEMENT LIGNE {} ==========", i + 1);

                    // ===============================
                    // 1. LECTURE DONNÉES MARCHAND
                    // ===============================
                    String nom = getCellValue(row.getCell(0));
                    String prenom = getCellValue(row.getCell(1));
                    String numCIN = getCellValue(row.getCell(2));
                    String tel1 = getCellValue(row.getCell(3));
                    String activite = getCellValue(row.getCell(4));
                    String nif = getCellValue(row.getCell(5));
                    String stat = getCellValue(row.getCell(6));

                    logger.debug("Données marchand lues - Nom: {}, Prénom: {}, CIN: {}", nom, prenom, numCIN);

                    // Validation obligatoires
                    if (nom == null || prenom == null || numCIN == null) {
                        logger.warn("Ligne {}: Données obligatoires manquantes", i + 1);
                        result.errors.add("Ligne " + (i + 1) + ": Nom, Prénom, CIN obligatoires.");
                        continue;
                    }

                    // Vérification CIN existant AVANT création
                    if (marchandsRepository.existsByNumCIN(numCIN)) {
                        logger.warn("Ligne {}: CIN '{}' déjà existant", i + 1, numCIN);
                        result.errors.add("Ligne " + (i + 1) + ": CIN '" + numCIN + "' déjà existant.");
                        continue;
                    }

                    // ===============================
                    // 2. CRÉATION MARCHAND (sans ID null)
                    // ===============================
                    logger.info("Ligne {}: Création du marchand {} {}", i + 1, nom, prenom);

                    Marchands marchand = new Marchands();
                    marchand.setNom(nom);
                    marchand.setPrenom(prenom);
                    marchand.setNumCIN(numCIN);
                    marchand.setNumTel1(tel1);
                    marchand.setActivite(activite);
                    marchand.setNIF(nif);
                    marchand.setSTAT(stat);
                    marchand.setIsCarteGenerer(false);

                    // SAUVEGARDE IMMÉDIATE + FLUSH pour obtenir l'ID
                    savedMarchand = marchandsRepository.saveAndFlush(marchand);

                    if (savedMarchand.getId() == null) {
                        logger.error("Ligne {}: Impossible de générer l'ID du marchand", i + 1);
                        result.errors.add("Ligne " + (i + 1) + ": Impossible de générer l'ID du marchand.");
                        continue;
                    }

                    logger.info("Ligne {}: Marchand créé avec succès - ID: {}", i + 1, savedMarchand.getId());
                    result.marchandsSaved.add(savedMarchand);

                    // ===============================
                    // 3. LECTURE DONNÉES CONTRAT
                    // ===============================
                    String categorieName = getCellValue(row.getCell(7));
                    String marcheeName = getCellValue(row.getCell(8));
                    String zoneName = getCellValue(row.getCell(9));
                    String hallName = getCellValue(row.getCell(10));
                    String placeName = getCellValue(row.getCell(11));

                    BigDecimal droitAnnuel = getCellValueAsBigDecimal(row.getCell(12));

                    logger.debug("Ligne {}: Données contrat - Place: {}, Hall: {}, Zone: {}, Marché: {}, Catégorie: {}, Droit: {}",
                            i + 1, placeName, hallName, zoneName, marcheeName, categorieName, droitAnnuel);

                    // Pas de place ⇒ marchand créé mais pas de contrat
                    if (placeName == null || placeName.isEmpty()) {
                        logger.info("Ligne {}: Pas de place spécifiée, marchand créé sans contrat", i + 1);
                        continue;
                    }

                    // ===============================
                    // 4. RECHERCHE PLACE PAR HIÉRARCHIE
                    // ===============================
                    logger.info("Ligne {}: Début recherche de la place '{}'", i + 1, placeName);
                    Place place = null;

                    // CAS 1: Si hallName existe, chercher par placeName + idHall
                    if (hallName != null && !hallName.isEmpty()) {
                        logger.debug("Ligne {}: Recherche par HALL - hallName: '{}'", i + 1, hallName);

                        // Trouver le hall d'abord
                        Optional<Halls> hallOpt = hallRepository.findByNom(hallName);

                        if (hallOpt.isEmpty()) {
                            logger.warn("Ligne {}: Hall '{}' introuvable", i + 1, hallName);
                            result.errors.add("Ligne " + (i + 1) + ": Hall '" + hallName +
                                    "' introuvable. Marchand créé mais sans contrat.");
                            continue;
                        }

                       Long idHall = hallOpt.get().getId();
                        logger.debug("Ligne {}: Hall trouvé - ID: {}", i + 1, idHall);

                        // Chercher la place par nom + idHall
                        Optional<Place> placeOpt = placeRepository.findByNomAndHallId(placeName, Math.toIntExact(idHall));

                        if (placeOpt.isEmpty()) {
                            logger.warn("Ligne {}: Place '{}' introuvable dans hall '{}' (ID: {})",
                                    i + 1, placeName, hallName, idHall);
                            result.errors.add("Ligne " + (i + 1) + ": Place '" + placeName +
                                    "' introuvable dans le hall '" + hallName + "'. Marchand créé mais sans contrat.");
                            continue;
                        }

                        place = placeOpt.get();
                        logger.info("Ligne {}: Place trouvée - ID: {}, Nom: '{}', Hall: '{}'",
                                i + 1, place.getId(), place.getNom(), hallName);
                    }
                    // CAS 2: Si hall null mais zoneName existe, chercher par placeName + idZone
                    else if (zoneName != null && !zoneName.isEmpty()) {
                        logger.debug("Ligne {}: Recherche par ZONE - zoneName: '{}'", i + 1, zoneName);

                        // Trouver la zone d'abord
                        Optional<Zone> zoneOpt = zoneRepository.findByNom(zoneName);

                        if (zoneOpt.isEmpty()) {
                            logger.warn("Ligne {}: Zone '{}' introuvable", i + 1, zoneName);
                            result.errors.add("Ligne " + (i + 1) + ": Zone '" + zoneName +
                                    "' introuvable. Marchand créé mais sans contrat.");
                            continue;
                        }

                        Long idZone = zoneOpt.get().getId();
                        logger.debug("Ligne {}: Zone trouvée - ID: {}", i + 1, idZone);

                        // Chercher la place par nom + idZone
                        Optional<Place> placeOpt = placeRepository.findByNomAndZoneId(placeName, Math.toIntExact(idZone));

                        if (placeOpt.isEmpty()) {
                            logger.warn("Ligne {}: Place '{}' introuvable dans zone '{}' (ID: {})",
                                    i + 1, placeName, zoneName, idZone);
                            result.errors.add("Ligne " + (i + 1) + ": Place '" + placeName +
                                    "' introuvable dans la zone '" + zoneName + "'. Marchand créé mais sans contrat.");
                            continue;
                        }

                        place = placeOpt.get();
                        logger.info("Ligne {}: Place trouvée - ID: {}, Nom: '{}', Zone: '{}'",
                                i + 1, place.getId(), place.getNom(), zoneName);
                    }
                    // CAS 3: Si hall null et zone null, chercher par placeName + idMarchee
                    else if (marcheeName != null && !marcheeName.isEmpty()) {
                        logger.debug("Ligne {}: Recherche par MARCHÉ - marcheeName: '{}'", i + 1, marcheeName);

                        // Trouver le marché d'abord
                        Optional<Marchee> marcheeOpt = marcheeRepository.findByNom(marcheeName);

                        if (marcheeOpt.isEmpty()) {
                            logger.warn("Ligne {}: Marché '{}' introuvable", i + 1, marcheeName);
                            result.errors.add("Ligne " + (i + 1) + ": Marché '" + marcheeName +
                                    "' introuvable. Marchand créé mais sans contrat.");
                            continue;
                        }

                        Long idMarchee = marcheeOpt.get().getId();
                        logger.debug("Ligne {}: Marché trouvé - ID: {}", i + 1, idMarchee);

                        // Chercher la place par nom + idMarchee
                        Optional<Place> placeOpt = placeRepository.findByNomAndMarcheeId(placeName, Math.toIntExact(idMarchee));

                        if (placeOpt.isEmpty()) {
                            logger.warn("Ligne {}: Place '{}' introuvable dans marché '{}' (ID: {})",
                                    i + 1, placeName, marcheeName, idMarchee);
                            result.errors.add("Ligne " + (i + 1) + ": Place '" + placeName +
                                    "' introuvable dans le marché '" + marcheeName + "'. Marchand créé mais sans contrat.");
                            continue;
                        }

                        place = placeOpt.get();
                        logger.info("Ligne {}: Place trouvée - ID: {}, Nom: '{}', Marché: '{}'",
                                i + 1, place.getId(), place.getNom(), marcheeName);
                    }
                    // CAS 4: Aucune hiérarchie fournie
                    else {
                        logger.error("Ligne {}: Aucune hiérarchie fournie (Hall, Zone, Marché tous vides)", i + 1);
                        result.errors.add("Ligne " + (i + 1) + ": Impossible de localiser la place '" + placeName +
                                "' sans hall, zone ou marché. Marchand créé mais sans contrat.");
                        continue;
                    }

                    // ===============================
                    // 5. VÉRIFICATION PLACE LIBRE
                    // ===============================
                    logger.debug("Ligne {}: Vérification si la place ID: {} est libre", i + 1, place.getId());
                    Optional<Contrat> contratExistant = contratRepository.findContratByPlace(place.getId());

                    if (contratExistant.isPresent()) {
                        // Récupérer les infos du marchand occupant
                        Contrat contrat = contratExistant.get();
                        Optional<Marchands> marchandOccupant = marchandsRepository.findById(contrat.getIdMarchand());

                        String marchandInfo = marchandOccupant.isPresent()
                                ? marchandOccupant.get().getNom() + " " + marchandOccupant.get().getPrenom()
                                : "ID: " + contrat.getIdMarchand();

                        logger.warn("Ligne {}: Place '{}' (ID: {}) déjà occupée par {} (Contrat ID: {})",
                                i + 1, placeName, place.getId(), marchandInfo, contrat.getId());

                        result.errors.add("Ligne " + (i + 1) + ": Place '" + placeName +
                                "' déjà occupée par " + marchandInfo + ". Marchand créé mais sans contrat.");
                        continue;
                    }

                    logger.info("Ligne {}: Place '{}' (ID: {}) est libre", i + 1, placeName, place.getId());

                    // ===============================
                    // 6. VALIDATION CATÉGORIE
                    // ===============================
                    logger.debug("Ligne {}: Validation catégorie '{}'", i + 1, categorieName);

                    if (categorieName == null || categorieName.isEmpty()) {
                        logger.warn("Ligne {}: Catégorie manquante", i + 1);
                        result.errors.add("Ligne " + (i + 1) + ": Catégorie manquante. Marchand créé mais sans contrat.");
                        continue;
                    }

                    Categorie.CategorieNom categorieNomEnum;
                    try {
                        categorieNomEnum = Categorie.CategorieNom.valueOf(categorieName.trim().toUpperCase());
                        logger.debug("Ligne {}: Catégorie enum convertie: {}", i + 1, categorieNomEnum);
                    } catch (IllegalArgumentException e) {
                        logger.error("Ligne {}: Catégorie '{}' invalide (enum inexistant)", i + 1, categorieName);
                        result.errors.add("Ligne " + (i + 1) + ": Catégorie '" + categorieName +
                                "' invalide. Marchand créé mais sans contrat.");
                        continue;
                    }

                    Optional<Categorie> categOpt = categorieRepository.findByNom(categorieNomEnum);
                    if (categOpt.isEmpty()) {
                        logger.error("Ligne {}: Catégorie '{}' introuvable en base de données", i + 1, categorieName);
                        result.errors.add("Ligne " + (i + 1) + ": Catégorie '" + categorieName +
                                "' introuvable en base. Marchand créé mais sans contrat.");
                        continue;
                    }

                    logger.info("Ligne {}: Catégorie validée - ID: {}, Nom: {}",
                            i + 1, categOpt.get().getId(), categOpt.get().getNom());

                    // ===============================
                    // 7. VALIDATION DROIT ANNUEL
                    // ===============================
                    logger.debug("Ligne {}: Validation droit annuel: {}", i + 1, droitAnnuel);

                    if (droitAnnuel == null) {
                        logger.warn("Ligne {}: Droit annuel manquant", i + 1);
                        result.errors.add("Ligne " + (i + 1) + ": Droit annuel manquant. Marchand créé mais sans contrat.");
                        continue;
                    }

                    Optional<DroitAnnuel> daOpt = droitannuelRepository.findByMontant(droitAnnuel);
                    if (daOpt.isEmpty()) {
                        logger.error("Ligne {}: Droit annuel '{}' introuvable en base de données", i + 1, droitAnnuel);
                        result.errors.add("Ligne " + (i + 1) + ": Droit annuel '" + droitAnnuel +
                                "' introuvable. Marchand créé mais sans contrat.");
                        continue;
                    }

                    logger.info("Ligne {}: Droit annuel validé - ID: {}, Montant: {}",
                            i + 1, daOpt.get().getId(), daOpt.get().getMontant());

                    // ===============================
                    // 8. CRÉATION CONTRAT
                    // ===============================
                    logger.info("Ligne {}: Création du contrat - Marchand ID: {}, Place ID: {}, Catégorie ID: {}, Droit ID: {}",
                            i + 1, savedMarchand.getId(), place.getId(), categOpt.get().getId(), daOpt.get().getId());

                    Contrat contrat = new Contrat();
                    contrat.setIdMarchand(savedMarchand.getId()); // ID garanti non null
                    contrat.setIdPlace(place.getId());
                    place.setMarchands(savedMarchand);
                    place.setIsOccuped(true);
                    place.setDateDebutOccupation(LocalDateTime.now());
                    place.setDateFinOccupation(null); // Pas de date de fin pour le moment
                    contrat.setCategorieId(categOpt.get().getId());
                    contrat.setDroitAnnuelId(daOpt.get().getId());
                    contrat.setDateOfStart(LocalDate.now());
                    contrat.setDateOfCreation(LocalDateTime.now());
                    contrat.setIsActif(true);
                    contrat.setFrequencePaiement(FrequencePaiement.MENSUEL);

                    contrat = contratRepository.saveAndFlush(contrat);
                    result.contratsSaved.add(contrat);

                    logger.info("Ligne {}: ✅ CONTRAT CRÉÉ AVEC SUCCÈS - Contrat ID: {}", i + 1, contrat.getId());

                } catch (Exception e) {
                    logger.error("Ligne {}: ❌ EXCEPTION LORS DU TRAITEMENT", i + 1, e);

                    // Rollback manuel si nécessaire
                    if (savedMarchand != null && savedMarchand.getId() != null) {
                        try {
                            logger.warn("Ligne {}: Tentative de rollback du marchand ID: {}", i + 1, savedMarchand.getId());
                            entityManager.detach(savedMarchand);
                            marchandsRepository.deleteById(savedMarchand.getId());
                            entityManager.flush();
                            logger.info("Ligne {}: Rollback effectué avec succès", i + 1);
                        } catch (Exception rollbackEx) {
                            logger.error("Ligne {}: Erreur lors du rollback", i + 1, rollbackEx);
                            System.err.println("Erreur lors du rollback: " + rollbackEx.getMessage());
                        }
                    }

                    result.errors.add("Ligne " + (i + 1) + ": ERREUR - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        logger.info("========== FIN IMPORT ==========");
        logger.info("Marchands créés: {}", result.marchandsSaved.size());
        logger.info("Contrats créés: {}", result.contratsSaved.size());
        logger.info("Erreurs: {}", result.errors.size());

        return result;
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
        existing.setPrenom(marchandDetails.getPrenom());
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
        dto.setPrenom(marchand.getPrenom());
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
        dto.setCin(marchand.getNumCIN());
        dto.setActivite(marchand.getActivite());
        dto.setTelephone(marchand.getNumTel1());
        dto.setNif(marchand.getNIF());
        dto.setStat(marchand.getSTAT());

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

        // Récupérer le dernier paiement du type demandé
        Paiement lastPayment = marchand.getPaiements().stream()
                .filter(p -> p.getTypePaiement() == type)
                .filter(p -> p.getDatePaiement() != null)
                .max(Comparator.comparing(Paiement::getDatePaiement))
                .orElse(null);

        LocalDate lastStart;
        LocalDate lastEnd;

        // -----------------------------------
        // Si un paiement existe → utiliser ses dates
        // -----------------------------------
        if (lastPayment != null && lastPayment.getDateDebut() != null && lastPayment.getDateFin() != null) {

            lastStart = lastPayment.getDateDebut();
            lastEnd = lastPayment.getDateFin();

        } else {
            // Aucun paiement de ce type → commencer à la date du contrat
            lastStart = contrat.getDateOfStart();
            if (lastStart == null) lastStart = LocalDate.now();

            switch (contrat.getFrequencePaiement()) {
                case MENSUEL -> lastEnd = lastStart.plusMonths(1).minusDays(1);
                case HEBDOMADAIRE -> lastEnd = lastStart.plusWeeks(1).minusDays(1);
                case JOURNALIER -> lastEnd = lastStart;
                default -> lastEnd = lastStart;
            }
        }

        // -----------------------------------
        // Calcul de la prochaine période
        // -----------------------------------
        LocalDate nextStart = lastEnd.plusDays(1);
        LocalDate nextEnd;

        switch (contrat.getFrequencePaiement()) {
            case MENSUEL -> nextEnd = nextStart.plusMonths(1).minusDays(1);
            case HEBDOMADAIRE -> nextEnd = nextStart.plusWeeks(1).minusDays(1);
            case JOURNALIER -> nextEnd = nextStart;
            default -> nextEnd = nextStart;
        }

        // Format du motif
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);

        return "Paiement du " + nextStart.format(fmt) + " au " + nextEnd.format(fmt);
    }



}