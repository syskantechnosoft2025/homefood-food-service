package com.homefood.food.dto;

import com.homefood.food.entity.FoodCategory;
import com.homefood.food.entity.FoodType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FoodResponse {
    private String id;
    private UUID sellerId;
    private String sellerName;
    private String name;
    private String description;
    private FoodCategory category;
    private FoodType foodType;
    private BigDecimal price;
    private int preparationTimeMinutes;
    private int servingSize;
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
    private int totalRatings;
    private boolean isOrganic;
    private boolean isHomemade;
    private List<String> availableDays;
    private String availableFrom;
    private String availableTo;
    private String cuisineType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
