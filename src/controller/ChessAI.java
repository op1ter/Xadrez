// ========================= src/controller/ChessAI.java (VERSÃO FINAL CORRIGIDA) =========================
package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.board.Position;
import model.pieces.Piece;

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
        // <<<<<<<<<<< CORREÇÃO 1: Guardar de quem é a vez no início >>>>>>>>>>>>
        final boolean isMaximizingPlayer = game.whiteToMove();
        double bestScore = isMaximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        List<Position[]> bestMoves = new ArrayList<>();

        List<Position[]> allMoves = collectAllLegalMovesForSide(this.game);
        
        // Otimização: Ordenação de Movimentos
        allMoves.sort((move1, move2) -> {
            boolean isCapture1 = game.board().get(move1[1]) != null;
            boolean isCapture2 = game.board().get(move2[1]) != null;
            if (isCapture1 && !isCapture2) return -1;
            if (!isCapture1 && isCapture2) return 1;
            return 0;
        });

        if (allMoves.isEmpty()) return null;

        for (Position[] move : allMoves) {
            MoveInfo info = game.makeMove(move[0], move[1]);
            double score = minimax(game, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            game.unmakeMove(info);

            // <<<<<<<<<<< CORREÇÃO 2: Usar a variável local para decidir se maximiza ou minimiza >>>>>>>>>>>>
            if (isMaximizingPlayer) { 
                if (score > bestScore) {
                    bestScore = score;
                    bestMoves.clear();
                    bestMoves.add(move);
                } else if (score == bestScore) {
                    bestMoves.add(move);
                }
            } else { // Jogador Minimizando (Pretas)
                if (score < bestScore) {
                    bestScore = score;
                    bestMoves.clear();
                    bestMoves.add(move);
                } else if (score == bestScore) {
                    bestMoves.add(move);
                }
            }
        }

        if (bestMoves.isEmpty()) return null;
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private double minimax(Game board, int depth, double alpha, double beta) {
        if (depth == 0 || board.isGameOver()) {
            return evaluateBoard(board);
        }

        List<Position[]> allMoves = collectAllLegalMovesForSide(board);
        
        allMoves.sort((move1, move2) -> {
            boolean isCapture1 = board.board().get(move1[1]) != null;
            boolean isCapture2 = board.board().get(move2[1]) != null;
            if (isCapture1 && !isCapture2) return -1;
            if (!isCapture1 && isCapture2) return 1;
            return 0;
        });

        if (board.whiteToMove()) { // Maximizando
            double maxEval = Double.NEGATIVE_INFINITY;
            for (Position[] move : allMoves) {
                MoveInfo info = board.makeMove(move[0], move[1]);
                double eval = minimax(board, depth - 1, alpha, beta);
                board.unmakeMove(info);

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else { // Minimizando
            double minEval = Double.POSITIVE_INFINITY;
            for (Position[] move : allMoves) {
                MoveInfo info = board.makeMove(move[0], move[1]);
                double eval = minimax(board, depth - 1, alpha, beta);
                board.unmakeMove(info);
                
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
        
        // Esta avaliação é simples. Uma melhoria futura poderia ser usar
        // "piece-square tables" para dar valor à posição das peças.
        for (Piece p : board.board().pieces(true)) { // Peças Brancas
            score += getPieceValue(p);
        }
        for (Piece p : board.board().pieces(false)) { // Peças Pretas
            score -= getPieceValue(p);
        }

        // Bônus/penalidades por xeque (ajuda a IA a buscar xeques)
        if (board.inCheck(true)) { // Rei branco em xeque
            score -= 50;
        } else if (board.inCheck(false)) { // Rei preto em xeque
            score += 50;
        }
        
        // Xeque-mate é um valor terminal
        if (board.isCheckmate(true)) {
            return Double.NEGATIVE_INFINITY; // Derrota para as Brancas
        } else if (board.isCheckmate(false)) {
            return Double.POSITIVE_INFINITY; // Vitória para as Brancas
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
            case "K" -> 20000; // Rei tem um valor material muito alto para evitar trocas
            default -> 0;
        };
    }
    
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