package com.xyz.moviebooking.repository;

import com.xyz.moviebooking.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByMovieIdAndShowDateAndStatus(Long movieId, LocalDate date, Show.ShowStatus status);

    List<Show> findByScreenTheatreIdAndShowDateAndStatus(Long theatreId, LocalDate date, Show.ShowStatus status);

    @Query("SELECT s FROM Show s WHERE s.movie.id = :movieId " +
           "AND s.screen.theatre.id = :theatreId AND s.showDate = :date AND s.status = 'SCHEDULED'")
    List<Show> findShowsByMovieTheatreAndDate(@Param("movieId") Long movieId,
                                              @Param("theatreId") Long theatreId,
                                              @Param("date") LocalDate date);

    @Query("SELECT s FROM Show s WHERE s.movie.id = :movieId " +
           "AND s.screen.theatre.city = :city AND s.showDate = :date AND s.status = 'SCHEDULED'")
    List<Show> findShowsByMovieCityAndDate(@Param("movieId") Long movieId,
                                           @Param("city") String city,
                                           @Param("date") LocalDate date);

    @Query("SELECT COUNT(ss) FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.status = 'AVAILABLE'")
    long countAvailableSeats(@Param("showId") Long showId);
}
