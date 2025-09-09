package Commune.Dev.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrdonnateurRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    @NotBlank(message = "Le pseudo est obligatoire")
    private String pseudo;

    private String telephone;


    @NotBlank(message = "Le nom de la commune est obligatoire")
    private String nomCommune;

    @NotBlank(message = "Le code de la commune est obligatoire")
    private String codeCommune;

    private String descriptionCommune;

    // Autres champs de commune selon votre modèle
    private String adresseCommune;

    private String regionCommune;


}