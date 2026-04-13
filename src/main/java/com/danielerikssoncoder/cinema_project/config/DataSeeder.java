package com.danielerikssoncoder.cinema_project.config;

import com.danielerikssoncoder.cinema_project.entity.*;
import com.danielerikssoncoder.cinema_project.repository.*;
import com.danielerikssoncoder.cinema_project.service.CurrencyService;
import com.danielerikssoncoder.cinema_project.service.KeycloakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Seeds the database on startup if it is empty.
 * <p>
 * Creates 5 customers, 5 movies, 3 rooms, 5 screenings and 2 bookings.
 * <p>
 * Skips seeding if data already exists.
 * <p>
 * Each customer gets a Keycloak account via KeycloakService with the
 * prefix "cinema-" (for example: cinema-c1).
 * <p>
 * The database username is still a readable name like firstname.lastname.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final CustomerRepository customerRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ScreeningRepository screeningRepository;
    private final BookingRepository bookingRepository;
    private final CurrencyService currencyService;
    private final KeycloakService keycloakService;

    /**
     * Spring automatically injects all dependencies via the constructor.
     * <p>
     * We use constructor-injection instead of @Autowired, it is the recommended way in Spring.
     */
    public DataSeeder(CustomerRepository customerRepository,
                      MovieRepository movieRepository,
                      RoomRepository roomRepository,
                      ScreeningRepository screeningRepository,
                      BookingRepository bookingRepository,
                      CurrencyService currencyService,
                      KeycloakService keycloakService) {
        this.customerRepository = customerRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.screeningRepository = screeningRepository;
        this.bookingRepository = bookingRepository;
        this.currencyService = currencyService;
        this.keycloakService = keycloakService;
    }

    /**
     * Runs once on startup via CommandLineRunner.
     * Does nothing if the database already has customers.
     */
    @Override
    public void run(String... args) {

        if (customerRepository.count() > 0) {
            logger.info("Database already seeded, skipping...");
            return;
        }

        logger.info("Seeding database...");

        // Customers with Keycloak accounts (cinema-c1 to cinema-c5)
        Customer c1 = createCustomerWithKeycloak("c1", "password", "lars@test.com", "Lars", "Johansson", "Drottninggatan 5", "Stockholm", "11151", "Sweden");
        Customer c2 = createCustomerWithKeycloak("c2", "password", "anna@test.com", "Anna", "Svensson", "Storgatan 12", "Stockholm", "11122", "Sweden");
        Customer c3 = createCustomerWithKeycloak("c3", "password", "erik@test.com", "Erik", "Lindgren", "Kyrkogatan 3", "Göteborg", "41101", "Sweden");
        Customer c4 = createCustomerWithKeycloak("c4", "password", "maja@test.com", "Maja", "Karlsson", "Parkvägen 7", "Malmö", "21120", "Sweden");
        Customer c5 = createCustomerWithKeycloak("c5", "password", "oscar@test.com", "Oscar", "Berg", "Industrigatan 22", "Västerås", "72213", "Sweden");

        // Movies
        Movie m1 = movieRepository.save(new Movie("The Matrix", "Sci-Fi", 136, 15));
        Movie m2 = movieRepository.save(new Movie("Inception", "Sci-Fi", 148, 11));
        Movie m3 = movieRepository.save(new Movie("The Lion King", "Animation", 88, 0));
        Movie m4 = movieRepository.save(new Movie("Pulp Fiction", "Crime", 154, 15));
        Movie m5 = movieRepository.save(new Movie("Interstellar", "Sci-Fi", 169, 11));

        // Rooms
        Room r1 = roomRepository.save(new Room("Salong 1", 150, "Dolby Atmos, 4K Projector"));
        Room r2 = roomRepository.save(new Room("Salong 2", 80, "Surround Sound, 4K Projector"));
        Room r3 = roomRepository.save(new Room("VIP-salongen", 30, "Dolby Atmos, IMAX, Recliner Seats"));

        // Screenings use relative dates so they are always in the future
        LocalDate today = LocalDate.now();
        screeningRepository.save(new Screening(m1, r1, today.plusDays(1), LocalTime.of(18, 0), 149.0));
        screeningRepository.save(new Screening(m2, r2, today.plusDays(1), LocalTime.of(19, 30), 149.0));
        screeningRepository.save(new Screening(m3, r1, today.plusDays(2), LocalTime.of(14, 0), 129.0));
        screeningRepository.save(new Screening(m4, r3, today.plusDays(2), LocalTime.of(21, 0), 199.0));
        screeningRepository.save(new Screening(m5, r2, today.plusDays(3), LocalTime.of(17, 0), 169.0));

        // Fixed room booking price, converted to USD via shared-lib
        double bookingPriceSek = 5000.0;

        // Booking 1: (c1) books the VIP lounge for a private viewing
        Booking b1 = new Booking();
        b1.setCustomer(c1);
        b1.setRoom(r3);
        b1.setDate(today.plusDays(7));
        b1.setNumberOfGuests(25);
        b1.setPerformance("Private screening: The Matrix");
        b1.setTechnicalEquipment("Dolby Atmos, IMAX");
        b1.setTotalPriceSek(bookingPriceSek);
        b1.setTotalPriceUsd(currencyService.convertSekToUsd(bookingPriceSek));
        bookingRepository.save(b1);

        // Booking 2: (c2) books Salon 1 for a company presentation
        Booking b2 = new Booking();
        b2.setCustomer(c2);
        b2.setRoom(r1);
        b2.setDate(today.plusDays(14));
        b2.setNumberOfGuests(100);
        b2.setPerformance("Company presentation by guest speaker");
        b2.setTechnicalEquipment("Dolby Atmos, 4K Projector, Microphone");
        b2.setTotalPriceSek(bookingPriceSek);
        b2.setTotalPriceUsd(currencyService.convertSekToUsd(bookingPriceSek));
        bookingRepository.save(b2);

        logger.info("Database seeded with 5 customers, 5 movies, 3 rooms, 5 screenings, 2 bookings");
    }

    /**
     * Creates a customer in the database and a matching Keycloak account.
     * <p>
     * The Keycloak username is built as "cinema-" + suffix (e.g. "cinema-c1").
     * The database username is set to firstname.lastname in lowercase.
     *
     * @param keycloakSuffix Suffix after "cinema-", e.g. "c1"
     * @param password Password for the Keycloak account
     * @param email Email address
     * @param firstName First name
     * @param lastName Last name
     * @param street Street address
     * @param city City
     * @param zipCode Zip code
     * @param country Country
     * @return The saved customer
     */
    private Customer createCustomerWithKeycloak(String keycloakSuffix, String password, String email,
                                                String firstName, String lastName, String street,
                                                String city, String zipCode, String country) {
        String username = firstName.toLowerCase() + "." + lastName.toLowerCase();
        String keycloakId = keycloakService.createUser("cinema-" + keycloakSuffix, password, email, firstName, lastName);

        Customer customer = new Customer(username, firstName, lastName);
        Address address = new Address(street, city, zipCode, country);
        address.setCustomer(customer);
        customer.getAddresses().add(address);
        customer.setKeycloakId(keycloakId);
        // Stored so we can calculate the next number when new customers are created
        customer.setKeycloakUsername("cinema-" + keycloakSuffix);
        return customerRepository.save(customer);
    }
}