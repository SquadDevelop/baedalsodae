package com.project.baedalsodae.recommendation.service;

import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.repository.MenuItemRepository;
import com.project.baedalsodae.recommendation.dto.VoiceRecommendationResponse;
import com.project.baedalsodae.store.entity.Store;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuSearchService {
    private final VectorStore vectorStore;
    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public List<VoiceRecommendationResponse.RecommendedMenu> recommendMenuItems(String query) {
        // 1. Vector Search
        List<Document> similarDocuments =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(query)
                                .topK(10)
                                .similarityThreshold(0.1)
                                .build());

        // 2. Fetch Entities and Apply Weighted Scoring
        List<VoiceRecommendationResponse.RecommendedMenu> recommendations = new ArrayList<>();

        record MenuScore(MenuItem menuItem, Store store, double score) {}
        List<MenuScore> scoredMenus = new ArrayList<>();

        for (Document doc : similarDocuments) {
            String menuIdStr = (String) doc.getMetadata().get("menuId");
            if (menuIdStr == null) continue;

            UUID menuId = UUID.fromString(menuIdStr);
            menuItemRepository
                    .findByIdAndDeletedIsFalse(menuId)
                    .ifPresent(
                            menuItem -> {
                                Store store = menuItem.getMenuCategory().getStore();
                                double score = store.getAvgRating() * 2.0;
                                if (menuItem.isPopular()) score += 1.0;

                                scoredMenus.add(new MenuScore(menuItem, store, score));
                            });
        }

        // 3. Sort by score descending and take Top 3
        return scoredMenus.stream()
                .sorted(Comparator.comparingDouble(MenuScore::score).reversed())
                .limit(3)
                .map(
                        ms ->
                                new VoiceRecommendationResponse.RecommendedMenu(
                                        ms.store().getId(),
                                        ms.store().getName(),
                                        ms.store().getAvgRating(),
                                        ms.menuItem().getId(),
                                        ms.menuItem().getName(),
                                        ms.menuItem().getPrice().intValue(),
                                        ms.menuItem().getDescription()))
                .toList();
    }
}
