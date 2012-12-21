/*
 * NewgraphDebugScene.java
 */

package anja.graph.debug;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

//import anja.geom.Point2;
import anja.graph.*;

import anja.SwingFramework.*;
import anja.SwingFramework.graph.*;
import anja.geom.*;

import static anja.graph.Constants.*;

public class GraphDebugScene extends JGraphScene
{
    //private static final int            _NUM_VERTICES  = 10;
    //private static final int            _NUM_EDGES     = 10;
    //private static final double         _GRAPH_EXTENTS = 300;

    int _vertices = 0;
	int _edges = 0;
    
   
    public GraphDebugScene(JSystemHub hub)
    {
        super(hub);
        Vertex.enableProxyZones(true);
    }

	public Vertex addVertex(Point2D p)
    {
    	Vertex v = super.addVertex(p);
		v.setLabel("v"+Integer.toString(++_vertices));
		return v;
    }

	public Edge addEdge(Vertex v1, Vertex v2)
	{
		Edge e = super.addEdge(v1, v2);
		e.setLabel("e"+Integer.toString(++_edges));
		return e;
	}

	public void removeVertex(Vertex vx)
	{
		super.removeVertex(vx);
	}

	public void removeEdge(Edge edge)
	{
		super.removeEdge(edge);
	}

    public void updateAffineTransform(AffineTransform tx, 
                                      double pixelSize, 
                                      Font invertedFont)
    {
        super.updateAffineTransform(tx, pixelSize, invertedFont);
    }
    
    public void draw(Graphics2D g)
    {
		JTextMessageDump msg = _hub.getTextDump();
		msg.clear();
		
        super.draw(g); 
		//m_Graph.draw(g,1.0 / _pixelSize, _invertedFont);
        
		m_Graph.drawSchematic(g, _pixelSize, _invertedFont, _hub.getCoordinateSystem().getWorldBoundingBox());	
		
		Iterator<Edge> ae = m_Graph.getAllEdges();
		while (ae.hasNext()) {
			Edge e = ae.next();
			System.out.println(e.getLabel()+":  "+e.getSource().getLabel()+" -> "+e.getTarget().getLabel());
		}
		System.out.println("");
		Iterator<Vertex> av = m_Graph.getAllVertices();
		while (av.hasNext()) {
			Vertex v = av.next();
			ae = v.getEdgeIterator(CLOCKWISE);
			System.out.print(v.getLabel()+":  ");
			while (ae.hasNext()) {
				Edge e = ae.next();
				System.out.print(e.getLabel()+" -> ");
			}
			System.out.println(";");
		}
		
		System.out.println("");
		Iterator<Vertex> vt = m_Graph.getBDFSVertexIterator(false);
		if (vt == null) return;
		Vertex v = m_Graph.getAnyVertex();
		if (v != null) {
			System.out.println(v.getLabel());
			while (vt.hasNext()) {
				v = vt.next();
				System.out.print(" -> ");
				System.out.println(v.getLabel());
			}
		}
		System.out.println("");
		
		Iterator<Edge> et = m_Graph.getBDFSEdgeIterator(false);
		if (et == null) return;
		while (et.hasNext()) {
			System.out.println(et.next().toString());
		}
		
    }
}



