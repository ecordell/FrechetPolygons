/*
 * Created on Mar 6, 2005
 *
 * GraphConstants.java
 * 
 * Event indentifiers' names speak for themselves.
 * Each VERTEX_* event contains the related vertex in getVertex().
 * Each EDGE_* event contains the related edge in getEdge().
 * Each VERTEX_SELECTION_*, EDGE_SELECTION_* and SELECTION_* event
 * contains the related selection in getSelection().
 * 
 */

package anja.SwingFramework.graph;

import anja.SwingFramework.event.EventType;

public enum GraphEvent implements EventType
{
	NO_ACTION,               //contains no additional info

	VERTEX_ADDED,
	VERTEX_REMOVED,
	VERTEX_SELECTED,
	VERTEX_MOVED,
	VERTEX_MOVE_START,
	VERTEX_MOVE_STOP,
	VERTEX_COLOR_CHANGED,
	VERTEX_LABEL_CHANGED,
	VERTEX_RADIUS_CHANGED,

	EDGE_ADDED,
	EDGE_REMOVED,
	EDGE_MODIFIED,
	EDGE_SELECTED,
	EDGE_MOVED,
	EDGE_COLOR_CHANGED,
	EDGE_WEIGHT_CHANGED,

	VERTEX_SELECTION_REMOVED,
	VERTEX_SELECTION_MODIFIED,
	VERTEX_SELECTION_CHANGED,
	VERTEX_SELECTION_COLOR_CHANGED,

	EDGE_SELECTION_MOVED,
	EDGE_SELECTION_MODIFIED,
	EDGE_SELECTION_REMOVED,
	EDGE_SELECTION_CHANGED,
	EDGE_SELECTION_COLOR_CHANGED,

	SELECTION_SCALED,
	SELECTION_ROTATED,
	SELECTION_TRANSLATED,
	SELECTION_SHEARED,

	GRAPH_CLEARED

}






