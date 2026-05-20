package com.spiregame;

import com.spiregame.controller.GameController;
import com.spiregame.view.scene.*;

import javax.swing.*;
import java.awt.*;

public class SpireGameApp {

    private JFrame frame;
    private JPanel root;
    private GameController gameController;
    private MapScene mapScene;

    public void start() {
        // Dark Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Dark UI defaults
        UIManager.put("Panel.background", new Color(7, 7, 18));
        UIManager.put("ScrollPane.background", new Color(7, 7, 18));
        UIManager.put("Viewport.background", new Color(7, 7, 18));
        UIManager.put("ComboBox.background", new Color(26, 26, 62));
        UIManager.put("ComboBox.foreground", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", new Color(41, 41, 90));
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("List.background", new Color(26, 26, 62));
        UIManager.put("List.foreground", Color.WHITE);
        UIManager.put("ScrollBar.track", new Color(13, 13, 34));
        UIManager.put("ScrollBar.thumb", new Color(44, 62, 80));

        gameController = new GameController();

        frame = new JFrame("Spire Quest — 분기 맵 로그라이크 카드 게임");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(980, 720);
        frame.setMinimumSize(new Dimension(900, 680));
        frame.setLocationRelativeTo(null);

        root = new JPanel(new BorderLayout());
        root.setBackground(new Color(7, 7, 18));
        frame.setContentPane(root);

        // Phase-change callback (runs on EDT)
        gameController.setPhaseChangeCallback(phase ->
            SwingUtilities.invokeLater(() -> switchScene(phase))
        );

        showMainMenu();
        frame.setVisible(true);
    }

    private void switchScene(GameController.GamePhase phase) {
        switch (phase) {
            case MAIN_MENU    -> showMainMenu();
            case DECK_EDITOR  -> showDeckEditor();
            case MAP          -> showMap();
            case BATTLE       -> showBattle();
            case CARD_REWARD  -> showCardReward();
            case SHOP         -> showShop();
            case REST         -> showRest();
            case DECK_BUILDER -> showDeckBuilder();
            case GAME_OVER    -> showGameOver(false);
            case VICTORY      -> showGameOver(true);
        }
    }

    private void showMainMenu() {
        setRoot(new MainMenuScene(gameController));
    }

    private void showDeckEditor() {
        setRoot(new DeckEditorScene(gameController, true));
    }

    private void showMap() {
        if (mapScene == null) mapScene = new MapScene(gameController);
        mapScene.refresh();
        setRoot(mapScene);
    }

    private void showBattle() {
        BattleScene battleScene = new BattleScene(gameController);
        setRoot(battleScene);
        battleScene.startBattle();
    }

    private void showCardReward() {
        setRoot(new CardRewardScene(gameController));
    }

    private void showShop() {
        ShopScene shopScene = new ShopScene(gameController);
        shopScene.refresh();
        setRoot(shopScene);
    }

    private void showRest() {
        setRoot(new RestScene(gameController));
    }

    private void showDeckBuilder() {
        DeckBuilderScene scene = new DeckBuilderScene(gameController);
        scene.refresh();
        setRoot(scene);
    }

    private void showGameOver(boolean victory) {
        mapScene = null;
        setRoot(new GameOverScene(gameController, victory));
    }

    private void setRoot(JPanel panel) {
        root.removeAll();
        root.add(panel, BorderLayout.CENTER);
        root.revalidate();
        root.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpireGameApp().start());
    }
}
