# Food Service

Manages food listings with MongoDB storage and Elasticsearch for search.

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/v1/foods | SELLER | Create food listing |
| GET | /api/v1/foods/{id} | Public | Get food detail |
| GET | /api/v1/foods/search | Public | Search with filters |
| GET | /api/v1/foods/top | Public | Top rated by pincode |
| GET | /api/v1/foods/seller/{id} | Public | Seller's foods |
| PUT | /api/v1/foods/{id} | SELLER | Update food |
| DELETE | /api/v1/foods/{id} | SELLER | Delete food |

## Search Filters
- `query` — full-text (name, description, cuisine)
- `pincode` / `city` — location
- `category` — BREAKFAST, LUNCH, DINNER, etc.
- `foodType` — VEG, NON_VEG, VEGAN, EGG
- `minPrice` / `maxPrice`
- `minRating`
- `sortBy` — averageRating, price, createdAt
