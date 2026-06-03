package com.homefood.food.service;

import com.homefood.food.dto.FoodRequest;
import com.homefood.food.dto.FoodResponse;
import com.homefood.food.entity.Food;
import com.homefood.food.entity.FoodCategory;
import com.homefood.food.entity.FoodType;
import com.homefood.food.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FoodService Unit Tests")
class FoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private FoodService foodService;

    private Food testFood;
    private FoodRequest foodRequest;
    private UUID sellerId;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();

        testFood = Food.builder()
                .id("food-123")
                .sellerId(sellerId)
                .sellerName("Test Seller")
                .name("Butter Chicken")
                .description("Delicious homemade butter chicken")
                .category(FoodCategory.DINNER)
                .foodType(FoodType.NON_VEG)
                .price(new BigDecimal("180.00"))
                .pincode("560001")
                .city("Bangalore")
                .available(true)
                .averageRating(4.5)
                .totalRatings(10)
                .build();

        foodRequest = new FoodRequest();
        foodRequest.setName("Butter Chicken");
        foodRequest.setDescription("Delicious homemade butter chicken");
        foodRequest.setCategory(FoodCategory.DINNER);
        foodRequest.setFoodType(FoodType.NON_VEG);
        foodRequest.setPrice(new BigDecimal("180.00"));
        foodRequest.setPincode("560001");
        foodRequest.setCity("Bangalore");
        foodRequest.setPreparationTimeMinutes(30);
        foodRequest.setServingSize(2);
    }

    @Test
    @DisplayName("Should create food successfully")
    void shouldCreateFood() {
        when(foodRepository.save(any(Food.class))).thenReturn(testFood);
        when(elasticsearchOperations.save(any(Food.class))).thenReturn(testFood);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(null);

        FoodResponse response = foodService.createFood(foodRequest, sellerId, "Test Seller");

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Butter Chicken");
        assertThat(response.getSellerId()).isEqualTo(sellerId);
        verify(foodRepository).save(any(Food.class));
    }

    @Test
    @DisplayName("Should get food by ID")
    void shouldGetFoodById() {
        when(foodRepository.findById("food-123")).thenReturn(Optional.of(testFood));

        FoodResponse response = foodService.getFoodById("food-123");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("food-123");
        assertThat(response.getName()).isEqualTo("Butter Chicken");
    }

    @Test
    @DisplayName("Should throw exception for non-existent food")
    void shouldThrowForNonExistentFood() {
        when(foodRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> foodService.getFoodById("non-existent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Food not found");
    }

    @Test
    @DisplayName("Should update food rating")
    void shouldUpdateFoodRating() {
        when(foodRepository.findById("food-123")).thenReturn(Optional.of(testFood));
        when(foodRepository.save(any())).thenReturn(testFood);
        when(elasticsearchOperations.save(any())).thenReturn(testFood);

        foodService.updateFoodRating("food-123", 4.8, 11);

        verify(foodRepository).save(argThat(f -> f.getAverageRating() == 4.8 && f.getTotalRatings() == 11));
    }

    @Test
    @DisplayName("Should get top foods by pincode")
    void shouldGetTopFoodsByPincode() {
        when(foodRepository.findTop10ByPincodeAndAvailableTrueOrderByAverageRatingDesc("560001"))
                .thenReturn(List.of(testFood));

        List<FoodResponse> results = foodService.getTopFoodsByPincode("560001");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPincode()).isEqualTo("560001");
    }

    @Test
    @DisplayName("Should throw SecurityException when unauthorized seller tries to delete")
    void shouldThrowForUnauthorizedDelete() {
        when(foodRepository.findById("food-123")).thenReturn(Optional.of(testFood));

        assertThatThrownBy(() -> foodService.deleteFood("food-123", UUID.randomUUID()))
                .isInstanceOf(SecurityException.class);
    }
}
