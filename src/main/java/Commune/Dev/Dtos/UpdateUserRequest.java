package Commune.Dev.Dtos;

import Commune.Dev.Models.Roletype;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String nom;
    private String prenom;



    @Email(message = "Email invalide")
    private String email;

    private Roletype role;
    private String telephone;
    private Boolean isActive;

    // Ajout des IDs pour les relations
    private List<Integer> marcheeIds;
    private List<Integer> zoneIds;
    private List<Integer> hallIds;
}
