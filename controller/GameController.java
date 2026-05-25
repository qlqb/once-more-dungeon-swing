package com.spiregame.controller;

import com.spiregame.model.card.Card;
import com.spiregame.model.card.CardLibrary;
import com.spiregame.model.enemy.*;
import com.spiregame.model.map.MapGenerator;
import com.spiregame.model.map.MapNode;
import com.spiregame.model.map.NodeType;
import com.spiregame.model.player.Player;

import java.util.*;
import java.util.function.Consumer;

/**
 * ──────────────────────────────────────────────────────────────────
 * GameController  —  게임 전체 흐름을 관리하는 컨트롤러
 * ──────────────────────────────────────────────────────────────────
 *
 * [책임]
 *   - 게임 시작 / 종료
 *   - 맵 생성 및 현재 위치(selectedNode) 관리
 *   - 노드 입장 처리: 노드 타입에 따라 적절한 Phase로 전환
 *   - 전투 생성 및 결과 처리
 *   - 카드 보상 / 상점 / 휴식 처리
 *
 * [기존 코드와 달라진 점]
 *   Before : int floor 하나로 현재 위치 표현
 *            advanceFloor() → floor++
 *
 *   After  : MapNode selectedNode 로 현재 위치 표현
 *            enterNode(MapNode) → 노드 타입 분기 처리
 *
 *   GamePhase는 그대로 유지했습니다.
 *   BattleController, 카드, 적 코드는 전혀 변경하지 않았습니다.
 */
public class GameController {

    // ── 게임 단계(페이즈) 열거형 ──────────────────────────────────
    /**
     * 현재 플레이어가 어떤 화면에 있는지를 나타냅니다.
     * SpireGameApp이 이 값을 보고 적절한 Scene을 화면에 표시합니다.
     */
    public enum GamePhase {
        MAIN_MENU,    // 타이틀 화면
        DECK_EDITOR,  // 게임 시작 전 덱 구성 화면  ← 신규
        MAP,          // 분기 맵 화면
        BATTLE,       // 전투 화면
        CARD_REWARD,  // 전투 후 카드 선택
        SHOP,         // 상점
        REST,         // 휴식 (HP 회복 또는 카드 업그레이드)
        TREASURE,     // 보물 (카드 즉시 획득)
        DECK_BUILDER, // 게임 중 덱 열람/편집
        GAME_OVER,    // 패배 화면
        VICTORY       // 승리 화면
    }

    // ── 필드 ──────────────────────────────────────────────────────

    /** 플레이어 상태 (HP, 덱, 골드 등) */
    private Player player;

    /** 현재 게임 단계 */
    private GamePhase phase;

    /**
     * 분기 맵 데이터.
     * floors.get(0) → 1층 노드 목록
     * floors.get(14) → 15층(보스) 노드 목록
     */
    private List<List<MapNode>> mapFloors;

    /**
     * 플레이어가 현재 있는(또는 방금 선택한) 노드.
     * 노드 타입에 따라 다른 이벤트가 발생합니다.
     */
    private MapNode selectedNode;

    /** 진행 중인 전투 컨트롤러 (전투 씬에서만 유효) */
    private BattleController currentBattle;

    /** 카드 보상 후보 목록 (카드 보상 씬에서만 유효) */
    private List<Card> currentRewardCards;

    /**
     * 페이즈가 바뀔 때 SpireGameApp에 알려주는 콜백.
     * 직접 참조 대신 콜백을 쓰는 이유:
     *   Controller가 View(JavaFX)를 import하지 않아도 되어
     *   테스트와 재사용이 쉬워집니다.
     */
    private Consumer<GamePhase> phaseChangeCallback;

    // ── 생성자 ────────────────────────────────────────────────────
    public GameController() {
        phase = GamePhase.MAIN_MENU;
    }

    // ══════════════════════════════════════════════════════════════
    //  게임 시작
    // ══════════════════════════════════════════════════════════════

    /**
     * 새 게임을 시작합니다.
     *
     * [처리 순서]
     *   1. 플레이어 생성 및 스타터 덱 지급
     *   2. MapGenerator로 분기 맵 생성
     *   3. MAP 페이즈로 전환 → MapScene이 표시됨
     */
    /**
     * 메인메뉴의 "새 게임 시작" 버튼 클릭 시 호출됩니다.
     * 덱 에디터 화면으로 전환하고, 플레이어는 아직 생성하지 않습니다.
     * (플레이어는 에디터에서 덱을 확정한 뒤 startNewGame(deck)에서 생성됩니다.)
     */
    public void startNewGame() {
        setPhase(GamePhase.DECK_EDITOR);
    }

    /**
     * 덱 에디터에서 "게임 시작" 버튼 클릭 시 호출됩니다.
     * 에디터에서 구성한 덱을 받아서 실제 게임을 시작합니다.
     *
     * @param customDeck 덱 에디터에서 플레이어가 구성한 카드 목록
     */
    public void startNewGame(List<Card> customDeck) {
        // 플레이어 생성 후 커스텀 덱 지급
        player = new Player(80);
        for (Card c : customDeck) {
            player.addCardToDeck(c);
        }

        // 분기 맵 생성
        mapFloors = new MapGenerator().generate();
        selectedNode = null;

        setPhase(GamePhase.MAP);
    }

    /**
     * 게임 중 "덱 편집" 버튼 클릭 시 호출됩니다.
     * 현재 게임 상태를 유지한 채 덱 에디터로 전환합니다.
     */
    public void openDeckEditor() {
        setPhase(GamePhase.DECK_BUILDER); // 게임 중 편집은 DECK_BUILDER 페이즈 재사용
    }

    /**
     * 게임 중 덱 에디터에서 "저장하고 돌아가기" 클릭 시 호출됩니다.
     * 현재 플레이어의 덱을 편집된 덱으로 교체합니다.
     *
     * [왜 덱 전체를 교체하는가?]
     *   부분 수정(추가/제거)보다 전체 교체가 버그가 적습니다.
     *   에디터는 항상 완성된 덱을 넘기므로 일관성이 보장됩니다.
     *
     * @param newDeck 편집된 카드 목록
     */
    public void applyEditedDeck(List<Card> newDeck) {
        if (player == null) return;
        // 기존 덱 전부 제거
        List<Card> current = new ArrayList<>(player.getDeck());
        for (Card c : current) player.removeCardFromDeck(c);
        // 새 덱 추가
        for (Card c : newDeck) player.addCardToDeck(c);
        setPhase(GamePhase.MAP);
    }

    /** 덱 에디터에서 취소하고 메인메뉴로 돌아갑니다. */
    public void goToMainMenu() {
        player = null;
        mapFloors = null;
        selectedNode = null;
        setPhase(GamePhase.MAIN_MENU);
    }

    // ══════════════════════════════════════════════════════════════
    //  노드 입장 — 핵심 변경 지점
    // ══════════════════════════════════════════════════════════════

    /**
     * 플레이어가 맵에서 노드를 클릭했을 때 호출됩니다.
     *
     * [왜 enterBattle(), enterShop() 대신 enterNode()인가?]
     *   기존에는 왼쪽 패널에 "전투 시작", "상점 방문" 버튼이 따로 있었고
     *   모든 층이 동일한 이벤트를 가졌습니다.
     *   분기 맵에서는 노드마다 타입이 다르므로,
     *   클릭된 노드의 타입을 보고 알아서 분기하는 방식이 더 자연스럽습니다.
     *
     * @param node 플레이어가 클릭한 노드
     */
    public void enterNode(MapNode node) {
        if (!node.isAvailable()) return; // 선택 불가 노드 방어

        this.selectedNode = node;

        switch (node.getType()) {
            case BATTLE, ELITE -> startBattle(node);
            case SHOP          -> setPhase(GamePhase.SHOP);
            case REST          -> setPhase(GamePhase.REST);
            case TREASURE      -> startTreasure();
            case BOSS          -> startBattle(node);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  전투
    // ══════════════════════════════════════════════════════════════

    /**
     * enterNode에서 타고 들어와서 현재 배틀 객체 생성하고 저장
     * 노드 타입(일반/엘리트/보스)에 맞는 적을 생성하고 전투를 시작합니다.
     */
    private void startBattle(MapNode node) {
        Enemy enemy = generateEnemy(node);
        currentBattle = new BattleController(player, enemy);
        setPhase(GamePhase.BATTLE);
        currentBattle.startBattle();
    }

    /**
     * 전투 승리 시 BattleScene → GameController 순으로 호출됩니다.
     *
     * [처리 순서]
     *   1. 골드 지급
     *   2. 노드 방문 처리 → 자식 노드 활성화
     *   3. 보스 격파 → 승리, 아니면 카드 보상으로
     */
    public void onBattleVictory() {
        int floor = selectedNode != null ? selectedNode.getFloor() : 1;
        int gold  = 10 + floor * 5 + new Random().nextInt(15);
        // 엘리트/보스는 추가 골드
        if (selectedNode != null &&
            (selectedNode.getType() == NodeType.ELITE ||
             selectedNode.getType() == NodeType.BOSS)) {
            gold += 30;
        }
        player.addGold(gold);

        // 방문 처리 → 자식 노드들이 클릭 가능해짐
        if (selectedNode != null) selectedNode.visit();

        if (selectedNode != null && selectedNode.getType() == NodeType.BOSS) {
            setPhase(GamePhase.VICTORY);
            return;
        }

        // 카드 보상 3장 생성
        List<Card> pool = CardLibrary.getCardRewardPool(floor);
        Collections.shuffle(pool);
        currentRewardCards = new ArrayList<>(pool.subList(0, Math.min(3, pool.size())));
        setPhase(GamePhase.CARD_REWARD);
    }

    public void onBattleDefeat() {
        setPhase(GamePhase.GAME_OVER);
    }

    // ══════════════════════════════════════════════════════════════
    //  카드 보상
    // ══════════════════════════════════════════════════════════════

    /** 카드 보상에서 카드를 선택했을 때. card=null이면 건너뛰기. */
    public void selectRewardCard(Card card) {
        if (card != null) player.addCardToDeck(card.copy());
        currentRewardCards = null;
        setPhase(GamePhase.MAP);
    }

    public void skipReward() {
        currentRewardCards = null;
        setPhase(GamePhase.MAP);
    }

    // ══════════════════════════════════════════════════════════════
    //  보물 노드
    // ══════════════════════════════════════════════════════════════

    /**
     * 보물 노드: 전투 없이 카드 1장을 즉시 획득합니다.
     */
    private void startTreasure() {
        if (selectedNode != null) selectedNode.visit();
        List<Card> pool = CardLibrary.getCardRewardPool(
            selectedNode != null ? selectedNode.getFloor() : 1);
        Collections.shuffle(pool);
        currentRewardCards = new ArrayList<>(pool.subList(0, Math.min(1, pool.size())));
        setPhase(GamePhase.CARD_REWARD);
    }

    // ══════════════════════════════════════════════════════════════
    //  휴식 노드
    // ══════════════════════════════════════════════════════════════

    /** 휴식에서 "HP 회복" 선택 → 최대 HP의 30% 회복 */
    public void restHeal() {
        player.heal((int)(player.getMaxHp() * 0.30));
        finishRestNode();
    }

    /** 휴식에서 "카드 업그레이드" 선택 */
    public void restUpgrade(Card card) {
        card.upgrade();
        finishRestNode();
    }

    private void finishRestNode() {
        if (selectedNode != null) selectedNode.visit();
        setPhase(GamePhase.MAP);
    }

    // ══════════════════════════════════════════════════════════════
    //  상점
    // ══════════════════════════════════════════════════════════════

    /** 상점을 나갈 때 — 방문 처리 후 맵 복귀 */
    public void exitShop() {
        if (selectedNode != null) selectedNode.visit();
        setPhase(GamePhase.MAP);
    }

    public List<Card> getShopCards() {
        int floor = selectedNode != null ? selectedNode.getFloor() : 1;
        List<Card> pool = CardLibrary.getCardRewardPool(floor);
        Collections.shuffle(pool);
        return new ArrayList<>(pool.subList(0, Math.min(5, pool.size())));
    }

    public int getCardShopPrice(Card card) {
        return switch (card.getRarity()) {
            case COMMON   -> 50  + new Random().nextInt(20);
            case UNCOMMON -> 80  + new Random().nextInt(30);
            case RARE     -> 130 + new Random().nextInt(50);
            default       -> 30;
        };
    }

    public boolean buyCard(Card card, int price) {
        if (player.spendGold(price)) { player.addCardToDeck(card.copy()); return true; }
        return false;
    }

    public boolean removeCardFromDeck(Card card, int price) {
        if (player.spendGold(price)) { player.removeCardFromDeck(card); return true; }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    //  덱 빌더
    // ══════════════════════════════════════════════════════════════

    /** 지도에서 "덱 보기/편집" 버튼 클릭 → 게임 중 덱 에디터 */
    public void openDeckBuilder()  { setPhase(GamePhase.DECK_BUILDER); }

    /** 게임 중 덱 에디터에서 취소 → 지도로 복귀 */
    public void closeDeckBuilder() { setPhase(GamePhase.MAP); }

    // ══════════════════════════════════════════════════════════════
    //  적 생성
    // ══════════════════════════════════════════════════════════════

    /**
     * 노드 타입과 층 번호를 함께 고려해 적을 생성합니다.
     *
     * ELITE 노드는 같은 층의 일반 전투보다 강한 적이 나옵니다.
     */
    private Enemy generateEnemy(MapNode node) {
        int floor = node.getFloor();

        if (node.getType() == NodeType.BOSS) {
            return new Random().nextBoolean() ? new Enemies.Dragon() : new Enemies.Demon();
        }

        if (node.getType() == NodeType.ELITE) {
            return new Random().nextBoolean() ? new Enemies.Mage() : new Enemies.StoneWarrior();
        }

        // 일반 전투: 층 구간별
        if (floor >= 10) {
            return switch (new Random().nextInt(3)) {
                case 0  -> new Enemies.Mage();
                case 1  -> new Enemies.StoneWarrior();
                default -> new Enemies.SkeletonArcher();
            };
        }
        if (floor >= 5) {
            return new Random().nextBoolean() ? new Enemies.SkeletonArcher() : new Enemies.Mage();
        }
        return new Random().nextBoolean() ? new Enemies.Goblin() : new Enemies.SkeletonArcher();
    }

    // ══════════════════════════════════════════════════════════════
    //  맵 조회 — MapScene이 사용
    // ══════════════════════════════════════════════════════════════

    /** 전체 맵 데이터 (읽기 전용) */
    public List<List<MapNode>> getMapFloors() {
        return mapFloors != null ? Collections.unmodifiableList(mapFloors) : Collections.emptyList();
    }

    /** 현재 선택된 노드 */
    public MapNode getSelectedNode() { return selectedNode; }

    /** 현재 층 번호 (선택된 노드 없으면 0) */
    public int getCurrentFloor() {
        return selectedNode != null ? selectedNode.getFloor() : 0;
    }

    public int getTotalFloors() { return MapGenerator.TOTAL_FLOORS; }

    // ── 일반 Getter ───────────────────────────────────────────────
    public Player           getPlayer()             { return player; }
    public GamePhase        getPhase()              { return phase; }
    public List<Card>       getCurrentRewardCards() { return currentRewardCards; }
    public BattleController getCurrentBattle()      { return currentBattle; }

    // ── 페이즈 전환 ───────────────────────────────────────────────
    private void setPhase(GamePhase p) {
        this.phase = p;
        if (phaseChangeCallback != null) phaseChangeCallback.accept(p);
    }

    public void setPhaseChangeCallback(Consumer<GamePhase> cb) {
        this.phaseChangeCallback = cb;
    }
}
