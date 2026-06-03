package com.homefood.food.dto;

import com.homefood.food.entity.FoodCategory;
import com.homefood.food.entity.FoodType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodSearchRequest {
    private String query;
    private String pincode;
    private String city;
    private FoodCategory category;
    private FoodType foodType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private String cuisineType;
    private String sortBy = "averageRating"; // averageRating, price, createdAt
    private String sortDir = "desc";
    private int page = 0;
    private int size = 20;
}
