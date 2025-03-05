import java.io.*;
import java.net.*;
import java.util.*;

public class ChessGameServer {
    private static final int PORT = 12345;
    private final List<PlayerHandler> players = new ArrayList<>();
    private int firstPlayerID = (new Random().nextBoolean()) ? 1 : -1;
    private boolean gameInProgress = false;

    public static void main(String[] args) {
        new ChessGameServer().start();
    }

    public void start() {
        System.out.println("Chess Game Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                handleNewConnection(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void handleNewConnection(Socket socket) {
        // Remove disconnected players
        players.removeIf(player -> !player.isConnected());

        if (players.size() < 2) {
            int playerID = players.isEmpty() ? firstPlayerID : -firstPlayerID;
            PlayerHandler player = new PlayerHandler(socket, playerID);
            players.add(player);
            System.out.println("New player " + playerID);
            new Thread(player).start();

            if (players.size() == 2) {
                startGame();
            }
        } else {
            try {
                socket.close();
                System.out.println("Rejected connection: game is full");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void startGame() {
        if (!gameInProgress && players.size() == 2) {
            System.out.println("Two players connected. Starting the game...");
            gameInProgress = true;
            for (PlayerHandler player : players) {
                ChessMessage startMessage = new ChessMessage(ChessMessage.START, 0, player.playerID);
                player.sendMessage(startMessage);
            }
        }
    }

    private synchronized void handlePlayerDisconnection(PlayerHandler disconnectedPlayer) {
        players.remove(disconnectedPlayer);
        gameInProgress = false;
        firstPlayerID = (new Random().nextBoolean()) ? 1 : -1; // Randomize first player for next game

        // Notify remaining player about opponent disconnection
        if (!players.isEmpty()) {
            ChessMessage disconnectMessage = new ChessMessage(ChessMessage.QUIT, 0, disconnectedPlayer.playerID);
            players.get(0).sendMessage(disconnectMessage);
            System.out.println("Player " + disconnectedPlayer.playerID + " disconnected. Waiting for new player...");
        } else {
            System.out.println("All players disconnected. Waiting for new players...");
        }
    }

    private class PlayerHandler implements Runnable {
        private final Socket socket;
        private final int playerID;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        private boolean connected = true;

        public PlayerHandler(Socket socket, int playerID) {
            this.socket = socket;
            this.playerID = playerID;
        }

        public boolean isConnected() {
            return connected && !socket.isClosed();
        }

        @Override
        public void run() {
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                while (connected) {
                    ChessMessage message = (ChessMessage) ois.readObject();
                    handleClientMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Player " + playerID + " disconnected");
                connected = false;
                handlePlayerDisconnection(this);
            } finally {
                cleanup();
            }
        }

        private void cleanup() {
            try {
                if (oos != null) oos.close();
                if (ois != null) ois.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
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
                if (player.playerID != message.playerID() && player.isConnected()) {
                    player.sendMessage(message);
                    System.out.println("Client " + player.playerID + " received a message: " + Arrays.toString((int[]) message.data()));
                }
            }
        }

        private void endGame(ChessMessage message) {
            gameInProgress = false;
            for (PlayerHandler player : players) {
                if (player.isConnected()) {
                    player.sendMessage(message);
                }
            }
            System.out.println("Game ended.");
        }

        private void sendMessage(ChessMessage message) {
            try {
                if (isConnected()) {
                    oos.writeObject(message);
                }
            } catch (IOException e) {
                connected = false;
                handlePlayerDisconnection(this);
            }
        }
    }
}