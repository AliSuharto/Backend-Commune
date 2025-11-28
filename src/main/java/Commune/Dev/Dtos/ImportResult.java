package Commune.Dev.Dtos;

import Commune.Dev.Models.Contrat;
import Commune.Dev.Models.Marchands;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {

        public List<Marchands> marchandsSaved = new ArrayList<>();
        public List<Contrat> contratsSaved = new ArrayList<>();
        public List<String> errors = new ArrayList<>();

}
