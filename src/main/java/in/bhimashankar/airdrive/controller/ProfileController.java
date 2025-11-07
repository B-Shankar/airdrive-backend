package in.bhimashankar.airdrive.controller;

import in.bhimashankar.airdrive.dto.ProfileDTO;
import in.bhimashankar.airdrive.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);
    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<?> registerProfile(@RequestBody ProfileDTO profileDTO) {
        log.info("Register request for clerkId: {}", profileDTO.getClerkId());

        boolean alreadyExists = profileService.existsByClerkId(profileDTO.getClerkId());

        ProfileDTO savedProfile = profileService.createProfile(profileDTO);

        HttpStatus status = alreadyExists ? HttpStatus.OK : HttpStatus.CREATED;

        return ResponseEntity.status(status).body(savedProfile);
    }

}
