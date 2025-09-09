package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_audits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    private String field; // Champ modifié (nom, email, role, etc.)
    private String oldValue;
    private String newValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by", nullable = false)
    private User modifiedBy;

    @Column(nullable = false)
    private LocalDateTime modifiedAt = LocalDateTime.now();

    private String description; // Description de l'action
}
