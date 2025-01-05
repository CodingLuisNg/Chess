public class ChessGame {
    private final ChessPlayer player;
    private final ChessPlayer opponent;
    private final Chess[][] board;
    private static final int BOARD_SIZE = 8;

    public ChessGame(ChessPlayer player) {
        this.player = player;
        opponent = new ChessPlayer(-player.getColour(), -player.getSide());
        board = new Chess[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
        new ChessGameGUI(this);
    }

    private void initializeBoard() {
        int colour = player.getColour();
        // Place pawns
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Pawn(-colour, opponent.getSide(), this);
            board[6][i] = new Pawn(colour, player.getSide(), this);
        }

        // Place other pieces
        Chess[] backRow = {new Rook(-colour), new Knight(-colour), new Bishop(-colour), new Queen(-colour), new King(-colour), new Bishop(-colour), new Knight(-colour), new Rook(-colour)};
        System.arraycopy(backRow, 0, board[0], 0, BOARD_SIZE);

        Chess[] frontRow = {new Rook(colour), new Knight(colour), new Bishop(colour), new Queen(colour), new King(colour), new Bishop(colour), new Knight(colour), new Rook(colour)};
        System.arraycopy(frontRow, 0, board[7], 0, BOARD_SIZE);
    }

    public ChessPlayer getPlayer() {
        return player;
    }

    public Chess[][] getBoard() {
        return board;
    }

    public static void main(String[] args) {
        new ChessGame(new ChessPlayer(-1, 1));
    }
}
