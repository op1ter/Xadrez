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
        final boolean isMaximizingPlayer = game.whiteToMove();
        double bestScore = isMaximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        List<Position[]> bestMoves = new ArrayList<>();

        List<Position[]> allMoves = collectAllLegalMovesForSide(this.game);
        // Ordena capturas primeiro (MVV-LVA)
        allMoves.sort((move1, move2) -> Integer.compare(
            moveScore(move2, this.game), moveScore(move1, this.game)
        ));

        for (Position[] move : allMoves) {
            Game clonedGame = game.snapshotShallow();
            clonedGame.move(move[0], move[1], null);
            double score = minimax(clonedGame, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

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

    // Ordenação MVV-LVA
    private int moveScore(Position[] move, Game gameState) {
        Piece captured = gameState.board().get(move[1]);
        Piece mover = gameState.board().get(move[0]);
        int capturedValue = captured != null ? (int)getPieceValue(captured) : 0;
        int moverValue = mover != null ? (int)getPieceValue(mover) : 0;
        return capturedValue * 100 - moverValue;
    }

    private double minimax(Game board, int depth, double alpha, double beta) {
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
                Game clonedGame = board.snapshotShallow();
                clonedGame.move(move[0], move[1], null);
                double eval = minimax(clonedGame, depth - 1, alpha, beta);
                bestScore = Math.max(bestScore, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
        } else { // Minimizando
            bestScore = Double.POSITIVE_INFINITY;
            for (Position[] move : allMoves) {
                Game clonedGame = board.snapshotShallow();
                clonedGame.move(move[0], move[1], null);
                double eval = minimax(clonedGame, depth - 1, alpha, beta);
                bestScore = Math.min(bestScore, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
        }
        return bestScore;
    }

    private double quiescenceSearch(Game board, double alpha, double beta) {
        double standPatScore = evaluateBoard(board);

        if (standPatScore >= beta) {
            return beta;
        }
        alpha = Math.max(alpha, standPatScore);

        List<Position[]> captureMoves = collectAllLegalCaptureMoves(board);
        captureMoves.sort((move1, move2) -> Integer.compare(
            moveScore(move2, board), moveScore(move1, board)
        ));

        for (Position[] capture : captureMoves) {
            Game clonedGame = board.snapshotShallow();
            clonedGame.move(capture[0], capture[1], null);
            double score = quiescenceSearch(clonedGame, alpha, beta);
            if (score >= beta) {
                return beta;
            }
            alpha = Math.max(alpha, score);
        }
        return alpha;
    }

    private List<Position[]> collectAllLegalCaptureMoves(Game gameState) {
        List<Position[]> moves = new ArrayList<>();
        for (Position[] move : collectAllLegalMovesForSide(gameState)) {
            if (gameState.board().get(move[1]) != null) {
                moves.add(move);
            }
        }
        return moves;
    }

    private double evaluateBoard(Game board) {
        double score = 0;
        for (Piece p : board.board().pieces(true)) score += getPieceValue(p);
        for (Piece p : board.board().pieces(false)) score -= getPieceValue(p);
        return score;
    }

    private double getPieceValue(Piece p) {
        if (p == null) return 0;
        switch (p.getSymbol()) {
            case "P": return 10;
            case "N": return 30;
            case "B": return 30;
            case "R": return 50;
            case "Q": return 90;
            case "K": return 20000;
            default: return 0;
        }
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