package anja.obstaclescene;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

import anja.util.SimpleList;
import anja.util.ListItem;
import anja.util.TreeItem;

/**
 * @version 0.1 04.11.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class QuadTreeSquare
//****************************************************************
{
    //************************************************************
    // protected constants
    //************************************************************


    protected static final short W_E_MASK = 0x0001;
    protected static final short S_N_MASK = 0x0002;

    protected static final short WEST = W_E_MASK;
    protected static final short EAST = 0x0000;

    protected static final short SOUTH = S_N_MASK;
    protected static final short NORTH = 0x0000;

    protected static final short NORTH_EAST = NORTH | EAST;
    protected static final short NORTH_WEST = NORTH | WEST;
    protected static final short SOUTH_EAST = SOUTH | EAST;
    protected static final short SOUTH_WEST = SOUTH | WEST;



    //************************************************************
    // private constants
    //************************************************************


    private static final int _MIN_OBJECTS_TO_GO_DEEPER = 2;




    //************************************************************
    // private variables
    //************************************************************


    private QuadTreeSquare  _parent = null;
                            // Parent-Square oder <null>, falls es
                            // sich bei diesem Square um die Wurzel
                            // eines QuadTrees handelt

    private QuadTreeSquare[] _children = null;
                             // Kind-Squares oder <null>, falls
                             // dieses Square keine Kinder hat.

    private short _quadrant; // Quadrant welches dieses Square im
                             // _parent-Square repraesentiert


    // Grenzkoordinaten dieses Squares
    //
    // Genaugenommen werden alle Objekte in dieses Square eingetragen,
    // die die quadratische Flaeche mit den Grenzkoordinaten
    //      _x_min-0.5, _y_min-0.5, _x_max+0.5, _y_max+0.5
    // schneiden bzw. auf ihr liegen
    //
    private int _x_min;
    private int _y_min;
    private int _x_max;
    private int _y_max;

    private int _x_east;  // Kleinste x-Koordinate der oestlichen
                          // Haelfte.

    private int _y_north; // Kleinste y-Koordinate der noerdlichen
                          // Haelfte


    // Objekte, die sich in diesem Square befinden

    private SimpleList _nodes_single = null;
                       // Liste der Knoten ohne Segmentverbindungen

    private SimpleList _nodes_connected = null;
                       // Liste der Knoten mit Segmentverbindungen

    private SimpleList _segments = null;
                       // Liste aller Segmente

    private SquareSegments _segments_intersect = null;
                       // Liste der Segmente, die dieses Square
                       // vollstaendig durchlaufen, also deren Knoten
                       // ausserhalb liegen



    private short[] _quadranten; // Hilfs-Array fuer Methode
                                 // _getQuadranten(..)


    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen des Wurzel-Squares fuer einen QuadTree.
     * Das Wurzel-Square hat die Grenzkoordinaten
     *      x_min == y_min == -( 2 hoch <exp> )
     *      x_max == y_max == ( 2 hoch <exp> ) - 1
     *
     * @param exp  nichtnegativer Exponent fuer Grenzkoordinaten.
     *             Da es sich bei den Koordinaten un 32-Bit-integer
     *             handelt, ist der hoechste moegliche Exponent 31.
     */

    //============================================================
    protected QuadTreeSquare( int exp )
    //============================================================
    {
        long limit = 0x00000001 << exp;

        _x_min = ( int ) -limit;
        _x_max = ( int ) limit - 1;
        _y_min = _x_min;
        _y_max = _x_max;

        _x_east  = 0;
        _y_north = 0;

    } // QuadTreeSquare


    /**
     * Erzeugen eines Kind-Squares fuer das Square <parent>, welches
     * in diesem den Quadranten <quadrant> repraesentiert
     */

    //============================================================
    protected QuadTreeSquare( QuadTreeSquare parent,
                              short          quadrant )
    //============================================================
    {
        _parent   = parent;
        _quadrant = quadrant;


        if( ( _quadrant & W_E_MASK ) == WEST )    // Westliche Haelfte
        {
            _x_min = _parent._x_min;
            _x_max = _parent._x_east - 1;
        }
        else                                      // Oestliche Haelfte
        {
            _x_min = _parent._x_east;
            _x_max = _parent._x_max;
        } // else


        if( ( _quadrant & S_N_MASK ) == SOUTH )   // Suedliche Haelfte
        {
            _y_min = _parent._y_min;
            _y_max = _parent._y_north - 1;
        }
        else                                      // Noerdliche Haelfte
        {
            _y_min = _parent._y_north;
            _y_max = _parent._y_max;
        } // else


        _x_east  = _x_max - ( ( _x_max - _x_min ) >> 1 );
        _y_north = _y_max - ( ( _y_max - _y_min ) >> 1 );

    } // QuadTreeSquare



    //************************************************************
    // protected methods
    //************************************************************


    /**
     * Eintragen des Knotens <node> in dieses Square.
     *
     * Es wird davon ausgegangen, dass der Knoten auf der durch
     * dieses Square repraesentierten Flaeche liegt.
     *
     * Falls es Kinder gibt, wird der Knoten rekursiv auch in das
     * betroffene Kind-Square eingetragen.
     *
     * Gibt es keine Kinder, so wird dieses Square falls erforderlich
     * weiter aufgeteilt. Dies ist dann der Fall, wenn es nach dem
     * Eintragen des Knotens mehr als einen Knoten enthaelt
     */

    //============================================================
    protected void put( Node node )
    //============================================================
    {
        // Knoten an Liste _nodes_single oder _nodes_connected
        // anfuegen

        ListItem item;

        if( node.isSingle() ) // Knoten hat keine Segmentverbindungen
        {
            if( _nodes_single == null )
                _nodes_single = new SimpleList();

            item = _nodes_single.add( node );
        }
        else                  // Knoten hat Segmentverbindungen
        {
            if( _nodes_connected == null )
                _nodes_connected = new SimpleList();

            item = _nodes_connected.add( node );
        } // else

        node.items_quadtree.add( ( Object )item );


        // Falls es Kinder gibt, Knoten in entsprechendes
        // Kind-Square eintragen
        //
        if( _children != null )
        {
            _putToChild( node );
            return;              // fertig !!!
        } // if


        // Dieses Square hat keine Kinder

        if( getNoOfNodes() == 1 )
            return;              // Der eingetragene Knoten war der
                                 // erste in diesem Area-Objekt
                                 // Also fertig !!!


        // Alle Objekte ( Knoten, Segmente ) aus diesem Square
        // in die entsprechenden Kind-Squares eintragen.
        // ( Das Array _children sowie die benoetigten Kind-Squares
        //   werden dabei neu erzeugt )

        _children = new QuadTreeSquare[ 4 ];

        // Alle Knoten zufuegen
        //
        for( ListItem i = _getFirstNodeItem();
                      i != null;
                      i = _getNextNodeItem( i ) )
            _putToChild( ( Node )i.value() );

        // Alle Segmente zufuegen
        //
        if( _segments != null )
            for( ListItem i = _segments.first();
                          i != null;
                          i = i.next() )
                _putToChildren( ( Segment )i.value() );
    } // put


    /**
     * Eintragen des Segments <segment> in dieses Square.
     *
     * Es wird davon ausgegangen, dass das Segment die durch dieses
     * Square repraesentierten Flaeche schneidet.
     *
     * Falls es Kinder gibt, wird das Segment auch in die betroffenen
     * Kind-Squares eingetragen.
     */

    //============================================================
    protected void put( Segment segment )
    //============================================================
    {
        // Segment an Segment-Liste anfuegen

        if( _segments == null )
            _segments = new SimpleList();

        ListItem item = _segments.add( segment );

        segment.listitems_quadtree.add( ( Object )item );


        // gegebenenfalls auch noch an die Liste _segments_intersect
        // ( falls beide Knoten ausserhalb dieses Squares liegen )

        Node node1 = segment.getNode1();
        Node node2 = segment.getNode2();

        int x1 = node1.getX();
        int y1 = node1.getY();
        int x2 = node2.getX();
        int y2 = node2.getY();

        boolean outside_1 = (    x1 < _x_min
                              || x1 > _x_max
                              || y1 < _y_min
                              || y1 > _y_max );

        boolean outside_2 = (    x2 < _x_min
                              || x2 > _x_max
                              || y2 < _y_min
                              || y2 > _y_max );

        if( outside_1 && outside_2 )
        {
            if( _segments_intersect == null )
                _segments_intersect =
                    new SquareSegments( _x_min,
                                        _y_min,
                                        _x_max - _x_min + 1 );

            TreeItem[] items = _segments_intersect.
                                    addSegment( segment );

            segment.treeitems_quadtree.add( ( Object )items );
        } // if



        // Falls es Kinder gibt, Segment in entsprechende
        // Kind-Squares eintragen
        //
        if( _children != null )
            _putToChildren( segment );
    } // put


    /**
     * Reorganisieren des QuadTreeSquares nach Entfernen des Knotens
     * <node> ( entstandene leere Blaetter des Trees werden ggf.
     * entfernt ).
     */

    //============================================================
    protected void hasRemoved( Node node )
    //============================================================
    {
        // Falls es Kinder gibt, wird zunaechst das betroffene
        // Kind-Square aktualisiert
        //
        if( _children != null )
        {
            int quadrant = _getQuadrant( node.getX(), node.getY() );
            _children[ quadrant ].hasRemoved( node );
        } // if

        _hasRemoved(); // Aktualisieren dieses Squares
    } // hasRemoved



    /**
     * Reorganisieren des QuadTreeSquares nach Entfernen des Segments
     * <segment> ( entstandene leere Blaetter des Trees werden ggf.
     * entfernt ).
     */

    //============================================================
    protected void hasRemoved( Segment segment )
    //============================================================
    {
        // Falls es Kinder gibt, werden zunaechst die betroffenen
        // Kind-Squares aktualisiert
        //
        if( _children != null )
        {
            int number = _getQuadranten( segment );

            for( int i = 0; i < number; i++ )
            {
                short quadrant = _quadranten[ i ];
                _children[ quadrant ].hasRemoved( segment );
            } // for
        } // if

        _hasRemoved(); // Aktualisieren dieses Squares
    } // hasRemoved


    /**
     * @return  Anzahl der Knoten, die sich in diesem Square
     *          befinden
     */

    //============================================================
    protected int getNoOfNodes()
    //============================================================
    {
        int number = 0;

        if( _nodes_single != null )
            number += _nodes_single.length();

        if( _nodes_connected != null )
            number += _nodes_connected.length();

        return number;
    } // getNoOfNodes


    /**
     * @return  Anzahl der Knoten mit Segmentverbindungen, die sich in
     *          diesem Square befinden
     */

    //============================================================
    protected int getNoOfConnectedNodes()
    //============================================================
    {
        return ( _nodes_connected == null ) ?
               0 :
               _nodes_connected.length();
    } // getNoOfConnectedNodes


    /**
     * @return  Anzahl der Knoten ohne Segmentverbindungen, die sich in
     *          diesem Square befinden
     */

    //============================================================
    protected int getNoOfSingleNodes()
    //============================================================
    {
        return ( _nodes_single == null ) ?
               0 :
               _nodes_single.length();
    } // getNoOfSingleNodes


    /**
     * @return  Anzahl der Segmente, die dieses Square schneiden
     */

    //============================================================
    protected int getNoOfSegments()
    //============================================================
    {
        return ( _segments == null ) ? 0 : _segments.length();
    } // getNoOfSegments



    //------------------------------------------------------------
    // Tests auf Schnitte fuer das erlaubte Setzen von neuen Knoten
    // und Segmenten.
    //------------------------------------------------------------


    /**
     * Testen, ob ein in diesem Square befindlicher Knoten auf der
     * Anfrage-Flaeche < area> liegt.
     *
     * @return  true <==> Ein Knoten dieses Squares liegt auf der
     *                    Anfrageflaeche < area>
     */

    //============================================================
    protected boolean nodeIntersectsArea( QueryArea area )
    //============================================================
    {
        int no_of_nodes = getNoOfNodes();

        if( no_of_nodes == 0
            )
            return false; // In diesem Square befindet sich kein Knoten,
                          // also liegt auch keiner auf der
                          // Anfrageflaeche area

        if( area.enclosesRectangle( _x_min, _y_min, _x_max, _y_max )
            )
            return true; // In diesem Square befindet sich mindestens
                         // ein Knoten. Da dieses Square vollstaendig
                         // innerhalb der Anfrageflaeche area liegt,
                         // liegt also mindestens ein Knoten auf dieser
                         // Flaeche.

        if(    _children == null
               // Es existieren keine Kind-Squares

            || no_of_nodes < _MIN_OBJECTS_TO_GO_DEEPER )
               // Die geringe Anzahl der Knoten erfordert kein
               // genaueres Untersuchen der Kind-Squares
        {
            for( ListItem item = _getFirstNodeItem();
                          item != null;
                          item = _getNextNodeItem( item ) )
            {
                Node node = ( Node )item.value();

                if( area.intersects( node ) )
                    return true;

            } // for

            return false;
        } // if


        if( _quadranten == null )
            _quadranten = new short[ 4 ];

        int number = area.getQuadranten( _x_east, _y_north, _quadranten );

        for( int i = 0; i < number; i++ )
        {
            short quadrant = _quadranten[ i ];

            if( _children[ quadrant ] != null )
            {
                if( _children[ quadrant ].nodeIntersectsArea( area )
                    )
                    return true;
            } // if
        } // for

        return false;
    } // nodeIntersectsArea


    /**
     */

    //============================================================
    protected boolean segmentIntersectsOutline( QueryArea  area )
    //============================================================
    {
        int no_of_segments = getNoOfSegments();

        if( no_of_segments == 0
            )
            return false; // In diesem Square befindet sich kein Segment
                          // also schneidet auch keines die Outline
                          // der Anfrageflaeche.

        if( area.enclosesRectangle( _x_min - 1, _y_min - 1,
                                    _x_max + 1, _y_max + 1 )
            )
            return false; // Da dieses Square die Outline der
                          // Anfrageflaeche nicht beruehrt koennen die
                          // enthaltenen Segmente ignoriert werden


        if(    _children == null
               // Es gibt keine weitere Aufteilung in Kind-Squares

            || no_of_segments < _MIN_OBJECTS_TO_GO_DEEPER )
               // Die geringe Anzahl der Segmente erfordert kein
               // genaueres Untersuchen der Kind-Squares
        {
            for( ListItem item = _segments.first();
                          item != null;
                          item = item.next() )
            {
                Segment seg = ( Segment )( item.value() );

                if( area.intersects( seg ) )
                    return true;
            } // for

            return false;
        } // if


        if( _quadranten == null )
            _quadranten = new short[ 4 ];

        int number = area.getQuadranten( _x_east, _y_north, _quadranten );

        for( int i = 0; i < number; i++ )
        {
            short quadrant = _quadranten[ i ];

            if( _children[ quadrant ] != null )
            {
                if( _children[ quadrant ].
                        segmentIntersectsOutline( area )
                    )
                    return true;
            } // if
        } // for

        return false;
    } // segmentIntersectsOutline



    /**
     */

    //============================================================
    protected boolean newSegmentIntersectsNodeSquare( int x1,
                                                      int y1,
                                                      int x2,
                                                      int y2 )
    //============================================================
    {
        int no_of_nodes = getNoOfNodes();

        if( no_of_nodes == 0
            )
            return false; // In diesem Square befindet sich kein Knoten

        if( ! Calculator.lineIntersectsRect(
                    x1, y1, x2, y2,
                    _x_min - 1, _y_min - 1, _x_max - 1, _y_max - 1 )
            )
            return false;

        if(    _children == null
               // Es existieren keine Kind-Squares

            || no_of_nodes < _MIN_OBJECTS_TO_GO_DEEPER )
               // Die geringe Anzahl der Knoten erfordert kein
               // genaueres Untersuchen der Kind-Squares
        {
            for( ListItem item = _getFirstNodeItem();
                          item != null;
                          item = _getNextNodeItem( item ) )
            {
                Node node = ( Node )item.value();

                int x = node.getX();
                int y = node.getY();

                if(    ( x == x1 && y == y1 )
                    || ( x == x2 && y == y2 )
                    )
                    continue;

                if( Calculator.lineIntersectsRect(
                        x1, y1, x2, y2,
                        x - 1, y - 1, x + 1, y + 1 )
                    )
                    return true;
            } // for

            return false;
        } // if


        for( int quadrant = 0; quadrant < 4; quadrant++ )
        {
            if(    _children[ quadrant ] != null
                && _children[ quadrant ].
                        newSegmentIntersectsNodeSquare( x1, y1, x2, y2 )
                )
                return true;
        } // for

        return false;
    } // newSegmentIntersectsNodeSquare


    /**
     */

    //============================================================
    protected boolean newSegmentIntersectsSegment( int x1,
                                                   int y1,
                                                   int x2,
                                                   int y2 )
    //============================================================
    {
        int no_of_segments = getNoOfSegments();

        if( no_of_segments == 0
            )
            return false; // In diesem Square befindet sich kein Segment
                          // also schneidet auch keines die Linie
                          // (x1,y1)---(x2,y2)


        if(    _children == null
               // Es gibt keine weitere Aufteilung in Kind-Squares

            || no_of_segments < _MIN_OBJECTS_TO_GO_DEEPER )
               // Die geringe Anzahl der Segmente erfordert kein
               // genaueres Untersuchen der Kind-Squares
        {
            for( ListItem item = _segments.first();
                          item != null;
                          item = item.next() )
            {
                Segment seg = ( Segment )( item.value() );

                Node node1 = seg.getNode1();
                Node node2 = seg.getNode2();

                if( Calculator.lineIntersectsLine(
                       x1, y1, x2, y2,
                       node1.getX(), node1.getY(),
                       node2.getX(), node2.getY() )
                    )
                    return true;
            } // for

            return false;
        } // if


        // Es existieren also auf jeden Fall Kind-Squares

        int number = _getQuadranten( x1, y1, x2, y2 );

        for( int i = 0; i < number; i++ )
        {
            short quadrant = _quadranten[ i ];

            if(    _children[ quadrant ] != null
                && _children[ quadrant ].
                        newSegmentIntersectsSegment( x1, y1, x2, y2 )
                )
                return true;
        } // for

        return false;
    } // newSegmentIntersectsSegment





    //------------------------------------------------------------
    // Ermittlung von Knoten und Segmenten
    //------------------------------------------------------------


    /**
     */

    //============================================================
    protected void getNodes( QueryArea  area,
                             SimpleList list_single,
                             SimpleList list_connected )
    //============================================================
    {
        int no_of_nodes_single = 0;
        if(    list_single != null
            && _nodes_single != null
            )
            no_of_nodes_single = _nodes_single.length();

        int no_of_nodes_connected = 0;
        if(    list_connected != null
            && _nodes_connected != null
            )
            no_of_nodes_connected = _nodes_connected.length();

        int no_of_nodes =   no_of_nodes_single
                          + no_of_nodes_connected;

        if( no_of_nodes == 0
            )
            return;

        boolean inside = area.enclosesRectangle( _x_min, _y_min,
                                                 _x_max, _y_max );

        if(    inside
               // Dieses Square liegt vollstaendig innerhalb der
               // Suchflaeche

            || _children == null

            || no_of_nodes < _MIN_OBJECTS_TO_GO_DEEPER )
               // Die geringe Anzahl der Knoten erfordert kein
               // genaueres Untersuchen der Kind-Squares
        {

            if( no_of_nodes_single > 0 )
            {
                for( ListItem item = _nodes_single.first();
                              item != null;
                              item = item.next() )
                {
                    Node node = ( Node )( item.value() );

                    if(    inside
                        || area.intersects( node )
                        )
                        list_single.add( node );
                } // for
            } // if

            if( no_of_nodes_connected > 0 )
            {
                for( ListItem item = _nodes_connected.first();
                              item != null;
                              item = item.next() )
                {
                    Node node = ( Node )( item.value() );

                    if(    inside
                        || area.intersects( node )
                        )
                        list_connected.add( node );
                } // for
            } // if

            return;            // fertig !!!
        } // if


        if( _quadranten == null )
            _quadranten = new short[ 4 ];

        int number = area.getQuadranten( _x_east, _y_north, _quadranten );

        for( int i = 0; i < number; i++ )
        {
            short quadrant = _quadranten[ i ];

            if( _children[ quadrant ] != null )
                _children[ quadrant ].getNodes( area,
                                                list_single,
                                                list_connected );
        } // for

    } // getNodes


    /**
     */

    //============================================================
    protected void getOutlineSegments( QueryArea  area,
                                       SimpleList list )
    //============================================================
    {
        int no_of_segments = getNoOfSegments();

        if(    no_of_segments == 0
            || area.enclosesRectangle( _x_min - 1, _y_min - 1,
                                       _x_max + 1, _y_max + 1 )

            )
            return;


        if(    _children == null
               // Es gibt keine weitere Aufteilung in Kind-Squares

            || no_of_segments < _MIN_OBJECTS_TO_GO_DEEPER )
               // Die geringe Anzahl der Segmente erfordert kein
               // genaueres Untersuchen der Kind-Squares
        {
            for( ListItem item = _segments.first();
                          item != null;
                          item = item.next() )
            {
                Segment seg = ( Segment )( item.value() );

                if( ! seg.is_marked )
                {
                    list.add( seg );
                    seg.is_marked = true;
                } // if
            } // for

            return;            // fertig !!!
        } // if


        if( _quadranten == null )
            _quadranten = new short[ 4 ];

        int number = area.getQuadranten( _x_east, _y_north, _quadranten );

        for( int i = 0; i < number; i++ )
        {
            short quadrant = _quadranten[ i ];

            if( _children[ quadrant ] != null )
                _children[ quadrant ].getOutlineSegments( area, list );
        } // for

    } // getOutlineSegments


    /**
     */

    //============================================================
    protected Position getNearestPosition( QueryArea area,
                                           Position  pos )
    //============================================================
    {
        int no_of_nodes_single = getNoOfSingleNodes();

//+
        return null;
    } // getNearestPosition



    /**
     * Ermitteln des Knotens mit den Koordinaten (<x>,<y>)
     *
     * @return  Ermittelter Knoten
     *          oder <null> falls kein Knoten die Koordinaten
     *          (<x>,<y>) hat
     */

    //============================================================
    protected Node getNode( int x, int y )
    //============================================================
    {
        int no_of_nodes = getNoOfNodes();

        if( no_of_nodes == 0 )
            return null;  // Es gibt keinen Knoten in diesem Square

        if( no_of_nodes == 1 )
        {
            Node node = ( Node )( _getFirstNodeItem().value() );

            if( node.getX() == x && node.getY() == y
                )
                return node;

            return null;
        } // if

        QuadTreeSquare child = _children[ _getQuadrant( x, y ) ];

        if( child == null )
            return null;

        return child.getNode( x, y );
    } // getNode


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
        if(    _segments == null
            || _segments.length() == 0 )
            return null;  // Es gibt keine Segmente in diesem Square

        if(    _children == null
                    // Es gibt keine Kind-Squares

            || _segments.length() < _MIN_OBJECTS_TO_GO_DEEPER )
                    // Die geringe Anzahl der Segmente erfordert kein
                    // genaueres Untersuchen der Kind-Squares
        {
            for( ListItem i = _segments.first();
                 i != null;
                 i = i.next() )
            {
                Segment seg = ( Segment )i.value();

                Node node1 = seg.getNode1();
                Node node2 = seg.getNode2();

                if( Calculator.segmentIntersectsPoint(
                                    node1.getX(), node1.getY(),
                                    node2.getX(), node2.getY(),
                                    x, y,
                                    false,
                                    false )
                    )
                    return seg;

            } // for

            return null;
        } // if

        QuadTreeSquare child = _children[ _getQuadrant( x, y ) ];

        if( child == null )
            return null;

        return child.getSegment( x, y );
    } // getSegment
*/



    /**
     */

    //============================================================
    protected long getNearestNodes( int        x,
                                    int        y,
                                    long       max_square_dist,
                                    SimpleList list )
    //============================================================
    {
        int no_of_nodes = getNoOfNodes();

        if( no_of_nodes == 0 )
            return max_square_dist; // Es gibt keinen Knoten in diesem
                                    // Square

        if(    _children == null
                // Es existieren keine Kind-Squares

            || no_of_nodes < _MIN_OBJECTS_TO_GO_DEEPER
                // Die geringe Anzahl der Knoten erfordert kein
                // genaueres Untersuchen der Kind-Squares
            )
        {
            for( ListItem item = _getFirstNodeItem();
                          item != null;
                          item = _getNextNodeItem( item ) )
            {
                Node node = ( Node )item.value();

                long square_dist =
                    Calculator.squareDistance( node.getX(), node.getY(),
                                               x, y );


                if( square_dist <= max_square_dist )
                {
                    if( square_dist < max_square_dist )
                    {
                        list.clear();
                        max_square_dist = square_dist;
                    } // if

                    list.add( node );
                } // if

            } // for
        }
        else
        {
            // Suche nach Knoten in Kind-Squares

            _getQuadranten( x, y );

            for( int i = 0; i < 4; i++ )
            {
                short quadrant = _quadranten[ i ];

                if( _isOutside( quadrant, x, y, max_square_dist ) )
                    break;

                if( _children[ quadrant ] != null )
                {
                    max_square_dist =
                        _children[ quadrant ].
                            getNearestNodes( x, y, max_square_dist, list );
                } // if
            } // for
        } // else

        return max_square_dist;
    } // getNearestNodes


    /**
     */

    //============================================================
    private boolean _isOutside( short quadrant,
                                int   x,
                                int   y,
                                long  max_square_distance )
    //============================================================
    {
        int xmin, ymin, xmax, ymax;

        if( ( quadrant & W_E_MASK ) ==  WEST )
        {
            xmin = _x_min;
            xmax = _x_east - 1;
        }
        else
        {
            xmin = _x_east;
            xmax = _x_max;
        } // else

        if( ( quadrant & S_N_MASK ) ==  SOUTH )
        {
            ymin = _y_min;
            ymax = _y_north - 1;
        }
        else
        {
            ymin = _y_north;
            ymax = _y_max;
        } // else

        if( x >= xmin && x <= xmax && y >= ymin && y <= ymax )
            return false;

//+

// Das ist noch nicht richtig so
        return    ( Calculator.squareDistance( x, y, xmin, ymin )
                    > max_square_distance )
               && ( Calculator.squareDistance( x, y, xmin, ymax )
                    > max_square_distance )
               && ( Calculator.squareDistance( x, y, xmax, ymin )
                    > max_square_distance )
               && ( Calculator.squareDistance( x, y, xmax, ymax )
                    > max_square_distance );
    } // _isOutside


    /**
     */

    //============================================================
    private void _getQuadranten( int x, int y )
    //============================================================
    {
        if( _quadranten == null )
            _quadranten = new short[ 4 ];

        int x_diff = x - _x_east;
        int y_diff = y - _y_north;

        if( x_diff >= 0 )
        {
            if( y_diff >= 0 )                       // NORTH_EAST
            {
                _quadranten[ 0 ] = NORTH_EAST;
                _quadranten[ 3 ] = SOUTH_WEST;

                if( x_diff < y_diff )
                {
                    _quadranten[ 1 ] = NORTH_WEST;
                    _quadranten[ 2 ] = SOUTH_EAST;
                }
                else
                {
                    _quadranten[ 1 ] = SOUTH_EAST;
                    _quadranten[ 2 ] = NORTH_WEST;
                } // else
            }
            else                                    // SOUTH_EAST
            {
                _quadranten[ 0 ] = SOUTH_EAST;
                _quadranten[ 3 ] = NORTH_WEST;

                if( x_diff < -y_diff )
                {
                    _quadranten[ 1 ] = SOUTH_WEST;
                    _quadranten[ 2 ] = NORTH_EAST;
                }
                else
                {
                    _quadranten[ 1 ] = NORTH_EAST;
                    _quadranten[ 2 ] = SOUTH_WEST;
                } // else
            } // else
        }
        else
        {
            if( y_diff >= 0 )                       // NORTH_WEST
            {
                _quadranten[ 0 ] = NORTH_WEST;
                _quadranten[ 3 ] = SOUTH_EAST;

                if( -x_diff < y_diff )
                {
                    _quadranten[ 1 ] = NORTH_EAST;
                    _quadranten[ 2 ] = SOUTH_WEST;
                }
                else
                {
                    _quadranten[ 1 ] = SOUTH_WEST;
                    _quadranten[ 2 ] = NORTH_EAST;
                } // else
            }
            else                                    // SOUTH_WEST
            {
                _quadranten[ 0 ] = SOUTH_WEST;
                _quadranten[ 3 ] = NORTH_EAST;

                if( -x_diff < -y_diff )
                {
                    _quadranten[ 1 ] = SOUTH_EAST;
                    _quadranten[ 2 ] = NORTH_WEST;
                }
                else
                {
                    _quadranten[ 1 ] = NORTH_WEST;
                    _quadranten[ 2 ] = SOUTH_EAST;
                } // else

            } // else
        } // else
    } // _getQuadranten






    /**
     */

    //============================================================
    protected Segment getNearestSegment( int  x,
                                         int  y,
                                         long max_square_dist )
    //============================================================
    {
//+
        return null;
    } // getNearestSegment




    //------------------------------------------------------------
    // Tests auf Schnitte
    //------------------------------------------------------------


    /**
     * Testen, ob die Strecke von (<x1>,<y1>) --- (<x2>,<y2>) in dieses
     * Square eingetragene Objekte ( Knoten oder Segmente ) schneidet
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
        int no_of_nodes = getNoOfNodes();

        if( no_of_nodes > 1 )
        {
            // Es existieren also auf jeden Fall Kind-Squares

            // Auf Schnitte mit Objekten in den betroffenen Kind-Squares
            // testen

            int number = _getQuadranten( x1, y1, x2, y2 );

            for( int i = 0; i < number; i++ )
            {
                short quadrant = _quadranten[ i ];

                if(    _children[ quadrant ] != null
                    && _children[ quadrant ].intersects( x1, y1, x2, y2,
                                                         ignore_1,
                                                         ignore_2 )
                    )
                    return true; // Schnitt gefunden
            } // for

            return false; // Kein Schnitt in Kind-Squares gefunden
        } // if


        // Es existiert also hoechstens ein Knoten in diesem Square

        if(    _nodes_single != null
            && _nodes_single.length() == 1 )
        {
            // Es existiert genau ein unverbundenener Knoten in diesem
            // Square

            ListItem i = _nodes_single.first();
            Node node = ( Node )( i.value() );

            if( Calculator.segmentIntersectsPoint(
                                x1, y1,
                                x2, y2,
                                node.getX(), node.getY(),
                                ignore_1,
                                ignore_2 )
                )
                return true;
        }
        else
        if(    _nodes_connected != null
            && _nodes_connected.length() == 1 )
        {
            // Es existiert genau ein verbundenener Knoten in diesem
            // Square

            ListItem item = _nodes_connected.first();
            Node node = ( Node )( item.value() );

            RayDirection ray_dir = new RayDirection( node.getX(),
                                                     node.getY(),
                                                     x1,
                                                     y1 );
            TourVertex[] vertices = node.getVertices( ray_dir );
            TourVertex   vertex = vertices[ 0 ];

            Segment seg1 = vertex.getSegment( TourVertex.LEFT_HAND );
            Segment seg2 = vertex.getSegment( TourVertex.RIGHT_HAND );

            Segment seg = seg1;
            for( int i = 0; i < 2; i++ )
            {
                Node node1 = seg.getNode1();
                Node node2 = seg.getNode2();

                if( Calculator.segmentIntersectsSegment(
                                    x1, y1,
                                    x2, y2,
                                    node1.getX(), node1.getY(),
                                    node2.getX(), node2.getY(),
                                    ignore_1,
                                    ignore_2 )
                    )
                    return true;

                if( seg == seg2 )
                    break;

                seg = seg2;
            } // for

        } // if


        if(    _segments_intersect != null
            && ! _segments_intersect.empty() )
        {
            // Es existieren Segmente, die dieses Square vollstaendig
            // schneiden

            boolean outside_1 = (    x1 < _x_min
                                  || x1 > _x_max
                                  || y1 < _y_min
                                  || y1 > _y_max );

            boolean outside_2 = (    x2 < _x_min
                                  || x2 > _x_max
                                  || y2 < _y_min
                                  || y2 > _y_max );



            if( outside_1 || outside_2 )
            {

//+ Hier ist noch ein logischer Fehler ( siehe Unterlagen zum QuadTree )


                Segment[] segments = ( outside_1 ) ?
                                     _segments_intersect.
                                        getNeighbourSegments( x1, y1,
                                                              x2, y2 ) :
                                     _segments_intersect.
                                        getNeighbourSegments( x2, y2,
                                                              x1, y1 );


                for( int i = 0; i < 2; i++ )
                {
                    Segment seg = segments[ i ];

                    Node node1 = seg.getNode1();
                    Node node2 = seg.getNode2();

                    if( Calculator.segmentIntersectsSegment(
                                        x1, y1,
                                        x2, y2,
                                        node1.getX(), node1.getY(),
                                        node2.getX(), node2.getY(),
                                        ignore_1,
                                        ignore_2 )
                        )
                        return true;

                    if( i == 0 && seg == segments[ 1 ] )
                        break;
                } // for

                return false;
            } // if

            // Es gilt ( ! outside_1 && ! outside_2 )

            for( TreeItem item = _segments_intersect.getFirst();
                          item != null;
                          item = _segments_intersect.getNext( item ) )
            {
                Segment seg = ( Segment )( item.value() );

                Node node1 = seg.getNode1();
                Node node2 = seg.getNode2();

                if( Calculator.segmentIntersectsSegment(
                                    x1, y1,
                                    x2, y2,
                                    node1.getX(), node1.getY(),
                                    node2.getX(), node2.getY(),
                                    ignore_1,
                                    ignore_2 )
                    )
                    return true;
            } // for

        } // if

        return false;
    } // intersects
*/

    /**
     * Ermitteln des ersten Schnittpunkts der Strecke
     * (<x1>,<y1>)---(<x2>,<y2>) mit einem in diesem Square
     * eingetragenen Objekt ( Knoten oder Segment )
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
        int no_of_nodes = getNoOfNodes();

        if( no_of_nodes > 1 )
        {
            // Es existieren also auf jeden Fall Kind-Squares

            // Auf Schnitte mit Objekten in den betroffenen Kind-Squares
            // testen

            int number = _getQuadranten( x1, y1, x2, y2 );


            for( int i = 0; i < number; i++ )
            {
                short quadrant = _quadranten[ i ];

                if( _children[ quadrant ] == null )
                    continue;

                Position pos = _children[ quadrant ].
                                    getFirstIntersection( x1, y1,
                                                          x2, y2,
                                                          ignore_1,
                                                          ignore_2 );
                if( pos != null )
                    return pos; // Schnitt gefunden
            } // for

            return null; // Kein Schnitt in Kind-Squares gefunden
        } // if


        Position pos = null;

        // Es existiert also hoechstens ein Knoten in diesem Square

        if(    _nodes_single != null
            && _nodes_single.length() == 1 )
        {
            // Es existiert genau ein unverbundenener Knoten in diesem
            // Square

            ListItem i = _nodes_single.first();
            Node node = ( Node )( i.value() );

            if( Calculator.segmentIntersectsPoint(
                                x1, y1,
                                x2, y2,
                                node.getX(), node.getY(),
                                ignore_1,
                                ignore_2 ) )
            {
                pos = new Position( node );
            } // if
        }
        else
        if(    _nodes_connected != null
            && _nodes_connected.length() == 1 )
        {

            // Es existiert genau ein verbundenener Knoten in diesem
            // Square

            ListItem item = _nodes_connected.first();
            Node node = ( Node )( item.value() );

            RayDirection ray_dir = new RayDirection( node.getX(),
                                                     node.getY(),
                                                     x1,
                                                     y1 );
            TourVertex[] vertices = node.getVertices( ray_dir );
            TourVertex   vertex = vertices[ 0 ];

            Segment seg1 = vertex.getSegment( TourVertex.LEFT_HAND );
            Segment seg2 = vertex.getSegment( TourVertex.RIGHT_HAND );

            Segment seg = seg1;
            for( int i = 0; i < 2; i++ )
            {
                Node node1 = seg.getNode1();
                Node node2 = seg.getNode2();

                if( Calculator.segmentIntersectsSegment(
                                    x1, y1,
                                    x2, y2,
                                    node1.getX(), node1.getY(),
                                    node2.getX(), node2.getY(),
                                    ignore_1,
                                    ignore_2 )
                    )
                    return true;

                if( seg == seg2 )
                    break;

                seg = seg2;
            } // for


        } // if


        if(    _segments_intersect != null
            && ! _segments_intersect.empty() )
        {

            // Es existieren Segmente, die dieses Square vollstaendig
            // schneiden

            boolean outside_1 = (    x1 < _x_min
                                  || x1 > _x_max
                                  || y1 < _y_min
                                  || y1 > _y_max );

            boolean outside_2 = (    x2 < _x_min
                                  || x2 > _x_max
                                  || y2 < _y_min
                                  || y2 > _y_max );



            if( outside_1 || outside_2 )
            {
                Segment[] segments = ( outside_1 ) ?
                                     _segments_intersect.
                                        getNeighbourSegments( x1, y1,
                                                              x2, y2 ) :
                                     _segments_intersect.
                                        getNeighbourSegments( x2, y2,
                                                              x1, y1 );


                for( int i = 0; i < 2; i++ )
                {
                    Segment seg = segments[ i ];

                    Node node1 = seg.getNode1();
                    Node node2 = seg.getNode2();

                    if( Calculator.segmentIntersectsSegment(
                                        x1, y1,
                                        x2, y2,
                                        node1.getX(), node1.getY(),
                                        node2.getX(), node2.getY(),
                                        ignore_1,
                                        ignore_2 )
                        )
                        return true;

                    if( i == 0 && seg == segments[ 1 ] )
                        break;
                } // for

                return false;
            } // if

            // Es gilt ( ! outside_1 && ! outside_2 )

            for( TreeItem item = _segments_intersect.getFirst();
                          item != null;
                          item = _segments_intersect.getNext( item ) )
            {
                Segment seg = ( Segment )( item.value() );

                Node node1 = seg.getNode1();
                Node node2 = seg.getNode2();

                if( Calculator.segmentIntersectsSegment(
                                    x1, y1,
                                    x2, y2,
                                    node1.getX(), node1.getY(),
                                    node2.getX(), node2.getY(),
                                    ignore_1,
                                    ignore_2 )
                    )
                    return true;
            } // for


        } // if

        return pos;
    } // getFirstIntersection
*/


    /**
     * Zeichnen des Squares
     */

    //============================================================
    protected void paint( Graphics  g,
                          Rectangle clip,
                          AffineTransform transform )
    //============================================================
    {
        if( _children == null )
            return;

        Point2D.Float min = new Point2D.Float( _x_min, _y_min );
        Point2D.Float max = new Point2D.Float( _x_max, _y_max );

        transform.transform( min, min );
        transform.transform( max, max );

        int x1 = Math.round( min.x );
        int y1 = Math.round( min.y );
        int x2 = Math.round( max.x );
        int y2 = Math.round( max.y );


        int min_x = ( x1 <= x2 ) ? x1 : x2;
        int max_x = ( x1 <= x2 ) ? x2 : x1;
        int min_y = ( y1 <= y2 ) ? y1 : y2;
        int max_y = ( y1 <= y2 ) ? y2 : y1;

        if(    max_x < clip.x
            && min_x > clip.x + clip.width
            && max_y < clip.y
            && min_y > clip.y + clip.width )
        {
            return;
        } // if

        if(    (long)max_x - min_x < 6
            || (long)max_y - min_y < 6 )
        {
            return;
        } // if


        Point2D.Float center = new Point2D.Float( ( float )_x_east  - ( float )0.5,
                                            	  ( float )_y_north - ( float )0.5 );

        transform.transform( center, center );

        int center_x = Math.round( center.x );
        int center_y = Math.round( center.y );


        g.setColor( Color.lightGray );

        g.drawLine( min_x + 3, center_y,
                    max_x - 3, center_y );

        g.drawLine( center_x, min_y + 3,
                    center_x, max_y - 3);

        g.setColor( Color.gray.brighter() );

        g.fillRect( center_x - 2, center_y - 2, 5, 5 );

        for( int quadrant = 0; quadrant < 4; quadrant ++ )
        {
            if( _children[ quadrant ] != null )
                _children[ quadrant ].paint( g, clip, transform );
        } // for
    } // paint




    //************************************************************
    // private methods
    //************************************************************


    /**
     * Zufuegen des Knotens <node> zum Kind-Square in welchem
     * er liegt.
     */

    //============================================================
    private void _putToChild( Node node )
    //============================================================
    {
        short quadrant = _getQuadrant( node.getX(), node.getY() );

        // Neues Kind-Square erzeugen, falls noch nicht vorhanden
        if( _children[ quadrant ] == null )
            _children[ quadrant ] = new QuadTreeSquare( this,
                                                        quadrant );

        // Knoten in Kind-Square einfuegen
        _children[ quadrant ].put( node );

    } // _putToChild


    /**
     * Zufuegen des Segments <segment> zu den Kind-Squares welche
     * es durchlaeuft.
     */

    //============================================================
    private void _putToChildren( Segment segment )
    //============================================================
    {
        // Quadranten ermitteln durch die das Segment laeuft
        //
        int number = _getQuadranten( segment );

        // Segment in zugehoerige Kind-Squares einfuegen

        for( int i = 0; i < number; i++ )
        {
            short quadrant = _quadranten[ i ];

            if( _children[ quadrant ] == null )
                _children[ quadrant ] = new QuadTreeSquare( this,
                                                            quadrant );
            _children[ quadrant ].put( segment );
        } // for

    } // _putToChildren


    /**
     * Ermitteln des Quadranten in dem der Punkt (<x>,<y>) liegt
     *
     * @return NORTH_WEST, NORTH_EAST, SOUTH_WEST oder SOUTH_EAST
     */

    //============================================================
    private short _getQuadrant( int x, int y )
    //============================================================
    {
        short we = ( x < _x_east  ) ? WEST  : EAST;
        short sn = ( y < _y_north ) ? SOUTH : NORTH;

        return ( short )( we | sn );

    } // _getQuadrant


    /**
     * Ermitteln der Quadranten die vom Rechteck <rect> geschnitten
     * werden
     *
     * Die geschnittenen Quadranten werden in das Hilfsarray
     * _quadranten eingetragen ( @see private variables )
     *
     * @return  Anzahl der geschnittenen Quadranten
     */

    //============================================================
    private int _getQuadranten( Rectangle rect )
    //============================================================
    {
        if( _quadranten == null )
            _quadranten = new short[ 4 ];

        int number = 0;

        int xmin = rect.x;
        int ymin = rect.y;
        int xmax = xmin + rect.width - 1;
        int ymax = ymin + rect.height - 1;


        if( xmax >= _x_east )
        {
            if( ymax >= _y_north )
                _quadranten[ number ++ ] = NORTH_EAST;

            if( ymin < _y_north )
                _quadranten[ number ++ ] = SOUTH_EAST;
        } // if

        if( xmin < _x_east )
        {
            if( ymax >= _y_north )
                _quadranten[ number ++ ] = NORTH_WEST;

            if( ymin < _y_north )
                _quadranten[ number ++ ] = SOUTH_WEST;
        } // if

        return number;
    } // _getQuadranten


    /**
     * Ermitteln der Quadranten, die auf dem Weg von Knoten Node1 nach
     * Knoten Node2 des Segments <segment> geschnitten werden.
     *
     * Es ist bereits bekannt, dass die Strecke mindestens einen
     * Quadranten dieses Squares schneidet
     *
     * Die geschnittenen Quadranten werden in der Reihenfolge wie
     * sie von Node1 nach Node2 angetroffen werden in das Hilfsarray
     * _quadranten eingetragen ( @see private variables )
     *
     * @return  Anzahl der durchlaufenen Quadranten ( mindestens 1 )
     */

    //============================================================
    private int _getQuadranten( Segment segment )
    //============================================================
    {
        Node node1 = segment.getNode1();
        Node node2 = segment.getNode2();

        return _getQuadranten( node1.getX(),
                               node1.getY(),
                               node2.getX(),
                               node2.getY() );
    } // _getQuadranten


    /**
     * Ermitteln der Quadranten, die auf dem Weg von (<x1>,<y1>)
     * nach (<x2>,<y2>) geschnitten werden.
     *
     * Es ist bereits bekannt, dass die Strecke mindestens einen
     * Quadranten dieses Squares schneidet
     *
     * Die geschnittenen Quadranten werden in der Reihenfolge wie
     * sie von (<x1,y1>) nach (<x2>,<y2>) angetroffen werden in
     * das Hilfsarray _quadranten eingetragen ( @see private variables )
     *
     * @return  Anzahl der durchlaufenen Quadranten ( mindestens 1 )
     */

    //============================================================
    private int _getQuadranten( int x1, int y1, int x2, int y2 )
    //============================================================
    {
        if( _quadranten == null )
            _quadranten = new short[ 4 ];

        // Ermitteln der Haelften in denen der Punkt (x1,y1) liegt
        //
        short we_1 = ( x1 >= _x_east  ) ? EAST  : WEST;
        short sn_1 = ( y1 >= _y_north ) ? NORTH : SOUTH;

        // Ermitteln der Haelften in denen der Punkt (x2,y2) liegt
        //
        short we_2 = ( x2 >= _x_east  ) ? EAST  : WEST;
        short sn_2 = ( y2 >= _y_north ) ? NORTH : SOUTH;


        if( we_1 == we_2 )
        {
            if( sn_1 == sn_2 )        // we_1 == we_2  &&  sn_1 == sn_2
            {
                // Beide Punkte liegen im gleichen Quadranten

                _quadranten[ 0 ] = ( short )( we_1 | sn_1 );
                return 1;
            }
            else                      // we_1 == we_2  &&  sn_1 != sn_2
            {
                if( we_1 == WEST )
                {
                    if( sn_1 == SOUTH )  // SOUTH_WEST -> NORTH_WEST
                    {
                        return _getQuadranten( x1, y1, x2, y2,
                                               _y_north,
                                               _x_min,
                                               SOUTH_WEST,
                                               NORTH_WEST );
                    }
                    else                  // NORTH_WEST -> SOUTH_WEST
                    {
                        return _getQuadranten( x1, -y1, x2, -y2,
                                               -_y_north + 1,
                                               _x_min,
                                               NORTH_WEST,
                                               SOUTH_WEST );
                    } // else
                }
                else
                {
                    if( sn_1 == SOUTH )  // SOUTH_EAST -> NORTH_EAST
                    {
                        return _getQuadranten( -x1, y1, -x2, y2,
                                               _y_north,
                                               -_x_max,
                                               SOUTH_EAST,
                                               NORTH_EAST );
                    }
                    else                  // NORTH_EAST -> SOUTH_EAST
                    {
                        return _getQuadranten( -x1, -y1, -x2, -y2,
                                               -_y_north + 1,
                                               -_x_max,
                                               NORTH_EAST,
                                               SOUTH_EAST );
                    } // else
                } // else
            } // else
        }
        else
        {
            if( sn_1 == sn_2 )        // we_1 != we_2  &&  sn_1 == sn_2
            {
                if( sn_1 == SOUTH )
                {
                    if( we_1 == WEST )   // SOUTH_WEST -> SOUTH_EAST
                    {
                        return _getQuadranten( y1, x1, y2, x2,
                                               _x_east,
                                               _y_min,
                                               SOUTH_WEST,
                                               SOUTH_EAST );
                    }
                    else                  // SOUTH_EAST -> SOUTH_WEST
                    {
                        return _getQuadranten( y1, -x1, y2, -x2,
                                               -_x_east + 1,
                                               _y_min,
                                               SOUTH_EAST,
                                               SOUTH_WEST );
                    } // else
                }
                else
                {
                    if( we_1 == WEST )   // NORTH_WEST -> NORTH_EAST
                    {
                        return _getQuadranten( -y1, x1, -y2, x2,
                                               _x_east,
                                               -_y_max,
                                               NORTH_WEST,
                                               NORTH_EAST );
                    }
                    else                  // NORTH_EAST -> NORTH_WEST
                    {
                        return _getQuadranten( -y1, -x1, -y2, -x2,
                                               -_x_east + 1,
                                               -_y_max,
                                               NORTH_EAST,
                                               NORTH_WEST );
                    } // else
                } // else
            }
            else                      // we_1 != we_2  &&  sn_1 != sn_2
            {
                if( we_1 == WEST )
                {
                    if( sn_1 == SOUTH )  // SOUTH_WEST -> NORTH_EAST
                    {
                        return _getQuadranten( x1, y1, x2, y2,
                                               _x_min,
                                               _x_east,
                                               _x_max,
                                               _y_min,
                                               _y_north,
                                               _y_max,
                                               SOUTH_WEST,
                                               NORTH_WEST,
                                               SOUTH_EAST,
                                               NORTH_EAST );
                    }
                    else                  // NORTH_WEST -> SOUTH_EAST
                    {
                        return _getQuadranten( x1, -y1, x2, -y2,
                                               _x_min,
                                               _x_east,
                                               _x_max,
                                               -_y_max,
                                               -_y_north + 1,
                                               -_y_min,
                                               NORTH_WEST,
                                               SOUTH_WEST,
                                               NORTH_EAST,
                                               SOUTH_EAST );
                    } // else
                }
                else
                {
                    if( sn_1 == SOUTH )  // SOUTH_EAST -> NORTH_WEST
                    {
                        return _getQuadranten( -x1, y1, -x2, y2,
                                               -_x_max,
                                               -_x_east + 1,
                                               -_x_min,
                                               _y_min,
                                               _y_north,
                                               _y_max,
                                               SOUTH_EAST,
                                               NORTH_EAST,
                                               SOUTH_WEST,
                                               NORTH_WEST );
                    }
                    else                  // NORTH_EAST -> SOUTH_WEST
                    {
                        return _getQuadranten( -x1, -y1, -x2, -y2,
                                               -_x_max,
                                               -_x_east + 1,
                                               -_x_min,
                                               -_y_max,
                                               -_y_north + 1,
                                               -_y_min,
                                               NORTH_EAST,
                                               SOUTH_EAST,
                                               NORTH_WEST,
                                               SOUTH_WEST );
                    } // else
                } // else
            } // else
        } // else
    } // _getQuadranten


    /**
     * Hilfsmethode fuer
     *      _getQuadranten( x1, y1, x2, y2 )
     *
     * Ermitteln der Quadranten, die auf dem Weg von (<x1>,<y1>)
     * nach (<x2>,<y2>) geschnitten werden.
     *
     * Es ist bereits bekannt, dass die Strecke auf der westlichen
     * Haelfte von Sued nach Nord laeuft und dabei mindestens einen
     * der Quadranten <south_west> oder <north_west> schneidet.
     *
     * Die geschnittenen Quadranten werden in das Hilfsarray
     * _quadranten eingetragen ( @see private variables )
     *
     * @return  Anzahl der geschnittenen Quadranten ( 1 oder 2 )
     */

    //============================================================
    private int _getQuadranten( int   x1,
                                int   y1,
                                int   x2,
                                int   y2,
                                int   y_north,
                                int   x_min,
                                short south_west,
                                short north_west )
    //============================================================
    {
        _quadranten[ 0 ] = south_west;
        _quadranten[ 1 ] = north_west;

        // Falls beide x-Koordinaten groesser oder gleich x_min sind,
        // werden der suedliche und der noerdliche Quadrant
        // durchlaufen
        //
        if(    ( x1 >= x_min && x2 >= x_min )
            || x1 == x2
            )
            return 2;  // fertig


        long d_x2 = x2 - x1;
        long d_y2 = y2 - y1;

        long d_x_min   = x_min   - x1;
        long d_y_north = y_north - y1;

        long value = 2 * ( d_x2 * d_y_north - d_y2 * d_x_min );

        if( value >= ( d_x2 - d_y2 ) )
            return 2;  // x-Koordinate des Schnitts der Strecke mit
                       // der horizontalen Geraden (y_north-0.5) ist
                       // groesser oder gleich x_min-0.5
                       // Also werden beide Quadranten durchlaufen


        // Es wird also nur einer der Quadranten durchlaufen

        if( x2 > x1 )
            _quadranten[ 0 ] = north_west;
            // Andernfalls gilt _quadranten[0] == south_west
            // ( ist bereits gesetzt )

        return 1;
    } // if


    /**
     * Hilfsmethode fuer
     *      _getQuadranten( x1, y1, x2, y2 )
     *
     * Ermitteln der Quadranten, die auf dem Weg von (<x1>,<y1>)
     * nach (<x2>,<y2>) geschnitten werden.
     *
     * Es ist bereits bekannt, dass die Strecke von Sued-West nach
     * Nord-Ost laeuft und dabei mindestens einen Quadranten des
     * Squares schneidet
     *
     * Die geschnittenen Quadranten werden in das Hilfsarray
     * _quadranten eingetragen ( @see private variables )
     *
     * @return  Anzahl der geschnittenen Quadranten ( mindestens 1 )
     */

    //============================================================
    private int _getQuadranten( int   x1,
                                int   y1,
                                int   x2,
                                int   y2,
                                int   x_min,
                                int   x_east,
                                int   x_max,
                                int   y_min,
                                int   y_north,
                                int   y_max,
                                short south_west,
                                short north_west,
                                short south_east,
                                short north_east )
    //============================================================
    {
        long d_x2 = x2 - x1;
        long d_y2 = y2 - y1;

        long d_x_east  = x_east  - x1;
        long d_y_north = y_north - y1;

        long value_y_north = 2 * ( d_x2 * d_y_north ) - d_x2;
        long value_x_east  = 2 * ( d_y2 * d_x_east )  - d_y2;

        if( value_y_north == value_x_east )
        {
            // x-Koordinate des Schnitts der Strecke mit der
            // horizontalen Geraden (y_north-0.5) ist gleich
            // x_east-0.5
            // Es wird also genau der Mittelpunkt des Squares
            // geschnitten und somit alle Quadranten durchlaufen

            _quadranten[ 0 ] = south_west;
            _quadranten[ 1 ] = north_west;
            _quadranten[ 2 ] = south_west;
            _quadranten[ 3 ] = north_east;

            return 4; // fertig
        } // if

        if( value_y_north > value_x_east )
        {
            // x-Koordinate des Schnitts der Strecke mit der
            // horizontalen Geraden (y_north-0.5) ist groesser als
            // x_east-0.5

            long d_x_max = x_max - x1 + 1;
            long value_x_max = 2 * ( d_y2 * d_x_max ) - d_y2;

            long d_y_min = y_min - y1;
            long value_y_min = 2 * ( d_x2 * d_y_min ) - d_x2;

            if( value_y_min <= value_x_east )
            {
                // x-Koordinate des Schnitts der Strecke mit der
                // horizontalen Geraden (y_min-0.5) ist kleiner oder
                // gleich x_east-0.5

                _quadranten[ 0 ] = south_west;
                _quadranten[ 1 ] = south_east;

                if( value_y_north > value_x_max )
                {
                    // x-Koordinate des Schnitts der Strecke mit der
                    // horizontalen Geraden (y_north-0.5) ist groesser
                    // x_max+0.5
                    return 2;
                }
                else
                {
                    _quadranten[ 2 ] = north_east;
                    return 3;
                } // else
            }
            else // value_y_min > value_x_east
            {
                // x-Koordinate des Schnitts der Strecke mit der
                // horizontalen Geraden (y_min-0.5) ist groesser als
                // x_east-0.5

                _quadranten[ 0 ] = south_east;

                if( value_y_north > value_x_max )
                {
                    // x-Koordinate des Schnitts der Strecke mit der
                    // horizontalen Geraden (y_north-0.5) ist groesser
                    // x_max+0.5
                    return 1;
                }
                else
                {
                    _quadranten[ 1 ] = north_east;
                    return 2;
                } // else
            } // else
        }
        else  // value_y_north < value_x_east
        {
            // x-Koordinate des Schnitts der Strecke mit der
            // horizontalen Geraden (y_north-0.5) ist kleiner als
            // x_east-0.5

            long d_x_min = x_min - x1;
            long value_x_min = 2 * ( d_y2 * d_x_min ) - d_y2;

            long d_y_max = y_max - y1 + 1;
            long value_y_max = 2 * ( d_x2 * d_y_max ) - d_x2;

            if( value_y_north >= value_x_min )
            {
                // x-Koordinate des Schnitts der Strecke mit der
                // horizontalen Geraden (y_north-0.5) ist groesser oder
                // gleich x_min-0.5

                _quadranten[ 0 ] = south_west;
                _quadranten[ 1 ] = north_west;

                if( value_y_max < value_x_east )
                {
                    // x-Koordinate des Schnitts der Strecke mit der
                    // horizontalen Geraden (y_max+0.5) ist kleiner
                    // x_east-0.5
                    return 2;
                }
                else
                {
                    _quadranten[ 2 ] = north_east;
                    return 3;
                } // else
            }
            else // value_y_north < value_x_min
            {
                // x-Koordinate des Schnitts der Strecke mit der
                // horizontalen Geraden (y_north-0.5) ist kleiner als
                // x_min-0.5

                _quadranten[ 0 ] = north_west;

                if( value_y_max < value_x_east )
                {
                    // x-Koordinate des Schnitts der Strecke mit der
                    // horizontalen Geraden (y_max+0.5) ist kleiner
                    // x_east-0.5
                    return 1;
                }
                else
                {
                    _quadranten[ 1 ] = north_east;
                    return 2;
                } // else
            } // else
        } // else
    } // _getQuadranten


    /**
     * Aktualisieren dieses Squares, nachdem ein Knoten oder ein
     * Segment aus ihm entfernt wurde
     */

    //============================================================
    private void _hasRemoved()
    //============================================================
    {

        if(    _nodes_single != null
            && _nodes_single.length() == 0
            )
            _nodes_single = null;

        if(    _nodes_connected != null
            && _nodes_connected.length() == 0
            )
            _nodes_connected = null;

        if(    _segments != null
            && _segments.length() == 0
            )
            _segments = null;

        if(    _segments_intersect != null
            && _segments_intersect.length() == 0
            )
            _segments_intersect = null;

        if(    _nodes_single       == null
            && _nodes_connected    == null
            && _segments           == null
            && _segments_intersect == null )
        {
            _children = null;
            if( _parent != null )
                _parent._children[ _quadrant ] = null;
        } // if
    } // _hasRemoved


    /**
     * Hilfsmethode zum aufeinanderfolgenden Durchlaufen der
     * Knotenlisten _nodes_single und _nodes_connected.
     *
     * Das folgende Programmstueck besucht alle Knoten ( erst Liste
     * _nodes_single und dann _nodes_connected ) die sich in diesem
     * Square befinden.
     *
     *      for( ListItem item = _getFirstNodeItem();
     *                    item != null;
     *                    item = _getNextNodeItem( item ) )
     *      {
     *          Node node = ( Node )item.value();
     *          ...
     *      }
     */

    //============================================================
    private ListItem _getFirstNodeItem()
    //============================================================
    {
        if(    _nodes_single != null
            && _nodes_single.length() > 0
            )
            return _nodes_single.first();

        if( _nodes_connected != null )
            return _nodes_connected.first();

        return null;
    } // _getFirstNodeItem


    /**
     * Hilfsmethode zum aufeinanderfolgenden Durchlaufen der
     * Knotenlisten _nodes_single und _nodes_connected.
     *
     * Das folgende Programmstueck besucht alle Knoten ( erst Liste
     * _nodes_single und dann _nodes_connected ) die sich in diesem
     * Square befinden.
     *
     *      for( ListItem item = _getFirstNodeItem();
     *                    item != null;
     *                    item = _getNextNodeItem( item ) )
     *      {
     *          Node node = ( Node )item.value();
     *          ...
     *      }
     */

    //============================================================
    private ListItem _getNextNodeItem( ListItem item )
    //============================================================
    {
        ListItem item_next = item.next();

        if( item_next != null )
            return item_next;


        // item_next == null

        if(    _nodes_single != null
            && _nodes_connected != null
            && _nodes_single.last() == item
            )
            return _nodes_connected.first();

        return null;
    } // _getNextNodeItem


} // QuadTreeSquare


