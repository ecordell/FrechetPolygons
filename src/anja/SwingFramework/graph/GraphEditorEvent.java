/*
 * GraphEditorEvent.java
 */

package anja.SwingFramework.graph;

import anja.graph.*;
import java.awt.geom.AffineTransform;
import java.util.*;

import anja.SwingFramework.*;
import anja.SwingFramework.event.*;


/**
 * NewGraphEditorEvents are the primary means of communication between
 * {@link JGraphEditor}(or subclasses) and the "outside
 * world". Most graph editor operations are mapped to specific events
 * (see {@link GraphEvent} class for enum fields),
 * which are fired upon any changes inside an editor.
 * 
 * <p>Every dispatched NewGraphEditorEvent contains an event, for
 * example <b>VERTEX_ADDED </b>, and a reference to the modified
 * vertex, edge, or vertex/edge selection. There are also some special
 * events, like <b>GRAPH_CLEARED </b>, which only contain the event
 * code.
 * To be able to use these events other than in switch clauses without
 * having to type NewGraphEvent.VERTEX_ADDED and such every time, you
 * can simply import these values:
 * import static anja.SwingFramework.graph.GraphEvent.*;
 * 
 * <p>Classes that wish to be notified of these events must implement
 * the {@link EditorListener}interface and register themselves
 * using the {@link JAbstractEditor#addEditorListener(EditorListener)}
 * method.
 * 
 *  
 */
public class GraphEditorEvent extends EditorEvent {
	//----------------------------- Data references ---------------------------

	protected int             _iTransformType;
	protected Vertex          _vertex; 	 // associated vertex
	protected Edge            _edge;  	 // associated edge
	protected Vector          _selection;	 // associated selection
	protected AffineTransform _transform;   // associated transform

	//*************************************************************************
	// 				      Constructors
	//*************************************************************************

	/**
	 * Creates a new graph editor event with an event code and no
	 * other attributes.
	 * 
	 * @param source The source editor that fired the event/
	 * @param event Event identifiert (see NewGraphConstants).
     */
	public GraphEditorEvent(JGraphEditor source, GraphEvent event) {
		super(source, event);
	}

	//*************************************************************************

	/**
	 * Creates a new vertex-type graph editor event.
	 * 
	 * @param source The source editor that fired the event.
	 * @param vertex Reference to the modified VisualVertex.
     * @param event Event identifier.
     */
	public GraphEditorEvent(JGraphEditor source, Vertex vertex, GraphEvent event) {
		super(source, event);
		_vertex = vertex;
	}

	//*************************************************************************
	/**
	 * Creates a new edge-type graph editor event.
	 * 
	 * @param source The source editor that fired the event.
     * @param edge Reference to the modified VisualEdge.
     * @param event Event identifier.
     */
	public GraphEditorEvent(JGraphEditor source, Edge edge, GraphEvent event) {
		super(source, event);
		_source = source;
		_edge = edge;
	}

	//*************************************************************************
	/**
	 * Creates a new selection-type graph editor event that
	 * additionally carries information about the applied affine
	 * transform and its type.
	 * -- Currently there are no events for this --
	 * 
	 * @param source The source editor that fired the event.
	 * @param selection Reference to the modified selection.
     * @param transform Reference to the affine transform that
     *        modified the selection.
     * @param event The event identifier.
     * @param type The transform type.
     */
	public GraphEditorEvent(JGraphEditor source,
				Vector selection,
				AffineTransform transform,
				GraphEvent event,
				int type) {
		super(source, event);
		_source = source;
		_selection = selection;
		_transform = transform;
		_iTransformType = type;
	}

	//*************************************************************************
	// 			      Public instance methods
	//*************************************************************************

	/**
     * Returns the vertex associated with this event.
     * 
     * @return reference to VisualVertex, or <code>null</code>
     * if there's no associated vertex.
     */
	public Vertex getVertex() 	{
		return _vertex;
	}

	//*************************************************************************

	/**
     * Returns the edge associated with this event.
     * 
     * @return reference to VisualEdge, or <code>null</code> 
     * if there's no associated edge.
     */
	public Edge getEdge() {
		return _edge;
	}

	//*************************************************************************

	/**
     * Returns the vertex/edge selection associated with 
     * this event.
     * 
     * @return reference to a Vector containing the selection,
	 * or <code>null</code> if there's no associated selection.
	 */
	public Vector getSelection() {
		return _selection;
	}

	//*************************************************************************
	/**
     * Returns the affine transformation associated to a selection event. 
     * 
	 * @return reference to a AffineTransform 
	 * or <code>null</code> if there's none.
	 */
	public AffineTransform getTransform() {
		return _transform;
	}

	//*************************************************************************
	/**
     * Returns the vertex/edge selection associated with 
     * this event.
     * 
	 * @return int indicatin the transform type,
	 * or <code>0</code> if there's transform associated with this event.
	 */
	public int getTransformType() {
		return _iTransformType;
	}

	//disabled default constructor
	protected GraphEditorEvent() {

	}
}




