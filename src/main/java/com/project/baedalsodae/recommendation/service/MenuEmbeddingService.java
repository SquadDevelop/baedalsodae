package com.project.baedalsodae.recommendation.service;

import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.repository.MenuItemRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuEmbeddingService {

    private final MenuItemRepository menuItemRepository;
    private final VectorStore vectorStore;

    @Transactional
    public void syncAllMenuItemsToVectorStore() {
        log.info("Starting synchronization of all menu items to VectorStore.");
        List<MenuItem> allMenus = menuItemRepository.findAll();

        List<Document> documents =
                allMenus.stream()
                        .filter(menu -> !menu.isDeleted())
                        .map(this::createDocument)
                        .collect(Collectors.toList());

        if (!documents.isEmpty()) {
            vectorStore.accept(documents);
            log.info("Successfully synchronized {} menu items to VectorStore.", documents.size());
        }
    }

    @Transactional
    public void syncMenuItem(MenuItem menuItem) {
        if (menuItem.isDeleted()) {
            vectorStore.delete(List.of(menuItem.getId().toString()));
            return;
        }
        Document doc = createDocument(menuItem);
        vectorStore.accept(List.of(doc));
    }

    private Document createDocument(MenuItem menuItem) {
        String storeName = menuItem.getMenuCategory().getStore().getName();
        String categoryName = menuItem.getMenuCategory().getName();
        String tags = menuItem.getTagMappings().stream()
                .map(mapping -> mapping.getTag().getName())
                .collect(Collectors.joining(", "));

        String content =
                String.format(
                        "[가게명] %s - [카테고리] %s - [메뉴명] %s - [설명] %s - [가격] %d원 - [태그] %s",
                        storeName,
                        categoryName,
                        menuItem.getName(),
                        menuItem.getDescription() != null ? menuItem.getDescription() : "",
                        menuItem.getPrice(),
                        tags.isEmpty() ? "없음" : tags);

        Map<String, Object> metadata =
                Map.of(
                        "menuId", menuItem.getId().toString(),
                        "storeId", menuItem.getMenuCategory().getStore().getId().toString(),
                        "isPopular", menuItem.isPopular());

        return new Document(menuItem.getId().toString(), content, metadata);
    }
}
