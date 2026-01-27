package Commune.Dev.Controller;

import Commune.Dev.Dtos.ApiResponse;
import Commune.Dev.Dtos.NoteResponse;
import Commune.Dev.Models.User;
import Commune.Dev.Request.NoteRequest;
import Commune.Dev.Services.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@PreAuthorize("isAuthenticated()")
public class NoteController {

    private final NoteService notesService;

    public NoteController(NoteService notesService) {
        this.notesService = notesService;
    }

    // GET /api/notes  → toutes mes notes (avec recherche optionnelle)
    @GetMapping
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getMyNotes(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String search) {

        List<NoteResponse> notes = notesService.getMyNotes(currentUser.getId(), search);
        return ResponseEntity.ok(ApiResponse.success("Notes récupérées", notes));
    }

    // POST /api/notes
    @PostMapping
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(
            @AuthenticationPrincipal User currentUser,
            @RequestBody NoteRequest request) {

        NoteResponse created = notesService.createNote(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note créée avec succès", created));
    }

    // PUT /api/notes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoteResponse>> updateNote(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @RequestBody NoteRequest request) {

        NoteResponse updated = notesService.updateNote(currentUser.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Note modifiée avec succès", updated));
    }

    // DELETE /api/notes/{id}  (optionnel mais souvent utile)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {

        notesService.deleteNote(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Note supprimée avec succès", null));
    }
}
