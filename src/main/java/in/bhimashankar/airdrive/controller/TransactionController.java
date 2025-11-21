package in.bhimashankar.airdrive.controller;

import in.bhimashankar.airdrive.dto.TransactionDTO;
import in.bhimashankar.airdrive.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Get all transactions for the current user
     * @return List of transactions
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getUserTransactions() {
        List<TransactionDTO> transactions = transactionService.getUserTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions by status
     * @param status Transaction status (SUCCESS, PENDING, FAILED, ERROR)
     * @return List of filtered transactions
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByStatus(@PathVariable String status) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByStatus(status);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transaction details by ID
     * @param transactionId Transaction ID
     * @return Transaction details
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable String transactionId) {
        TransactionDTO transaction = transactionService.getTransactionById(transactionId);

        if (transaction != null)
            return ResponseEntity.ok(transaction);
        else
            return ResponseEntity.notFound().build();
    }

    /**
     * Download transaction receipt (future feature)
     * @param transactionId Transaction ID
     * @return Receipt data or file
     */
    @GetMapping("/{transactionId}/receipt")
    public ResponseEntity<?> downloadReceipt(@PathVariable String transactionId) {
        // TODO: Implement receipt generation
        return ResponseEntity.ok("Receipt for transaction: " + transactionId);
    }
}
