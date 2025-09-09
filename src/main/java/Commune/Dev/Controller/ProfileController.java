package Commune.Dev.Controller;

import Commune.Dev.Dtos.ApiResponse;
import Commune.Dev.Dtos.UpdateProfileRequest;
import Commune.Dev.Dtos.UserResponse;
import Commune.Dev.Models.User;
import Commune.Dev.Services.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user) {
        UserResponse userResponse = profileService.updateProfile(request, user);
        return ResponseEntity.ok(ApiResponse.success("Profil mis à jour avec succès", userResponse));
    }
}
