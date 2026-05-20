package com.spiregame.view.scene;

import com.spiregame.controller.GameController;
import com.spiregame.model.card.*;
import com.spiregame.view.component.CardView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class DeckEditorScene extends JPanel {

    private static final int MIN_DECK_SIZE = 10;
    private static final int MAX_DECK_SIZE = 20;
    private static final Map<CardRarity, Integer> MAX_COPIES = Map.of(
        CardRarity.BASIC, 3, CardRarity.COMMON, 3,
        CardRarity.UNCOMMON, 2, CardRarity.RARE, 1, CardRarity.EPIC, 1
    );

    private final GameController game;
    private final boolean isPreGame;
    private final List<Card> allCards;
    private final Map<String, Integer> deckCounts = new LinkedHashMap<>();
    private final Map<String, Card>    deckCards  = new LinkedHashMap<>();

    private JPanel poolGrid, deckListBox;
    private JLabel deckSizeLabel, validationLabel;
    private JButton startBtn;
    private JComboBox<String> filterTypeBox, filterRarityBox, sortBox;

    public DeckEditorScene(GameController game, boolean isPreGame) {
        this.game = game;
        this.isPreGame = isPreGame;
        this.allCards = CardLibrary.getAllCards();
        if (!isPreGame && game.getPlayer() != null) loadCurrentDeck();
        setBackground(new Color(7, 7, 18));
        setLayout(new BorderLayout());
        buildLayout();
        refreshPool();
        refreshDeckList();
        updateValidation();
    }

    private void loadCurrentDeck() {
        for (Card card : game.getPlayer().getDeck()) {
            deckCounts.merge(card.getId(), 1, Integer::sum);
            deckCards.put(card.getId(), card);
        }
    }

    private void buildLayout() {
        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildPoolPanel(), BorderLayout.WEST);
        add(buildDeckPanel(), BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(5, 5, 16));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(26, 26, 62)),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        JLabel title = new JLabel(isPreGame ? "🃏 덱 구성" : "🃏 덱 편집");
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setForeground(new Color(243, 156, 18));

        deckSizeLabel = new JLabel("0 / " + MAX_DECK_SIZE);
        deckSizeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        deckSizeLabel.setForeground(new Color(189, 195, 199));

        validationLabel = new JLabel();
        validationLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(title); left.add(deckSizeLabel); left.add(validationLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        filterTypeBox   = combo("전체", "공격", "스킬", "파워");
        filterRarityBox = combo("전체", "기본", "일반", "고급", "희귀");
        sortBox         = combo("비용순", "이름순", "희귀도순");
        filterTypeBox.addActionListener(e -> refreshPool());
        filterRarityBox.addActionListener(e -> refreshPool());
        sortBox.addActionListener(e -> refreshPool());

        startBtn = new JButton(isPreGame ? "▶ 게임 시작" : "✔ 저장하고 돌아가기");
        startBtn.setFont(new Font("Serif", Font.BOLD, 13));
        startBtn.setForeground(Color.WHITE);
        startBtn.setBackground(new Color(39, 174, 96));
        startBtn.setFocusPainted(false); startBtn.setBorderPainted(false); startBtn.setOpaque(true);
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> onConfirm());

        JButton cancelBtn = new JButton(isPreGame ? "← 메인메뉴" : "← 취소");
        cancelBtn.setFont(new Font("Serif", Font.PLAIN, 12));
        cancelBtn.setForeground(new Color(189, 195, 199));
        cancelBtn.setBackground(new Color(44, 62, 80));
        cancelBtn.setFocusPainted(false); cancelBtn.setBorderPainted(false); cancelBtn.setOpaque(true);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> onCancel());

        right.add(new JLabel("타입:") {{ setForeground(Color.WHITE); }});
        right.add(filterTypeBox);
        right.add(new JLabel("희귀도:") {{ setForeground(Color.WHITE); }});
        right.add(filterRarityBox);
        right.add(new JLabel("정렬:") {{ setForeground(Color.WHITE); }});
        right.add(sortBox);
        right.add(cancelBtn);
        right.add(startBtn);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildPoolPanel() {
        poolGrid = new JPanel(new GridLayout(0, 3, 8, 8));
        poolGrid.setBackground(new Color(7, 7, 18));
        poolGrid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(poolGrid);
        scroll.setPreferredSize(new Dimension(500, 600));
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(26, 26, 62)));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setBackground(new Color(7, 7, 18));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        return scroll;
    }

    private JPanel buildDeckPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(6, 6, 18));
        panel.setPreferredSize(new Dimension(400, 600));

        JLabel header = new JLabel("  내 덱");
        header.setFont(new Font("Serif", Font.BOLD, 15));
        header.setForeground(new Color(243, 156, 18));
        header.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));

        JLabel rule = new JLabel("  최소 " + MIN_DECK_SIZE + "장 ~ 최대 " + MAX_DECK_SIZE + "장");
        rule.setFont(new Font("SansSerif", Font.PLAIN, 10));
        rule.setForeground(new Color(74, 74, 106));

        JPanel topPart = new JPanel();
        topPart.setOpaque(false);
        topPart.setLayout(new BoxLayout(topPart, BoxLayout.Y_AXIS));
        topPart.add(header); topPart.add(rule);

        deckListBox = new JPanel();
        deckListBox.setBackground(new Color(6, 6, 18));
        deckListBox.setLayout(new BoxLayout(deckListBox, BoxLayout.Y_AXIS));
        deckListBox.setBorder(BorderFactory.createEmptyBorder(6, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(deckListBox);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(6, 6, 18));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JButton clearBtn = new JButton("🗑 덱 초기화");
        clearBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
        clearBtn.setForeground(new Color(231, 76, 60));
        clearBtn.setBackground(new Color(44, 10, 10));
        clearBtn.setFocusPainted(false); clearBtn.setBorderPainted(false); clearBtn.setOpaque(true);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> {
            deckCounts.clear(); deckCards.clear();
            refreshDeckList(); refreshPool(); updateValidation();
        });
        JPanel clearRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clearRow.setOpaque(false);
        clearRow.add(clearBtn);

        panel.add(topPart, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(clearRow, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshPool() {
        poolGrid.removeAll();
        List<Card> filtered = new ArrayList<>(allCards);

        String typeFilter = filterTypeBox != null ? (String) filterTypeBox.getSelectedItem() : "전체";
        String rarityFilter = filterRarityBox != null ? (String) filterRarityBox.getSelectedItem() : "전체";
        if (!"전체".equals(typeFilter)) filtered.removeIf(c -> !c.getType().getDisplayName().equals(typeFilter));
        if (!"전체".equals(rarityFilter)) filtered.removeIf(c -> !c.getRarity().getDisplayName().equals(rarityFilter));

        String sort = sortBox != null ? (String) sortBox.getSelectedItem() : "비용순";
        Comparator<Card> cmp = switch (sort) {
            case "이름순" -> Comparator.comparing(Card::getName);
            case "희귀도순" -> Comparator.comparing(Card::getRarity);
            default -> Comparator.comparingInt(Card::getCost);
        };
        filtered.sort(cmp);

        for (Card card : filtered) {
            int current = deckCounts.getOrDefault(card.getId(), 0);
            int maxCopies = MAX_COPIES.getOrDefault(card.getRarity(), 1);
            int total = deckCounts.values().stream().mapToInt(Integer::intValue).sum();
            boolean canAdd = total < MAX_DECK_SIZE && current < maxCopies;

            JPanel wrapper = new JPanel();
            wrapper.setOpaque(false);
            wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

            CardView cv = new CardView(card);
            cv.setPlayable(canAdd);
            if (canAdd) cv.setOnPlayAction(() -> addCardToDeck(card));
            cv.setAlignmentX(CENTER_ALIGNMENT);

            if (current > 0) {
                JLabel countLbl = new JLabel(current + " / " + maxCopies + " 장", SwingConstants.CENTER);
                countLbl.setFont(new Font("SansSerif", Font.BOLD, 9));
                countLbl.setForeground(canAdd ? new Color(243, 156, 18) : new Color(231, 76, 60));
                countLbl.setAlignmentX(CENTER_ALIGNMENT);
                wrapper.add(cv); wrapper.add(countLbl);
            } else {
                wrapper.add(cv);
            }
            poolGrid.add(wrapper);
        }
        poolGrid.revalidate(); poolGrid.repaint();
    }

    private void refreshDeckList() {
        deckListBox.removeAll();

        List<Card> sorted = new ArrayList<>(deckCards.values());
        sorted.sort(Comparator.comparing((Card c) -> c.getType().getDisplayName())
            .thenComparingInt(Card::getCost).thenComparing(Card::getName));

        CardType lastType = null;
        for (Card card : sorted) {
            if (card.getType() != lastType) {
                lastType = card.getType();
                JLabel typeHeader = new JLabel(card.getType().getDisplayName());
                typeHeader.setFont(new Font("Serif", Font.BOLD, 10));
                typeHeader.setForeground(Color.decode(card.getType().getColor()));
                typeHeader.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 4));
                typeHeader.setAlignmentX(LEFT_ALIGNMENT);
                deckListBox.add(typeHeader);
            }
            int count = deckCounts.getOrDefault(card.getId(), 0);
            deckListBox.add(buildDeckRow(card, count));
            deckListBox.add(Box.createVerticalStrut(3));
        }

        deckListBox.revalidate(); deckListBox.repaint();
        updateValidation();
    }

    private JPanel buildDeckRow(Card card, int count) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(new Color(13, 13, 34));
        row.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel dot = new JLabel("●");
        dot.setForeground(Color.decode(card.getRarity().getColor()));
        dot.setFont(new Font("SansSerif", Font.PLAIN, 9));

        JLabel name = new JLabel(card.getName());
        name.setFont(new Font("SansSerif", Font.BOLD, 11));
        name.setForeground(Color.WHITE);

        JPanel left2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left2.setOpaque(false);
        left2.add(dot); left2.add(name);

        JLabel countLbl = new JLabel("x" + count);
        countLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        countLbl.setForeground(new Color(189, 195, 199));

        int maxCopies = MAX_COPIES.getOrDefault(card.getRarity(), 1);
        int total = deckCounts.values().stream().mapToInt(Integer::intValue).sum();
        boolean canAddMore = count < maxCopies && total < MAX_DECK_SIZE;

        JButton addBtn = smallBtn("+", new Color(26, 82, 118));
        addBtn.setEnabled(canAddMore);
        addBtn.addActionListener(e -> addCardToDeck(card));

        JButton remBtn = smallBtn("−", new Color(146, 43, 33));
        remBtn.addActionListener(e -> removeCardFromDeck(card));

        JPanel right2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right2.setOpaque(false);
        right2.add(countLbl); right2.add(addBtn); right2.add(remBtn);

        row.add(left2, BorderLayout.WEST);
        row.add(right2, BorderLayout.EAST);
        return row;
    }

    private JButton smallBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 10));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(28, 22));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void addCardToDeck(Card card) {
        String id = card.getId();
        int current = deckCounts.getOrDefault(id, 0);
        int max = MAX_COPIES.getOrDefault(card.getRarity(), 1);
        int total = deckCounts.values().stream().mapToInt(Integer::intValue).sum();
        if (current >= max || total >= MAX_DECK_SIZE) return;
        deckCounts.put(id, current + 1);
        deckCards.put(id, card);
        refreshDeckList(); refreshPool(); updateValidation();
    }

    private void removeCardFromDeck(Card card) {
        String id = card.getId();
        int count = deckCounts.getOrDefault(id, 0);
        if (count <= 0) return;
        if (count == 1) { deckCounts.remove(id); deckCards.remove(id); }
        else deckCounts.put(id, count - 1);
        refreshDeckList(); refreshPool(); updateValidation();
    }

    private void updateValidation() {
        int total = deckCounts.values().stream().mapToInt(Integer::intValue).sum();
        deckSizeLabel.setText(total + " / " + MAX_DECK_SIZE);
        if (total < MIN_DECK_SIZE) {
            validationLabel.setText("▲ " + (MIN_DECK_SIZE - total) + "장 더 필요");
            validationLabel.setForeground(new Color(231, 76, 60));
            startBtn.setEnabled(false);
        } else if (total > MAX_DECK_SIZE) {
            validationLabel.setText("▼ 덱이 꽉 찼습니다");
            validationLabel.setForeground(new Color(230, 126, 34));
            startBtn.setEnabled(false);
        } else {
            validationLabel.setText("✔ 시작 가능");
            validationLabel.setForeground(new Color(39, 174, 96));
            startBtn.setEnabled(true);
        }
    }

    private void onConfirm() {
        List<Card> finalDeck = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : deckCounts.entrySet()) {
            Card template = deckCards.get(entry.getKey());
            if (template == null) continue;
            for (int i = 0; i < entry.getValue(); i++) finalDeck.add(template.copy());
        }
        if (isPreGame) game.startNewGame(finalDeck);
        else game.applyEditedDeck(finalDeck);
    }

    private void onCancel() {
        if (isPreGame) game.goToMainMenu();
        else game.closeDeckBuilder();
    }

    private JComboBox<String> combo(String... items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setBackground(new Color(26, 26, 62));
        box.setForeground(Color.WHITE);
        return box;
    }
}
