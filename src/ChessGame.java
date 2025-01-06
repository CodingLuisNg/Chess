import java.io.IOException;

public class ChessGame {
    private ChessPlayer player;
    private ChessPlayer opponent;
    private Chess[][] board;
    public static final int BOARD_SIZE = 8;
    private final ChessGameClient client;
    private ChessGameGUI gui;
    public int currentPlayer = 1;

    public ChessGame() throws IOException {
        client = new ChessGameClient(this);
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

    public void setPlayer(int playerID) {
        player = new ChessPlayer(playerID, 1);
    }

    public Chess[][] getBoard() {
        return board;
    }

    public void makeMove(int playerID, int selectedRow, int selectedCol, int row, int col) {
        Chess floatingPiece;
        if (playerID == player.getColour()) {
            floatingPiece = gui.getFloatingPiece();
        } else {
            floatingPiece = board[selectedRow][selectedCol];
            board[selectedRow][selectedCol] = null;
        }
        currentPlayer *= -1;
        if (board[row][col] != null && board[row][col].type == 'K') {
            board[row][col] = floatingPiece;
            client.sendCheckMate();
        } else if (floatingPiece.type == 'P' && (row == 0 || row == board.length - 1)) {
            gui.promotion(row, col, floatingPiece.colour);
        } else if (board[row][col] != null && floatingPiece.type == 'K' && floatingPiece.colour == board[row][col].colour) {
            castling(selectedRow, selectedCol, row, col, floatingPiece);
        } else {
            board[row][col] = floatingPiece;
        }
    }

    private void castling(int selectedRow, int selectedCol, int row, int col, Chess floatingPiece) {
        int new_col;
        if (selectedCol < col) {
            new_col = selectedCol + 1;
            selectedCol += 2;
        } else {
            new_col = selectedCol - 1;
            selectedCol -= 2;
        }
        board[selectedRow][selectedCol] = floatingPiece;
        board[row][new_col] = board[row][col];
        board[row][col] = null;
    }

    public void start(int playerID) {
        setPlayer(playerID);
        opponent = new ChessPlayer(-player.getColour(), -1);
        board = new Chess[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
        gui = new ChessGameGUI(this);
    }

    public static void main(String[] args) throws IOException {
        new ChessGame();
    }

    public ChessGameGUI getGUI() {
        return gui;
    }

    public ChessGameClient getClient() {
        return client;
    }
}
