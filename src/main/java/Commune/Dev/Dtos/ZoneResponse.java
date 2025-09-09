package Commune.Dev.Dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZoneResponse {
    private Integer id;
    private String nom;
    private String description;
    private Integer marcheeId;
    private String marcheeNom;
}
