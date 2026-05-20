package com.spiregame.view.scene;

import com.spiregame.controller.GameController;
import com.spiregame.model.card.Card;
import com.spiregame.model.card.CardRarity;
import com.spiregame.view.component.CardView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class DeckBuilderScene extends JPanel {

    private final GameController game;
    private JPanel cardGrid;
    private JLabel deckSizeLabel;
    private JComboBox<String> sortBox, filterBox;

    public DeckBuilderScene(GameController game) {
        this.game = game;
        setBackground(new Color(7, 7, 18));
        setLayout(new BorderLayout());
        buildLayout();
    }

    private void buildLayout() {
        add(buildTopBar(), BorderLayout.NORTH);

        cardGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        cardGrid.setBackground(new Color(7, 7, 18));

        JScrollPane scroll = new JScrollPane(cardGrid);
        scroll.setBorder(null);
        scroll.setBackground(new Color(7, 7, 18));
        scroll.getViewport().setBackground(new Color(7, 7, 18));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(5, 5, 16));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(26, 26, 62)),
            BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        JLabel title = new JLabel("📚 덱 관리");
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setForeground(new Color(243, 156, 18));

        deckSizeLabel = new JLabel("카드: 0장");
        deckSizeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        deckSizeLabel.setForeground(new Color(189, 195, 199));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(title);
        left.add(deckSizeLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel sortLbl = smallLabel("정렬:");
        sortBox = new JComboBox<>(new String[]{"비용순", "이름순", "유형순", "희귀도순"});
        sortBox.addActionListener(e -> refreshCards());

        JLabel filterLbl = smallLabel("필터:");
        filterBox = new JComboBox<>(new String[]{"전체", "공격", "스킬", "파워"});
        filterBox.addActionListener(e -> refreshCards());

        JButton backBtn = new JButton("← 지도로");
        backBtn.setFont(new Font("Serif", Font.BOLD, 12));
        backBtn.setForeground(new Color(243, 156, 18));
        backBtn.setBackground(new Color(26, 26, 62));
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setOpaque(true);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> game.closeDeckBuilder());

        right.add(sortLbl); right.add(sortBox);
        right.add(filterLbl); right.add(filterBox);
        right.add(backBtn);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    public void refresh() {
        if (game.getPlayer() == null) return;
        refreshCards();
    }

    private void refreshCards() {
        if (game.getPlayer() == null) return;
        List<Card> deck = new ArrayList<>(game.getPlayer().getDeck());

        String filter = (String) filterBox.getSelectedItem();
        if (!"전체".equals(filter)) {
            deck.removeIf(c -> !c.getType().getDisplayName().equals(filter));
        }

        String sort = (String) sortBox.getSelectedItem();
        Comparator<Card> cmp = switch (sort) {
            case "이름순"   -> Comparator.comparing(Card::getName);
            case "유형순"   -> Comparator.comparing(c -> c.getType().getDisplayName());
            case "희귀도순" -> Comparator.comparing(Card::getRarity);
            default         -> Comparator.comparingInt(Card::getCost);
        };
        deck.sort(cmp);

        deckSizeLabel.setText("카드: " + game.getPlayer().getDeck().size() + "장");
        cardGrid.removeAll();

        for (Card card : deck) {
            CardView cv = new CardView(card);
            cv.setPlayable(false);
            cardGrid.add(cv);
        }

        // Stats
        long attacks = deck.stream().filter(c -> c.getType().getDisplayName().equals("공격")).count();
        long skills  = deck.stream().filter(c -> c.getType().getDisplayName().equals("스킬")).count();
        long powers  = deck.stream().filter(c -> c.getType().getDisplayName().equals("파워")).count();
        long rare    = deck.stream().filter(c -> c.getRarity() == CardRarity.RARE).count();

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        stats.setOpaque(true);
        stats.setBackground(new Color(13, 13, 34));
        stats.add(chip("⚔ 공격 " + attacks, new Color(231, 76, 60)));
        stats.add(chip("🛡 스킬 " + skills, new Color(52, 152, 219)));
        stats.add(chip("✨ 파워 " + powers, new Color(155, 89, 182)));
        stats.add(chip("희귀 " + rare, new Color(243, 156, 18)));
        cardGrid.add(stats);

        cardGrid.revalidate();
        cardGrid.repaint();
    }

    private JLabel chip(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(color);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(26, 26, 62));
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return lbl;
    }

    private JLabel smallLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }
}
