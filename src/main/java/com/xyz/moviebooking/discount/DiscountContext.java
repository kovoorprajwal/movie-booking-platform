package com.xyz.moviebooking.discount;

import com.xyz.moviebooking.model.Show;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiscountContext {
    private int numberOfTickets;
    private double baseAmountPerTicket;
    private Show show;
}
