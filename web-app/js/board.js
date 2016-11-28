/*
 * board.js - JavaScript functions for client-side functionality of the
 *            'Traffic Jam' web UI
 */

function solveClick() {
    $.post(
        "${createLink(controller: 'board', action: 'solve')}",
        {},
        function(data, status) {
            handleSolveResponse(data, status);
        }
    );
}


function handleSolveResponse(data, status) {
    if (status == "success") {
        window.solution = data;
        window.curMove = 0;
    }
}

function nextClick() {
	if (window.solution == null) return;
	var nextMove = window.solution[window.curMove++];
	var pid = nextMove[0];
	if (pid == -1) {
		window.solution = null;
		window.curMove = 0;
		return;
    }
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

function btnClick() {
    var moves = $(this).data("moves");
    if (moves == null || moves.length == 0)
        return;
    var dir = moves[(moves.length == 1) ? 0 :
                  (window.altDir) ? 1 : 0];
    var pid = $(this).data("pID");
    $.post(
        "${createLink(controller: 'board', action: 'movePiece')}",
        {
            pID: pid,
            dir: dir
        },
        function(data, status) {
            if (status == "success") {
                window.solution = null;
                window.curMove = 0;
            }
            handleMoveResponse(pid, data, status);
        }
    );
}

function handleMoveResponse(pid, data, status) {
    if (status == "success") {
        var movedWebPiece = data[pid];
        var movedBtn = $("#btn" + pid);
        movedBtn.css({
            top: (movedWebPiece.yPos * 50).toString()+"px", 
            left: (movedWebPiece.xPos * 50).toString()+"px", 
            position:"absolute"
        });
        for (var i = 0; i < data.length; ++i) {
            var wp = data[i];
            var btn = $("#btn" + wp.id);
            btn.data("moves", wp.moves);
        }
    }
}

function keyIsDown(event) {
    var key = event.keyCode;
    if (key == 65) // 'a'
    {
        window.altDir = true;
        $("#altDir").prop("checked", true);
    }
}

function keyIsUp(event) {
    var key = event.keyCode;
    if (key == 65) // 'a'
    {
        window.altDir = false;
        $("#altDir").prop("checked", false);
    }
}

$(document).ready(function(){
    window.altDir = false;
    $("#altDir").click(function() {
        window.altDir = this.checked;
    });
    $("#solve").on("click", solveClick);
    $("#next").on("click", nextClick);
    var btn;
    <g:each in="${webPieceList}" var="piece">
    btn = $("#btn${piece.id}");
    btn.data("pID", ${piece.id});
    btn.on("click", btnClick);
    btn.data("moves", ${piece.moves});
    </g:each>
});
