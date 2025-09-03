package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class RoomSettingsUpdatedMessage {
    private String roomCode;
    private UpdateRoomRequest settings;
    private Long timestamp;
}
