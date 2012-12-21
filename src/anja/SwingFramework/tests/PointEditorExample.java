/*
 * File: JPointEditorExample.java
 * Created on Sep 1, 2006 by ibr
 *
 * TODO Write documentation
 */
package anja.SwingFramework.tests;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.BorderLayout;

import java.util.*;

import anja.geom.Point2;

import anja.SwingFramework.*;
import anja.SwingFramework.event.*;
import anja.SwingFramework.point.*;


/**
 * This class is an example implementation of point set editor component
 * based on JPointScene/JPointEditor classes.
 * 
 * 
 * @author ibr
 *
 * TODO Write documentation
 */

public class PointEditorExample extends JPanel implements EditorListener
{
    
    //*************************************************************************
    //                             Public constants
    //*************************************************************************
    
    //*************************************************************************
    //                             Private constants
    //*************************************************************************
    
    
    private static final long serialVersionUID = -6507414761186200061L;
    
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
    private JPointScene         _scene;         // graph view scene
    private JPointEditor        _editor;        // dummy editor
    private JSimpleDisplayPanel _display;       // display component
    private JTextMessageDump    _msg_dump;      // message dump
    
    // ------------------------------ UI code ---------------------------------
         
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    public PointEditorExample(JFrame parent)
    {
        super(); 
        
        // slap together a basic graph viewer
        _hub = new JSystemHub();
        _hub.setParent(this);
            
        _coordsystem  = new CoordinateSystem(_hub);
        _scene        = new JPointScene(_hub);
        _editor       = new JPointEditor(_hub);
        _display      = new JSimpleDisplayPanel(_hub);              
        
        _coordsystem.setVisible(true);
        _coordsystem.setAxisLabels("X", "Y");
        
        /* Disable this section if you don't want the message dump
         * window. 
         * 
         */
        _msg_dump = new JTextMessageDump(_hub);
        _msg_dump.setMaxNumberOfLines(100);
        _msg_dump.enableStreamCapturing(true);   
         
        // test
        //_editor.addEditorListener(this);
        
        // init UI;
        createUI(parent);
        
    }
    
    //*************************************************************************
    //                         Public instance methods
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
     * 
     * There's still a problem somewhere.... The window can still be 
     * made arbitrarily small, so it would seem that there's something other 
     * than specification of sizes that controls the layout management.... 
     */
    public Dimension getMinimumSize()
    {
       return new Dimension(300, 250);
    }
    
    //*************************************************************************
    
    public void editorActionPerformed(EditorEvent event)
    {
        JPointEditorEvent ev = (JPointEditorEvent)event;        
        PointEventConstants type = (PointEventConstants)ev.getEvent();
        
        switch(type)
        {
            case POINT_ADDED:
                System.out.println("Point added!");
            break;
            
            case POINT_REMOVED:
                System.out.println("Point removed!");
            break;
            
            case POINT_MOVED:
                System.out.println("Point moved!");
            break;
            
        }
    }
    
    //*************************************************************************
    
    public void contextActionPerformed(EditorEvent event)
    {
        
    }
    
    //*************************************************************************
    
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************
    
    private void createUI(JFrame parent)
    {
        //set up the viewer panel
        
        this.setBorder(BorderFactory.createTitledBorder(
                       BorderFactory.createEmptyBorder(5,5,5,5),
                       "Point set editor"));
        
        this.setLayout(new BorderLayout());
        this.add(_display, BorderLayout.CENTER);                
        //this.add(buttonPanel, BorderLayout.SOUTH);
        
        // add all system menus to the parent frame menu bar
        JMenuBar menu_bar = new JMenuBar();
        
        menu_bar.add(_editor.getFileMenu());
        menu_bar.add(_editor.getEditMenu());
        menu_bar.add(_display.getMenus());
        menu_bar.add(_coordsystem.getMenus());
         
        parent.setJMenuBar(menu_bar);
    }
    
    //*************************************************************************
    
    private void testScene()
    {
        // generate a few random points within the scene
        
        int num_points = 20;
        
        float lower_bound = -200, upper_bound = 200;
        float next_rnd;
        float x,y;
        
        Random rnd = new Random();
        rnd.setSeed(System.currentTimeMillis());
        
        for(int i = 0; i < num_points; i++)
        {
            next_rnd = rnd.nextFloat();
            x = next_rnd * lower_bound + (1.0f - next_rnd) * upper_bound;
            
            next_rnd = rnd.nextFloat();
            y = next_rnd * lower_bound + (1.0f - next_rnd) * upper_bound;
            
            _scene.addPoint(new Point2(x,y));   
        }
        
    }
    
    //*************************************************************************
       
}

		
    