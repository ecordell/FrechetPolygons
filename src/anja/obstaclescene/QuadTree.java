package anja.obstaclescene;


import anja.util.SimpleList;
import anja.util.ListItem;
import anja.util.TreeItem;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;




/**
 * @version 0.1 04.11.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class QuadTree
//****************************************************************
{
    //************************************************************
    // private variables
    //************************************************************


    private QuadTreeSquare _root;
            // Wurzel des Baumes ( repraesentiert die gesamte Flaeche )




    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen QuadTree's fuer das Quadrat mit den
     * Grenzkoordinaten
     *      x_min == y_min == -( 2 hoch <exp> )
     *      x_max == y_max == ( 2 hoch <exp> ) - 1
     *
     * @param exp  nichtnegativer Exponent fuer Grenzkoordinaten.
     *             Der hoechste moegliche Exponent ist 30.
     */

    //============================================================
    protected QuadTree( int exp )
    //============================================================
    {
        // Exponent ggf. in zulaessige Groessenordnung bringen
        if( exp < 0 )
            exp = 0;
        if( exp > 30 )
            exp = 30;

        _root = new QuadTreeSquare( exp );
    } // QuadTree




    //************************************************************
    // protected methods
    //************************************************************


    //------------------------------------------------------------
    // Zufuegen und Entfernen von Knoten und Segmenten
    //------------------------------------------------------------


    /**
     * Zufuegen des Knotens <node> zum QuadTree
     */

    //============================================================
    protected void put( Node node )
    //============================================================
    {
        node.items_quadtree = new SimpleList();

        _root.put( node );
    } // put


    /**
     * Zufuegen des Segments <segment> zum QuadTree
     */

    //============================================================
    protected void put( Segment segment )
    //============================================================
    {
        Node node1 = segment.getNode1();
        Node node2 = segment.getNode2();

        if( node1.getNoOfSegments() == 1 )
        {
            _remove( node1);
            put( node1 );
        } // if

        if( node2.getNoOfSegments() == 1 )
        {
            _remove( node2);
            put( node2 );
        } // if

        segment.listitems_quadtree = new SimpleList();
        segment.treeitems_quadtree = new SimpleList();

        _root.put( segment );
    } // put


    /**
     * Entfernen des Knotens <node> aus dem QuadTree
     */

    //============================================================
    protected void remove( Node node )
    //============================================================
    {
        // Knoten aus allen Listen in den QuadTreeSquares entfernen
        _remove( node );

        // QuadTreeSquares nach Entfernen des Knotens reorganisieren.
        // ( entstandene leere Blaetter des Trees werden ggf. entfernt )
        //
        _root.hasRemoved( node );
    } // remove


    /**
     * Entfernen des Segments <segment> aus dem QuadTree
     */

    //============================================================
    protected void remove( Segment segment )
    //============================================================
    {
        // Segment aus allen Listen in den QuadTreeSquares entfernen
        _remove( segment );


        Node node1 = segment.getNode1();
        Node node2 = segment.getNode2();

        if( node1.isSingle() )
        {
            _remove( node1);
            put( node1 );
        } // if

        if( node2.isSingle() )
        {
            _remove( node2);
            put( node2 );
        } // if


        // QuadTreeSquares nach Entfernen des Segments reorganisieren.
        // ( entstandene leere Blaetter des Trees werden ggf. entfernt )
        //
        _root.hasRemoved( segment );
    } // remove




    //------------------------------------------------------------
    // Tests fuer das erlaubte Setzen von neuen Knoten und Segmenten.
    //
    // Die Bedingungen fuer das erlaubte Setzen von Knoten und Segmenten
    // sind so gewaehlt, dass verschiedene Segmente nur dann das selbe
    // Elementarquadrat schneiden koennen, wenn sie in einem gemeinsamen
    // Knoten miteinander verbunden sind.
    //------------------------------------------------------------


    /**
     * Testen, ob das Setzen eines neuen Knotens mit den Koordinaten
     * (<x>,<y>) erlaubt ist.
     *
     * Das Setzen eines neuen Knotens mit Koordinaten (<x>,<y>) ist
     * genau dann erlaubt, wenn das durch (<x>,<y>) bestimmte
     * Elementarquadrat sowie die daran angrenzenden Elementarquadrate
     * weder bereits einen Knoten enthalten noch von Segmenten
     * geschnitten werden.
     * Anders formuliert: Ein Knoten mit den Koordinaten (<x>,<y>) darf
     * nur gesetzt werden, wenn das Quadrat mit den Grenzkoordinaten
     *      xmin = <x>-1.5,
     *      ymin = <y>-1.5,
     *      xmax = <x>+1.5,
     *      ymax = <y>+1.5
     * nicht von bereits existierenden Knoten oder Segmenten geschnitten
     * wird.
     *
     * @return true <==> Das Setzen eines neuen Knotens mit den
     *                   Koordinaten (<x>,<y>) ist erlaubt
     */

    //============================================================
    protected boolean isNewNodeAllowed( int x, int y )
    //============================================================
    {
        QueryRectangle rect =
            new QueryRectangle( x - 1, y - 1, x + 1, y + 1 );

        if( _root.nodeIntersectsArea( rect )
            )
            return false;

        return ! _root.segmentIntersectsOutline( rect );
    } // isNewNodeAllowed


    /**
     * Testen, ob das Verbinden der Punkte (<x1>,<y1>) und (<x2>,<y2>)
     * durch ein Segment erlaubt ist.
     *
     * Vorbedingungen:
     *
     *    abs(<x1>-<x2>) > 1 || abs(<y1>-<y2>) > 1
     *    ( Minimalabstand zwischen Knoten wird nicht unterschritten ).
     *
     *    An (<x1>,<y1>) bzw. (<x2>,<y2>) befindet sich bereits ein
     *    Knoten oder das Setzen eines Knotens mit den jeweiligen
     *    Koordinaten ist erlaubt ( @see isNewNodeAllowed(..) ).
     *
     * Unter den gegebenen Vorbedingungen ist ein Segment
     * (<x1>,<y1>)---(<x2>,<y2>) genau dann erlaubt, wenn es keinen
     * "echten" Schnitt mit einem bereits existierenden Segment gibt
     * und kein "verbotener Bereich" eines Knotens geschnitten wird,
     * der nicht die Koordinaten (<x1>,<y1>) oder (<x2>,<y2>) hat.
     * Dabei ist der "verbotene Bereich" eines Knotens mit den
     * Koordinaten (x,y) das Quadrat mit den Grenzkoordinaten
     *      xmin = x-1.5,
     *      ymin = y-1.5,
     *      xmax = x+1.5,
     *      ymax = y+1.5
     *
     * @return true <==> Das Verbinden der Punkte (<x1>,<y1>) und
     *                   (<x2>,<y2>) durch ein Segment ist erlaubt
     */

    //============================================================
    protected boolean isNewSegmentAllowed( int x1,
                                           int y1,
                                           int x2,
                                           int y2 )
    //============================================================
    {
        if( _root.newSegmentIntersectsNodeSquare( x1, y1, x2, y2 ) )
            return false;

        return ! _root.newSegmentIntersectsSegment( x1, y1, x2, y2 );
    } // isNewSegmentAllowed




    //------------------------------------------------------------
    // Ermittlung von Knoten und Segmenten
    //------------------------------------------------------------


    /**
     */

    //============================================================
    protected void getNodes( QueryArea  area,
                             SimpleList nodes_single,
                             SimpleList nodes_connected )
    //============================================================
    {
        _root.getNodes( area, nodes_single, nodes_connected );
    } // getNodes


    /**
     */

    //============================================================
    protected void getSegments( QueryArea  area,
                                SimpleList list_inside,
                                SimpleList list_outside )
    //============================================================
    {
        if( list_inside != null )
        {
            // Ermitteln aller Knoten mit Segmentverbindungen, die sich
            // auf der Suchflaeche area befinden
            //
            SimpleList nodes_connected = new SimpleList();
            _root.getNodes( area, null, nodes_connected );

            // Eintragen der mit den Knoten verbundenen Segmente
            // in die Liste list_inside
            //
            for( ListItem item = nodes_connected.first();
                          item != null;
                          item = item.next() )
            {
                Node node = ( Node )( item.value() );

                for( Segment seg = node.getFirstSegment();
                             seg != null;
                             seg = node.getNextSegment( seg ) )
                {
                    if( ! seg.is_marked )
                    {
                        list_inside.add( seg );
                        seg.is_marked = true;
                    } // if
                } // for
            } // for
        } // if

        if( list_outside != null )
        {
            SimpleList list = new SimpleList();
            _root.getOutlineSegments( area, list );

            for( ListItem item = list.first();
                          item != null;
                          item = item.next() )
            {
                Segment seg = ( Segment )( item.value() );
                seg.is_marked = false;

                if( area.intersects( seg ) )
                    list_outside.add( seg );
            } // for

        } // if

        if( list_inside != null )
            _resetFoundMarks( list_inside );

    } // getSegments


    /**
     */

    //============================================================
    protected Position getNearestPosition( QueryArea area,
                                           Position  pos )
    //============================================================
    {
        return _root.getNearestPosition( area, pos );
    } // getNearestPosition


    /**
     */

    //============================================================
    protected TourVertex[] getVisibleVertices( Position pos )
    //============================================================
    {
//+
        return null;
    } // getVisibleVertices



    /**
     * Ermitteln des Knotens mit den Koordinaten (<x>,<y>)
     *
     * @return  Ermittelter Knoten
     *          oder <null> falls kein Knoten die Koordinaten
     *          (<x>,<y>) hat
     */
/*
    //============================================================
    protected Node getNode( int x, int y )
    //============================================================
    {
        return _root.getNode( x, y );
    } // getNode
*/

    /**
     * Ermitteln des Segments, welches den Punkt (<x>,<y>) schneidet
     *
     * Falls mehrere Segmente den Punkt (<x>,<y>) schneiden ( Segmente
     * sind in x,y durch Knoten verbunden ), wird das erste gefundene
     * zurueckgegeben.
     *
     * @return  Ermitteltes Segment
     *          oder <null> falls kein Segment den Punkt (<x>,<y>)
     *          schneidet
     */
/*
    //============================================================
    protected Segment getSegment( int x, int y )
    //============================================================
    {
        return _root.getSegment( x, y );
    } // getSegment
*/

    /**
     * Ermitteln des Objekts ( Node oder Segment ) welches den Punkt
     * (<x>,<y>) schneidet
     *
     * @return  Ermitteltes Objekt ( Node oder Segment )
     *          oder <null> falls der Punkt (<x>,<y>) weder von einem
     *          Knoten noch von einem Segment geschnitten wird
     */
/*
    //============================================================
    protected Object getObject( int x, int y )
    //============================================================
    {
        Node node = _root.getNode( x, y );

        if( node != null )
            return node; // Knoten mit Koordinaten (x,y) gefunden

        return _root.getSegment( x, y );
    } // getObject
*/




    /**
     */

    //============================================================
    protected Node getNearestNode( int  x,
                                   int  y,
                                   long max_square_dist )
    //============================================================
    {
        SimpleList list = new SimpleList();

        _root.getNearestNodes( x, y, max_square_dist, list );

        if( list.length() == 0
            )
            return null;

        return ( Node )list.first().value();

    } // getNearestNode


    /**
     */

    //============================================================
    protected SimpleList getNearestNodes( int  x,
                                          int  y,
                                          long max_square_dist )
    //============================================================
    {
        SimpleList list = new SimpleList();

        _root.getNearestNodes( x, y, max_square_dist, list );

        return list;

    } // getNearestNodes


    /**
     */

    //============================================================
    protected Segment getNearestSegment( int  x,
                                         int  y,
                                         long max_square_dist )
    //============================================================
    {
        return _root.getNearestSegment( x, y, max_square_dist );
    } // getNearestSegment




    //------------------------------------------------------------
    // Diverse Tests
    //------------------------------------------------------------


    /**
     * Testen, ob die Strecke von (<x1>,<y1>) --- (<x2>,<y2>) in den
     * Baum eingetragene Objekte ( Knoten oder Segmente ) schneidet
     *
     * @param ignore_1   true <==>
     *                   Der Punkt (<x1>,<y1>) darf mit den Koordinaten
     *                   eines Knotens uebereinstimmen oder auf einem
     *                   Segment liegen ohne dass dies als Schnitt
     *                   erkannt wird.
     *
     * @param ignore_2   wie ignore_1, aber fuer Punkt (<x2>,<y2>)
     *
     * @return  true <==> Es gibt mindestens einen Schnitt der gegebenen
     *                    Strecke mit in diesem Baum eingetragenen
     *                    Objekten
     */
/*
    //============================================================
    protected boolean intersects( int     x1,
                                  int     y1,
                                  int     x2,
                                  int     y2,
                                  boolean ignore_1,
                                  boolean ignore_2 )
    //============================================================
    {
        return _root.intersects( x1, y1, x2, y2,
                                 ignore_1,
                                 ignore_2 );
    } // intersects
*/

    /**
     * Ermitteln des ersten Schnittpunkts der Strecke
     * (<x1>,<y1>)---(<x2>,<y2>) mit einem in den Baum eingetragenen
     * Objekt ( Knoten oder Segment )
     *
     * @param ignore_1   true <==>
     *                   Der Punkt (<x1>,<y1>) darf mit den Koordinaten
     *                   eines Knotens uebereinstimmen oder auf einem
     *                   Segment liegen ohne dass dies als Schnitt
     *                   erkannt wird.
     *
     * @param ignore_2   wie ignore_1, aber fuer Punkt (<x2>,<y2>)
     *
     * @return  gefundener Schnittpunkt oder
     *          <null>, falls es keinen Schnittpunkt gibt
     */
/*
    //============================================================
    protected Position getFirstIntersection( int     x1,
                                             int     y1,
                                             int     x2,
                                             int     y2,
                                             boolean ignore_1,
                                             boolean ignore_2 )
    //============================================================
    {
        return _root.getFirstIntersection( x1, y1, x2, y2,
                                           ignore_1,
                                           ignore_2 );
    } // getFirstIntersection
*/



    //------------------------------------------------------------
    // Diverse andere Methoden
    //------------------------------------------------------------


    /**
     * Zeichnen des Baumes
     */

    //============================================================
    protected void paint( Graphics  g,
                          AffineTransform transform )
    //============================================================
    {
        _root.paint( g, g.getClipBounds(), transform );
    } // paint




    //************************************************************
    // private methods
    //************************************************************


    /**
     * Entfernen der "found"-Markierungen der in der Liste list
     * gegebenen Segmente.
     *
     * Diese Markierungen wurden beim Aufbau der Liste fuer jedes
     * zugefuegte Segment gesetzt, um zu verhindern, dass das gleiche
     * Segment mehrmals in die Liste aufgenommen wird.
     */

    //============================================================
    private static void _resetFoundMarks( SimpleList list )
    //============================================================
    {
        for( ListItem i = list.first();
                      i != null;
                      i = i.next() )
        {
            Segment seg = ( Segment )( i.value() );
            seg.is_marked = false;
        } // for
    } // _resetFoundMarks


    /**
     * Entfernen des Knotens <node> aus allen Listen in den
     * QuadTreeSquares
     */

    //============================================================
    private static void _remove( Node node )
    //============================================================
    {
        for( ListItem i = node.items_quadtree.first();
                      i != null;
                      i = i.next() )
        {
            ListItem item = ( ListItem )i.value();

            SimpleList l = ( SimpleList )item.getOwningList();
            l.remove( item );
        } // for

        node.items_quadtree = null;
    } // _remove


    /**
     * Entfernen des Segments <segment> aus allen Listen in den
     * QuadTreeSquares
     */

    //============================================================
    private static void _remove( Segment segment )
    //============================================================
    {
        for( ListItem i = segment.listitems_quadtree.first();
                      i != null;
                      i = i.next() )
        {
            ListItem item = ( ListItem )i.value();

            SimpleList l = ( SimpleList )item.getOwningList();
            l.remove( item );
        } // for

        segment.listitems_quadtree = null;


        if( segment.treeitems_quadtree == null )
            return;

        for( ListItem i = segment.treeitems_quadtree.first();
                      i != null;
                      i = i.next() )
        {
            TreeItem[] items = ( TreeItem[] )i.value();

            SquareSegments l = ( SquareSegments )items[0].getOwningTree();
            l.removeSegment( items );
        } // for

        segment.treeitems_quadtree = null;
    } // _remove

} // QuadTree


