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

    public enum BattleState { PLAYER_TURN, ENEMY_TURN, VICTORY, DEFEAT }

    private final Player player;
    private final Enemy enemy;
    private BattleState state;
    private int turnNumber;
    private boolean barricade;
    private boolean brutality;
    private int bonusEnergy;

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
        player.startBattle();
        enemy.planIntent();
        startPlayerTurn();
    }

    private void startPlayerTurn() {
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
        if (state != BattleState.PLAYER_TURN) return false;

        int cost = card.getCost();
        if (player.getEnergy() < cost) {
            log("❌ 에너지가 부족합니다!");
            return false;
        }

        boolean removed = player.playCard(card, cost);
        if (!removed) return false;

        EffectContext ctx = new EffectContext(player, enemy, this);
        card.play(ctx);

        log("🃏 " + card.getName() + " 사용");

        checkBattleEnd();
        if (state == BattleState.PLAYER_TURN) {
            refresh();
        }
        return true;
    }

    public void endPlayerTurn() {
        if (state != BattleState.PLAYER_TURN) return;
        if (!barricade) {
            // block resets naturally in startTurn
        }
        player.endTurn();
        state = BattleState.ENEMY_TURN;
        log("▶ 적의 턴");
        executeEnemyTurn();
    }

    private void executeEnemyTurn() {
        enemy.startTurn();
        if (!enemy.isAlive()) { endBattle(true); return; }

        enemy.executeIntent(player);
        log("👹 " + enemy.getName() + ": " + enemy.getCurrentIntent());

        if (!player.isAlive()) { endBattle(false); return; }

        enemy.planIntent();
        startPlayerTurn();
    }

    private void checkBattleEnd() {
        if (!enemy.isAlive()) endBattle(true);
        else if (!player.isAlive()) endBattle(false);
    }

    private void endBattle(boolean playerWon) {
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
