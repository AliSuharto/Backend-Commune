package Commune.Dev.Dtos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneDTOnomComplet {
    private Long id;
    private String nom;
    private Long marcheeId;
    private Integer nbrHall;
    private Integer nbrPlace;
    private Integer nbrPlaceLibre;
    private Integer nbrPlaceOccupee;
    private String nomMarchee;
    private List<UserDtoZone> users;

}
