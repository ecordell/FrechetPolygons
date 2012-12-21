package anja.obstaclescene;

import anja.util.*;

import java.awt.Point;

/**
 * Menge der in einem Knoten miteinander verbundenen Segmente.
 * Diese Klasse wird ausschliesslich von der Klasse Node benutzt
 *
 * @version 0.1 21.11.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class NodeSegments extends ExtendedRedBlackTree
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


                double radians1 = ( ( RayDirection )o1 ).getRadians();
                double radians2 = ( ( RayDirection )o2 ).getRadians();

                if( radians1 > radians2 )
                    return Comparitor.BIGGER;

                if( radians1 < radians2 )
                    return Comparitor.SMALLER;

                return Comparitor.EQUAL;
            } // compare
        };


    //************************************************************
    // private variables
    //************************************************************


    private Node _node; // gemeinsamer Knoten der Segmente


    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen NodeSegments-Objekts fuer den angegebenen
     * Knoten
     */

    //============================================================
    protected NodeSegments( Node node )
    //============================================================
    {
        super( _comp );

        _node = node;
    } // NodeSegments



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
    protected TreeItem addSegment( Segment segment )
    //============================================================
    {
        RayDirection dir = segment.getDirectionFrom( _node );

        return add( dir, segment );
    } // addSegment


    /**
     * Entfernen eines Segments
     */

    //============================================================
    protected void removeSegment( TreeItem item )
    //============================================================
    {
        remove( item );
    } // removeSegment




    //------------------------------------------------------------
    // Ermittlung von Segmenten aus dieser Segmentmenge
    //------------------------------------------------------------


    /**
     */

    //============================================================
    protected Segment getNextSegment( TreeItem item,
                                      boolean  dir )
    //============================================================
    {
        item = getNextItem( item, dir );

        return ( Segment )( item.value() );
    } // getNextSegment


    /**
     */

    //============================================================
    protected Segment getNextSegment( RayDirection ray_dir,
                                      boolean      turn_dir )
    //============================================================
    {
        TreeItem item = getNextItem( ray_dir, turn_dir );

        if( item == null )
            return null;

        return ( Segment )( item.value() );
    } // getNextSegment


    /**
     * @return  Array der Segmente, die in diesem Knoten miteinander
     *          verbundenen sind.
     *          Die Array enthaelt die Segmente nach ihren Strahlen-
     *          Richtungen geordnet
     */

    //============================================================
    protected Segment[] getSegments()
    //============================================================
    {
        TreeItem item = getFirst();

        Segment[] segments = new Segment[ length() ];

        for( int i = 0; i < segments.length; i++ )
        {
            segments[ i ] = ( Segment )( item.value() );
            item = getNext( item );
        } // for

        return segments;
    } // getSegments




    //------------------------------------------------------------
    // Ermittlung von TreeItems mit denen Segmente in dieser
    // Segmentmenge eingetragen sind
    //------------------------------------------------------------


    /**
     * Ermitteln des TreeItems des Segments, welches in Umlaufrichtung
     * dir mit dem Segment benachbart ist, welches mit dem gegebenen
     * TreeItem in dieser Segmentmenge eingetragen ist.
     *
     * @param dir  CLOCKWISE oder COUNTERCLOCKWISE
     *
     * @param item  TreeItem des Segments, dessen Nachbarsegment-Item
     *              zu ermitteln ist
     *
     * @return  TreeItem des benachbarten Segments
     */

    //============================================================
    protected TreeItem getNextItem( TreeItem item,
                                    boolean  dir )
    //============================================================
    {
        return ( dir == Node.CLOCKWISE ) ?
               getPrevCyclic( item ) :
               getNextCyclic( item ) ;
    } // getNextItem


    /**
     */

    //============================================================
    protected TreeItem getNextItem( RayDirection ray_dir,
                                    boolean      turn_dir )
    //============================================================
    {
        if(    ray_dir == null
            || empty() )
            return null;

        TreeItem item;

        if( turn_dir == Node.CLOCKWISE )
        {
            item = findSmaller( ray_dir );
            if( item == null )
                item = getLast();
        }
        else
        {
            item = findBigger( ray_dir );
            if( item == null )
                item = getFirst();
        }

        return item;
    } // getNextItem

} // NodeSegments
