package com.homefood.food.service;

import com.homefood.food.dto.FoodRequest;
import com.homefood.food.dto.FoodResponse;
import com.homefood.food.dto.FoodSearchRequest;
import com.homefood.food.entity.Food;
import com.homefood.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public FoodResponse createFood(FoodRequest request, UUID sellerId, String sellerName) {
        Food food = Food.builder()
                .sellerId(sellerId)
                .sellerName(sellerName)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .foodType(request.getFoodType())
                .price(request.getPrice())
                .preparationTimeMinutes(request.getPreparationTimeMinutes())
                .servingSize(request.getServingSize())
                .pincode(request.getPincode())
                .city(request.getCity())
                .state(request.getState())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .imageUrls(request.getImageUrls())
                .ingredients(request.getIngredients())
                .allergens(request.getAllergens())
                .available(request.isAvailable())
                .maxDailyOrders(request.getMaxDailyOrders())
                .isOrganic(request.isOrganic())
                .isHomemade(request.isHomemade())
                .availableDays(request.getAvailableDays())
                .availableFrom(request.getAvailableFrom())
                .availableTo(request.getAvailableTo())
                .cuisineType(request.getCuisineType())
                .averageRating(0.0)
                .build();

        food = foodRepository.save(food);

        // Index in Elasticsearch
        try {
            elasticsearchOperations.save(food);
        } catch (Exception e) {
            log.warn("Failed to index food in Elasticsearch: {}", e.getMessage());
        }

        kafkaTemplate.send("report.event", food.getId(),
                String.format("{\"type\":\"FOOD_CREATED\",\"foodId\":\"%s\",\"sellerId\":\"%s\"}", food.getId(), sellerId));

        log.info("Food created: {} by seller: {}", food.getName(), sellerId);
        return toResponse(food);
    }

    public FoodResponse getFoodById(String id) {
        Food food = foodRepository.findById(id)
                .filter(f -> f.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Food not found: " + id));
        return toResponse(food);
    }

    public Page<FoodResponse> searchFoods(FoodSearchRequest request) {
        Sort sort = request.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(request.getSortBy()).ascending()
                : Sort.by(request.getSortBy()).descending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Try Elasticsearch first for full-text search
        if (request.getQuery() != null && !request.getQuery().isBlank()) {
            return searchElasticsearch(request, pageable);
        }

        // MongoDB queries for filtered search
        if (request.getPincode() != null && request.getMinRating() != null) {
            return foodRepository.findByPincodeAndMinRating(request.getPincode(), request.getMinRating(), pageable)
                    .map(this::toResponse);
        }
        if (request.getPincode() != null) {
            return foodRepository.findByPincodeAndAvailableTrueAndDeletedAtIsNull(request.getPincode(), pageable)
                    .map(this::toResponse);
        }
        if (request.getCity() != null) {
            return foodRepository.findByCityAndAvailableTrueAndDeletedAtIsNull(request.getCity(), pageable)
                    .map(this::toResponse);
        }
        if (request.getCategory() != null) {
            return foodRepository.findByCategoryAndAvailableTrueAndDeletedAtIsNull(request.getCategory(), pageable)
                    .map(this::toResponse);
        }
        if (request.getFoodType() != null) {
            return foodRepository.findByFoodTypeAndAvailableTrueAndDeletedAtIsNull(request.getFoodType(), pageable)
                    .map(this::toResponse);
        }

        return foodRepository.findAll(pageable).map(this::toResponse);
    }

    private Page<FoodResponse> searchElasticsearch(FoodSearchRequest request, Pageable pageable) {
        try {
            Criteria criteria = new Criteria("name").matches(request.getQuery())
                    .or(new Criteria("description").matches(request.getQuery()))
                    .or(new Criteria("cuisineType").matches(request.getQuery()));

            if (request.getPincode() != null) {
                criteria = criteria.and(new Criteria("pincode").is(request.getPincode()));
            }
            if (request.getFoodType() != null) {
                criteria = criteria.and(new Criteria("foodType").is(request.getFoodType().name()));
            }
            if (request.getCategory() != null) {
                criteria = criteria.and(new Criteria("category").is(request.getCategory().name()));
            }

            CriteriaQuery query = new CriteriaQuery(criteria, pageable);
            SearchHits<Food> hits = elasticsearchOperations.search(query, Food.class);

            List<FoodResponse> results = hits.getSearchHits().stream()
                    .map(h -> toResponse(h.getContent()))
                    .collect(Collectors.toList());

            return new org.springframework.data.domain.PageImpl<>(results, pageable, hits.getTotalHits());
        } catch (Exception e) {
            log.warn("Elasticsearch search failed, falling back to MongoDB: {}", e.getMessage());
            return foodRepository.findByNameContainingIgnoreCase(request.getQuery(), pageable)
                    .map(this::toResponse);
        }
    }

    public FoodResponse updateFood(String id, FoodRequest request, UUID sellerId) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food not found: " + id));

        if (!food.getSellerId().equals(sellerId)) {
            throw new SecurityException("Not authorized to update this food");
        }

        food.setName(request.getName());
        food.setDescription(request.getDescription());
        food.setCategory(request.getCategory());
        food.setFoodType(request.getFoodType());
        food.setPrice(request.getPrice());
        food.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        food.setAvailable(request.isAvailable());
        food.setMaxDailyOrders(request.getMaxDailyOrders());
        food.setImageUrls(request.getImageUrls());
        food.setIngredients(request.getIngredients());
        food.setAllergens(request.getAllergens());
        food.setAvailableDays(request.getAvailableDays());
        food.setAvailableFrom(request.getAvailableFrom());
        food.setAvailableTo(request.getAvailableTo());

        food = foodRepository.save(food);

        // Re-index in Elasticsearch
        try {
            elasticsearchOperations.save(food);
        } catch (Exception e) {
            log.warn("Failed to re-index food: {}", e.getMessage());
        }

        return toResponse(food);
    }

    public void deleteFood(String id, UUID sellerId) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food not found: " + id));

        if (!food.getSellerId().equals(sellerId)) {
            throw new SecurityException("Not authorized to delete this food");
        }

        food.setDeletedAt(LocalDateTime.now());
        food.setAvailable(false);
        foodRepository.save(food);

        try {
            elasticsearchOperations.delete(id, Food.class);
        } catch (Exception e) {
            log.warn("Failed to delete food from Elasticsearch: {}", e.getMessage());
        }
    }

    public List<FoodResponse> getTopFoodsByPincode(String pincode) {
        return foodRepository.findTop10ByPincodeAndAvailableTrueOrderByAverageRatingDesc(pincode)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Page<FoodResponse> getSellerFoods(UUID sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return foodRepository.findBySellerIdAndDeletedAtIsNull(sellerId, pageable).map(this::toResponse);
    }

    public void updateFoodRating(String foodId, double newAvgRating, int totalRatings) {
        foodRepository.findById(foodId).ifPresent(food -> {
            food.setAverageRating(newAvgRating);
            food.setTotalRatings(totalRatings);
            foodRepository.save(food);
            try {
                elasticsearchOperations.save(food);
            } catch (Exception e) {
                log.warn("Failed to update rating in Elasticsearch: {}", e.getMessage());
            }
        });
    }

    private FoodResponse toResponse(Food food) {
        return FoodResponse.builder()
                .id(food.getId())
                .sellerId(food.getSellerId())
                .sellerName(food.getSellerName())
                .name(food.getName())
                .description(food.getDescription())
                .category(food.getCategory())
                .foodType(food.getFoodType())
                .price(food.getPrice())
                .preparationTimeMinutes(food.getPreparationTimeMinutes())
                .servingSize(food.getServingSize())
                .pincode(food.getPincode())
                .city(food.getCity())
                .state(food.getState())
                .latitude(food.getLatitude())
                .longitude(food.getLongitude())
                .imageUrls(food.getImageUrls())
                .ingredients(food.getIngredients())
                .allergens(food.getAllergens())
                .available(food.isAvailable())
                .maxDailyOrders(food.getMaxDailyOrders())
                .currentDailyOrders(food.getCurrentDailyOrders())
                .averageRating(food.getAverageRating())
                .totalRatings(food.getTotalRatings())
                .isOrganic(food.isOrganic())
                .isHomemade(food.isHomemade())
                .availableDays(food.getAvailableDays())
                .availableFrom(food.getAvailableFrom())
                .availableTo(food.getAvailableTo())
                .cuisineType(food.getCuisineType())
                .createdAt(food.getCreatedAt())
                .updatedAt(food.getUpdatedAt())
                .build();
    }
}
