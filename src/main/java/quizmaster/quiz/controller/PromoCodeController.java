package quizmaster.quiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizmaster.quiz.dto.RedeemRequest;
import quizmaster.quiz.dto.RedeemResponse;
import quizmaster.quiz.service.PromoCodeService;

import java.util.Map;

@RestController
@RequestMapping("/api/promocodes")
public class PromoCodeController {

    @Autowired
    private PromoCodeService promoCodeService;

    // Rate limiting ideally should be added here using a Filter or Interceptor
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemCode(@RequestParam Long userId, @RequestBody RedeemRequest request) {
        try {
            RedeemResponse response = promoCodeService.redeem(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Ocorreu um erro interno ao processar o código."
            ));
        }
    }
}
