package anja.obstaclescene;

import java.awt.Point;
import java.awt.Rectangle;

import anja.geom.Point2;

import anja.util.*;

/**
 * Ein Objekt der Klasse Node repraesentiert ein punktfoermiges Hindernis
 * und dient insbesondere als Verbindungs- oder Endpunkt von
 * Segmenten ( @see Segment ).
 *
 * @version 0.1 14.06.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class Node
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

    // Polygon-Typen
    //
    public final static boolean OBSTACLE = ObstacleScene.OBSTACLE;
    public final static boolean YARD     = ! OBSTACLE;



    //************************************************************
    // private variables
    //************************************************************


    private ObstacleScene _scene; // Szene, zu der der Knoten gehoert

    // Koordinaten dieses Knotens im Koordinatensystem der Szene
    private int _x;
    private int _y;

    private NodeSegments _segments;
                         // Menge der Segmente, die in diesem Knoten
                         // miteinander verbunden sind


    private boolean _is_moving;
                    // Dieses Flag ist eine
                    // Markierung fuer den Szenen-Editor und hat
                    // keinen Einfluss auf die Szene

    private boolean _is_found = false;
                // Diese Markierung wird ggf. von der Methode
                // getConnectedComponent() benutzt, um diesen Knoten
                // als bereits gefunden zu markieren.



    //************************************************************
    // protected variables
    //************************************************************


    //------------------------------------------------------------
    // Variablen, die ausschliesslich von der Szene benutzt werden
    //------------------------------------------------------------

    protected ListItem item_nodes;
                       // ListItem mit dem dieser Knoten in der Liste
                       // _nodes der Szene eingetragen ist

    protected TreeItem item_sorted_nodes_x;
                       // TreeItem mit dem dieser Knoten im Baum
                       // _sorted_nodes_x der Szene eingetragen ist

    protected TreeItem item_sorted_nodes_y;
                       // TreeItem mit dem dieser Knoten im Baum
                       // _sorted_nodes_y der Szene eingetragen ist




    //------------------------------------------------------------
    // Variablen, die ausschliesslich vom QuadTree der Szene benutzt
    // werden
    //------------------------------------------------------------

    protected SimpleList items_quadtree;
                         // ListItems mit dem dieser Knoten in den
                         // Squares des QuadTrees der Szene eingetragen
                         // ist



    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen nicht initialisierten Knotens.
     * Zur Initialisierung steht die Methode init(..) zur Verfuegung
     *
     * Dieser Konstruktor wird ausschliesslich von der Szene benutzt
     * und ist deshalb protected.
     */

    //============================================================
    protected Node()
    //============================================================
    {
    } // Node



    //************************************************************
    // public methods
    //************************************************************


    /**
     * Ermittlung der Szene in der sich dieser Knoten befindet.
     *
     * Falls der Knoten bereits aus der Szene entfernt wurde, fuer
     * die er urspruengich erzeugt wurde, so wird <null> zurueckgegeben.
     *
     * @return  Szene in der sich dieser Knoten befindet.
     */

    //============================================================
    public ObstacleScene getScene()
    //============================================================
    {
        return _scene;
    } // getScene


    /**
     * Testen, ob dieser Knoten ohne Segmentverbindungen ist.
     *
     * @return  true <==> Es ist kein Segment mit diesem Knoten
     *                    verbunden
     */

    //============================================================
    public boolean isSingle()
    //============================================================
    {
        return _segments.empty();
    } // isSingle


    /**
     * @return  Anzahl der in diesem Knoten miteinander verbundenen
     *          Segment
     */

    //============================================================
    public int getNoOfSegments()
    //============================================================
    {
        return _segments.length();
    } // getNoOfSegments




    //------------------------------------------------------------
    // Ermittlung der Koordinaten dieses Knotens
    //------------------------------------------------------------


    /**
     * @return  X-Koordinate dieses Knotens.
     */

    //============================================================
    public int getX()
    //============================================================
    {
        return _x;
    } // getX


    /**
     * @return  Y-Koordinate dieses Knotens.
     */

    //============================================================
    public int getY()
    //============================================================
    {
        return _y;
    } // getY


    /**
     * @return  Koordinaten dieses Knotens als Point2-Objekt
     *          ( @see anja.geom.Point2 )
     */

    //============================================================
    public Point2 getPoint2()
    //============================================================
    {
        return new Point2( _x, _y );
    } // getPoint2




    //------------------------------------------------------------
    // Ermittlung von Strahlenrichtungen und Drehwinkeln bezueglich
    // dieses Knotens
    //------------------------------------------------------------


    /**
     * @return  Richtung des Strahls, der von diesem Knoten zum Punkt
     *          ( <x>, <y> ) zeigt
     *          oder <null>, falls die angegebenen Koordinaten gleich
     *          den Koordinaten dieses Knotens sind.
     */

    //============================================================
    public RayDirection getDirectionTo( int x, int y )
    //============================================================
    {
        if( x == _x && y == _y )
            return null;

        return new RayDirection( _x, _y, x, y );
    } // getDirectionTo


    /**
     * @return  Richtung des Strahls, der vom Punkt ( <x>, <y> ) auf
     *          diesen Knoten zeigt
     *          oder <null>, falls die angegebenen Koordinaten gleich
     *          den Koordinaten dieses Knotens sind.
     */

    //============================================================
    public RayDirection getDirectionFrom( int x, int y )
    //============================================================
    {
        if( x == _x && y == _y )
            return null;

        return new RayDirection( x, y, _x, _y );
    } // getDirectionFrom


    /**
     * Ermittlung des Drehwinkels den man durchlaeuft, wenn man von
     * diesem Knoten zum Punkt ( <x_start>, <y_start> ) ausgerichtet ist
     * und sich mit der Drehrichtung <turn_dir> zum Punkt
     * ( <x_end>, <y_end> ) ausrichtet.
     *
     * @return  Der ermittelte Drehwinkel
     *          oder <null> falls eines der beiden Koordinatenpaare
     *          ( <x_start>, <y_start> ) oder ( <x_end>, <y_end> ) mit
     *          den Koordinaten dieses Knotens identisch ist
     */

    //============================================================
    public TurnAngle getTurnAngle( int     x_start,
                                   int     y_start,
                                   int     x_end,
                                   int     y_end,
                                   boolean turn_dir )
    //============================================================
    {
        RayDirection start = getDirectionTo( x_start, y_start );
        RayDirection end   = getDirectionTo( x_end, y_end );

        if( start == null || end == null )
            return null;

        return new TurnAngle( start, end, turn_dir );
    } // getTurnAngle




    //------------------------------------------------------------
    // Ermittlung von Segmenten, die in diesem Knoten miteinander
    // verbunden sind
    //------------------------------------------------------------


    /**
     * Ermitteln des Segments, welches diesen Knoten mit dem Knoten
     * <node> verbindet.
     *
     * @return  Segment, welches diesen Knoten mit Knoten <node>
     *          verbindet
     *          oder <null>, falls es kein solches Segment gibt
     */

    //============================================================
    public Segment getSegment( Node node )
    //============================================================
    {
        if(    node == null
            || _segments.empty()
            )
            return null;

        // Richtung des Strahls von diesem Knoten zum Knoten node
        // ermitteln
        //
        RayDirection raydir =
            new RayDirection( _x, _y, node._x, node._y );

        // Segment ermitteln, welches ausgehend von der Richtung raydir
        // im Uhrzeigersinn als naechstes angetroffen wird
        //
        Segment seg = getNextSegment( raydir, CLOCKWISE );

        if( seg.getOtherNode( this ) == node )
            return seg; // Das ermittelte Segment verbindet diesen
                        // Knoten mit dem Knoten node

        return null; // Es gibt kein Segment, dass diesen Knoten mit
                     // dem Knoten node verbindet
    } // getSegment


    /**
     * Ermitteln des mit diesem Knoten verbundenen Segments, welches in
     * Drehrichtung <turn_dir> mit dem Segment <segment> benachbart ist.
     *
     * Wenn das Segment <segment> das einzige Segment dieses Knotens
     * ist, dann ist es in beide Richtungen sein eigener Nachbar
     *
     * @param segment   Segment, dessen Nachbarsegment zu ermitteln ist
     *
     * @param turn_dir  CLOCKWISE oder COUNTERCLOCKWISE
     *
     * @return  Segment, welches in Drehrichtung <turn_dir> mit dem
     *          Segment <segment> benachbart ist
     *          oder <null>, falls das Segment <segment> gar nicht mit
     *          diesem Knoten verbunden ist
     */

    //============================================================
    public Segment getNextSegment( Segment segment,
                                   boolean turn_dir )
    //============================================================
    {
        if( segment == null )
            return null;

        boolean hand = ( turn_dir == CLOCKWISE ) ?
                       LEFT_HAND :
                       RIGHT_HAND;

        TourVertex vertex = segment.getVertex( this, hand );

        if( vertex == null )
            return null; // Das Segment segment ist gar nicht mit
                         // diesem Knoten verbunden

        return vertex.getSegment( ! hand );
    } // getNextSegment


    /**
     * Ermitteln des mit diesem Knoten verbundenen Segments, welches
     * als erstes angetroffen wird, wenn ab der Strahlenrichtung
     * <ray_dir> in Drehrichtung <turn_dir> danach gesucht wird.
     *
     * @param ray_dir   Strahlenrichtung, ab der nach einem Segment
     *                  gesucht wird
     *
     * @param turn_dir  CLOCKWISE oder COUNTERCLOCKWISE
     *
     * @return  gefundenes Segment
     *          oder <null>, falls dieser Knoten gar nicht mit
     *          Segmenten verbunden ist
     */

    //============================================================
    public Segment getNextSegment( RayDirection ray_dir,
                                   boolean      turn_dir )
    //============================================================
    {
        return _segments.getNextSegment( ray_dir, turn_dir );
    } // getNextSegment


    /**
     * @return  das erste im Baum eingetragene Segment des Knotens
     */

    //============================================================
    public Segment getFirstSegment()
    //============================================================
    {
        TreeItem item = _segments.getFirst();

        if( item == null )
            return null;

        return ( Segment )item.value();
    } // getFirstSegment


    /**
     * @return  das naechste im Baum eingetragene Segment des Knotens
     *          oder <null>, falls kein weiteres Segment vorhanden ist
     */

    //============================================================
    public Segment getNextSegment( Segment segment )
    //============================================================
    {
        if( segment == null )
            return null;

        TreeItem item = segment.getTreeItem( this );

        if( item == null )
            return null;

        item = ( ( LinkedTreeItem )item ).getNext();

        if( item == null )
            return null;

        return ( Segment )item.value();
    } // getNextSegment


    /**
     * @return  Array der Segmente, die in diesem Knoten miteinander
     *          verbundenen sind.
     *          Das Array enthaelt die Segmente nach ihren Richtungen
     *          geordnet ( also von der Richtung der positiven x-Achse
     *          ausgehend gegen den Uhrzeigersinn )
     */

    //============================================================
    public Segment[] getSegments()
    //============================================================
    {
        return _segments.getSegments();
    } // getSegments


    /**
     * Ermitteln der mit diesem Knoten verbundenene Segmente, die
     * in der Drehrichtung <turn_dir> vom Segment <seg_first> bis zum
     * Segment <seg_last> aufeinander folgen.
     *
     * @param  seg_first  erstes Segment des zu liefernden Arrays
     *
     * @param  seg_last   letztes Segment des zu liefernden Arrays
     *                    oder null, falls alle Segmente ab Segment
     *                    <seg_first> geliefert werden sollen
     *
     * @param  turn_dir   CLOCKWISE oder COUNTERCLOCKWISE
     *
     * @return  Array der von <seg_first> bis <seg_last> in
     *          Drehrichtung <turn_dir> geordneten Segmente
     *          oder <null>, falls <seg_first oder <seg_last> gar nicht
     *          mit diesem Knoten verbunden ist
     */

    //============================================================
    public Segment[] getSegments( Segment seg_first,
                                  Segment seg_last,
                                  boolean turn_dir )
    //============================================================
    {
        if( seg_first == null )
            return null;

        TreeItem item_first = seg_first.getTreeItem( this );

        if( item_first == null )
            return null; // seg_first ist gar nicht mit diesem Knoten
                         // verbunden


        TreeItem item_last;

        // Falls seg_last gleich null ist, wird als letztes Segment
        // das vorhergehende des ersten Segments genommen. Dadurch
        // werden alle Segmente dieses Knotens ab seg_first geliefert

        if( seg_last == null )
            item_last = _segments.getNextItem( item_first, ! turn_dir );
        else
        {
            item_last = seg_last.getTreeItem( this );
            if( item_last == null )
                return null; // seg_last ist gar nicht mit diesem
                             // Knoten verbunden
        } // else


        // Groesse des benoetigten Arrays ermitteln
        //
        int length = 1;
        for( TreeItem item = item_first;
                      item != item_last;
                      item = _segments.getNextItem( item, turn_dir ) )
            length++;


        // Array erzeugen und mit den Segmenten von seg_first bis
        // seg_last fuellen

        Segment[] segments = new Segment[ length ];

        TreeItem item = item_first;
        for( int i = 0; i < length; i++ )
        {
            segments[ i ] = ( Segment )( item.value() );
            item = _segments.getNextItem( item, turn_dir );
        } // for


        return segments;
    } // getSegments


    /**
     * Ermitteln der mit diesem Knoten verbundenen Segmente, so wie
     * sie der Reihe nach im Drehwinkel <turn> angetroffen werden
     *
     * @return  Array der ermittelten Segmente
     */

    //============================================================
    public Segment[] getSegments( TurnAngle turn )
    //============================================================
    {
        if(    turn == null
            || _segments.empty() )
            return new Segment[ 0 ]; // Es gibt gar keine Segmente, die
                                     // mit diesem Knoten verbunden sind

        boolean turn_dir = turn.getTurnDirection();

        // Erstes Segment bestimmen
        //
        Segment seg_first =
            _segments.getNextSegment( turn.getStartDirection(),
                                      turn_dir );


        // Falls sich das gefundene Segment nicht innerhalb des
        // Drehwinkels turn befindet, existiert gar kein solches Segment

        if( ! turn.isInside( seg_first.getDirectionFrom( this ) ) )
            return new Segment[ 0 ];


        // Letztes Segment bestimmen
        //
        Segment seg_last = null;

        if( turn.getNoOfRevolutions() == 0 )
            seg_last = _segments.getNextSegment( turn.getEndDirection(),
                                                 ! turn_dir );

        return getSegments( seg_first, seg_last, turn_dir );
    } // getSegments




    //------------------------------------------------------------
    // Ermittlung von Knoten, die mit diesem Knoten durch Segmente
    // verbunden sind
    //------------------------------------------------------------


    /**
     * @return  Knoten, der durch das angegebene Segment mit diesem
     *          Knoten verbunden ist
     *          oder <null>, falls das gegebene Segment gar nicht mit
     *          diesem Knoten verbunden ist
     */

    //============================================================
    public Node getNode( Segment segment )
    //============================================================
    {
        if( segment == null )
            return null;

        return segment.getOtherNode( this );
    } // getNode


    /**
     * Ermitteln des mit diesem Knoten verbundenen Knotens, welcher in
     * Drehrichtung <turn_dir> mit dem Knoten <node> benachbart ist.
     *
     * Wenn der Knoten <node> der einzige ist, mit dem dieser Knoten
     * verbunden ist dann ist er in beide Richtungen sein eigener Nachbar
     *
     * @param node     Knoten, dessen Nachbarknoten zu ermitteln ist
     *
     * @param turn_dir CLOCKWISE oder COUNTERCLOCKWISE
     *
     * @return  Knoten, welcher in Richtung <turn_dir> mit Knoten <node>
     *          benachbart ist
     *          oder <null>, falls der Knoten <node> gar nicht mit
     *          diesem Knoten durch ein Segment verbunden ist
     */

    //============================================================
    public Node getNextNode( Node    node,
                             boolean turn_dir )
    //============================================================
    {
        if( node == null )
            return null;

        // Segment ermitteln, welches diesen Knoten mit dem Knoten
        // node verbindet
        //
        Segment seg = getSegment( node );

        if( seg == null )
            return null; // Es gibt kein solches Segment


        // Naechstes Segment in Richtung turn_dir ermitteln
        //
        seg = getNextSegment( seg, turn_dir );

        // Segment-Knoten zurueckgeben, der nicht mit diesem Knoten
        // uebereinstimmt
        //
        return seg.getOtherNode( this );
    } // getNextNode


    /**
     * Ermitteln des mit diesem Knoten verbundenen Knotens, welcher
     * als erstes angetroffen wird, wenn ab der Strahlenrichtung
     * <ray_dir> in Drehrichtung <turn_dir> danach gesucht wird.
     *
     * @param ray_dir   Strahlenrichtung, ab der nach einem verbundenen
     *                  Knoten gesucht wird
     *
     * @param turn_dir  CLOCKWISE oder COUNTERCLOCKWISE
     *
     * @return  gefundener Knoten
     *          oder <null>, falls dieser Knoten gar nicht mit
     *          anderen Knoten verbunden ist
     */

    //============================================================
    public Node getNextNode( RayDirection ray_dir,
                             boolean      turn_dir )
    //============================================================
    {
        Segment seg = _segments.getNextSegment( ray_dir, turn_dir );

        if( seg == null )
            return null; // Es gibt keine Segmente und damit auch keine
                         // Knoten, die mit diesem Knoten verbunden sind

        // Segment-Knoten zurueckgeben, der nicht mit diesem Knoten
        // uebereinstimmt
        //
        return seg.getOtherNode( this );
    } // getNextNode


    /**
     * @return  Array der Knoten, die mit diesem Knoten verbundenen
     *          sind.
     *          Das Array enthaelt die Knoten nach den Richtungen
     *          der zugehoerigen Segmente geordnet ( also von der
     *          Richtung der positiven x-Achse ausgehend gegen den
     *          Uhrzeigersinn )
     */

    //============================================================
    public Node[] getNodes()
    //============================================================
    {
        return _getNodes( getSegments() );
    } // getNodes


    /**
     * Ermitteln der mit diesem Knoten verbundenen Knoten, die in
     * Drehrichtung <turn_dir> vom Knoten <node_first> bis zum
     * Knoten <node_last> aufeinander folgen
     *
     * @param node_first  erster Knoten des zu liefernden Arrays
     *
     * @param node_last   letzter Knoten des zu liefernden Arrays
     *                    oder null, falls alle verbundenen Knoten
     *                    ab Knoten <node_first> geliefert werden
     *                    sollen
     *
     * @param turn_dir    CLOCKWISE oder COUNTERCLOCKWISE
     *
     * @return  Array der von <node_first> bis <node_last> in
     *          Drehrichtung <turn_dir> geordneten Knoten
     *          oder <null>, falls <node_first oder <node_last> gar
     *          nicht mit diesem Knoten verbunden ist
     */

    //============================================================
    public Node[] getNodes( Node    node_first,
                            Node    node_last,
                            boolean turn_dir )
    //============================================================
    {
        if( node_first == null )
            return null;

        Segment seg_first = getSegment( node_first );
               // Segment, welches diesen Knoten mit Knoten node_first
               // verbindet

        if( seg_first == null )
            return null; // Knoten node_first ist gar nicht mit diesem
                         // Knoten verbunden

        Segment seg_last = null;

        // Falls node_last ungleich null ist, wird in seg_last das
        // Segment ermittelt, welches den Knoten node_last mit diesem
        // Knoten verbindet
        //
        if( node_last != null )
        {
            seg_last = getSegment( node_last );

            if( seg_last == null )
                return null; // Knoten node_last ist gar nicht mit
                             // diesem Knoten verbunden
        } // if

        return _getNodes( getSegments( seg_first, seg_last, turn_dir ) );
    } // getNodes


    /**
     * Ermitteln der mit diesem Knoten verbundenen Knoten, so wie
     * sie der Reihe nach im Drehwinkel <turn> angetroffen werden
     *
     * @return  Array der ermittelten Knoten
     */

    //============================================================
    public Node[] getNodes( TurnAngle turn )
    //============================================================
    {
        return _getNodes( getSegments( turn ) );
    } // getNodes




    //------------------------------------------------------------
    // Ermittlung von Informationen ueber TourVertices die zu diesem
    // Knoten gehoeren
    //------------------------------------------------------------


    /**
     * Ermitteln des eindeutig bestimmten Vertex, welcher "sichtbar"
     * ist, wenn man sich von diesem Knoten aus infinitisimal in die
     * Strahlenrichtung <ray_dir> bewegt und von dort auf diesen Knoten
     * zurueck schaut.
     *
     * Wenn die Richtung <ray_dir> mit der Richtung eines Segments
     * uebereinstimmt, ist die eindeutige Bestimmung eines Vertex nicht
     * moeglich ( Es kommen dann genau zwei in Frage ). In diesem
     * Fall wird <null> zurueckgegeben.
     *
     * @return  Durch die Richtung <ray_dir> eindeutig bestimmter
     *          Vertex
     *          oder <null>, falls es keinen Vertex an diesem Knoten
     *          gibt oder keiner eindeutig bestimmt werden kann
     */

    //============================================================
    public TourVertex getVertex( RayDirection ray_dir )
    //============================================================
    {
        if(    ray_dir == null
            || _segments.empty() )
            return null;

        // erstes Segment ab der Strahlenrichtung ray_dir gegen den
        // Uhrzeigersinn ermitteln

        Segment segment =
            _segments.getNextSegment( ray_dir, COUNTERCLOCKWISE );

        if( segment.getDirectionFrom( this ).isEqualTo( ray_dir ) )
            return null; // Segment liegt genau auf dem Strahl, also
                         // ist keine eindeutige Vertex-Bestimmung
                         // moeglich

        return segment.getVertex( this, LEFT_HAND );
    } // getVertex


    /**
     * Ermitteln der beiden ggf. unterschiedlichen Vertices, die
     * durch die Strahlenrichtung <ray_dir> bestimmt werden
     * ( @see getVertex( RayDirection ) ).
     *
     * Wenn die Richtung <ray_dir> nicht mit der Richtung eines Segments
     * uebereinstimmt, so sind beide ermittelten Vertices identisch und
     * stimmen mit dem durch  getVertex( ray_dir )  gelieferten
     * ueberein.
     * Andernfalls werden zwei verschiedene Vertices ermittelt, und
     * zwar vom Knoten aus gesehen erst der rechts vom betroffenen
     * Segment liegende und dann der linke.
     *
     * Im Falle, dass zu diesem Knoten gar kein Vertex gehoert ( das
     * bedeutet er ist nicht mit Segmenten verbunden ) wird fuer beide
     * Vertices null zurueckgegeben.
     *
     * @return  2-elementiges Array mit den ermittelten Vertices
     */

    //============================================================
    public TourVertex[] getVertices( RayDirection ray_dir )
    //============================================================
    {
        TourVertex[] vertices = new TourVertex[ 2 ];

        if(    ray_dir == null
            ||_segments.empty() )
            return vertices;

        // erstes Segment ab der Strahlenrichtung ray_dir gegen den
        // Uhrzeigersinn ermitteln
        //
        Segment segment =
            _segments.getNextSegment( ray_dir, COUNTERCLOCKWISE );

        // Rechts vom Segment liegenden Vertex bestimmen
        // ( Das ist kein Fehler !!!
        //   @see Segment.getVertex( boolean, Node ) )
        //
        vertices[ 0 ] = segment.getVertex( this, LEFT_HAND );

        // Falls das Segment genau auf der Strahlenrichtung ray_dir
        // liegt muss noch der links von ihm liegende Vertex ermittelt
        // werden.
        // Ansonsten ist der der zweite zu ermittelnde Vertex mit dem
        // ersten identisch
        //
        if( segment.getDirectionFrom( this ).isEqualTo( ray_dir ) )
        {
            vertices[ 1 ] = segment.getVertex( this, RIGHT_HAND );
        }
        else
            vertices[ 1 ] = vertices[ 0 ];

        return vertices;
    } // getVertices


    /**
     * Ermitteln des naechsten Vertex der erreicht wird, wenn dieser
     * Knoten vom Vertex <vertex> aus in der Drehrichtung <turn_dir>
     * umrundet wird.
     *
     * @param turn_dir    CLOCKWISE oder COUNTERCLOCKWISE
     *
     * @return  Der ermittelte Vertex
     *          oder <null> falls der Vertex <vertex> gar nicht zu
     *          diesem Knoten gehoert
     */

    //============================================================
    public TourVertex getNextVertex( TourVertex  vertex,
                                     boolean     turn_dir )
    //============================================================
    {
        if(    vertex == null
            || vertex.getNode() != this )
            return null;

         return vertex.getNextNodeVertex( turn_dir );
    } // getNextVertex


    /**
     * @return  Array der Vertices, die zu diesem Knoten gehoeren
     *
     *          Das Array enthaelt die Vertices in der Reihenfolge,
     *          wie sie bei einer Umrundung dieses Knotens gegen den
     *          Uhrzeigersinn ( ausgehend von der positiven x-Achse )
     *          angetroffen werden.
     */

    //============================================================
    public TourVertex[] getVertices()
    //============================================================
    {
        return _getVertices( getSegments(), RIGHT_HAND );
    } // getVertices


    /**
     * Ermitteln der zu diesem Knoten gehoerenden Vertices, die in
     * der Drehrichtung <turn_dir> von <vertex_first> bis <vertex_last>
     * aufeinander folgen.
     *
     * @param vertex_first  erster Vertex des zu liefernden Arrays
     *
     * @param vertex_last   letzter Vertex des zu liefernden Arrays
     *                      oder null, falls alle Vertices ab Vertex
     *                      <vertex_first> geliefert werden sollen
     *
     * @param turn_dir      CLOCKWISE oder COUNTERCLOCKWISE
     *
     * @return  Array der von <vertex_first> bis <vertex_last>
     *          in Drehrichtung <turn_dir> geordneten Vertices
     *          oder <null>, falls <vertex_first> oder <vertex_last>
     *          gar nicht zu diesem Knoten gehoeren
     */

    //============================================================
    public TourVertex[] getVertices( TourVertex vertex_first,
                                     TourVertex vertex_last,
                                     boolean    turn_dir )
    //============================================================
    {
        if( vertex_first == null )
            return null;

        Segment seg_first = vertex_first.getSegment( RIGHT_HAND );

        Segment seg_last = ( vertex_last == null ) ?
                           null :
                           vertex_last.getSegment( RIGHT_HAND );

        Segment[] segments =
                        getSegments( seg_first, seg_last, turn_dir );

        return _getVertices( segments, RIGHT_HAND );
    } // getVertices


    /**
     * Ermitteln aller zu diesem Knoten gehoerenden Vertices, so wie
     * sie der Reihe nach im Drehwinkel <turn> angetroffen werden
     *
     * @return  Array der ermittelten Vertices
     */

    //============================================================
    public TourVertex[] getVertices( TurnAngle turn )
    //============================================================
    {
        if(    turn == null
            || _segments.empty()
            )
            return new TourVertex[ 0 ]; // keine Vertices


        // Alle Segmente im Drehwinkel turn ermitteln
        //
        Segment[] segments = getSegments( turn );

        // Falls keine Segmente im Drehwinkel turn liegen, so liegt
        // genau ein Vertex in diesem Winkel
        //
        if( segments.length == 0 )
        {
            TourVertex[] vertices = new TourVertex[ 1 ];
            vertices[ 0 ] = getVertex( turn.getStartDirection() );
            return vertices;
        } // if


        // Erstes und letztes im Drehwinkel turn angetroffenes Segment
        // ermitteln
        //
        Segment seg_first = segments[ 0 ];
        Segment seg_last  = segments[ segments.length - 1 ];


        // Liegt das erste Segment genau in der Startrichtung des
        // Drehwinkels ?
        //
        boolean first_on_start =
                ( seg_first.getDirectionFrom( this )
                             .isEqualTo( turn.getStartDirection() ) );

        // Liegt das letzte Segment genau in der Endrichtung des
        // Drehwinkels ?
        //
        boolean last_on_end =
                ( ! seg_last.getDirectionFrom( this )
                            .isEqualTo( turn.getEndDirection() ) );


        // Anzahl der im Drehwinkel turn liegenden Vertices ermitteln
        // und entsprechend grosses Array erzeugen

        int no_of_vertices = segments.length + 1;

        if( first_on_start )
            no_of_vertices --;
        if( last_on_end )
            no_of_vertices --;

        TourVertex[] vertices = new TourVertex[ no_of_vertices ];


        // Vertices in der durch den Drehwinkel turn festgelegten
        // Reihenfolge in das Array eintragen

        boolean hand = ( turn.getTurnDirection() == CLOCKWISE ) ?
                       LEFT_HAND :
                       RIGHT_HAND;

        int i = 0;
        if( ! first_on_start )
            vertices[ i++ ] = seg_first.getVertex( this, ! hand );

        for( int j = 0; j < ( segments.length - 1 ); j++ )
            vertices[ i++ ] = segments[ j ].getVertex( this, hand );

        if( ! last_on_end )
            vertices[ i++ ] = seg_last.getVertex( this, hand );


        return vertices;
    } // getVertices




    //------------------------------------------------------------
    // Ermittlung von Informationen ueber TourPolygone die an diesen
    // Knoten angrenzen
    //------------------------------------------------------------


    /**
     * Ermitteln des eindeutig bestimmten Polygons, innerhalb dessen
     * man sich befindet, wenn man sich von diesem Knoten aus
     * infinitisimal in die Strahlenrichtung <ray_dir> bewegt
     *
     * Wenn die Richtung <ray_dir> mit der Richtung eines Segments
     * uebereinstimmt, ist die eindeutige Bestimmung eines Polygons
     * nicht moeglich. In diesem Fall wird <null> zurueckgegeben.
     *
     * @return  Durch die Richtung <ray_dir> eindeutig bestimmtes
     *          Polygon
     *          oder <null>, falls es kein Polygon an diesem Knoten
     *          gibt oder nicht eindeutig bestimmt werden kann
     */

    //============================================================
    public TourPolygon getPolygon( RayDirection ray_dir )
    //============================================================
    {
        TourVertex vertex = getVertex( ray_dir );

        if( vertex == null )
            return null;

        return vertex.getPolygon();
    } // getPolygon


    /**
     * @return  Array aller paarweise verschiedenen Polygone, die an
     *          diesen Knoten angrenzen
     */

    //============================================================
    public TourPolygon[] getPolygons()
    //============================================================
    {
        return _getPolygons( false, getVertices(), false );
    } // getVertices


    /**
     * Ermitteln aller paarweise verschiedenen Polygone des Typs <type>,
     * die an diesen Knoten angrenzen
     *
     * @param type  OBSTACLE oder YARD
     *
     * @return  Array aller ermittelten Polygone
     */

    //============================================================
    public TourPolygon[] getPolygons( boolean type )
    //============================================================
    {
        return _getPolygons( type, getVertices(), true );
    } // getVertices


    /**
     * @return  Array aller paarweise verschiedenen Polygone, die
     *          innerhalb des Drehwinkels turn> an diesen Knoten
     *          angrenzen
     */

    //============================================================
    public TourPolygon[] getPolygons( TurnAngle turn )
    //============================================================
    {
        return _getPolygons( false, getVertices( turn ), false );
    } // getPolygons


    /**
     * Ermitteln aller paarweise verschiedenen Polygone des Typs <type>,
     * die innerhalb des Drehwinkels <turn> an diesen Knoten angrenzen
     *
     * @param type  OBSTACLE oder YARD
     *
     * @return  Array aller ermittelten Polygone
     */

    //============================================================
    public TourPolygon[] getPolygons( boolean   type,
                                      TurnAngle turn )
    //============================================================
    {
        return _getPolygons( type, getVertices( turn ), true );
    } // getPolygons




    //------------------------------------------------------------
    // Methoden, die die Move-Markierung betreffen
    //------------------------------------------------------------


    /**
     * Setzen bzw. Ruecksetzen der Move-Markierung
     */

    //============================================================
    public void setMoveFlag( boolean flag )
    //============================================================
    {
        _is_moving = flag;
    } // setMoveFlag


    /**
     * @return  Move-Markierung
     */

    //============================================================
    public boolean isMoving()
    //============================================================
    {
        return _is_moving;
    } // isMoving




    //------------------------------------------------------------
    // Diverse andere Methoden
    //------------------------------------------------------------


    /**
     * Ermitteln aller Knoten der durch diesen Knoten bestimmten
     * Zusammenhangskomponente, also der Zusammenhangskomponente zu der
     * dieser Knoten gehoert.
     *
     * @return  Array aller Knoten der durch diesen Knoten bestimmten
     *          Zusammenhangskomponente ( einschliesslich dieses
     *          Knotens ).
     *          Dabei gilt immer:
     *              ( node.getConnectedComponent() )[0] == node
     */

    //============================================================
    public Node[] getConnectedComponent()
    //============================================================
    {
        SimpleList list = new SimpleList();
        list.add( this );
        _is_found = true;

        for( ListItem item = list.first();
                      item != null;
                      item = item.next() )
        {
            Node node = ( Node )item.value();

            for( TreeItem i = node._segments.getFirst();
                          i != null;
                          i = ( (LinkedTreeItem)i ).getNext() )
            {
                Segment seg = ( Segment )( i.value() );
                Node node_next = seg.getOtherNode( node );

                if( ! node_next._is_found )
                {
                    list.add( node_next );
                    node_next._is_found = true;
                } // for
            } // for
        } // for


        for( ListItem item = list.first();
                      item != null;
                      item = item.next()
            )
            ( ( Node )item.value() )._is_found = false;


        return toArray( list );
    } // getConnectedComponent




    //************************************************************
    // public class methods
    //************************************************************


    /**
     * Liefert einen Comparitor ( @see anja.util.Comparitor ), zum Vergleich
     * der X-Koordinaten von Knoten.
     * Solch ein Comparitor wird z.B. beim Erzeugen eines RedBlackTree's
     * benoetigt ( @see anja.util.RedBlackTree ).
     */

    //============================================================
    static public Comparitor getXComparitor()
    //============================================================
    {
        // Erzeugen und Zurueckgeben eines Comparitors
        //
        return new Comparitor()
        {
            // Definition der Interface-Methode compare
            //
            public short compare( Object o1, Object o2)
            {
                Node node1 = ( Node )( ( SimpleTreeItem )o1 ).key();
                Node node2 = ( Node )( ( SimpleTreeItem )o2 ).key();

                int x1 = node1._x;
                int x2 = node2._x;

                if( x1 > x2 ) return Comparitor.BIGGER;
                if( x1 < x2 ) return Comparitor.SMALLER;

                // x1 == x2

                int y1 = node1._y;
                int y2 = node2._y;

                if( y1 > y2 ) return Comparitor.BIGGER;
                if( y1 < y2 ) return Comparitor.SMALLER;
                return Comparitor.EQUAL;
            } // compare
        };
    } // getXComparitor


    /**
     * Liefert einen Comparitor ( @see anja.util.Comparitor ), zum Vergleich
     * der Y-Koordinaten von Knoten.
     * Solch ein Comparitor wird z.B. beim Erzeugen eines RedBlackTree's
     * benoetigt ( @see anja.util.RedBlackTree ).
     */

    //============================================================
    static public Comparitor getYComparitor()
    //============================================================
    {
        // Erzeugen und Zurueckgeben eines Comparitors
        //
        return new Comparitor()
        {
            // Definition der Interface-Methode compare
            //
            public short compare( Object o1, Object o2)
            {
                Node node1 = ( Node )( ( SimpleTreeItem )o1 ).key();
                Node node2 = ( Node )( ( SimpleTreeItem )o2 ).key();

                int y1 = node1._y;
                int y2 = node2._y;

                if( y1 > y2 ) return Comparitor.BIGGER;
                if( y1 < y2 ) return Comparitor.SMALLER;

                // y1 == y2

                int x1 = node1._x;
                int x2 = node2._x;

                if( x1 > x2 ) return Comparitor.BIGGER;
                if( x1 < x2 ) return Comparitor.SMALLER;
                return Comparitor.EQUAL;
            } // compare
        };
    } // getYComparitor


    /**
     * @return  Array aller Knoten der Liste <list>
     *          Es wird davon ausgegangen, dass es sich bei den in der
     *          Liste enthaltenen Objekten um Knoten handelt.
     */

    //============================================================
    static public Node[] toArray( SimpleList list)
    //============================================================
    {
        if( list == null )
            return new Node[ 0 ];

        Node[] nodes = new Node[ list.length() ];

        int index = 0;
        for( ListItem item = list.first();
                      item != null;
                      item = item.next()
            )
            nodes[ index ++ ] = ( Node )item.value();

        return nodes;
    } // toArray




    //************************************************************
    // protected methods
    //************************************************************


    /**
     * Knoten ueber sein Entfernen aus der Szene informieren
     */

    //============================================================
    protected void remove()
    //============================================================
    {
        _scene = null; // Knoten als entfernt markieren
    } // remove


    /**
     * Initialisieren dieses Knotens
     */

    //============================================================
    protected void init( ObstacleScene scene,
                         int           x,
                         int           y )
    //============================================================
    {
        _scene = scene;

        _x = x;
        _y = y;

        _segments = new NodeSegments( this );

        _is_moving = false;

        item_nodes = null;
        item_sorted_nodes_x = null;
        item_sorted_nodes_y = null;
        items_quadtree = null;
    } // init


    //------------------------------------------------------------
    // Zufuegen und Entfernen von Segmenten zur Segmentmenge dieses
    // Knotens
    //------------------------------------------------------------


    /**
     * Verbinden des Segments <segment> mit diesem Knoten.
     *
     * Es wird davon ausgegangen, dass dieser Knoten einer der
     * Knoten des zu verbindenen Segments ist.
     *
     * Diese Methode wird ausschliesslich vom Segment-Konstruktor
     * aufgerufen und aktualisiert alle betroffenen Verbindungen
     * zwischen TourVertices und TourEdges sowie alle betroffenen
     * TourPolygone
     *
     * @return  TreeItem mit dem das Segment in die Segmentmenge
     *          dieses Knotens eingefuegt wurde
     */

    //============================================================
    protected TreeItem connectToSegment( Segment segment )
    //============================================================
    {
        TreeItem item = _segments.addSegment( segment );

        if( _segments.length() == 1 )
            return item;

        Segment seg_next = _segments.getNextSegment( item, CLOCKWISE );


        // Verknuepfungen von TourEdges und TourVertices sowie
        // TourPolygone aktualisieren

        TourEdge edge_1 = segment.getEdge( this, RIGHT_HAND );
        TourEdge edge_2 = seg_next.getEdge( this, RIGHT_HAND );

        TourVertex.updateConnections( edge_1, edge_2 );

        TourPolygon.updatePolygons( edge_1, edge_2, YARD );

        return item;
    } // connectToSegment


    /**
     * Aufheben der Verbindung zwischen dem durch das TreeItem <item>
     * gegebenen Segment und diesem Knoten
     *
     * Diese Methode wird ausschliesslich von der Methode
     * Segment.remove() aufgerufen und aktualisiert alle betroffenen
     * Verbindungen zwischen TourVertices und TourEdges sowie alle
     * betroffenen TourPolygone
     *
     * @param item  TreeItem mit dem das Segment in der Segmentmenge
     *              des Knotens eingefuegt ist
     */

    //============================================================
    protected void disconnectFromSegment( TreeItem item )
    //============================================================
    {
        // Segment, welches von diesem Knoten getrennt werden soll
        // und dessen Nachbarsegment ( im Uhrzeigersinn ) ermitteln.
        //
        // Die Segmente muessen ermittelt werden bevor das zu trennende
        // Segment aus _segments entfernt wird

        Segment seg = ( Segment )( item.value() );

        Segment seg_next =
                _segments.getNextSegment( item, CLOCKWISE );



        _segments.removeSegment( item ); // seg_disconnect aus _segments
                                         // entfernen

        if( _segments.length() == 0 )
            return; // seg_disconnect war das einzige Segment
                    // in _segments. Also fertig.



        // Verknuepfungen von TourEdges und TourVertices sowie
        // TourPolygone aktualisueren

        TourEdge edge_1 = seg.getEdge( this, RIGHT_HAND );
        TourEdge edge_2 = seg_next.getEdge( this, RIGHT_HAND );

        TourVertex.updateConnections( edge_1, edge_2 );

        TourPolygon.updatePolygons( edge_1, edge_2, OBSTACLE );
    } // disconnectFromSegment




    //************************************************************
    // private methods
    //************************************************************


    /**
     * Liefern eines Arrays der Knoten welche durch die Segmente
     * <segments> mit diesem Knoten verbunden sind
     */

    //============================================================
    private Node[] _getNodes( Segment[] segments )
    //============================================================
    {
        if( segments == null )
            return null;

        Node[] nodes = new Node[ segments.length ];

        for( int i = 0; i < segments.length; i++ )
        {
            Segment seg = segments[ i ];
            nodes[ i ] = seg.getOtherNode( this );
        } // for

        return nodes;
    } // _getNodes


    /**
     * Ermitteln eines Arrays von Vertices, welche die Segmente
     * segments beruehren.
     * Es gilt  vertices[ i ] == segments[ i ].getVertex( hand, this )
     *
     * @param hand  LEFT_HAND oder RIGHT_HAND
     */

    //============================================================
    private TourVertex[] _getVertices( Segment[] segments,
                                       boolean   hand )
    //============================================================
    {
        if( segments == null )
            return null;

        TourVertex[] vertices = new TourVertex[ segments.length ];

        for( int i = 0; i < segments.length; i++ )
        {
            Segment seg = segments[ i ];
            vertices[ i ] = seg.getVertex( this, hand );
        } // for

        return vertices;
    } // getVertices


    /**
     * Ermitteln eines Arrays der paarweise verschiedenen Polygone
     * zu denen die Vertices <vertices> gehoeren.
     *
     * Falls use_type == true, werden nur die Polygone des Typs <type>
     * geliefert.
     *
     * @param type  OBSTACLE oder YARD
     */

    //============================================================
    private TourPolygon[] _getPolygons( boolean      type,
                                        TourVertex[] vertices,
                                        boolean      use_type )
    //============================================================
    {
        if( vertices == null )
            return null;

        SimpleList poly_list = new SimpleList();

        for( int i = 0; i < vertices.length; i++ )
        {
            TourPolygon poly = vertices[ i ].getPolygon();

            if(    ! poly.is_found
                && (    ! use_type
                     || poly.getType() == type ) )
            {
                poly_list.add( poly );
                poly.is_found = true;
            } // if
        } // for

        TourPolygon[] polygons = new TourPolygon[ poly_list.length() ];

        ListItem item = poly_list.first();
        for( int i = 0; i < polygons.length; i++ )
        {
            polygons[ i ] = ( TourPolygon )( item.value() );
            polygons[ i ].is_found = false;

            item = item.next();
        } // for

        return polygons;
    } // getPolygons

} // Node
