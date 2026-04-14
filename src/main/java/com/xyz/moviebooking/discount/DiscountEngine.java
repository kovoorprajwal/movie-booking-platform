package com.xyz.moviebooking.discount;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Discount Engine - evaluates all applicable strategies and picks the best discount.
 * Uses best-of-applicable strategy: customer always gets the maximum benefit.
 */
@Component
public class DiscountEngine {

    private final List<DiscountStrategy> strategies;

    public DiscountEngine(List<DiscountStrategy> strategies) {
        this.strategies = strategies;
    }

    public DiscountResult getBestDiscount(DiscountContext context) {
        return strategies.stream()
                .filter(strategy -> strategy.isApplicable(context))
                .map(strategy -> strategy.calculate(context))
                .max(java.util.Comparator.comparingDouble(DiscountResult::getDiscountAmount))
                .orElse(DiscountResult.noDiscount());
    }

    public List<DiscountResult> getAllApplicableDiscounts(DiscountContext context) {
        return strategies.stream()
                .filter(strategy -> strategy.isApplicable(context))
                .map(strategy -> strategy.calculate(context))
                .toList();
    }
}
