package Commune.Dev.Services;

import Commune.Dev.Models.Marchands;
import Commune.Dev.Repositories.MarchandsRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MarchandsService {

    @Autowired
    private MarchandsRepository marchandsRepository;

    @Value("${app.photo.upload.dir:uploads/photos/marchands}")
    private String photoUploadDirectory;

    // Créer le dossier s'il n'existe pas
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

    // Sauvegarder une photo
    public String savePhoto(MultipartFile photo) throws IOException {
        if (photo == null || photo.isEmpty()) {
            return null;
        }

        createDirectoryIfNotExists();

        // Générer un nom de fichier unique
        String originalFilename = photo.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                fileExtension;

        // Chemin complet du fichier
        Path filePath = Paths.get(photoUploadDirectory, uniqueFilename);

        // Copier le fichier
        Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename; // Retourner seulement le nom du fichier
    }

    // Supprimer une photo
    public void deletePhoto(String photoFilename) {
        if (photoFilename != null && !photoFilename.isEmpty()) {
            try {
                Path filePath = Paths.get(photoUploadDirectory, photoFilename);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log l'erreur mais ne pas arrêter le processus
                System.err.println("Erreur lors de la suppression de la photo: " + e.getMessage());
            }
        }
    }

    // Enregistrer un marchand avec photo
    public Marchands saveMarchandWithPhoto(Marchands marchand, MultipartFile photo) throws IOException {
        // Vérifier si le CIN existe déjà
        if (marchandsRepository.existsByNumCIN(marchand.getNumCIN())) {
            throw new IllegalArgumentException("Un marchand avec ce numéro CIN existe déjà");
        }

        // Sauvegarder la photo si fournie
        if (photo != null && !photo.isEmpty()) {
            String photoFilename = savePhoto(photo);
            marchand.setPhoto(photoFilename);
        }

        return marchandsRepository.save(marchand);
    }

    // Importer des marchands depuis Excel
    public List<Marchands> importFromExcel(MultipartFile excelFile) throws IOException {
        List<Marchands> marchands = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Ignorer la première ligne (en-tête)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    Marchands marchand = new Marchands();

                    // Nom (colonne 1)
                    Cell nomCell = row.getCell(0);
                    if (nomCell != null) {
                        marchand.setNom(nomCell.getStringCellValue().trim());
                    }

                    // Prénom (colonne 2)
                    Cell prenomCell = row.getCell(1);
                    if (prenomCell != null) {
                        marchand.setPrenom(prenomCell.getStringCellValue().trim());
                    }


                    Cell adressCell = row.getCell(2);
                    if (adressCell != null) {
                        marchand.setAdress(adressCell.getStringCellValue().trim());
                    }

                    Cell descriptionCell = row.getCell(3);
                    if (descriptionCell  != null) {
                        marchand.setDescription(descriptionCell .getStringCellValue().trim());
                    }


                    // NumCIN (colonne 3)
                    Cell cinCell = row.getCell(4);
                    if (cinCell != null) {
                        String cinValue = cinCell.getCellType() == CellType.NUMERIC ?
                                String.valueOf((long) cinCell.getNumericCellValue()) :
                                cinCell.getStringCellValue().trim();
                        marchand.setNumCIN(cinValue);
                    }

                    // NumTel1 (colonne 4)
                    Cell tel1Cell = row.getCell(5);
                    if (tel1Cell != null) {
                        String tel1Value = tel1Cell.getCellType() == CellType.NUMERIC ?
                                String.valueOf((long) tel1Cell.getNumericCellValue()) :
                                tel1Cell.getStringCellValue().trim();
                        marchand.setNumTel1(tel1Value);
                    }

                    // NumTel2 (colonne 5)
                    Cell tel2Cell = row.getCell(6);
                    if (tel2Cell != null) {
                        String tel2Value = tel2Cell.getCellType() == CellType.NUMERIC ?
                                String.valueOf((long) tel2Cell.getNumericCellValue()) :
                                tel2Cell.getStringCellValue().trim();
                        marchand.setNumTel2(tel2Value);
                    }

                    // Validation basique
                    if (marchand.getNom() == null || marchand.getNom().isEmpty() ||
                            marchand.getPrenom() == null || marchand.getPrenom().isEmpty() ||
                            marchand.getNumCIN() == null || marchand.getNumCIN().isEmpty()) {
                        errors.add("Ligne " + (i + 1) + ": Nom, prénom et CIN sont obligatoires");
                        continue;
                    }

                    // Vérifier si le CIN existe déjà
                    if (marchandsRepository.existsByNumCIN(marchand.getNumCIN())) {
                        errors.add("Ligne " + (i + 1) + ": CIN " + marchand.getNumCIN() + " existe déjà");
                        continue;
                    }

                    marchands.add(marchand);

                } catch (Exception e) {
                    errors.add("Ligne " + (i + 1) + ": Erreur de traitement - " + e.getMessage());
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException("Erreurs lors de l'importation: " + String.join(", ", errors));
        }

        return marchandsRepository.saveAll(marchands);
    }

    // Obtenir tous les marchands
    public List<Marchands> getAllMarchands() {
        return marchandsRepository.findAll();
    }

    // Obtenir les marchands avec places
    public List<Marchands> getMarchandsWithPlaces() {
        return marchandsRepository.findMarchandsWithPlaces();
    }

    // Rechercher par ID
    public Optional<Marchands> getMarchandById(Integer id) {
        return marchandsRepository.findById(id);
    }

    // Rechercher par nom ou prénom
    public List<Marchands> searchMarchands(String searchTerm) {
        return marchandsRepository.findByNomOrPrenomContaining(searchTerm);
    }

    // Rechercher par CIN
    public Marchands getMarchandByNumCIN(String numCIN) {
        return marchandsRepository.findByNumCIN(numCIN);
    }

    // Mettre à jour un marchand
    public Marchands updateMarchand(Integer id, Marchands marchandDetails, MultipartFile newPhoto) throws IOException {
        Optional<Marchands> marchandOpt = marchandsRepository.findById(id);
        if (!marchandOpt.isPresent()) {
            throw new RuntimeException("Marchand non trouvé avec l'ID: " + id);
        }

        Marchands existingMarchand = marchandOpt.get();

        // Vérifier si le nouveau CIN n'est pas déjà utilisé par un autre marchand
        if (!existingMarchand.getNumCIN().equals(marchandDetails.getNumCIN()) &&
                marchandsRepository.existsByNumCIN(marchandDetails.getNumCIN())) {
            throw new IllegalArgumentException("Ce numéro CIN est déjà utilisé par un autre marchand");
        }

        // Mettre à jour les champs
        existingMarchand.setNom(marchandDetails.getNom());
        existingMarchand.setPrenom(marchandDetails.getPrenom());
        existingMarchand.setNumCIN(marchandDetails.getNumCIN());
        existingMarchand.setNumTel1(marchandDetails.getNumTel1());
        existingMarchand.setNumTel2(marchandDetails.getNumTel2());

        // Gérer la photo
        if (newPhoto != null && !newPhoto.isEmpty()) {
            // Supprimer l'ancienne photo
            deletePhoto(existingMarchand.getPhoto());

            // Sauvegarder la nouvelle photo
            String newPhotoFilename = savePhoto(newPhoto);
            existingMarchand.setPhoto(newPhotoFilename);
        }

        return marchandsRepository.save(existingMarchand);
    }

    // Supprimer un marchand
    public void deleteMarchand(Integer id) {
        Optional<Marchands> marchandOpt = marchandsRepository.findById(id);
        if (marchandOpt.isPresent()) {
            Marchands marchand = marchandOpt.get();

            // Supprimer la photo associée
            deletePhoto(marchand.getPhoto());

            // Supprimer le marchand
            marchandsRepository.deleteById(id);
        } else {
            throw new RuntimeException("Marchand non trouvé avec l'ID: " + id);
        }
    }

    // Obtenir le chemin complet d'une photo
    public Path getPhotoPath(String photoFilename) {
        if (photoFilename == null || photoFilename.isEmpty()) {
            return null;
        }
        return Paths.get(photoUploadDirectory, photoFilename);
    }
}