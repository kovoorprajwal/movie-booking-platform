package com.xyz.moviebooking.config;

import com.xyz.moviebooking.model.*;
import com.xyz.moviebooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Seeding sample data...");

        // --- Movies ---
        Movie movie1 = movieRepository.save(Movie.builder()
                .title("Inception").language(Movie.Language.ENGLISH).genre(Movie.Genre.SCI_FI)
                .durationMinutes(148).rating(8.8).description("A thief who steals corporate secrets through dream-sharing technology.")
                .releaseDate(LocalDate.of(2024, 3, 15)).status(Movie.MovieStatus.ACTIVE).build());

        Movie movie2 = movieRepository.save(Movie.builder()
                .title("Kalki 2898 AD").language(Movie.Language.TELUGU).genre(Movie.Genre.ACTION)
                .durationMinutes(181).rating(7.5).description("A futuristic dystopian mythology sci-fi action film.")
                .releaseDate(LocalDate.of(2024, 6, 27)).status(Movie.MovieStatus.ACTIVE).build());

        Movie movie3 = movieRepository.save(Movie.builder()
                .title("Animal").language(Movie.Language.HINDI).genre(Movie.Genre.DRAMA)
                .durationMinutes(201).rating(6.9).description("A man reconnects with his father in an action-drama saga.")
                .releaseDate(LocalDate.of(2023, 12, 1)).status(Movie.MovieStatus.ACTIVE).build());

        // --- Theatres ---
        Theatre pvr = theatreRepository.save(Theatre.builder()
                .name("PVR Cinemas - Phoenix").city("Mumbai").address("Phoenix Mills, Lower Parel")
                .pincode("400013").contactEmail("pvr.phoenix@xyz.com").contactPhone("9876543210").active(true).build());

        Theatre inox = theatreRepository.save(Theatre.builder()
                .name("INOX - GVK One").city("Hyderabad").address("GVK One Mall, Banjara Hills")
                .pincode("500034").contactEmail("inox.gvk@xyz.com").contactPhone("9876543211").active(true).build());

        Theatre cinepolis = theatreRepository.save(Theatre.builder()
                .name("Cinepolis - DLF").city("Delhi").address("DLF Mall of India, Noida")
                .pincode("201301").contactEmail("cinepolis.dlf@xyz.com").contactPhone("9876543212").active(true).build());

        // --- Screens ---
        Screen screen1 = buildAndSaveScreen("IMAX Screen", pvr, Screen.ScreenType.IMAX, 6, 20);
        Screen screen2 = buildAndSaveScreen("Screen 2", pvr, Screen.ScreenType.DOLBY, 5, 15);
        Screen screen3 = buildAndSaveScreen("Audi 1", inox, Screen.ScreenType.STANDARD, 8, 18);
        Screen screen4 = buildAndSaveScreen("Screen 1", cinepolis, Screen.ScreenType.FOUR_DX, 4, 12);

        // --- Shows (today's date for demo) ---
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        createShowWithSeats(movie1, screen1, today, LocalTime.of(10, 0), 350.0);   // Morning
        createShowWithSeats(movie1, screen1, today, LocalTime.of(14, 0), 350.0);   // Afternoon (20% off)
        createShowWithSeats(movie1, screen2, today, LocalTime.of(19, 30), 250.0);  // Evening
        createShowWithSeats(movie2, screen3, today, LocalTime.of(11, 0), 200.0);   // Morning
        createShowWithSeats(movie2, screen3, today, LocalTime.of(15, 30), 200.0);  // Afternoon (20% off)
        createShowWithSeats(movie3, screen4, today, LocalTime.of(18, 0), 180.0);   // Evening
        createShowWithSeats(movie1, screen4, tomorrow, LocalTime.of(13, 0), 200.0); // Tomorrow afternoon

        log.info("Sample data seeded successfully. {} movies, {} theatres, {} shows ready.",
                movieRepository.count(), theatreRepository.count(), showRepository.count());
    }

    private Screen buildAndSaveScreen(String name, Theatre theatre, Screen.ScreenType type,
                                      int rows, int seatsPerRow) {
        Screen screen = Screen.builder()
                .name(name).theatre(theatre).screenType(type)
                .totalSeats(rows * seatsPerRow).build();
        Screen saved = screenRepository.save(screen);

        List<Seat> seats = new ArrayList<>();
        String[] rowLabels = {"A","B","C","D","E","F","G","H"};
        for (int r = 0; r < rows; r++) {
            String row = rowLabels[r];
            Seat.SeatType seatType = r < 2 ? Seat.SeatType.REGULAR
                    : r < 4 ? Seat.SeatType.PREMIUM : Seat.SeatType.VIP;
            for (int s = 1; s <= seatsPerRow; s++) {
                seats.add(Seat.builder().screen(saved).rowLabel(row).seatNumber(s).seatType(seatType).build());
            }
        }
        List<Seat> savedSeats = seatRepository.saveAll(seats);
        saved.setSeats(savedSeats);
        return saved;
    }

    private void createShowWithSeats(Movie movie, Screen screen, LocalDate date,
                                     LocalTime startTime, double basePrice) {
        Show show = showRepository.save(Show.builder()
                .movie(movie).screen(screen).showDate(date)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(movie.getDurationMinutes()))
                .basePrice(basePrice).status(Show.ShowStatus.SCHEDULED).build());

        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : screen.getSeats()) {
            double price = switch (seat.getSeatType()) {
                case REGULAR  -> basePrice;
                case PREMIUM  -> basePrice * 1.5;
                case VIP      -> basePrice * 2.0;
                case RECLINER -> basePrice * 2.5;
                case COUPLE   -> basePrice * 2.0;
            };
            showSeats.add(ShowSeat.builder().show(show).seat(seat).price(price)
                    .status(ShowSeat.SeatStatus.AVAILABLE).build());
        }
        showSeatRepository.saveAll(showSeats);
    }
}
