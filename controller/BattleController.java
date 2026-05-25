package com.spiregame.controller;

import com.spiregame.model.card.Card;
import com.spiregame.model.effect.EffectContext;
import com.spiregame.model.enemy.Enemy;
import com.spiregame.model.player.Player;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class BattleController {

    // 전투는 아래 네 상태 중 하나로만 진행된다.
    // UI는 이 값을 보고 카드 사용 가능 여부와 승리/패배 처리를 판단한다.
    public enum BattleState { PLAYER_TURN, ENEMY_TURN, VICTORY, DEFEAT }

    private final Player player;
    private final Enemy enemy;
    private BattleState state;
    private int turnNumber;

    // 특정 카드가 켜는 "이번 전투 한정" 지속 효과들이다.
    // BattleController는 전투마다 새로 생성되므로 다음 전투까지 유지되지 않는다.
    private boolean barricade;
    private boolean brutality;
    private int bonusEnergy;

    // 전투 로직은 Swing UI를 직접 몰라야 한다.
    // 대신 콜백으로 "로그를 남겨라", "화면을 갱신해라"만 요청한다.
    private Consumer<String> logCallback;
    private Runnable uiRefreshCallback;

    public BattleController(Player player, Enemy enemy) {
        this.player = player;
        this.enemy = enemy;
        this.state = BattleState.PLAYER_TURN;
        this.turnNumber = 0;
        this.barricade = false;
        this.brutality = false;
        this.bonusEnergy = 0;
    }

    public void startBattle() {
        // 전투 시작 시 플레이어의 전투용 카드 더미를 초기화하고,
        // 적의 첫 의도를 정한 뒤 플레이어 턴을 시작한다.
        player.startBattle();
        enemy.planIntent();
        startPlayerTurn();
    }

    private void startPlayerTurn() {
        // 플레이어 턴 시작의 전체 흐름:
        // 턴 번호 증가 -> 상태 변경 -> 방어도 유지 여부 처리 -> 지속 효과 처리 -> 플레이어 턴 준비 -> UI 갱신.
        turnNumber++;
        state = BattleState.PLAYER_TURN;

        if (brutality) {
            player.takeDamage(1);
            player.drawCard();
        }

        player.startTurn();
        if (bonusEnergy > 0) {
            // Bonus energy is applied after startTurn which already set energy
            bonusEnergy = 0;
        }

        log("═══ 턴 " + turnNumber + " 시작 ═══");

        if (!player.isAlive()) {
            endBattle(false);
            return;
        }

        refresh();
    }

    public boolean playCard(Card card) {
        // 카드 사용은 반드시 플레이어 턴에서만 가능하다.
        if (state != BattleState.PLAYER_TURN) return false;

        // 1. 에너지 검사
        int cost = card.getCost();
        if (player.getEnergy() < cost) {
            log("❌ 에너지가 부족합니다!");
            return false;
        }

        // 2. Player가 에너지를 차감하고 손패에서 카드를 제거한다.
        //    카드는 소멸 여부에 따라 exhaustPile 또는 discardPile로 이동한다.
        boolean removed = player.playCard(card, cost);
        if (!removed) return false;

        // 3. 카드 효과 실행에 필요한 객체들을 EffectContext로 묶어 넘긴다.
        EffectContext ctx = new EffectContext(player, enemy, this);
        card.play(ctx);

        log("🃏 " + card.getName() + " 사용");

        // 4. 카드 효과로 적/플레이어가 죽었는지 확인한다.
        checkBattleEnd();

        // 전투가 끝났다면 endBattle() 안에서 이미 refresh()가 호출된다.
        // 여기서 또 refresh()하면 BattleScene의 승리/패배 처리가 중복 실행될 수 있다.
        if (state == BattleState.PLAYER_TURN) {
            refresh();
        }
        return true;
    }

    public void endPlayerTurn() {
        if (state != BattleState.PLAYER_TURN) return;
        player.endTurn();
        state = BattleState.ENEMY_TURN;
        log("▶ 적의 턴");
        executeEnemyTurn();
    }

    private void executeEnemyTurn() {
        // 적 턴 흐름:
        // 적 턴 시작 효과 -> 적 생존 확인 -> 적 행동 실행 -> 플레이어 생존 확인
        // -> 다음 의도 계획 -> 다시 플레이어 턴.
        enemy.startTurn();
        if (!enemy.isAlive()) { endBattle(true); return; }

        enemy.executeIntent(player);
        log("👹 " + enemy.getName() + ": " + enemy.getCurrentIntent());

        if (!player.isAlive()) { endBattle(false); return; }

        enemy.planIntent();
        startPlayerTurn();
    }

    private void checkBattleEnd() {
        // 승패 판정은 전투 상태를 VICTORY 또는 DEFEAT로 바꾸는 관문이다.
        if (!enemy.isAlive()) endBattle(true);
        else if (!player.isAlive()) endBattle(false);
    }

    private void endBattle(boolean playerWon) {
        // 상태를 먼저 바꾼 뒤 refresh()를 호출해야 BattleScene이 승리/패배 상태를 읽을 수 있다.
        state = playerWon ? BattleState.VICTORY : BattleState.DEFEAT;
        log(playerWon ? "🏆 승리!" : "💀 패배...");
        refresh();
    }

    // ─── Special Card Effects ─────────────────────────────────────
    public void upgradeRandomHandCard() {
        List<Card> hand = player.getHand();
        List<Card> mutable = new java.util.ArrayList<>(hand);
        Collections.shuffle(mutable);
        mutable.stream()
            .filter(c -> !c.isUpgraded())
            .findFirst()
            .ifPresent(Card::upgrade);
    }

    public void setBarricade(boolean b) { this.barricade = b; }
    public void setBrutality(boolean b) { this.brutality = b; }
    public void gainBonusEnergy(int amount) { this.bonusEnergy += amount; }

    // ─── Callbacks ────────────────────────────────────────────────
    // 콜백이 없으면 아무 것도 하지 않는다. 그래서 테스트나 초기화 중에도 안전하다.
    private void log(String msg) { if (logCallback != null) logCallback.accept(msg); }
    private void refresh() { if (uiRefreshCallback != null) uiRefreshCallback.run(); }

    public void setLogCallback(Consumer<String> cb) { this.logCallback = cb; }
    public void setUiRefreshCallback(Runnable cb) { this.uiRefreshCallback = cb; }

    // ─── Getters ─────────────────────────────────────────────────
    public Player getPlayer() { return player; }
    public Enemy getEnemy() { return enemy; }
    public BattleState getState() { return state; }
    public int getTurnNumber() { return turnNumber; }
    public boolean isBarricade() { return barricade; }
}
