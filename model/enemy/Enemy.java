package com.spiregame.model.enemy;

import com.spiregame.model.effect.StatusEffect;
import com.spiregame.model.player.Player;

import java.util.*;

public abstract class Enemy {
    // Enemy는 모든 몬스터의 공통 데이터와 공통 전투 처리를 가진 부모 클래스다.
    // 실제 몬스터별 행동 계획/실행은 하위 클래스가 구현한다.
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

    // 다음 턴에 무엇을 할지 정한다. BattleScene은 currentIntent를 읽어서 화면에 보여준다.
    public abstract void planIntent();

    // planIntent()로 정한 행동을 실제로 실행한다.
    public abstract void executeIntent(Player player);

    public abstract String getEmoji();

    public void startTurn() {
        // 적 턴 시작 처리. 방어도를 초기화하고 독/화상 같은 턴 시작 피해를 적용한다.
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
        // 플레이어의 약화 여부와 적의 취약 여부를 반영해 최종 피해를 계산한다.
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
        // 적이 플레이어를 공격할 때 쓰는 공통 헬퍼다.
        // 적의 힘/약화와 플레이어의 가시 효과를 함께 처리한다.
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
        // 적 상태는 0 이하가 되면 Map에서 제거한다.
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
