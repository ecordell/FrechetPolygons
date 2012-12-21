package anja.GridEditor;

import static anja.GridEditor.Directions.*;

import java.awt.*;
import java.util.Vector;

/** This class represent a robot moving on the grid.
 * Each Robot contains several coordinates with information
 * about its current position, target, the direction it is moving, etc.
 * 
 * Each information of the robot is implemented as a public field without
 * setters/getters since these would have no real purpose and slow the
 * animation/computation down.
 * 
 * Additionally the robot contains methods to set its desired size in pixels
 * and to draw itself or its target, these methods are used by the Grid class.
 * 
 * @author Joerg Wegener *
 */

public class Robot {
	
	/** X-Coord of the position in the grid */
	public int x = NONE;
	/** Y-Coord of the position in the grid */
	public int y = NONE;
	
	/** X-Coord of the starting position in the grid */
	public int startX = NONE;
	/** Y-Coord of the starting position in the grid */
	public int startY = NONE;
	
	/** X-Coord of the position of the robot's target */
	public int targetX = NONE;
	/** Y-Coord of the position of the robot's target */
	public int targetY = NONE;
	
	/** X-Coord of the target position of the next step
	 *  (robot is always moving from x/y to nextX/Y) */
	public int nextX = NONE;
	/** Y-Coord of the target position of the next step */
	public int nextY = NONE;
	
	/** X-Coord of the position of the last step before reaching x/y */
	public int lastX = NONE;
	/** Y-Coord of the position of the last step before reaching x/y */
	public int lastY = NONE;
	
	/** Direction the robot is facing, could also be computed by
	 * comparing x/y and nextX/Y */
	public int direction = NONE;
		
	/**
	 * ID of the robot. Each Robot has a unique id, starting at 0
	 * and counting up. Used for loading different animations automatically,
	 * coloring the robots and drawing targets differently. 
	 */
	private int _id = -1;
	
	/** Color of the robot, is set automatically by SetID */
	private Color _color = Color.black;
	
	/** Position of the robot between x/y and nextX/Y as a fraction,
	 * 0 means robot is at x/y, 1 means robot is at nextX/Y,
	 * this is used for the animation of moving between to cells */
	public float position = 0;
	
	/** Running speed of the robot relative to the GridEditor's speed.
	 * Default is 1.0, 0.5 means half speed (meaning it takes double
	 * the time to move between two cells), 2.0 means double speed.*/
	public float speed = 1.0f;
	
	/** The search strategy the robot uses to find its target */
	public SearchStrategy strategy;
	
	/** Size of a grid cell, simply stored for drawing */
	private int _cellSize;

	private static final Color[] _colors
		= {Color.red, Color.blue, Color.green, Color.orange};
	
	/**
	 * Sets the ID of the robot. The robots sets its animations, colors
	 * and target positions accordingly. 
	 * This is not meant to be used manually, the Grid class will set the ID.
	 */	
	public void SetID(int newid) {
		_id = newid;
		if (_id > -1) {
			_color = _colors[_id % 4];
		}
	}
	
	/**
	 * @return ID of the robot.
	 */
	public int GetID() {
		return _id;
	}
	
	/**
	 * @return Color of the robot
	 */
	public Color GetColor() {
		return _color;
	}
	
	/** 
	 * Set the Robot width and height in pixels.
	 * Since the drawing area for a cell is always square, 
	 * only one parameter is needed.
	 * 
	 * This methods simply updates the scaling transformations
	 * the robot uses to draw its sprites at the correct size 
	 * 
	 * @param newsize Size of a cell in pixels
	 */
	public void SetCellSize(int newsize) {
		if (newsize != _cellSize) {
			_cellSize = newsize;
			//TODO: Affine Transformations
		}		
	}
	
	/**
	 * Draws the robot
	 * @param g2d Graphics2D-Object to draw the robot on
	 * @param gridBoundingBox bounding box of the grid on the g2d object
	 */
	public void DrawRobot(Graphics2D g2d, Rectangle gridBoundingBox) {
		if (x == NONE || y == NONE) return;
		g2d.setColor(_color);
		float posX = (nextX - x) * position;		
		float posY = (nextY - y) * position;
		float drawX = gridBoundingBox.x + (x + posX) * _cellSize;
		float drawY = gridBoundingBox.y + (y + posY) * _cellSize;
		g2d.fillArc((int)(drawX+_cellSize*0.2f), (int)(drawY+_cellSize*0.2f),(int)(_cellSize*0.6f),(int)(_cellSize*0.6f), 0, 360);
	}
	

	/**
	 * Draw the robots start point, the position in the cell depends on its ID.
	 * @param g2d Graphics2D-Object to draw the robot on
	 * @param gridBoundingBox bounding box of the grid on the g2d object
	 */
	public void DrawStart(Graphics2D g2d, Rectangle gridBoundingBox) {
		if (startX == NONE || startY == NONE) return;
		g2d.setColor(_color);
		int drawX = gridBoundingBox.x + startX * _cellSize + (_id % 2) * (_cellSize/2);
		int drawY = gridBoundingBox.y + startY * _cellSize;
		if (_id % 4 > 1) drawY += _cellSize/2;
		g2d.fillArc(drawX, drawY, _cellSize/2, _cellSize/2, 0, 360);
	}
	
	
	/**
	 * Draw the robots target, the position in the cell depends on its ID.
	 * @param g2d Graphics2D-Object to draw the robot on
	 * @param gridBoundingBox bounding box of the grid on the g2d object
	 */
	public void DrawTarget(Graphics2D g2d, Rectangle gridBoundingBox) {
		if (targetX == NONE || targetY == NONE) return;
		g2d.setColor(_color);
		int drawX = gridBoundingBox.x + targetX * _cellSize + (_id % 2) * (_cellSize/2);
		int drawY = gridBoundingBox.y + targetY * _cellSize;
		if (_id % 4 > 1) drawY += _cellSize/2;
		g2d.drawLine(drawX, drawY, drawX+_cellSize/2, drawY+_cellSize/2);
		g2d.drawLine(drawX+_cellSize/2, drawY, drawX, drawY+_cellSize/2);
	}
}
