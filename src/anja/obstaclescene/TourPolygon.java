package anja.obstaclescene;

import anja.geom.Polygon2;
import anja.geom.Point2;




/**
 * Ein Objekt der Klasse TourPolygon beschreibt den polygonalen Weg
 * entlang der Seiten eines Hindernisses, welches aus einem oder
 * mehreren zyklisch miteinander verbundenen Segmenten
 * ( @see Segment ) besteht.
 *
 * @version 0.1 14.06.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class TourPolygon
//****************************************************************
{
    //************************************************************
    // public constants
    //************************************************************


    // Polygon-Typen
    //
    public final static boolean OBSTACLE = ObstacleScene.OBSTACLE;
    public final static boolean YARD     = ! OBSTACLE;



    //************************************************************
    // private variables
    //************************************************************

    private TourEdge _first_edge; // erstes Wegstueck auf dem
                                  // durch das Polygon beschrieben
                                  // Weg

    private int _no_of_edges; // Anzahl der Wegstuecke

    private boolean _type; // OBSTACLE oder YARD;



    //************************************************************
    // protected variables
    //************************************************************


    protected boolean is_found = false;
                // Diese Markierung wird ggf. von anderen Klassen
                // dieses Packets benutzt, um bei Such-Anfragen dieses
                // Polygon als gefunden zu markieren
                //
                // Eine gesetzte Markierung muss nach einer
                // Polygon-Suche von der Suchmethode wieder
                // zurueckgesetzt werden



    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen TourPolygons als Wegbeschreibung um
     * ein neues Segment, das nicht mit anderen Segmenten verbunden
     * ist.
     *
     * Das neu erzeugte TourPolygon ist vom Typ OBSTACLE.
     * Es beschreibt also den Weg um ein Hindernis, welches umrundet
     * werden kann. ( Im Gegensatz zu einem Weg entlang den
     * Aussenkanten eines Hofes )
     *
     * @param segment  Segment, dessen Umrundung durch das Polygon
     *                 beschrieben werden soll
     */

    //============================================================
    protected TourPolygon( Segment segment )
    //============================================================
    {
        TourEdge edge_l = new TourEdge( this, segment );
        TourEdge edge_r = new TourEdge( this, segment );

        new TourVertex( segment.getNode1(), edge_l, edge_r );
        new TourVertex( segment.getNode2(), edge_r, edge_l );


        _first_edge  = edge_l;
        _no_of_edges = 2;
        _type = OBSTACLE;

    } // TourPolygon



    /**
     * Erzeugen ein neues nicht initialisierten TourPolygons
     */

    //============================================================
    private TourPolygon()
    //============================================================
    {
    } // TourPolygon



    //************************************************************
    // public methods
    //************************************************************


    /**
     * Ermitteln des ersten Wegstuecks auf dem durch das Polygon
     * beschriebenem Weg.
     */

    //============================================================
    public TourEdge getFirstEdge()
    //============================================================
    {
        return _first_edge;
    } // getFirstEdge


    /**
     * @return  Typ des Polygons ( OBSTACLE oder YARD )
     */

    //============================================================
    public boolean getType()
    //============================================================
    {
        return _type;
    } // getType


    /**
     * @return  Anzahl der TourEdges des Polygons
     */

    //============================================================
    public int getNoOfEdges()
    //============================================================
    {
        return _no_of_edges;
    } // getNoOfEdges


    /**
     * @return  Polygon als Polygon2-Objekt ( @see anja.geom.Polygon2 )
     */

    //============================================================
    public Polygon2 getPolygon2()
    //============================================================
    {
        Polygon2 poly = new Polygon2();

        TourVertex vertex = _first_edge.getVertexLeftHand();

        for( int i = 0; i < _no_of_edges; i++ )
        {
            poly.addPoint( new Point2( vertex.getX(), vertex.getY() ) );
            vertex = vertex.getVertexLeftHand();
        } // for

        poly.setClosed();

        return poly;
    } // getPolygon2




    //************************************************************
    // protected class methods
    //************************************************************


    /**
     */

    //============================================================
    static protected void updatePolygons( TourEdge edge_1,
                                          TourEdge edge_2,
                                          boolean  type )
    //============================================================
    {
        TourPolygon poly_1 = edge_1.getPolygon();
        TourPolygon poly_2 = edge_2.getPolygon();

        if( poly_1 != poly_2 )
        {
            _mergePolygons( edge_1, edge_2, type );
        }
        else // poly_1 == poly_2
        {
            poly_2._first_edge = edge_2;
            poly_2._splitNewPolygon( edge_1, type );
        } // else
    } // updatePolygons



    //************************************************************
    // private class methods
    //************************************************************


    /**
     */

    //============================================================
    static private void _mergePolygons( TourEdge edge_1,
                                        TourEdge edge_2,
                                        boolean  type )
    //============================================================
    {
        TourPolygon poly_1 = edge_1.getPolygon();
        TourPolygon poly_2 = edge_2.getPolygon();

        if( poly_1._no_of_edges < poly_2._no_of_edges )
        {
            poly_2._addEdgesRightHand( edge_1 );
            poly_2._setType( poly_1, type );
        }
        else
        {
            poly_1._addEdgesRightHand( edge_2 );
            poly_1._setType( poly_2, type );
        } // else
    } // _mergePolygons


    /**
     * Ermitteln und Setzen des Typs ( YARD oder OBSTACLE ) der
     * beiden angegebenen Polygone.
     *
     * Es wird vorausgesetzt, dass die beiden Polygone von
     * unterschiedlichem Typ sind.
     */

    //============================================================
    static private void _setDifferentTypes( TourPolygon poly_1,
                                           TourPolygon poly_2 )
    //============================================================
    {
        if( poly_2._no_of_edges < poly_1._no_of_edges )
        {
            TourPolygon poly;

            poly   = poly_1;
            poly_1 = poly_2;
            poly_2 = poly;
        } // if

        // poly_1 hat nicht mehr TourEdges als poly_2.

        poly_1._recalcType(); // Typ von poly_1 neu ermitteln und
                              // setzen

        poly_2._type = ! poly_1._type;
    } // _setDifferentTypes



    //************************************************************
    // private methods
    //************************************************************


    /**
     * Ermitteln und Setzen des Typs ( OBSTACLE oder YARD ) dieses
     * TourPolygons
     */

    //============================================================
    private void _recalcType()
    //============================================================
    {
        double angle_inside  = 0.0;
        double angle_outside = 0.0;

        TourVertex vertex = _first_edge.getVertexLeftHand();

        for( int i = 0; i < _no_of_edges; i++ )
        {
            angle_inside  += vertex.getAngleInside();
            angle_outside += vertex.getAngleOutside();

            vertex = vertex.getVertexLeftHand();
        } // for

        _type = ( angle_inside < angle_outside ) ?
                OBSTACLE :
                YARD;
    } // _recalcType


    /**
     */

    //============================================================
    private void _addEdgesRightHand( TourEdge edge )
    //============================================================
    {
        TourPolygon poly = edge.getPolygon();

        do
        {
            edge.setPolygon( this );

            _no_of_edges++;
            poly._no_of_edges--;

            edge = edge.getEdgeRightHand();
        }
        while( edge.getPolygon() != this );
    } // _addEdgesRightHand


    /**
     */

    //============================================================
    private TourPolygon _splitNewPolygon( TourEdge edge,
                                          boolean  type )
    //============================================================
    {
        TourPolygon new_poly = new TourPolygon();
        new_poly._first_edge = edge;

        new_poly._addEdgesRightHand( edge );

        if( _type == type )
        {
            new_poly._type = type;
        }
        else  // _type != type
        {
            TourPolygon._setDifferentTypes( new_poly, this );
        } // else

        return new_poly;
    } // _splitNewPolygon


    /**
     */

    //============================================================
    private void _setType( TourPolygon poly, boolean type )
    //============================================================
    {
        if( _type != poly._type )
        {
            _type = type;
        }
        else  // _type == poly._type
        {
            if( _type == OBSTACLE )
                _type = ! type;
        } // else
    } // _setType

} // TourPolygon
