package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalleFilterDTO {
    private String nom;
    private Integer marcheeId;
    private Integer zoneId;
    private String sortBy = "id";
    private String sortDirection = "ASC";
    private int page = 0;
    private int size = 20;
}
