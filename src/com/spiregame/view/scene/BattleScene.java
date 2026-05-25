package com.spiregame.view.scene;

import com.spiregame.controller.BattleController;
import com.spiregame.controller.GameController;
import com.spiregame.model.card.Card;
import com.spiregame.model.enemy.Enemy;
import com.spiregame.model.player.Player;
import com.spiregame.view.component.CardView;
import com.spiregame.view.component.StatusBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class BattleScene extends JPanel {

    private final GameController game;
    private BattleController battle;
    private boolean battleEnded;

    private StatusBar playerStatus, enemyStatus;
    private JLabel enemyEmoji, enemyIntentLabel, turnLabel;
    private JLabel energyLabel, drawLabel, discardLabel;
    private JPanel handPane;
    private JTextArea battleLog;
    private JScrollPane logScroll;
    private JButton endTurnBtn;
    private JPanel centerArena;

    //SpireGameApp의 showBattle 메서드에서 만들어짐
    //showBattle은 gameController가 SpireGameApp에게 받은 콜백 함수로 실행이 된다
    public BattleScene(GameController game) {
        this.game = game;
        setBackground(new Color(10, 10, 24));
        setLayout(new BorderLayout());
        buildLayout();
    }

    public void startBattle() {
        this.battle = game.getCurrentBattle();
        this.battleEnded = false;
        battle.setLogCallback(this::addLog);
        battle.setUiRefreshCallback(this::refreshUI);
        refreshUI();
    }

    private void buildLayout() {
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildArena(),     BorderLayout.CENTER);
        add(buildBottom(),    BorderLayout.SOUTH);
        add(buildLogPanel(),  BorderLayout.EAST);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(5, 5, 16));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(26, 26, 62)),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        playerStatus = new StatusBar("🧙 모험가", true);
        enemyStatus = new StatusBar("적", false);

        turnLabel = new JLabel("턴 1", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Serif", Font.BOLD, 18));
        turnLabel.setForeground(new Color(243, 156, 18));

        bar.add(playerStatus, BorderLayout.WEST);
        bar.add(turnLabel, BorderLayout.CENTER);
        bar.add(enemyStatus, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildArena() {
        centerArena = new JPanel(new GridBagLayout());
        centerArena.setBackground(new Color(10, 10, 24));
        centerArena.setPreferredSize(new Dimension(800, 280));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 40, 0, 40);

        // Player avatar
        JPanel playerAvatar = buildPlayerAvatar();
        gbc.gridx = 0;
        centerArena.add(playerAvatar, gbc);

        // VS
        JLabel vs = new JLabel("⚔", SwingConstants.CENTER);
        vs.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        vs.setForeground(new Color(50, 50, 80));
        gbc.gridx = 1;
        centerArena.add(vs, gbc);

        // Enemy
        JPanel enemyDisplay = buildEnemyDisplay();
        gbc.gridx = 2;
        centerArena.add(enemyDisplay, gbc);

        return centerArena;
    }

    private JPanel buildPlayerAvatar() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel emoji = new JLabel("🧙", SwingConstants.CENTER);
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        emoji.setBorder(BorderFactory.createLineBorder(new Color(39, 174, 96), 3));
        emoji.setPreferredSize(new Dimension(110, 110));
        emoji.setHorizontalAlignment(SwingConstants.CENTER);
        emoji.setOpaque(true);
        emoji.setBackground(new Color(26, 26, 62));
        p.add(emoji);
        return p;
    }

    private JPanel buildEnemyDisplay() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        enemyEmoji = new JLabel("?", SwingConstants.CENTER);
        enemyEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        enemyEmoji.setBorder(BorderFactory.createLineBorder(new Color(231, 76, 60), 3));
        enemyEmoji.setPreferredSize(new Dimension(110, 110));
        enemyEmoji.setMaximumSize(new Dimension(130, 130));
        enemyEmoji.setHorizontalAlignment(SwingConstants.CENTER);
        enemyEmoji.setOpaque(true);
        enemyEmoji.setBackground(new Color(30, 10, 10));
        enemyEmoji.setAlignmentX(CENTER_ALIGNMENT);

        enemyIntentLabel = new JLabel("...", SwingConstants.CENTER);
        enemyIntentLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        enemyIntentLabel.setForeground(new Color(231, 76, 60));
        enemyIntentLabel.setOpaque(true);
        enemyIntentLabel.setBackground(new Color(44, 10, 10));
        enemyIntentLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        enemyIntentLabel.setAlignmentX(CENTER_ALIGNMENT);

        p.add(enemyEmoji);
        p.add(Box.createVerticalStrut(8));
        p.add(enemyIntentLabel);
        return p;
    }

    private JPanel buildBottom() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(8, 8, 21));
        panel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(26, 26, 62)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Resource row
        JPanel resourceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        resourceRow.setOpaque(false);

        energyLabel  = resourceLabel("⚡", new Color(243, 156, 18));
        drawLabel    = resourceLabel("📚", new Color(52, 152, 219));
        discardLabel = resourceLabel("🗑", new Color(149, 165, 166));

        endTurnBtn = new JButton("턴 종료 ▶");
        endTurnBtn.setFont(new Font("Serif", Font.BOLD, 13));
        endTurnBtn.setForeground(Color.WHITE);
        endTurnBtn.setBackground(new Color(192, 57, 43));
        endTurnBtn.setFocusPainted(false);
        endTurnBtn.setBorderPainted(false);
        endTurnBtn.setOpaque(true);
        endTurnBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        endTurnBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { endTurnBtn.setBackground(new Color(231, 76, 60)); }
            @Override public void mouseExited(MouseEvent e)  { endTurnBtn.setBackground(new Color(192, 57, 43)); }
        });
        //배틀이 널이 아니고 state가 player_turn이면 endPlayerTurn 메서드 실행
        endTurnBtn.addActionListener(e -> {
            if (battle != null && battle.getState() == BattleController.BattleState.PLAYER_TURN) {
                battle.endPlayerTurn();
            }
        });

        resourceRow.add(energyLabel);
        resourceRow.add(drawLabel);
        resourceRow.add(discardLabel);
        resourceRow.add(Box.createHorizontalGlue());
        resourceRow.add(endTurnBtn);
        resourceRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Hand
        handPane = new JPanel();
        handPane.setOpaque(false);
        handPane.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 5));

        JScrollPane handScroll = new JScrollPane(handPane);
        handScroll.setPreferredSize(new Dimension(800, 200));
        handScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        handScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        handScroll.setBackground(new Color(8, 8, 21));
        handScroll.getViewport().setBackground(new Color(8, 8, 21));
        handScroll.setBorder(null);

        panel.add(resourceRow);
        panel.add(handScroll);
        return panel;
    }

    private JLabel resourceLabel(String emoji, Color color) {
        JLabel lbl = new JLabel(emoji + " ?");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(color);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(15, 15, 37));
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return lbl;
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 400));
        panel.setBackground(new Color(6, 6, 18));
        panel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(26, 26, 62)));

        JLabel title = new JLabel("📜 전투 로그");
        title.setFont(new Font("Serif", Font.BOLD, 12));
        title.setForeground(new Color(243, 156, 18));
        title.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setBackground(new Color(6, 6, 18));
        battleLog.setForeground(new Color(189, 195, 199));
        battleLog.setFont(new Font("SansSerif", Font.PLAIN, 10));
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);

        logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(null);
        logScroll.setBackground(new Color(6, 6, 18));

        panel.add(title, BorderLayout.NORTH);
        panel.add(logScroll, BorderLayout.CENTER);
        return panel;
    }

    public void refreshUI() {
        SwingUtilities.invokeLater(() -> {
            if (battle == null) return;
            Player p = battle.getPlayer();
            Enemy e  = battle.getEnemy();
            BattleController.BattleState state = battle.getState();

            playerStatus.update(p.getCurrentHp(), p.getMaxHp(), p.getBlock(), p.getStatusEffects());
            enemyStatus.update(e.getCurrentHp(), e.getMaxHp(), e.getBlock(), e.getStatusEffects());
            enemyEmoji.setText(e.getEmoji());
            if (e.getCurrentIntent() != null)
                enemyIntentLabel.setText(e.getCurrentIntent().toString());

            turnLabel.setText("턴 " + battle.getTurnNumber());
            energyLabel.setText("⚡ " + p.getEnergy() + "/" + p.getMaxEnergy());
            drawLabel.setText("📚 " + p.getDrawPile().size());
            discardLabel.setText("🗑 " + p.getDiscardPile().size());

            rebuildHand(p, state);
            endTurnBtn.setEnabled(state == BattleController.BattleState.PLAYER_TURN);

            if (!battleEnded && state == BattleController.BattleState.VICTORY) {
                battleEnded = true;
                showEndDialog(true);
            } else if (!battleEnded && state == BattleController.BattleState.DEFEAT) {
                battleEnded = true;
                showEndDialog(false);
            }
        });
    }

    private void rebuildHand(Player p, BattleController.BattleState state) {
        handPane.removeAll();
        boolean playerTurn = state == BattleController.BattleState.PLAYER_TURN;

        List<Card> hand = new ArrayList<>(p.getHand());
        for (Card card : hand) {
            CardView cv = new CardView(card);
            boolean affordable = p.getEnergy() >= card.getCost();
            cv.setPlayable(playerTurn && affordable);
            final Card c = card;
            cv.setOnPlayAction(() -> {
                battle.playCard(c);
            });
            handPane.add(cv);
        }
        handPane.revalidate();
        handPane.repaint();
    }

    private void showEndDialog(boolean victory) {
        SwingUtilities.invokeLater(() -> {
            String msg = victory ? "전투 승리! 🏆" : "전투 패배... 💀";
            String btnText = victory ? "계속 ▶" : "메인 메뉴로";
            int result = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(this),
                msg, victory ? "승리" : "패배",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, new String[]{btnText}, btnText
            );
            if (victory) game.onBattleVictory();
            else game.onBattleDefeat();
        });
    }

    private void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            battleLog.append(message + "\n");
            battleLog.setCaretPosition(battleLog.getDocument().getLength());
        });
    }
}
