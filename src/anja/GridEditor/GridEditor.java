package anja.GridEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import static anja.GridEditor.Directions.*;

public class GridEditor extends JPanel
		implements ActionListener, WindowListener, Runnable
    {
	
	private Grid _grid;
	private JButton _increaseButton;
	private JButton _decreaseButton;
	private JButton _runButton;
	
	private boolean _animationActive = false;
	
	private Vector _searchStrategies;
	
    public GridEditor()
    {
        this.setLayout(new BorderLayout());
        _grid = new Grid();
        this.add(_grid, BorderLayout.CENTER);
        
        JPanel ap = new JPanel();
        ap.setBackground(Color.WHITE);
        this.add(ap, BorderLayout.NORTH);
        JPanel row1 = new JPanel();
        row1.setBackground(Color.WHITE);
        JPanel row2 = new JPanel();
        row2.setBackground(Color.WHITE);
        ap.setLayout(new BoxLayout(ap, BoxLayout.Y_AXIS));
        ap.add(row1);
        ap.add(row2);
        row1.setLayout(new FlowLayout());
        row2.setLayout(new FlowLayout());
        
        _increaseButton = new JButton("+");
        row1.add(_increaseButton);
        _increaseButton.addActionListener(this);
        _decreaseButton = new JButton("-");
        row1.add(_decreaseButton);
        _decreaseButton.addActionListener(this);
        _runButton = new JButton("Run");
        row1.add(_runButton);
        _runButton.addActionListener(this);
        
        /*row2.add(_result);*/
        _searchStrategies = new Vector();
        try {
        	Class c = Class.forName("anja.GridEditor.ShannonsMouseStrategy");
        	_searchStrategies.add(c);
        }
        catch (Exception e) {}
        
    }
    
    /**
     * Adds a robot the the editor. Only needs to be used if the built in
     * robot managing gui is not used.
     * @param robot The robot to be added
     */
    public void AddRobot(Robot robot) {
    	if (_animationActive) return;
    	_grid.AddRobot(robot);
    }
    
    /**
     * Removes a robot from the editor. Only needs to be used if the built in
     * robot managing gui is not used.
     * @param robot The robot to be removed
     */
    public void RemoveRobot(Robot robot) {
    	if (_animationActive) return;
    	_grid.RemoveRobot(robot);
    }
    
    /**
     * Sets a specific robot a active. Following mouse events will be
     * executed for that robot. Only needs to be used if the built in
     * robot managing gui is not used.
     * @param robot The robot to be made active
     */
    public void SetActiveRobot(Robot robot) {
    	if (_animationActive) return;
    	_grid.SetActiveRobot(robot);
    }
    
    /**
     * Adds a new search strategy class to the robot managing gui.
     */
    public void AddStrategy(String strategy) {
    	try {
    		Class c = Class.forName(strategy);
    		_searchStrategies.add(c);
    	}
    	catch (Exception e) {}
    }
    
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == _increaseButton) {
    		_grid.SetGridSize(_grid.GetGridSize()+2);
    	}
    	else if (e.getSource() == _decreaseButton) {
    		_grid.SetGridSize(_grid.GetGridSize()-2);
    	}
    	else if (e.getSource() == _runButton) {
    		if (!_animationActive) {
    			_animationActive = true;
    			_runButton.setText("Stop");
    			Thread t = new Thread(this);
    			t.start();
    		}
    		else {
    			_animationActive = false;    			
    		}
    	}
    }
    
    private void StopAnimation() {
    	_animationActive = false;
    	_grid.enableMouse();
    	_runButton.setText("Run");
    }
    
    
    public void run() {
    	Vector<Robot> robots = _grid.GetRobots();
    	if (robots == null || robots.size() == 0) {
    		StopAnimation();
    		return;
    	}
    	Iterator<Robot> ir = robots.iterator();
    	while (ir.hasNext()) {
    		Robot r = ir.next();
    		r.x = r.startX;
    		r.y = r.startY;
    		r.lastX = r.x;
    		r.lastY = r.y;
    		r.nextX = r.x;
    		r.nextY = r.y;
    		r.direction = NONE;
    		r.position = 0.0f;
    		if (!_grid.IsOnGrid(r.x,r.y)
    				|| !_grid.IsOnGrid(r.targetX, r.targetY)
    				|| r.strategy == null) {
    			StopAnimation();
    			return;
    		}    		
    	}
    	ir = robots.iterator();
    	while (ir.hasNext()) {
    		Robot r = ir.next();
    		r.strategy.start(r, _grid);
    	}
    	_grid.disableMouse();
    	boolean finished = true;
    	while(_animationActive) {
    		finished = true;
    		ir = robots.iterator();    		
    		while (ir.hasNext()) {
    			Robot r = ir.next();
    			if (r.position >= 1.0f) r.position = 0.0f;
    			if (r.position == 0.0f && (r.x != r.targetX || r.y != r.targetY)) {
    				r.x = r.nextX;
    				r.y = r.nextY;
        			int dir = r.strategy.getDirection(r, _grid);
        			switch (dir) {
        			case NORTH:
        				r.nextY = r.y-1;
        				r.nextX = r.x;
        				break;
        			case EAST:
        				r.nextX = r.x+1;
        				r.nextY = r.y;
        				break;
        			case SOUTH:
        				r.nextY = r.y+1;
        				r.nextX = r.x;
        				break;
        			case WEST:
        				r.nextX = r.x-1;
        				r.nextY = r.y;
        			}
        			r.direction = dir;
    			}
    			if (r.x != r.targetX || r.y != r.targetY) {
    				finished = false;
    				r.position += 0.1f;
    			}
    		}
    		if (finished) _animationActive = false;
    		try {
    			repaint();
    			Thread.sleep(60);    			
    		}
    		catch (Exception e) {
    			_animationActive = false;
    		}
    	}
    	ir = robots.iterator();
    	while (ir.hasNext()) {
    		Robot r = ir.next();
    		r.strategy.stop();
    		r.position = 0.0f;
    	}
    	StopAnimation();
    }
    
    public void windowDeactivated(WindowEvent e) {}    
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
    	StopAnimation();
        System.exit(0);
    }
    public void windowClosed(WindowEvent e) {}

}
