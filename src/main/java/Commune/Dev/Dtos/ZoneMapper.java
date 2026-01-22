package Commune.Dev.Dtos;

import Commune.Dev.Models.User;
import Commune.Dev.Models.Zone;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ZoneMapper {

    /**
     * Convertit une Zone en ZoneDTO avec les statistiques précalculées
     */
    public ZoneDTO toDTO(
            Zone zone,
            int nbrHall,
            int nbrPlaceTotal,
            int nbrPlaceLibre,
            int nbrPlaceOccupee
    ) {
        if (zone == null) {
            return null;
        }

        // Mapper les utilisateurs
        List<UserDtoZone> usersDto = null;
        if (zone.getUsers() != null) {
            usersDto = zone.getUsers().stream()
                    .map(this::toUserDto)
                    .collect(Collectors.toList());
        }

        return ZoneDTO.builder()
                .id(zone.getId())
                .nom(zone.getNom())
                .marcheeId(zone.getMarchee() != null ? zone.getMarchee().getId() : null)
                .nomMarchee(zone.getMarchee() != null ? zone.getMarchee().getNom() : null)
                .nbrHall(nbrHall)
                .nbrPlace(nbrPlaceTotal)
                .nbrPlaceLibre(nbrPlaceLibre)
                .nbrPlaceOccupee(nbrPlaceOccupee)
                .users(usersDto)
                .build();
    }

    /**
     * Convertit un User en UserDtoZone
     */
    private UserDtoZone toUserDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDtoZone(
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getTelephone()
        );
    }
}