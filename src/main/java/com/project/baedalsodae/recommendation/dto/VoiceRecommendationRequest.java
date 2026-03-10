package com.project.baedalsodae.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VoiceRecommendationRequest(
        @NotBlank(message = "음성 텍스트는 필수입니다") String transcribedText, @NotNull UUID userId) {}
