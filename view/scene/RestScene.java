package com.spiregame.view.scene;

import com.spiregame.controller.GameController;
import com.spiregame.model.card.Card;
import com.spiregame.view.component.CardView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class RestScene extends JPanel {

    private final GameController game;
    private JPanel upgradeArea;

    public RestScene(GameController game) {
        this.game = game;
        setBackground(new Color(7, 7, 18));
        setLayout(new GridBagLayout());
        buildLayout();
    }

    private void buildLayout() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🔥", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("휴식처", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(new Color(39, 174, 96));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("잠시 숨을 고르세요. 어떻게 하시겠습니까?", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(149, 165, 166));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        int healAmount = (int)(game.getPlayer().getMaxHp() * 0.30);

        JPanel choices = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        choices.setOpaque(false);

        JPanel healBox = buildChoiceCard("❤", "HP 회복",
            "최대 HP의 30% 회복\n(+" + healAmount + " HP)",
            new Color(20, 90, 50), new Color(39, 174, 96),
            () -> game.restHeal());

        JPanel upgradeBox = buildChoiceCard("⬆", "카드 업그레이드",
            "덱의 카드 1장을\n영구 업그레이드",
            new Color(26, 58, 94), new Color(41, 128, 185),
            this::showUpgradeSelection);

        choices.add(healBox);
        choices.add(upgradeBox);

        upgradeArea = new JPanel();
        upgradeArea.setOpaque(false);
        upgradeArea.setLayout(new BoxLayout(upgradeArea, BoxLayout.Y_AXIS));
        upgradeArea.setVisible(false);

        container.add(icon);
        container.add(Box.createVerticalStrut(8));
        container.add(title);
        container.add(Box.createVerticalStrut(6));
        container.add(subtitle);
        container.add(Box.createVerticalStrut(20));
        container.add(choices);
        container.add(Box.createVerticalStrut(16));
        container.add(upgradeArea);

        add(container);
    }

    private JPanel buildChoiceCard(String emoji, String title, String desc,
                                   Color normalBg, Color hoverBg, Runnable action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(normalBg);
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        card.setPreferredSize(new Dimension(190, 180));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel emojiLbl = new JLabel(emoji, SwingConstants.CENTER);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        emojiLbl.setForeground(Color.WHITE);
        emojiLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Serif", Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel descLbl = new JLabel("<html><center>" + desc.replace("\n", "<br>") + "</center></html>", SwingConstants.CENTER);
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        descLbl.setForeground(new Color(189, 195, 199));
        descLbl.setAlignmentX(CENTER_ALIGNMENT);

        JButton btn = new JButton("선택");
        btn.setFont(new Font("Serif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(255, 255, 255, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.addActionListener(e -> action.run());

        card.add(emojiLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(descLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(btn);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(hoverBg); }
            @Override public void mouseExited(MouseEvent e)  { card.setBackground(normalBg); }
        });
        return card;
    }

    private void showUpgradeSelection() {
        upgradeArea.removeAll();

        JLabel hint = new JLabel("업그레이드할 카드를 선택하세요", SwingConstants.CENTER);
        hint.setFont(new Font("Serif", Font.BOLD, 14));
        hint.setForeground(new Color(243, 156, 18));
        hint.setAlignmentX(CENTER_ALIGNMENT);
        upgradeArea.add(hint);

        List<Card> upgradeable = game.getPlayer().getDeck().stream()
            .filter(c -> !c.isUpgraded()).toList();

        if (upgradeable.isEmpty()) {
            JLabel noCard = new JLabel("업그레이드 가능한 카드가 없습니다.");
            noCard.setFont(new Font("SansSerif", Font.PLAIN, 12));
            noCard.setForeground(new Color(149, 165, 166));
            noCard.setAlignmentX(CENTER_ALIGNMENT);
            upgradeArea.add(noCard);
        } else {
            JPanel cardRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            cardRow.setOpaque(false);
            for (Card card : upgradeable) {
                CardView cv = new CardView(card);
                cv.setPlayable(true);
                cv.setOnPlayAction(() -> game.restUpgrade(card));
                cardRow.add(cv);
            }
            JScrollPane scroll = new JScrollPane(cardRow);
            scroll.setPreferredSize(new Dimension(700, 210));
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            upgradeArea.add(scroll);
        }

        upgradeArea.setVisible(true);
        upgradeArea.revalidate();
        upgradeArea.repaint();
        revalidate();
        repaint();
    }
}
