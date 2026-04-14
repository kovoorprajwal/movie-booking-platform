package com.xyz.moviebooking.controller;

import com.xyz.moviebooking.model.Movie;
import com.xyz.moviebooking.repository.MovieRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Browse movies by city, language, and genre")
public class MovieController {

    private final MovieRepository movieRepository;

    @GetMapping
    @Operation(summary = "READ: List all active movies")
    public ResponseEntity<List<Movie>> getAllMovies() {
        return ResponseEntity.ok(movieRepository.findByStatus(Movie.MovieStatus.ACTIVE));
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "READ: List movies currently playing in a city")
    public ResponseEntity<List<Movie>> getMoviesInCity(@PathVariable String city) {
        return ResponseEntity.ok(movieRepository.findMoviesPlayingInCity(city));
    }

    @GetMapping("/city/{city}/language/{language}")
    @Operation(summary = "READ: Movies playing in a city filtered by language")
    public ResponseEntity<List<Movie>> getMoviesByCityAndLanguage(
            @PathVariable String city,
            @PathVariable Movie.Language language) {
        return ResponseEntity.ok(movieRepository.findMoviesPlayingInCityByLanguage(city, language));
    }

    @GetMapping("/genre/{genre}")
    @Operation(summary = "READ: Filter movies by genre")
    public ResponseEntity<List<Movie>> getMoviesByGenre(@PathVariable Movie.Genre genre) {
        return ResponseEntity.ok(movieRepository.findByGenre(genre));
    }
}
