package com.xyz.moviebooking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "amenities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g. PARKING, FOOD_COURT, WHEELCHAIR_ACCESS, DOLBY_ATMOS

    private String description;
}
