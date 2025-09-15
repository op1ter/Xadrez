// ========================= src/controller/ChessAI.java (CORRIGIDO) =========================
package controller;

import model.board.Board;
import model.board.Position;
import model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChessAI {

    private final Game game;
    private int searchDepth;
    private static final Random random = new Random();

    public ChessAI(Game game, int depth) {
        this.game = game;
        this.searchDepth = depth;
    }

    public void setDepth(int depth) {
        this.searchDepth = depth;
    }

    public int getDepth() {
        return this.searchDepth;
    }

    public Position[] findBestMove() {
        return findBestMove(searchDepth);
    }

    private Position[] findBestMove(int depth) {
        Position bestFrom = null;
        Position bestTo = null;
        double bestScore = game.whiteToMove() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        List<Position[]> bestMoves = new ArrayList<>();
        
        // Coleta todos os movimentos legais para o estado atual do jogo
        List<Position[]> allMoves = collectAllLegalMovesForSide(this.game);
        
        if (allMoves.isEmpty()) return null;

        for (Position[] move : allMoves) {
            Game snapshot = game.snapshotShallow();
            snapshot.move(move[0], move[1], null);
            
            double score = minimax(snapshot, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

            if (game.whiteToMove()) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMoves.clear();
                    bestMoves.add(move);
                } else if (score == bestScore) {
                    bestMoves.add(move);
                }
            } else { 
                if (score < bestScore) {
                    bestScore = score;
                    bestMoves.clear();
                    bestMoves.add(move);
                } else if (score == bestScore) {
                    bestMoves.add(move);
                }
            }
        }
        
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private double minimax(Game board, int depth, double alpha, double beta) {
        if (depth == 0 || board.isGameOver()) {
            return evaluateBoard(board);
        }

        // AQUI ESTÁ A CORREÇÃO PRINCIPAL: Passar 'board' em vez de usar 'this.game'
        List<Position[]> allMoves = collectAllLegalMovesForSide(board);

        if (board.whiteToMove()) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (Position[] move : allMoves) {
                Game snapshot = board.snapshotShallow();
                snapshot.move(move[0], move[1], null);
                double eval = minimax(snapshot, depth - 1, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (Position[] move : allMoves) {
                Game snapshot = board.snapshotShallow();
                snapshot.move(move[0], move[1], null);
                double eval = minimax(snapshot, depth - 1, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    private double evaluateBoard(Game board) {
        double score = 0;
        
        for (Piece p : board.board().pieces(true)) {
            score += getPieceValue(p);
        }
        for (Piece p : board.board().pieces(false)) {
            score -= getPieceValue(p);
        }

        if (board.isCheckmate(true)) {
            score = -1000000;
        } else if (board.isCheckmate(false)) {
            score = 1000000;
        } else if (board.inCheck(true)) {
            score -= 50;
        } else if (board.inCheck(false)) {
            score += 50;
        }

        return score;
    }

    private double getPieceValue(Piece p) {
        if (p == null) return 0;
        return switch (p.getSymbol()) {
            case "P" -> 10;
            case "N", "B" -> 30;
            case "R" -> 50;
            case "Q" -> 90;
            case "K" -> 2000;
            default -> 0;
        };
    }
    
    // MÉTODO ALTERADO PARA RECEBER O ESTADO DO JOGO COMO PARÂMETRO
    private List<Position[]> collectAllLegalMovesForSide(Game gameState) {
        List<Position[]> moves = new ArrayList<>();
        boolean whiteSide = gameState.whiteToMove();
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = gameState.board().get(from);
                if (piece != null && piece.isWhite() == whiteSide) {
                    for (Position to : gameState.legalMovesFrom(from)) {
                        moves.add(new Position[]{from, to});
                    }
                }
            }
        }
        return moves;
    }
}