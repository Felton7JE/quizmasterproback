package quizmaster.quiz.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class AddBotsRequest {
    @NotNull
    private Long hostId;
    private int count;
}