package com.thetruemarket.api.infrastructure.persistence.entity;

import com.thetruemarket.api.domain.valueobject.TaskStatus;
import com.thetruemarket.api.domain.valueobject.Wear;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for History Update Task persistence
 * Infrastructure layer implementation
 */
@Entity
@Table(name = "history_update_tasks", indexes = {
        @Index(name = "idx_status_created", columnList = "status, created_at"),
        @Index(name = "idx_skin_name_wear_status", columnList = "skin_name, wear, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryUpdateTaskEntity {
    /**
     * Auto-generated unique identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Skin name (e.g., "AK-47 | Redline")
     */
    @Column(name = "skin_name", nullable = false, length = 500)
    private String skinName;

    /**
     * Wear category
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "wear", nullable = false, length = 50)
    private Wear wear;

    /**
     * Current task status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status;

    /**
     * When the task was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the task was completed (null if still waiting)
     */
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
