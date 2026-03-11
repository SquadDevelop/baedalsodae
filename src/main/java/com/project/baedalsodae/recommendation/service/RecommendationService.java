package com.project.baedalsodae.recommendation.service;

import com.project.baedalsodae.recommendation.dto.VoiceRecommendationRequest;
import com.project.baedalsodae.recommendation.dto.VoiceRecommendationResponse;
import com.project.baedalsodae.recommendation.dto.VoiceRecommendationResponse.RecommendedMenu;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ChatClient chatClient;
    private final MenuSearchService menuSearchService;

    public VoiceRecommendationResponse recommendMenuItems(
            VoiceRecommendationRequest request, UUID userId) {
        // 1. 읽기 트랜잭션 (빠르게 종료)
        List<RecommendedMenu> top3 =
                menuSearchService.recommendMenuItems(request.transcribedText());

        if (top3.isEmpty()) {
            return new VoiceRecommendationResponse("죄송해요, 원하시는 조건에 맞는 메뉴를 찾지 못했어요.", List.of());
        }

        // 2. AI 호출 + ChatMemory 저장 (트랜잭션 없음)
        String aiMessage = "추천할 메뉴를 찾았어요!";
        try {
            aiMessage =
                    chatClient
                            .prompt()
                            .user(buildPrompt(request.transcribedText(), top3))
                            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId.toString()))
                            .call()
                            .content();
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
        sb.append("\n위의 내용을 바탕으로 사용자에게 친절하게 음성으로 읽어줄 1~2문장의 자연스러운 추천 멘트를 작성해 주세요.\n");
        sb.append("[출력 조건] 추천 멘트는 **100자**를 넘을 수 없습니다. ");
        return sb.toString();
    }
}
