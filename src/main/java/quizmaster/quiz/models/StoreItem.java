package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.enums.ItemType;

@Entity
@Table(name = "store_items")
@Getter
@Setter
@NoArgsConstructor
public class StoreItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType type;

    @Column(nullable = false)
    private String value; // The image URL or the text phrase

    @Column(name = "rarity", columnDefinition = "VARCHAR(255) DEFAULT 'Comum'")
    private String rarity;
}
