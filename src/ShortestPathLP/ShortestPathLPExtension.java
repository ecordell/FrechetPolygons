package ShortestPathLP;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import anja.geom.Point2;
import anja.geom.Point2List;
import anja.geom.Polygon2;
import anja.geom.ConvexPolygon2;
import anja.geom.Segment2;
import anja.geom.Polygon2Scene;
import anja.geom.triangulation.*;

import anja.swinggui.BufferedCanvas;
import anja.swinggui.DisplayPanel;
import anja.swinggui.polygon.Extendable;
import anja.swinggui.polygon.ExtendablePolygonEditor;

import anja.util.GraphicsContext;

/**
 * This class implements the Extendable-Interface and extends the editor in a
 * way that it can plot the SeidelTriangulation, draw the dual graph and the
 * diagonals, that need to be crossed by the shortest path (found by
 * DepthFirstSearch), calculate the shortest path using <i>funnels</i> in three
 * different ways and finally draw that shortest path. <br>
 * The plotting of the triangulation is done by drawing the single triangles,
 * that are stored in an array, separately. Then the diagonals, that were marked
 * by the DFS, are selected and added to a funnel evolution that finally
 * computes the shortest path.
 * 
 * @author Anja Haupts
 * @version 1.5 02.12.07
 */

public class ShortestPathLPExtension implements Extendable
{
        // *********************************************************************
        // Private Constants
        // *********************************************************************

        /** the general dash pattern for dashed lines */
        private static final float _LINE_DASH[] = { 1.0f, 2.0f };

        /** the colour for polygon */
        private static final Color _EDGE_COLOR = Color.black;

        /** the line width for outline polygon */
        private static final float _LINE_WIDTH = 0.5f;

        /** the fill to clear the canvas */
        private static final Color _FILL_COLOR = Color.white;

        /** the fill style to clear the canvas */
        private static final int _FILL_STYLE = 1;

        /** the colours for the SeidelTriangulation */
        private static final Color _TRIA_COLOR = Color.yellow;

        /** the line width of the diagonals */
        private static final float _TRIA_LINE_WIDTH = 1f;

        /** the colour for the edges of the dual graph */
        private static final Color _DUAL_EDGE_COLOR = Color.blue.darker();

        /** the colour for the vertexes of the dual graph */
        private static final Color _VERTEX_COLOR = Color.blue;

        /** the colour of the diagonals the shortest path has to cross */
        private static final Color _DFS_DIAGONALS_COLOR = Color.yellow;

        /** the line width of the needed diagonals */
        private static final float _DFS_LINE_WIDTH = 1.5f;

        /** the dash pattern for the diagonals that need to be crossed */
        private static final float _DFS_LINE_DASH[] = { 16.0f, 16.0f };

        /** the colour of the shortest path */
        private static final Color _SP_COLOR = Color.green;

        /** the line width of the shortest path */
        private static final float _SP_LINE_WIDTH = 3.0f;

        /** the fill colour of the funnels */
        private static final Color _FUNNEL_FILL_COLOR = Color.red;

        /** the fill style of the funnels */
        private static final int _FUNNEL_FILL_STYLE = 1;

        /** the outline colour of the funnels */
        private static final Color _FUNNEL_OUTLINE_COLOR = Color.white;

        /** the line width of the funnel */
        private static final float _FUNNEL_LINE_WIDTH = 2.5f;

        /** the line width of the possible tangents */
        private static final float _TANGENTS_LINE_WIDTH = 4f;

        /** the colour of the possible tangents */
        private static final Color _TANGENTS_COLOR = new Color(0, 110, 255);

        /** the fill colour of the previous funnel */
        private static final Color _PREV_FUNNEL_FILL_COLOR = new Color(180, 0, 5);

        /** the fill style of the previous funnel */
        private static final int _PREV_FUNNEL_FILL_STYLE = 1;

        /** the outline colour of the previous funnel */
        private static final Color _PREV_FUNNEL_OUTLINE_COLOR = Color.red;

        /** the line width of the previous funnel */
        private static final float _PREV_FUNNEL_LINE_WIDTH = 2.0f;

        /** to clear the canvas from the old funnel */
        private static final Color _CLEAR_FILL_COLOR = Color.lightGray;

        /** to clear the canvas from the old funnel */
        private static final int _CLEAR_FILL_STYLE = 1;

        /** the outline colour of the cleared funnel */
        private static final Color _CLEAR_FUNNEL_OUTLINE_COLOR = Color.lightGray;

        /** the line width of the outline of the deleted funnel */
        private static final float _CLEAR_LINE_WIDTH = 2.5f;

        /** the amount of time in milliseconds between two automated steps */
        private static int timer = 700;

        // *********************************************************************
        // Public variables
        // *********************************************************************

        /** the display panel of the applet */
        public DisplayPanel display;

        // *********************************************************************
        // Protected variables
        // *********************************************************************

        /** the extended editor */
        protected ShortestPathLPEditor _editor;

        /** the lower panel, where the "Next" and "Previous" buttons are placed */
        protected JPanel panel;

        // *********************************************************************
        // Private variables
        // *********************************************************************

        /** click this to go to the next funnel in the step by step evolution */
        private JButton _next;

        /** click this to go to the previous funnel in the step by step evolution */
        private JButton _previous;

        /** the general view of the polygon outline */
        private GraphicsContext _gc;

        /** view of the SeidelTriangulation */
        private GraphicsContext _gcTriang;

        /** view of the dual graph */
        private GraphicsContext _gcDG;

        /** view of the diagonals of the DFS */
        private GraphicsContext _gcDFS;

        /** view of the funnels */
        private GraphicsContext _gcFunnel;

        /** view of the possible tangents */
        private GraphicsContext _gcTangents;

        /** view of the previous funnels */
        private GraphicsContext _gcPrevFunnel;

        /** view of the ShortestPath */
        private GraphicsContext _gcSP;

        /** the Graphics Context to clear the canvas */
        private GraphicsContext _gcClear;

        /** the outlining edges of the polygon */
        private Segment2[] _polyArray;

        /**
         * The SeidelTriangulation of the Polygon (always without the start and
         * target points)
         */
        private SeidelTriangulation _triangulation;

        /**
         * the array of casted ConvexPolygons containing the triangles of the
         * Triangulation
         */
        private ConvexPolygon2[] _triangPolys;

        /** the dual graph */
        private DualGraph _dg;

        /** The array containing the diagonals of the triangulation. */
        private Segment2[] _triangDiagonals;

        /**
         * The array containing the diagonals of the DFS in the DFS order that
         * are needed for the calculation of the shortest path.
         */
        private Segment2[] _diagonalsDFS;

        /** the amount if diagonals to cross */
        private int _amountOfDiagonals;

        /** the number of diagonals that already has been crossed */
        private int _crossedDiagonals;

        /** the array containing all edges of the triangulation */
        private Segment2[] _allEdges;

        /** start point */
        private Point2 _start;

        /** target point */
        private Point2 _target;

        /** the funnel needed for the step by step evolution */
        private Funnel _stepFunnel;

        /**
         * saves how many steps have already been made before - used for making
         * backspaces
         */
        private int _stepsIndex = 0;

        /** the funnel needed for the automatic evolution */
        private Funnel _runFunnel;

        /** the final shortest path */
        private Segment2[] _shortestPath;

        /**
         * true, if a previous funnel at the step by step funnel evolution
         * should be visible
         */
        private boolean _showPrev = false;

        /**
         * true, if the next funnel at the step by step funnel evolution should
         * be visible
         */
        private boolean _showNext = true;


        // *********************************************************************
        // Constructor
        // *********************************************************************

        /**
         * The constructor defines the basic appearances of the elements that
         * might be visualized.
         */
        public ShortestPathLPExtension()
        {
                _gc = new GraphicsContext();
                _gc.setForegroundColor(_EDGE_COLOR);
                _gc.setLineWidth(_LINE_WIDTH);
                _gc.setFillColor(_FILL_COLOR);
                _gc.setFillStyle(_FILL_STYLE);

                _gcTriang = new GraphicsContext();
                _gcTriang.setForegroundColor(_TRIA_COLOR);
                _gcTriang.setLineWidth(_TRIA_LINE_WIDTH);
                _gcTriang.setDash(_LINE_DASH);

                _gcDG = new GraphicsContext();
                _gcDG.setForegroundColor(_DUAL_EDGE_COLOR);
                _gcDG.setDash(_LINE_DASH);

                _gcDFS = new GraphicsContext();
                _gcDFS.setForegroundColor(_DFS_DIAGONALS_COLOR);
                _gcDFS.setLineWidth(_DFS_LINE_WIDTH);
                _gcDFS.setDash(_DFS_LINE_DASH);

                _gcFunnel = new GraphicsContext();
                _gcFunnel.setForegroundColor(_FUNNEL_FILL_COLOR);
                _gcFunnel.setFillColor(_FUNNEL_FILL_COLOR);
                _gcFunnel.setFillStyle(_FUNNEL_FILL_STYLE);
                _gcFunnel.setOutlineColor(_FUNNEL_OUTLINE_COLOR);
                _gcFunnel.setLineWidth(_FUNNEL_LINE_WIDTH);

                _gcTangents = new GraphicsContext();
                _gcTangents.setForegroundColor(_TANGENTS_COLOR);
                _gcTangents.setLineWidth(_TANGENTS_LINE_WIDTH);

                _gcPrevFunnel = new GraphicsContext();
                _gcPrevFunnel.setForegroundColor(_PREV_FUNNEL_FILL_COLOR);
                _gcPrevFunnel.setFillColor(_PREV_FUNNEL_FILL_COLOR);
                _gcPrevFunnel.setFillStyle(_PREV_FUNNEL_FILL_STYLE);
                _gcPrevFunnel.setOutlineColor(_PREV_FUNNEL_OUTLINE_COLOR);
                _gcPrevFunnel.setLineWidth(_PREV_FUNNEL_LINE_WIDTH);

                _gcSP = new GraphicsContext();
                _gcSP.setForegroundColor(_SP_COLOR);
                _gcSP.setLineWidth(_SP_LINE_WIDTH);

                _gcClear = new GraphicsContext();
                _gcClear.setFillColor(_CLEAR_FILL_COLOR);
                _gcClear.setFillStyle(_CLEAR_FILL_STYLE);
                _gcClear.setOutlineColor(_CLEAR_FUNNEL_OUTLINE_COLOR);
                _gcClear.setLineWidth(_CLEAR_LINE_WIDTH);

                _start = null;
                _target = null;
                _polyArray = null;
                _stepFunnel = null;
                _runFunnel = null;

                _dg = null;
                _shortestPath = null;

        } // ShortestPathLPExtension constructor


        // *********************************************************************
        // Protected instance methods
        // *********************************************************************

        /**
         * Add the start point to the scene and calculate the dual graph.
         * 
         * @param start
         *                the start point
         * @param g2d
         *                the drawing area
         */
        protected void addStart(Point2 start, Graphics2D g2d)
        {
                _start = start;

                _refresh(g2d);

        }// protected void addStart(Point2 start, Graphics2D g2d)


        // *********************************************************************

        /**
         * Add the target point to the scene and calculate the dual graph.
         * 
         * @param target
         *                the start point
         * @param g2d
         *                the drawing area
         */
        protected void addTarget(Point2 target, Graphics2D g2d)
        {
                _target = target;

                _refresh(g2d);

        }// protected void addTarget(Point2 target, Graphics2D g2d)


        // *********************************************************************

        /**
         * draws the diagonals of the Seidel-Triangulation
         * 
         * @param g2d
         *                the drawing area
         */
        protected void drawTria(Graphics2D g2d)
        {
                for (int i = 0; i < _triangDiagonals.length; i++)
                {
                        _triangDiagonals[i].draw(g2d, _gcTriang);

                }// for (int i = 0; i < _triangDiagonals.length; i++)

        }// protected void drawTria(Graphics2D g2d)


        // *********************************************************************

        /**
         * draws the DualGraph
         * 
         * @param g2d
         *                the drawing area
         */
        protected void drawDG(Graphics2D g2d)
        {
                for (int i = 0; i < _dg.getDualEdges_size(); i++)
                {
                        _dg.getDualEdge(i).draw(g2d, _gcDG);

                }// for (int i = 0; i<_dualEdges.size(); i++)

                _gcDG.setForegroundColor(_VERTEX_COLOR);

                for (int i = 0; i < _triangPolys.length; i++)
                {
                        _dg.getDualVertex(i).draw(g2d, _gcDG, 1, 1.5, true);

                }// for (int i = 0; i < _triangCount; i++)

                _gcDG.setForegroundColor(_DUAL_EDGE_COLOR);

        }// protected void drawDG(Graphics2D g2d)


        // *********************************************************************

        /**
         * Adds a diagonal to the funnel evolution.
         * 
         * @see #_stepFunnel
         * @see #_crossedDiagonals
         * @see #_diagonalsDFS
         */
        protected void addDiagonalByStep()
        {
                if (_crossedDiagonals >= _amountOfDiagonals)
                        return;

                _stepFunnel.add(_diagonalsDFS[_crossedDiagonals]);
                _crossedDiagonals++;
                return;

        }// protected void addDiagonalByStep()


        // *********************************************************************

        /**
         * Finally adds the target point to the step by step evolution.
         * 
         * @see #_stepFunnel
         * @see #_crossedDiagonals
         * @see #_diagonalsDFS
         * @see #_shortestPath
         */
        protected void stepToTarget()
        {
                _stepFunnel.addTarget(_target);
                _shortestPath = _stepFunnel.getShortestPath();
                _crossedDiagonals++;
                _showNext = false;

        }// protected void stepToTarget()


        // *********************************************************************

        /**
         * Draws the previous funnel for the step by step evolution.
         * 
         * @param g2d
         *                the drawing area
         */
        protected void drawOldFunnel(Graphics2D g2d)
        {
                _showPrev = false;
                Polygon2 temp_polygon = new Polygon2(_stepFunnel.getActiveFunnel());
                temp_polygon.draw(g2d, _gcPrevFunnel);
                drawSP(g2d);

        }// protected void drawOldFunnel()


        // ********************************************************************

        /**
         * Finally draws the shortest path.
         * 
         * @param g2d
         *                the drawing area
         */
        protected void drawSP(Graphics2D g2d)
        {
                if (_shortestPath == null)
                        return;

                for (int i = 0; i < _shortestPath.length; i++)
                {
                        _shortestPath[i].draw(g2d, _gcSP);
                }// for

        }// protected void drawSP(Graphics2D g2d)


        // *********************************************************************

        /**
         * calculates the shortest path at once
         * 
         * @param g2d
         *                the drawing area
         * @see appsSwingGui.ShortestPathLP.Funnel
         */
        protected void calculateFullPath(Graphics2D g2d)
        {
                // if _start and _target are both inside the polygon,
                // calculate the funnels
                if (!_pointInPolygon(_start) || !_pointInPolygon(_target))
                        return;

                if (_diagonalsDFS == null)
                {
                        Segment2 path = new Segment2(_start, _target);
                        _shortestPath = new Segment2[1];
                        _shortestPath[0] = path;
                        drawSP(g2d);
                        return;

                }// if(_diagonalsDFS == null)

                Funnel act_funnel = new Funnel(_start, _diagonalsDFS.length + 2);

                for (int i = 0; i < _diagonalsDFS.length; i++)
                {
                        act_funnel.add(_diagonalsDFS[i]);
                }// for

                act_funnel.addTarget(_target);
                _shortestPath = act_funnel.getShortestPath();
                drawSP(g2d);
                return;

        }// protected void calculateFullPath(Graphics2D g2d)
        
        
        // *********************************************************************

        /**
         * calculates the shortest path iteratively evolving
         * 
         * @param g2d
         *                the drawing area
         * @see appsSwingGui.ShortestPathLP.Funnel
         * @see #_runFunnel
         */
        protected void calculateRun(Graphics2D g2d)
        {                
                // if _start and _target are both inside the polygon,
                // calculate the funnels
                if (!_pointInPolygon(_start) || !_pointInPolygon(_target))
                        return;

                if (_diagonalsDFS == null)
                {
                        Segment2 path = new Segment2(_start, _target);
                        _shortestPath = new Segment2[1];
                        _shortestPath[0] = path;
                        drawSP(g2d);
                        return;

                }// if(_diagonalsDFS == null)

                _runFunnel = new Funnel(_start, _diagonalsDFS.length + 2);

                // run the automatic funnel exploration
                for (int i = 0; i < _diagonalsDFS.length; i++)
                {
                        _runFunnel.add(_diagonalsDFS[i]);
                        Point2List temp_funnel = _runFunnel.getActiveFunnel();
                        _drawFunnel(g2d, temp_funnel, _runFunnel.oldFunnel);

                        // this is needed to create a
                        // short pause between the steps
                        BufferedCanvas b = display.getCanvas();
                        Graphics g = b.getGraphics();
                        b.paint(g);
                        try
                        {
                                Thread.sleep(timer);

                        } catch (InterruptedException ie)
                        {
                                return;
                        }// catch
                }// for i

                _runFunnel.addTarget(_target);
                _shortestPath = _runFunnel.getShortestPath();
                drawSP(g2d);

        }// protected void calculateRun(Graphics2D g2d)


        // *********************************************************************

        /**
         * calculates the shortest step by step
         * 
         * @param g2d
         *                the drawing area
         * @see appsSwingGui.ShortestPathLP.Funnel
         */
        protected void calculateSteps(Graphics2D g2d)
        {
                // if _start and _target are both inside the polygon,
                // calculate the funnels
                if (!_pointInPolygon(_start) || !_pointInPolygon(_target))
                        return;

                if (_diagonalsDFS == null)
                {
                        Segment2 path = new Segment2(_start, _target);
                        _shortestPath = new Segment2[1];
                        _shortestPath[0] = path;
                        drawSP(g2d);
                        return;

                }// if(_diagonalsDFS == null)

                if (_stepFunnel != null)
                        return;

                _stepFunnel = new Funnel(_start, _diagonalsDFS.length + 2);
                _crossedDiagonals = 0;
                _showNext = true;

        }// protected void calculateSteps(Graphics2D g2d)


        // *********************************************************************

        /**
         * If start or target have been replaced or the way of the calculation
         * has chaged, erase the current shortest path, the _stepFunnel and the
         * _runFunnel.
         * 
         * @see #_stepFunnel
         * @see #_runFunnel
         * @see #_shortestPath
         */
        protected void eraseFunnel()
        {
                _stepFunnel = null;
                _runFunnel = null;
                _shortestPath = null;

        }// protected void eraseFunnel()


        // *********************************************************************

        /**
         * Should a further step in the step by step evolution be possible?
         * 
         * @param b
         *                a boolean that is true, when it should
         */
        protected void setNext(boolean b)
        {
                _next.setEnabled(b);

        }// protected void setNext(boolean b)


        // *********************************************************************

        /**
         * Should a backspace in the step by step evolution be possible?
         * 
         * @param b
         *                a boolean that is true, when it should
         */
        protected void setPrev(boolean b)
        {
                _previous.setEnabled(b);

        }// protected void setPrev(boolean b)


        // **********************************************************************

        /**
         * Refreshes the view of the polygon.
         * 
         * @param g2d
         *                the drawing area
         */
        protected void clearPolygon(Graphics2D g2d)
        {
                eraseFunnel();

                GraphicsContext gc = new GraphicsContext();
                gc.setFillColor(Color.lightGray);
                gc.setFillStyle(1);
                gc.setForegroundColor(Color.lightGray);
                gc.setLineWidth(0.5f);

                // draw the polygon
                for (int i = 0; i < _triangPolys.length; i++)
                {
                        _triangPolys[i].draw(g2d, gc);
                }// for i

                // draw the outline of the polygon
                for (int i = _triangDiagonals.length; i < _allEdges.length; i++)
                {
                        _allEdges[i].draw(g2d, _gc);
                }// for i

        }// protected void clearPolygon(Graphics2D g2d)


        // *********************************************************************
        /**
         * refreshs the visualisation
         * 
         * @param g2d
         *                the drawing area
         * @see #_refresh(Graphics2D g2d)
         */
        protected void refreshView(Graphics2D g2d)
        {
                _refresh(g2d);

        }// protected void refreshView(Graphics2D g2d)


        // *********************************************************************
        // Private instance methods
        // *********************************************************************

        /**
         * refreshs the visualisation
         * 
         * @param g2d
         *                the drawing area
         * @see #_paintDFS(Graphics2D g2d)
         * @see #drawSP(Graphics2D g2d)
         */
        private void _refresh(Graphics2D g2d)
        {
                // draw the outline of the polygon
                for (int i = _triangDiagonals.length; i < _allEdges.length; i++)
                {
                        _allEdges[i].draw(g2d, _gc);
                }// for i

                _dg = new DualGraph(_triangPolys);
                _shortestPath = null;

                _paintDFS(g2d);

                if (_stepFunnel != null)
                {
                        if (_showNext)
                        {
                                _drawStepsFunnel(g2d);
                                _showNext = false;
                        } else
                                drawSP(g2d);

                        if (_showPrev)
                                drawOldFunnel(g2d);

                        return;
                }// if

                if (_runFunnel != null)
                        drawSP(g2d);

        }// private void _refresh(Graphics2D g2d)


        // *********************************************************************

        /**
         * Calculates the triangulation of the polygon and saves it to
         * {@link #_triangDiagonals} and to {@link #_allEdges}.
         * 
         * @param poly
         *                the single, simple, closed Polygon2
         * @see appsSwingGui.SeidelTriag.SeidelTriangulation
         */
        private void _calculateTriangulation(Polygon2 poly)
        {
                _polyArray = poly.edges();

                // computing and drawing the SeidelTriangulation
                _triangulation = new SeidelTriangulation(poly);
                _triangDiagonals = _triangulation.getDiagonals();

                _allEdges = new Segment2[(_triangDiagonals.length + _polyArray.length)];

                int j = 0;

                // first the calculated diagonals are added to allEdges,
                // then the outlining edges of the polygon are added
                for (int i = 0; i < _triangDiagonals.length + _polyArray.length; i++)
                {
                        if (i < _triangDiagonals.length)
                        {
                                _allEdges[i] = _triangDiagonals[i];
                        } else
                        {
                                _allEdges[i] = _polyArray[j];
                                j++;
                        }// else
                }// for

        }// private void _calculateTriangulation(Polygon2 poly)


        // *********************************************************************

        /**
         * Calculates and draws the diagonals of the DFS that need to be crossed
         * by the shortest path.
         * 
         * @param g2d
         *                the drawing area
         * 
         * @see #_drawDFS(Graphics2D)
         * @see #_diagonalsDFS
         * @see appsSwingGui.ShortestPathLP.DuaglGraph.getDiagonals_by_DFS
         * 
         */
        private void _paintDFS(Graphics2D g2d)
        {
                // only continue to calculate the shortest path, when there is
                // a start and a target inside the polygon
                if ((_start == null) || (_target == null))
                        return;

                if (!_pointInPolygon(_start) || !_pointInPolygon(_target))
                        return;

                _diagonalsDFS = _dg.get_diagonals_by_DFS(_locatePoint(_start), _locatePoint(_target));

                if (_diagonalsDFS == null)
                        return;

                _amountOfDiagonals = _diagonalsDFS.length;

                _drawDFS(g2d);

        }// private void _paintDFS(Graphics2d g2d)


        // *********************************************************************

        /**
         * Tests if that point is inside the polygon. Returns false, when the
         * point is outside the polygon, true else
         * 
         * @return true, if the point is inside the Polygon2
         * @param point
         *                the point to be tested
         * 
         * @see #_locatePoint(Point2 point)
         */
        private boolean _pointInPolygon(Point2 point)
        {
                if (_locatePoint(point) == -1)
                        return false;
                return true;

        }// private boolean _pointInPolygon(Point2 point)


        // *********************************************************************

        /**
         * returns the identification number of the triangle, in which the
         * point_item lies
         * 
         * @return -1, when the point is not inside the polygon at all
         * @return the identification number of the triangle, in which the
         *         point_item lies
         * @param point_item
         *                the Point2 to be located
         */
        private int _locatePoint(Point2 point_item)
        {
                Segment2[] tester = new Segment2[1];

                for (int i = 0; i < _triangPolys.length; i++)
                {
                        Segment2[] triang_edges = _triangPolys[i].edges();
                        Point2 center = _triangPolys[i].getCenterOfMass();
                        tester[0] = new Segment2(point_item, center);

                        // if addedPoint is not inside or on this convex
                        // polygon, continue
                        if (Segment2.intersects(tester, triang_edges))
                        {
                                boolean on_the_outline = false;

                                for (int k = 0; k < 3; k++)
                                {
                                        if (triang_edges[k].distance(point_item) == 0.0)
                                        {
                                                on_the_outline = true;
                                                break;
                                        }// if
                                }// for k

                                if (!on_the_outline)
                                        continue;
                        }// if

                        return i;

                }// for (int i = 0; i < _triangPolys.length; i++)

                // if the addedPoint is not inside the polygon, send an error
                // message
                System.out.println("Start and Target have to be INSIDE the polygon!");
                return -1;

        }// private int _locatePoint(Point2 point_item)


        // *********************************************************************

        /**
         * draws the DualGraph
         * 
         * @param g2d
         *                the drawing area
         * 
         * @see #_diagonalsDFS
         */
        private void _drawDFS(Graphics2D g2d)
        {
                // draw the needed diagonals of the DFS
                for (int i = 0; i < _diagonalsDFS.length; i++)
                {
                        _diagonalsDFS[i].draw(g2d, _gcDFS);

                }// for (int i = 0; i < _triangDiagonals.length; i++)

        }// private void _drawDFS(Graphics2D g2d)


        // *********************************************************************

        /**
         * draws the actual Funnel
         * 
         * @param g2d
         *                the drawing area
         * @param funnel
         *                the outline of the funnel as Point2List
         * @param oldFunnel
         *                the outline of the old funnel that has to be
         *                overwritten
         */
        private void _drawFunnel(Graphics2D g2d, Point2List funnel,
                        Point2List oldFunnel)
        {
                Polygon2 temp_polygon = null;

                if (oldFunnel != null)
                {
                        temp_polygon = new Polygon2(oldFunnel);
                        temp_polygon.draw(g2d, _gcClear);
                        _drawDFS(g2d);
                }// if

                // refresh the outline of the polygon as it might have been
                // overpainted
                for (int i = _triangDiagonals.length; i < _allEdges.length; i++)
                {
                        _allEdges[i].draw(g2d, _gc);
                }// for i

                _editor.refreshView();

                temp_polygon = new Polygon2(funnel);
                temp_polygon.draw(g2d, _gcFunnel);
                _drawDFS(g2d);

        }// private void _drawFunnel


        // *********************************************************************

        /**
         * First draws the old funnel, then the segments, that might be tangents
         * to the funnel and finally the current Funnel for the step by step
         * funnel evolution.
         * 
         * @param g2d
         *                the drawing area
         * 
         * @see #_stepFunnel
         */
        private void _drawStepsFunnel(Graphics2D g2d)
        {
                // first draw the old funnel
                Polygon2 temp_polygon = new Polygon2(_stepFunnel.oldFunnel);
                temp_polygon.draw(g2d, _gcPrevFunnel);

                // show the segments that are checked for being tangents
                if (_stepFunnel.possTangents != null)
                {
                        Segment2[] tangents = _stepFunnel.possTangents.toArray(new Segment2[0]);
                        for (int i = 0; i < tangents.length; i++)
                        {
                                tangents[i].draw(g2d, _gcTangents);

                                // this is needed to create a short pause
                                // between the show up of the tangents
                                BufferedCanvas b = display.getCanvas();
                                Graphics g = b.getGraphics();
                                b.paint(g);
                                try
                                {
                                        Thread.sleep(timer - 250);

                                } catch (InterruptedException ie)
                                {
                                }// catch
                        }// for i
                }// if

                temp_polygon = null;

                Point2List funnel = _stepFunnel.getActiveFunnel();

                // draw the current funnel
                temp_polygon = new Polygon2(funnel);
                temp_polygon.draw(g2d, _gcFunnel);

        }// private void _drawStepsFunnel(Graphics2D g2d)


        // *********************************************************************
        // Interface Extendable
        // *********************************************************************

        /**
         * Draws the SeidelTriangulation (of a copy of the actual polygon) of
         * the single simple Polygon after removing redundant points. Then it
         * calculates the dual graph of the copy and draws the result of the
         * DFS. If the step by step evolution is selected, it draws the funnels.
         * Finally it draws the shortest path.
         * 
         * @param scene
         *                the Polygon2Scene, representing the scene
         * @param g2d
         *                the drawing area
         */
        public void paint(Polygon2Scene scene, Graphics2D g2d)
        {
                // there can be only one polygon, so it is [0]
                Polygon2 poly = scene.getPolygon(0);

                // the SeidelTriangulation and all following operations can
                // only be executed, if the polygon is closed
                if (!poly.isSimple())
                        return;

                Polygon2 act_poly = new Polygon2(poly);
                act_poly.removeRedundantPoints();

                // calculate the Seidel Triangulation
                _calculateTriangulation(act_poly);

                // draw the outline of the polygon
                for (int i = _triangDiagonals.length; i < _allEdges.length; i++)
                {
                        _allEdges[i].draw(g2d, _gc);
                }// for i

                _triangPolys = _triangulation.getTriangles();
                _dg = new DualGraph(_triangPolys);

                _paintDFS(g2d);

                if (_stepFunnel != null)
                {
                        if (_showNext)
                        {
                                _drawStepsFunnel(g2d);
                                _showNext = false;
                        } else
                                drawSP(g2d);

                        return;
                }// if

                if (_runFunnel != null)
                        drawSP(g2d);

        } // public void paint(Polygon2Scene scene, Graphics2D g2d)


        // *********************************************************************

        /**
         * Handles the ActionEvents for the case that a step by step evolution
         * is generated.
         * 
         * @param e
         *                the ActionEvent
         * @param point
         *                the position of the mouse
         */
        public boolean processPopup(ActionEvent e, Point2 point)
        {
                if (_start == null || _target == null)
                        return true; // allow processing in the editor

                // if the user wants to see the next funnel of the step
                // by step evolution
                if (e.getActionCommand().equals("Next"))
                {
                        if (_crossedDiagonals >= 0)
                        {
                                if (_crossedDiagonals == _amountOfDiagonals)
                                {
                                        stepToTarget();
                                        _next.setEnabled(false);
                                } else
                                {
                                        addDiagonalByStep();
                                        _showNext = true;
                                }// else
                                _previous.setEnabled(true);
                        }// if(_crossedDiagonals >= 0)

                        return false;

                }// if (e.getActionCommand().equals("Next"))

                // if the user wants to see the previous funnel of the step
                // by step evolution
                if (e.getActionCommand().equals("Previous"))
                {
                        if (_crossedDiagonals > 1)
                        {
                                _stepsIndex = _crossedDiagonals - 1;
                                _crossedDiagonals = 0;
                                eraseFunnel();
                                _stepFunnel = new Funnel(_start, _diagonalsDFS.length + 2);
                                while (_crossedDiagonals < _stepsIndex)
                                        addDiagonalByStep();
                                _showPrev = true;
                        } else
                                _previous.setEnabled(false);

                        _next.setEnabled(true);

                        return false;
                }// if(e.getActionCommand().equals("Previous"))

                return true; // allowing processing in the editor

        }// processPopup


        // *********************************************************************

        /**
         * Adds the _next and _previous buttons to the lowerPanel and connects
         * the to the ActionListener of the editor.
         * 
         * @param editor
         *                the ShortestPathLPEditor
         */
        public void registerPolygonEditor(ExtendablePolygonEditor editor)
        {
                _editor = (ShortestPathLPEditor) editor;
                _previous = new JButton("Previous");
                _next = new JButton("Next");
                panel.add(_previous);
                panel.add(_next);
                _next.addActionListener(editor);
                _previous.addActionListener(editor);
                _next.setEnabled(true);
                _previous.setEnabled(false);

        } // registerPolygonEditor(ExtendablePolygonEditor editor)


        // *********************************************************************
        // unused Interface methods
        // *********************************************************************

        /**
         * the editor handles this event
         * 
         * @param e
         *                the MouseEvent
         * @param point
         *                the location of the mouse
         */
        public boolean processMouseDragged(MouseEvent e, Point2 point)
        {
                return true; // allow processing in the editor

        } // processMouseDragged(MouseEvent e, Point2 point)


        // *********************************************************************

        /**
         * the editor handles this event
         * 
         * @param e
         *                the MouseEvent
         * @param point
         *                the location of the mouse
         */
        public boolean processMousePressed(MouseEvent e, Point2 point)
        {
                return true; // allow processing in the editor

        } // processMousePressed(MouseEvent e, Point2 point)


        // *********************************************************************

        /**
         * the editor handles this event
         * 
         * @param e
         *                the MouseEvent
         * @param point
         *                the location of the mouse
         */
        public boolean processMouseReleased(MouseEvent e, Point2 point)
        {
                return true; // allowing processing in the editor

        } // processMouseReleased(MouseEvent e, Point2 point)


        // *********************************************************************

        /**
         * is unused
         * 
         * @param menu
         *                the JPopupMenu
         */
        // the following methods are unused:
        public void popupMenu(JPopupMenu menu)
        {
        } // popupMenu

} // ShortestPathLPExtension
