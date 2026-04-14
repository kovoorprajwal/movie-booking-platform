package com.xyz.moviebooking.dto;

import com.xyz.moviebooking.model.Show;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShowResponse {

    private Long showId;
    private Long movieId;
    private String movieTitle;
    private String language;
    private String genre;
    private Long theatreId;
    private String theatreName;
    private String city;
    private String screenName;
    private String screenType;
    private String showDate;
    private String startTime;
    private String endTime;
    private double basePrice;
    private long availableSeats;
    private long totalSeats;
    private Show.ShowStatus status;
    private boolean afternoonShow;
    private double afternoonDiscount;
}
