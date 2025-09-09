package Commune.Dev.Dtos;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String pseudo;
    private String photoUrl;
}
