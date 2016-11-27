package traffic;

/**
 * A trie structure for fast storage and lookup of board layouts.<br/>
 * <br/>
 * The conceptual approach is to treat the board layout as a string.
 * The alphabet is the set of piece types, plus a special value indicating
 * that no piece is anchored (top-left) in the specified board position.<br/>
 * <br/>
 * The layout of pieces on the Board is stored as a 2-D array of piece ID 
 * integers (see {@link Board#getBoard()}), which gets converted into a flat 
 * array of piece ids by a call to {@link Board#pieceLocs()}. It is a 
 * modified version of this flat array that the trie stores.
 * @see MoveTrieNode
 */
public class MoveTrie {
    
    // The values stored in the trie - 0 through NUM_TYPES
    public static int NUM_TYPES;

    // The number of squares on the board
    public static int SQUARES;
    
    // Special marker value indicating no piece is anchored in this spot
    public static int EMPTY_VAL = -1;
    
    // Indicates if the trie has been configured
    public static boolean initialized = false;
    
    /**
     * Initializes the trie with values from a Board
     * @param b     the Board to configure the trie
     */
    public static void init(Board b) {
        if (initialized) return;
        NUM_TYPES = b.getTypes().size();
        SQUARES = b.getHeight() * b.getWidth();
        initialized = true;
    }
    
    /**
     * Construct a new trie
     */
    public MoveTrie() {
        root = new MoveTrieNode();
        root.allocChildren();
    }

    /**
     * Retrieve the root node of the trie
     * @return  the root MoveTrieNode
     */
    public MoveTrieNode getRoot() {
        return root;
    }

    // The root of the trie
    private MoveTrieNode root;
}
