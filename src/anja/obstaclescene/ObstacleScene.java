package anja.obstaclescene;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import anja.geom.Point2;
import anja.geom.Segment2;
import anja.geom.Rectangle2;

import anja.util.SimpleList;
import anja.util.ListItem;
import anja.util.TreeItem;
import anja.util.SimpleTreeItem;

import anja.util.RedBlackTree;
import anja.util.Comparitor;

//import java_ersatz.java2d.Transform;
//import java_ersatz.java2d.Rectangle2D;



/**
 * Ein Objekt der Klasse ObstacleScene repraesentiert eine als "freie"
 * Flaeche gedachte Ebene, auf der sich punktfoermige, linienfoermige
 * und/oder polygonale "Hindernisse" befinden.
 *
 * Punktfoermige Hindernisse werden durch "Knoten" ( @see Node )
 * dargestellt.
 * Im 3-dimensionalen Roboter-Modell kann ein Knoten als vertikale Stange
 * mit vernachlaessigbar kleinem Radius aufgefasst werden.
 *
 * Das elementare linienfoermige Hindernis ist das Segment
 * ( @see Segment ), welches als die gerade Verbindungslinie zwischen
 * zwei Knoten verstanden werden soll.
 * Ein Segment kann man sich im Roboter-Modell gut als eine zwischen
 * zwei "Stangen" vertikal aufgestellte ebene Trennwand vorstellen,
 * deren Dicke gegen 0 geht.
 *
 * Alle komplexeren Hindernisse der Szene sind aufgebaut aus in Knoten
 * miteinander verbundenen Segmenten.
 * So koennen z.B. offene Polygone durch die lineare Verkettung von
 * Segmenten und geschlossene Polygone durch zyklische Verkettung
 * gebildet werden.
 * Wichtig ist, dass es sich bei Segmenten und Knoten um die einzigen
 * "Konstruktionselemente" einer Hindernisszene handelt. Jeder Knoten
 * und jedes Segment wird durch genau ein Objekt der entsprechenden
 * Klasse repraesentiert und kann mit diesem Objekt identifiziert
 * werden.
 * Im Gegensatz dazu sind Polygone, Segmentgeflechte und dergleichen
 * zunaechst nur implizit durch die Verbindungsstruktur von Segmenten
 * und Knoten definiert.
 *
 * Die aus Segmenten und Knoten aufgebaute Hindernisszene soll ( auch
 * in Anlehnung an das Bild mit Waenden und Stangen ) folgende
 * Bedingungen erfuellen:
 *
 *      Zwei verschiedene Knoten beruehren sich nicht ( haben also
 *      verschiedene Positionskoordinaten ).
 *
 *      Zwei verschiedene Segmente schneiden sich entweder gar nicht
 *      oder beruehren sich mit ihren Endpunkten in genau einem Knoten.
 *
 *      Ein Knoten schneidet ( beruehrt ) ein Segment hoechtens in
 *      einem Endpunkt des Segments.
 *
 * Zum Aufbau einer Szene mit sich nicht beruehrenden offenen oder
 * geschlossenen einfachen Polygonen ( ggf. innerhalb eines
 * Bounding-Polygons ) ( @see Polygon2Scene ) reicht es aus, in
 * einem Knoten die Verbindung von hoechstens 2 Segmenten zuzulassen.
 * Dies wird fuer die Gueltigkeit einer Hindernisszene jedoch nicht
 * gefordert:
 *
 *      In einem Knoten koennen prinzipiell beliebig viele Segmente
 *      miteinander verbunden sein.
 *
 * Es sind also z.B. auch sternfoermig miteinander verbundene Segmente
 * als Hindernisse moeglich.
 * Diese Erweiterung gegenueber der "einfachen Polygonszene" bietet sich
 * aufgrund des Konstruktionsprinzips der Szene an und stellt keine
 * oder kaum erweiterte Anforderungen an die Implementierung. Im
 * Gegenteil, eine Einschraenkung auf hoechstens zwei verbundene Segmente
 * in einem Knoten wuerde das Modell nur unnatuerlich einengen.
 *
 * Ein geschlossenes einfaches Polygon grenzt eine Flaeche ( das Innere
 * des Polygons ) gegenueber der Aussenflaeche ab. Ob das Innere als
 * "gefuellt" oder "ungefuellt" betrachtet werden kann, haengt
 * entscheidend von der Position eines gedachten Roboters innerhalb der
 * Szene ab.
 * Befindet er sich ausserhalb eines polygonalen Hindernisses, so kann
 * er dieses als gefuellt ansehen ( Es gibt keinen Weg, der einen Punkt
 * im Aussenraum mit einem Punkt im Inneren verbinden koennte ).
 * Befindet er sich im Inneren, kann er die unzugaengliche Aussenflaeche
 * als gefuellt betrachten.
 * Aus diesem Grunde sind polygonale Hindernisse nicht so sehr als
 * Flaechen sondern eher als Ketten zyklisch verbundener Segmente
 * ( was sie ja in ObstacleScene auch sind ) aufzufassen.
 * Allerdings kommt den Polygonen, die auf diese Weise konstruiert sind,
 * sowieso keine herausragende Bedeutung zu, da sie nur den Fall einer
 * speziellen Segment-Knoten-Verkettung realisieren. Wegen der
 * Moeglichkeit, mehr als zwei Segmente in einem Knoten zusammenzufuehren,
 * koennen beliebige ueberschneidungsfreie "Netze" aus Segmenten und
 * Knoten aufgebaut werden.
 *
 * Aber auch in beliebigen Segment-Knoten-Netzen koennen auf sinnvolle
 * Weise geschlossene Polygone definiert werden
 * ( @see TourPolygon ).
 * Und zwar als die Umlaufpolygone, die sich ergeben, wenn man z.B.
 * mit der linken Hand an einem Hindernis solange vorwaerts geht bis man
 * den Startpunkt wieder erreicht hat.
 * An jeder der beiden "Seiten" eines Segments kann ein Umlauf begonnen
 * werden, so dass sich jeder Segmentseite genau ein Umlaufpolygon
 * zuordnen laesst.
 * Wenn man die geraden Teilstuecke der Umlaufpolygone als Kanten
 * bezeichnet, gehoeren zu jedem Segment genau zwei Kanten, die entweder
 * zu verschiedenen Umlaufpolygonen oder auch ( wie z.B. im Falle einer
 * offenen linearen Segmentkette ) zum selben Umlaufpolygon gehoeren
 * koennen.
 * Dabei gelten 2 Umlaufpolygone nur als identisch, wenn sich zwei
 * punktfoermige Roboter, die sich jeweils auf einem der Umlaeufe
 * befinden, prinzipiell begegnen koennten.
 * Somit ist das Umlaufpolygon im Inneren eines einfachen geschlossenen
 * Polygons nicht identisch mit dem aeusseren Umlaufpolygon, obwohl es
 * durch gleiche Punkt-Koordinaten beschrieben werden kann.
 *
 * Wie bereits erwaehnt, gehoeren zu jedem Segment genau zwei Kanten, die
 * ihrerseits Wegstuecke von Umlaeufen darstellen.
 * ( @see TourEdge )
 * Kanten sind ueber Vertices miteinander verbunden ( @see TourVertex )
 * Jede Kante bzw. jeder Vertex gehoert zu genau einem Umlaufpolygon und
 * kann mit einem Wegstueck bzw. einem Eckpunkt des Umlaufs identifiziert
 * werden.
 *
 * Insbesondere werden Kanten und Vertices auch benoetigt, um
 * eindeutige Positionsangaben machen zu koennen ( @see Position ).
 * Fuer einen punktfoermigen Roboter der sich an Position ( 0,1 ) eines
 * Segments ( 0,0 )-->( 0,2 ) befindet, wird erst durch die Angabe der
 * zugehoerigen Umlaufkante festgelegt, auf welcher Seite des Segments
 * er sich befindet.
 * Ebenso ist die Aussage, dass ein Roboter sich am Knoten ( 0,0 )
 * befindet, nicht eindeutig, wenn in diesem Knoten mehrere Segmente
 * zusammenlaufen. Erst durch Angabe eines Vertex, also eines Eckpunktes
 * eines Umlaufpolygons, wird die Position eindeutig bestimmt.
 *
 *
 * Die zentralen Objekte einer Hindernisszene lassen sich wie folgt
 * zusammenfassen:
 *
 *  1. Segmente und Knoten, die als elementare Hindernisse aufgefasst
 *     werden koennen.
 *
 *  2. Umlaufpolygone, durch die Umlaeufe eines gedachten punktfoermigen
 *     Roboters entlang an Hindernisgrenzen ( Segmenten ) beschrieben
 *     werden.
 *
 *  3. Kanten und Vertices, die mit Wegstuecken und Abknickpunkten
 *     eines Umlaufpolygons identifiziert werden koennen und somit genaue
 *     Positionsangaben ermoeglichen.
 *
 *
 *
 * @version 0.1 17.06.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class ObstacleScene
//****************************************************************
{
    //************************************************************
    // public constants
    //************************************************************


    // Richtungen fuer Polygonumlaeufe
    // Vorwaerts und linke/rechte Hand am Polygon
    //
    public final static boolean LEFT_HAND  = true;
    public final static boolean RIGHT_HAND = ! LEFT_HAND;


    // Seiten ( beispielsweise einer Polygonkante )
    //
    public final static boolean INSIDE  = true;
    public final static boolean OUTSIDE = ! INSIDE;


    // Richtungen fuer Drehungen, Umlaeufe etc.
    //
    public final static boolean CLOCKWISE        = true;
    public final static boolean COUNTERCLOCKWISE = ! CLOCKWISE;


    // Polygon-Typen
    //
    public final static boolean OBSTACLE = true;
    public final static boolean YARD     = ! OBSTACLE;



    //************************************************************
    // private variables
    //************************************************************

    private SimpleList _nodes = new SimpleList();
                       // Liste aller Knoten der Szene

    private SimpleList _segments = new SimpleList();
                       // Liste aller Segmente der Szene


    // Nach Koordinaten sortierte Knoten
    // ( Balancierte Binaer-Baeume )
    //
    private SortedNodes _sorted_nodes_x =
                        new SortedNodes( SortedNodes.X_ORDER );

    private SortedNodes _sorted_nodes_y =
                        new SortedNodes( SortedNodes.Y_ORDER );


    private QuadTree _quadtree; // Baum fuer Knoten und Segmente


    private Rectangle _rect_limit; // Begrenzungsrechteck der Szene
                                   // @see Konstruktor


    private int _valid_code; // Anhand dieses Codes kann ueberprueft
                    // werden, ob eine Position ( @see Position ) noch
                    // gueltig ist.
                    // _valid_code wird nach jeder Veraenderung der
                    // Szene inkrementiert


    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen einer neuen Hindernisszene
     *
     * @param rect  Begrenzungsrechteck der Szene
     */

    //============================================================
    public ObstacleScene( Rectangle rect )
    //============================================================
    {
        _rect_limit = new Rectangle( rect );

        _quadtree = new QuadTree( 30 );
    } // ObstacleScene



    //************************************************************
    // public methods
    //
    // Methoden zur Veraenderung der Szene durch Einfuegen und
    // Entfernen etc. von Objekten ( Knoten, Segmente etc. ).
    //************************************************************


    /**
     * Erzeugen und Zufuegen eines neuen Knotens zur Hindernisszene.
     *
     * @return  Erzeugter Knoten
     *          oder null, falls das Zufuegen eines neuen Knotens mit
     *          den Koordinaten <x,y> nicht erlaubt ist.
     */

    //============================================================
    public Node addNode( int x, int y )
    //============================================================
    {
        if( ! _quadtree.isNewNodeAllowed( x, y ) )
            return null;

        // Neuen Knoten erzeugen und der Szene zufuegen
        //
        Node node = _addNode( x, y );

        _valid_code ++; // Dadurch werden alle vorher ermittelten
                        // Positionen ungueltig ( @see Position )
        return node;
    } // addNode



    /**
     * Entfernen eines Knotens, der nicht mit Segmenten verbunden ist
     *
     * @param node   Der zu entfernende Knoten
     *
     * @return       true, falls der Knoten entfernt wurde
     */

    //============================================================
    public boolean removeSingleNode( Node node )
    //============================================================
    {
        // Parameter-Ueberpruefung
        if(    node == null
            || node.getScene() != this
            || ! node.isSingle()
            )
            return false; // Ungueltige Parameter

        // Knoten aus allen Datenstrukturen der Szene entfernen
        // und als entfernt markieren
        //
        _removeNode( node );

        _valid_code ++; // Dadurch werden alle vorher ermittelten
                        // Positionen ungueltig ( @see Position )

        return true;
    } // removeSingleNode



    /**
     * Entfernen eines Knotens.
     * Falls der Knoten mit Segmenten verbunden ist, werden diese
     * ebenfalls entfernt.
     *
     * @param node   Der zu entfernende Knoten
     *
     * @return       true, falls der Knoten entfernt wurde
     */

    //============================================================
    public boolean removeNode( Node node )
    //============================================================
    {
        // Parameter-Ueberpruefung
        if(    node == null
            || node.getScene() != this
            )
            return false; // Ungueltige Parameter

        Segment segments[] = node.getSegments();

        for( int i = 0; i < segments.length; i++ )
            removeSegment( segments[ i ] );

        // Knoten aus allen Datenstrukturen der Szene entfernen
        // und als entfernt markieren
        //
        _removeNode( node );

        _valid_code ++; // Dadurch werden alle vorher ermittelten
                        // Positionen ungueltig ( @see Position )

        return true;
    } // removeNode




    /**
     * Einfuegen eines neuen Segments zwischen Knoten node_1 und
     * Knoten node_2.
     *
     * @return  Das eingefuegte Segment oder
     *          null, falls kein Segment eingefuegt werden konnte
     */

    //============================================================
    public Segment addSegment( Node node_1,
                               Node node_2 )
    //============================================================
    {
        // Parameter-Ueberpruefung
        if(    node_1 == null
            || node_1.getScene() != this
            || node_2 == null
            || node_2.getScene() != this
            )
            return null; // Ungueltige Parameter

        // Neues Segment erzeugen und der Szene zufuegen
        //
        Segment seg = _addSegment( node_1.getX(),
                                   node_1.getY(),
                                   node_2.getX(),
                                   node_2.getY(),
                                   node_1,
                                   node_2 );
        if( seg != null )
            _valid_code ++; // Dadurch werden alle vorher ermittelten
                            // Positionen ungueltig ( @see Position )
        return seg;
    } // addSegment


    /**
     * Einfuegen eines neuen Segments zwischen dem Knoten <node_1> und
     * dem Punkt (<x>,<y>)
     * Dabei wird ggf. auch ein neuer Knoten der Szene zugefuegt
     *
     * @return  Das eingefuegte Segment oder
     *          null, falls kein Segment eingefuegt werden konnte
     */

    //============================================================
    public Segment addSegment( Node     node_1,
                               int      x,
                               int      y )
    //============================================================
    {
        // Parameter-Ueberpruefung
        if(    node_1 == null
            || node_1.getScene() != this
            )
            return null; // Ungueltige Parameter

        // Neues Segment erzeugen und der Szene zufuegen
        //
        Segment seg = _addSegment( node_1.getX(),
                                   node_1.getY(),
                                   x,
                                   y,
                                   node_1,
                                   null );
        if( seg != null )
            _valid_code ++; // Dadurch werden alle vorher ermittelten
                            // Positionen ungueltig ( @see Position )
        return seg;
    } // addSegment


    /**
     * Einfuegen eines neuen Segments zwischen den Punkten (<x1>,<y1>)
     * (<x2>,<y2>)
     * Dabei werden ggf. auch neue Knoten der Szene zugefuegt
     *
     * @return  Das eingefuegte Segment oder
     *          null, falls kein Segment eingefuegt werden konnte
     */

    //============================================================
    public Segment addSegment( int x1,
                               int y1,
                               int x2,
                               int y2 )
    //============================================================
    {
        // Neues Segment erzeugen und der Szene zufuegen
        //
        Segment seg = _addSegment( x1,
                                   y1,
                                   x2,
                                   y2,
                                   null,
                                   null );
        if( seg != null )
            _valid_code ++; // Dadurch werden alle vorher ermittelten
                            // Positionen ungueltig ( @see Position )
        return seg;
    } // addSegment


    /**
     * Entfernen eines Segments
     */

    //============================================================
    public boolean removeSegment( Segment segment )
    //============================================================
    {
        // Parameter-Ueberpruefung
        if(    segment == null
            || segment.getScene() != this
            )
            return false; // Ungueltige Parameter

        // Segment aus allen Datenstrukturen der Szene entfernen
        // und als entfernt markieren
        //
        _removeSegment( segment );

        _valid_code ++; // Dadurch werden alle vorher ermittelten
                        // Positionen ungueltig ( @see Position )

        return true;
    } // removeSegment


    /**
     * Verschieben von Knoten
     *
     * @param nodes  Array der zu verschiebenden Knoten
     * @param dx     Verschiebung in x-Richtung
     * @param dy     Verschiebung in y-Richtung
     *
     * @return  true <==> Knoten wurden verschoben
     */

    //============================================================
    public boolean moveNodes( Node[] nodes,
                              int    dx,
                              int    dy )
    //============================================================
    {
        // Parameter-Ueberpruefung
        if( nodes == null )
            return false; // Ungueltige Parameter

        if(    nodes.length == 0       // Keine Knoten zu verschieben
            || ( dx == 0 && dy == 0 )  // Keine Verschiebung
            )
            return false;


        // maximale Anzahl der betroffenen Segmente ermitteln
        int max_no_of_segments = 0;
        for( int i = 0; i < nodes.length; i++ )
            max_no_of_segments += nodes[ i ].getNoOfSegments();

        Segment[] segments = new Segment[ max_no_of_segments ];
        int no_of_segments = 0;

        int[] x_coors = new int[ nodes.length ];
        int[] y_coors = new int[ nodes.length ];
        int[] x_coors_new = new int[ nodes.length ];
        int[] y_coors_new = new int[ nodes.length ];


        // Alle Knoten einschliesslich der mit ihnen verbundenen
        // Segmente entfernen und entfernte Segmente sowie
        // alte und neue Koordinaten speichern
        //
        for( int i = 0; i < nodes.length; i++ )
        {
            Node node = nodes[ i ];

            int x = node.getX();
            int y = node.getY();
            x_coors[ i ] = x;
            y_coors[ i ] = y;
            x_coors_new[ i ] = x + dx;
            y_coors_new[ i ] = y + dy;


            Segment[] segs = node.getSegments();
            for( int j = 0; j < segs.length; j++ )
                segments[ no_of_segments++ ] = segs[ j ];

            removeNode( node );
        } // for


        boolean possible = true;

        // Alle Knoten mit veraenderten Koordinaten wieder zufuegen
        //
        for( int i = 0; i < nodes.length; i++ )
        {
            Node node = nodes[ i ];
            int x = x_coors_new[ i ];
            int y = y_coors_new[ i ];

            if( ! _quadtree.isNewNodeAllowed( x, y ) )
            {
                // Knoten an ( x,y ) nicht moeglich
                possible = false;
                break;
            } // if

            _addNode( node, x, y );
        } // for

        if( possible )
        {
            // Segmente neu zufuegen
            //
            for( int i = 0; i < no_of_segments; i++ )
            {
                Segment seg = segments[ i ];

                Node node1 = seg.getNode1();
                Node node2 = seg.getNode2();

                seg = _addSegment( node1.getX(),
                                   node1.getY(),
                                   node2.getX(),
                                   node2.getY(),
                                   node1,
                                   node2 );
                if( seg == null )
                {
                    possible = false;
                    break;
                } // if
            } // for
        }


        if( ! possible )
        {
            // Alle neu eingefuegten Knoten einschliesslich der mit
            // ihnen verbundenen neuen Segmente wieder entfernen
            //
            for( int i = 0; i < nodes.length; i++ )
            {
                Node node = nodes[ i ];
                if( node.getScene() == null )
                    break;

                removeNode( node );
            } // for

            // Alle Knoten an den urspruenglichen
            // Koordinaten wieder einfuegen
            //
            for( int i = 0; i < nodes.length; i++ )
            {
                Node node = nodes[ i ];
                int x = x_coors[ i ];
                int y = y_coors[ i ];

                _addNode( node, x, y );
            } // for

            // Alle Segmente wieder einfuegen
            //
            for( int i = 0; i < no_of_segments; i++ )
            {
                Segment seg = segments[ i ];

                Node node_1 = seg.getNode1();
                Node node_2 = seg.getNode2();

                // Neues Segment erzeugen
                //
                seg = new Segment( this, node_1, node_2 );

                // Segment allen Datenstrukturen der Szene zufuegen
                //
                seg.item_segments = _segments.add( seg );
                _quadtree.put( seg );
            } // for

        } // if

        _valid_code ++; // Dadurch werden alle vorher ermittelten
                        // Positionen ungueltig ( @see Position )

        return possible;
    } // moveNodes




    //************************************************************
    // public methods
    //
    // Methoden zur Abfrage von Informationen ueber die Szene
    //************************************************************


    /**
     * @return  Bounding-Box der Knoten.
     */

    //============================================================
    public Rectangle getBoundingBox()
    //============================================================
    {
        if( _nodes.empty() )
            return null;

        int x_min = _sorted_nodes_x.getMinCoor();
        int x_max = _sorted_nodes_x.getMaxCoor();

        int y_min = _sorted_nodes_y.getMinCoor();
        int y_max = _sorted_nodes_y.getMaxCoor();


        return new Rectangle( x_min,
                              y_min,
                              x_max - x_min + 1,
                              y_max - y_min + 1 );
    } // getBoundingBox



    /**
     * @return  Begrenzungsrechteck.
     */

    //============================================================
    public Rectangle getBoundingBoxLimit()
    //============================================================
    {
        return new Rectangle( _rect_limit );
    } // getBoundingBoxLimit



    /**
     * Ermitteln der Position, die durch die gegebenen Koordinaten
     * bestimmt wird.
     */

    //============================================================
    public Position getPosition( Point  coors,
                                 int    node_distance,
                                 int    seg_distance )
    //============================================================
    {
        if( coors == null )
            return null;


        // Array aller Knoten ermitteln, die innerhalb des Quadrats
        // liegen, dessen Mittelpunkt durch coors und dessen Kantenlaenge
        // durch 2*node_distance + 1 gegeben ist

        int x = coors.x - node_distance;
        int y = coors.y - node_distance;
        int size = node_distance * 2 + 1;

        Node[] nodes = getNodes( x, y, size, size );


        // Den Knoten mit dem geringsten Abstand zum Punkt coors
        // bestimmen ( von allen Knoten deren Abstand zum Punkt coors
        // kleiner oder gleich dem Abstand distance ist )

        Node nearest_node = null;
        double square_dist = node_distance * node_distance;

        for( int i = 0; i < nodes.length; i++ )
        {
            Node node = nodes[ i ];
            long sq_dist = Calculator.squareDistance( coors.x,
                                                      coors.y,
                                                      node.getX(),
                                                      node.getY() );

            if( sq_dist <= square_dist )
            {
                square_dist = sq_dist;
                nearest_node = node;
            } // if
        } // for


        if( nearest_node != null )
        {
            if( nearest_node.isSingle() )
                return new Position( nearest_node );

            TourVertex[] vertices =
                nearest_node.getVertices(
                    new RayDirection( nearest_node.getX(),
                                      nearest_node.getY(),
                                      coors.x,
                                      coors.y ) );

            TourVertex vertex = ( vertices[0 ] != null ) ?
                                vertices[ 0 ] :
                                vertices[ 1 ];

            return new Position( vertex );
        } // if

        // nearest_node == null


        x = coors.x - seg_distance;
        y = coors.y - seg_distance;
        size = seg_distance * 2 + 1;

        Segment[] segments = getSegments( new Rectangle2D.Float( ( float )x,
                                                           ( float )y,
                                                           ( float )size,
                                                           ( float )size ) );

        // Das Segment mit dem geringsten Abstand zum Punkt coors
        // bestimmen ( von allen Segmenten deren Abstand zum Punkt coors
        // kleiner oder gleich dem Abstand distance ist )

        Segment nearest_segment = null;
        Point2 pt = null;
        square_dist = seg_distance * seg_distance;


        Point2 coors_pt2 = new Point2( coors.x, coors.y );

        for( int i = 0; i < segments.length; i++ )
        {
            Segment seg = segments[ i ];
            Segment2 segment2 = seg.getSegment2();

            double sq_dist = segment2.squareDistance( coors_pt2 );

            if( sq_dist <= square_dist )
            {
                pt = segment2.plumb( coors_pt2 );
                square_dist = sq_dist;
                nearest_segment = seg;

            } // if
        } // for


        if( nearest_segment != null )
        {
            TourEdge edge = nearest_segment.getEdge( coors.x,
                                                     coors.y );
            if( edge == null )
            {
                edge = nearest_segment.getEdgeLeftHand();
                System.out.println( "lies_on" );
            }
            else
            {
                System.out.println();
                if( edge == nearest_segment.getEdgeLeftHand() )
                    System.out.println( "left_hand" );
                else
                    System.out.println( "right_hand" );
            }


            return new Position( edge, Math.round( pt.x ),
                                       Math.round( pt.y ) );

        } // if


        // nearest_segment == null

        return new Position( this, coors.x, coors.y );
    } // getPosition



    /**
     * @return  Anzahl der Knoten dieser Szene
     */

    //============================================================
    public int getNoOfNodes()
    //============================================================
    {
        return _nodes.length();
    } // getNoOfNodes


    /**
     * @return  Array aller Knoten der Szene
     */

    //============================================================
    public Node[] getNodes()
    //============================================================
    {
        return Node.toArray( _nodes );
    } // getNodes


    /**
     * @return  Alle Knoten, die innerhalb des gegebenen Rechtecks
     *          liegen
     */

    //============================================================
    public Node[] getNodes( double x,
                            double y,
                            double width,
                            double height )
    //============================================================
    {
        return getNodes( new Rectangle2D.Float( ( float )x,
	                                          ( float )y,
	                                          ( float )width,
	                                          ( float )height ) );
    } // getNodes


    /**
     * @return  Alle Knoten, die innerhalb des gegebenen Rechtecks
     *          liegen
     */

    //============================================================
    public Node[] getNodes( Rectangle2D rect )
    //============================================================
    {
        // Parameter-Ueberpruefung
        if( rect == null )
            return null; // Ungueltige-Parameter

        int x_min = Math.round( (float)rect.getX() );
        int y_min = Math.round( (float)rect.getY() );
        int x_max = Math.round( (float)rect.getX() + (float)rect.getWidth() );
        int y_max = Math.round( (float)rect.getY() + (float)rect.getHeight() );


        QueryRectangle r =
            new QueryRectangle( x_min, y_min, x_max, y_max );

        SimpleList list_single    = new SimpleList();
        SimpleList list_connected = new SimpleList();

        _quadtree.getNodes( r, list_single, list_connected );


        int no_of_single    = list_single.length();
        int no_of_connected = list_connected.length();

        Node[] nodes = new Node[ no_of_single + no_of_connected ];

        int index = 0;

        for( ListItem item = list_single.first();
                      item != null;
                      item = item.next()
            )
            nodes[ index ++ ] = ( Node )item.value();

        for( ListItem item = list_connected.first();
                      item != null;
                      item = item.next()
            )
            nodes[ index ++ ] = ( Node )item.value();

        return nodes;
    } // getNodes




    /**
     * @return  Anzahl der Segmente dieser Szene
     */

    //============================================================
    public int getNoOfSegments()
    //============================================================
    {
        return _segments.length();
    } // getNoOfSegments


    /**
     * @return  Array aller Segmente der Szene
     */

    //============================================================
    public Segment[] getSegments()
    //============================================================
    {
        return Segment.toArray( _segments );
    } // getSegments


    /**
     * @return  Alle Segmente, die innerhalb des gegebenen Rechtecks
     *          liegen
     */

    //============================================================
    public Segment[] getSegments( double x,
                                  double y,
                                  double width,
                                  double height )
    //============================================================
    {
        return getSegments( new Rectangle2D.Float( ( float )x,
		                                             ( float )y,
		                                             ( float )width,
		                                             ( float )height ) );
    } // getSegments


    /**
     * @return  Alle Segmente, die innerhalb des gegebenen Rechtecks
     *          liegen
     */

    //============================================================
    public Segment[] getSegments( Rectangle2D rect )
    //============================================================
    {
        // Parameter-Ueberpruefung
        if( rect == null )
            return null; // Ungueltige-Parameter

        int x_min = Math.round( (float)rect.getX() );
        int y_min = Math.round( (float)rect.getY() );
        int x_max = Math.round( (float)rect.getX() + (float)rect.getWidth() );
        int y_max = Math.round( (float)rect.getY() + (float)rect.getHeight() );

        QueryRectangle r =
            new QueryRectangle( x_min, y_min, x_max, y_max );

        SimpleList list_inside  = new SimpleList();
        SimpleList list_outside = new SimpleList();

        _quadtree.getSegments( r, list_inside, list_outside );


        int no_of_inside  = list_inside.length();
        int no_of_outside = list_outside.length();

        Segment[] segments =
            new Segment[ no_of_inside + no_of_outside ];

        int index = 0;

        for( ListItem item = list_inside.first();
                      item != null;
                      item = item.next()
            )
            segments[ index ++ ] = ( Segment )item.value();

        for( ListItem item = list_outside.first();
                      item != null;
                      item = item.next()
            )
            segments[ index ++ ] = ( Segment )item.value();

        return segments;
    } // getSegments



    //************************************************************
    // public methods
    //
    // Den QuadTree der Szene betreffende Methoden
    //************************************************************



    /**
     * Zeichnen des QuadTree's der Hindernis-Szene
     */

    //============================================================
    public void paintQuadTree( Graphics g,
                               AffineTransform transform )
    //============================================================
    {
        _quadtree.paint( g, transform );
    } // paintQuadTree


    /**
     * Neu aufbauen des QuadTree's der Hindernis-Szene
     * ( Alle Objekte entfernen und wieder einfuegen )
     */

    //============================================================
    public void reorganizeQuadTree()
    //============================================================
    {
        // Alle Segmente aus QuadTree entfernen
        for( ListItem i = _segments.first(); i != null; i = i.next() )
        {
            Segment seg = ( Segment )i.value();
            _quadtree.remove( seg );
        } // for

        // Alle Knoten aus QuadTree entfernen
        for( ListItem i = _nodes.first(); i != null; i = i.next() )
        {
            Node node = ( Node )i.value();
            _quadtree.remove( node );
        } // for

        // Alle Knoten wieder in QuadTree einfuegen
        for( ListItem i = _nodes.first(); i != null; i = i.next() )
        {
            Node node = ( Node )i.value();
            _quadtree.put( node );
        } // for

        // Alle Segmente wieder in QuadTree einfuegen
        for( ListItem i = _segments.first(); i != null; i = i.next() )
        {
            Segment seg = ( Segment )i.value();
            _quadtree.put( seg );
        } // for
    } // reorganizeQuadTree



    //************************************************************
    // protected methods
    //************************************************************


    /**
     */

    //============================================================
    protected int getValidCode()
    //============================================================
    {
        return _valid_code;
    } // getValidCode



    //************************************************************
    // private methods
    //************************************************************


    /**
     * Zufuegen eines neuen Knotens am Punkt (<x>,<y>) der Szene
     *
     * Es wird davon ausgegangen, dass das Zufuegen eines Knotens
     * am gegebenen Punkt erlaubt ist.
     *
     * @return  Der zugefuegte Knoten
     */

    //============================================================
    private Node _addNode( int x, int y )
    //============================================================
    {
        // Neuen Knoten erzeugen
        //
        Node node = new Node();

        _addNode( node, x, y );

        return node;
    } // _addNode


    /**
     * Zufuegen eines vorhandenen Knotens am Punkt (<x>,<y>) der Szene
     *
     * Es wird davon ausgegangen, dass das Zufuegen eines Knotens
     * am gegebenen Punkt erlaubt ist und der Knoten <node> nicht
     * bereits zu einer Szene gehoert.
     */

    //============================================================
    private void _addNode( Node node, int x, int y )
    //============================================================
    {
        // Knoten neu initialisieren
        //
        node.init( this, x, y );

        // Knoten allen Datenstrukturen der Szene zufuegen
        //
        node.item_nodes = _nodes.add( node );
        node.item_sorted_nodes_x = _sorted_nodes_x.add( node );
        node.item_sorted_nodes_y = _sorted_nodes_y.add( node );
        _quadtree.put( node );
    } // _addNode


    /**
     * Entfernen eines Knotens aus allen Datenstrukturen dieser Szene
     * und Knoten als entfernt markieren
     */

    //============================================================
    private void _removeNode( Node node )
    //============================================================
    {
        _nodes.remove( node.item_nodes );

        _sorted_nodes_x.remove( node.item_sorted_nodes_x );
        _sorted_nodes_y.remove( node.item_sorted_nodes_y );

        _quadtree.remove( node );

        node.remove();
    } // _removeNode


    /**
     * Zufuegen eines neuen Segments zwischen den Punkten (<x1>,<y1>)
     * und (<x2>,<y2>).
     * Dabei werden ggf. auch neue Knoten erzeugt
     *
     * Es wird davon ausgegangen, dass das Zufuegen von neuen Knoten
     * an den gegebenen Punkten erlaubt ist, falls sich dort noch keine
     * befinden
     *
     * @param node_1  Knoten mit den Kooordinaten (<x1>,<y1>)
     *                oder null, falls es noch keinen Knoten mit diesen
     *                Koordinaten gibt.
     *
     * @param node_2  Knoten mit den Kooordinaten (<x2>,<y2>)
     *                oder null, falls es noch keinen Knoten mit diesen
     *                Koordinaten gibt.
     *
     * @return  Das zugefuegte Segment
     *          oder <null>, falls das Zufuegen eines Segments zwischen
     *          den gegebenen Punkten nicht moeglich ist
     */

    //============================================================
    private Segment _addSegment( int x1,
                                 int y1,
                                 int x2,
                                 int y2,
                                 Node node_1,
                                 Node node_2 )
    //============================================================
    {
        if( x1 == x2 && y1 == y2
            )
            return null; // Die Koordinaten stimmen ueberein,
                         // also kann kein Segment zwischen ihnen
                         // eingefuegt werden

        if( node_1 == null && ! _quadtree.isNewNodeAllowed( x1, y1 ) )
            return null;

        if( node_2 == null && ! _quadtree.isNewNodeAllowed( x2, y2 ) )
            return null;

        if( ! _quadtree.isNewSegmentAllowed( x1, y1, x2, y2 )
            )
            return null; // Es laesst sich kein neues Segment
                         // schnittfrei einfuegen


        // ggf.neuen Knoten an Punkt (x1,y1) zufuegen
        //
        if( node_1 == null )
            node_1 = _addNode( x1, y1 );

        // ggf.neuen Knoten an Punkt (x2,y2) zufuegen
        //
        if( node_2 == null )
            node_2 = _addNode( x2, y2 );

        // Neues Segment erzeugen
        //
        Segment seg = new Segment( this, node_1, node_2 );

        // Segment allen Datenstrukturen der Szene zufuegen
        //
        seg.item_segments = _segments.add( seg );
        _quadtree.put( seg );

        return seg;
    } // _addSegment


    /**
     * Entfernen eines Segments aus allen Datenstrukturen dieser Szene
     * und Segment als entfernt markieren
     */

    //============================================================
    private void _removeSegment( Segment seg )
    //============================================================
    {
        _segments.remove( seg.item_segments );

        seg.remove();

        _quadtree.remove( seg );
    } // _removeSegment


} // ObstacleScene
