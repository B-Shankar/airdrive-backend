package in.bhimashankar.airdrive.service;

import in.bhimashankar.airdrive.document.ProfileDocument;
import in.bhimashankar.airdrive.dto.ProfileDTO;
import in.bhimashankar.airdrive.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;

    public ProfileDTO createProfile(ProfileDTO profileDTO) {

        if (profileDTO.getClerkId() != null && !profileDTO.getClerkId().isEmpty() && existsByClerkId(profileDTO.getClerkId())) {
            return updateProfile(profileDTO);
        }

        log.info("Creating new profile for clerkId: {}", profileDTO.getClerkId());

        ProfileDocument profile = ProfileDocument.builder()
                .clerkId(profileDTO.getClerkId())
                .email(profileDTO.getEmail())
                .firstName(profileDTO.getFirstName())
                .lastName(profileDTO.getLastName())
                .photoUrl(profileDTO.getPhotoUrl())
                .credits(5)
                .build();

        ProfileDocument savedProfile = profileRepository.save(profile);
        log.info("Profile saved successfully for clerkId: {}", savedProfile.getClerkId());

        return ProfileDTO.builder()
                .id(savedProfile.getId())
                .clerkId(savedProfile.getClerkId())
                .email(savedProfile.getEmail())
                .firstName(savedProfile.getFirstName())
                .lastName(savedProfile.getLastName())
                .photoUrl(savedProfile.getPhotoUrl())
                .credits(savedProfile.getCredits())
                .createdAt(savedProfile.getCreatedAt())
                .build();
    }

    public ProfileDTO updateProfile(ProfileDTO profileDTO) {
        log.info("Updating new profile: {}", profileDTO.getClerkId());

        Optional<ProfileDocument> existingProfile = profileRepository.findByClerkId(profileDTO.getClerkId());

        if (existingProfile.isPresent()) {

            ProfileDocument updateProfile = existingProfile.get();

            if (profileDTO.getEmail() != null && !profileDTO.getEmail().isEmpty())
                updateProfile.setEmail(profileDTO.getEmail());

            if (profileDTO.getFirstName() != null && !profileDTO.getFirstName().isEmpty())
                updateProfile.setFirstName(profileDTO.getFirstName());

            if (profileDTO.getLastName() != null && !profileDTO.getLastName().isEmpty())
                updateProfile.setLastName(profileDTO.getLastName());

            if (profileDTO.getPhotoUrl() != null && !profileDTO.getPhotoUrl().isEmpty())
                updateProfile.setPhotoUrl(profileDTO.getPhotoUrl());

            ProfileDocument updatedProfile = profileRepository.save(updateProfile);
            log.info("Profile updated successfully for clerkId: {}", updatedProfile.getClerkId());

            return ProfileDTO.builder()
                    .id(updatedProfile.getId())
                    .clerkId(updatedProfile.getClerkId())
                    .email(updatedProfile.getEmail())
                    .firstName(updatedProfile.getFirstName())
                    .lastName(updatedProfile.getLastName())
                    .photoUrl(updatedProfile.getPhotoUrl())
                    .credits(updatedProfile.getCredits())
                    .createdAt(updatedProfile.getCreatedAt())
                    .build();
        }
        log.warn("Profile not found for clerkId: {}", profileDTO.getClerkId());
        return null;
    }

    public boolean existsByClerkId(String clerkId) {
        return profileRepository.existsByClerkId(clerkId);
    }

    public void deleteProfile(String clerkId) {
        Optional<ProfileDocument> existingProfile = profileRepository.findByClerkId(clerkId);

        existingProfile.ifPresent(profileRepository::delete);
    }
}
