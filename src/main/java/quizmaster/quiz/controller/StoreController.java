package quizmaster.quiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.UserRepository;
import quizmaster.quiz.service.StoreService;
import quizmaster.quiz.dto.StoreItemDTO;
import quizmaster.quiz.dto.UserItemDTO;
import quizmaster.quiz.enums.ItemType;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/items/available/{userId}")
    public ResponseEntity<List<StoreItemDTO>> getStoreFront(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(storeService.getStoreFront(user));
    }

    @GetMapping("/items/purchased/{userId}")
    public ResponseEntity<List<UserItemDTO>> getInventory(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(storeService.getUserInventory(user));
    }

    @PostMapping("/buy")
    public ResponseEntity<String> buyItem(@RequestParam Long userId, @RequestParam Long itemId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        storeService.buyItem(user, itemId);
        return ResponseEntity.ok("Item purchased successfully");
    }

    @PostMapping("/equip")
    public ResponseEntity<String> equipItem(@RequestParam Long userId, @RequestParam Long itemId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        storeService.equipItem(user, itemId);
        return ResponseEntity.ok("Item equipped successfully");
    }

    @PostMapping("/unequip")
    public ResponseEntity<String> unequipItem(
        @RequestParam Long userId,
        @RequestParam(required = false) String itemType,
        @RequestParam(required = false) Long itemId
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (itemId != null) {
            storeService.unequipSpecificItem(user, itemId);
            return ResponseEntity.ok("Item unequipped successfully");
        }
        
        if (itemType != null) {
            ItemType type;
            try {
                type = ItemType.valueOf(itemType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Tipo de item inválido: " + itemType);
            }
            storeService.unequipItem(user, type);
            return ResponseEntity.ok("Item unequipped successfully");
        }
        
        return ResponseEntity.badRequest().body("itemId ou itemType obrigatório");
    }

    /**
     * Consome um item consumível do inventário do jogador.
     * itemType: ENERGY_REFILL → restaura energia para 100
     * itemType: XP_BOOST     → será aplicado na próxima partida (equip direto)
     */
    @PostMapping("/consume")
    public ResponseEntity<String> consumeItem(@RequestParam Long userId, @RequestParam String itemType) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        ItemType type;
        try {
            type = ItemType.valueOf(itemType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Tipo de item inválido: " + itemType);
        }
        storeService.consumeItem(user, type);
        return ResponseEntity.ok("Item consumido com sucesso");
    }
}
