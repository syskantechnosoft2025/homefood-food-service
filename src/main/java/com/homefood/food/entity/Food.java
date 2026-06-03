package com.homefood.food.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "foods")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "idx_seller_pincode", def = "{'sellerId': 1, 'pincode': 1}"),
        @CompoundIndex(name = "idx_category_type", def = "{'category': 1, 'foodType': 1}")
})
public class Food {

    @Id
    private String id;

    @Indexed
    private UUID sellerId;

    private String sellerName;

    @Indexed
    private String name;

    private String description;

    private FoodCategory category;

    private FoodType foodType;

    private BigDecimal price;

    private int preparationTimeMinutes;

    private int servingSize;

    @Indexed
    private String pincode;

    private String city;

    private String state;

    private Double latitude;

    private Double longitude;

    private List<String> imageUrls;

    private List<String> ingredients;

    private List<String> allergens;

    private boolean available;

    private int maxDailyOrders;

    private int currentDailyOrders;

    private Double averageRating;

    @Builder.Default
    private int totalRatings = 0;

    private boolean isOrganic;

    private boolean isHomemade;

    private List<String> availableDays; // ["MON","TUE","WED","THU","FRI","SAT","SUN"]

    private String availableFrom; // "08:00"

    private String availableTo;   // "20:00"

    private String cuisineType; // "South Indian", "North Indian", "Chinese", etc.

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
