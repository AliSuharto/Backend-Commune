package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "recu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recu {

    @Id
    private Integer id;

    @Column(name = "date_envoie")
    private LocalDateTime dateEnvoie;

    @Column(name = "id_paiement")
    private Integer idPaiement;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_recu")
    private TypeRecu typeRecu = TypeRecu.sms;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutRecu statut = StatutRecu.succes;

    @Column(name = "contenu")
    private String contenu;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paiement", insertable = false, updatable = false)
    private Paiement paiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quittance_id", insertable = false, updatable = false)
    private Quittance quittance;

    public enum TypeRecu {
        sms, qr_code, pdf
    }

    public enum StatutRecu {
        succes, en_attente, echec
    }
}
