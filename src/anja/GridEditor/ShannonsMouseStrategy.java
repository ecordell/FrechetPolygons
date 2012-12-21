package anja.GridEditor;

import static anja.GridEditor.Directions.*;

public class ShannonsMouseStrategy implements SearchStrategy {
	
	/** This array contains the directions
	 * into which the robot left a cell last.
	 */
	private int[][] _lastDirection;

	
	/**
	 * Initializes an array which for every cell contains the last
	 * direction into which the robot left the cell last.
	 */
	public void start(Robot robot, Grid grid) {
		_lastDirection = new int[grid.GetGridSize()][grid.GetGridSize()];
		for (int i=0; i<_lastDirection.length; i++) {
			for (int j=0; j<_lastDirection.length; j++) {
				_lastDirection[i][j] = NORTH;
			}
		}
	}
	
	
	/**
	 * To get the direction in which the robot moves next,
	 * the direction of the cell the robot stands on has to be
	 * turned clockwise. This is achieved by simply incrementing it
	 * by 1 and setting it to 0 if the direction is >3, since 
	 * the directions are defined in the Directions class in clockwise
	 * order from 0=NORTH to 3=WEST.
	 */
	public int getDirection(Robot robot, Grid grid) {
		int counter = 0;
		do {
			_lastDirection[robot.x][robot.y] += 1;
			if (_lastDirection[robot.x][robot.y] > 3)
				_lastDirection[robot.x][robot.y] = 0;
			counter++;
		} while (grid._cells[robot.x][robot.y].walls[_lastDirection[robot.x][robot.y]] == true && counter < 4);
		return _lastDirection[robot.x][robot.y];
	}
	
	
	public void directionSuccessful(Robot robot, Directions dir, int cause) {}
	
	public void directionFailed(Robot robot, Directions dir) {}


	public void stop() {
		//nothing to do here
	}
	
	public SearchStrategy CreateNew() {
		SearchStrategy s = new ShannonsMouseStrategy();
		return s;
	}

}
