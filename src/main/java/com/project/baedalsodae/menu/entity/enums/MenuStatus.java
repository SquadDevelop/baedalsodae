package com.project.baedalsodae.menu.entity.enums;

public enum MenuStatus {
  AVAILABLE("판매중"),
  UNAVAILABLE("판매중지"),
  SOLD_OUT("품절"),
  PREPARING("준비중"),
  HIDDEN("숨김");

  private final String description;

  MenuStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
