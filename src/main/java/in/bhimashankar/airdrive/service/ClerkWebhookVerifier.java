package in.bhimashankar.airdrive.service;

import com.svix.Webhook;
import com.svix.exceptions.WebhookVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ClerkWebhookVerifier {

    @Value("${clerk.webhook.secret}")
    private String webhookSecret;

    public boolean verifyWebhookSignature(String svixId, String svixSignature, String svixTimestamp, String payload) {
        try {
            // Create Webhook instance
            Webhook webhook = new Webhook(webhookSecret);

            // Build java.net.http.HttpHeaders using factory method
            HttpHeaders headers = HttpHeaders.of(
                    Map.of(
                            "svix-id", List.of(svixId),
                            "svix-signature", List.of(svixSignature),
                            "svix-timestamp", List.of(svixTimestamp)
                    ),
                    (key, value) -> true // accept all headers
            );

            // Verify the webhook
            webhook.verify(payload, headers);

            return true;
        } catch (WebhookVerificationException e) {
            log.error("Invalid Clerk webhook signature: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error verifying Clerk webhook: {}", e.getMessage());
            return false;
        }
    }
}
