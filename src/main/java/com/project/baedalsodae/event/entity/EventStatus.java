package com.project.baedalsodae.event.entity;

public enum EventStatus {
    PENDING, // 이벤트가 생성된 상태
    PUBLISHED, // 이벤트가 성공적으로 발행된 상태
    FAILED // 이벤트 발행이 실패한 상태
}
