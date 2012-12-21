package anja.swinggui.polygon;

import javax.swing.*;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.*;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;


import anja.geom.Point2;
import anja.geom.Segment2;
import anja.geom.Rectangle2;
import anja.geom.Circle2;
import anja.geom.Point2List;
import anja.geom.Polygon2;
import anja.geom.Polygon2Scene;

import anja.util.GraphicsContext;
import anja.util.SimpleList;
import anja.util.ListItem;

import java.util.Vector;
import java.io.File;

import java.net.URL;

/**
 * @version 0.7 08.02.05
 * @author Ulrich Handel, Sascha Ternes
 */

//****************************************************************
public class Polygon2SceneEditor
       extends Polygon2SceneWorker
       implements Polygon2ImportListener
//****************************************************************
{
    //************************************************************
    // private constants
    //************************************************************

    // Editier-Zyklen
    private final static int _CYCLE_NONE                = 0;
    private final static int _CYCLE_PRESSED             = 1;
    private final static int _CYCLE_POINT_DRAG          = 3;
    private final static int _CYCLE_SEGMENT_DRAG        = 4;
    private final static int _CYCLE_POLYGON_DRAG        = 5;
    private final static int _CYCLE_POINT_ADD           = 6;
    private final static int _CYCLE_TRANSFORMATION      = 7;
    private final static int _CYCLE_ADD_POLY            = 8;
    private final static int _CYCLE_MOVE                = 9;
    private final static int _CYCLE_TRANSFORMATION_INIT = 10;
    private final static int _CYCLE_ADD_POLY_INIT       = 11;
    private final static int _CYCLE_MOVE_INIT           = 12;
    private final static int _CYCLE_PENCIL              = 14;


    private final static int _SCALE      = 0;
    private final static int _SCALE_PROP = 1;
    private final static int _SCALE_HOR  = 2;
    private final static int _SCALE_VERT = 3;
    private final static int _ROTATE     = 4;
    private final static int _SHEAR_HOR  = 5;
    private final static int _SHEAR_VERT = 6;


    private boolean fullRefreshMode = false;

    //************************************************************
    // protected variables
    //************************************************************

    protected Color color_Grid = Color.gray.brighter();
    protected Color color_Marking = Color.red;
    protected Color color_MarkingCross = Color.black;


    //************************************************************
    // private variables
    //************************************************************

    private Point2   _sel_vertex;  // Selektierter Polygon-Eckpunkt
    private Polygon2 _sel_poly;    // Selektiertes Polygon
    private Segment2 _sel_edge;    // Selektiertes Segment
    //
    // _sel_vertex, _sel_poly und _sel_edge werden durch mousePressed
    // gesetzt und verweisen auf die durch den Mauskursor aktuell
    // ausgewaehlten Objekte der polygonalen Szene.
    //
    // Gegebenenfalls sind ein oder mehrere Verweise gleich null:
    //
    // Es kann z.B nur *entweder* ein Eckpunkt *oder* ein Segment selektiert.
    // sein. Wenn eines von beiden selektiert ist, ist auch das zugehoerige
    // Polygon mit _sel_poly verfuegbar.
    //
    // Ein *geschlossenes* Polygon kann natuerlich auch selektiert sein,
    // ohne dass ein Eckpunkt oder ein Segment ausgewaehlt wurde ( Maus
    // innerhalb des Polygons ).
    //
    // Es ist wichtig, dass es sich bei den Punkten _sel_vertex bzw.
    // _sel_edge.source() und _sel_edge.target() um Verweise auf
    // Punkte ( nicht auf Kopien ) in der Punkteliste des Polygons
    // _sel_poly handelt.
    // Das heisst, durch Veraendern der Koordinaten eines solchen Punktes
    // wird er auch tatsaechlich innerhalb der polygonalen Szene verschoben.

    protected Point2 _sel_mouse; // Mauskursor-Position in Weltkoordinaten
    private Point2 _sel_mark;  // Weltkoordinaten-Punkt auf den sich
                               // eine Objekt-Auswahl bezieht
    
     
    private int _selected;

    private int _edit_cycle = _CYCLE_NONE; // Momentaner Editier-Zyklus

    // Variablen fuer Editier-Zyklus _CYCLE_POINT_ADD _CYCLE_PENCIL
    private Polygon2 _edit_poly;

    private Point2 _fixed_point; // Fixierter Polygonpunkt fuer Stretch,
                                 // Rotate und anderen Operationen

    private int _transformation; // _SCALE, _SCALE_HOR etc.

    
    private Polygon2 _drag_poly;
    private Point2List _drag_points_init;
    private Point2List _drag_points;
    private float _drag_init_x;
    private float _drag_init_y;

    private boolean _seg_drag_mode = false;
        //        Beim Draggen von Segmenten werden ..
        // false: .. keine neuen Eckpunkte
        // true:  .. neue Eckpunkte erzeugt



    private Point2 _pencil_curr;
    private Point2 _pencil_prev;
    private float _pencil_min_angle = 15;
    private float _pencil_min_length = 20;
    private Vector _pencil_points = new Vector();

    private boolean _pencil_mode = true;


    private double _grid_size = 10.0;
    private boolean _grid_active = false;

    private boolean _isRect = false;

    private RandomPolygon2Tool _random_poly_tool;
    private boolean _random_poly_tool_is_valid;
    private PointInsertionTool _point_insertion_tool;
    private boolean _point_insertion_tool_is_valid;

    private ExampleChooser _exampleChooser = null; // the dialog object for choosing example files
    // Items fuer PopupMenu

    private JMenuItem _item_erase_point =
        new JMenuItem( "Erase Point" );
    private JMenuItem _item_erase_segment =
        new JMenuItem( "Erase Segment" );
    private JMenuItem _item_erase_edges =
        new JMenuItem( "Erase Edges" );
    private JMenuItem _item_erase_polygon =
        new JMenuItem( "Erase Polygon" );
    private JMenuItem _item_move_polygon =
        new JMenuItem( "Move Polygon" );
    private JMenuItem _item_copy_polygon =
        new JMenuItem( "Copy Polygon" );
    private JMenuItem _item_bounding_poly =
        new JMenuItem( "Set To Bounding Polygon" );
    private JMenuItem _item_scale =
        new JMenuItem( "Scale" );
    private JMenuItem _item_scale_prop =
        new JMenuItem( "Scale Proportional" );
    private JMenuItem _item_scale_vert =
        new JMenuItem( "Scale Vertical" );
    private JMenuItem _item_scale_hor =
        new JMenuItem( "Scale Horizontal" );
    private JMenuItem _item_rotate =
        new JMenuItem( "Rotate" );
    private JMenuItem _item_shear_vert =
        new JMenuItem( "Shear Vertical" );
    private JMenuItem _item_shear_hor =
        new JMenuItem( "Shear Horizontal" );

    private JCheckBoxMenuItem _item_show_coors =
        new JCheckBoxMenuItem( "Show point coordinates" );

    private JMenuItem _item_random_tool =
        new JMenuItem( "Open random polygon tool" );
    private JMenuItem _item_point_tool =
        new JMenuItem( "Open point insertion tool" );
    
    private JMenuItem _choose_examples =
        new JMenuItem("Load Example file");
    
    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen Polygon-Szenen-Editors
     */

    //============================================================
    public Polygon2SceneEditor()
    //============================================================
    {

        _random_poly_tool = null;
        _random_poly_tool_is_valid = false;
        _point_insertion_tool = null;
        _point_insertion_tool_is_valid = false;


        setShowScene(    true,  false );
        setShowEdges(    true,  false );
        setShowVertices( true, false );

        setVertexSize(    4, false );
        setEndVertexSize( 6, false );


        // Editor als ActionListener bei Menu-Items registrieren

        _item_erase_point.addActionListener( this );
        _item_erase_segment.addActionListener( this );
        _item_erase_edges.addActionListener( this );
        _item_erase_polygon.addActionListener( this );
        _item_move_polygon.addActionListener( this );
        _item_copy_polygon.addActionListener( this );
        _item_scale.addActionListener( this );
        _item_scale_prop.addActionListener( this );
        _item_scale_vert.addActionListener( this );
        _item_scale_hor.addActionListener( this );
        _item_rotate.addActionListener( this );
        _item_shear_vert.addActionListener( this );
        _item_shear_hor.addActionListener( this );
        _item_bounding_poly.addActionListener( this );
        _item_show_coors.addActionListener( this );
        _item_random_tool.addActionListener( this );
        _item_point_tool.addActionListener( this );
        _choose_examples.addActionListener(this);
        
    } // Polygon2SceneEditor



    //************************************************************
    // public methods
    //************************************************************

    /**
      * Setzt ob innerhalb des Editors nur Teilbereiche bei Aenderungen aktualisiert werden oder einfach alles
      */
    public void setFullRefreshMode(boolean value) {
      fullRefreshMode = value;
    }



    /**
      * Setzen der aktuellen Farbe fuer die Markierungen bei Aenderungen der Szene
      */
    public void setMarkingColor( Color c ) {
      color_Marking = c;
      refresh();
    }



    /**
      * Ermitteln der aktuellen Farbe fuer die Markierungen bei Aenderungen der Szene
      */
    public Color getMarkingColor() {
      return color_Marking;
    }



    /**
      * Setzen der aktuellen Farbe fuer das Kreuz bei der Markierung fuer die Rotation
      */
    public void setMarkingCrossColor( Color c ) {
      color_MarkingCross = c;
      refresh();
    }



    /**
      * Ermitteln der aktuellen Farbe fuer das Kreuz bei der Markierung fuer die Rotation
      */
    public Color getMarkingCrossColor() {
      return color_MarkingCross;
    }



    /**
      * Setzen der aktuellen Farbe fuer das Grid
      */
    public void setGridColor( Color c ) {
      color_Grid = c;
      refresh();
    }



    /**
      * Ermitteln der aktuellen Farbe fuer das Grid
      */
    public Color getGridColor() {
      return color_Grid;
    }
    
   
    
    /**
     * Loeschen aller Polygone der angegebenen Art
     */

    //============================================================
    public void erasePolygons( int mode )
    //============================================================
    {
        _cancelEditCycle( false );
        super.erasePolygons( mode );
    } // erasePolygons



    /**
     * Setzen der minimalen Laenge einer Kante beim Erweitern eines
     * offenen Polygons im Pencil-Modus.
     */

    //============================================================
    public void setPencilLength( float length )
    //============================================================
    {
        _pencil_min_length = length;

        fireActionEvent();
    } // setPencilLength



    /**
     * Setzen des minimalen Winkels fuer Pencil-Modus.
     */

    //============================================================
    public void setPencilAngle( float angle )
    //============================================================
    {
        _pencil_min_angle = angle;

        fireActionEvent();
    } // setPencilAngle



    /**
     * Ermitteln der minimalen Laenge einer Kante beim Erweitern eines
     * offenen Polygons im Pencil-Modus.
     */

    //============================================================
    public float getPencilLength()
    //============================================================
    {
        return _pencil_min_length;
    } // getPencilLength



    /**
     * Ermitteln des minimalen Winkels fuer Pencil-Modus.
     */

    //============================================================
    public float getPencilAngle()
    //============================================================
    {
        return _pencil_min_angle;
    } // getPencilAngle



    /**
     * Setzen des Pencil-Modus fuer das draggen eines Endpunktes
     */

    //============================================================
    public void setPencilMode( )
    //============================================================
    {
        _pencil_mode = true;

        fireActionEvent();
    } // setPencilMode



    /**
     * Setzen des Pencil-Modus fuer das draggen eines Endpunktes
     */

    //============================================================
    public boolean getPencilMode( )
    //============================================================
    {
        return _pencil_mode;
    } // getPencilMode



    /**
     * Setzen des Move-Modus fuer das draggen eines Endpunktes
     */

    //============================================================
    public void setMoveMode( )
    //============================================================
    {
        _pencil_mode = false;

        fireActionEvent();
    } // setMoveMode



    /**
     * Modus fuer Grid setzen ( aktiv oder nicht aktiv )
     */

    //============================================================
    public void setGridActive( boolean activate )
    //============================================================
    {
        _grid_active = activate;

        refresh();
    } // setGridActive




    /**
     * Modus fuer Grid ermitteln
     */

    //============================================================
    public boolean getGridActive()
    //============================================================
    {
        return _grid_active;
    } // getGridActive



    /**
     * Setzen der Gridgroesse
     */

    //============================================================
    public void setGridSize( double size )
    //============================================================
    {
        _grid_size = size;

        refresh();
    } // setGridSize



    /**
     * Ermitteln der Gridgroesse
     */

    //============================================================
    public double getGridSize()
    //============================================================
    {
        return _grid_size;
    } // getGridSize


    /**
     * Modus setzen, ob Polygon rechtwinkling ist
     */

    //============================================================
    public void setRect( boolean activate )
    //============================================================
    {
        _isRect = activate;

    } // setRect


    /**
     * Setzen des Modus fuer Segment Drag.
     * true:  Es werden neue Eckpunkte in des Polygon eingefuegt
     * false: Die Original-Eckpunkte werden bewegt
     */

    //============================================================
    public void setSegmentDragMode( boolean mode )
    //============================================================
    {
        _seg_drag_mode = mode;

        fireActionEvent();
    } // setSegmentDragMode



    /**
     * @return Modus fuer Segment-Drag
     */

    //============================================================
    public boolean getSegmentDragMode()
    //============================================================
    {
        return _seg_drag_mode;
    } // getSegmentDragMode



    /**
     * Speichern der Polygonszene
     */

    //============================================================
    public void save( File file )
    //============================================================
    {
        _cancelEditCycle( true );
        super.save( file );
    } // savePolygon2Scene



    /**
     * Setzen der Polygonszene
     */

    //============================================================
    public void setPolygon2Scene( Polygon2Scene scene,
                                  boolean       refresh )
    //============================================================
    {
        _cancelEditCycle( false );
        super.setPolygon2Scene( scene, false );

        if( refresh )
            refresh();
    } // setPolygon2Scene


    /**
     * Sets the example files the user may choose from. Needs the file names, descriptions on the buttons and icons displayed on the buttons.
     * @param fileNames The names of the files containing the examples. (Should have leading "/", see Class.getResource())
     * Files may be located anywhere where they can be found by Class.getResource() (e.g. a subdirectory or the jar file)
     * @param descriptions Descriptions displayed on the buttons instead of the file names. May be null.
     * @param icons Icons displayed on the buttons as previews. If the files are Polygon2Scene files, icons can be generated automatically, in this case icons may be null.
     */
    public void setExamples(String[] fileNames, String[] descriptions, Icon[] icons)
    {
        if (_exampleChooser == null)
            _exampleChooser = new ExampleChooser(JOptionPane.getFrameForComponent(this.getDisplayPanel()), this.getDisplayPanel());
        _exampleChooser.setExamples(fileNames, descriptions, icons);
    }
    
    /**
     * Sets the example files the user may choose from.  Needs the file names, descriptions on the buttons. Files have to be Polygon2Scene files.
     * @param fileNames The names of the files containing the examples. (Should have leading "/", see Class.getResource())
     * Files may be located anywhere where they can be found by Class.getResource() (e.g. a subdirectory or the jar file)
     * @param descriptions Descriptions displayed on the buttons instead of the file names. May be null.
     */
    public void setExamples(String[] fileNames, String[] descriptions)
    {
        this.setExamples(fileNames, descriptions, null);
    }
    
    /**
     * Sets the example files the user may choose from. Files have to be Polygon2Scene files. File names will be displayed on the dialog buttons as descriptions.
     * @param fileNames The names of the files containing the examples. (Should have leading "/", see Class.getResource())
     * Files may be located anywhere where they can be found by Class.getResource() (e.g. a subdirectory or the jar file)
     */
    public void setExamples(String[] fileNames)
    {
        this.setExamples(fileNames, null, null);
    }

    //************************************************************
    // public methods for listeners
    //************************************************************


    /**
     * ActionListener-Methode.
     *
     * Wird aufgerufen, wenn ein Menue-Item des Popup-Menues
     * ausgewaehlt wurde.
     */

    //============================================================
    public void actionPerformed( ActionEvent e )
    //============================================================
    {
        if( e.getSource() == _item_erase_point )    // Erase Point
            removePoint( _sel_poly, _sel_vertex );

        if( e.getSource() == _item_erase_segment )  // Erase Segment
            removeSegment( _sel_poly, _sel_edge );

        if( e.getSource() == _item_erase_edges )    // Erase Edges
            removeEdges( _sel_poly, _sel_vertex );

        if( e.getSource() == _item_erase_polygon )  // Erase Polygon
            removePolygon( _sel_poly, true );


        if( e.getSource() == _item_copy_polygon )   // Copy Polygon
            _startCycleCopyInit( _sel_poly );

        if( e.getSource() == _item_move_polygon )   // Move Polygon
            _startCycleMoveInit( _sel_poly );


        if( e.getSource() == _item_scale )          // Scale
            _startCycleTransformationInit( _SCALE );

        if( e.getSource() == _item_scale_prop )     // Scale Proportional
            _startCycleTransformationInit( _SCALE_PROP );

        if( e.getSource() == _item_scale_hor )      // Scale Horizontal
            _startCycleTransformationInit( _SCALE_HOR );

        if( e.getSource() == _item_scale_vert )     // Scale Vertical
            _startCycleTransformationInit( _SCALE_VERT );

        if( e.getSource() == _item_rotate )         // Rotate
            _startCycleTransformationInit( _ROTATE );

        if( e.getSource() == _item_shear_hor )      // Shear Horizontal
            _startCycleTransformationInit( _SHEAR_HOR );

        if( e.getSource() == _item_shear_vert )     // Shear Vertical
            _startCycleTransformationInit( _SHEAR_VERT );


        if( e.getSource() == _item_bounding_poly )  // Set To
                                                    // Bounding Polygon
        {
            removePolygon( _sel_poly, false );
            setBoundingPolygon( _sel_poly, false, true );
        } // if

        if( e.getSource() == _item_show_coors ) {   // Show point coordinates
            show_coors = _item_show_coors.isSelected();
            refresh();
        } // if


        if ( e.getSource() == _item_random_tool )   // Open (new)
                                                    // RandomPolygon2Tool
        {
        	   if ( ( _random_poly_tool != null ) &&
        	        ( _random_poly_tool_is_valid == false ) ) {
        	      _random_poly_tool.reactivate( this );
        	   } else { // if
        	      _random_poly_tool = new RandomPolygon2Tool();
        	      _random_poly_tool.addImportListener( this );
        	   } // else
        } // if

        if ( e.getSource() == _item_point_tool )    // Open (new)
                                                    // PointInsertionTool
        {
        	   if ( ( _point_insertion_tool != null ) &&
        	        ( _point_insertion_tool_is_valid == false ) ) {
        	      _point_insertion_tool.reactivate( this );
        	   } else { // if
        	      _point_insertion_tool = new PointInsertionTool();
        	      _point_insertion_tool.addImportListener( this );
        	   } // else
        } // if
        
        if (e.getSource() == _choose_examples) {
            String fileName = _exampleChooser.showDialog();
            if (fileName != null) {
                this.load(fileName);
            }
        }
     
    } // actionPerformed



    /**
     * MouseListener-Methode.
     *
     * Wird aufgerufen, wenn ein Maus-Button in der Zeichenflaeche
     * gedrueckt wurde.
     */

    //============================================================
    public void mousePressed( MouseEvent e )
    //============================================================
    {
        // Berechne Mauskursor-Position in Weltkoordinaten
        _sel_mouse = transformScreenToWorld( e.getX(), e.getY() );

        // Ermittle die durch den Mauskursor selektierten Objekte
        // ( _sel_vertex, _sel_edge, _sel_poly )
        _selected = _getSelectedObjects( _sel_mouse );


        // Ermitteln des Punktes _sel_mark.
        if( _sel_vertex != null )
            _sel_mark = new Point2( _sel_vertex );
        else
        if( _sel_edge != null )
            _sel_mark = _sel_edge.plumb( _sel_mouse );
        else
            _sel_mark = _sel_mouse;


        if( e.isMetaDown() )                     // rechte Maustaste
        {
            switch( _edit_cycle )
            {
              case _CYCLE_NONE:
              case _CYCLE_TRANSFORMATION_INIT:
              case _CYCLE_MOVE_INIT:

                _cancelEditCycle( true ); // ggf. laufenden Zyklus
                                          // abbrechen

                // ggf. kontextbezogenes Popup-Menue offnen
                processPopupMenu( e.getX(), e.getY(), _sel_mark, " " );
                break;

              default:

                _cancelEditCycle( true );
                break;

              // !!! Auch wenn es so aussieht, _cancelEditCycle darf
              // auf keinen Fall vor die switch-Anweisung gesetzt
              // werden, da dann immer gilt: _edit_cycle == _CYCLE_NONE

            } // switch
        }
        else                                     // linke Maustaste
        {
            switch( _edit_cycle )
            {
              case _CYCLE_TRANSFORMATION_INIT:
              case _CYCLE_ADD_POLY_INIT:
              case _CYCLE_MOVE_INIT:
                if( _sel_poly != _drag_poly )
                    _cancelEditCycle( true );
                break;

              case _CYCLE_NONE:  // Kein Edit-Zyklus am laufen
                // also kann ein neuer Zyklus begonnen werden.
                _edit_cycle = _CYCLE_PRESSED;
                break;
            } // switch
        } // else
    } // mousePressed




    /**
     */

    //============================================================
    public void mouseDragged( MouseEvent e )
    //============================================================
    {
        if( e.isMetaDown() )
            return;

        switch( _edit_cycle )
        {
          case _CYCLE_TRANSFORMATION_INIT:
            _startCycleTransformation();
            break;

          case _CYCLE_ADD_POLY_INIT:
            _startCycle( _CYCLE_ADD_POLY );
            break;

          case _CYCLE_MOVE_INIT:
            _startCycle( _CYCLE_MOVE );
            break;


          case _CYCLE_PRESSED:
            switch( _selected )
            {
              case FREE_SPACE:
                _startCyclePencil();
                break;
              case FORBIDDEN_SPACE:
                  if (!_check_validity) _startCyclePencil();
                  break;

              case VERTEX:
                if(    _pencil_mode
                    && _sel_poly.isOpen()
                    && (    _sel_vertex == _sel_poly.lastPoint()
                         || _sel_vertex == _sel_poly.firstPoint() ) )

                    _startCyclePencil( _sel_poly, _sel_vertex );
                else
                    _startCyclePointDrag();
                break;

              case EDGE:
                _startCycleSegmentDrag();
                break;

              case POLYGON:
                _startCyclePolygonDrag();
                break;

            } // switch

            break;
        } // switch



        switch( _edit_cycle )
        {
          case _CYCLE_POLYGON_DRAG:
          case _CYCLE_SEGMENT_DRAG:
          case _CYCLE_POINT_DRAG:
          case _CYCLE_ADD_POLY:
          case _CYCLE_MOVE:
            _performCycleDrag( e.getX(), e.getY() );
            break;

          case _CYCLE_TRANSFORMATION:
            _performCycleTransformation( _transformation,
                                         e.getX(), e.getY() );
            break;

          case _CYCLE_PENCIL:
            _performCyclePencil( e.getX(), e.getY() );
            break;

          case _CYCLE_POINT_ADD:
            _performCyclePointAdd( e.getX(), e.getY() );
            break;

          default:
            _cancelEditCycle( true );
            break;
        } // switch

    } // mouseDragged



    /**
     */

    //============================================================
    public void mouseReleased( MouseEvent e )
    //============================================================
    {
    	switch( _edit_cycle )
        {

          case _CYCLE_TRANSFORMATION_INIT:
          case _CYCLE_ADD_POLY_INIT:
          case _CYCLE_MOVE_INIT:
            _cancelEditCycle( true );
            break;

          case _CYCLE_MOVE:
            if( ! _endCycleMove() )
                _edit_cycle = _CYCLE_MOVE_INIT;
            break;

          case _CYCLE_ADD_POLY:
            if( ! _endCycleMove() )
                _edit_cycle = _CYCLE_ADD_POLY_INIT;
            break;


          case _CYCLE_TRANSFORMATION:
            _endCycleTransformation();
            break;


          case _CYCLE_POINT_DRAG:
          case _CYCLE_SEGMENT_DRAG:
            _endCyclePointSegDrag();
            break;


          case _CYCLE_POLYGON_DRAG:
            _endCyclePolygonDrag();
            break;


          case _CYCLE_PRESSED:
            switch( _selected )
            {
              case FREE_SPACE:
                _startCyclePointAdd();
                break;
              case FORBIDDEN_SPACE:
                  if (!_check_validity) _startCyclePointAdd();
                  break;

              case VERTEX:
                _startCyclePointAdd( _sel_poly, _sel_vertex );
                break;

              case EDGE:
                // Neuen Punkt in das zugehoerige Polygon einfuegen
                insertPoint( _sel_poly, _sel_edge, _sel_mouse );
                _edit_cycle = _CYCLE_NONE;
                break;

              default:
                _edit_cycle = _CYCLE_NONE;
                break;
            } // switch
            break;


          case _CYCLE_PENCIL:
            _endCyclePencil( e.getX(), e.getY() );
            break;


          case _CYCLE_POINT_ADD:
            _endCyclePointAdd( e.getX(), e.getY() );
            break;

        } // switch

    } // mouseReleased



    /**
     */

    //============================================================
    public void mouseMoved( MouseEvent e )
    //============================================================
    {
        if( _edit_cycle == _CYCLE_POINT_ADD )
            _performCyclePointAdd( e.getX(), e.getY() );
    } // mouseMoved



    /**
     */

    //============================================================
    public void mouseExited( MouseEvent e )
    //============================================================
    {
        eraseLastXORLine();
    } // mouseExited





    //************************************************************
    // protected inherited methods
    //************************************************************


    /**
     * Diese Methode fuehrt alle notwendigen Abschluss-Aktionen durch,
     * bevor diese Szene vom DisplayPanel entfernt werden kann.
     */

    //============================================================
    protected void removingFromDisplayPanel()
    //============================================================
    {
        _cancelEditCycle( false );
    } // removingFromDisplayPanel



    /**
     * Zeichnen des Hintergrundes
     */

    //============================================================
    protected void paintBackground( Graphics2D g2d, Graphics g )
    //============================================================
    {
        super.paintBackground( g2d, g );

        if( ! _grid_active )
            return;

        // Clipping-Rechteck in Bildkoordinaten ermitteln
        Rectangle clip = g.getClipBounds();

        g.setColor( color_Grid );

        Rectangle2 world_rect =
            transformScreenToWorld( clip.x, clip.y,
                                    clip.width, clip.height );


        double grid_size = Math.max( transformScreenToWorld( 3 ),
                                      _grid_size );

        double world_left =
            ( ( int )( world_rect.x / grid_size ) ) * grid_size;

        double world_right = world_rect.x + world_rect.width;



        for( double x_world = world_left;
             x_world <= world_right;
             x_world += grid_size )
        {
            Point2 pt = transformWorldToScreen( x_world, 0 );
            int x = ( int )pt.x;
            g.drawLine( x, clip.y, x, clip.y + clip.height );
        } // for


        double world_top =
            ( ( int )( world_rect.y / grid_size ) ) * grid_size;

        double world_bottom = world_rect.y + world_rect.height;



        for( double y_world = world_top;
             y_world <= world_bottom;
             y_world += grid_size )
        {
            Point2 pt = transformWorldToScreen( 0, y_world );
            int y = ( int )pt.y;
            g.drawLine( clip.x, y, clip.x + clip.width, y );
        } // for
    } // paintBackground



    /**
     * Zeichnen aller Polygone
     */

    //============================================================
    protected void paintPolygons( Graphics2D g2d, Graphics g )
    //============================================================
    {
        super.paintPolygons( g2d, g );

        switch( _edit_cycle )
        {
          case _CYCLE_ADD_POLY_INIT:
          case _CYCLE_ADD_POLY:
          case _CYCLE_MOVE_INIT:
          case _CYCLE_MOVE:
            GraphicsContext gc = new GraphicsContext();
            gc.setForegroundColor( color_Marking );
            _drag_poly.draw( g2d, gc );
            break;

          case _CYCLE_TRANSFORMATION_INIT:
          case _CYCLE_TRANSFORMATION:
            gc = new GraphicsContext();
            gc.setForegroundColor( color_Marking );
            _drag_poly.draw( g2d, gc );

            double square = transformScreenToWorld( 20 );

            float x = _fixed_point.x;
            float y = _fixed_point.y;

            float dist = (float) square / 4;

            Segment2 hor =
                new Segment2( x - dist, y, x + dist, y );

            Segment2 vert =
                new Segment2( x, y - dist, x, y + dist );

            gc = new GraphicsContext();
            gc.setForegroundColor( color_MarkingCross );

            hor.draw( g2d, gc );
            vert.draw( g2d, gc );

            switch( _transformation )
            {
              case _SCALE:
              case _SCALE_PROP:
              case _SCALE_HOR:
              case _SCALE_VERT:
              case _SHEAR_HOR:
              case _SHEAR_VERT:

                dist = (float) square / 2;

                float x_l = x - dist;
                float x_r = x + dist;
                float y_t = y - dist;
                float y_b = y + dist;

                Segment2 top    = new Segment2( x_l, y_t, x_r, y_t );

                Segment2 bottom = new Segment2( x_l, y_b, x_r, y_b );

                Segment2 left   = new Segment2( x_l, y_t, x_l, y_b );

                Segment2 right  = new Segment2( x_r, y_t, x_r, y_b );

                dist = (float) square / 4;


                Point2 left_top = new Point2();
                Point2 right_top = new Point2();
                Point2 left_bottom = new Point2();
                Point2 right_bottom = new Point2();

                switch( _transformation )
                {
                  case _SCALE_PROP:
                    left_top.moveTo(     x_l - dist, y_t - dist );
                    right_top.moveTo(    x_r + dist, y_t - dist );
                    left_bottom.moveTo(  x_l - dist, y_b + dist );
                    right_bottom.moveTo( x_r + dist, y_b + dist );
                    break;

                  case _SCALE:
                  case _SCALE_HOR:
                    left_top.moveTo(     x_l - dist, y_t );
                    right_top.moveTo(    x_r + dist, y_t );
                    left_bottom.moveTo(  x_l - dist, y_b );
                    right_bottom.moveTo( x_r + dist, y_b );
                    break;

                  case _SCALE_VERT:
                    left_top.moveTo(     x_l, y_t - dist );
                    right_top.moveTo(    x_r, y_t - dist );
                    left_bottom.moveTo(  x_l, y_b + dist );
                    right_bottom.moveTo( x_r, y_b + dist );
                    break;

                  case _SHEAR_HOR:
                    left_top.moveTo(     x_l + dist, y_t );
                    right_top.moveTo(    x_r + dist, y_t );
                    left_bottom.moveTo(  x_l - dist, y_b );
                    right_bottom.moveTo( x_r - dist, y_b );
                    break;

                  case _SHEAR_VERT:
                    left_top.moveTo(     x_l, y_t - dist );
                    right_top.moveTo(    x_r, y_t + dist );
                    left_bottom.moveTo(  x_l, y_b - dist );
                    right_bottom.moveTo( x_r, y_b + dist );
                    break;
                } // switch


                Segment2 top_out    = new Segment2( left_top, right_top );

                Segment2 bottom_out = new Segment2( left_bottom, right_bottom );

                Segment2 left_out   = new Segment2( left_top, left_bottom );

                Segment2 right_out  = new Segment2( right_top, right_bottom );


                gc = new GraphicsContext();
                gc.setForegroundColor( color_Marking );

                top.draw( g2d, gc );
                bottom.draw( g2d, gc );
                left.draw( g2d, gc );
                right.draw( g2d, gc );

                top_out.draw( g2d, gc );
                bottom_out.draw( g2d, gc );
                left_out.draw( g2d, gc );
                right_out.draw( g2d, gc );

                if( _transformation == _SCALE )
                {
                    left_top.moveTo(     x_l, y_t - dist );
                    right_top.moveTo(    x_r, y_t - dist );
                    left_bottom.moveTo(  x_l, y_b + dist );
                    right_bottom.moveTo( x_r, y_b + dist );

                    top_out    = new Segment2( left_top, right_top );
                    bottom_out = new Segment2( left_bottom, right_bottom );
                    left_out   = new Segment2( left_top, left_bottom );
                    right_out  = new Segment2( right_top, right_bottom );

                    top_out.draw( g2d, gc );
                    bottom_out.draw( g2d, gc );
                    left_out.draw( g2d, gc );
                    right_out.draw( g2d, gc );
                } // if



                break;


              case _ROTATE:
                dist = (float) Math.sqrt( square * square / 2 );
                Circle2 circle = new Circle2( x, y, dist );

                gc = new GraphicsContext();
                gc.setForegroundColor( color_Marking );

                circle.draw( g2d, gc );
                break;
            } // switch

            break;
        } // switch
    } // paintPolygons



    /**
     * Aufbauen des Popup-Menues
     */

    //============================================================
    protected void buildPopupMenu( JPopupMenu menu )
    //============================================================
    {
        if (_exampleChooser != null) {
            menu.add(_choose_examples);
            menu.addSeparator();
        }
        
        if( _sel_poly == null ) {
            // Menueeintrag fuer RandomPolygon2Tool:
            menu.add( _item_random_tool );
            // nur ein Tool zulassen:
            if ( _random_poly_tool_is_valid ) {
               _item_random_tool.setEnabled( false );
            } else { // if
               _item_random_tool.setEnabled( true );
            } // else
            // Menueeintrag fuer PointInsertionTool:
            menu.add( _item_point_tool );
            // nur ein Tool zulassen:
            if ( _point_insertion_tool_is_valid ) {
               _item_point_tool.setEnabled( false );
            } else { // if
               _item_point_tool.setEnabled( true );
            } // else
            return; // Ohne selektiertes Polygon sind dem Menu
                    // keine Items zuzufuegen
        } // if

        Polygon2 bounding_poly = getPolygon2Scene().getBoundingPolygon();

        if(    bounding_poly == null
            && getPolygon2Scene().getInteriorPolygons().length == 1
            && _sel_poly.isClosed() )
        {
            menu.add( _item_bounding_poly );
            menu.addSeparator();
        } // if


        switch( _selected )
        {
          case VERTEX:
//            menu.add( new JMenuItem( _sel_vertex.toString() ) );
            if( _sel_poly.length() > 1 )
            {
                _item_erase_point.setText( "Erase Vertex" );
                menu.add( _item_erase_point );


                if(   _sel_poly.length() > 3
                   && (   _sel_poly.isClosed()
                       || (   _sel_vertex != _sel_poly.firstPoint()
                           && _sel_vertex != _sel_poly.lastPoint() ) ) )
                {
                    _item_erase_edges.setText( "Erase Edges" );
                    menu.add( _item_erase_edges );
                } // if
            }
            else
            {
                _item_erase_point.setText( "Erase Point" );
                menu.add( _item_erase_point );
            } // else
            break;

          case EDGE:
            if( _sel_poly.length() > 2 )
                _item_erase_segment.setText( "Erase Edge" );
            else
                _item_erase_segment.setText( "Erase Segment" );
            menu.add( _item_erase_segment );
            break;
        } // switch

        if( _sel_poly.length() > 1 )
        {
            if( _sel_poly == bounding_poly )
            {
                _item_erase_polygon.setText( "Open Scene" );
                menu.add( _item_erase_polygon );

                _item_move_polygon.setText( "Move Bounding Polygon" );
            }
            else
            {
                if( _sel_poly.length() == 2 )
                {
                    _item_move_polygon.setText( "Move Segment" );
                    _item_copy_polygon.setText( "Copy Segment" );
                }
                else
                {
                    _item_erase_polygon.setText( "Erase Polygon" );
                    menu.add( _item_erase_polygon );

                    _item_move_polygon.setText( "Move Polygon" );
                    _item_copy_polygon.setText( "Copy Polygon" );
                } // else
            } // else


            menu.addSeparator();

            menu.add( _item_move_polygon );

            if( _sel_poly != bounding_poly )
                menu.add( _item_copy_polygon );

            menu.addSeparator();

            menu.add( _item_scale );
            menu.add( _item_scale_prop );
            menu.add( _item_scale_hor );
            menu.add( _item_scale_vert );

            menu.addSeparator();

            menu.add( _item_rotate );

            if( _sel_poly.length() > 2 )
            {
                menu.addSeparator();
                menu.add( _item_shear_hor );
                menu.add( _item_shear_vert );
            } // if

            menu.addSeparator();
            menu.add( _item_show_coors );
            
        } // if
 
    } // buildPopupMenu


   //**************************************************************************
   // Interface Polygon2ImportListener
   //**************************************************************************

   /*
   * [javadoc-Beschreibung wird aus Polygon2ImportListener kopiert]
   */
   public void importPolygon(
      Polygon2 polygon,
      Object exporter
   ) {

      // Zufallspolygone muessen noch transformiert werden:
      if ( exporter instanceof RandomPolygon2Tool ) {

         SimpleList points = polygon.points(); // Liste der Polygonpunkte

         // ( bisherige ) Minimal- und Maximal-Werte der Punkt-Koordinaten
         // im Screen-Koordinatensystem des Random-Polygon-Tools
         double x_min = Double.MAX_VALUE;
         double y_min = Double.MAX_VALUE;
         double x_max = -Double.MAX_VALUE;
         double y_max = -Double.MAX_VALUE;

         // Alle Punktkoordinaten des Polygons in die entsprechenden
         // Screenkoordinaten des Random-Polygon-Tools umwandeln.
         // Nebenbei werden die Extremwerte ( x_min u.s.w ) der neuen
         // Koordinaten ermittelt
         for( ListItem item = points.first();
              item != null;
              item = points.next( item ) ) {
            Point2 pt = ( Point2 )points.value( item );
            Point2 pt_1 = _random_poly_tool.transformWorldToScreen( pt.x,
                                                                        pt.y );
            x_min = Math.min( x_min, pt_1.x );
            x_max = Math.max( x_max, pt_1.x );
            y_min = Math.min( y_min, pt_1.y );
            y_max = Math.max( y_max, pt_1.y );
            pt.moveTo( pt_1 );
         } // for

         // Distanzen ermitteln mit denen die Screenkoordinaten in x- und
         // y-Richtung verschoben werden muessen, um das Polygon in der
         // Zeichenflaeche des Editors zu zentrieren
         Dimension dim = getCanvas().getSize();
         double d_x = ( dim.width  - ( x_min + x_max ) ) / 2.0;
         double d_y = ( dim.height - ( y_min + y_max ) ) / 2.0;

         // Alle Punktkoordinaten des Polygons um die ermittelten
         // Distanzen verschieben und anschliessend in Weltkoordinaten
         // umwandeln
         for( ListItem item = points.first();
            item != null;
            item = points.next( item ) ) {
            Point2 pt = ( Point2 )points.value( item );
            Point2 pt_1 = transformScreenToWorld( pt.x + d_x, pt.y + d_y );
            pt.moveTo( pt_1 );
         } // for

      } // if

      _cancelEditCycle( false ); // Falls momentan ein Editierzyklus
                                 // aktiv ist, wird dieser abgebrochen

      // Importiertes Polygon der dargestellten Szene zufuegen
      //
      if ( addInteriorPolygon( polygon, true, true ) == false ) {
         // Das Polygon liess sich nicht ueberschneidungsfrei einfuegen,
         // also wird ein Editierzyklus gestartet, der es dem Benutzer
         // ermoeglicht das Polygon endgueltig zu positionieren
         //
         _startCycleAddPolyInit( polygon );
      } // if

   } // importPolygon


   /*
   * [javadoc-Beschreibung wird aus Polygon2ImportListener kopiert]
   */
   public void instanceClosed(
      Object exporter
   ) {

      if ( exporter instanceof RandomPolygon2Tool ) {
         _random_poly_tool_is_valid = false;
         _random_poly_tool = null;
      } // if
      else
      if ( exporter instanceof PointInsertionTool ) {
         _point_insertion_tool_is_valid = false;
         _point_insertion_tool = null;
      } // if

   } // instanceClosed


   /*
   * [javadoc-Beschreibung wird aus Polygon2ImportListener kopiert]
   */
   public void instanceOpened(
      Object exporter
   ) {

      if ( exporter instanceof RandomPolygon2Tool )
         _random_poly_tool_is_valid = true;
      else
      if ( exporter instanceof PointInsertionTool )
         _point_insertion_tool_is_valid = true;

   } // instanceOpened


    //************************************************************
    // Private methods
    //************************************************************


    /**
     * Aktuellen Editier-Zyklus abbrechen
     */

    //============================================================
    private void _cancelEditCycle( boolean redraw )
    //============================================================
    {
        switch( _edit_cycle )
        {
          case _CYCLE_ADD_POLY_INIT:
          case _CYCLE_ADD_POLY:
            removePolygon( _drag_poly, false );
            break;

          case _CYCLE_TRANSFORMATION:
          case _CYCLE_MOVE:
          case _CYCLE_POINT_DRAG:
          case _CYCLE_POLYGON_DRAG:
            _copyPointCoors( _drag_points_init, _drag_points );
            break;

          case _CYCLE_SEGMENT_DRAG:
            _copyPointCoors( _drag_points_init, _drag_points );

            if( _seg_drag_mode )
            {
                // Die zusaetzlich eingefuegten Eckpunkte muessen
                // wieder entfernt werden

                SimpleList pts = _drag_poly.points();

                boolean is_bounding = removePolygon( _drag_poly, false );

                ListItem item = pts.find( _drag_points.firstPoint() );
                pts.remove( item );
                item = pts.find( _drag_points.lastPoint() );
                pts.remove( item );

                addPolygon( is_bounding, _drag_poly, false, false );
            } // if

            break;
        } // switch

        _edit_cycle = _CYCLE_NONE;

        if( redraw )
            updateDisplayPanel();

    } // _cancelEditCycle



    /**
     * Ermitteln der durch den gegebenen Punkt ( Weltkoordinaten )
     * selektierten Objekte ( Eckpunkt, Polygon, Segment ).
     *
     * Die Objekte sind nach der Methode in _sel_vertex, _sel_poly
     * und _sel_edge verfuegbar.
     * ( Gegebenenfalls null )
     */

    //============================================================
    private int _getSelectedObjects( Point2 point )
    //============================================================
    {
        int ret = getSelectedObjects( point );

        _sel_poly   = getSelectedPolygon();
        _sel_vertex = getSelectedVertex();
        _sel_edge   = getSelectedEdge();
                
        return ret;
    } // _getSelectedObjects



    /**
     */

    //============================================================
    private boolean _concatEndPoints()
    //============================================================
    {
        if( _drag_poly.isClosed() )
            return false;

        Point2 first = _drag_poly.firstPoint();
        Point2 last  = _drag_poly.lastPoint();

        boolean first_dragged =
            ( _drag_points.points().find( first ) != null );

        boolean last_dragged =
            (   last != first
             && _drag_points.points().find( last ) != null );


        Point2 sel_vertex_first = null;
        Polygon2 sel_poly_first = null;
        if( first_dragged )
        {
            sel_vertex_first = getSelectedVertex( first );
            if( sel_vertex_first != null )
            {
                Polygon2 poly =
                    getPolygon2Scene().
                    getPolygonWithVertex( sel_vertex_first );

                if(   poly.isOpen()
                   && (   poly.firstPoint() == sel_vertex_first
                       || poly.lastPoint()  == sel_vertex_first ) )
                {
                    if( poly != _drag_poly || ! last_dragged )
                        sel_poly_first = poly;
                } // if
            } // if
        } // if

        Point2 sel_vertex_last = null;
        Polygon2 sel_poly_last = null;
        if( last_dragged )
        {
            sel_vertex_last = getSelectedVertex( last );
            if(    sel_vertex_last != sel_vertex_first
                && sel_vertex_last != null )
            {
                Polygon2 poly =
                    getPolygon2Scene().
                    getPolygonWithVertex( sel_vertex_last );

                if(   poly.isOpen()
                   && (   poly.firstPoint() == sel_vertex_last
                       || poly.lastPoint()  == sel_vertex_last ) )
                {
                    if( poly != _drag_poly || ! first_dragged )
                        sel_poly_last = poly;
                } // if
            } // if
        } // if


        if( sel_poly_first == null && sel_poly_last == null )
            return false;


        removePolygon( _drag_poly, false );

        Polygon2 new_poly = new Polygon2( _drag_poly );
        _copyPointCoors( _drag_points_init, _drag_points );

        Polygon2 init_poly = new Polygon2( _drag_poly );

        // _drag_poly ist jetzt wieder das unveraenderte Original,
        // waehrend new_poly eine Kopie des veraenderten Polygons
        // ist.

        // init_poly ist eine Kopie des unveraenderten Originals


        if(   sel_poly_first == _drag_poly
           || sel_poly_last == _drag_poly )
        {
            if( _drag_poly.length() >= 4 )
            {

                if( sel_poly_last == _drag_poly )
                {
                    new_poly.lastPoint().moveTo( new_poly.firstPoint() );
                    new_poly.removeFirstPoint();
                    init_poly.removeFirstPoint();
                }
                else
                {
                    new_poly.firstPoint().moveTo( new_poly.lastPoint() );
                    new_poly.removeLastPoint();
                    init_poly.removeLastPoint();
                } // else

                addInteriorPolygon( new_poly, false, false );

                if(     getPolygon2Scene().polygonIsValid( new_poly, init_poly )
                    &&  closeOpenPolygon( new_poly ) )
                    return true;

                removePolygon( new_poly,false );

            } // if
        } // if

        else
        if( sel_poly_first == sel_poly_last )
        {
            if( sel_poly_first.length() + _drag_poly.length() >= 5 )
            {
                removePolygon( sel_poly_first, false );

                Polygon2 poly_conc = new Polygon2( sel_poly_first );

                if( sel_vertex_first == sel_poly_first.firstPoint() )
                    poly_conc.points().reverse();

                new_poly.lastPoint().moveTo( poly_conc.firstPoint() );
                new_poly.firstPoint().moveTo( poly_conc.lastPoint() );

                poly_conc.removeFirstPoint();
                poly_conc.removeLastPoint();

                new_poly.appendCopy( poly_conc );
                init_poly.appendCopy( poly_conc );

                addInteriorPolygon( new_poly, false, false );

                if(     getPolygon2Scene().polygonIsValid( new_poly, init_poly )
                    &&  closeOpenPolygon( new_poly ) )
                    return true;

                removePolygon( new_poly,false );

                addInteriorPolygon( sel_poly_first, false, false );
            } // if
        } // if

        else
        {
            if( sel_poly_first != null )
                removePolygon( sel_poly_first, false );
            if( sel_poly_last != null )
                removePolygon( sel_poly_last, false );

            if( sel_poly_last != null )
            {
                Polygon2 poly_conc = new Polygon2( sel_poly_last );

                if( sel_vertex_last == sel_poly_last.lastPoint() )
                    poly_conc.points().reverse();

                new_poly.lastPoint().moveTo( poly_conc.firstPoint() );
                poly_conc.removeFirstPoint();

                new_poly.appendCopy( poly_conc );
                init_poly.appendCopy( poly_conc );
            } // if

            if( sel_poly_first != null )
            {
                new_poly.points().reverse();
                init_poly.points().reverse();

                Polygon2 poly_conc = new Polygon2( sel_poly_first );

                if( sel_vertex_first == sel_poly_first.lastPoint() )
                    poly_conc.points().reverse();

                new_poly.lastPoint().moveTo( poly_conc.firstPoint() );
                poly_conc.removeFirstPoint();

                new_poly.appendCopy( poly_conc );
                init_poly.appendCopy( poly_conc );
            } // if

            addInteriorPolygon( new_poly, false, false );
            if( getPolygon2Scene().polygonIsValid( new_poly, init_poly ) )
            {
                refresh();
                return true;
            } // if
            removePolygon( new_poly,false );

            if( sel_poly_first != null )
                addInteriorPolygon( sel_poly_first, false, false );
            if( sel_poly_last != null )
                addInteriorPolygon( sel_poly_last, false, false );
        } // else

        addInteriorPolygon( _drag_poly, false, false );
        refresh();
        return true;
    } // _concatEndPoints



    /**
     *
     */

    //============================================================
    private void _copyPointCoors( Point2List source,
                                  Point2List target )
    //============================================================
    {
        SimpleList points_s = source.points();
        SimpleList points_t = target.points();

        ListItem s = points_s.first();
        ListItem t = points_t.first();

        while( s != null && t != null )
        {
            Point2 point_s = ( Point2 )points_s.value( s );
            Point2 point_t = ( Point2 )points_t.value( t );

            point_t.moveTo( point_s );

            s = points_s.next( s );
            t = points_t.next( t );
        } // while
    } // _copyPointCoors



    /**
     * Berechnet den Winkel im Bogenmass, um den der Linienzug
     * ( pt_first, pt_angle, pt_last ) am Punkt pt_angle abknickt.
     */

    //============================================================
    private double _getAngle( Point2 pt_first,
                              Point2 pt_angle,
                              Point2 pt_last )
    //============================================================
    {
        if(    pt_angle.equals( pt_first )
            || pt_angle.equals( pt_last ) )
            return 0.0;

        double alpha1 = pt_first.angle( pt_angle );
        double alpha2 = pt_angle.angle( pt_last );

        double angle = Math.abs( alpha1 - alpha2 );

        if( angle > Math.PI )
            angle = 2 * Math.PI - angle;

        return angle;
    } // _getAngle



    /**
     * Ermittelt aus der Weltkoordinatenposition ( x, y ) den
     * zugeordneten Grid-Punkt.
     */

    //============================================================
    private Point2 _snapToGrid( double x, double y )
    //============================================================
    {
        double dist_x = _grid_size / 2;
        if( x < 0 )
            dist_x = - dist_x;

        double dist_y = _grid_size / 2;
        if( y < 0 )
            dist_y = - dist_y;

        double grid_x =
            (int)( ( x + dist_x ) / _grid_size ) * _grid_size;

        double grid_y =
            (int)( ( y + dist_y ) / _grid_size ) * _grid_size;

        return new Point2( grid_x, grid_y );
    } // _snapToGrid




    /**
     * Ermittelt aus der Weltkoordinatenposition ( x, y ) den
     * zugehoerigen Punkt fuer ein rechtwinkliges Polygon.
     */

    //============================================================
    private Point2 _snapToRect( double x, double y )
    //============================================================
    {
        // Vorherigen Punkt holen um bezug zu setzen...

        Point2 lastpoint = _edit_poly.lastPoint();
        
        if (lastpoint==null) 
                return new Point2( x, y );

        double dist_x = (x-lastpoint.x)*(x-lastpoint.x);

        double dist_y = (y-lastpoint.y)*(y-lastpoint.y);

        double rect_x = x;
        double rect_y = y;        


        if (dist_x >= dist_y){
                // X-Koordinate wird akzeptiert und Y-Koordinate an vorigen Punkt angepasst
                rect_y = lastpoint.y;
        } // if
        
        else rect_x = lastpoint.x; // Sonst eben andersrum

        return new Point2( rect_x, rect_y );
    } // _snapToRect














    /**
     * Starten des Editier-Zyklus _CYCLE_PENCIL
     */

    //============================================================
    private void _startCyclePencil()
    //============================================================
    {
        Point2 point;

        if( _grid_active )
            point = _snapToGrid( _sel_mouse.x, _sel_mouse.y );

        else
            point = _sel_mouse;

        Polygon2 poly = createNewInteriorPolygon( point );
        if( poly != null )
            _startCyclePencil( poly, point );
        else
            _edit_cycle = _CYCLE_NONE;
    } // _startCyclePencil



    /**
     * Starten des Editier-Zyklus _CYCLE_PENCIL
     */

    //============================================================
    private void _startCyclePencil( Polygon2 poly, Point2 point )
    //============================================================
    {
        _edit_cycle = _CYCLE_PENCIL;

        _pencil_prev = null;
        _pencil_curr = point;

        if( point == poly.firstPoint() )
            poly.points().reverse();

        _edit_poly = poly;

        _pencil_points.clear();
        _pencil_points.addElement( _pencil_curr );
    } // _startCyclePencil



    /**
     * Starten des Editier-Zyklus _CYCLE_ADD_POLY_INIT mit einer
     * Kopie des gegebenen Polygons als neu einzufuegendes Polygon
     */

    //============================================================
    private void _startCycleCopyInit( Polygon2 poly )
    //============================================================
    {
        Polygon2 poly_copy = new Polygon2( poly );

        double dist = transformScreenToWorld( 10 );
        if( _grid_active )
        {
            int ratio = ( int )( dist / _grid_size );
            double new_dist = ratio * _grid_size;

            if( new_dist < dist )
                new_dist += _grid_size;

            dist = new_dist;
        } // if

        poly_copy.translate( ( float )dist, ( float )dist );

        _startCycleAddPolyInit( poly_copy );
    } // _startCycleCopyInit



    /**
     * Starten des Editier-Zyklus _CYCLE_ADD_POLY_INIT
     */

    //============================================================
    private void _startCycleAddPolyInit( Polygon2 poly )
    //============================================================
    {
        _drag_poly = poly;

        _edit_cycle = _CYCLE_ADD_POLY_INIT;

        addInteriorPolygon( _drag_poly, false, false );

        setForegroundPoly( _drag_poly );

        updateDisplayPanel();
    } // _startCycleAddPolyInit



    /**
     * Starten des Editier-Zyklus _CYCLE_MOVE_INIT
     */

    //============================================================
    private void _startCycleMoveInit( Polygon2 poly )
    //============================================================
    {
        _edit_cycle = _CYCLE_MOVE_INIT;

        _drag_poly = poly;
        setForegroundPoly( _drag_poly );

        updateDisplayPanel();
    } // _startCycleMoveInit



    /**
     * Starten des Editier-Zyklus _CYCLE_TRANSFORMATION_INIT
     */

    //============================================================
    private void _startCycleTransformationInit( int mode )
    //============================================================
    {
        _edit_cycle = _CYCLE_TRANSFORMATION_INIT;

        _transformation = mode;

        if( _grid_active && _selected != VERTEX )
            _fixed_point = _snapToGrid( _sel_mouse.x, _sel_mouse.y );

        else
            _fixed_point = _sel_mark;


        _drag_poly = _sel_poly;
        setForegroundPoly( _drag_poly );

        updateDisplayPanel();
    } // _startCycleTransformationInit



    /**
     * Starten des Editier-Zyklus _CYCLE_TRANSFORMATION
     */

    //============================================================
    private void _startCycleTransformation()
    //============================================================
    {
        _edit_cycle = _CYCLE_TRANSFORMATION;

        Point2 point;
        if( _grid_active && _selected != VERTEX )
            point = _snapToGrid( _sel_mouse.x, _sel_mouse.y );
        else
            point = _sel_mark;

        _initDragVariables( _drag_poly, point );
    } // _startCycleTransformation



    /**
     * Starten der Editier-Zyklen CYCLE_COPY und CYCLE_MOVE
     */

    //============================================================
    private void _startCycle( int cycle )
    //============================================================
    {
        _edit_cycle = cycle;

        Point2 point;
        if( _grid_active )
            point = _snapToGrid( _sel_mouse.x, _sel_mouse.y );
        else
            point = _sel_mark;


        _initDragVariables( _drag_poly, point );
    } // _startCycle




    /**
     * Starten des Editier-Zyklus _CYCLE_POINT_DRAG
     */

    //============================================================
    private void _startCyclePointDrag()
    //============================================================
    {
        _edit_cycle = _CYCLE_POINT_DRAG;

        Point2List points = new Point2List();
        points.addPoint( _sel_vertex );

        _initDragVariables( points, _sel_vertex );

        _drag_poly = _sel_poly;
        setForegroundPoly( _drag_poly );
    } // _startCyclePointDrag



    /**
     * Starten des Editier-Zyklus _CYCLE_SEGMENT_DRAG
     */

    //============================================================
    private void _startCycleSegmentDrag()
    //============================================================
    {
        _edit_cycle = _CYCLE_SEGMENT_DRAG;

        if( _seg_drag_mode )
        {
            Segment2 edge_copy =
                new Segment2( new Point2( _sel_edge.source() ),
                              new Point2( _sel_edge.target() ) );

            // Es werden neue Eckpunkte erzeugt
            SimpleList pts = _sel_poly.points();

            boolean is_bounding = removePolygon( _sel_poly, false );

            ListItem item = pts.find( _sel_edge.target() );
            pts.insert( item, edge_copy.source() );
            pts.insert( item, edge_copy.target() );

            addPolygon( is_bounding, _sel_poly, false, false );

            _sel_edge = edge_copy;
        } // if


        Point2List points = new Point2List();
        points.addPoint( _sel_edge.source() );
        points.addPoint( _sel_edge.target() );


        Point2 point;
        if( _grid_active )
            point = _snapToGrid( _sel_mouse.x, _sel_mouse.y );

        else
            point = _sel_mark;

        _initDragVariables( points, point );

        _drag_poly = _sel_poly;
        setForegroundPoly( _drag_poly );
    } // _startCycleSegmentDrag



    /**
     * Starten des Editier-Zyklus _CYCLE_POLYGON_DRAG
     */

    //============================================================
    private void _startCyclePolygonDrag()
    //============================================================
    {
        _edit_cycle = _CYCLE_POLYGON_DRAG;

        Point2 point;
        if( _grid_active )
            point = _snapToGrid( _sel_mouse.x, _sel_mouse.y );

        else
            point = _sel_mark;

        _initDragVariables( _sel_poly, point );

        _drag_poly = _sel_poly;
        setForegroundPoly( _drag_poly );
    } // _startCyclePolygonDrag



    /**
     * Starten des Editier-Zyklus _CYCLE_POINT_ADD
     */

    //============================================================
    private void _startCyclePointAdd()
    //============================================================
    {
        Point2 point;
        if( _grid_active )
            point = _snapToGrid( _sel_mouse.x, _sel_mouse.y );

        else
            point = _sel_mouse;

        _edit_poly = createNewInteriorPolygon( point );
        if( _edit_poly != null )
            _edit_cycle = _CYCLE_POINT_ADD;
        else
            _edit_cycle = _CYCLE_NONE;
    } // _startCyclePointAdd



    /**
     * Starten des Editier-Zyklus _CYCLE_POINT_ADD
     */

    //============================================================
    private void _startCyclePointAdd( Polygon2 poly, Point2 point )
    //============================================================
    {
        if(   poly.isOpen()
           && (   point == poly.lastPoint()
               || point == poly.firstPoint() ) )
        {
            if( point == poly.firstPoint() )
                poly.points().reverse();

            _edit_poly = poly;
            _edit_cycle = _CYCLE_POINT_ADD;
        }
        else
            _edit_cycle = _CYCLE_NONE;
    } // _startCyclePointAdd



    /**
     * Initialisieren der privaten Variablen
     * _drag_init_x, _drag_init_y, _drag_points und _drag_points_init.
     */

    //============================================================
    private void _initDragVariables( Point2List points, Point2 point )
    //============================================================
    {
        _drag_init_x = point.x;
        _drag_init_y = point.y;

        _drag_points = points;
        _drag_points_init = new Point2List( _drag_points );
    } // _initDragVariables



    /**
     * Ausfuehren des Edit-Zyklus _CYCLE_PENCIL
     */

    //============================================================
    private void _performCyclePencil( int mouse_x, int mouse_y )
    //============================================================
    {
        eraseLastXORLine();

        // Berechne Mauskursor-Position in Weltkoordinaten

        Point2 mouse = transformScreenToWorld( mouse_x, mouse_y );
        Point2 sel_mouse = mouse;


        Point2 sel_vertex = getSelectedVertex( sel_mouse );

        boolean ready = false;
        if( sel_vertex != null )
        {
            Polygon2 sel_poly = getSelectedPolygon();

            if( sel_vertex == _edit_poly.firstPoint() )
            {
                if( closeOpenPolygon( _edit_poly ) )
                    _edit_cycle = _CYCLE_NONE;
                ready = true;
            } // if

            else
            if(    sel_poly != _edit_poly
                && sel_poly.isOpen()
                && (   sel_vertex == sel_poly.lastPoint()
                    || sel_vertex == sel_poly.firstPoint() ) )
            {
                if( sel_vertex == sel_poly.lastPoint() )
                    sel_poly.points().reverse();

                if( concatOpenPolygons( _edit_poly, sel_poly ) )
                {
                    if( sel_poly.length() > 1 )
                        _edit_cycle = _CYCLE_NONE;
                    else
                    {
                        _pencil_prev = _pencil_curr;
                        _pencil_curr = sel_vertex;

                        _pencil_points.addElement( _pencil_curr );
                        ready = true;
                    } //
                } // if
            } // if

            if( _edit_cycle == _CYCLE_NONE )
                return;
        } // if

        if( ! ready )
        {
            if( _grid_active )
                sel_mouse = _snapToGrid( sel_mouse.x, sel_mouse.y );
            else if( _isRect )
                sel_mouse = _snapToRect( sel_mouse.x, sel_mouse.y );


            // Minimale Segmentlaenge in Weltkoordinaten ermitteln
            double min_length =
                transformScreenToWorld( _pencil_min_length );

            // Minimalen Abknickwinkel im Bogenmass ermitteln
            double min_angle = ( _pencil_min_angle * Math.PI / 180 );


            double max_angle = -1;
            int index = -1;
            boolean not_valid = false;
            for( int i = _pencil_points.size() - 1; i >= 0; i-- )
            {
                Point2 pt = ( Point2 )_pencil_points.elementAt( i );

                if( pt == _pencil_curr )
                    break;

                if(    sel_mouse.distance( pt ) <=
                       transformScreenToWorld( 5 )
                    || _pencil_curr.distance( pt ) <=
                       transformScreenToWorld( 5 ) )
                    continue;

                if( _check_validity && ! getPolygon2Scene().
                        segmentIsValid( new Segment2( _pencil_curr, pt ) ) )
                {
                    not_valid = true;
                    continue;
                } // if

                double alpha =
                    _getAngle( _pencil_curr, pt, sel_mouse );

                if(    not_valid
                    || _pencil_curr.distance( pt ) >= min_length
                    || alpha > Math.PI * 0.51 )
                {
                    if( alpha > max_angle )
                    {
                        max_angle = alpha;
                        index = i;
                    } // if
                } // if
            } // for

            if( index >= 0  && ( not_valid ||  max_angle >= min_angle  ) )
            {
                Point2 point_max_angle =
                    ( Point2 )_pencil_points.elementAt( index );

                boolean eraselast = false;

                if( _pencil_prev != null )
                {
                    eraselast = true;
                    for( int i = index; i >= 0; i-- )
                    {
                        Point2 pt = ( Point2 )_pencil_points.elementAt( i );

                        if( pt == _pencil_prev )
                            break;

                        if(    point_max_angle.distance( pt ) <=
                               transformScreenToWorld( 5 )
                            || _pencil_prev.distance( pt ) <=
                               transformScreenToWorld( 5 ) )
                            continue;

                        double alpha =
                            _getAngle( _pencil_prev, pt, point_max_angle );

                        if(   alpha > Math.PI * 0.51
                           || (    alpha >= min_angle
                                && point_max_angle.distance( pt )
                                   >= min_length
                                && _pencil_prev.distance( pt )
                                   >= min_length ) )
                        {
                            eraselast = false;
                            break;
                        } // if
                    } // for
                } // if

                if( eraselast )
                {
                    _edit_poly.removeLastPoint();
                    boolean is_valid =
                        getPolygon2Scene().segmentIsValid(
                            new Segment2( _pencil_prev,
                                          point_max_angle ) );
                    _edit_poly.addPoint( _pencil_curr );

                    if( is_valid )
                    {
                        removePolygon( _edit_poly, false );
                        _edit_poly.removeLastPoint();
                        addInteriorPolygon( _edit_poly, false, false );

                        Segment2[] segs = new Segment2[ 1 ];
                        segs[ 0 ] = new Segment2( _pencil_prev, _pencil_curr );
                        Rectangle2D rect = Segment2.getBoundingRect( segs );

                        if (fullRefreshMode)
                          refresh();
                        else
                          refresh( rect.getX(), rect.getY(), 
                          		   rect.getWidth(), rect.getHeight() );
                        _pencil_curr = _pencil_prev;
                    }
                }
                extendOpenPolygon( _edit_poly, point_max_angle, _check_validity );
                _pencil_prev = _pencil_curr;
                _pencil_curr = point_max_angle;
            } // if

            _pencil_points.addElement( sel_mouse );
        } // else

        drawXORLine( _pencil_curr, mouse );
    } // _performCyclePencil



    /**
     * Ausfuehren eines Drag-Edit-Zyklus
     */

    //============================================================
    private void _performCycleDrag( int mouse_x, int mouse_y )
    //============================================================
    {
        // Berechne Mauskursor-Position in Weltkoordinaten
        Point2 sel_mouse = transformScreenToWorld( mouse_x, mouse_y );

        if( _grid_active )
            sel_mouse = _snapToGrid( sel_mouse.x, sel_mouse.y );

        double dist_x = sel_mouse.x - _drag_init_x;
        double dist_y = sel_mouse.y - _drag_init_y;

        SimpleList points_s = _drag_points_init.points();
        SimpleList points_t = _drag_points.points();

        Rectangle2D rect = _drag_poly.getBoundingRect();


        ListItem s = points_s.first();
        ListItem t = points_t.first();

        double xmin = rect.getX();
        double ymin = rect.getY();
        double xmax = rect.getX() + rect.getWidth();
        double ymax = rect.getY() + rect.getHeight();

        while( s != null && t != null )
        {
            Point2 point_s = ( Point2 )points_s.value( s );
            Point2 point_t = ( Point2 )points_t.value( t );

            point_t.moveTo( point_s.x + dist_x,
                            point_s.y + dist_y );

            xmin = Math.min( point_t.x, xmin );
            ymin = Math.min( point_t.y, ymin );
            xmax = Math.max( point_t.x, xmax );
            ymax = Math.max( point_t.y, ymax );

            s = points_s.next( s );
            t = points_t.next( t );
        } // while

        if (fullRefreshMode)
          refresh();
        else
          refresh( xmin, ymin, xmax - xmin, ymax - ymin );
    } // _performCycleDrag



    /**
     * Ausfuehren des Edit-Zyklus CycleTransformation
     */

    //============================================================
    private void _performCycleTransformation( int mode,
                                              int x_m,
                                              int y_m )
    //============================================================
    {
        // Berechne Mauskursor-Position in Weltkoordinaten
        Point2 sel_mouse = transformScreenToWorld( x_m, y_m );

        if( _grid_active )
            sel_mouse = _snapToGrid( sel_mouse.x, sel_mouse.y );

        double fixed_x = _fixed_point.x;
        double fixed_y = _fixed_point.y;

        double init_diff_x = _drag_init_x - fixed_x;
        double init_diff_y = _drag_init_y - fixed_y;
        double curr_diff_x = sel_mouse.x - fixed_x;
        double curr_diff_y = sel_mouse.y - fixed_y;

        double m00 = 1.0;
        double m01 = 0;
        double m10 = 0;
        double m11 = 1.0;

        double min_dist = transformScreenToWorld( 10 );

        boolean no_x_scale = false;
        boolean no_y_scale = false;

        switch( mode )
        {
          case _SCALE_PROP:

            double max = Math.max( Math.abs( init_diff_x ),
                                   Math.abs( init_diff_y ) );

            if( init_diff_x < 0.0 )
                init_diff_x = - max;
            else
                init_diff_x = max;

            if( init_diff_y < 0.0 )
                init_diff_y = - max;
            else
                init_diff_y = max;

            max = Math.max( Math.abs( curr_diff_x ),
                            Math.abs( curr_diff_y ) );

            if( curr_diff_x < 0.0 )
                curr_diff_x = - max;
            else
                curr_diff_x = max;

            if( curr_diff_y < 0.0 )
                curr_diff_y = - max;
            else
                curr_diff_y = max;

          case _SCALE:
          case _SCALE_HOR:
          case _SCALE_VERT:

            if( mode != _SCALE_VERT )
            {
                if( Math.abs( init_diff_x ) < min_dist )
                    _drag_init_x = ( float ) sel_mouse.x;
                else
                    m00 = curr_diff_x / init_diff_x;
            } // if

            if( mode != _SCALE_HOR )
            {
                if( Math.abs( init_diff_y ) < min_dist )
                    _drag_init_y = ( float ) sel_mouse.y;
                else
                    m11 = curr_diff_y / init_diff_y;
            } // if

            if( m00 == 0.0 )
                no_x_scale = true;

            if( m11 == 0.0 )
                no_y_scale = true;
            break;


          case _SHEAR_HOR:

            if( Math.abs( init_diff_y ) < min_dist )
                init_diff_y = init_diff_y < 0 ? -min_dist : min_dist;

            m01 = ( curr_diff_x - init_diff_x ) / init_diff_y;
            break;


          case _SHEAR_VERT:

            if( Math.abs( init_diff_x ) < min_dist )
                init_diff_x = init_diff_x < 0 ? -min_dist : min_dist;

            m10 = ( curr_diff_y - init_diff_y ) / init_diff_x;
            break;


          case _ROTATE:

            double init_hyp = Math.sqrt( init_diff_x * init_diff_x +
                                         init_diff_y * init_diff_y );

            double curr_hyp = Math.sqrt( curr_diff_x * curr_diff_x +
                                         curr_diff_y * curr_diff_y );

            min_dist = transformScreenToWorld( 2 );
            if( curr_hyp < min_dist )
                return;

            if( init_hyp < min_dist )
            {
                _drag_init_x = ( float ) sel_mouse.x;
                _drag_init_y = ( float ) sel_mouse.y;
                return;
            } // if

            double init_sin = init_diff_x / init_hyp;
            double init_cos = init_diff_y / init_hyp;
            double curr_sin = curr_diff_x / curr_hyp;
            double curr_cos = curr_diff_y / curr_hyp;

            double sin_sin = init_sin * curr_sin;
            double sin_cos = init_sin * curr_cos;
            double cos_sin = init_cos * curr_sin;
            double cos_cos = init_cos * curr_cos;

            m00 = cos_cos + sin_sin;
            m01 = cos_sin - sin_cos;
            m10 = -m01;
            m11 = m00;

            break;
        } // switch


        SimpleList points_s = _drag_points_init.points();
        SimpleList points_t = _drag_points.points();

        ListItem s = points_s.first();
        ListItem t = points_t.first();

        float xmin = Float.MAX_VALUE;
        float ymin = Float.MAX_VALUE;
        float xmax = - Float.MAX_VALUE;
        float ymax = - Float.MAX_VALUE;

        while( s != null && t != null )
        {
            Point2 point_s = ( Point2 )points_s.value( s );

            Point2 point_t = ( Point2 )points_t.value( t );

            xmin = Math.min( point_t.x, xmin );
            ymin = Math.min( point_t.y, ymin );
            xmax = Math.max( point_t.x, xmax );
            ymax = Math.max( point_t.y, ymax );

            double x = point_s.x;
            double y = point_s.y;

            x -= fixed_x;
            y -= fixed_y;

            double x_new = m00 * x + m01 * y;
            double y_new = m10 * x + m11 * y;

            x_new += fixed_x;
            y_new += fixed_y;

            if( no_x_scale )
                x_new = point_t.x;
            if( no_y_scale )
                y_new = point_t.y;

            point_t.moveTo( x_new, y_new );

            xmin = Math.min( point_t.x, xmin );
            ymin = Math.min( point_t.y, ymin );
            xmax = Math.max( point_t.x, xmax );
            ymax = Math.max( point_t.y, ymax );


            s = points_s.next( s );
            t = points_t.next( t );
        } // while

        if (fullRefreshMode)
          refresh();
        else
          refresh( xmin, ymin, xmax - xmin, ymax - ymin );
    } // _performCycleTransformation



    /**
     * Ausfuehren des Edit-Zyklus _CyclePointAdd
     */

    //============================================================
    private void _performCyclePointAdd( int mouse_x, int mouse_y )
    //============================================================
    {
        Point2 pt1 = _edit_poly.lastPoint();
        Point2 pt2 = transformScreenToWorld( mouse_x, mouse_y );
        if ( _isRect )
                pt2 = _snapToRect( pt2.x, pt2.y );
        drawXORLine( pt1, pt2 );
    } // _performCyclePointAdd



    /**
     * Beenden der Editier-Zyklen _CYCLE_MOVE und _CYCLE_COPY
     */

    //============================================================
    private boolean _endCycleMove()
    //============================================================
    {
        if( ! _concatEndPoints() )
        {
            boolean is_bounding = removePolygon( _drag_poly, false );
            if( ! addPolygon( is_bounding, _drag_poly, true, false ) )
            {
                _copyPointCoors( _drag_points_init, _drag_points );
                addPolygon( is_bounding, _drag_poly, false, true );
                return false;
            } // if
        } // if

        _edit_cycle = _CYCLE_NONE;
        refresh();
        return true;

    } // _endCycleMove



    /**
     * Beenden des Editier-Zyklus _CYCLE_TRANSFORMATION
     */

    //============================================================
    private void _endCycleTransformation()
    //============================================================
    {
        boolean is_bounding = removePolygon( _drag_poly, false );
        
        if (this.getValidityCheck()){
        	if( ! addPolygon( is_bounding, _drag_poly, true, true ) )
        	{
        		_copyPointCoors( _drag_points_init, _drag_points );
        	} // if
        }
        else {
        	addPolygon( is_bounding, _drag_poly, false, true );
        }
        	
        _edit_cycle = _CYCLE_TRANSFORMATION_INIT;
    } // _endCycleTransformation



    /**
     * Beenden der Editier-Zyklen _CYCLE_POINT_DRAG
     * und _CYCLE_SEGMENT_DRAG
     */

    //============================================================
    private void _endCyclePointSegDrag()
    //============================================================
    {
        if( _concatEndPoints() )
        {
            _edit_cycle = _CYCLE_NONE;
            return;
        } // if

        Polygon2 new_poly = new Polygon2( _drag_poly );
        //new_poly.setUserflag(_drag_poly.getUserflag());
        _copyPointCoors( _drag_points_init, _drag_points );

        // _drag_poly ist jetzt wieder das unveraenderte Original,
        // waehrend new_poly eine Kopie des veraenderten Polygons
        // ist.

        // Das Original wird jetzt entfernt und stattdessen
        // das neue Polygon zugefuegt
        boolean is_bounding = removePolygon( _drag_poly, false );
        addPolygon( is_bounding, new_poly, false, false );

        if (this.getValidityCheck()){
        	if( ! getPolygon2Scene().polygonIsValid( new_poly, _drag_poly ) )
        	{
        		// Das neue Polygon ist nicht gueltig, also wird es
        		// wieder entfernt und das Original wieder zugefuegt

        		removePolygon( new_poly, false );
        		addPolygon( is_bounding, _drag_poly, false, false );

        		_cancelEditCycle( true );
        		return;
        	} // if
        }

        _edit_cycle = _CYCLE_NONE;
        if (!fullRefreshMode) refresh();
    } // _endCyclePointSegDrag



    /**
     * Beenden des Editier-Zyklus _CYCLE_POLYGON_DRAG
     */

    //============================================================
    private void _endCyclePolygonDrag()
    //============================================================
    {
        if( _concatEndPoints() )
        {
            _edit_cycle = _CYCLE_NONE;
            return;
        } // if

        boolean is_bounding = removePolygon( _drag_poly, false );
        
        if( ! addPolygon( is_bounding, _drag_poly, this.getValidityCheck(), true ) )
        {
            _copyPointCoors( _drag_points_init, _drag_points );
            addPolygon( is_bounding, _drag_poly, false, true );
        } // if}

        _edit_cycle = _CYCLE_NONE;
    } // _endCyclePolygonDrag



    /**
     * Beenden des Editier-Zyklus _CYCLE_POINT_ADD
     */

    //============================================================
    private void _endCyclePointAdd( int x, int y )
    //============================================================
    {
        eraseLastXORLine();

        // Berechne Mauskursor-Position in Weltkoordinaten
        _sel_mouse = transformScreenToWorld( x, y );

        // Ermittle die durch den Mauspunkt selektierten Objekte
        // ( _sel_vertex, _sel_edge, _sel_poly )
        _selected = _getSelectedObjects( _sel_mouse );

        if( _sel_vertex != null )
            _sel_mouse = new Point2( _sel_vertex );
        else
        if( _grid_active )
            _sel_mouse = _snapToGrid( _sel_mouse.x, _sel_mouse.y );
        else if( _isRect )
            _sel_mouse = _snapToRect( _sel_mouse.x, _sel_mouse.y );

        if( _selected == VERTEX )
        {
            if( _sel_vertex == _edit_poly.lastPoint() )
            {
                _edit_cycle = _CYCLE_NONE;
            } // if

            else
            if( _sel_vertex == _edit_poly.firstPoint() )
            {
                // Bei rechtwinkligen Polygonen muss ein Verbindungspunkt hinzugefuegt werden...
                if ( _isRect )
                {
                        _addConnector();
                } // if
                if( closeOpenPolygon( _edit_poly ) )
                    _edit_cycle = _CYCLE_NONE;
            } // if

            else
            if(    _sel_poly != _edit_poly
                && _sel_poly.isOpen()
                && (   _sel_vertex == _sel_poly.lastPoint()
                    || _sel_vertex == _sel_poly.firstPoint() ) )
            {
                if( _sel_vertex == _sel_poly.lastPoint() )
                    _sel_poly.points().reverse();

                if( concatOpenPolygons( _edit_poly, _sel_poly ) )
                {
                    if( _sel_poly.length() > 1 )
                        _edit_cycle = _CYCLE_NONE;
                } // if
            } // ifk

            else {
                extendOpenPolygon( _edit_poly, _sel_mouse, _check_validity );
            }
        }
        else {
        	extendOpenPolygon( _edit_poly, _sel_mouse, _check_validity );
        }

        if( _edit_cycle == _CYCLE_POINT_ADD )
            drawXORLine( _edit_poly.lastPoint(), _sel_mouse );
    } // _endCyclePointAdd



    /**
     * Beenden des Editier-Zyklus _CYCLE_PENCIL
     */

    //============================================================
    private void _endCyclePencil( int x, int y )
    //============================================================
    {
        _edit_cycle = _CYCLE_NONE;

        _endCyclePointAdd( x, y );
    } // _endCyclePencil


    /**
    * Verbindungspunkte zweier Punkte im Rechtwinkligen Polygon berechnen...
    **/

    //============================================================
    private void _addConnector()
    //============================================================
    {

        Point2 _connector = new Point2(_edit_poly.firstPoint());
        Point2 pt1 =  _edit_poly.firstPoint();
        Point2 pt2 =  _edit_poly.lastPoint();       

        _connector.y = pt2.y;

        if ( !extendOpenPolygon( _edit_poly, new Point2(_connector) ))
        {
                _connector.x = pt2.x;
                _connector.y = pt1.y;
                if ( !extendOpenPolygon( _edit_poly, new Point2(_connector) )) // Es muessen zwei Konnektoren eingefuegt werden
                {
                        // Fall 1
                        _connector.x = (pt1.x + pt2.x) / 2;
                        _connector.y = pt2.y;
                        if ( extendOpenPolygon( _edit_poly, new Point2(_connector) ))
                        {
                                _connector.y = pt1.y;
                                extendOpenPolygon( _edit_poly, new Point2(_connector));
                        }// if
                        else  // Fall 2
                        {
                                _connector.x = pt2.x;
                                _connector.y = (pt1.y + pt2.y) / 2;
                                extendOpenPolygon( _edit_poly, new Point2(_connector));
                                _connector.x = pt1.x;
                        } //else
                        
                } // if
        } //if
         
    } // _addConnector





	/**
	 * @return Returns the _edit_cycle.
	 */
	public int getEditCycle()
	{
		return _edit_cycle;
	}
	
	
	 /**
	 * Sets _check_validity
	 */
/*	public void setValidityCheck(Boolean check)
	{
		_check_validity = check;
	}
	*/
	/**
	 * @return Returns the _check_validity 
	 */
/*	public boolean getValidityCheck ()
	{
		if (_check_validity) return true;
		else return false;
	} 
	
	*/
	
} // Polygon2SceneEditor
