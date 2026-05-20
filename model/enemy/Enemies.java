package com.spiregame.model.enemy;

import com.spiregame.model.effect.StatusEffect;
import com.spiregame.model.player.Player;

import java.util.Random;

public class Enemies {

    static final Random rng = new Random();

    // ── 고블린 ──────────────────────────────────────────────
    public static class Goblin extends Enemy {
        public Goblin() { super("고블린", 20 + rng.nextInt(10)); }

        @Override public String getEmoji() { return "👺"; }

        @Override public void planIntent() {
            setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.ATTACK, 6, null));
        }

        @Override public void executeIntent(Player p) {
            attackPlayer(p, 6);
        }
    }

    // ── 해골 궁수 ─────────────────────────────────────────────
    public static class SkeletonArcher extends Enemy {
        private int turnCount = 0;
        public SkeletonArcher() { super("해골 궁수", 28 + rng.nextInt(8)); }

        @Override public String getEmoji() { return "💀"; }

        @Override public void planIntent() {
            turnCount++;
            if (turnCount % 3 == 0) {
                setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.DEBUFF, 0, "(취약)"));
            } else {
                setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.ATTACK, 8, null));
            }
        }

        @Override public void executeIntent(Player p) {
            if (getCurrentIntent().getType() == EnemyIntent.IntentType.DEBUFF) {
                p.addStatus(StatusEffect.VULNERABLE, 2);
            } else {
                attackPlayer(p, 8);
            }
        }
    }

    // ── 석고상 전사 ────────────────────────────────────────────
    public static class StoneWarrior extends Enemy {
        private boolean defended = false;
        public StoneWarrior() { super("석고상 전사", 45 + rng.nextInt(10)); }

        @Override public String getEmoji() { return "🗿"; }

        @Override public void planIntent() {
            if (!defended) {
                setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.DEFEND, 12, null));
            } else {
                setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.ATTACK, 14, null));
            }
            defended = !defended;
        }

        @Override public void executeIntent(Player p) {
            if (getCurrentIntent().getType() == EnemyIntent.IntentType.DEFEND) {
                gainBlock(12);
            } else {
                attackPlayer(p, 14);
            }
        }
    }

    // ── 마법사 ─────────────────────────────────────────────────
    public static class Mage extends Enemy {
        private int castCount = 0;
        public Mage() { super("마법사", 35 + rng.nextInt(10)); }

        @Override public String getEmoji() { return "🧙"; }

        @Override public void planIntent() {
            castCount++;
            if (castCount % 2 == 0) {
                setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.ATTACK, 12, "(독 3)"));
            } else {
                setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.BUFF, 3, "(근력)"));
            }
        }

        @Override public void executeIntent(Player p) {
            if (getCurrentIntent().getType() == EnemyIntent.IntentType.ATTACK) {
                attackPlayer(p, 12);
                p.addStatus(StatusEffect.POISON, 3);
            } else {
                addStatus(StatusEffect.STRENGTH, 3);
            }
        }
    }

    // ── 드래곤 (보스) ──────────────────────────────────────────
    public static class Dragon extends Enemy {
        private int phase = 0;
        public Dragon() { super("용 군주", 120 + rng.nextInt(20)); }

        @Override public String getEmoji() { return "🐉"; }

        @Override public void planIntent() {
            phase++;
            switch (phase % 4) {
                case 1 -> setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.ATTACK, 18, null));
                case 2 -> setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.ATTACK, 10, "(x3 연속)"));
                case 3 -> setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.DEFEND, 20, null));
                default -> setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.BUFF, 4, "(근력)"));
            }
        }

        @Override public void executeIntent(Player p) {
            switch (phase % 4) {
                case 1 -> attackPlayer(p, 18);
                case 2 -> { attackPlayer(p, 10); attackPlayer(p, 10); attackPlayer(p, 10); }
                case 3 -> gainBlock(20);
                default -> addStatus(StatusEffect.STRENGTH, 4);
            }
        }
    }

    // ── 악마 (최종 보스) ─────────────────────────────────────
    public static class Demon extends Enemy {
        private int turn = 0;
        public Demon() { super("공허의 악마", 200 + rng.nextInt(30)); }

        @Override public String getEmoji() { return "😈"; }

        @Override public void planIntent() {
            turn++;
            if (turn % 5 == 0) {
                setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.ATTACK, 40, "(처형 일격)"));
            } else if (turn % 3 == 0) {
                setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.DEBUFF, 0, "(약화+취약)"));
            } else {
                setCurrentIntent(new EnemyIntent(EnemyIntent.IntentType.ATTACK, 22, null));
            }
        }

        @Override public void executeIntent(Player p) {
            if (turn % 5 == 0) {
                attackPlayer(p, 40);
            } else if (turn % 3 == 0) {
                p.addStatus(StatusEffect.WEAK, 2);
                p.addStatus(StatusEffect.VULNERABLE, 2);
            } else {
                attackPlayer(p, 22);
            }
        }
    }
}
