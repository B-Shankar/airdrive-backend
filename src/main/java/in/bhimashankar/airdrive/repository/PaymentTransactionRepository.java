package in.bhimashankar.airdrive.repository;

import in.bhimashankar.airdrive.document.PaymentTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends MongoRepository<PaymentTransaction, String> {
    List<PaymentTransaction> findByClerkId(String clerkId);

    List<PaymentTransaction> findByClerkIdOrderByTransactionDateDesc(String clerkId);

    List<PaymentTransaction> findByClerkIdAndStatusOrderByTransactionDateDesc(String clerkId, String status);

    Optional<PaymentTransaction> findByOrderId(String orderId);

    Optional<PaymentTransaction> findByPaymentId(String paymentId);
}
