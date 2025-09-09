package Commune.Dev.Controller;

import Commune.Dev.Dtos.CategorieRequestDTO;
import Commune.Dev.Dtos.CategorieResponseDTO;
import Commune.Dev.Services.CategorieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategorieController {

    private final CategorieService categorieService;

    // Créer une nouvelle catégorie
    @PostMapping
    public ResponseEntity<CategorieResponseDTO> creerCategorie(
            @Valid @RequestBody CategorieRequestDTO requestDTO) {
        try {
            CategorieResponseDTO categorie = categorieService.creerCategorie(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(categorie);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Récupérer toutes les catégories
    @GetMapping
    public ResponseEntity<List<CategorieResponseDTO>> obtenirToutesLesCategories() {
        List<CategorieResponseDTO> categories = categorieService.obtenirToutesLesCategories();
        return ResponseEntity.ok(categories);
    }

    // Récupérer une catégorie par ID
    @GetMapping("/{id}")
    public ResponseEntity<CategorieResponseDTO> obtenirCategorieParId(@PathVariable Integer id) {
        try {
            CategorieResponseDTO categorie = categorieService.obtenirCategorieParId(id);
            return ResponseEntity.ok(categorie);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Modifier une catégorie
    @PutMapping("/{id}")
    public ResponseEntity<CategorieResponseDTO> modifierCategorie(
            @PathVariable Integer id,
            @Valid @RequestBody CategorieRequestDTO requestDTO) {
        try {
            CategorieResponseDTO categorie = categorieService.modifierCategorie(id, requestDTO);
            return ResponseEntity.ok(categorie);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Supprimer une catégorie
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerCategorie(@PathVariable Integer id) {
        try {
            categorieService.supprimerCategorie(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

