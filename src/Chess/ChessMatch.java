package Chess;

import Boardgame.Board;
import Boardgame.Piece;
import Boardgame.Position;
import Chess.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class ChessMatch {

    private Board board;

    private int turn;

    private Color currentPlayer;
    private boolean check;
    private boolean checkmate;
    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();
    private ChessPiece enPassantVulnerable;

    private ChessPiece promoted;

    public int getTurn(){
        return turn;
    }

    public Color getCurrentPlayer(){
        return currentPlayer;
    }

    public boolean getCheck(){
        return check;
    }

    public boolean getCheckmate(){
        return checkmate;
    }

    public ChessPiece getEnPassantVulnerable(){
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted() { return promoted; }

    public ChessMatch(){
        board = new Board(8,8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initialSetup();
    }

    public ChessPiece[][] getPieces(){
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
        for (int i=0; i<board.getRows(); i++){
            for (int j=0; j<board.getColumns(); j++){
                mat[i][j] = (ChessPiece) board.piece(i,j);
            }
        }
        return mat;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition){
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition){
        Position source = sourcePosition.toPosition(); // conversão da posição para valor de matriz
        Position target = targetPosition.toPosition(); // conversão da posição para valor de matriz

        validateSourcePosition(source);
        validateTargetPosition(source,target);

        Piece capturedPiece = makeMove(source, target);

        if(testCheck(currentPlayer)){ // testando se o jogador colocou-se em check;
            undoMove(source, target, capturedPiece);
            throw new ChessException("Você não pode se colocar em check");
        }

        ChessPiece movedPiece = (ChessPiece) board.piece(target); // referenciando a peça movida para o enPassant

        // movimento especial - promoção - deve ser tratada antes de testar o check, porque na troca de peça, pode ser que deixe o rei oponente em check
        promoted = null;
        if (movedPiece instanceof Pawn) {
            if ((movedPiece.getColor() == Color.WHITE && target.getRow() == 0) || (movedPiece.getColor() == Color.BLACK && target.getRow() == 7)){ // testando a cor da peça e o posiçao que ela tem que estar
                promoted = (ChessPiece) board.piece(target);
                promoted = replacePromotedPiece("Q");
            }
        }

        //testando se o oponente está em check e mudando o status de check
        check = (testCheck(opponent(currentPlayer))) ? true : false;


        if (testCheckmate(opponent(currentPlayer))){ // testando se o rei está em check.
            checkmate = true;
        } else{
            nextTurn();
        }

        //Movimento especial - enPassant

        if (movedPiece instanceof Pawn && (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2)) {
            enPassantVulnerable = movedPiece;
        } else {
            enPassantVulnerable = null;
        }

        return (ChessPiece) capturedPiece;
    }

    public ChessPiece replacePromotedPiece(String type){
        if (promoted == null) { // fazendo programação defensiva, com essas duas exceções
            throw new IllegalStateException("Não há peça para ser promovida");
        }
        if (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
            return promoted;
        }

        Position pos = promoted.getChessPosition().toPosition();
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);

        ChessPiece newPiece = newPiece(type, promoted.getColor()); // chamando a peça nova da mesma cor do peão
        board.placePiece(newPiece, pos); // colocando a peça promovida na posição do peão
        piecesOnTheBoard.add(newPiece);

        return newPiece;

    }

    private ChessPiece newPiece(String type, Color color){ // método para associar a letra com a peça a ser criada
        if (type.equals("B")) return new Bishop(board, color);
        if (type.equals("N")) return new Knight(board, color);
        if (type.equals("Q")) return new Queen(board, color);
        return new Rook(board, color);
    }

    private void validateSourcePosition(Position position){
        if (!board.thereIsAPiece(position)){ // validando se há uma peça naquela posição inicial escolhida
            throw new ChessException("Não há peça nessa posição inicial");
        }
        if (!board.piece(position).isThereAnyPossibleMove()) { //verificando se há movimentos possíveis para a peça
            throw new ChessException("Não existe movimentos possíveis para a peça escolhida");
        }
        if (currentPlayer != ((ChessPiece)board.piece(position)).getColor()){
            throw new ChessException("Peça escolhida não é sua");
        }
    }

    private Piece makeMove(Position source, Position target){
        ChessPiece p = (ChessPiece)board.removePiece(source); // pega a peça da posição inicial, e passa para a variável e remove da posição
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target); // remove a peça da posição final
        board.placePiece(p, target); // coloca a peça p na posição final

        if (capturedPiece != null){ //se a peça captured diferir de nulo (quer dizer que há uma peça) então ela será retirada de uma lista e adicionada a outra
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        // movimento especial - roque pequeno (lado do rei)
        if(p instanceof King && target.getColumn() == source.getColumn() + 2){ // testando se o rei moveu duas casas para direita (roque pequeno)
            Position sourceT = new Position(source.getRow(), source.getColumn()+3); // pegando a posiçao da torre
            Position targetT = new Position(source.getRow(), source.getColumn()+1); // local para onde a torre vai
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT); // removendo a torre da posição que ela está
            board.placePiece(rook, targetT); // movendo a torre para a posição final ao lado esquerdo do rei
            rook.increaseMoveCount(); // aumentando o número de movimentos da peça
        }

        // movimento especial - roque grande (lado da rainha)
        if(p instanceof King && target.getColumn() == source.getColumn() - 2){ // testando se o rei moveu duas casas para direita (roque pequeno)
            Position sourceT = new Position(source.getRow(), source.getColumn()-4); // pegando a posiçao da torre
            Position targetT = new Position(source.getRow(), source.getColumn()-1); // local para onde a torre vai
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT); // removendo a torre da posição que ela está
            board.placePiece(rook, targetT); // movendo a torre para a posição final ao lado esquerdo do rei
            rook.increaseMoveCount(); // aumentando o número de movimentos da peça
        }

        // movimento especial - enPassant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) { // para saber se foi um enPassant, a coluna inicial vai ser diferente da final e não haverá peça capturada.
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) { // no caso para peça branca
                    pawnPosition = new Position(target.getRow() + 1, target.getColumn()); // a posição da peça a ser capturada será abaixo da peça branca, por isso +1.
                } else {
                    pawnPosition = new Position(target.getRow() - 1, target.getColumn()); // para peça preta a posição a ser capturada sera acima da peça preta, por isso -1.
                }
                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }
        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece){
        ChessPiece p = (ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);
        if(capturedPiece != null){
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }

        // movimento especial - roque pequeno (lado do rei)
        if(p instanceof King && target.getColumn() == source.getColumn() + 2){
            Position sourceT = new Position(source.getRow(), source.getColumn()+3);
            Position targetT = new Position(source.getRow(), source.getColumn()+1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        // movimento especial - roque grande (lado da rainha)
        if(p instanceof King && target.getColumn() == source.getColumn() - 2){
            Position sourceT = new Position(source.getRow(), source.getColumn()-4);
            Position targetT = new Position(source.getRow(), source.getColumn()-1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        // movimento especial - enPassant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) { // para desfazer o movimento, é necessário testar se a peça capturada era vulneravel
                ChessPiece pawn = (ChessPiece) board.removePiece(target);
                // ao retornar a peça ao lugar (feito no começo do UndoMove) a peça vai para o lugar que ela supostamente foi capturada, no lugar da peça que capturou, mas para esse movimento
                // isso não é verdade, deve estar numa posição abaixo.
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) { // no caso para peça branca
                    pawnPosition = new Position(3, target.getColumn()); // para ajeitar onde a peça deve ser devolvida, já se coloca a linha que ela deve voltar, no caso para peças pretas a linha 3
                } else {
                    pawnPosition = new Position(4, target.getColumn()); // a mesma coisa, mas para peças brancas é na linha 4.
                }
                board.placePiece(pawn, pawnPosition);
            }
        }
    }

    private void placeNewPiece(char column, int row, ChessPiece piece){
        board.placePiece(piece, new ChessPosition(column,row).toPosition());
        piecesOnTheBoard.add(piece);
    }

    private void initialSetup(){
        placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE, this)); // this é a auto referencia da partida que é necessária para instaciar o rei
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

        placeNewPiece('d', 8, new Queen(board, Color.BLACK));
        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));

    }

    private void validateTargetPosition(Position source, Position target){
        if (!board.piece(source).possibleMove(target)) {
            throw new ChessException("A peça escolhida não pode ser mover para posição de destino");
        }
    }

    private void nextTurn(){
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE; // condição ternária para alternar o jogador atual; condição ? verdadeiro : falso;
    }

    private Color opponent(Color color){
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(Color color){ //procurando pelo rei da cor.
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).toList(); //Procurando peças na lista da mesma cor do argumento. piece não tem cor, então é feito downcast para ChessPiece.
        for (Piece p : list){
            if (p instanceof King){
                return (ChessPiece)p;
            }
        }
        throw new IllegalStateException("Não existe o rei " + color + " no tabuleiro");
    }

    private boolean testCheck(Color color){
        Position kingPosition = king(color).getChessPosition().toPosition(); // aqui pega a posição do rei em formato de matriz.
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).toList(); // filtrando as peças do oponente pela cor.
        for (Piece p : opponentPieces){ // procurando por movimentos que vao para o rei.
            boolean[][] mat = p.possibleMoves(); // cria uma matriz com os movimentos das peças inimigas.
            if (mat[kingPosition.getRow()][kingPosition.getColumn()]){ // compara se a matriz de movimentos da peça inimiga conincide com a posição do rei.
                return true;
            }
        }
        return false;
    }

    private boolean testCheckmate(Color color){ // teste para saber se está em checkmate ou não.
        if(!testCheck(color)){ //se a peça nao estiver em check, então não é checkmate.
            return false;
        }
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).toList(); //aqui filtra todas as peças da cor
        for(Piece p : list){
            boolean[][] mat = p.possibleMoves(); // para cada peça da lista, procura os movimentos possíveis e joga numa matriz.
            for (int i=0; i<board.getRows(); i++){
                for(int j=0; j<board.getColumns(); j++){
                    if(mat[i][j]){ // percorre a matriz e testa se a posição da matriz é um movimento possível, caso seja possível, ele tira do check?
                        Position source = ((ChessPiece)p).getChessPosition().toPosition(); // a peça p vai para a posição i,j da matriz e testar se está em check ainda, senão estiver, ai retorna falso, caso esteja retorna true.
                        // aqui é feito um downcast para chesspiece pra conseguir chamar a posição, pq position é protected.
                        Position target = new Position(i,j);
                        Piece capturedPiece = makeMove(source, target); // aqui é feito é colocada a peça p na posição da matriz
                        boolean testCheck = testCheck(color); // aqui vai testar se o rei da cor ainda está em check;
                        undoMove(source,target,capturedPiece); // aqui precisa desfazer o movimento, pq o movimento é só um teste.
                        if(!testCheck){ // aqui se a variável é não é true, então tirou o rei do check.
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

}
