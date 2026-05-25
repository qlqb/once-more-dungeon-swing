package com.spiregame.model.card;

public enum CardRarity {
    BASIC("기본", "#7f8c8d"),
    COMMON("일반", "#bdc3c7"),
    UNCOMMON("고급", "#27ae60"),
    RARE("희귀", "#f39c12"),
    EPIC("서사", "#8e44ad");

    private final String displayName;
    private final String color;

    CardRarity(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
}
