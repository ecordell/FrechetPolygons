package anja.swinggui.polygon;

/*
import java_ersatz.java2d.Transform;
import java_ersatz.java2d.AffineTransform;
import java_ersatz.java2d.Graphics2D;
import java_ersatz.java2d.Rectangle2D;
import java_ersatz.java2d.BasicStroke; */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;



import java.util.Vector;
import java.io.*;

import anja.swinggui.*;

import anja.geom.Polygon2Scene;
import anja.geom.Polygon2;
import anja.geom.Point2;
import anja.geom.Segment2;
import anja.geom.Rectangle2;
import anja.geom.Intersection;

import anja.util.GraphicsContext;
import anja.util.ListItem;
import anja.util.SimpleList;


/**
 * Polygon2SceneWorker ist von WorldCoorScene abgeleitet und ist die
 * Basisklasse fuer Klassen, die ein Objekt der Klasse
 * anja.geom.Polygon2Scene in einem DisplayPanel darstellen
 *
 * @version 0.7 08.02.05
 * @author Ulrich Handel, Sascha Ternes
 */

//****************************************************************
public class Polygon2SceneWorker extends
                                 WorldCoorScene
//****************************************************************
{
    //************************************************************
    // public constants
    //************************************************************

    // Konstanten zur Auswahl von speziellen Polygonen
    public final static int ALL_POLYGONS     = 1;
    public final static int BOUNDING_POLYGON = 2;
    public final static int POINT_POLYGONS   = 3;
    public final static int OPEN_POLYGONS    = 4;


    //************************************************************
    // protected constants
    //************************************************************


    protected final static int FORBIDDEN_SPACE = 0;
    protected final static int FREE_SPACE      = 1;
    protected final static int VERTEX          = 2;
    protected final static int EDGE            = 3;
    protected final static int POLYGON         = 4;


    //************************************************************
    // private constants
    //************************************************************


    private final static int _SEGMENT_SELECT_DISTANCE = 5;
    private final static int _POINT_SELECT_DISTANCE = 6;




    //************************************************************
    // protected variables
    //************************************************************

    protected Color color_background = Color.white;
    protected Color color_BoundingPolygon = Color.gray.brighter();
    protected Color color_Points = Color.gray;
    protected Color color_Edges = Color.black;
    protected Color color_Polygon = Color.lightGray;

    protected boolean show_coors = false;
    protected boolean show_active_coors = false;
    
    protected boolean _check_validity = true; // definiert Boolean um Polygonszene beim 
                                              // Einfügen eines Punktes auf validity zu checken,
                                              // oder nicht


    //************************************************************
    // private variables
    //************************************************************


    private Polygon2Scene _poly_scene = new Polygon2Scene();

    private boolean _show_scene = true;
    private boolean _show_edges = true;
    private boolean _show_vertices = true;

    private int _vertex_size = 4;
    private int _end_vertex_size = 4;
    
    private Polygon2 _foreground_poly; // Dieses Polygon wird beim
                          // Zeichnen als letztes gezeichnet, so dass
                          // es ueber allen anderen liegt
                          // ( wird fuer Editieroperationen gebraucht,
                          //   bei denen zwischenzeitlich Ueberlappungen
                          //   moeglich sind )


    private Polygon2 _sel_polygon;
    private Point2   _sel_vertex;
    private Segment2 _sel_edge;


    //************************************************************
    // constructors
    //************************************************************




    //************************************************************
    // public methods
    //************************************************************


    /**
     * Ermitteln der gesetzten Polygonszene
     */

    //============================================================
    public Polygon2Scene getPolygon2Scene()
    //============================================================
    {
        return _poly_scene;
    } // getPolygon2Scene


    /**
     * Setzen einer neuen Polygonszene
     */

    //============================================================
    public void setPolygon2Scene( Polygon2Scene scene )
    //============================================================
    {
        setPolygon2Scene( scene, true );
    } // setPolygon2Scene


    /**
     * Setzen einer neuen Polygonszene
     */

    //============================================================
    public void setPolygon2Scene( Polygon2Scene scene,
                                  boolean       refresh)
    //============================================================
    {
        if( scene != _poly_scene )
        {
            _poly_scene = scene;

            if( refresh )
                refresh();
        } // if
    } // setPolygon2Scene


    /**
     * Setzen der Sichtbarkeit der Polygonszene
     */

    //============================================================
    public void setShowScene( boolean show )
    //============================================================
    {
        setShowScene( show, true );
    } // setShowScene


    /**
     * Setzen der Sichtbarkeit der Polygonszene
     */

    //============================================================
    public void setShowScene( boolean show, boolean refresh )
    //============================================================
    {
        if( show != _show_scene )
        {
            _show_scene = show;
            if( refresh ) refresh();
        } // if
    } // setShowScene


    /**
     * Ermitteln der Sichtbarkeit der Polygonszene
     */

    //============================================================
    public boolean isSetShowScene()
    //============================================================
    {
        return _show_scene;
    } // isSetShowScene


    /**
     * Setzen der Hervorhebung der Polygonkanten
     */

    //============================================================
    public void setShowEdges( boolean show )
    //============================================================
    {
        setShowEdges( show, true );
    } // setShowEdges


    /**
     * Setzen der Hervorhebung der Polygonkanten
     */

    //============================================================
    public void setShowEdges( boolean show, boolean refresh )
    //============================================================
    {
        if( show != _show_edges )
        {
            _show_edges = show;
            if( refresh ) refresh();
        } // if
    } // setShowEdges


    /**
     * Ermitteln der Sichtbarkeit der Polygonkanten
     */

    //============================================================
    public boolean isSetShowEdges()
    //============================================================
    {
        return _show_edges;
    } // isSetShowEdges


    /**
     * Setzen der Hervorhebung der Polygon-Vertices
     */

    //============================================================
    public void setShowVertices( boolean show )
    //============================================================
    {
        setShowVertices( show, true );
    } // setShowVertices


    /**
     * Setzen der Hervorhebung der Polygon-Vertices
     */

    //============================================================
    public void setShowVertices( boolean show, boolean refresh )
    //============================================================
    {
        if( show != _show_vertices )
        {
            _show_vertices = show;
            if( refresh ) refresh();
        } // if
    } // setShowEdges


    /**
     * Ermitteln der Sichtbarkeit der Vertices
     */

    //============================================================
    public boolean isSetShowVertices()
    //============================================================
    {
        return _show_vertices;
    } // isSetShowVertices




    /**
      * Setzen der aktuellen Hintergrundfarbe
      */
    public void setBackgroundColor( Color c ) {
      color_background = c;
      refresh();
    }



    /**
      * Ermitteln der aktuellen Hintergrundfarbe
      */
    public Color getBackgroundColor() {
      return color_background;
    }



    /**
      * Setzen der aktuellen Farbe fuer das Bounding-Polygon
      */
    public void setBoundingPolygonColor( Color c ) {
      color_BoundingPolygon = c;
      refresh();
    }



    /**
      * Ermitteln der aktuellen Farbe fuer das Bounding-Polygon
      */
    public Color getBoundingPolygonColor() {
      return color_BoundingPolygon;
    }



    /**
      * Setzen der aktuellen Farbe fuer die Punkte der Polygone
      */
    public void setPointColor( Color c ) {
      color_Points = c;
      refresh();
    }



    /**
      * Ermitteln der aktuellen Farbe fuer die Punkte der Polygone
      */
    public Color getPointColor() {
      return color_Points;
    }



    /**
      * Setzen der aktuellen Farbe fuer die Kanten der Polygone
      */
    public void setEdgeColor( Color c ) {
      color_Edges = c;
      refresh();
    }



    /**
      * Ermitteln der aktuellen Farbe fuer die Kanten der Polygone
      */
    public Color getEdgeColor() {
      return color_Edges;
    }



    /**
      * Setzen der aktuellen Farbe fuer die Polygone
      */
    public void setPolygonColor( Color c ) {
      color_Polygon = c;
      refresh();
    }



    /**
      * Ermitteln der aktuellen Farbe fuer die Polygone
      */
    public Color getPolygonColor() {
      return color_Polygon;
    }



    /**
     * Setzen der Groesse der Polygon-Vertices ( Durchmesser in Pixeln )
     */

    //============================================================
    public void setVertexSize( int size, boolean refresh )
    //============================================================
    {
        if( size != _vertex_size )
        {
            _vertex_size = size;
            if( refresh ) refresh();
        } // if
    } // setVertexSize



    /**
     * Setzen der Groesse der End-Vertices bei offenen Polygonen
     * ( Durchmesser in Pixeln )
     */

    //============================================================
    public void setEndVertexSize( int size, boolean refresh )
    //============================================================
    {
        if( size != _end_vertex_size )
        {
            _end_vertex_size = size;
            if( refresh ) refresh();
        } // if
    } // setEndVertexSize






    /**
     * Loeschen aller Polygone der angegebenen Art
     */

    //============================================================
    public void erasePolygons( int mode )
    //============================================================
    {
        erasePolygons( mode, true );
    } // erasePolygons


    /**
     * Loeschen aller Polygone der angegebenen Art
     */

    //============================================================
    public void erasePolygons( int mode, boolean refresh )
    //============================================================
    {
        Polygon2 bounding_poly = _poly_scene.getBoundingPolygon();

        switch( mode )
        {
          case ALL_POLYGONS:
            Polygon2Scene scene = new Polygon2Scene();
            setPolygon2Scene( scene, false );
            break;

          case BOUNDING_POLYGON:
            if( bounding_poly != null )
                removePolygon( bounding_poly, false );
            break;

          case POINT_POLYGONS:
          case OPEN_POLYGONS:
            Polygon2[] polygons = _poly_scene.getInteriorPolygons();
            for( int i = 0; i < polygons.length; i++ )
            {
                Polygon2 poly = polygons[ i ];

                if(   ( mode == POINT_POLYGONS && poly.length() == 1 )
                   || (   mode == OPEN_POLYGONS
                       && poly.isOpen()
                       && poly.length() > 1 ) )

                {
                    removePolygon( poly, false );
                } // if
            } // for
            break;

          default:
            refresh = false;
            break;
        } // switch

        if( refresh )
            refresh();
    } // erasePolygons








    //============================================================
    public void save( File file )
    //============================================================
    {
        try
        {
            FileOutputStream fos = new FileOutputStream ( file );
            DataOutputStream dos = new DataOutputStream ( fos );

            _poly_scene.save( dos );

            dos.flush ();
            dos.close ();
        }
        catch ( IOException ex )
        {
            System.out.println( ex );
        }
    }

    /**
     * Loads a file, only works on local files.
     * @param file File to load
     */
    //============================================================
    public void load( File file )
    //============================================================
    {
        try
        {
            FileInputStream fis = new FileInputStream ( file );
            DataInputStream dis = new DataInputStream ( fis );

            Polygon2Scene scene = new Polygon2Scene( dis );
            setPolygon2Scene( scene, true );
        }
        catch ( IOException ex )
        {
            System.out.println( ex );
        }
    }
   
    /**
     * Loads a file using getResourceAsStream, should work loading from remote jar files.
     * @param file Filename of the file to load.
     */
    public void load(String file)
    {
        InputStream is = this.getClass().getResourceAsStream(file);
        DataInputStream di = new DataInputStream(is);
        Polygon2Scene scene = new Polygon2Scene(di);
        setPolygon2Scene( scene, true );
    }



    //************************************************************
    // protected methods
    //************************************************************



    /**
     * Einfuegen des Polygons poly in die Polygonszene.
     *
     * Das Polygon wird nur eingefuegt, wenn es mindestens einen
     * Eckpunkt hat.
     */

    //============================================================
    protected boolean addPolygon( boolean  bounding,
                                  Polygon2 poly,
                                  boolean  test_validity,
                                  boolean  redraw )
    //============================================================
    {
        if( bounding )
            return setBoundingPolygon( poly, test_validity, redraw );
        else
            return addInteriorPolygon( poly, test_validity, redraw );
    } // addPolygon



    /**
     * Einfuegen des Interior-Polygons poly in die Polygonszene.
     *
     * Das Polygon wird nur eingefuegt, wenn es mindestens einen
     * Eckpunkt hat.
     */

    //============================================================
    protected boolean addInteriorPolygon( Polygon2 poly,
                                          boolean  test_validity,
                                          boolean  redraw )
    //============================================================
    {
        if( poly.length() < 1 )
            return false;

        if( test_validity && ! _poly_scene.polygonIsValid( poly ) )
            return false;


        _poly_scene.add( poly );

        if( redraw )
            refresh();

        return true;
    } // addInteriorPolygon



    /**
     * Erzeugen eines neuen Interior-Polygons mit dem gegebenen
     * Punkt als ersten Polygonpunkt.
     *
     * @return Das neue Polygon oder null, falls das erzeugen nicht
     *         moeglich ist.
     */

    //============================================================
    protected Polygon2 createNewInteriorPolygon( Point2 point )
    //============================================================
    {
        Polygon2 poly = new Polygon2();
        poly.setOpen();
        poly.addPoint( point );
        if( addInteriorPolygon( poly, _check_validity, true ) )
            return poly;
        else
            return null;
    } // createNewInteriorPolygon



    /**
     * Anhaengen des offenen Polygons poly2 an das offene Polygon poly1.
     *
     * @return false, falls das Anhaengen nicht moeglich war.
     */

    //============================================================
    protected boolean concatOpenPolygons( Polygon2 poly1,
                                          Polygon2 poly2 )
    //============================================================
    {
        if( poly1.isClosed() || poly2.isClosed() )
            return false;

        Segment2 seg = new Segment2( poly1.lastPoint(),
                                     poly2.firstPoint() );

        if( ! _poly_scene.segmentIsValid( seg )  )
            return false;

        removePolygon( poly1, false );
        removePolygon( poly2, false );

        poly1.appendCopy( poly2 );

        addInteriorPolygon( poly1, false, false );

        refresh();
        return true;
    } // concatOpenPolygons



    /**
     * Schliessen des offenen Interior-Polygons poly.
     *
     * Falls das Schliessen des Polygons als Interior-Polygon nicht
     * moeglich ist und die Szene noch kein Bounding-Polygon hat,
     * wird versucht das Polygon zum Bounding Polygon zu machen
     *
     * @return false, falls das Schliessen nicht moeglich ist.
     */

    //============================================================
    protected boolean closeOpenPolygon( Polygon2 poly )
    //============================================================
    {
        if( poly.isClosed() || poly.length() < 3 )
            return false;

        Segment2 seg = new Segment2( poly.lastPoint(),
                                     poly.firstPoint() );

        if( ! _poly_scene.segmentIsValid( seg ) )
            return false;

        removePolygon( poly, false );

        poly.setClosed();

        if( _poly_scene.closingIsValid( poly ) )
        {
            addInteriorPolygon( poly, false, false );
            refresh();
            return true;
        } // if


        if(    _poly_scene.getBoundingPolygon() == null
            && _poly_scene.boundingIsValid( poly ) )
        {
            setBoundingPolygon( poly, false, false );
            refresh();
            return true;
        } // if

        poly.setOpen();
        addInteriorPolygon( poly, false, false );

        return false;
    } // closeOpenPolygon



    /**
     * Erweitern des offenen Polygons poly um den Punkt point.
     *
     * @return false, falls das Erweitern nicht moeglich war.
     */

    //============================================================
    protected boolean extendOpenPolygon( Polygon2 poly, Point2 point )
    //============================================================
    {
        if( poly.isClosed() )
            return false;

        Segment2 seg = new Segment2( poly.lastPoint(), point );

        if( ! _poly_scene.segmentIsValid( seg ) )
            return false;

        removePolygon( poly, false );

        poly.addPoint( point );

        addInteriorPolygon( poly, false, false );

        Segment2[] segs = new Segment2[ 1 ];
        segs[ 0 ] = seg;
        Rectangle2D rect = Segment2.getBoundingRect( segs );

        refresh( rect.getX(), rect.getY(), 
        		 rect.getWidth(), rect.getHeight() );
        return true;
    } // extendOpenPolygon

    /**
     * Erweitern des offenen Polygons poly um den Punkt point.
     * Boolean check wird dazu genutzt um Validity-Check auszuschalten
     * @return false, falls das Erweitern nicht moeglich war.
     */  
    
//  ============================================================
    protected boolean extendOpenPolygon( Polygon2 poly, Point2 point, boolean check )
    //============================================================
    {
        if( poly.isClosed() )
            return false;

        Segment2 seg = new Segment2( poly.lastPoint(), point );

        if (check){
            if( ! _poly_scene.segmentIsValid( seg ) )
                return false; 
        }

        removePolygon( poly, false );

        poly.addPoint( point );

        addInteriorPolygon( poly, false, false );

        Segment2[] segs = new Segment2[ 1 ];
        segs[ 0 ] = seg;
        Rectangle2D rect = Segment2.getBoundingRect( segs );

        refresh( rect.getX(), rect.getY(), 
                rect.getWidth(), rect.getHeight() );
        return true;



    } // extendOpenPolygon


    /**
     * Einfuegen eines neuen Eckpunktes in das Polygon poly
     */

    //============================================================
    protected Point2 insertPoint( Polygon2 poly,
                                  Segment2 segment,
                                  Point2   point )
    //============================================================
    {
        point = segment.plumb( point );

        if( point == null )
            return null;

        SimpleList pts = poly.points();

        boolean is_bounding = removePolygon( poly, false );

        ListItem item = pts.find( segment.target() );
        pts.insert( item, point );

        addPolygon( is_bounding, poly, false, false );

        refresh();
        return point;
    } // insertPoint




    /**
     * Ermitteln des die Szene umschliessenden Rechtecks im
     * Weltkoordinaten-System
     */

    //============================================================
    protected Rectangle2 getBoundingRectWorld()
    //============================================================
    {
        return _poly_scene.getBoundingBox();
    } // getBoundingRectWorld



    /**
     * Zeichnen der Polygonszene
     */

    //============================================================
    protected void paint( Graphics2D g2d, Graphics g )
    //============================================================
    {
    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	
        paintBackground( g2d, g );
        paintPolygons( g2d, g );
    } // paint




    /**
     * Zeichnen des Hintergrundes
     */

    //============================================================
    protected void paintBackground( Graphics2D g2d, Graphics g )
    //============================================================
    {
        // Clipping-Rechteck in Bildkoordinaten ermitteln
        Rectangle clip = g.getClipBounds();


        // Hintergrund fuellen

        Polygon2 bounding_poly = _poly_scene.getBoundingPolygon();

        if( bounding_poly != null && _show_scene)
            g.setColor( color_BoundingPolygon );
        else
            g.setColor( color_background );

        g.fillRect( clip.x, clip.y, clip.width, clip.height );


        if( bounding_poly != null && _show_scene )
        {
            // Flaeche des Bounding-Polygon fuellen

            GraphicsContext gc = new GraphicsContext();
            gc.setFillColor( color_background );
            gc.setFillStyle( 1 );
            bounding_poly.draw( g2d, gc );
        } // if
    } // paintBackground



    /**
     * Zeichnen der Polygone
     */

    //============================================================
    protected void paintPolygons( Graphics2D g2d, Graphics g )
    //============================================================
    {
        if( _show_scene == false )
            return;

        GraphicsContext gc_points = new GraphicsContext();
        gc_points.setLineWidth( _vertex_size );
        gc_points.setForegroundColor( color_Points );
        gc_points.setEndCap( BasicStroke.CAP_SQUARE );

        GraphicsContext gc_endpoints = new GraphicsContext();
        gc_endpoints.setLineWidth( _end_vertex_size );
        gc_endpoints.setForegroundColor( color_Points );
        gc_endpoints.setEndCap( BasicStroke.CAP_ROUND );

        GraphicsContext gc_polygons = new GraphicsContext();

        gc_polygons.setForegroundColor( _show_edges ?
                                        color_Edges :
                                        color_Polygon );

        gc_polygons.setFillColor( color_Polygon );
        gc_polygons.setFillStyle( 1 );


        Polygon2 bounding_poly = _poly_scene.getBoundingPolygon();
        Polygon2[] polygons = _poly_scene.getInteriorPolygons();

        boolean found = false; // Fuer das Zeichnen des _foreground_poly
        for( int i = -1; i <= polygons.length; i++ )
        {
            Polygon2 poly;

            if( i == -1 )
            {
                poly = bounding_poly;
                if( poly == null )
                    continue;

                GraphicsContext gc = new GraphicsContext();
                gc.setForegroundColor( _show_edges ?
                                       color_Edges :
                                       color_BoundingPolygon );
                poly.draw( g2d, gc );
            }
            else
            {
                if( i == polygons.length )
                {
                    if( ! found )
                        break;
                    poly = _foreground_poly;
                }
                else
                {
                    poly = polygons[ i ];
                    if( poly == _foreground_poly )
                    {
                        found = true;
                        continue;
                    } // if
                } // else

                if( poly.length() > 1 )
                    poly.draw( g2d, gc_polygons );
                else
                    poly.firstPoint().draw( g2d, gc_endpoints );
            } // else



            if( ! ( show_coors || _show_vertices ) )
                continue;

            SimpleList points = poly.points();

            for( ListItem item = points.first();
                 item != null;
                 item = points.next( item ) )
            {
                Point2 point = ( Point2 )points.value( item );

                if ( show_coors ) {
                   ListItem last = points.prev( item );
                   ListItem next = points.next( item );
                   Point2 lp = null;
                   if ( last != null ) lp = (Point2) last.value();
                   else lp = (Point2) points.lastValue();
                   Point2 np = null;
                   if ( next != null ) np = (Point2) next.value();
                   else np = (Point2) points.firstValue();
                   paintCoors( g2d, point, lp, np );
                } // if

                if ( _show_vertices )
                   if(    poly.isOpen()
                       && (    point == poly.lastPoint()
                            || point == poly.firstPoint() ) )
                      point.draw( g2d, gc_endpoints );
                   else
                      point.draw( g2d, gc_points );

            } // for


        } // for
    } // paintPolygons


   /**
   * Zeichnet die Koordinaten des spezifizierten Punkts.
   *
   * @param point der Punkt, dessen Koordinaten gezeichnet werden sollen
   * @param last der Vorgaengerpunkt
   * @param next der Nachfolgerpunkt
   */
   protected void paintCoors(
      Graphics2D g2d,
      Point2 point,
      Point2 last,
      Point2 next
   ) {

      String coors = point.x + "/" + point.y;
      g2d.transform( AffineTransform.getScaleInstance( 1.0, -1.0 ) );
      g2d.setPaint( Color.BLACK );
      g2d.drawString( coors, point.x, - point.y + 16 );
      g2d.transform( AffineTransform.getScaleInstance( 1.0, -1.0 ) );

   } // paintCoors


    /**
     * Setzt das Polygon, dass im Vordergrund liegt, also als letztes
     * zu zeichnen ist.
     */

    //============================================================
    protected void setForegroundPoly( Polygon2 poly )
    //============================================================
    {
        _foreground_poly = poly;
    } // setForegroundPoly



    /**
     * Szene neu zeichnen und _action_listener informieren.
     */

    //============================================================
    protected void refresh()
    //============================================================
    {
        updateDisplayPanel();

        fireActionEvent();
    } // refresh


    /**
     * Szene neu zeichnen und _action_listener informieren.
     */

    //============================================================
    protected void refresh( double x,
                            double y,
                            double width,
                            double height )
    //============================================================
    {
        updateDisplayPanel( x, y, width, height, 10 );

        fireActionEvent();
    } // refresh



    /**
     * Entfernen des Eckpunktes point aus dem Polygon poly
     *
     * Falls das Polygon nach Entfernen des Punktes weniger als
     * 3 Eckpunkte hat, wird es zu einem offenen Polygon.
     * Falls es keinen Eckpunkt mehr hat, wird es aus der Polygonszene
     * entfernt.
     */

    //============================================================
    protected boolean removePoint( Polygon2 poly, Point2 point )
    //============================================================
    {
        if(    poly.length() < 4
            && _poly_scene.getBoundingPolygon() == poly )
            return false; // Im Bounding-Polygon mit weniger als
                          // 4 Punkten darf grundsaetzlich kein
                          // Punkt heloescht werden


        // Original-Polygon erst mal aus der Szene entfernen
        boolean is_bounding = removePolygon( poly, false );

        // Die beiden Nachbarpunkte des zu loeschenden Punktes ermitteln
        Segment2[] segments = poly.edges();
        Point2 pt1 = null;
        Point2 pt2 = null;

        for( int i = 0; i < segments.length; i++ )
        {
            Segment2 seg = segments[ i ];

            if( point == seg.source() )
                pt1 = seg.target();

            if( point == seg.target() )
                pt2 = seg.source();
        } // for


        if(    ( pt1 == null || pt2 == null )
            || ( poly.isClosed() && poly.length() == 3 ) )
        {
            // Fall 1:
            // Der zu loeschende Punkt hat hoechstens einen Nachbar
            // und kann ohne weiteres geloescht werden, da es sich
            // um einen Endpunkt eines offenen Interior-Polygons
            // handeln muss.

            // Fall 2:
            // Das geschlossene Polygon hat 3 Eckpunkte, und der Punkt
            // kann ohne weiteres geloescht werden, da ein einzelnes
            // Segment uebrig bleibt

            // Das verbleibende Polygon ist auf jeden Fall offen
            poly.setOpen();

            poly.points().remove( point );

            if( ! poly.empty() )
                addInteriorPolygon( poly, false, false );

            refresh();
            return true;
        } // if


        // Neues Polygon erzeugen
        Polygon2 new_poly = new Polygon2();
        if( poly.isOpen() )
            new_poly.setOpen();
        new_poly.appendCopy( poly );
        
        // Polygontyp mit kopieren
        
        new_poly.setUserflag(poly.getUserflag());
        
        // Punkt entfernen und neues Polygon der Szene zufuegen
        new_poly.points().remove( point );
        addPolygon( is_bounding, new_poly, false, false );


        // Testen, ob das sich neu ergebene Segment zwischen den
        // Nachbarpunkten des geloeschten Punktes gueltig ist
        Segment2 seg = new Segment2( pt1, pt2 );
        
        // Falls die Szene auf Vailiditaet untersucht werden soll
        if (_check_validity) {
        	if( ! _poly_scene.segmentIsValid( seg ) )
        	{
        		// Ungueltig, also Original wieder herstellen
        		removePolygon( new_poly, false );
        		addPolygon( is_bounding, poly, false, false );
        		return false;
        	} // if


        	if( new_poly.isClosed() )
        	{
        		if(   (   is_bounding && ! _poly_scene.boundingIsValid( new_poly ) )
        				|| ( ! is_bounding && ! _poly_scene.closingIsValid( new_poly ) ) )
        		{
        			// Ungueltig, also Original wieder herstellen
        			removePolygon( new_poly, false );
        			addPolygon( is_bounding, poly, false, false );
        			return false;
        		} // if

        	} // if
        }  //if
        refresh();
        return true;
    } // removePoint



    /**
     * Entfernen des Polygons poly aus der Polygonszene.
     */

    //============================================================
    protected boolean removePolygon( Polygon2 poly, boolean redraw )
    //============================================================
    {
        boolean is_bounding =
            ( poly == _poly_scene.getBoundingPolygon() );

        if( is_bounding )
        {
            _poly_scene.setBoundingPolygon( null );
        }
        else
        {
            _poly_scene.remove( poly );
        } // else

        if( redraw )
            refresh();

        return is_bounding;
    } // removePolygon



    /**
     * Entfernen der Kanten des Polygons poly, die als source oder
     * target den Punkt vertex haben.
     *
     * Es werden also hoechstens 2 Kanten aus dem Polygon entfernt.
     */

    //============================================================
    protected void removeEdges( Polygon2 poly, Point2 vertex )
    //============================================================
    {
        Segment2[] edges = poly.edges();
        for( int i = 0; i < edges.length; i++ )
        {
            Segment2 edge = edges[ i ];
            if( edge.source() == vertex )
            {
                removeSegment( poly, edge );
                break;
            } // if
        } // for

        for( int i = 0; i < edges.length; i++ )
        {
            Segment2 edge = edges[ i ];
            if( edge.target() == vertex )
            {
                removeSegment( _poly_scene.getPolygonWithVertex( vertex ),
                               edge );
                break;
            } // if
        } // for
    } // removeEdges



    /**
     * Entfernen des Segments seg aus dem Polygon poly
     *
     * Falls das Polygon offen ist, wird es durch das Entfernen des
     * Segments in 2 offene Polygone zerlegt.
     * Ist das Polygon geschlossen, wird es zu einem offenen Polygon
     */

    //============================================================
    protected void removeSegment( Polygon2 poly, Segment2 seg )
    //============================================================
    {
        removePolygon( poly,
                       false  // Szene nicht neu zeichnen
                     );

        SimpleList pts = poly.points();

        if( poly.isOpen() )
        {
            // Das offene Polygon wird durch Entfernen des
            // Segments in zwei offene Polygone zerlegt.

            Polygon2 poly1 = new Polygon2();
            poly1.setOpen();
            Polygon2 poly2 = new Polygon2();
            poly2.setOpen();

            Polygon2 p = poly1;
            for( ListItem item = pts.first();
                 item != null;
                 item = pts.next( item ) )
            {
                Point2 pt = ( Point2 )pts.value( item );
                if( pt == seg.target() )
                    p = poly2;

                p.addPoint( pt );
            } // for

            if( poly1.length() > 1 )
                addInteriorPolygon( poly1, false, false );

            if( poly2.length() > 1 )
                addInteriorPolygon( poly2, false, false );
        }
        else
        {
            // Das geschlossene Polygon wird durch Entfernen des
            // Segments zu einem offenen Polygon.

            pts.cycle( pts.find( seg.target() ) );
            poly.setOpen();

            addInteriorPolygon( poly, false, false );
        } // else

        refresh();
    } // removeSegment



    /**
     * Einfuegen des Polygons poly in die Polygonszene als
     * Bounding-Polygon.
     */

    //============================================================
    protected boolean setBoundingPolygon( Polygon2 poly,
                                          boolean  test_validity,
                                          boolean  redraw )
    //============================================================
    {
        if(   poly.length() < 3
           || poly.isOpen()
           || _poly_scene.getBoundingPolygon() != null )
            return false;

        _poly_scene.setBoundingPolygon( poly );


        if( test_validity && ! _poly_scene.polygonIsValid( poly ) )
        {
            _poly_scene.setBoundingPolygon( null );
            return false;
        } // if

        if( redraw )
            refresh();

        return true;
    } // setBoundingPolygon




    //************************************************************
    // Methoden zum Suchen von Eckpunkten, Kanten und Polygonen
    //************************************************************



    /** 
     * Ermitteln der durch den gegebenen Weltkoordinaten-Punkt
     * selektierten Objekte ( Polygon, Edge, Vertex ).
     *
     * Die gefundenen Objekte koennen nach Aufruf der Methode
     * mit
     *      getSelectedPolygon(),
     *      getSelectedEdge(),
     * und  getSelectedVertex()
     * abgefragt werden.
     *
     * @return VERTEX, EDGE, POLYGON, FREE_SPACE oder FORBIDDEN_SPACE
     */

    //============================================================
    protected int getSelectedObjects( Point2 point )
    //============================================================
    {
        _sel_polygon = null;
        _sel_vertex  = null;
        _sel_edge    = null;

        if( _poly_scene.isPointInsidePolygon( point, _foreground_poly ) )
        {
            // Das Polygon _foreground_poly ist also ein geschlossenes
            // Polygon der Szene auf dessen Flaeche der Punkt point
            // liegt

            _sel_polygon = _foreground_poly;

            double point_distance =
                transformScreenToWorld( _POINT_SELECT_DISTANCE );

            _sel_vertex = _poly_scene.getNearestVertex( _foreground_poly,
                                                        point,
                                                        point_distance );
            if( _sel_vertex != null )
                return VERTEX; // Durch point wird also der Eckpunkt
                               // _sel_vertex des Polygons
                               // _foreground_poly ausgewaehlt.

            double segment_distance =
                transformScreenToWorld( _SEGMENT_SELECT_DISTANCE );

            _sel_edge = _poly_scene.getNearestEdge( _foreground_poly,
                                                    point,
                                                    segment_distance );

            if( _sel_edge != null )
                return EDGE; // Durch point wird also die Kante
                             // _sel_edge des Polygons _foreground_poly
                             // ausgewaehlt.

            return POLYGON; // Durch point wird das Polygon
                            // _foreground_poly ausgewaehlt
        } // if


        // Ermitteln des durch point ausgewaehlten Polygon-Eckpunktes

        _sel_vertex = getSelectedVertex( point );

        if( _sel_vertex != null )
            return VERTEX;


        // Ermitteln der durch point ausgewaehlten Polygon-Kante

        _sel_edge = getSelectedEdge( point );

        if( _sel_edge != null )
            return EDGE;

        
        // Ermitteln des durch point ausgewaehlten Polygons

        _sel_polygon = _poly_scene.getPolygonWithPointInside( point );

        if( _sel_polygon == _poly_scene.getBoundingPolygon() )
            return FREE_SPACE;

        if( _sel_polygon != null )
            return POLYGON;

        return FORBIDDEN_SPACE;

    } // getSelectedObjects



    /**
     */

    //============================================================
    protected Polygon2 getSelectedPolygon()
    //============================================================
    {
        return _sel_polygon;
    } // getSelectedPolygon



    /**
     */

    //============================================================
    protected Point2 getSelectedVertex()
    //============================================================
    {
        return _sel_vertex;
    } // getSelectedVertex



    /**
     */

    //============================================================
    protected Segment2 getSelectedEdge()
    //============================================================
    {
        return _sel_edge;
    } // getSelectedEdge



    /**
     * Ermitteln des Eckpunktes der Szene der sich innerhalb des
     * durch _POINT_SELECT_DISTANCE gegebenen Abstandes am naechsten
     * am Punkt point befindet.
     *
     * Falls fuer einen Eckpunkt vertex gilt  vertex == point, so wird
     * dieser bei der Suche ausgeschlossen.
     *
     * Das Polygon, zu dem der gefundene Eckpunkt gehoert, kann
     * unmittelbar nach Aufruf dieser Methode durch getSelectedPolygon()
     * abgefragt werden.
     *
     * @return Der gefundene Eckpunkt oder null, falls kein Eckpunkt
     *         gefunden wurde
     */

    //============================================================
    protected Point2 getSelectedVertex( Point2 point )
    //============================================================
    {
        double dist = transformScreenToWorld( _POINT_SELECT_DISTANCE );

        Point2 vertex = _poly_scene.getNearestVertex( point, dist );

        if( vertex != null )
            _sel_polygon = _poly_scene.getPolygonWithVertex( vertex );
        else
            _sel_polygon = null;

        return vertex;
    } // getSelectedVertex



    /**
     * Ermitteln der Kante der Szene, die sich innerhalb des durch
     * _SEGMENT_SELECT_DISTANCE gegebenen Abstandes am naechsten
     * am Punkt point befindet.
     *
     * Das Polygon, zu dem die gefundene Kante gehoert, kann
     * unmittelbar nach Aufruf dieser Methode durch getSelectedPolygon()
     * abgefragt werden.
     *
     * @return Die gefundene Kante oder null
     */

    //============================================================
    protected Segment2 getSelectedEdge( Point2 point )
    //============================================================
    {
        double dist = transformScreenToWorld( _SEGMENT_SELECT_DISTANCE );

        Segment2 edge = _poly_scene.getNearestEdge( point, dist );

        if( edge != null )
            _sel_polygon = _poly_scene.getPolygonWithEdge( edge );
        else
            _sel_polygon = null;

        return edge;
    } // getSelectedEdge

  
	/**
	 * Sets _check_validity
	 */
	public void setValidityCheck(boolean check)
	{
		_check_validity = check;
	}
	/**
	 * @return Returns the _check_validity 
	 */
	public boolean getValidityCheck ()
	{
		if (_check_validity) return true;
		else return false;
	}
	
    


    //************************************************************
    // Private methods
    //************************************************************

} // Polygon2SceneWorker
