package Commune.Dev.Request;

import Commune.Dev.Dtos.CommuneDto;
import Commune.Dev.Dtos.UserDto;

public record FinalizeRequest(
        String email, String validationCode,
        CommuneDto commune, UserDto ordonnateur) { }
