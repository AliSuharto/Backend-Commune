package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record CommuneDto(String nom, String localisation, String mail,String pays, String codePostal, String region, String telephone) { }

