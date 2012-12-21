package anja.swinggui;

import anja.geom.Point2;
import anja.geom.Rectangle2;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;

/**
 * WorldCoorScene implementiert das Interface DisplayPanelScene und
 * ist die Basisklasse fuer Szenerien, deren Koordinaten nicht in
 * Bildschirmkoordinaten, sondern in davon unabhaengigen Weltkoordinaten
 * vorliegen.
 *
 * @version 0.1 21.08.01
 * @author      Ulrich Handel
 */

//****************************************************************
public abstract class WorldCoorScene
    implements
    DisplayPanelScene,
    MouseListener,
    MouseMotionListener,
    ActionListener,
    PopupMenuListener
//****************************************************************
{
    //************************************************************
    // private variables
    //************************************************************

    private DisplayPanel _display_panel; // DisplayPanel auf dessen
                       // Zeichenflaeche die Szene dargestellt wird


    private WorldCoorTransformer _transformer;
            // Transformer zur Umrechnung von Welt- in Bildkoordinaten
            // und umgekehrt



    // Popup-Menue

    private JPopupMenu _popup_menu;

    private Point2 _popup_point; // Punkt in Weltkoordinaten, auf den
                                 // sich das Menue bezieht.

    private MouseListener[] _mouse_listeners; // Array zum Zwischen-
        // speichern aller MouseListener des Canvas. Diese werden
        // vor dem Oeffnen des Menues vom Canvas abgemeldet und wieder
        // beim Canvas registriert, wenn das Menue geschlossen wird.
        //
        // Der Sinn ist, dass ein evtl. Mausklick in die Zeichenflaeche
        // zum Schliessen des Menues ohne Auswahl, nicht von
        // registrierten MouseListenern ausgewertet werden soll.


    private Point2 _xor_point1 = null; // Koordinaten fuer XOR-Linie
    private Point2 _xor_point2 = null;


    //************************************************************
    // constructors
    //************************************************************



    //************************************************************
    // public methods
    //************************************************************


    /**
     * Setzen des Koordinaten-Transformers
     */

    //============================================================
    public void setTransformer( WorldCoorTransformer transformer )
    //============================================================
    {
        _transformer = transformer;
    } // setTransformer


    /**
     * @return Koordinaten-Transformer
     */

    //============================================================
    public WorldCoorTransformer getTransformer()
    //============================================================
    {
        return _transformer;
    } // getTransformer


    /**
     * Setzen der Transformationsmatrix fuer Koordinatenumwandlung
     * Welt --> Bild.
     */

    //============================================================
    public void setWorldToScreen( AffineTransform world_to_screen )
    //============================================================
    {
        _transformer = new WorldCoorTransformer( world_to_screen );
    } // setWorldToScreen


    /**
     * @return Die Transformationsmatrix fuer Koordinatenumwandlung
     *         Welt --> Bild
     */

    //============================================================
    public AffineTransform getWorldToScreen()
    //============================================================
    {
        return _transformer.getWorldToScreen();
    } // getWorldToScreen


    /**
     * @return Die Transformationsmatrix fuer Koordinatenumwandlung
     *         Bild --> Welt
     */

    //============================================================
    public AffineTransform getScreenToWorld()
    //============================================================
    {
        return _transformer.getScreenToWorld();
    } // getScreenToWorld




    //************************************************************
    // public methods for interface DisplayPanelScene
    //************************************************************



    /**
     * Ermitteln der die Szene umschliessenden Box im
     * Screenkoordinaten-System
     */

    //============================================================
    public Rectangle2D getBoundingBox()
    //============================================================
    {
        return _getScreenRectangle( getBoundingRectWorld() );
    } // getBoundingBox


    /**
     * Ermitteln der maximalen Bounding-Box im
     * Screenkoordinaten-System
     */

    //============================================================
    public Rectangle2D getBoundingBoxMax()
    //============================================================
    {
        return _getScreenRectangle( getBoundingRectMaxWorld() );
    } // getBoundingBoxMax


    /**
     * Setzen der Transformationsmatrix zur Umwandlung von Welt- in
     * Bildkoordinaten.
     *
     * Die Transformationsmatrix wird derart gesetzt, dass bei einer
     * Umwandlung des die Szene umgebenden Rechtecks in Bildkoordinaten
     * dieses genau in das gegebene Bildkoordinaten-Rechteck passt.
     */

    //============================================================
    public void fitToBox( double x, double y, double width, double height )
    //============================================================
    {
        Rectangle2 rect_world = getBoundingRectWorld();

        if( rect_world == null )
        {
            // Die Szene hat keine definierten Abmessungen

            _initTransformer();

            return;
        } // if


        // Skalierungsfaktors fuer die Transformationsmatrix berechnen

        double scale;

        if( rect_world.width == 0 && rect_world.height == 0 )
        {
            // Die Szene ist ein Punkt, der natuerlich nicht skaliert
            // werden kann.
            scale = 1.0;
        }
        else
        {
            // horizontaler Skalierungsfaktor
            double scale_x = Double.MAX_VALUE;
            if( rect_world.width != 0 )
                scale_x = width / (double)rect_world.width;

            // vertikaler Skalierungsfaktor
            double scale_y = Double.MAX_VALUE;
            if( rect_world.height != 0 )
                scale_y = height / (double)rect_world.height;

            // Der horizontal und vertikal zu benutzende
            // Skalierungsfaktor ist das Minimum von beiden berechneten
            // Werten, da sich das Verhaeltnis von von x- und y-Abmessungen
            // nicht veraendern soll.
            scale = Math.min( scale_x, scale_y );
        } // else

        
        //TODO: check this stuff once more. Seems correct though

        // Falls der berechnete Skalierungsfaktor groesser als der
        // maximal erlaubte ist, wird er auf diesen begrenzt
        scale = Math.min( scale, getMaxScale() );


        // Abmessungen und Position des Weltkoordinaten-Rechtecks
        // in Screen-Koordinaten umrechnen
        double rect_x      = rect_world.x * scale;
        double rect_y      = ( rect_world.y + rect_world.height) * -scale;
        // Bild-y-Koordinaten laufen von oben nach unten

        double rect_width  = rect_world.width * scale;
        double rect_height = rect_world.height * scale;


        // Horizontale und vertikale Verschiebung fuer die
        // Transformationsmatrix berechnen.
        double dist_x = x - rect_x - ( rect_width - width ) / 2;
        double dist_y = y - rect_y - ( rect_height - height ) / 2;


        // Transformationsmatrix setzen
        _transformer = new WorldCoorTransformer( dist_x, dist_y, scale );
    } // fitToBox



    /**
     * Zeichnen der Szene
     */

    //============================================================
    public void paint( Graphics g )
    //============================================================
    {
        Graphics2D g2d = (Graphics2D)g;
               
        //AffineTransform tx = g2d.getTransform();
              
        g2d.setTransform( _transformer.getWorldToScreen() );
        
        //AffineTransform inv = new AffineTransform(1,0,0,-1,0,0);
        //g2d.getTransform().concatenate(inv);
        
        paint( g2d, g );
        
        //g2d.setTransform(tx);
    } // paint





    //************************************************************
    // abstract protected methods
    //************************************************************


    /**
     * Ermitteln des die Szene umschliessenden Rechtecks im
     * Weltkoordinaten-System
     */

    //============================================================
    abstract protected Rectangle2 getBoundingRectWorld();
    //============================================================


    /**
     * Zeichnen der Szene
     *
     * @param g2d Graphics2D-Objekt zum Zeichnen in Weltkoordinaten.
     * @param g   Graphics-Objekt zum Zeichnen in Bildkoordinaten
     */

    //============================================================
    abstract protected void paint( Graphics2D g2d, Graphics g );
    //============================================================




    //************************************************************
    // protected methods
    //************************************************************


    /**
     * Ermitteln der maximalen Bounding-Box im Weltkoordinaten-System
     * bzw. null, falls nicht definiert
     *
     * Falls definiert, muss jedes durch getBoundingRectWorld()
     * ermittelte Rechteck vollstaendig innerhalb der maximalen
     * Bounding-Box liegen.
     */

    //============================================================
    protected Rectangle2 getBoundingRectMaxWorld()
    //============================================================
    {
        return null;
    } // getBoundingRectMaxWorld


    /**
     * Liefert den anfaenglich zu benutzenden Skalierungsfaktor fuer
     * die Umrechnung von Weltkoordinaten in Bildkoordinaten.
     *
     * Diese Methode kann von einer Subklasse ueberschrieben werden,
     * wenn das Anfangsverhaeltnis von Weltkoordinatenabmessungen zu
     * Bildkoordinatenabmessungen ungleich 1.0 sein soll.
     *
     * Ein zurueckgegebener Wert von 0.5 bedeutet z.B. dass eine Welt-
     * koordinaten-Einheit im Bildkoordinatensystem die Ausdehnung von
     * 0.5 Pixeln hat.
     */

    //============================================================
    protected double getInitScale()
    //============================================================
    {
        return 1.0;
    } // getInitScale


    /**
     * Liefert den groessten erlaubten Wert fuer den Skalierungsfaktor
     * bei der Umrechnung von Weltkoordinaten in Bildkoordinaten.
     *
     * Diese Methode kann von einer Subklasse ueberschrieben werden,
     * um zu verhindert, dass beliebig tief in die Szene gezoomt werden
     * kann ( siehe Methode fitToBox(..) )
     *
     * Ein zurueckgegebener Wert von 2.0 bedeutet z.B. dass eine Welt-
     * koordinaten-Einheit im Bildkoordinatensystem hoechstens die
     * Ausdehnung von 2 Pixeln haben darf.
     */

    //============================================================
    protected double getMaxScale()
    //============================================================
    {
        return Double.MAX_VALUE;
    } // getMaxScale




    //************************************************************
    // Methoden zur Koordinaten-Umwandlungen
    // ( Bildkoordinaten <-> Weltkoordinaten )
    //************************************************************


    /**
     * Umwandeln der gegebenen Bildkoordinaten-Distanz in
     * Weltkoordinaten.
     *
     * @return die Weltkoordinaten-Distanz
     */

    //============================================================
    public double transformScreenToWorld( double distance )
    //============================================================
    {
    	
    	// TODO: check transforms in methods below
        return _transformer.screenToWorld( distance );
    } // transformScreenToWorld



    /**
     * Umwandeln des gegebenen Bildkoordinaten-Punktes in
     * Weltkoordinaten.
     *
     * @return der Weltkoordinaten-Punkt
     */

    //============================================================
    public Point2 transformScreenToWorld( double x, double y )
    //============================================================
    {
        return _transformer.screenToWorld( x, y );
    } // transformScreenToWorld



    /**
     * Umwandeln des gegebenen Bildkoordinaten-Rechtecks in
     * Weltkoordinaten.
     *
     * @return das Weltkoordinaten-Rechteck
     */

    //============================================================
    public Rectangle2 transformScreenToWorld( double x,
                                              double y,
                                              double width,
                                              double height )
    //============================================================
    {
        return _transformer.screenToWorld( x, y, width, height );
    } // transformScreenToWorld



    /**
     * Umwandeln der gegebenen Weltkoordinaten-Distanz in
     * Bildkoordinaten.
     *
     * @return die Bildkoordinaten-Distanz
     */

    //============================================================
    public double transformWorldToScreen( double distance )
    //============================================================
    {
        return _transformer.worldToScreen( distance );
    } // transformWorldToScreen



    /**
     * Umwandeln des gegebenen Weltkoordinaten-Punktes in
     * Bildkoordinaten.
     *
     * @return der Bildkoordinaten-Punkt
     */

    //============================================================
    public Point2 transformWorldToScreen( double x, double y )
    //============================================================
    {
        return _transformer.worldToScreen( x, y );
    } // transformWorldToScreen



    /**
     * Umwandeln des gegebenen Weltkoordinaten-Rechtecks in
     * Bildkoordinaten.
     *
     * @return das Bildkoordinaten-Rechteck
     */

    //============================================================
    public Rectangle2 transformWorldToScreen( double x,
                                              double y,
                                              double width,
                                              double height )
    //============================================================
    {
        return _transformer.worldToScreen( x, y, width, height );
    } // transformWorldToScreen




    //************************************************************
    // Methoden zum Zugriff auf das DisplayPanel in dem die Szene
    // dargestellt wird
    //************************************************************


    /**
     * Setzen des DisplayPanels.
     *
     * Mit dieser Methode registriert sich ein DisplayPanel bei dieser
     * Szene ( @see DisplayPanel.setScene() )
     */

    //============================================================
    public void setDisplayPanel( DisplayPanel panel )
    //============================================================
    {
        if( panel == null && _display_panel != null )
        {
            // Die Szene wird vom DisplayPanel entfernt
            removingFromDisplayPanel();
        } // if

        _display_panel = panel;

        if( _display_panel == null )
            return;

        // Falls es noch keine Transformationsmatrix gibt, muss
        // diese jetzt erzeugt werden.
        //
        if( _transformer == null )
            _initTransformer();

    } // setDisplayPanel



    /**
     * @return Das DisplayPanel, in dem die Szene dargestellt wird.
     */

    //============================================================
    protected DisplayPanel getDisplayPanel()
    //============================================================
    {
        return _display_panel;
    } // getDisplayPanel



    /**
     * Diese Methode muss ggf. von Unterklassen so ueberschrieben werden,
     * dass alle notwendigen Abschluss-Aktionen durchgefuehrt werden,
     * bevor die Szene vom DisplayPanel entfernt werden kann.
     */

    //============================================================
    protected void removingFromDisplayPanel()
    //============================================================
    {
    } // removingFromDisplayPanel



    /**
     * Zeichen einer XOR-Linie zwischen den beiden
     * Weltkoordinatenpunkten point1 und point2.
     */

    //============================================================
    protected void drawXORLine( Point2 point1, Point2 point2 )
    //============================================================
    {
        eraseLastXORLine(); // Loeschen einer evtl. noch vorhandenen
                            // XOR-Linie

        // Ermitteln der Bildkoordinaten
        
       
        
        _xor_point1 = _transformer.worldToScreen( point1.x, point1.y );
        _xor_point2 = _transformer.worldToScreen( point2.x, point2.y );

        _drawXORLine();
    } // drawXORLine



    /**
     * Loescht die letzte XOR-Linie, falls noch eine gezeichnet ist
     */

    //============================================================
    protected void eraseLastXORLine()
    //============================================================
    {
        if( _xor_point1 == null || _xor_point2 == null )
            return;

        _drawXORLine();

        _xor_point1 = null;
        _xor_point2 = null;
    } // eraseLastXORLine



    /**
     * Liefern eines Verweises auf die Zeichenflaeche des DisplayPanels
     */

    //============================================================
    protected BufferedCanvas getCanvas()
    //============================================================
    {
        if( _display_panel == null )
            return null;

        return _display_panel.getCanvas();
    } // getCanvas



    /**
     * Aktualisieren aller Bedienelemente des DisplayPanels
     * ( Scrollbars etc. ) und Neuzeichnen der Szene.
     */

    //============================================================
    protected void updateDisplayPanel()
    //============================================================
    {
        if( _display_panel == null )
            return;


        eraseLastXORLine();

        _display_panel.refresh();
    } // updateDisplayPanel



    /**
     * Aktualisieren aller Bedienelemente des DisplayPanels
     * ( Scrollbars etc. ) und Neuzeichnen der Szene.
     *
     * Es wird nur der Teil der Szene neu gezeichnet, der sich innerhalb
     * des angegebenen Weltkoordinaten-Rechtecks befindet.
     */

    //============================================================
    protected void updateDisplayPanel( double x,
                                       double y,
                                       double width,
                                       double height )
    //============================================================
    {
        if( _display_panel == null )
            return;

        
        Rectangle2 clip = _transformer.worldToScreen( x, y, width, height );


        eraseLastXORLine();

        _display_panel.refresh( ( int )clip.x - 1,
                                ( int )clip.y - 1,
                                ( int )clip.width  + 2,
                                ( int )clip.height + 2 );
    } // updateDisplayPanel



    /**
     * Aktualisieren aller Bedienelemente des DisplayPanels
     * ( Scrollbars etc. ) und Neuzeichnen der Szene.
     *
     * Es wird nur der Teil der Szene neu gezeichnet, der sich innerhalb
     * des um extend erweiterten Weltkoordinaten-Rechtecks
     * ( x, y, width, height ) befindet.
     * Dabei ist extend in Pixeln gegeben.
     */

    //============================================================
    protected void updateDisplayPanel( double x,
                                       double y,
                                       double width,
                                       double height,
                                       int    extend )
    //============================================================
    {
    	
        double border = _transformer.screenToWorld( extend );

        updateDisplayPanel( x - border,
                            y - border,
                            width  + border * 2,
                            height + border * 2 );
    } // updateDisplayPanel



    /**
     * Informiert den beim DisplayPanel registrierten ActionListener
     * ueber Aenderungen in der Szene.
     */

    //============================================================
    protected void fireActionEvent()
    //============================================================
    {
        if( _display_panel != null )
            _display_panel.fireActionEvent();
    } // fireActionEvent




    //************************************************************
    // Methoden zur Verwaltung eines Popup-Menues
    //************************************************************


    /**
     * Erzeugen und Oeffnen eines Popup-Menues.
     *
     * Zum Aufbau des Menues ( Hinzufuegen von Menu-Items ) muss die
     * Methode buildPopupMenu() von der Subklasse entsprechend
     * ueberschrieben sein.
     *
     * Das Popup-Menue wird an der Position ( x, y ) auf der
     * Zeichenflaeche des DisplayPanels geoeffnet.
     */

    //============================================================
    protected void processPopupMenu( int x, int y )
    //============================================================
    {
        processPopupMenu( x, y, null, null );
    } // processPopupMenu



    /**
     * Erzeugen und Oeffnen eines Popup-Menues.
     *
     * Zum Aufbau des Menues ( Hinzufuegen von Menu-Items ) muss die
     * Methode buildPopupMenu() von der Subklasse entsprechend
     * ueberschrieben sein.
     *
     * Das Popup-Menue wird an der Position ( x, y ) auf der
     * Zeichenflaeche des DisplayPanels geoeffnet.
     *
     * @param point Im Falle  point != null  ist point der Punkt in
     *              Weltkoordinaten, auf den sich das Menue bezieht.
     *              Dieser Punkt wird auf der Zeichenflaeche markiert,
     *              solange das Menue geoeffnet ist.
     *              ( siehe popupMenuWillBecomeVisible )
     */

    //============================================================
    protected void processPopupMenu( int    x,
                                     int    y,
                                     Point2 point )
    //============================================================
    {
        processPopupMenu( x, y, point, null );
    } // processPopupMenu



    /**
     * Erzeugen und Oeffnen eines Popup-Menues.
     *
     * Zum Aufbau des Menues ( Hinzufuegen von Menu-Items ) muss die
     * Methode buildPopupMenu() von der Subklasse entsprechend
     * ueberschrieben sein.
     *
     * Das Popup-Menue wird an der Position ( x, y ) auf der
     * Zeichenflaeche des DisplayPanels geoeffnet.
     *
     * @param title Im Falle  title != null  beginnt das Menue mit einem
     *              JLabel zur Anzeige des Titels gefolgt von einem
     *              Separator.
     */

    //============================================================
    protected void processPopupMenu( int    x,
                                     int    y,
                                     String title )
    //============================================================
    {
        processPopupMenu( x, y, null, title );
    } // processPopupMenu



    /**
     * Erzeugen und Oeffnen eines Popup-Menues.
     *
     * Zum Aufbau des Menues ( Hinzufuegen von Menu-Items ) muss die
     * Methode buildPopupMenu() von der Subklasse entsprechend
     * ueberschrieben sein.
     *
     * Das Popup-Menue wird an der Position ( x, y ) auf der
     * Zeichenflaeche des DisplayPanels geoeffnet.
     *
     * @param point Im Falle  point != null  ist point der Punkt in
     *              Weltkoordinaten, auf den sich das Menue bezieht.
     *              Dieser Punkt wird auf der Zeichenflaeche markiert,
     *              solange das Menue geoeffnet ist.
     *              ( siehe popupMenuWillBecomeVisible )
     *
     * @param title Im Falle  title != null  beginnt das Menue mit einem
     *              JLabel zur Anzeige des Titels gefolgt von einem
     *              Separator.
     */

    //============================================================
    protected void processPopupMenu( int    x,
                                     int    y,
                                     Point2 point,
                                     String title )
    //============================================================
    {
        JLabel     top_label     = null;  // wird fuer title gebraucht,
        JSeparator top_separator = null;  // falls angegeben


        _popup_point = point; // Speichern fuer
                // popupMenuWillBecomeInvisible


        // Neues Popup-Menu erzeugen
        _popup_menu = new JPopupMenu();


        // Titelzeile ( JLabel ) + Separator zufuegen, falls
        // ein Titel angegeben wurde
        //
        if( title != null )
        {
            top_label     = new JLabel( title );
            top_separator = new JSeparator();
            _popup_menu.add( top_label );
            _popup_menu.add( top_separator );
        } // if



        // PopupMenu aufbauen. buildPopupMenu muss von der Subklasse
        // entsprechend ueberschrieben werden.
        buildPopupMenu( _popup_menu );



        // Testen, ob dem PopupMenu durch buildPopupMenu() ueberhaupt
        // etwas ( MenuItems oder evtl. auch andere Komponenten )
        // zugefuegt wurde.
        // Falls das PopupMenu leer ist, wird es nicht geoeffnet.
        // Dabei gilt das PopupMenu auch als leer, wenn es nur die
        // Komponenten top_label und top_separator enthaelt
        // ( siehe oben )
        //
        boolean empty = true;
        int count = _popup_menu.getComponentCount();
        for( int i = 0; i < count; i++ )
        {
            Component component = _popup_menu.getComponent( i );
            if(    component != top_label
                && component != top_separator )
            {
                empty = false;
                break;
            } // if
        } // for
        if( empty )
            return;



        // Alle beim Canvas registrierten MouseListener vorruebergehend
        // abmelden.
        // ( siehe auch Kommentar zur Variablen _mouse_listeners )

        BufferedCanvas canvas = getCanvas();

        _mouse_listeners = ( MouseListener[] )
                           ( canvas.getListeners( MouseListener.class ) );

        for( int i = 0; i < _mouse_listeners.length; i++ )
            canvas.removeMouseListener( _mouse_listeners[ i ] );


        // Beim Menue als PopupMenuListener registrieren und
        // Menue oeffnen.
        _popup_menu.addPopupMenuListener( this );
        _popup_menu.show( canvas, x, y );

    } // processPopupMenu



    /**
     * Aufbauen des Popup-Menues durch Hinzufuegen von Menue-Items.
     *
     * Diese Methode muss von der Subklasse, die ein PopupMenu
     * oeffnen will, entsprechend ueberschrieben werden.
     */

    //============================================================
    protected void buildPopupMenu( JPopupMenu popup_menu ) {}
    //============================================================



    /**
     * PopupMenuListener-Methode.
     *
     * Wird aufgerufen, wenn das Popup-Menue geoeffnet wird.
     */

    //============================================================
    public void popupMenuWillBecomeVisible( PopupMenuEvent e )
    //============================================================
    {
        if( e.getSource() != _popup_menu )
            return;

        if( _popup_point != null )
        {

            BufferedCanvas canvas = getCanvas();

            // Markieren des Weltkoordinaten-Punktes, auf den sich das
            // Menue bezieht durch kleine "Zielscheibe".

            // TODO: Check transform code
            
            Point2 point = _transformer.worldToScreen( _popup_point.x,
                                                       _popup_point.y );

            int mark_x = Math.round( point.x );
            int mark_y = Math.round( point.y );


            // Zielscheibe ins Offscreen-Image zeichen

            Graphics img_gr = canvas.getImageGraphics();
            img_gr.setColor( Color.red );
            img_gr.drawLine( mark_x - 12, mark_y, mark_x + 12, mark_y );
            img_gr.drawLine( mark_x, mark_y - 12, mark_x, mark_y + 12 );
            img_gr.drawOval( mark_x - 8, mark_y - 8, 16, 16 );
            img_gr.drawOval( mark_x - 12, mark_y - 12, 24, 24 );
            img_gr.dispose();


            // Offscreen-Image nach Canvas kopieren

            Graphics gr = canvas.getGraphics();
            canvas.paint( gr );
            gr.dispose();

        } // if

    } // popupMenuWillBecomeVisible



    /**
     * PopupMenuListener-Methode.
     *
     * Wird aufgerufen, wenn das Popup-Menue geschlossen wird.
     */

    //============================================================
    public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
    //============================================================
    {
        if( e.getSource() != _popup_menu )
            return;

        BufferedCanvas canvas = getCanvas();


        // Alle MouseListener, die vor dem Oeffnen des Menues vom
        // Canvas entfernt wurden, wieder registrieren.
        for( int i = 0; i < _mouse_listeners.length; i++ )
            canvas.addMouseListener( _mouse_listeners[ i ] );
        _mouse_listeners = null;


        // Falls vor dem Oeffnen des Menues ein Punkt auf der
        // Zeichenflaeche markiert wurde, muss die Markierung wieder
        // entfernt werden.
        // Dies geschieht durch einen Aufruf von updateDisplayPanel()
        // den das Offscreen-Image des gebufferten Canvas wieder in
        if( _popup_point != null )
            updateDisplayPanel();


        // Beim Menue als Listener abmelden
        _popup_menu.removePopupMenuListener( this );

    } // popupMenuWillBecomeInvisible




    //************************************************************
    // Diverse Hilfs-Methoden, die von der Methode paint(..) zum
    // Zeichnen der Szene benutzt werden koennen.
    //************************************************************



    /**
     * Zeichnen eines gefuellten Kreises dessen Mittelpunkt in
     * Weltkoordinaten und dessen Radius in Pixeln gegeben ist.
     *
     * Diese Methode markiert also einen Weltkoordinatenpunkt mit
     * einem Kreis, dessen Groesse Zoom-unabhaengig ist.
     *
     * @param g       Graphics-Objekt zum Zeichnen in Pixelkoordinaten
     *                ( wird der Methode paint uebergeben ).
     * @param point   Mittelpunkt des Kreises in Weltkoordinaten.
     * @param radius  Radius des Kreises in Pixeln.
     * @param color   Fuellfarbe.
     */

    //============================================================
    protected void drawFilledCircle( Graphics g,
                                     Point2   point,
                                     int      radius,
                                     Color    color )
    //============================================================
    {
        _transformer.drawFilledCircle( g, point, radius, color );
    } // drawFilledCircle





    //************************************************************
    // Leer implementierte Listener-Methoden, die bei Bedarf
    // von Subklassen ueberschrieben werden muessen.
    //************************************************************


    // ActionListener
    //
    //============================================================
    public void actionPerformed( ActionEvent e ) {}
    //============================================================

    // MouseListener
    //
    //============================================================
    public void mouseClicked ( MouseEvent e ) {}
    public void mouseEntered ( MouseEvent e ) {}
    public void mouseExited  ( MouseEvent e ) {}
    public void mousePressed ( MouseEvent e ) {}
    public void mouseReleased( MouseEvent e ) {}
    //============================================================

    // MouseMotionListener
    //
    //============================================================
    public void mouseDragged( MouseEvent e ) {}
    public void mouseMoved  ( MouseEvent e ) {}
    //============================================================

    // PopupMenuListener
    //
    //============================================================
    public void popupMenuCanceled( PopupMenuEvent e ) {}
    //============================================================




    //************************************************************
    // Private methods
    //************************************************************



    /**
     * Zeichen einer XOR-Linie zwischen den beiden
     * Bildkoordinatenpunkten _xor_point1 und _xor_point2.
     */

    //============================================================
    private void _drawXORLine()
    //============================================================
    {
        Graphics gr = getCanvas().getGraphics();

        gr.setColor( Color.white );
        gr.setXORMode( Color.red );
        gr.drawLine( (int) _xor_point1.x,
                     (int) _xor_point1.y,
                     (int) _xor_point2.x,
                     (int) _xor_point2.y );
        gr.dispose();
    } // _drawXORLine



    /**
     * Ermitteln des Screenkoordinaten-Rechtecks aus dem gegebenen
     * Weltkoordinaten-Rechteck
     */

    //============================================================
    private Rectangle2D.Double _getScreenRectangle(
                                            Rectangle2 rect_world )
    //============================================================
    {
        if( rect_world == null )
            return null;
                
        /*
        return new Rectangle2D.Double(rect_world.getX(), rect_world.getY(),
        							  rect_world.getWidth(), 
									  rect_world.getHeight());*/
        
        // Umrechnen in Screen-Koordinaten
        
        
        Rectangle2 rect_screen =
            _transformer.worldToScreen( rect_world.x,
                                        rect_world.y,
                                        rect_world.width,
                                        rect_world.height ); 

        return new Rectangle2D.Double( rect_screen.x,
                                       rect_screen.y,
                                       rect_screen.width,
                                       rect_screen.height ); 
    } // _getScreenRectangle


    /**
     * Initialisieren der Transformationsmatrix
     */

    //============================================================
    private void _initTransformer()
    //============================================================
    {
        double x_screen = getCanvas().getWidth() / 2;
        double y_screen = getCanvas().getHeight() / 2;

        double scale = Math.min( getInitScale(), getMaxScale() );

        _transformer = new WorldCoorTransformer( x_screen, y_screen, scale );
    } // _initTransformer

} // WorldCoorScene
