/* 
 * File: DCEL.java
 * Created on 01.08.2005 by snej
 */

package anja.graph.dcel;

import java.awt.geom.*;
import java.awt.*;
import java.util.*;

import static anja.util.MathConstants.*; // for math constants
import anja.graph.*;
import static anja.graph.Constants.*;

/**
 * This class implements a modified DCEL(Doubly Connected Edge List) data
 * structure which can handle both directed and undirected graphs, with or
 * without multiedges [i.e. multiple edges that connect the same vertices].
 * 
 * @version 1.0
 * @author Ibragim Kouliev
 */

public class DCEL extends Graph<DCEL_Vertex, DCEL_Edge> {

    // *************************************************************************
    // Public constants
    // *************************************************************************

    // *************************************************************************
    // Private constants
    // *************************************************************************

    private final static boolean _ALPHA_BLEND = false;
    private final static boolean _DRAW_LINKS = true;

    // *************************************************************************
    // Class variables
    // *************************************************************************

    // *************************************************************************
    // Public instance variables
    // *************************************************************************

    // *************************************************************************
    // Protected instance variables
    // *************************************************************************

    // *************************************************************************
    // Private instance variables
    // *************************************************************************

    // ------------------------- Graph element storage -------------------------

    private LinkedList<DCEL_Vertex> _vertices; // DCEL_Vertex containers
    private LinkedList<DCEL_Edge> _edges; // DCEL_Edge containers
    private LinkedList<DCEL_Face> _faces; // DCEL_Face containers;

    // *************************************************************************
    // Constructors
    // *************************************************************************

    /**
     * 
     */
    public DCEL() {
        _vertices = new LinkedList<DCEL_Vertex>();
        _edges = new LinkedList<DCEL_Edge>();
        _faces = new LinkedList<DCEL_Face>();
    }

    public DCEL(SpacePartition pointLocator) {
    	this();
        _spacePartition = pointLocator;
    }

    // *************************************************************************
    // Public instance methods
    // *************************************************************************

    // ---------------------- Addition & removal of elements -------------------

    protected DCEL_Vertex createVertex() {
        DCEL_Vertex vtx = new DCEL_Vertex();
            _vertices.add(vtx);

            if (_spacePartition != null) {
                _spacePartition.addVertex(vtx);
            }
        return vtx;
    }

    // *************************************************************************

    /**
     * Wrapper for {@link DCEL#addEdge(Edge)}, instantiates a new edge on the
     * fly given the start and target vertices.
     * 
     * @param source
     *            start vertex
     * @param target
     *            target vertex
     */
    protected DCEL_Edge createEdge(DCEL_Vertex source, DCEL_Vertex target) {
    	DCEL_Edge edge = new DCEL_Edge((DCEL_Vertex)source, (DCEL_Vertex)target);
    	this.insertEdge(edge);
    	return edge;
    }
    
    private void insertEdge(DCEL_Edge edge) {    	
    	DCEL_Vertex s = edge.getSource();
    	DCEL_Vertex t = edge.getTarget();

        if (s == t) {
            // no edges with same source and target at this time
            // return;
            // if there's no edge present at the vertex the edge is selflinked
            // in all directions
            if (s.getDcelEdge() == null)
                s.setDcelEdge(edge);
            s._slings.add(edge);
        } else {
            /*
             * Set the adj.edge pointer for the source vertex if doesn't have
             * one assigned to it yet.
             */
            if (s.getDcelEdge() == null
                    || s.getDcelEdge().isSling()) {
                /*
                 * the start vertex doesn't have an adjacent edge yet -> set the
                 * adj. edge pointer, and also link the edge to itself as it's
                 * the only one.
                 */
                s.setDcelEdge(edge);

                edge.setPrevDcelEdgeCW(edge);
                edge.setPrevDcelEdgeCCW(edge);
            } else {
                VertexEdgeIterator it = new VertexEdgeIterator(
                        edge.getSource(), CLOCKWISE);

                DCEL_Edge prev_edge = null, next_edge = null;

                double this_angle = edge.getSourceAngle();
                double next_angle = 0, prev_angle = 0;
                int counter = 0;
                while (it.hasNext()) {
                    next_edge = it.next();
                    if (next_edge.isSling())
                        break;

                    if (next_edge.hasSameSource(edge)) {
                        next_angle = next_edge.getSourceAngle();
                        prev_edge = next_edge.getPrevDcelEdgeCCW();
                    } else {
                        next_angle = next_edge.getTargetAngle();
                        prev_edge = next_edge.getNextDcelEdgeCCW();
                    }
                    if (prev_edge.hasSameSource(edge))
                        prev_angle = prev_edge.getSourceAngle();
                    else
                        prev_angle = prev_edge.getTargetAngle();

                    /*
                     * next edge is now further CW or at the same angle the new
                     * one, if an edge with a smaller angle exists
                     */

                    if (((this_angle > next_angle) || Math.abs(this_angle
                            - next_angle) < DBL_EPSILON))
                        break;
                }

                /*
                 * if this_angle < next_angle, the new edge has the smallest
                 * angle, next_angle is then the first angle of the vertex
                 */
                if (this_angle < next_angle) {
                    next_edge = s.getDcelEdge();
                    if (next_edge.hasSameSource(edge)) {
                        next_angle = next_edge.getSourceAngle();
                        prev_edge = next_edge.getPrevDcelEdgeCCW();
                    } else {
                        next_angle = next_edge.getTargetAngle();
                        prev_edge = next_edge.getNextDcelEdgeCCW();
                    }
                    if (prev_edge.hasSameSource(edge))
                        prev_angle = prev_edge.getSourceAngle();
                    else
                        prev_angle = prev_edge.getTargetAngle();
                }
                /*
                 * if prev_angle < this_angle, it means that no edge with a
                 * higher angle than the new edge is present. That means it has
                 * to be set as the vertex's first angle so that the above
                 * search will function correctly
                 */
                if (this_angle > prev_angle)
                    s.setDcelEdge(edge);

                // link edge pointers to appropriate neighbors
                edge.setPrevDcelEdgeCCW(prev_edge);
                edge.setPrevDcelEdgeCW(next_edge);

                if (next_edge.hasSameSource(edge))
                    next_edge.setPrevDcelEdgeCCW(edge);
                else
                    next_edge.setNextDcelEdgeCCW(edge);

                if (prev_edge.hasSameSource(edge))
                    prev_edge.setPrevDcelEdgeCW(edge);
                else
                    prev_edge.setNextDcelEdgeCW(edge);
            }
            // END of connectivity operations for the SOURCE vertex

            /* now the same for the target vertex, comments see above */
            if (t.getDcelEdge() == null
                    || t.getDcelEdge().isSling()) {
                t.setDcelEdge(edge);

                edge.setNextDcelEdgeCW(edge);
                edge.setNextDcelEdgeCCW(edge);
            } else {
                VertexEdgeIterator it = new VertexEdgeIterator(
                        edge.getTarget(), CLOCKWISE);
                DCEL_Edge prev_edge = null, next_edge = null;

                double this_angle = edge.getTargetAngle();
                double next_angle = 0, prev_angle = 0;

                while (it.hasNext()) {
                    next_edge = it.next();
                    if (next_edge.isSling())
                        break;

                    if (next_edge.hasSameTarget(edge)) {
                        next_angle = next_edge.getTargetAngle();
                        prev_edge = next_edge.getNextDcelEdgeCCW();
                    } else {
                        next_angle = next_edge.getSourceAngle();
                        prev_edge = next_edge.getPrevDcelEdgeCCW();
                    }
                    if (prev_edge.hasSameTarget(edge))
                        prev_angle = prev_edge.getTargetAngle();
                    else
                        prev_angle = prev_edge.getSourceAngle();
                    if (((this_angle > next_angle) || (Math.abs(next_angle
                            - this_angle) < DBL_EPSILON)))
                        break;
                }

                if (this_angle < next_angle) {
                    next_edge = t.getDcelEdge();
                    if (next_edge.hasSameTarget(edge)) {
                        next_angle = next_edge.getTargetAngle();
                        prev_edge = next_edge.getNextDcelEdgeCCW();
                    } else {
                        next_angle = next_edge.getSourceAngle();
                        prev_edge = next_edge.getPrevDcelEdgeCCW();
                    }
                    if (prev_edge.hasSameTarget(edge))
                        prev_angle = prev_edge.getTargetAngle();
                    else
                        prev_angle = prev_edge.getSourceAngle();
                }

                if (this_angle > prev_angle)
                    t.setDcelEdge(edge);

                edge.setNextDcelEdgeCW(next_edge);
                edge.setNextDcelEdgeCCW(prev_edge);
                if (next_edge.hasSameTarget(edge))
                    next_edge.setNextDcelEdgeCCW(edge);
                else
                    next_edge.setPrevDcelEdgeCCW(edge);
                if (prev_edge.hasSameTarget(edge))
                    prev_edge.setNextDcelEdgeCW(edge);
                else
                    prev_edge.setPrevDcelEdgeCW(edge);
            }
            // END of connectivity operations for the TARGET vertex
        }

        // insert edge container into storate
        _edges.add(edge);

        if (_spacePartition != null) {
            _spacePartition.addEdge(edge);
        }

    }

    // *************************************************************************


    /**
     * Removes the edges around a Vertex from the graph.
     * The vertex itself is only removed from the vertex list if deleteFromList is true.
     * This is so a vertex can be removed from an iterator without destroying the iterator of the vertex list. 
     */
    protected void deleteVertex(DCEL_Vertex vertex, boolean deleteFromList) {
    	if (vertex == null || vertex.getGraph() != this) return;
    	while (vertex.getIncidentEdge() != null)
    			deleteEdge(vertex.getIncidentEdge());
    	if (_spacePartition != null) _spacePartition.removeVertex(vertex);
    	if (deleteFromList) _vertices.remove(vertex);
    	super.deleteVertex(vertex);
    }
    
    
    
    public void deleteVertex(DCEL_Vertex vertex) {
        deleteVertex(vertex, true);
    }

    // *************************************************************************
    /**
     * Removes an edges  from the graph.
     * The edge  is only removed from the edge list if deleteFromList is true.
     * This is so an edge can be removed from an iterator without destroying the iterator of the edge list. 
     */    
    protected void deleteEdge(DCEL_Edge edge, boolean deleteFromList) {
        if (edge == null) return;

        if (!_edges.contains(edge)) return;
        if (_spacePartition != null) {
            _spacePartition.removeEdge(edge);
        }
        if (edge.isSling()) {
            DCEL_Vertex vertex = edge.getSource();
            vertex._slings.remove(edge);
            if (edge.equals(vertex.getDcelEdge())) {
                if (vertex._slings.size() > 0)
                    vertex.setDcelEdge(vertex._slings.firstElement());
                else
                    vertex.setDcelEdge(null);
            }
            return;
        }

        DCEL_Vertex target = edge.getTarget();
        DCEL_Vertex source = edge.getSource();
        // if we remove the first edge of either vertex we have to set the edge
        // with the next highest angle
        // as the first which is the next edge clockwise, since addEdge() always
        // assumes that the first
        // edge is the one with the highest angle
        if (edge.equals(target.getDcelEdge())) {
            if (edge.targetIsSelfLinked())
                target.setDcelEdge(null);
            else
                target.setDcelEdge(target.getDcelEdge().getNextDcelEdgeCW());
        }
        if (edge.hasSameTarget(edge.getNextDcelEdgeCW()))
            edge.getNextDcelEdgeCW().setNextDcelEdgeCCW(edge.getNextDcelEdgeCCW());
        else
            edge.getNextDcelEdgeCW().setPrevDcelEdgeCCW(edge.getNextDcelEdgeCCW());
        if (edge.hasSameTarget(edge.getNextDcelEdgeCCW()))
            edge.getNextDcelEdgeCCW().setNextDcelEdgeCW(edge.getNextDcelEdgeCW());
        else
            edge.getNextDcelEdgeCCW().setPrevDcelEdgeCW(edge.getNextDcelEdgeCW());

        if (edge.equals(source.getDcelEdge())) {
            if (edge.sourceIsSelfLinked())
                source.setDcelEdge(null);
            else
                source.setDcelEdge(source.getDcelEdge().getPrevDcelEdgeCW());
        }
        if (edge.hasSameSource(edge.getPrevDcelEdgeCW()))
            edge.getPrevDcelEdgeCW().setPrevDcelEdgeCCW(edge.getPrevDcelEdgeCCW());
        else
            edge.getPrevDcelEdgeCW().setNextDcelEdgeCCW( edge.getPrevDcelEdgeCCW());
        if (edge.hasSameSource(edge.getPrevDcelEdgeCCW()))
            edge.getPrevDcelEdgeCCW().setPrevDcelEdgeCW(edge.getPrevDcelEdgeCW());
        else
            edge.getPrevDcelEdgeCCW().setNextDcelEdgeCW(edge.getPrevDcelEdgeCW());
        
        if (deleteFromList) _edges.remove(edge);
    }
    
    public void deleteEdge(DCEL_Edge edge) {
    	deleteEdge(edge, true);
    }

    // *************************************************************************

    public void clear() {
        _vertices.clear();
        _edges.clear();
        _faces.clear();

        // TODO: any additional cleanup code
    }

    // ------------------------------ Queries ----------------------------------

    public boolean isEmpty() {
        return (_vertices.isEmpty() & _edges.isEmpty());
    }

    // *************************************************************************

    public int getNumVertices() {
        return _vertices.size();
    }

    // *************************************************************************

    public int getNumEdges() {
        return _edges.size();
    }

    // *************************************************************************

    public int getNumFaces() {
        return _faces.size();
    }

    // *************************************************************************

    public boolean contains(Vertex v) {
        if (v == null)
            return false;
        DCEL_Vertex dcel_v = (DCEL_Vertex) v;
        if (dcel_v == null)
            return false;
        if (_vertices.indexOf(dcel_v) != -1)
            return true;
        return false;
    }

    // *************************************************************************

    public boolean contains(Edge e) {
        if (e == null)
            return false;
        DCEL_Edge dcel_e = (DCEL_Edge) e;
        if (dcel_e == null)
            return false;
        if (_edges.indexOf(dcel_e) != -1)
            return true;
        return false;
    }

    // -------------------- Element access via iterators -----------------------

    public Iterator<Vertex> getAllVertices() {
    	return new AllElementsAccessor<Vertex>(_vertices, this);    	
    }

    // *************************************************************************

    public Iterator<Edge> getAllEdges() {
    	return new AllElementsAccessor<Edge>(_edges, this);
    }

    // *************************************************************************

    public Iterator<Face> getAllFaces() {
    	return new AllElementsAccessor<Face>(_faces, this);
    }

    public DCEL_Vertex getAnyVertex() {
        if (_vertices.size() > 0)
            return _vertices.getFirst();
        else
            return null;
    }

    public DCEL_Edge getAnyEdge() {
        if (_edges.size() > 0)
            return _edges.getFirst();
        else
            return null;
    }

    // --------------------------- Visualization -------------------------------

    public void drawSchematic(Graphics2D graphics, double pixelSize, Font font,
            Rectangle2D viewport) {
        // TODO: debug rendering

        // remember previous composite
        Composite comp = graphics.getComposite();

        AlphaComposite alpha_blend = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.5f);

        if (_ALPHA_BLEND)
            graphics.setComposite(alpha_blend);

        // ------- THIS IS SOME PRETTY LAME CODE !!!!!!!!! -----------------

        if (_DRAW_LINKS) {
            // ------------------- draw legend display
            // --------------------------
            Rectangle2D.Double color_box = new Rectangle2D.Double();

            Color prev_color = graphics.getColor();
            Font prev_font = graphics.getFont();

            {
                graphics.setFont(font);

                graphics.drawString("LEGEND:",
                        (float) (viewport.getX() + viewport.getWidth() - 180 * pixelSize),
                        (float) (viewport.getY() + viewport.getHeight() - 20 * pixelSize));

                color_box.setRect((viewport.getX() + viewport.getWidth() - 180 * pixelSize), (viewport
                        .getY() + viewport.getHeight() - 40 * pixelSize), 10.0 * pixelSize,
                        10.0 * pixelSize);

                graphics.setColor(Color.red);
                graphics.fill(color_box);

                graphics.drawString("Previous clockwise", (float) (color_box
                        .getX() + 15 * pixelSize), (float) color_box.getY());

                color_box.y -= 20 * pixelSize;

                graphics.setColor(Color.blue);
                graphics.fill(color_box);

                graphics.drawString("Previous counterclockwise",
                        (float) (color_box.getX() + 15 * pixelSize),
                        (float) color_box.getY());

                color_box.y -= 20 * pixelSize;

                graphics.setColor(Color.magenta);
                graphics.fill(color_box);

                graphics.drawString("Next clockwise",
                        (float) (color_box.getX() + 15 * pixelSize),
                        (float) color_box.getY());

                color_box.y -= 20 * pixelSize;

                graphics.setColor(Color.black);
                graphics.fill(color_box);

                graphics.drawString("Next counterclockwise", (float) (color_box
                        .getX() + 15 * pixelSize), (float) color_box.getY());
                
                graphics.setColor(Color.black);
                graphics.drawRect((int)(viewport.getX() + viewport.getWidth() - 190 * pixelSize),
                		(int)(viewport.getY() + viewport.getHeight() - 110 * pixelSize),
                		(int)(180*pixelSize),(int)(110*pixelSize));

            }
            graphics.setColor(prev_color);
            graphics.setFont(prev_font);

            //------------------------------------------------------------------
            // -

            // visualize edge links
            Iterator edge_it = _edges.iterator();
            DCEL_Edge next_edge;

            int counter = 0;

            while (edge_it.hasNext()) {
                next_edge = (DCEL_Edge) edge_it.next();
                next_edge.drawLinks(graphics, pixelSize, font);

                counter++;
            }

            // vertex coordinate frames

            Line2D marker = new Line2D.Double();

            Iterator vtx_it = _vertices.iterator();
            Vertex next_vertex;
            double x, y;
            double radius;

            while (vtx_it.hasNext()) {
                next_vertex = ((DCEL_Vertex) vtx_it.next());

                x = next_vertex.getX();
                y = next_vertex.getY();
                radius = next_vertex.getActualRadius();

                // draw crosshair of the local coordinate system axes

                marker.setLine(x, y + radius, x, y + radius + 50);
                graphics.draw(marker);

                marker.setLine(x, y - radius, x, y - radius - 50);
                graphics.draw(marker);

                marker.setLine(x + radius, y, x + radius + 50, y);
                graphics.draw(marker);

                marker.setLine(x - radius, y, x - radius - 50, y);
                graphics.draw(marker);
            }
        }

        // iterate through all of vertex's 0 edges clockwise and number
        // them.

        if (!_edges.isEmpty()) {

            VertexEdgeIterator itt = (VertexEdgeIterator) _vertices.getFirst()
                    .getEdgeIterator(CLOCKWISE);
            DCEL_Vertex vtx = _vertices.getFirst();
            //vtx.setLabel("0");

            DCEL_Edge temp_edge;
            int number = 1;

            /*while (itt.hasNext()) {

                temp_edge = itt.next();

                if (temp_edge.getSource() == vtx) {
                    // set label on target vertex

                    temp_edge.getTarget().setLabel(
                            String.valueOf(number));
                } else {
                    // set label on source vertex
                    temp_edge.getSource().setLabel(
                            String.valueOf(number));

                }

                number++;

            } // end while()*/

            // System.out.print("\n");
        } // endif

        // anything else ?

        /*
         * // temporary debug
         * 
         * float h_delta = 1.0f / _edges.size(); Color temp =
         * Color.getHSBColor(counter h_delta, 1.0f, 1.0f); Color color = new
         * Color(temp.getRed() / 255.0f, temp.getGreen() / 255.0f,
         * temp.getBlue() / 255.0f, 1.0f);
         * 
         * graphics.setColor(color);
         */

        // END
        // restore previous composite
        graphics.setComposite(comp);

        // System.out.println("\n\n\n");
    }

    // ------------------------------- Other -----------------------------------

    public void vertexMoved(Vertex vertex) {
        // remove all edges, call their verticesMoved function so that their
        // angles will be updated
        // and add them again
        // it's not the fastest way for reordering the edges but the simplest
        Vector<DCEL_Edge> edges = new Vector<DCEL_Edge>();
        VertexEdgeIterator it = new VertexEdgeIterator((DCEL_Vertex)vertex, CLOCKWISE);
        int counter = 0;
        while (it.hasNext()) {
            counter++; /* System.out.println(counter); */
            edges.addElement(it.next());
        }
        for (int i = 0; i < edges.size(); i++)
            this.deleteEdge(edges.elementAt(i));
        for (int i = 0; i < edges.size(); i++) {
            edges.elementAt(i).verticesMoved();
            this.addEdge(edges.elementAt(i));
        }
        /*
         * if(vertex.getIncidentEdge() != null) { VertexEdgeIterator it = new
         * VertexEdgeIterator(vertex, Iterator<Edge>.CLOCKWISE);
         * 
         * //TODO: check edge angle recalculation DCEL_Edge edge;
         * while(it.hasNext()) { it.nextEdge(); // go to next edge edge =
         * it.getDcelEdge(); edge.verticesMoved(); }
         * 
         * //TODO: edge reordering code... }
         */

        if (_spacePartition != null) {
            _spacePartition.vertexMoved(vertex);
        }
    }



    // *************************************************************************
    // Protected instance methods
    // *************************************************************************

    protected void finalize() throws Throwable {
        _vertices.clear();
        _edges.clear();
        _faces.clear();

        if (_spacePartition != null) {
            _spacePartition.clear();
        }
        super.finalize();
    }

    // *************************************************************************

    // insert an edge into appropriate order around a vertex, and link it
    protected void orderAndLinkEdge() {
        // stub
    }

    // *************************************************************************
    // Private instance methods
    // *************************************************************************

    /*
     * All methods below are not yet decided upon....
     */

    // ---------------------------------------------------------------------

    public LinkedList getAdjList(Vertex v) {

        return null;
    }

    // *************************************************************************

    public LinkedList getIncidenceList(Vertex v) {

        return null;
    }

    // --------------------------- STUBS ------------------------------------

}
