package com.itimpulse.urlshortener.model;

import com.itimpulse.urlshortener.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class ShortenUrl extends Auditable {
    @Id
    private String id;

    @Column(nullable = false)
    private String url;

    private LocalDateTime ttl;

}
