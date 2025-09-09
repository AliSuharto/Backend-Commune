package Commune.Dev.Services;

import Commune.Dev.Models.AuditAction;
import Commune.Dev.Models.User;
import Commune.Dev.Models.UserAudit;
import Commune.Dev.Repositories.UserAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final UserAuditRepository auditRepository;

    public void logAction(User targetUser, AuditAction action, User modifiedBy, String description) {
        logAction(targetUser, action, null, null, null, modifiedBy, description);
    }

    public void logAction(User targetUser, AuditAction action, String field,
                          String oldValue, String newValue, User modifiedBy, String description) {
        UserAudit audit = new UserAudit();
        audit.setUser(targetUser);
        audit.setAction(action);
        audit.setField(field);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setModifiedBy(modifiedBy);
        audit.setModifiedAt(LocalDateTime.now());
        audit.setDescription(description);

        auditRepository.save(audit);
        log.info("Action auditée: {} pour l'utilisateur {} par {}",
                action, targetUser.getEmail(), modifiedBy.getEmail());
    }

    public List<UserAudit> getUserAuditHistory(User user) {
        return auditRepository.findByUserOrderByModifiedAtDesc(user);
    }
}
