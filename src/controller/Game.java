package controller;
import model.board.Board;
import model.board.Position;
import model.pieces.*;
public class Game {
private Board board;
private boolean isWhiteTurn;
private boolean isGameOver;
private Piece selectedPiece;
public Game() {
board = new Board();
isWhiteTurn = true;
isGameOver = false;
setupPieces();
}
private void setupPieces() {
// Colocar peças na posição inicial
// Peças brancas
board.placePiece(new Rook(board, true), new Position(7, 0));
board.placePiece(new Knight(board, true), new Position(7, 1));
board.placePiece(new Bishop(board, true), new Position(7, 2));
board.placePiece(new Queen(board, true), new Position(7, 3));
board.placePiece(new King(board, true), new Position(7, 4));
board.placePiece(new Bishop(board, true), new Position(7, 5));
board.placePiece(new Knight(board, true), new Position(7, 6));
board.placePiece(new Rook(board, true), new Position(7, 7));
for (int col = 0; col < 8; col++) {
board.placePiece(new Pawn(board, true), new Position(6, col));
}
// Peças pretas (mesma lógica)
// ...
}
public Board getBoard() {
return board;
}
public boolean isWhiteTurn() {
return isWhiteTurn;
}
public boolean isGameOver() {
return isGameOver;
}
// Métodos para selecionar peça e fazer movimento
public Piece getSelectedPiece() {
return selectedPiece;
}
public void selectPiece(Position position) {
Piece piece = board.getPieceAt(position);
// Só pode selecionar peça da cor do jogador atual
if (piece != null && piece.isWhite() == isWhiteTurn) {
selectedPiece = piece;
}
}
public boolean movePiece(Position destination) {
if (selectedPiece == null || isGameOver) {
return false;
}
// Verificar se o movimento é válido
if (!selectedPiece.canMoveTo(destination)) {
return false;
}
// Verificar se o movimento deixa o rei em xeque
if (moveCausesCheck(selectedPiece, destination)) {
return false;
}
// Capturar peça, se necessário
Piece capturedPiece = board.getPieceAt(destination);
// Guardar posição original para desfazer o movimento, se necessário
Position originalPosition = selectedPiece.getPosition();
// Fazer o movimento
board.removePiece(originalPosition);
board.placePiece(selectedPiece, destination);
// Verificar condições especiais (promoção de peão, etc.)
checkSpecialConditions(selectedPiece, destination);
// Verificar se o oponente está em xeque ou xeque-mate
checkGameStatus();
// Passar o turno
isWhiteTurn = !isWhiteTurn;
selectedPiece = null;
return true;
}
private boolean moveCausesCheck(Piece piece, Position destination) {
// Implementação para verificar se um movimento deixa o próprio rei em xeque
// ...
return false;
}
private void checkSpecialConditions(Piece piece, Position destination) {
// Implementação para promoção de peão, roque, en passant, etc.
// ...
}
private void checkGameStatus() {
// Implementação para verificar xeque, xeque-mate e empate
// ...
}