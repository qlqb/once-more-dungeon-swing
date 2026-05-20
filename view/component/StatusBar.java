package com.spiregame.view.component;

import com.spiregame.model.effect.StatusEffect;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class StatusBar extends JPanel {

    private final JLabel nameLabel;
    private final JProgressBar hpBar;
    private final JLabel hpLabel;
    private final JLabel blockLabel;
    private final JPanel statusBox;
    private final boolean isPlayer;

    public StatusBar(String name, boolean isPlayer) {
        this.isPlayer = isPlayer;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(10, 10, 25, 210));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setOpaque(true);

        nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Serif", Font.BOLD, 13));
        nameLabel.setForeground(isPlayer ? new Color(39, 174, 96) : new Color(231, 76, 60));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);

        hpBar = new JProgressBar(0, 100);
        hpBar.setValue(100);
        hpBar.setStringPainted(false);
        hpBar.setPreferredSize(new Dimension(180, 10));
        hpBar.setMaximumSize(new Dimension(200, 10));
        hpBar.setAlignmentX(LEFT_ALIGNMENT);
        hpBar.setForeground(isPlayer ? new Color(39, 174, 96) : new Color(231, 76, 60));
        hpBar.setBackground(new Color(30, 30, 50));
        hpBar.setBorderPainted(false);

        hpLabel = new JLabel("80 / 80");
        hpLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hpLabel.setForeground(Color.WHITE);
        hpLabel.setAlignmentX(LEFT_ALIGNMENT);

        blockLabel = new JLabel("🛡 0");
        blockLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        blockLabel.setForeground(new Color(52, 152, 219));
        blockLabel.setAlignmentX(LEFT_ALIGNMENT);
        blockLabel.setVisible(false);

        JPanel hpRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        hpRow.setOpaque(false);
        hpRow.add(hpLabel);
        hpRow.add(blockLabel);
        hpRow.setAlignmentX(LEFT_ALIGNMENT);

        statusBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        statusBox.setOpaque(false);
        statusBox.setAlignmentX(LEFT_ALIGNMENT);

        add(nameLabel);
        add(Box.createVerticalStrut(2));
        add(hpBar);
        add(hpRow);
        add(statusBox);
    }

    public void update(int currentHp, int maxHp, int block, Map<StatusEffect, Integer> effects) {
        int ratio = maxHp > 0 ? (int) ((double) currentHp / maxHp * 100) : 0;
        hpBar.setValue(ratio);
        hpLabel.setText(currentHp + " / " + maxHp);

        blockLabel.setText("🛡 " + block);
        blockLabel.setVisible(block > 0);

        statusBox.removeAll();
        for (Map.Entry<StatusEffect, Integer> e : effects.entrySet()) {
            JLabel badge = new JLabel(e.getKey().getDisplayName() + " " + e.getValue());
            badge.setFont(new Font("SansSerif", Font.PLAIN, 9));
            badge.setForeground(Color.WHITE);
            badge.setOpaque(true);
            badge.setBackground(e.getKey().isBuff() ? new Color(26, 82, 118) : new Color(146, 43, 33));
            badge.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            statusBox.add(badge);
        }
        statusBox.revalidate();
        statusBox.repaint();
        revalidate();
        repaint();
    }
}
