package traffic;

/**
 * A node in the tree of all possible board layouts
 * 
 * The tree builds top-down, but children maintain references to parents (so
 * that once a solution is found, it's easy to walk up the tree back to the
 * starting position).
 * 
 * Each node contains a board layout, a reference to a parent node, a piece id,
 * and a {@link Board.MoveDir}
 * 
 * Notice that the layout is state of the board BEFORE moving the piece
 * 
 * @see Board#solve(int[])
 */
public class MoveNode {

    /**
     * Construct a new MoveNode instance
     * 
     * @param parent    the parent MoveNode (null if this is the root node)
     * @param p         the Piece to move (null if this is the root node)
     * @param dir       the direction to move the piece (NONE if the root node)
     * @param pieces    an array of piece locations (use {@link Board#pieceLocs()})
     */
    public MoveNode(MoveNode parent, Piece p, Board.MoveDir dir, int pieces[]) {
        this.parent = parent;
        this.depth = (parent == null) ? 0 : parent.depth + 1;
        this.dir = dir;
        this.pID = (p == null) ? -1 : p.getpID();
        this.pieces = pieces;
    }

    /**
     * Get the direction this MoveNode indicates the piece should move
     * @return  a {@link Board.MoveDir} instance
     */
    public Board.MoveDir getDir() {
        return dir;
    }
    
    /**
     * Get the id of the Piece this MoveNode indicates should move
     * @return  the id of the Piece to move
     */
    public int getpID() {
        return pID;
    }

    /**
     * Retrieve the parent MoveNode which resulted in this board layout
     * @return  the MoveNode's parent node
     */
    public MoveNode getParent() {
        return parent;
    }

    /**
     * Set this MoveNode's parent node 
     * @param parent    the MoveNode that resulted in this board's layout
     */
    public void setParent(MoveNode parent) {
        this.parent = parent;
    }

    /**
     * Get the array of piece locations representing the board layout prior
     * to the move indicated by this MoveNode
     * 
     * @return  the array of piece locations
     */
    public int[] getPieces() {
        return pieces;
    }

    /**
     * Return the depth in the tree of this MoveNode (how many moves from
     * the starting position)
     * 
     * @return  the depth of this node in the tree
     */
    public int getDepth() {
        return depth;
    }

    // The direction of an available move from this layout
    private Board.MoveDir dir;
    
    // The id of the Piece which could move
    private int pID;
    
    // The parent move of this move
    private MoveNode parent;
    
    // The layout of the board prior to this move
    private int pieces[];
    
    // How many moves there are between this layout and the starting point
    private int depth;
}
