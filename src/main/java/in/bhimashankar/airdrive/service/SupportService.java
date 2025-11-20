package in.bhimashankar.airdrive.service;

import in.bhimashankar.airdrive.document.ProfileDocument;
import in.bhimashankar.airdrive.document.SupportTicket;
import in.bhimashankar.airdrive.dto.SupportTicketDTO;
import in.bhimashankar.airdrive.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportService.class);

    private final SupportTicketRepository supportTicketRepository;
    private final ProfileService profileService;

    public SupportTicketDTO createTicket(SupportTicketDTO ticketDTO) {
        try {
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            String clerkId = currentProfile.getClerkId();

            // Generate unique ticket ID
            String ticketId = generateTicketId();

            // Determine priority based on category
            String priority = determinePriority(ticketDTO.getCategory());

            SupportTicket ticket = SupportTicket.builder()
                    .ticketId(ticketId)
                    .clerkId(clerkId)
                    .userEmail(ticketDTO.getUserEmail() != null ? ticketDTO.getUserEmail() : currentProfile.getEmail())
                    .userName(ticketDTO.getUserName() != null ? ticketDTO.getUserName() :
                            currentProfile.getFirstName() + " " + currentProfile.getLastName())
                    .category(ticketDTO.getCategory())
                    .subject(ticketDTO.getSubject())
                    .message(ticketDTO.getMessage())
                    .orderId(ticketDTO.getOrderId())
                    .paymentId(ticketDTO.getPaymentId())
                    .attemptedAmount(ticketDTO.getAttemptedAmount())
                    .status("OPEN")
                    .priority(priority)
                    .createdAt(LocalDateTime.now())
                    .build();

            supportTicketRepository.save(ticket);

            log.info("Support ticket created: {} for user: {}", ticketId, clerkId);

            return SupportTicketDTO.builder()
                    .ticketId(ticketId)
                    .status("OPEN")
                    .priority(priority)
                    .success(true)
                    .responseMessage("Support ticket created successfully. Ticket ID: " + ticketId)
                    .build();

        } catch (Exception e) {
            log.error("Error creating support ticket", e);
            return SupportTicketDTO.builder()
                    .success(false)
                    .responseMessage("Failed to create support ticket: " + e.getMessage())
                    .build();
        }
    }

    public List<SupportTicketDTO> getUserTickets() {
        try {
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            String clerkId = currentProfile.getClerkId();

            List<SupportTicket> tickets = supportTicketRepository.findByClerkIdOrderByCreatedAtDesc(clerkId);

            return tickets.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching user tickets", e);
            return List.of();
        }
    }

    public SupportTicketDTO getTicketByTicketId(String ticketId) {
        try {
            SupportTicket ticket = supportTicketRepository.findByTicketId(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            return convertToDTO(ticket);

        } catch (Exception e) {
            log.error("Error fetching ticket: {}", ticketId, e);
            return SupportTicketDTO.builder()
                    .success(false)
                    .responseMessage("Ticket not found")
                    .build();
        }
    }

    private String generateTicketId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = supportTicketRepository.count() + 1;
        return String.format("TKT-%s-%04d", datePart, count);
    }

    private String determinePriority(String category) {
        switch (category.toUpperCase()) {
            case "PAYMENT_FAILED":
            case "REFUND_REQUEST":
                return "HIGH";
            case "TECHNICAL_ISSUE":
                return "MEDIUM";
            case "GENERAL_QUERY":
            default:
                return "LOW";
        }
    }

    private SupportTicketDTO convertToDTO(SupportTicket ticket) {
        return SupportTicketDTO.builder()
                .ticketId(ticket.getTicketId())
                .category(ticket.getCategory())
                .subject(ticket.getSubject())
                .message(ticket.getMessage())
                .userEmail(ticket.getUserEmail())
                .userName(ticket.getUserName())
                .orderId(ticket.getOrderId())
                .paymentId(ticket.getPaymentId())
                .attemptedAmount(ticket.getAttemptedAmount())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .success(true)
                .build();
    }
}
