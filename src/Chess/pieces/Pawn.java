package Chess.pieces;

import Boardgame.Board;
import Boardgame.Position;
import Chess.ChessPiece;
import Chess.Color;

public class Pawn extends ChessPiece {
    public Pawn(Board board, Color color) {
        super(board, color);
    }

    @Override
    public boolean[][] possibleMoves() {
        boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];
        Position p = new Position(0,0);

        if (getColor() == Color.WHITE){
            p.setValues(position.getRow()-1, position.getColumn()); // testando se a primeira casa acima do peão está livre e se a posição existe
            if(getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)){
                mat[p.getRow()][p.getColumn()] = true;
            }

            p.setValues(position.getRow()-2, position.getColumn());
            Position p2 = new Position(position.getRow()-1, position.getColumn()); // testando se as duas casas acima do peão está livre e se for o primeiro movimento to peão, então ele pode andar duas casas.
            if(getBoard().positionExists(p) && !getBoard().thereIsAPiece(p) && getBoard().positionExists(p2) && !getBoard().thereIsAPiece(p2) && getMoveCount() == 0){
                mat[p.getRow()][p.getColumn()] = true;
            }

            p.setValues(position.getRow()-1, position.getColumn()-1); // testando se pode comer alguma peça na diagonal esquerda
            if(getBoard().positionExists(p) && isThereOpponentPiece(p)){
                mat[p.getRow()][p.getColumn()] = true;
            }

            p.setValues(position.getRow()-1, position.getColumn()+1); // testando se pode comer alguma peça na diagonal direita
            if(getBoard().positionExists(p) && isThereOpponentPiece(p)){
                mat[p.getRow()][p.getColumn()] = true;
            }

        } else{
            p.setValues(position.getRow()+1, position.getColumn()); // testando se a primeira casa acima do peão está livre e se a posição existe
            if(getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)){
                mat[p.getRow()][p.getColumn()] = true;
            }

            p.setValues(position.getRow()+2, position.getColumn());
            Position p2 = new Position(position.getRow()+1, position.getColumn()); // testando se as duas casas acima do peão está livre e se for o primeiro movimento to peão, então ele pode andar duas casas.
            if(getBoard().positionExists(p) && !getBoard().thereIsAPiece(p) && getBoard().positionExists(p2) && !getBoard().thereIsAPiece(p2) && getMoveCount() == 0){
                mat[p.getRow()][p.getColumn()] = true;
            }

            p.setValues(position.getRow()+1, position.getColumn()-1); // testando se pode comer alguma peça na diagonal esquerda
            if(getBoard().positionExists(p) && isThereOpponentPiece(p)){
                mat[p.getRow()][p.getColumn()] = true;
            }

            p.setValues(position.getRow()+1, position.getColumn()+1); // testando se pode comer alguma peça na diagonal direita
            if(getBoard().positionExists(p) && isThereOpponentPiece(p)){
                mat[p.getRow()][p.getColumn()] = true;
            }
        }
        return mat;
    }

    @Override
    public String toString() {
        return "P";
    }
}
