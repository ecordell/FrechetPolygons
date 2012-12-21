package anja.obstaclescene;


/**
 * Ein Objekt der Klasse QueryArea repraesentiert eine Flaeche, die
 * im QuadTree fuer diverse Anfragen benutzt wird.
 * ( @see QuadTree.getNodes(..), QuadTree.getSegments(..) )
 *
 *
 * @version 0.1 20.04.03
 * @author      Ulrich Handel
 */

//****************************************************************
abstract public class QueryArea
//****************************************************************
{
    //************************************************************
    // protected constants
    //************************************************************


    protected static final short W_E_MASK = QuadTreeSquare.W_E_MASK;
    protected static final short S_N_MASK = QuadTreeSquare.S_N_MASK;

    protected static final short WEST  = QuadTreeSquare.WEST;
    protected static final short EAST  = QuadTreeSquare.EAST;
    protected static final short SOUTH = QuadTreeSquare.SOUTH;
    protected static final short NORTH = QuadTreeSquare.NORTH;

    protected static final short NORTH_EAST = NORTH | EAST;
    protected static final short NORTH_WEST = NORTH | WEST;
    protected static final short SOUTH_EAST = SOUTH | EAST;
    protected static final short SOUTH_WEST = SOUTH | WEST;



    //************************************************************
    // abstract public methods
    //************************************************************


    /**
     * return  true <==> Das Rechteck (<xmin>, <ymin>, <xmax>, <ymax>)
     *                   liegt vollstaendig auf der durch dieses
     *                   QueryArea-Objekt repraesentierten Flaeche
     */

    //============================================================
    abstract public boolean enclosesRectangle( int xmin,
                                               int ymin,
                                               int xmax,
                                               int ymax );
    //============================================================


    /**
     * return  true <==> Der Punkt (<x>, <y>) liegt auf der durch
     *                   dieses QueryArea-Objekt repraesentierten
     *                   Flaeche
     */

    //============================================================
    abstract public boolean intersectsPoint( int x, int y );
    //============================================================


    /**
     */

    //============================================================
    abstract public boolean intersectsSegment( int x1,
                                               int y1,
                                               int x2,
                                               int y2 );
    //============================================================


    /**
     */

    //============================================================
    abstract public int getQuadranten( int     x_east,
                                       int     y_north,
                                       short[] quadranten );
    //============================================================


    /**
     */

    //============================================================
    public boolean intersects( Node node )
    //============================================================
    {
        return intersectsPoint( node.getX(), node.getY() );
    } // intersects


    /**
     */

    //============================================================
    public boolean intersects( Segment segment )
    //============================================================
    {
        Node node1 = segment.getNode1();
        Node node2 = segment.getNode2();

        return intersectsSegment( node1.getX(),
                                  node1.getY(),
                                  node2.getX(),
                                  node2.getY() );
    } // intersects

} // QueryArea
