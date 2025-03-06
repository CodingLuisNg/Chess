import java.io.Serializable;

public record ChessMessage(int type, int playerID, Object data) implements Serializable {
    public static final int START = 0;  // Start game and send player color
    public static final int MOVE = 1;  // Player move
    public static final int CHECKMATE = 2;  // End game
    public static final int QUIT = 3;
    public static final int PLACE = 4; //send a message containing PlayerID and the pieces that it places (1) / removes (-1): data: int[4]: [place/remove, piece, row, col]
    //piece type: 0: pawn, 1: knight, 2: bishop, 3: rook, 4: queen, 5: king
}
