import java.io.*;
import java.net.*;
import java.util.*;

public class ChessGameServer {
    private static final int PORT = 12345;
    private final List<PlayerHandler> players = new ArrayList<>();
    private final int firstPlayerID = (new Random().nextBoolean()) ? 1 : -1; // Randomly assign 1 or -1 to the first player

    public static void main(String[] args) {
        new ChessGameServer().start();
    }

    public void start() {
        System.out.println("Chess Game Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                int playerID = (players.isEmpty()) ? firstPlayerID : -firstPlayerID;
                PlayerHandler player = new PlayerHandler(socket, playerID);
                players.add(player);
                System.out.println("New player " + playerID);
                new Thread(player).start();

                if (players.size() == 2) {
                    startGame();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame() {
        System.out.println("Two players connected. Starting the game...");
        for (PlayerHandler player : players) {
            ChessMessage startMessage = new ChessMessage(ChessMessage.START, 0, player.playerID);
            player.sendMessage(startMessage);
        }
    }

    private class PlayerHandler implements Runnable {
        private final Socket socket;
        private final int playerID;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;

        public PlayerHandler(Socket socket, int playerID) {
            this.socket = socket;
            this.playerID = playerID;
        }

        @Override
        public void run() {
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    ChessMessage message = (ChessMessage) ois.readObject();
                    handleClientMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void handleClientMessage(ChessMessage message) {
            switch (message.type()) {
                case ChessMessage.MOVE -> forwardMove(message);
                case ChessMessage.CHECKMATE -> endGame(message);
            }
            System.out.println("Client " + playerID + " sent a message: " + Arrays.toString((int[]) message.data()));
        }

        private void forwardMove(ChessMessage message) {
            for (PlayerHandler player : players) {
                if (player.playerID != message.playerID()) {
                    player.sendMessage(message);
                    System.out.println("Client " + player.playerID + " received a message: " + Arrays.toString((int[]) message.data()));
                }
            }
        }

        private void endGame(ChessMessage message) {
            for (PlayerHandler player : players) {
                player.sendMessage(message);
            }
            System.out.println("Game ended.");
        }

        private void sendMessage(ChessMessage message) {
            try {
                oos.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
