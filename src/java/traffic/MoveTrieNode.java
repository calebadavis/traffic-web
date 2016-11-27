package traffic;

/**
 * A node in the trie structure (for holding board layouts).
 * 
 * It's helpful to think of the board layouts contained in the trie as being
 * 'strings', and each node being a 'letter' in a particular sequence of pieces
 * 
 * @see MoveTrie
 */
public class MoveTrieNode {
    
    /**
     * Create a new trie node to represent a 'letter' in a board layout
     * 'string'.
     */
    public MoveTrieNode() {
        children = null;
    }

    /**
     * Create the array of possible child 'letters' from this trie node
     */
    public void allocChildren() {
        if (children == null) 
            children = new MoveTrieNode[MoveTrie.NUM_TYPES + 1];
    }
    
    /**
     * Recursively adds a board layout (or a partial board layout) to a trie of
     * known board positions. See {@link TrieNode} for a discussion of how this
     * implementation relates board layouts to the trie structure.
     * 
     * @param pieces - The piece id of every piece on the board (the array 
     * represents the board grid, and a value other than -1 indicates 
     * the piece with that id is on the board with its top-left corner 
     * occupying that square). To generate this array, consider 
     * {@link Board#pieceLocs()}.
     * 
     * @param pos - The position in the pieces array that this trie node is to
     * occupy - The first position (top-left of the board) uses index 0, and
     * subsequent positions have higher indices.
     * 
     * @param b - The one and only game board
     * 
     * @return true if the board layout was already in the trie, or false 
     * if the layout wasn't found
     */
    boolean addBoard(int pieces[], int pos, Board b) {
        
        // End condition of the recursive call - we've walked the entire array
        if (pos == pieces.length)
            return true;
        

        // The ultimate return value - true if we add every position
        boolean ret = true;
        
        // If this node has no children, go ahead and create them. Also set the
        // return value to false, since the position is new to the trie
        if (children == null) {
            allocChildren();
            ret = false;
        }
        
        // Get the id of the piece occupying the position
        int pID = pieces[pos];

        // If there is no piece (pID of -1), set the trie node value to 
        // NUM_TYPES, otherwise set it to the type id of the piece
        int type = 
                (pID == -1) ? 
                        MoveTrie.NUM_TYPES 
                        : b.getPieces().get(pID).getType().getId();
        
        // By knowing the type id, we know which child needs to come next. If
        // that child doesn't yet exist, we know the  layout is new
        if (children[type] == null) {
            children[type] = new MoveTrieNode();
            ret = false;
        }
        
        // Finally make the recursive call on the next position in the array.
        // We return true only if all other tests say this is a new layout, and
        // the recursive call similarly indicates a new layout
        return (children[type].addBoard(pieces, pos+1, b) && ret);
        
    }
    
    /**
     * Get the following 'letters' to this trie node
     * @return an array containing the set of next 'letters' from this one
     */
    public MoveTrieNode[] getChildren() {
        return children;
    }

    private MoveTrieNode children[];
}
