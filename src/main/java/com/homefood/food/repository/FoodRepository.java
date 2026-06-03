package com.homefood.food.repository;

import com.homefood.food.entity.Food;
import com.homefood.food.entity.FoodCategory;
import com.homefood.food.entity.FoodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface FoodRepository extends MongoRepository<Food, String> {

    Page<Food> findByPincodeAndAvailableTrueAndDeletedAtIsNull(String pincode, Pageable pageable);

    Page<Food> findByCityAndAvailableTrueAndDeletedAtIsNull(String city, Pageable pageable);

    Page<Food> findBySellerIdAndDeletedAtIsNull(UUID sellerId, Pageable pageable);

    Page<Food> findByCategoryAndAvailableTrueAndDeletedAtIsNull(FoodCategory category, Pageable pageable);

    Page<Food> findByFoodTypeAndAvailableTrueAndDeletedAtIsNull(FoodType foodType, Pageable pageable);

    @Query("{'pincode': ?0, 'available': true, 'deletedAt': null, 'averageRating': {$gte: ?1}}")
    Page<Food> findByPincodeAndMinRating(String pincode, Double minRating, Pageable pageable);

    @Query("{'pincode': ?0, 'foodType': ?1, 'category': ?2, 'available': true, 'deletedAt': null}")
    Page<Food> findByPincodeAndFoodTypeAndCategory(String pincode, String foodType, String category, Pageable pageable);

    @Query("{'name': {$regex: ?0, $options: 'i'}, 'available': true, 'deletedAt': null}")
    Page<Food> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("{'price': {$gte: ?0, $lte: ?1}, 'available': true, 'deletedAt': null}")
    Page<Food> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    List<Food> findTop10ByPincodeAndAvailableTrueOrderByAverageRatingDesc(String pincode);

    long countBySellerIdAndDeletedAtIsNull(UUID sellerId);
}
