/*
 * Created on Oct 12, 2005
 *
 *  GraphExperiments.java
 */
package anja.SwingFramework.tests;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.*;

import anja.SwingFramework.*;
import anja.SwingFramework.graph.*;

//import anja.SwingFramework.graphView.JGraphView;
//import anja.SwingFramework.graph.*;

/* Editor panel for various graph-related experiments.
 * 
 */

public class GraphExperiments extends JPanel
{
    
    //*************************************************************************
    //                             Public constants
    //*************************************************************************
    
    //*************************************************************************
    //                             Private constants
    //*************************************************************************
    
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
    
    /**
     * 
     */
    private static final long serialVersionUID = 3998440283233566516L;

    private JSystemHub          _hub;
        
    private JSimpleDisplayPanel _display;
    private CoordinateSystem    _coordSystem;
    private JGraphScene      _graphScene;
    private JGraphEditor     _scrapEditor;
    private JTextMessageDump    _msgDump;
    
    //private AlgorithmsTest      _algorithms;
    
        
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    public GraphExperiments(JFrame parent)
    {
        super();
        
        // slap together a basic graph editor system
        
        _hub = new JSystemHub();
        _hub.setParent(this);
        
        _msgDump        = new JTextMessageDump(_hub);
        _coordSystem    = new CoordinateSystem(_hub);
        _graphScene     = new ScrapGraphScene(_hub);
        _scrapEditor    = new ScrapGraphEditor(_hub);
        _display        = new JSimpleDisplayPanel(_hub);              
        
        _coordSystem.setVisible(true);
        _coordSystem.setAxisLabels("X", "Y");
        
        _msgDump.setMaxNumberOfLines(100);
        _msgDump.enableStreamCapturing(true);
        
        //_algorithms = new AlgorithmsTest(_hub);
        
        // hook up the algorithms object to the editor
        //_scrap_editor.addEditorListener(_algorithms);
        
        // init UI
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
       return new Dimension(800, 600);
    }
    
    /**
     * Reimplemented from JPanel
     */
    public Dimension getMinimumSize()
    {
       return new Dimension(800, 600);
    }
        
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    protected void createUI(JFrame parent)
    {
        // just add all the menus to the UI
        JMenuBar bar = new JMenuBar();
        
        bar.add(_scrapEditor.getFileMenu());
        bar.add(_scrapEditor.getEditMenu());
        bar.add(_coordSystem.getMenus());
        bar.add(_display.getMenus());
        
        parent.setJMenuBar(bar);
        
        // now set up the editor panel
        
        this.setBorder(BorderFactory.createTitledBorder(
                       BorderFactory.createEmptyBorder(5,5,5,5),
                       "Scrap graph editor"));
        
        this.setLayout(new BorderLayout());
        this.add(_display, BorderLayout.CENTER);                
        this.add(_scrapEditor.getEditorUI(), BorderLayout.SOUTH);
        
        // test Graph View panel
        
        /*
        boolean separate = false;
        
        if(separate)
        {
            // in its own window
            
            JGraphView viewer = new JGraphView(null);
            viewer.setGraph(_scrap_scene.getGraph());
        }
        else
        {
            // or as a child widget in the main window
            
            JGraphView viewer = new JGraphView(this);
            viewer.setGraph(_scrap_scene.getGraph());
            
            this.add(viewer, BorderLayout.EAST);
            
        }*/
          
    }
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************
	
}


