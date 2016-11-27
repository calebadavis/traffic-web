package traffic;

import java.util.List;

/**
 * Type information for a piece - height + width
 */
public class PieceType {

    /**
     * Create a new PieceType instance
     * 
     * @param h     the height of all pieces of this type
     * @param w     the width of all pieces of this type
     * @param b     the Board to store pieces of this type
     */
    public PieceType(int h, int w, Board b) {
        
        // member data storage
        this.height = h;
        this.width = w;
        
        // add this type to the board's list of types
        List<PieceType> types = b.getTypes();
        id = types.size();
        types.add(this);
    }
    
    /**
     * Retrieve the height property
     * @return  the height of all pieces of this type
     */
    public int getHeight() {
        return height;
    }

    /**
     * Retrieve the width property
     * @return  the width of all pieces of this type
     */
    public int getWidth() {
        return width;
    }

    /**
     * Retrieve the ID of this PieceType (this type's position in the Board's
     * list of PieceTypes). 
     * @return  this type's ID
     */
    public int getId() {
        return id;
    }

    // height of pieces of this type    
    private int height;

    // width of pieces of this type
    private int width;

    // unique id of this type, matching the type's position in the Board's list
    // of PieceTypes
    private int id;

}
