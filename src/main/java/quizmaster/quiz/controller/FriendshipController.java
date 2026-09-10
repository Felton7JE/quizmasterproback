package quizmaster.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizmaster.quiz.dto.FriendDTO;
import quizmaster.quiz.service.FriendshipService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@RequestParam Long userId, @RequestParam String targetUsername) {
        try {
            friendshipService.sendFriendRequest(userId, targetUsername);
            return ResponseEntity.ok(Map.of("message", "Pedido de amizade enviado!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<?> acceptFriendRequest(@RequestParam Long userId, @PathVariable Long friendshipId) {
        try {
            friendshipService.acceptFriendRequest(userId, friendshipId);
            return ResponseEntity.ok(Map.of("message", "Pedido aceite com sucesso!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reject/{friendshipId}")
    public ResponseEntity<?> rejectFriendRequest(@RequestParam Long userId, @PathVariable Long friendshipId) {
        try {
            friendshipService.rejectFriendRequest(userId, friendshipId);
            return ResponseEntity.ok(Map.of("message", "Pedido rejeitado."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<?> removeFriend(@RequestParam Long userId, @PathVariable Long friendId) {
        try {
            friendshipService.removeFriend(userId, friendId);
            return ResponseEntity.ok(Map.of("message", "Amigo removido."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<FriendDTO>> getFriendsList(@RequestParam Long userId) {
        return ResponseEntity.ok(friendshipService.getFriendsList(userId));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendDTO>> getPendingRequests(@RequestParam Long userId) {
        return ResponseEntity.ok(friendshipService.getPendingRequests(userId));
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<FriendDTO>> getSentRequests(@RequestParam Long userId) {
        return ResponseEntity.ok(friendshipService.getSentRequests(userId));
    }
}
