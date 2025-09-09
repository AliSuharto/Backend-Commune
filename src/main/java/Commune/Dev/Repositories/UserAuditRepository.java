package Commune.Dev.Repositories;

import Commune.Dev.Models.AuditAction;
import Commune.Dev.Models.User;
import Commune.Dev.Models.UserAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAuditRepository extends JpaRepository<UserAudit, Long> {
    List<UserAudit> findByUserOrderByModifiedAtDesc(User user);
    List<UserAudit> findByModifiedByOrderByModifiedAtDesc(User modifiedBy);
    List<UserAudit> findByAction(AuditAction action);
    List<UserAudit> findByModifiedAtBetween(LocalDateTime start, LocalDateTime end);
}
