package in.bhimashankar.airdrive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FileMetadataDTO {
    private String id;
    private String name;
    private String type;
    private Long size;
    private String clerkId;
    private boolean isPublic;
    private String fileLocation;
    private LocalDateTime updatedAt;
}
