package Commune.Dev.Request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CreateMarchandRequest {

    private String nom;
    private String prenom;
    private String numCIN;
    private String numTel1;
    private String adress;
    private String description;
    private String activite;
    private String stat;
    private String nif;

    public CreateMarchandRequest() {}
}
