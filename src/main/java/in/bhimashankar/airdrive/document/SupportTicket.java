package in.bhimashankar.airdrive.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "support_tickets")
public class SupportTicket {

    @Id
    private String id;

    @Indexed
    private String ticketId; // Auto-generated: TKT-20250120-001

    @Indexed
    private String clerkId;

    private String userEmail;
    private String userName;

    private String category; // PAYMENT_FAILED, TECHNICAL_ISSUE, GENERAL_QUERY, REFUND_REQUEST
    private String subject;
    private String message;

    // Payment related fields (optional)
    private String orderId;
    private String paymentId;
    private Integer attemptedAmount;

    @Indexed
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    private String priority; // LOW, MEDIUM, HIGH, URGENT

    private String adminResponse;
    private String adminId; // If assigned to admin

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
