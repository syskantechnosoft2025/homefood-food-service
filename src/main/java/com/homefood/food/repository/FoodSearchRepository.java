package com.homefood.food.repository;

import com.homefood.food.entity.Food;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodSearchRepository extends ElasticsearchRepository<Food, String> {
}
