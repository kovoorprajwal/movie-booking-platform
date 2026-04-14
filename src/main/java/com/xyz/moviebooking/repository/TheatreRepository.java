package com.xyz.moviebooking.repository;

import com.xyz.moviebooking.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    List<Theatre> findByCityIgnoreCaseAndActiveTrue(String city);

    @Query("SELECT DISTINCT s.screen.theatre FROM Show s " +
           "WHERE s.movie.id = :movieId AND s.showDate = :date AND s.status = 'SCHEDULED' " +
           "AND s.screen.theatre.active = true")
    List<Theatre> findTheatresShowingMovieOnDate(@Param("movieId") Long movieId,
                                                  @Param("date") LocalDate date);

    @Query("SELECT DISTINCT s.screen.theatre FROM Show s " +
           "WHERE s.movie.id = :movieId AND s.showDate = :date AND s.status = 'SCHEDULED' " +
           "AND s.screen.theatre.city = :city AND s.screen.theatre.active = true")
    List<Theatre> findTheatresShowingMovieInCityOnDate(@Param("movieId") Long movieId,
                                                        @Param("city") String city,
                                                        @Param("date") LocalDate date);
}
