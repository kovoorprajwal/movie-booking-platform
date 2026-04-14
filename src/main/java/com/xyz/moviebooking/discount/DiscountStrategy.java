package com.xyz.moviebooking.discount;

/**
 * Strategy Pattern Interface for Discount Calculation.
 * Allows plugging in new discount rules without modifying existing code (Open/Closed Principle).
 */
public interface DiscountStrategy {

    boolean isApplicable(DiscountContext context);

    DiscountResult calculate(DiscountContext context);

    String getStrategyName();
}
