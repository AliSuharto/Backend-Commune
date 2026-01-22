package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDtoZone{
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
}
