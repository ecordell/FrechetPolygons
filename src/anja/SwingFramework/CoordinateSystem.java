/*
 * Created on Nov 23, 2004
 *
 * CoordinateSystem.java
 */
package anja.SwingFramework;

import java.util.*;
import java.text.*;

import java.net.URL;

import java.awt.Insets;
import java.awt.FontMetrics;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import anja.geom.*;
import anja.util.MathConstants;

import org.jdom.*;

/**
 * The <code>CoordinateSystem</code> class defines a virtual
 * cartesian coordinate system that can be used in conjunction with
 * JSimpleDisplayPanel or similar classes.
 * 
 * <br><br>The coordinate system parameters can be controlled via a
 * system of menus. This method may also notify multiple action
 * listeners about the selected menu item - the resulting action event
 * contains one one of the constants CMD_... which identifies the
 * selected menu item. (see internal documentation)
 * 
 * <br><br>This class provides built-in mouse / keyboard event
 * handlers which allow easy modifications to the coordinate system.
 * See method descriptions for details.
 * 
 * @version 0.9 27.08.2004
 * @author Ibragim Kuliyev, portions by Sascha Ternes
 * 
 * <p>TODO: Add missing method documentation 
 * <br>TODO: Complete and
 * fix the UI glue <br>
 * 
 * TODO: "dots" grid mode <br>
 * 
 * TODO: Do we need additional constructors ??<br>
 * 
 * <p>TODO: This is a bigass todo: will we ever have any use for
 * non-uniformly scaled coordinate systems ????? If the answer is NO,
 * this could significantly simplify the code in this class! <br>
 * 
 * <p>TODO: The primary performance bottleneck in this class is the
 * drawing code, more precisely, the actual drawing commands issued
 * through the graphics context. This cannot be rectified even through
 * double-buffering. The next release of Java seems to support
 * hardware acceleration with OpenGL, so it might help to slowly move
 * the project to Java 1.5. <br>
 * 
 * <p>FIXME: There's some weird bug in the coordinate
 * transformations, which sometimes causes the coordinates under the
 * mouse cursor in JSimpleDisplayPanel to be incorrectly transformed
 * for a fleeting moment, then everything returns to normal... <br>
 * 
 * <p>FIXME: Possible quirks in the code that draws the values for
 * large ticks. <br>
 *  
 */

public class CoordinateSystem implements ActionListener, 
                                         KeyListener,
                                         ChangeListener,
                                         MathConstants
{
    
    //*************************************************************************
    //				  Public constants
    //*************************************************************************
	
    public static final String XML_SETTINGS_NAME = 
    						"Coordinate_system_settings";
    
    //---------------------------- mode switches ------------------------------
    
    public static final int NO_GRID  	 	= 0;
    public static final int COARSE_GRID 	= 1;
    public static final int FINE_GRID  	 	= 2;
    
    public static final int GRID_MODE_DOTS      = 3;
    public static final int GRID_MODE_LINES     = 4;
    
    public static final int GRID_TYPE_RECT      = 5;
    public static final int GRID_TYPE_POLAR     = 6;
    
    public static final int NO_ARROWS 		= 7;
    public static final int POSITIVE_ARROWS     = 8;
    public static final int BOTH_ARROWS 	= 9;
    
    public static final int NO_AXES 		= 10;
    public static final int HORIZONTAL_AXIS     = 11;
    public static final int VERTICAL_AXIS       = 12;
    public static final int BOTH_AXES 		= 13;
    
    // commands that are externally visible	
    public static final int CMD_ANGLE_PITCH     = 15;
	
    //*************************************************************************
    //				  Private constants
    //*************************************************************************
	
    //--------------------- XML input / output constants ----------------------
	
    private static final String XML_SYS_PROPERTIES   = "System_props";
    private static final String XML_GRID_PROPERTIES  = "Grid_props";
    
    private static final String XML_SYS_VISIBILITY   = "Show_system";
    private static final String XML_NO_DECORATIONS   = "No_decorations";
    private static final String XML_AXIS_COLOR 	     = "Axis_color";
    
    private static final String XML_ORIGIN_X	     = "Origin_x";
    private static final String XML_ORIGIN_Y	     = "Origin_y";
    
    private static final String XML_ZOOM_FACTOR      = "Zoom_factor";
    
    private static final String XML_GRID_COLOR       = "Grid_color";
    private static final String XML_GRID_VISIBILITY  = "Show_grid";	
    private static final String XML_GRID_TYPE  	     = "Grid_type";
    private static final String XML_TICK_PITCH	     = "Tick_pitch";
    private static final String XML_ANGLE_PITCH	     = "Angle_pitch";
    private static final String XML_GRID_SNAP	     = "Snap_to_grid";
	
    //------------------------- internal constants ----------------------------
    
    // roundoff bias for world-to-screen transformations
    private static final double PIXEL_BIAS = 0.25;
    
    // sorrounding border offset in pixels
    private static final int   BORDER_WIDTH = 0;
    
    // min/max number of ticks per unit of scale
    private static final int   MIN_TICKS_PER_UNIT = 1;
    private static final int   MAX_TICKS_PER_UNIT = 10;
    
    // min/max angle step values in degrees
    private static final int   MIN_ANGLE_STEP  = 5;
    private static final int   MAX_ANGLE_STEP  = 120;
    private static final int   ANGLE_INCREMENT = 15; // not used yet
    	
    // tick half-length in pixels
    private static final int SMALL_TICK_SIZE = 2;
    private static final int LARGE_TICK_SIZE = 5;
    		
    // arrow appearance
    private static final int ARROW_WIDTH  = 10;
    private static final int ARROW_HEIGHT = 5;
    
    //----------------------- UI command identifiers --------------------------
    
    private static final int CMD_ENABLE         = 1;
    private static final int CMD_NO_DECORATIONS = 2;
    private static final int CMD_RESET          = 3;
    
    private static final int CMD_TYPE_CARTESIAN = 4;
    private static final int CMD_TYPE_POLAR     = 5;
    
    private static final int CMD_GRID_COLOR     = 6;
    private static final int CMD_AXES_COLOR     = 7;
    
    private static final int CMD_GRID_NONE      = 8;
    private static final int CMD_GRID_COARSE    = 9;
    private static final int CMD_GRID_FINE      = 10;
    	
    private static final int CMD_MODE_DOTS      = 11;
    private static final int CMD_MODE_LINES     = 12;
    
    private static final int CMD_SNAP_TO_GRID   = 13;
    private static final int CMD_CENTER 	= 14;
    
    //private static final int CMD_ANGLE_PITCH  = 15;
				
    //*************************************************************************
    //				   Class variables
    //*************************************************************************
    		
    //*************************************************************************
    //			      Private instance variables
    //*************************************************************************
	
    private JSystemHub          m_Hub;
    private JSimpleDisplayPanel m_Display; // may be temporary...
    private Vector 	        m_ActionListeners;
    
    /* ------- attributes ------*/
    
    private Color               m_GridColor;
    private Color               m_SystemColor;
    
    private String              m_xAxisLabel;
    private String              m_yAxisLabel;
    
    private AffineTransform     m_affineXForm;	
    private Rectangle2D   	m_worldRectangle;
    	
    /* ------- flags -----------*/
    	
    private boolean             m_bVisible;
    
    private boolean             m_bDrawDecorations;
    private boolean             m_bDrawTicks;
    private boolean             m_bDrawLabels;
    
    private int                 m_iAxisType;
    private int                 m_iArrowType;
    private int                 m_iGridVisibility;
    private int                 m_iGridMode;
    private int                 m_iGridType;
    		
    private boolean             m_bSnapToGrid;
		
    /* --------- UI elements --------- */
    
    /* Please note that some UI elements are not explicitly 
     * declared as members variables so that they don't clutter 
     * up the code. Only those elements which might need to be later
     * modified by internal functions are exposed here.
     */
    
    private JMenu                m_uiMenu;
    private JMenu                m_uiGridTypeSubMenu;
    private JMenu                m_uiGridModeSubMenu;
    private JMenu                m_uiGridVisibilitySubMenu;
    	
    private JMenuItem            m_uiResetItem;
    private JMenuItem            m_uiCenterItem;
    
    private JMenuItem            m_uiGridColorItem;
    private JMenuItem            m_uiAxesColorItem;
    
    private JCheckBoxMenuItem    m_uiEnableBox;
    private JCheckBoxMenuItem    m_uiNoDecorBox;
    
    private JCheckBoxMenuItem    m_uiSnapBox;
    
    private JRadioButtonMenuItem m_uiGridNoneItem;
    private JRadioButtonMenuItem m_uiGridCoarseItem;
    private JRadioButtonMenuItem m_uiGridFineItem;
    
    private JRadioButtonMenuItem m_uiGridDotsItem;
    private JRadioButtonMenuItem m_uiGridLinesItem;
    
    private JRadioButtonMenuItem m_uiCartGridItem;
    private JRadioButtonMenuItem m_uiPolarGridItem;
    
    private JSpinner 		 m_uiTicksPerUnitItem;
    private JSpinner 		 m_uiAngleStepItem;
    
    private JToolBar m_uiToolBox;
    //TODO: Toolbox components 

    /* -------- various state variables ------*/
    		
    private Point		m_Origin; // coordinate origin in screen space
    private Point 		m_zoomPixel; // zoom center in screen space
    private Point2D.Double 	m_zoomPoint; // zoom center in world space
    private Point		m_pressedPoint; // previous position...
    private int			m_iMouseButton; // pressed button(s)
    	
    private int			m_iDisplayWidth;
    private int			m_iDisplayHeight;	
    private Insets		m_Border;
    
    private boolean 		m_bHitLeftEdge;
    private boolean 		m_bHitRightEdge;
    private boolean 		m_bHitTopEdge;
    private boolean		m_bHitBottomEdge;
    
    private int 		m_iTicksPerUnit; // number of ticks per unit
    private int			m_iAnglePitch; // polar grid angle pitch
    
    // scaling and tick parameters
    private double 		m_dSceneExtentsX;
    private double 		m_dSceneExtentsY;
    
    private double 		m_dPixelsPerX;
    private double		m_dPixelsPerY;
    private double		m_dCoarsePitchX;
    private double		m_dCoarsePitchY;
    private double		m_dFinePitchX;
    private double		m_dFinePitchY;
    
    // backup variables to reset the system to initial settings
    private double 		m_dInitialExtentsX;
    private double 		m_dInitialExtentsY;
    
    private double		m_dInitialPixelsPerX;
    private double		m_dInitialPixelsPerY;
    private double		m_dInitialCoarsePitchX;
    private double		m_dInitialCoarsePitchY;
    private double		m_dInitialFinePitchX;
    private double		m_dInitialFinePitchY;
			
	// various helper variables
    private double[] 		m_dSines;
    private double[] 		m_dCosines;
    
    private DecimalFormat 	m_Formatter;
    	
    //polygon coordinates for drawArrows()
    private int[]               _px;
    private int[]               _py;
	
    // start and end coordinates for ticks
    private double              _coarse_start_x;
    private double              _coarse_end_x;
    private double              _coarse_start_y;
    private double              _coarse_end_y;
    
    private double              _fine_start_x;
    private double              _fine_end_x;
    private double              _fine_start_y;
    private double              _fine_end_y;
	
    /* These variables are primarily intended for
     * providing grid parameters to outside components
     * via various get() methods.
     * 
     */
	
    // start and end coordinates for grid
    private double              _grid_start_x;
    private double              _grid_end_x;
    private double              _grid_start_y;
    private double              _grid_end_y;
    
    // max. polar grid radius
    private double              _polar_grid_radius;
    
    private Point2D.Double      _temp_point;
    	
    //*************************************************************************
    //				    Constructors
    //*************************************************************************
	
    /**
     * Creates a coordinate system with 'default' parameters, that is,
     * a visible Cartesian system, black axes, axes ends capped, light
     * blue grid, grid is in line mode and turned off by default,
     * origin is centered in the display component(*), 5 ticks per
     * unit of scale, and an initial broad pitch of 10 and fine pitch
     * of 1 units. Angle pitch is 5 degrees. Snap is off by default,
     * the axes are labeled "X" and "Y". <br><br><b>* </b>As soon as
     * the display panel initializes.
     * 
     * 
     * @param hub reference to the system hub
     */
    public CoordinateSystem(JSystemHub hub)
    {	
    	initVars();
    	m_Hub = hub;
    	
    	// initialize attributes to default values
    	m_bVisible    = true;
    	m_bDrawLabels = true;
    	m_bDrawTicks  = true;
    	m_bSnapToGrid = false;
        
        m_bDrawDecorations = true;
    			
    	m_SystemColor = Color.black;
    	m_GridColor = new Color(220,220,255);
    	
    	m_xAxisLabel = "X"; m_yAxisLabel = "Y";
    	
    	m_iGridType 	  = GRID_TYPE_RECT;
    	m_iGridMode 	  = GRID_MODE_LINES;
    	m_iGridVisibility = NO_GRID;
    	m_iAxisType		  = BOTH_AXES;
    	m_iArrowType 	  = BOTH_ARROWS;
    	
    	m_iTicksPerUnit = 5;
    	m_iAnglePitch    = 5;
    	
    	m_dInitialExtentsX = m_dInitialExtentsY = 20.0;
    	m_dSceneExtentsX = m_dSceneExtentsY = 20.0;
    	m_dCoarsePitchX = m_dCoarsePitchY = 10.0;
    	
    	m_dPixelsPerX = m_dPixelsPerY = 1.0;
    		
    	createUI();
    			
    	//FIXME: Check the correctness of this code!
    	// TODO: menu activation/deactivation? Perhaps not needed here...
    	
    	m_uiEnableBox.setSelected(true);
    	m_uiGridNoneItem.setSelected(true); 
    	m_uiCartGridItem.setSelected(true);
    	m_uiGridLinesItem.setSelected(true);
    	m_uiSnapBox.setSelected(false);
    	
    	m_uiGridModeSubMenu.setEnabled(false);
    	m_uiAngleStepItem.setEnabled(false);
    	
    	saveSettings();
    	precalculateTrig();
    	
    	hub.setCoordinateSystem(this);						
    }
    
    //*************************************************************************
    
    /**
     * This here is a <b><i>BEAST </i> </b> constructor, since it
     * allows you to initialize pretty much every aspect of a
     * CoordinateSystem object. <br><br><b>Have fun, hehehe :) </b>
     * <br><b>WARNING </b> Do NOT use this constructor yet! It is
     * incomplete and may cause unexpected behaviour.
     * 
     * @param hub reference to the system hub
     * @param visible <b>true </b> for visible, <b>false </b> for off.
     * 
     * @param gridType can be GRID_TYPE_RECT for a cartesian or
     *            GRID_TYPE_POLAR for a polar coordinate system.
     * 
     * @param gridMode can be GRID_MODE_DOTS or GRID_MODE_LINES
     * @param gridVis can be NO_GRID, COARSE_GRID or FINE_GRID
     * @param snapToGrid <b>true </b> to enable snap, <b>false </b>
     *            for no snap
     * @param arrowType can be NO_ARROWS, POSITIVE_ARROWS or
     *            BOTH_ARROWS
     * @param axisType can be NO_AXES, HORIZONTAL_AXIS, VERTICAL_AXIS
     *            or BOTH_AXES;
     * @param axesColor color of the coordinate system axes
     * @param gridColor color of grid dots/lines
     * @param xlabel label text for the X axis
     * @param ylabel label text for the Y axis
     * @param drawTicks <b>true </b> for ticks, <b>false </b> for no
     *            ticks
     * @param drawLabels <b>true </b> for axes labels, <b>false </b>
     *            for no labels
     * @param coarsex initial coarse (large tick) pitch for the X axis
     * @param coarsey initial coarse (large tick) pitch for the Y axis
     * @param finex initial fine (small tick) pitch for the X axis
     * @param finey initial fine (small tick) pitch for the Y axis
     * @param ticksPerUnit number of ticks per unit. If the value is
     *            outside of allowed range, a warning is issued and
     *            the value is clipped to the allowed minimum or
     *            maximum, respectively.
     */
    public CoordinateSystem(JSystemHub hub,
                            boolean visible,
                            int gridType,
                            int gridMode,
                            int gridVis,
                            boolean snapToGrid,
                            int arrowType,
                            int axisType,
                            Color axesColor,
                            Color gridColor,
                            String xlabel,
                            String ylabel,
                            boolean drawTicks,
                            boolean drawLabels,
                            double coarsex, double coarsey,
                            double finex, double finey,
                            int ticksPerUnit, 
                            int angleStep)
    {
    	initVars();
    			
    	m_bVisible 	  = visible;
    	m_bDrawLabels 	  = drawLabels;
    	m_bDrawTicks  	  = drawTicks;
    	m_bSnapToGrid 	  = snapToGrid;		
    	m_iGridType   	  = gridType;
    	m_iGridMode   	  = gridMode;
    	m_iGridVisibility = gridVis;
    	m_iArrowType	  = arrowType;
    	m_iAxisType	  = axisType;
    	m_SystemColor 	  = axesColor;
    	m_GridColor  	  = gridColor;
    	m_xAxisLabel	  = xlabel;
    	m_yAxisLabel	  = ylabel;
    	m_dCoarsePitchX   = coarsex;
    	m_dCoarsePitchY	  = coarsey;
    	m_dFinePitchX     = finex;
    	m_dFinePitchY	  = finey;
    	
    	m_iAnglePitch     = angleStep;
    	
    	setTicksPerUnit(ticksPerUnit);
    	precalculateTrig();
    	
    	createUI();
    		
    	//FIXME: Check the correctness and completeness of this code!
    	
    	m_uiEnableBox.setSelected(m_bVisible);
    	m_uiSnapBox.setSelected(m_bSnapToGrid);
    			
    	switch(m_iGridType)
    	{
            case GRID_TYPE_RECT:
            m_uiCartGridItem.setSelected(true);
            m_uiAngleStepItem.setEnabled(false);
            break;
            	
            case GRID_TYPE_POLAR:
            m_uiPolarGridItem.setSelected(true);
            m_uiAngleStepItem.setEnabled(true);
            break;
    		
    	}
    	switch(m_iGridMode)
    	{
            case GRID_MODE_DOTS:
            m_uiGridDotsItem.setSelected(true);
            break;
            
            case GRID_MODE_LINES:
            m_uiGridLinesItem.setSelected(true);
            break;
    	}
    	switch(m_iGridVisibility)
    	{
            case NO_GRID:
            m_uiGridNoneItem.setSelected(true);
            //m_uiSnapBox.setEnabled(false);
            break;
            
            case COARSE_GRID:
            m_uiGridCoarseItem.setSelected(true);
            break;
            
            case FINE_GRID:				
            m_uiGridFineItem.setSelected(true);
            break;			
    	}
    	
    	m_uiGridVisibilitySubMenu.setEnabled(m_bVisible);
    	m_uiGridModeSubMenu.setEnabled(m_bVisible);
    	m_uiGridTypeSubMenu.setEnabled(m_bVisible);
    	
    	m_uiSnapBox.setEnabled(m_bVisible);
    	m_uiAxesColorItem.setEnabled(m_bVisible);
    	m_uiGridColorItem.setEnabled(m_bVisible);	
    	m_uiResetItem.setEnabled(m_bVisible);	
    	
    	hub.setCoordinateSystem(this);
    	saveSettings();	
    }
	
    //*************************************************************************
    //			       Public instance methods
    //*************************************************************************
	
    //------------------ Getters / setters and configuration ------------------
    	
    // grid modes and type
    
    /**
     * Sets the visibility status of the entire coordinate system.
     * 
     * @param on <b>true </b> for visible.
     */
    public void setVisible(boolean on)
    { 
    	m_bVisible = on; 
    	m_uiEnableBox.setSelected(on);	
    }
    
    //*************************************************************************
		
    public boolean isVisible() 
    { 
        return m_bVisible; 
    }
    
    //*************************************************************************
	
    /**
     * Sets the visibility type of the grid.
     * 
     * @param gridVis can be NO_GRID, COARSE_GRID, FINE_GRID
     */
    public void setGridVisibility(int gridVis)
    { 
    	m_iGridVisibility = gridVis; 
    	
    	switch(m_iGridVisibility)
    	{
            case NO_GRID:
            	m_uiGridNoneItem.doClick();
            break;
            
            case COARSE_GRID:
            	m_uiGridCoarseItem.doClick();				
            break;
            
            case FINE_GRID:
            	m_uiGridFineItem.doClick();
            break;
    	}
    }
    
    //*************************************************************************
	
    public int getGridVisibility() 
    { 
        return m_iGridVisibility; 
    }
    
    //*************************************************************************
	
    /**
     * Sets the type of the coordinate grid
     * 
     * @param gridType can be GRID_TYPE_RECT for a Cartesian
     *            coordinate <br>system, or GRID_TYPE_POLAR for a
     *            polar coordinate system.
     */
    public void setGridType(int gridType)
    { 
    	m_iGridType = gridType;
    	
    	switch(m_iGridType)
    	{
            case GRID_TYPE_RECT:
            	m_uiCartGridItem.doClick();
            break;
            
            case GRID_TYPE_POLAR:
            	m_uiPolarGridItem.doClick();
            break;
    	}
    }
    
    //*************************************************************************
	
    public int getGridType()
    { 
        return m_iGridType;
    }
    
    //*************************************************************************
	
    /*
     * public void setSnapEnabled(boolean on) {
     * m_uiEnableBox.setSelected(on); }
     */
    
    /**
     * Sets the grid mode
     * 
     * @param gridMode can be GRID_MODE_DOTS or GRID_MODE_LINES
     */
    public void setGridMode(int gridMode)
    { 
        m_iGridMode = gridMode; 
    }
    
    //*************************************************************************
	
    /**
     * Determines how the axes' ends are to be capped.
     * 
     * @param arrowType can be either NO_ARROWS, POSITIVE_ARROWS or
     *            BOTH_ARROWS
     */
    public void setArrowType(int arrowType)
    { 
        m_iArrowType = arrowType; 
    }
    
    //*************************************************************************
	
    public void setAxisType(int axisType)
    { 
        m_iAxisType = axisType; }
    
    //*************************************************************************
    
    public void enableTicks(boolean on)
    { 
        m_bDrawTicks = on; 
    }
    
    //*************************************************************************
    
    public void enableAxisLabels(boolean on)
    { 
        m_bDrawLabels = on;     
    }
    
    //*************************************************************************
    
    //colors
    public void setAxesColor(Color c)
    { 
        m_SystemColor = c;
    }
    
    //*************************************************************************
    
    public void setGridColor(Color c)
    {
        m_GridColor = c;     
    }
    
    //*************************************************************************
	
    // additional attributes
		
    /**
     * Sets the axis labels' text.
     * 
     * @param xLabel label for the x (horizontal) axis
     * @param yLabel label for the y (vertical) axis
     *  
     */
    public void setAxisLabels(String xLabel, String yLabel)
    { 
            m_xAxisLabel = xLabel; 
            m_yAxisLabel = yLabel; 
    }
    
    //*************************************************************************
	
    /**
     * Enables / disables snapping of coordinates to grid points.
     * 
     * @param on <b>true </b> for enable, otherwise <b>false </b>
     */
    public void enableSnap(boolean on)
    { 
    	m_bSnapToGrid = on; 
    	
    	if(
            ((on == true) && (m_uiSnapBox.isSelected() == false)) ||
            ((on == false) && (m_uiSnapBox.isSelected() == true))
            )
    	{
    	    m_uiSnapBox.doClick();
    	}
    }
    
    //*************************************************************************
	
    public void toggleSnap()
    {
    	m_uiSnapBox.doClick();
    }
    
    //*************************************************************************
	
    public boolean isSnapEnabled()
    { 
        return m_bSnapToGrid; 
    }
    
    //*************************************************************************
    
    /**
     * Gets the current world-to-screen transformation matrix
     * 
     * @return an AffineTransform object
     */	 
    public AffineTransform getAffineTransform()
    { 
        return m_affineXForm; 
    }
    
    //*************************************************************************
    
    /**
     * Returns the rectangle that defines the extents of the world
     * coordinate system - i.e. extents of the visible area of the
     * display panel which have been mapped to the world coordinate
     * space.
     * 
     * @return current world bounding box
     */	
    public Rectangle2D getWorldBoundingBox()
    {
    	return m_worldRectangle;
    }
    
    //*************************************************************************
	
    public void getGridExtents(double extents[])
    {
    	/*
    	switch(m_iGridVisibility)
    	{
    		case NO_GRID:				
    		break;
    		
    		case COARSE_GRID:
    			extents[0] = _coarse_start_x;
    			extents[1] = _coarse_end_x;
    			extents[2] = _coarse_start_y;
    			extents[3] = _coarse_end_y;
    		break;
    		
    		case FINE_GRID:
    			extents[0] = _fine_start_x;
    			extents[1] = _fine_end_x;
    			extents[2] = _fine_start_y;
    			extents[3] = _fine_end_y;
    		break;
    	}*/
    	
    	extents[0] = _grid_start_x;
    	extents[1] = _grid_end_x;
    	extents[2] = _grid_start_y;
    	extents[3] = _grid_end_y;	
    }
    
    //*************************************************************************
	
    public void getPitchValues(double pitch[])
    {
    	pitch[0] = m_dCoarsePitchX;
    	pitch[1] = m_dFinePitchX;
    }
    
    //*************************************************************************
	
    public double getPolarGridRadius()
    {
    	return _polar_grid_radius;
    }
    
    //*************************************************************************
	
    public int getAnglePitch()
    { 
        return m_iAnglePitch; 
    } 
    
    //*************************************************************************
    
    public void getPolarGridTrig(double sines[], double cosines[])
    {
    	sines 	= m_dSines;
    	cosines = m_dCosines;
    }
    
    //*************************************************************************
    
    /**
     * 
     * @param ticks Number of ticks per unit. If the value is outside
     *            of allowed range, a warning is issued and the value
     *            is clipped to the allowed minimum or maximum,
     *            respectively.
     * 
     * FIXME: Don't know if this really has to be synchronized
     */
    public synchronized void setTicksPerUnit(int ticks)
    {
    	m_iTicksPerUnit = ticks;
    	
    	if(m_iTicksPerUnit < MIN_TICKS_PER_UNIT)
    	{
            System.out.println("ticks per " +
            		"unit is lower that allowed minimum of "+
            		MIN_TICKS_PER_UNIT);
            
            m_iTicksPerUnit = MIN_TICKS_PER_UNIT;			
    	}
    	
    	if(m_iTicksPerUnit > MAX_TICKS_PER_UNIT)
    	{
            System.out.println("ticks per " +
            		"unit is higher that allowed maximum of "+
            		MAX_TICKS_PER_UNIT);
            
            m_iTicksPerUnit = MAX_TICKS_PER_UNIT;					
    	}
    	
    	m_uiTicksPerUnitItem.setValue(new Integer(m_iTicksPerUnit));
    }
    
    //*************************************************************************
	
	
    /**
     * @return a JMenu object which contains the entire menu
     *         structure.
     */	
    public JMenu getMenus()
    { 
        return m_uiMenu; 
    }
    
    //*************************************************************************
    
    public JToolBar getToolBar()
    { 
        return m_uiToolBox; 
    }
    
    //*************************************************************************
    
    public void addActionListener(ActionListener listener)
    {
    	m_ActionListeners.add(listener);
    }
    
    //*************************************************************************
    
    public void removeActionListener(ActionListener listener)
    {
    	m_ActionListeners.remove(listener);
    }
    
    //*************************************************************************
	
    /**
     * Resets the appearance of the coordinate system to its initial
     * settings and centers it in the view. <br>Please note that the
     * word 'settings' here refers to pixels-per-unit and tick pitch
     * values, i.e. other settings like snap, grid options, etc. are
     * left unaffected.
     *  
     */	
    public void reset()
    {
    	//restore settings
    	m_dCoarsePitchX = m_dInitialCoarsePitchX;
    	m_dCoarsePitchY = m_dInitialCoarsePitchY;
    	m_dFinePitchX   = m_dInitialFinePitchX;
    	m_dFinePitchY   = m_dInitialFinePitchY;
    	m_dPixelsPerX   = m_dInitialPixelsPerX;
    	m_dPixelsPerY   = m_dInitialPixelsPerY;
    					
    	resize();	
    }
    
    //*************************************************************************
	
    /**
     * Default centering in the middle of the display component 
     */	
    public void center()
    {
    	m_Origin.x = (m_iDisplayWidth / 2) + m_Border.left;
    	m_Origin.y = (m_iDisplayHeight / 2) + m_Border.top;
    	
    	adjustTickExtents();	
    	updateTransform();
    }
    
    //*************************************************************************
	
    public void resize()
    {
    	m_Display = m_Hub.getDisplayPanel();	
    	m_Border = m_Display.getInsets();
    	
    	/* m_Border is required so that all drawing
    	 * is constrained to the region which is NOT
    	 * overlapped by the display component's border.
    	 * 
    	 */
    	
    	m_iDisplayWidth = m_Display.getWidth() - 
    			   m_Border.left - m_Border.right;
    	
    	m_iDisplayHeight = m_Display.getHeight() - 
    			    m_Border.top - m_Border.bottom;
    	
    	m_Origin.x = (m_iDisplayWidth / 2) + m_Border.left;
    	m_Origin.y = (m_iDisplayHeight / 2) + m_Border.top;
    	
    	// display resized, tick bounds must be re-adjusted
    	
    	adjustPitchValues();
    	adjustTickExtents();
    	
    	updateTransform();		
    }
    
    //*************************************************************************
	
    public void setOrigin(Point origin)
    {
    	m_Origin.setLocation(origin);		
    	updateTransform();
    }
    
    //*************************************************************************
   
    /**
     * 
     */
    public void fitViewToRect(Rectangle2D rect)
    {
        
        // center the view on the supplied rectangle
        
        
        //m_Origin.setLocation( worldToScreenX(rect.getCenterX()),
        //                      worldToScreenY(rect.getCenterY()) );
         
        
        /* Fit the view into supplied rectangle.
         * For this, we calculate the ratios of the width and 
         * height of the rectangle to those of the current 
         * world bounding rectangle. The minimum of these
         * two values is then used as the scaling factor
         * for the view transform.
         * 
         */
        
        /*
        // trap for very small bounding rectangles
        if(
           (Math.abs(rect.getWidth()) < DBL_EPSILON) || 
           (Math.abs(rect.getHeight()) < DBL_EPSILON) )
        {
            return;
        }
        
        double factor = Math.min(
                rect.getWidth() / m_worldRectangle.getWidth(),
                rect.getHeight() / m_worldRectangle.getHeight());
        
        //zoomView(factor);
        
        m_dPixelsPerX = factor;
        m_dPixelsPerY = factor;
        
        adjustPitchValues();
        adjustTickExtents();
        updateTransform();*/
        
    }
    
    //*************************************************************************
    
    /**
     * Zoom in/out by a given factor
     * 
     */
    public void zoomView(double zoomFactor)
    {
        m_dPixelsPerX *= zoomFactor;
        m_dPixelsPerY *= zoomFactor;
        
        // protect against very small / very large zoom!
        if( m_dPixelsPerX < 1.0e-4 )
        {
            m_dPixelsPerX = m_dPixelsPerY = 1.0e-4;
        }
        if( m_dPixelsPerX > 1.0e+8 )
        {
            m_dPixelsPerX = m_dPixelsPerY = 1.0e+8;
        }
          
        adjustPitchValues();
        
        /*
         * pitch values have been changed, so we must re-adjust the
         * tick bounds accordingly
         */
        adjustTickExtents();
        updateTransform();        
    }
    
    //*************************************************************************
    
    //------------------------- XML input / output ----------------------------
    
    /**
     * 
     * @return The object saved in XML
     */
    public Element saveSettingsToXML()	
    {
    	// create root node
    	Element root = new Element(XML_SETTINGS_NAME);
    	
    	// property nodes
    	Element sys_props = new Element(XML_SYS_PROPERTIES);
    	Element grid_props = new Element(XML_GRID_PROPERTIES);
    			
    	//------------- convert coordinate system properties -----------
    	
    	sys_props.setAttribute(XML_SYS_VISIBILITY, 
                               String.valueOf(m_bVisible));
        
        sys_props.setAttribute(XML_NO_DECORATIONS,
                               String.valueOf(m_bDrawDecorations));
    		
    	sys_props.setAttribute(XML_AXIS_COLOR,
    	                       String.valueOf(m_SystemColor.getRGB()));
    	
    	sys_props.setAttribute(XML_ORIGIN_X, 
                               String.valueOf(m_Origin.x));
    	
    	sys_props.setAttribute(XML_ORIGIN_Y, 
                               String.valueOf(m_Origin.y));
    	
    	sys_props.setAttribute(XML_ZOOM_FACTOR, 
                               String.valueOf(m_dPixelsPerX));
    	
    	// ---------------- convert grid properties -----------------
    	
    	grid_props.setAttribute(XML_GRID_COLOR, 
     			   	String.valueOf(m_GridColor.getRGB()));
    	
    	grid_props.setAttribute(XML_GRID_VISIBILITY,
    				String.valueOf(m_iGridVisibility));
    	
    	grid_props.setAttribute(XML_GRID_TYPE,
    				String.valueOf(m_iGridType));
    	
    	grid_props.setAttribute(XML_GRID_SNAP,
    				String.valueOf(m_bSnapToGrid));
    	
    	grid_props.setAttribute(XML_TICK_PITCH,
    				String.valueOf(m_iTicksPerUnit));
    	
    	/* Angle pitch is a finite list of discrete values, so
    	 * instead of saving the actual value of angle pitch,
    	 * we save its position in the list. This facilitates
    	 * synchronization of angle pitch spinner when loading
    	 * the settings.
    	 * 
    	 */
    	
    	SpinnerListModel model = 
    	 (SpinnerListModel)m_uiAngleStepItem.getModel();
    	
    	List values = model.getList();
    	int index = values.indexOf(m_uiAngleStepItem.getValue());
    			
    	grid_props.setAttribute(XML_ANGLE_PITCH, String.valueOf(index));
    	
    	// put everything together
    	root.addContent(sys_props);
    	root.addContent(grid_props);
    	
    	return root;
    }
    
    //*************************************************************************
    
    /**
     * 
     * @param root
     */
    public void loadSettingsFromXML(Element root)
    {
    	Element sys_props = root.getChild(XML_SYS_PROPERTIES);
    	Element grid_props = root.getChild(XML_GRID_PROPERTIES);
    	
    	if( (sys_props == null) || (grid_props == null) )
    	{
            System.err.println("Can't parse XML data!");
            System.err.println("Reverting to default settings...");
            return;
    	}
    	
    	// reconstruct parameters from XML data
    	try
    	{
            Attribute prop;
            
            // system settings
            prop =  sys_props.getAttribute(XML_SYS_VISIBILITY);		
            setVisible(prop.getBooleanValue());
            
            prop = sys_props.getAttribute(XML_NO_DECORATIONS);
            m_bDrawDecorations = prop.getBooleanValue();
            
            prop = sys_props.getAttribute(XML_AXIS_COLOR);			
            setAxesColor(new Color(prop.getIntValue()));
            
            // zoom factor and origin coordinates
            prop = sys_props.getAttribute(XML_ZOOM_FACTOR);
            m_dPixelsPerX = m_dPixelsPerY = prop.getDoubleValue();
            
            int x,y;
            prop = sys_props.getAttribute(XML_ORIGIN_X);
            x = prop.getIntValue();
            
            prop = sys_props.getAttribute(XML_ORIGIN_Y);
            y = prop.getIntValue();
            
            m_Origin.setLocation(x,y);
            
            // grid settings
            
            prop = grid_props.getAttribute(XML_GRID_COLOR);
            setGridColor(new Color(prop.getIntValue()));
            
            prop = grid_props.getAttribute(XML_GRID_SNAP);
            enableSnap(prop.getBooleanValue());
            
            prop = grid_props.getAttribute(XML_GRID_TYPE);
            setGridType(prop.getIntValue());
            
            prop = grid_props.getAttribute(XML_GRID_VISIBILITY);
            setGridVisibility(prop.getIntValue());
            
            prop = grid_props.getAttribute(XML_TICK_PITCH);
            setTicksPerUnit(prop.getIntValue());
            
            prop = grid_props.getAttribute(XML_ANGLE_PITCH);
            
            /* Now we use the retrieved angle pitch attribute
             * as the index into the list of all possible 
             * values of angle pitch.
             * 
             */
            
            int index = prop.getIntValue();
            SpinnerListModel model = 
             (SpinnerListModel)m_uiAngleStepItem.getModel();
            			
            List values = model.getList();
            Integer pitch = (Integer)values.get(index);
            			
            m_iAnglePitch = pitch.intValue();
            m_uiAngleStepItem.setValue(pitch);
    	}
    	catch(DataConversionException ex)
    	{
            System.err.println(
             "One or more XML attributes couldn't be parsed!" +
             " Default settings will be used!");
            
            System.err.println(ex.getMessage());
    	}
        catch(NullPointerException ex)
        {
            System.err.println(
                    "One or more XML attributes are missing!" +
                    " Default settings will be used!");
            
            System.err.println(ex.getMessage());
        }
        
    	
    	updateTransform();
    	adjustPitchValues();
    	adjustTickExtents();
    	
    	m_Display.repaint();
    }
	
    //*************************************************************************
    //                    Coordinate transformation methods
    //*************************************************************************
    
    public double screenToWorldX(int x)
    {
    	return (x - m_Origin.x) / m_dPixelsPerX;
    }
    
    //*************************************************************************
	
    public double screenToWorldY(int y)
    {
    	/* note that y coordinate is essentially negated here
    	 * because Java2D screen-space y-coordinates increase
    	 * 'down'.
    	 */
    	return (m_Origin.y - y) / m_dPixelsPerY;
    }
    
    //*************************************************************************
	
    public int worldToScreenX(double x)
    {
    	return (int)(x * m_dPixelsPerX + m_Origin.x + PIXEL_BIAS);
    }
    
    //*************************************************************************
	
    public int worldToScreenY(double y)
    {
    	return (int)(m_Origin.y - y * m_dPixelsPerY + PIXEL_BIAS);
    }
	
    //****** point transforms ********
    
    /* FIXME: Using Point2D get/set methods
     * may have a negative impact on performance of 
     * point transformations. Also, there may be a 
     * loss of precision when using 32-bit floating
     * point target coordinates. 
     *  	  
     */
	
    public void worldToScreen(Point2D.Double source, Point target)
    {		
    	target.setLocation(worldToScreenX(source.x),
    			   worldToScreenY(source.y));
    }
    
    //*************************************************************************
	
    public void worldToScreen(Point2D source, Point2D target)
    {
    	target.setLocation(worldToScreenX(source.getX()),
    			   worldToScreenY(source.getY()));
    }
    
    //*************************************************************************
    
    /**
     * Transforms an entire array of points in world coordinates
     * into screen coordinates. The original array is not altered,
     * the results are placed into target array.<br><br>
     * <b>WARNING:</b> This function assumes that both source and
     * target arrays are properly allocated and are of same size!
     * (This implies that the target array is filled with valid
     * instances of Point). It does NOT perform any checks on the 
     * data! If the above conditions are not met, a
     * <code>NullPointerException</code> will likely be thrown.
     * 
     * @param source array of points in world coordinates
     * @param target resulting points in screen coordinates
     */
    
    public void worldToScreen(Point2D.Double[] source, Point[] target)
    {
    	for(int i = 0; i < source.length; i++)
    	{
            target[i].x = worldToScreenX(source[i].x);
            target[i].y = worldToScreenY(source[i].y);			
    	}
    }
    
    //*************************************************************************

    /**
     * 
     */
    public void worldToScreen(Vector source, Vector target)
    {		
    	Iterator source_it = source.iterator();
    	Iterator target_it = target.iterator();
    	
    	Point2D s;
    	Point   t;
    	
    	if(target.isEmpty())
    	{
            /*
             * Fill the target vector with transformed points
             */
            while(source_it.hasNext())
            {
            	 s = (Point2D)source_it.next();
            	 t = new Point(worldToScreenX(s.getX()),
            	               worldToScreenY(s.getY()));
            	target.add(t);
            }
    	}
    	else
    	{
            //FIXME: This error trap needs some work...
            if(source.size() != target.size())
            {				
            	System.out.println("Source and target sizes don't match");
            	return; // bail out..
            }
            
            /*
             * Modify existing points stored in the target
             * vector
             */
            while(source_it.hasNext())
            {
            	s = (Point2D)source_it.next();
            	t = (Point)target_it.next();
            	
            	t.x = worldToScreenX(s.getX());
            	t.y = worldToScreenY(s.getY());
            }			
    	}
    }
    
    //*************************************************************************
	
    /**
     * Maps a point from screen(pixel)space to world(object) space.
     * This function also optionally snaps the resulting point to the
     * coordinate grid. The snap behaviour is controlled by
     * <code>respectSnap</code> parameter. If it is set to
     * <code>true</code>, the function will respect the current
     * snap and grid settings, i.e. it will snap points if snap is
     * enabled and the grid is visible. If <code>respectSnap</code>
     * is set to <code>false</code>, the function will simply
     * transform points irrespective of curent grid/snap settings.
     * This is useful is situations like tracking mouse cursor
     * position (in this case snapping would be an
     * undesired behaviour).<br><br>
     * 
     * @param source point in screen coordinates
     * @param target resulting point in world coordinates
     * @param respectSnap Set to <code>true </code> to activate.
     */
	
    public void screenToWorld(Point source, Point2D.Double target, 
    					    boolean respectSnap)
    {
    	target.x = screenToWorldX(source.x);
    	target.y = screenToWorldY(source.y);
    	
    	if((respectSnap == true) && (m_bSnapToGrid == true))
    	{			
            /* snap to closest grid point.
             * For this, we need to figure where the point is first,
             * and then adjust its coordinates accordingly.
             */ 
            
            int x_pos = 0, y_pos = 0;
            double radius = 0.0, phi = 0.0; 
            
            switch(m_iGridVisibility)
            {
            	case NO_GRID:
            	return; // nuthin' to do...
            	
            	case COARSE_GRID:
            		
            	if(m_iGridType == GRID_TYPE_RECT)
            	{
                    //Cartesian coordinates
                    x_pos = (int)Math.round(target.x / m_dCoarsePitchX);
                    y_pos = (int)Math.round(target.y / m_dCoarsePitchY);
                    
                    target.x = x_pos * m_dCoarsePitchX;
                    target.y = y_pos * m_dCoarsePitchY;		
            	}
            	else
            	{					
                    // Polar coordinates
                    // Convert point to polar representation 
                    
                    radius = target.x * target.x + target.y * target.y;
                    radius = Math.sqrt(radius); // in world coordinates
                    
                    target.x /= radius; 
                    target.y /= radius; //  as angle					
                    
                    phi = Math.asin(target.x);
                    
                    /*
                     * Since arcsin can only map the ranges
                     * [-1,1]->[-pi/2, pi/2] bijectively, we have to
                     * manually adjust the angle if the point is any
                     * but the first quadrant. Note that some
                     * inequalities are strict - this is done to avoid
                     * out-of-range values of phi. (To be precise, the
                     * first inequality is strict so that phi doesn't
                     * become 2PI which is same as 0, while the second
                     * one is semistrict so that phi doesn't jump to
                     * -PI/2 when crossing the negative x axis.) 
                     */
                    
                    if((target.y < 0.0))
                    {
                    	// quadrants II and III
                    	phi = PI - phi;
                    }
                    else if((target.y >= 0.0) && (target.x < 0.0))
                    {
                    	// quadrant IV
                    	phi = 2.0 * PI + phi;
                    }
                    					
                    // snap radius
                    x_pos = (int)Math.round(radius / m_dCoarsePitchX);
                    radius  = x_pos * m_dCoarsePitchX;
                    
                    // snap angle
                    
                    double angle = Math.toDegrees(phi);
                    angle = (int)Math.round(angle / m_iAnglePitch);
                    phi = Math.toRadians( angle * m_iAnglePitch);
                    
                    /* convert back to cartesian coordinates
                     * Note that same trick with swapped trig
                     * functions as in drawPolarGrid() 
                     */
                    
                    target.x = radius * Math.sin(phi);
                    target.y = radius * Math.cos(phi);			            		
            	}
            	
            	break;
            	
            	case FINE_GRID:
            		
            	if(m_iGridType == GRID_TYPE_RECT)
            	{
                    //Cartesian coordinates
                    x_pos = (int)Math.round(target.x / m_dFinePitchX);
                    y_pos = (int)Math.round(target.y / m_dFinePitchY);
                    
                    target.x = x_pos * m_dFinePitchX;
                    target.y = y_pos * m_dFinePitchY;		
            	}
            	else
            	{
                    // Polar coordinates
                    // Convert point to polar representation 
                    
                    radius = target.x * target.x + target.y * target.y;
                    radius = Math.sqrt(radius); // in world coordinates
                    
                    target.x /= radius; 
                    target.y /= radius; //  as angle					
                    
                    phi = Math.asin(target.x);
                    			
                    if((target.y < 0.0))
                    {
                    	// quadrants II and III
                    	phi = PI - phi;
                    }
                    else if((target.y >= 0.0) && (target.x < 0.0))
                    {
                    	// quadrant IV
                    	phi = 2.0 * PI + phi;
                    }
                    					
                    // snap radius
                    x_pos = (int)Math.round(radius / m_dFinePitchX);
                    radius  = x_pos * m_dFinePitchX;
                    
                    // snap angle
                    
                    double angle = Math.toDegrees(phi);
                    angle = (int)Math.round(angle / m_iAnglePitch);
                    phi = Math.toRadians( angle * m_iAnglePitch);
                    
                    target.x = radius * Math.sin(phi);
                    target.y = radius * Math.cos(phi);		
            	}
            	
            	break;		
            } // end switch
    	} // end if(snapTogrid && respectSnap)
    }
    
    //*************************************************************************
	
    /**
     * Essentially a wrapper for
     * {@link #screenToWorld(Point, Point2D.Double, boolean)}
     * which handles instances of {@link anja.geom.Point2}. 
     * 
     * <p> Be aware that because of downcasting of coordinates 
     * from <code>double</code> to <code>float</code> there's
     * inherent loss of precision here!
     * 
     * @param source
     * @param target
     * @param snap
     */
    
    public void screenToWorld(Point source, Point2 target, boolean snap)
    {
    	screenToWorld(source, _temp_point, snap);
    	target.x = (float)_temp_point.x;
    	target.y = (float)_temp_point.y;
    }
    
    //*************************************************************************
	
    public Point worldToScreen(Point2D.Double source)
    {
    	//FIXME: Maybe add snap here as well..
        return new Point(worldToScreenX(source.x),
    			 worldToScreenY(source.y));
    	
    }
    
    //*************************************************************************
    
    public Point2 screenToWorld(Point source)
    {		
    	return new 
    	Point2(screenToWorldX(source.x),
    		   screenToWorldY(source.y));
    	
    }
    
    //*************************************************************************
	
    /**
     * This is a scaled-down version of screenToWorld()
     * that does not perform any of the complicated tests
     * for snapping etc. It can be used to quickly map
     * a point from image to object space (one place
     * where I use it is for querying mouse coordinates to
     * display them in the status panel). For efficiency
     * the target point must be a valid reference, because
     * it is only modified.
     * 
     * @param source point in image space coordinates
     * @param target resulting point in world coordinates
     */
    
    public void screenToWorld(Point source, Point2D.Double target)
    {
    	target.x = screenToWorldX(source.x);
    	target.y = screenToWorldY(source.y);
    }
    
    //*************************************************************************
	
    /**
     * Same as above, but additionally converts the point into polar
     * coordinate representation.
     * 
     * <p>The x component of <code>target</code> contains the
     * 'radius' of the <br>point relative to the origin
     * 
     * <p>The y component of <code>target</code> contains the 
     * angle,in radians, enclosed <br>by the point and the positive
     * y-axis. The angle lies in range <code>[0, 2PI]</code>
     * 
     * 
     * @param source point in image space coordinates
     * @param target resulting point in world polar coordinates
     */
	
    public void polarScreenToWorld(Point source, Point2D.Double target)
    {
    	double radius, phi;
    	
    	target.x = screenToWorldX(source.x);
    	target.y = screenToWorldY(source.y);
    	
    	radius = target.x * target.x + target.y * target.y;
    	radius = Math.sqrt(radius); // in world coordinates
    	
    	target.x /= radius; 
    	target.y /= radius; //  as angle					
    	
    	phi = Math.asin(target.x);
    	
    	// see comments in 'big' screenToWorld()
    	
    	if((target.y < 0.0))
    	{
            // quadrants II and III
            phi = PI - phi;
    	}
    	else if((target.y >= 0.0) && (target.x < 0.0))
    	{
            // quadrant IV
            phi = 2.0 * PI + phi;
    	}
    	
    	
    	target.x = radius;
    	target.y = phi;
    }
	
    //*************************************************************************
    //                             Event handlers
    //*************************************************************************
			
    /**
     * Handles events generated by the menus, buttons etc. This will
     * automatically call the update/redraw method of the parent
     * display component after any parameter has been changed. It will
     * also call any additional registered action event listeners.
     */
	
    public void actionPerformed(ActionEvent event)
    {
    	String cmd = event.getActionCommand();
    	
    	switch(Integer.parseInt(cmd))
    	{
            // general stuff
            case CMD_ENABLE:
            
            Object source = event.getSource();
            if(source.equals(m_uiEnableBox))
            {
            	m_bVisible = m_uiEnableBox.isSelected();
            }
            else
            {
            	m_bVisible =! m_bVisible;
            }
            	
            //m_bVisible = m_uiEnableBox.isSelected();
            
            m_uiGridVisibilitySubMenu.setEnabled(m_bVisible);
            m_uiGridModeSubMenu.setEnabled(m_bVisible);
            m_uiGridTypeSubMenu.setEnabled(m_bVisible);
            
            m_uiSnapBox.setEnabled(m_bVisible);
            
            if(m_uiSnapBox.isSelected())
             m_uiSnapBox.doClick();
            
            m_uiAxesColorItem.setEnabled(m_bVisible);
            m_uiGridColorItem.setEnabled(m_bVisible);
            m_uiResetItem.setEnabled(m_bVisible);
            m_uiCenterItem.setEnabled(m_bVisible);			
            m_uiTicksPerUnitItem.setEnabled(m_bVisible);
            m_uiAngleStepItem.setEnabled(m_bVisible);
            
            break;
            
            case CMD_NO_DECORATIONS:
            
            m_bDrawDecorations = !m_uiNoDecorBox.isSelected();
   
            break;
            
            case CMD_RESET:
            reset();
            break;
            
            case CMD_CENTER:
            center();
            break;
            
            // grid type 
            case CMD_TYPE_CARTESIAN:
            m_iGridType = GRID_TYPE_RECT;
            m_uiAngleStepItem.setEnabled(false);
            break;
            
            case CMD_TYPE_POLAR:
            m_iGridType = GRID_TYPE_POLAR;
            m_uiAngleStepItem.setEnabled(true);
            break;
            
            // grid visibility
            case CMD_GRID_NONE:
            m_iGridVisibility = NO_GRID;
            m_uiGridModeSubMenu.setEnabled(false);
            m_uiSnapBox.setEnabled(false);
            break;
            
            case CMD_GRID_COARSE:
            m_iGridVisibility = COARSE_GRID;
            m_uiGridModeSubMenu.setEnabled(true);
            m_uiSnapBox.setEnabled(true);
            break;
            
            case CMD_GRID_FINE:
            m_iGridVisibility = FINE_GRID;
            m_uiGridModeSubMenu.setEnabled(true);
            m_uiSnapBox.setEnabled(true);
            break;
            
            // grid mode
            case CMD_MODE_DOTS:
            m_iGridMode = GRID_MODE_DOTS;
            break;
            
            case CMD_MODE_LINES:
            m_iGridMode = GRID_MODE_LINES;
            break;
            
            // snap, colors etc.
            case CMD_SNAP_TO_GRID:
            m_bSnapToGrid = m_uiSnapBox.isSelected();
            break;
            
            case CMD_AXES_COLOR:
            
            Color c = JColorChooser.showDialog(null,
                      "Choose system color", m_SystemColor);
            if(c != null)
            {
            	m_SystemColor = c;
            	m_Hub.getDisplayPanel().setForegroundColor(c);
            }
            break;
            
            case CMD_GRID_COLOR:
            			
            Color cc = JColorChooser.showDialog(null,
                       "Choose grid color", m_GridColor);
            
            if(cc != null)
             m_GridColor = cc;
            			
            break;    				
    	} // end event handler switch
    	
    	/* FIXME: Maybe place the call to repaint()
    	 * after the event dispatcher loop...
    	 * 
    	 */
    	
    	// update the parent display
    	m_Hub.getDisplayPanel().repaint();
    			
    	// call registered action listeners, if any
    	
    	// reparent the event for easier identification later on...
    	event.setSource(this);
    	
    	ActionListener l;			
    	Iterator it = m_ActionListeners.iterator();
    	while(it.hasNext())
    	{
            l = (ActionListener)it.next();
            l.actionPerformed(event);
    	}
    }
    
    //*************************************************************************
	
    /** Listen to events generated by spinner items in the
     * menu. Added here to avoid creating a bunch of local
     * anonymous classes (Otherwise compiler bitches about 
     * access to enclosing class fields being performed
     * by synthetic acessor methods etc.) + all those
     * anonymous classes look strange!
     *  
     */
	
    public void stateChanged(ChangeEvent event)
    {
    	Object source = event.getSource();
    	
    	if(source == m_uiAngleStepItem)
    	{
            // modify angle pitch value
            m_iAnglePitch = 
             ((Integer)m_uiAngleStepItem.getValue()).intValue();
            
            precalculateTrig();				
            
            String cmd = String.valueOf(CMD_ANGLE_PITCH);
            ActionEvent ev = new ActionEvent(this, 0, cmd);
            actionPerformed(ev);
            
            //m_Hub.getDisplayPanel().repaint();
    	}
    	else if(source == m_uiTicksPerUnitItem)
    	{
            // modify ticks-per-unit value
            m_iTicksPerUnit = 
             ((Integer)m_uiTicksPerUnitItem.getValue()).intValue();
            
            adjustPitchValues();
            adjustTickExtents();
            				
            m_Hub.getDisplayPanel().repaint();			
    	}
    }
    	
    //------------------------ Keyboard event handlers ------------------------
	
    public void keyTyped(KeyEvent event)
    { 
        /* stub */ 
    }
    
    //*************************************************************************
        
    public void keyReleased(KeyEvent event)
    { 
        /* stub */     
    }
    
    //*************************************************************************
    
    /**
     * Handles keyboard shortcuts for various coordinate system
     * options. 
     * <br><br>Curent key mappings:
     * <br><b>g</b> - switches grid between NONE, BROAD and FINE modes.
     * <br><b>s</b> - toggles snap 
     * <br><br>Note: Currently, this is the only viable option for creating key
     * mappings, since this class does not descend from JComponent and
     * thus cannot employ the InputMap/ActionMap system. 
     */	
	
	
    public void keyPressed(KeyEvent event)
    {
    	// FIXME: Fix the keyboard mappings...
    
    	if((event.getModifiersEx() & 
    		InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK)
            
    	switch(event.getKeyCode())
    	{
            // grid visibility
            case KeyEvent.VK_G:
            
            if(m_uiGridNoneItem.isSelected())
            {
            	m_uiGridCoarseItem.doClick();
            }
            else if(m_uiGridCoarseItem.isSelected())
            {
            	m_uiGridFineItem.doClick();
            }
            else m_uiGridNoneItem.doClick();
            
            break;
            
            case KeyEvent.VK_S:		
            m_uiSnapBox.doClick();			
            break;
            
            case KeyEvent.VK_C:
            m_uiCenterItem.doClick();
            break;
    	}				
    }
    
    //------------------------- Mouse event handlers --------------------------
	
    public void mousePressed(MouseEvent event)
    {
    	// store current cursor position and button(s)
    	m_zoomPixel = event.getPoint();
    	screenToWorld(m_zoomPixel, m_zoomPoint, true);
    	
    	m_iMouseButton = event.getButton();
    	m_pressedPoint = m_zoomPixel;
    	
    	/*
    	// experimental popup menu support 
    	if(m_iMouseButton == MouseEvent.BUTTON3)
    	{
    		m_uiMenu.getPopupMenu().show(m_Hub.getParent(), 
    		m_zoomPixel.x, m_zoomPixel.y);
    	}*/
    }
    
    //*************************************************************************
	
    public void mouseReleased(MouseEvent event)
    {
    	// stub
    }
    
    //*************************************************************************
	
    public void mouseDragged(MouseEvent event)
    {    
    	Point p = event.getPoint();
    	
    	if(m_iMouseButton == MouseEvent.BUTTON1)
    	{
            //translate coordinate system
            m_Origin.x += p.x - m_pressedPoint.x;
            m_Origin.y += p.y - m_pressedPoint.y;							
    	}
    	else
    	{
            // zoom coordinate system
            double zoom_factor =
             Math.pow(1.01, Math.abs(p.y - m_pressedPoint.y));
            
            if(p.y - m_pressedPoint.y < 0) 
             zoom_factor  = 1.0 / zoom_factor; // invert zoom direction
            
            m_dPixelsPerX *= zoom_factor;
            m_dPixelsPerY *= zoom_factor;
            
            if( m_dPixelsPerX < 1.0e-4 )
            {
            	m_dPixelsPerX = m_dPixelsPerY = 1.0e-4;
            }
            if( m_dPixelsPerX > 1.0e+8 )
            {
            	m_dPixelsPerX = m_dPixelsPerY = 1.0e+8;
            }
            
            // adjust coordinate system origin etc.
            m_Origin.x -= worldToScreenX(m_zoomPoint.x) - m_zoomPixel.x;
            m_Origin.y -= worldToScreenY(m_zoomPoint.y) - m_zoomPixel.y;    						
    	}
    	
    	m_pressedPoint = p;
    	
    	adjustPitchValues();
    	
    	/*
    	 * pitch values have been changed, so we must re-adjust the
    	 * tick bounds accordingly
    	 */
    	adjustTickExtents();
    	updateTransform();
    }
    
    //*************************************************************************
	
    public void mouseWheelMoved(MouseWheelEvent event)
    {
    	/* mouse wheel zooming. same zoom as with
    	 * right mouse button */
    	
    	// get current cursor position and map it to object space
    	m_zoomPixel = event.getPoint(); 
    	screenToWorld(m_zoomPixel, m_zoomPoint, false);
    	
    	int wheel_clicks = event.getWheelRotation();
    	
    	double zoom_factor = 1.1;
    	if(wheel_clicks > 0) 
    	 zoom_factor  = 1.0 / zoom_factor; // invert zoom direction
    	
    	m_dPixelsPerX *= zoom_factor;
    	m_dPixelsPerY *= zoom_factor;
    	
    	if( m_dPixelsPerX < 1.0e-4 )
    	{
    	    m_dPixelsPerX = m_dPixelsPerY = 1.0e-4;
    	}
    	if( m_dPixelsPerX > 1.0e+8 )
    	{
    	    m_dPixelsPerX = m_dPixelsPerY = 1.0e+8;
    	}
    	
    	// adjust coordinate system origin etc.
    	m_Origin.x -= worldToScreenX(m_zoomPoint.x) - m_zoomPixel.x;
    	m_Origin.y -= worldToScreenY(m_zoomPoint.y) - m_zoomPixel.y;
    	
    	adjustPitchValues();
    	
    	/*
    	 * pitch values have been changed, so we must re-adjust the
    	 * tick bounds accordingly
    	 */
    	adjustTickExtents();
    	updateTransform();
    			
    }
    
    //------------------------------ UI drawing -------------------------------
    
    /**
     * 
     */
    public void draw(Graphics2D g)
    {	
    	//draw grid if it's enabled
    	g.setColor(m_GridColor);
    	
    	switch(m_iGridType)
    	{
            case GRID_TYPE_RECT:
             drawRectangularGrid(g);
            break;
            
            case GRID_TYPE_POLAR:
             drawPolarGrid(g);
            break;
    	}
    	
    	/* draw coordinate axes and their decorations
    	 * These are actually drawn in the 'original' coordinate system,
    	 * because it's simpler and improves performance.
    	 */
    	
    	g.setColor(m_SystemColor);
    	
    	switch(m_iAxisType)
    	{
            case NO_AXES:
            
            break;
            
            case BOTH_AXES:			 			 
             g.drawLine(0, m_Origin.y, 
             		m_Display.getWidth(), m_Origin.y);
             g.drawLine(m_Origin.x, 0, 
             		m_Origin.x, m_Display.getHeight());
             
             if(m_bDrawDecorations)
             {
                 drawHorizontalDecorations(g);
                 drawVerticalDecorations(g);
             }		 
            break;
            
            case HORIZONTAL_AXIS:	
             g.drawLine(0, m_Origin.y, 
            		m_Display.getWidth(), m_Origin.y);
             
             if(m_bDrawDecorations)
             {
                 drawHorizontalDecorations(g);
             }
            break;
            
            case VERTICAL_AXIS:			 
             g.drawLine(m_Origin.x, 0, 
            		m_Origin.x, m_Display.getHeight());
             
             if(m_bDrawDecorations)
             {
                 drawVerticalDecorations(g);
             }
            break;			
    	}
    			
    	drawStatus(g);	
    }
		
    //*************************************************************************
    //			      Protected instance methods
    //*************************************************************************
	
    /** Forbidden default constructor */
    protected CoordinateSystem() 
    {
        
    }
    
    //*************************************************************************
    //			       Private instance methods
    //*************************************************************************
		
    private void updateTransform()
    {
    	m_affineXForm.setTransform(m_dPixelsPerX,   0,
    				   0, - m_dPixelsPerY,
    				   m_Origin.x, m_Origin.y);
    	
    	//recalculate world bounding rectangle
    	calculateWorldRectangle();
    	
    	// update transforms throughtout the system
    	m_Hub.updateTransform(m_affineXForm);	
    }
    
    //*************************************************************************
	
    /* Calculate visible portion of the coordinate system
     * 
     */
    private void calculateWorldRectangle()
    {
    	
    	/* map the display window corners back into
    	 * world coordinate space
    	 */
    			
    	Point2D lower_left = 
    	 screenToWorld(new Point(0,m_Display.getHeight()));
    	
    	Point2D upper_right =
    	  screenToWorld(new Point(m_Display.getWidth(), 0));
    
    	m_worldRectangle.setRect(
    			lower_left.getX(), // x  
    			lower_left.getY(), // y
    			upper_right.getX() - lower_left.getX(), // width
    			upper_right.getY() - lower_left.getY()  // height
    				);
    			
    	
    	/* TODO: VERY IMPORTANT NOTE!
    	 * Don't forget that because of the mirrored-Y coordinate 
    	 * space all rectangles should be specified with their
    	 * lower-left point first!!!!
    	 */
    	
    	// test culling
    	/*
    	boolean cont = m_worldRectangle.contains(_test_rect);
    	boolean intr = m_worldRectangle.intersects(_test_rect);
    	
    	_culled = !(cont | intr);*/
    	
    }
    
    //*************************************************************************
	
    private void adjustPitchValues()
    {		
    	/*
    	 * Tick pitch adjustments are based on the current
    	 * distance(s) between adjacent large ticks in 
    	 * pixel space. 
    	 * WARNING: This code is still buggy!!!!
    	 * FIXME: Possible bugs in floating point equality 
    	 * comparisons.
    	 */
    	
    	/* Until I find a better implementation of this algorithm,
    	 * I will use Sascha's code, slightly modified to allow for
    	 * arbitrary number of fine ticks.
    	 * 
    	 */ 	
    			
    	// Adjust units for the X axis
    	double a = 1.0;
    	double b = 1.0;
    	
    	// zoom out
    	if(m_dPixelsPerX / (a * b) < 20.0) 
    	 while(m_dPixelsPerX / (a * b) < 20.0)
    	 {
            if(Math.abs(a - 5.0) < DBL_EPSILON) a = 4.0;
            else if(Math.abs(a - 1.0) < DBL_EPSILON)
            {
            	a = 5.0;
            	b /= 10.0;
            } 
            else a /= 2.0;
    	 } 
    	
    	// zoom in
    	else if(m_dPixelsPerX / (a * b) > 40.0) 
    	 while(m_dPixelsPerX / (a * b) > 40.0)
    	 {
            if(a < 4.0) a *= 2.0;
            else if(Math.abs(a - 4.0) < DBL_EPSILON) a = 5.0;
            else
            {
            	a = 1.0;
            	b *= 10.0;
            } 
    	 } 
    			
    	double fpx = 1.0 / (a * b);
    	if(Math.abs(a - 1.0) < DBL_EPSILON) a = 2.0;
    	
    	m_dCoarsePitchX = a * fpx;
    	m_dFinePitchX = m_dCoarsePitchX / m_iTicksPerUnit;
    		      
        // Adjust units for the Y-axis
    	a = 1.0;
    	b = 1.0;
    	
    	// zoom out
    	if(m_dPixelsPerY / (a * b) < 20.0) 
    	 while(m_dPixelsPerY / (a * b) < 20.0)
    	 {
            if(Math.abs(a - 5.0) < DBL_EPSILON) a = 4.0;
            else if(Math.abs(a - 1.0) < DBL_EPSILON)
            {
            	a = 5.0;
            	b /= 10.0;
            } 
            else a /= 2.0;
    	 } 
    	
    	// zoom in
    	else if(m_dPixelsPerY / (a * b) > 40.0) 
    	 while(m_dPixelsPerY / (a * b) > 40.0)
    	 {
            if(a < 4.0) a *= 2.0;
            else if(Math.abs(a - 4.0) < DBL_EPSILON) a = 5.0;
            else
            {
            	a = 1.0;
            	b *= 10.0;
            } 
    	 } 
    		
    	double fpy = 1.0 / (a * b);
    	if(Math.abs(a - 1.0) < DBL_EPSILON) a = 2.0;
    	m_dCoarsePitchY = a * fpy;
    	m_dFinePitchY = m_dCoarsePitchY / m_iTicksPerUnit;	
    		
    }
    
    //*************************************************************************
	
    private void adjustTickExtents()
    {
    	//adjust start / end tick coordinates
    	
    	/* If the coordinate system origin is outside of the
    	 * visible window, at least one of the 'rulers' 
    	 * will be drawn at the corresponding edge of
    	 * the window. In this case, the tick extents can
    	 * be a bit wider because no arrows will be drawn.
    	 * Hence the code below.
    	 */
    	
    	// true if origin's x or y are out of the window
    	boolean x_out = (m_Origin.x < m_Border.left) | 
    			(m_Origin.x > m_iDisplayWidth);
    	
    	boolean y_out = (m_Origin.y < m_Border.top) | 
    			(m_Origin.y > m_iDisplayHeight);
    	
    	if(m_iArrowType != NO_ARROWS)
    	{
            _coarse_start_x = 
             screenToWorldX(m_Border.left +
             ((y_out == true)?0:(ARROW_WIDTH)));
            			
            _coarse_end_x = 
             screenToWorldX(m_iDisplayWidth  + m_Border.right -
             ((y_out == true)?0:(ARROW_WIDTH)));
            
            _coarse_start_y = 
             screenToWorldY(m_iDisplayHeight + m_Border.bottom - 
             ((x_out == true)?0:(ARROW_WIDTH)));
            	
            _coarse_end_y = 		
             screenToWorldY(m_Border.top + 
             ((x_out == true)?0:(ARROW_WIDTH)));
    	}
    	else
    	{
            _coarse_start_x = 
             screenToWorldX(m_Border.left);
            			
            _coarse_end_x = 
            screenToWorldX(m_iDisplayWidth  + m_Border.right);
            
            _coarse_start_y = 
             screenToWorldY(m_iDisplayHeight + m_Border.bottom);
            	
            _coarse_end_y = 		
             screenToWorldY(m_Border.top);			
    	}
    	
    	// copy to fine tick values and modify them
    	_fine_start_x = _coarse_start_x;
    	_fine_start_y = _coarse_start_y;
    	_fine_end_x   = _coarse_end_x;
    	_fine_end_y   = _coarse_end_y;
    	
    	/* The below manipulations with Math.ceil and Math.floor
    	 * are necessary to produce tick positions that are integer
    	 * multiples of the pitch values. 
    	 * (i.e if the start is -23 and end is +18, and the 
    	 * coarse pitch is 5, the coarse ticks should
    	 * run from -20 to +15)
    	 */
    		
    	_coarse_start_x = 
    	 Math.ceil(_coarse_start_x / m_dCoarsePitchX) * m_dCoarsePitchX;
    	
    	_coarse_end_x = 
    	 Math.floor(_coarse_end_x / m_dCoarsePitchX) * m_dCoarsePitchX;
    	
    	_coarse_start_y = 
    	 Math.ceil(_coarse_start_y / m_dCoarsePitchY) * m_dCoarsePitchY;
    	
    	_coarse_end_y =
    	 Math.floor(_coarse_end_y / m_dCoarsePitchY) * m_dCoarsePitchY;
    	
    	_fine_start_x = 
    	 Math.ceil(_fine_start_x / m_dFinePitchX) * m_dFinePitchX;
    	
    	_fine_end_x = 
    	 Math.floor(_fine_end_x / m_dFinePitchX) * m_dFinePitchX;
    	
    	_fine_start_y = 
    	 Math.ceil(_fine_start_y / m_dFinePitchY) * m_dFinePitchY;
    	
    	_fine_end_y =
    	 Math.floor(_fine_end_y / m_dFinePitchY) * m_dFinePitchY;
    
    }
    
    //*************************************************************************

    private void drawStatus(Graphics2D g)
    {
    	if(m_bSnapToGrid)
    	{
    	    g.drawString("Snap: ON", 10, 20);
    	}
    	else
    	{
    	    g.drawString("Snap: OFF", 10, 20);
    	}
    	
    	switch(m_iGridType)
    	{
            case GRID_TYPE_RECT:
             g.drawString("Grid: CART.", 10, 35);				
            break;
            
            case GRID_TYPE_POLAR:
             g.drawString("Grid: POLAR", 10, 35);	
            break;
    	}	
    }
    
    //*************************************************************************
	
    /*
     * TODO: The entire switch() / if()logic in the drawing code
     * could be freaky. Maybe I should try to improve it...
     * 
     * WARNING: Most of the drawing code here relies on various 
     * tricks (i.e font metrics etc) and magic numbers to place the 
     * decoration elements appropriately. Do not change anything in this
     * section unless you absolutely have to!
     * 
     * FIXME: Take a closer look at fillPolygon()
     * Explanation: apparently, fillPolygon() fills the interior part
     * of the specified polygon, and does not fill the outline, hence
     * all the +1 / -1 offsets in the drawing code for the arrows.
     * 
     * Draw horizontal ticks.
     * FIXME: The <code><<=></code> comparisons in the 
     * <code>while()</code> loops might be numerically unstable
     * when dealing with very small coordinate values! 
     * 
     * @param g rendering context
     */
	
    private void drawHorizontalDecorations(Graphics2D g)
    {	
    	int y_position; // y-coordinate for all decorations
    	
    	// Helper variables for tick label placement
    	Rectangle2D text_bounds; 
    	int size1 = 0, size2 = 0;			
    	int x_offset;
    	
    	// vertical offset of tick labels
    	int y_offset;
    	
    	FontMetrics metrics = g.getFontMetrics();
    	Rectangle2D label_bounds = 
    	 metrics.getStringBounds(m_xAxisLabel,g);
    	
    	//restructuring....
    	if( (m_Origin.y > m_Border.top) && 
    	   ( m_Origin.y < m_iDisplayHeight) )
     	{
            y_position = m_Origin.y;
            
            m_bHitBottomEdge = m_bHitTopEdge = false;
            
            /* These values have been found empirically !*/
            y_offset = g.getFont().getSize() + 
             LARGE_TICK_SIZE + SMALL_TICK_SIZE;
            
            // volles dekor....
            drawHorizontalArrows(g);
            
            //draw X axis label
            if(m_bDrawLabels && (!m_bHitRightEdge))
            {	 		
            	g.drawString(m_xAxisLabel, 
            	             m_iDisplayWidth - 
            	             (int)label_bounds.getWidth(),
            	             m_Origin.y + 
            	             (int)(label_bounds.getHeight() * 1.25));		 		
            }    			
     	}
    	else
    	{
            /* Just the ticks. Some variables
             * need to be adjusted for this.
             */
            
            //FIXME: This logic is risky! Check it!
            if(m_Origin.y <= m_Border.top)
            {
              y_position = m_Border.top;
              m_bHitTopEdge = true;
              
              y_offset = g.getFont().getSize() + 
            		  LARGE_TICK_SIZE + SMALL_TICK_SIZE;
            }
            
            else if(m_Origin.y >= m_iDisplayHeight)
            {    
              y_position = m_iDisplayHeight;			  
              y_offset = -(g.getFont().getSize());
              m_bHitBottomEdge = true; 
            }
            
            else
            {
            	// error trap
            	y_position = m_Origin.y;
            	y_offset   = 0;
            }
    								
    	}
    			
    	if(m_bDrawTicks)
    	{
            double xcoord;
            int    screenx;
            
            // stuff for drawing tick text
            String xtext;
            			
            //draw coarse ticks
            xcoord = _coarse_start_x;
            while(xcoord <= _coarse_end_x + (m_dCoarsePitchX * 0.5))
            {	
            	screenx = worldToScreenX(xcoord);
            	
            	// don't draw zero
            	if(Math.abs(m_Origin.x - screenx) > 1) 
            	{											
                    g.drawLine(screenx, y_position - LARGE_TICK_SIZE, 
                               screenx, y_position + LARGE_TICK_SIZE); 
                    						
                    /*
                     * Draw values at large ticks size1 and size2
                     * variables are used to figure out the
                     * distance from right-most value text to the
                     * axis label, so that the text that would
                     * overlap the label is not drawn.
                     */
                    						
                    xtext = m_Formatter.format(xcoord);									
                    text_bounds = metrics.getStringBounds(xtext,g);
                    
                    /*
                     * If the grid is enabled, the tick values' x
                     * coordinates are shifted a little bit to the
                     * right so that they don't overlap the grid
                     * lines. (at least in the coarse grid mode)
                     */
                    
                    x_offset = (m_iGridVisibility == NO_GRID)?
                               (int)(text_bounds.getWidth()/2):-2;
                    
                    size1 = screenx  + 
                    ((m_iGridVisibility == NO_GRID)?
                     (int)(text_bounds.getWidth() / 2):
                     (int)(text_bounds.getWidth()) );
                    
                    size2 = m_iDisplayWidth - 
                    		(
                    		(m_bHitTopEdge | m_bHitBottomEdge)?
                    		 0 : (int)label_bounds.getWidth());
                    
                    if( (size2 - size1 > 2) )
                    {
                    	g.drawString(xtext,
                    	             screenx - x_offset,
                    	             y_position + y_offset);
                    }
            	}				 					
            	xcoord += m_dCoarsePitchX;		
            }
            
            // draw fine ticks
            
            /*
             * Ok, here's something VERY IMPORTANT: The
             * m_dFinePitchX * 0.5 value below (similar stuff in
             * all other drawing loops) is necessary so that the
             * code doesn't lose ticks near the ends of axes.
             * Reason for this crap: when the coordinate system is
             * zoomed in, the start and end values for tick
             * coordinates become small, and at some point,
             * incrementation of tick position variables in the
             * loops can 'overshoot'. Consequently, the loops
             * might quit earlier than required, and lose the very
             * last tick. [Happens both to large and small ticks!]
             */
            
            xcoord = _fine_start_x;
            while(xcoord <= _fine_end_x + (m_dFinePitchX * 0.5))
            {
            	screenx = worldToScreenX(xcoord);
            	
            	if(Math.abs(m_Origin.x - screenx) > 1)
            	{																
            	    g.drawLine(screenx, y_position - SMALL_TICK_SIZE,
            	               screenx, y_position + SMALL_TICK_SIZE);
            		
            	}
            						
            	xcoord += m_dFinePitchX;					
            }
    		
    	} // if(m_bDrawTicks)
        
    } // drawHorizontalDecorations
    
    //*************************************************************************
	
    private void drawVerticalDecorations(Graphics2D g)
    {
    	//helper variables for tick label placement
    	
    	FontMetrics metrics = g.getFontMetrics();
    	Rectangle2D label_bounds = 
    	 metrics.getStringBounds(m_yAxisLabel,g);
    			
    	Rectangle2D text_bounds;
    	int size1 = 0, size2 = 0;			
    	
    	int y_offset; // vertical offset of tick labels
    	int x_offset; // horizontal offset of tick labels
    	
    	int x_position; // x coordinate for all decorations
    			
    	if((m_Origin.x > m_Border.left) && 
    	  (m_Origin.x < (m_iDisplayWidth)))
    	{
    		
            x_position = m_Origin.x;
            m_bHitRightEdge = m_bHitLeftEdge = false;
            
            /* These values have been found empirically !*/
            x_offset = g.getFont().getSize();
            
            drawVerticalArrows(g);
            
            //draw axis label
            if(m_bDrawLabels && (!m_bHitTopEdge))
            {		 					 		
            	g.drawString(m_yAxisLabel, 
    			     m_Origin.x + 
    			     g.getFontMetrics().charWidth('O'),
    			     (int)(label_bounds.getHeight()));		 		
            }
    	}
    	else
    	{
            /* stick to left edge, place ticks on the right
             * of the edge.
             */			
            if(m_Origin.x <= m_Border.left)
            {
            	x_position = m_Border.left;
            	x_offset   = g.getFont().getSize();
            	m_bHitLeftEdge = true;
            }
            
            /* stick to right edge, place ticks on the 
             * left of the edge.
             */
            else if(m_Origin.x >= m_iDisplayWidth)
            {
            	x_position = m_iDisplayWidth;
            	x_offset   = -(g.getFont().getSize() + LARGE_TICK_SIZE);
            	
            	m_bHitRightEdge = true;
            }
            else
            {
            	//error trap
            	x_position = m_Origin.x;
            	x_offset   = 0;
            }

    	}
    			
    	if(m_bDrawTicks)
    	{
    
            double ycoord;
            int screeny;
            
            // stuff for drawing tick text
            String ytext;
            		
            size2 = (int)label_bounds.getHeight() + 0;
            
            //draw coarse ticks
            ycoord = _coarse_start_y;
            while(ycoord <= _coarse_end_y + (m_dCoarsePitchY * 0.5))
            {	
            	screeny = worldToScreenY(ycoord);
            	
            	// don't draw zero
            	if(Math.abs(m_Origin.y - screeny) > 1) 
            	{						
                    g.drawLine(x_position - LARGE_TICK_SIZE, screeny,  
                               x_position + LARGE_TICK_SIZE, screeny); 
                    							
                    ytext = m_Formatter.format(ycoord);				
                    text_bounds = metrics.getStringBounds(ytext,g);
                    
                    if(m_bHitRightEdge)
                    {
                    	// have to shift text LEFT of the axis
                    	x_offset = - ((int)text_bounds.getWidth() +
                    	             metrics.getFont().getSize());
                    }
                    					
                    /*
                     * When grid is enabled, the tick value's y
                     * coordinates are shifted a little bit lower
                     * so that they don't overlap the grid lines.
                     */
                    
                    y_offset = 
                     (m_iGridVisibility == NO_GRID)?
                     ((int)(text_bounds.getHeight()/2.0)-3):
                      (int)(text_bounds.getHeight() * 0.75);
                    
                    size1 = screeny -  
                    (			
                     (m_bHitLeftEdge | m_bHitRightEdge) ?
                      0 : (int)(text_bounds.getHeight() / 2.0));
                    
                    /*
                     * This if() statement works similarly as the
                     * one in drawHorizontalDecorations().
                     * However, the constants in this case a
                     * little different, due to some peculiarities
                     * in the way Java draws text! IXDA!
                     */
                    
                    if( size1 - size2 > 0 )
                    {
                    	g.drawString(ytext,							 
                    	             x_position + x_offset,
                    	             screeny + y_offset);
                    }
            	}				 					
            	ycoord += m_dCoarsePitchY;		
            }
            
            // draw fine ticks
            			
            ycoord = _fine_start_y;
            while(ycoord <= _fine_end_y + (m_dFinePitchY * 0.5))
            {
            	screeny = worldToScreenY(ycoord);
            	
            	if(Math.abs(m_Origin.y - screeny) > 1)
            	{																
                    g.drawLine(x_position - SMALL_TICK_SIZE, screeny,  
                               x_position + SMALL_TICK_SIZE, screeny); 	
            	}
            						
            	ycoord += m_dFinePitchY;					
            }
    		
    	} // if(drawTicks)
    		
    } // drawVerticalDecorations
	
    //*************************************************************************
    
    /*
     * Draws the rectangular grid. 
     */
    private void drawRectangularGrid(Graphics2D g)
    {
    	if(m_iGridVisibility == NO_GRID) return; // bail...
    			
    	int screenx = 0, screeny = 0;
    	
    	double startx, endx;
    	double starty, endy;
    	
    	double pitchx = (m_iGridVisibility == COARSE_GRID)?
    			 m_dCoarsePitchX:m_dFinePitchX;
    	
    	double pitchy = (m_iGridVisibility == COARSE_GRID)?
    			 m_dCoarsePitchY:m_dFinePitchY;
    	
    	startx = 
    	 Math.floor(screenToWorldX(0) / pitchx) * pitchx;
    	
    	endx = 
    	  Math.ceil(screenToWorldX(m_iDisplayWidth) / pitchx) * pitchx;
    	
    	endy = 
         Math.floor(screenToWorldY(0) / pitchy) * pitchy;
    		
    	starty = 
    	  Math.ceil(screenToWorldY(m_iDisplayHeight) / pitchy) * pitchy;
    	
    	_grid_start_x      = startx;
    	_grid_end_x        = endx;
    	_grid_start_y      = starty;
    	_grid_end_y	   = endy;
    					
    	// vertical lines
    	while(startx <= endx)
    	
    	{
            screenx = worldToScreenX(startx);
            
            g.drawLine(screenx, 0, 
                       screenx, m_iDisplayHeight);
            
            startx += pitchx;
    	}
    	
    	// horizontal lines				
    	while(starty <= endy)
    	{
            screeny = worldToScreenY(starty);
            
            g.drawLine(0, screeny,
                       m_iDisplayWidth, screeny);
    		
    	    starty += pitchx;				
    	}
    }
    
    //*************************************************************************
	
    private void drawPolarGrid(Graphics2D g)
    {
    	if(m_iGridVisibility == NO_GRID) return; // bail...
    	
    	int end, radius;
    	
    	double x, prev_x;
    	double pitch;
    	
    	int pixel_radius;
    	int pixel_pitch;
    			
    	pitch = (m_iGridVisibility == COARSE_GRID)? 
                m_dCoarsePitchX : m_dFinePitchX;
    		
    	pixel_pitch = worldToScreenX(pitch) - worldToScreenX(0.0);
    	
    	/* calculate distances from the origin to four corners 
    	 * of the display, and use the maximum of these as the
    	 * limit for the radii of grid circles (in pixel space)
    	 */
    			
    	// upper - left
    	int dist1 = (m_Origin.x - m_Border.left) * 
    		    (m_Origin.x - m_Border.left) +
    		    (m_Origin.y - m_Border.top) *
    		    (m_Origin.y - m_Border.top);
    	
    	// upper - right
    	int dist2 = (m_Origin.x - m_iDisplayWidth) * 
    		    (m_Origin.x - m_iDisplayWidth) +
    		    (m_Origin.y - m_Border.top) *
    		    (m_Origin.y - m_Border.top);
    	
    	// lower - left
    	int dist3 = (m_Origin.x - m_Border.left) * 
    		    (m_Origin.x - m_Border.left) +
    		    (m_Origin.y - m_iDisplayHeight) *
    		    (m_Origin.y - m_iDisplayHeight);
    	
    	// lower-right
    	int dist4 = (m_Origin.x - m_iDisplayWidth) * 
    		    (m_Origin.x - m_iDisplayWidth) +
    		    (m_Origin.y - m_iDisplayHeight) *
    		    (m_Origin.y - m_iDisplayHeight);
    	
    	radius = Math.max(Math.max(dist1,dist2), 
    			  Math.max(dist3,dist4));
    	
    	_polar_grid_radius = screenToWorldX(radius) - screenToWorldX(0);
    	
    	radius = (int)Math.sqrt(radius);	
    		
    	pixel_radius = pixel_pitch;
    	end  = radius;
    	
    	x = pitch; prev_x = 0.0;
    	
    	/*
    	 * Draw grid circles. The circles themselves are drawn
    	 * directly in screen space, but the pixel pitch is always
    	 * calculated as the distance between the transformed x
    	 * coordinates of adjacent large ticks. This is necessary
    	 * because some pixel positions are rounded during rendering
    	 * which makes the use of a constant pitch impossible.
    	 */
    	
    	while(pixel_radius <= end)
    	{		
            pixel_pitch = worldToScreenX(x) - worldToScreenX(prev_x);
            			
            g.drawOval(m_Origin.x - pixel_radius, 
                       m_Origin.y - pixel_radius,
                       2 * pixel_radius, 2 * pixel_radius);
            			
            pixel_radius += pixel_pitch;
            
            prev_x = x;
            x += pitch;
    	}
    	
    	/*
    	 * Draw grid lines. These are drawn in polar coordinates Note
    	 * that I used a little trick here so that 0 degrees is at
    	 * 12 o'clock : the trig functions for x and y coordinates are
    	 * swapped !
    	 */
    			
    	int xcoord = 0, ycoord = 0;
    	
    	for(int i = 0; i < m_dSines.length; i++)
    	{
            xcoord = (int)(radius * m_dSines[i]); 
            ycoord = (int)(radius * m_dCosines[i]); 
            
            g.drawLine(m_Origin.x, m_Origin.y, 
                       m_Origin.x + xcoord,
                       m_Origin.y - ycoord);
    	}
    	
    }
    
    //*************************************************************************
	
    private void drawHorizontalArrows(Graphics2D g)
    {
    	switch(m_iArrowType)
    	{
            case NO_ARROWS:
            break;
            			
            case POSITIVE_ARROWS:
            
            	_px[0] = m_iDisplayWidth + m_Border.right - ARROW_WIDTH;
            	_px[1] = m_iDisplayWidth + m_Border.right;
            	_px[2] = m_iDisplayWidth + m_Border.right - ARROW_WIDTH;
            	
            	_py[0] = m_Origin.y - ARROW_HEIGHT;
            	_py[1] = m_Origin.y;
            	_py[2] = m_Origin.y + ARROW_HEIGHT;
            		 
            	if(!m_bHitRightEdge)
            	{			
            	    g.fillPolygon(_px, _py, 3);
            	}
            																			
            break;
            
            case BOTH_ARROWS:
            	
            	_px[0] = m_iDisplayWidth + m_Border.right - ARROW_WIDTH;
            	_px[1] = m_iDisplayWidth + m_Border.right;
            	_px[2] = m_iDisplayWidth + m_Border.right - ARROW_WIDTH;
            	
            	_py[0] = m_Origin.y - ARROW_HEIGHT;
            	_py[1] = m_Origin.y;
            	_py[2] = m_Origin.y + ARROW_HEIGHT;
            				 
            	if(!m_bHitRightEdge)
            	{			
            	    g.fillPolygon(_px, _py, 3);
            	}
            	
            	_px[0] = m_Border.left + ARROW_WIDTH - 1;
            	_px[1] = m_Border.left - 1;
            	_px[2] = m_Border.left + ARROW_WIDTH - 1;
            	
            	if(!m_bHitLeftEdge)
            	{			
            	    g.fillPolygon(_px, _py, 3);
            	}
            	
            break;							
    	} // switch
    	
    }
    
    //*************************************************************************
	
    private void drawVerticalArrows(Graphics2D g)
    {
    	switch(m_iArrowType)
    	{
            case NO_ARROWS:
            break;
            
            case POSITIVE_ARROWS:
            	
            	_px[0] = m_Origin.x - ARROW_HEIGHT;
            	_px[1] = m_Origin.x;
            	_px[2] = m_Origin.x + ARROW_HEIGHT;
            	
            	_py[0] = m_Border.top + ARROW_WIDTH - 1;
            	_py[1] = m_Border.top - 1;
            	_py[2] = m_Border.top + ARROW_WIDTH - 1;
            	
            	if(!m_bHitTopEdge)
            	{			
            	    g.fillPolygon(_px, _py, 3);
            	}
            								
            break;
            
            case BOTH_ARROWS:
            	
            	_px[0] = m_Origin.x - ARROW_HEIGHT;
            	_px[1] = m_Origin.x;
            	_px[2] = m_Origin.x + ARROW_HEIGHT;
            	
            	_py[0] = m_Border.top + ARROW_WIDTH - 1;
            	_py[1] = m_Border.top - 1;
            	_py[2] = m_Border.top + ARROW_WIDTH - 1;
            	
            	if(!m_bHitTopEdge)
            	{			
            	    g.fillPolygon(_px, _py, 3);
            	}
            	
            	_py[0] = m_iDisplayHeight + m_Border.bottom - ARROW_WIDTH;
            	_py[1] = m_iDisplayHeight + m_Border.bottom;
            	_py[2] = m_iDisplayHeight + m_Border.bottom - ARROW_WIDTH;
            	
            	if(!m_bHitBottomEdge)
            	{			
            	    g.fillPolygon(_px, _py, 3);	
            	}
            	
            break;			
    	} // switch 
    }
    
    //*************************************************************************
		
    /* Common initialization of members variables for all constructors etc.
     * 
     */
    private void initVars()
    {
    			
    	NumberFormat ff = NumberFormat.getInstance();
    	try
    	{
            m_Formatter = (DecimalFormat)ff;
            m_Formatter.setMaximumFractionDigits(5);				
    	}
    	catch(ClassCastException ex)
    	{
            System.err.println("Decimal number formatter im ARSCH !!!!");
            System.err.println("No really, couldn't cast " +
            		       "the NumberFormat instance to" +
            		       "DecimalFormat!");
            
            System.exit(-1); // for now...
    	}
    	
    	m_ActionListeners = new Vector();
    	
    	m_Origin = m_zoomPixel = null; 
    	m_pressedPoint = null;
    	m_iMouseButton = 0;
    	m_dPixelsPerX = m_dPixelsPerY = 0.0;
    	m_dCoarsePitchX = m_dCoarsePitchY = 0.0;
    	m_dFinePitchX = m_dFinePitchY = 0.0;
    	
    	m_Origin = new Point();
    	m_affineXForm = new AffineTransform();
    	m_zoomPoint = new Point2D.Double();
    	
    	m_worldRectangle = new Rectangle2D.Double();
    
    	// temporary polygon coordinates
    	_px = new int[3];
    	_py = new int[3];
    	
    	_coarse_start_x = _coarse_end_x = 
    	_coarse_start_y = _coarse_end_y = 0;
    	
    	m_dSines = m_dCosines = null;
    	
    	_temp_point = new Point2D.Double();
    	
    	m_bHitBottomEdge   = false;
    	m_bHitTopEdge 	   = false;
    	m_bHitLeftEdge 	   = false;
    	m_bHitRightEdge    = false;
    }
    
    //*************************************************************************
	
    private void saveSettings()
    {
    	// so far...
    	m_dInitialPixelsPerX   = m_dPixelsPerX;
    	m_dInitialPixelsPerY   = m_dPixelsPerY;
    	m_dInitialCoarsePitchX = m_dCoarsePitchX;
    	m_dInitialCoarsePitchY = m_dCoarsePitchY;
    	m_dInitialFinePitchX   = m_dFinePitchX;
    	m_dInitialFinePitchY   = m_dFinePitchY; 
    	
    	m_dInitialExtentsX     = m_dSceneExtentsX;
    	m_dInitialExtentsY     = m_dSceneExtentsY;	
    	
    }
    
    //*************************************************************************
	
    /* This initializes the m_Sines and m_Cosines arrays 
     * with the values of the corresponding trigonometric functions
     * for subsequent use in drawing the polar grid. This approach
     * is more efficient than calculating these values on the
     * fly because trigonometry is slow (in JAVA... LOL)
     * 
     */
    
    private void precalculateTrig()
    {
    	// dump old contents
    	m_dSines = null; 
    	m_dCosines = null;
    	System.gc();
    	
    	// init and populate arrays with new values
    	
    	/*
    	 * This particular line may be a bit suspicions, in situations
    	 * where we have an angle step of say, 7, through which 360
    	 * will not divide evenly. However, this should still work
    	 * correctly as the number of values essentially determines
    	 * the number of grid lines which is an integer, too.  
    	 */	
    	int num_values = 360 / m_iAnglePitch;
    	
    	m_dSines   = new double[num_values];
    	m_dCosines = new double[num_values];
    	
    	for(int i = 0; i < num_values; i++)
    	{
            m_dSines[i] = Math.sin(Math.toRadians(m_iAnglePitch * i));
            m_dCosines[i] = Math.cos(Math.toRadians(m_iAnglePitch * i));
    	} 	
    }
    
    //*************************************************************************
	
    /*
     * Set up the user interface
     */
    private void createUI()
    {
    	createMenus();
    	createToolBox();
    }
    
    //*************************************************************************
	
    /*
     * Create, initialize and glue together the menu
     * structure
     * 
     */
    private void createMenus()
    {
    	// menus
    	m_uiMenu = new JMenu("Coordinate system");
    	
    	m_uiGridVisibilitySubMenu = new JMenu("Grid");
    	m_uiGridTypeSubMenu  	  = new JMenu("Grid type");
    	m_uiGridModeSubMenu 	  = new JMenu("Grid mode");
    	
    	ButtonGroup grid_vis  = new ButtonGroup();
    	ButtonGroup grid_type = new ButtonGroup();
    	ButtonGroup grid_mode = new ButtonGroup();
    		
    	// show / hide item
    	m_uiEnableBox = 
    		new JCheckBoxMenuItem("Show coordinate system", true);
    	m_uiEnableBox.setActionCommand(String.valueOf(CMD_ENABLE));
    	m_uiEnableBox.addActionListener(this);
        
        // no decorations item
        m_uiNoDecorBox = 
            new JCheckBoxMenuItem("No axis decorations", false);
        m_uiNoDecorBox.setActionCommand(String.valueOf(CMD_NO_DECORATIONS));
        m_uiNoDecorBox.addActionListener(this);
    			
    	// grid visibility options
    	m_uiGridNoneItem = new JRadioButtonMenuItem("None", true);
    	m_uiGridNoneItem.setActionCommand(String.valueOf(CMD_GRID_NONE));
    	m_uiGridNoneItem.addActionListener(this);
    	
    	m_uiGridCoarseItem = new JRadioButtonMenuItem("Coarse");
    	m_uiGridCoarseItem.setActionCommand(String.valueOf(CMD_GRID_COARSE));
    	m_uiGridCoarseItem.addActionListener(this);
    	
    	m_uiGridFineItem = new JRadioButtonMenuItem("Fine");
    	m_uiGridFineItem.setActionCommand(String.valueOf(CMD_GRID_FINE));
    	m_uiGridFineItem.addActionListener(this);
    			
    	grid_vis.add(m_uiGridCoarseItem); 
    	grid_vis.add(m_uiGridFineItem);
    	grid_vis.add(m_uiGridNoneItem);
    	
    	m_uiGridVisibilitySubMenu.add(m_uiGridNoneItem);
    	m_uiGridVisibilitySubMenu.add(m_uiGridCoarseItem);
    	m_uiGridVisibilitySubMenu.add(m_uiGridFineItem);
    	
    	// grid type options
    	m_uiCartGridItem = new JRadioButtonMenuItem("Cartesian");
    	m_uiCartGridItem.setActionCommand(String.valueOf(CMD_TYPE_CARTESIAN));
    	m_uiCartGridItem.addActionListener(this);
    	
    	m_uiPolarGridItem = new JRadioButtonMenuItem("Polar");
    	m_uiPolarGridItem.setActionCommand(String.valueOf(CMD_TYPE_POLAR));
    	m_uiPolarGridItem.addActionListener(this);
    	
    	grid_type.add(m_uiCartGridItem); 
    	grid_type.add(m_uiPolarGridItem);
    	m_uiGridTypeSubMenu.add(m_uiCartGridItem);
    	m_uiGridTypeSubMenu.add(m_uiPolarGridItem);
    	
    	// grid mode options
    	m_uiGridDotsItem = new JRadioButtonMenuItem("Dots");
    	m_uiGridDotsItem.setActionCommand(String.valueOf(CMD_MODE_DOTS));
    	m_uiGridDotsItem.addActionListener(this);
    	
    	m_uiGridLinesItem = new JRadioButtonMenuItem("Lines", true);
    	m_uiGridLinesItem.setActionCommand(String.valueOf(CMD_MODE_LINES));
    	m_uiGridLinesItem.addActionListener(this);
    	
    	grid_mode.add(m_uiGridDotsItem); grid_mode.add(m_uiGridLinesItem);
    	m_uiGridModeSubMenu.add(m_uiGridDotsItem);
    	m_uiGridModeSubMenu.add(m_uiGridLinesItem);
    	
    	// color options etc.
    	m_uiSnapBox = new JCheckBoxMenuItem("Snap to grid", false);
    	m_uiSnapBox.setActionCommand(String.valueOf(CMD_SNAP_TO_GRID));
    	m_uiSnapBox.addActionListener(this);
    	
    	m_uiAxesColorItem = new JMenuItem("System color..");
    	m_uiAxesColorItem.setActionCommand(String.valueOf(CMD_AXES_COLOR));
    	m_uiAxesColorItem.addActionListener(this);
    	
    	m_uiGridColorItem = new JMenuItem("Grid color..");
    	m_uiGridColorItem.setActionCommand(String.valueOf(CMD_GRID_COLOR));
    	m_uiGridColorItem.addActionListener(this);
    	
    	m_uiResetItem = new JMenuItem("Reset");
    	m_uiResetItem.setActionCommand(String.valueOf(CMD_RESET));
    	m_uiResetItem.addActionListener(this);
    	
    	m_uiCenterItem = new JMenuItem("Center!");
    	m_uiCenterItem.setActionCommand(String.valueOf(CMD_CENTER));
    	m_uiCenterItem.addActionListener(this);
    	
    	SpinnerNumberModel ticks = 
    		new SpinnerNumberModel(m_iTicksPerUnit,
    				        MIN_TICKS_PER_UNIT,
    					MAX_TICKS_PER_UNIT,
    					1);
    													
    	m_uiTicksPerUnitItem = new JSpinner(ticks);
    	
    	/* a JSpinner needs a ChangeListener => I can't add it 
    	 * to the common actionPerformed() method in this class,
    	 * so I implemented it a local class here.  LAME!
    	 * FIXED: implemented a global stateChanged() method
    	 * 
    	 */
    	m_uiTicksPerUnitItem.addChangeListener(this);
    	
    	/*
    	 * In order to ensure a certain 'beauty' of the polar grid
    	 * mode, I've constrained the angle step to a small list of
    	 * some most commonly used values. All angles are in degrees.
    	 */
    	
    	Vector angle_values = new Vector();
    	angle_values.add(new Integer(5));
    	angle_values.add(new Integer(10));
    	angle_values.add(new Integer(15));
    	angle_values.add(new Integer(20));
    	angle_values.add(new Integer(30));
    	angle_values.add(new Integer(40));
    	angle_values.add(new Integer(45));
    	angle_values.add(new Integer(60));
    	angle_values.add(new Integer(72));
    	angle_values.add(new Integer(90));
    	angle_values.add(new Integer(120));
    	
    	SpinnerListModel angle_step = new SpinnerListModel(angle_values);	
    	m_uiAngleStepItem	    = new JSpinner(angle_step);
    	
    	//angle_step.setValue(new Integer(30));
    	m_uiAngleStepItem.addChangeListener(this);
    		
    	// put everything together
    	m_uiMenu.add(m_uiEnableBox);
        m_uiMenu.add(m_uiNoDecorBox);
    	m_uiMenu.addSeparator();
    	
    	m_uiMenu.add(m_uiGridVisibilitySubMenu);
    	m_uiMenu.add(m_uiGridTypeSubMenu);
    	m_uiMenu.add(m_uiGridModeSubMenu);
    	m_uiMenu.addSeparator();
    	
    	m_uiMenu.add(m_uiSnapBox);
    	m_uiMenu.add(m_uiAxesColorItem);
    	m_uiMenu.add(m_uiGridColorItem);
    	m_uiMenu.add(m_uiResetItem);
    	m_uiMenu.add(m_uiCenterItem);
    	
    	m_uiMenu.addSeparator();
    	m_uiMenu.add(new JLabel("Ticks per unit"));
    	m_uiMenu.add(m_uiTicksPerUnitItem);
    			
    	m_uiMenu.add(new JLabel("Angle pitch"));
    	m_uiMenu.add(m_uiAngleStepItem);
    	
    }
    
    //*************************************************************************
	
    private void createToolBox()
    {
    	// set up the toolbar
    	
    	m_uiToolBox = new JToolBar("Coordinate system settings");
    	
    	URL url;
    	url = getClass().getResource("icons/coord_icon_24.png");	
    	ImageIcon coord_icon = new ImageIcon(url);
    	
    	JToggleButton enable_button = 
    		new JToggleButton(coord_icon, m_bVisible);
        
    	enable_button.setToolTipText("Show/hide visual aids");
    	enable_button.setActionCommand(String.valueOf(CMD_ENABLE));
    	enable_button.addActionListener(this);
    	
    	m_uiToolBox.add(enable_button);
    	
    	m_uiToolBox.setFloatable(false);
    }
	

}

