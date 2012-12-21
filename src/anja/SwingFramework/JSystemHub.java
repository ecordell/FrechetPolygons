/*
 * File : JSystemHub.java
 * 
 * Created on Oct 21, 2004
 * 
 */
package anja.SwingFramework;

// these are for updateAffineTransform() implementation

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.Font;

import javax.swing.*;

import org.jdom.Element; // for load- and saveGLsettingsToXML() 

/**
 * Somewhat a superset of Sascha's Register class, meant to be used
 * with my own component architecture ;)
 * 
 * <p>
 * Purpose of this damn thing: to simplify the initialization of
 * various components by providing a common access point. Also, it can
 * automatically remind ye if any of the components haven't been
 * initialized.
 * 
 * <p>
 * WARNING: This has to be instantiated as the very first thing,, and
 * in the 'outermost' container class, in order for everything to work
 * properly.
 * 
 * <br>Exception: NullPointerException if a component wasn't initialized
 * 
 * @author ibragim kouliev
 * 
 * <p>
 * Notes:
 * <p>
 * Concerning the separation of GL-related stuff from the core
 * framework:
 * <p>
 * Since not everyone needs to use openGL in their applications, I've
 * separated all GL-related functionality into SwingFramework.GL
 * subpackage. There are however a few dependencies within
 * JAbstractEditor's loadDocument() and saveDocument() routines (the
 * sections that load and save JGLDisplayPanel()'s settings).
 * <p>
 * These sections can't call any JGLDisplayPanel - specific methods,
 * since that would require importing the JGLDisplayPanel type which
 * is what we're trying to avoid!
 * <p>
 * [otherwise it's a vicious circle, really, because importing
 * JGLDisplayPanel would require that it be compileable, which in
 * turns need JOGL etc etc.]
 * <p>
 * Thus, I've did two important things here: 1) Moved all GL things
 * into the JGLSystemHub class, derived from JSystemHub, to separate
 * GL stuff from the base package.
 * <p>
 * 2) put three virtual functions into the base JSystemHub class,
 * namely hasGLDisplayPanel(), saveGLSettingsToXML() and
 * loadGLSettingsFromXML(), which allow JAbstractEditor (and possibly
 * other components as well) to query and access the JGLDisplayPanel
 * functionality indirectly, without importing its class type, which
 * resolves the aforementioned compilation issues. The base
 * implementations of these methods do nothing, whereas the derived
 * ones in JGLSystemHub patch the calls through to corresponding
 * methods in JGLDisplayPanel.
 * <p>
 * [It is necessary to redeclare JGLDisplayPanel's XML_SETTINGS_NAME
 * constant inside JSystemHub so that JAbstractEditor can still use
 * it!]
 * 
 */
public class JSystemHub
{
    //*************************************************************************
    //                             public constants
    //*************************************************************************
    
    public static final String XML_GL_SETTINGS_NAME = "GL_view_settings";
    
    //*************************************************************************
    //                             private constants
    //*************************************************************************
                
    private static final String XML_GL_DUMMY         = "GL_Dummy";
   
    //*************************************************************************
    //	 		     Private instance variables
    //*************************************************************************
	
    // system components	
    private JComponent	                m_Parent;
    
    private CoordinateSystem            m_coordSystem;
    private JSimpleDisplayPanel         m_displayPanel;	
    private JAbstractScene              m_Scene;
    private JAbstractEditor	        m_Editor;	
    private JTextMessageDump            m_messageDump;
    	
    // visuals
    private Font                        _defaultFont;    
    private AffineTransform             _fontTransform;
    private Point2D                     _tempPoint1;
    private Point2D                     _tempPoint2;
    
    
    //*************************************************************************
    //	 			   Class variables
    //*************************************************************************
		
    //*************************************************************************
    //	 			    Constructors
    //*************************************************************************
	
    /**
     * Creates a new system hub. All internal component pointers are
     * initialized to <code>null</code>
     *  
     */
    public JSystemHub()
    {	
    	m_Parent 		= null;		
    	m_coordSystem  		= null;
    	m_displayPanel 		= null;
    	m_Scene  	   	= null;
    	m_Editor 	   	= null;		
    	m_messageDump  		= null;		
    	
        
        // init default font etc.
        _defaultFont   = new Font("Arial", Font.PLAIN, 12);
        
        _fontTransform  = new AffineTransform();
        
        _tempPoint1     = new Point2D.Double();
        _tempPoint2     = new Point2D.Double();
        
        _tempPoint1.setLocation(0.0, 0.0);
        _tempPoint2.setLocation(1.0, 0.0);
    }
	
    //*************************************************************************
    //	 		       Public instance methods
    //*************************************************************************

    // ------------------------- Getters / Setters ----------------------------
    
    /**
     * Sets the reference to the parent component. The parent can be
     * anything derived from {@link JComponent}
     * 
     * @param parent Reference to a parent component
     */
    public void setParent(JComponent parent)
    {
    	m_Parent = parent;
    }
    
    //*************************************************************************
	
    /**
     * Sets the coordinate system component.
     * 
     * @param cs reference to a coordinate system component
     */
    public void setCoordinateSystem(CoordinateSystem cs)
    {
    	m_coordSystem = cs;
    }
    
    //*************************************************************************
	
    /**
     * Sets the 2-d display panel component.
     * 
     * @param dpanel reference to a display component
     */
    public void setDisplay(JSimpleDisplayPanel dpanel)
    {
    	m_displayPanel = dpanel;
    }
    
    //*************************************************************************
    
    /**
     * Sets the scene reference. The <code>scene</code> parameter
     * can be a reference to any subclass of {@link JAbstractScene}
     * 
     * @param scene scene reference
     */
    public void setScene(JAbstractScene scene)
    {
    	m_Scene = scene;
    }
    
    //*************************************************************************
	
    /**
     * Sets the editor reference. The <code>editor</code> parameter
     * can be a reference to any subclass of {@link JAbstractEditor}
     * 
     * @param editor editor reference
     */
    public void setEditor(JAbstractEditor editor)
    {
    	m_Editor = editor;
    }
    
    //*************************************************************************
    
    /**
     * Sets the reference to the diagnostic message dump window.
     * 
     * @param dump
     */
    public void setTextDump(JTextMessageDump dump)
    {
    	m_messageDump = dump;
    }
    
    //*************************************************************************
	
    /**
     * Convenience method for accessing the top-level application
     * container from any system component.
     * 
     * @return Reference to a JFrame top-level container
     */
    public JFrame getApplication()
    {
    	return (JFrame)m_Parent.getTopLevelAncestor();
    }
    
    //*************************************************************************
    
    /**
     * 
     * @return Reference to the parent component.
     * @throws NullPointerException if parent has not been set.
     */
    public JComponent getParent()
    {
    	if(m_Parent == null)
    	{
            throw new NullPointerException
            ("Parent pointer has not been set!");
    		
    	}
    	
    	return m_Parent;
    }
    
    //*************************************************************************
	
    /**
     * 
     * @return Reference to the coordinate system.
     * @throws NullPointerException if coordinate system has not been
     *             set.
     */
    public CoordinateSystem getCoordinateSystem()
    {
    	if( m_coordSystem  ==  null )
    	{
            throw new NullPointerException
            ("Coordinate system has not been initialized!");
    	}
    	
    	return m_coordSystem;		
    }
    
    //*************************************************************************
    
    /**
     * 
     * @return Reference to the display panel.
     * @throws NullPointerException if the display panel has not been
     *             set.
     */
    public JSimpleDisplayPanel getDisplayPanel()	
    {
    	if( m_displayPanel == null)
    	{
            throw new NullPointerException
            ("Display panel has not been initialized!");			
    	}
    	
    	return m_displayPanel;
    }
    
    //*************************************************************************
    
    /**
     * 
     * @return Reference to the scene.
     * @throws NullPointerException if the scene has not been set.
     */
    public JAbstractScene getScene()
    {
    	if( m_Scene == null )
	{
            throw new NullPointerException
            ("Scene has not been initialized!");
    	}
    	
    	return m_Scene;
    }
    
    //*************************************************************************
	
    /**
     * 
     * @return Reference to the editor.
     * @throws NullPointerException if the editor has not been set.
     */
    public JAbstractEditor getEditor()
    {
    	if( m_Editor == null )
    	{
            throw new NullPointerException
            ("Editor has not been initialized!");
    	}
    	
    	return m_Editor;
    }
    
    //*************************************************************************
	
    /**
     * 
     * @return Reference to the message dump window.
     * @throws NullPointerException if the message dump hasn't been set.
     */
    public JTextMessageDump getTextDump()
    {
    	if(m_messageDump == null)
    	{
            throw new NullPointerException
            ("Message dump hasn't been initialized!");
    	}
    	return m_messageDump;
    }
    
    //*************************************************************************
    
    /**
     * Used by JAbstractEditor to determine whether a GL display
     * panel component is present, for loading/saving XML settings.
     * 
     * @return always <code><b>false</b></code> here to indicate
     * that there's no JGLDisplayPanel component!
     */
    public boolean hasGLDisplayPanel()
    {
        return false;
    }
    
    //*************************************************************************
	
    /**
     * This function updates affine transforms throughout
     * the entire system. Used primarily by various
     * {@link CoordinateSystem} system methods to synchronize
     * transforms in other components after a coordinate 
     * system has been modified. 
     * 
     * @param tx new affine transform to be used
     */	
    public void updateTransform(AffineTransform tx)
    {
        
        // calculate current pixel size and inverted font
        
        _tempPoint1.setLocation(0.0, 0.0);
        _tempPoint2.setLocation(1.0, 0.0);
        
        tx.transform(_tempPoint1, _tempPoint1);
        tx.transform(_tempPoint2, _tempPoint2);
        
        double pixel_size = _tempPoint2.getX() - _tempPoint1.getX();
        
        _fontTransform.setTransform(1.0 / tx.getScaleX(), 0.0,
                                    0.0,  1.0 / tx.getScaleY(), 
                                    0.0, 0.0); 
        
        Font invertedFont = _defaultFont.deriveFont(_fontTransform);
        
        // pass new visuals to the rest of the system
        
    	m_displayPanel.updateAffineTransform(tx);
        
    	m_Scene.updateAffineTransform(tx, pixel_size, invertedFont);		
    	m_Editor.updateAffineTransform(tx, pixel_size, invertedFont);		
    	
    }
    
    //*************************************************************************
    
    /**
     * Default implementation supplies an JDOM Element which guaranteed
     * to be DISMISSED by the JAbstractEditor XML loading routine. 
     * (Note: This is required by the routine's implementation).
     * 
     * 
     */
    public Element saveGLSettingsToXML()
    {
        return new Element(XML_GL_DUMMY);
    }
    
    //*************************************************************************
    
    /**
     * Default implementation does nothing.
     * 
     */
    public void loadGLSettingsFromXML(Element subRoot)
    {
        // do nothing here!
    }
		
    //*************************************************************************
    //	 			 Static methods
    //*************************************************************************
		
}




