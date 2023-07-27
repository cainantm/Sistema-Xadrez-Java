package Chess.pieces;

import Boardgame.Board;
import Boardgame.Position;
import Chess.ChessMatch;
import Chess.ChessPiece;
import Chess.Color;

public class Pawn extends ChessPiece {

    private ChessMatch chessMatch;
    public Pawn(Board board, Color color, ChessMatch chessMatch) {
        super(board, color);
        this.chessMatch = chessMatch;
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

             // Movimento especial - enPassant peças brancas
            //aqui tem uma sacada, a peça branca tem que chegar na posição 3 e depois se o oponente mover o peão duas casas que vai disponibilizar o enPassant para a peça branca
            // se o peão preto já tiver movido +2 e depois a peça branca chegar, não será disponibilizado o movimento.
            if (position.getRow() == 3){ // no caso da peça branca, deve ser feito enPassant qnd a peça está na posição xadrez 4, que é 3 na matriz.
                Position left = new Position(position.getRow(), position.getColumn() - 1); // pegando a peça que está ao lado esquerdo
                if (getBoard().positionExists(left) && isThereOpponentPiece(left) && getBoard().piece(left) == chessMatch.getEnPassantVulnerable()){ // fazendo os testes para saber
                    //se a posição existe, se é uma peça oponente e se a peça está vulnerável ao enPassant
                mat[left.getRow() - 1][left.getColumn()] = true;
                }
                Position right = new Position(position.getRow(), position.getColumn() + 1);
                if (getBoard().positionExists(right) && isThereOpponentPiece(right) && getBoard().piece(right) == chessMatch.getEnPassantVulnerable()){
                    mat[right.getRow() - 1][right.getColumn()] = true;
                }
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

            // Movimento especial - enPassant peças preta

            if (position.getRow() == 4){ // no caso da peça preta, deve ser feito enPassant qnd a peça está na posição xadrez 4, sendo 4 na matriz.
                Position left = new Position(position.getRow(), position.getColumn() - 1); // pegando a peça que está ao lado esquerdo
                if (getBoard().positionExists(left) && isThereOpponentPiece(left) && getBoard().piece(left) == chessMatch.getEnPassantVulnerable()){ // fazendo os testes para saber se a peça ao lado
                    //se a posição existe, se é uma peça oponente e se a peça está vulnerável ao enPassant
                    mat[left.getRow() + 1][left.getColumn()] = true;
                }
                Position right = new Position(position.getRow(), position.getColumn() + 1);
                if (getBoard().positionExists(right) && isThereOpponentPiece(right) && getBoard().piece(right) == chessMatch.getEnPassantVulnerable()){
                    mat[right.getRow() + 1][right.getColumn()] = true;
                }
            }

        }
        return mat;
    }

    @Override
    public String toString() {
        return "P";
    }
}
