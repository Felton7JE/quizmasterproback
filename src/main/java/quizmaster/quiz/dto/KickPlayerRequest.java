package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KickPlayerRequest {
    @NotNull
    private Long hostId;
    @NotNull
    private Long playerId;
}
