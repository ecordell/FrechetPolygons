package anja.swinggui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;


/**
 * BufferedCanvas ist eine von JComponent abgeleitete Zeichenflaeche.
 *
 * BufferedCanvas verwaltet ein Offscreen-Image, in das mit dem
 * durch getImageGraphics() gelieferten Graphics-Objekt gezeichnet
 * werden kann.
 *
 * Durch die Methode paint() wird das Offscreen-Image in die Zeichenflaeche
 * kopiert.
 *
 * Ein Programmstueck, das gebuffert auf der Zeichenflaeche zeichnet,
 * koennte also etwa folgendermassen aussehen:
 *
 *      // Zeichnen auf offscreen image
 *      Graphics g = canvas.getImageGraphics
 *      g.fillRect(...);
 *      ...
 *      g.dispose();
 *
 *      // Kopieren des offscreen image auf Canvas
 *      g = canvas.getGraphics();
 *      canvas.paint( g );
 *      g.dispose();
 *
 * @version 0.1 21.07.01
 * @author      Ulrich Handel
 */

//****************************************************************
public class BufferedCanvas extends JComponent
//****************************************************************
{

    //************************************************************
    // private variables
    //************************************************************

    private Image _img = null;      // offscreen image

    private int _img_width  = 0;    // current size of the image
    private int _img_height = 0;    //
    
    private Dimension _preferred_size; //  preferred canvas size

    //************************************************************
    // constructors
    //************************************************************

    /**
     * Create a new BufferedCanvas
     */

    //============================================================
    public BufferedCanvas()
    //============================================================
    {
    	_preferred_size = new Dimension(); 
    	
        setOpaque( true );
        setBackground( Color.white );

        // enable component events
        // so the canvas can react to a resize
        enableEvents( AWTEvent.COMPONENT_EVENT_MASK );
    } // BufferedCanvas




    //************************************************************
    // public methods
    //************************************************************


    /**
     * Get the Graphics object of the offscreen image.
     * Returns null, if no offscreen image exists.
     */

    //============================================================
    public Graphics getImageGraphics()
    //============================================================
    {
        if( _img == null )
            return null;

        return _img.getGraphics();
    } // getImageGraphics




    //************************************************************
    // public methods which overwrite inherited methods
    //************************************************************



    /**
     * Return the minimum canvas size.
     * This method is used by the layout manager.
     */

    //============================================================
    public Dimension getMinimumSize()
    //============================================================
    {
        return _preferred_size;
    } // getMinimumSize



    /**
     * Return the preferred canvas size.
     * This method is used by the layout manager.
     */

    //============================================================
    public Dimension getPreferredSize()
    //============================================================
    {
        return _preferred_size;
    } // getPreferredSize



    /**
     * The paint method is called, when a repaint of the canvas is
     * neccessary. It will copy the offscreen image to the canvas.
     */

    //============================================================
    public void paint( Graphics g )
    //============================================================
    {
        if( g != null && _img != null )
        {
            g.drawImage( _img, 0, 0, this );
        } // if
    } // paint



    /**
     * React to a component event
     */

    //============================================================
    public void processComponentEvent( ComponentEvent e )
    //============================================================
    {
        if( e.getID() == ComponentEvent.COMPONENT_RESIZED )
        {
            // the canvas has been resized

            // Hier wird falls erforderlich ein neues offscreen image
            // erzeugt, dass mindestens die neuen Abmessungen des Canvas
            // hat.

            // Es ist wichtig, dass dies geschieht, bevor registrierte
            // ComponentListener ueber den Canvas-Resize informiert
            // werden ( Aufruf super.processComponentEvent() am Ende
            // dieser Methode ), da diese dann eventuell schon auf das
            // aktuelle offscreen image zeichnen wollen.

            Dimension dim = getSize();
        	//Dimension dim = getPreferredSize();
        	
            int can_width  = dim.width;
            int can_height = dim.height;

            _preferred_size.setSize(dim);
            
            //_preferred_size = dim;  // Die bevorzugten Abmessungen sind
                       // immer die Aktuellen, damit bei einem re-layout
                       // des Containers welcher den Canvas enthaelt,
                       // die Canvas-Groesse nicht veraendert wird.

            if( can_width > _img_width || can_height > _img_height )
            {
                // the size of the canvas extends that of the
                // previous image

                Image old_image = _img; // preserve the previous
                                        // image

                // calculate the new size to which the image
                // has to be enlarged
                _img_width  = Math.max( can_width, _img_width );
                _img_height = Math.max( can_height, _img_height );

                // create the new image
                _img = createImage( _img_width, _img_height );

                if( old_image != null )
                {
                    // copy the old image to the new one
                    Graphics g = _img.getGraphics();
                    
                    g.drawImage( old_image, 0, 0, this );
                    
                    g.dispose();

                    // free all occupied resources
                    old_image.flush();
                } // if
            } // if
        } // if


        super.processComponentEvent( e );
    } // processComponentEvent



    /**
     * Set the preferred canvas size.
     */

    //============================================================
    public void setPreferredSize( Dimension size )
    //============================================================
    {
        _preferred_size = size;
    } // setPreferredSize

} // BufferedCanvas
