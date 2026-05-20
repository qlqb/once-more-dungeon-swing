package com.spiregame.model.effect;

import com.spiregame.model.player.Player;
import com.spiregame.model.enemy.Enemy;
import com.spiregame.controller.BattleController;

public class EffectContext {
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
