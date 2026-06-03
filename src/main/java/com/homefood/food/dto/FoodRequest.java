package com.homefood.food.dto;

import com.homefood.food.entity.FoodCategory;
import com.homefood.food.entity.FoodType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FoodRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotNull
    private FoodCategory category;

    @NotNull
    private FoodType foodType;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("9999.99")
    private BigDecimal price;

    @Min(5)
    @Max(180)
    private int preparationTimeMinutes;

    @Min(1)
    private int servingSize;

    @NotBlank
    private String pincode;

    @NotBlank
    private String city;

    private String state;

    private Double latitude;
    private Double longitude;

    private List<String> imageUrls;
    private List<String> ingredients;
    private List<String> allergens;

    private boolean available = true;
    private int maxDailyOrders = 20;
    private boolean isOrganic;
    private boolean isHomemade = true;
    private List<String> availableDays;
    private String availableFrom;
    private String availableTo;
    private String cuisineType;
}
