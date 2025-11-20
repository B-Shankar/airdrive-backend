package in.bhimashankar.airdrive.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import in.bhimashankar.airdrive.document.PaymentTransaction;
import in.bhimashankar.airdrive.document.ProfileDocument;
import in.bhimashankar.airdrive.document.UserCredits;
import in.bhimashankar.airdrive.dto.PaymentDTO;
import in.bhimashankar.airdrive.dto.PaymentVerificationDTO;
import in.bhimashankar.airdrive.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;


    public PaymentDTO createOrder(PaymentDTO paymentDTO) {
        try {
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            String clerkId = currentProfile.getClerkId();

            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", paymentDTO.getAmount());
            orderRequest.put("currency", paymentDTO.getCurrency());
            orderRequest.put("receipt", "order_"+System.currentTimeMillis());

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");

            //create pending transaction record
            PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                    .clerkId(clerkId)
                    .planId(paymentDTO.getPlanId())
                    .amount(paymentDTO.getAmount())
                    .currency(paymentDTO.getCurrency())
                    .orderId(orderId)
                    .status("PENDING")
                    .transactionDate(LocalDateTime.now())
                    .userEmail(currentProfile.getEmail())
                    .userName(currentProfile.getFirstName() + " " + currentProfile.getLastName())
                    .build();

            paymentTransactionRepository.save(paymentTransaction);

            return PaymentDTO.builder()
                    .orderId(orderId)
                    .success(true)
                    .message("Order created successfully")
                    .build();
        } catch (Exception e) {
            return PaymentDTO.builder()
                            .success(false)
                            .message("Error creating order: " + e.getMessage())
                            .build();
        }
    }

    public PaymentDTO verifyPayment(PaymentVerificationDTO request) {
        try {

            ProfileDocument currentProfile = profileService.getCurrentProfile();
            String clerkId = currentProfile.getClerkId();

            String data = request.getRazorpay_order_id() + "|" + request.getRazorpay_payment_id();
            String generatedSignature = generateHmacSha256Signature(data, razorpayKeySecret);

            if(!generatedSignature.equals(request.getRazorpay_signature())) {
                updateTransactionStatus(request.getRazorpay_order_id(), "FAILED", request.getRazorpay_payment_id(), null);
                return PaymentDTO.builder()
                        .success(false)
                        .message("Payment signature verification failed")
                        .build();
            }

            //Add credits based on plan
            int creditsToAdd = 0;
            String plan = "BASIC";

            switch (request.getPlanId()) {
                case "premium":
                    creditsToAdd = 500;
                    plan = "PREMIUM";
                    break;
                case "ultimate":
                    creditsToAdd = 5000;
                    plan = "ULTIMATE";
                    break;
            }

            if (creditsToAdd > 0) {
                UserCredits userCredits = userCreditsService.addCredits(clerkId, creditsToAdd, plan);
                updateTransactionStatus(request.getRazorpay_order_id(), "SUCCESS", request.getRazorpay_payment_id(), creditsToAdd);
                return PaymentDTO.builder()
                        .success(true)
                        .message("Payment verified and credits added successfully")
                        .credits(userCreditsService.getUserCredits(clerkId).getCredits())
                        .build();
            } else {
                updateTransactionStatus(request.getRazorpay_order_id(), "FAILED", request.getRazorpay_payment_id(), creditsToAdd);
                return PaymentDTO.builder()
                        .success(false)
                        .message("Invalid plan selected")
                        .build();
            }
        } catch (Exception e) {
            try {
                updateTransactionStatus(request.getRazorpay_order_id(), "ERROR", request.getRazorpay_payment_id(), null);
            } catch (RuntimeException ex) {
                throw new RuntimeException(ex);
            }

            return PaymentDTO.builder()
                    .success(false)
                    .message("Error verifying payment:" + e.getMessage())
                    .build();
        }
    }

    private void updateTransactionStatus(String razorpayOrderId, String status, String razorpayPaymentId, Integer credits) {
        paymentTransactionRepository.findAll().stream()
                .filter(t -> t.getOrderId() != null && t.getOrderId().equals(razorpayOrderId))
                .findFirst()
                .map(transaction -> {
                    transaction.setStatus(status);
                    transaction.setPaymentId(razorpayPaymentId);
                    if (credits != null)
                        transaction.setCreditsAdded(credits);

                    return paymentTransactionRepository.save(transaction);
                }).orElseGet(() -> {
                    // log warning: transaction not found
                    log.warn("PaymentTransaction with orderId={} not found", razorpayOrderId);
                    return null;
                });
    }

    private String generateHmacSha256Signature(String data, String razorpayKeySecret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            byte[] macData = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(macData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC SHA256 signature", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b)); // lowercase hex
        }
        return sb.toString();
    }
}