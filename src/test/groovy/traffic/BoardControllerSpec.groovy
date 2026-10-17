package traffic

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class BoardControllerSpec extends Specification implements ControllerUnitTest<BoardController> {

    void "index stores a new board in the session and passes its layout to the view"() {
        when:
        def model = controller.index()

        then:
        session.board != null
        model.width == 4
        model.height == 5
        model.webPieceList.size() > 0
        model.solvedLayout.size() > 0
    }

    void "movePiece without a board in the session returns an empty JSON array"() {
        when:
        controller.movePiece()

        then:
        response.json.size() == 0
    }

    void "solve returns a non-empty list of (piece id, direction) moves"() {
        given:
        controller.index()
        response.reset()

        when:
        controller.solve()

        then:
        response.json.size() > 0
        response.json.every { it.size() == 2 }
    }
}
