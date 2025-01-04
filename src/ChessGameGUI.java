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

    public ChessGameGUI(Chess[][] board, ChessPlayer player) {
        this.board = board;
        loadImages();
        if (player.getColour() == 1) {
            backgroundImage = new ImageIcon("src/whiteBoard.jpg").getImage();
        } else {
            backgroundImage = new ImageIcon("src/blackBoard.jpg").getImage();
        }

        // Mouse listener for handling clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = (e.getY() - verticalMargin) / tileSize;
                int col = (e.getX() - horizontalMargin) / tileSize;

                if (!isValidTile(row, col)) {
                    return; // Ignore clicks outside the board
                }

                if (isValidTile(row, col) && board[row][col] != null) {
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
                    if (isValidTile(row, col) && floatingPiece.checkMove(new int[] {selectedRow, selectedCol, row, col}, board)) {
                        if (board[row][col] != null && board[row][col].type == 'K') {
                            System.out.println("Checkmate");
                        }
                        board[row][col] = floatingPiece;
                    } else {
                        // Return the piece to its original position if dropped out of bounds
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
            pieceImages.put(piece, new ImageIcon("src/pieces/" + piece + ".png").getImage());
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

    private boolean isValidTile(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private int getSquareSize() {
        return tileSize * BOARD_SIZE;
    }
}
