package Commune.Dev.Services;

import Commune.Dev.Models.Contrat;
import Commune.Dev.Models.Marchands;
import Commune.Dev.Models.Place;
import Commune.Dev.Models.Categorie;
import Commune.Dev.Repositories.ContratRepository;
import Commune.Dev.Repositories.MarchandsRepository;
import Commune.Dev.Repositories.PlaceRepository;
import Commune.Dev.Repositories.CategorieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ContratService {

    @Autowired
    private ContratRepository contratRepository;

    @Autowired
    private MarchandsRepository marchandsRepository;

    @Autowired
    private PlaceRepository placeRepository;

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

    // Créer un nouveau contrat
    @Transactional
    public Contrat createContrat(Contrat contrat) {
        // Vérifications
        if (contrat.getIdMarchand() == null || contrat.getIdPlace() == null || contrat.getCategorieId() == null) {
            throw new IllegalArgumentException("Le marchand, la place et la catégorie sont obligatoires");
        }

        // Vérifier que le marchand existe
        Optional<Marchands> marchand = marchandsRepository.findById(contrat.getIdMarchand());
        if (marchand.isEmpty()) {
            throw new IllegalArgumentException("Marchand non trouvé avec l'ID: " + contrat.getIdMarchand());
        }

        // Vérifier que la place existe
        Optional<Place> place = placeRepository.findById(contrat.getIdPlace());
        if (place.isEmpty()) {
            throw new IllegalArgumentException("Place non trouvée avec l'ID: " + contrat.getIdPlace());
        }

        // Vérifier que la catégorie existe
        Optional<Categorie> categorie = categorieRepository.findById(contrat.getCategorieId());
        if (categorie.isEmpty()) {
            throw new IllegalArgumentException("Catégorie non trouvée avec l'ID: " + contrat.getCategorieId());
        }

        // Vérifier qu'il n'y a pas déjà un contrat pour cette place
        Optional<Contrat> existingContrat = contratRepository.findContratByPlace(contrat.getIdPlace());
        if (existingContrat.isPresent()) {
            throw new IllegalStateException("Un contrat existe déjà pour cette place");
        }

        // Définir la date de début si elle n'est pas définie
        if (contrat.getDateOfStart() == null) {
            contrat.setDateOfStart(LocalDateTime.now());
        }

        // Générer un nom si non fourni
        if (contrat.getNom() == null || contrat.getNom().isEmpty()) {
            contrat.setNom("Contrat " + place.get().getNom() + " - " +
                    marchand.get().getPrenom() + " " + marchand.get().getNom());
        }

        // Sauvegarder le contrat
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
}
