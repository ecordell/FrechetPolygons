
/* File: GraphicsContext.java
 * Created on: ?
 * 
 */

package anja.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Stroke;

/**
* GraphicsContext
*
* "Style" for drawing objects
*
* @version	2.0 24 May 2004
* @author	Ibragim Kouliev
*/

public class GraphicsContext implements Cloneable 
{
       
    //*************************************************************************
    //                        Private instance variables
    //*************************************************************************
            
    private Color       _foregroundColor;
    private Color       _backgroundColor;
        
    private Color       _fillColor;
    private Color       _outlineColor;
    
    /* Now here's a bummer....
     * _foregroundColor is actually meant to act
     * as the outline color. Now that I introduced the
     * outline color parameter as well, I've actually
     * got a duplicate here :-( Silly me.....
     * 
     */
    
    private BasicStroke _stroke;
    private int         _fillStyle;
    private Font        _font;

        
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    /**
     * Default Constructor<br>
     * Default parameters are:
     * <ul>
     * <li>Foreground color: black</li>
     * <li>Background color: white</li>
     * <li>Outline color: black</li>
     * <li>Fill color: gray</li>
     * <li> Default jawa.awt.BasicStroke instance </li>
     * <li> Font: Helvetica, plain, size: 14 points</li>
     * </ul>
     */
    public GraphicsContext()
    {
        _foregroundColor = Color.black;
        _backgroundColor = Color.white;
        
        _fillColor       = Color.gray;
        _outlineColor    = Color.black;
       
        _stroke = new BasicStroke(0, BasicStroke.CAP_BUTT,
                                  BasicStroke.JOIN_BEVEL);
        
        _fillStyle = 0;
        _font = new Font("Helvetica", Font.PLAIN, 14);
    }
    
    //*************************************************************************
    
    /**
     * Constructor
     * 
     * @param foreColor Foreground color
     * @param backColor Background color
     * @param filColor Filling color
     * @param newFont The font to use
     * @param cap The cap style
     * @param join The line join style
     * @param width The pen width (of the lines)
     * @param dash The dash type
     * @param phase The dash phase
     * 
     */
    public GraphicsContext(Color foreColor,
                           Color backColor,
                           Color filColor,
                           Font  newFont,
                           int cap,
                           int join,
                           float width,
                           float[] dash,
                           float phase)
    {
        _foregroundColor = foreColor;
        _backgroundColor = backColor;
        _fillColor       = filColor;
        _font           = newFont;
        _fillStyle       = 0;
        
        _stroke = new BasicStroke(width, cap, join, 10.0f, 
                                 dash, phase);
    }
    
    //*************************************************************************
    
    /**
     * Constructor
     * 
     * @param foreColor Foreground color
     * @param backColor Background color
     * @param filColor Filling color
     * @param cap The cap style
     * @param join The line join style
     * @param width The pen width (of the lines)
     * @param dash The dash type
     * @param phase The dash phase
     * 
     */
    public GraphicsContext(Color foreColor,
                           Color backColor,
                           Color filColor,
                           int cap,
                           int join,
                           float width,
                           float[] dash,
                           float phase)
    {
        _foregroundColor = foreColor;
        _backgroundColor = backColor;
        _fillColor      = filColor;
        
        _fillStyle = 0;
        _font = new Font("Helvetica", Font.PLAIN, 14);
                
        _stroke = new BasicStroke(width, cap, join, 10.0f, 
                                 dash, phase);
    }
    
    //*************************************************************************
        
    /**
     * A constructor with a minimal and useful set of parameters.
     * Other parameters of the internal basic stroke 
     * and font are fixed as follows:
     * 
     * <br>Font is Helvetica, PLAIN, 14 points
     * <br>Cap style is {@link java.awt.BasicStroke#CAP_BUTT}
     * <br>Join style is {@link java.awt.BasicStroke#JOIN_MITER}
     * <br>Miter limit is 10.0
     * <br>Dash phase is 0.0
     * 
     * 
     * @param foreColor Foreground color
     * @param backColor Background color
     * @param fillColor Filling color
     * @param width The pen width (of the lines)
     * @param dash The dash type
     */
    public GraphicsContext(Color foreColor,
                           Color backColor,
                           Color fillColor,
                           float width,
                           float[] dash)
    {
        this(foreColor, backColor, fillColor,
             BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
             width, dash, 0.0f);
    }
    
    //*************************************************************************
    
    /**
     * A constructor with minimal useful set of parameters.
     * 
     * <br>Uses the default constructor for further initialization.
     * 
     * @param fill Filling color
     * @param outline Outline and foreground color
     */
    public GraphicsContext(Color outline,
                           Color fill)
    {
        this();
        _fillColor      = fill;
        _outlineColor   = outline;
        _fillStyle      = 1;
        
        /* compatibility section for old code
         * Reason: current geometry core primitives use the 
         * 'foreground' color parameter for outline drawing. It
         * should really have been called 'outline' color.....
         */
        _foregroundColor = outline;
    }
    
    //*************************************************************************
    
    /**
     * Copy constructor
     * 
     * @param source A GraphicsContext from which to copy attributes
     */
    public GraphicsContext(GraphicsContext source)
    {
        super();
        
        BasicStroke stroke = (BasicStroke)source.getStroke();
        
        this.setForegroundColor(source._foregroundColor);
        this.setBackgroundColor(source._backgroundColor);
        this.setFillColor(source._fillColor);
        this.setFillStyle(source._fillStyle);
    
        this.setOutlineColor(source._outlineColor);
        
        this.setFont(source._font);
        this.setStroke(new BasicStroke(stroke.getLineWidth(),
                                       stroke.getEndCap(),
                                       stroke.getLineJoin(),
                                       stroke.getMiterLimit(),
                                       stroke.getDashArray(),
                                       stroke.getDashPhase()));
               
    }
    
    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
    
    /**
     * Get foreground color
     * 
     * @return The foreground color
     */
    public Color getForegroundColor() 
    {
       return _foregroundColor;
    }
     
    //*************************************************************************

    /**
     * Set foreground color
     * 
     * @param color The new foreground color
     */
    public void setForegroundColor(Color color) 
    {
       _foregroundColor = color;
    }
 
    //*************************************************************************

    /**
     * Get background color
     * 
     * @return The background color
     */
    public Color getBackgroundColor() 
    {
        return _backgroundColor;
    }
     
    //*************************************************************************

    /**
     * Set background color
     * 
     * @param color The new background color
     */
    public void setBackgroundColor(Color color) 
    {
        _backgroundColor = color;
    }
     
    //*************************************************************************

    /**
     * Get fill color
     * 
     * @return The fill color
     */
    public Color getFillColor() 
    {
       return _fillColor;
    }
     
    //*************************************************************************

    /**
     * Set fill color
     * 
     * @param color The new fill color
     */
    public void setFillColor(Color color) 
    {
       _fillColor = color;
    }
     
    //*************************************************************************

    /**
     * Get outline color
     * 
     * @return The outline color
     */
    public Color getOutlineColor()
    {
        return _outlineColor;
    }
     
    //*************************************************************************
     
    /**
     * Set outline color
     * 
     * @param color The new outline color
     */
    public void setOutlineColor(Color color)
    {
        _outlineColor = color;
        
        // compatibility section for old code
        _foregroundColor = color;
    }
     
    //*************************************************************************
     
    /**
    * Get Stroke
    * 
    * @return The stroke object
    */
    public Stroke getStroke() 
    {
       return _stroke;
    }
    
    //*************************************************************************
    
    /**
     * Sets a completely new BasicStroke instance. All old
     * stroke parameters are discarded!
     * 
     * @param newstroke The new stroke instance
     */
    public void setStroke(BasicStroke newstroke)
    {
        _stroke = newstroke;
    }
     
    //*************************************************************************

    /**
     * Get BasicStroke line width
     * 
     * @see java.awt.BasicStroke
     * 
     * @return The line width
     */
    public float getLineWidth() 
    {
       return _stroke.getLineWidth();
    }
     
    //*************************************************************************

    /**
     * Set line width
     * 
     * @param width The new line width
     */
    public void setLineWidth(float width) 
    {
        _stroke = new BasicStroke(width, _stroke.getEndCap(), 
                                  _stroke.getLineJoin(),
                                  _stroke.getMiterLimit(), 
                                  _stroke.getDashArray(),
                                  _stroke.getDashPhase());
    }
     
    //*************************************************************************
    
    /**
     * Returns the current line dash style 
     * 
     * @return The dash style
     */
    public float[] getDashArray()
    {
        return _stroke.getDashArray();
    }
    
    //*************************************************************************
    
    /**
     * Set dash pattern
     * 
     * @param dash The dash pattern
     */
    public void setDash(float dash[])
    {
        _stroke = new BasicStroke(_stroke.getLineWidth(),
                                 _stroke.getEndCap(), _stroke.getLineJoin(),
                                 _stroke.getMiterLimit(), dash,
                                 _stroke.getDashPhase());
    }
     
    //*************************************************************************

    /**
     * Get BasicStroke end cap
     * 
     * @see java.awt.BasicStroke
     * 
     * @return The end cap
     */
    public float getEndCap() 
    {
       return _stroke.getEndCap();
    }
     
    //*************************************************************************

    /**
     * Set en dcap style
     * 
     * @param cap Cap style
     */
    public void setEndCap(int cap) 
    {
        _stroke = new BasicStroke(_stroke.getLineWidth(), 
                                  cap, _stroke.getLineJoin(),
                                  _stroke.getMiterLimit(), 
                                  _stroke.getDashArray(),
                                  _stroke.getDashPhase());
    }
     
    //*************************************************************************

    /**
     * Get BasicStroke Line Join Style
     * 
     * @see java.awt.BasicStroke
     * 
     * @return The line join style
     */
    public float getLineJoin()
    {
       return _stroke.getLineJoin();
    }
     
    //*************************************************************************

    /**
     * Set line join style
     * 
     * @param join The new line join style
     */
    public void setLineJoin(int join)
    {
        _stroke = new BasicStroke(_stroke.getLineWidth(), 
                                  _stroke.getEndCap(), join,
                                 _stroke.getMiterLimit(), 
                                  _stroke.getDashArray(),
                                  _stroke.getDashPhase());
    }
     
    //*************************************************************************
        
    /**
     * Set the dash style
     * 
     * @param pattern The dash pattern
     * @param phase The dash phase
     *
     */
    public void setDashStyle(float[] pattern, float phase)
    {
        _stroke = new BasicStroke(_stroke.getLineWidth(), 
                                  _stroke.getEndCap(), 
                                 _stroke.getLineJoin(), 
                                  _stroke.getMiterLimit(),
                                  pattern, phase);
        
    }
     
    //*************************************************************************

    /**
     * Get fill style
     * 
     * @return The fill style
     */
    public int getFillStyle() 
    {
       return _fillStyle;
    }
     
    //*************************************************************************

    /**
     * Set fill style
     * 
     * @param style The new fill style
     */
    public void setFillStyle(int style) 
    {
       _fillStyle = style;
    }
     
    //*************************************************************************

    /**
     * Get font
     * 
     * @return The font
     */
    public Font getFont() 
    {
       return(_font);
    }
     
    //*************************************************************************

    /**
     * Set font
     * 
     * @param font The new font
     */
    public void setFont(Font font) 
    {
       _font = font;
    }
     
    //*************************************************************************

    /**
     * Clones a GraphicsContext object
     * 
     * @return A copy of this object
     */
    public GraphicsContext copy() 
    {
        
        GraphicsContext gc = new GraphicsContext(this);
        return gc;
    }
    
    
    
    /**
     * Clones a GraphicsContext object
     * 
     * @return A copy of this object
     */
    @Override
    public GraphicsContext clone()
    {
        return copy();
    }
     
    //*************************************************************************

    /**
     * Generates textual description of a GraphicsContext object
     * 
     * @return The textual description
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append(   "foreground: " + _foregroundColor );
        result.append( "\nbackground: " + _backgroundColor );
        result.append( "\nfill      : " + _fillColor );
        result.append( "\nstroke    : " + _stroke );
        result.append( "\nstyle     : " + _fillStyle );
        result.append( "\nfont      : " + _font );
        return result.toString();
    }
        
}
