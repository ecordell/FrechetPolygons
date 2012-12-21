package anja.graph.algorithms;

import anja.graph.*;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Vector;
import java.util.Iterator;

import static anja.graph.Constants.*;

/**
 * This Class implements the Dijkstra algorithm, e.g. it finds the 
 * length of the shortest paths from a given start vertex to all
 * other vertices in a graph.
 * It will only calculate the length of the path, not the path itself.
 * The length will be stored as the vertices' weight. If a vertex can't be
 * reached it's weight will be -1.
 * 
 * @author  Jörg Wegener
 * @version 1.0
 */


public class Dijkstra implements Comparator
{
	public int compare(Object vertex1, Object vertex2)
	{
		Vertex v1 = (Vertex)vertex1;
		Vertex v2 = (Vertex)vertex2;
		if (v1.getWeight() < 0 && v2.getWeight() < 0) return 0;
		if (v1.getWeight() == v2.getWeight()) return 0; 
		if (v1.getWeight() < 0)	return 1;
		if (v1.getWeight() < v2.getWeight() || v2.getWeight() < 0) return -1;
		else return 1;
	}


	/** 
	 * Dijkstra algorithm which finds the shortest path via the edges' weight.
	 * 
	 * @param g	The Graph
	 * @param s	Start Vertex
	 */

	static public void dijkstraWeight(Graph g, Vertex s)
	{
		PriorityQueue<Vertex> pq = new PriorityQueue<Vertex>(g.getNumVertices(), new Dijkstra());
		Iterator<Vertex> vertexIterator = g.getAllVertices();
		while (vertexIterator.hasNext())
		{
			Vertex v = vertexIterator.next();
			v.setWeight(-1);
			pq.add(v);
		}
		pq.remove(s);
		s.setWeight(0);
		pq.add(s);
		while (!pq.isEmpty())
		{
			Vertex v = pq.poll();
			if (v.getWeight() == -1) return;
			Iterator<Edge> edgeIterator = v.getEdgeIterator(CLOCKWISE);
			while (edgeIterator.hasNext())
			{
				Edge e = edgeIterator.next();
				if (e.getSource() == v || !g.isDirected())
				{
					Vertex target;
					if (e.getSource() == v) target = e.getTarget();
					else target = e.getSource();
					if (target.getWeight() == -1 || (target.getWeight() > (v.getWeight() + e.getWeight())))
					{
						boolean b = pq.remove(target);
						target.setWeight(v.getWeight() + e.getLength());
						pq.add(target);
					}
				}
			}
		}
	}


	/** 
	 * Dijkstra algorithm which finds the shortest path via the edges' length.
	 * @param g	The Graph
	 * @param s	Start Vertex
	 */

	static public void dijkstraLength(Graph g, Vertex s) {
		PriorityQueue<Vertex> pq = new PriorityQueue<Vertex>(g.getNumVertices(), new Dijkstra());
		Iterator<Vertex> vertexIterator = g.getAllVertices();
		while (vertexIterator.hasNext()) {
			Vertex v = vertexIterator.next();
			v.setWeight(-1);
			pq.add(v);
		}
		pq.remove(s);
		s.setWeight(0);
		pq.add(s);
		while (!pq.isEmpty()) {
			Vertex v = pq.poll();
			if (v.getWeight() == -1) return;
			Iterator<Edge> edgeIterator = v.getEdgeIterator(CLOCKWISE);
			while (edgeIterator.hasNext()) {
				Edge e = edgeIterator.next();
				if (e.getSource() == v || !g.isDirected()) {
					Vertex target;
					if (e.getSource() == v) target = e.getTarget();
					else target = e.getSource();
					if (target.getWeight() == -1 || (target.getWeight() > (v.getWeight() + e.getLength()))) {
						boolean b = pq.remove(target);
						target.setWeight(v.getWeight() + e.getLength());
						pq.add(target);
					}
				}
			}
		}
	}
	
	
	static public void dijkstraLengthWithPaths(Graph g, Vertex s) {
		PriorityQueue<Vertex> pq = new PriorityQueue<Vertex>(g.getNumVertices(), new Dijkstra());
		Iterator<Vertex> vertexIterator = g.getAllVertices();
		while (vertexIterator.hasNext()) {
			Vertex v = vertexIterator.next();
			v.setWeight(-1);
			pq.add(v);
		}
		pq.remove(s);
		s.setWeight(0);
		pq.add(s);
		while (!pq.isEmpty()) {
			Vertex v = pq.poll();
			if (v.getWeight() == -1) return;
			Iterator<Edge> edgeIterator = v.getEdgeIterator(CLOCKWISE);
			while (edgeIterator.hasNext()) {
				Edge e = edgeIterator.next();
				if (e.getSource() == v || !g.isDirected()) {
					Vertex target;
					if (e.getSource() == v) target = e.getTarget();
					else target = e.getSource();
					if (target.getWeight() == -1 || (target.getWeight() > (v.getWeight() + e.getLength()))) {
						boolean b = pq.remove(target);
						target.setWeight(v.getWeight() + e.getLength());
						pq.add(target);
						Vector<Vertex> newPath = v.getPath();
						if (newPath == null) newPath = new Vector<Vertex>();
						else newPath = (Vector<Vertex>)newPath.clone();
						newPath.add(v);
						target.setPath(newPath);
					}
				}
			}
		}
	}
}
