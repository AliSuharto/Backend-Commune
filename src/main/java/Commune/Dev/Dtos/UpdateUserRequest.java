package Commune.Dev.Dtos;

import Commune.Dev.Models.Roletype;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nom;
    private String prenom;

    @Email(message = "Email invalide")
    private String email;

    private Roletype role;
    private String telephone;
    private Boolean isActive;
}
