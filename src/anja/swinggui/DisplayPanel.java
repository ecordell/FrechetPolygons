package anja.swinggui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

import java.util.Vector;


/**
 * Panel mit gepufferter Zeichenflaeche ( BufferedCanvas ), Scrollbars
 * und Zoom-Buttons.
 *
 * Ein DisplayPanel hat die Aufgabe eine graphische Szene zu
 * praesentieren und dabei Zoom- und Scrollmoeglichkeiten bereitzustellen.
 *
 * Die darzustellende Szene muss das Interface DisplayPanelScene
 * implementieren und wird mit der Methode setScene() beim DisplayPanel
 * registriert( @see DisplayPanelScene ).
 *
 * Die wesentliche Methode ist refresh().
 * Durch refresh() wird das gesamte DisplayPanel aktualisiert
 * ( Einstellungen der Scrollbars und Buttons sowie Neuzeichnen
 *   der Szene )
 *
 * Mit getCanvas() kann die Zeichenflaeche des Panels erfragt werden
 * ( z.B. um sich dort als MouseListener zu registrieren ).
 *
 * @version 0.1 21.07.01
 * @author      Ulrich Handel
 */


//****************************************************************
public class DisplayPanel extends JPanel
                          implements AdjustmentListener,
                                     ActionListener,
                                     ComponentListener
//****************************************************************
{

    //************************************************************
    // private constants
    //************************************************************

    private final static int _MIN_INSIDE = 10; // siehe refresh()
    private final static int _FIT_BORDER = 10; // siehe actionPerformed()



    //************************************************************
    // private variables
    //************************************************************


    private JScrollBar _scroll_hor;  // the horizontal scrollbar.
    private JScrollBar _scroll_vert; // the vertical scrollbar.


    private JPanel _zoom_panel;      // panel for zoom buttons

    private JRepeatButton _button_zoom_in;  // zoom buttons
    private JRepeatButton _button_zoom_out; //
    private JButton _button_fit;            //


    private BufferedCanvas _canvas;   // gebufferter Canvas


    private DisplayPanelScene _scene; // Die Szene, die im DisplayPanel
                                      // dargestellt wird

    private boolean _first_refresh = true; // Erster Refresh des Panels
                                           // mit der  gesetzten Szene
                                    // ( siehe refresh() und setScene() )

    private double _can_center_x; // Mittelpunkt des Canvas beim letzten
    private double _can_center_y; // refresh()-Aufruf


    private double _zoom_factor = 1.1;  // Faktor um den beim Zoomen
                                        // die Darstellung vergroessert
                                        // bzw. verkleinert wird


    private Vector _action_listener = new Vector();
                             // Liste aller registrierten ActionListener


    //************************************************************
    // constructors
    //************************************************************



    /**
     * Create a new DisplayPanel
     */

    //============================================================
    public DisplayPanel( ActionListener action_listener )
    //============================================================
    {
        addActionListener( action_listener );

        // Alle benoetigten Komponenten erzeugen

        _scroll_vert = new JScrollBar( JScrollBar.VERTICAL );
        _scroll_hor  = new JScrollBar( JScrollBar.HORIZONTAL );

        _button_zoom_in  = new JRepeatButton( "Zoom in" );
        _button_zoom_out = new JRepeatButton( "Zoom out" );
        _button_fit      = new JButton( "Fit" );


        // Panel fuer Zeichenflaeche mit Rahmen konstruieren

        _canvas = new BufferedCanvas();

        JPanel canvas_panel = new JPanel( new GridLayout() );
        canvas_panel.setBackground( _canvas.getBackground() );
        canvas_panel.setBorder( new BevelBorder( BevelBorder.LOWERED) );
        canvas_panel.add( _canvas );


        // Panel fuer Zoom-Buttons konstruieren

        _zoom_panel = new JPanel();
        _zoom_panel.add( _button_zoom_in );
        _zoom_panel.add( _button_zoom_out );
        _zoom_panel.add( _button_fit );


        // DisplayPanel konstruieren

        setLayout( new BorderLayout() );

        add( BorderLayout.NORTH, _zoom_panel );
        add( BorderLayout.CENTER, canvas_panel );
        add( BorderLayout.SOUTH, _scroll_hor );
        add( BorderLayout.EAST, _scroll_vert );


        // Das DisplayPanel als Listener bei Scrollbars, Buttons
        // und Canvas registrieren

        _scroll_hor.addAdjustmentListener( this );
        _scroll_vert.addAdjustmentListener( this );

        _button_zoom_in.addActionListener( this );
        _button_zoom_out.addActionListener( this );
        _button_fit.addActionListener( this );

        _canvas.addComponentListener( this );
    } // DisplayPanel



    /**
     * Create a new DisplayPanel
     */

    //============================================================
    public DisplayPanel()
    //============================================================
    {
        this( null );
    } // DisplayPanel



    //************************************************************
    // public methods
    //************************************************************



    /**
     * @return Die Zeichenflaeche
     */

    //============================================================
    public BufferedCanvas getCanvas()
    //============================================================
    {
        return _canvas;
    } // getCanvas



    /**
     * @return Die dargestellte Szene
     */

    //============================================================
    public DisplayPanelScene getScene()
    //============================================================
    {
        return _scene;
    } // getScene



    /**
     * Setzen der Szene, die im DisplayPanel dargestellt werden
     * soll.
     */

    //============================================================
    public void setScene( DisplayPanelScene scene )
    //============================================================
    {
        setScene( scene, true );
    }
    
    
    //============================================================
    public void setScene( DisplayPanelScene scene , boolean first )
    //============================================================
    {
        if( _scene != null )
        {
            if( _scene instanceof WorldCoorScene )
            {
                ((WorldCoorScene)_scene).setDisplayPanel( null );
            } // if
        } // if

        _scene = scene;
        _first_refresh = first;

        if( _scene != null )
        {
            if( _scene instanceof WorldCoorScene )
            {
                ((WorldCoorScene)_scene).setDisplayPanel( this );
            } // if
        } // if
    } // setScene



    /**
     * @return Panel mit Zoom-Buttons
     */

    //============================================================
    public JPanel getZoomPanel()
    //============================================================
    {
        return _zoom_panel;
    } // getZoomPanel



    /**
     * Registrieren eines ActionListeners
     */

    //============================================================
    public void addActionListener( ActionListener l )
    //============================================================
    {
        if( l == null )
            return;

        if( ! _action_listener.contains( l ) )
            _action_listener.addElement( l );
    } // addActionListener



    /**
     * Entfernen eines ActionListeners
     */

    //============================================================
    public void removeActionListener( ActionListener l )
    //============================================================
    {
        if( l == null )
            return;

        _action_listener.removeElement( l );
    } // removeActionListener



    /**
     * Informiert alle registrierten ActionListener ueber Aenderungen
     * in der Szene.
     */

    //============================================================
    public void fireActionEvent()
    //============================================================
    {
        // Aufrufen der Methode actionPerformed aller registrierten
        // ActionListener
        for( int i = 0; i < _action_listener.size(); i++ )
        {
            ((ActionListener) _action_listener.elementAt( i )).
                actionPerformed( new ActionEvent( this, 0, "" ) );
        } // for
    } // fireActionEvent



    /**
     * Das gesamte DisplayPanel in Abhaengigkeit von der dargestellten
     * Szene aktualisieren.
     *
     * Zoom- und Fit-Buttons werden enabled/disabled.
     * Scrollbars werden eingestellt
     *
     * Die Szene wird ggf. verschoben ( scene.fitToBox() ) und
     * neu gezeichnet.
     */

    //============================================================
    public void refresh()
    //============================================================
    {
        refresh( 0, 0, _canvas.getSize().width,
                       _canvas.getSize().height );
    } // refresh



    /**
     * Das gesamte DisplayPanel in Abhaengigkeit von der dargestellten
     * Szene aktualisieren.
     *
     * Zoom- und Fit-Buttons werden enabled/disabled.
     * Scrollbars werden eingestellt
     *
     * Die Szene wird ggf. verschoben ( scene.fitToBox() ) und
     * neu gezeichnet.
     */

    //============================================================
    public synchronized void refresh( int clip_x,
                                      int clip_y,
                                      int clip_width,
                                      int clip_height )
    //============================================================
    {
        if( _scene == null )
        {
            // Es gibt keine Szene, also werden alle Buttons und
            // Scrollbars ausgeschaltet

            _scroll_hor.setEnabled( false );
            _scroll_vert.setEnabled( false );
            _button_zoom_in.setEnabled( false );
            _button_zoom_out.setEnabled( false );
            _button_fit.setEnabled( false );

            _drawScene(); // Zeichenflaeche mit Hintergrundfarbe fuellen

            return; // fertig
        } // if


        // Abmessungen und Position der Szene ermitteln
        // Falls die Szene keine definierten Abmessungen hat,
        // werden alle Werte auf 0.0 gesetzt

        Rectangle2D.Double rect 
		 = (Rectangle2D.Double)_scene.getBoundingBox();
        
        double scene_width = 0.0;
        double scene_height = 0.0;
        double scene_left = 0.0;
        double scene_top = 0.0;
        if( rect != null )
        {
            scene_width = rect.width;
            scene_height = rect.height;
            scene_left = rect.x;
            scene_top = rect.y;
        } // if


        // Maximale Abmessungen der Szene ermitteln
        Rectangle2D.Double max_rect 
		 = (Rectangle2D.Double)_scene.getBoundingBoxMax();


        // Abmessungen und Mittelpunkt des Canvas ermitteln

        Dimension dim = _canvas.getSize();

        double can_width  = dim.width;
        double can_height = dim.height;
        double can_center_x = can_width / 2.0;
        double can_center_y = can_height / 2.0;



        // Scrollbars ein- bzw. ausschalten
        // ( Scrollbars sind nur eingeschaltet, wenn die Szene
        //   definierte Abmessungen hat )

        _scroll_hor.setEnabled( rect != null );
        _scroll_vert.setEnabled( rect != null );


        // Buttons ein- bzw. ausschalten
        // ( Buttons sind nur eingeschaltet, wenn die Szene
        //   mindestens eine Abmessung > 0.0 hat.
        //   "Zoom out" ist sogar nur eingeschaltet, wenn die Szene
        //   mindestens eine Abmessung >= 1.0 hat )

        _button_zoom_in.setEnabled( scene_width  > 0 || scene_height > 0 );
        _button_fit.setEnabled(     scene_width  > 0 || scene_height > 0 );

        _button_zoom_out.
            setEnabled(   (    scene_width  >= 1.0
                            || scene_height >= 1.0 )
                       && (    max_rect == null
                            || (    max_rect.width  > can_width
                                 && max_rect.height > can_height ) ) );


        if( ! _first_refresh )
        {
            // scene_left und scene_top so veraendern, dass der Punkt
            // der Szene, der sich beim letzten refresh in der Mitte
            // der Zeichenflaeche befand, weiterhin in der Mitte ist

            double diff_x = can_center_x - _can_center_x;
            double diff_y = can_center_y - _can_center_y;

            scene_left += diff_x;
            scene_top  += diff_y;

            if( max_rect != null )
            {
                max_rect.x += diff_x;
                max_rect.y += diff_y;
            } // if

        } // if
        else
            _first_refresh = false;


        // Mittelpunkt-Koordinaten fuer naechsten refresh speichern
        _can_center_x = can_center_x;
        _can_center_y = can_center_y;


        // Berechnen der Scrollbar-Einstellungen
        // und ggf. scene_left, scene_top etc. so veraendern, dass die
        // Szene nicht vollstaendig ausserhalb der Zeichenflaeche ist
        // und die Zeichenflaeche vollstaendig innerhalb der maximalen
        // Szenen-Abmessungen liegt

        if( max_rect != null )
        {
            double factor = Math.max( can_width / max_rect.width,
                                      can_height / max_rect.height );

            if( factor > 1 )
            {
                scene_left = can_center_x -
                             ( can_center_x - scene_left ) * factor;
                scene_top  = can_center_y -
                             ( can_center_y - scene_top ) * factor;
                scene_width  *= factor;
                scene_height *= factor;

                max_rect.x = can_center_x -
                             ( can_center_x - max_rect.x ) * factor;
                max_rect.y = can_center_y -
                             ( can_center_y - max_rect.y ) * factor;
                max_rect.width  *= factor;
                max_rect.height *= factor;
            } // if
        } // if


        double minimum_h = 0 - can_width + _MIN_INSIDE;
        double maximum_h = scene_width + can_width - _MIN_INSIDE;

        double minimum_v = 0 - can_height + _MIN_INSIDE;
        double maximum_v = scene_height + can_height - _MIN_INSIDE;


        // Maximale Abmessungen der Szene ermitteln und Minimal- und
        // Maximal-Werte fuer Scrollbars ggf. korrigieren

        if( max_rect != null )
        {
            double scene_left_min  = max_rect.x;
            double scene_right_max = scene_left_min + max_rect.width;
            double scene_right     = scene_left + scene_width;

            minimum_h = Math.max( minimum_h,
                                  scene_left_min - scene_left );
            maximum_h = Math.min( maximum_h,
                                    scene_width
                                  + ( scene_right_max - scene_right ) );


            double scene_top_min    = max_rect.y;
            double scene_bottom_max = scene_top_min + max_rect.height;
            double scene_bottom     = scene_top + scene_height;

            minimum_v = Math.max( minimum_v,
                                  scene_top_min - scene_top );
            maximum_v = Math.min( maximum_v,
                                    scene_height
                                  + ( scene_bottom_max - scene_bottom ) );

        } // if


        if( - scene_left < minimum_h )
            scene_left = - minimum_h;
        if( - scene_left > maximum_h - can_width )
            scene_left = - maximum_h + can_width;


        if( - scene_top < minimum_v )
            scene_top = - minimum_v;
        if( - scene_top > maximum_v - can_height )
            scene_top = - maximum_v + can_height;

        int value_h = - (int)scene_left;
        int value_v = - (int)scene_top;
        int visible_h = (int)can_width;
        int visible_v = (int)can_height;


        // Setzen der ggf. neuen Scrollbar-Einstellungen.

        // Um zu verhindern, dass durch das Setzen der Einstellungen
        // die Methode adjustmentValueChanged() aufgerufen wird,
        // wird das DisplayPanel vorher als Listener bei den
        // Scrollbars abgemeldet und anschliessend wieder registriert.
        // ( adjustmentValueChanged() wuerde nur unnoetiges _drawScene()
        //   ausloesen )

        _scroll_hor.removeAdjustmentListener( this );
        _scroll_vert.removeAdjustmentListener( this );

        _scroll_hor.setValues( value_h, visible_h,
                               (int)minimum_h, (int)maximum_h );
        _scroll_vert.setValues( value_v, visible_v,
                                (int)minimum_v, (int)maximum_v );


        _scroll_hor.setUnitIncrement( (int)( can_width / 100 ) + 1 );
        _scroll_hor.setBlockIncrement( (int)( can_width / 10 ) + 1 );
        _scroll_vert.setUnitIncrement( (int)( can_height / 100 ) + 1 );
        _scroll_vert.setBlockIncrement( (int)( can_height / 10 ) + 1 );


        _scroll_hor.addAdjustmentListener( this );
        _scroll_vert.addAdjustmentListener( this );


        if(    rect != null
            && scene_left   == rect.x
            && scene_top    == rect.y
            && scene_width  == rect.width
            && scene_height == rect.height )
        {
            // Keine Veraenderungen bei den Szenen-Abmessungen
            // also nur den Clipping-Bereich neu zeichnen
            _drawScene( clip_x, clip_y, clip_width, clip_height );
        }
        else
        {
            // Es gab Veraenderungen bei scene_left oder scene_top
            // Also der Szene mitteilen, wie ihre neue Position
            // in Bild-Koordinaten ist und komplett neu zeichnen
            _scene.fitToBox( scene_left, scene_top,
                             scene_width, scene_height );
            _drawScene();
        } // else
    } // refresh




    //************************************************************
    // public methods for implemented listeners
    //************************************************************


    /**
     * @see AdjustmentListener
     */

    //============================================================
    public void adjustmentValueChanged( AdjustmentEvent e )
    //============================================================
    {
        // Einer der beiden Scrollbars wurde betaetigt

        if( _scene == null )
            return; // Ohne Szene ist nichts zu tun


        // Ermitteln der Position und Abmessungen der Szene

        Rectangle2D.Double rect 
		 = (Rectangle2D.Double)_scene.getBoundingBox();

        if( rect == null )
            return; // Ohne Abmessungen ist nichts zu tun


        // Der Szene mitteilen, wie die aus den Scrollbar-Einstellungen
        // resultierende Position ist

        if( e.getSource() == _scroll_hor )
            _scene.fitToBox( -_scroll_hor.getValue(),
                             rect.y,
                             rect.width,
                             rect.height );

        if( e.getSource() == _scroll_vert )
            _scene.fitToBox( rect.x,
                             -_scroll_vert.getValue(),
                             rect.width,
                             rect.height );


        // Szene neu zeichnen
        _drawScene();

    } // adjustmentValueChanged



    /**
     * @see ActionListener
     */

    //============================================================
    public void actionPerformed( ActionEvent e )
    //============================================================
    {
        // Einer der Buttons wurde betaetigt

        if( _scene == null )
            return; // Ohne Szene ist nichts zu tun


        // Ermitteln der Position und Abmessungen der Szene

        Rectangle2D.Double rect = (Rectangle2D.Double)_scene.getBoundingBox();

        if( rect == null )
            return; // Ohne Abmessungen ist nichts zu tun


        Object source = e.getSource();

        if( source == _button_zoom_in )
        {
            //System.out.println( "Zoom in" );

            _scene.fitToBox(
                _can_center_x + ( rect.x - _can_center_x ) * _zoom_factor,
                _can_center_y + ( rect.y - _can_center_y ) * _zoom_factor,
                rect.width * _zoom_factor,
                rect.height * _zoom_factor );
        } // if

        if( source == _button_zoom_out )
        {
            //System.out.println( "Zoom out" );

            _scene.fitToBox(
                _can_center_x + ( rect.x - _can_center_x ) / _zoom_factor,
                _can_center_y + ( rect.y - _can_center_y ) / _zoom_factor,
                rect.width / _zoom_factor,
                rect.height / _zoom_factor );
        } // if

        if( source == _button_fit )
        {
            //System.out.println( "Fit" );

            Dimension dim = _canvas.getSize();
            _scene.fitToBox( _FIT_BORDER,
                             _FIT_BORDER,
                             dim.width  - _FIT_BORDER * 2,
                             dim.height - _FIT_BORDER * 2 );

        } // if


        // DisplayPanel aktualisieren
        refresh();

    } // actionPerformed



    /**
     * @see ComponentListener
     */

    //============================================================
    public synchronized void componentResized( ComponentEvent e )
    //============================================================
    {
        // Canvasabmessungen wurden veraendert

        // DisplayPanel aktualisieren
        refresh();
    } // componentResized



    //============================================================
    public void componentHidden( ComponentEvent e ) {}
    public void componentMoved( ComponentEvent e ) {}
    public void componentShown( ComponentEvent e ) {}
    //============================================================




    //************************************************************
    // Private methods
    //************************************************************



    /**
     * Zeichnen der Szene.
     * Falls keine Szene mit setScene() gesetzt wurde, wird die
     * Zeichenflaeche mit der Hintergrundfarbe gefuellt.
     */

    //============================================================
    private void _drawScene()
    //============================================================
    {
        _drawScene( 0, 0, _canvas.getSize().width,
                          _canvas.getSize().height );
    } // _drawScene



    /**
     * Zeichnen der Szene mit Clipping.
     * Falls keine Szene mit setScene() gesetzt wurde, wird die
     * Zeichenflaeche mit der Hintergrundfarbe gefuellt.
     */

    //============================================================
    private synchronized void _drawScene( int clip_x,
                                          int clip_y,
                                          int clip_width,
                                          int clip_height )
    //============================================================
    {
        Graphics img_gr = _canvas.getImageGraphics();

        if( img_gr == null )
            return; // Ohne Image wird nichts gezeichnet

        int clip_left   = Math.max( 0, clip_x );
        int clip_top    = Math.max( 0, clip_y );
        int clip_right  = Math.min( _canvas.getSize().width,
                                    clip_x + clip_width );
        int clip_bottom = Math.min( _canvas.getSize().height,
                                    clip_y + clip_height );

        clip_x = clip_left;
        clip_y = clip_top;
        clip_width = clip_right - clip_left;
        clip_height = clip_bottom - clip_top; // <---- This might be screwed...

        img_gr.setClip( clip_x, clip_y, clip_width, clip_height );

        if( _scene != null )
        {
            // Szene ins Offscreen-Image zeichnen
            _scene.paint( img_gr );
        }
        else
        {
            // Offscreen-Image mit Hintergrundfarbe fuellen
            img_gr.setColor( _canvas.getBackground() );
            img_gr.fillRect( clip_x, clip_y, clip_width, clip_height );
        } // else

        img_gr.dispose();


        // Offscreen-Image auf Canvas kopieren
        Graphics gr = _canvas.getGraphics();

        if( gr != null )
        {
            gr.setClip( clip_x, clip_y, clip_width, clip_height );
            _canvas.paint( gr );

            gr.dispose();
        } // if
    } // _drawScene

} // DisplayPanel
