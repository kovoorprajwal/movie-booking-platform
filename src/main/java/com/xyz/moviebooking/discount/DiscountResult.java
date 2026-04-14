package com.xyz.moviebooking.discount;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiscountResult {
    private double discountAmount;
    private String description;
    private double discountPercentage;

    public static DiscountResult noDiscount() {
        return DiscountResult.builder()
                .discountAmount(0.0)
                .description("No discount applicable")
                .discountPercentage(0.0)
                .build();
    }
}
