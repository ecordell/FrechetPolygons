/*
 * Created on Jan 11, 2005
 * 
 * JAbstractScene.java
 */
package anja.SwingFramework;


import java.awt.*;
import java.awt.geom.*;

import org.jdom.*;

/**
 * Generic container for various objects. A scene can be modified
 * through various provided methods. It can also be queried, drawn and
 * loaded from \converted to XML representation.
 * 
 * <p>Please note the that almost all methods in this class are
 * abstract - this way it defines the basic structure and facilities
 * of a scene without specifying any particular implementation and
 * data structures. These must be provided by derived classes!
 * 
 * <p> For an example on how to design and implement a subclass 
 * look at {@link anja.SwingFramework.graph.JGraphScene}
 * 
 * 
 * @author Ibragim Kouliev
 */
public abstract class JAbstractScene
{
    //*************************************************************************
    //	 			   Public constants
    //*************************************************************************
	
    public static final String XML_SCENE_ROOT_NAME 	= "Scene";
	
    //*************************************************************************
    //	 			   Class variables
    //*************************************************************************
	
    //*************************************************************************
    //	 		     Protected instance variables
    //*************************************************************************
	
    protected JSystemHub 	  _hub;
    protected boolean 	 	  _sceneWasChanged;
    
    protected double	          _pixelSize; // current pixel size
    
    protected AffineTransform     _textTransform;
    protected AffineTransform     _worldToScreenTransform;
    
    protected Font                _invertedFont;
	
    //*************************************************************************
    //	 		      Private instance variables
    //*************************************************************************
	
    //*************************************************************************
    //	 			     Constructors
    //*************************************************************************
	
    /**
     * Creates an empty scene.
     * 
     *  
     */	
    public JAbstractScene(JSystemHub hub)
    {
    	_textTransform = new AffineTransform();
    	
    	_hub = hub;
    	hub.setScene(this);	
    }
	
    //*************************************************************************
    //	 		       Public instance methods
    //*************************************************************************
	
    /**
     * Used by editors to determine whether a scene has been changed
     * since the last file save.
     * 
     * @return <code>true</code> if the scene has been modified
     *         since the last save.
     */	
    public boolean sceneHasChanged()
    {
    	return _sceneWasChanged;
    }
    
    //*************************************************************************
	
    /**
     * Used internally by the {@link JAbstractEditor#saveDocument()}
     * <br>When the change flag is reset, the system will know that
     * no changes have occured since the last save, and consequently,
     * will not pop up the warning dialog when the user calls the
     * load/exit/clear command from the file/editor menus
     *  
     */
    public void resetChangeFlag()
    {
    	_sceneWasChanged = false;
    }
    
    //*************************************************************************
    
    /**
     * Sets the change flag to signal to the system that a change
     * has occured since the last save.
     *
     */
    public void setChangeFlag()
    {
    	_sceneWasChanged = true;
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
     * <br>
     * TODO: Currently the transform update mechanism makes no
     * provision for general affine transforms(i.e. if they contain
     * rotation or shearing components). This will need to be fixed
     * later!
     */ 
    public void  updateAffineTransform(AffineTransform tx, 
                                       double pixelSize, 
                                       Font invertedFont)
    {
    	 _worldToScreenTransform = tx;
    	
         _pixelSize = pixelSize;
         _invertedFont = invertedFont;
         
         /*
       	 m_textTransform.setTransform(tx.getScaleX(), 0,
        			      0, - tx.getScaleY(),
        			      tx.getTranslateX(), 
        			      tx.getTranslateY());
       	
         Point2 point0 = new Point2(0,0);
         Point2 point1 = new Point2(1,0);
          
         tx.transform( point0, point0 );
         tx.transform( point1, point1 );
         	      
         m_dPixelSize = point1.x - point0.x;*/	        
    }
  
		
    //*************************************************************************
    //	 		           Abstract methods
    //*************************************************************************
	
    /**
     * Draws the scene using the specific graphics context.
     */
    public abstract void draw(Graphics2D g);
    
    //*************************************************************************
	
    /**
     * Returns the bounding rectangle of a scene.
     * 
     * @return The bounding rectangle
     */
    public abstract Rectangle2D getBoundingRectangle();
    
    //*************************************************************************
	
    /**
     * Default implementation does nothing.
     * 
     * @param root
     */
    public void readFromXML(Element root)
    {
    	
    }
    
    //*************************************************************************
	
    /**
     * The default implementation creates and return an empty scene
     * root node. This <u>MUST</u> be called by every overriding
     * implementation in order to maintain correct XML document
     * structure!
     * 
     * <p>The code would look like this: 
     * <p><pre>
     * public Element convertToXML()
     * {
     *    Element root = super.convertToXML();
     *    .... Add stuff to root; 
     *    
     *    return root;
     * }
     * </pre>
     * 
     * @return an empty scene root
     */
    public Element convertToXML()
    {
    	Element scene_root = new Element(XML_SCENE_ROOT_NAME);
    	return scene_root;
    }
    
    //*************************************************************************
	
    /**
     * Clears the scene 
     * 
     */	
    public abstract void clear();
    
    //*************************************************************************

    /**
     * 
     * @return <code>true</code> if the scene is empty,
     * or <code>false</code> otherwise 
     */
    public abstract boolean isEmpty();
    
    //*************************************************************************
        
    //*************************************************************************
    //		              Protected instance methods
    //*************************************************************************
    
    //*************************************************************************
    //	 		       Private instance methods
    //*************************************************************************
	
}



