package in.bhimashankar.airdrive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private String id;
    private String orderId;
    private String paymentId;
    private String planId;
    private String planName;
    private Integer amount;
    private String currency;
    private Integer creditsAdded;
    private String status; // SUCCESS, PENDING, FAILED, ERROR
    private String description;
    private LocalDateTime transactionDate;
    private String userEmail;
    private String userName;
}
