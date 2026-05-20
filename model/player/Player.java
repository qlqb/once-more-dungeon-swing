package com.spiregame.model.player;

import com.spiregame.model.card.Card;
import com.spiregame.model.effect.StatusEffect;

import java.util.*;

public class Player {
    private int maxHp;
    private int currentHp;
    private int block;
    private int energy;
    private int maxEnergy;
    private int gold;
    private int floor;

    private final List<Card> deck;
    private final List<Card> hand;
    private final List<Card> drawPile;
    private final List<Card> discardPile;
    private final List<Card> exhaustPile;

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
        drawPile.clear();
        discardPile.clear();
        hand.clear();
        exhaustPile.clear();
        statusEffects.clear();
        block = 0;
        for (Card c : deck) drawPile.add(c.copy());
        Collections.shuffle(drawPile);
    }

    public void startTurn() {
        energy = maxEnergy + getStatus(StatusEffect.ENERGY_UP);
        block = 0;

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
        discardPile.addAll(hand);
        hand.clear();
        // Block is reset at next turn start
    }

    public void drawCard() {
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
        int dex = getStatus(StatusEffect.DEXTERITY);
        boolean frail = getStatus(StatusEffect.FRAIL) > 0;
        int total = Math.max(0, (int)((amount + dex) * (frail ? 0.75 : 1.0)));
        block += total;
        return total;
    }

    public int takeDamage(int amount) {
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
        statusEffects.merge(effect, amount, Integer::sum);
    }

    public int getStatus(StatusEffect effect) {
        return statusEffects.getOrDefault(effect, 0);
    }

    private void tickStatusEffects() {
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
    public void increaseMaxHp(int amount) { maxHp += amount; currentHp += amount; }
}
