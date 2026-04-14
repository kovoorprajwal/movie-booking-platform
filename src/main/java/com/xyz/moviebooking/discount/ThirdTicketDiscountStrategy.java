package com.xyz.moviebooking.discount;

import org.springframework.stereotype.Component;

/**
 * Discount Rule: 50% off on every 3rd ticket in a booking.
 * Example: 5 tickets → tickets #3 gets 50% off.
 */
@Component
public class ThirdTicketDiscountStrategy implements DiscountStrategy {

    private static final double THIRD_TICKET_DISCOUNT_PERCENT = 50.0;

    @Override
    public boolean isApplicable(DiscountContext context) {
        return context.getNumberOfTickets() >= 3;
    }

    @Override
    public DiscountResult calculate(DiscountContext context) {
        int tickets = context.getNumberOfTickets();
        double pricePerTicket = context.getBaseAmountPerTicket();

        int thirdTicketCount = tickets / 3;
        double discountAmount = thirdTicketCount * pricePerTicket * (THIRD_TICKET_DISCOUNT_PERCENT / 100.0);

        return DiscountResult.builder()
                .discountAmount(Math.round(discountAmount * 100.0) / 100.0)
                .discountPercentage(THIRD_TICKET_DISCOUNT_PERCENT)
                .description(String.format("50%% discount on every 3rd ticket (%d qualifying ticket(s))", thirdTicketCount))
                .build();
    }

    @Override
    public String getStrategyName() {
        return "THIRD_TICKET_50_PERCENT";
    }
}
