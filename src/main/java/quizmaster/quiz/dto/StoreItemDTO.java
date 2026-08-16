package quizmaster.quiz.dto;

import lombok.Data;
import quizmaster.quiz.enums.ItemType;

@Data
public class StoreItemDTO {
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private ItemType type;
    private String value;
    private String rarity;
    private boolean isOwned;
}
