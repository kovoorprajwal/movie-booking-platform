package com.xyz.moviebooking.service;

import com.xyz.moviebooking.dto.ShowRequest;
import com.xyz.moviebooking.dto.ShowResponse;
import com.xyz.moviebooking.exception.BusinessException;
import com.xyz.moviebooking.exception.ResourceNotFoundException;
import com.xyz.moviebooking.model.*;
import com.xyz.moviebooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final ShowSeatRepository showSeatRepository;

    @Transactional(readOnly = true)
    public List<ShowResponse> getShowsByMovieAndCity(Long movieId, String city, LocalDate date) {
        List<Show> shows = showRepository.findShowsByMovieCityAndDate(movieId, city, date);
        return shows.stream().map(this::toShowResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowResponse> getShowsByMovieAndTheatre(Long movieId, Long theatreId, LocalDate date) {
        List<Show> shows = showRepository.findShowsByMovieTheatreAndDate(movieId, theatreId, date);
        return shows.stream().map(this::toShowResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowResponse> getShowsByTheatreAndDate(Long theatreId, LocalDate date) {
        List<Show> shows = showRepository.findByScreenTheatreIdAndShowDateAndStatus(theatreId, date, Show.ShowStatus.SCHEDULED);
        return shows.stream().map(this::toShowResponse).collect(Collectors.toList());
    }

    @Transactional
    public ShowResponse createShow(ShowRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + request.getMovieId()));

        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + request.getScreenId()));

        List<Show> existingShows = showRepository.findByScreenTheatreIdAndShowDateAndStatus(
                screen.getTheatre().getId(), request.getShowDate(), Show.ShowStatus.SCHEDULED);

        boolean hasConflict = existingShows.stream().anyMatch(existing ->
                existing.getScreen().getId().equals(screen.getId()) &&
                !(request.getStartTime().isAfter(existing.getEndTime()) ||
                  request.getStartTime().plusMinutes(movie.getDurationMinutes()).isBefore(existing.getStartTime())));

        if (hasConflict) {
            throw new BusinessException("Show time conflicts with an existing show on the same screen");
        }

        Show show = Show.builder()
                .movie(movie)
                .screen(screen)
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(request.getStartTime().plusMinutes(movie.getDurationMinutes()))
                .basePrice(request.getBasePrice())
                .status(Show.ShowStatus.SCHEDULED)
                .build();

        Show saved = showRepository.save(show);

        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : screen.getSeats()) {
            double seatPrice = calculateSeatPrice(request.getBasePrice(), seat.getSeatType());
            showSeats.add(ShowSeat.builder()
                    .show(saved)
                    .seat(seat)
                    .price(seatPrice)
                    .status(ShowSeat.SeatStatus.AVAILABLE)
                    .build());
        }
        showSeatRepository.saveAll(showSeats);

        return toShowResponse(saved);
    }

    @Transactional
    public ShowResponse updateShow(Long showId, ShowRequest request) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));

        if (show.getStatus() == Show.ShowStatus.CANCELLED) {
            throw new BusinessException("Cannot update a cancelled show");
        }

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + request.getMovieId()));

        show.setMovie(movie);
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getStartTime().plusMinutes(movie.getDurationMinutes()));
        show.setBasePrice(request.getBasePrice());

        return toShowResponse(showRepository.save(show));
    }

    @Transactional
    public void cancelShow(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));

        if (show.getStatus() == Show.ShowStatus.CANCELLED) {
            throw new BusinessException("Show is already cancelled");
        }

        show.setStatus(Show.ShowStatus.CANCELLED);
        showRepository.save(show);
    }

    @Transactional(readOnly = true)
    public List<ShowSeat> getAvailableSeats(Long showId) {
        return showSeatRepository.findByShowIdAndStatus(showId, ShowSeat.SeatStatus.AVAILABLE);
    }

    private double calculateSeatPrice(double basePrice, Seat.SeatType seatType) {
        return switch (seatType) {
            case REGULAR  -> basePrice;
            case PREMIUM  -> basePrice * 1.5;
            case VIP      -> basePrice * 2.0;
            case RECLINER -> basePrice * 2.5;
            case COUPLE   -> basePrice * 2.0;
        };
    }

    public ShowResponse toShowResponse(Show show) {
        long available = showRepository.countAvailableSeats(show.getId());
        return ShowResponse.builder()
                .showId(show.getId())
                .movieId(show.getMovie().getId())
                .movieTitle(show.getMovie().getTitle())
                .language(show.getMovie().getLanguage().name())
                .genre(show.getMovie().getGenre().name())
                .theatreId(show.getScreen().getTheatre().getId())
                .theatreName(show.getScreen().getTheatre().getName())
                .city(show.getScreen().getTheatre().getCity())
                .screenName(show.getScreen().getName())
                .screenType(show.getScreen().getScreenType().name())
                .showDate(show.getShowDate().format(DateTimeFormatter.ISO_DATE))
                .startTime(show.getStartTime().toString())
                .endTime(show.getEndTime().toString())
                .basePrice(show.getBasePrice())
                .availableSeats(available)
                .totalSeats(show.getScreen().getTotalSeats())
                .status(show.getStatus())
                .afternoonShow(show.isAfternoonShow())
                .afternoonDiscount(show.isAfternoonShow() ? 20.0 : 0.0)
                .build();
    }
}
