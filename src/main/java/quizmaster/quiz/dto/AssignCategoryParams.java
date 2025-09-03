package quizmaster.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignCategoryParams {
    @NotBlank
    private String roomCode;

    @NotNull
    private Long playerId;

    @NotNull
    private Long categoryId;
}
