package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecuPlageResponse {
    private Long plageId;
    private List<String> numerosGeneres;
    private int totalGeneres;
    private String message;

    // Constructeur pour la création avec succès
    public RecuPlageResponse(Long plageId, List<String> numerosGeneres) {
        this.plageId = plageId;
        this.numerosGeneres = numerosGeneres;
        this.totalGeneres = numerosGeneres != null ? numerosGeneres.size() : 0;
        this.message = "Plage créée avec succès";
    }

    // Constructeur pour les erreurs
    public RecuPlageResponse(String message) {
        this.message = message;
        this.totalGeneres = 0;
    }

    // Méthode statique pour créer une réponse de succès
    public static RecuPlageResponse success(Long plageId, List<String> numerosGeneres) {
        return new RecuPlageResponse(plageId, numerosGeneres);
    }

    // Méthode statique pour créer une réponse d'erreur
    public static RecuPlageResponse error(String message) {
        return new RecuPlageResponse(message);
    }
}
