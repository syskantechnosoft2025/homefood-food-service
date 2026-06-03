package com.homefood.food.controller;

import com.homefood.food.dto.FoodRequest;
import com.homefood.food.dto.FoodResponse;
import com.homefood.food.dto.FoodSearchRequest;
import com.homefood.food.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @PostMapping
    public ResponseEntity<FoodResponse> createFood(
            @Valid @RequestBody FoodRequest request,
            @RequestHeader("X-User-Id") UUID sellerId,
            @RequestHeader(value = "X-User-Name", defaultValue = "Seller") String sellerName) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodService.createFood(request, sellerId, sellerName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFood(@PathVariable String id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<FoodResponse>> searchFoods(FoodSearchRequest request) {
        return ResponseEntity.ok(foodService.searchFoods(request));
    }

    @GetMapping("/top")
    public ResponseEntity<List<FoodResponse>> getTopFoods(@RequestParam String pincode) {
        return ResponseEntity.ok(foodService.getTopFoodsByPincode(pincode));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Page<FoodResponse>> getSellerFoods(
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(foodService.getSellerFoods(sellerId, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodResponse> updateFood(
            @PathVariable String id,
            @Valid @RequestBody FoodRequest request,
            @RequestHeader("X-User-Id") UUID sellerId) {
        return ResponseEntity.ok(foodService.updateFood(id, request, sellerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(
            @PathVariable String id,
            @RequestHeader("X-User-Id") UUID sellerId) {
        foodService.deleteFood(id, sellerId);
        return ResponseEntity.noContent().build();
    }
}
