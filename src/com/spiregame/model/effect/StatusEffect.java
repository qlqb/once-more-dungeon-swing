package com.spiregame.model.effect;

public enum StatusEffect {
    // 상태 효과의 "종류"만 enum으로 정의한다.
    // 실제 수치는 Player/Enemy의 Map<StatusEffect, Integer>에 저장된다.
    // isBuff가 false인 효과는 Player.tickStatusEffects()에서 턴마다 감소한다.
    STRENGTH("근력", "공격력 증가", true),
    DEXTERITY("민첩", "방어력 증가", true),
    BLOCK("방어", "피해 감소", true),
    VULNERABLE("취약", "받는 피해 50% 증가", false),
    WEAK("약화", "가하는 피해 25% 감소", false),
    FRAIL("허약", "방어력 25% 감소", false),
    POISON("독", "매 턴 피해", false),
    BURN("화상", "매 턴 피해", false),
    THORNS("가시", "공격 시 반격 피해", true),
    REGEN("재생", "매 턴 체력 회복", true),
    DRAW_CARD("드로우", "추가 카드 드로우", true),
    ENERGY_UP("에너지 증가", "에너지 추가", true);

    private final String displayName;
    private final String description;
    private final boolean isBuff;

    StatusEffect(String displayName, String description, boolean isBuff) {
        this.displayName = displayName;
        this.description = description;
        this.isBuff = isBuff;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public boolean isBuff() { return isBuff; }
}
