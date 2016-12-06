<!DOCTYPE html>
<!--  
index.gsp
The browser-based client-side page which displays the 'Traffic Jam' game board.
This file is intended to be rendered by a Grails controller 
(grails-app/controllers/traffic/BoardController.groovy).
-->
<html>
    <head>
        <!-- Use the latest jQuery library -->
        <script 
         src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js">
        </script>
        <script>
        $(document).ready(function() {

            // altDir allows the user to specify that a click on a piece
            // should result in a move in a direction lower in the priority
            // list, assuming the piece can move in more than one direction
            window.altDir = false;

            // The 'Use alternate direction' checkbox sets the above 'altDir'
            // global variable
            $("#altDir").click(function() {
                window.altDir = this.checked;
            });

            // The 'Solve' and 'Next' buttons, when clicked, will call the
            // functions 'solve()' and 'next()'
            $("#solve").on("click", solveClick);
            $("#next").on("click", nextClick);

            // These 'each' tags allow the framework to insert this code
            // dynamically at the time it constructs the page. Essentially each
            // button representing a piece gets a couple custom data elements 
            // attached through jQuery magic (the .data() function). These
            // elements associate the piece ID and the set of available moves
            // with the HTML button.
            var btn;
            <g:each in="${webPieceList}" var="piece">
            btn = $("#btn${piece.id}");
            btn.data("pID", ${piece.id});
            btn.on("click", btnClick);
            btn.data("moves", ${piece.moves});
            </g:each>
        });

        // Hitting the 'Solve' button results in this AJAX call:
        function solveClick() {
            $.post(
                "${createLink(controller: 'board', action: 'solve')}",
                {},
                function(data, status) {

                    // When the AJAX call returns, this 'handleSolveResponse'
                    // function kicks in
                    handleSolveResponse(data, status);
                }
            );
        }

        // When the server responds that it's sent a solution,
        // this function stores the solution data on the page
        function handleSolveResponse(data, status) {
            if (status == "success") {
                window.solution = data;
                window.curMove = 0;
                $("#next").prop("disabled", false);
            }
        }

        // Hitting the 'Next' button invokes this function
        function nextClick() {

            // Only proceed if the user has previously hit the 'Solve' button,
            // and the solution was successfully stored in the UI:
        	if (window.solution == null) return;

        	// Get the next pair of (piece ID, direction) integers from the
        	// solution array, incrementing the global variable tracking which
        	// move we're on in the solution chain
        	var nextMove = window.solution[window.curMove++];

            // Check the piece ID, if it's the special '-1' value, it means
            // the current board is already at the solved state
        	var pid = nextMove[0];
        	if (pid == -1) {
        		window.solution = null;
        		window.curMove = 0;
        		return;
        		$("#next").prop("disabled", true);
        		$("#solve").prop("disabled", true);
            }

            // Otherwise fire off the same AJAX call to the board controller
            // that we'd normally call from a click on a piece. Handle the
            // response the same way 
            $.post(
                    "${createLink(controller: 'board', action: 'movePiece')}",
                    {
                        pID: pid,
                        dir: nextMove[1]
                    },
                    function(data, status) {
                        handleMoveResponse(pid, data, status);
                    }
                );
        }

        // Handles a click on one of the move buttons:
        function btnClick() {

            // Get the moves available to the piece
            var moves = $(this).data("moves");

            // If no moves are available, nothing to do!
            if (moves == null || moves.length == 0)
                return;

            // Determine which direction to move the piece. If only one move
            // is available, move that way. Otherwise choose which move based
            // on the state of the 'altDir' global boolean.
            var dir = moves[(moves.length == 1) ? 0 :
                          (window.altDir) ? 1 : 0];

            // Get the ID of the piece to move, then fire off the AJAX request
            // to the board controller asking to perform the move.
            var pid = $(this).data("pID");
            $.post(
                "${createLink(controller: 'board', action: 'movePiece')}",
                {
                    pID: pid,
                    dir: dir
                },
                function(data, status) {

                    // Don't forget that a manual move, if successful, will
                    // invalidate the chain of automated solution moves. Clear
                    // them out.
                    if (status == "success") {
                        window.solution = null;
                        window.curMove = 0;
                        $("#next").prop("disabled", true);
                        $("#solve").prop("disabled", false);
                    }

                    // Finally update the board with the results of the move
                    handleMoveResponse(pid, data, status);
                }
            );
        }

        // Update the board to reflect a new layout after a successful move:
        function handleMoveResponse(pid, data, status) {
            if (status == "success") {

                // Determine which piece was moved:
                var movedWebPiece = data[pid];

                // Get the associated UI button from the DOM:
                var movedBtn = $("#btn" + pid);

                // Display the button to reflect its new position on the board
                movedBtn.css({
                    top: (movedWebPiece.yPos * 50).toString()+"px", 
                    left: (movedWebPiece.xPos * 50).toString()+"px", 
                    position:"absolute"
                });

                // Update the set of moves available to each piece
                for (var i = 0; i < data.length; ++i) {

                    // The ID of the web piece:
                    var wp = data[i];

                    // The button in the DOM
                    var btn = $("#btn" + wp.id);

                    // Store the available moves to the button
                    btn.data("moves", wp.moves);
                }
            }
        }

        // Allow the user to hit or release the 'a' button to indicate a move
        // click should be in an alternate direction
        function keyIsDown(event) {
            var key = event.keyCode;
            if (key == 65) // 'a'
            {
                window.altDir = true;

                // Also set the 'checked' state of the alternate direction
                // checkbox as a visual reminder to the user that they're
                // performing an alternate move
                $("#altDir").prop("checked", true);
            }
        }

        // Restore the altDir variable and checkbox if the user releases the
        // 'a' key
        function keyIsUp(event) {
            var key = event.keyCode;
            if (key == 65) // 'a'
            {
                window.altDir = false;
                $("#altDir").prop("checked", false);
            }
        }

        // End of JavaScript code
        </script>
		<meta name="layout" content="main"/>
        <title>Traffic Jam</title>
    </head>
    <body>
        <div id="page-body" role="main">
        <h1>Traffic Jam</h1>
        
        <!-- 
            This div is the main board rectangle
            It gets sized by .GSP variable processing with data passed to the
            page by the board controller (variables 'height' and 'width') 
        -->
        <div 
          id="board" 
          style=
            "position: relative; 
             height:${height * 50}px;
             width:${(width * 2 + 2) * 50}px"
          onkeydown="keyIsDown(event)"
          onkeyup="keyIsUp(event)"
        >
        
        <!-- Grails .GSP magic, creating a <button> element for each piece -->
        <g:each in="${webPieceList}" var="piece">
            <button 
              type="button" 
              id="btn${piece.id}"
              style=
                "position: absolute;
                 height:${piece.height * 50}px;
                 width:${piece.width * 50}px;
                 left:${piece.xPos * 50}px;
                 top:${piece.yPos * 50}px"
            />
        </g:each>

        <!-- More .GSP magic, placing <button> elements showing solution  -->
        <g:each in="${solvedLayout}" var="piece">
            <button 
              type="button" disabled
              style=
                "position: absolute;
                 height:${piece.height * 50}px;
                 width:${piece.width * 50}px;
                 left:${(piece.xPos  + 2 + width) * 50}px;
                 top:${piece.yPos * 50}px"
            />
        </g:each>

        </div>
        <button type="button" id="solve">Solve</button>
        <button type="button" disabled id="next">Next</button>
        <input 
            type="checkbox" 
            id="altDir" 
            name="altDir" 
            value="altDir"
        />
        Move other way
    </div>
    </body>
</html>