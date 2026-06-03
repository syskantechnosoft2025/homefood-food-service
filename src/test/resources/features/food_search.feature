Feature: Food Search
  As a buyer on HomeFOOD
  I want to search for homemade food
  So that I can find delicious meals near me

  Background:
    Given the food catalog has the following items:
      | name           | category | foodType | price  | pincode | city      | rating |
      | Butter Chicken | DINNER   | NON_VEG  | 180.00 | 560001  | Bangalore | 4.5    |
      | Dal Makhani    | LUNCH    | VEG      | 120.00 | 560001  | Bangalore | 4.2    |
      | Masala Dosa    | BREAKFAST| VEG      | 80.00  | 560001  | Bangalore | 4.8    |
      | Chicken Biryani| DINNER   | NON_VEG  | 220.00 | 110001  | Delhi     | 4.6    |

  Scenario: Search food by pincode
    When I search for food in pincode "560001"
    Then I should see 3 results
    And all results should be from pincode "560001"

  Scenario: Search food by category
    When I search for food in category "BREAKFAST"
    Then I should see 1 result
    And the first result should be "Masala Dosa"

  Scenario: Search food by type VEG
    When I filter food by type "VEG"
    Then all results should have food type "VEG"

  Scenario: Search food with minimum rating
    When I search for food with minimum rating 4.5
    Then all results should have rating >= 4.5

  Scenario: Full-text search by name
    When I search for "chicken"
    Then results should include "Butter Chicken"
    And results should include "Chicken Biryani"

  Scenario: Empty results for unavailable pincode
    When I search for food in pincode "999999"
    Then I should see 0 results
