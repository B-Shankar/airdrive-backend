package in.bhimashankar.airdrive.controller;

import in.bhimashankar.airdrive.dto.PaymentDTO;
import in.bhimashankar.airdrive.dto.PaymentVerificationDTO;
import in.bhimashankar.airdrive.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody PaymentDTO paymentDTO) {
        PaymentDTO response = paymentService.createOrder(paymentDTO);

        if (response.getSuccess())
            return ResponseEntity.ok(response);
        else
            return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/payment-verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationDTO request) {
        PaymentDTO response = paymentService.verifyPayment(request);

        if (response.getSuccess())
            return ResponseEntity.ok(response);
        else
            return ResponseEntity.badRequest().body(response);
    }
}
