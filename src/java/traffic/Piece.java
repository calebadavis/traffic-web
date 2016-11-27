package traffic;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a rectangular puzzle piece on the 'Traffic Jam' board
 * @see Board
 */
public class Piece {
    
    /**
     * Construct a new Piece instance
     * 
     * @param type the attributes of the Piece (height, width)
     * @param b the Board which holds this Piece
     * @param leftPos the Board column which is the Piece's leftmost edge
     * @param topPos the Board row which is the Piece's topmost edge
     */
    public Piece(PieceType type, Board b, int leftPos, int topPos) {
        
        // Store the type and position
        this.type = type;
        this.leftPos = leftPos;
        this.topPos = topPos;
        
        // Initialize the list of possible moves for this piece
        moves = new ArrayList<Board.MoveDir>();
        
        // The id of the Piece needs to match the Piece's index in the Board's
        // list of Pieces
        List<Piece> pieces = b.getPieces();
        pID = pieces.size();
        pieces.add(this);
        
        // Mark the spots occupied by this Piece on the Board array
        b._markBoard(this, true);
    }

    /**
     * Retrieve type information for this Piece
     * @return a PieceType instance describing this Piece's attributes
     */
    public PieceType getType() {
        return type;
    }

    /**
     * Retrieve the leftmost column occupied by this Piece
     * @return the leftmost column occupied by this Piece
     */
    public int getLeftPos() {
        return leftPos;
    }

    /**
     * Set a new left position (leftmost column occupied)<br/>
     * Note this doesn't update the Board layout array.
     * 
     * @param leftPos the new leftmost column for the Piece to occupy 
     * @see Board#move(Piece, traffic.Board.MoveDir)
     */
    public void setLeftPos(int leftPos) {
        this.leftPos = leftPos;
    }

    /**
     * Retrieve the topmost row occupied by this Piece 
     * @return the topmost row occupied by this Piece
     */
    public int getTopPos() {
        return topPos;
    }

    /**
     * Set a new top position (topmost row occupied)<br/>
     * Note this doesn't update the Board layout array.
     * 
     * @param topPos the new topmost row for the Piece to occupy
     * @see Board#move(Piece, traffic.Board.MoveDir)
     */
    public void setTopPos(int topPos) {
        this.topPos = topPos;
    }

    /**
     * Retrieve the Piece's unique ID
     * @return the Piece's unique ID
     */
    public int getpID() {
        return pID;
    }

    /**
     * Retrieve the set of moves available to the Piece
     * @return the set of moves available to the Piece
     * @see Board#storeMoves(Piece)
     */
    public List<Board.MoveDir> getMoves() {
        return moves;
    }

    // The attributes of the Piece
    private PieceType type;

    // Position
    private int leftPos;
    private int topPos;
    
    // Unique ID
    private int pID;

    // Available moves
    private List<Board.MoveDir> moves = null; 
}