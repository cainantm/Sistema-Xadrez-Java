package Boardgame;

public class Piece {

    protected Position position;
    private Board board;

    public Piece(Board board) {
        this.board = board;
        position = null; // qnd a peça é criada a posição é nula pq ela ainda nao foi colocada no tabuleiro.
    }

    protected Board getBoard() {
        return board;
    }

}
