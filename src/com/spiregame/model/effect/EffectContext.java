package com.spiregame.model.effect;

import com.spiregame.model.player.Player;
import com.spiregame.model.enemy.Enemy;
import com.spiregame.controller.BattleController;

public class EffectContext {
    // 카드 효과가 실행될 때 필요한 전투 주변 정보를 한 번에 넘기기 위한 객체다.
    // 카드 클래스가 Player, Enemy, BattleController를 각각 따로 받지 않아도 되게 해준다.
    private final Player player;
    private final Enemy target;
    private final BattleController battle;

    public EffectContext(Player player, Enemy target, BattleController battle) {
        this.player = player;
        this.target = target;
        this.battle = battle;
    }

    public Player getPlayer() { return player; }
    public Enemy getTarget() { return target; }
    public BattleController getBattle() { return battle; }
}
