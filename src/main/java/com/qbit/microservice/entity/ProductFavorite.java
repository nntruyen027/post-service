package com.qbit.microservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_favorites", uniqueConstraints = @UniqueConstraint(columnNames = {"productId", "userId"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
