package Commune.Dev.Controller;

import Commune.Dev.Dtos.CreateDroitAnnuelRequest;
import Commune.Dev.Dtos.DroitannuelDTO;
import Commune.Dev.Models.DroitAnnuel;
import Commune.Dev.Services.DroitannuelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/droits-annuels")
public class DroitAnnuelController {

    private final DroitannuelService service;

    public DroitAnnuelController(DroitannuelService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DroitAnnuel> create(@Valid @RequestBody CreateDroitAnnuelRequest request) {
        DroitAnnuel created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<DroitannuelDTO>> getAll() {
        List<DroitannuelDTO> droits = service.getAll();
        return ResponseEntity.ok(droits);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DroitAnnuel> getById(@PathVariable Integer id) {
        DroitAnnuel droit = service.getById(id);
        return ResponseEntity.ok(droit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DroitAnnuel> update(
            @PathVariable Integer id,
            @RequestBody CreateDroitAnnuelRequest request) {

        DroitAnnuel updated = service.update(id, request);
        return ResponseEntity.ok(updated);
    }

}