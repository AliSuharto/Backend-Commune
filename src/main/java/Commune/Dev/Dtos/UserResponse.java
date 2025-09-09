package Commune.Dev.Dtos;

import Commune.Dev.Models.Roletype;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String pseudo;
    private String photoUrl;
    private Roletype role;
    private Boolean isActive;
    private Boolean mustChangePassword;
    private String telephone;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



}
