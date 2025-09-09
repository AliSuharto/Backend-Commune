package Commune.Dev.Request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ZoneRequest {
    private String nom;
    private String description;
    private Integer marcheeId; // ID du marché auquel appartient la zone
}
