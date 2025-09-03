package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "categories")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructor with name and displayName
    public CategoryEntity(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    // Constructor with all fields except id and timestamps
    public CategoryEntity(String name, String displayName, String description) {
        this(name, displayName);
        this.description = description;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
