package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarcheeInfoDTO {
    private Integer id;
    private String nom;
    private String adresse;
    private Integer nbrPlace;
    private Long PlaceOccupe;
    private Long PlaceLibre;
    private Long totalZones;
    private Long totalHalls;
    private Double occupationRate;
    private List<ZoneDto> Zone;
    private List<HallDto> Hall;
    private List<PlaceDto> Place;
    private List<UserInfoDto> user;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZoneDto{
        private Long Id;
        private String nom;
        private Long PlaceOccupe;
        private Long PlaceLibre;
        private Integer nbrHall;
        private Integer nbrPlace;
        private List<HallDto> Hall;
        private List<PlaceDto> Place;
        private List<UserInfoDto> user;

    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HallDto{
        private Long Id;
        private String nom;
        private Integer nbrPlace;
        private Long PlaceOccupe;
        private Long PlaceLibre;
        private List<PlaceDto> place;
        private Integer zoneId;
        private Integer marcheeId;
        private List<UserInfoDto> user;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceDto{
        private Long Id;
        private String nom;
        private String statut;
        private Integer HallId;
        private Integer zoneId;
        private Integer marcheeId;
        private String nomMarchand;
        private String statutMarchand;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDto{
        private Long Id;
        private String nom;
        private String phoneNumber;
        private String mail;
    }

}