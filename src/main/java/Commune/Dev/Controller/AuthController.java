package Commune.Dev.Controller;

import Commune.Dev.Dtos.*;
import Commune.Dev.Models.User;
import Commune.Dev.Services.AuthService;
import Commune.Dev.Services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", authResponse));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User user) {
        authService.changePassword(request, user);
        return ResponseEntity.ok(ApiResponse.success("Mot de passe changé avec succès", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        UserResponse userResponse = userService.convertToUserResponse(user);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }
}
