package com.spiregame.model.player;

import com.spiregame.model.card.Card;
import com.spiregame.model.effect.StatusEffect;

import java.util.*;

public class Player {
    // 전투와 게임 진행에서 계속 변하는 플레이어 기본 수치들이다.
    // currentHp/gold/floor는 전투가 끝나도 유지되고, block/energy는 턴마다 갱신된다.
    private int maxHp;
    private int currentHp;
    private int block;
    private int energy;
    private int maxEnergy;
    private int gold;
    private int floor;

    // deck은 게임 전체에서 유지되는 원본 덱이다.
    // hand/drawPile/discardPile/exhaustPile은 한 전투 안에서 움직이는 카드 더미들이다.
    //플레이어가 선택한 덱
    private final List<Card> deck;

    //플레이어의 손패
    private final List<Card> hand;

    //전투중에 사용할 수 있는 카드 더미
    private final List<Card> drawPile;

    //이전 턴에 손패에 있던 카드 더미
    private final List<Card> discardPile;

    //사용시 소멸되는 카드 더미
    private final List<Card> exhaustPile;

    // 상태 이상과 버프는 "종류 -> 수치" 형태로 저장한다.
    // 예: STRENGTH 2, VULNERABLE 1
    //상태이상효과들
    private final Map<StatusEffect, Integer> statusEffects;

    private static final int HAND_SIZE = 5;

    public Player(int maxHp) {
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.block = 0;
        this.energy = 3;
        this.maxEnergy = 3;
        this.gold = 100;
        this.floor = 0;

        this.deck = new ArrayList<>();
        this.hand = new ArrayList<>();
        this.drawPile = new ArrayList<>();
        this.discardPile = new ArrayList<>();
        this.exhaustPile = new ArrayList<>();
        this.statusEffects = new HashMap<>();
    }

    // --- Combat Setup ---
    public void startBattle() {
        // 전투 시작마다 전투용 더미를 새로 만든다.
        // 원본 deck 자체를 섞거나 소모하지 않기 위해 card.copy()를 drawPile에 넣는다.
        drawPile.clear();
        discardPile.clear();
        hand.clear();
        exhaustPile.clear();
        statusEffects.clear();
        block = 0;
        // 원본덱을 유지하기 위해 drawPile에 복사해서 사용함
        for (Card c : deck) drawPile.add(c.copy());
        Collections.shuffle(drawPile);
    }

    public void startTurn() {
        // 턴 시작 처리:
        // 에너지 회복 -> 회복 효과 -> 손패 새로 뽑기 -> 디버프 지속시간 감소.
        // 방어도 초기화 여부는 Barricade 같은 전투 규칙을 아는 BattleController가 결정한다.
        energy = maxEnergy + getStatus(StatusEffect.ENERGY_UP);

        int poison = getStatus(StatusEffect.POISON);
        if (poison > 0) {
            currentHp = Math.max(0, currentHp - poison);
            if(!isAlive()) return;
        }

        // Apply regen
        int regen = getStatus(StatusEffect.REGEN);
        if (regen > 0) heal(regen);

        // Draw hand
        hand.clear();
        int draws = HAND_SIZE + getStatus(StatusEffect.DRAW_CARD);
        for (int i = 0; i < draws; i++) drawCard();

        // Decrement duration-based effects
        tickStatusEffects();
    }

    public void endTurn() {
        // 턴 종료 시 남은 손패는 모두 버린 카드 더미로 이동한다.
        discardPile.addAll(hand);
        hand.clear();
        // Block is reset at next turn start
    }

    public void drawCard() {
        // drawPile이 비면 discardPile을 섞어서 새 drawPile로 만든다.
        // 이것이 덱 순환 구조다.
        if (drawPile.isEmpty()) {
            if (discardPile.isEmpty()) return;
            drawPile.addAll(discardPile);
            discardPile.clear();
            Collections.shuffle(drawPile);
        }
        if (!drawPile.isEmpty()) {
            hand.add(drawPile.remove(0));
        }
    }

    public boolean playCard(Card card, int energyCost) {
        // 실제 에너지 차감과 손패 제거는 Player가 담당한다.
        // BattleController는 이 메서드의 성공 여부를 보고 카드 효과를 실행할지 결정한다.
        if (energy < energyCost) return false;
        if (!hand.contains(card)) return false;
        energy -= energyCost;
        hand.remove(card);
        if (card.isExhausts()) {
            exhaustPile.add(card);
        } else {
            discardPile.add(card);
        }
        return true;
    }

    // --- Combat ---
    public int gainBlock(int amount) {
        // 방어도 획득량은 민첩/허약 상태의 영향을 받는다.
        int dex = getStatus(StatusEffect.DEXTERITY);
        boolean frail = getStatus(StatusEffect.FRAIL) > 0;
        int total = Math.max(0, (int)((amount + dex) * (frail ? 0.75 : 1.0)));
        block += total;
        return total;
    }

    public void clearBlock() {
        block = 0;
    }

    public int takeDamage(int amount) {
        // 피해는 취약 상태와 현재 방어도를 반영한 뒤 HP에 적용된다.
        boolean vulnerable = getStatus(StatusEffect.VULNERABLE) > 0;
        int finalDmg = (int)(amount * (vulnerable ? 1.5 : 1.0));
        int absorbed = Math.min(block, finalDmg);
        block -= absorbed;
        int remaining = finalDmg - absorbed;
        currentHp = Math.max(0, currentHp - remaining);
        return remaining;
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    // --- Status Effects ---
    public void addStatus(StatusEffect effect, int amount) {
        // 같은 상태가 다시 걸리면 수치를 누적한다.
        statusEffects.merge(effect, amount, Integer::sum);
    }

    public int getStatus(StatusEffect effect) {
        return statusEffects.getOrDefault(effect, 0);
    }

    private void tickStatusEffects() {
        // 버프가 아닌 상태 이상은 턴이 지날 때마다 1씩 감소한다.
        List<StatusEffect> toRemove = new ArrayList<>();
        for (StatusEffect effect : new ArrayList<>(statusEffects.keySet())) {
            if (!effect.isBuff()) {
                int val = statusEffects.get(effect) - 1;
                if (val <= 0) toRemove.add(effect);
                else statusEffects.put(effect, val);
            }
        }
        toRemove.forEach(statusEffects::remove);
    }

    // --- Deck Management ---
    public void addCardToDeck(Card card) { deck.add(card); }
    public void removeCardFromDeck(Card card) { deck.remove(card); }

    // --- Getters/Setters ---
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getBlock() { return block; }
    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return maxEnergy; }
    public int getGold() { return gold; }
    public int getFloor() { return floor; }
    public List<Card> getDeck() { return Collections.unmodifiableList(deck); }
    public List<Card> getHand() { return Collections.unmodifiableList(hand); }
    public List<Card> getDrawPile() { return Collections.unmodifiableList(drawPile); }
    public List<Card> getDiscardPile() { return Collections.unmodifiableList(discardPile); }
    public Map<StatusEffect, Integer> getStatusEffects() { return Collections.unmodifiableMap(statusEffects); }
    public boolean isAlive() { return currentHp > 0; }
    public void addGold(int amount) { gold += amount; }
    public boolean spendGold(int amount) { if (gold < amount) return false; gold -= amount; return true; }
    public void incrementFloor() { floor++; }
    public void setMaxEnergy(int e) { this.maxEnergy = e; }
    public void gainEnergy(int amount) { this.energy += amount; }
    public void increaseMaxHp(int amount) { maxHp += amount; currentHp += amount; }
}
