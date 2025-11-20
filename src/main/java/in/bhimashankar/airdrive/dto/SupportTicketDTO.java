package in.bhimashankar.airdrive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketDTO {

    // Request fields
    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Subject is required")
    @Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(min = 10, max = 2000, message = "Message must be between 10 and 2000 characters")
    private String message;

    @Email(message = "Valid email is required")
    private String userEmail;

    private String userName;

    // Optional payment-related fields
    private String orderId;
    private String paymentId;
    private Integer attemptedAmount;

    // Response fields
    private String ticketId;
    private String status;
    private String priority;
    private Boolean success;
    private String responseMessage;
}
