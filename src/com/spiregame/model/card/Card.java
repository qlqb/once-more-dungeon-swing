package com.spiregame.model.card;

import com.spiregame.model.effect.EffectContext;

public abstract class Card {
    private final String id;
    private final String name;
    private final String description;
    private final int cost;
    private final CardType type;
    private final CardRarity rarity;
    private boolean upgraded;
    private boolean exhausts;

    public Card(String id, String name, String description, int cost, CardType type, CardRarity rarity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.type = type;
        this.rarity = rarity;
        this.upgraded = false;
        this.exhausts = false;
    }

    public abstract void play(EffectContext ctx);
    public abstract Card copy();

    // Getters
    public String getId() { return id; }
    public String getName() { return upgraded ? name + "+" : name; }
    public String getBaseName() { return name; }
    public String getDescription() { return description; }
    public int getCost() { return cost; }
    public CardType getType() { return type; }
    public CardRarity getRarity() { return rarity; }
    public boolean isUpgraded() { return upgraded; }
    public boolean isExhausts() { return exhausts; }
    public void setExhausts(boolean exhausts) { this.exhausts = exhausts; }

    public void upgrade() {
        if (!upgraded) {
            upgraded = true;
            onUpgrade();
        }
    }

    protected void onUpgrade() {}

    @Override
    public String toString() {
        return getName() + " [" + type.getDisplayName() + "] 비용:" + cost;
    }
}
