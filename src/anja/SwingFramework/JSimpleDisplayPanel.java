/*
 * Created on Oct 8, 2004
 *
 * JSimpleDisplayPanel.java
 */

package anja.SwingFramework;

import java.io.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.text.*; // for DecimalFormat

//import anja.geom.*;
//import anja.gui.*;

import org.jdom.*;

/**
 * A lightweight general-purpose two-dimensional display. This
 * component posesses a built-in visual coordinate system, as well as
 * other visual aids that can be turned on and off. The display area
 * is double-buffered by default.
 * 
 * <p>Mouse and keyboard events may be passed on to the editor
 * application as required. (See method descriptions for details.)
 * 
 * <p><b>Note </b>:This is a design similar to JDisplayPanel written by
 * Sascha Ternes but with one significant difference: it doesn't
 * belong to the JDisplayPanel-Scene-Editor system and thus can't be
 * used to render 'Scenes'!
 * 
 * 
 * @author Ibragim Kuliyev
 * 
 * <p>TODO: Guidelines would be a nice feature 
 * <br>TODO: Grid drawing in CoordinateSystem may be inefficient 
 * <br>FIXME: Remaining eventualities of VolatileImage buffer
 * 
 */
public class JSimpleDisplayPanel extends JPanel implements
						ComponentListener,
						MouseListener,
						MouseMotionListener,
						MouseWheelListener,
						ActionListener,
						KeyListener
{
    //*************************************************************************
    // 	 			   Public constants
    //*************************************************************************
	
    /**
     * The name for a JDOM Element containing the view settings
     */
    public static final String XML_SETTINGS_NAME  = "View_settings";
    
    //*************************************************************************
    //                             Private constants
    //*************************************************************************
	
	
    private static final int CMD_HW_ACCEL   = 0x01;
    private static final int CMD_CROSSHAIR  = 0x02;
    private static final int CMD_BACK_COLOR = 0x03;
    private static final int CMD_ANTIALIAS  = 0x04;
    private static final int CMD_SAVE_IMG   = 0x05;
    
    //----------------- XML attribute names for view settings -----------------
    
    private static final String XML_BGCOLOR_NAME   = "Background_color";
    private static final String XML_CROSSHR_NAME   = "Use_crosshair";
    private static final String XML_ANTIALIAS_NAME = "Use_antialiasing";
	
    //*************************************************************************
    //	 			   Class variables
    //*************************************************************************
    
    //*************************************************************************
    //	                       Private instance variables
    //*************************************************************************
	
    //--------------------------------- Data ----------------------------------
    
    // component data
    protected JSystemHub 		m_Hub;
    protected CoordinateSystem          m_coordinateSystem;
    protected JAbstractEditor           m_Editor;
    protected JAbstractScene            m_Scene;
    
    // Off-screen buffer and associated graphics context
    protected VolatileImage             m_hardwareBackBuffer;
    protected BufferedImage             m_backBuffer;
    protected Graphics2D                m_Renderer;
    
    protected boolean 			m_bUseHardwareBuffer;
    protected boolean 			m_bUseAntialiasing;
    	
    // mouse coordinates, buttons & modifiers
    protected Point 			m_mousePosition; 
    protected int 			m_iMouseButton;
    protected int			m_iMouseModifiers;
    	
    protected Point2D.Double            m_worldMousePosition;
	
    // etc.	
    protected AffineTransform 	        m_viewTransform;
    
    // ----------------------- UI elements & flags ----------------------------
    
    protected boolean 			m_bDrawCrossHairs;
    
    protected Cursor 			m_defaultCursor;
    protected Cursor 			m_panCursor;
    protected Cursor 			m_zoomCursor;
    
    protected JMenu 		    	m_viewOptionsMenu;
    
    protected JCheckBoxMenuItem 	m_uiCrossItem;
    protected JCheckBoxMenuItem 	m_uiAntialiasItem;
    protected JCheckBoxMenuItem 	m_uiHwBufferItem;
	
		
    //----------------------- Drawing attributes etc. -------------------------
    
    private   BasicStroke		m_dashStroke;
    protected Color	 	  	m_backgroundColor;
    private   Color 	  		m_crossHairColor;
    private   Color		 	m_foregroundColor;
    
    private   DecimalFormat             m_Formatter; // number formatter
    
    // temporary testing variables
    /*
    private Point2D.Double              _testPoint;
    private Circle2                     _testCircle;
    private GraphicsContext             _testGC;
    private Rectangle2D 	        _test_rect;
    private boolean 		        _culled;*/
	
    // temporary profiling stuff
    private int 	           _frame_counter = 0;
    private int 	           _x_position    = 0; // histogram position
    private int[]                  _prev_values = new int[50]; 
	
    //*************************************************************************
    //	 			    Constructors
    //*************************************************************************
	
    /**
     * Constructs a new display panel with default parameters -
     * no crosshairs, white background and antialiasing.
     * 
     * 
     * @param hub an instance of JSystemHub
     */	
    public JSimpleDisplayPanel(JSystemHub hub)
    {		
    	super(false); // still don't know about this
    	
    	m_bUseHardwareBuffer = true; // off by default
		m_bUseAntialiasing = true;
    	this.setFocusable(true);
    	
    	m_Hub = hub;		
    						
    	// create UI elements
    	m_mousePosition            = new Point();
    	m_worldMousePosition       = new Point2D.Double();
    			
    	m_defaultCursor            = new Cursor(Cursor.DEFAULT_CURSOR);
    	m_panCursor 	           = new Cursor(Cursor.MOVE_CURSOR);
    	m_zoomCursor 	           = new Cursor(Cursor.N_RESIZE_CURSOR);
    			
    	// initialize references to system components
    	m_coordinateSystem         = m_Hub.getCoordinateSystem();
    	m_Editor 		   = m_Hub.getEditor();
    	m_Scene 		   = m_Hub.getScene();
    	
    	// init number formatter		
    	NumberFormat ff = NumberFormat.getInstance();		
    	m_Formatter = (DecimalFormat)ff;
    	m_Formatter.setMinimumFractionDigits(4);
    	m_Formatter.setMaximumFractionDigits(4);
    			
    	//set component parameters;
    	setCursor(m_defaultCursor);
    			
        // setup rendering attributes for visual aids
        m_foregroundColor = Color.black;
    	m_crossHairColor  = Color.black;
    	m_bDrawCrossHairs = false; 	
    	
        float dash[] = {4.0f};
        m_dashStroke = new BasicStroke(	1.0f,
				 	BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER,
					1.0f, dash, 0.0f );
    	
    	// register event handlers
    	addMouseListener(this);
    	addMouseMotionListener(this);
    	addMouseWheelListener(this);
    	addComponentListener(this);
    	addKeyListener(this);
    	
    	// necessary for coordinate system key bindings
    	addKeyListener(m_coordinateSystem);
    	addKeyListener(m_Editor);
    		
    	createUI();
    	
    	// initialization complete!
    	m_Hub.setDisplay(this);	
    }
	
    //*************************************************************************
    // 			       Public instance methods
    //*************************************************************************
	
    /**
     * Used internally by the 
     * {@link JSystemHub#updateTransform(AffineTransform)}
     * <br>synchronization method to sync the display panel to the 
     * changes in the coordinate system.
     * 
     * @param tx an affine transform to be set
     */
    public void updateAffineTransform(AffineTransform tx)
    {
    	m_viewTransform = tx;
    }
    
    //*************************************************************************
	
    /**
     * Returns the menu that controls the appearance of the
     * display panel. <br>Can be inserted into a {@link JMenuBar}
     * in the parent window.
     * 
     * @return display panel menu
     */
    public JMenu getMenus()
    {    
    	return m_viewOptionsMenu;	
    }
    
    //*************************************************************************
		
    /**
     * Can be used to set the crosshair color externally.
     * 
     * @param c new crosshair color
     */
    public void setCrossHairColor(Color c)
    { 
    	m_crossHairColor = c; 
    }
    
    //*************************************************************************
	
    /**
     * Enables/disables the crosshair.
     * 
     * @param on set to <code>true</code> to enable crosshair,
     * or to <code>false</code> to disable it.
     */
    public void enableCrossHair(boolean on)
    { 
    	m_bDrawCrossHairs = on;
    	
    	m_uiCrossItem.setSelected(on); // sync UI
    			
    	if( (this.getHeight() != 0) && (this.getWidth() != 0) )
    	 repaint();	
    }
    
    //*************************************************************************
	
    /**
     * Sets the background color.
     * 
     * @param c new background color
     */
    public void setBackgroundColor(Color c)
    {
    	m_backgroundColor = c;
    	repaint();
    }
    
    //*************************************************************************
	
    /**
     * Sets the foreground color
     * 
     * @param c new foreground color
     */
    public void setForegroundColor(Color c)
    {
    	m_foregroundColor = c;
    	repaint();
    }
    
    //*************************************************************************
		
    /**
     * Enables / disables antialiasing.
     *  
     * @param on
     */
    public void enableAntialiasing(boolean on)
    {
    	m_bUseAntialiasing = on;
    	
    	if(on)
    	{
            m_Renderer.
             setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_ON);
    	}
    	else
    	{
            m_Renderer.
             setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_OFF);
    	}
    	
    	m_uiAntialiasItem.setSelected(on);
    	
    	if( (this.getHeight() != 0) && (this.getWidth() != 0) )
    	 repaint();		
    }
    
    //*************************************************************************

    /** Reimplemented from {@link JPanel} */
    public void repaint()
    {
    	render();
    	super.repaint();
    }
    
    //*************************************************************************
	
    /**
     * Overrides the default paintComponent() method of JPanel. It is
     * important to override this method, and not the paint() method,
     * because paint() is also responsible for correctly drawing the
     * component's children and border, and essentially invokes
     * paintComponent() at the appropriate time.
     * 
     * <p>TODO: might need multi-threading here if the event handlers
     * become sufficiently complicated. Will need to take a look at
     * invokeLater() in SwingUtilities.
     */
	
    public void paintComponent(Graphics g)
    {	
    	/*
    	 * For now, blit the entire back buffer - I'll try to fix
    	 * the border overdraw problem by other means...
    	 * 
    	 */
    			
    	// blit back buffer onto screen
    	if(m_bUseHardwareBuffer)
    	{
    	  // use hardware-accelerated buffer
    	  g.drawImage(m_hardwareBackBuffer, 0, 0, this); 
    	}
    	else
    	{
    	  // use normal buffer
    	  g.drawImage(m_backBuffer, 0, 0, this);
    	}
    }
    
    //*************************************************************************
	
    /**
     * Serializes the display panel settings and outputs
     * them as a JDOM element. This method is used to by 
     * the save routine in JAbstractEditor (but could 
     * be used anywhere else, as well).
     * 
     * @return A new JDOM Element containing the view settings
     */
    
    public Element saveSettingsToXML()
    {
    	// create a new element and store settings into it
    	Element settings = new Element(XML_SETTINGS_NAME);
    	
    	Attribute bgcol	   = new Attribute(XML_BGCOLOR_NAME,
    					    String.valueOf(
                                             m_backgroundColor.getRGB()));
        
    	Attribute usecross = new Attribute(XML_CROSSHR_NAME,
                                           String.valueOf(
                                                   m_bDrawCrossHairs));
    	
    	Attribute antialias = new Attribute(XML_ANTIALIAS_NAME,
                                            String.valueOf(
                                                    m_bUseAntialiasing));
    	
    	settings.setAttribute(bgcol);
    	settings.setAttribute(usecross);
    	settings.setAttribute(antialias);
    	
    	return settings;
    }
    
    //*************************************************************************
	
    /**
     * Reads the settings from a JDOM Element and restores
     * them.
     * 	  
     * @param settings A JDOM Element to read the settings from
     */
    public void loadSettingsFromXML(Element settings)
    {
    	// parse attributes and modify the view settings
    	Attribute bgcolor = settings.getAttribute(XML_BGCOLOR_NAME);
    	Attribute crosshr = settings.getAttribute(XML_CROSSHR_NAME);
    	Attribute antials = settings.getAttribute(XML_ANTIALIAS_NAME);
    	
    	try
    	{
            Color c = new Color(bgcolor.getIntValue());
            setBackgroundColor(c);
            
            enableCrossHair(crosshr.getBooleanValue());
            enableAntialiasing(antials.getBooleanValue());
    	}
    	catch(DataConversionException ex)
    	{
            System.err.println("Loading settings from XML failed, " +
            	               "cause:" + ex.getMessage() +
            	               "\nReverting to default settings!");
            
            //revert to default settings
            setBackgroundColor(Color.white);
            enableCrossHair(false);
            enableAntialiasing(false);
    	}	
    }
	
    //*************************************************************************
    // 				    Event handlers
    //*************************************************************************
	
    /**
     * Performs action handling for the menus.
     * 
     */	
    public void actionPerformed(ActionEvent event)
    {
    	// handle "View" menu items 
    	String cmd = event.getActionCommand();
    	int cmd_code = Integer.parseInt(cmd);
    	
    	switch(cmd_code)
    	{
            case CMD_ANTIALIAS:
             enableAntialiasing(m_uiAntialiasItem.isSelected());
            break;
            
            case CMD_CROSSHAIR:
             enableCrossHair(m_uiCrossItem.isSelected());
            break;
            
            case CMD_BACK_COLOR:
            	
             Color c = 
            	  JColorChooser.showDialog(null,
            	                          "Select new background color",
            	                          Color.WHITE);
            
             // only if dialog was not closed with "Cancel"
             if(c != null)
              setBackgroundColor(c);
             
            break;
            
            case CMD_HW_ACCEL:
            break;
            
            case CMD_SAVE_IMG:
             saveImage();
            break;    		
    	}	
    }
    
    //*************************************************************************
	
    public void componentHidden(ComponentEvent event)
    {
    }
    
    //*************************************************************************
    
    public void componentMoved(ComponentEvent event)
    {	
    }
    
    //*************************************************************************
    
    /* (non-Javadoc)
     * 
     */
    public void componentShown(ComponentEvent event)
    {       
            m_coordinateSystem.resize();                                        
    }
    
    //*************************************************************************
    
	
    /* (non-Javadoc)
     *
     */
    public void componentResized(ComponentEvent event)
    {		
    	m_coordinateSystem.resize();
    							
    	if(m_bUseHardwareBuffer)
    	{
            //first-time off-screen buffer init
            if(m_hardwareBackBuffer == null)
            {				
            	m_hardwareBackBuffer = 
            	 createVolatileImage(this.getWidth(),this.getHeight());				
            	
            	m_Renderer = m_hardwareBackBuffer.createGraphics();
             	if(m_bUseAntialiasing)
            	 m_Renderer.setRenderingHint(
                             RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);           }
            else
            {
            	//reshape the off-screen buffer
            	m_Renderer.dispose();
            	m_hardwareBackBuffer.flush();
            	
            	m_hardwareBackBuffer = 
            	 createVolatileImage(this.getWidth(),this.getHeight());
            	
            	m_Renderer = m_hardwareBackBuffer.createGraphics();
            	if(m_bUseAntialiasing)
            	 m_Renderer.setRenderingHint(
                             RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);            }
    	}
    	else
    	{
            if(m_backBuffer == null)
            {
            	m_backBuffer = 
            	 (BufferedImage)createImage(this.getWidth(),
                                            this.getHeight());
            			
            	m_Renderer = m_backBuffer.createGraphics();
            }
            else
            {
            	m_backBuffer.flush();
            	m_Renderer.dispose();
            					
            	m_backBuffer = 
            	 (BufferedImage)createImage(this.getWidth(),
            				    this.getHeight());
            			
            	m_Renderer = m_backBuffer.createGraphics();
            	
            	if(m_bUseAntialiasing)
            	 m_Renderer.setRenderingHint(
                             RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
            }
    	}
    			
    	repaint();
    }
    
    //*************************************************************************
	
    public void mouseClicked(MouseEvent event)
    {  	
    }
    
    //*************************************************************************
    
    public void mouseExited(MouseEvent event)
    {           
    }
    
    //*************************************************************************
    
    public void mouseEntered(MouseEvent event)
    {
    	//this.requestFocusInWindow();
    	requestFocus();
    }
    
    //*************************************************************************
    
    /* (non-Javadoc)
     * 
     */
    public void mousePressed(MouseEvent event)
    {
    
    	m_mousePosition  = event.getPoint();
    	m_iMouseButton 	 = event.getButton();
    	m_iMouseModifiers = event.getModifiersEx();
    	
    	/* If ALT is pressed, pass the event onto
    	 * the coordinate system instead of editor
    	 */
    	
    	if((m_iMouseModifiers & InputEvent.ALT_DOWN_MASK) == 
    		                InputEvent.ALT_DOWN_MASK)
    	{
    	    m_coordinateSystem.mousePressed(event);
    	}
    	else
    	{
    	    m_Editor.mousePressed(event);
    	}
    	
    	repaint();
    }
    
    //*************************************************************************
	
    /* (non-Javadoc)
     * 
     */
    public void mouseReleased(MouseEvent event)
    {
    	setCursor(m_defaultCursor);
    	
    	if((m_iMouseModifiers & InputEvent.ALT_DOWN_MASK) == 
    		                InputEvent.ALT_DOWN_MASK)
    	{
    	    m_coordinateSystem.mouseReleased(event);
    	}
    	else
    	{
    	    m_Editor.mouseReleased(event);
    	}
    			
    	repaint();
    }
    
    //*************************************************************************
	
    /* (non-Javadoc)
     * 
     */
    public void mouseDragged(MouseEvent event)
    {		
    	m_mousePosition.setLocation(event.getPoint());
    	
    	/* If ALT is pressed, pass the event onto
    	 * the coordinate system instead of editor
    	 */
    	
    	if((m_iMouseModifiers & InputEvent.ALT_DOWN_MASK) == 
    		                InputEvent.ALT_DOWN_MASK)
    	{
            // modify cursor shape
            if(m_iMouseButton == MouseEvent.BUTTON1)
             setCursor(m_panCursor);
            else
             setCursor(m_zoomCursor);
            
            m_coordinateSystem.mouseDragged(event);					
    	}
    	else
    	{
    	    m_Editor.mouseDragged(event);
    	}			
    	repaint();		
    }
    
    //*************************************************************************
    
    /* (non-Javadoc)
     * 
     */
    public void mouseMoved(MouseEvent event)
    {
    	// remember current cursor position
    	m_mousePosition.setLocation( event.getPoint() );	
    	m_Editor.mouseMoved(event);		
    	repaint();		
    }
    
    //*************************************************************************
		
    /* (non-Javadoc)
     * 
     */
    public void mouseWheelMoved(MouseWheelEvent event)
    {					
    	m_coordinateSystem.mouseWheelMoved(event);
    	repaint();		
    }
    
    //*************************************************************************
    
    public void keyPressed(KeyEvent e)
    {
    	// stub
    }
    
    //*************************************************************************
    
    public void keyReleased(KeyEvent e)
    {
    	// stub
    }
    
    //*************************************************************************
	
    /**
     *  Issue a repaint after any keyboard event
     *  to be on the safe side.
     * 
     */
    public void keyTyped(KeyEvent e)
    {
    	repaint();
    }
    
    //*************************************************************************
    //	 		     Protected instance methods
    //*************************************************************************
	
    /**
     *  Renders everything to the off-screen buffer.
     */	
    protected void render()
    {	
    	/* protection in case repaint events
    	 * arrive too early, before the offscreen
    	 * buffer's been properly initialized. 
    	 */
    	
    	if(m_Renderer == null) return;
    	
    	// temporary profiling info
    	long start = System.currentTimeMillis();
    			
    	//clear everything to background color
    	m_Renderer.setBackground(m_backgroundColor);
    	m_Renderer.clearRect(0, 0, this.getWidth(), this.getHeight());
    	
    	//draw coordinate system and grid
    	if(m_coordinateSystem.isVisible())
    	 m_coordinateSystem.draw(m_Renderer);
    			
    	/**** Draw testing stuff ****/
    						
    	/**** Draw scene ************/
    	
    	AffineTransform tx = m_Renderer.getTransform();
    	
    	m_Renderer.transform(m_viewTransform);
    			
    	/* HACK:  This is a workaround for a bunch of bugs
    	 * in the damn rendering code of anja.graph package
    	 * (at least parts of it) which doesn't put the 
    	 * stroke attributes of a render back to the original parameters
    	 * after it's done!
    	 */
    	
    	Stroke s = m_Renderer.getStroke();
    	
    	m_Scene.draw(m_Renderer);
    	m_Editor.draw(m_Renderer);
    			
    	// restore previous affine transfrorm
    	m_Renderer.setTransform(tx);
    	
    	m_Renderer.setStroke(s);
    	
    	/**** Draw visual aids ******/
    				
    	// draw crosshairs
    	if(m_bDrawCrossHairs)
    	 drawCrossHair(m_Renderer);
    	
    	// draw mouse coordinate information panel
    	drawMouseStatus(m_Renderer);
    			
    	/* Temporary section that draws a histogram showing
    	 * the rendering performance over time.
    	 * 
    	 */
    	/*
    	long end = System.currentTimeMillis();		
    	float time = (end - start) / 1000.0f;		
    	int  height = (int)(20 * time * 50.0); // 50 pixels = 50 msec
    	
    	if(_frame_counter > 49)
    	{
            _x_position = 0;
            _frame_counter = 0;
    	}
    	else
    	{	
            _prev_values[_frame_counter] = height;
            _frame_counter++;
            _x_position++; // go to next position			
    	}
    	
    	float avg_time = 0;
    	
    	for(int i=0; i < _prev_values.length; i++)
    	{
            m_Renderer.drawLine(20 + i,70, 
                                20 + i, 
                                70 - _prev_values[i]);						
            avg_time += _prev_values[i];
    	}
    	
    	avg_time /= _prev_values.length;
    	height = (int)avg_time;
    	
    	m_Renderer.setColor(Color.blue);
    	m_Renderer.drawLine(20,70 - height,70,70 - height);
    	m_Renderer.drawString(String.valueOf(height), 75, 70 - height);
    	
    	m_Renderer.setColor(Color.gray);
    	m_Renderer.drawLine(20,20+25,70,20+25);
    	m_Renderer.drawString("25 ms", 75, 45);
    	
    	m_Renderer.setColor(Color.red);
    	m_Renderer.drawLine(20,20,70,20);
    	m_Renderer.drawString("50 ms",75,20);*/						
    }
		
    //*************************************************************************
    //	 		     Private instance methods
    //*************************************************************************
	
    // create UI elements and set up various UI properties	
    private void createUI()
    {
    	m_backgroundColor = Color.WHITE;
    	
    	// set help text	
    	String helpText = "<html>" +
    			"Press and hold ALT key to modify the coordinate" +
    			"<br>system. Use LEFT + Drag to move the coordinate" +
    			"<br>origin. Use RIGHT + Drag or mouse wheel" +
    			"<br>to zoom in / out at the" +
    			"current cursor position.</html>";
    	
    	//this.setToolTipText(helpText);
    			
    	//change default tooltip text to light yellow
    	UIManager.put("ToolTip.background",
    	              new ColorUIResource(new Color(255,255,200)));
    	
    	createMenu();
    	createToolBox();
    }
    
    //*************************************************************************

    private void createToolBox()
    {
      // stub	
    }
    
    //*************************************************************************
	
    private void createMenu()
    {			
    	m_viewOptionsMenu = new JMenu("View");
    	//m_viewOptionsMenu.setMnemonic(KeyEvent.VK_V);
    	
    	//hardware acceleration checkbox
    	m_uiHwBufferItem = new JCheckBoxMenuItem("Use HW buffer", false);
    	m_uiHwBufferItem.setActionCommand(String.valueOf(CMD_HW_ACCEL));
    	m_uiHwBufferItem.addActionListener(this);
    	
    	m_uiHwBufferItem.setToolTipText("<html>" +
    					"Use hardware acceleration" +
    					"<br>(Experimental!!)</html>");
    	
    	// crosshair checkbox  		
    	m_uiCrossItem = new JCheckBoxMenuItem("Show crosshair", false);
    	m_uiCrossItem.setActionCommand(String.valueOf(CMD_CROSSHAIR));
    	m_uiCrossItem.addActionListener(this);
    	
    	m_uiCrossItem.setToolTipText("Show / hide crosshair");
    	m_uiCrossItem.setAccelerator(KeyStroke.getKeyStroke('c'));
    	
    	// antialiasing checkbox
    	m_uiAntialiasItem = 
    		new JCheckBoxMenuItem("Use Antialiasing",true);
    	
    	m_uiAntialiasItem.
    		setActionCommand(String.valueOf(CMD_ANTIALIAS));
    	
    	m_uiAntialiasItem.addActionListener(this);
    	m_uiAntialiasItem.
    	 setToolTipText("<html>Enable/disable" +
			"<br><b>antialiasing</b>. When activated," +
			"<br>this makes sharp edges and lines" +
			"<br>look smoother, at the expense" +
			"<br>of higher CPU usage." +
			"<br><b>Beware! This can slow down " +
			"<br>the system significantly!</b></html>");
    	
    	JMenuItem color_item = new JMenuItem("Background color..");
    	color_item.setActionCommand(String.valueOf(CMD_BACK_COLOR));
    	color_item.addActionListener(this);
    
    	color_item.setToolTipText("Change display background color");
    	
    	JMenuItem save_img_item = new JMenuItem("Export image...");
    	save_img_item.setActionCommand(String.valueOf(CMD_SAVE_IMG));
    	save_img_item.addActionListener(this);
    	
    	save_img_item.setToolTipText("Save view snapshot as PNG image");
    	
    	// put everything together
    	m_viewOptionsMenu.add(m_uiHwBufferItem);
    	m_viewOptionsMenu.add(m_uiCrossItem);
    	m_viewOptionsMenu.add(m_uiAntialiasItem);
    	
    	m_viewOptionsMenu.addSeparator();
    	m_viewOptionsMenu.add(color_item);
    	
    	m_viewOptionsMenu.addSeparator();
    	m_viewOptionsMenu.add(save_img_item);
    	
    	// temporarily disable the HW buffer option
    	m_uiHwBufferItem.setEnabled(false);  		
    }
    
    //*************************************************************************
    	
    private void drawMouseStatus(Graphics2D g)
    {		
    	// draw mouse coordinates on screen
    	
    	/*
    	 * This code might also represent a potential performance
    	 * bottleneck because of a whole bunch of associated local
    	 * variables and the actual time spent on text formatting.
    	 */
    	
    	m_coordinateSystem.screenToWorld(m_mousePosition, 
					 m_worldMousePosition);
    			
    	String xtext = "X :  " + m_Formatter.format(m_worldMousePosition.x);			
    	String ytext = "Y :  " + m_Formatter.format(m_worldMousePosition.y);
    	
    	m_coordinateSystem.polarScreenToWorld(m_mousePosition,
    	                                      m_worldMousePosition);
    
    	String r_text = "R : " + m_Formatter.format(m_worldMousePosition.x);
    	
    	String phi_text = "Phi : " // \u03B1 for alpha, \u03C6 for phi
    			  + m_Formatter.format(
    			    Math.toDegrees(m_worldMousePosition.y))
    			  + " \u00BA";
    	
    	FontMetrics ff = g.getFontMetrics();
    			
    	Rectangle2D xtext_bounds = ff.getStringBounds(xtext,g);		
    	Rectangle2D ytext_bounds = ff.getStringBounds(ytext, g);	
    	Rectangle2D rtext_bounds = ff.getStringBounds(r_text,g);
    	Rectangle2D phitext_bounds = ff.getStringBounds(phi_text,g);	
    	
    	int font_size = g.getFont().getSize();
    	
    	int width = (int)Math.max(Math.max(xtext_bounds.getWidth(),
    	                                   ytext_bounds.getWidth()),
    			          Math.max(rtext_bounds.getWidth(),
    			                    phitext_bounds.getWidth())
    							  );
    	
    	// keep right text margin at least 10 pixels 
    	// away from the display border
    	
    	int x = this.getWidth() - width - 10;   
    	        
    	// set y to four text lines' height
    	int y = this.getHeight() - 4 * font_size - 10;
    
    	// make box for text
    			
    	g.setColor(m_backgroundColor);
    	
    	g.fillRect(x - 5, y - font_size, 
    	           this.getWidth() - x + 10, 
    	           this.getHeight() - y + font_size);
    				
    	g.setColor(m_foregroundColor);
    		
    	g.drawString(xtext, x,y);
    	g.drawString(ytext, x, 
    	             y + (int)(1.4 * font_size));
    		
    	g.drawString(r_text, x, y + 
    	             (int)(2.8 * font_size));
    	
    	g.drawString(phi_text, x, y + 
    	             (int)(4.2 * font_size));			
    }
    
    //*************************************************************************
	
    /*
     * Draws a crosshair. 
     * TODO: Figure out how to draw dashed lines faster.
     * (May be a bug / pitfall in Java2D API)
     * 
     */
    
    private void drawCrossHair(Graphics2D g)
    {
        
    	g.setColor(m_foregroundColor);		
        BasicStroke lastStroke = (BasicStroke)g.getStroke();
        g.setStroke(m_dashStroke);
        		
    	/*
    	 * Since we don't care about the crosshair lines being
    	 * overlapped by a potential border, we just draw them using
    	 * component width and height.
    	 */
    	
    	g.drawLine(0, m_mousePosition.y,
    	           this.getWidth(), m_mousePosition.y);
    	
    	g.drawLine(m_mousePosition.x, 0, 
    	           m_mousePosition.x, this.getHeight());
        
        g.setStroke(lastStroke);
    }
    
    //*************************************************************************
		
    private void saveImage()
    {
    	//set up file chooser
    	JFileChooser filechooser = new JFileChooser();
    	FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Image", ".png");
    			
    	filechooser.setDialogTitle("Save image...");
    	
    	/*
    	 * restrict some options so that the user can't accidentally
    	 * drag-and-drop files, select files of incorrect types etc.
    	 */
    	filechooser.setDragEnabled(false);
    	filechooser.setMultiSelectionEnabled(false);
    	filechooser.setFileHidingEnabled(true);
    	
    	filechooser.setAcceptAllFileFilterUsed(false);
    	filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    	
    	filechooser.setFileFilter(filter);
    	
    	/* using null as parent here causes the dialog 
    	 * to be centered on screen.
    	 */				
    	int retcode = filechooser.showSaveDialog(null);
    	
    	if(retcode == JFileChooser.APPROVE_OPTION)
    	{
            File savefile = filechooser.getSelectedFile();
            String name = savefile.getAbsolutePath();
            
            if(savefile.exists())
            {
                // ask user for permission to overwrite file
                int result 
                 = JOptionPane.showConfirmDialog(null,
                   "File exists! Overwrite ?",
                   "Confirm file overwrite",
                   JOptionPane.YES_NO_OPTION,
                   JOptionPane.WARNING_MESSAGE);
                
                // cancel operation if "No" button was clicked
            	if(result == JOptionPane.NO_OPTION)
            	 return;
            }
            
            // check name and append extension of necessary
            if( !name.endsWith(".png") )
            {
            	name += ".png";
            }
            			
            // create output file
            FileOutputStream file_stream;
            try 
            {
            	file_stream = new FileOutputStream( name );
            			
                // filestream ok, save image
                System.out.println("writing image...");
                
                //debug
                System.out.println(name);	
                
                ImageIO.write(m_backBuffer, "PNG", file_stream);	
                System.out.print("done!");
            } 
            catch ( FileNotFoundException ex ) 
            {  
            	System.err.println(ex.getMessage());
            	return;
            }			
            catch(IOException ex)
            {
            	System.err.println(ex.getMessage());
            }						
    	}
    } // saveImage()
}

