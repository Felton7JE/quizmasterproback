package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "seasons")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Season {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private boolean active = true;

    // Optional constraint to bind this season to a specific category (e.g. for themed questions)
    private Long exclusiveCategoryId;

    // Theme assets
    private String bannerUrl;
    private String mapBackgroundUrl;
    private String lockedNodeIconUrl;
    private String currentNodeIconUrl;
    private String completedNodeIconUrl;
}
