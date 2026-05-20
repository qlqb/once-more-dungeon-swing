package com.spiregame.model.map;

/**
 * ──────────────────────────────────────────────────────────────────
 * NodeType  —  맵 위 노드(칸)의 종류를 나타내는 열거형
 * ──────────────────────────────────────────────────────────────────
 *
 * [왜 enum으로 만들었나?]
 *   노드 종류는 "전투", "상점" 같은 고정된 범주이므로
 *   String 대신 enum을 쓰면 오타 위험이 없고,
 *   switch 문에서 컴파일러가 빠진 케이스를 잡아줍니다.
 *
 * [각 필드 역할]
 *   displayName : 화면에 표시할 한글 이름
 *   emoji       : 맵 노드 위에 그릴 아이콘
 *   color       : 노드 원의 배경색 (16진수 색상 코드)
 */
public enum NodeType {

    BATTLE  ("전투",   "⚔️",  "#c0392b"),   // 일반 전투
    ELITE   ("엘리트", "💀",  "#8e44ad"),   // 강력한 적, 보상이 좋음
    SHOP    ("상점",   "🛒",  "#1a5276"),   // 카드 구매 / 제거
    REST    ("휴식",   "🔥",  "#145a32"),   // HP 회복 또는 카드 업그레이드
    TREASURE("보물",   "💎",  "#b7950b"),   // 카드를 즉시 획득
    BOSS    ("보스",   "👑",  "#4a235a");   // 각 막의 마지막 노드

    // ── 필드 ──────────────────────────────────────────────────────
    private final String displayName;
    private final String emoji;
    private final String color;

    // ── 생성자 ────────────────────────────────────────────────────
    NodeType(String displayName, String emoji, String color) {
        this.displayName = displayName;
        this.emoji       = emoji;
        this.color       = color;
    }

    // ── Getter ────────────────────────────────────────────────────
    public String getDisplayName() { return displayName; }
    public String getEmoji()       { return emoji; }
    public String getColor()       { return color; }
}
