package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private LocalDateTime firstLogin;
    private LocalDateTime lastLogin;

    @Column(nullable = false)
    private Boolean hasUsedApp = false;

    private Integer loginCount = 0;
}
