/**
 * BoardController.groovy
 * 
 * Server side HTTP handling of the 'Traffic Jam' puzzle
 */

package traffic

import traffic.Board
import traffic.Board.MoveDir
import traffic.Piece
import traffic.PieceType
import traffic.MoveNode
import traffic.WebPiece

import grails.converters.*

class BoardController {

	/**
	 * Handle a request (most likely an AJAX call via. POST) to move a piece
	 * @return a JSON array of the new positions of all the pieces on the board
	 * and the available moves to every piece
	 */
    def movePiece() {
		
		// The array to return
        def ret = []
		
		// Get the underlying Java Board object from the session
        Board b = session.board
        if (b != null) {
			
			// Get the piece's integer ID, and the direction to move it,
			// from the client's move request
            def pID = Integer.parseInt(params.pID)
            def iDir = Integer.parseInt(params.dir)
			
			// Ask the board for the matching Piece object
            Piece p = b.getPieces().get(pID)
			
            if (p != null) {
				
				// Convert the requested direction (integer) into a MoveDir
                Board.MoveDir md = MoveDir.NONE
                switch (iDir) {
                case 0:
                    md = MoveDir.LEFT
                    break
                case 1:
                    md = MoveDir.RIGHT
                    break
                case 2:
                    md = MoveDir.UP
                    break
                case 3:
                    md = MoveDir.DOWN
                    break
                default:
                    break
                }

				// Only perform the move if the direction could be resolved
                if (md != MoveDir.NONE) {

					// Move the piece on the board
                    if (b.move(p, md)) {
						
						// Use _webPieces() helper to generate the proper array
						// of data on every piece, in a web-friendly format
                        ret = _webPieces(b)
                    }

                }

            }
        }
		
		// Send the board data to the client
        render ret as JSON
    }

	/**
	 * Handle a client's request to solve the game, and return an array of
	 * solution (piece id, move direction) pairs. This array represents the
	 * full set of moves required to solve the puzzle
	 * 
	 * @return the solution - an array of (piece id, direction) pairs.
	 */
    def solve() {

		// The array to return (as JSON) containing the solution
		def dirs = []
		
		// Try to get the current board from the user's session
        Board b = session.board
		if (b != null) { 
		
            // Squirrel away the current board layout, so we can restore it
            // once we're done solving
            // A MoveNode is a convenient way to store a board layout
            def curLayout = 
                new MoveNode(null, null, MoveDir.NONE, b.pieceLocs())
        
            // Build up the MoveNode tree until a solution is found:
            MoveNode solved = b.solve()
        
			// Walk the solved tree back up from the solution to the root
            MoveNode mn = solved
            while(mn.getParent() != null) {
				
				// For each move, store a pair of (piece id, direction)
				// integers
                dirs.add(0, [mn.getpID(), _moveToInt(mn.getDir())])
                mn = mn.getParent()
            }
    
			// Restore the board to the layout it had before we invoked
			// the 'solve()' method
            b.reset(curLayout)
        
			// Since we altered the board, need to recalculate possible moves
            for (Piece p : b.getPieces())
                b.storeMoves(p)
		}
		
		// Send the results back to the client
        render dirs as JSON
    }

	// Display the main board page, passing in variables representing the
	// board dimensions, and the data associated with all the pieces
    def index() { 
		
		// Create the board, using the config file from in the trafficcore.jar
        Board b = new Board(true)
		
		// Store the board in the user's session
        session.board = b
		
		// Calculate the availale moves
        for (Piece p : b.getPieces()) {
            b.storeMoves(p)
        }
		
		// Pass data to the .GSP processing layer
        [
            webPieceList:_webPieces(b),
            solvedLayout:_solvedLayout(b), 
            height:b.getHeight(), 
            width:b.getWidth()
        ]
    }

	/**
	 * Helper function to populate WebPiece objects with data associated with
	 * every Piece on the board
	 * @param b the board containing all the pieces
	 * @return an array of WebPiece objects, containing dimensions, locations,
	 * and available moves for each piece
	 */
    private WebPiece[] _webPieces(Board b) {
	
	// Get the pieces from the board
	def pieces = b.getPieces()
	
	    // Generate an appropriately sized array
        def webPieces = new WebPiece[pieces.size()]
		
        // Walk the list of pieces, generating an equivalent WebPiece for each
		def i = 0
        for (Piece p : pieces) {
            webPieces[i++] = _pieceToWebPiece(p)
        }
		
		// Return the generated array
        webPieces
    }

	/**
	 * Helper method to convert the solved layout into a WebPiece array.
	 * Note that the resulting WebPiece instances have no ID, as the solved
	 * layout only dictates where any piece of a particular type might be found
	 * 
	 * @param b the Board instance containing a solved layout
	 * @return an array of WebPiece instances
	 */
	private ArrayList<WebPiece> _solvedLayout(Board b) {
		
		// We need Board width to calculate the 2D position of a piece from
		// its absolute index in the 1D solved layout
		def bWidth = b.getWidth()
		
		// The ArrayList to return
		def wpa = []
		
		// We need to get PieceType instances from the type ids in the solution
		def pTypes = b.getTypes()
		
		// The PieceType
		def pt

		// Walk the solution array, looking for PieceType ids
        b.getSolvedBoard().eachWithIndex { num, idx ->
			
			// Skip if empty
			if (num != -1) {

                // Get the PieceType (for height/width)
                pt = pTypes.get(num)
				
				// Create the WebPiece, add it to the list
				wpa << new WebPiece(
					id: -1, 
					xPos: idx % bWidth,
					yPos: idx / bWidth,
					width: pt.getWidth(),
					height: pt.getHeight()
			    )	
			} 
		}
		
		// return the list
		wpa
	}
	
	/**
	 * Helper function to generate a WebPiece containing the important data
	 * for a piece on the board
	 * @param p the Piece on the Board
	 * @return a WebPiece representation of the supplied Piece from the board
	 */
    private WebPiece _pieceToWebPiece(Piece p) {

		// Populate an array of available move directions to each piece		
        def dirs = []
        p.getMoves().each { dir ->
            dirs << _moveToInt(dir)
        }

		// Some of the important data is stored in the PieceType:
		PieceType pt = p.getType()

		// Create the WebPiece with piece ID, position, size, and moves
        def ret = new WebPiece(
            id: p.getpID(),
            xPos: p.getLeftPos(),
            yPos: p.getTopPos(),
            width: pt.getWidth(),
            height: pt.getHeight(),
            moves: dirs
        )
		
		// Return the WebPiece
        ret
    }

	/**
	 * Helper function to convert a MoveDir into an integer move
	 * @param md the MoveDir to convert
	 * @return the integer representing the move direction
	 */
    private int _moveToInt(Board.MoveDir md) {
	int ret = 4
        switch (md) {
        case MoveDir.LEFT:
            ret = 0
            break
        case MoveDir.RIGHT:
            ret = 1
            break
        case MoveDir.UP:
            ret = 2
            break
        case MoveDir.DOWN:
            ret = 3
            break
        default:
            break
        }
        ret
    }
}
