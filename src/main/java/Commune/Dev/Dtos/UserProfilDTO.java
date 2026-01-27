package Commune.Dev.Dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfilDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String mail;
    private LocalDateTime creationDate;
    private String role;
}
