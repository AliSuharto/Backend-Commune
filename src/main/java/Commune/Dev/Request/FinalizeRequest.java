package Commune.Dev.Request;

import Commune.Dev.Dtos.CommuneDto;
import Commune.Dev.Dtos.OrdonnateurDto;

public record FinalizeRequest(
        String email, String code,
        CommuneDto commune, OrdonnateurDto ordonnateur) { }
