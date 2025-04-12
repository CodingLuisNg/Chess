import javax.swing.*;
import java.io.*;
import java.net.*;

/**
 * Handles network communication between chess clients.
 */
public class ChessGameClient {
    private final String serverAddress;
    private final int serverPort;
    private int playerID;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private final ChessGame game;
    private Socket socket;
    private boolean connected = false;

    /**
     * Creates a client connection to the chess server.
     */
    public ChessGameClient(ChessGame game, String serverAddress, int serverPort) throws IOException {
        this.game = game;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        connect();
        startMessageListener();
    }

    /**
     * Establishes connection to the server.
     */
    private void connect() throws IOException {
        try {
            socket = new Socket(serverAddress, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            connected = true;
            System.out.println("Connected to the server.");
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Starts a background thread to listen for incoming messages.
     */
    private void startMessageListener() {
        new Thread(this::listenForMessages).start();
    }

    /**
     * Closes all connections safely.
     */
    private void disconnect() {
        connected = false;
        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    /**
     * Background thread that continuously listens for messages.
     */
    private void listenForMessages() {
        try {
            while (connected) {
                ChessMessage message = (ChessMessage) inputStream.readObject();
                processMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {  // Only handle as error if we didn't intentionally disconnect
                System.err.println("Error in message listener: " + e.getMessage());
                handleDisconnection();
            }
        }
    }

    /**
     * Handles unexpected disconnection from the server.
     */
    private void handleDisconnection() {
        disconnect();
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "Lost connection to the server. Please restart the game.",
                    "Connection Lost",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        });
    }

    /**
     * Processes incoming messages and updates the game state.
     */
    private void processMessage(ChessMessage message) {
        System.out.println("Message received: " + message.type() + " from player " + message.playerID());

        switch (message.type()) {
            case ChessMessage.START -> handleStartMessage(message);
            case ChessMessage.MOVE -> handleMoveMessage(message);
            case ChessMessage.CHECKMATE -> handleCheckmateMessage(message);
            case ChessMessage.QUIT -> handleOpponentDisconnection();
            case ChessMessage.PLACE -> handlePlacePieceMessage(message);
        }
    }

    /**
     * Handles the START message, which assigns player ID.
     */
    private void handleStartMessage(ChessMessage message) {
        playerID = (int) message.data();
        System.out.println("Game started. You are " + (playerID == 1 ? "White" : "Black"));
        game.start(playerID);
    }

    /**
     * Handles a MOVE message from the opponent.
     */
    private void handleMoveMessage(ChessMessage message) {
        int[] move = (int[]) message.data();
        
        // Invert board coordinates for opponent's move
        game.makeMove(
            message.playerID(),
            ChessGame.BOARD_SIZE - move[0] - 1,
            move[1],
            ChessGame.BOARD_SIZE - move[2] - 1,
            move[3]
        );
        game.getGUI().repaint();
    }

    /**
     * Handles a CHECKMATE message.
     */
    private void handleCheckmateMessage(ChessMessage message) {
        game.getGUI().checkmate((int) message.data());
    }

    /**
     * Handles placing a piece on the board (for promotions).
     */
    private void handlePlacePieceMessage(ChessMessage message) {
        int[] moveData = (int[]) message.data();
        int pieceType = moveData[1];
        int row = moveData[2];
        int col = moveData[3];
        
        // Create appropriate piece based on the type code
        Chess piece = createPieceFromCode(pieceType, message.playerID());
        
        // Check if placing (1) or removing (0) a piece
        if (moveData[0] == 1) {
            game.getBoard()[ChessGame.BOARD_SIZE - row - 1][col] = piece;
        } else {
            game.getBoard()[row][col] = null;
        }
        
        game.getGUI().repaint();
    }

    /**
     * Creates a chess piece based on type code.
     */
    private Chess createPieceFromCode(int pieceCode, int playerColor) {
        return switch (pieceCode) {
            case 2 -> new Bishop(playerColor);
            case 1 -> new Knight(playerColor);
            case 3 -> new Rook(playerColor);
            default -> new Queen(playerColor);
        };
    }

    /**
     * Handles the opponent disconnecting from the game.
     */
    private void handleOpponentDisconnection() {
        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showOptionDialog(null,
                    "Your opponent has disconnected. Would you like to wait for a new opponent?",
                    "Opponent Disconnected",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Wait for new opponent", "Exit game"},
                    "Wait for new opponent");

            if (option == JOptionPane.NO_OPTION) {
                disconnect();
                System.exit(0);
            } else {
                // Reset the game board and wait for new opponent
                game.reset();
            }
        });
    }

    /**
     * Sends a move to the opponent.
     */
    public void sendMove(int type, int[] move) {
        try {
            if (connected) {
                outputStream.writeObject(new ChessMessage(type, playerID, move));
            }
        } catch (IOException e) {
            System.err.println("Error sending move: " + e.getMessage());
            handleDisconnection();
        }
    }

    /**
     * Sends a checkmate notification.
     */
    public void sendCheckMate() {
        try {
            if (connected) {
                outputStream.writeObject(new ChessMessage(ChessMessage.CHECKMATE, 0, playerID));
            }
        } catch (IOException e) {
            System.err.println("Error sending checkmate: " + e.getMessage());
            handleDisconnection();
        }
    }
}
