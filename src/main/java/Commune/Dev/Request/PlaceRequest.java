package Commune.Dev.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceRequest {
    private String nom;
    private String adresse;
    private boolean isOccuped;
    private Long marcheeId;
    private Long categorieId;
    private Long HallId;
    private Long zoneId;
}
