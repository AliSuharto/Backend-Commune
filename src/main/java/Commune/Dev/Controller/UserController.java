package Commune.Dev.Controller;

import Commune.Dev.Dtos.*;
import Commune.Dev.Exception.UserNotFoundException;
import Commune.Dev.Models.User;
import Commune.Dev.Models.UserAudit;
import Commune.Dev.Services.AuditService;
import Commune.Dev.Services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuditService auditService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ORDONNATEUR', 'DIRECTEUR')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal User createdBy) {

        log.debug("User principal: {}", createdBy);
        log.debug("User role: {}", createdBy != null ? createdBy.getRole() : "NULL");

        if (createdBy == null) {
            log.error("Aucun utilisateur authentifié détecté");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Utilisateur non authentifié"));
        }


        // Log pour debug
        log.info("Creating user by: {} (ID: {})", createdBy.getEmail(), createdBy.getId());

        UserResponse userResponse = userService.createUser(request, createdBy);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur créé avec succès", userResponse));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ORDONNATEUR', 'DIRECTEUR')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/regisseurs-percepteurs")
    public ResponseEntity<List<UserResponseRegisseur>> getRegisseursAndPercepteurs() {
        List<UserResponseRegisseur> users = userService.getRegisseurAndPercepteur();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/percepteur")
    public ResponseEntity<List<UserResponsePercepteur>> getPercepteur() {
        List<UserResponsePercepteur> users = userService.getPercepteur();
        return ResponseEntity.ok(users);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DIRECTEUR')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal User currentUser) {

        try {
            UserResponse userResponse = userService.getCurrentUser(currentUser.getId());

            return ResponseEntity.ok(ApiResponse.success(
                    "Utilisateur récupéré avec succès",
                    userResponse
            ));

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal User currentUser) {

        UserResponse userResponse = userService.updateCurrentUser(
                currentUser.getId(),
                request,
                currentUser
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Profil mis à jour avec succès",
                userResponse
        ));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DIRECTEUR')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal User updatedBy) {
        UserResponse userResponse = userService.updateUser(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur mis à jour avec succès", userResponse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DIRECTEUR')")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User deletedBy) {
        userService.deleteUser(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé avec succès", null));
    }

    @GetMapping("/{id}/audit")
    @PreAuthorize("hasRole('DIRECTEUR')")
    public ResponseEntity<ApiResponse<List<AuditResponse>>> getUserAudit(@PathVariable Long id) {
        User user = userService.findById(id);
        List<UserAudit> audits = auditService.getUserAuditHistory(user);

        List<AuditResponse> auditResponses = audits.stream()
                .map(this::convertToAuditResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(auditResponses));
    }

    private AuditResponse convertToAuditResponse(UserAudit audit) {
        AuditResponse response = new AuditResponse();
        response.setId(audit.getId());
        response.setAction(audit.getAction());
        response.setField(audit.getField());
        response.setOldValue(audit.getOldValue());
        response.setNewValue(audit.getNewValue());
        response.setModifiedByName(audit.getModifiedBy().getNom() + " " + audit.getModifiedBy().getPrenom());
        response.setModifiedAt(audit.getModifiedAt());
        response.setDescription(audit.getDescription());
        return response;
    }

    @PutMapping("/{userId}/disable")
    @PreAuthorize("hasAnyRole('DIRECTEUR', 'ORDONNATEUR')")
    public ResponseEntity<UserResponse> disableUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("Demande de désactivation de l'utilisateur {} par {}", userId, currentUser.getEmail());
        UserResponse response = userService.disableUser(userId, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Réactiver un utilisateur
     * Accessible uniquement par DIRECTEUR et ORDONNATEUR
     */
    @PutMapping("/{userId}/enable")
    @PreAuthorize("hasAnyRole('DIRECTEUR', 'ORDONNATEUR')")
    public ResponseEntity<UserResponse> enableUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("Demande de réactivation de l'utilisateur {} par {}", userId, currentUser.getEmail());
        UserResponse response = userService.enableUser(userId, currentUser);
        return ResponseEntity.ok(response);
    }



}

