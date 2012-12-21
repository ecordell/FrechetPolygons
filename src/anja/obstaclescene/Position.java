package anja.obstaclescene;

import java.awt.Point;
import anja.geom.Point2;

/**
 * Ein Objekt der Klasse Position beschreibt eine Position in einer
 * Hindernis-Szene ( @see ObstacleScene ).
 *
 * Zusaetzlich zu den Positionskoordinaten gehoeren zur vollstaendigen
 * bzw. eindeutigen Beschreibung einer Position auch Informationen ueber
 * damit verbundene Objekte ( TourVertex, TourEdge u.s.w ).
 *
 * @version 0.1 21.06.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class Position
//****************************************************************
{
    //************************************************************
    // public constants
    //************************************************************


    // Typ der Position

    public final static int SPACE       = 0; // "freier Raum"
                                             // Positionskoordinaten
                                             // sind also weder die
                                             // Koordinaten eines Knotens
                                             // noch liegen sie auf einem
                                             // Segment.

    public final static int VERTEX      = 1; // Eckpunkt eines
                                             // Umlaufpolygons

    public final static int EDGE        = 2; // Kante eines Umlaufpolygons

    public final static int SINGLE_NODE = 3; // Alleinstehender Knoten
                                             // ( ist kein Endpunkt eines
                                             //   Segments )


    //************************************************************
    // private variables
    //************************************************************

    private ObstacleScene _scene; // Szene, zu der diese Position
                                  // gehoert

    private int _valid_code; // Code zum ueberpruefen, ob sich die
                             // Szene seit dem Erzeugen der Position
                             // veraendert hat

    private int _type;  // SPACE, VERTEX, u.s.w.

    private int _x; // Positions-Koordinaten
    private int _y;


    private TourVertex _vertex; // Vertex, falls _type == VERTEX

    private TourEdge   _edge;   // Edge, falls _type == EDGE

    private Node   _node;       // Node, falls _type == SINGLE_NODE



    //************************************************************
    // constructors
    //
    // Alle Konstruktoren sind protected, da ein gueltiges
    // Position-Objekt nur von ObstacleScene erzeugt werden kann.
    //
    //************************************************************


    /**
     * Erzeugen einer Position des Typs SPACE.
     */

    //============================================================
    protected Position( ObstacleScene scene,
                        int           x,
                        int           y )
    //============================================================
    {
        _scene = scene;

        _valid_code = _scene.getValidCode();

        _type  = SPACE;
        _x = x;
        _y = y;
    } // Position


    /**
     * Erzeugen einer Position des Typs VERTEX.
     */

    //============================================================
    protected Position( TourVertex vertex )
    //============================================================
    {
        _scene = vertex.getScene();

        _valid_code = _scene.getValidCode();

        _type   = VERTEX;
        _vertex = vertex;

        _x = _vertex.getX();
        _y = _vertex.getY();
    } // Position


    /**
     * Erzeugen einer Position des Typs EDGE.
     */

    //============================================================
    protected Position( TourEdge edge,
                        int x,
                        int y )
    //============================================================
    {
        _scene = edge.getScene();

        _valid_code = _scene.getValidCode();

        _type  = EDGE;
        _edge  = edge;

        _x = x;
        _y = y;
    } // Position


    /**
     * Erzeugen einer Position des Typs SINGLE_NODE.
     */

    //============================================================
    protected Position( Node node )
    //============================================================
    {
        _scene = node.getScene();

        _valid_code = _scene.getValidCode();

        _type = SINGLE_NODE;
        _node = node;

        _x = _node.getX();
        _y = _node.getY();
    } // Position




    //************************************************************
    // public methods
    //************************************************************



    /**
     * Ermitteln des Typs der Position
     *
     * @return SPACE, VERTEX u.s.w.
     */

    //============================================================
    public int getType()
    //============================================================
    {
        return _type;
    } // getType



    /**
     * Ermitteln der Positionskoordinaten
     */

    //============================================================
    public Point getCoors()
    //============================================================
    {
        return new Point( _x, _y );
    } // getCoors


    /**
     * Ermitteln der Positionskoordinaten als Point2-Objekt
     * ( @see anja.geom.Point2 )
     */

    //============================================================
    public Point2 getPoint2()
    //============================================================
    {
        return new Point2( _x, _y );
    } // getCoors


    /**
     * Ermitteln der x-Koordinate
     */

    //============================================================
    public int getX()
    //============================================================
    {
        return _x;
    } // getX


    /**
     * Ermitteln der y-Koordinate
     */

    //============================================================
    public int getY()
    //============================================================
    {
        return _y;
    } // getX



    /**
     * Ermitteln des durch die Position ausgewaehlten Eckpunktes
     * eines Umlaufpolygons
     *
     * @return Eckpunkt, falls Type == VERTEX,
     *         ansonsten null
     */

    //============================================================
    public TourVertex getVertex()
    //============================================================
    {
        return _vertex;
    } // getVertex



    /**
     * Ermitteln der durch die Position ausgewaehlte Kante
     * eines Umlaufpolygons
     *
     * @return Kante, falls Type == EDGE,
     *         ansonsten null
     */

    //============================================================
    public TourEdge getEdge()
    //============================================================
    {
        return _edge;
    } // getEdge



    /**
     * Ermitteln des durch die Position ausgewaehlten Knotens
     *
     * @return Knoten, falls Type == VERTEX oder Type == SINGLE_NODE,
     *         ansonsten null
     */

    //============================================================
    public Node getNode()
    //============================================================
    {
        switch( _type )
        {
          case VERTEX:
            return _vertex.getNode();

          case SINGLE_NODE:
            return _node;

          default:
            return null;
        } // switch

    } // getNode



    /**
     * Ermitteln des durch die Position ausgewaehlten Segments
     *
     * @return Segment, falls Type == EDGE,
     *         ansonsten null
     */

    //============================================================
    public Segment getSegment()
    //============================================================
    {
        if( _type == EDGE )
            return _edge.getSegment();
        else
            return null;
    } // getSegment



    /**
     * Testen, ob die Position noch gueltig ist
     *
     * @return true  <==> Position ist noch gueltig
     *         false <==> Szene hat sich seit der Erzeugung dieser
     *                    Position veraendert
     */

    //============================================================
    public boolean isValid()
    //============================================================
    {
        return ( _valid_code == _scene.getValidCode() );
    } // isValid


    /**
     * Testen, ob die Position fuer die angegebene Szene noch gueltig
     * ist
     *
     * @return true  <==> Position ist noch gueltig
     *         false <==> Szene hat sich seit der Erzeugung dieser
     *                    Position veraendert
     */

    //============================================================
    public boolean isValid( ObstacleScene scene )
    //============================================================
    {
        return (    scene == _scene
                 && _valid_code == _scene.getValidCode() );
    } // isValid


} // Position
