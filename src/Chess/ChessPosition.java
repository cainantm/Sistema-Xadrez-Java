package Chess;

import Boardgame.Position;

public class ChessPosition {

    private char column;
    private int row;

    public ChessPosition(char column, int row) {
        if (column < 'a' || column > 'h' || row < 1 || row > 8){
            throw new ChessException("Erro instanciando posição no xadrez. Valores válidos são de a1 até h8");
        }
        this.column = column;
        this.row = row;
    }

    public char getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    protected Position toPosition(){
        return new Position(8-row, column-'a');
        // a posição da linha do xadrez vai ser sempre o máximo (8) menos o row
        // a coluna é atraves do valor unicode de a, sendo a-a=0, b-a=1 e assim por diante.
    }

    protected static ChessPosition fromPosition(Position position){
        return new ChessPosition((char)('a' - position.getColumn()), 8- position.getRow());
    }

    @Override
    public String toString(){
        return "" + column + row; // o string vazio é pra forçar a concatenação.
    }

}
