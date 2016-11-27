package traffic

import traffic.Board
import traffic.Board.MoveDir
import traffic.Piece
import traffic.PieceType
import traffic.MoveNode
import traffic.WebPiece

import grails.converters.*

class BoardController {

    def movePiece() {
        def ret = null
        Board b = session.board
        if (b != null) {
            def pID = Integer.parseInt(params.pID)
            def iDir = Integer.parseInt(params.dir)
            Piece p = b.getPieces().get(pID)
            if (p != null) {
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

                if (md != MoveDir.NONE) {

                    if (b.move(p, md)) {
                        ret = _webPieces(b)
                    }

                }

            }
        }
        render ret as JSON
    }

    def solve() {

        Board b = session.board

        def curLayout = new MoveNode(null, null, MoveDir.NONE, b.pieceLocs())
        

        // Build up the MoveNode tree until a solution is found:
        MoveNode solved = b.solve()
        
        def dirs = []

        MoveNode mn = solved
        while(mn.getParent() != null) {
            dirs.add(0, [mn.getpID(), _moveToInt(mn.getDir())])
            mn = mn.getParent()
        }
    
        b.reset(curLayout)
        
        // Calculate possible moves
        for (Piece p : b.getPieces())
            b.storeMoves(p)

        render dirs as JSON
    }

    def index() { 
        Board b = new Board("/tmp/config")
        session.board = b
        for (Piece p : b.getPieces()) {
            b.storeMoves(p)
        }
        [webPieceList:_webPieces(b), height:b.getHeight(), width:b.getWidth()]
    }

    private WebPiece[] _webPieces(Board b) {
	def pieces = b.getPieces()
        def webPieces = new WebPiece[pieces.size()]
        def i = 0
        for (Piece p : pieces) {
            webPieces[i++] = _pieceToWebPiece(p)
        }
        webPieces
    }

    private WebPiece _pieceToWebPiece(Piece p) {
	PieceType pt = p.getType()
        def dirs = []
        p.getMoves().each { dir ->
            dirs << _moveToInt(dir)
        }

	def ret = new WebPiece(
            id: p.getpID(),
            xPos: p.getLeftPos(),
            yPos: p.getTopPos(),
            width: pt.getWidth(),
            height: pt.getHeight(),
            moves: dirs
        )

        ret
    }

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
