package com.danielerikssoncoder.cinema_project.service;

import com.danielerikssoncoder.cinema_project.dto.request.TicketRequest;
import com.danielerikssoncoder.cinema_project.dto.response.TicketResponse;
import com.danielerikssoncoder.cinema_project.entity.Customer;
import com.danielerikssoncoder.cinema_project.entity.Screening;
import com.danielerikssoncoder.cinema_project.entity.Ticket;
import com.danielerikssoncoder.cinema_project.exception.NotEnoughSeatsException;
import com.danielerikssoncoder.cinema_project.exception.ResourceNotFoundException;
import com.danielerikssoncoder.cinema_project.repository.CustomerRepository;
import com.danielerikssoncoder.cinema_project.repository.ScreeningRepository;
import com.danielerikssoncoder.cinema_project.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for tickets.
 * <p>
 * Available to USER and ADMIN (set in SecurityConfig).
 * <p>
 * The customerId is always set from the JWT by the controller, never from the client.
 */
@Service
@Transactional
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final ScreeningRepository screeningRepository;
    private final CurrencyService currencyService;

    public TicketService(TicketRepository ticketRepository,
                         CustomerRepository customerRepository,
                         ScreeningRepository screeningRepository,
                         CurrencyService currencyService) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.screeningRepository = screeningRepository;
        this.currencyService = currencyService;
    }

    /**
     * Returns all tickets for a customer as DTOs.
     *
     * @param customerId Customer's database ID
     * @return List of the customer's tickets
     */
    public List<TicketResponse> getTicketsByCustomerId(Long customerId) {
        return ticketRepository.findByCustomerId(customerId).stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }

    /**
     * Purchases tickets for a screening.
     * <p>
     * The screening is fetched with a pessimistic write lock (findByIdForUpdate)
     * to prevent race conditions: without the lock, two simultaneous purchases
     * could both read the same availableSeats value, pass the check, and overbooking.
     * <p>
     * The lock is held until the @Transactional transaction commits.
     *
     * @param request Ticket data (customerId set from JWT, screeningId, numberOfTickets)
     * @return The saved Ticket entity
     */
    public Ticket purchaseTicket(TicketRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        // Pessimistic lock prevents race conditions on availableSeats
        Screening screening = screeningRepository.findByIdForUpdate(request.getScreeningId())
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + request.getScreeningId()));

        if (screening.getAvailableSeats() < request.getNumberOfTickets()) {
            throw new NotEnoughSeatsException("Not enough available seats. Available: " + screening.getAvailableSeats());
        }

        double totalSek = screening.getPricePerTicket() * request.getNumberOfTickets();

        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setScreening(screening);
        ticket.setNumberOfTickets(request.getNumberOfTickets());
        ticket.setTotalPriceSek(totalSek);
        ticket.setTotalPriceUsd(currencyService.convertSekToUsd(totalSek));

        // Reduce available seats and save the screening
        screening.setAvailableSeats(screening.getAvailableSeats() - request.getNumberOfTickets());
        screeningRepository.save(screening);

        Ticket saved = ticketRepository.save(ticket);
        logger.info("user purchased {} ticket(s) for screening id {}", request.getNumberOfTickets(), screening.getId());
        return saved;
    }
}