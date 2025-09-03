package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class QuestionStartedMessage {
    private Long gameId;
    private Integer questionIndex;
    private QuestionResponse question;
    private Integer timeLimit;
    private Long timestamp;
}
