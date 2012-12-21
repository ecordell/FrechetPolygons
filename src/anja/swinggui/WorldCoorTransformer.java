package anja.swinggui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;


import anja.geom.Point2;
import anja.geom.Rectangle2;

/*import java_ersatz.java2d.Transform;
import java_ersatz.java2d.AffineTransform;
import java_ersatz.java2d.NoninvertibleTransformException;

import java.awt.*; */


/**
 * Ein Objekt der Klasse WorldCoorTransformer definiert den Zusammenhang
 * zwischen Weltkoordinaten und Bildkoordinaten bei einer WorldCoorScene.
 *
 * Zur Umwandlung der Koordinaten von Punkten und Rechtecken sowie
 * von Distanzen stehen die public-Methoden
 *
 *  Point2     screenToWorld( x, y )
 *  Rectangle2 screenToWorld( x, y, width, height )
 *  double     screenToWorld( distance )
 *
 *  Point2     worldToScreen( x, y )
 *  Rectangle2 worldToScreen( x, y, width, height )
 *  double     worldToScreen( distance )
 *
 * bereit.
 *
 *
 * @version 0.1 04.08.03
 * @author      Ulrich Handel
 */

//****************************************************************
public class WorldCoorTransformer
//****************************************************************
{
    //************************************************************
    // private variables
    //************************************************************


    private AffineTransform _world_to_screen;
            // Transformationsmatrix zur Umrechnung von Welt- in
            // Bildkoordinaten

    private AffineTransform _screen_to_world;
            // Transformationsmatrix zur Umrechnung von Bild- in
            // Weltkoordinaten



    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen WorldCoorTransformer-Objekts.
     *
     * @param x_screen  x-Screen-Koordinate des Weltkoordinaten-Ursprungs
     * @param y_screen  y-Screen-Koordinate des Weltkoordinaten-Ursprungs
     * @param scale     Faktor um den das Bildkoordinatensystem gegenueber
     *                  dem Weltkoordinatensystem gedehnt ist
     */

    //============================================================
    public WorldCoorTransformer( double x_screen,
                                 double y_screen,
                                 double scale )
    //============================================================
    {
    	/*
    	 * Huh ? 
    	 * Below:
    	 * new AffineTransform(scale, 0, 0, - scale,
    	 * 					   x_screen, y_screen);
    	 * 
    	 *  This would invert the y-coordinate..... why do this -
    	 * in order to turn the user space into a 'standard' cartesian
    	 * coordinate system! (since in image space the positive y 
    	 * coordinate 'looks down'.
    	 * 
    	 */
    	
        _world_to_screen = new AffineTransform( scale, 0, 0, -scale,
                                                x_screen, y_screen );

        _screen_to_world = _createInverse( _world_to_screen ) ;
    } // setWorldToScreen


    /**
     * Erzeugen eines neuen WorldCoorTransformer-Objekts.
     *
     * @param world_to_screen  AffineTransform-Objekt zur Umwandlung von
     *                         Welt- in BildKoordinaten
     */

    //============================================================
    public WorldCoorTransformer( AffineTransform world_to_screen )
    //============================================================
    {
        _world_to_screen = ( AffineTransform )world_to_screen.clone();

        _screen_to_world = _createInverse( _world_to_screen ) ;
    } // setWorldToScreen




    //************************************************************
    // public methods
    //************************************************************


    /**
     * @return Das AffineTransform-Objekt fuer die Umwandlung von Welt- in
     *         Bildkoordinaten.
     */

    //============================================================
    public AffineTransform getWorldToScreen()
    //============================================================
    {
        return ( AffineTransform )_world_to_screen.clone();
    } // getWorldToScreen


    /**
     * @return Das AffineTransform-Objekt fuer die Umwandlung von Bild-
     *         in Weltkoordinaten.
     */

    //============================================================
    public AffineTransform getScreenToWorld()
    //============================================================
    {
        return ( AffineTransform )_screen_to_world.clone();
    } // getScreenToWorld




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
    public double screenToWorld( double distance )
    //============================================================
    {
        return _transform( _screen_to_world, distance );
    } // screenToWorld



    /**
     * Umwandeln des gegebenen Bildkoordinaten-Punktes in
     * Weltkoordinaten.
     *
     * @return der Weltkoordinaten-Punkt
     */

    //============================================================
    public Point2 screenToWorld( double x, double y )
    //============================================================
    {
        return _transform( _screen_to_world, x, y );
    } // screenToWorld



    /**
     * Umwandeln des gegebenen Bildkoordinaten-Punktes in
     * Weltkoordinaten.
     *
     * @return der Weltkoordinaten-Punkt
     */

    //============================================================
    public Point2 screenToWorld( Point2 point )
    //============================================================
    {
        return _transform( _screen_to_world, point.x, point.y );
    } // screenToWorld



    /**
     * Umwandeln des gegebenen Bildkoordinaten-Rechtecks in
     * Weltkoordinaten.
     *
     * @return das Weltkoordinaten-Rechteck
     */

    //============================================================
    public Rectangle2 screenToWorld( double x,
                                     double y,
                                     double width,
                                     double height )
    //============================================================
    {
        return _transform( _screen_to_world, x, y, width, height );
    } // screenToWorld



    /**
     * Umwandeln des gegebenen Bildkoordinaten-Rechtecks in
     * Weltkoordinaten.
     *
     * @return das Weltkoordinaten-Rechteck
     */

    //============================================================
    public Rectangle2 screenToWorld( Rectangle2 rect )
    //============================================================
    {
        return _transform( _screen_to_world,
                           rect.x,
                           rect.y,
                           rect.width,
                           rect.height );
    } // screenToWorld



    /**
     * Umwandeln der gegebenen Weltkoordinaten-Distanz in
     * Bildkoordinaten.
     *
     * @return die Bildkoordinaten-Distanz
     */

    //============================================================
    public double worldToScreen( double distance )
    //============================================================
    {
        return _transform( _world_to_screen, distance );
    } // worldToScreen



    /**
     * Umwandeln des gegebenen Weltkoordinaten-Punktes in
     * Bildkoordinaten.
     *
     * @return der Bildkoordinaten-Punkt
     */

    //============================================================
    public Point2 worldToScreen( double x, double y )
    //============================================================
    {
        return _transform( _world_to_screen, x, y );
    } // worldToScreen



    /**
     * Umwandeln des gegebenen Weltkoordinaten-Punktes in
     * Bildkoordinaten.
     *
     * @return der Bildkoordinaten-Punkt
     */

    //============================================================
    public Point2 worldToScreen( Point2 point )
    //============================================================
    {
        return _transform( _world_to_screen, point.x, point.y );
    } // worldToScreen



    /**
     * Umwandeln des gegebenen Weltkoordinaten-Rechtecks in
     * Bildkoordinaten.
     *
     * @return das Bildkoordinaten-Rechteck
     */

    //============================================================
    public Rectangle2 worldToScreen( double x,
                                     double y,
                                     double width,
                                     double height )
    //============================================================
    {
        return _transform( _world_to_screen, x, y, width, height );
    } // worldToScreen



    /**
     * Umwandeln des gegebenen Weltkoordinaten-Rechtecks in
     * Bildkoordinaten.
     *
     * @return das Bildkoordinaten-Rechteck
     */

    //============================================================
    public Rectangle2 worldToScreen( Rectangle2 rect )
    //============================================================
    {
        return _transform( _world_to_screen,
                           rect.x,
                           rect.y,
                           rect.width,
                           rect.height );
    } // worldToScreen



    /**
     * Zeichnen eines gefuellten Kreises dessen Mittelpunkt in
     * Weltkoordinaten und dessen Radius in Pixeln gegeben ist.
     *
     * Diese Methode markiert also einen Weltkoordinatenpunkt mit
     * einem Kreis, dessen Groesse Zoom-unabhaengig ist.
     *
     * @param g       Graphics-Objekt zum Zeichnen in Pixelkoordinaten
     * @param point   Mittelpunkt des Kreises in Weltkoordinaten.
     * @param radius  Radius des Kreises in Pixeln.
     * @param color   Fuellfarbe.
     */

    //============================================================
    public void drawFilledCircle( Graphics g,
                                  Point2   point,
                                  int      radius,
                                  Color    color )
    //============================================================
    {
    	
    	//TODO: the circle might not be drawn correctly
    	
    	/* The coordinate transformation should not be done manually,
    	 * since everything is transformed automatically by the 
    	 * Graphics2D rendering pipeline !
    	 * 
    	 */
		    	
        //point = worldToScreen( point.x, point.y );
       
    	/*
        int x = Math.round( point.x );
        int y = Math.round( point.y );

        g.setColor( color );

        g.fillOval( x - radius,
                    y - radius,
                    2 * radius + 1,
                    2 * radius + 1 ); */
    	
    	g.setColor(color);
    	g.fillOval(Math.round(point.x - radius),
    			   Math.round(point.y - radius),
				   2 * radius,
				   2 * radius);
    	
    } // drawFilledCircle

    //************************************************************
    // Private methods
    //************************************************************


    /**
     * Umwandeln des Abstandes distance mit Hilfe des AffineTransform-Objekts
     * transform.
     *
     * @return der transformierte Abstand
     */

    //============================================================
    private double _transform( AffineTransform transform,
                               double    distance )
    //============================================================
    {
        Point2 point0 = _transform( transform,
                                    0, 0 );
        Point2 point1 = _transform( transform,
                                    distance, 0 );

        return point1.x - point0.x;
    } // _transform



    /**
     * Umwandeln des Punktes ( x, y ) mit Hilfe des AffineTransform-Objekts
     * transform.
     *
     * @return der transformierte Punkt
     */

    //============================================================
    private Point2 _transform( AffineTransform transform,
                               double    x,
                               double    y )
    //============================================================
    {
        Point2 point = new Point2();

        //try
        //{
            transform.transform( new Point2( x, y ), point );
        //}
        //catch( Exception ex )
        //{
        //   ex.printStackTrace();
        //}

        return point;
    } // _transform


    /**
     * Umwandeln des Rechtecks ( x, y, width, height ) mit Hilfe des
     * AffineTransform-Objekts transform.
     *
     * @return das transformierte Rechteck
     */

    //============================================================
    private Rectangle2 _transform( AffineTransform transform,
                                   double    x,
                                   double    y,
                                   double    width,
                                   double    height )
    //============================================================
    {
    	
    	// TODO: Potential loss of coordinate precision
        Point2 pt1 = _transform( transform, x, y );
        Point2 pt2 = _transform( transform, x + width, y + height );

        x = Math.min( pt1.x, pt2.x );
        y = Math.min( pt1.y, pt2.y );
        width  = Math.abs( pt1.x - pt2.x );
        height = Math.abs( pt1.y - pt2.y );

        return new Rectangle2( (float)x,
                               (float)y,
                               (float)width,
                               (float)height );
    } // _transform


    /**
     * @return  Das inverse AffineTransform-Objekt zum gegebenen
     *          AffineTransform-Objekt
     */

    //============================================================
    private AffineTransform _createInverse( AffineTransform transform )
    //============================================================
    {
        try
        {
            return _world_to_screen.createInverse();
        }
        catch( NoninvertibleTransformException ex )
        {
            //ex.printStackTrace();
            System.out.println("Warning: non-invertible AffineTransform set" +
            				   "in WorldCoorTransformer!");
            return null;
        }
    } // _createInverse


} // WorldCoorTransformer
