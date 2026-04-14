package com.xyz.moviebooking.controller;

import com.xyz.moviebooking.exception.ResourceNotFoundException;
import com.xyz.moviebooking.model.Theatre;
import com.xyz.moviebooking.repository.TheatreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/theatres")
@RequiredArgsConstructor
@Tag(name = "Theatres", description = "B2B theatre onboarding and browsing")
public class TheatreController {

    private final TheatreRepository theatreRepository;

    @GetMapping
    @Operation(summary = "READ: List all theatres in a city")
    public ResponseEntity<List<Theatre>> getTheatresByCity(@RequestParam String city) {
        return ResponseEntity.ok(theatreRepository.findByCityIgnoreCaseAndActiveTrue(city));
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "READ: Theatres currently running a specific movie on a date in a city")
    public ResponseEntity<List<Theatre>> getTheatresShowingMovie(
            @PathVariable Long movieId,
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(theatreRepository.findTheatresShowingMovieInCityOnDate(movieId, city, date));
    }

    @GetMapping("/{id}")
    @Operation(summary = "READ: Get theatre details")
    public ResponseEntity<Theatre> getTheatre(@PathVariable Long id) {
        return ResponseEntity.ok(theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found: " + id)));
    }

    @PostMapping
    @Operation(summary = "WRITE (B2B): Onboard a new theatre partner")
    public ResponseEntity<Theatre> onboardTheatre(@RequestBody Theatre theatre) {
        theatre.setActive(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(theatreRepository.save(theatre));
    }

    @PutMapping("/{id}")
    @Operation(summary = "WRITE (B2B): Update theatre details")
    public ResponseEntity<Theatre> updateTheatre(@PathVariable Long id, @RequestBody Theatre updated) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found: " + id));
        theatre.setName(updated.getName());
        theatre.setAddress(updated.getAddress());
        theatre.setContactEmail(updated.getContactEmail());
        theatre.setContactPhone(updated.getContactPhone());
        return ResponseEntity.ok(theatreRepository.save(theatre));
    }
}
