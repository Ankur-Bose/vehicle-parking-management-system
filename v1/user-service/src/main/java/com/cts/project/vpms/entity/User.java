package com.cts.project.vpms.entity;

import com.cts.project.vpms.enums.Role;
import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 10)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String password_hash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", updatable = false, insertable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
