<!DOCTYPE html>
<html>
    <head>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
        <g:javascript src="board.js" />
        <title>Traffic Jam</title>
    </head>
    <body>
        <h1>Traffic Jam</h1>
        <div 
          id="board" 
          style=
            "position: relative; 
             height:${height * 50}px;
             width:${width * 50}px"
          onkeydown="keyIsDown(event)"
          onkeyup="keyIsUp(event)"
        >
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
        </div>
        <button type="button" id="solve">Solve</button>
        <button type="button" id="next">Next</button>
        <input type="checkbox" id="altDir" name="altDir" value="altDir">Move other way
    </body>
</html>