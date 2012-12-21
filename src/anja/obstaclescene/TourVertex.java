package anja.obstaclescene;

import anja.geom.Point2;



/**
 * Ein Objekt der Klasse TourVertex repraesentiert den Verbindungspunkt
 * zwischen zwei Kanten ( @see TourEdge ) eines Umlaufpolygons
 * ( @see TourPolygon ).
 *
 * @version 0.1 14.06.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class TourVertex
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

    private ObstacleScene _scene; // Szene, zu der dieser Vertex gehoert

    private Node _node; // Knoten, zu dem dieser Vertex gehoert


    // Die durch diesen TourVertex miteinander verbundenen TourEdges
    //
    private TourEdge _edge_left_hand;  // wird bei einem
                // LEFT_HAND-Umlauf als naechstes erreicht

    private TourEdge _edge_right_hand; // wird bei einem
                // RIGHT_HAND-Umlauf als naechstes erreicht



    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen TourVertex-Objekts fuer den gegebenen
     * Knoten.
     */

    //============================================================
    protected TourVertex( Node     node,
                          TourEdge edge_left_hand,
                          TourEdge edge_right_hand )
    //============================================================
    {
        _scene = node.getScene();
        _node  = node;

        _connectToEdge( LEFT_HAND , edge_left_hand );
        _connectToEdge( RIGHT_HAND, edge_right_hand );
    } // TourVertex


    //************************************************************
    // public methods
    //************************************************************



    /**
     * @return  ObstacleScene, zu dem dieses TourVertex-Objekt gehoert.
     */

    //============================================================
    public ObstacleScene getScene()
    //============================================================
    {
        return _scene;
    } // getScene


    /**
     * @return  Node, zu dem dieses TourVertex-Objekt gehoert.
     */

    //============================================================
    public Node getNode()
    //============================================================
    {
        return _node;
    } // Node





    /**
     * @return  TourPolygon, zu dem dieses TourVertex-Objekt gehoert.
     */

    //============================================================
    public TourPolygon getPolygon()
    //============================================================
    {
        return _edge_left_hand.getPolygon();
    } // getPolygon


    /**
     * @return  Typ des TourPolygons, zu dem dieses TourVertex-Objekt
     *          gehoert.
     */

    //============================================================
    public boolean getType()
    //============================================================
    {
        return _edge_left_hand.getPolygon().getType();
    } // getType



    /**
     * @return  Koordinaten dieses TourVertex als Point2-Objekt
     *          ( @see anja.geom.Point2 )
     */

    //============================================================
    public Point2 getPoint2()
    //============================================================
    {
        return _node.getPoint2();
    } // getPoint2


    /**
     * @return  X-Koordinate dieses TourVertex
     */

    //============================================================
    public int getX()
    //============================================================
    {
        return _node.getX();
    } // getX


    /**
     * @return  Y-Koordinate dieses TourVertex
     */

    //============================================================
    public int getY()
    //============================================================
    {
        return _node.getY();
    } // getY





    /**
     * @return  TourEdge, welche bei einem LEFT_HAND-Umlauf
     *          ( vorwaerts mit linker Hand am zugehoerigen Knoten )
     *          als naechstes erreicht wird.
     */

    //============================================================
    public TourEdge getEdgeLeftHand()
    //============================================================
    {
        return _edge_left_hand;
    } // getEdgeLeftHand


    /**
     * @return  TourEdge, welche bei einem RIGHT_HAND-Umlauf
     *          ( vorwaerts mit rechter Hand am zugehoerigen Knoten )
     *          als naechstes erreicht wird.
     */

    //============================================================
    public TourEdge getEdgeRightHand()
    //============================================================
    {
        return _edge_right_hand;
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
            return _edge_left_hand;
        else
            return _edge_right_hand;
    } // getEdge


    /**
     * Ermitteln der TourEdge, der mit der gegebenen TourEdge
     * <edge> durch dieses TourVertex-Objekt verbunden ist.
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
        if( edge == _edge_left_hand )
            return _edge_right_hand;

        if( edge == _edge_right_hand )
            return _edge_left_hand;

        return null;
    } // getOtherEdge





    /**
     * @return  TourVertex, welcher bei einem LEFT_HAND-Umlauf
     *          ( vorwaerts mit linker Hand am zugehoerigen Knoten )
     *          als naechstes erreicht wird.
     */

    //============================================================
    public TourVertex getVertexLeftHand()
    //============================================================
    {
        return _edge_left_hand.getVertexLeftHand();
    } // getVertexLeftHand


    /**
     * @return  TourVertex, welcher bei einem RIGHT_HAND-Umlauf
     *          ( vorwaerts mit rechter Hand am zugehoerigen Knoten )
     *          als naechstes erreicht wird.
     */

    //============================================================
    public TourVertex getVertexRightHand()
    //============================================================
    {
        return _edge_right_hand.getVertexRightHand();
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
            return _edge_left_hand.getVertexLeftHand();
        else
            return _edge_right_hand.getVertexRightHand();
    } // getVertex


    /**
     * Ermitteln des TourVertex, der mit dem gegebenen TourVertex
     * <vertex> durch dieses TourVertex-Objekt verbunden ist.
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
        TourVertex vertex_left_hand  = _edge_left_hand.
                                       getVertexLeftHand();

        TourVertex vertex_right_hand = _edge_right_hand.
                                       getVertexRightHand();

        if( vertex == vertex_left_hand )
            return vertex_right_hand;

        if( vertex == vertex_right_hand )
            return vertex_left_hand;

        return null;
    } // getOtherVertex




    /**
     * @return  Segment, welches bei einem LEFT_HAND-Umlauf
     *          ( vorwaerts mit linker Hand am zugehoerigen Knoten )
     *          als naechstes beruehrt wird.
     */

    //============================================================
    public Segment getSegmentLeftHand()
    //============================================================
    {
        return _edge_left_hand.getSegment();
    } // getSegmentLeftHand


    /**
     * @return  Segment, welches bei einem RIGHT_HAND-Umlauf
     *          ( vorwaerts mit rechter Hand am zugehoerigen Knoten )
     *          als naechstes beruehrt wird.
     */

    //============================================================
    public Segment getSegmentRightHand()
    //============================================================
    {
        return _edge_right_hand.getSegment();
    } // getSegmentRightHand


    /**
     * Ermitteln des Segments, welche bei einem durch <dir> gegebenen
     * Umlauf als naechstes beruehrt wird.
     *
     * @param dir  LEFT_HAND oder RIGHT_HAND
     *
     * @return     Segment, welches bei einem <dir>-Umlauf als
     *             naechstes beruehrt wird.
     */

    //============================================================
    public Segment getSegment( boolean dir )
    //============================================================
    {
        if( dir == LEFT_HAND )
            return _edge_left_hand.getSegment();
        else
            return _edge_right_hand.getSegment();
    } // getSegment


    /**
     * Ermitteln des Segments, das mit dem gegebenen Segment
     * durch dieses TourVertex-Objekt verbunden ist.
     *
     * @param segment  SegmentLeftHand. oder SegmentRightHand
     *
     * @return  SegmentLeftHand,  falls edge == SegmentRightHand.
     *          SegmentRightHand, falls edge == SegmentLeftHand.
     *          null           sonst
     */

    //============================================================
    public Segment getOtherSegment( Segment segment )
    //============================================================
    {
        if( segment == _edge_left_hand.getSegment() )
            return _edge_right_hand.getSegment();

        if( segment == _edge_right_hand.getSegment() )
            return _edge_left_hand.getSegment();

        return null;
    } // getOtherSegment





    /**
     * @return  Knoten, welcher bei einem LEFT_HAND-Umlauf
     *          ( vorwaerts mit linker Hand am zugehoerigen Knoten )
     *          als naechstes beruehrt wird.
     */

    //============================================================
    public Node getNodeLeftHand()
    //============================================================
    {
        return _edge_left_hand.getVertexLeftHand().getNode();
    } // getNodeLeftHand


    /**
     * @return  Knoten, welcher bei einem RIGHT_HAND-Umlauf
     *          ( vorwaerts mit rechter Hand am zugehoerigen Knoten )
     *          als naechstes beruehrt wird.
     */

    //============================================================
    public Node getNodeRightHand()
    //============================================================
    {
        return _edge_right_hand.getVertexRightHand().getNode();
    } // getNodeRightHand


    /**
     * Ermitteln des Knotens, welcher bei einem durch <dir> gegebenen
     * Umlauf als naechstes beruehrt wird.
     *
     * @param dir  LEFT_HAND oder RIGHT_HAND
     *
     * @return     Knoten, welchesrbei einem <dir>-Umlauf als
     *             naechstes beruehrt wird.
     */

    //============================================================
    public Node getNode( boolean dir )
    //============================================================
    {
        if( dir == LEFT_HAND )
            return _edge_left_hand.getVertexLeftHand().getNode();
        else
            return _edge_right_hand.getVertexRightHand().getNode();
    } // getSegment


    /**
     * Ermitteln des Knotens, der mit dem gegebenen Knoten
     * durch dieses TourVertex-Objekt verbunden ist.
     *
     * @param node  NodeLeftHand. oder NodeRightHand
     *
     * @return  NodeLeftHand,  falls edge == NodeRightHand.
     *          NodeRightHand, falls edge == NodeLeftHand.
     *          null           sonst
     */

    //============================================================
    public Node getOtherNode( Node node )
    //============================================================
    {
        if( node == _edge_left_hand.getVertexLeftHand().getNode() )
            return _edge_right_hand.getVertexRightHand().getNode();

        if( node == _edge_right_hand.getVertexRightHand().getNode() )
            return _edge_left_hand.getVertexLeftHand().getNode();

        return null;
    } // getOtherNode





    /**
     */

    //============================================================
    public TourVertex getVertexClockwise()
    //============================================================
    {
        TourEdge edge = _edge_left_hand.getOtherSegmentEdge();
        return edge.getVertexLeftHand();
    } // getVertexClockwise


    /**
     */

    //============================================================
    public TourVertex getVertexCounterclockwise()
    //============================================================
    {
        TourEdge edge = _edge_right_hand.getOtherSegmentEdge();
        return edge.getVertexRightHand();
    } // getVertexCounterclockwise


    /**
     */

    //============================================================
    public TourVertex getNextNodeVertex( boolean dir )
    //============================================================
    {
        if( dir == CLOCKWISE )
            return getVertexClockwise();
        else
            return getVertexCounterclockwise();
    } // getNextNodeVertex


    /**
     */

    //============================================================
    public TourVertex getOtherNodeVertex( TourVertex vertex )
    //============================================================
    {
        TourVertex vertex_clockwise = getVertexClockwise();

        TourVertex vertex_counter_c = getVertexCounterclockwise();

        if( vertex == vertex_clockwise )
            return vertex_counter_c;

        if( vertex == vertex_counter_c )
            return vertex_clockwise;

        return null;
    } // getOtherNodeVertex





    /**
     * Ermitteln des Winkels ( im Bogenmass ), der von den TourEdges
     * EdgeLeftHand und EdgeRightHand in Richtung hin zum zug.
     * Knoten eingeschlossen wird.
     *
     * Es gilt:  ( 0 <= AngleInside < 2*PI )
     */

    //============================================================
    public double getAngleInside()
    //============================================================
    {
        RayDirection dir_r = _edge_right_hand.getDirectionFrom( this );
        RayDirection dir_l = _edge_left_hand.getDirectionFrom( this );

        return dir_l.getEnclosedAngle( dir_r );
    } // getAngleInside


    /**
     * Ermitteln des Winkels ( im Bogenmass ), der von den TourEdges
     * EdgeLeftHand und EdgeRightHand in Richtung weg vom zug.
     * Knoten eingeschlossen wird.
     *
     * Es gilt:  ( 0 < AngleOutside <= 2*PI ) !!!
     * sowie     ( AngleInside + AngleOutside ) == ( Math.PI * 2 )
     */

    //============================================================
    public double getAngleOutside()
    //============================================================
    {
        RayDirection dir_r = _edge_right_hand.getDirectionFrom( this );
        RayDirection dir_l = _edge_left_hand.getDirectionFrom( this );

        return dir_l.getExcludedAngle( dir_r );
    } // getAngleOutside


    /**
     * Ermitteln des Winkels ( im Bogenmass ), der von den TourEdges
     * EdgeLeftHand und EdgeRightHand auf der durch <side> angegebenen
     * Seite eingeschlossen wird.
     *
     * @param side  INSIDE oder OUTSIDE
     *
     * @return  AngleInside,  falls side == INSIDE.
     *          AngleOutside, falls side == OUTSIDE
     */

    //============================================================
    public double getAngle( boolean side )
    //============================================================
    {
        return ( side == INSIDE ) ?
               getAngleInside() :
               getAngleOutside() ;
    } // getAngle



    /**
     * Ermitteln der Richtung der Winkelhalbierenden von AngleInside
     */

    //============================================================
    public RayDirection getBisectorInside()
    //============================================================
    {
        RayDirection dir_r = _edge_right_hand.getDirectionFrom( this );
        RayDirection dir_l = _edge_left_hand.getDirectionFrom( this );

        return dir_l.getBisectorEnclosed( dir_r );
    } // getBisectorInside


    /**
     * Ermitteln der Richtung der Winkelhalbierenden von AngleOutside
     */

    //============================================================
    public RayDirection getBisectorOutside()
    //============================================================
    {
        RayDirection dir_r = _edge_right_hand.getDirectionFrom( this );
        RayDirection dir_l = _edge_left_hand.getDirectionFrom( this );

        return dir_l.getBisectorExcluded( dir_r );
    } // getBisectorOutside


    /**
     * Ermitteln der Richtung der Winkelhalbierenden
     * von AngleInside oder AngleOutside
     *
     * @param side   INSIDE oder OUTSIDE
     *
     * @return  BisectorInside,  falls side == INSIDE.
     *          BisectorOutside, falls side == OUTSIDE
     */

    //============================================================
    public RayDirection getBisector( boolean side )
    //============================================================
    {
        return ( side == INSIDE ) ?
               getBisectorInside() :
               getBisectorOutside() ;
    } // getBisector



    //************************************************************
    // protected class methods
    //************************************************************



    /**
     */

    //============================================================
    static protected void updateConnections( TourEdge edge_1,
                                             TourEdge edge_2 )
    //============================================================
    {
        TourVertex vertex_1 = edge_1.getVertexLeftHand();
        TourVertex vertex_2 = edge_2.getVertexLeftHand();

        vertex_1._connectToEdge( RIGHT_HAND, edge_2 );
        vertex_2._connectToEdge( RIGHT_HAND, edge_1 );
    } // updateConnections



    //************************************************************
    // protected methods
    //************************************************************


    /**
     * Entfernen dieses Vertex aus der Szene
     */

    //============================================================
    protected void remove()
    //============================================================
    {
        _scene = null; // Vertex als nicht mehr zur Szene gehoerend
                       // markieren
    } // remove


    //************************************************************
    // private methods
    //************************************************************


    /**
     */

    //============================================================
    private void _connectToEdge( boolean  dir,
                                 TourEdge edge )
    //============================================================
    {
        if( dir == LEFT_HAND )
            _edge_left_hand  = edge;
        else
            _edge_right_hand = edge;

        edge.setVertex( ! dir, this );
    } // _connectToEdge

} // TourVertex
