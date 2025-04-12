import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 * GUI class for the chess game, handling rendering and user interactions.
 */
public class ChessGameGUI extends JPanel {
    // Constants
    private static final int BOARD_SIZE = 8; // 8x8 chessboard
    
    // Board and piece dimensions
    private int tileSize; // Size of each square
    private int pieceSize;
    private int pieceMargin;
    private int horizontalMargin;
    private int verticalMargin;

    // Game state
    private final Map<String, Image> pieceImages = new HashMap<>();
    private final Chess[][] board;
    private int selectedRow = -1, selectedCol = -1;
    private boolean pieceSelected = false;
    private Chess floatingPiece = null; // The piece currently being dragged
    private int cursorX = 0, cursorY = 0; // Cursor position for floating piece
    
    // Visual elements
    private final Image backgroundImage;
    private Image preMoveImage;
    private Image lastMoveImage;
    
    // Game references
    private final int playerColour;
    private final ChessGame game;

    /**
     * Creates and initializes the chess game GUI.
     */
    public ChessGameGUI(ChessGame game) {
        this.game = game;
        this.playerColour = game.getPlayer().getColour();
        this.board = game.getBoard();
        
        // Load images and resources
        loadImages();
        backgroundImage = loadBoardImage();
        
        setupMouseListeners();
        setupGameWindow();
    }

    /**
     * Loads the appropriate board image based on player color.
     */
    private Image loadBoardImage() {
        String imagePath = (playerColour == 1) ? "/whiteBoard.png" : "/blackBoard.png";
        return new ImageIcon(getClass().getResource(imagePath)).getImage();
    }

    /**
     * Loads all piece images and UI elements.
     */
    private void loadImages() {
        // Load chess piece images
        String[] pieces = {"wP", "wR", "wN", "wB", "wQ", "wK", "bP", "bR", "bN", "bB", "bQ", "bK"};
        for (String piece : pieces) {
            pieceImages.put(piece, new ImageIcon(getClass().getResource("/pieces/" + piece + ".png")).getImage());
        }
        
        // Load UI element images
        preMoveImage = new ImageIcon(getClass().getResource("/preMove.png")).getImage();
        lastMoveImage = new ImageIcon(getClass().getResource("/lastMove.png")).getImage();
    }

    /**
     * Sets up mouse listeners for piece movement.
     */
    private void setupMouseListeners() {
        // Mouse listener for handling clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
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
    }

    /**
     * Handles mouse press events for picking up pieces.
     */
    private void handleMousePressed(MouseEvent e) {
        int row = (e.getY() - verticalMargin) / tileSize;
        int col = (e.getX() - horizontalMargin) / tileSize;

        // Check if valid selection
        if (!isValidTile(row, col) || board[row][col] == null || 
            game.currentPlayer != playerColour || 
            board[row][col].colour != playerColour) {
            return; // Ignore invalid clicks
        }

        // Select piece to move
        SoundPlayer.playSound("/Select.wav");
        selectedRow = row;
        selectedCol = col;
        floatingPiece = board[row][col];
        board[row][col] = null; // Temporarily remove piece from board
        pieceSelected = true;
    }

    /**
     * Handles mouse release events for placing pieces.
     */
    private void handleMouseReleased(MouseEvent e) {
        if (!pieceSelected) return;
        
        int row = (e.getY() - verticalMargin) / tileSize;
        int col = (e.getX() - horizontalMargin) / tileSize;
        
        // Try to place the piece on the new tile
        if (isValidTile(row, col) && 
            floatingPiece.checkMove(new int[] {selectedRow, selectedCol, row, col}, board) && 
            (selectedRow != row || selectedCol != col)) {
            
            // Make move and send to opponent
            if (game.makeMove(playerColour, selectedRow, selectedCol, row, col)) {
                game.getClient().sendMove(ChessMessage.MOVE, new int[] {selectedRow, selectedCol, row, col});
            }
        } else {
            // Invalid move - return piece to original position
            board[selectedRow][selectedCol] = floatingPiece;
        }
        
        // Reset temporary variables
        floatingPiece = null;
        pieceSelected = false;
        selectedRow = -1;
        selectedCol = -1;
        repaint();
    }

    /**
     * Sets up the main game window.
     */
    private void setupGameWindow() {
        JFrame frame = new JFrame("Chess Game");
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.add(new Graveyard(), BorderLayout.EAST);
        setPreferredSize(new Dimension(1280, 720));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
        
        // Draw pieces
        drawPieces(g);

        // Draw floating piece if dragging
        if (floatingPiece != null) {
            Image img = pieceImages.get(floatingPiece.getName());
            if (img != null) {
                g.drawImage(img, cursorX - tileSize / 2, cursorY - tileSize / 2, tileSize, tileSize, this);
            }
        }
    }

    /**
     * Draws all chess pieces on the board.
     */
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

    /**
     * Adjusts dimensions based on current window size.
     */
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

    /**
     * Calculates the x-coordinate for a piece.
     */
    private int getTileX(int col) {
        return col * tileSize + pieceMargin + horizontalMargin;
    }

    /**
     * Calculates the y-coordinate for a piece.
     */
    private int getTileY(int row) {
        return row * tileSize + pieceMargin + verticalMargin;
    }

    /**
     * Checks if the given row and column are within the board bounds.
     */
    public boolean isValidTile(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /**
     * Gets the total size of the board in pixels.
     */
    private int getSquareSize() {
        return tileSize * BOARD_SIZE;
    }

    /**
     * Handles pawn promotion.
     */
    public void promotion(int row, int col, int playerColour) {
        // Get promotion choice from player
        int choice = showPromotionDialog(playerColour);
        if (choice < 0) choice = 0; // Default to Queen
        
        // Create the new piece based on choice
        int pieceTypeCode = 4; // Default queen
        Chess newPiece;
        
        switch (choice) {
            case 0: // Queen
                newPiece = new Queen(playerColour);
                pieceTypeCode = 4;
                break;
            case 1: // Rook
                newPiece = new Rook(playerColour);
                pieceTypeCode = 3;
                break;
            case 2: // Knight
                newPiece = new Knight(playerColour);
                pieceTypeCode = 1;
                break;
            case 3: // Bishop
                newPiece = new Bishop(playerColour);
                pieceTypeCode = 2;
                break;
            default:
                newPiece = new Queen(playerColour);
                pieceTypeCode = 4;
        }
        
        // Place the new piece on the board
        board[row][col] = newPiece;
        
        // Send move information to opponent
        game.getClient().sendMove(ChessMessage.MOVE, new int[] {selectedRow, selectedCol, row, col});
        game.getClient().sendMove(ChessMessage.PLACE, new int[] {1, pieceTypeCode, row, col});
    }

    /**
     * Shows a dialog for pawn promotion choices.
     */
    private int showPromotionDialog(int playerColour) {
        String prefix = (playerColour == 1) ? "w" : "b";
        
        // Create icons for each promotion option
        ImageIcon[] icons = {
                new ImageIcon(pieceImages.get(prefix + "Q")),
                new ImageIcon(pieceImages.get(prefix + "R")),
                new ImageIcon(pieceImages.get(prefix + "N")),
                new ImageIcon(pieceImages.get(prefix + "B"))
        };

        // Display dialog for user choice
        return JOptionPane.showOptionDialog(
                null,
                "Choose a piece for promotion:",
                "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                icons,
                icons[0]
        );
    }

    /**
     * Displays a checkmate message.
     */
    public void checkmate(int playerID) {
        String colour = (playerID == 1) ? "White" : "Black";
        floatingPiece = null;
        repaint();
        JOptionPane.showMessageDialog(this, 
                colour + " Won The Game", 
                "CHECK MATE", 
                JOptionPane.INFORMATION_MESSAGE, 
                null);
    }

    public Chess getFloatingPiece() {
        return floatingPiece;
    }

    /**
     * Panel for displaying captured pieces.
     */
    private class Graveyard extends JPanel {
        // To be implemented for displaying captured pieces
    }
}
