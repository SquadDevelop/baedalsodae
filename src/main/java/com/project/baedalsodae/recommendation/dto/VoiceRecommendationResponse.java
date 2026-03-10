package com.project.baedalsodae.recommendation.dto;

import java.util.List;
import java.util.UUID;

public record VoiceRecommendationResponse(
        String aiMessage, List<RecommendedMenu> recommendedMenus) {
    public record RecommendedMenu(
            UUID storeId,
            String storeName,
            double rating,
            UUID menuId,
            String menuName,
            int price,
            String description) {}
}
