package com.spiregame.view.scene;

import com.spiregame.controller.GameController;
import com.spiregame.model.map.MapNode;
import com.spiregame.model.map.NodeType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MapScene extends JPanel {

    private final GameController game;
    private JLabel hpLabel, goldLabel, floorLabel;
    private MapCanvas mapCanvas;

    public MapScene(GameController game) {
        this.game = game;
        setBackground(new Color(7, 7, 18));
        setLayout(new BorderLayout());
        buildLayout();
    }

    private void buildLayout() {
        setLeft(buildLeftPanel());
        JScrollPane scroll = new JScrollPane(buildMapCanvas());
        scroll.setBackground(new Color(7, 7, 18));
        scroll.getViewport().setBackground(new Color(7, 7, 18));
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(buildLeftPanel(), BorderLayout.WEST);
        add(scroll, BorderLayout.CENTER);

        // Scroll to bottom (1층)
        SwingUtilities.invokeLater(() -> {
            JScrollBar vBar = scroll.getVerticalScrollBar();
            vBar.setValue(vBar.getMaximum());
        });
    }

    private void setLeft(JPanel panel) {
        // handled in buildLayout
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(210, 600));
        panel.setBackground(new Color(5, 5, 16));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(26, 26, 62)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(26, 26, 62)),
            BorderFactory.createEmptyBorder(16, 12, 16, 12)
        ));

        JLabel title = new JLabel("🧙 모험가");
        title.setFont(new Font("Serif", Font.BOLD, 17));
        title.setForeground(new Color(243, 156, 18));
        title.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(26, 26, 62));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        hpLabel   = statLabel("❤ HP", new Color(231, 76, 60));
        goldLabel  = statLabel("💰 골드", new Color(241, 196, 15));
        floorLabel = statLabel("🗺 층", new Color(52, 152, 219));

        JButton deckBtn = mapButton("📚  덱 보기", new Color(20, 90, 50), new Color(39, 174, 96));
        deckBtn.addActionListener(e -> game.openDeckBuilder());
        deckBtn.setAlignmentX(LEFT_ALIGNMENT);

        JPanel legend = buildLegend();
        legend.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(10));
        panel.add(hpLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(goldLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(floorLabel);
        panel.add(Box.createVerticalGlue());
        panel.add(legend);
        panel.add(Box.createVerticalStrut(10));
        panel.add(deckBtn);
        return panel;
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel();
        legend.setBackground(new Color(13, 13, 34));
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JLabel lTitle = new JLabel("노드 종류");
        lTitle.setFont(new Font("Serif", Font.BOLD, 10));
        lTitle.setForeground(new Color(149, 165, 166));
        lTitle.setAlignmentX(LEFT_ALIGNMENT);
        legend.add(lTitle);

        for (NodeType type : NodeType.values()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            row.setOpaque(false);
            JLabel dot = new JLabel("●");
            dot.setForeground(Color.decode(type.getColor()));
            dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
            JLabel lbl = new JLabel(type.getDisplayName());
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
            lbl.setForeground(new Color(189, 195, 199));
            row.add(dot);
            row.add(lbl);
            legend.add(row);
        }
        return legend;
    }

    private JScrollPane buildMapCanvas() {
        mapCanvas = new MapCanvas();
        JScrollPane scroll = new JScrollPane(mapCanvas);
        scroll.setBackground(new Color(7, 7, 18));
        scroll.getViewport().setBackground(new Color(7, 7, 18));
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        SwingUtilities.invokeLater(() -> {
            JScrollBar vb = scroll.getVerticalScrollBar();
            vb.setValue(vb.getMaximum());
        });
        return scroll;
    }

    public void refresh() {
        if (game.getPlayer() == null) return;
        hpLabel.setText("❤ HP: " + game.getPlayer().getCurrentHp() + "/" + game.getPlayer().getMaxHp());
        goldLabel.setText("💰 골드: " + game.getPlayer().getGold());
        floorLabel.setText("🗺 층: " + game.getCurrentFloor() + "/" + game.getTotalFloors());

        // Rebuild map canvas
        removeAll();
        buildLayout();
        revalidate();
        repaint();
    }

    private JLabel statLabel(String text, Color color) {
        JLabel lbl = new JLabel(text + ": ...");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(color);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(13, 13, 34));
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton mapButton(String text, Color normal, Color hover) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Serif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(normal);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        });
        return btn;
    }

    // ── Inner Canvas that draws the map ───────────────────────────
    private class MapCanvas extends JPanel {

        private static final int NODE_R = 20;
        private static final int BOSS_R = 28;
        private static final int CANVAS_W = 620;
        private static final int CANVAS_H = 680;

        MapCanvas() {
            setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));
            setBackground(new Color(7, 7, 18));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    handleClick(e.getX(), e.getY());
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    updateCursor(e.getX(), e.getY());
                }
            });
        }

        private void handleClick(int mx, int my) {
            for (List<MapNode> floor : game.getMapFloors()) {
                for (MapNode node : floor) {
                    if (node.isAvailable() && !node.isVisited()) {
                        int r = node.getType() == NodeType.BOSS ? BOSS_R : NODE_R;
                        double dx = mx - node.getX(), dy = my - node.getY();
                        if (dx*dx + dy*dy <= r*r) {
                            game.enterNode(node);
                            return;
                        }
                    }
                }
            }
        }

        private void updateCursor(int mx, int my) {
            for (List<MapNode> floor : game.getMapFloors()) {
                for (MapNode node : floor) {
                    if (node.isAvailable() && !node.isVisited()) {
                        int r = node.getType() == NodeType.BOSS ? BOSS_R : NODE_R;
                        double dx = mx - node.getX(), dy = my - node.getY();
                        if (dx*dx + dy*dy <= r*r) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            return;
                        }
                    }
                }
            }
            setCursor(Cursor.getDefaultCursor());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            List<List<MapNode>> floors = game.getMapFloors();
            if (floors.isEmpty()) { g2.dispose(); return; }

            // Title
            g2.setFont(new Font("Serif", Font.BOLD, 18));
            g2.setColor(new Color(243, 156, 18));
            g2.drawString("🗺  탑 정복 경로", 10, 30);

            // Draw edges first
            for (List<MapNode> floor : floors) {
                for (MapNode node : floor) {
                    drawEdges(g2, node);
                }
            }
            // Then nodes
            for (List<MapNode> floor : floors) {
                for (MapNode node : floor) {
                    drawNode(g2, node);
                }
            }
            g2.dispose();
        }

        private void drawEdges(Graphics2D g2, MapNode node) {
            for (MapNode child : node.getChildren()) {
                int x1 = (int) node.getX(), y1 = (int) node.getY();
                int x2 = (int) child.getX(), y2 = (int) child.getY();
                if (node.isVisited()) {
                    g2.setColor(new Color(39, 174, 96));
                    g2.setStroke(new BasicStroke(2.5f));
                } else if (node.isAvailable()) {
                    g2.setColor(new Color(243, 156, 18, 150));
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1f, new float[]{6f, 4f}, 0f));
                } else {
                    g2.setColor(new Color(44, 62, 80));
                    g2.setStroke(new BasicStroke(1.5f));
                }
                g2.drawLine(x1, y1, x2, y2);
            }
            g2.setStroke(new BasicStroke(1f));
        }

        private void drawNode(Graphics2D g2, MapNode node) {
            boolean isBoss = node.getType() == NodeType.BOSS;
            int r = isBoss ? BOSS_R : NODE_R;
            int x = (int) node.getX() - r, y = (int) node.getY() - r;
            int d = r * 2;

            Color typeColor = Color.decode(node.getType().getColor());

            if (node.isVisited()) {
                g2.setColor(new Color(typeColor.getRed(), typeColor.getGreen(), typeColor.getBlue(), 130));
                g2.fillOval(x, y, d, d);
                g2.setColor(typeColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, d, d);
            } else if (node.isAvailable()) {
                // Glow
                g2.setColor(new Color(typeColor.getRed(), typeColor.getGreen(), typeColor.getBlue(), 60));
                g2.fillOval(x - 4, y - 4, d + 8, d + 8);
                g2.setColor(new Color(typeColor.getRed(), typeColor.getGreen(), typeColor.getBlue(), 215));
                g2.fillOval(x, y, d, d);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x, y, d, d);
            } else {
                g2.setColor(new Color(26, 26, 62, 128));
                g2.fillOval(x, y, d, d);
                g2.setColor(new Color(44, 62, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, d, d);
            }
            g2.setStroke(new BasicStroke(1f));

            // Floor number below node
            if (!isBoss) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.setColor(node.isAvailable() ? new Color(243, 156, 18) : new Color(74, 74, 106));
                String floorStr = String.valueOf(node.getFloor());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(floorStr, (int) node.getX() - fm.stringWidth(floorStr) / 2,
                    (int) node.getY() + r + 12);
            }

            // Type display name (abbreviated)
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.setColor(node.isAvailable() ? Color.WHITE : new Color(100, 100, 130));
            String abbr = node.getType().getDisplayName();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(abbr, (int) node.getX() - fm.stringWidth(abbr) / 2, (int) node.getY() + fm.getAscent() / 2 - 2);
        }
    }
}
