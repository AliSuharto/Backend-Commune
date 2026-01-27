package Commune.Dev.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteRequest {
//    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

//    @NotBlank(message = "Le contenu est obligatoire")
    private String contenu;
}
