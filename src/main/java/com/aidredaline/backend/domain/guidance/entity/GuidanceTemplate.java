package com.aidredaline.backend.domain.guidance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "guidance_templates")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuidanceTemplate {

    @Id
    @Column(name = "guidance_id", length = 50)
    private String guidanceId;  // PK: "TURN_LEFT_50"

    @Column(nullable = false, length = 500)
    private String text;  // "50미터 앞에서 좌회전하세요"

    @Column(name = "file_path", nullable = false, length = 200)
    private String filePath;  // "turn_left_50.mp3"

    @Column(length = 50)
    private String category;  // "turn", "straight", "event" 등

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}