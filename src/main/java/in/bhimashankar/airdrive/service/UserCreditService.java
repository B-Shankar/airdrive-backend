package in.bhimashankar.airdrive.service;

import in.bhimashankar.airdrive.document.UserCredits;
import in.bhimashankar.airdrive.repository.UserCreditsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreditService {

    private static final Logger log = LoggerFactory.getLogger(UserCreditService.class);

    private final UserCreditsRepository userCreditsRepository;

    public UserCredits createInitialCredits(String clerkId) {
        log.info("Updating the credits for user with clerkId: {}", clerkId);

        UserCredits userCredits = UserCredits.builder()
                .clerkId(clerkId)
                .credits(5)
                .plan("BASIC")
                .build();

        return userCreditsRepository.save(userCredits);
    }
}
