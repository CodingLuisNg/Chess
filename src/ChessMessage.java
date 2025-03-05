import java.io.Serializable;

public record ChessMessage(int type, int playerID, Object data) implements Serializable {
    public static final int START = 0;  // Start game and send player color
    public static final int MOVE = 1;  // Player move
    public static final int CHECKMATE = 2;  // End game
    public static final int QUIT = 3;
}
