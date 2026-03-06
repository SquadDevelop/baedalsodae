package com.project.baedalsodae.menu.fixture;

public final class MenuTestConstants {

  private MenuTestConstants() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  // orderNo
  public static final int FIRST_ORDER_NUMBER = 1;
  public static final int SECOND_ORDER_NUMBER = 2;
  public static final int THIRD_ORDER_NUMBER = 3;
  public static final int EXISTING_MAX_ORDER_NUMBER = 2;

  // MenuItem
  public static final String DEFAULT_MENU_ITEM_NAME = "후라이드치킨";
  public static final String DEFAULT_ITEM_DESCRIPTION = "바삭한 후라이드";
  public static final int DEFAULT_ITEM_PRICE = 18000;

  // Alternative MenuItem
  public static final String ALTERNATIVE_MENU_ITEM_NAME = "양념치킨";
  public static final String ALTERNATIVE_ITEM_DESCRIPTION = "달콤한 양념";
  public static final int ALTERNATIVE_ITEM_PRICE = 19000;

  // MenuCategory
  public static final String DEFAULT_CATEGORY_NAME = "치킨류";
}
