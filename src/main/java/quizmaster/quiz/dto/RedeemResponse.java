package quizmaster.quiz.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedeemResponse {
    private boolean success;
    private String rewardType;
    private Integer rewardValue;
    private Integer newBalance;
    private String message;
}
