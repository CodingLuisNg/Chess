public class ChessPlayer {
    private final int colour;
    private final int side;

    public ChessPlayer(int colour, int side) {
        this.colour = colour;
        this.side = side;
    }

    public int getColour() {
        return colour;
    }

    public int getSide() {
        return side;
    }
}
