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

import anja.SwingFramework.*;
import anja.graph.*;
import static anja.SwingFramework.graph.GraphEvent.*;

import java.awt.geom.Point2D;
import java.awt.Color;
import java.util.Vector;
import java.awt.Graphics2D;

public class GraphUndo extends JAbstractUndoAction
{
	private GraphEvent _event;
	private Object _object;
	private Object _addData;  //additional data, for example the old vertex position
	private JGraphScene _scene;
	private JGraphEditor _editor;

	public GraphUndo(JGraphScene scene, JGraphEditor editor,
		GraphEvent event, Object relatedObject) {
		_scene = scene;
		_editor = editor;
		_event = event;
		_object = relatedObject;
		_addData = null;
	}

	public GraphUndo(JGraphScene scene, JGraphEditor editor,
		GraphEvent event, Object relatedObject, Object additionalData) {
		_scene = scene;
		_editor = editor;
		_event = event;
		_object = relatedObject;
		_addData = additionalData;
	}


	public void undo() {
		switch (_event) {
			case VERTEX_ADDED:
				_scene.removeVertex((Vertex)_object);
				_editor.fireGraphEditorEvent(_editor, (Vertex)_object, VERTEX_REMOVED);
				break;
			case VERTEX_REMOVED:
				_scene.getGraph().addVertex((Vertex)_object);
				_editor.fireGraphEditorEvent(_editor, (Vertex)_object, VERTEX_ADDED);
				break;
			case VERTEX_MOVED:
				((Vertex)_object).moveTo((Point2D)_addData);
				_editor.fireGraphEditorEvent(_editor, (Vertex)_object, VERTEX_MOVED);
				break;
			case VERTEX_COLOR_CHANGED:
				((Vertex)_object).setColor((Color)_addData);
				_editor.fireGraphEditorEvent(_editor, (Vertex)_object, VERTEX_COLOR_CHANGED);
				break;
			case VERTEX_LABEL_CHANGED:
				((Vertex)_object).setLabel((String)_addData);
				_editor.fireGraphEditorEvent(_editor, (Vertex)_object, VERTEX_LABEL_CHANGED);
				break;
			case VERTEX_RADIUS_CHANGED:
				((Vertex)_object).setRadius(((Integer)_addData).intValue());
				_editor.fireGraphEditorEvent(_editor, (Vertex)_object, VERTEX_RADIUS_CHANGED);
				break;

			case EDGE_ADDED:
				_scene.removeEdge((Edge)_object);
				_editor.fireGraphEditorEvent(_editor, (Edge)_object, EDGE_REMOVED);
				break;
			case EDGE_REMOVED:
				_scene.getGraph().addEdge((Edge)_object);
				_editor.fireGraphEditorEvent(_editor, (Edge)_object, EDGE_ADDED);
				break;
			case EDGE_COLOR_CHANGED:
				((Edge)_object).setColor((Color)_addData);
				_editor.fireGraphEditorEvent(_editor, (Edge)_object, EDGE_COLOR_CHANGED);
				break;
			case EDGE_WEIGHT_CHANGED:
				((Edge)_object).setWeight(((Double)_addData).doubleValue());
				_editor.fireGraphEditorEvent(_editor, (Edge)_object, EDGE_WEIGHT_CHANGED);
				break;

			case VERTEX_SELECTION_REMOVED:
				{
					Object[] vertices = (Object[])_object;
					for (int i=0;i<vertices.length;i++) {
						Vertex v = (Vertex)vertices[i];
						_scene.getGraph().addVertex(v);
						_editor.fireGraphEditorEvent(_editor, v, VERTEX_ADDED);
					}
					Object[] edges = (Object[])_addData;
					System.out.println(edges.length);
					for (int i=0; i<edges.length && i<5; i++) {
						_scene.draw((Graphics2D)_editor.getHub().getDisplayPanel().getGraphics());
						_editor.getHub().getTextDump().println(Integer.toString(i));
						Edge e = (Edge)edges[i];
						_scene.getGraph().addEdge(e);
						_editor.fireGraphEditorEvent(_editor, e, EDGE_ADDED);
					}
				}
				break;
			case EDGE_SELECTION_REMOVED:
				{
					Object[] edges = ((Object[])_object);
					for (int i=0;i<edges.length;i++) {
						Edge e = (Edge)edges[i];
						_scene.getGraph().addEdge(e);
						_editor.fireGraphEditorEvent(_editor, e, EDGE_ADDED);
					}
				}
				break;
		}	
	}

	public String toString() {
		String s = "";
		switch (_event) {
			case VERTEX_ADDED:
				s+="VERTEX_ADDED "+((Vertex)_object).getLabel();
				break;
			case VERTEX_REMOVED:
				s+="VERTEX_REMOVED "+((Vertex)_object).getLabel();
				break;
			case VERTEX_MOVED:
				s+="VERTEX_MOVED "+((Vertex)_object).getLabel();
				break;
			case VERTEX_COLOR_CHANGED:
				s+="VERTEX_COLOR_CHANGED "+((Vertex)_object).getLabel();
				break;
			case VERTEX_LABEL_CHANGED:
				s+="VERTEX_LABEL_CHANGED "+((Vertex)_object).getLabel();
				break;
			case VERTEX_RADIUS_CHANGED:
				s+="VERTEX_RADIUS_CHANGED "+((Vertex)_object).getLabel();
				break;

			case EDGE_ADDED:
				s+="EDGE_ADDED "+((Edge)_object).getLabel();
				break;
			case EDGE_REMOVED:
				s+="EDGE_REMOVED "+((Edge)_object).getLabel();
				break;
			case EDGE_COLOR_CHANGED:
				s+="EDGE_COLOR_CHANGED "+((Edge)_object).getLabel();
				break;
			case EDGE_WEIGHT_CHANGED:
				s+="EDGE_RADIUS_CHANGED "+((Edge)_object).getLabel();
				break;

			case VERTEX_SELECTION_REMOVED:
				s+="VERTEX_SELECTION_REMOVED";
				break;
			case EDGE_SELECTION_REMOVED:
				s+="EDGE_SELECTION_REMOVED";
				break;
		}
		return s;
	}
}






