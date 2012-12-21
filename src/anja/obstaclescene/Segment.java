package anja.obstaclescene;

//import java.awt.Rectangle;

import anja.geom.Segment2;

import anja.util.SimpleList;
import anja.util.ListItem;
import anja.util.TreeItem;

/**
 * Ein Objekt der Klasse Segment repraesentiert als Hindernis
 * gesehen eine "unendlich duenne Wand".
 * Als solche trennt sie immer zwei Seiten voneinander, die durch
 * Kanten ( @see TourEdge ) beschrieben werden.
 *
 * Die Endpunkte eines Segments sind durch zwei Knoten ( @see Node )
 * gegeben. Ueber diese kann das Segment mit anderen Segmenten verbunden
 * sein.
 *
 * @version 0.1 14.06.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class Segment
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




    //************************************************************
    // private variables
    //************************************************************


    private ObstacleScene _scene; // Szene, zu der das Segment gehoert


    // Als Knoten gegebene Endpunkte des Segments ueber die dieses
    // ggf. mit weiteren Segmenten verbunden ist.
    //
    private Node _node_1;
    private Node _node_2;


    // "Seiten" des Segments
    //
    private TourEdge _edge_left_hand;
                     // Segment-Seite, an der man sich befindet, wenn
                     // man sich mit der linken Hand am Segment
                     // vorwaerts vom Knoten Node1 zum Knoten Node2
                     // bewegt

    private TourEdge _edge_right_hand;
                     // Segment-Seite, an der man sich befindet, wenn
                     // man sich mit der rechten Hand am Segment
                     // vorwaerts vom Knoten Node1 zum Knoten Node2
                     // bewegt


    private double _length;  // Laenge des Segments

    private RayDirection _ray_dir_1;
                         // Richtung des Strahl ( _node_1 --> _node_2 )

    private RayDirection _ray_dir_2;
                         // Richtung des Strahl ( _node_2 --> _node_1 )


    private TreeItem _item_node_1; // TreeItem mit dem dieses Segment
                                   // in der Segmentmenge des Knotens
                                   // _node_1 eingetragen ist.
    private TreeItem _item_node_2; // ... entsprechend fuer _node_2



    //************************************************************
    // protected variables
    //************************************************************


    //------------------------------------------------------------
    // Variablen, die ausschliesslich von der Szene benutzt werden
    //------------------------------------------------------------

    protected ListItem item_segments;
                       // ListItem mit dem dieses Segment in der Liste
                       // _segments der Szene eingetragen ist


    //------------------------------------------------------------
    // Variablen, die ausschliesslich vom QuadTree der Szene benutzt
    // werden
    //------------------------------------------------------------

    protected SimpleList listitems_quadtree;
                         // ListItems mit dem dieses Segment in den
                         // Squares des QuadTree's der Szene eingetragen
                         // ist.

    protected SimpleList treeitems_quadtree;
                         // TreeItems mit dem dieses Segment in den
                         // Squares des QuadTree's der Szene eingetragen
                         // ist.


    protected boolean is_marked = false;
                // Diese Markierung wird ggf. vom QuadTree bei einer
                // Segment-Such-Anfrage benutzt, um das Segment
                // als bereits geprueft zu markieren.
                // Eine gesetzte Markierung wird nach der Suche vom
                // QuadTree wieder zurueckgesetzt.


    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen Segments als Verbindung zwischen Knoten
     * node_1 und Knoten node_2.
     *
     * @param scene   Szene, in der die Knoten node_1 und node_2
     *                durch ein neues Segment zu verbinden sind
     *
     * Es wird davon ausgegangen, dass es sich bei den gegebenen Knoten
     * um existierende Knoten der Szene handelt und dass das Setzen
     * eines neuen Segments zwischen ihnen erlaubt ist
     * ( Segmente schneiden sich nur in gemeinsamen Knoten )
     */

    //============================================================
    protected Segment( ObstacleScene scene,
                       Node          node_1,
                       Node          node_2 )
    //============================================================
    {
        // Szene und Knoten speichern
        //
        _scene  = scene;
        _node_1 = node_1;
        _node_2 = node_2;


        // Laenge und Richtungen des Segments ermitteln und setzen
        // ( @see _length, _ray_dir_1 und _ray_dir_2 )

        int x1 = _node_1.getX();
        int y1 = _node_1.getY();
        int x2 = _node_2.getX();
        int y2 = _node_2.getY();

        _length = Calculator.distance( x1, y1, x2, y2 );

        _ray_dir_1 = new RayDirection( x1, y1, x2, y2 );
        _ray_dir_2 = new RayDirection( x2, y2, x1, y1 );


        TourPolygon poly = new TourPolygon( this );

        _edge_left_hand  = poly.getFirstEdge();
        _edge_right_hand = _edge_left_hand.getEdgeRightHand();


        // Neues Segment jeweils den Segmentmengen der Knoten
        // _node_1 und _node_2 zufuegen und die dabei erzeugten
        // TreeItems speichern
        //
        _item_node_1 = _node_1.connectToSegment( this );
        _item_node_2 = _node_2.connectToSegment( this );

    } // Segment




    //************************************************************
    // public methods
    //************************************************************


    /**
     * Ermittlung der Szene in der sich dieses Segment befindet.
     *
     * Falls das Segment bereits aus der Szene entfernt wurde, fuer
     * die er urspruengich erzeugt wurde, so wird <null> zurueckgegeben.
     *
     * @return  Szene in der sich dieses Segment befindet.
     */

    //============================================================
    public ObstacleScene getScene()
    //============================================================
    {
        return _scene;
    } // getScene




    //------------------------------------------------------------
    // Ermittlung von Laenge und Koordinaten dieses Segments
    //------------------------------------------------------------


    /**
     * @return  Laenge dieses Segments
     */

    //============================================================
    public double getLength()
    //============================================================
    {
        return _length;
    } // getLength


    /**
     * @return  Koordinaten dieses Segments als Segment2-Objekt
     *          ( @see anja.geom.Segment2 )
     */

    //============================================================
    public Segment2 getSegment2()
    //============================================================
    {
        return new Segment2( _node_1.getX(), _node_1.getY(),
                             _node_2.getX(), _node_2.getY() );
    } // getSegment2




    //------------------------------------------------------------
    // Ermittlung von Strahlenrichtungen und Drehwinkeln bezueglich
    // dieses Segments
    //------------------------------------------------------------


    /**
     * Ermitteln der Richtung des Strahls, der vom Knoten <node> zum
     * anderen Knoten dieses Segments zeigt
     *
     * @return  Die ermittelte Richtung
     *          oder <null>, falls der Knoten <node> gar kein Knoten
     *          dieses Segments ist
     */

    //============================================================
    public RayDirection getDirectionFrom( Node node )
    //============================================================
    {
        if( node == _node_1 )
            return _ray_dir_1;

        if( node == _node_2 )
            return _ray_dir_2;

        return null;
    } // getDirectionFrom


    /**
     * Ermitteln der Strahlenrichtung die sich ergibt, wenn man den
     * Strahl vom Segment-Knoten <node> zum anderen Segment-Knoten
     * in der Drehrichtung <turn_dir> um einen Viertelkreis dreht.
     *
     * @return  Die ermittelte Richtung
     *          oder <null>, falls der Knoten <node> gar kein Knoten
     *          dieses Segments ist
     */

    //============================================================
    public RayDirection getPerpendicular( Node    node,
                                          boolean turn_dir )
    //============================================================
    {
        if( node == _node_1 )
            return _ray_dir_1.getPerpendicular( turn_dir );

        if( node == _node_2 )
            return _ray_dir_2.getPerpendicular( turn_dir );

        return null;
    } // getPerpendicular


    /**
     * Ermitteln des Drehwinkels den man durchlaeuft, wenn man vom
     * Punkt ( <x>, <y> ) auf den Segment-Knoten <node> ausgerichtet
     * ist und sich mit Blick auf das Segment zum anderen Knoten des
     * Segments dreht.
     *
     * @return  Der ermittelte Drehwinkel
     *          oder <null>, falls der Knoten <node> gar kein Knoten
     *          dieses Segments ist oder das Koordinatenpaar ( <x>,<y> )
     *          mit den Koordinaten eines der beiden Segment-Knoten
     *          identisch ist
     */

    //============================================================
    public TurnAngle getTurnAngle( int  x,
                                   int  y,
                                   Node node )
    //============================================================
    {
        RayDirection dir_1 = _node_1.getDirectionFrom( x, y );
        RayDirection dir_2 = _node_2.getDirectionFrom( x, y );

        if( dir_1 == null || dir_2 == null )
            return null; // Das Koordinatenpaar ( x,y ) stimmt mit
                         // den Koordinaten eines der beiden
                         // Segmentknoten ueberein

        if( node == _node_1 )
            return new TurnAngle( dir_1, dir_2 );

        if( node == _node_2 )
            return new TurnAngle( dir_2, dir_1 );

        return null;
    } // getTurnAngle




    //------------------------------------------------------------
    // Ermittlung der Knoten mit denen dieses Segment verbunden ist
    //------------------------------------------------------------


    /**
     * @return  Erster Knoten ( Node1 ) dieses Segments
     */

    //============================================================
    public Node getNode1()
    //============================================================
    {
        return _node_1;
    } // getNode1


    /**
     * @return  Zweiter Knoten ( Node2 ) dieses Segments
     */

    //============================================================
    public Node getNode2()
    //============================================================
    {
        return _node_2;
    } // getNode2


    /**
     * Ermitteln des Knotens der durch dieses Segment mit dem gegebenen
     * Knoten verbunden ist.
     *
     * @param node   Knoten Node1 oder Knoten Node2
     *
     * @return       Node1, falls  ( node == Node2 )
     *               Node2, falls  ( node == Node1 )
     *               <null> sonst
     */

    //============================================================
    public Node getOtherNode( Node node )
    //============================================================
    {
        if( node == _node_1 )
            return _node_2;

        if( node == _node_2 )
            return _node_1;

        return null;
    } // getOtherNode




    //------------------------------------------------------------
    // Ermittlung der als TourEdges gegebenen Seiten dieses Segments
    //------------------------------------------------------------


    /**
     * @return  Als TourEdge gegebene Segment-Seite, an der man sich
     *          befindet, wenn man sich mit der linken Hand am Segment
     *          vorwaerts vom Knoten Node1 zum Knoten Node2 bewegt.
     */

    //============================================================
    public TourEdge getEdgeLeftHand()
    //============================================================
    {
        return _edge_left_hand;
    } // getEdgeLeftHand


    /**
     * @return  Als TourEdge gegebene Segment-Seite, an der man sich
     *          befindet, wenn man sich mit der rechten Hand am Segment
     *          vorwaerts vom Knoten Node1 zum Knoten Node2 bewegt.
     */

    //============================================================
    public TourEdge getEdgeRightHand()
    //============================================================
    {
        return _edge_right_hand;
    } // getEdgeRightHand


    /**
     * Ermitteln einer der beiden durch TourEdge-Objekte gegebenen
     * Seiten dieses Segments
     *
     * @param hand  LEFT_HAND oder RIGHT_HAND
     *
     * @return  Als TourEdge gegebene Segment-Seite an der man sich
     *          befindet, wenn man sich mit der durch <hand> gegebenen
     *          Hand am Segment vorwaerts vom Segment-Knoten <node> zum
     *          anderen Knoten des Segments bewegt
     *          oder <null>, falls der gegebene Knoten gar nicht mit
     *          diesem Segment verbunden ist
     */

    //============================================================
    public TourEdge getEdge( Node node, boolean hand )
    //============================================================
    {
        if( node == _node_1 )
            return ( hand == LEFT_HAND ) ?
                   _edge_left_hand :
                   _edge_right_hand;

        if( node == _node_2 )
            return ( hand == LEFT_HAND ) ?
                   _edge_right_hand :
                   _edge_left_hand;

        return null;
    } // getEdge


    /**
     * Die als TourEdge gegebene Segment-Seite, die einem zugewandt ist,
     * wenn man sich am Punkt ( <x>,<y> ) befindet.
     *
     * Falls der Punkt ( <x>,<y> ) mit Node1 und Node2 auf einer
     * gemeinsamen Geraden liegen koennte, wird null zurueckgegeben.
     *
     * @return  Ermittelt Segmentseite
     *          oder <null>, falls sich diese nicht eindeutig bestimmen
     *          laesst.
     */

    //============================================================
    public TourEdge getEdge( int x, int y )
    //============================================================
    {
        int x1 = _node_1.getX();
        int y1 = _node_1.getY();
        int x2 = _node_2.getX();
        int y2 = _node_2.getY();


        int ori = Calculator.orientation( x, y, x1, y1, x2, y2 );

        if( ori == Calculator.COLLINEAR )
            return null; // Es kann nicht ausgeschlossen werden, dass
                         // der Punkt ( x, y ) mit den beiden Segment-
                         // knoten auf einer gemeinsamen Geraden liegt

        return ( ori == Calculator.LEFT ) ?
               _edge_right_hand :
               _edge_left_hand;
               //
               // Das ist kein Fehler !!!
               // _edge_right_hand liegt auf der linken Halbebene
               // der Strecke ( Node1 --> Node2 )
    } // getEdge


    /**
     * Ermitteln des TourEdge-Objekts, das auf der anderen Seite des
     * Segments als das TourEdge-Objekt <edge> liegt
     *
     * @param edge   EdgeLeftHand oder EdgeRightHand
     *
     * @return       EdgeLeftHand,  falls  ( <edge> == EdgeRightHand )
     *               EdgeRightHand, falls  ( <edge> == EdgeLeftHand )
     *               <null> sonst
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




    //------------------------------------------------------------
    // Ermittlung der mit den TourEdges dieses Segments verbundenen
    // TourVertices
    //------------------------------------------------------------


    /**
     * Ermitteln des TourVertex an dem man sich befindet, wenn man sich
     * mit der durch <hand> gegebenen Hand am Segment-Knoten <node>
     * festhaelt und dabei in Richtung des anderen Segment-Knotens
     * blickt
     *
     * @param hand  LEFT_HAND oder RIGHT_HAND
     *
     * @return  Der ermittelte TourVertex
     *          oder <null>, falls der Knoten <node> gar kein Knoten
     *          dieses Segments ist
     */

    //============================================================
    public TourVertex getVertex( Node node, boolean hand )
    //============================================================
    {
        if( node == _node_1 )
            return ( hand == LEFT_HAND ) ?
                   _edge_left_hand.getVertexRightHand() :
                   _edge_right_hand.getVertexLeftHand();

        if( node == _node_2 )
            return ( hand == LEFT_HAND ) ?
                   _edge_right_hand.getVertexRightHand() :
                   _edge_left_hand.getVertexLeftHand();

        return null;
    } // getVertex



    //------------------------------------------------------------
    // Diverse andere Methoden
    //------------------------------------------------------------



    //************************************************************
    // public class methods
    //************************************************************


    /**
     * @return  Array aller Segmente der Liste <list>.
     *          Es wird davon ausgegangen, dass es sich bei den in der
     *          Liste enthaltenen Objekten um Segmente handelt.
     */

    //============================================================
    static public Segment[] toArray( SimpleList list)
    //============================================================
    {
        if( list == null )
            return new Segment[ 0 ];

        Segment[] segments = new Segment[ list.length() ];

        int index = 0;
        for( ListItem item = list.first();
                      item != null;
                      item = item.next()
            )
            segments[ index ++ ] = ( Segment )item.value();

        return segments;
    } // toArray




    //************************************************************
    // protected methods
    //************************************************************


    /**
     * @return  TreeItem mit dem dieses Segment in der Segmentmenge
     *          des Knotens <node> eingetragen ist
     *          oder <null>, falls der Knoten <node> gar kein Knoten
     *          dieses Segments ist
     */

    //============================================================
    protected TreeItem getTreeItem( Node node )
    //============================================================
    {
        if( node == _node_1 )
            return _item_node_1;

        if( node == _node_2 )
            return _item_node_2;

        return null;
    } // getTreeItem


    /**
     * Entfernen dieses Segments aus der Szene
     */

    //============================================================
    protected void remove()
    //============================================================
    {
        _node_1.disconnectFromSegment( _item_node_1 );
        _node_2.disconnectFromSegment( _item_node_2 );

        _scene = null;
    } // remove




    //************************************************************
    // private methods
    //************************************************************

} // Segment
