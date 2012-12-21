/*
 * File: JGraphView.java
 * Created on May 23, 2006 by ibr
 *
 * TODO Write documentation
 */
package anja.SwingFramework.graphView;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Dimension;

import anja.graph.*;
import anja.SwingFramework.*;


/**
 * 
 * 
 * 
 * @author ibr
 *
 * TODO Write documentation
 */
public class JGraphView extends JPanel implements ActionListener,
                                                  MouseListener
{
    
    //*************************************************************************
    //                             Public constants
    //*************************************************************************
    
    //*************************************************************************
    //                             Private constants
    //*************************************************************************
   
    private static final long serialVersionUID = 8440354352034075205L;
    
    private static final String CMD_ZOOM_IN  = "zoom_in";
    private static final String CMD_ZOOM_OUT = "zoom_out";
    private static final String CMD_ZOOM_FIT = "zoom_fit";
    
    private static final double ZOOM_IN_FACTOR  = 1.05;
    private static final double ZOOM_OUT_FACTOR = 0.95;
    
    //*************************************************************************
    //                             Class variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Public instance variables
    //*************************************************************************
    
    //*************************************************************************
    //                       Protected instance variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance variables
    //*************************************************************************
    
    // -------------------------- Panel components ----------------------------
    

    private JSystemHub          _hub;
    
    private CoordinateSystem    _coordsystem;   // coordinate system
    private JGraphViewScene     _scene;         // graph view scene
    private JGraphViewEditor    _editor;        // dummy editor
    private JSimpleDisplayPanel _display;       // display component
    //private JTextMessageDump    _msg_dump;      // message dump
    
    // ------------------------------ UI code ---------------------------------
    
    // interface components that mimic the legacy UI by Uli Handel
    
    private JButton                   _zoomFitButton;
    private JRepeatButton             _zoomInButton;
    private JRepeatButton             _zoomOutButton;
    
    private JMenu                     _contextMenus;
    private boolean                   _bInseparateWindow;
    
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    public JGraphView(JComponent parent)
    {
        super(); // init jpanel
        
        // slap together a basic graph viewer
        
        _hub = new JSystemHub();
        _hub.setParent(this);
        
        
        
        _coordsystem  = new CoordinateSystem(_hub);
        _scene        = new JGraphViewScene(_hub);
        _editor       = new JGraphViewEditor(_hub);
        _display      = new JSimpleDisplayPanel(_hub);              
        
        _coordsystem.setVisible(false);
        _coordsystem.setAxisLabels("X", "Y");
        
        //_msg_dump = new JTextMessageDump(_hub);
        //_msg_dump.setMaxNumberOfLines(100);
        //_msg_dump.enableStreamCapturing(true);
        
        _bInseparateWindow = false; // assume "parented" by default
         
        // init UI
        createUI(parent);
    }
    
    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
    
    public void setGraph(Graph graphObject)
    {
        _scene.setGraph(graphObject);
        repaint(); // update view
    }
    
    //*************************************************************************
    
    // ---------------------------- Event handlers ----------------------------
    
    public void actionPerformed(ActionEvent event)
    {
        String cmd = event.getActionCommand();
        
        if(cmd.equals(CMD_ZOOM_IN))
        {
            zoomIn();
        }
        else if(cmd.equals(CMD_ZOOM_OUT))
        {
            zoomOut();
        }
        else if(cmd.equals(CMD_ZOOM_FIT))
        {
            zoomFit();
        }
        
    }
    
    //*************************************************************************
    
    public void mouseClicked(MouseEvent event)
    {
        // show popup menus 
        if( event.getButton() == MouseEvent.BUTTON3)
        {
            _contextMenus.getPopupMenu().
              show(this, event.getX(), event.getY());
        }
    }
    
    //*************************************************************************
    
    // ---------------------------- STUBS -------------------------------------
    
    public void mousePressed(MouseEvent e)
    {
    }
    
    //*************************************************************************
    
    public void mouseEntered(MouseEvent e)
    {   
    }
    
    //*************************************************************************
    
    public void mouseExited(MouseEvent e)
    {  
    }
    
    //*************************************************************************
    
    public void mouseReleased(MouseEvent e)
    {  
    }
    
    //*************************************************************************
    
    /**
     * Reimplemented from JPanel
     */
    public Dimension getPreferredSize()
    {
       return new Dimension(300, 250);
    }
    
    //*************************************************************************
    
    /**
     * Reimplemented from JPanel
     */
    public Dimension getMinimumSize()
    {
       return new Dimension(300, 250);
    }
    
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    protected void createUI(JComponent parent)
    {   
        // create additional UI components
        
        _zoomInButton   = new JRepeatButton("Zoom in");
        _zoomOutButton  = new JRepeatButton("Zoom out");
        _zoomFitButton  = new JButton("Fit view");
        
        _zoomInButton.setActionCommand(CMD_ZOOM_IN);
        _zoomOutButton.setActionCommand(CMD_ZOOM_OUT);
        _zoomFitButton.setActionCommand(CMD_ZOOM_FIT);
        
        _zoomInButton.addActionListener(this);
        _zoomOutButton.addActionListener(this);
        _zoomFitButton.addActionListener(this);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(_zoomInButton);
        buttonPanel.add(_zoomOutButton);
        buttonPanel.add(_zoomFitButton);
       
        //set up the viewer panel
        
        this.setBorder(BorderFactory.createTitledBorder(
                       BorderFactory.createEmptyBorder(5,5,5,5),
                       "Graph view"));
        
        this.setLayout(new BorderLayout());
        this.add(_display, BorderLayout.CENTER);                
        this.add(buttonPanel, BorderLayout.SOUTH);
         
        /* If the parent widget reference is NULL, we automatically
         * instantiate a separate window and put the view into it.
         * If not, we assume the graph view will be at some point inserted
         * into the parent's widget hierarchy. 
         * 
         */
        if(parent == null)
        {
            _bInseparateWindow = false;
            
            //Create the top-level container and add contents to it.
            JFrame frame = new JFrame("Graph view");
            frame.getContentPane().add(this);
            
            JMenuBar menu_bar = new JMenuBar();
            menu_bar.add(_display.getMenus());
            menu_bar.add(_coordsystem.getMenus());
            frame.setJMenuBar(menu_bar);
            
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
             
        }
        else
        {
            _bInseparateWindow = true;
            
            /* we don't want the menus as part of the parent's menu bar,
             * so we use context menus instead.
             */
            
            _contextMenus = new JMenu();
            _contextMenus.add(_display.getMenus());
            _contextMenus.add(_coordsystem.getMenus());
            
            
            // hook up mouse event handler to work with the context menus
            _display.addMouseListener(this);
        }
        
    }
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************
    
    
    private void zoomFit()
    {
        //TODO: zoomFit()
        _coordsystem.fitViewToRect(_scene.getBoundingRectangle());
        _display.repaint();
    }
    
    //*************************************************************************
    
    private void zoomIn()
    {
        //TODO: zoomIn()
        _coordsystem.zoomView(ZOOM_IN_FACTOR);
        _display.repaint();
    }
    
    //*************************************************************************
    
    private void zoomOut()
    {
        //TODO: zoomOut()
        _coordsystem.zoomView(ZOOM_OUT_FACTOR);
        _display.repaint();
    }
    
    //*************************************************************************
}

		
   
    
    
    
   