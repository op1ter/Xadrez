// ========================= src/controller/Zobrist.java (NOVO ARQUIVO) =========================
package controller;

import java.security.SecureRandom;

public final class Zobrist {
    // Array para [peça][casa] -> 12 peças (6 brancas, 6 pretas), 64 casas
    public static final long[][] PIECE_KEYS = new long[12][64];
    public static final long BLACK_TO_MOVE_KEY;

    // Índices das peças para o array
    public static final int WHITE_PAWN = 0;
    public static final int WHITE_KNIGHT = 1;
    public static final int WHITE_BISHOP = 2;
    public static final int WHITE_ROOK = 3;
    public static final int WHITE_QUEEN = 4;
    public static final int WHITE_KING = 5;
    public static final int BLACK_PAWN = 6;
    public static final int BLACK_KNIGHT = 7;
    public static final int BLACK_BISHOP = 8;
    public static final int BLACK_ROOK = 9;
    public static final int BLACK_QUEEN = 10;
    public static final int BLACK_KING = 11;

    static {
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 64; j++) {
                PIECE_KEYS[i][j] = random.nextLong();
            }
        }
        BLACK_TO_MOVE_KEY = random.nextLong();
        // Em um motor mais avançado, teríamos chaves para roque e en passant também.
    }
}