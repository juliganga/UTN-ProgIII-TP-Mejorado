package com.utn.ProgIII.model.Audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Table(name = "general_audit_log")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AuditLog {

    @Id
    @Column(name = "revision_id")
    private Long revisionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Column(name = "revision_date")
    private LocalDateTime revisionDate;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "category")
    private String category;

    @Column(name = "revision_type")
    private Integer revisionType;

    @Column(name = "entity_id")
    private Long entityId;
}
