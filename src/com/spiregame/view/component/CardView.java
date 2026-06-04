package com.spiregame.view.component;

import com.spiregame.model.card.Card;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class CardView extends JPanel {
//카드 크기 부분도 수정했습니다.
    private static final int CARD_W = 100;
    private static final int CARD_H = 145;

    private final Card card;
    private boolean playable = true;
    private Runnable onPlayAction;

    public CardView(Card card) {
        this.card = card;
        setPreferredSize(new Dimension(CARD_W, CARD_H));
        setMaximumSize(new Dimension(CARD_W, CARD_H));
        setMinimumSize(new Dimension(CARD_W, CARD_H));
        setOpaque(false);
        setLayout(new BorderLayout());
        setupInteractions();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Shadow / glow
        Color rarityColor = Color.decode(card.getRarity().getColor());
        if (playable) {
            g2.setColor(new Color(rarityColor.getRed(), rarityColor.getGreen(), rarityColor.getBlue(), 60));
            g2.fillRoundRect(3, 3, w - 2, h - 2, 14, 14);
        }

        // Card background
        g2.setColor(new Color(26, 26, 46));
        g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);

        // Rarity border
        g2.setColor(playable ? rarityColor : rarityColor.darker());
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

        // Type color bar at top
        Color typeColor = Color.decode(card.getType().getColor());
        g2.setColor(playable ? typeColor : typeColor.darker());
        g2.fillRoundRect(0, 0, w, 28, 12, 12);
        g2.fillRect(0, 14, w, 14);

        // Cost badge (top-left)
        g2.setColor(new Color(243, 156, 18));
        g2.fillOval(4, 4, 24, 24);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(4, 4, 24, 24);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String costStr = String.valueOf(card.getCost());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(costStr, 4 + (24 - fm.stringWidth(costStr)) / 2, 4 + 17);

        // Type emoji
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        String emoji = card.getType().getEmoji();
        fm = g2.getFontMetrics();
        g2.setColor(playable ? Color.WHITE : Color.GRAY);
        g2.drawString(emoji, (w - fm.stringWidth(emoji)) / 2, 52);

        // Card name
        g2.setFont(new Font("Serif", Font.BOLD, 11));
        g2.setColor(playable ? Color.WHITE : Color.GRAY);
        drawCenteredString(g2, card.getName(), 0, 60, w, 20);

        // Divider
        g2.setColor(new Color(44, 62, 80));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(10, 82, w - 10, 82);

        // Description
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(playable ? new Color(189, 195, 199) : Color.GRAY);
        drawWrappedString(g2, card.getDescription(), 6, 88, w - 12, 50);

        // Rarity label at bottom
        g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
        g2.setColor(rarityColor);
        fm = g2.getFontMetrics();
        String rarityText = card.getRarity().getDisplayName();
        g2.drawString(rarityText, (w - fm.stringWidth(rarityText)) / 2, h - 5);

        g2.dispose();
    }

    private void drawCenteredString(Graphics2D g2, String text, int x, int y, int width, int height) {
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (width - fm.stringWidth(text)) / 2;
        int ty = y + (height + fm.getAscent()) / 2 - 2;
        g2.drawString(text, tx, ty);
    }

    private void drawWrappedString(Graphics2D g2, String text, int x, int y, int maxW, int maxH) {
        FontMetrics fm = g2.getFontMetrics();
        int lineH = fm.getHeight();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y + fm.getAscent();
        int maxY = y + maxH;

        for (String word : words) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(test) <= maxW) {
                line = new StringBuilder(test);
            } else {
                if (curY <= maxY) {
                    g2.drawString(line.toString(), x + (maxW - fm.stringWidth(line.toString())) / 2, curY);
                }
                line = new StringBuilder(word);
                curY += lineH;
            }
        }
        if (!line.isEmpty() && curY <= maxY) {
            g2.drawString(line.toString(), x + (maxW - fm.stringWidth(line.toString())) / 2, curY);
        }
    }

    private void setupInteractions() {
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (playable) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (playable && onPlayAction != null) onPlayAction.run();
            }
        });
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
        repaint();
    }

    public void setOnPlayAction(Runnable action) { this.onPlayAction = action; }
    public Card getCard() { return card; }
}
