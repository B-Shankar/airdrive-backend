package in.bhimashankar.airdrive.service;

import in.bhimashankar.airdrive.document.PaymentTransaction;
import in.bhimashankar.airdrive.document.ProfileDocument;
import in.bhimashankar.airdrive.dto.TransactionDTO;
import in.bhimashankar.airdrive.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ProfileService profileService;

    public List<TransactionDTO> getUserTransactions() {
        try {
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            String clerkId = currentProfile.getClerkId();

            List<PaymentTransaction> transactions = paymentTransactionRepository
                    .findByClerkIdAndStatusOrderByTransactionDateDesc(clerkId, "SUCCESS");

            return transactions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching user transactions", e);
            return List.of();
        }
    }

    public List<TransactionDTO> getTransactionsByStatus(String status) {
        try {
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            String clerkId = currentProfile.getClerkId();

            List<PaymentTransaction> transactions = paymentTransactionRepository
                    .findByClerkIdAndStatusOrderByTransactionDateDesc(clerkId, status.toUpperCase());

            return transactions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching transactions by status: {}", status, e);
            return List.of();
        }
    }

    public TransactionDTO getTransactionById(String transactionId) {
        try {
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            String clerkId = currentProfile.getClerkId();

            return paymentTransactionRepository.findById(transactionId)
                    .filter(transaction -> transaction.getClerkId().equals(clerkId))
                    .map(this::convertToDTO)
                    .orElse(null);

        } catch (Exception e) {
            log.error("Error fetching transaction by ID: {}", transactionId, e);
            return null;
        }
    }

    private TransactionDTO convertToDTO(PaymentTransaction transaction) {
        String planName = getPlanName(transaction.getPlanId());
        String description = planName + " - " + transaction.getCreditsAdded() + " Credits";

        return TransactionDTO.builder()
                .id(transaction.getId())
                .orderId(transaction.getOrderId())
                .paymentId(transaction.getPaymentId())
                .planId(transaction.getPlanId())
                .planName(planName)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .creditsAdded(transaction.getCreditsAdded())
                .status(transaction.getStatus())
                .description(description)
                .transactionDate(transaction.getTransactionDate())
                .userEmail(transaction.getUserEmail())
                .userName(transaction.getUserName())
                .build();
    }

    private String getPlanName(String planId) {
        if (planId == null) return "Unknown Plan";

        switch (planId.toLowerCase()) {
            case "premium":
                return "Premium";
            case "ultimate":
                return "Ultimate";
            default:
                return "Basic";
        }
    }
}
