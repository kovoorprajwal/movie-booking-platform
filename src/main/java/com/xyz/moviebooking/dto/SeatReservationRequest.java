package com.xyz.moviebooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SeatReservationRequest {

    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(max = 10, message = "Cannot reserve more than 10 seats at once")
    private List<Long> showSeatIds;

    @NotBlank(message = "User ID is required")
    private String userId;
}
