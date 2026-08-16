package quizmaster.quiz.dto;

import lombok.Data;
import quizmaster.quiz.enums.ItemType;

@Data
public class UserItemDTO {
    private Long id;
    private StoreItemDTO storeItem;
    private Boolean isEquipped;
}
