package Commune.Dev.Dtos;

import Commune.Dev.Models.AuditAction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditResponse {
    private Long id;
    private AuditAction action;
    private String field;
    private String oldValue;
    private String newValue;
    private String modifiedByName;
    private LocalDateTime modifiedAt;
    private String description;
}
