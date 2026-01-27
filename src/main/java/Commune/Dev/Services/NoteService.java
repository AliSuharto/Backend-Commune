package Commune.Dev.Services;


import Commune.Dev.Dtos.NoteResponse;
import Commune.Dev.Models.Notes;

import Commune.Dev.Repositories.NoteRepository;
import Commune.Dev.Request.NoteRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepository notesRepository;

    public NoteService(NoteRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    public List<NoteResponse> getMyNotes(Long userId, String search) {
        List<Notes> notes;

        if (search != null && !search.trim().isEmpty()) {
            notes = notesRepository.findByUserIdAndTitreContainingIgnoreCase(userId, search.trim());
        } else {
            notes = notesRepository.findByUserId(userId);
        }

        return notes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public NoteResponse createNote(Long userId, NoteRequest request) {
        Notes note = new Notes();
        note.setTitre(request.getTitre());
        note.setContenu(request.getContenu());
        note.setUserId(userId);
        note.setCreationDate(LocalDateTime.now());
        note.setModifDate(LocalDateTime.now());

        Notes saved = notesRepository.save(note);
        return toResponse(saved);
    }

    public NoteResponse updateNote(Long userId, Long noteId, NoteRequest request) {
        Notes note = notesRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note non trouvée"));

        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette note");
        }

        note.setTitre(request.getTitre());
        note.setContenu(request.getContenu());
        note.setModifDate(LocalDateTime.now());

        Notes updated = notesRepository.save(note);
        return toResponse(updated);
    }

    public void deleteNote(Long userId, Long noteId) {
        Notes note = notesRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note non trouvée"));

        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette note");
        }

        notesRepository.delete(note);
    }

    private NoteResponse toResponse(Notes note) {
        NoteResponse dto = new NoteResponse();
        dto.setId(note.getId());
        dto.setTitre(note.getTitre());
        dto.setContenu(note.getContenu());
        dto.setCreationDate(note.getCreationDate());
        dto.setModifDate(note.getModifDate());
        return dto;
    }
}
