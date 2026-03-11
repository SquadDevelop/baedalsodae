# 배달소대 AI 서비스 소개

본 문서는 **baedalsodae** 프로젝트에 적용된 AI 서비스의 기능 및 기술적 세부 사항을 설명합니다.

---

## 1. 기능 설명 (Feature Description)

### 1.1. 음성 기반 맞춤형 메뉴 추천 (Voice Recommendation)
사용자의 음성 입력(Text로 변환된 텍스트)을 분석하여, 사용자가 원하는 조건에 가장 잘 부합하는 배달 메뉴를 스마트하게 추천해주는 기능입니다.

*   **자연어 의도 파악**: 단순한 키워드가 아닌 자연스러운 문장(예: "매콤하고 국물 있는 요리 찾아줘" 등)을 통해 메뉴 검색이 가능합니다.
*   **복합적 추천 알고리즘**: 메뉴의 의미론적 유사도(Semantic Search)뿐만 아니라, **가게 평점** 및 **메뉴 인기 여부**를 복합적으로 고려하여 최적의 Top 3 메뉴를 선정합니다.
*   **자연스러운 AI 응답 생성**: 추천된 3개의 메뉴를 바탕으로 실제 대화하는 것처럼 자연스럽고 친절한 1~2문장의 추천 멘트를 AI가 생성하여 음성 응답(TTS)용으로 함께 제공합니다.

### 1.2. 메뉴 정보 벡터화 매니지먼트 (Menu Vectorization & Sync)
정확한 의미 기반 검색을 위해 시스템 내부의 메뉴 정보를 다차원 벡터 형태로 저장하고 관리하는 백그라운드 기능입니다.

*   가게명, 카테고리, 메뉴명, 설명, 가격 및 태그 정보를 하나의 문맥으로 통합하여 벡터화(Embedding)합니다.
*   메뉴 관리에 따른 데이터 싱크를 맞추어 항상 최신 상태의 검색 품질을 유지합니다.

---

## 2. 기술 설명 (Technical Description)

본 프로젝트는 **Spring AI** 생태계를 활용하여 대형 언어 모델(LLM) 및 벡터 데이터베이스(Vector Database)를 매끄럽게 통합하였습니다.

### 2.1. 사용 기술 스택
*   **프레임워크**: Spring Boot 3.x, Spring AI (`spring-ai-bom:1.0.0-M3`)
*   **LLM (대규모 언어 모델)**: OpenAI (`spring-ai-openai-spring-boot-starter`)
    *   **Text Embedding**: 텍스트 형태의 메뉴 정보를 고차원 벡터로 변환합니다.
    *   **Chat Completion**: 자연스러운 추천 멘트를 생성하는데 활용됩니다 (`ChatClient`).
*   **Vector Database**: PostgreSQL 기반 pgvector (`spring-ai-pgvector-store-spring-boot-starter`)
    *   메뉴 임베딩 벡터 데이터를 저장하고 높은 성능의 코사인/L2 거리 기반 유사도 검색(Similarity Search)을 수행합니다.

### 2.2. 핵심 컴포넌트 및 동작 흐름

#### A. 메뉴 데이터 임베딩 파이프라인 (`MenuEmbeddingService.java`)
1.  **데이터 구성**: 활성 상태의 메뉴(`MenuItem`) 정보를 바탕으로 아래 형식의 텍스트 뭉치를 생성합니다.
    *   `[가게명] {storeName} - [카테고리] {category} - [메뉴명] {menu} - [설명] {description} - [가격] {price}원 - [태그] {tags}`
2.  **메타데이터 부착**: 필터링 및 조회를 위해 `menuId`, `storeId`, `isPopular` 속성을 메타데이터로 부착한 `Document` 객체를 생성합니다.
3.  **Vector Store 저장**: 생성된 `Document` 객체 리스트를 `VectorStore` 인스턴스에 전달하여 OpenAI 임베딩 서버를 거친 후 pgvector 저장소에 동기화합니다.

#### B. 메뉴 추천 및 AI 응답 처리 로직 (`RecommendationService.java`)
1.  **벡터 기반 유사도 검색 (Vector Search)**
    *   사용자의 입력 텍스트(`transcribedText`)를 기반으로 `VectorStore.similaritySearch()` 함수를 호출합니다.
    *   의미적으로 가장 유사도가 높은 상위 10개의 메뉴(`Document`)를 먼저 추출합니다.
2.  **데이터베이스 조회 및 가중치 스코어링 (Scoring)**
    *   추출된 10개의 메뉴 ID 값으로 실제 RDB에서 엔티티를 조회합니다.
    *   단순 벡터 유사도를 넘어 비즈니스 룰에 따른 추가 스코어링을 수행합니다.
        *   **가중치 공식**: `Score = (가게 평균 평점 * 2.0) + (인기 메뉴 여부 ? 1.0 : 0.0)`
3.  **최종 결과 도출 및 AI 프롬프팅**
    *   산출된 스코어 점수를 기준으로 내림차순 정렬하여 최종 Top 3 메뉴를 선택합니다.
    *   사용자의 원본 질의 내용과 Top 3 추천 메뉴 정보(가게명, 메뉴명, 평점)를 엮어 AI에게 전달할 **프롬프트(Prompt)** 를 조립합니다.
    *   `ChatClient` 시스템을 호출하여 프롬프트를 전송하고, 사용자에게 제공할 1~2문장의 응답 메시지 문자열을 반환 받습니다.
4.  **응답 반환**: 생성된 답변 메시지와 상위 3개의 추천 메뉴 상세 리스트를 DTO(`VoiceRecommendationResponse`)에 담아 클라이언트에 전달합니다.
