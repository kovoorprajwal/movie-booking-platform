package com.xyz.moviebooking.discount;

import com.xyz.moviebooking.model.Show;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountEngineTest {

    private DiscountEngine discountEngine;
    private ThirdTicketDiscountStrategy thirdTicketStrategy;
    private AfternoonShowDiscountStrategy afternoonStrategy;

    @BeforeEach
    void setUp() {
        thirdTicketStrategy = new ThirdTicketDiscountStrategy();
        afternoonStrategy = new AfternoonShowDiscountStrategy();
        discountEngine = new DiscountEngine(List.of(thirdTicketStrategy, afternoonStrategy));
    }

    private Show buildShow(LocalTime startTime) {
        Show show = new Show();
        show.setStartTime(startTime);
        show.setEndTime(startTime.plusMinutes(120));
        show.setShowDate(LocalDate.now());
        show.setBasePrice(200.0);
        show.setStatus(Show.ShowStatus.SCHEDULED);
        return show;
    }

    @Test
    @DisplayName("No discount for 2 tickets on evening show")
    void noDiscount_eveningShow_twoTickets() {
        Show show = buildShow(LocalTime.of(19, 0));
        DiscountContext ctx = DiscountContext.builder()
                .numberOfTickets(2).baseAmountPerTicket(200.0).show(show).build();

        DiscountResult result = discountEngine.getBestDiscount(ctx);
        assertThat(result.getDiscountAmount()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("50% discount on 3rd ticket - exactly 3 tickets evening show")
    void thirdTicketDiscount_eveningShow_threeTickets() {
        Show show = buildShow(LocalTime.of(19, 0));
        DiscountContext ctx = DiscountContext.builder()
                .numberOfTickets(3).baseAmountPerTicket(200.0).show(show).build();

        DiscountResult result = discountEngine.getBestDiscount(ctx);
        assertThat(result.getDiscountAmount()).isEqualTo(100.0); // 50% of 200
        assertThat(result.getDescription()).contains("50%");
    }

    @Test
    @DisplayName("50% discount applies for 6 tickets (2 third-tickets)")
    void thirdTicketDiscount_sixTickets() {
        Show show = buildShow(LocalTime.of(19, 0));
        DiscountContext ctx = DiscountContext.builder()
                .numberOfTickets(6).baseAmountPerTicket(200.0).show(show).build();

        DiscountResult result = discountEngine.getBestDiscount(ctx);
        assertThat(result.getDiscountAmount()).isEqualTo(200.0); // 2 * 50% of 200
    }

    @Test
    @DisplayName("20% discount for afternoon show with 2 tickets")
    void afternoonDiscount_twoTickets() {
        Show show = buildShow(LocalTime.of(14, 0));
        DiscountContext ctx = DiscountContext.builder()
                .numberOfTickets(2).baseAmountPerTicket(200.0).show(show).build();

        DiscountResult result = discountEngine.getBestDiscount(ctx);
        assertThat(result.getDiscountAmount()).isEqualTo(80.0); // 20% of 400
        assertThat(result.getDescription()).contains("20%");
    }

    @Test
    @DisplayName("Best discount chosen: afternoon 20% vs 3rd ticket 50% for 3 tickets afternoon show")
    void bestDiscountChosen_afternoonShowThreeTickets() {
        Show show = buildShow(LocalTime.of(14, 0));
        DiscountContext ctx = DiscountContext.builder()
                .numberOfTickets(3).baseAmountPerTicket(200.0).show(show).build();

        // 3rd ticket discount = 100 (50% of 200)
        // Afternoon discount  = 120 (20% of 600)
        // Best = afternoon (120 > 100)
        DiscountResult result = discountEngine.getBestDiscount(ctx);
        assertThat(result.getDiscountAmount()).isEqualTo(120.0);
        assertThat(result.getDescription()).contains("afternoon");
    }

    @Test
    @DisplayName("All applicable discounts list returns both for afternoon show with 3+ tickets")
    void allApplicableDiscounts_returnsMultiple() {
        Show show = buildShow(LocalTime.of(13, 0));
        DiscountContext ctx = DiscountContext.builder()
                .numberOfTickets(3).baseAmountPerTicket(200.0).show(show).build();

        List<DiscountResult> results = discountEngine.getAllApplicableDiscounts(ctx);
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("Show.isAfternoonShow() returns true for 12:00-16:59")
    void afternoonShowBoundaryCheck() {
        assertThat(buildShow(LocalTime.of(12, 0)).isAfternoonShow()).isTrue();
        assertThat(buildShow(LocalTime.of(16, 59)).isAfternoonShow()).isTrue();
        assertThat(buildShow(LocalTime.of(17, 0)).isAfternoonShow()).isFalse();
        assertThat(buildShow(LocalTime.of(11, 59)).isAfternoonShow()).isFalse();
    }
}
