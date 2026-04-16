package com.xyz.moviebooking.service;

import com.xyz.moviebooking.discount.DiscountContext;
import com.xyz.moviebooking.discount.DiscountEngine;
import com.xyz.moviebooking.discount.DiscountResult;
import com.xyz.moviebooking.dto.BookingRequest;
import com.xyz.moviebooking.dto.BookingResponse;
import com.xyz.moviebooking.dto.SeatReservationRequest;
import com.xyz.moviebooking.dto.SeatReservationResponse;
import com.xyz.moviebooking.exception.BusinessException;
import com.xyz.moviebooking.exception.ResourceNotFoundException;
import com.xyz.moviebooking.model.*;
import com.xyz.moviebooking.repository.BookingRepository;
import com.xyz.moviebooking.repository.ShowRepository;
import com.xyz.moviebooking.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final DiscountEngine discountEngine;

    @Transactional
    public SeatReservationResponse reserveSeats(SeatReservationRequest request) {
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + request.getShowId()));

        if (show.getStatus() != Show.ShowStatus.SCHEDULED) {
            throw new BusinessException("Show is not available for booking. Status: " + show.getStatus());
        }

        List<Long> sortedSeatIds = request.getShowSeatIds().stream().sorted().collect(Collectors.toList());
        List<ShowSeat> seats = showSeatRepository.findAvailableSeats(request.getShowId(), sortedSeatIds);

        if (seats.size() != request.getShowSeatIds().size()) {
            throw new BusinessException("One or more seats are not available. Please choose different seats.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(10);
        seats.forEach(seat -> {
            seat.setStatus(ShowSeat.SeatStatus.LOCKED);
            seat.setLockedAt(now);
            seat.setLockedByUserId(request.getUserId());
        });

        try {
            showSeatRepository.saveAll(seats);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException("One or more seats were just taken by another user. Please select different seats.");
        }

        return SeatReservationResponse.builder()
                .showId(request.getShowId())
                .seatCodes(seats.stream().map(ss -> ss.getSeat().getSeatCode()).collect(Collectors.toList()))
                .reservedAt(now)
                .expiresAt(expiresAt)
                .message("Seats reserved for 10 minutes. Complete payment to confirm booking.")
                .build();
    }

    @Transactional
    public BookingResponse bookTickets(BookingRequest request) {
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + request.getShowId()));

        if (show.getStatus() != Show.ShowStatus.SCHEDULED) {
            throw new BusinessException("Show is not available for booking. Status: " + show.getStatus());
        }

        List<ShowSeat> selectedSeats = showSeatRepository.findLockedSeatsByUser(
                request.getShowId(), request.getShowSeatIds(), request.getCustomerEmail());

        if (selectedSeats.size() != request.getShowSeatIds().size()) {
            throw new BusinessException("Seat reservation expired or not found. Please reserve seats again.");
        }

        double baseAmountPerTicket = show.getBasePrice();
        double totalBase = selectedSeats.stream().mapToDouble(ShowSeat::getPrice).sum();

        DiscountContext discountContext = DiscountContext.builder()
                .numberOfTickets(selectedSeats.size())
                .baseAmountPerTicket(baseAmountPerTicket)
                .show(show)
                .build();

        DiscountResult discount = discountEngine.getBestDiscount(discountContext);
        double finalAmount = Math.max(0, totalBase - discount.getDiscountAmount());

        String bookingRef = "XYZ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Booking booking = Booking.builder()
                .bookingReference(bookingRef)
                .show(show)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .numberOfTickets(selectedSeats.size())
                .baseAmount(totalBase)
                .discountAmount(discount.getDiscountAmount())
                .finalAmount(finalAmount)
                .discountDescription(discount.getDescription())
                .status(Booking.BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        try {
            selectedSeats.forEach(seat -> {
                seat.setStatus(ShowSeat.SeatStatus.BOOKED);
                seat.setLockedAt(null);
                seat.setLockedByUserId(null);
                seat.setBooking(savedBooking);
            });
            showSeatRepository.saveAll(selectedSeats);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException("Booking conflict detected. Please try again.");
        }

        return toBookingResponse(savedBooking, selectedSeats);
    }

    @Transactional
    public BookingResponse cancelBooking(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingReference));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BusinessException("Booking is already cancelled");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);

        List<ShowSeat> seats = showSeatRepository.findByShowId(booking.getShow().getId())
                .stream().filter(ss -> booking.equals(ss.getBooking())).collect(Collectors.toList());
        seats.forEach(ss -> {
            ss.setStatus(ShowSeat.SeatStatus.AVAILABLE);
            ss.setBooking(null);
        });
        showSeatRepository.saveAll(seats);

        bookingRepository.save(booking);
        return toBookingResponse(booking, seats);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingReference));
        List<ShowSeat> seats = showSeatRepository.findByShowId(booking.getShow().getId())
                .stream().filter(ss -> booking.equals(ss.getBooking())).collect(Collectors.toList());
        return toBookingResponse(booking, seats);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByEmail(String email) {
        return bookingRepository.findByCustomerEmail(email).stream()
                .map(b -> {
                    List<ShowSeat> seats = showSeatRepository.findByShowId(b.getShow().getId())
                            .stream().filter(ss -> b.equals(ss.getBooking())).collect(Collectors.toList());
                    return toBookingResponse(b, seats);
                }).collect(Collectors.toList());
    }

    private BookingResponse toBookingResponse(Booking booking, List<ShowSeat> seats) {
        Show show = booking.getShow();
        List<String> seatCodes = seats.stream()
                .map(ss -> ss.getSeat().getSeatCode())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .movieTitle(show.getMovie().getTitle())
                .theatreName(show.getScreen().getTheatre().getName())
                .city(show.getScreen().getTheatre().getCity())
                .showDate(show.getShowDate().format(DateTimeFormatter.ISO_DATE))
                .showTime(show.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .screenName(show.getScreen().getName())
                .customerName(booking.getCustomerName())
                .customerEmail(booking.getCustomerEmail())
                .seatCodes(seatCodes)
                .numberOfTickets(booking.getNumberOfTickets())
                .baseAmount(booking.getBaseAmount())
                .discountAmount(booking.getDiscountAmount())
                .finalAmount(booking.getFinalAmount())
                .discountDescription(booking.getDiscountDescription())
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .build();
    }
}
