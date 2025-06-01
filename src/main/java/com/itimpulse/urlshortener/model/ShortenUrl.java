package com.itimpulse.urlshortener.model;

import com.itimpulse.urlshortener.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Data
public class ShortenUrl extends Auditable {
    @Id
    private String id;

    @Column(nullable = false)
    private String url;

    private LocalDateTime ttl;

    @CreatedDate
    private LocalDateTime createdAt;
}
