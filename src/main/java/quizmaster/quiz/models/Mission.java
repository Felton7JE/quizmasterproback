package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.enums.MissionType;

@Entity
@Table(name = "missions")
@Getter
@Setter
@NoArgsConstructor
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer targetValue; 

    @Column(nullable = false)
    private Integer rewardCoins;

    @Column(nullable = false)
    private String actionType; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType type;

    private Long rewardItemId;
    private String rewardItemType;
    private String rewardItemName;
    private String rewardItemValue;
}
