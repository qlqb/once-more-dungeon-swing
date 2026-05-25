package com.spiregame.view.scene;

import com.spiregame.controller.GameController;
import com.spiregame.model.card.Card;
import com.spiregame.view.component.CardView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShopScene extends JPanel {

    private final GameController game;
    private List<Card> shopCards;
    private final Map<Card, Integer> prices = new LinkedHashMap<>();
    private JLabel goldLabel;
    private JPanel shopCardRow, removeSection;

    public ShopScene(GameController game) {
        this.game = game;
        setBackground(new Color(7, 7, 18));
        setLayout(new BorderLayout());
    }

    public void refresh() {
        removeAll();
        shopCards = game.getShopCards();
        prices.clear();
        for (Card c : shopCards) prices.put(c, game.getCardShopPrice(c));
        buildLayout();
        revalidate();
        repaint();
    }

    private void buildLayout() {
        add(buildTopBar(), BorderLayout.NORTH);

        JPanel content = buildContent();
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setBackground(new Color(7, 7, 18));
        scroll.getViewport().setBackground(new Color(7, 7, 18));
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(5, 5, 16));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(26, 26, 62)),
            BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        JLabel title = new JLabel("🛒 상점");
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setForeground(new Color(241, 196, 15));

        goldLabel = new JLabel("💰 " + game.getPlayer().getGold() + " 골드");
        goldLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        goldLabel.setForeground(new Color(241, 196, 15));

        JButton exitBtn = new JButton("떠나기 →");
        exitBtn.setFont(new Font("Serif", Font.BOLD, 12));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(new Color(146, 43, 33));
        exitBtn.setFocusPainted(false);
        exitBtn.setBorderPainted(false);
        exitBtn.setOpaque(true);
        exitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> game.exitShop());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(title);
        left.add(goldLabel);

        bar.add(left, BorderLayout.WEST);
        bar.add(exitBtn, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setBackground(new Color(7, 7, 18));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel buyTitle = sectionTitle("🪙 구매 가능한 카드");
        buyTitle.setAlignmentX(LEFT_ALIGNMENT);

        shopCardRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        shopCardRow.setOpaque(false);
        shopCardRow.setAlignmentX(LEFT_ALIGNMENT);
        buildShopCards();

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(26, 26, 62));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel removeTitle = sectionTitle("🗑 카드 제거 (25골드)");
        removeTitle.setAlignmentX(LEFT_ALIGNMENT);

        removeSection = new JPanel();
        removeSection.setOpaque(false);
        removeSection.setLayout(new BoxLayout(removeSection, BoxLayout.Y_AXIS));
        removeSection.setAlignmentX(LEFT_ALIGNMENT);
        buildRemoveSection();

        content.add(buyTitle);
        content.add(Box.createVerticalStrut(10));
        content.add(shopCardRow);
        content.add(Box.createVerticalStrut(10));
        content.add(sep);
        content.add(Box.createVerticalStrut(10));
        content.add(removeTitle);
        content.add(Box.createVerticalStrut(8));
        content.add(removeSection);
        return content;
    }

    private void buildShopCards() {
        shopCardRow.removeAll();
        for (Card card : shopCards) {
            int price = prices.get(card);
            JPanel item = buildShopItem(card, price);
            shopCardRow.add(item);
        }
        shopCardRow.revalidate();
        shopCardRow.repaint();
    }

    private JPanel buildShopItem(Card card, int price) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        CardView cv = new CardView(card);
        cv.setPlayable(false);
        cv.setAlignmentX(CENTER_ALIGNMENT);

        JLabel priceLbl = new JLabel("💰 " + price + " 골드", SwingConstants.CENTER);
        priceLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        priceLbl.setForeground(new Color(241, 196, 15));
        priceLbl.setAlignmentX(CENTER_ALIGNMENT);

        boolean canAfford = game.getPlayer().getGold() >= price;
        JButton buyBtn = new JButton(canAfford ? "구매" : "골드 부족");
        buyBtn.setEnabled(canAfford);
        buyBtn.setFont(new Font("Serif", Font.BOLD, 11));
        buyBtn.setForeground(Color.WHITE);
        buyBtn.setBackground(canAfford ? new Color(26, 82, 118) : new Color(44, 62, 80));
        buyBtn.setFocusPainted(false);
        buyBtn.setBorderPainted(false);
        buyBtn.setOpaque(true);
        buyBtn.setAlignmentX(CENTER_ALIGNMENT);
        buyBtn.addActionListener(e -> {
            if (game.buyCard(card, price)) {
                goldLabel.setText("💰 " + game.getPlayer().getGold() + " 골드");
                shopCards.remove(card);
                prices.remove(card);
                buildShopCards();
                buildRemoveSection();
            }
        });

        box.add(cv);
        box.add(Box.createVerticalStrut(4));
        box.add(priceLbl);
        box.add(Box.createVerticalStrut(4));
        box.add(buyBtn);
        return box;
    }

    private void buildRemoveSection() {
        removeSection.removeAll();
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        flow.setOpaque(false);

        List<Card> deck = new java.util.ArrayList<>(game.getPlayer().getDeck());
        for (Card card : deck) {
            JButton removeBtn = new JButton("✖ " + card.getName() + "  (-25💰)");
            removeBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
            removeBtn.setForeground(new Color(231, 76, 60));
            removeBtn.setBackground(new Color(44, 10, 10));
            removeBtn.setFocusPainted(false);
            removeBtn.setBorderPainted(false);
            removeBtn.setOpaque(true);
            removeBtn.setEnabled(game.getPlayer().getGold() >= 25);
            removeBtn.addActionListener(e -> {
                if (game.removeCardFromDeck(card, 25)) {
                    goldLabel.setText("💰 " + game.getPlayer().getGold() + " 골드");
                    buildRemoveSection();
                    buildShopCards();
                }
            });
            flow.add(removeBtn);
        }
        JLabel hint = new JLabel("* 카드를 덱에서 영구 제거합니다");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 9));
        hint.setForeground(new Color(127, 140, 141));

        removeSection.add(flow);
        removeSection.add(hint);
        removeSection.revalidate();
        removeSection.repaint();
    }

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Serif", Font.BOLD, 16));
        lbl.setForeground(new Color(243, 156, 18));
        return lbl;
    }
}
