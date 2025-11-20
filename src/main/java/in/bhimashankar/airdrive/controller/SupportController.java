package in.bhimashankar.airdrive.controller;

import in.bhimashankar.airdrive.dto.SupportTicketDTO;
import in.bhimashankar.airdrive.service.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    @PostMapping("/create-ticket")
    public ResponseEntity<?> createTicket(@Valid @RequestBody SupportTicketDTO ticketDTO) {
        SupportTicketDTO response = supportService.createTicket(ticketDTO);

        if (response.getSuccess())
            return ResponseEntity.ok(response);
        else
            return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<SupportTicketDTO>> getUserTickets() {
        List<SupportTicketDTO> tickets = supportService.getUserTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getTicket(@PathVariable String ticketId) {
        SupportTicketDTO ticket = supportService.getTicketByTicketId(ticketId);

        if (ticket.getSuccess())
            return ResponseEntity.ok(ticket);
        else
            return ResponseEntity.notFound().build();
    }
}
