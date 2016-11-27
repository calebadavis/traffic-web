package traffic;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Core logic:
 * 
 * An implementation of the 'Traffic Jam' sliding blocks game (aka 'Klotski').
 * 
 * The goal is, on a rectangular board mostly filled with rectangular pieces,
 * to slide the pieces (up, down, left, or right) from some initial position to
 * a final solved position.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Klotski">Klotski</a>
 */
public class Board {

    // Default board dimensions
	public static int DEFAULT_HEIGHT=5;
	public static int DEFAULT_WIDTH=4;
	
	/**
	 * An enumeration of the possible move directions (left, right, up, down, 
	 * and none)
	 */
	public enum MoveDir {
	    LEFT,
	    RIGHT,
	    UP,
	    DOWN,
	    NONE
	}
	
	// Height and width of the board
    private int height;     
    private int width;
    
    // Array of piece locations on the board (by piece id)
    private int aBoard[][] = null;
    
    // The set of pieces in play
    private ArrayList<Piece> pieces;
    
    // The set of piece types (ORDER MATTERS!!!)
    private ArrayList<PieceType> types;
    
    // The final locations of the various types of pieces - the point of the
    // puzzle is to arrange the pieces in this configuration
    private int solvedBoard[] = null;
    
    /**
     * Construct a new Board instance, reading configuration from a file
     * @param cfgFName the configuration file for the new Board
     */
    public Board(String cfgFName) {
        
        // Create an (incomplete) board object
        this();
        
        // Read the file
        Path path = Paths.get(cfgFName);
        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(path, Charset.forName("UTF-8"));

            // State variables for tokenizing and parsing the config file
            String line = null;
            char c;
            String[] toks;
            int height = -1, width = -1, val;

            // Read each line, parse it
            while ((line = reader.readLine()) != null) {
                
                // Split the line into tokens with ':' as delimiter
                toks = line.split(":");
                
                // The first character tells us what kind of line it is
                c = toks[0].charAt(0);

                // Inspect that first character, act appropriately:
                switch(c) {
                
                // 'H' or 'W' means a height or width directive (board size)
                // Notice we wait for both a 'H' and a 'W' to be processed
                // before we try to create a new Board
                case 'H':
                case 'W':
                    
                    // Read the height or width into our state
                    val = Integer.parseInt(toks[1]);
                    if (c == 'W') width = val;
                    else height = val;

                    // If we have both height and width, initialize a complete
                    // (albeit empty) board
                    if (height != -1 && width != -1) 
                        init(height, width);
                    break;

                // 'T' means a new PieceType
                case 'T':
                    new PieceType(
                        Integer.parseInt(toks[3]), 
                        Integer.parseInt(toks[2]), 
                        this
                    );
                    break;

                // 'P' means a new Piece
                case 'P':
                    new Piece(
                        getTypes().get(Integer.parseInt(toks[1])),
                        this,
                        Integer.parseInt(toks[2]),
                        Integer.parseInt(toks[3])
                    );
                    break;
                    
                // 'S' means one of the Pieces in the solved layout
                case 'S':
                    if (solvedBoard == null) {
                        solvedBoard = new int[getHeight() * getWidth()];
                        Arrays.fill(solvedBoard, -1);
                    }
                    solvedBoard[
                        Integer.parseInt(toks[3]) * getWidth()
                        + Integer.parseInt(toks[2])
                                ] = Integer.parseInt(toks[1]);
                    break;

                default: // do nothing if some other leading character
                    break;
                    
                }
            }
        } catch (IOException iox) {
            System.err.println("Error while reading from config file " + cfgFName);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                
            }
        }

    }
    
    /**
     * Default constructor - creates a new, incomplete Board
     */
	public Board() {

	    // Create the lists to contain the pieces and types
        pieces = new ArrayList<Piece>();
        types = new ArrayList<PieceType>();

	}

	/**
	 * Specific constructor - creates a new Board using supplied dimensions
	 * @param height   The height (in squares) of the Board
	 * @param width    The width (in squares) of the Board
	 */
	public Board(int height, int width) {
	    init(height, width);
    }
    	
	/**
	 * Initialize the board with the supplied height/width
	 * @param height
	 * @param width
	 */
	private void init(int height, int width) {

	    // Store dimensions
        this.height = height;
        this.width = width;
                
        // Create the array of piece locations
        aBoard = new int[height][width];
        
        // Initialize the locations array to empty
        for (int row = 0; row < height; ++row) {
            for (int col = 0; col < width; ++col) {
                aBoard[row][col] = -1;
            }
        }
	    
	}
	
    /**
     * For a given piece, calculate the possible moves (available by calling
     * {@link Piece#getMoves()}).
     * 
     * @param p The piece for which to calculate moves
     */
	public void storeMoves(Piece p) {
        
	    // Conceptually this method 'picks up' the piece from the board array, 
	    // then checks if the spots the piece would cover are clear in each of 
	    // the four possible directions.	    
	    
        // Clear out the old stored moves
        p.getMoves().clear();
        
        // Empty out the spots occupied by the piece
        _markBoard(p, false);        

        // Try placing the piece left
        if (_doesFit(p, p.getLeftPos() - 1, p.getTopPos()))
            p.getMoves().add(Board.MoveDir.LEFT);
        
        // Try right
        if (_doesFit(p, p.getLeftPos() + 1, p.getTopPos()))
            p.getMoves().add(Board.MoveDir.RIGHT);
        
        // Try up
        if (_doesFit(p, p.getLeftPos(), p.getTopPos() - 1))
            p.getMoves().add(Board.MoveDir.UP);

        // Try down
        if (_doesFit(p, p.getLeftPos(), p.getTopPos() + 1))
            p.getMoves().add(Board.MoveDir.DOWN);
        
        // Restore the spots occupied by the piece
        _markBoard(p, true);
        
    }
    

    /**
     * Attempt to move a Piece on the board by one square in a given direction
     * @param p The piece to move
     * @param dir The direction to move the piece
     * @return true if the piece was moved, false if an invalid move
     * @see Piece
     * @see MoveDir
     */
    public boolean move(Piece p, MoveDir dir) {
        
        boolean ret = true;

        // Clear the piece from the board array
        _markBoard(p, false);

        PieceType pType = p.getType();
        int pTop = p.getTopPos();
        int pLeft = p.getLeftPos();
        int pHeight = pType.getHeight();
        int pWidth = pType.getWidth();

        // The logic to slide a piece differs based on the direction
        switch (dir) {

        // Attempting to slide the piece to the left:
        case LEFT:
            // First make sure the piece isn't on the left edge
            if (pLeft == 0) {
                ret = false;
                break;
            }

            
            // look at each row occupied by the piece
            for (int iRow = pTop; iRow < pTop + pHeight; ++iRow) {
                // Check that the new spot to be occupied is clear
                if (aBoard[iRow][pLeft - 1] != -1) {
                    ret = false;
                    break;
                }
                    
                // Mark the spots to be occupied on the board array
                for (
                        int iCol = pLeft -1; 
                        iCol < pLeft + pWidth - 1; 
                        ++iCol
                ) {
                    aBoard[iRow][iCol] = pType.getId();                        
                }
            }
            p.setLeftPos(pLeft - 1);
            break;

        case RIGHT:
            
            // First make sure the piece isn't on the right edge
            if (pLeft + pWidth > width - 1) {
                ret = false;
                break;
            }
                
            // look at each row occupied by the piece
            for (int iRow = pTop; iRow < pTop + pHeight; ++iRow) {
                
                // Check that the new spot to be occupied is clear
                if (aBoard[iRow][pLeft + pWidth] != -1) {
                    ret = false;
                    break;
                }
                    
                // Mark the spots to be occupied on the board array
                for (
                        int iCol = pLeft + 1;
                        iCol < pLeft + pWidth + 1; 
                        ++iCol
                ) {
                    aBoard[iRow][iCol] = pType.getId();
                }
            }
            p.setLeftPos(pLeft + 1);
            break;

        case UP:
            // First make sure the piece isn't on the top edge
            if (pTop == 0) {
                ret = false;
                break;
            }
                
            // look at each column occupied by the piece
            for (int iCol = pLeft; iCol < pLeft + pWidth; ++iCol) {
                
                // Check that the new spot to be occupied is clear
                if (aBoard[pTop - 1][iCol] != -1) {
                    ret = false;
                    break;
                }
                    
                // Mark the spots to be occupied on the board array
                for (
                        int iRow = pTop - 1;
                        iRow < pTop + pHeight - 1; 
                        ++iRow
                ) {
                    aBoard[iRow][iCol] = pType.getId();
                }
            }
            p.setTopPos(pTop - 1);
            break;

        case DOWN:

            // First make sure the piece isn't on the bottom edge
            if (pTop + pHeight  + 1 > height) {
                ret = false;
                break;
            }
                
            // look at each column occupied by the piece
            for (int iCol = pLeft; iCol < pLeft + pWidth; ++iCol) {
                
                // Check that the new spot to be occupied is clear
                if (aBoard[pTop + pHeight][iCol] != -1) {
                    ret = false;
                    break;
                }
                    
                // Mark the spots to be occupied on the board array
                for (
                        int iRow = pTop + 1;
                        iRow < pTop + pHeight + 1; 
                        ++iRow
                ) {
                    aBoard[iRow][iCol] = pType.getId();
                }
            }
            p.setTopPos(pTop + 1);
                
            break;

        default:
            break;

        }

        if (!ret) {
            
            // if the move failed, restore board array
            _markBoard(p, true);
            
        } else {
            
            // If the move was successful, store the set of possible next moves
            for (Piece nextP :  pieces) {
                storeMoves(nextP);
            }
            
        }

        return ret;            
    }
    
    /**
     * Solve the game<br/>
     * Uncomment the desired solution algorithm to switch between recursive
     * and iterative<br/>
     * <br/>
     * NOTE: I had to increase my default stack size to 6m (-Xss6m) with OSX
     * and Sun 64 bit JRE 1.8 to get the recursive version to run.
     * 
     * @return the final move node for the solution
     */
    public MoveNode solve() { 
        return 
                solveIter(solvedBoard);
                //solveRec(solvedBoard);
    }

    /**
     * A recursive solution implementation<br/>
     * <br/>
     * This approach uses a pending list so that as new moves are discovered,
     * the algorithm stores them on the back of the list. This assures that
     * earlier moves (closer to the root) are investigated before later ones
     * (farther away from the root). This results in a breadth-first build of
     * the MoveNode tree.
     * 
     * @param pending   A List of MoveNode instances to try
     * @param tries     A lookup trie structure containing the previously 
     *     attempted move configurations
     * @param solution  an array of piece locations representing the 'solved' 
     *     configuration
     * @return          a MoveNode containing the final move to a solved layout 
     */
    private MoveNode _recSolve(List<MoveNode> pending, MoveTrie tries, int solution[]) {

        // returns the final node to the solution
        // the caller can walk back up each parent until the root (starting
        // layout)

        MoveNode ret = null;
        
        // Sanity check
        if (pending.size() < 1) return null;
        
        // Investigate the first move in the pending list
        MoveNode mn = pending.remove(0);
        
        // Set the board to that move's starting layout
        reset(mn);
        
        // This check is necessary because the root node has null values
        if (mn.getpID() != -1) {
            
            // Perform the move
            move(pieces.get(mn.getpID()), mn.getDir());
        
            // Retrieve the new board layout
            int locs[] = pieceLocs();
        
            // If this move is in our list of tries, it's a duplicate
            if (tries.getRoot().addBoard(locs, 0, this)) {
                mn.setParent(null);
                return null;
            }
        
            // Check if this move resulted in a solved board
            if (matches(locs, solution)) {
                
                // Make sure to append one extra move at the bottom so the
                // caller (probably a UI) can perform the final move to the
                // solved configuration
                mn = new MoveNode(mn, null, MoveDir.NONE, locs);
                return mn;
            }
            
            // If we haven't yet found a solution, append all possible moves
            // from the current configuration to the pending list
            for (MoveNode next : _getNextMoves(mn, locs))
                pending.add(next);
        }

        // Recursively call this algorithm while pending moves remain to check
        while (pending.size() > 0) {
            ret = _recSolve(pending, tries, solution);
            if (ret != null) return ret;
        }
        
        return null;
       
    }
        
    /**
     * Solve the game using the recursive algorithm
     * @param solution an array of piece locations representing a solved layout
     * @return the final move node for the solution
     */
    MoveNode solveRec(int solution[]) {
        
        // Set up the trie of previously discovered moves
        MoveTrie.init(this);
        MoveTrie trie = new MoveTrie();
        
        // Create the root MoveNode
        int pieceLocs[] = pieceLocs();
        MoveNode root = new MoveNode(null, null, Board.MoveDir.NONE, pieceLocs);
        
        // Seed the pending list with the moves from the current layout
        List<MoveNode> pending = this._getNextMoves(root, pieceLocs);
        
        // Invoke the recursive algorithm
        return _recSolve(pending, trie, solution);        
    }
    
    /**
     * Iterative solution algorithm - uses a breadth-first approach with a
     * pending list of moves to attempt.
     * 
     * @param solution an array of piece locations representing a solved layout
     * @return the final move node for the solution
     */
    MoveNode solveIter(int solution[]) {
        
        // Initialize the trie of previously encountered layouts
        MoveTrie.init(this);
        MoveTrie tries = new MoveTrie();
        
        // As we discover moves available, defer nodes for those moves in a
        // pending list
        List<MoveNode> pending = new LinkedList<MoveNode>();
        
        // The current board layout
        int locs[] = pieceLocs();

        // allocate the root MoveNode to match the current board
        MoveNode root = new MoveNode(null, null, MoveDir.NONE, locs);
        
        // For each available move, add that move to the pending list
        for (MoveNode n : _getNextMoves(root, locs)) {
            pending.add(n);
        }
        
        // As we remove pending moves, store each in 'next'
        MoveNode next = null;
        
        // Keep removing nodes until none are left
        while(pending.size() > 0) {

            // Remove the earliest move first
            next = pending.remove(0);
            
            // reset the board layout to match that move, and make the move
            reset(next);
            move(pieces.get(next.getpID()), next.getDir());
            
            // check if the move resulted in a solved configuration
            locs = pieceLocs();
            if (matches(locs, solution)) {
                next = new MoveNode(next, null, MoveDir.NONE, locs);
                return next;
            }
            
            // only add child moves to the pending list if the new layout is
            // one we haven't seen before
            if (!tries.getRoot().addBoard(locs, 0, this)) {
                for (MoveNode n : _getNextMoves(next, locs)) {
                    pending.add(n);
                }
            }            
        }
        
        // Reaching here means we've exhausted all moves - the board is 
        // unsolvable!
        return null;
    }
    
    /**
     * Retrieve an array of piece ids located at each position of the
     * board. This 'flattens' the board array into a single dimension. This
     * representation of the board layout is appropriate for use in both the
     * MoveNode solution tree, and the trie of previously encountered moves.
     * 
     * @return an array of piece ids representing the board layout
     * 
     * @see #MoveNode
     * @see #MoveTrie
     */
    public int[] pieceLocs() {
        
        // Create a flattened array to represent the board
        int[] ret = new int[height * width];
        
        // Initialize the board to unoccupied
        Arrays.fill(ret, -1);
        
        // Fill the board with piece types at the top-left corner of each
        // piece
        for (Piece p : pieces) {
            ret[p.getTopPos() * width + p.getLeftPos()] = p.getpID();
        }
        
        // Return the newly filled-in layout array
        return ret;
    }
    
    /**
     * Reset the board layout to match a MoveNode<br/>
     * <br/>
     * Note this doesn't perform the move - it just sets the board up
     * so that the move COULD be performed
     * 
     * @param mn    the MoveNode containing the desired layout
     */
    public void reset(MoveNode mn) {
        
        // Get the new board layout
        int aPieces[] = mn.getPieces();

        int pID, row, col;
        Piece p = null;

        // Clear the board
        for (int iRow = 0; iRow < height; ++iRow)
            for (int iCol = 0; iCol < width; ++iCol)
                aBoard[iRow][iCol] = -1;
        
        // For each spot in the layout
        for (int i = 0; i < aPieces.length; ++i) {

            // If no Piece in the spot, move to the next
            if ((pID = aPieces[i]) == -1)
                continue;
            
            // retrieve the Piece occupying the spot
            p = pieces.get(pID);

            // Calculate its 2-D position 
            row = i / width;
            col = i % width;

            // Set the Piece's position values
            p.setTopPos(row);
            p.setLeftPos(col);
            
            // Place the Piece on the board
            _markBoard(p, true);
        }
    }
    
    /**
     * Determine if a board layout (as provided by {@link #pieceLocs()})
     * matches the Board solution (see {@link TrafficUI#solvedBoard})
     * @param pids a board layout (as provided by {@link #pieceLocs()})
     * @param types a solved layout of type ids (see {@link TrafficUI#solvedBoard})
     * @return true if the provided pids array matches the types solution array
     */
    public boolean matches(int pids[], int types[]) {
        
        int pID, tID;
        
        // for each item in the board layout array
        for (int i = 0, size = pids.length; i < size; ++i) {
            
            // see if the position in the array contains a piece
            pID = pids[i];
            
            // If the position array says there's no piece there, but the types
            // array shows a piece type, then the two layouts are not a match
            if (pID == -1)
                if (types[i] != -1) return false;
                else continue;
            
            // Reaching here means the position array shows a piece there
            // Get the piece by its id, get its type, and check that the typeID
            // matches the one specified in the type array at the same position
            tID = pieces.get(pID).getType().getId();
            if ( tID != types[i]) 
                return false;
        }
        
        // We've walked all the positions, and each was a match. Done!
        return true;
    }
    
    /**
     * The board height
     * @return the board height
     */
    public int getHeight() {
        return height;
    }

    /**
     * The board width
     * @return the board width
     */
    public int getWidth() {
        return width;
    }

    /**
     * The list of all PieceTypes on the board
     * @return the list of known PieceTypes
     */
    public ArrayList<PieceType> getTypes() {
        return types;
    }

    /**
     * The list of all pieces on the board
     * @return the list of all pieces on the board
     */
    public ArrayList<Piece> getPieces() {
        return pieces;
    }
    
    public int[] getSolvedBoard() {
        return solvedBoard;
    }

    /**
     * Helper method to test if a piece fits on the board at a specified pos
     * 
     * @param p        The piece to test
     * @param leftPos  The column occupied by the left edge of the piece
     * @param topPos   The row occupied by the top edge of the piece
     * @return         true if the board fits, false if it doesn't
     */
    private boolean _doesFit(Piece p, int leftPos, int topPos) {
        // Check if the piece would be off the bounds of the board
        if (
                leftPos < 0
                || topPos < 0
                || p.getType().getWidth() + leftPos > width 
                || p.getType().getHeight() + topPos > height
                
        )
            return false;
        
        // Check that all the squares occupied by the piece are vacant
        for (int row = topPos; row < topPos + p.getType().getHeight(); ++row) {
            for (int col = leftPos; col < leftPos + p.getType().getWidth(); ++col) {
                if (aBoard[row][col] != -1) return false;
            }
        }
        
        // If the piece is entirely in the bounds of the board, and all its
        // squares are vacant, then it fits
        return true;
    }

    /**
     * Helper method to either clear a piece from the board array, or add it
     * 
     * @param p         The piece to remove or add
     * @param place     true if the piece is to be placed, false if removed
     */
    void _markBoard(Piece p, boolean place) {

        // The type of the piece
        PieceType pt = p.getType();

        // The id of that type
        int id = pt.getId();

        // The height and width of the piece
        int height = pt.getHeight();
        int width = pt.getWidth();

        // The location of the piece
        int topPos = p.getTopPos();
        int leftPos = p.getLeftPos();

        // For each square of the piece, either mark it occupied, or vacant
        // (depending on the value of the 'place' boolean)
        for (int row = topPos; row < topPos + height; ++row) {
            for (int col = leftPos; col < leftPos + width; ++col) {
                aBoard[row][col] = (place) ? id : -1;
            }
        }
    }
    
    /**
     * Helper method to retrieve the next possible moves from the current
     * board. To minimize computation time, assumes the user has previously
     * called {@link #pieceLocs()}, passing the returned array as parameter 
     * pieceLocs[]
     * 
     * @param mn        the {@link #MoveNode} which resulted in the current board
     * @param pieceLocs the array of piece locations describing this board
     * @return          a LinkedList of {@link #MoveNode} objects, representing the 
     * set of possible moves available from this board configuration
     */
    private List<MoveNode> _getNextMoves(MoveNode mn, int pieceLocs[]) {
        
        // Create the list of nodes
        List<MoveNode> nextMoves = new LinkedList<MoveNode>();

        // For every piece
        for (Piece p : pieces) {
            
            // Get the possible moves, and for each one
            for (MoveDir md : p.getMoves()) {
                
                // Add the move (as a MoveNode) to the move list
                nextMoves.add(new MoveNode(mn, p, md, pieceLocs));
            
            }
        }

        return nextMoves;
    }
    
    /**
     * For debugging purposes, print a text representation of the board layout
     */
    public void printBoard() {
        
        // The type id of the piece occupying a spot
        int val;

        for (int row = 0; row < height; ++row) {
            for (int col = 0; col < width; ++col) {
 
                val = aBoard[row][col];
                System.out.print((val == -1) ? " " : val);

            }
            
            System.out.print('\n');
        }
    }

}

