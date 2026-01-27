package Commune.Dev.Dtos;

import Commune.Dev.Models.Roletype;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponseRegisseur {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private Roletype role;
    private Boolean isActive;
    private String telephone;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MarcheeR> marchee;
    private List<HallR>halls;
    private List<ZoneR>zones;

    @Data
     public class MarcheeR{
        private Long id;
        private String nom;

    }
    @Data
    public class HallR{
        private Long id;
        private String nom;

    }
    @Data
    public class ZoneR{
        private Long id;
        private String nom;

    }
}

