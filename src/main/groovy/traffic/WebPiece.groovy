/**
 * WebPiece.groovy
 * A simple struct of basic data for a Piece on a 'Traffic Jam' board
 */

package traffic

class WebPiece {
	
	// The id of the Piece on the Board
    int id
	
	// The horizontal and vertical locations of the Piece
    int xPos
    int yPos
	
	// The dimensions of the Piece
    int width
    int height
	
	// The moves available to the Piece
    ArrayList moves
}