package com.xyz.moviebooking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SeatReservationResponse {

    private Long showId;
    private List<String> seatCodes;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private String message;
}
