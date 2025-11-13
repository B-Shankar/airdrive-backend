package in.bhimashankar.airdrive.service;

import in.bhimashankar.airdrive.document.FileMetadataDocument;
import in.bhimashankar.airdrive.document.ProfileDocument;
import in.bhimashankar.airdrive.dto.FileMetadataDTO;
import in.bhimashankar.airdrive.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private static final Logger log = LoggerFactory.getLogger(FileMetadataService.class);

    private final ProfileService profileService;
    private final UserCreditsService userCreditService;
    private final FileMetadataRepository fileMetadataRepository;

    public List<FileMetadataDTO> uploadFiles(MultipartFile[] files) throws IOException {
        log.info("Received request to upload {} file(s)", files.length);

        ProfileDocument profile = profileService.getCurrentProfile();
        if (profile == null) {
            log.error("Profile not found for the authenticated user");
            throw new IllegalStateException("Profile not found for the authenticated user");
        }

        if (!userCreditService.hasEnoughCredits(files.length)) {
            log.warn("Insufficient credits. Required: {}, Available: {}", files.length, userCreditService.getUserCredits().getCredits());
            throw new RuntimeException("Not enough credits to upload files. Please purchase more credits.");
        }

        Path uploadPath = Paths.get("upload").toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        log.debug("Upload directory created or already exists at {}", uploadPath);

        List<FileMetadataDocument> savedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            log.info("Processing file: {}", file.getOriginalFilename());

            String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + (fileExtension != null ? "." + fileExtension : "");

            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.debug("File copied to: {}", targetLocation);

            FileMetadataDocument fileMetadata = FileMetadataDocument.builder()
                    .fileLocation(targetLocation.toString())
                    .name(file.getOriginalFilename())
                    .size(file.getSize())
                    .type(file.getContentType())
                    .clerkId(profile.getClerkId())
                    .isPublic(false)
                    .updatedAt(LocalDateTime.now())
                    .build();

            userCreditService.consumeCredit();
            log.debug("Credit consumed for user: {}", profile.getClerkId());

            FileMetadataDocument saved = fileMetadataRepository.save(fileMetadata);
            log.info("File metadata saved: id={}, name={}, size={} bytes", saved.getId(), saved.getName(), saved.getSize());

            savedFiles.add(saved);
        }

        List<FileMetadataDTO> result = savedFiles.stream().map(this::mapToDTO).collect(Collectors.toList());
        log.info("File upload completed successfully for user: {} ({} file(s))", profile.getClerkId(), result.size());

        return result;
    }

    private FileMetadataDTO mapToDTO(FileMetadataDocument fileMetadataDocument) {
        return FileMetadataDTO.builder()
                .id(fileMetadataDocument.getId())
                .fileLocation(fileMetadataDocument.getFileLocation())
                .name(fileMetadataDocument.getName())
                .size(fileMetadataDocument.getSize())
                .type(fileMetadataDocument.getType())
                .clerkId(fileMetadataDocument.getClerkId())
                .isPublic(fileMetadataDocument.isPublic())
                .updatedAt(fileMetadataDocument.getUpdatedAt())
                .build();
    }

    public List<FileMetadataDTO> getFiles() {
        ProfileDocument profile = profileService.getCurrentProfile();
        if (profile == null) {
            log.error("Profile not found for the authenticated user");
            throw new IllegalStateException("Profile not found for the authenticated user");
        }

        List<FileMetadataDocument> files = fileMetadataRepository.findByClerkId(profile.getClerkId());
        log.info("Retrieved {} file(s) for user: {}", files.size(), profile.getClerkId());

        return files.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public FileMetadataDTO getPublicFile(String id) {
        Optional<FileMetadataDocument> fileOptional = fileMetadataRepository.findById(id);

        if (fileOptional.isEmpty() || !fileOptional.get().isPublic())
            throw new RuntimeException("Unable to get the file");

        FileMetadataDocument file = fileOptional.get();

        return mapToDTO(file);
    }

    public FileMetadataDTO getDownloadableFile(String id) {
        FileMetadataDocument file = fileMetadataRepository.findById(id).orElseThrow(() -> new RuntimeException("File Not Found"));

        return mapToDTO(file);
    }

    public void deleteFile(String id) {
        try {
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            FileMetadataDocument file = fileMetadataRepository.findById(id).orElseThrow(() -> new RuntimeException("File not found"));

            if (!file.getClerkId().equals(currentProfile.getClerkId()))
                throw new RuntimeException("File Not belongs to current user");

            Path filePath = Paths.get(file.getFileLocation());
            Files.deleteIfExists(filePath);

            fileMetadataRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting the file");
        }
    }

    public FileMetadataDTO togglePublic(String id) {
        FileMetadataDocument file = fileMetadataRepository.findById(id).orElseThrow(() -> new RuntimeException("File not found"));

        file.setPublic(!file.isPublic());
        fileMetadataRepository.save(file);
        return mapToDTO(file);
    }
}
