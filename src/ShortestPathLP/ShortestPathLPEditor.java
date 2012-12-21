package ShortestPathLP;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import anja.geom.Polygon2Scene;
import anja.geom.Circle2;
import anja.geom.Point2;

import anja.swinggui.polygon.ExtendablePolygonEditor;

import anja.util.GraphicsContext;

/**
 * This class inherits the attributes of the Polygon2SceneEditor and extends
 * these with some functions.
 * 
 * @author Anja Haupts
 * @version 1.4 02.12.07
 */

public class ShortestPathLPEditor extends ExtendablePolygonEditor
{
        // *********************************************************************
        // Protected constants
        // *********************************************************************

        protected Color color_SourcePoint = new Color(27, 144, 60);

        protected Color color_TargetPoint = new Color(255, 0, 0);

        protected Color color_Poly = new Color(255, 153, 153);

        // *********************************************************************
        // Private constants
        // *********************************************************************

        private final float CIRCLE_SIZE = 5.0f;

        private Color color_newEdgeColor = new Color(0, 0, 0);

        private Color color_newPointColor = new Color(0, 0, 0);

        private Color color_newPolyColor = Color.lightGray;

        // *********************************************************************
        // Protected variables
        // *********************************************************************

        /** the drawing area */
        protected Graphics2D _g2d;

        /** Show the Dual Graph. */
        protected boolean showDualGraph = false;

        /** Show the triangulation of the polygon. */
        protected boolean showTria = false;

        /** Show the shortest path directly. */
        protected boolean showCompletePath = false;

        /** Show the shortest path evoltuion step by step. */
        protected boolean showSteps = true;

        /** Show the shortest path evoltuion automatically evolving. */
        protected boolean showRun = false;

        // *********************************************************************
        // Private variables
        // *********************************************************************

        private ShortestPathLPExtension _extension;

        private JMenuItem _menuSourcePoint = new JMenuItem("Set source point");

        private JMenuItem _menuTargetPoint = new JMenuItem("Set target point");

        private GraphicsContext _gcSPoint;

        private GraphicsContext _gcTPoint;

        // start (source)
        private boolean _showSourcePoint = false;

        private boolean _sourcePointIsDragged = false;

        private Point2 _sourcePoint = new Point2(0.0, 0.0);

        // destination (target)
        private boolean _showTargetPoint = false;

        private boolean _targetPointIsDragged = false;

        private Point2 _targetPoint = new Point2(1.0, 1.0);

        // save the position of the cursor
        private Point2 _actMousePos;

        // Has a full run been already presented?
        private boolean _showedRun = false;


        // *********************************************************************
        // Constructor
        // *********************************************************************

        /**
         * The base colours for the drawing of the polygons are defined in the
         * constructor. In addition the 'FullRefreshMode' is set and the
         * MenuePoints which allow the setting of the source and target points
         * are added.
         * 
         * @param extension
         *                the ShortestPathLPExtension that provides the
         *                algorithm by Lee and Preparata
         */
        public ShortestPathLPEditor(ShortestPathLPExtension extension)
        {
                super(extension);

                this._extension = extension;

                setPointColor(color_newPointColor);
                setEdgeColor(color_newEdgeColor);
                setPolygonColor(color_newPolyColor);

                _menuSourcePoint.addActionListener(this);
                _menuTargetPoint.addActionListener(this);

                // refresh everything -> this avoids, that only parts are
                // refreshed.
                setFullRefreshMode(true);

        }// constructor


        // *********************************************************************
        // Protected instance methods
        // *********************************************************************

        /**
         * Reflects the creation of the closed, simple polygon and draws the
         * source and target point if neccessary. It starts the paint-method of
         * the _extension, its method to draw the triangulation and the dual
         * graph and, if called by the user, the different kinds of shortest
         * path evolution.
         * 
         * @param g2d
         *                the drawing area
         * @param g
         *                the abstract base class for all graphics contexts that
         *                allows an application to draw
         * @see #_extension
         */
        protected void paint(Graphics2D g2d, Graphics g)
        {
                _g2d = g2d;
                super.paint(_g2d, g); // drawing the polygon of the editor

                // this try-catch-statement is used to avoid
                // NullPointerExceptions, when there is no polygon drawn yet
                try
                {
                        Polygon2Scene scene = getPolygon2Scene();

                        // all following operations can only be executed, if the
                        // polygon is closed
                        if (!scene.getPolygon(0).isSimple())
                                return;
                        
                        // hand over to the actual control flow of the algorithm
                        _extension.paint(scene, _g2d);

                        // if the start point is set
                        if (_showSourcePoint)
                        {                                
                                _gcSPoint = new GraphicsContext();
                                _gcSPoint.setForegroundColor(color_SourcePoint);
                                _gcSPoint.setFillColor(color_SourcePoint);
                                _gcSPoint.setFillStyle(1);
                                Circle2 circle = new Circle2(_sourcePoint, CIRCLE_SIZE);
                                circle.draw(g2d, _gcSPoint);
                                
                                // add start to the calculation
                                _extension.addStart(_sourcePoint, _g2d);

                        }// if(_showSourcePoint)

                        // if the target point is set
                        if (_showTargetPoint)
                        {                                
                                _gcTPoint = new GraphicsContext();
                                _gcTPoint.setForegroundColor(color_TargetPoint);
                                _gcTPoint.setFillColor(color_TargetPoint);
                                _gcTPoint.setFillStyle(1);

                                Circle2 circle = new Circle2(_targetPoint, CIRCLE_SIZE);
                                circle.draw(_g2d, _gcTPoint);
                                
                                // add target to the calculation
                                _extension.addTarget(_targetPoint, g2d);

                        }// if(_showTargetPoint)

                        // if the user wants to see the triangulation
                        if (showTria)
                                _extension.drawTria(g2d);

                        // if the user wants to see the dual graph
                        if (showDualGraph)
                                _extension.drawDG(g2d);

                        // if the user wants to see the complete shortest path
                        // immediately, step by step or automaticly evolving
                        if (_showSourcePoint && _showTargetPoint)
                        {
                                if (showCompletePath)
                                {
                                        _extension.calculateFullPath(g2d);
                                        _showedRun = false;
                                }// if

                                if (showRun)
                                {
                                        if (_showedRun)
                                                return;

                                        _extension.clearPolygon(g2d);
                                        refreshView();
                                        _extension.calculateRun(g2d);
                                        _showedRun = true;
                                }// if

                                if (showSteps)
                                {
                                        _extension.calculateSteps(g2d);
                                        _showedRun = false;
                                }// if

                        }// if(_showSourcePoint && _showTargetPoint)

                } catch (NullPointerException npe)
                {
                }// catch

        }// paint(Graphics2D g2d, Graphics g)


        // *********************************************************************

        /** refreshes after an event */
        protected void recalculate()
        {                
                updateDisplayPanel();

                if (showRun && _showedRun)
                        return;

                // if the polygon is closed, recalculate the whole scene
                Polygon2Scene scene = getPolygon2Scene();
                try
                {
                        if (!scene.getPolygon(0).isSimple())
                                return;

                        _extension.clearPolygon(_g2d);

                        if (showSteps)
                        {
                                _extension.setNext(true);
                                _extension.setPrev(false);
                        }// if(showSteps)

                        _extension.paint(scene, _g2d);

                        refresh();

                } catch (NullPointerException npe)
                {
                }// catch

                updateDisplayPanel();

        }// public void recalculate()


        // *********************************************************************

        /** refreshes during the automated funnel evolution */
        protected void refreshView()
        {
                refreshTriaDG();

                // repaint the maybe overpainted items
                Circle2 circle = new Circle2(_sourcePoint, CIRCLE_SIZE);
                circle.draw(_g2d, _gcSPoint);

                circle = new Circle2(_targetPoint, CIRCLE_SIZE);
                circle.draw(_g2d, _gcTPoint);

        }// public void refreshView()


        // *********************************************************************
        // Private instance methods
        // *********************************************************************

        /**
         * Refreshes the view of the triangulation and the dual graph, if
         * necessary.
         */
        private void refreshTriaDG()
        {
                // if the user wants to see the triangulation
                if (showTria)
                        _extension.drawTria(_g2d);

                // if the user wants to see the dual graph
                if (showDualGraph)
                        _extension.drawDG(_g2d);

        }// refreshTriaDG


        // *********************************************************************
        // Public overriding methods
        // *********************************************************************

        /**
         * Catch the mouse events for start and target (if they are pressed).
         * 
         * @param e
         *                the MouseEvent (pressed) that needs to be handled
         */
        public void mousePressed(MouseEvent e)
        {
                _actMousePos = transformScreenToWorld(e.getX(), e.getY());

                // Is start pressed?
                if (_showSourcePoint
                                && _sourcePoint.distance(_actMousePos) <= CIRCLE_SIZE)
                {
                        _sourcePointIsDragged = true;
                        recalculate();
                        return;
                }// if

                // Is target pressed?
                if (_showTargetPoint
                                && _targetPoint.distance(_actMousePos) <= CIRCLE_SIZE)
                {
                        _targetPointIsDragged = true;
                        recalculate();
                        return;
                }// if

                super.mousePressed(e);

                recalculate();

        }// public void mousePressed(MouseEvent e)


        // *********************************************************************

        /**
         * Catch the mouse events for start and target(if they are released).
         * 
         * @param e
         *                the MouseEvent (release) that needs to be handled
         */
        public void mouseReleased(MouseEvent e)
        {
                _actMousePos = transformScreenToWorld(e.getX(), e.getY());

                // Is start dropped?
                if (_showSourcePoint && _sourcePointIsDragged)
                {
                        _sourcePointIsDragged = false;
                        _showedRun = false;
                        recalculate();
                        return;
                }// if

                // Is target dropped?
                if (_showTargetPoint && _targetPointIsDragged)
                {
                        _targetPointIsDragged = false;
                        _showedRun = false;
                        recalculate();
                        return;
                }// if

                super.mouseReleased(e);

                recalculate();

        }// public void mouseReleased(MouseEvent e)


        // *********************************************************************

        /**
         * Catch the mouse events for dragging.
         * 
         * @param e
         *                the MouseEvent (drag) that needs to be handled
         */
        public void mouseDragged(MouseEvent e)
        {
                _actMousePos = transformScreenToWorld(e.getX(), e.getY());

                // Is start moved?
                if (_showSourcePoint && _sourcePointIsDragged)
                {
                        _sourcePoint.moveTo(_actMousePos);
                        recalculate();
                        return;
                }// if

                // Is target moved?
                if (_showTargetPoint && _targetPointIsDragged)
                {
                        _targetPoint.moveTo(_actMousePos);
                        recalculate();
                        return;
                }// if

                super.mouseDragged(e);

                // if the polygon has been edited, recalculate the whole scene
                Polygon2Scene scene = getPolygon2Scene();
                try
                {
                        if (!scene.getPolygon(0).isSimple())
                                return;

                        _showedRun = false;
                        recalculate();

                } catch (NullPointerException npe)
                {
                }// catch

        }// public void mouseDragged(MouseEvent e)


        // *********************************************************************

        /**
         * The PopupMenu to set start and target.
         * 
         * @param menu
         *                the JPopupMenu
         */
        public void buildPopupMenu(JPopupMenu menu)
        {
                menu.add(_menuSourcePoint);
                menu.add(_menuTargetPoint);
                menu.addSeparator();

                super.buildPopupMenu(menu);

                // remove the random polygon editor and
                // the set-to-boundig-polygon item from the menu
                menu.remove(5);
            //    menu.remove(5);

        }// public void buildPopupMenu(JPopupMenu menu)


        // *********************************************************************

        /**
         * If start and target are set, they are placed on the actual mouse
         * position.
         * 
         * @param e
         *                the ActionEvent
         */
        public void actionPerformed(ActionEvent e)
        {
                // set start
                if (e.getSource() == _menuSourcePoint)
                {
                        _showSourcePoint = true;
                        _sourcePoint.moveTo(_actMousePos);
                        _showedRun = false;
                        recalculate();
                        return;
                }// if

                // set target
                if (e.getSource() == _menuTargetPoint)
                {
                        _showTargetPoint = true;
                        _targetPoint.moveTo(_actMousePos);
                        _showedRun = false;
                        recalculate();
                        return;
                }// if

                super.actionPerformed(e);

        }// public void actionPerformed(ActionEvent e)

}// public class ShortestPathLPEditor
