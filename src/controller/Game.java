// ========================= src/controller/Game.java (VERSÃO FINAL CORRIGIDA) =========================
package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.board.Board;
import model.board.Position;
import model.pieces.*;

public class Game {

    private Board board;
    private boolean whiteToMove = true;
    private boolean gameOver = false;
    private Position enPassantTarget = null;
    private final List<String> history = new ArrayList<>();

    public Game() {
        this.board = new Board();
        setupPieces();
    }

    // --- Getters Públicos ---
    public Board board() { return board; }
    public boolean whiteToMove() { return whiteToMove; }
    public boolean isGameOver() { return gameOver; }
    public List<String> history() { return Collections.unmodifiableList(history); }

    // --- Controle do Jogo ---
    public void newGame() {
        this.board = new Board();
        this.whiteToMove = true;
        this.gameOver = false;
        this.enPassantTarget = null;
        this.history.clear();
        setupPieces();
    }

    // --- Lógica de Movimento (para o jogador humano) ---
    public void move(Position from, Position to, Character promotion) {
        if (gameOver) return;
        Piece p = board.get(from);
        if (p == null || p.isWhite() != whiteToMove) return;

        List<Position> legal = legalMovesFrom(from);
        if (!legal.contains(to)) return;

        String moveNotation = generateMoveNotation(from, to, promotion);
        makeMove(from, to, promotion);

        if (isCheckmate(whiteToMove)) {
            moveNotation += "#";
            gameOver = true;
        } else if (inCheck(whiteToMove)) {
            moveNotation += "+";
        }
        addHistory(moveNotation);
        if (!gameOver) checkGameEnd();
    }

    public boolean isPromotion(Position from, Position to) {
        Piece p = board.get(from);
        if (!(p instanceof Pawn)) return false;
        return p.isWhite() ? to.getRow() == 0 : to.getRow() == 7;
    }

    // =================================================================================
    // MÉTODOS OTIMIZADOS PARA A IA (makeMove / unmakeMove)
    // =================================================================================

    public MoveInfo makeMove(Position from, Position to) {
        return makeMove(from, to, 'Q');
    }

    private MoveInfo makeMove(Position from, Position to, Character promotionChar) {
        Piece mover = board.get(from);
        Piece captured = board.get(to);
        boolean wasMoved = mover.hasMoved();
        Position oldEnPassantTarget = this.enPassantTarget;
        boolean isPawn = mover instanceof Pawn;
        boolean isKing = mover instanceof King;
        boolean isCastle = isKing && Math.abs(from.getColumn() - to.getColumn()) == 2;
        boolean isPromotion = isPawn && (to.getRow() == 0 || to.getRow() == 7);

        MoveInfo info = new MoveInfo(mover, from, to, captured, wasMoved, oldEnPassantTarget, isCastle, isPromotion);

        board.set(to, mover);
        board.set(from, null);
        mover.setMoved(true);

        if (isPromotion) {
            Piece newPiece = switch (Character.toUpperCase(promotionChar)) {
                case 'R' -> new Rook(board, mover.isWhite());
                case 'B' -> new Bishop(board, mover.isWhite());
                case 'N' -> new Knight(board, mover.isWhite());
                default -> new Queen(board, mover.isWhite());
            };
            board.set(to, newPiece);
        }
        if (isCastle) {
            int row = from.getRow();
            if (to.getColumn() == 6) { // Roque curto
                Piece rook = board.get(new Position(row, 7));
                board.set(new Position(row, 5), rook);
                board.set(new Position(row, 7), null);
                if (rook != null) rook.setMoved(true);
            } else { // Roque longo
                Piece rook = board.get(new Position(row, 0));
                board.set(new Position(row, 3), rook);
                board.set(new Position(row, 0), null);
                if (rook != null) rook.setMoved(true);
            }
        }
        if (isPawn && to.equals(oldEnPassantTarget)) {
            int dir = mover.isWhite() ? 1 : -1;
            Position capturedPawnPos = new Position(to.getRow() + dir, to.getColumn());
            info = new MoveInfo(mover, from, to, board.get(capturedPawnPos), wasMoved, oldEnPassantTarget, false, false);
            board.set(capturedPawnPos, null);
        }
        if (isPawn && Math.abs(from.getRow() - to.getRow()) == 2) {
            this.enPassantTarget = new Position((from.getRow() + to.getRow()) / 2, from.getColumn());
        } else {
            this.enPassantTarget = null;
        }
        this.whiteToMove = !this.whiteToMove;
        return info;
    }

    public void unmakeMove(MoveInfo info) {
        this.whiteToMove = !this.whiteToMove;
        this.enPassantTarget = info.previousEnPassantTarget;
        Piece pieceToRestore = info.wasPromotion ? new Pawn(board, info.pieceMoved.isWhite()) : info.pieceMoved;
        board.set(info.from, pieceToRestore);
        pieceToRestore.setMoved(info.wasMoved);

        if (info.wasCastle) {
            int row = info.from.getRow();
            if (info.to.getColumn() == 6) {
                Piece rook = board.get(new Position(row, 5));
                board.set(new Position(row, 7), rook);
                board.set(new Position(row, 5), null);
                if (rook != null) rook.setMoved(false);
            } else {
                Piece rook = board.get(new Position(row, 3));
                board.set(new Position(row, 0), rook);
                board.set(new Position(row, 3), null);
                if (rook != null) rook.setMoved(false);
            }
        }
        boolean wasEnPassantCapture = info.pieceMoved instanceof Pawn && info.to.equals(info.previousEnPassantTarget);
        if (wasEnPassantCapture) {
            board.set(info.to, null);
            int dir = info.pieceMoved.isWhite() ? 1 : -1;
            Position capturedPawnPos = new Position(info.to.getRow() + dir, info.to.getColumn());
            board.set(capturedPawnPos, info.pieceCaptured);
        } else {
            board.set(info.to, info.pieceCaptured);
        }
    }

    // --- Métodos de Checagem de Estado e Legalidade ---
    
    public List<Position> legalMovesFrom(Position from) {
        Piece p = board.get(from);
        if (p == null || p.isWhite() != whiteToMove) return List.of();
        List<Position> moves = new ArrayList<>(p.getPossibleMoves());
        addSpecialMoves(p, from, moves);
        moves.removeIf(to -> leavesKingInCheck(from, to));
        return moves;
    }

    private void addSpecialMoves(Piece p, Position from, List<Position> moves) {
        if (p instanceof Pawn && enPassantTarget != null) {
            int dir = p.isWhite() ? -1 : 1;
            if (from.getRow() + dir == enPassantTarget.getRow() && Math.abs(from.getColumn() - enPassantTarget.getColumn()) == 1) {
                moves.add(enPassantTarget);
            }
        }
        if (p instanceof King && !p.hasMoved() && !inCheck(p.isWhite())) {
            int row = from.getRow();
            if (canCastle(row, 7, new int[]{5, 6})) moves.add(new Position(row, 6));
            if (canCastle(row, 0, new int[]{3, 2, 1})) moves.add(new Position(row, 2));
        }
    }

    private boolean canCastle(int row, int rookCol, int[] emptyCols) {
        Piece rook = board.get(new Position(row, rookCol));
        if (!(rook instanceof Rook) || rook.hasMoved()) return false;
        for (int col : emptyCols) {
            if (board.get(new Position(row, col)) != null) return false;
        }
        if (isSquareAttacked(new Position(row, 4), !whiteToMove) ||
            isSquareAttacked(new Position(row, emptyCols[0]), !whiteToMove)) {
            return false;
        }
        return emptyCols.length <= 1 || !isSquareAttacked(new Position(row, emptyCols[1]), !whiteToMove);
    }

    private boolean leavesKingInCheck(Position from, Position to) {
        MoveInfo info = this.makeMove(from, to);
        boolean isCheck = this.inCheck(info.pieceMoved.isWhite());
        this.unmakeMove(info);
        return isCheck;
    }

    public boolean inCheck(boolean whiteSide) {
        Position kingPos = findKing(whiteSide);
        if (kingPos == null) return false;
        return isSquareAttacked(kingPos, !whiteSide);
    }

    public boolean isCheckmate(boolean whiteSide) {
        if (!inCheck(whiteSide)) return false;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                if (board.get(from) != null && board.get(from).isWhite() == whiteSide) {
                    if (!legalMovesFrom(from).isEmpty()) return false;
                }
            }
        }
        return true;
    }

    private void checkGameEnd() {
        if (isCheckmate(whiteToMove)) {
            gameOver = true;
            return;
        }
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                if (board.get(from) != null && board.get(from).isWhite() == whiteToMove) {
                    if (!legalMovesFrom(from).isEmpty()) return;
                }
            }
        }
        if (!inCheck(whiteToMove)) gameOver = true; // Stalemate
    }

    /**
     * <<<<<<<<<<< MÉTODO CRÍTICO CORRIGIDO >>>>>>>>>>>>
     * Verifica se uma casa 'sq' é atacada por qualquer peça da cor 'byWhite'.
     * Esta implementação é "bruta" e não-recursiva para evitar ciclos infinitos.
     */
    private boolean isSquareAttacked(Position sq, boolean byWhite) {
        // Ataques de Peão
        int dir = byWhite ? -1 : 1;
        Position p1 = new Position(sq.getRow() + dir, sq.getColumn() - 1);
        Position p2 = new Position(sq.getRow() + dir, sq.getColumn() + 1);
        if (p1.isValid() && board.get(p1) instanceof Pawn && board.get(p1).isWhite() == byWhite) return true;
        if (p2.isValid() && board.get(p2) instanceof Pawn && board.get(p2).isWhite() == byWhite) return true;

        // Ataques de Cavalo
        int[][] knightJumps = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
        for(int[] j : knightJumps) {
            Position p = new Position(sq.getRow() + j[0], sq.getColumn() + j[1]);
            if (p.isValid() && board.get(p) instanceof Knight && board.get(p).isWhite() == byWhite) return true;
        }

        // Ataques de Rei
        for (int dr = -1; dr <= 1; dr++) for (int dc = -1; dc <= 1; dc++) {
            if (dr == 0 && dc == 0) continue;
            Position p = new Position(sq.getRow() + dr, sq.getColumn() + dc);
            if (p.isValid() && board.get(p) instanceof King && board.get(p).isWhite() == byWhite) return true;
        }

        // Ataques Deslizantes (Torre, Bispo, Rainha)
        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}};
        for (int[] d : directions) {
            Position p = new Position(sq.getRow(), sq.getColumn());
            while(true) {
                p = new Position(p.getRow() + d[0], p.getColumn() + d[1]);
                if (!p.isValid()) break;
                Piece piece = board.get(p);
                if (piece != null) {
                    if (piece.isWhite() == byWhite) {
                        boolean isRookMove = (d[0] == 0 || d[1] == 0);
                        if (isRookMove && (piece instanceof Rook || piece instanceof Queen)) return true;
                        if (!isRookMove && (piece instanceof Bishop || piece instanceof Queen)) return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    private Position findKing(boolean whiteSide) {
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) {
            Position pos = new Position(r, c);
            Piece p = board.get(pos);
            if (p instanceof King && p.isWhite() == whiteSide) return pos;
        }
        return null;
    }

    // --- Métodos de Notação e Setup ---
    private String generateMoveNotation(Position from, Position to, Character promotion) {
        return coord(from) + "-" + coord(to) + (promotion != null ? "=" + promotion : "");
    }
    private void addHistory(String moveStr) { history.add(moveStr); }
    private String coord(Position p) {
        return "" + (char)('a' + p.getColumn()) + (8 - p.getRow());
    }

    private void setupPieces() {
        // Peças Pretas
        board.placePiece(new Rook(board, false), new Position(0, 0));
        board.placePiece(new Knight(board, false), new Position(0, 1));
        board.placePiece(new Bishop(board, false), new Position(0, 2));
        board.placePiece(new Queen(board, false), new Position(0, 3));
        board.placePiece(new King(board, false), new Position(0, 4));
        board.placePiece(new Bishop(board, false), new Position(0, 5));
        board.placePiece(new Knight(board, false), new Position(0, 6));
        board.placePiece(new Rook(board, false), new Position(0, 7));
        for (int c = 0; c < 8; c++) board.placePiece(new Pawn(board, false), new Position(1, c));

        // Peças Brancas
        board.placePiece(new Rook(board, true), new Position(7, 0));
        board.placePiece(new Knight(board, true), new Position(7, 1));
        board.placePiece(new Bishop(board, true), new Position(7, 2));
        board.placePiece(new Queen(board, true), new Position(7, 3));
        board.placePiece(new King(board, true), new Position(7, 4));
        board.placePiece(new Bishop(board, true), new Position(7, 5));
        board.placePiece(new Knight(board, true), new Position(7, 6));
        board.placePiece(new Rook(board, true), new Position(7, 7));
        for (int c = 0; c < 8; c++) board.placePiece(new Pawn(board, true), new Position(6, c));
    }
}