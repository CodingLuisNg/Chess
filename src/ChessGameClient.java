import javax.swing.*;
import java.io.*;
import java.net.*;

public class ChessGameClient {
    private static String SERVER_ADDRESS;
    private static int SERVER_PORT;
    private int playerID;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final ChessGame game;
    private Socket socket;
    private boolean connected = false;

    public ChessGameClient(ChessGame game) throws IOException {
        this.game = game;
        SERVER_ADDRESS = JOptionPane.showInputDialog(null, "Input the server IP address", "localhost");
        String portInput = JOptionPane.showInputDialog(null, "Input the server port", "2396");
        SERVER_PORT = Integer.parseInt(portInput);
        connect();
        new Thread(this::listenForMessages).start();
    }

    private void connect() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        connected = true;
        System.out.println("Connected to the server.");
    }

    private void disconnect() {
        connected = false;
        try {
            if (oos != null) oos.close();
            if (ois != null) ois.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForMessages() {
        try {
            while (connected) {
                ChessMessage message = (ChessMessage) ois.readObject();
                processMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            handleDisconnection();
        }
    }

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

    private void processMessage(ChessMessage message) {
        System.out.println("Message received: " + message.type() + ' ' + message.data().toString());

        switch (message.type()) {
            case ChessMessage.START -> {
                playerID = (int) message.data();
                System.out.println("Game started. You are " + (playerID == 1 ? "White" : "Black"));
                game.start(playerID);
            }
            case ChessMessage.MOVE -> {
                int[] move = (int[]) message.data();
                game.makeMove(message.playerID(), ChessGame.BOARD_SIZE - move[0] - 1, move[1],
                        ChessGame.BOARD_SIZE - move[2] - 1, move[3]);
                game.getGUI().repaint();
            }
            case ChessMessage.CHECKMATE -> game.getGUI().checkmate((int) message.data());
            case ChessMessage.QUIT -> handleOpponentDisconnection();
            case ChessMessage.PLACE -> {
                int[] move = (int[]) message.data();
                Chess piece;
                switch (move[1]) {
                    case 2 -> piece = new Bishop(message.playerID());
                    case 1 -> piece = new Knight(message.playerID());
                    case 3 -> piece = new Rook(message.playerID());
                    default -> piece = new Queen(message.playerID());
                }
                if (move[0] == 1) {
                    game.getBoard()[game.getBoard().length - move[2] - 1][move[3]] = piece;
                } else {
                    game.getBoard()[move[2]][move[3]] = null;
                }
                game.getGUI().repaint();
            }
        }
    }

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
                try {
                    game.reset();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void sendMove(int type, int[] move) {
        try {
            if (connected) {
                oos.writeObject(new ChessMessage(type, playerID, move));
            }
        } catch (IOException e) {
            handleDisconnection();
        }
    }

    public void sendCheckMate() {
        try {
            if (connected) {
                oos.writeObject(new ChessMessage(ChessMessage.CHECKMATE, 0, playerID));
            }
        } catch (IOException e) {
            handleDisconnection();
        }
    }
}