import java.io.*;
import java.net.*;

public class ChessGameClient {
    private static final String SERVER_ADDRESS = "192.168.0.181";
    private static final int SERVER_PORT = 12345;
    private int playerID;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final ChessGame game;

    public ChessGameClient(ChessGame game) throws IOException {
        this.game = game;
        connect();
        new Thread(this::listenForMessages).start();
    }

    private void connect() throws IOException {
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        System.out.println("Connected to the server.");
    }

    private void listenForMessages() {
        try {
            while (true) {
                ChessMessage message = (ChessMessage) ois.readObject();
                processMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(ChessMessage message) {
        switch (message.type()) {
            case ChessMessage.START -> {
                playerID = (int) message.data();
                System.out.println("Game started. You are " + (playerID == 1 ? "White" : "Black"));
                game.start(playerID);
            }
            case ChessMessage.MOVE -> {
                int[] move = (int[]) message.data();
                System.out.println("Player " + message.playerID() + " Move " + (ChessGame.BOARD_SIZE - move[0] - 1) + " " + move[1] + " " + (ChessGame.BOARD_SIZE - move[2] - 1) + " " + move[3]);
                game.makeMove(message.playerID(), ChessGame.BOARD_SIZE - move[0] - 1, move[1], ChessGame.BOARD_SIZE - move[2] - 1, move[3]);
                game.getGUI().repaint();
            }
            case ChessMessage.CHECKMATE -> {
                game.getGUI().checkmate((int) message.data());
                // Handle end game logic
            }
        }
    }

    public void sendMove(int[] move) {
        try {
            oos.writeObject(new ChessMessage(ChessMessage.MOVE, playerID, move));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCheckMate() {
        try {
            oos.writeObject(new ChessMessage(ChessMessage.CHECKMATE, 0, playerID));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
