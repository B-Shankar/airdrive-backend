package in.bhimashankar.airdrive.repository;

import in.bhimashankar.airdrive.document.SupportTicket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends MongoRepository<SupportTicket, String> {

    Optional<SupportTicket> findByTicketId(String ticketId);

    List<SupportTicket> findByClerkIdOrderByCreatedAtDesc(String clerkId);

    List<SupportTicket> findByStatusOrderByCreatedAtDesc(String status);

    List<SupportTicket> findByCategoryAndStatusOrderByCreatedAtDesc(String category, String status);
}
