package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DroitannuelDTO {
    private Integer id;
    private String description;
    private LocalDateTime dateCreation;
    private BigDecimal montant;
    private Integer nombrePlaces;
}
