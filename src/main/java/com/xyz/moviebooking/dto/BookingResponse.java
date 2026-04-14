package com.xyz.moviebooking.dto;

import com.xyz.moviebooking.model.Booking;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {

    private Long bookingId;
    private String bookingReference;
    private String movieTitle;
    private String theatreName;
    private String city;
    private String showDate;
    private String showTime;
    private String screenName;
    private String customerName;
    private String customerEmail;
    private List<String> seatCodes;
    private int numberOfTickets;
    private double baseAmount;
    private double discountAmount;
    private double finalAmount;
    private String discountDescription;
    private Booking.BookingStatus status;
    private LocalDateTime bookingTime;
}
