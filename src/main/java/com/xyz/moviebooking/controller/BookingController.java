package com.xyz.moviebooking.controller;

import com.xyz.moviebooking.dto.BookingRequest;
import com.xyz.moviebooking.dto.BookingResponse;
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

    @PostMapping
    @Operation(summary = "WRITE: Book movie tickets (applies discounts: 3rd ticket 50% off / afternoon 20% off)",
               description = "Books selected seats for a show. Discount engine auto-applies best available offer.")
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
