package com.skillproof.backend.realtime.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.realtime.application.BattleRepository;
import com.skillproof.backend.realtime.application.WebSocketTicketService;

@RestController
@RequestMapping("/api/v1/realtime")
public class RealtimeController {

    private final WebSocketTicketService tickets;
    private final BattleRepository battles;

    public RealtimeController(WebSocketTicketService tickets, BattleRepository battles) {
        this.tickets = tickets;
        this.battles = battles;
    }

    private UUID user(Authentication a) {
        return (UUID) a.getPrincipal();
    }

    @PostMapping("/ws-ticket")
    @ResponseStatus(HttpStatus.CREATED)
    public WebSocketTicketService.Ticket ticket(Authentication a) {
        return tickets.issue(user(a));
    }

    @GetMapping("/ratings")
    public List<Map<String, Object>> ratings(Authentication a) {
        return battles.ratings(user(a));
    }

    @GetMapping("/leaderboard")
    public List<Map<String, Object>> leaderboard(@RequestParam String skillId, @RequestParam(defaultValue = "50") int size) {
        return battles.leaderboard(skillId, Math.min(Math.max(size, 1), 100));
    }

    @GetMapping("/battles")
    public List<Map<String, Object>> history(Authentication a) {
        return battles.history(user(a));
    }

    @GetMapping("/battles/{id}")
    public Map<String, Object> detail(Authentication a, @PathVariable UUID id) {
        return battles.detail(id, user(a));
    }
}
