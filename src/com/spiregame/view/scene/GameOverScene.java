package com.spiregame.view.scene;

import com.spiregame.controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameOverScene extends JPanel {

    private final GameController game;
    private final boolean victory;

    public GameOverScene(GameController game, boolean victory) {
        this.game = game;
        this.victory = victory;
        setBackground(victory ? new Color(5, 15, 5) : new Color(15, 5, 5));
        setLayout(new GridBagLayout());
        buildLayout();
    }

    private void buildLayout() {
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel(victory ? "🏆" : "💀", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel(victory ? "정복 성공!" : "모험 종료", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 44));
        title.setForeground(victory ? new Color(243, 156, 18) : new Color(231, 76, 60));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel msg = new JLabel(
            "<html><center>" + (victory
                ? "탑의 꼭대기를 정복했습니다!<br>당신의 이름은 전설이 될 것입니다."
                : "어둠이 당신을 삼켰습니다...<br>하지만 전사는 다시 일어납니다.") + "</center></html>",
            SwingConstants.CENTER
        );
        msg.setFont(new Font("SansSerif", Font.PLAIN, 15));
        msg.setForeground(new Color(149, 165, 166));
        msg.setAlignmentX(CENTER_ALIGNMENT);

        center.add(icon);
        center.add(Box.createVerticalStrut(8));
        center.add(title);
        center.add(Box.createVerticalStrut(10));
        center.add(msg);
        center.add(Box.createVerticalStrut(14));

        if (game.getPlayer() != null) {
            JPanel stats = new JPanel();
            stats.setOpaque(true);
            stats.setBackground(new Color(10, 10, 30, 160));
            stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
            stats.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

            for (String line : new String[]{
                "🗺 도달 층: " + game.getCurrentFloor() + "/" + game.getTotalFloors(),
                "🃏 최종 덱: " + game.getPlayer().getDeck().size() + "장",
                "💰 남은 골드: " + game.getPlayer().getGold(),
                "❤ 최종 HP: " + game.getPlayer().getCurrentHp() + "/" + game.getPlayer().getMaxHp()
            }) {
                JLabel lbl = new JLabel(line, SwingConstants.CENTER);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
                lbl.setForeground(new Color(189, 195, 199));
                lbl.setAlignmentX(CENTER_ALIGNMENT);
                stats.add(lbl);
                stats.add(Box.createVerticalStrut(4));
            }
            stats.setAlignmentX(CENTER_ALIGNMENT);
            stats.setMaximumSize(new Dimension(300, 200));
            center.add(stats);
            center.add(Box.createVerticalStrut(16));
        }

        JButton retryBtn = new JButton("🔄  다시 시작");
        retryBtn.setFont(new Font("Serif", Font.BOLD, 15));
        retryBtn.setForeground(Color.WHITE);
        retryBtn.setBackground(new Color(26, 82, 118));
        retryBtn.setFocusPainted(false); retryBtn.setBorderPainted(false); retryBtn.setOpaque(true);
        retryBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        retryBtn.setPreferredSize(new Dimension(220, 44));
        retryBtn.setMaximumSize(new Dimension(220, 44));
        retryBtn.setAlignmentX(CENTER_ALIGNMENT);
        retryBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { retryBtn.setBackground(new Color(41, 128, 185)); }
            @Override public void mouseExited(MouseEvent e)  { retryBtn.setBackground(new Color(26, 82, 118)); }
        });
        retryBtn.addActionListener(e -> game.startNewGame());
        center.add(retryBtn);

        add(center);
    }
}
