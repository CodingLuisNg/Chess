public abstract class Chess {
    protected char type;
    protected final int colour;

    public Chess(char type, int colour) {
        this.type = type;
        this.colour = colour;
    }

    public String getName() {
        if (colour == 1) {
            return "w" + type;
        } else {
            return "b" + type;
        }
    }

    public boolean checkMove(int[] move, Chess[][] board) {
         return isInBound(move, board) && (isEmptyTile(move, board) || isOpponentPiece(move, board)) && isPathClear(move, board);
    }

    private boolean isInBound(int[] move, Chess[][] board) {
        return move[2] < board.length && move[2] >= 0 && move[3] < board.length && move[3] >= 0;
    }

    protected boolean isEmptyTile(int[] move, Chess[][] board) {
        return board[move[2]][move[3]] == null;
    }

    protected boolean isOpponentPiece(int[] move, Chess[][] board) {
        return board[move[2]][move[3]].colour != colour;
    }

    private boolean isPathClear(int[] move, Chess[][] board) {
        int startRow = move[0];
        int startCol = move[1];
        int endRow = move[2];
        int endCol = move[3];

        // Check for horizontal moves
        if (startRow == endRow) {
            int colStep = Integer.compare(endCol, startCol); // Step is +1 for right, -1 for left
            for (int col = startCol + colStep; col != endCol; col += colStep) {
                if (board[startRow][col] != null) {
                    return false; // Obstacle found
                }
            }
        }
        // Check for vertical moves
        else if (startCol == endCol) {
            int rowStep = Integer.compare(endRow, startRow); // Step is +1 for down, -1 for up
            for (int row = startRow + rowStep; row != endRow; row += rowStep) {
                if (board[row][startCol] != null) {
                    return false; // Obstacle found
                }
            }
        }
        // Check for diagonal moves
        else if (Math.abs(endRow - startRow) == Math.abs(endCol - startCol)) {
            int rowStep = Integer.compare(endRow, startRow);
            int colStep = Integer.compare(endCol, startCol);
            int row = startRow + rowStep;
            int col = startCol + colStep;
            while (row != endRow && col != endCol) {
                if (board[row][col] != null) {
                    return false; // Obstacle found
                }
                row += rowStep;
                col += colStep;
            }
        }

        // Return true if no obstacles were found
        return true;
    }
}
