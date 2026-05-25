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
        //컴포넌트를 위에서 아래로 향하게 레이아웃 설정
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(10, 10, 25, 210));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setOpaque(true);

        nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Serif", Font.BOLD, 13));
        nameLabel.setForeground(isPlayer ? new Color(39, 174, 96) : new Color(231, 76, 60));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);

        //프러그레스바를 0%~100%로 설정
        hpBar = new JProgressBar(0, 100);
        hpBar.setValue(100);
        hpBar.setStringPainted(false);
        //이정도 크기로 만들어라고 요청
        hpBar.setPreferredSize(new Dimension(180, 10));
        //BoxLayout이 이상하게 만들 수도 있으니까 멕시멈 사이즈를 정해줌
        hpBar.setMaximumSize(new Dimension(200, 10));
        //왼쪽정렬해라
        hpBar.setAlignmentX(LEFT_ALIGNMENT);
        //플레이어면 초록색 적이면 빨간색으로 표시
        hpBar.setForeground(isPlayer ? new Color(39, 174, 96) : new Color(231, 76, 60));
        //깎였을때 빈 부분에 보여지는 색
        hpBar.setBackground(new Color(30, 30, 50));
        //기본 테두리를 없앰 더 깔끔하게 보임
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

        //hp숫자랑 방어도가 같은 라인에 보이게 하려고 flowLayout 사용
        //왼쪽 정렬하고 가로간격은 6 세로간격은 0
        JPanel hpRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        //투명하게 둠. 뒤에 있는 부모 배경이 보이게 함
        hpRow.setOpaque(false);
        hpRow.add(hpLabel);
        hpRow.add(blockLabel);
        hpRow.setAlignmentX(LEFT_ALIGNMENT);

        statusBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        statusBox.setOpaque(false);
        statusBox.setAlignmentX(LEFT_ALIGNMENT);

        add(nameLabel);
        //box 레이아웃에서 투명 컴포넌트를 만들어서 간격을 2px만큼 추가하는 코드
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
        //enetrySet은 Map.Entry로 키 밸류값을 동시에 취할 수 있음
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
