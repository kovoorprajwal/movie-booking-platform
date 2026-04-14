package com.xyz.moviebooking.repository;

import com.xyz.moviebooking.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(Movie.MovieStatus status);

    List<Movie> findByLanguage(Movie.Language language);

    List<Movie> findByGenre(Movie.Genre genre);

    @Query("SELECT DISTINCT s.movie FROM Show s WHERE s.screen.theatre.city = :city " +
           "AND s.status = 'SCHEDULED' AND s.showDate >= CURRENT_DATE")
    List<Movie> findMoviesPlayingInCity(@Param("city") String city);

    @Query("SELECT DISTINCT s.movie FROM Show s WHERE s.screen.theatre.city = :city " +
           "AND s.movie.language = :language AND s.status = 'SCHEDULED' AND s.showDate >= CURRENT_DATE")
    List<Movie> findMoviesPlayingInCityByLanguage(@Param("city") String city,
                                                   @Param("language") Movie.Language language);
}
