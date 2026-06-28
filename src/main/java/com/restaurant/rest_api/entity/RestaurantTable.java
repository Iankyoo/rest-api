package com.restaurant.rest_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "restaurant_table")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer number;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_status", nullable = false)
    private TableStatus status;
}
