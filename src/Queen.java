public class Queen extends Chess{
    public Queen(int colour) {
        super('Q', colour);
    }

    @Override
    public boolean checkMove(int[] move, Chess[][] board) {
        return super.checkMove(move, board) && ((Math.abs(move[0] - move[2]) == Math.abs(move[1] - move[3])) || (move[0] == move[2] && move[1] != move[3]) || (move[0] != move[2] && move[1] == move[3]));
    }
}
