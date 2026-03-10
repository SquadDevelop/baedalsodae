package com.project.baedalsodae.recommendation.service;

import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.repository.MenuItemRepository;
import com.project.baedalsodae.recommendation.dto.VoiceRecommendationRequest;
import com.project.baedalsodae.recommendation.dto.VoiceRecommendationResponse;
import com.project.baedalsodae.recommendation.dto.VoiceRecommendationResponse.RecommendedMenu;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.repository.StoreRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final VectorStore vectorStore;
    private final MenuItemRepository menuItemRepository;
    private final StoreRepository storeRepository;
    private final ChatClient chatClient;

    @Transactional(readOnly = true)
    public VoiceRecommendationResponse recommendMenuItems(VoiceRecommendationRequest request) {
        // 1. Vector Search
        List<Document> similarDocuments =
                vectorStore.similaritySearch(
                        SearchRequest.query(request.transcribedText()).withTopK(10));

        if (similarDocuments.isEmpty()) {
            return new VoiceRecommendationResponse("죄송해요, 원하시는 조건에 맞는 메뉴를 찾지 못했어요.", List.of());
        }

        // 2. Fetch Entities and Apply Weighted Scoring
        List<RecommendedMenu> recommendations = new ArrayList<>();

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
        List<RecommendedMenu> top3 =
                scoredMenus.stream()
                        .sorted(Comparator.comparingDouble(MenuScore::score).reversed())
                        .limit(3)
                        .map(
                                ms ->
                                        new RecommendedMenu(
                                                ms.store().getId(),
                                                ms.store().getName(),
                                                ms.store().getAvgRating(),
                                                ms.menuItem().getId(),
                                                ms.menuItem().getName(),
                                                ms.menuItem().getPrice().intValue(),
                                                ms.menuItem().getDescription()))
                        .toList();

        // 4. Generate AI Message using ChatClient
        String prompt = buildPrompt(request.transcribedText(), top3);
        String aiMessage = "추천할 메뉴를 찾았어요!";

        try {
            aiMessage = chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.error("Failed to generate AI message", e);
        }

        return new VoiceRecommendationResponse(aiMessage, top3);
    }

    private String buildPrompt(String userText, List<RecommendedMenu> menus) {
        StringBuilder sb = new StringBuilder();
        sb.append("사용자는 다음과 같이 말했습니다: \"").append(userText).append("\"\n");
        sb.append("데이터베이스에서 다음 3개의 메뉴를 추천 음식으로 찾았습니다:\n");
        for (int i = 0; i < menus.size(); i++) {
            RecommendedMenu m = menus.get(i);
            sb.append(i + 1)
                    .append(". [")
                    .append(m.storeName())
                    .append("] 의 ")
                    .append(m.menuName())
                    .append(" (평점: ")
                    .append(m.rating())
                    .append(")\n");
        }
        sb.append("\n위의 내용을 바탕으로 사용자에게 친절하게 음성으로 읽어줄 1~2문장의 자연스러운 추천 멘트를 작성해 주세요.");
        return sb.toString();
    }
}
