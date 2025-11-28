package Commune.Dev.Dtos;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MarchandsResponseDTO {
    private Integer id;
    private String nom;
    private String prenom;
    private String numCIN;
    private LocalDateTime dateDelivrance;
    private String photo;
    private String numTel1;
    private String numTel2;
    private Boolean estEndette;
    private int nombrePaiements;
    private List<PlaceDTO> places;
}
