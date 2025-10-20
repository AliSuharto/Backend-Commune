package Commune.Dev.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttributionRequest {
    @NotNull(message = "L'ID du marchand est obligatoire")
    private Integer marchandId;

    @NotNull(message = "L'ID de la place est obligatoire")
    private Integer placeId;
}