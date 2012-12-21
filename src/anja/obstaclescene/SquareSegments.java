package anja.obstaclescene;

import anja.util.ExtendedRedBlackTree;
import anja.util.Comparitor;
import anja.util.KeyValueHolder;
import anja.util.TreeItem;

import java.awt.geom.Point2D;



/**
 * Menge der Segmente, die ein QuadTreeSquare vollstaendig durchlaufen,
 * also keinen Knoten in diesem haben.
 *
 * Jedes Segment wird fuer jeden Schnittpunkt mit der QuadTreeSquare-
 * Kante einmal in den Baum eingetragen. Also 2 mal, wenn das Segment
 * das QuadTreeSquare "echt" durchlaeuft, und 1 mal, wenn es das
 * QuadTreeSquare nur an einer Ecke "streift".
 *
 * Die eingetragen Segmente sind im Baum nach ihren Schnittpunkten mit
 * der QuadTreeSquare-Kante geordnet ( gegen den Uhrzeigersinn ).
 *
 * @version 0.1 19.02.03
 * @author      Ulrich Handel
 */

//****************************************************************
public class SquareSegments extends ExtendedRedBlackTree
//****************************************************************
{
    //************************************************************
    // private class variables
    //************************************************************

    private final static Comparitor _comp =
        new Comparitor()
        {
            // Definition der Interface-Methode compare
            //
            public short compare( Object o1, Object o2)
            {
                // Bei KeyValueHolder-Objekten werden die Schluessel
                // verglichen
                if( o1 instanceof KeyValueHolder )
                    o1 = ( ( KeyValueHolder ) o1 ).key();
                if( o2 instanceof KeyValueHolder )
                    o2 = ( ( KeyValueHolder ) o2 ).key();

                Point2D.Double point1 = ( Point2D.Double )o1;
                Point2D.Double point2 = ( Point2D.Double )o2;

                double x1 = point1.x;
                double y1 = point1.y;
                double x2 = point2.x;
                double y2 = point2.y;


                if( x1 == x2 && y1 == y2
                    )
                    return Comparitor.EQUAL;


                int quadrant1 = ( y1 >= 0.0 ) ?
                                ( ( x1 > 0.0 ) ? 0 : 1 ) :
                                ( ( x1 > 0.0 ) ? 3 : 2 );

                int quadrant2 = ( y2 >= 0.0 ) ?
                                ( ( x2 > 0.0 ) ? 0 : 1 ) :
                                ( ( x2 > 0.0 ) ? 3 : 2 );



                if( quadrant1 > quadrant2
                    )
                    return Comparitor.BIGGER;

                if( quadrant1 < quadrant2
                    )
                    return Comparitor.SMALLER;


                // quadrant1 == quadrant2

                switch( quadrant1 )
                {
                  case 0:
                    if( x1 < x2 || y1 > y2
                        )
                        return Comparitor.BIGGER;
                    break;

                  case 1:
                    if( x1 < x2 || y1 < y2
                        )
                        return Comparitor.BIGGER;
                    break;

                  case 2:
                    if( x1 > x2 || y1 < y2
                        )
                        return Comparitor.BIGGER;
                    break;

                  case 3:
                    if( x1 > x2 || y1 > y2
                        )
                        return Comparitor.BIGGER;
                    break;
                } // switch

                return Comparitor.SMALLER;
            } // compare
        };


    //************************************************************
    // private variables
    //************************************************************


    private long _d_x_center;
    private long _d_y_center;
    private int _limit;



    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen SquareSegments-Objekts
     */

    //============================================================
    protected SquareSegments( int x_min,
                              int y_min,
                              int size )
    //============================================================
    {
        super( _comp );

        _d_x_center = 2 * ( long )x_min + size - 1;
        _d_y_center = 2 * ( long )y_min + size - 1;
        _limit      = size;
    } // SquareSegments



    //************************************************************
    // protected methods
    //************************************************************


    //------------------------------------------------------------
    // Zufuegen und Entfernen von Segmenten zu dieser Segmentmenge
    //------------------------------------------------------------


    /**
     * Zufuegen eines Segments
     */

    //============================================================
    protected TreeItem[] addSegment( Segment segment )
    //============================================================
    {
        TreeItem[] items = new TreeItem[ 2 ];

        Node node1 = segment.getNode1();
        Node node2 = segment.getNode2();

        int x1 = node1.getX();
        int y1 = node1.getY();
        int x2 = node2.getX();
        int y2 = node2.getY();

        Point2D.Double pt1 = new Point2D.Double();
        Point2D.Double pt2 = new Point2D.Double();

        _getIntersectionPoints( x1, y1, x2, y2, pt1, pt2 );

        items[ 0 ] = add( pt1, segment );

        if(    pt2.x != pt1.x
            || pt2.y != pt1.y
            )
            items[ 1 ] = add( pt2, segment );

        return items;
    } // addSegment


    /**
     * Entfernen eines Segments
     */

    //============================================================
    protected void removeSegment( TreeItem[] items )
    //============================================================
    {
        remove( items[ 0 ] );

        if( items[ 1 ] != null )
            remove( items[ 1 ] );
    } // removeSegment




    //------------------------------------------------------------
    // Ermitteln von Segmenten aus dieser Segmentmenge
    //------------------------------------------------------------


    /**
     * Ermitteln der beiden Segmente die mit dem Square-Eintrittspunkt
     * der Strecke (<x1>,<y1>)---(<x2>,<y2>) benachbart sind
     */

    //============================================================
    protected Segment[] getNeighbourSegments( int x1,
                                              int y1,
                                              int x2,
                                              int y2 )
    //============================================================
    {
        if( empty()
            )
            return null;

        Segment[] segments = new Segment[ 2 ];

        Point2D.Double pt = new Point2D.Double();
        _getIntersectionPoints( x1, y1, x2, y2, pt, null );


        TreeItem item;

        item = findSmaller( pt );
        if( item == null )
            item = getLast();

        segments[ 0 ] = ( Segment )item.value();


        item = findBigger( pt );
        if( item == null )
            item = getFirst();

        segments[ 1 ] = ( Segment )item.value();


        return segments;
    } // getNeighbourSegments




    //************************************************************
    // private methods
    //************************************************************


    /**
     * Ermitteln der beiden Schnittpunkte der Strecke
     * (<x1>,<y1>)---(<x2>,<y2>) mit dem QuadTreeSquare
     */

    //============================================================
    private void _getIntersectionPoints( long           x1,
                                         long           y1,
                                         long           x2,
                                         long           y2,
                                         Point2D.Double pt1,
                                         Point2D.Double pt2 )
    //============================================================
    {
        x1 = 2 * x1 - _d_x_center;
        y1 = 2 * y1 - _d_y_center;

        x2 = 2 * x2 - _d_x_center;
        y2 = 2 * y2 - _d_y_center;

        int xmin = -_limit;
        int ymin = -_limit;
        int xmax = _limit;
        int ymax = _limit;

        if( x1 == x2 )
        {
            pt1.x = x1;
            pt1.y = ( y1 < y2 ) ? ymin : ymax;

            if( pt2 != null )
            {
                pt2.x = x1;
                pt2.y = ( y1 < y2 ) ? ymax : ymin;
            } // if

            return;
        } // if

        if( y1 == y2 )
        {
            pt1.y = y1;
            pt1.x = ( x1 < x2 ) ? xmin : xmax;

            if( pt2 != null )
            {
                pt2.y = y1;
                pt2.x = ( x1 < x2 ) ? xmax : xmin;
            } // if

            return;
        } // if


        boolean reverse_x = ( x1 > x2 );
        if( reverse_x )
        {
            x1 = -x1;
            x2 = -x2;
        } // if

        boolean reverse_y = ( y1 > y2 );
        if( reverse_y )
        {
            y1 = -y1;
            y2 = -y2;
        } // if


        // Es gilt jetzt  x2 > x1 && y2 > y1

        long dx = x2 - x1;
        long dy = y2 - y1;

        long value_xmin = dy * ( xmin - x1 );
        long value_ymin = dx * ( ymin - y1 );
        long value_xmax = dy * ( xmax - x1 );
        long value_ymax = dx * ( ymax - y1 );

        pt1.x = ( reverse_x ) ? xmax : xmin;
        pt1.y = ( reverse_y ) ? ymax : ymin;

        if( value_ymin > value_xmin )
        {
            if( value_ymin >= value_xmax )
            {
                pt1.x = -pt1.x;

                if( pt2 != null )
                {
                    pt2.x = pt1.x;
                    pt2.y = pt2.y;
                } // if

                return;
            } // if

            pt1.x = (double)( value_ymin + ( dy * x1 ) ) / (double)dy;
            if( reverse_x )
                pt1.x = -pt1.x;
        }
        else
        if( value_ymin < value_xmin )
        {
            if( value_xmin >= value_ymax )
            {
                pt1.y = -pt1.y;

                if( pt2 != null )
                {
                    pt2.x = pt1.x;
                    pt2.y = pt2.y;
                } // if

                return;
            } // if

            pt1.y = (double)( value_xmin + ( dx * y1 ) ) / (double)dx;
            if( reverse_y )
                pt1.y = -pt1.y;
        } // if


        if( pt2 == null )
            return;


        pt2.x = xmax;
        pt2.y = ymax;

        if( value_ymax > value_xmax )
        {
            pt2.y = (double)( value_xmax + ( dx * y1 ) ) / (double)dx;
        }
        else
        if( value_ymax < value_xmax )
        {
            pt2.x = (double)( value_ymax + ( dy * x1 ) ) / (double)dy;
        } // if

        if( reverse_x )
            pt2.x = -pt2.x;

        if( reverse_y )
            pt2.y = -pt2.y;

    } // _getIntersectionPoints


} // SquareSegments
