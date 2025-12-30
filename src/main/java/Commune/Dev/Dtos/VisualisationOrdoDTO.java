package Commune.Dev.Dtos;

import lombok.Data;

import java.util.List;

@Data
public class VisualisationOrdoDTO {
    private Integer nbr_marchee;
    private Integer nbr_marchands;
    private Integer nbr_user;
    private Integer nbr_marchands_endettee;
    private List<UserResponse> users;
    private List<MarchandDTO> marchands;
    private List<MarcheeDTO> marchees;

}
