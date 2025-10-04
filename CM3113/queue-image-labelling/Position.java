public class Position
{
	int row,
		col;
	Position(int r, int c) { row = r; col = c; }
	Position(Position pos) {
		this.row = pos.row;
		this.col = pos.col;
	}
}
