package in.bhimashankar.airdrive.repository;

import in.bhimashankar.airdrive.document.UserCredits;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserCreditsRepository extends MongoRepository<UserCredits, String> {
}
