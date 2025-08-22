package Commune.Dev.Services;


import Commune.Dev.Dtos.MarchandsRequestDTO;
import Commune.Dev.Dtos.MarchandsResponseDTO;

import java.util.List;
import java.util.Optional;

public interface MarchandsService {

    MarchandsResponseDTO creerMarchand(MarchandsRequestDTO marchandDTO);

    Optional<MarchandsResponseDTO> obtenirMarchandParId(Integer id);

    Optional<MarchandsResponseDTO> obtenirMarchandParCIN(String numCIN);

    List<MarchandsResponseDTO> obtenirTousLesMarchands();

    List<MarchandsResponseDTO> rechercherMarchandsParNom(String nom);

    MarchandsResponseDTO mettreAJourMarchand(Integer id, MarchandsRequestDTO marchandDTO);

    boolean supprimerMarchand(Integer id);

    boolean marchandExiste(Integer id);

    boolean cinExiste(String numCIN);
}
