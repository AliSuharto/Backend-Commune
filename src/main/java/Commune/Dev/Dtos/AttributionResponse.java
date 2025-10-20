package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributionResponse {
    private boolean success;
    private String message;
    private PlaceAttrDTO place;
    private MarchandDTO marchand;
}
