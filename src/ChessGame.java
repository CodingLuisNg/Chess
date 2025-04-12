import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Main class representing the chess game logic, handling board state and player interactions.
 */
public class ChessGame {
    private ChessPlayer player;
    private ChessPlayer opponent;
    private Chess[][] board;
    public static final int BOARD_SIZE = 8;
    private ChessGameClient client;
    private ChessGameGUI gui;
    public int currentPlayer = 1; // 1 for white, -1 for black
    private int colour; // Player's color
    private int[] myLastMove;
    private int[] opponentLastMove;
    private ArrayList<Chess> graveyard;

    /**
     * Constructor initializes the game and manages the server/client setup.
     */
    public ChessGame() throws IOException {
        graveyard = new ArrayList<>();
        promptForNetworkSetup();
    }

    /**
     * Prompts user to host or join a game and sets up networking.
     */
    private void promptForNetworkSetup() throws IOException {
        int choice = JOptionPane.showConfirmDialog(
                null,
                "Do you want to host the game?",
                "Host or Join",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            setupAsHost();
        } else {
            setupAsClient();
        }
    }

    /**
     * Sets up the game as host (server).
     */
    private void setupAsHost() throws IOException {
        String portInput = JOptionPane.showInputDialog(null, "Enter the port to host the game:", "2396");
        int port = Integer.parseInt(portInput);
        
        // Start the server in a new thread
        new Thread(() -> {
            try {
                new ChessGameServer().start(port);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        // Initialize the client to connect to localhost
        client = new ChessGameClient(this, "localhost", port);
    }

    /**
     * Sets up the game as client (connecting to a server).
     */
    private void setupAsClient() throws IOException {
        String serverAddress = JOptionPane.showInputDialog(null, "Input the server IP address", "localhost");
        String portInput = JOptionPane.showInputDialog(null, "Enter the port of the game:", "2396");
        int port = Integer.parseInt(portInput);
        client = new ChessGameClient(this, serverAddress, port);
    }

    /**
     * Initializes the chess board with pieces in starting positions.
     */
    private void initializeBoard() {
        board = new Chess[BOARD_SIZE][BOARD_SIZE];
        colour = player.getColour();
        
        // Place pawns
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Pawn(-colour, opponent.getSide(), this);
            board[6][i] = new Pawn(colour, player.getSide(), this);
        }

        // Place back row pieces for opponent
        Chess[] backRow = {
            new Rook(-colour), new Knight(-colour), new Bishop(-colour), 
            new Queen(-colour), new King(-colour), 
            new Bishop(-colour), new Knight(-colour), new Rook(-colour)
        };
        System.arraycopy(backRow, 0, board[0], 0, BOARD_SIZE);

        // Place back row pieces for player
        Chess[] frontRow = {
            new Rook(colour), new Knight(colour), new Bishop(colour), 
            new Queen(colour), new King(colour), 
            new Bishop(colour), new Knight(colour), new Rook(colour)
        };
        System.arraycopy(frontRow, 0, board[7], 0, BOARD_SIZE);
    }

    public int[] getLastMove(boolean mine) {
        return mine ? myLastMove : opponentLastMove;
    }

    public void setLastMove(boolean mine, int[] lastMove) {
        if (mine) {
            myLastMove = lastMove;
        } else {
            opponentLastMove = lastMove;
        }
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

    /**
     * Handles the logic for making a move on the board.
     * 
     * @return true if the move was completed, false if promotion is needed
     */
    public boolean makeMove(int playerID, int selectedRow, int selectedCol, int row, int col) {
        // Get the piece being moved
        Chess movingPiece;
        if (playerID == player.getColour()) {
            movingPiece = gui.getFloatingPiece();
        } else {
            movingPiece = board[selectedRow][selectedCol];
            board[selectedRow][selectedCol] = null;
        }
        
        // Switch current player
        currentPlayer *= -1;
        
        // Handle capture and play appropriate sound
        if (board[row][col] == null) {
            SoundPlayer.playSound("/Move.wav");
        } else {
            SoundPlayer.playSound("/Capture.wav");
            if (board[row][col].colour == playerID * -1) {
                graveyard.add(board[row][col]);
            }
        }
        
        // Check for special moves
        if (board[row][col] != null && board[row][col].type == 'K') {
            // Checkmate condition
            board[row][col] = movingPiece;
            client.sendCheckMate();
        } else if (movingPiece.colour == colour && movingPiece.type == 'P' && (row == 0 || row == board.length - 1)) {
            // Pawn promotion
            gui.promotion(row, col, movingPiece.colour);
            return false;
        } else if (board[row][col] != null && movingPiece.type == 'K' && movingPiece.colour == board[row][col].colour) {
            // Castling move
            handleCastling(selectedRow, selectedCol, row, col, movingPiece);
        } else {
            // Standard move
            board[row][col] = movingPiece;
        }
        
        return true;
    }

    /**
     * Handles the special castling move between king and rook.
     */
    private void handleCastling(int kingRow, int kingCol, int rookRow, int rookCol, Chess king) {
        int newKingCol;
        if (kingCol < rookCol) {
            // Castling kingside
            newKingCol = kingCol + 2;
            board[kingRow][newKingCol] = king;
            board[rookRow][kingCol + 1] = board[rookRow][rookCol]; // Move rook
        } else {
            // Castling queenside
            newKingCol = kingCol - 2;
            board[kingRow][newKingCol] = king;
            board[rookRow][kingCol - 1] = board[rookRow][rookCol]; // Move rook
        }
        board[rookRow][rookCol] = null; // Remove the rook from its original position
    }

    /**
     * Starts the game with assigned player ID.
     */
    public void start(int playerID) {
        setPlayer(playerID);
        opponent = new ChessPlayer(-player.getColour(), -1);
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

    public void reset() {
        // To be implemented for game restart
    }
}
