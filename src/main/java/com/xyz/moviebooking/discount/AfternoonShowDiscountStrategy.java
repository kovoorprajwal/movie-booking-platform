package com.xyz.moviebooking.discount;

import org.springframework.stereotype.Component;

/**
 * Discount Rule: 20% off on all tickets for afternoon shows (12:00 PM - 4:59 PM).
 */
@Component
public class AfternoonShowDiscountStrategy implements DiscountStrategy {

    private static final double AFTERNOON_DISCOUNT_PERCENT = 20.0;

    @Override
    public boolean isApplicable(DiscountContext context) {
        return context.getShow() != null && context.getShow().isAfternoonShow();
    }

    @Override
    public DiscountResult calculate(DiscountContext context) {
        double totalBase = context.getNumberOfTickets() * context.getBaseAmountPerTicket();
        double discountAmount = totalBase * (AFTERNOON_DISCOUNT_PERCENT / 100.0);

        return DiscountResult.builder()
                .discountAmount(Math.round(discountAmount * 100.0) / 100.0)
                .discountPercentage(AFTERNOON_DISCOUNT_PERCENT)
                .description("20% discount on afternoon show (12 PM - 5 PM)")
                .build();
    }

    @Override
    public String getStrategyName() {
        return "AFTERNOON_SHOW_20_PERCENT";
    }
}
