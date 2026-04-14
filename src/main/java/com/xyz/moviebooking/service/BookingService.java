package com.xyz.moviebooking.service;

import com.xyz.moviebooking.discount.DiscountContext;
import com.xyz.moviebooking.discount.DiscountEngine;
import com.xyz.moviebooking.discount.DiscountResult;
import com.xyz.moviebooking.dto.BookingRequest;
import com.xyz.moviebooking.dto.BookingResponse;
import com.xyz.moviebooking.exception.BusinessException;
import com.xyz.moviebooking.exception.ResourceNotFoundException;
import com.xyz.moviebooking.model.*;
import com.xyz.moviebooking.repository.BookingRepository;
import com.xyz.moviebooking.repository.ShowRepository;
import com.xyz.moviebooking.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public BookingResponse bookTickets(BookingRequest request) {
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + request.getShowId()));

        if (show.getStatus() != Show.ShowStatus.SCHEDULED) {
            throw new BusinessException("Show is not available for booking. Status: " + show.getStatus());
        }

        List<ShowSeat> selectedSeats = showSeatRepository.findAvailableSeatsForLock(
                request.getShowId(), request.getShowSeatIds());

        if (selectedSeats.size() != request.getShowSeatIds().size()) {
            throw new BusinessException("One or more selected seats are no longer available. Please choose different seats.");
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

        selectedSeats.forEach(seat -> {
            seat.setStatus(ShowSeat.SeatStatus.BOOKED);
            seat.setBooking(savedBooking);
        });
        showSeatRepository.saveAll(selectedSeats);

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
