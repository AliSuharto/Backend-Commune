package Commune.Dev.Dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteResponse {
    private Long id;
    private String titre;
    private String contenu;
    private LocalDateTime creationDate;
    private LocalDateTime modifDate;
}
