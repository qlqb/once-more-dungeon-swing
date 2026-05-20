package com.spiregame.model.enemy;

public class EnemyIntent {
    public enum IntentType {
        ATTACK("⚔️ 공격"),
        DEFEND("🛡️ 방어"),
        BUFF("✨ 강화"),
        DEBUFF("💀 약화"),
        UNKNOWN("❓ ???");

        private final String display;
        IntentType(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }

    private final IntentType type;
    private final int value;
    private final String detail;

    public EnemyIntent(IntentType type, int value, String detail) {
        this.type = type;
        this.value = value;
        this.detail = detail;
    }

    public IntentType getType() { return type; }
    public int getValue() { return value; }
    public String getDetail() { return detail; }

    @Override
    public String toString() {
        return type.getDisplay() + (value > 0 ? " " + value : "") + (detail != null ? " " + detail : "");
    }
}
