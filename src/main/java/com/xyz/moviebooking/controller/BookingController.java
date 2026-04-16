package com.xyz.moviebooking.controller;

import com.xyz.moviebooking.dto.BookingRequest;
import com.xyz.moviebooking.dto.BookingResponse;
import com.xyz.moviebooking.dto.SeatReservationRequest;
import com.xyz.moviebooking.dto.SeatReservationResponse;
import com.xyz.moviebooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Book and manage movie tickets - Write scenario with discount engine")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/reserve")
    @Operation(summary = "WRITE: Phase 1 — Reserve seats (10-min hold, optimistic locking)",
               description = "Temporarily locks selected seats for 10 minutes. Call /confirm after payment.")
    public ResponseEntity<SeatReservationResponse> reserveSeats(@Valid @RequestBody SeatReservationRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(bookingService.reserveSeats(request));
    }

    @PostMapping("/confirm")
    @Operation(summary = "WRITE: Phase 2 — Confirm booking after payment (applies discounts)",
               description = "Confirms booking for reserved seats. Seats must be reserved first via /reserve.")
    public ResponseEntity<BookingResponse> bookTickets(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookTickets(request));
    }

    @GetMapping("/{bookingReference}")
    @Operation(summary = "READ: Get booking details by reference")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingReference) {
        return ResponseEntity.ok(bookingService.getBooking(bookingReference));
    }

    @GetMapping
    @Operation(summary = "READ: Get all bookings for a customer by email")
    public ResponseEntity<List<BookingResponse>> getMyBookings(@RequestParam String email) {
        return ResponseEntity.ok(bookingService.getBookingsByEmail(email));
    }

    @DeleteMapping("/{bookingReference}/cancel")
    @Operation(summary = "WRITE: Cancel a booking and release seats")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable String bookingReference) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingReference));
    }
}
