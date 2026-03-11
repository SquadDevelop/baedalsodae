package com.project.baedalsodae.recommendation.entity;

import com.project.baedalsodae.recommendation.entity.enums.MessageType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "spring_ai_chat_memory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMemoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", length = 36)
    private String conversationId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING) // Enum 이름을 문자열로 DB에 저장 ('USER', 'ASSISTANT' 등)
    @Column(length = 10, nullable = false)
    private MessageType type;

    @CreationTimestamp // INSERT 시 현재 시간 자동 입력
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
