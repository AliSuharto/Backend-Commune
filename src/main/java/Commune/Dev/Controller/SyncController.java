package Commune.Dev.Controller;

import Commune.Dev.Dtos.ApiResponse;
import Commune.Dev.Dtos.SyncDataResponse;
import Commune.Dev.Models.User;
import Commune.Dev.Services.JwtManualService;
import Commune.Dev.Services.SyncService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;
    private final JwtManualService jwtManualService;

    /**
     * Endpoint pour la synchronisation initiale
     * Télécharge toutes les données liées à l'utilisateur connecté
     */
    @GetMapping("/initial")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SyncDataResponse>> getInitialSyncData(
            @AuthenticationPrincipal User currentUser) {

        SyncDataResponse syncData = syncService.getSyncDataForUser(currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(
                "Données de synchronisation récupérées avec succès",
                syncData
        ));
    }

    /**
     * Endpoint pour synchroniser un utilisateur spécifique
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<SyncDataResponse>> getSyncDataForUser(
            @PathVariable Long userId,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token manquant"));
        }

        String token = authHeader.substring(7);

        Claims claims;
        try {
            claims = jwtManualService.decodeAndValidate(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token invalide ou expiré"));
        }

        Long tokenUserId = claims.get("id", Long.class);
        String role = claims.get("role", String.class);

        // 🔐 Sécurité ID
        if (!tokenUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Accès interdit à cet utilisateur"));
        }

        // 🔐 Sécurité rôle
        if (!List.of("REGISSEUR", "PERCEPTEUR").contains(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Rôle non autorisé"));
        }

        // ✅ OK sécurisé
        SyncDataResponse syncData = syncService.getSyncDataForUser(tokenUserId);

        return ResponseEntity.ok(ApiResponse.success(
                "Données de synchronisation récupérées avec succès",
                syncData
        ));
    }
}

