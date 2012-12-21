/*
 * Created on Jan 11, 2005
 *
 *	JAbstractEditor.java 
 */
package anja.SwingFramework;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import anja.geom.*;
import anja.SwingFramework.event.*;


/**
 * This class contains the base abstract skeleton for an editor. An
 * editor object is used by the SwingFramework to manipulate contents
 * of a scene. An editor has seperate "file" and "edit" menus which
 * can be inserted into the menu bar of the parent window, and does
 * the necessary keyboard / mouse event handling. It can also display
 * additional information inside a display panel.
 * 
 * <p>For an example how to design and implement an editor by
 * subclassing JAbstractEditor, look at
 * {@link anja.SwingFramework.graph.JGraphEditor}
 * 
 * 
 * @author Ibragim Kouliev
 * 
 * TODO: write and integrate XML I/O for editor settings
 * 
 */

public abstract class JAbstractEditor implements ActionListener,
                        						 MouseListener,
                        						 MouseMotionListener,
                        						 KeyListener
{
		
    //*************************************************************************
    // 				 Public constants
    //*************************************************************************
    
    //*************************************************************************
    // 				 Private constants
    //*************************************************************************
	
    // ------------------------ XML I/O constants -----------------------------
    
    private static final String XML_DOCUMENT_TYPE 
    					= "Swing_Framework_document";
    
    private static final String XML_VERSION_NAME = "Version";
    private static final String XML_VERSION_VALUE = "1.0";
    	
    // --------------------- UI command identifiers ---------------------------
    
    private static final int CMD_LOAD = 1;
    private static final int CMD_SAVE = 2;
    private static final int CMD_EXIT = 3;
    
    protected static final int CMD_CLEAR = 4;
    protected static final int CMD_RESET = 5;
	
    //*************************************************************************
    // 				Class variables
    //*************************************************************************
    
    //*************************************************************************
    // 			   Protected instance variables
    //*************************************************************************
	
    protected JSystemHub       _hub;
    protected JAbstractScene   _scene;
    protected CoordinateSystem _coordSystem;
    	
    // ---------------------------- UI elements -------------------------------
    
    protected JMenu  _uiFileMenu;
    protected JMenu  _uiEditorMenu;	
    	
    protected JPanel _uiEditorPanel;
	
    // ----------------------------- variables --------------------------------
	
    protected int 	 _mouseButton;      // pressed button(s)
    protected int 	 _mouseModifiers;   // pressed modifier keys
    protected Point	 _mousePosition;     // image space mouse position
    	
    protected Rectangle2      _selectionBoundingBox;		
    protected double	      _pixelSize; // current pixel size
    protected Font            _invertedFont;
    
    protected AffineTransform _textTransform; 
    
    // event listeners
    protected Vector	      _editorListeners;
    
    // "undo" system stuff
    protected Stack	      _undoStack;
	
    //*************************************************************************
    // 			     Private instance variables
    //*************************************************************************
	
    // file saving/loading stuff
    private File  _lastDirectory;
    
    //*************************************************************************
    // 				    Constructors
    //*************************************************************************
	
    /**
     * Base class constructor. 
     * This constructor will automatically initialize all sections common
     * to any editor, i.e. "File menu" and some other stuff.
     * 
     * @param hub an instance of JSystemHub
     */	
    public JAbstractEditor(JSystemHub hub)
    {
    	createCommonMenus();
    	_selectionBoundingBox     = new Rectangle2();
    	_textTransform            = new AffineTransform();
    	
    	hub.setEditor(this);
    	
    	_hub 		   = hub;
    	_scene            = hub.getScene();
    	_coordSystem      = hub.getCoordinateSystem();
    	
    	_editorListeners  = new Vector();
    	_undoStack        = new Stack();
    				
    }
		
    //*************************************************************************
    // 			      Public instance methods
    //*************************************************************************
    
    /**
     * Helper method, used by classes associated with an editor to
     * access other class through a {@link JSystemHub}instance.
     * 
     * @return the system hub
     */
    public JSystemHub getHub()
    {
    	return _hub;
    }
    
    //*************************************************************************
	
    /**
     * Should be called by methods of the parent window to add the
     * "File" menu to the common menu bar.
     * 
     * @return The "File" menu.
     */
    public JMenu getFileMenu()
    { 
    	return _uiFileMenu;
    }
    
    //*************************************************************************
	
    /**
     * Should be called by methods of the parent window to add the
     * "Edit" menu to the common menu bar.
     * 
     * @return the "Edit" menu
     */
    public JMenu getEditMenu()
    {
    	return _uiEditorMenu;
    }
    
    //*************************************************************************

    /**
     * Returns a JPanel instance containing the additional UI
     * elements. This panel can then be appropriately inserted into
     * the parent container
     * 
     * @return the editor UI
     */
    public JPanel getEditorUI()
    {
    	return _uiEditorPanel;
    }
    
    //*************************************************************************
	
    /**
     * This method is automatically invoked by the system whenever
     * there's a change to the coordinate system parameters, i.e.
     * translation and zoom factor. The system supplies the new
     * object-to-screen space affine transformation matrix, pixel size
     * and a special version of the default system font. This modified
     * font is mirrored on the Y-axis and should be used to properly
     * render any text information (e.g by using the drawString()
     * method in Graphics2D), for the following reason:
     * <p>
     * The SwingFramework coordinate system has its positive Y-axis 
     * 'looking up', as opposed to the standard Java2D coordinate system.
     * As the result, all drawing calls to Graphics2D methods have their
     * y-coordinates inverted, including font rendering methods. Thus,
     * it becomes necessary to use an inverted font, otherwise all text
     * will be rendered upside down!
     * 
     * 
     * @param tx new AffineTransform instance
     * @param pixelSize TODO doc
     * @param invertedFont TODO doc
     * 
     * <p>
     * TODO: Currently the transform update mechanism makes no
     * provision for general affine transforms(i.e. if they contain
     * rotation or shearing components). This will need to be fixed
     * later!
     */	
	
    public void  updateAffineTransform(AffineTransform tx, 
                                       double pixelSize, 
                                       Font invertedFont)
    {	
        _pixelSize = pixelSize;
        _invertedFont = invertedFont;
        
          /*
       	  m_textTransform.setTransform(tx.getScaleX(), 0,
       	                               0, - tx.getScaleY(),
    				       tx.getTranslateX(), 
    				       tx.getTranslateY());
       	
         Point2 point0 = new Point2();
         Point2 point1 = new Point2();
          
         tx.transform( new Point2( 0, 0 ), point0 );
         tx.transform( new Point2( 1, 0 ), point1 );
         	      
         m_fPixelSize = point1.x - point0.x;*/	
    }
    
    //*************************************************************************
	
    /**
     * Registers a new editor action listener with this editor.
     * 
     * @param listener Listener to be added
     */	
    public void addEditorListener(EditorListener listener)
    {
    	if(!_editorListeners.contains(listener))
    	 _editorListeners.add(listener);
    }
    
    //*************************************************************************
	
    /**
     * Removes an editor action listener from this editor's list.
     * 
     * @param listener Listener to be removed.
     */
    public void removeEditorListener(EditorListener listener)
    {
    	if(_editorListeners.contains(listener))
    	 _editorListeners.remove(listener);
    }
    
    //*************************************************************************
	
    /**
     * Can be used to pass an editor event to an editor instance
     * externally (for example from an undo object).
     * 
     * @param event An externally generated event
     */
    public void postEditorActionEvent(EditorEvent event)
    {
    	fireEditorActionEvent(event);
    }
	
	
    //*************************************************************************
    // 			           Event handlers
    //*************************************************************************
		
    /**
     * Action event handler for the basic things. Don't forget to call
     * this at the end of your own actionPerformed() in a derived
     * editor class!
     * 
     */
    public void actionPerformed(ActionEvent e)
    {
    	String command = e.getActionCommand();
    	int cmd_code = Integer.parseInt(command);
    	
    	/* Special case handling for file save confirmation dialog
    	 * If the scene contents change since the last save, a dialog
    	 * will pop up asking the user whether to save the changes. 
    	 */
    	switch(cmd_code)
    	{
            case CMD_CLEAR:
            case CMD_RESET:
            case CMD_LOAD:
            case CMD_EXIT:
    			
             if(_scene.sceneHasChanged())
             {				
        	int retcode = JOptionPane.
        	showConfirmDialog(_hub.getApplication(),				  					
        			"Workspace contents have changed\n" +
        			"since the last save. Would you\n" +
        			"like to save it now?",
        			"Save document",
        			JOptionPane.YES_NO_CANCEL_OPTION,
        			JOptionPane.WARNING_MESSAGE);
        		
        	if(retcode == JOptionPane.CANCEL_OPTION)
        	 return; // break out of here early!
        		
        	else if(retcode == JOptionPane.YES_OPTION)
        	{
        	    saveDocument();										
        	}
        		
        	else if(retcode == JOptionPane.NO_OPTION)
        	{
        	    _scene.resetChangeFlag();
        	}
        		
    		/* NO_OPTION is handled implicitly - i.e. the 
    		 * document is not saved, and the command is 
    		 * executed in the usual manner. 
    		 */
                
             }
             break;
             
    	} // end switch
    	
    	// now handle the actual commands...
    	switch(cmd_code)
    	{
    	    case CMD_CLEAR:				
    	        clear();			 
            break;
    		
    	    case CMD_RESET:
    			
    		clear();
    		_coordSystem.reset();
    		_coordSystem.center();
    			
    	    break;
    		
    	    case CMD_LOAD:
    			
    		clear();
    		loadDocument();
    									
	    break;
    		
    	    case CMD_SAVE:
    			
    		saveDocument();
    			
    	    break;
    		
    	    case CMD_EXIT:
    					
    		// show exit confirmation dialog
    		int retcode = 
    		 JOptionPane.showConfirmDialog(_hub.getApplication(),
    		 			       "Exit now?",
    		 			       "Confirm exit",
    		 			       JOptionPane.YES_NO_OPTION,
    					       JOptionPane.WARNING_MESSAGE);
    		
    		if(retcode == JOptionPane.YES_OPTION)
    		{
    		    // exit 
    		    _hub.getApplication().setVisible(false);
    		    _hub.getApplication().dispose();
    		    System.exit(0);
    		}
    			
    	    break;	
            
    	}// end switch
    }
    
    //*************************************************************************
	
    /**
     * Default implementations are provided for keyReleased() and
     * keyTyped() so that the derived classes don't have to implement
     * them if it isn't necessary - these event handlers will rarely
     * be used.
     */	
    public void keyReleased(KeyEvent e)
    {
    	// stub
    }
    
    //*************************************************************************
	
    /**
     *  
     */
    public void keyTyped(KeyEvent e)
    {
    	// stub
    }
    
    //*************************************************************************
	
    /**
     * Current keys handled by the base implementation:
     * 
     * <p>CTRL - Z undoes the last editor action
     *  
     */
    public void keyPressed(KeyEvent event)
    {
    	//TODO: Undo handler
    	int code = event.getKeyCode();
    	int modifiers = event.getModifiersEx();
    	switch(code)
    	{
    	    case KeyEvent.VK_Z:
				if ((modifiers & InputEvent.CTRL_DOWN_MASK)
					 != InputEvent.CTRL_DOWN_MASK) break;
			case KeyEvent.VK_BACK_SPACE:
				undoLastEdit();        		
            break;
    	}
    }
	
    //*************************************************************************
    // 			        Abstract methods
    //*************************************************************************
	
    // inherited abstract methods
    
    public abstract void  mouseClicked(MouseEvent e);	
    public abstract void  mouseEntered(MouseEvent e);		
    public abstract void   mouseExited(MouseEvent e);	
    public abstract void  mousePressed(MouseEvent e);	
    public abstract void mouseReleased(MouseEvent e);
    public abstract void  mouseDragged(MouseEvent e);
    public abstract void    mouseMoved(MouseEvent e);
    
    //*************************************************************************
	
    /**
     * Draws additional stuff into the display view
     * 
     * @param g
     */
    public abstract void  draw(Graphics2D g);
		
    //*************************************************************************
    // 			     Protected instance methods
    //*************************************************************************
	
    protected abstract void createUI();

    //*************************************************************************
    
    /**
     * Returns an instance of an JAbsractFileFilter subclass which
     * specifies a file filter for a particular scene document type.
     * 
     */
    protected abstract FileNameExtensionFilter getFileFilter();
    
    //*************************************************************************
    
    /**
     * This method dispatches an editor "action" event to all
     * registered editor listeners. It should be called by all
     * specific subclasses to fire various editor events as necessary.
     * 
     * @param event An event to be dispatched
     */	
    protected void fireEditorActionEvent(EditorEvent event)
    {
    	Enumeration e = _editorListeners.elements();
    	while(e.hasMoreElements())
    	{
    	    EditorListener l = (EditorListener)e.nextElement();
    	    l.editorActionPerformed(event);
    	}
    }
    
    //*************************************************************************
	
    protected void fireContextActionEvent(EditorEvent event)
    {
    	Enumeration e = _editorListeners.elements();
    	while(e.hasMoreElements())
    	{
    	    EditorListener l = (EditorListener)e.nextElement();
    	    l.contextActionPerformed(event);
    	}
    	
    }
    
    //*************************************************************************
	
    protected void putUndoAction(JAbstractUndoAction action)
    {
    	_undoStack.push(action);
		//System.out.println("Saving: " + action.toString());
    }
    
    //*************************************************************************
    
    protected void clearAllUndos()
    {
    	_undoStack.clear();
    }
    
    //*************************************************************************
    
    protected void undoLastEdit()
    {
    	
    	if(_undoStack.isEmpty()) // nothing to do...
    	 return;
    	
    	// retrieve last stored undo action
    	//the undo stack now contains one less undo object!
    	JAbstractUndoAction action = 
    	 (JAbstractUndoAction)_undoStack.pop();
    			
    	action.undo();		
    			
    	// temporary debugging section
    	//System.out.println("Undoing: " + action.toString());
    	//System.out.println("Remaining undos: " + _undoStack.size());
    	action = null;
    }
    
    //*************************************************************************
	
    protected void clear()
    {
    	 _scene.clear();
    	 clearAllUndos();			 
    	 
         //_hub.getTextDump().clear();
    }
    
    //*************************************************************************
    
    // ---------------------------- XML I/O -----------------------------------
    
    protected void readFromXML(Element root)
    {
        // TODO: readFromXML()
    }
    
    //*************************************************************************
    
    protected Element writeToXML()
    {
        return null; // TODO: writeToXML()
    }
    
    
    //*************************************************************************
    
    
    protected void loadDocument()
    {
    	//set up file chooser
    	JFileChooser filechooser = new JFileChooser();
    	FileNameExtensionFilter filter = getFileFilter();
    
    	filechooser.setDialogTitle("Load scene");
    	
    	/*
    	 * restrict some options so that the user can't accidentally
    	 * drag-and-drop files, select files of incorrect types etc.
    	 */
    	filechooser.setDragEnabled(false);
    	filechooser.setMultiSelectionEnabled(false);
    	filechooser.setFileHidingEnabled(true);
    	
    	filechooser.setAcceptAllFileFilterUsed(true);
    	filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    	
    	filechooser.setFileFilter(filter);
    	
    	// go to the recently visited directory
    	filechooser.setCurrentDirectory(_lastDirectory);
    	
    	/*
    	 * using null as parent here causes the dialog to be centered
    	 * on screen.
    	 */
    			
    	int retcode = filechooser.showOpenDialog(null);
    	
    	if(retcode == JFileChooser.APPROVE_OPTION)
    	{
    	    // remember this directory
    	    _lastDirectory = filechooser.getCurrentDirectory();
        	
    	    File loadfile = filechooser.getSelectedFile();
    	    //FileInputStream in_stream; 
    	    Document doc = null;

			try {
				//open file and try to load it
				//in_stream = new FileInputStream(loadfile);

				//input stream OK, try to build a JDOM tree
				SAXBuilder builder = new SAXBuilder();
				//doc = builder.build(in_stream);
				doc = builder.build(loadfile);
			}
			catch (FileNotFoundException ex) {
    		System.out.println("Could not open file!");
    		System.out.println(ex.getMessage());
    		return;
    	    }				
    	    catch(IOException ex) 
    	    {
    		System.out.println("Could not load file!");
    		System.out.println(ex.getMessage());
    		return;
    	    }
    	    catch(JDOMException ex)
    	    {
    		System.out.println("Error loading XML data!");
    		System.out.println(ex.getMessage());
    		return;
    	    }
    		
    	    if(doc == null)
    	    {
    		System.out.
    		println("SAXBuilder run OK, but returned document " +
    			 "contains no data!");
    		return;
    	    }
    		
    	    //File loaded OK, now parse actual XML data
    	    System.out.println("File loaded OK!");
    			
    	    parseXMLDocument(doc);
    		
            /*
             * Since the scene has just been loaded, the change flag
             * can be initially set to false because scene contents
             * have not yet been modified.
             *  
             */
            _scene.resetChangeFlag(); 
    	}		
    }
    
    //*************************************************************************
	
    protected void saveDocument()
    {
    	
    	// set up file chooser
    	JFileChooser filechooser = new JFileChooser();
    	FileNameExtensionFilter filter = getFileFilter();
   	
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
    	
    	// go to the recently used directory
    	filechooser.setCurrentDirectory(_lastDirectory);
    	
    	/*
    	 * using null as parent here causes the dialog to be centered
    	 * on screen.
    	 */
    			
    	int retcode = filechooser.showSaveDialog(null);
    	
    	if(retcode == JFileChooser.APPROVE_OPTION)
    	{
            _lastDirectory = filechooser.getCurrentDirectory();
            
            File savefile = filechooser.getSelectedFile();
            String name = savefile.getAbsolutePath();
            // check name and append extension of necessary
			String[] ext = ((FileNameExtensionFilter)filter).getExtensions();
			boolean ends = false;
			for (int i = 0; i < ext.length; i++) if (name.endsWith(ext[i])) ends = true;
			if (!ends && ext.length>0)
				name += "."+ext[0];
			savefile = new File(name);
    		
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
            // debug
            //System.out.println(name);	
            
            // create output file
            FileOutputStream file_stream;
            try 
            {
            	file_stream = new FileOutputStream( name );
            } 
            catch ( FileNotFoundException e ) 
            {  
            	System.out.println(e.getMessage());
            	return;
            }
            
            // serialize JDOM tree and write it to file
            XMLOutputter xml_out 
             = new XMLOutputter(Format.getPrettyFormat());
    
            try 
            {
            	Document doc = assembleXMLDocument();   	
            	xml_out.output( doc, file_stream );
            	
            	file_stream.close();
            } 
            catch ( IOException e ) 
            {
            	System.out.println
            	("Could not serialize the XML stream because of:");
            	
            	System.out.println(e.getMessage());
            }
            
            // Everything went OK!
            
            System.out.println(name + " saved!");
            
            // scene saved, change flag can now be reset!
            _scene.resetChangeFlag();
    	}
    	else
    	{
    	    return; // for now...
    	}
    }
    
    //*************************************************************************
	
    protected void parseXMLDocument(Document doc)
    {
    	System.out.println("Parsing XML data...\n");
    	
    	// first, check XML data for valid header
    	Element root_node = doc.getRootElement();
    	
    	if(!root_node.getName().equals(XML_DOCUMENT_TYPE))
    	{
            System.out.println("Wrong file format!");
            return;
    	}
    	
    	// load display settings
    	Element disp_settings = 
    	 root_node.getChild(JSimpleDisplayPanel.XML_SETTINGS_NAME);
    	
    	_hub.getDisplayPanel().loadSettingsFromXML(disp_settings);
    	
    	// load coordinate system settings
    	Element coord_root = 
    	 root_node.getChild(CoordinateSystem.XML_SETTINGS_NAME);
    	
    	_hub.getCoordinateSystem().loadSettingsFromXML(coord_root);
    	
    	//TODO: load editor settings
    	
    	// load scenery
    	
    	Element scene_root 
    	 = root_node.getChild(JAbstractScene.XML_SCENE_ROOT_NAME);
    	
    	_scene.readFromXML(scene_root);
    	
    	// If a GL view is present, load its settings
    	if(_hub.hasGLDisplayPanel())
    	{
            Element glview_settings = 
             root_node.getChild(JSystemHub.XML_GL_SETTINGS_NAME);
            
            if(glview_settings == null)
            {
            	System.out.println("\nNo GL view settings " +
            				   "in this scene!");
            }
            else
            {
            	_hub.loadGLSettingsFromXML(glview_settings);
            }
    	}
    	
    	// everything went OK!
    	System.out.print("done!\n");
    }
    
    //*************************************************************************
	
    protected Document assembleXMLDocument()
    {
    	// create base document & root node
    	Document doc = new Document();
    	
    	Element root_node = new Element(XML_DOCUMENT_TYPE);
    	root_node.setAttribute(XML_VERSION_NAME, XML_VERSION_VALUE);
    	
    	doc.setRootElement(root_node);
    		
    	// add display settings subtree
    	
    	Element disp_settings 
    	 = _hub.getDisplayPanel().saveSettingsToXML();
    	
    	root_node.addContent(disp_settings);
    	
    	// add coordinate system settings subtree
    	
    	Element coord_system_settings = 
    	  _hub.getCoordinateSystem().saveSettingsToXML();
    	
    	root_node.addContent(coord_system_settings);
    	
    	//TODO: add editor settings subtree
    
    	// add scene contents subtree
    	
    	Element scene_data = _scene.convertToXML();
    	root_node.addContent(scene_data);
    
    	// if GL view is present, add GL view settings subtree
    	if(_hub.hasGLDisplayPanel())
    	{
    	    //Element glview_settings = 
            //m_Hub.getGLDisplayPanel().saveSettingsToXML();
    	    
            Element glview_settings = _hub.saveGLSettingsToXML();
            
    	    root_node.addContent(glview_settings);
    	}
    	
    	return doc; // done!
    }
	
    //*************************************************************************
    // 			      Private instance methods
    //*************************************************************************
		
    private void createCommonMenus()
    {
    	//create file menu and its items
    	_uiFileMenu = new JMenu("File");
    	
    	JMenuItem _FileLoadItem = new JMenuItem("Load..");
    	_FileLoadItem.setActionCommand(String.valueOf(CMD_LOAD));
    	_FileLoadItem.addActionListener(this);
    	
    	JMenuItem _FileSaveItem = new JMenuItem("Save..");
    	_FileSaveItem.setActionCommand(String.valueOf(CMD_SAVE));
    	_FileSaveItem.addActionListener(this);
    	
    	JMenuItem _ExitItem 	 = new JMenuItem("Exit");
    	_ExitItem.setActionCommand(String.valueOf(CMD_EXIT));
    	_ExitItem.addActionListener(this);
    	
    	_uiFileMenu.add(_FileLoadItem);
    	_uiFileMenu.add(_FileSaveItem);
    	
    	_uiFileMenu.addSeparator();
    	
    	_uiFileMenu.add(_ExitItem);
    			
    	// create editor menu and some of its common items
    	_uiEditorMenu = new JMenu("Edit");
    	
    	JMenuItem clear_item = new JMenuItem("Clear scene");
    	clear_item.setActionCommand(String.valueOf(CMD_CLEAR));
    	clear_item.addActionListener(this);
    	
    	JMenuItem reset_item = new JMenuItem("Reset everything!");
    	reset_item.setActionCommand(String.valueOf(CMD_RESET));
    	reset_item.addActionListener(this);
    	
    	_uiEditorMenu.add(clear_item);
    	_uiEditorMenu.add(reset_item);
    	_uiEditorMenu.addSeparator();
    }
	
}


	

