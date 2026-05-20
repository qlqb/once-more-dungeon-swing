package com.spiregame.model.card;

public enum CardType {
    ATTACK("공격", "⚔️", "#e74c3c"),
    SKILL("스킬",  "🛡️", "#3498db"),
    POWER("파워",  "✨", "#9b59b6"),
    STATUS("상태", "💫", "#95a5a6"),
    CURSE("저주",  "💀", "#2c3e50");

    private final String emoji;
    private final String displayName;
    private final String color;

    CardType(String displayName, String emoji, String color) {
        this.emoji=emoji;
        this.displayName = displayName;
        this.color = color;
    }

    public String getEmoji() { return emoji; }
    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
}
