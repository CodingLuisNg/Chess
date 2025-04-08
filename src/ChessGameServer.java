import java.io.*;
import java.net.*;
import java.util.*;

public class ChessGameServer {
    private int port;
    private final List<PlayerHandler> players = new ArrayList<>();
    private int firstPlayerID = (new Random().nextBoolean()) ? 1 : -1;
    private boolean gameInProgress = false;

    // Start the server and listen for incoming connections
    public void start(int port) throws IOException {
        this.port = port;
        System.out.println("Chess Game Server started on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                handleNewConnection(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handle new player connections
    private synchronized void handleNewConnection(Socket socket) {
        // Remove disconnected players
        players.removeIf(player -> !player.isConnected());

        if (players.size() < 2) {
            int playerID = players.isEmpty() ? firstPlayerID : -firstPlayerID;
            PlayerHandler player = new PlayerHandler(socket, playerID);
            players.add(player);
            System.out.println("New player connected with ID: " + playerID);
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

    // Start the game when two players are connected
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

    // Handle player disconnection
    private synchronized void handlePlayerDisconnection(PlayerHandler disconnectedPlayer) {
        players.remove(disconnectedPlayer);
        gameInProgress = false;
        firstPlayerID = (new Random().nextBoolean()) ? 1 : -1; // Randomize first player for next game

        // Notify remaining player about opponent disconnection
        if (!players.isEmpty()) {
            ChessMessage disconnectMessage = new ChessMessage(ChessMessage.QUIT, 0, disconnectedPlayer.playerID);
            players.getFirst().sendMessage(disconnectMessage);
            System.out.println("Player " + disconnectedPlayer.playerID + " disconnected. Waiting for new player...");
        } else {
            System.out.println("All players disconnected. Waiting for new players...");
        }
    }

    // Inner class to handle player communication
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
                System.out.println("Player " + playerID + " disconnected unexpectedly");
                connected = false;
                handlePlayerDisconnection(this);
            } finally {
                cleanup();
            }
        }

        // Clean up resources
        private void cleanup() {
            try {
                if (oos != null) oos.close();
                if (ois != null) ois.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Handle messages from the client
        private void handleClientMessage(ChessMessage message) {
            switch (message.type()) {
                case ChessMessage.MOVE, ChessMessage.PLACE -> forwardMove(message);
                case ChessMessage.CHECKMATE -> endGame(message);
            }
            System.out.println("Received message from player " + message.playerID() + ": " + message.type() + " " + Arrays.toString((int[]) message.data()));
        }

        // Forward move messages to the other player
        private void forwardMove(ChessMessage message) {
            for (PlayerHandler player : players) {
                if (player.playerID != message.playerID() && player.isConnected()) {
                    player.sendMessage(message);
                    System.out.println("Forwarded message to player " + player.playerID + ": " + message.type() + " " + Arrays.toString((int[]) message.data()));
                }
            }
        }

        // End the game and notify players
        private void endGame(ChessMessage message) {
            gameInProgress = false;
            for (PlayerHandler player : players) {
                if (player.isConnected()) {
                    player.sendMessage(message);
                }
            }
            System.out.println("Game ended with message: " + message.type() + " " + Arrays.toString((int[]) message.data()));
        }

        // Send a message to the client
        private void sendMessage(ChessMessage message) {
            try {
                if (isConnected()) {
                    oos.writeObject(message);
                    System.out.println("Sent message to player " + playerID + ": " + message.type() + " " + Arrays.toString((int[]) message.data()));
                }
            } catch (IOException e) {
                connected = false;
                handlePlayerDisconnection(this);
            }
        }
    }
}