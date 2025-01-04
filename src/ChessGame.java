public class ChessGame {
    private final ChessPlayer[] players;
    private final ChessPlayer player;
    private final Chess[][] board;
    private final ChessGameGUI gui;
    private static final int BOARD_SIZE = 8;

    public ChessGame(ChessPlayer player) {
        this.player = player;
        this.players = new ChessPlayer[]{player, new ChessPlayer(-player.getColour())};
        board = new Chess[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
        gui = new ChessGameGUI(board, player);
        play();
    }

    private void initializeBoard() {
        int colour = player.getColour();
        // Place pawns
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Pawn(-colour, -1);
            board[6][i] = new Pawn(colour, 1);
        }

        // Place other pieces
        Chess[] backRow = {new Rook(-colour), new Knight(-colour), new Bishop(-colour), new Queen(-colour), new King(-colour), new Bishop(-colour), new Knight(-colour), new Rook(-colour)};
        System.arraycopy(backRow, 0, board[0], 0, BOARD_SIZE);

        Chess[] frontRow = {new Rook(colour), new Knight(colour), new Bishop(colour), new Queen(colour), new King(colour), new Bishop(colour), new Knight(colour), new Rook(colour)};
        System.arraycopy(frontRow, 0, board[7], 0, BOARD_SIZE);
    }

    private void play() {
        boolean checkMate = false;
    }

    public static void main(String[] args) {
        new ChessGame(new ChessPlayer(-1));
    }
}
