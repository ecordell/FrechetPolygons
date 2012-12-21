package anja.geom.triangulation;


import anja.geom.triangulation.pslgtriang.*;
import anja.geom.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.Iterator;
import anja.util.GraphicsContext;


/**
 * Berechnet die Triangulation eines Polygons mittels des pslgtriang-package.
 * Methoden verhalten sich ähnlich zu denen der Seideltriangulierung.
 */
public class PSLGTriangulation
		extends Triangulation
{

	private ConvexPolygon2[]	_triangulation;
	private Segment2[]			_diagonals;


	/**
	 * Der Konstruktor berechnet direkt die eigentliche Triangulierung.
	 * 
	 * @param poly
	 *            Das zu triangulierende Polygon
	 */
	public PSLGTriangulation(
			Polygon2 poly)
	{
		PointsAccess pa = new PointsAccess(poly);
		Point2 pl = null;
		String s = "p";
		Triangulator triangulator = new Triangulator();
		triangulator.reset();
		while (pa.hasNextPoint())
		{
			Point2 p = pa.nextPoint();
			if (pl == null)
			{
				pl = p;
			}
			else
			{
				triangulator.addEdge(new Vertex(pl.getX(), pl.getY()),
						new Vertex(p.getX(), p.getY()), Color.black, s);
				pl = p;
			}
		}
		triangulator.addEdge(new Vertex(poly.lastPoint().getX(), poly
				.lastPoint().getY()), new Vertex(poly.firstPoint().getX(), poly
				.firstPoint().getY()), Color.black, s);
		triangulator.computeIntermediateArea();
		HashSet<anja.geom.triangulation.pslgtriang.Triangle> triangles = triangulator
				.getTriangles();
		_triangulation = new ConvexPolygon2[triangles.size()];
		Iterator<anja.geom.triangulation.pslgtriang.Triangle> itt = triangles
				.iterator();
		int count = -1;
		while (itt.hasNext())
		{
			anja.geom.triangulation.pslgtriang.Triangle t = itt.next();
			anja.geom.triangulation.pslgtriang.Vertex[] vertices = t.vertices;
			count++;
			_triangulation[count] = new ConvexPolygon2();
			for (int i = 0; i < vertices.length; i++)
				_triangulation[count].addPoint(vertices[i].x, vertices[i].y);
		}

		//Calculation of the diagonals
		Vector<anja.geom.triangulation.pslgtriang.Edge> edges = triangulator
				.getEdges();
		_diagonals = new Segment2[edges.size()];
		for (int i = 0; i < edges.size(); i++)
		{
			anja.geom.triangulation.pslgtriang.Edge e = edges.get(i);
			Point2 p = new Point2(e.v1.x, e.v1.y);
			Point2 p2 = new Point2(e.v2.x, e.v2.y);
			_diagonals[i] = new Segment2(p, p2);
		}
	}


	/**
	 * Erzeugt eine Triangulation von den 2 Polygonen
	 * 
	 * @param poly1
	 *            Polygon 1
	 * @param poly2
	 *            Polygon 2
	 */
	public PSLGTriangulation(
			Polygon2 poly1,
			Polygon2 poly2)
	{
		Triangulator triangulator = new Triangulator();
		triangulator.reset();

		Point2 pl = null;
		String s = "p1";
		PointsAccess pa = new PointsAccess(poly1);
		while (pa.hasNextPoint())
		{
			Point2 p = pa.nextPoint();
			if (pl == null)
			{
				pl = p;
			}
			else
			{
				triangulator.addEdge(new Vertex(pl.getX(), pl.getY()),
						new Vertex(p.getX(), p.getY()), Color.black, s);
				pl = p;
			}
		}
		if (poly1.isClosed())
			triangulator.addEdge(new Vertex(poly1.lastPoint().getX(), poly1
					.lastPoint().getY()), new Vertex(poly1.firstPoint().getX(),
					poly1.firstPoint().getY()), Color.black, s);

		s = "p2";
		pl = null;
		pa = new PointsAccess(poly2);
		while (pa.hasNextPoint())
		{
			Point2 p = pa.nextPoint();
			if (pl == null)
			{
				pl = p;
			}
			else
			{
				triangulator.addEdge(new Vertex(pl.getX(), pl.getY()),
						new Vertex(p.getX(), p.getY()), Color.black, s);
				pl = p;
			}
		}
		if (poly2.isClosed())
			triangulator.addEdge(new Vertex(poly2.lastPoint().getX(), poly2
					.lastPoint().getY()), new Vertex(poly2.firstPoint().getX(),
					poly2.firstPoint().getY()), Color.black, s);

		triangulator.computeIntermediateArea();
		HashSet<anja.geom.triangulation.pslgtriang.Triangle> triangles = triangulator
				.getTriangles();
		_triangulation = new ConvexPolygon2[triangles.size()];
		Iterator<anja.geom.triangulation.pslgtriang.Triangle> itt = triangles
				.iterator();
		int count = -1;
		while (itt.hasNext())
		{
			anja.geom.triangulation.pslgtriang.Triangle t = itt.next();
			anja.geom.triangulation.pslgtriang.Vertex[] vertices = t.vertices;
			count++;
			_triangulation[count] = new ConvexPolygon2();
			for (int i = 0; i < vertices.length; i++)
				_triangulation[count].addPoint(vertices[i].x, vertices[i].y);
		}

		Vector<anja.geom.triangulation.pslgtriang.Edge> edges = triangulator
				.getEdges();
		_diagonals = new Segment2[edges.size()];
		for (int i = 0; i < edges.size(); i++)
		{
			anja.geom.triangulation.pslgtriang.Edge e = edges.get(i);
			Point2 p = new Point2(e.v1.x, e.v1.y);
			Point2 p2 = new Point2(e.v2.x, e.v2.y);
			_diagonals[i] = new Segment2(p, p2);
		}
	}


	/**
	 * Liefert die Dreiecke zurueck in die das Polygon zerlegt wurde.
	 * 
	 * @return ConvexPolygon2[] enthaelt die Dreiecke
	 */
	public ConvexPolygon2[] getTriangles()
	{
		return _triangulation;
	}


	/**
	 * Zeichnet die Diagonalen der Triangulierung
	 * 
	 * @param g2d
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * 
	 * @see #draw(Graphics2D, GraphicsContext)
	 */
	public void draw(
			Graphics2D g2d)
	{
		GraphicsContext gc = new GraphicsContext();
		gc.setForegroundColor(Color.red);
		draw(g2d, gc);
	}


	/**
	 * Zeichnet die Diagonalen der Triangulierung in das Graphics2D-Objekt ein
	 * 
	 * @param g2d
	 *            Das Graphics2D-Objekt
	 * @param gc
	 *            Der GraphicsContext, mit den Angaben zur Formatierung
	 */
	public void draw(
			Graphics2D g2d,
			GraphicsContext gc)
	{
		for (int i = 0; i < _diagonals.length; i++)
		{
			_diagonals[i].draw(g2d, gc);
		}
	}

}
