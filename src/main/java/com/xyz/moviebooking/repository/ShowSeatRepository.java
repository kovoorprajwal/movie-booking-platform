package com.xyz.moviebooking.repository;

import com.xyz.moviebooking.model.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowId(Long showId);

    List<ShowSeat> findByShowIdAndStatus(Long showId, ShowSeat.SeatStatus status);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.id IN :seatIds AND ss.status = 'AVAILABLE'")
    List<ShowSeat> findAvailableSeats(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.id IN :seatIds " +
           "AND ss.status = 'LOCKED' AND ss.lockedByUserId = :userId")
    List<ShowSeat> findLockedSeatsByUser(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds,
                                         @Param("userId") String userId);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'AVAILABLE', ss.lockedAt = null, ss.lockedByUserId = null " +
           "WHERE ss.status = 'LOCKED' AND ss.lockedAt < :expiryTime")
    int releaseExpiredLocks(@Param("expiryTime") LocalDateTime expiryTime);
}
