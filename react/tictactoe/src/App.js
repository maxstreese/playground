import { useState } from 'react';

function Square({ value, onSquareClick, isWinner }) {
  return (
    isWinner ?
    <button className="square highlight" onClick={onSquareClick}>{value}</button> :
    <button className="square" onClick={onSquareClick}>{value}</button>
  );
}

function Board({ xIsNext, squares, onPlay }) {

  const winner = calculateWinner(squares);
  const isDraw = !winner && squares.filter(s => s).length === 9

  function handleClick(i) {
    const nextSquares = squares.slice();
    if (squares[i] || winner) {
      return;
    }
    if (xIsNext) {
      nextSquares[i] = "X";
    } else {
      nextSquares[i] = "O";
    }
    onPlay(nextSquares, i)
  }

  let status;
  if (winner) {
    status = "Winner: " + squares[winner[0]];
  } else if (isDraw) {
    status = "Draw"
  } else {
    status = "Next player: " + (xIsNext ? "X" : "O");
  }

  const cell = (i) => <Square key={i} value={squares[i]} onSquareClick={() => handleClick(i)} isWinner={winner && winner.includes(i)} />

  const row = (start) => <div key={start} className="board-row">{[0,1,2].map(i => cell(start + i))}</div>

  return (
    <>
      <div className="status">{status}</div>
      {[0,3,6].map(start => row(start))}
    </>
  );
}

export default function Game() {
  const [history, setHistory] = useState([{ squares: Array(9).fill(null), move: null }]);
  const [currentMove, setCurrentMove] = useState(0);
  const [showHistoryAsc, setShowHistoryAsc] = useState(true);

  const xIsNext = currentMove % 2 === 0;
  const currentSquares = history[currentMove].squares;

  function handlePlay(squares, move) {
    const nextHistory = [...history.slice(0, currentMove + 1), { squares, move }];
    setHistory(nextHistory);
    setCurrentMove(nextHistory.length - 1);
  }

  function jumpTo(nextMove) {
    setCurrentMove(nextMove);
  }

  const moves = history.map(({ move }, i) => {
    let description;

    const row = move ? Math.floor(move / 3) + 1 : 0;
    const col = move ? move % 3 + 1 : 0;

    switch(i) {
      case 0:
        description = i === currentMove ? 'You are at game start' : 'Go to game start';
        break;
      case currentMove:
        description = `You are at move #${i} (${row}, ${col})`;
        break;
      default:
        description = `Go to move #${i} (${row}, ${col})`;
        break;
    }

    const content =
      i === currentMove ?
      <div>{description}</div> :
      <button onClick={() => jumpTo(i)}>{description}</button>

    return (<li key={i}>{content}</li>);
  });

  const orderedMoves = showHistoryAsc ? moves : moves.reverse()

  return (
    <div className="game">
      <div className="game-board">
        <Board xIsNext={xIsNext} squares={currentSquares} onPlay={handlePlay} />
      </div>
      <div className="game-info">
        <button onClick={() => setShowHistoryAsc(!showHistoryAsc)}>
          {showHistoryAsc ? 'Order Desc' : 'Order Asc'}
        </button>
        <ol>{orderedMoves}</ol>
      </div>
    </div>
  );
}

function calculateWinner(squares) {
  const lines = [
    [0, 1, 2],
    [3, 4, 5],
    [6, 7, 8],
    [0, 3, 6],
    [1, 4, 7],
    [2, 5, 8],
    [0, 4, 8],
    [2, 4, 6]
  ];
  for (let i = 0; i < lines.length; i++) {
    const [a, b, c] = lines[i];
    if (squares[a] && squares[a] === squares[b] && squares[a] === squares[c]) {
      return lines[i];
    }
  }
  return null;
}
