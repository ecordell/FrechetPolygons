package anja.GridEditor;

import java.awt.*;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.*;

//imports all fields of Directions so that these 
//identifiers can be used directly withtout
//"Directions." in front.
import static anja.GridEditor.Directions.*;

/** This class represents the actual core of the GridEditor.
 * It represents the actual grid/maze and does the actual editing
 * and drawing. The animation is controlled by the GridEditor class.
 */

public class Grid extends JPanel implements MouseListener, MouseMotionListener{
	
	/** the size of the maze in cells per row and column
	 * the maze is always a square and the numbers of cells per row
	 * and column has to be uneven always (for easier resizing,
	 * if the size is increased the actual maze can be put into the
	 * center of the new maze
	 */
	private int _size = 5;
	
	/** Is set to true if the grid size changes to renew the grid image
	 * on the next repaint */
	private boolean _sizeChanged = false;
	
	/** Array with grid cells */
	public Cell[][] _cells;
	
	/** x-coord of highlighted cell, is NONE (-1) when no cell is highlighted*/
    int _highlightedCellX = -1; 
	/** y-coord of highlighted cell, is NONE (-1) when no cell is highlighted*/
    int _highlightedCellY = -1;
    
	/** direction of highlighted wall, is NONE (-1) when no wall is highlighted*/
    int _highlightedWall = -1;
	/** x-coord of highlighted wall, is NONE (-1) when no wall is highlighted*/
    int _highlightedWallX = -1;
	/** y-coord of highlighted wall, is NONE (-1) when no wall is highlighted*/
    int _highlightedWallY = -1;

    /** if true, mouse events do not get processed
     * this is used when animation starts or stops */
	boolean _mouseDisabled = false;
	
	/** Backbuffer */
    private BufferedImage _backBufferImage; // the backbuffer, this image is created new every time the window size changes
    
    /** The image of the actual maze. This is buffered and only redrawn if a wall
     *  or the windows dimensions change to speed up animation.
     *  The mazeimage is copied to the backbuffer for every frame.
     */
    private BufferedImage _gridImage;    

    /** actual dimension of the backbuffer image */
    private Dimension _dim; // actual dimension of the backbuffer image
    
    //line strokes for walls, cell borders without walls and highlighted walls
    private float[] _walldash = {2.0f,5.0f};    
    private Stroke _nonWall = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL,0,_walldash, 0);
    private Stroke _highlight = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL,0,new float[] {1f, 2f}, 0);
    private Stroke _wall = new BasicStroke(3.0f);
    
    /** the size of a cell in pixels, updated when window size changes, used for drawing */
    int _cellSize = 0;
    
    /** The bounding box on the grid in pixels, denoting its position in the image */
    Rectangle _gridBoundingBox;
    
    /** A Vector containing all robots on the grid */
    private Vector<Robot> _robots = new Vector<Robot>();
    
    /** The Robot MouseEvents are executed for */
    private Robot _activeRobot;
    
    /** Used Robot-IDs */
    private boolean _usedIDs[] = new boolean[4];

    
	public Grid() {
		_cells = new Cell[_size][_size];
		for (int i=0;i<_size;i++)
			for (int j=0;j<_size;j++)
				_cells[i][j] = new Cell();
		
		for (int i=0;i<_size;i++) {
			_cells[i][0].walls[NORTH] = true;
			_cells[i][_size-1].walls[SOUTH] = true;
			_cells[0][i].walls[WEST] = true;
			_cells[_size-1][i].walls[EAST] = true;
		}
		
		addMouseListener(this);
        addMouseMotionListener(this);
	}
	
	
	public void SetWall(int x, int y, int dir) {
		switch(dir) {
		case NORTH:
			if (y>0) _cells[x][y-1].walls[SOUTH] = true; break;
		case EAST:
			if (x<_size-1) _cells[x+1][y].walls[WEST] = true; break;
		case SOUTH:
			if (y<_size-1) _cells[x][y+1].walls[NORTH] = true; break;
		case WEST:
			if (x>0) _cells[x-1][y].walls[WEST] = true;			
		}
		_cells[x][y].walls[dir] = true;		
	}
	
	
	public void RemoveWall(int x, int y, int dir) {
		switch(dir) {
		case NORTH:
			if (y == 0) return;
		case EAST:
			if (x == _size-1) return;
		case SOUTH:
			if (y == _size-1) return;
		case WEST:
			if (x == 0) return;
		}
		switch(dir) {
		case NORTH:
			if (y>0) _cells[x][y-1].walls[SOUTH] = false; break;
		case EAST:
			if (x<_size-1) _cells[x+1][y].walls[WEST] = false; break;
		case SOUTH:
			if (y<_size-1) _cells[x][y+1].walls[NORTH] = false; break;
		case WEST:
			if (x>0) _cells[x-1][y].walls[WEST] = false;			
		}
		_cells[x][y].walls[dir] = false;		
		
	}
	
	/**
	 * Changes the size of the grid, the size is the nr of
	 * rows and columns in the grid. It is always changed to an
	 * uneven number.
	 * If the grid size is increased empty columns and rows are added
	 * around the grid. If the grid size is decreased outer rows and
	 * columns will be removed.
	 * @param newsize The new size of the grid.
	 */
	public void SetGridSize(int newsize) {
		if (newsize < 3) newsize = 3;
		if (newsize % 2 == 0) newsize -= 1;		
		Cell[][] cells = new Cell[newsize][newsize];
		//nr of emptycells to each side of the old grid
		//when its placed into the center of the new grid.
        // <0 when grid is shrinked
		int emptycells = (newsize - _size)/2;
		
		//remove outer walls of old grid
		for (int i=0;i<_size;i++) {
			_cells[i][0].walls[NORTH] = false;
			_cells[i][_size-1].walls[SOUTH] = false;
			_cells[0][i].walls[WEST] = false;
			_cells[_size-1][i].walls[EAST] = false;
		}
		
		//copy old grid into the center of the new one,
		//create new empty outer cells if size is increased
		for (int i=0; i<newsize; i++) {
			for (int j=0; j<newsize; j++) {
				if (i>=emptycells && j>=emptycells && i<newsize-emptycells && j<newsize-emptycells) {
					cells[i][j] = _cells[i-emptycells][j-emptycells];					
				}
				else {
					cells[i][j] = new Cell();
				}
			}
		}
		//add outer walls to the border cells
		for (int i=0;i<newsize;i++) {
			cells[i][0].walls[NORTH] = true;
			cells[i][newsize-1].walls[SOUTH] = true;
			cells[0][i].walls[WEST] = true;
			cells[newsize-1][i].walls[EAST] = true;
		}
		
		
		_cells = cells;
		_size = newsize;
		_sizeChanged = true;
		
		//after changing grid size is finished update robot positions
		Iterator<Robot> ir = _robots.iterator();
		while (ir.hasNext()) {
			Robot r = ir.next();
			if (r.startX != NONE && r.startY != NONE) {
				r.startX += emptycells;
				r.startY += emptycells;
				if (!IsOnGrid(r.startX,r.startY)) {
					r.startX = NONE;
					r.startY = NONE;
				}
			}
			//robot is set to its starting point when grid size changes			
			r.x = r.startX;
			r.y = r.startY;
			if (r.targetX != NONE && r.targetY != NONE) {
				r.targetX += emptycells;
				r.targetY += emptycells;
				if (!IsOnGrid(r.targetX, r.targetY)) {
					r.targetX = NONE;
					r.targetY = NONE;
				}
			}
		}
		
		repaint();		
	}
	
	public int GetGridSize() {
		return _size;
	}

	public boolean AddRobot(Robot robot) {
		if (_robots.contains(robot)) return false;
		for (int i = 0; i < _usedIDs.length; i++) {
			if (!_usedIDs[i]) {
				robot.SetID(i);
				robot.SetCellSize(_cellSize);
				_usedIDs[i] = true;
				_robots.add(robot);
				repaint();
				return true;
			}
		}
		return false;
	}
	
	public void RemoveRobot(Robot robot) {
		if (_robots.contains(robot)) {
			_usedIDs[robot.GetID()] = false;
			_robots.remove(robot);
		}		
		repaint();
	}
	
	public void SetActiveRobot(Robot robot) {
		if (_robots.contains(robot))
			_activeRobot = robot;
	}
	
	public Vector<Robot> GetRobots() {
		return _robots;
	}
	
	/** Tests if given coords are on the grid
	 * 
	 * @param x x-coord to test
	 * @param y y coord to test
	 * @return True if on grid else false
	 */
	public boolean IsOnGrid(int x, int y) {
		if (x<0 || y<0 || x>=_size || y>=_size) return false;
		return true;
	}
	
	//-----------------------------------------
	//drawing methods
	
    public void drawHighlights(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.setStroke(_highlight);
        if (_highlightedCellX > -1) {
            g2d.drawRect(_gridBoundingBox.x+(int)((_highlightedCellX+0.25f)*_cellSize),
                    _gridBoundingBox.y+(int)((_highlightedCellY+0.25f)*_cellSize),
                    _cellSize/2,
                    _cellSize/2);
        }
        if (_highlightedWall > -1) {
            switch(_highlightedWall) {
            case 0:
                g2d.drawRect(_gridBoundingBox.x+_highlightedWallX*_cellSize-_cellSize/10,
                             _gridBoundingBox.y+_highlightedWallY*_cellSize-_cellSize/10,
                             _cellSize+_cellSize/5, _cellSize/5);
                break; 
            case 1:
                g2d.drawRect(_gridBoundingBox.x+(_highlightedWallX+1)*_cellSize-_cellSize/10,
                             _gridBoundingBox.y+_highlightedWallY*_cellSize-_cellSize/10,
                            _cellSize/5, _cellSize+_cellSize/5);
                break;
            case 2:
                g2d.drawRect(_gridBoundingBox.x+_highlightedWallX*_cellSize-_cellSize/10,
                             _gridBoundingBox.y+(_highlightedWallY+1)*_cellSize-_cellSize/10,
                             _cellSize+_cellSize/5, _cellSize/5);
                break;
            case 3:
                g2d.drawRect(_gridBoundingBox.x+_highlightedWallX*_cellSize-_cellSize/10,
                             _gridBoundingBox.y+_highlightedWallY*_cellSize-_cellSize/10,
                             _cellSize/5, _cellSize+_cellSize/5);
                break;
           }
        }        
    }

	/*
	 * Draws the grid, highlights and robots onto the screen.
	 * The grid is simply copied from _gridImage, this image is only changed
	 * when the window size or a wall status changes. This severely speeds
	 * up redrawing.
	 * After the grid is copied to the backbuffer, the highlights and robots
	 * are drawn on top.
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g)
	{
	    Dimension newdim = this.getSize();
	    
	    if (_dim == null || newdim.width != _dim.width || newdim.height != _dim.height || _sizeChanged) {
	        _sizeChanged = false;
	        _backBufferImage = new BufferedImage(newdim.width, newdim.height, BufferedImage.TYPE_INT_RGB);
	        _gridImage = new BufferedImage(newdim.width, newdim.height, BufferedImage.TYPE_INT_RGB);
	        _dim = newdim;            
	        int maxsize = Math.min(_dim.height, _dim.width) - 10;
	        _gridBoundingBox = new Rectangle((_dim.width-maxsize)/2, (_dim.height-maxsize)/2, maxsize, maxsize);
	        _cellSize = maxsize/_size;
	        Iterator<Robot> ir = _robots.iterator();
	        while (ir.hasNext()) {
	        	ir.next().SetCellSize(_cellSize);
	        }
	        //createImages();
	        drawGrid();
	    }
	    Graphics2D g2d = _backBufferImage.createGraphics();
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2d.setColor(Color.WHITE);
	    g2d.fillRect(0,0,_dim.width, _dim.height);
	    g2d.setColor(Color.BLACK);        
	    g2d.drawImage(_gridImage,0,0,this);
	    /*drawDirections(g2d);
	    drawEndPoints(g2d);
	    drawMouse(g2d);*/
	    
	    //draw Robots
	    Iterator<Robot> ir = _robots.iterator();
	    while (ir.hasNext()) {
	    	Robot r = ir.next();
	    	r.DrawRobot(g2d, _gridBoundingBox);
	    	r.DrawTarget(g2d, _gridBoundingBox);
	    	r.DrawStart(g2d, _gridBoundingBox);
	    }
	    
	    drawHighlights(g2d);
	    g2d.dispose();

	    g.drawImage(_backBufferImage, 0, 0, this);
    }  

	/*
	 * Draws the grid onto the _gridImage.
	 * This is only called, when a wall or the window size changes to
	 * speed up redrawing.
	 */
    private void drawGrid() {
        Graphics2D g2d = _gridImage.createGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0,0,this.getSize().width, this.getSize().height);
        g2d.setColor(Color.black);
        //draw each cell
        //since the common wall of two neighboured cells are identical, only 
        //the top and left walls of each cells need to be drawn
        for (int i = 0;i<_size;i++) {
            for (int j=0;j<_size;j++) {
                //draw top wall
                if (_cells[i][j].walls[NORTH] || j == 0) g2d.setStroke(_wall);
                else g2d.setStroke(_nonWall);
                g2d.drawLine(_gridBoundingBox.x+i*_cellSize,_gridBoundingBox.y+j*_cellSize, _gridBoundingBox.x+(i+1)*_cellSize, _gridBoundingBox.y+j*_cellSize);
                
                //draw left wall
                if (_cells[i][j].walls[WEST] || i == 0) g2d.setStroke(_wall);
                else g2d.setStroke(_nonWall);
                g2d.drawLine(_gridBoundingBox.x+i*_cellSize, _gridBoundingBox.y+j*_cellSize, _gridBoundingBox.x+i*_cellSize, _gridBoundingBox.y+(j+1)*_cellSize);                
            }
        }
        //draw the right and bottom border of the maze
        g2d.setStroke(_wall);
        for (int i = 0; i < _size; i++) {
	        //draw bottom wall
            g2d.drawLine(_gridBoundingBox.x, _gridBoundingBox.y+_size*_cellSize, _gridBoundingBox.x+_size*_cellSize, _gridBoundingBox.y+_size*_cellSize);
            //draw right wall
            g2d.drawLine(_gridBoundingBox.x+_size*_cellSize, _gridBoundingBox.y, _gridBoundingBox.x+_size*_cellSize, _gridBoundingBox.y+_size*_cellSize);
        }
        
    }
    
    //----------------------------------------------------
    //Mouse movement functions and cell-/wall-highlighting
    
    /* Clears the current highlighted cell/wall
     * Use by the editor, when animation starts
     */
    public void clearHighlights() {
    	_highlightedCellX = NONE;
    	_highlightedCellY = NONE;
    	_highlightedWall = NONE;
    	_highlightedWallX = NONE;
    	_highlightedWallY = NONE;
    }
    
    /*
     * Calculates which cell the mouse cursor hovers over
     * and saves its coords in _highlightedCellX/Y, coords are -1/-1
     * if no cell is highlighted because mouse is over a wall or
     * outside the grid.
     * @param: The last MouseEvent with mouse coordinates (passed from MouseMoved)
     * @return: true if the highlighted cell changed, else false
     */
    private boolean calculateHighlightedCell(MouseEvent me) {
        if (_cellSize == 0) return false;
        int oldX = _highlightedCellX;
        int oldY = _highlightedCellY;
        _highlightedCellX = -1;
        _highlightedCellY = -1;
        //translate event coords by grid coords to get coords relative to the
        //grid onscreen, then divide by cell size to get the coords of the cell
        //in the _cells-array 
        int cellx = (me.getX()-_gridBoundingBox.x)/_cellSize; 
        int celly = (me.getY()-_gridBoundingBox.y)/_cellSize; 
        if (cellx < _size && celly < _size) {
        	//test if event is inside the cell, event has to be
        	//20% of cell size away from each cell wall to be counted as inside
        	//else a wall is highlighted
            if (me.getX()<=_gridBoundingBox.x+(cellx+0.8f)*_cellSize
                && me.getX()>=_gridBoundingBox.x+(cellx+0.2f)*_cellSize
                && me.getY()>=_gridBoundingBox.y+(celly+0.2f)*_cellSize
                && me.getY()<=_gridBoundingBox.y+(celly+0.8f)*_cellSize) {
                _highlightedCellX = cellx;
                _highlightedCellY = celly;
            }
        }
        if (oldX != _highlightedCellX || oldY != _highlightedCellY)
        	return true;
        else return false;
       
    }
 
    /*
     * Calculates which wall the mouse cursor hovers over
     * and saves the coords of its cell in _highlightedWallX/Y, coords are -1/-1
     * if no cell is highlighted because mouse is over a wall or
     * outside the grid, direction of the wall is saved in _highlightedWall.
     * If the cursor has moved between cells and a wall of the old cell was
     * highlighted, the new highlight is the same wall of the new cell. 
     * This way by dragging the mouse a longer horizontal or vertical wall
     * can be drawn without having to click each individually.  
     * @param: The last MouseEvent with mouse coordinates (passed from MouseMoved)
     * @return: true if the highlighted cell changed, else false
     */
    private boolean calculateHighlightedWall(MouseEvent me) {
        if (_cellSize == 0) return false;
        int oldhigh = _highlightedWall;
        int oldX = _highlightedWallX;
        int oldY = _highlightedWallY;
        _highlightedWall = -1;
        _highlightedWallX = -1;
        _highlightedWallY = -1;
        
        //translate event coords by grid coords to get coords relative to the
        //grid onscreen, then divide by cell size to get the coords of the cell
        //in the _cells-array        
        int cellx = (me.getX()-_gridBoundingBox.x)/_cellSize;
        int celly = (me.getY()-_gridBoundingBox.y)/_cellSize;
        if (cellx < _size && celly < _size && me.getX()>=_gridBoundingBox.x && me.getY() >= _gridBoundingBox.y ) {
        	//test all four walls to find the one the mouse is in range of,
        	//mouse has to be not farther away than 20% of cell size from a wall
        	
        	/* example: left/west wall:
        	 * 1. test if x-coord of event is < x-coord of cell + 0.2 * cell size, abort if not
        	 * 2. test if previously highlighted wall was a west wall
        	 *    (then it was the west wall of the cell above or below)
        	 *    or an east wall (then it was the east well of the cell to the left)
        	 *    or if no wall was highlighted
        	 *    => mouse is over west wall
        	 * 3. test if y-coord is farther away from the top and bottom wall
        	 *    than 0.2 * cell size (inside the cell) which means that the mouse
        	 *    is not over the region where several walls meet and only the west wall
        	 *    can be highlighted.
        	 *    => mouse is over west wall 
        	 * 
        	 * the test for the east wall is the same, only the x-coord differs of course
        	 */
        
            if (cellx > 0 && me.getX()<_gridBoundingBox.x+(cellx+0.2f)*_cellSize
                    && (oldhigh == WEST || oldhigh == EAST || oldhigh == NONE
                            || (me.getY()<=_gridBoundingBox.y+(celly+0.8f)*_cellSize
                                    && me.getY()>=_gridBoundingBox.y+(celly+0.2f)*_cellSize))) {
                _highlightedWall = WEST;
                _highlightedWallX = cellx;
                _highlightedWallY = celly;
            }
            
            if (cellx < _size-1 && me.getX()>_gridBoundingBox.x+(cellx+0.8f)*_cellSize
                    && (oldhigh == WEST || oldhigh == EAST || oldhigh == NONE
                            || (me.getY()<=_gridBoundingBox.y+(celly+0.8f)*_cellSize
                                    && me.getY()>=_gridBoundingBox.y+(celly+0.2f)*_cellSize))) {
                _highlightedWall = EAST;
                _highlightedWallX = cellx;
                _highlightedWallY = celly;
            }
            
        	/* 2. example: bottom/south wall:
        	 * 1. test if y-coord of event is > y-coord of cell bottom - 0.2 * cell size, abort if not
        	 * 2. test if previously highlighted wall was a south wall
        	 *    (then it was the south wall of the cell to the left or right)
        	 *    or an north wall (then it was the north well of the cell below)
        	 *    or if no wall was highlighted
        	 *    => mouse is over south wall
        	 * 3. test if x-coord is farther away from the left and right wall
        	 *    than 0.2 * cell size (inside the cell) which means that the mouse
        	 *    is not over the region where several walls meet and only the south wall
        	 *    can be highlighted.
        	 *    => mouse is over south wall 
        	 * 
        	 * the test for the north wall is the same, only the y-coord differs of course
        	 */
            
            if (celly < _size-1 && me.getY()>_gridBoundingBox.y+(celly+0.8f)*_cellSize
                && (oldhigh == SOUTH || oldhigh == NORTH || oldhigh == NONE
                        || (me.getX()<=_gridBoundingBox.x+(cellx+0.8f)*_cellSize
                                && me.getX()>=_gridBoundingBox.x+(cellx+0.2f)*_cellSize))) {
                _highlightedWall = SOUTH;
                _highlightedWallX = cellx;
                _highlightedWallY = celly;
            }
            if (celly>0 && me.getY()<_gridBoundingBox.y+(celly+0.2f)*_cellSize
                && (oldhigh == SOUTH || oldhigh == NORTH || oldhigh == NONE
                        || (me.getX()<=_gridBoundingBox.x+(cellx+0.8f)*_cellSize
                                && me.getX()>=_gridBoundingBox.x+(cellx+0.2f)*_cellSize))) {
                _highlightedWall = NORTH;
                _highlightedWallX = cellx;
                _highlightedWallY = celly;
            }
            /* What happens if the mouse is over the region where several walls meet?
             * If no wall was highlighted before either the horizontal or vertical wall
             * at that region of the cell is highlighted, it does not matter which it is
             * in the end.
             * If a vertical wall was highlighted before, the vertical wall at that region
             * is highlighted, same with the horizontal wall.
             * This ensures, that if the mouse is dragged vertically, only the vertical walls
             * the mouse is dragged along are changed and not horizontal walls adjacent to them
             * (and vice versa).
             */
        }
        if (oldhigh != _highlightedWall
        	|| oldX != _highlightedWallX || oldY != _highlightedWallY)
        	return true;
        return false;
    }
    
    
    public void mouseMoved(MouseEvent me)
    {
    	if (_dim == null || _mouseDisabled) return;
        boolean updated = false;
        updated = calculateHighlightedCell(me);
        updated = calculateHighlightedWall(me) || updated;
        if (updated) repaint();
    }
    
    //----------------------------------------
    //Mouse click methods and walls/cells editing
    
    /* Processes the click of the mouse button on a wall
     * @param button: the mouse button pressed
     */
    private void doWallClick(int button) {
        if (_highlightedWall > NONE) {
            _cells[_highlightedWallX][_highlightedWallY].walls[_highlightedWall] = (button==1);
            //set walls of cells adjacent to the highlight one accordingly
            switch(_highlightedWall) {
            case NORTH:
                if (_highlightedWallY > 0) _cells[_highlightedWallX][_highlightedWallY-1].walls[SOUTH] = (button==1);
                break;
            case EAST:
                if (_highlightedWallX < _size-1) _cells[_highlightedWallX+1][_highlightedWallY].walls[WEST] = (button==1);
                break;
            case SOUTH:
                if (_highlightedWallY < _size-1) _cells[_highlightedWallX][_highlightedWallY+1].walls[NORTH] = (button==1);
                break;
            case WEST:
                if (_highlightedWallX > 0) _cells[_highlightedWallX-1][_highlightedWallY].walls[EAST] = (button==1);
                break;
            }
            drawGrid(); //a wall is changed, so the grid image has to be updated 
        }        
    }

    //saves the mouse button of the pressed event
    //in case the mouse is dragged, the drag event
    //does not give a mouse button
    
    int _dragButton = 0;
    
    public void mousePressed(MouseEvent me)
    {
    	if (_dim == null || _mouseDisabled) return;
    	_dragButton = me.getButton();
        doWallClick(me.getButton());
        //if a cell is highlighted, a robot or its target is set
        if (_highlightedCellX != NONE && _highlightedCellY != NONE) {
        	if (_activeRobot != null) {
        		if (me.getButton() == MouseEvent.BUTTON1) {
        			_activeRobot.startX = _highlightedCellX;
        			_activeRobot.startY = _highlightedCellY;
        			_activeRobot.x = _highlightedCellX;
        			_activeRobot.y = _highlightedCellY;
        		}
        		else {
        			_activeRobot.targetX = _highlightedCellX;
        			_activeRobot.targetY = _highlightedCellY;
        		}
        	}
        }
        repaint();
    }
    
    public void mouseDragged(MouseEvent me)
    {
    	if (_dim == null || _mouseDisabled) return;
        boolean updated = calculateHighlightedWall(me);
        if (updated) {
        	doWallClick(_dragButton);
        	repaint();
        }
    }
    
    public void enableMouse() {
    	_mouseDisabled = false;
    }
    
    public void disableMouse() {
    	_mouseDisabled = true;
    }
    
    public void mouseReleased(MouseEvent me) {}
    public void mouseClicked(MouseEvent me) {}
    public void mouseEntered(MouseEvent me) {}
    public void mouseExited(MouseEvent me) {}
}
