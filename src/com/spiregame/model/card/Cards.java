package com.spiregame.model.card;

import com.spiregame.model.effect.EffectContext;
import com.spiregame.model.effect.StatusEffect;

public class Cards {

    // ══════════════════════════════════════════════════════════════
    //  기본 카드
    // ══════════════════════════════════════════════════════════════

    public static class Strike extends Card {
        private int damage;
        public Strike() {
            super("strike", "강타", "적에게 {dmg} 피해를 줍니다.", 1, CardType.ATTACK, CardRarity.BASIC);
            this.damage = 6;
        }
        @Override public void play(EffectContext ctx) {
            int str = ctx.getPlayer().getStatus(StatusEffect.STRENGTH);
            boolean weak = ctx.getPlayer().getStatus(StatusEffect.WEAK) > 0;
            ctx.getTarget().takeDamage(damage+str, weak);
        }
        @Override protected void onUpgrade() { damage = 9; }
        @Override public String getDescription() { return "적에게 " + damage + " 피해를 줍니다."; }
        @Override public Card copy() { Strike c = new Strike(); if (isUpgraded()) c.upgrade(); return c; }
    }

    public static class Defend extends Card {
        private int blockAmount;
        public Defend() {
            super("defend", "방어", "방어도 {blk}을 얻습니다.", 1, CardType.SKILL, CardRarity.BASIC);
            this.blockAmount = 5;
        }
        @Override public void play(EffectContext ctx) { ctx.getPlayer().gainBlock(blockAmount); }
        @Override protected void onUpgrade() { blockAmount = 8; }
        @Override public String getDescription() { return "방어도 " + blockAmount + "을 얻습니다."; }
        @Override public Card copy() { Defend c = new Defend(); if (isUpgraded()) c.upgrade(); return c; }
    }

    // ══════════════════════════════════════════════════════════════
    //  일반 카드
    // ══════════════════════════════════════════════════════════════

    public static class BashCard extends Card {
        private int damage; private int vulnTurns;
        public BashCard() {
            super("bash", "강격", "적에게 8 피해 + 취약 2 부여.", 2, CardType.ATTACK, CardRarity.COMMON);
            damage = 8; vulnTurns = 2;
        }
        @Override public void play(EffectContext ctx) {
            int str = ctx.getPlayer().getStatus(StatusEffect.STRENGTH);
            ctx.getTarget().takeDamage(damage + str, ctx.getPlayer().getStatus(StatusEffect.WEAK) > 0);
            ctx.getTarget().addStatus(StatusEffect.VULNERABLE, vulnTurns);
        }
        @Override protected void onUpgrade() { damage = 10; vulnTurns = 3; }
        @Override public String getDescription() { return "적에게 " + damage + " 피해 + 취약 " + vulnTurns + " 부여."; }
        @Override public Card copy() { BashCard c = new BashCard(); if (isUpgraded()) c.upgrade(); return c; }
    }

    public static class ShrugItOff extends Card {
        private int blockAmount;
        public ShrugItOff() {
            super("shrug_it_off", "훌훌 털기", "방어도 8 + 카드 1장 드로우.", 1, CardType.SKILL, CardRarity.COMMON);
            blockAmount = 8;
        }
        @Override public void play(EffectContext ctx) {
            ctx.getPlayer().gainBlock(blockAmount);
            ctx.getPlayer().drawCard();
        }
        @Override protected void onUpgrade() { blockAmount = 11; }
        @Override public String getDescription() { return "방어도 " + blockAmount + " + 카드 1장 드로우."; }
        @Override public Card copy() { ShrugItOff c = new ShrugItOff(); if (isUpgraded()) c.upgrade(); return c; }
    }

    public static class TwinStrike extends Card {
        private int damage;
        public TwinStrike() {
            super("twin_strike", "쌍격", "적에게 5 피해를 2회 줍니다.", 1, CardType.ATTACK, CardRarity.COMMON);
            damage = 5;
        }
        @Override public void play(EffectContext ctx) {
            int str = ctx.getPlayer().getStatus(StatusEffect.STRENGTH);
            boolean weak = ctx.getPlayer().getStatus(StatusEffect.WEAK) > 0;
            for (int i = 0; i < 2; i++) ctx.getTarget().takeDamage((damage + str), weak);
        }
        @Override protected void onUpgrade() { damage = 7; }
        @Override public String getDescription() { return "적에게 " + damage + " 피해를 2회 줍니다."; }
        @Override public Card copy() { TwinStrike c = new TwinStrike(); if (isUpgraded()) c.upgrade(); return c; }
    }

    public static class Pommel extends Card {
        private int damage;
        public Pommel() {
            super("pommel", "손잡이 타격", "9 피해 + 카드 1장 드로우.", 1, CardType.ATTACK, CardRarity.COMMON);
            damage = 9;
        }
        @Override public void play(EffectContext ctx) {
            int str = ctx.getPlayer().getStatus(StatusEffect.STRENGTH);
            ctx.getTarget().takeDamage(damage + str, ctx.getPlayer().getStatus(StatusEffect.WEAK) > 0);
            ctx.getPlayer().drawCard();
        }
        @Override protected void onUpgrade() { damage = 10; }
        @Override public String getDescription() { return damage + " 피해 + 카드 1장 드로우."; }
        @Override public Card copy() { Pommel c = new Pommel(); if (isUpgraded()) c.upgrade(); return c; }
    }

    public static class Armaments extends Card {
        public Armaments() {
            super("armaments", "무장", "방어도 5. 손패 카드 1장 업그레이드.", 1, CardType.SKILL, CardRarity.COMMON);
        }
        @Override public void play(EffectContext ctx) {
            ctx.getPlayer().gainBlock(5);
            ctx.getBattle().upgradeRandomHandCard();
        }
        @Override public String getDescription() { return "방어도 5. 손패 카드 1장 업그레이드."; }
        @Override public Card copy() { return new Armaments(); }
    }

    // ══════════════════════════════════════════════════════════════
    //  고급 카드
    // ══════════════════════════════════════════════════════════════

    public static class Whirlwind extends Card {
        public Whirlwind() {
            super("whirlwind", "회오리", "에너지 소비당 모든 적에게 5 피해.", 0, CardType.ATTACK, CardRarity.UNCOMMON);
        }
        @Override public void play(EffectContext ctx) {
            int energy = ctx.getPlayer().getEnergy();
            int str = ctx.getPlayer().getStatus(StatusEffect.STRENGTH);
            boolean weak = ctx.getPlayer().getStatus(StatusEffect.WEAK) > 0;
            for (int i = 0; i < energy; i++) {
                ctx.getTarget().takeDamage((5 + str), weak);
            }
        }
        @Override public String getDescription() { return "에너지 소비당 적에게 5 피해."; }
        @Override public Card copy() { return new Whirlwind(); }
    }

    public static class Barricade extends Card {
        public Barricade() {
            super("barricade", "바리케이드", "방어도가 턴 종료 시 사라지지 않습니다.", 3, CardType.POWER, CardRarity.UNCOMMON);
            setExhausts(false);
        }
        @Override public void play(EffectContext ctx) {
            ctx.getBattle().setBarricade(true);
        }
        @Override public String getDescription() { return "방어도가 턴 종료 시 사라지지 않습니다."; }
        @Override public Card copy() { return new Barricade(); }
    }

    public static class Inflame extends Card {
        private int strAmount;
        public Inflame() {
            super("inflame", "불꽃 각성", "근력 2 영구 획득.", 1, CardType.POWER, CardRarity.UNCOMMON);
            strAmount = 2;
        }
        @Override public void play(EffectContext ctx) {
            ctx.getPlayer().addStatus(StatusEffect.STRENGTH, strAmount);
        }
        @Override protected void onUpgrade() { strAmount = 3; }
        @Override public String getDescription() { return "근력 " + strAmount + " 영구 획득."; }
        @Override public Card copy() { Inflame c = new Inflame(); if (isUpgraded()) c.upgrade(); return c; }
    }

    public static class SpotWeakness extends Card {
        private int strGain;
        public SpotWeakness() {
            super("spot_weakness", "약점 포착", "적이 공격 의도이면 근력 3 획득.", 1, CardType.SKILL, CardRarity.UNCOMMON);
            strGain = 3;
        }
        @Override public void play(EffectContext ctx) {
            if (ctx.getTarget().getCurrentIntent() != null &&
                ctx.getTarget().getCurrentIntent().getType() == com.spiregame.model.enemy.EnemyIntent.IntentType.ATTACK) {
                ctx.getPlayer().addStatus(StatusEffect.STRENGTH, strGain);
            }
        }
        @Override protected void onUpgrade() { strGain = 4; }
        @Override public String getDescription() { return "적 공격 의도 시 근력 " + strGain + " 획득."; }
        @Override public Card copy() { SpotWeakness c = new SpotWeakness(); if (isUpgraded()) c.upgrade(); return c; }
    }

    public static class HeavyBlade extends Card {
        private int baseDmg; private int strMult;
        public HeavyBlade() {
            super("heavy_blade", "중검", "14 피해. 근력을 3배 추가 적용.", 2, CardType.ATTACK, CardRarity.UNCOMMON);
            baseDmg = 14; strMult = 3;
        }
        @Override public void play(EffectContext ctx) {
            int str = ctx.getPlayer().getStatus(StatusEffect.STRENGTH);
            boolean weak = ctx.getPlayer().getStatus(StatusEffect.WEAK) > 0;
            ctx.getTarget().takeDamage((int)((baseDmg + str * strMult) * (weak ? 0.75 : 1.0)), false);
        }
        @Override protected void onUpgrade() { strMult = 5; }
        @Override public String getDescription() { return baseDmg + " 피해. 근력을 " + strMult + "배 적용."; }
        @Override public Card copy() { HeavyBlade c = new HeavyBlade(); if (isUpgraded()) c.upgrade(); return c; }
    }

    // ══════════════════════════════════════════════════════════════
    //  희귀 카드
    // ══════════════════════════════════════════════════════════════

    public static class Reaper extends Card {
        public Reaper() {
            super("reaper", "낫", "모든 적에게 4 피해 + 준 피해만큼 체력 회복.", 2, CardType.ATTACK, CardRarity.RARE);
            setExhausts(true);
        }
        @Override public void play(EffectContext ctx) {
            int str = ctx.getPlayer().getStatus(StatusEffect.STRENGTH);
            boolean weak = ctx.getPlayer().getStatus(StatusEffect.WEAK) > 0;
            int dmg = ctx.getTarget().takeDamage((int)((4 + str) * (weak ? 0.75 : 1.0)), false);
            ctx.getPlayer().heal(dmg);
        }
        @Override public String getDescription() { return "적에게 4 피해 + 피해만큼 체력 회복."; }
        @Override public Card copy() { return new Reaper(); }
    }

    public static class LimitBreak extends Card {
        public LimitBreak() {
            super("limit_break", "한계 돌파", "현재 근력을 2배로 만듭니다.", 1, CardType.SKILL, CardRarity.RARE);
            setExhausts(true);
        }
        @Override public void play(EffectContext ctx) {
            int str = ctx.getPlayer().getStatus(StatusEffect.STRENGTH);
            if (str > 0) ctx.getPlayer().addStatus(StatusEffect.STRENGTH, str);
        }
        @Override public String getDescription() { return "현재 근력을 2배로 만듭니다. (소진)"; }
        @Override public Card copy() { return new LimitBreak(); }
    }

    public static class Impervious extends Card {
        private int blockAmount;
        public Impervious() {
            super("impervious", "불굴", "방어도 30 획득. (소진)", 2, CardType.SKILL, CardRarity.RARE);
            setExhausts(true);
            blockAmount = 30;
        }
        @Override public void play(EffectContext ctx) { ctx.getPlayer().gainBlock(blockAmount); }
        @Override protected void onUpgrade() { blockAmount = 40; }
        @Override public String getDescription() { return "방어도 " + blockAmount + " 획득. (소진)"; }
        @Override public Card copy() { Impervious c = new Impervious(); if (isUpgraded()) c.upgrade(); return c; }
    }

    public static class Offering extends Card {
        public Offering() {
            super("offering", "제물", "HP 6 소모 → 에너지 2 획득 + 카드 3장 드로우. (소진)", 0, CardType.SKILL, CardRarity.RARE);
            setExhausts(true);
        }
        @Override public void play(EffectContext ctx) {
            ctx.getPlayer().takeDamage(6);
            ctx.getBattle().gainBonusEnergy(2);
            for (int i = 0; i < 3; i++) ctx.getPlayer().drawCard();
        }
        @Override public String getDescription() { return "HP 6 소모 → 에너지 2 획득 + 카드 3장 드로우. (소진)"; }
        @Override public Card copy() { return new Offering(); }
    }

    public static class Brutality extends Card {
        public Brutality() {
            super("brutality", "잔인함", "매 턴 시작 시 HP 1 소모 + 카드 1장 드로우.", 0, CardType.POWER, CardRarity.RARE);
        }
        @Override public void play(EffectContext ctx) {
            ctx.getBattle().setBrutality(true);
        }
        @Override public String getDescription() { return "매 턴: HP 1 소모 + 카드 1장 드로우."; }
        @Override public Card copy() { return new Brutality(); }
    }
}
