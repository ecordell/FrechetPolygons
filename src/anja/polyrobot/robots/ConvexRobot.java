package anja.polyrobot.robots;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.Color;


import anja.geom.Polygon2Scene;
import anja.geom.Point2;
import anja.geom.Polygon2;

import anja.util.GraphicsContext;
import anja.util.SimpleList;
import anja.util.ListItem;



//import java_ersatz.java2d.Graphics2D;
//import java_ersatz.java2d.Transform;




/**
 * Ein Objekt der Klasse ConvexRobot ist ein Robot ( @see Robot )
 * dessen Grundflaeche durch ein konvexes Polygon gegeben ist.
 *
 * @version 0.1 18.07.03
 * @author      Ulrich Handel
 */

//****************************************************************
public class ConvexRobot extends Robot
//****************************************************************
{
    //************************************************************
    // public class variables
    //************************************************************


    // Farben die beim Zeichnen des Roboters benutzt werden
    // ( @see paint(..) )

    public static Color color_outline = Color.green.darker();
    public static Color color_fill    = Color.yellow;
    public static Color color_center  = Color.green.darker();
    public static Color color_circle  = Color.red;

    //************************************************************
    // private variables
    //************************************************************

    private Polygon2 _shape;  // polygonale Form des Robots
    //
    // Dieses Polygon speichert die Koordinaten des nicht
    // gedrehten Roboters ( getOrientation() == 0.0 ) relativ
    // zu seiner Position ( @see getCoors() ).
    // Seine Koordinaten sind also konstant und werden
    // insbesondere nicht !!! durch Verschiebungen und Drehungen
    // des Roboters veraendert.
    // ( @see getShape() und getCurrentPolygon() )

    //************************************************************
    // constructors
    //************************************************************

    /**
     * Erzeugen eines neuen ConvexRobots.
     *
     * @param scene  Polygonszene in der sich der Roboter befindet.
     * @param coors  Anfaengliche Positionskoordinaten des Roboters.
     * @param shape  Polygonale Form des Roboters
     */

    //============================================================
    public ConvexRobot( Polygon2Scene scene,
                        Point2        coors,
                        Polygon2      shape )
    //============================================================
    {
        super( scene, coors );

        _shape = new Polygon2( shape );
    } // ConvexRobot


    /**
     * Erzeugen eines neuen ConvexRobots als Kopie des ConvexRobots
     * <robot>.
     */

    //============================================================
    public ConvexRobot( ConvexRobot robot )
    //============================================================
    {
        super( robot );

        _shape = robot._shape; // Da die Form eines ConvexRobots
                               // unveraenderlich ist, reicht hier das
                               // einfache Kopieren des Verweises
    } // ConvexRobot




    //************************************************************
    // public methods
    //************************************************************


    /**
     * Ermitteln der unveraenderlichen !!! Form dieses Roboters.
     *
     * Zu beachten:
     *    Die Koordinaten der Roboterform entsprechen den Koordinaten
     *    des Polygons welches durch diesen Roboter abgedeckt wuerde
     *    ( @see getCurrentPolygon() ), wenn er sich mit der
     *    Orientierung 0.0 ( @see getOrientation() ) an den
     *    Positionskoordinaten 0.0 ( @see getCoors() befinden wuerde.
     *
     * @return  Form des Roboters.
     */

    //============================================================
    public Polygon2 getShape()
    //============================================================
    {
        return new Polygon2( _shape );
    } // getShape

      /**
     * Setzen der unveraenderlichen !!! Form dieses Roboters.
     */

    //============================================================
    public void setShape( Polygon2 shape )
    //============================================================
    {
        _shape = shape;
    } // getShape
   

    /**
     * Ermitteln der polygonalen Flaeche, welche augenblicklich ( unter
     * Beruecksichtigung von Orientierung und Positionskoordinaten )
     * von diesem Roboter abgedeckt wird.
     */

    //============================================================
    public Polygon2 getCurrentPolygon()
    //============================================================
    {
        // aktuelle Positionskoordinaten ermitteln
        //
        Point2 pt = getCoors();
        double pos_x = pt.x;
        double pos_y = pt.y;

        // Sinus und Cosinus der aktuellen Orientierung ermitteln
        //
        double ori = getOrientation();
        double sin = Math.sin( ori );
        double cos = Math.cos( ori );


        // Leeres geschlossenes Rueckgabe-Polygon erzeugen
        //
        Polygon2 poly = new Polygon2();
        poly.setClosed();


        // Alle Punkte des Polygons _shape durchlaufen und dabei
        // das Rueckgabe-Polygon um neu erzeugte Punkte ( mit
        // entsprechend der Orientierung und den Positionskoordinaten
        // berechneten Koordinaten ) erweitern
        //
        SimpleList points = _shape.points();
        for( ListItem item = points.first();
             item != null;
             item = points.next( item ) )
        {
            Point2 point = ( Point2 )points.value( item );
            double x = point.x;
            double y = point.y;

            poly.addPoint( new Point2(  cos * x + sin * y + pos_x,
                                        - sin * x + cos * y + pos_y ) ); 
        } // for


        return poly;
    } // getCurrentPolygon

    //************************************************************
    // public methods
    //
    // ( Implementierung der abstrakten Robot-Methoden )
    //************************************************************


    /**
     * Liefern eines neuen Robots, der ein Duplikat dieses
     * Robots ist.
     */

    //============================================================
    public Object clone()
    //============================================================
    {
        return new ConvexRobot( this );
    } // clone


    /**
     */

    //============================================================
    public void move( Point2 target )
    //============================================================
    {
    	//--
    } // move


    /**
     */

    //============================================================
    public double turn( double turn_angle )
    //============================================================
    {
        double angle = 0.0;
        //--

        if( angle != 0.0 )
            setOrientation( getOrientation() + angle, angle );

        return angle;
    } // doTurn


    /**
     * Zeichnen des Roboters
     */

    //============================================================
    public void paint( Graphics2D g_world,
                       Graphics   g_screen )
    //============================================================
    {
        Polygon2 poly = getCurrentPolygon();

        GraphicsContext gc = new GraphicsContext();

        if( color_outline != null )
            gc.setForegroundColor( color_outline );

        if( color_fill != null )
        {
            gc.setFillColor( color_fill );
            gc.setFillStyle( 1 );
        } // if

        poly.draw( g_world, gc );
      
        Point2 point = getCoors();

        // Roboterposition in Bildkoordinaten ermitteln
        //AffineTransform transform = g_world.getTransform();
        //transform.transform( point, point );

        Robot.drawRobotCenter( g_screen,
                               Math.round( point.x ),
                               Math.round( point.y ),
                               getOrientation(),
                               color_circle,
                               color_center );
    } // paint


    //************************************************************
    // private methods
    //************************************************************


} // ConvexRobot
