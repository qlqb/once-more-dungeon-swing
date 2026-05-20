package com.spiregame.view.scene;

import com.spiregame.controller.GameController;
import com.spiregame.model.card.Card;
import com.spiregame.view.component.CardView;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CardRewardScene extends JPanel {

    private final GameController game;

    public CardRewardScene(GameController game) {
        this.game = game;
        setBackground(new Color(7, 7, 18));
        setLayout(new GridBagLayout());
        buildLayout();
    }

    private void buildLayout() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("🎁 카드 보상", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(new Color(243, 156, 18));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("덱에 추가할 카드를 선택하세요", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(149, 165, 166));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        JPanel cardRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 10));
        cardRow.setOpaque(false);

        List<Card> rewards = game.getCurrentRewardCards();
        if (rewards != null) {
            for (Card card : rewards) {
                JPanel cardBox = buildRewardCard(card);
                cardRow.add(cardBox);
            }
        }

        JButton skipBtn = new JButton("건너뛰기 →");
        skipBtn.setFont(new Font("Serif", Font.PLAIN, 13));
        skipBtn.setForeground(new Color(149, 165, 166));
        skipBtn.setBackground(new Color(44, 62, 80));
        skipBtn.setFocusPainted(false);
        skipBtn.setBorderPainted(false);
        skipBtn.setOpaque(true);
        skipBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        skipBtn.addActionListener(e -> game.skipReward());
        skipBtn.setAlignmentX(CENTER_ALIGNMENT);

        container.add(title);
        container.add(Box.createVerticalStrut(6));
        container.add(subtitle);
        container.add(Box.createVerticalStrut(20));
        container.add(cardRow);
        container.add(Box.createVerticalStrut(16));
        container.add(skipBtn);

        add(container);
    }

    private JPanel buildRewardCard(Card card) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        CardView cv = new CardView(card);
        cv.setPlayable(true);
        cv.setAlignmentX(CENTER_ALIGNMENT);

        JLabel rarityLbl = new JLabel(card.getRarity().getDisplayName(), SwingConstants.CENTER);
        rarityLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        rarityLbl.setForeground(Color.decode(card.getRarity().getColor()));
        rarityLbl.setAlignmentX(CENTER_ALIGNMENT);

        JButton pickBtn = new JButton("선택");
        pickBtn.setFont(new Font("Serif", Font.BOLD, 12));
        pickBtn.setForeground(Color.WHITE);
        pickBtn.setBackground(new Color(39, 174, 96));
        pickBtn.setFocusPainted(false);
        pickBtn.setBorderPainted(false);
        pickBtn.setOpaque(true);
        pickBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pickBtn.addActionListener(e -> game.selectRewardCard(card));
        pickBtn.setAlignmentX(CENTER_ALIGNMENT);

        box.add(cv);
        box.add(Box.createVerticalStrut(4));
        box.add(rarityLbl);
        box.add(Box.createVerticalStrut(4));
        box.add(pickBtn);
        return box;
    }
}
