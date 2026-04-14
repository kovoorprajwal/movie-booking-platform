package com.xyz.moviebooking.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private Language language;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    private int durationMinutes;

    private double rating;

    private String description;

    private String posterUrl;

    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MovieStatus status = MovieStatus.ACTIVE;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Show> shows = new ArrayList<>();

    public enum Language { ENGLISH, HINDI, TAMIL, TELUGU, KANNADA, MALAYALAM, BENGALI, MARATHI }
    public enum Genre    { ACTION, COMEDY, DRAMA, THRILLER, HORROR, ROMANCE, SCI_FI, ANIMATION, DOCUMENTARY }
    public enum MovieStatus { ACTIVE, INACTIVE, COMING_SOON }
}
