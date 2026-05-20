package com.spiregame.model.enemy;

import com.spiregame.model.effect.StatusEffect;
import com.spiregame.model.player.Player;

import java.util.*;

public abstract class Enemy {
    private final String name;
    private int maxHp;
    private int currentHp;
    private int block;
    private final Map<StatusEffect, Integer> statusEffects;
    private EnemyIntent currentIntent;

    public Enemy(String name, int maxHp) {
        this.name = name;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.block = 0;
        this.statusEffects = new HashMap<>();
    }

    public abstract void planIntent();
    public abstract void executeIntent(Player player);
    public abstract String getEmoji();

    public void startTurn() {
        block = 0;
        // Poison damage
        int poison = getStatus(StatusEffect.POISON);
        if (poison > 0) {
            currentHp = Math.max(0, currentHp - poison);
            addStatus(StatusEffect.POISON, -1);
        }
        // Burn damage
        int burn = getStatus(StatusEffect.BURN);
        if (burn > 0) {
            currentHp = Math.max(0, currentHp - burn);
        }
    }

    public int takeDamage(int amount, boolean playerWeak) {
        boolean vulnerable = getStatus(StatusEffect.VULNERABLE) > 0;
        double mult = (playerWeak ? 0.75 : 1.0) * (vulnerable ? 1.5 : 1.0);
        int finalDmg = Math.max(0, (int)(amount * mult));
        int absorbed = Math.min(block, finalDmg);
        block -= absorbed;
        int remaining = finalDmg - absorbed;
        currentHp = Math.max(0, currentHp - remaining);
        return remaining;
    }

    protected int attackPlayer(Player player, int baseDmg) {
        int str = getStatus(StatusEffect.STRENGTH);
        boolean weak = getStatus(StatusEffect.WEAK) > 0;
        int dmg = Math.max(0, (int)((baseDmg + str) * (weak ? 0.75 : 1.0)));
        // Player thorns
        int thorns = player.getStatus(StatusEffect.THORNS);
        if (thorns > 0) {
            currentHp = Math.max(0, currentHp - thorns);
        }
        return player.takeDamage(dmg);
    }

    protected int gainBlock(int amount) {
        block += amount;
        return amount;
    }

    public void addStatus(StatusEffect effect, int amount) {
        int current = statusEffects.getOrDefault(effect, 0);
        int newVal = current + amount;
        if (newVal <= 0) statusEffects.remove(effect);
        else statusEffects.put(effect, newVal);
    }

    public int getStatus(StatusEffect effect) {
        return statusEffects.getOrDefault(effect, 0);
    }

    public String getName() { return name; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getBlock() { return block; }
    public boolean isAlive() { return currentHp > 0; }
    public Map<StatusEffect, Integer> getStatusEffects() { return Collections.unmodifiableMap(statusEffects); }
    public EnemyIntent getCurrentIntent() { return currentIntent; }
    public void setCurrentIntent(EnemyIntent intent) { this.currentIntent = intent; }
}
