// ========================= src/controller/MoveInfo.java (VERSÃO FINAL CORRIGIDA) =========================
package controller;

import model.board.Position;
import model.pieces.Piece;

/**
 * Guarda o estado de um movimento para que ele possa ser desfeito (unmade).
 * Versão aprimorada para incluir jogadas especiais como roque e promoção.
 */
public class MoveInfo {
    final Piece pieceMoved;
    final Position from;
    final Position to;

    // Informações para reverter o estado
    final Piece pieceCaptured;
    final boolean wasMoved; // Se a peça já tinha se movido antes deste lance
    final Position previousEnPassantTarget;

    // <<<<<<<<<<< MUDANÇA: Flags para jogadas especiais >>>>>>>>>>>>
    final boolean wasCastle;
    final boolean wasPromotion;

    // <<<<<<<<<<< MUDANÇA: Construtor atualizado para incluir as novas flags >>>>>>>>>>>>
    public MoveInfo(Piece pieceMoved, Position from, Position to, Piece pieceCaptured, boolean wasMoved, Position previousEnPassantTarget, boolean wasCastle, boolean wasPromotion) {
        this.pieceMoved = pieceMoved;
        this.from = from;
        this.to = to;
        this.pieceCaptured = pieceCaptured;
        this.wasMoved = wasMoved;
        this.previousEnPassantTarget = previousEnPassantTarget;
        this.wasCastle = wasCastle;
        this.wasPromotion = wasPromotion;
    }
}