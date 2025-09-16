// ========================= src/controller/ChessAI.java (ALTERADO) =========================
package controller;

import model.board.Position;
import model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChessAI {

    private final Game game;
    private int searchDepth;
    private static final Random random = new Random();
    private final TranspositionTable transpositionTable = new TranspositionTable();

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
        transpositionTable.clear(); // Limpa a memória a cada nova jogada
        return findBestMove(searchDepth);
    }

    private Position[] findBestMove(int depth) {
        final boolean isMaximizingPlayer = game.whiteToMove();
        double bestScore = isMaximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        List<Position[]> bestMoves = new ArrayList<>();

        List<Position[]> allMoves = collectAllLegalMovesForSide(this.game);
        allMoves.sort((move1, move2) -> { /*... ordenação ...*/ return 0; }); // Ordenação ajuda muito

        for (Position[] move : allMoves) {
            MoveInfo info = game.makeMove(move[0], move[1]);
            double score = minimax(game, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            game.unmakeMove(info);

            if (isMaximizingPlayer) {
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
        if (bestMoves.isEmpty()) return null;
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private double minimax(Game board, int depth, double alpha, double beta) {
        long hash = board.getZobristHash();
        double originalAlpha = alpha;

        // 1. VERIFICA NA TABELA DE TRANSPOSIÇÃO
        TableEntry entry = transpositionTable.probe(hash);
        if (entry != null && entry.depth >= depth) {
            if (entry.type == TableEntry.NodeType.EXACT) {
                return entry.score;
            } else if (entry.type == TableEntry.NodeType.LOWERBOUND) {
                alpha = Math.max(alpha, entry.score);
            } else if (entry.type == TableEntry.NodeType.UPPERBOUND) {
                beta = Math.min(beta, entry.score);
            }
            if (alpha >= beta) {
                return entry.score;
            }
        }

        // 2. CONDIÇÃO DE PARADA -> CHAMA A BUSCA DE QUIESCÊNCIA
        if (depth == 0) {
            return quiescenceSearch(board, alpha, beta);
        }

        List<Position[]> allMoves = collectAllLegalMovesForSide(board);
        if (allMoves.isEmpty()) {
            return board.inCheck(board.whiteToMove()) ? (board.whiteToMove() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY) : 0;
        }

        double bestScore;
        if (board.whiteToMove()) { // Maximizando
            bestScore = Double.NEGATIVE_INFINITY;
            for (Position[] move : allMoves) {
                MoveInfo info = board.makeMove(move[0], move[1]);
                double eval = minimax(board, depth - 1, alpha, beta);
                board.unmakeMove(info);
                bestScore = Math.max(bestScore, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
        } else { // Minimizando
            bestScore = Double.POSITIVE_INFINITY;
            for (Position[] move : allMoves) {
                MoveInfo info = board.makeMove(move[0], move[1]);
                double eval = minimax(board, depth - 1, alpha, beta);
                board.unmakeMove(info);
                bestScore = Math.min(bestScore, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
        }

        // 3. SALVA O RESULTADO NA TABELA ANTES DE RETORNAR
        TableEntry.NodeType type;
        if (bestScore <= originalAlpha) {
            type = TableEntry.NodeType.UPPERBOUND;
        } else if (bestScore >= beta) {
            type = TableEntry.NodeType.LOWERBOUND;
        } else {
            type = TableEntry.NodeType.EXACT;
        }
        transpositionTable.store(hash, new TableEntry(bestScore, depth, type));

        return bestScore;
    }

    /**
     * NOVO MÉTODO: Busca de Quiescência
     * Analisa apenas capturas para evitar o "efeito horizonte".
     */
    private double quiescenceSearch(Game board, double alpha, double beta) {
        double standPatScore = evaluateBoard(board);

        if (standPatScore >= beta) {
            return beta;
        }
        alpha = Math.max(alpha, standPatScore);

        List<Position[]> captureMoves = collectAllLegalCaptureMoves(board);
        // Idealmente, ordenar capturas por valor (MVV-LVA)

        for (Position[] capture : captureMoves) {
            MoveInfo info = board.makeMove(capture[0], capture[1]);
            double score = -quiescenceSearch(board, -beta, -alpha); // Técnica NegaMax
            board.unmakeMove(info);

            if (score >= beta) {
                return beta; // Falha alta
            }
            alpha = Math.max(alpha, score);
        }
        return alpha;
    }

    // Método auxiliar para a busca de quiescência
    private List<Position[]> collectAllLegalCaptureMoves(Game gameState) {
        List<Position[]> moves = new ArrayList<>();
        for (Position[] move : collectAllLegalMovesForSide(gameState)) {
            if (gameState.board().get(move[1]) != null) { // Se a casa de destino não está vazia
                moves.add(move);
            }
            // Adicionar lógica para En Passant também
        }
        return moves;
    }

    private double evaluateBoard(Game board) {
        // Sua função de avaliação (pode ser aprimorada com Piece-Square Tables)
        double score = 0;
        for (Piece p : board.board().pieces(true)) score += getPieceValue(p);
        for (Piece p : board.board().pieces(false)) score -= getPieceValue(p);
        return score;
    }

    private double getPieceValue(Piece p) {
        if (p == null) return 0;
        return switch (p.getSymbol()) {
            case "P" -> 10; case "N", "B" -> 30; case "R" -> 50; case "Q" -> 90; case "K" -> 20000;
            default -> 0;
        };
    }
    
    private List<Position[]> collectAllLegalMovesForSide(Game gameState) {
        List<Position[]> moves = new ArrayList<>();
        boolean whiteSide = gameState.whiteToMove();
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) {
            Position from = new Position(r, c);
            Piece piece = gameState.board().get(from);
            if (piece != null && piece.isWhite() == whiteSide) {
                for (Position to : gameState.legalMovesFrom(from)) {
                    moves.add(new Position[]{from, to});
                }
            }
        }
        return moves;
    }
}