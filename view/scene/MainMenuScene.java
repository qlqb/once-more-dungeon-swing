package com.spiregame.view.scene;

import com.spiregame.controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenuScene extends JPanel {

    // 게임 시작버튼 누르면 startNewGame 메서드 실행하기 위해 의존성 직접 주입
    private final GameController game;


    public MainMenuScene(GameController game) {
        this.game = game;
        setBackground(new Color(5, 5, 16));
        setLayout(new GridBagLayout());
        buildLayout();
    }

    //패널의 컴포넌트들을 그려내는 메서드
    private void buildLayout() {
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel titleLine1 = new JLabel("SPIRE");
        titleLine1.setFont(new Font("Serif", Font.BOLD, 72));
        titleLine1.setForeground(new Color(243, 156, 18));
        titleLine1.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titleLine2 = new JLabel("QUEST");
        titleLine2.setFont(new Font("Serif", Font.BOLD, 36));
        titleLine2.setForeground(new Color(230, 126, 34));
        titleLine2.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("카드를 모으고 탑을 정복하라");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(new Color(149, 165, 166));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel divider = new JLabel("— ⚔ 🃏 ⚔ —");
        divider.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        divider.setForeground(new Color(44, 62, 80));
        divider.setAlignmentX(CENTER_ALIGNMENT);

        JButton startBtn = menuButton("🃏  덱 구성하고 시작", new Color(192, 57, 43), new Color(231, 76, 60));
        startBtn.addActionListener(e -> game.startNewGame());
        startBtn.setAlignmentX(CENTER_ALIGNMENT);

        JButton quitBtn = menuButton("✕  종료", new Color(26, 26, 46), new Color(44, 44, 78));
        quitBtn.addActionListener(e -> System.exit(0));
        quitBtn.setAlignmentX(CENTER_ALIGNMENT);

        // Feature list
        JPanel features = new JPanel();
        features.setOpaque(true);
        features.setBackground(new Color(10, 10, 30, 180));
        features.setLayout(new BoxLayout(features, BoxLayout.Y_AXIS));
        features.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        String[] feats = {
            "🃏 시작 전 덱을 자유롭게 구성",
            "🔄 게임 중에도 덱 편집 가능",
            "👹 6종의 다양한 적",
            "🗺 15층 분기 맵",
            "⬆ 카드 업그레이드 시스템"
        };
        for (String f : feats) {
            JLabel fl = new JLabel(f);
            fl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            fl.setForeground(new Color(189, 195, 199));
            fl.setAlignmentX(CENTER_ALIGNMENT);
            features.add(fl);
            features.add(Box.createVerticalStrut(4));
        }
        features.setAlignmentX(CENTER_ALIGNMENT);
        features.setMaximumSize(new Dimension(340, 200));

        center.add(titleLine1);
        center.add(Box.createVerticalStrut(4));
        center.add(titleLine2);
        center.add(Box.createVerticalStrut(10));
        center.add(subtitle);
        center.add(Box.createVerticalStrut(10));
        center.add(divider);
        center.add(Box.createVerticalStrut(20));
        center.add(startBtn);
        center.add(Box.createVerticalStrut(10));
        center.add(quitBtn);
        center.add(Box.createVerticalStrut(20));
        center.add(features);

        add(center);
    }

    private JButton menuButton(String text, Color normal, Color hover) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Serif", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(normal);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(280, 46));
        btn.setMaximumSize(new Dimension(280, 46));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        });
        return btn;
    }
}
