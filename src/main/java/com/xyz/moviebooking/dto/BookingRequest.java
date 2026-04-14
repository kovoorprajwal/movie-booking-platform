package com.xyz.moviebooking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {

    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @Email(message = "Valid email is required")
    @NotBlank
    private String customerEmail;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Valid 10-digit Indian phone number required")
    private String customerPhone;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(max = 10, message = "Cannot book more than 10 seats at once")
    private List<Long> showSeatIds;
}
