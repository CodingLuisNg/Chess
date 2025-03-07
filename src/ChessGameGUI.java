import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Map;

public class ChessGameGUI extends JPanel {
    private static final int BOARD_SIZE = 8; // 8x8 chessboard
    private int tileSize; // Size of each square
    private int pieceSize;
    private int pieceMargin;
    private int horizontalMargin;
    private int verticalMargin;

    private final Map<String, Image> pieceImages = new HashMap<>();
    private final Chess[][] board;
    private int selectedRow = -1, selectedCol = -1;
    private boolean pieceSelected = false;
    private Chess floatingPiece = null; // The piece currently being dragged
    private int cursorX = 0, cursorY = 0; // Cursor position for floating piece
    private final Image backgroundImage;
    private final int playerColour;
    private final ChessGame game;

    public ChessGameGUI(ChessGame game) {
        this.game = game;
        playerColour = game.getPlayer().getColour();
        this.board = game.getBoard();
        loadImages();
        if (playerColour == 1) {
            backgroundImage = new ImageIcon(getClass().getResource("/whiteBoard.png")).getImage();
        } else {
            backgroundImage = new ImageIcon(getClass().getResource("/blackBoard.png")).getImage();
        }

        // Mouse listener for handling clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = (e.getY() - verticalMargin) / tileSize;
                int col = (e.getX() - horizontalMargin) / tileSize;

                if (!isValidTile(row, col) || board[row][col] == null || game.currentPlayer != playerColour || board[row][col].colour != playerColour) {
                    return; // Ignore clicks outside the board
                }

                if (isValidTile(row, col) && board[row][col] != null) {
                    SoundPlayer.playSound("/Select.wav");
                    // Select a piece to move
                    selectedRow = row;
                    selectedCol = col;
                    floatingPiece = board[row][col];
                    board[row][col] = null; // Temporarily remove the piece from the board
                    pieceSelected = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (pieceSelected) {
                    int row = (e.getY() - verticalMargin) / tileSize;
                    int col = (e.getX() - horizontalMargin) / tileSize;
                    // Place the piece on the new tile if within bounds
                    if (isValidTile(row, col) && floatingPiece.checkMove(new int[] {selectedRow, selectedCol, row, col}, board) && (selectedRow != row || selectedCol != col)) {
                        if (game.makeMove(playerColour, selectedRow, selectedCol, row, col)) {
                            game.getClient().sendMove(ChessMessage.MOVE, new int[] {selectedRow, selectedCol, row, col});
                        }
                    } else {
                        board[selectedRow][selectedCol] = floatingPiece;
                    }
                    // Reset temporary variables
                    floatingPiece = null;
                    pieceSelected = false;
                    selectedRow = -1;
                    selectedCol = -1;

                    repaint();
                }
            }
        });

        // Mouse motion listener for dragging
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (pieceSelected) {
                    cursorX = e.getX();
                    cursorY = e.getY();
                    repaint();
                }
            }
        });

        JFrame frame = new JFrame("Chess Game");
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1280, 720));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void loadImages() {
        String[] pieces = {"wP", "wR", "wN", "wB", "wQ", "wK", "bP", "bR", "bN", "bB", "bQ", "bK"};
        for (String piece : pieces) {
            pieceImages.put(piece, new ImageIcon(getClass().getResource("/pieces/" + piece + ".png")).getImage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        adjustDimensions();

        // Fill background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw board
        g.drawImage(backgroundImage, horizontalMargin, verticalMargin, getSquareSize(), getSquareSize(), this);
        drawPieces(g);

        // Draw the floating piece if one is being dragged
        if (floatingPiece != null) {
            Image img = pieceImages.get(floatingPiece.getName());
            if (img != null) {
                g.drawImage(img, cursorX - tileSize / 2, cursorY - tileSize / 2, tileSize, tileSize, this);
            }
        }
    }


    private void drawPieces(Graphics g) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Chess piece = board[row][col];
                if (piece != null) {
                    Image img = pieceImages.get(piece.getName());
                    if (img != null) {
                        g.drawImage(img, getTileX(col), getTileY(row), pieceSize, pieceSize, this);
                    }
                }
            }
        }
    }


    private void adjustDimensions() {
        int width = getWidth();
        int height = getHeight();
        horizontalMargin = 0;
        verticalMargin = 0;

        tileSize = Math.min(width, height) / BOARD_SIZE;
        if (width > height) {
            horizontalMargin = (width - height) / 2;
        } else {
            verticalMargin = (height - width) / 2;
        }

        pieceSize = tileSize * 8 / 10;
        pieceMargin = (tileSize - pieceSize) / 2;
    }


    private int getTileX(int col) {
        return col * tileSize + pieceMargin + horizontalMargin;
    }

    private int getTileY(int row) {
        return row * tileSize + pieceMargin + verticalMargin;
    }

    public boolean isValidTile(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private int getSquareSize() {
        return tileSize * BOARD_SIZE;
    }

    public void promotion(int row, int col, int playerColour) {
        // Available promotion options
        String[] options = {"Queen", "Rook", "Knight", "Bishop"};
        ImageIcon[] icons = {
                new ImageIcon(pieceImages.get((playerColour == 1 ? "wQ" : "bQ"))),
                new ImageIcon(pieceImages.get((playerColour == 1 ? "wR" : "bR"))),
                new ImageIcon(pieceImages.get((playerColour == 1 ? "wN" : "bN"))),
                new ImageIcon(pieceImages.get((playerColour == 1 ? "wB" : "bB")))
        };

        // Display a dialog for the user to choose a piece
        int choice = JOptionPane.showOptionDialog(
                null,
                "Choose a piece for promotion:",
                "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                icons,
                icons[0]
        );

        // Handle invalid choice or close dialog
        if (choice < 0) {
            choice = 0; // Default to Queen
        }
        int piece = 4;
        // Replace pawn with the chosen piece
        switch (options[choice]) {
            case "Queen" -> board[row][col] = new Queen(playerColour);
            case "Rook" -> {
                board[row][col] = new Rook(playerColour);
                piece = 3;
            }
            case "Knight" -> {
                board[row][col] = new Knight(playerColour);
                piece = 1;
            }
            case "Bishop" -> {
                board[row][col] = new Bishop(playerColour);
                piece = 2;
            }
        }
        game.getClient().sendMove(ChessMessage.MOVE, new int[] {selectedRow, selectedCol, row, col});
        game.getClient().sendMove(ChessMessage.PLACE, new int[] {1, piece, row, col});
    }

    public void checkmate(int playerID) {
        String colour;
        if (playerID == 1) {
            colour = "White";
        } else {
            colour = "Black";
        }
        floatingPiece = null;
        repaint();
        JOptionPane.showMessageDialog(this, colour + " Won The Game", "CHECK MATE", JOptionPane.INFORMATION_MESSAGE, null);
    }

    public Chess getFloatingPiece() {
        return floatingPiece;
    }
}
