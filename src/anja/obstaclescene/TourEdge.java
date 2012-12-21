package anja.obstaclescene;

/**
 * Ein Objekt der Klasse TourEdge repraesentiert eine der zwei Seiten
 * eines Segments ( @see Segment ).
 *
 * Als "Wegstueck" ist es immer Bestandteil eines Umlauf-Polygons
 * ( @see TourPolygon ), welches den geschlossenen Weg entlang der
 * Seiten eines aus Segmenten gebildeten Hindernisses beschreibt.
 *
 * Mit seinen zwei Nachbar-TourEdges ist es ueber TourVertex-Objekte
 * verbunden ( @see TourVertex ).
 *
 * @version 0.1 14.06.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class TourEdge
//****************************************************************
{
    //************************************************************
    // public constants
    //************************************************************


    // Richtungen fuer Drehungen, Umlaeufe etc.
    //
    public final static boolean CLOCKWISE  = ObstacleScene.CLOCKWISE;
    public final static boolean COUNTERCLOCKWISE = ! CLOCKWISE;

    // Richtungen fuer Polygonumlaeufe
    // Vorwaerts und linke/rechte Hand am Polygon
    //
    public final static boolean LEFT_HAND  = ObstacleScene.LEFT_HAND;
    public final static boolean RIGHT_HAND = ! LEFT_HAND;

    // Seiten ( beispielsweise einer Polygonkante )
    //
    public final static boolean INSIDE  = ObstacleScene.INSIDE;
    public final static boolean OUTSIDE = ! INSIDE;



    //************************************************************
    // private variables
    //************************************************************

    private Segment _segment; // Segment, zu dem dieses TourEdge-
                              // Objekt als eines von zweien gehoert.


    // Verbindungspunkte zu den Nachbarkanten des Umlauf-Polygons
    //
    private TourVertex _vertex_left_hand;  // wird bei einem
                    // LEFT_HAND-Umlauf als naechstes erreicht

    private TourVertex _vertex_right_hand; // wird bei einem
                    // RIGHT_HAND-Umlauf als naechstes erreicht

    private TourPolygon _polygon; // Umlauf-Polygon, zu dem diese Kante
                                  // als Wegstueck gehoert



    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen TourEdge-Objekts
     */

    //============================================================
    protected TourEdge( TourPolygon poly, Segment seg )
    //============================================================
    {
        _polygon = poly;
        _segment = seg;
    } // TourEdge




    //************************************************************
    // public methods
    //************************************************************



    /**
     * @return  ObstacleScene, zu dem dieses TourEdge-Objekt gehoert.
     */

    //============================================================
    public ObstacleScene getScene()
    //============================================================
    {
        return _segment.getScene();
    } // getScene


    /**
     * @return  TourPolygon, zu dem dieses TourEdge-Objekt gehoert.
     */

    //============================================================
    public TourPolygon getPolygon()
    //============================================================
    {
        return _polygon;
    } // getPolygon


    /**
     * @return  Typ des TourPolygons, zu dem dieses TourEdge-Objekt
     *          gehoert.
     */

    //============================================================
    public boolean getType()
    //============================================================
    {
        return _polygon.getType();
    } // getType


    /**
     * @return  Segment, zu dem dieses TourEdge-Objekt gehoert.
     */

    //============================================================
    public Segment getSegment()
    //============================================================
    {
        return _segment;
    } // Segment




    /**
     * @return  TourVertex, welcher bei einem LEFT_HAND-Umlauf
     *          ( vorwaerts mit linker Hand am zugehoerigen Segment )
     *          als naechstes erreicht wird.
     */

    //============================================================
    public TourVertex getVertexLeftHand()
    //============================================================
    {
        return _vertex_left_hand;
    } // getVertexLeftHand


    /**
     * @return  TourVertex, welcher bei einem RIGHT_HAND-Umlauf
     *          ( vorwaerts mit rechter Hand am zugehoerigen Segment )
     *          als naechstes erreicht wird.
     */

    //============================================================
    public TourVertex getVertexRightHand()
    //============================================================
    {
        return _vertex_right_hand;
    } // getVertexRightHand


    /**
     * Ermitteln des TourVertex, welcher bei einem durch <dir> gegebenen
     * Umlauf als naechstes erreicht wird.
     *
     * @param dir  LEFT_HAND oder RIGHT_HAND
     *
     * @return     TourVertex, welcher bei einem <dir>-Umlauf als
     *             naechstes erreicht wird.
     */

    //============================================================
    public TourVertex getVertex( boolean dir )
    //============================================================
    {
        if( dir == LEFT_HAND )
            return _vertex_left_hand;
        else
            return _vertex_right_hand;
    } // getVertex


    /**
     * Ermitteln des TourVertex, der mit dem gegebenen TourVertex
     * <vertex> durch dieses TourEdge-Objekt verbunden ist.
     *
     * @param vertex  VertexLeftHand oder VertexRightHand
     *
     * @return  VertexLeftHand,  falls vertex == VertexRightHand.
     *          VertexRightHand, falls vertex == VertexLeftHand.
     *          null             sonst
     */

    //============================================================
    public TourVertex getOtherVertex( TourVertex vertex )
    //============================================================
    {
        if( vertex == _vertex_left_hand )
            return _vertex_right_hand;

        if( vertex == _vertex_right_hand )
            return _vertex_left_hand;

        return null;
    } // getOtherVertex



    /**
     * @return  TourEdge, welche bei einem LEFT_HAND-Umlauf
     *          ( vorwaerts mit linker Hand am zugehoerigen Segment )
     *          als naechstes erreicht wird.
     */

    //============================================================
    public TourEdge getEdgeLeftHand()
    //============================================================
    {
        return _vertex_left_hand.getEdgeLeftHand();
    } // getEdgeLeftHand


    /**
     * @return  TourEdge, welche bei einem RIGHT_HAND-Umlauf
     *          ( vorwaerts mit rechter Hand am zugehoerigen Segment )
     *          als naechstes erreicht wird.
     */

    //============================================================
    public TourEdge getEdgeRightHand()
    //============================================================
    {
        return _vertex_right_hand.getEdgeRightHand();
    } // getEdgeRightHand


    /**
     * Ermitteln der TourEdge, welche bei einem durch <dir> gegebenen
     * Umlauf als naechstes erreicht wird.
     *
     * @param dir  LEFT_HAND oder RIGHT_HAND
     *
     * @return     TourEdge, welche bei einem <dir>-Umlauf als
     *             naechstes erreicht wird.
     */

    //============================================================
    public TourEdge getEdge( boolean dir )
    //============================================================
    {
        if( dir == LEFT_HAND )
            return _vertex_left_hand.getEdgeLeftHand();
        else
            return _vertex_right_hand.getEdgeRightHand();
    } // getEdge


    /**
     * Ermitteln der TourEdge, der mit der gegebenen TourEdge
     * <edge> durch dieses TourEdge-Objekt verbunden ist.
     *
     * @param edge  EdgeLeftHand oder EdgeRightHand
     *
     * @return  EdgeLeftHand,  falls edge == EdgeRightHand.
     *          EdgeRightHand, falls edge == EdgeLeftHand.
     *          null           sonst
     */

    //============================================================
    public TourEdge getOtherEdge( TourEdge edge )
    //============================================================
    {
        TourEdge edge_left_hand  = _vertex_left_hand.getEdgeLeftHand();
        TourEdge edge_right_hand = _vertex_right_hand.getEdgeRightHand();

        if( edge == edge_left_hand )
            return edge_right_hand;

        if( edge == edge_right_hand )
            return edge_left_hand;

        return null;
    } // getOtherEdge



    /**
     */

    //============================================================
    public TourEdge getOtherSegmentEdge()
    //============================================================
    {
        return _segment.getOtherEdge( this );
    } // getOtherSegmentEdge




    /**
     * @return  Laenge der TourEdge
     */

    //============================================================
    public double getLength()
    //============================================================
    {
        return _segment.getLength();
    } // getLength



    /**
     * @return  Richtung des Strahls
     *          ( VertexLeftHand --> VertexRightHand )
     */

    //============================================================
    public RayDirection getDirectionFromVertexLeftHand()
    //============================================================
    {
        return _segment.getDirectionFrom( _vertex_left_hand.getNode() );
    } // getDirectionFromVertexLeftHand


    /**
     * @return  Richtung des Strahls
     *          ( VertexRightHand --> VertexLeftHand )
     */

    //============================================================
    public RayDirection getDirectionFromVertexRightHand()
    //============================================================
    {
        return _segment.getDirectionFrom( _vertex_right_hand.getNode() );
    } // getDirectionFromVertexRightHand


    /**
     */

    //============================================================
    public RayDirection getDirectionFrom( TourVertex vertex )
    //============================================================
    {
        if( vertex == null )
            return null;

        return _segment.getDirectionFrom( vertex.getNode() );
    } // getDirectionFrom



    /**
     * Ermitteln der Richtung des Strahls, der Senkrecht auf der Kante
     * steht und in Richtung des zugehoerigen Segments zeigt
     */

    //============================================================
    public RayDirection getPerpendicularInside()
    //============================================================
    {
        return getDirectionFromVertexLeftHand().
                    getPerpendicular( CLOCKWISE );
    } // getPerpendicularInside


    /**
     * Ermitteln der Richtung des Strahls, der Senkrecht auf der Kante
     * steht und in Richtung weg vom zugehoerigen Segments zeigt
     */

    //============================================================
    public RayDirection getPerpendicularOutside()
    //============================================================
    {
        return getDirectionFromVertexRightHand().
                    getPerpendicular( CLOCKWISE );
    } // getPerpendicularOutside


    /**
     * Ermitteln der Richtung einer auf dieser Kante stehenden
     * Senkrechten.
     *
     * @param side  INSIDE oder OUTSIDE
     *
     * @return  PerpendicularInside, falls side  == INSIDE
     *          PerpendicularOutside, falls side == OUTSIDE
     */

    //============================================================
    public RayDirection getPerpendicular( boolean side )
    //============================================================
    {
        return ( side == INSIDE ) ?
               getPerpendicularInside() :
               getPerpendicularOutside() ;
    } // getPerpendicular



    //************************************************************
    // protected methods
    //************************************************************


    /**
     */

    //============================================================
    protected void setVertex( boolean    dir,
                              TourVertex vertex )
    //============================================================
    {
        if( dir == LEFT_HAND )
            _vertex_left_hand = vertex;
        else
            _vertex_right_hand = vertex;
    } // setVertex


    /**
     */

    //============================================================
    protected void setPolygon( TourPolygon poly )
    //============================================================
    {
        _polygon = poly;
    } // setPolygon

} // TourEdge
