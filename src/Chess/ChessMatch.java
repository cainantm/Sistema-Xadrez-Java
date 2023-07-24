package Chess;

import Boardgame.Board;
import Boardgame.Piece;
import Boardgame.Position;
import Chess.pieces.King;
import Chess.pieces.Rook;

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

        //testando se o oponente está em check e mudando o status de check
        check = (testCheck(opponent(currentPlayer))) ? true : false;


        if (testCheckmate(opponent(currentPlayer))){ // testando se o rei está em check.
            checkmate = true;
        } else{
            nextTurn();
        }

        return (ChessPiece) capturedPiece;
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
        Piece p = board.removePiece(source); // pega a peça da posição inicial, e passa para a variável e remove da posição
        Piece capturedPiece = board.removePiece(target); // remove a peça da posição final
        board.placePiece(p, target); // coloca a peça p na posição final

        if (capturedPiece != null){ //se a peça captured diferir de nulo (quer dizer que há uma peça) então ela será retirada de uma lista e adicionada a outra
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece){
        Piece p = board.removePiece(target);
        board.placePiece(p, source);
        if(capturedPiece != null){
            board.placePiece(capturedPiece, source);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }
    }

    private void placeNewPiece(char column, int row, ChessPiece piece){
        board.placePiece(piece, new ChessPosition(column,row).toPosition());
        piecesOnTheBoard.add(piece);
    }

    private void initialSetup(){
        placeNewPiece('h', 7, new Rook(board, Color.WHITE));
        placeNewPiece('d', 1, new Rook(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE));

        placeNewPiece('b', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 8, new King(board, Color.BLACK));

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
