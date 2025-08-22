package Commune.Dev.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CarteMarchands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarteMarchands {

    @Id
    @NotNull
    private Integer id;

    @Size(max = 255)
    private String description;



    @Size(max = 255)
    private String qrcode;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marchand_id", insertable = false, updatable = false)
    private Marchands marchand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", insertable = false, updatable = false)
    private Place place;
}