package Commune.Dev.Dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiplePaiementRequestDTO {

    @NotNull(message = "La liste des paiements est obligatoire")
    @NotEmpty(message = "La liste des paiements ne peut pas être vide")
    @Valid
    private List<PaiementRequestDTO> paiements;
}