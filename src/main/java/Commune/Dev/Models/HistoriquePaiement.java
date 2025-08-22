package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Historique-paiement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriquePaiement {

    @Id
    private Integer id;

    @Column(name = "marchand_id")
    private Integer marchandId;

    @Column(name = "paiement_id")
    private Integer paiementId;

    @Column(name = "place_id")
    private Integer placeId;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marchand_id", insertable = false, updatable = false)
    private Marchands marchand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiement_id", insertable = false, updatable = false)
    private Paiement paiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", insertable = false, updatable = false)
    private Place place;
}
