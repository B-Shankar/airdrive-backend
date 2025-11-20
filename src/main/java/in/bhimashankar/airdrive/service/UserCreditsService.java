package in.bhimashankar.airdrive.service;

import in.bhimashankar.airdrive.document.UserCredits;
import in.bhimashankar.airdrive.repository.UserCreditsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreditsService {

    private static final Logger log = LoggerFactory.getLogger(UserCreditsService.class);

    private final UserCreditsRepository userCreditsRepository;
    private final ProfileService profileService;

    public UserCredits createInitialCredits(String clerkId) {
        log.info("Updating the credits for user with clerkId: {}", clerkId);

        UserCredits userCredits = UserCredits.builder()
                .clerkId(clerkId)
                .credits(5)
                .plan("BASIC")
                .build();

        return userCreditsRepository.save(userCredits);
    }

    public UserCredits getUserCredits(String clerkId) {
        return userCreditsRepository.findByClerkId(clerkId)
                .orElseGet(() -> createInitialCredits(clerkId));
    }

    public UserCredits getUserCredits() {
        String clerkId = profileService.getCurrentProfile().getClerkId();
        return getUserCredits(clerkId);
    }

    public Boolean hasEnoughCredits(int requiredCredits) {
        UserCredits userCredits = getUserCredits();
        return userCredits.getCredits() >= requiredCredits;
    }

    public void consumeCredit() {
        UserCredits userCredits = getUserCredits();

        if (userCredits.getCredits() <= 0)
            return;

        userCredits.setCredits(userCredits.getCredits() - 1);
        userCreditsRepository.save(userCredits);
    }

    public UserCredits addCredits(String clerkId, int creditsToAdd, String plan) {
        UserCredits userCredits = userCreditsRepository.findByClerkId(clerkId)
                .orElseGet(() -> createInitialCredits(clerkId));

        userCredits.setCredits(userCredits.getCredits() + creditsToAdd);
        userCredits.setPlan(userCredits.getPlan());
        return userCreditsRepository.save(userCredits);
    }
}
