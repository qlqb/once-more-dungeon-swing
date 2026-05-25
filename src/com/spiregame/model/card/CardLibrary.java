package com.spiregame.model.card;

import java.util.ArrayList;
import java.util.List;

public class CardLibrary {

    public static List<Card> getAllCards() {
        List<Card> all = new ArrayList<>();
        // Basic
        all.add(new Cards.Strike());
        all.add(new Cards.Defend());
        // Common
        all.add(new Cards.BashCard());
        all.add(new Cards.ShrugItOff());
        all.add(new Cards.TwinStrike());
        all.add(new Cards.Pommel());
        all.add(new Cards.Armaments());
        // Uncommon
        all.add(new Cards.Whirlwind());
        all.add(new Cards.Barricade());
        all.add(new Cards.Inflame());
        all.add(new Cards.SpotWeakness());
        all.add(new Cards.HeavyBlade());
        // Rare
        all.add(new Cards.Reaper());
        all.add(new Cards.LimitBreak());
        all.add(new Cards.Impervious());
        all.add(new Cards.Offering());
        all.add(new Cards.Brutality());
        return all;
    }

    public static List<Card> getStarterDeck() {
        List<Card> starter = new ArrayList<>();
        for (int i = 0; i < 5; i++) starter.add(new Cards.Strike());
        for (int i = 0; i < 4; i++) starter.add(new Cards.Defend());
        starter.add(new Cards.BashCard());
        return starter;
    }

    public static List<Card> getCardRewardPool(int floor) {
        List<Card> pool = new ArrayList<>();
        // More rare cards at higher floors
        double rareMult = Math.min(1.0, floor / 15.0);
        for (Card c : getAllCards()) {
            if (c.getRarity() == CardRarity.BASIC) continue;
            if (c.getRarity() == CardRarity.RARE && Math.random() > rareMult) continue;
            pool.add(c.copy());
        }
        return pool;
    }
}
