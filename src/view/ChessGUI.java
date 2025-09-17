package view;

import ai.IANivel3;
import controller.Game;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import model.board.Position;
import model.pieces.Pawn;
import model.pieces.Piece;

public class ChessGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    // Cores fixas: cinza claro e azul
    private static final Color LIGHT_SQUARE = new Color(192, 192, 192); // Cinza claro
    private static final Color DARK_SQUARE = new Color(30, 144, 255);   // Azul

    private static final Color HILITE_SELECTED = new Color(255, 237, 41);
    private static final Color HILITE_LEGAL = new Color(26, 20, 196);
    private static final Color HILITE_LASTMOVE = new Color(220, 170, 30);

    private static final Border BORDER_SELECTED = new MatteBorder(3, 3, 3, 3, HILITE_SELECTED);
    private static final Border BORDER_LEGAL = new MatteBorder(3, 3, 3, 3, HILITE_LEGAL);
    private static final Border BORDER_LASTMOVE = new MatteBorder(3, 3, 3, 3, HILITE_LASTMOVE);

    private final Game game;

    private final JPanel boardPanel;
    private final JButton[][] squares = new JButton[8][8];

    private final JLabel status;
    private final JTextArea history;
    private final JScrollPane historyScroll;

    // Menu e controles
    private JCheckBoxMenuItem pcAsBlack;
    private JMenuItem newGameItem, quitItem;

    // Placar de peças capturadas
    private final List<Piece> capturedWhite = new ArrayList<>();
    private final List<Piece> capturedBlack = new ArrayList<>();
    private JLabel capturedWhiteLabel;
    private JLabel capturedBlackLabel;

    // Controle de seleção e movimentos legais
    private Position selected = null;
    private List<Position> legalForSelected = new ArrayList<>();

    // Realce do último lance
    private Position lastFrom = null, lastTo = null;

    // IA
    private boolean aiThinking = false;
    private final Random rnd = new Random();
    private int aiLevel = 0;

    public ChessGUI() {
        super("RoyalChess");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ignored) {}

        this.game = new Game();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        setJMenuBar(buildMenuBar());

        // Painel do tabuleiro (8x8)
        boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
        boardPanel.setBackground(new Color(210, 210, 210));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int rr = r;
                final int cc = c;
                JButton b = new JButton();
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setFocusPainted(false);
                b.setOpaque(true);
                b.setBorderPainted(true);
                b.setContentAreaFilled(true);
                b.setFont(b.getFont().deriveFont(Font.BOLD, 24f));
                b.addActionListener(e -> handleClick(new Position(rr, cc)));
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        // Barra inferior de status
        status = new JLabel("Jogada: Brancas");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        status.setFont(new Font("Segoe UI", Font.CENTER_BASELINE, 14));
        status.setForeground(Color.WHITE);

        // Histórico de jogadas
        history = new JTextArea(14, 22);
        history.setEditable(false);
        history.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        history.setForeground(Color.WHITE);
        history.setBackground(new Color(30, 144, 255)); // Azul claro
        historyScroll = new JScrollPane(history);

        // Painel lateral direito (histórico + controles)
        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        rightPanel.setBackground(new Color(210, 210, 210));
        JLabel histLabel = new JLabel("Histórico de Jogadas:");
        histLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        histLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        rightPanel.add(histLabel, BorderLayout.NORTH);
        rightPanel.add(historyScroll, BorderLayout.CENTER);
        rightPanel.add(buildSideControls(), BorderLayout.SOUTH);

        // Placar de peças capturadas
        capturedWhiteLabel = new JLabel("Brancas capturadas: ");
        capturedBlackLabel = new JLabel("Pretas capturadas: ");
        JPanel capturedPanel = new JPanel(new GridLayout(2, 1));
        capturedPanel.add(capturedWhiteLabel);
        capturedPanel.add(capturedBlackLabel);

        add(capturedPanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        boardPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh();
            }
        });

        getContentPane().setBackground(Color.DARK_GRAY);
        setMinimumSize(new Dimension(1100, 780));
        setLocationRelativeTo(null);

        setupAccelerators();

        setVisible(true);
        refresh();
        maybeTriggerAI();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu gameMenu = new JMenu("Menu");

        newGameItem = new JMenuItem("Novo Jogo");
        newGameItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newGameItem.addActionListener(e -> doNewGame());

        pcAsBlack = new JCheckBoxMenuItem("PC joga com as Pretas");
        pcAsBlack.setSelected(false);

        quitItem = new JMenuItem("Sair");
        quitItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quitItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(pcAsBlack);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);

        mb.add(gameMenu);
        return mb;
    }

    private JPanel buildSideControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnNew = new JButton("Novo Jogo");
        btnNew.addActionListener(e -> doNewGame());
        btnNew.setBackground(new Color(128,128,128));
        btnNew.setForeground(Color.WHITE);
        btnNew.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNew.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnNew.setBackground(new Color(93, 101, 50));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnNew.setBackground(new Color(128,128,128));
            }
        });
        panel.add(btnNew);

        JCheckBox cb = new JCheckBox("PC (Pretas)");
        cb.setSelected(pcAsBlack.isSelected());
        cb.addActionListener(e -> pcAsBlack.setSelected(cb.isSelected()));
        cb.setFocusPainted(false);
        cb.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cb.setForeground(new Color(126, 140, 84));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cb.setForeground(new Color(44, 62, 80));
            }
        });
        panel.add(cb);

        panel.add(new JLabel("IA:"));
        JComboBox<String> aiLevelBox = new JComboBox<>(new String[]{"Fácil", "Médio", "Difícil"});
        aiLevelBox.setSelectedIndex(aiLevel);
        aiLevelBox.addActionListener(e -> aiLevel = aiLevelBox.getSelectedIndex());
        panel.add(aiLevelBox);

        return panel;
    }

    private void setupAccelerators() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                        "newGame");
        getRootPane().getActionMap().put("newGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doNewGame();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                        "quit");
        getRootPane().getActionMap().put("quit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(ChessGUI.this, WindowEvent.WINDOW_CLOSING));
            }
        });
    }

    private void doNewGame() {
        selected = null;
        legalForSelected.clear();
        lastFrom = lastTo = null;
        aiThinking = false;
        capturedWhite.clear();
        capturedBlack.clear();
        game.newGame();
        refresh();
        maybeTriggerAI();
    }

    private void handleClick(Position clicked) {
        if (game.isGameOver() || aiThinking)
            return;

        if (pcAsBlack.isSelected() && !game.whiteToMove())
            return;

        Piece p = game.board().get(clicked);

        if (selected == null) {
            if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            }
        } else {
            List<Position> legals = game.legalMovesFrom(selected);
            if (legals.contains(clicked)) {
                Character promo = null;
                Piece moving = game.board().get(selected);
                Piece captured = game.board().get(clicked);
                if (moving instanceof Pawn && game.isPromotion(selected, clicked)) {
                    promo = askPromotion();
                }
                lastFrom = selected;
                lastTo = clicked;

                game.move(selected, clicked, promo);

                // Adiciona peça capturada ao placar
                if (captured != null) {
                    if (captured.isWhite()) capturedWhite.add(captured);
                    else capturedBlack.add(captured);
                }

                selected = null;
                legalForSelected.clear();

                refresh();
                maybeAnnounceEnd();
                maybeTriggerAI();
                return;
            } else if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            } else {
                selected = null;
                legalForSelected.clear();
            }
        }
        refresh();
    }

    private Character askPromotion() {
        String[] opts = { "Rainha", "Torre", "Bispo", "Cavalo" };
        int ch = JOptionPane.showOptionDialog(
                this,
                "Escolha a peça para promoção:",
                "Promoção",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opts,
                opts[0]);
        return switch (ch) {
            case 1 -> 'R';
            case 2 -> 'B';
            case 3 -> 'N';
            default -> 'Q';
        };
    }

    private void maybeTriggerAI() {
        if (game.isGameOver())
            return;
        if (!pcAsBlack.isSelected())
            return;
        if (game.whiteToMove())
            return;

        aiThinking = true;
        status.setText("Vez: Pretas — PC pensando...");

        new SwingWorker<Void, Void>() {
            Position aiFrom, aiTo;

            @Override
            protected Void doInBackground() {
                Move chosen = null;
                if (aiLevel == 0) {
                    var allMoves = collectAllLegalMovesForSide(false);
                    if (allMoves.isEmpty())
                        return null;
                    chosen = allMoves.get(rnd.nextInt(allMoves.size()));
                } else if (aiLevel == 1) {
                    var allMoves = collectAllLegalMovesForSide(false);
                    if (allMoves.isEmpty())
                        return null;
                    int bestScore = Integer.MIN_VALUE;
                    List<Move> bestList = new ArrayList<>();
                    for (Move mv : allMoves) {
                        int score = 0;
                        Piece target = game.board().get(mv.to);
                        if (target != null) score += pieceValue(target);
                        score += centerBonus(mv.to);
                        if (score > bestScore) {
                            bestScore = score;
                            bestList.clear();
                            bestList.add(mv);
                        } else if (score == bestScore) {
                            bestList.add(mv);
                        }
                    }
                    chosen = bestList.get(rnd.nextInt(bestList.size()));
                } else if (aiLevel == 2) {
                    IANivel3 iaNivel3 = new IANivel3();
                    model.board.Move move = iaNivel3.makeMove(game);
                    if (move != null) {
                        aiFrom = move.getFrom();
                        aiTo = move.getTo();
                        return null;
                    }
                }
                if (chosen != null) {
                    aiFrom = chosen.from;
                    aiTo = chosen.to;
                }
                return null;
            }

            @Override
            protected void done() {
                try { get(); } catch (Exception ignored) {}
                if (aiFrom != null && aiTo != null && !game.isGameOver() && !game.whiteToMove()) {
                    lastFrom = aiFrom;
                    lastTo = aiTo;
                    Character promo = null;
                    Piece moving = game.board().get(aiFrom);
                    Piece captured = game.board().get(aiTo);
                    if (moving instanceof Pawn && game.isPromotion(aiFrom, aiTo)) {
                        promo = 'Q';
                    }
                    game.move(aiFrom, aiTo, promo);

                    // Adiciona peça capturada ao placar
                    if (captured != null) {
                        if (captured.isWhite()) capturedWhite.add(captured);
                        else capturedBlack.add(captured);
                    }
                }
                aiThinking = false;
                refresh();
                maybeAnnounceEnd();
            }
        }.execute();
    }

    private static class Move {
        final Position from, to;
        Move(Position f, Position t) {
            this.from = f;
            this.to = t;
        }
    }

    private List<Move> collectAllLegalMovesForSide(boolean whiteSide) {
        List<Move> moves = new ArrayList<>();
        if (whiteSide != game.whiteToMove())
            return moves;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = game.board().get(from);
                if (piece != null && piece.isWhite() == whiteSide) {
                    for (Position to : game.legalMovesFrom(from)) {
                        moves.add(new Move(from, to));
                    }
                }
            }
        }
        return moves;
    }

    private int pieceValue(Piece p) {
        if (p == null)
            return 0;
        switch (p.getSymbol()) {
            case "P": return 100;
            case "N":
            case "B": return 300;
            case "R": return 500;
            case "Q": return 900;
            case "K": return 20000;
        }
        return 0;
    }

    private int centerBonus(Position pos) {
        int r = pos.getRow(), c = pos.getColumn();
        if ((r == 3 || r == 4) && (c == 3 || c == 4))
            return 10;
        if ((r >= 2 && r <= 5) && (c >= 2 && c <= 5))
            return 4;
        return 0;
    }

    private void refresh() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                Color base = light ? LIGHT_SQUARE : DARK_SQUARE;
                JButton b = squares[r][c];
                b.setBackground(base);
                b.setBorder(null);
                b.setToolTipText(null);
            }
        }

        if (lastFrom != null)
            squares[lastFrom.getRow()][lastFrom.getColumn()].setBorder(BORDER_LASTMOVE);
        if (lastTo != null)
            squares[lastTo.getRow()][lastTo.getColumn()].setBorder(BORDER_LASTMOVE);

        if (selected != null) {
            squares[selected.getRow()][selected.getColumn()].setBorder(BORDER_SELECTED);
            for (Position d : legalForSelected) {
                squares[d.getRow()][d.getColumn()].setBorder(BORDER_LEGAL);
            }
        }

        int iconSize = computeSquareIconSize();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.board().get(new Position(r, c));
                JButton b = squares[r][c];

                if (p == null) {
                    b.setIcon(null);
                    b.setText("");
                    continue;
                }

                char sym = p.getSymbol().charAt(0);
                ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), sym, iconSize);
                if (icon != null) {
                    b.setIcon(icon);
                    b.setText("");
                } else {
                    b.setIcon(null);
                    b.setText(toUnicode(p.getSymbol(), p.isWhite()));
                }
            }
        }

        String side = game.whiteToMove() ? "Brancas" : "Pretas";
        String chk = game.inCheck(game.whiteToMove()) ? " — Xeque!" : "";
        if (aiThinking)
            chk = " — PC pensando...";
        status.setText("Jogada: " + side + chk);

        history.setBackground(new Color(30, 144, 255)); // Azul claro

        StringBuilder sb = new StringBuilder();
        var hist = game.history();
        for (int i = 0; i < hist.size(); i++) {
            if (i % 2 == 0)
                sb.append((i / 2) + 1).append('.').append(' ');
            sb.append(hist.get(i)).append(' ');
            if (i % 2 == 1)
                sb.append('\n');
        }
        history.setText(sb.toString());
        history.setCaretPosition(history.getDocument().getLength());

        updateCapturedLabels();
    }

    private void updateCapturedLabels() {
        StringBuilder sbWhite = new StringBuilder("Brancas capturadas: ");
        for (Piece p : capturedWhite) sbWhite.append(toUnicode(p.getSymbol(), true)).append(" ");
        StringBuilder sbBlack = new StringBuilder("Pretas capturadas: ");
        for (Piece p : capturedBlack) sbBlack.append(toUnicode(p.getSymbol(), false)).append(" ");
        capturedWhiteLabel.setText(sbWhite.toString());
        capturedBlackLabel.setText(sbBlack.toString());
    }

    private void maybeAnnounceEnd() {
        if (!game.isGameOver())
            return;
        String msg;
        if (game.inCheck(game.whiteToMove())) {
            msg = "Xeque-mate! " + (game.whiteToMove() ? "Brancas" : "Pretas") + " estão em mate.";
        } else {
            msg = "Empate por afogamento (stalemate).";
        }
        JOptionPane.showMessageDialog(this, msg, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
    }

    private String toUnicode(String sym, boolean white) {
        return switch (sym) {
            case "K" -> white ? "\u2654" : "\u265A";
            case "Q" -> white ? "\u2655" : "\u265B";
            case "R" -> white ? "\u2656" : "\u265C";
            case "B" -> white ? "\u2657" : "\u265D";
            case "N" -> white ? "\u2658" : "\u265E";
            case "P" -> white ? "\u2659" : "\u265F";
            default -> "";
        };
    }

    private int computeSquareIconSize() {
        JButton b = squares[0][0];
        int w = Math.max(1, b.getWidth());
        int h = Math.max(1, b.getHeight());
        int side = Math.min(w, h);
        if (side <= 1)
            return 64;
        return Math.max(24, side - 8);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}