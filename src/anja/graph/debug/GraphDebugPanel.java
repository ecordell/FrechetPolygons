/*
 * NewgraphDebugPanel.java
 */

package anja.graph.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.*;

import anja.SwingFramework.*;
import anja.SwingFramework.graph.*;

public class GraphDebugPanel extends JPanel
{
    private static final long serialVersionUID = 3998440283233566516L;

    private JSystemHub          _hub;
        
    private JSimpleDisplayPanel _display;
    private CoordinateSystem    _coordSystem;
    private JGraphScene      _graphScene;
    private JGraphEditor     _scrapEditor;
    private JTextMessageDump    _msgDump;

	
    public GraphDebugPanel(JFrame parent)
    {
        super();
        
        // slap together a basic graph editor system
        
        _hub = new JSystemHub();
        _hub.setParent(this);
        
        _msgDump        = new JTextMessageDump(_hub);
        _coordSystem    = new CoordinateSystem(_hub);
        _graphScene     = new GraphDebugScene(_hub);
		_scrapEditor	= new JGraphEditor(_hub);
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
		_msgDump.show();
	}
    
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
          
    }	
}



