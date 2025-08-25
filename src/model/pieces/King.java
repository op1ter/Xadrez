package model.pieces;
import model.board.Board;
import model.board.Position;
import java.util.ArrayList;
import java.util.List;
public class King extends Piece {
public King(Board board, boolean isWhite) {
super(board, isWhite);
}
@Override
public List getPossibleMoves() {
List moves = new ArrayList<>();
int[][] directions = {
{-1, -1}, {-1, 0}, {-1, 1},
{0, -1}, {0, 1},
{1, -1}, {1, 0}, {1, 1}
};
for (int[] dir : directions) {
Position newPos = new Position(
position.getRow() + dir[0],
position.getColumn() + dir[1]
);
if (newPos.isValid()) {
Piece pieceAt = board.getPieceAt(newPos);
if (pieceAt == null || pieceAt.isWhite() != isWhite) {
moves.add(newPos);
}
}
}
// Adicionar ao método getPossibleMoves() do King
// Verificar possibilidade de roque
if (!hasMoved) { // Rei não se moveu
// Roque curto (lado do rei)
Piece rookKingSide = board.getPieceAt(
new Position(position.getRow(), 7));
if (rookKingSide instanceof Rook &&
!((Rook)rookKingSide).hasMoved()) {
boolean pathClear = true;
for (int col = position.getColumn() + 1;
col < 7; col++) {
if (!board.isPositionEmpty(
new Position(position.getRow(), col))) {
pathClear = false;
break;
}
}
if (pathClear) {
Position castlingPosition = new Position(
position.getRow(), position.getColumn() + 2);
moves.add(castlingPosition);
}
}
// Roque longo (lado da rainha)
// Lógica similar...
}
return moves;
}
