package anja.swinggui.polygon;

import anja.geom.Polygon2Scene;
import anja.geom.Polygon2;

import anja.swinggui.*;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;


/**
 * Panel zur Ansteuerung des Polygon2SceneEditors
 *
 * @version 0.1 19.08.01
 * @author      Ulrich Handel
 */

//****************************************************************
public class EditorPanel extends AbstractPanel
//****************************************************************
{
    //************************************************************
    // public constants
    //************************************************************

    // action IDs which will be send to the applet
    public static final int CLEAR_SCENE    = 1;
    public static final int OPEN_SCENE     = 2;
    public static final int ERASE_POINTS   = 3;
    public static final int ERASE_SEGMENTS = 4;

    public static final int SET_PENCIL_LENGTH = 5;
    public static final int SET_PENCIL_ANGLE  = 6;

    public static final int SET_PENCIL_MODE   = 7;
    public static final int SET_MOVE_MODE     = 8;

    public static final int SET_DRAG_COPY_SEG  = 9;
    public static final int SET_DRAG_ORIG_SEG = 10;

    public static final int SET_GRID_SIZE   = 11;
    public static final int ACTIVATE_GRID   = 12;
    public static final int DEACTIVATE_GRID = 13;


    //************************************************************
    // private constants
    //************************************************************

    // diverse strings
    static final String _CLEAR_SCENE    = "Clear scene";
    static final String _OPEN_SCENE     = "Open scene";
    static final String _ERASE_POINTS   = "Erase single points";
    static final String _ERASE_SEGMENTS = "Erase open polygons";

    static final String _PENCIL_PARAMETERS = "Pencil parameters";
    static final String _EDGE_LENGTH       = "Edge lenght";
    static final String _ANGLE             = "Angle";

    static final String _END_POINT_DRAGGING = "End point drag";
    static final String _MOVE_POINT         = "Move point";
    static final String _PENCIL_MODE        = "Pencil mode";

    static final String _SEGMENT_DRAGGING = "Segment drag";
    static final String _MOVE_ORIGINAL    = "Original vertices";
    static final String _MOVE_COPY        = "New vertices";

    static final String _GRID          = "Grid";
    static final String _GRID_SIZE     = "Size";
    static final String _ACTIVATE_GRID = "Activate";



    //************************************************************
    // private variables
    //************************************************************

    private ButtonGroup _group_drag  = new ButtonGroup();
    private JRadioButton _radio_move =
        new JRadioButton( _MOVE_POINT );
    private JRadioButton _radio_pencil =
        new JRadioButton( _PENCIL_MODE );


    private ButtonGroup _group_seg_drag = new ButtonGroup();
    private JRadioButton _radio_drag_orig_seg =
        new JRadioButton( _MOVE_ORIGINAL );
    private JRadioButton _radio_drag_copy_seg =
        new JRadioButton( _MOVE_COPY );



    private JButton _button_clear_scene =
        new JButton( _CLEAR_SCENE );
    private JButton _button_open_scene =
        new JButton( _OPEN_SCENE );
    private JButton _button_erase_points =
        new JButton( _ERASE_POINTS );
    private JButton _button_erase_segments =
        new JButton( _ERASE_SEGMENTS );

    private JTextFieldInteger _textfield_length =
        new JTextFieldInteger( 15, 5, 100 );
    private JTextFieldInteger _textfield_angle =
        new JTextFieldInteger( 20, 0, 60 );
    private JLabel _label_edge_length = new JLabel( _EDGE_LENGTH,
                                                    JLabel.LEFT );
    private JLabel _label_angle = new JLabel( _ANGLE,
                                              JLabel.LEFT);



    private JLabel _label_size = new JLabel( _GRID_SIZE,
                                             JLabel.LEFT);

    private JTextFieldDouble _textfield_grid =
        new JTextFieldDouble( 10.0, 0.01, 1000.0 );

    private JCheckBox _box_activate =
        new JCheckBox( _ACTIVATE_GRID );




    //************************************************************
    // constructors
    //************************************************************


    /**
     * Create the edit command panel.
     */

    //============================================================
    public EditorPanel( DisplayPanel display_panel )
    //============================================================
    {
        this( null, display_panel );
    } // EditorPanel


    /**
     * Create the edit command panel.
     */

    //============================================================
    public EditorPanel( ActionListener action_listener,
                        DisplayPanel   display_panel )
    //============================================================
    {
        super( action_listener, display_panel );

        JPanel panel_pencil = new JPanel( new GridBagLayout() );

        GridBagConstraints con = new GridBagConstraints();
        con.gridheight = 1;

        con.anchor     = GridBagConstraints.WEST;
        con.fill       = GridBagConstraints.NONE;

        con.gridwidth = GridBagConstraints.RELATIVE;
        panel_pencil.add( _label_edge_length, con );

        con.gridwidth = GridBagConstraints.REMAINDER;
        panel_pencil.add( _textfield_length, con );

        con.insets = new Insets( 5,0,0,0 );

        con.gridwidth = GridBagConstraints.RELATIVE;
        panel_pencil.add( _label_angle, con );

        con.gridwidth = GridBagConstraints.REMAINDER;
        panel_pencil.add( _textfield_angle, con );

        TitledBorder border = new TitledBorder( _PENCIL_PARAMETERS );

        panel_pencil.setBorder( border );
        add( panel_pencil );




        JPanel panel_drag = new JPanel( new GridLayout( 2, 1 ) );

        _group_drag.add( _radio_move );
        _group_drag.add( _radio_pencil );
        panel_drag.add( _radio_pencil );
        panel_drag.add( _radio_move );
        border = new TitledBorder( _END_POINT_DRAGGING );
        panel_drag.setBorder( border );
        add( panel_drag );



        JPanel panel_seg_drag = new JPanel( new GridLayout( 2, 1 ) );

        _group_seg_drag.add( _radio_drag_orig_seg );
        _group_seg_drag.add( _radio_drag_copy_seg );
        panel_seg_drag.add( _radio_drag_orig_seg );
        panel_seg_drag.add( _radio_drag_copy_seg );
        border = new TitledBorder( _SEGMENT_DRAGGING );
        panel_seg_drag.setBorder( border );
        add( panel_seg_drag );



        JPanel panel_grid = new JPanel( new GridLayout( 2, 1 ) );

        JPanel panel = new JPanel( new GridLayout( 1, 2 ) );
        panel.add( _label_size );
        panel.add( _textfield_grid );

        panel_grid.add( _box_activate );
        panel_grid.add( panel );

        border = new TitledBorder( _GRID );
        panel_grid.setBorder( border );
        add( panel_grid );



        panel = new JPanel( new GridLayout( 4, 1 ) );
        panel.add( _button_clear_scene );
        panel.add( _button_open_scene );
        panel.add( _button_erase_points );
        panel.add( _button_erase_segments );
        add( panel );


        _button_clear_scene.addActionListener( this );
        _button_open_scene.addActionListener( this );
        _button_erase_points.addActionListener( this );
        _button_erase_segments.addActionListener( this );

        _radio_move.addActionListener( this );
        _radio_pencil.addActionListener( this );

        _radio_drag_orig_seg.addActionListener( this );
        _radio_drag_copy_seg.addActionListener( this );

        _textfield_length.addActionListener( this );
        _textfield_angle.addActionListener( this );


        _textfield_grid.addActionListener( this );
        _box_activate.addItemListener( this );

    } // EditorPanel



    //************************************************************
    // public interface methods
    //************************************************************


    /**
     * @see ActionListener
     */

    //============================================================
    public void actionPerformed( ActionEvent e )
    //============================================================
    {
        if( e.getSource() == getDisplayPanel() )
        {
            refresh();
            return;
        } // if

        if( e.getSource() == this )
        {
            DisplayPanelScene display_panel_scene = getScene();
            if( ! ( display_panel_scene instanceof Polygon2SceneEditor ) )
                return;

            Polygon2SceneEditor editor =
                ( Polygon2SceneEditor ) display_panel_scene;

            switch( e.getID() )
            {
              case CLEAR_SCENE:
                editor.erasePolygons( Polygon2SceneWorker.ALL_POLYGONS );
                break;

              case OPEN_SCENE:
                editor.erasePolygons( Polygon2SceneWorker.BOUNDING_POLYGON );
                break;

              case ERASE_POINTS:
                editor.erasePolygons( Polygon2SceneWorker.POINT_POLYGONS );
                break;

              case ERASE_SEGMENTS:
                editor.erasePolygons( Polygon2SceneWorker.OPEN_POLYGONS );

              case SET_PENCIL_ANGLE:
                editor.setPencilAngle( getPencilAngle() );
                break;

              case SET_PENCIL_LENGTH:
                editor.setPencilLength( getPencilLength() );
                break;

              case SET_MOVE_MODE:
                editor.setMoveMode();
                break;

              case SET_PENCIL_MODE:
                editor.setPencilMode();
                break;


              case SET_DRAG_ORIG_SEG:
                editor.setSegmentDragMode( false );
                break;

              case SET_DRAG_COPY_SEG:
                editor.setSegmentDragMode( true );
                break;


              case SET_GRID_SIZE:
                editor.setGridSize( getGridSize() );
                break;

              case ACTIVATE_GRID:
                editor.setGridActive( true );
                break;

              case DEACTIVATE_GRID:
                editor.setGridActive( false );
                break;
            } // switch

            return;
        } // if



        int action = NO_ACTION;

        if( e.getSource() == _button_clear_scene )
            action = CLEAR_SCENE;
        if( e.getSource() == _button_open_scene )
            action = OPEN_SCENE;
        if( e.getSource() == _button_erase_points )
            action = ERASE_POINTS;
        if( e.getSource() == _button_erase_segments )
            action = ERASE_SEGMENTS;

        if( e.getSource() == _textfield_length )
            action = SET_PENCIL_LENGTH;
        if( e.getSource() == _textfield_angle )
            action = SET_PENCIL_ANGLE;

        if( e.getSource() == _radio_move )
            action = SET_MOVE_MODE;
        if( e.getSource() == _radio_pencil )
            action = SET_PENCIL_MODE;

        if( e.getSource() == _radio_drag_copy_seg )
            action = SET_DRAG_COPY_SEG;
        if( e.getSource() == _radio_drag_orig_seg )
            action = SET_DRAG_ORIG_SEG;

        if( e.getSource() == _textfield_grid )
            action = SET_GRID_SIZE;


        fireActionEvent( action );
    } // actionPerformed




    /**
     * @see ItemListener
     */

    //============================================================
    public void itemStateChanged( ItemEvent e )
    //============================================================
    {
        int action = NO_ACTION;

        if( e.getSource() == _box_activate )
        {
            if( _box_activate.isSelected() )
                action = ACTIVATE_GRID;
            else
                action = DEACTIVATE_GRID;
        } // if

        fireActionEvent( action );
    } // itemStateChanged






    //************************************************************
    // public methods
    //************************************************************


    /**
     * Refresh the panel.
     */

    //============================================================
    public void refresh()
    //============================================================
    {
        DisplayPanelScene display_panel_scene = getScene();
        if( ! ( display_panel_scene instanceof Polygon2SceneEditor ) )
            return;

        Polygon2SceneEditor editor =
            ( Polygon2SceneEditor ) display_panel_scene;


        Polygon2Scene scene = editor.getPolygon2Scene();
        Polygon2[] polygons = scene.getInteriorPolygons();
        Polygon2 bounding_poly = scene.getBoundingPolygon();

        setEnabled( _button_clear_scene,
                    polygons.length > 0 || bounding_poly != null );

        setEnabled( _button_open_scene,
                    bounding_poly != null );


        boolean points = false;
        boolean segments = false;

        for( int i = 0; i < polygons.length; i++ )
        {
            Polygon2 poly = polygons[ i ];

            if( poly.length() == 1 )
                points = true;

            if( poly.isOpen() && poly.length() > 1 )
                segments = true;
        } // for


        setEnabled( _button_erase_points, points );
        setEnabled( _button_erase_segments, segments );


        setValue( _textfield_angle,
                  (int)editor.getPencilAngle() );

        setValue( _textfield_length,
                  (int)editor.getPencilLength() );

        boolean flag;

        flag = editor.getPencilMode();
        setSelected( _radio_move, ! flag );
        setSelected( _radio_pencil, flag );

        flag = editor.getSegmentDragMode();
        setSelected( _radio_drag_orig_seg, ! flag );
        setSelected( _radio_drag_copy_seg, flag );


        setValue( _textfield_grid,
                  editor.getGridSize() );

        flag = editor.getGridActive();
        setSelected( _box_activate, flag );
        setEnabled( _textfield_grid , flag );
    } // refresh



    /**
     * Ermitteln des eingestellten Winkels fuer Pencil-Mode.
     */

    //============================================================
    public int getPencilAngle()
    //============================================================
    {
        return _textfield_angle.getValue();
    } // getPencilAngle



    /**
     * Ermitteln der eingestellten Kanten-Laenge fuer Pencil-Mode.
     */

    //============================================================
    public int getPencilLength()
    //============================================================
    {
        return _textfield_length.getValue();
    } // getPencilLength



    /**
     * Ermitteln des eingestellten Grid Size
     */

    //============================================================
    public double getGridSize()
    //============================================================
    {
        return _textfield_grid.getValue();
    } // getGridSize

} // EditorPanel
