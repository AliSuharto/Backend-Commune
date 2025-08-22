package Commune.Dev.Services;
import Commune.Dev.Dtos.MarchandsRequestDTO;
import Commune.Dev.Dtos.MarchandsResponseDTO;
import Commune.Dev.Models.Marchands;
import Commune.Dev.Repositories.MarchandsRepository;
import Commune.Dev.Services.MarchandsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MarchandsServiceImpl implements MarchandsService {

    private final MarchandsRepository marchandsRepository;

    @Override
    public MarchandsResponseDTO creerMarchand(MarchandsRequestDTO marchandDTO) {
        log.info("Création d'un nouveau marchand avec CIN: {}", marchandDTO.getNumCIN());

        // Vérifier si le CIN existe déjà
        if (cinExiste(marchandDTO.getNumCIN())) {
            throw new IllegalArgumentException("Un marchand avec ce numéro CIN existe déjà");
        }

        // Vérifier si l'ID existe déjà
        if (marchandExiste(marchandDTO.getId())) {
            throw new IllegalArgumentException("Un marchand avec cet ID existe déjà");
        }

        Marchands marchand = convertirDTOVersEntite(marchandDTO);
        Marchands marchandSauvegarde = marchandsRepository.save(marchand);

        log.info("Marchand créé avec succès - ID: {}", marchandSauvegarde.getId());
        return convertirEntiteVersDTO(marchandSauvegarde);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MarchandsResponseDTO> obtenirMarchandParId(Integer id) {
        log.debug("Recherche du marchand avec ID: {}", id);
        return marchandsRepository.findById(id)
                .map(this::convertirEntiteVersDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MarchandsResponseDTO> obtenirMarchandParCIN(String numCIN) {
        log.debug("Recherche du marchand avec CIN: {}", numCIN);
        return marchandsRepository.findByNumCIN(numCIN)
                .map(this::convertirEntiteVersDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarchandsResponseDTO> obtenirTousLesMarchands() {
        log.debug("Récupération de tous les marchands");
        return marchandsRepository.findAll()
                .stream()
                .map(this::convertirEntiteVersDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarchandsResponseDTO> rechercherMarchandsParNom(String nom) {
        log.debug("Recherche des marchands avec nom contenant: {}", nom);
        return marchandsRepository.findByNomContainingIgnoreCase(nom)
                .stream()
                .map(this::convertirEntiteVersDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MarchandsResponseDTO mettreAJourMarchand(Integer id, MarchandsRequestDTO marchandDTO) {
        log.info("Mise à jour du marchand avec ID: {}", id);

        Marchands marchandExistant = marchandsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marchand non trouvé avec l'ID: " + id));

        // Vérifier si le nouveau CIN n'est pas déjà utilisé par un autre marchand
        if (!marchandExistant.getNumCIN().equals(marchandDTO.getNumCIN()) &&
                cinExiste(marchandDTO.getNumCIN())) {
            throw new IllegalArgumentException("Un autre marchand utilise déjà ce numéro CIN");
        }

        // Mettre à jour les champs
        marchandExistant.setNom(marchandDTO.getNom());
        marchandExistant.setPrenom(marchandDTO.getPrenom());
        marchandExistant.setNumCIN(marchandDTO.getNumCIN());
        marchandExistant.setDateDelivrance(marchandDTO.getDateDelivrance());
        marchandExistant.setPhoto(marchandDTO.getPhoto());
        marchandExistant.setNumTel1(marchandDTO.getNumTel1());
        marchandExistant.setNumTel2(marchandDTO.getNumTel2());

        Marchands marchandMisAJour = marchandsRepository.save(marchandExistant);
        log.info("Marchand mis à jour avec succès - ID: {}", id);

        return convertirEntiteVersDTO(marchandMisAJour);
    }

    @Override
    public boolean supprimerMarchand(Integer id) {
        log.info("Suppression du marchand avec ID: {}", id);

        if (!marchandExiste(id)) {
            log.warn("Tentative de suppression d'un marchand inexistant - ID: {}", id);
            return false;
        }

        try {
            marchandsRepository.deleteById(id);
            log.info("Marchand supprimé avec succès - ID: {}", id);
            return true;
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du marchand - ID: {}", id, e);
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean marchandExiste(Integer id) {
        return marchandsRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean cinExiste(String numCIN) {
        return marchandsRepository.findByNumCIN(numCIN).isPresent();
    }

    // Méthodes utilitaires de conversion
    private Marchands convertirDTOVersEntite(MarchandsRequestDTO dto) {
        Marchands marchand = new Marchands();
        marchand.setId(dto.getId());
        marchand.setNom(dto.getNom());
        marchand.setPrenom(dto.getPrenom());
        marchand.setNumCIN(dto.getNumCIN());
        marchand.setDateDelivrance(dto.getDateDelivrance());
        marchand.setPhoto(dto.getPhoto());
        marchand.setNumTel1(dto.getNumTel1());
        marchand.setNumTel2(dto.getNumTel2());
        return marchand;
    }

    private MarchandsResponseDTO convertirEntiteVersDTO(Marchands marchand) {
        MarchandsResponseDTO dto = new MarchandsResponseDTO();
        dto.setId(marchand.getId());
        dto.setNom(marchand.getNom());
        dto.setPrenom(marchand.getPrenom());
        dto.setNumCIN(marchand.getNumCIN());
        dto.setDateDelivrance(marchand.getDateDelivrance());
        dto.setPhoto(marchand.getPhoto());
        dto.setNumTel1(marchand.getNumTel1());
        dto.setNumTel2(marchand.getNumTel2());

        // Compter les relations associées
        dto.setNombreCartes(marchand.getCarteMarchands() != null ? marchand.getCarteMarchands().size() : 0);
        dto.setNombrePaiements(marchand.getPaiements() != null ? marchand.getPaiements().size() : 0);
        dto.setNombreContrats(marchand.getContrats() != null ? marchand.getContrats().size() : 0);
        dto.setNombrePlaces(marchand.getPlaces() != null ? marchand.getPlaces().size() : 0);

        return dto;
    }
}
