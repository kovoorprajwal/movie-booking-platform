package com.xyz.moviebooking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"screen_id", "row_label", "seat_number"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "row_label", nullable = false)
    private String rowLabel; // A, B, C ...

    @Column(name = "seat_number", nullable = false)
    private int seatNumber; // 1, 2, 3 ...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType;

    public String getSeatCode() {
        return rowLabel + seatNumber;
    }

    public enum SeatType { REGULAR, PREMIUM, VIP, RECLINER, COUPLE }
}
