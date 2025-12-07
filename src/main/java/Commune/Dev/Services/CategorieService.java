package Commune.Dev.Services;
import Commune.Dev.Dtos.CategorieRequestDTO;
import Commune.Dev.Dtos.CategorieResponseDTO;
import Commune.Dev.Models.Categorie;
import Commune.Dev.Repositories.CategorieRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategorieService {

    private final CategorieRepository categorieRepository;

    // Créer une catégorie
    public CategorieResponseDTO creerCategorie(CategorieRequestDTO requestDTO) {
        // Vérifier si la catégorie existe déjà
        if (categorieRepository.existsByNom(requestDTO.getNom())) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà : " + requestDTO.getNom());
        }

        // Générer un nouvel ID (ou utiliser auto-increment si vous modifiez votre modèle)

        Categorie categorie = new Categorie();
        categorie.setNom(requestDTO.getNom());
//        categorie.setIdCreateur(requestDTO.getIdCreateur());
        categorie.setDescription(requestDTO.getDescription());
        categorie.setMontant(requestDTO.getMontant());
        categorie.setDateCreation(LocalDateTime.now());

        Categorie categorieSauvee = categorieRepository.save(categorie);
        return mapToResponseDTO(categorieSauvee);
    }

    // Modifier une catégorie
    public CategorieResponseDTO modifierCategorie(Integer id, CategorieRequestDTO requestDTO) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID : " + id));

        // Vérifier si le nouveau nom n'existe pas déjà (sauf pour la catégorie actuelle)
        if (!categorie.getNom().equals(requestDTO.getNom()) &&
                categorieRepository.existsByNom(requestDTO.getNom())) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà : " + requestDTO.getNom());
        }

        categorie.setNom(requestDTO.getNom());
//        categorie.setIdModificateur(requestDTO.getIdCreateur());
        categorie.setDescription(requestDTO.getDescription());
        categorie.setMontant(requestDTO.getMontant());

        Categorie categorieModifiee = categorieRepository.save(categorie);
        return mapToResponseDTO(categorieModifiee);
    }

    // Récupérer par ID
    @Transactional(readOnly = true)
    public CategorieResponseDTO obtenirCategorieParId(Integer id) {
        Categorie categorie = categorieRepository.findByIdWithoutRelations(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID : " + id));
        return mapToResponseDTO(categorie);
    }

    // Récupérer toutes les catégories
    @Transactional(readOnly = true)
    public List<CategorieResponseDTO> obtenirToutesLesCategories() {
        return categorieRepository.findAllWithoutRelations()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Supprimer une catégorie
    public void supprimerCategorie(Integer id) {
        if (!categorieRepository.existsById(id)) {
            throw new RuntimeException("Catégorie non trouvée avec l'ID : " + id);
        }

        // Vérifier s'il y a des relations (optionnel - dépend de votre logique métier)
        Categorie categorie = categorieRepository.findById(id).get();
        if (categorie.getPlaces() != null && !categorie.getPlaces().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer : cette catégorie contient des places");
        }
        if (categorie.getContrats() != null && !categorie.getContrats().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer : cette catégorie contient des contrats");
        }

        categorieRepository.deleteById(id);
    }

    // Méthode utilitaire pour mapper vers DTO
    private CategorieResponseDTO mapToResponseDTO(Categorie categorie) {
        return new CategorieResponseDTO(
                categorie.getId(),
                categorie.getNom(),
                categorie.getDescription(),
                categorie.getMontant(),
                categorie.getDateCreation()
        );
    }


}
