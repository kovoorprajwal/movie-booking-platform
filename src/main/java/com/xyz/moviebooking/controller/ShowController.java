package com.xyz.moviebooking.controller;

import com.xyz.moviebooking.dto.ShowRequest;
import com.xyz.moviebooking.dto.ShowResponse;
import com.xyz.moviebooking.model.ShowSeat;
import com.xyz.moviebooking.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
@Tag(name = "Shows", description = "Browse and manage shows - Read & Write scenarios")
public class ShowController {

    private final ShowService showService;

    @GetMapping("/search")
    @Operation(summary = "READ: Browse shows by movie + city + date (with timings)",
               description = "Find all shows for a selected movie in a given city on a specific date")
    public ResponseEntity<List<ShowResponse>> browseShows(
            @RequestParam Long movieId,
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showService.getShowsByMovieAndCity(movieId, city, date));
    }

    @GetMapping("/theatre/{theatreId}")
    @Operation(summary = "READ: Get all shows for a theatre on a given date")
    public ResponseEntity<List<ShowResponse>> getTheatreShows(
            @PathVariable Long theatreId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showService.getShowsByTheatreAndDate(theatreId, date));
    }

    @GetMapping("/{showId}/seats")
    @Operation(summary = "READ: Get available seats for a show")
    public ResponseEntity<List<ShowSeat>> getAvailableSeats(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getAvailableSeats(showId));
    }

    @PostMapping
    @Operation(summary = "WRITE (B2B): Theatre partner creates a new show")
    public ResponseEntity<ShowResponse> createShow(@Valid @RequestBody ShowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.createShow(request));
    }

    @PutMapping("/{showId}")
    @Operation(summary = "WRITE (B2B): Theatre partner updates a show")
    public ResponseEntity<ShowResponse> updateShow(@PathVariable Long showId,
                                                   @Valid @RequestBody ShowRequest request) {
        return ResponseEntity.ok(showService.updateShow(showId, request));
    }

    @DeleteMapping("/{showId}")
    @Operation(summary = "WRITE (B2B): Theatre partner cancels/deletes a show")
    public ResponseEntity<Void> cancelShow(@PathVariable Long showId) {
        showService.cancelShow(showId);
        return ResponseEntity.noContent().build();
    }
}
