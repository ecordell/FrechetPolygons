package anja.geom;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import java.awt.geom.GeneralPath;

import anja.geom.triangulation.SeidelTriangulation;

import anja.util.Drawable;
import anja.util.GraphicsContext;
import anja.util.ListItem;
import anja.util.Savable;
import anja.util.SimpleList;


/**
 * Zweidimensionales zeichenbares Polygon. Mit <tt>setOpen()</tt> und <tt>
 * setClosed()</tt> setzt man den Zustand des Polygons auf offen oder
 * geschlossen, das wirkt sich z.B. auf <tt>draw()</tt> und <tt>
 * intersection()</tt> aus. Der Defaultwert ist geschlossen. Abfragen kann man
 * den Zustand mit <tt>isOpen()</tt> und <tt>isClosed()</tt>.
 * 
 * @version 1.4 15.12.1997
 * @author Norbert Selle
 */

public class Polygon2
		extends Point2List
		implements Drawable, Savable
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	/** Links herum orientieren. Gegen den Uhrzeigersinn. */
	public final static byte	ORIENTATION_LEFT	= 1;

	/** Rechts herum orientieren. Mit dem Uhrzeigersinn. */
	public final static byte	ORIENTATION_RIGHT	= 2;

	/**
	 * Rückgabekonstante für locatePoint.
	 * @see #locatePoint
	 */
	public static final byte	POINT_INSIDE		= 1;

	/**
	 * Rückgabekonstante für locatePoint.
	 * @see #locatePoint
	 */
	public static final byte	POINT_ON_EDGE		= 2;

	/**
	 * Rückgabekonstante für locatePoint.
	 * @see #locatePoint
	 */
	public static final byte	POINT_OUTSIDE		= 3;

	// ************************************************************************
	// Variables
	// ************************************************************************

	private byte				_userflag;

	/** Flagge ob das Polygon offen ist */
	private boolean				_is_open;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Initialisierung eines neuen Polygons.
	 */
	private void _init()
	{
		_is_open = false;
		setUserflag((byte) 0);

	} // _init


	// ********************************

	/**
	 * Erzeugt ein leeres Polygon.
	 */
	public Polygon2()
	{
		super();
		_init();

	} // Polygon2


	// ********************************

	/**
	 * Erzeugt ein Polygon aus einem DataInputStream.
	 * 
	 * @param dis
	 *            Der InputStream
	 */
	public Polygon2(
			DataInputStream dis)
	{
		super();
		_init();

		try
		{
			String type = dis.readUTF();

			if (type.compareTo("Polygon2") != 0)
				return;

			int n = dis.readInt();

			_is_open = dis.readBoolean();

			_userflag = dis.readByte();

			for (int i = 0; i < n; i++)
				addPoint(new Point2(dis));
		}
		catch (IOException ex)
		{}
	} // Polygon2


	// ********************************

	/**
	 * Erzeugt ein Polygon aus der eingegebenen Punktliste, die Punkte des
	 * Polygons sind <B>Kopien</B> - nicht etwa Referenzen - der Punkte der
	 * Eingabeliste.
	 * 
	 * @param input_points
	 *            Die Eingabeliste
	 */
	public Polygon2(
			Point2List input_points)
	{
		super(input_points);
		_init();

	} // Polygon2


	// ********************************

	/**
	 * Erzeugt ein neues Polygon als Kopie des Eingabepolygons, die Punkte des
	 * neuen Polygons sind <B>Kopien</B> - nicht etwa Referenzen - der Punkte
	 * des Eingabepolygons.
	 * 
	 * @param input_polygon
	 *            Das Eingabepolygon
	 */
	public Polygon2(
			Polygon2 input_polygon)
	{
		super((Point2List) input_polygon);
		_is_open = input_polygon._is_open;
		setUserflag(input_polygon.getUserflag());
	} // Polygon2


	// ********************************

	/**
	 * Erzeugt ein neues Polygon als skalierte Kopie des Eingabepolygons. Die
	 * Skalierung mit dem Faktor s erfolgt bezueglich des eingegebenen Punktes
	 * p.
	 * 
	 * @param s
	 *            Der Skalierungsfaktor
	 * @param input_polygon
	 *            Das Eingabepolygon
	 * @param p
	 *            Der Referenzpunkt
	 */
	public Polygon2(
			double s,
			Polygon2 input_polygon,
			Point2 p)
	{
		super(s, input_polygon, p);
		_is_open = input_polygon._is_open;
		setUserflag(input_polygon.getUserflag());
	} // Polygon2


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Kopiert das Polygon, die Punkte der Kopie sind <B>Kopien</B> - nicht etwa
	 * Referenzen - der Punkte des Originals.
	 * 
	 * @return die Kopie des Objekts
	 */
	public Object clone()
	{
		return (new Polygon2(this));

	} // clone


	// ************************************************************************

	/**
	 * Gibt den Punkt des Polygonrands zurück, der dem Eingabepunkt am nächsten
	 * liegt, sowie den Index der geschnittenen Kante.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return Der nächste Punkt und Index der geschnittenen Kante
	 */
	public Point2 closestPoint(
			Point2 input_point)
	{
		if (length() == 0)
			return (null);
		if (length() == 1)
			return (firstPoint());

		double min_distance = 0;
		Point2 min_point = null;
		Segment2 edges[] = edges();

		for (int i = 0; i < edges.length; i++)
		{
			Segment2 segment = edges[i];
			Point2 point = segment.closestPoint(input_point);
			double distance = point.squareDistance(input_point);

			if ((min_point == null) || (distance < min_distance))
			{
				min_point = point;
				min_point.setValue(i);
				min_distance = distance;
			} // if
		} // for

		return (min_point);

	} // closestPoint


	// ************************************************************************

	/**
	 * Zeichnet das Polygon. Vererbt von {@link anja.util.Drawable}
	 * 
	 * @param g
	 *            Das Graphics-Objekt, in das gezeichnet wird
	 * @param gc
	 *            Der dazugehörige GraphicsContext
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		if (_points.length() == 0)
			return;

		if (_points.length() == 1)
		{
			firstPoint().draw(g, gc);
		}
		else if (_is_open) // !!NS verallgemeinern
		{
			//TODO: Check if clipping here is necessary
			_drawClipped(g, gc);
		}
		else
		{
			PointsAccess draw_points;

			g.setStroke(gc.getStroke());
			draw_points = new PointsAccess(_points);

			GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

			int npoints = 0;

			Point2 pt = draw_points.nextPoint();
			if (pt != null)
			{
				path.moveTo(pt.x, pt.y);
				npoints = 1;
			}

			while (draw_points.hasNextPoint())
			{
				pt = draw_points.nextPoint();
				npoints++;

				path.lineTo(pt.x, pt.y);
			}

			if (!_is_open)
			{
				path.closePath();
			} // if

			if (gc.getFillStyle() != 0 && npoints >= 3)
			{
				g.setColor(gc.getFillColor());
				g.fill(path);
			}

			g.setColor(gc.getForegroundColor());
			g.draw(path);
		} // if

	} // draw


	// ************************************************************************

	// Erweiterungen um ein Polygon2 mit änderbaren Farben für die Kanten und die Eckpunkte zu zeichnen.
	// Zusätzlich kann ausgewählt werden iwe dick die Kanten und die Eckpunkte gezeichnet werden sollen.
	// Author: Pit Prüßner<br>   

	/**
	 * Zeichnet ein Polygon in den angegeben Farben
	 * 
	 * @param g2d
	 *            Das Graphics-Object, in das gezeichnet wird
	 * @param color_Points
	 *            Farbe der Eckpunkte
	 * @param color_Edges
	 *            Farbe der Kanten
	 */
	public void paintPolygon(
			Graphics2D g2d,
			Color color_Points,
			Color color_Edges)
	{
		paintPolygon(g2d, true, 2, 4, 4, color_Points, color_Edges, false,
				Color.gray);
	}


	/**
	 * Zeichnet ein Polygon in den angegeben Farben
	 * 
	 * @param g2d
	 *            Das Graphics-Object, in das gezeichnet wird
	 * @param line_width
	 *            Breite der Kanten
	 * @param color_Edges
	 *            Farbe der Kanten
	 */
	public void paintPolygon(
			Graphics2D g2d,
			int line_width,
			Color color_Edges)
	{
		paintPolygon(g2d, false, line_width, 1, 1, Color.gray, color_Edges,
				false, Color.gray);
	}


	/**
	 * Zeichnet ein Polygon in den angegeben Farben
	 * 
	 * @param g2d
	 *            Das Graphics-Object, in das gezeichnet wird
	 * @param line_width
	 *            Breite der Kanten
	 * @param vertex_size
	 *            Größe der inneren Eckpunkte
	 * @param end_vertex_size
	 *            Größe der äußeren Eckpunkte
	 * @param color_Points
	 *            Farbe der Eckpunkte
	 * @param color_Edges
	 *            Farbe der Kanten
	 */
	public void paintPolygon(
			Graphics2D g2d,
			int line_width,
			int vertex_size,
			int end_vertex_size,
			Color color_Points,
			Color color_Edges)
	{
		paintPolygon(g2d, true, line_width, vertex_size, end_vertex_size,
				color_Points, color_Edges, false, Color.gray);
	}


	/**
	 * Zeichnet ein Polygon in den angegeben Farben
	 * 
	 * @param g2d
	 *            Das Graphics-Object, in das gezeichnet wird
	 * @param line_width
	 *            Breite der Kanten
	 * @param vertex_size
	 *            Größe der inneren Eckpunkte
	 * @param end_vertex_size
	 *            Größe der äußeren Eckpunkte
	 * @param color_Points
	 *            Farbe der Eckpunkte
	 * @param color_Edges
	 *            Farbe der Kanten
	 * @param color_Polygon
	 *            Farbe des Polygons
	 */
	public void paintPolygon(
			Graphics2D g2d,
			int line_width,
			int vertex_size,
			int end_vertex_size,
			Color color_Points,
			Color color_Edges,
			Color color_Polygon)
	{
		paintPolygon(g2d, true, line_width, vertex_size, end_vertex_size,
				color_Points, color_Edges, true, color_Polygon);
	}


	/**
	 * Zeichnet ein Polygon in den angegeben Farben
	 * 
	 * @param g2d
	 *            Das Graphics-Object, in das gezeichnet wird
	 * @param show_vertices
	 *            Sollen die Eckpunkte gezeichnet werden?
	 * @param line_width
	 *            Breite der Kanten
	 * @param vertex_size
	 *            Größe der inneren Eckpunkte
	 * @param end_vertex_size
	 *            Größe der äußeren Eckpunkte
	 * @param color_Points
	 *            Farbe der Eckpunkte
	 * @param color_Edges
	 *            Farbe der Kanten
	 * @param color_Polygon
	 *            Farbe mit der das Polygon gefüllt werden soll
	 */
	public void paintPolygon(
			Graphics2D g2d,
			boolean show_vertices,
			int line_width,
			int vertex_size,
			int end_vertex_size,
			Color color_Points,
			Color color_Edges,
			Color color_Polygon)
	{
		paintPolygon(g2d, show_vertices, line_width, vertex_size,
				end_vertex_size, color_Points, color_Edges, true, color_Polygon);
	}


	/**
	 * Zeichnet ein Polygon in den angegeben Farben
	 * 
	 * @param g2d
	 *            Das Graphics-Object, in das gezeichnet wird
	 * @param show_vertices
	 *            Sollen die Eckpunkte gezeichnet werden?
	 * @param line_width
	 *            Breite der Kanten
	 * @param vertex_size
	 *            Größe der inneren Eckpunkte
	 * @param end_vertex_size
	 *            Größe der äußeren Eckpunkte
	 * @param color_Points
	 *            Farbe der Eckpunkte
	 * @param color_Edges
	 *            Farbe der Kanten
	 * @param filled
	 *            Soll das Polygon gefüllt werden?
	 * @param color_Polygon
	 *            Farbe mit der das Polygon gefüllt werden soll
	 */
	public void paintPolygon(
			Graphics2D g2d,
			boolean show_vertices,
			int line_width,
			int vertex_size,
			int end_vertex_size,
			Color color_Points,
			Color color_Edges,
			boolean filled,
			Color color_Polygon)
	{
		GraphicsContext gc_points = new GraphicsContext();
		gc_points.setLineWidth(vertex_size);
		gc_points.setForegroundColor(color_Points);
		gc_points.setEndCap(BasicStroke.CAP_SQUARE);

		GraphicsContext gc_endpoints = new GraphicsContext();
		gc_endpoints.setLineWidth(end_vertex_size);
		gc_endpoints.setForegroundColor(color_Points);
		gc_endpoints.setEndCap(BasicStroke.CAP_ROUND);

		GraphicsContext gc_polygon = new GraphicsContext();
		gc_polygon.setLineWidth(line_width);
		gc_polygon.setForegroundColor(color_Edges);

		if (filled)
		{
			gc_polygon.setFillColor(color_Polygon);
			gc_polygon.setFillStyle(1);
		}
		else
			gc_polygon.setFillStyle(0);

		Polygon2 poly = this;

		GraphicsContext gc = new GraphicsContext();
		gc.setForegroundColor(color_Edges);
		poly.draw(g2d, gc);
		if (poly.length() > 1)
			poly.draw(g2d, gc_polygon);
		else
			poly.firstPoint().draw(g2d, gc_endpoints);

		SimpleList points = poly.points();

		for (ListItem item = points.first(); item != null; item = points
				.next(item))
		{
			Point2 point = (Point2) points.value(item);
			if (show_vertices)
				if (poly.isOpen()
						&& (point == poly.lastPoint() || point == poly
								.firstPoint()))
					point.draw(g2d, gc_endpoints);
				else
					point.draw(g2d, gc_points);

		} // for
	} // paintPolygons


	// ************************************************************************

	/**
	 * Gibt die Anzahl der Kanten zurück, zwei bei einem geschlossenem Polygon
	 * mit zwei Punkten.
	 * 
	 * @return Die Anzahl der Kanten
	 */
	public int edgeNumber()
	{
		if (_points.length() <= 1)
		{
			return (0);
		} // if

		if (_is_open)
		{
			return (_points.length() - 1);
		}
		else
		{
			return (_points.length());
		} // if

	} // edgeNumber


	// ************************************************************************

	/**
	 * Gibt ein Array der Kanten zurück.
	 * 
	 * @return Die Kanten als Array
	 */
	public Segment2[] edges()
	{
		int edge_number = edgeNumber();
		Segment2 output_edges[] = new Segment2[edge_number];

		if (edge_number > 0)
		{
			PointsAccess access = new PointsAccess(this);
			Point2 current_point;
			Point2 next_point;

			current_point = access.nextPoint();
			for (int index = 0; index < edge_number; index++)
			{
				next_point = access.cyclicNextPoint();
				output_edges[index] = new Segment2(current_point, next_point);
				current_point = next_point;
			} // for
		} // if

		return (output_edges);

	} // edges


	// **********************************************************************

	/**
	 * The difference between the polygon itself and the convex hull of the
	 * polygon.
	 * 
	 * This is defined by multiple (closed) polygons, that represent the left
	 * over, if you cut the polygon out of its convex hull. The method also
	 * works with open polygons, but interprets them as closed ones. The method
	 * works in O(n) time.
	 * 
	 * @return Multiple polygons or null, if the polygon itself is convex (or
	 *         has 2 or less points).
	 */
	public Polygon2[] getDifferenceToConvexHull()
	{
		if (this.length() <= 2 || this.isConvex())
		{
			return null;
		}

		ArrayList<Polygon2> theList = new ArrayList<Polygon2>();
		ConvexPolygon2 conv_poly = new ConvexPolygon2(this.getConvexHull());

		conv_poly.setOrientation(this.getOrientation());

		Point2[] conv = conv_poly.toArray();
		Point2[] poly = this.toArray();

		int current_poly = -1;

		for (int i = 0; current_poly == -1 && i < poly.length; ++i)
		{
			if (conv[0].equals(poly[i]))
			{
				current_poly = i;
			}
		}

		int current_conv = 0;
		boolean breakpoint = false;
		do
		{
			if (conv[current_conv].equals(poly[current_poly]))
			{
				current_poly = (current_poly + 1) % poly.length;
				current_conv = (current_conv + 1) % conv.length;
			}
			else
			{
				Polygon2 new_poly = new Polygon2();
				new_poly.setClosed();
				int last = (current_conv - 1 + conv.length) % conv.length;
				new_poly.addPoint(conv[last]);

				while (!conv[current_conv].equals(poly[current_poly]))
				{
					new_poly.addPoint(poly[current_poly]);
					current_poly = (current_poly + 1) % poly.length;
				}

				new_poly.addPoint(conv[current_conv]);
				theList.add(new_poly);

			}

			if (current_conv == 2)
			{
				breakpoint = true;
			}
		}
		while (current_conv != 1 || !breakpoint);

		return theList.toArray(new Polygon2[1]);
	}


	// **********************************************************************

	/**
	 * Calculates the shortest path from start to target inside a simple
	 * polygon. Calculates the shortest path from a start to a destination
	 * inside a simple polygon according to the Funnel algorithm invented by Lee
	 * and Preparata in 1984 (<i>D. T. Lee and F. P. Preparata: Euclidean
	 * shortest paths in the presence of rectilinear barriers. Networks,
	 * 14:393-410, 1984.)</i>.<br> As redundant points have to be removed for a
	 * useful triangulation, it works on a copy of the original polygon.<br><br>
	 * 
	 * by Anja Haupts
	 * 
	 * @return null, if the polygon is not closed or if start or target are not
	 *         inside the polygon; the shortest path as Segment[] otherwise
	 * 
	 * @param start
	 *            the Point2 inside the simple Polygon2 where the path should
	 *            start
	 * @param target
	 *            the Point2 inside the simple Polygon2 where the path should
	 *            end
	 * 
	 * @see anja.geom.Funnel
	 * @see anja.geom.DualGraph
	 */
	public Segment2[] getShortestPath(
			Point2 start,
			Point2 target)
	{
		Polygon2 poly = new Polygon2(this);

		Segment2[] shortest_path = null;

		// return null if start or target are not inside the polygon
		// or if the polygon is not closed
		if (!poly.isSimple() || poly.locatePoint(start) == POINT_OUTSIDE
				|| poly.locatePoint(target) == POINT_OUTSIDE)
			return null;

		poly.removeRedundantPoints();

		ConvexPolygon2[] triangs = poly.getTriangulation();

		int start_tria = -1;
		int target_tria = -1;

		for (int i = 0; i < triangs.length
				&& (start_tria == -1 || target_tria == -1); i++)
		{
			if (start_tria == -1 && triangs[i].inTriangle(start, true))
				start_tria = i;
			if (target_tria == -1 && triangs[i].inTriangle(target, true))
				target_tria = i;
		}// for (int i = 0; i < triangs.length; i++)

		DualGraph dg = new DualGraph(triangs);
		Segment2[] diagonalsDFS = dg.get_diagonals_by_DFS(start_tria,
				target_tria);

		if (diagonalsDFS == null)
		{
			Segment2 path = new Segment2(start, target);
			shortest_path = new Segment2[1];
			shortest_path[0] = path;
			return shortest_path;

		}// if(_diagonalsDFS == null)

		Funnel act_funnel = new Funnel(start, diagonalsDFS.length + 2);

		shortest_path = act_funnel.getShortestPath(diagonalsDFS, target);

		return shortest_path;

	}// public Segment2[] getShortestPath(Point2 start, Point2 target)


	// ************************************************************************
	
	
	/**
	 * Returns the area of the polygon
	 * 
	 * @return The area, real number >=0
	 */
	public double getArea()
	{
		if (this.isOpen())
			return 0.0;

		double area = 0;
		
		double miny = 0;
		Point2[] points = this.toArray();
		for (Point2 p: points)
		{
			if (miny > p.y)
				miny = p.y;
		}
		miny -= 10;
		
		for (int i=1; i <= points.length; ++i)
		{
			Point2 p1 = points[i-1];
			Point2 p2 = points[i%points.length];
			
			if (p1.x < p2.x)
			{
				area -= ((p1.y+p2.y)/2.0 - miny) * (p2.x-p1.x);  
			}
			if (p1.x > p2.x)
			{
				area += ((p1.y+p2.y)/2.0 - miny) * (p1.x-p2.x);  
			}			
		}
		
		return (area < 0) ? -1*area : area;
	} //end area
	
	
	/**
	 * Calculates the shortest path from start to target inside a simple
	 * polygon. Calculates the shortest path from a start to a destination
	 * inside a simple polygon according to the Funnel algorithm invented by Lee
	 * and Preparata in 1984 (<i>D. T. Lee and F. P. Preparata: Euclidean
	 * shortest paths in the presence of rectilinear barriers. Networks,
	 * 14:393-410, 1984.)</i>.<br> As redundant points have to be removed for a
	 * useful triangulation, it works on a copy of the original polygon.<br><br>
	 * 
	 * <br>All checks, if the points are inside the polygon or if the polygon is simple
	 * have been deleted in this method. Therefor the calculation only takes O(n) time,
	 * when the DualGraph is an argument of this method.
	 * 
	 * by Andreas Lenerz
	 * 
	 * @return null, if the polygon is not closed or if start or target are not
	 *         inside the polygon; the shortest path as Segment[] otherwise
	 * 
	 * @param g The already calculated DualGraph
	 * @param triangs The triangulation of the polygon
	 * @param start
	 *            the Point2 inside the simple Polygon2 where the path should
	 *            start
	 * @param target
	 *            the Point2 inside the simple Polygon2 where the path should
	 *            end
	 * 
	 * @see anja.geom.Funnel
	 * @see anja.geom.DualGraph
	 */
	public Segment2[] getShortestPath(
			Point2 start,
			Point2 target,
			DualGraph g,
			ConvexPolygon2[] triangs			
	)
	{
		if (g == null || triangs == null)
			return null;
		
		Segment2[] shortest_path = null;

		int start_tria = -1;
		int target_tria = -1;

		for (int i = 0; i < triangs.length
				&& (start_tria == -1 || target_tria == -1); ++i)
		{
			if (start_tria == -1 && triangs[i].inTriangle(start, true))
				start_tria = i;
			if (target_tria == -1 && triangs[i].inTriangle(target, true))
				target_tria = i;
		}// end for

		Segment2[] diagonalsDFS = g.get_diagonals_by_DFS(start_tria,
				target_tria);

		if (diagonalsDFS == null)
		{
			Segment2 path = new Segment2(start, target);
			shortest_path = new Segment2[1];
			shortest_path[0] = path;
			return shortest_path;

		}// if(_diagonalsDFS == null)

		Funnel act_funnel = new Funnel(start, diagonalsDFS.length + 2);

		shortest_path = act_funnel.getShortestPath(diagonalsDFS, target);

		return shortest_path;
	}// public Segment2[] getShortestPath(Point2 start, Point2 target)


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge der Polygonkanten mit dem BasicCircle und gibt
	 * die Schnittmenge im Intersection-Parameter zurück, sie kann leer sein
	 * oder eine eine nichtleere Liste von Punkten enthalten.
	 * 
	 * @param input_basic
	 *            Das Kreisobjekt
	 * @param inout_intersection
	 *            Das Intersection-Objekt mit den Schnitten
	 */
	public void intersection(
			BasicCircle2 input_basic,
			Intersection inout_intersection)
	{
		if (_points.empty())
		{
			inout_intersection.set();
		}
		else
		{
			SimpleList intersection_list = new SimpleList();

			if (_points.length() == 1)
			{
				if (input_basic.liesOn(firstPoint()))
				{
					intersection_list.add(new Point2(firstPoint()));
				} // if
			}
			else
			// mindestens 2 Punkte
			{
				Intersection current_set = new Intersection();
				Segment2[] edges = edges();

				for (int index = 0; index < edgeNumber(); index++)
				{
					input_basic.intersection(edges[index], current_set);
					_addPoints(intersection_list, current_set);
				} // for
			} // if

			if (intersection_list.empty())
			{
				inout_intersection.set();
			}
			else
			{
				_removeDuplicates(intersection_list);
				inout_intersection.set(intersection_list);
			} // if
		} // if

	} // intersection


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge der Polygonkanten mit der Eingabelinie und
	 * gibt die Schnittmenge im Intersection-Parameter zurueck, sie kann leer
	 * sein oder eine eine nichtleere Liste von Punkten und/oder Segmenten
	 * enthalten.<br>Ist das Polygon einfach, so enthaelt die Schnittmenge keine
	 * Punkte, die andere Elemente der Schnittmenge schneiden.
	 * 
	 * @param input_basic
	 *            Das Linienobjekt
	 * @param inout_intersection
	 *            Das Intersection-Objekt mit den Schnitten
	 */
	public void intersection(
			BasicLine2 input_basic,
			Intersection inout_intersection)
	{
		if (_points.empty())
		{
			inout_intersection.set();
		}
		else
		{
			SimpleList intersection_list = new SimpleList();

			if (_points.length() == 1)
			{
				if (input_basic.liesOn(firstPoint()))
				{
					intersection_list.add(new Point2(firstPoint()));
				} // if
			}
			else
			{
				int number = _points.length();
				Intersection current_set = new Intersection();
				PointsAccess access = new PointsAccess(this);
				Point2 current_point;
				Point2 next_point;
				Segment2 current_segment;

				if (_is_open)
				{
					number--;
				} // if

				current_point = access.nextPoint();
				for (int index = 0; index < number; index++)
				{
					next_point = access.cyclicNextPoint();
					current_segment = new Segment2(current_point, next_point);
					input_basic.intersection(current_segment, current_set);
					_conjunction(intersection_list, current_set);
					current_point = next_point;
				} // for

				_checkFirstTouchLast(intersection_list);
			} // if

			if (intersection_list.empty())
			{
				inout_intersection.set();
			}
			else
			{
				inout_intersection.set(intersection_list);
			} // if
		} // if

	} // intersection


	/**
	 * Funktioniert wie intersection ( BasicLine2 input_basic , Intersection
	 * inout_intersection ) gibt aber bei Listenlänge==1 nicht result==LIST
	 * zurueck sondern POINT2 bzw. SEGMENT2
	 * 
	 * Verbesserung ! 24.09.2001
	 * 
	 * @param input_basic
	 *            Das Kreisobjekt
	 * @param inout_intersection
	 *            Das Intersection-Objekt mit den Schnitten
	 */

	public void intersectionSingle(
			BasicLine2 input_basic,
			Intersection inout_intersection)
	{
		Point2 singlePoint = null;
		Segment2 singleSegment = null;
		if (_points.empty())
		{
			inout_intersection.set();
		}
		else
		{
			SimpleList intersection_list = new SimpleList();

			if (_points.length() == 1)
			{
				if (input_basic.liesOn(firstPoint()))
				{
					intersection_list.add(new Point2(firstPoint()));
					singlePoint = new Point2(firstPoint());
				} // if
			}
			else
			{
				int number = _points.length();
				Intersection current_set = new Intersection();
				PointsAccess access = new PointsAccess(this);
				Point2 current_point;
				Point2 next_point;
				Segment2 current_segment;

				if (_is_open)
				{
					number--;
				} // if

				current_point = access.nextPoint();
				for (int index = 0; index < number; index++)
				{
					next_point = access.cyclicNextPoint();
					current_segment = new Segment2(current_point, next_point);
					input_basic.intersection(current_segment, current_set);
					if (current_set.result == Intersection.POINT2)
					{
						singlePoint = current_set.point2;
					}
					if (current_set.result == Intersection.SEGMENT2)
					{
						singleSegment = current_set.segment2;
					}
					_conjunction(intersection_list, current_set);
					current_point = next_point;
				} // for

				_checkFirstTouchLast(intersection_list);
			} // if

			if (intersection_list.empty())
			{
				inout_intersection.set();
			}
			else
			{
				if (intersection_list.length() == 1)
				{
					if (singleSegment != null)
					{
						inout_intersection.set(singleSegment);
						return;
					}
					if (singlePoint != null)
					{
						inout_intersection.set(singlePoint);
						return;
					}
				}
				inout_intersection.set(intersection_list);
			} // if
		} // if

	} // intersection


	// ************************************************************************

	/**
	 * Testet ob die Linie den Polygonrand schneidet.
	 * 
	 * @param input_basic
	 *            die Linie
	 * 
	 * @return true wenn ja, sonst false
	 */

	public boolean intersectsEdge(
			BasicLine2 input_basic)
	{
		if (_points.empty())
		{
			return (false);
		}

		if (_points.length() == 1)
		{
			return (input_basic.liesOn(firstPoint()));
		} // if

		int number = _points.length();
		Intersection current_set = new Intersection();
		PointsAccess access = new PointsAccess(this);
		Point2 current_point;
		Point2 next_point;
		Segment2 current_segment;

		if (_is_open)
		{
			number--;
		} // if

		current_point = access.nextPoint();
		for (int index = 0; index < number; index++)
		{
			next_point = access.cyclicNextPoint();
			current_segment = new Segment2(current_point, next_point);
			input_basic.intersection(current_segment, current_set);
			if (current_set.result != Intersection.EMPTY)
			{
				return (true);
			} // if
			current_point = next_point;
		} // for

		return (false);

	} // intersectsEdge


	// ************************************************************************

	/**
	 * Testet ob das Rechteck das Polygon schneidet, bei einem geschlossenem
	 * einfachem Polygon wird sowohl auf Schnitt mit der Fläche als auch auf
	 * Schnitt mit dem Rand getestet, bei allen anderen Polygonen nur auf
	 * Schnitt mit dem Rand. Vererbt von {@link anja.util.Drawable}.
	 * 
	 * @param input_box
	 *            Das Rechteck
	 * 
	 * @return true wenn ja, sonst false
	 * 
	 * @see anja.util.Drawable
	 */
	public boolean intersects(
			Rectangle2D input_box)
	{
		if (_points.empty())
			return (false);

		if (_points.length() == 1)
			return (input_box.contains(firstPoint().x, firstPoint().y));

		if (super.intersects(input_box))
			return (true); // mindestens eine Punkt liegt im Rechteck

		// Changed if-statement, please consider the note
		// at anja.geom.Rectangle2.intersectsOrTouches() !
		Rectangle2 rect = new Rectangle2(getBoundingRect());
		if (!rect.intersectsOrTouches(input_box))
			return (false); // das polygonumschliessende Rechteck
		// schneidet das Rechteck nicht

		Rectangle2 rect2 = new Rectangle2(input_box);

		if (intersectsEdge(rect2.top()) || intersectsEdge(rect2.bottom())
				|| intersectsEdge(rect2.left())
				|| intersectsEdge(rect2.right()))
			return (true); // das Rechteck schneidet den Rand

		if (_is_open)
			return (false); // offenes Polygon: fertig, kein Schnitt
		else
		{
			if (isSimple())
			{
				// geschlossenes einfaches Polygon; Schnitt wenn das Rechteck drin
				// liegt, es reicht eine Ecke zu testen

				return (locatePoint(new Point2(rect2.x, rect2.y)) != POINT_OUTSIDE);
			}
			else
				return (false); // kein einfaches Polygon: fertig, kein Schnitt
		} // if

	} // intersects


	// ************************************************************************

	/**
	 * Gibt true zurück wenn der Modus des Polygons geschlossen ist.
	 * 
	 * @return true, wenn geschlossen, false sonst
	 */
	public boolean isClosed()
	{
		return (!_is_open);

	} // isClosed


	// ************************************************************************

	/**
	 * Gibt true zurück wenn der Modus des Polygons offen ist.
	 * 
	 * @return true, wenn offen, false sonst
	 */
	public boolean isOpen()
	{
		return (_is_open);

	} // isOpen


	// ************************************************************************

	/**
	 * Setzt den Modus auf geschlossen.
	 */
	public void setClosed()
	{
		_is_open = false;

	} // setClosed


	// ************************************************************************

	/**
	 * Setzt den Modus auf offen.
	 */
	public void setOpen()
	{
		_is_open = true;

	} // setOpen


	// ************************************************************************

	/**
	 * Setzt ein Userflag in das Polygon
	 * 
	 * @param userflag
	 *            Das Flag
	 */
	public void setUserflag(
			byte userflag)
	{

		_userflag = userflag;
	} // setUserflag


	/**
	 * Gibt Userflag zurück
	 * 
	 * @return Byte mit Userflag
	 */
	public byte getUserflag()
	{
		return _userflag;
	}


	/**
	 * Testet ob das Polygon einfach ist, das heisst die folgenden Bedingungen
	 * erfuellt: <ul> <li> es ist geschlossen</li> <li> die Anzahl der Eckpunkte
	 * ist groesser als zwei</li> <li> es gibt keine uebereinanderliegenden
	 * Eckpunkte</li><li> Kanten schneiden sich nur an ihren Endpunkten
	 * </li></ul>
	 * 
	 * @return true, wenn das Polygon einfach ist, false sonst
	 */
	public boolean isSimple()
	{
		if (_is_open || (_points.length() <= 2))
		{
			return (false);
		} // if

		Intersection set = new Intersection();
		Segment2 primary;
		Segment2 secondary;
		Segment2 edges[] = edges();
		int max_index = edges.length - 1;

		for (int primary_index = 0; primary_index <= max_index; primary_index++)
		{
			primary = edges[primary_index];

			if (primary.source().equals(primary.target()))
				return (false); // uebereinanderliegende Eckpunkte

			if (primary_index == max_index)
				secondary = edges[0];
			else
				secondary = edges[primary_index + 1];

			primary.intersection(secondary, set);
			if (set.result != Intersection.POINT2)
				return (false); // Nachfolger wird nicht in genau
			// einem Punkt geschnitten

			for (int secondary_index = primary_index + 2; secondary_index <= max_index; secondary_index++)
			{
				secondary = edges[secondary_index];
				primary.intersection(secondary, set);

				if ((primary_index == 0) && (secondary_index == max_index))
				{
					if (set.result != Intersection.POINT2)
						return (false); // erste und letzte Kante schneiden
					// sich nicht in einem Punkt
				}
				else
				{
					if (set.result != Intersection.EMPTY)
						return (false); // Schnitt nicht aufeinanderfolgender
					// Kanten
				} // if
			} // for
		} // for

		return (true);

	} // isSimple


	// ************************************************************************

	/**
	 * Berechnet die Länge.
	 * 
	 * @return Die Länge des Polygons
	 */
	public double len()
	{
		int edge_number = edgeNumber();
		double result_len = 0;

		if (edge_number > 0)
		{
			PointsAccess access = new PointsAccess(this);
			Point2 current = access.nextPoint();
			Point2 next;

			for (int counter = 0; counter < edge_number; counter++)
			{
				next = access.cyclicNextPoint();
				result_len += current.distance(next);
				current = next;
			} // for
		} // if

		return (result_len);

	} // len


	// ************************************************************************

	/**
	 * Berechnet das Quadrat der minimalen Distanz zwischen Polygonrand und
	 * Eingabepunkt. Erzeugt eine NoSuchElementException bei einem Polygon mit
	 * null Punkten.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return Quadrat der minimalen Distanz zwischen Polygonrand und
	 *         Eingabepunkt
	 * 
	 * @throws java.util.NoSuchElementException
	 */
	public double squareDistance(
			Point2 input_point)
	{
		Point2 min_point = closestPoint(input_point);

		if (min_point == null)
		{
			throw new java.util.NoSuchElementException();
		}
		else
		{
			return (min_point.squareDistance(input_point));
		} // if

	} // squareDistance


	// ************************************************************************

	/**
	 * Berechnet die minimale Distanz zwischen Polygonrand und Eingabepunkt.
	 * Erzeugt eine NoSuchElementException bei einem Polygon mit null Punkten.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return minimale Distanz zwischen Polygonrand und Eingabepunkt
	 * 
	 * @throws java.util.NoSuchElementException
	 */
	public double distance(
			Point2 input_point)
	{
		double square_distance = 0;

		try
		{
			square_distance = squareDistance(input_point);
		}
		catch (java.util.NoSuchElementException ex)
		{
			throw ex;
		} // try

		return (Math.sqrt(square_distance));

	} // distance


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repräsentation.
	 * 
	 * @return Die textuelle Repräsentation
	 */
	public String toString()
	{
		return "Polygon2 <" + _points.length() + " | "
				+ (_is_open ? "open" : "closed") + "> [" + listPoints() + "]";

	} // toString


	// ************************************************************************

	/**
	 * Speichert das Objekt in der durch den OutputStream gegebenen Quelle
	 * 
	 * @param dio
	 *            Der OutputStream
	 */
	public void save(
			DataOutputStream dio)
	{
		try
		{
			dio.writeUTF("Polygon2");
			dio.writeInt(_points.length());
			dio.writeBoolean(_is_open);
			dio.writeByte(_userflag);
			PointsAccess ac = new PointsAccess(_points);

			while (ac.hasNextPoint())
				ac.nextPoint().save(dio);
		}
		catch (IOException ioex)
		{}
	}


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Hängt die nur Punkte enthaltende Menge an die Liste. Enthält die Menge
	 * eine Liste, so ist diese anschließend leer.
	 * 
	 * @param inout_list
	 *            die Liste
	 * @param inout_set
	 *            die Menge
	 */
	private void _addPoints(
			SimpleList inout_list,
			Intersection inout_set)
	{
		switch (inout_set.result)
		{
			case Intersection.EMPTY:
				break;
			case Intersection.POINT2:
				inout_list.add(inout_set.point2);
				break;
			case Intersection.LIST:
				inout_list.concat(inout_set.list);
				break;
			default:
				System.err.println("\nPolygon2._addPoints: unexpected case: "
						+ "\n  Polygon     : " + this + "\n  Intersection: "
						+ inout_set);
				break;
		} // switch

	} // _addPoints


	// ************************************************************************

	/**
	 * Entfernt das erste oder letzte Element der Eingabeliste, wenn es ein
	 * Punkt ist und das letzte bzw. erste Element der Eingabeliste berührt.
	 * 
	 * @param inout_list
	 *            Die Eingabeliste
	 */
	private void _checkFirstTouchLast(
			SimpleList inout_list)
	{
		if (inout_list.length() >= 2)
		{
			if (inout_list.lastValue() instanceof Point2)
			{
				if (_isTouching((Point2) inout_list.lastValue(), inout_list
						.firstValue()))
				{
					// letztes Element loeschen ( kleines p bei pop )
					inout_list.pop();
				} // if
			}
			else if (inout_list.firstValue() instanceof Point2)
			{
				if (_isTouching((Point2) inout_list.firstValue(), inout_list
						.lastValue()))
				{
					// erstes Element loeschen ( grosses P bei Pop )
					inout_list.Pop();
				} // if
			} // if
		} // if

	} // _checkFirstTouchLast


	// ************************************************************************

	/**
	 * Vereinigt die Liste und das Eingabeelement wie folgt: - Ist das
	 * Eingabeelement ein Punkt, so wird er an die Liste angehängt, wenn er das
	 * letzte Listenelement nicht schneidet - Ist das Eingabeelement ein Segment
	 * so wird es an die Liste angehängt, wenn das vorher letzte Listenelement
	 * ein das Segment schneidender Punkt ist, so wird der Punkt aus der Liste
	 * entfernt.
	 * 
	 * @param inout_elements
	 *            Liste von Segmenten und Punkten
	 * @param input_element
	 *            Leer oder ein Punkt oder ein Segment
	 */
	private void _conjunction(
			SimpleList inout_elements,
			Intersection input_element)
	{
		switch (input_element.result)
		{
			case Intersection.EMPTY:
				break;
			case Intersection.POINT2:
				if (inout_elements.empty())
				{
					inout_elements.add(input_element.point2);
				}
				else
				{
					if (!_isTouching(input_element.point2, inout_elements
							.lastValue()))
					{
						inout_elements.add(input_element.point2);
					} // if

				} // if
				break;
			case Intersection.SEGMENT2:
				if ((!inout_elements.empty())
						&& (inout_elements.lastValue() instanceof Point2)
						&& (input_element.segment2
								.liesOn((Point2) inout_elements.lastValue())))
				{
					inout_elements.pop();
				} // if
				inout_elements.add(input_element.segment2);
				break;
			default:
				System.err.println("Polygon2._conjunction: unexpected case: "
						+ "  Polygon " + this + "  Intersection "
						+ input_element);
				break;
		} // switch

	} // _conjunction


	// ************************************************************************

	/**
	 * Zeichnet das Objekt
	 * 
	 * @param g
	 *            Das Graphics-Objekt, in das gezeichnet wird
	 * @param gc
	 *            Der entsprechende GraphicsContext
	 */
	synchronized public void _drawClipped(
			Graphics2D g,
			GraphicsContext gc)
	{
		Rectangle2D clip_rectangle = BasicLine2.worldClipRectangle(g);

		if (clip_rectangle == null)
		{
			System.out
					.println("Warning: Polygon2.drawClipped(): clip rectangle null ");
			return;
		} // if

		GeneralPath path = null;
		PointsAccess access = new PointsAccess(points());
		Point2 actual = access.nextPoint();
		Point2 next = access.nextPoint();
		Segment2 clipped = null;

		g.setColor(gc.getForegroundColor());
		g.setStroke(gc.getStroke());

		for (int i = 0; i <= length() - 2; i++)
		{
			clipped = (new Segment2(actual, next)).clip(clip_rectangle);

			if (clipped == null)
			{
				if (path != null)
				{
					g.draw(path);
					path = null;
				} // if
			}
			else
			{
				if (path == null)
				{
					path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
					path.moveTo(clipped.source().x, clipped.source().y);
				}
				else
				{
					if (!clipped.source().equals(actual))
					{
						g.draw(path);
						path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
						path.moveTo(clipped.source().x, clipped.source().y);
					} // if
				} // if

				path.lineTo(clipped.target().x, clipped.target().y);

				if (!clipped.target().equals(next))
				{
					g.draw(path);
					path = null;
				} // if

			} // if

			actual = next;
			next = access.nextPoint();
		} // for

		if (path != null)
		{
			g.draw(path);
		} // if

	} // _drawClipped


	// ************************************************************************

	/**
	 * Testet ob der Eingabepunkt das Eingabeobjekt der Klasse Point2 oder
	 * Segment2 berührt.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * @param input_object
	 *            Das Eingabeobjekt
	 * 
	 * @return true, wenn eine Berührung existiert, false sonst
	 */
	private boolean _isTouching(
			Point2 input_point,
			Object input_object)
	{
		if (input_object instanceof Point2)
		{
			return ((Point2) input_object).equals(input_point);
		}
		else
		{
			return ((Segment2) input_object).liesOn(input_point);
		} // if

	} // _isTouching


	// ************************************************************************

	/**
	 * Löscht die Duplikate mehrfach in der Liste enthaltender Punkte aus
	 * ebendieser, die Reihenfolge der Punkte kann dabei geändert werden.
	 * 
	 * @param inout_points
	 *            Die Eingabeliste
	 */
	private void _removeDuplicates(
			SimpleList inout_points)
	{
		if (inout_points.length() < 2)
		{
			return;
		} // if

		// die Punkte der Groesse nach sortieren

		PointComparitor compare = new PointComparitor();

		compare.setOrder(PointComparitor.X_ORDER);
		inout_points.sort(compare);

		// Duplikate aus der Liste loeschen

		ListItem current_item = inout_points.first();
		ListItem next_item = current_item.next();

		while (next_item != null)
		{
			if (((Point2) current_item.value()).equals((Point2) next_item
					.value()))
			{
				inout_points.remove(next_item);
			}
			else
			{
				current_item = next_item;
			} // if

			next_item = current_item.next();

		} // while

	} // _removeDuplicates


	// ************************************************************************

	/**
	 * Orientiert das Polygon entsprechend ori.
	 * 
	 * @param ori
	 *            Entweder ORIENTATION_LEFT oder ORIENTATION_RIGHT
	 * 
	 * @see #ORIENTATION_LEFT
	 * @see #ORIENTATION_RIGHT
	 */
	public void setOrientation(
			byte ori)
	{
		SimpleList L = points();
		if (((ori != ORIENTATION_LEFT) && (ori != ORIENTATION_RIGHT))
				|| (L.length() <= 2))
			return;
		if (getOrientation() != ori)
		{
			L.reverse(); // Punktreihenfole umkehren
			L.cycle(-1); // wieder den urspruenglichen Anfangspunkt herstellen
		}
	} // setOrientation


	/**
	 * Ermittelt die Orientierung des Polygons.
	 * 
	 * Falls das Polygon weniger als 2 Punkte hat, wird ORIENTATION_LEFT
	 * zurückgegeben.
	 * 
	 * Bei der Ermittlung der Orientierung wird davon ausgangen, dass es sich um
	 * ein einfaches geschlossenes Polygon handelt.
	 * 
	 * @return Entweder ORIENTATION_LEFT oder ORIENTATION_RIGHT
	 * 
	 * @see #ORIENTATION_LEFT
	 * @see #ORIENTATION_RIGHT
	 */
	public byte getOrientation()
	{
		SimpleList L = points();
		if (L.length() <= 2)
			return ORIENTATION_LEFT;
		double innen = 0, aussen = 0, phi;
		Point2 p1, p2, p3;
		ListItem i = L.first();
		while (i != null)
		{
			p1 = (Point2) i.value();
			p2 = (Point2) L.cyclicRelative(i, 1).value();
			p3 = (Point2) L.cyclicRelative(i, 2).value();
			phi = p2.angle(p3, p1);
			innen += phi;
			aussen += 2 * Math.PI - phi;
			i = i.next();
		}

		if (innen < aussen)
			return ORIENTATION_LEFT;
		else
			return ORIENTATION_RIGHT;
	} // getOrientation


	// lup, 24.5. '97
	/**
	 * Ein Point-in-Polygon Test. Gibt true zurück, falls der Punkt p innerhalb
	 * des Polygons liegt. Auf einer Polygonkante zaehlt hierbei als ausserhalb.
	 * Benutzt locatePoint.
	 * 
	 * @param p
	 *            Zu testender Punkt
	 * 
	 * @return true, falls p innerhalb des Polygons liegt, ansonsten false
	 * 
	 * @see #locatePoint
	 */
	public boolean inside(
			Point2 p)
	{
		return locatePoint(p) == POINT_INSIDE;
	} // inside


	// lup, 24.5. '97
	/**
	 * Point-Location. Je nach Position des Punktes p wird eine der drei
	 * Konstanten zurueckgegeben: <ul> <li><strong>POINT_INSIDE</strong>, falls
	 * p innerhalb des Polygons liegt</li> <li><strong>POINT_ON_EDGE</strong>,
	 * falls der Punkt p auf dem Rand des Polygons liegt</li>
	 * <li><strong>POINT_OUTSIDE</strong>, falls p ausserhalb des Polygons
	 * liegt</li> </ul>
	 * 
	 * @param q
	 *            Zu lokalisierender Punkt
	 * 
	 * @return POINT_INSIDE, POINT_OUTSIDE oder POINT_ON_EDGE, je nach Lage des
	 *         Punktes.
	 */
	public byte locatePoint(
			Point2 q)
	{
		SimpleList L = points();
		if (L.length() < 1)
			return POINT_OUTSIDE;
		if (L.length() == 1)
		{
			if (((Point2) L.firstValue()).equals(q))
				return POINT_ON_EDGE;
			else
				return POINT_OUTSIDE;
		}
		if (L.length() == 2)
		{
			if ((new Segment2((Point2) L.firstValue(), (Point2) L.lastValue()))
					.liesOn(q))
				return POINT_ON_EDGE;
			else
				return POINT_OUTSIDE;
		}
		if (q == null)
		{
			System.out.println("Q ist NUll");
		}

		{
			Ray2 test = new Ray2(q, 0, Ray2.LEFT);
			Segment2 s;
			Point2 pl, p1, p2 = (Point2) L.lastKey(), pi;
			Intersection inter = new Intersection();
			boolean on = false;
			int l = 0;
			for (ListItem i = L.first(); (i != null) && (!on); i = i.next())
			{
				p1 = p2;
				p2 = (Point2) i.key();
				s = new Segment2(p1, p2);

				// modified by Joerg Matz 13.9.98
				// due to invalid POINT_OUTSIDE when POINT_ON_EDGE would be right
				// old code as follows:
				// if (!s.isHorizontal()) {
				// if ((pi=s.intersection(test,inter))!=null) {
				//  if (s.source().y<s.target().y) pl=s.source(); else pl=s.target();
				//  if (!pl.equals(pi)) l++;
				//  on=q.equals(pi);
				// }
				// } else on=s.liesOn(q);
				// new code

				if (s.liesOn(q))
				{
					on = true;
				}
				else if (!s.isHorizontal())
				{
					if ((pi = s.intersection(test, inter)) != null)
					{
						if (s.source().y < s.target().y)
							pl = s.source();
						else
							pl = s.target();
						if (!pl.equals(pi))
							l++;
					}
				}
				// end of modification
			}
			if (on)
				return POINT_ON_EDGE;
			if (l % 2 == 1)
				return POINT_INSIDE;
			else
				return POINT_OUTSIDE;
		}
	} // locatePoint


	/**
	 * Berechnet mit Hilfe der Klasse visPolygon das Sichtbarkeitspolygon
	 * bezüglich des Punktes p in O(n) Zeit.
	 * 
	 * @param p
	 *            Sichtpunkt
	 * 
	 * @return Sichtbarkeitspolygon bezueglich des Punktes p
	 */
	public Polygon2 vis(
			Point2 p)
	{
		return new visPolygon(this).vis(p);
	} // vis


	/**
	 * Skaliert das Polygon winkeltreu um den angegebenen Faktor.
	 * 
	 * @param factor
	 *            der Skalierungsfaktor
	 */
	public void scale(
			float factor)
	{

		for (int i = 0; i < _points.length(); i++)
		{
			Point2 p = (Point2) _points.getValueAt(i);
			p.moveTo(p.x * factor, p.y * factor);
		} // for

	} // scale


	//************************************************************************/
	/**
	 * Ermittelt alle reflexen Ecken des Polygons und gibt sie als Point2List
	 * zurück
	 * 
	 * Die reflexen Ecken sind dabei nach Auftreten sortiert (als Nebenprodukt)
	 * 
	 * Andrea Eubeler, 23.07.2003
	 * 
	 * @return Die Liste der reflexen Ecken des Polygons
	 */
	public Point2List getReflexVertices()

	{
		Point2List rvl = new Point2List();

		PointsAccess points = new PointsAccess(_points);

		while (points.hasNextPoint())
		{
			Point2 vertex = points.nextPoint();
			if (vertex.signedArea(points.getPrec(), points.getSucc()) < 0)
			{
				rvl.addPoint(vertex);
			}
		}

		return rvl;
	}


	/**
	 * Es wird ueberprueft, ob das Polygon konvex ist. Ein Polygon ist konvex,
	 * wenn alle Aussenwinkel groesser sind als PI.
	 * 
	 * <br>Author: Marina Bachran
	 * 
	 * @return True, wenn das Polygon konvex ist, sonst false
	 */
	public boolean isConvex()
	{
		Point2 actual;
		Point2 prev, post;
		int pointCount = length();
		PointsAccess access = new PointsAccess(this);
		double angle, firstAngle;
		boolean firstAngleSize;

		prev = access.cyclicNextPoint();
		actual = access.cyclicNextPoint();
		post = access.cyclicNextPoint();

		firstAngle = actual.angle(prev, post);
		firstAngleSize = (firstAngle <= Math.PI);

		for (int i = 1; i < pointCount; i++)
		{
			prev = actual;
			actual = post;
			post = access.cyclicNextPoint();
			angle = actual.angle(prev, post);
			if (firstAngleSize != (angle <= Math.PI))
				return false;
		}
		return true; // firstAngle und alle anderen angles stammen aus demselben
		// Intervall, also ist das Polygon konvex.
	}


	/**
	 * Using the order of the edges along the simple polygon, the calculation of
	 * the convex hull takes just O(n) time.
	 * 
	 * The polygon has to be simple. Otherwise the order of the edges is
	 * useless. The method works in O(n) time.
	 * 
	 * @return The convex hull of the polygon
	 */
	public ConvexPolygon2 getConvexHull()
	{
		ConvexPolygon2 cp2 = new ConvexPolygon2();
		cp2.createConvexHullFrom(this);

		return cp2;
	}


	/**
	 * Wenn das Polygon konvex ist, so wird es in ein ConvexPolygon2
	 * umgewandelt.
	 * 
	 * <br>Author: Marina Bachran
	 * 
	 * @return ConvexPolygon2; null, wenn das Polygon nicht konvex ist.
	 */
	public ConvexPolygon2 getConvexPolygon()
	{
		if (!isConvex())
			return null;
		return new ConvexPolygon2(this);
	}


	/**
	 * Berechnet die Triangulierung zu dem Polygon. Wenn das Polygon schon ein
	 * Dreieck ist, so wird es einfach in ein ConvexPolygon2 umgewandelt und im
	 * Rückgabe-Array an Position 0 abgelegt. Ansonsten wird das Polygon mit
	 * Hilfe der Klasse SeidelTriangulation in Dreiecke zerlegt, die im
	 * Rückgabe-Array abgelegt werden.
	 * 
	 * <br>Author: Marina Bachran
	 * 
	 * @return Array, der die Dreiecke der Triangulierung des Polygons enthält
	 */
	public ConvexPolygon2[] getTriangulation()
	{
		ConvexPolygon2[] triangles;
		if (edges().length < 4)
		{
			triangles = new ConvexPolygon2[1];
			triangles[0] = new ConvexPolygon2(this);
			return triangles;
		}
		SeidelTriangulation triangulation = new SeidelTriangulation(this);
		return triangulation.getTriangles();
	}


	/**
	 * Berechnet den Massenschwerpunkt des Polygons, wie beschrieben unter
	 * 
	 * <br>Author: Torsten Baumgartner
	 * 
	 * @return Enthält die Koordinaten des Masseschwerpunkts
	 * 
	 * @see "http://astronomy.swin.edu.au/~pbourke/geometry/polyarea"
	 */
	public Point2 getCenterOfMass()
	{
		double x = 0;
		double y = 0;
		double polygon_area = 0;
		int edge_number = edgeNumber();

		PointsAccess access = new PointsAccess(this);
		Point2 current_point;
		Point2 next_point;

		// berechne den Schwerpunkt des Polygons
		current_point = access.nextPoint();
		double helper;
		for (int index = 0; index < edge_number; index++)
		{
			next_point = access.cyclicNextPoint();
			helper = current_point.x * next_point.y - next_point.x
					* current_point.y;
			polygon_area += current_point.x * next_point.y - next_point.x
					* current_point.y;
			x += (current_point.x + next_point.x) * helper;
			y += (current_point.y + next_point.y) * helper;
			current_point = next_point;
		} // for
		polygon_area *= 0.5;
		x /= (polygon_area * 6);
		y /= (polygon_area * 6);

		return new Point2(x, y);
	}


	/**
	 * Returns the diameter of this Polygon2 in angle degrees. For further
	 * functionalities see getDiameterFunction().
	 * 
	 * Birgit Engels [ Version 1.0, Dez 2004 ]
	 * 
	 * @param angle
	 *            angle in degrees in which the diameter is calculated.
	 * 
	 * @return The diameter
	 */
	public double diameterDeg(
			double angle)
	{

		DiameterFunction df = new DiameterFunction(this);
		df.calcDiameter();
		return df.calcDiaInDegAngle(angle);
	}


	/**
	 * Returns the diameter of this Polygon2 in angle radiants. For further
	 * functionalities see getDiameterFunction().
	 * 
	 * Birgit Engels [ Version 1.0, Dez 2004 ]
	 * 
	 * @param angle
	 *            angle in radiants in which the diameter is calculated.
	 * 
	 * @return The diameter
	 * 
	 */
	public double diameterRad(
			double angle)
	{

		DiameterFunction df = new DiameterFunction(this);
		df.calcDiameter();
		return df.calcDiaInAngle(angle);
	}


	/**
	 * Returns the maximum Diameter of this Polygon2. For further
	 * functionalities see getDiameterFunction().
	 * 
	 * Birgit Engels [ Version 1.0, Dez 2004 ]
	 * 
	 * @return The maximum diameter
	 * 
	 * @see #getDiameterFunction()
	 */
	public double maxDiameter()
	{

		DiameterFunction df = new DiameterFunction(this);
		df.calcDiameter();
		return df.getMaxDiameter();

	}


	/**
	 * Returns the minimum Diameter of this Polygon2. For further
	 * functionalities see getDiameterFunction().
	 * 
	 * Birgit Engels [ Version 1.0, Dez 2004 ]
	 * 
	 * @return The minimum diameter
	 * 
	 * @see #getDiameterFunction()
	 */
	public double minDiameter()
	{

		DiameterFunction df = new DiameterFunction(this);
		df.calcDiameter();
		return df.getMinDiameter();

	}


	/**
	 * Returns the DiameterFunction of this Polygon2. For further
	 * functionalities see DiameterFunction()!
	 * 
	 * Birgit Engels [ Version 1.0, Dez 2004 ]
	 * 
	 * @return The diameter function
	 * 
	 * @see #getDiameterFunction()
	 */
	public DiameterFunction getDiameterFunction()
	{

		DiameterFunction df = new DiameterFunction(this);
		return df;
	}


	/**
	 * Dreht dieses Polygon um den Winkel angle um den Punkt center herum.
	 * 
	 * Birgit Engels [ Version 1.0, Nov 2004 ]
	 * 
	 * @param angle
	 *            Dieses Polygon wird um angle gedreht.
	 * @param center
	 *            Das Zentrum der Drehung ist center.
	 */
	public void turnAroundCenter(
			Point2 center,
			double angle)
	{

		Polygon2 poly = (Polygon2) clone();
		int len = poly.length();
		clear();
		Point2 act;
		Point2 turned_act;

		for (int i = 0; i < len; i++)
		{
			act = poly.firstPoint();
			poly.removeFirstPoint();

			turned_act = new Point2(Math.cos(angle) * (act.x - center.x)
					+ Math.sin(angle) * (act.y - center.y) + center.x, -Math
					.sin(angle)
					* (act.x - center.x) + Math.cos(angle) * (act.y - center.y) + center.y);

			addPoint(turned_act);

		}

	}// turnAroundCenter


	/**
	 * Verschiebt dieses Polygon gemäß der Differenz der beiden Referenzpunkte.
	 * 
	 * Birgit Engels [ Version 1.0, Nov 2004 ]
	 * 
	 * @param now
	 *            Referenzpunkt des Polygons vor der Verschiebung
	 * @param then
	 *            Soll-Referenzpunkt des Polygons nach der Verschiebung
	 * 
	 * @return Das Ergebnispolygon
	 */
	public Polygon2 transform(
			Point2 now,
			Point2 then)
	{

		float x, y;
		x = then.x - now.x;
		y = then.y - now.y;
		Polygon2 clone = (Polygon2) this.clone();
		Polygon2 current = new Polygon2();
		Point2 curr = null;
		for (int i = 0; i < length(); i++)
		{
			curr = clone.firstPoint();
			current.addPoint(new Point2(curr.x + x, curr.y + y));
			clone.removeFirstPoint();
		}
		return current;
	}


	/**
	 * Entfernt alle Punkte aus dem Polygon, die auf einer Kante des Polygons
	 * liegen, also keine eigene Ecke bilden und damit überflüssig sind. Implizit
	 * entfernt der Algorithmus alle aufeinanderfolgenden Punkte, die doppelt sind.
	 * 
	 * <br>Der Algorithmus arbeitet aktuell nur mit geschlossenen Polygonen.
	 * 
	 * <br>Author: Marina Bachran
	 */
	public void removeRedundantPoints()
	{
		// Entferne evtl. ueberfluessige Punkte aus dem Polygon
		int pointCount = length();
		if (isClosed())
		{
			PointsAccess pAccess = new PointsAccess(this);
			Point2 first = pAccess.cyclicNextPoint();
			Point2 second = pAccess.cyclicNextPoint();
			Point2 third = pAccess.cyclicNextPoint();
			for (int i = 0; i < pointCount; i++)
			{
				if ((new Segment2(first, third)).liesOn(second))
				{
					pAccess.cyclicPrevPoint();
					pAccess.removePoint();
					pAccess.cyclicNextPoint();
				}
				else
				{
					first = second;
				}
				second = third;
				third = pAccess.cyclicNextPoint();
			}
		}
	}


	/**
	 * Vergleicht zwei Polygone auf Gleichheit miteinander.
	 * 
	 * <br>Author Marina Bachran
	 * 
	 * @param otherPolygon
	 *            Das Polygon, mit dem verglichen werden soll
	 * 
	 * @return true, wenn die Polygone gleich sind, sonst false
	 */
	public boolean equals(
			Polygon2 otherPolygon)
	{
		// Ueberpruefe die Laenge des Polygons - bei Gleichheit weitere Ueberpruefungen
		int pointCount = length();
		if (pointCount != otherPolygon.length())
			return false; // Polygone sind nicht gleich
		// Finde den ersten Punkt dieses Polygons in otherPoly
		PointsAccess thisPoly = new PointsAccess(this);
		PointsAccess otherPoly = new PointsAccess(otherPolygon);
		Point2 actual = thisPoly.cyclicNextPoint();
		Point2 other = null;
		boolean pointFound = false;
		for (int i = 0; i < pointCount; i++)
		{
			other = otherPoly.cyclicNextPoint();
			if (actual.equals(other))
			{
				pointFound = true;
				break;
			}
		}
		if (!pointFound) // Punkt war nicht enthalten => Polygone nicht gleich
			return false;
		// Gehe Polygone durch und ueberpruefe die restlichen Punkte
		boolean sameOrientation = (getOrientation() == otherPolygon
				.getOrientation());
		for (int i = 0; i < pointCount - 1; i++)
		{
			actual = thisPoly.cyclicNextPoint();
			other = sameOrientation ? otherPoly.cyclicNextPoint() : otherPoly
					.cyclicPrevPoint();
			if (!actual.equals(other))
				return false;
		}
		// Polygone sind gleich
		return true;
	}


	/**
	 * Get the nearest cut point with polygon edge. startPoint and endPoint must
	 * lie in polygon.
	 * 
	 * Karl Czaputa (Version 1.00.00.01, 04.11.2005)
	 * 
	 * @param startPoint
	 *            start point of this ray.
	 * @param endPoint
	 *            direction of this ray.
	 * 
	 * @return Point2 cut point. otherwise null.
	 */
	public Point2 getFirstIntersection(
			Point2 startPoint,
			Point2 endPoint)
	{
		if (length() == 0)
			return (null);

		if (length() == 1)
			return (firstPoint());

		if (startPoint.distance(endPoint) < 0.0000001)
			return (null);

		Ray2 Strahl = new Ray2(startPoint, endPoint);
		Intersection schnittMenge = new Intersection();

		Segment2 poly_edge[] = this.edges();
		Point2 schnittPunkt = new Point2(Float.MAX_VALUE, Float.MAX_VALUE);
		Point2 schnittPunktTemp = new Point2(Float.MAX_VALUE, Float.MAX_VALUE);
		boolean schnittFound = false;

		// get the cut with all edges 
		for (int j = 0; j < poly_edge.length; j++)
		{
			Strahl.intersection(poly_edge[j], schnittMenge);

			// if cut is a point 
			// and new cut is closer than old cut   
			// and cut is not equal to startPoint 
			// and cut is not equal to endPoint

			if ((schnittMenge.result == Intersection.POINT2)
					&& (endPoint.distance(schnittMenge.point2) < endPoint
							.distance(schnittPunkt))
					&& (endPoint.distance(schnittMenge.point2) < endPoint
							.distance(schnittPunktTemp))
					&& (!endPoint.equals(schnittMenge.point2))
					&& (!startPoint.equals(schnittMenge.point2)))
			{

				// if the Segment between endPoint and Cut lie in Polygon 
				// then you have a new cut point
				schnittFound = false;
				schnittPunktTemp = schnittMenge.point2;
				float xValue = endPoint.x
						+ (schnittMenge.point2.x - endPoint.x) / 2;
				float yValue = endPoint.y
						+ (schnittMenge.point2.y - endPoint.y) / 2;
				Point2 isInPoly = new Point2(xValue, yValue);
				if (this.inside(isInPoly))
				{
					schnittPunkt = schnittMenge.point2;
					schnittFound = true;
				} //if this.inside(isInPoly))

			}//if ((schnittMenge.result == ...
		}// for

		if (schnittFound)
		{
			return (schnittPunkt);
		}// if

		return (null);

	} // getFirstIntersection


	/**
	 * This function is only a wrapper for getShortestPath and only there for
	 * backwards compatibility with older applets
	 * 
	 * The original shortestPath was removed due to getShortestPath being able
	 * to use points on the polygon edges and being much faster
	 * 
	 * @param start
	 *            start of the path as Point2
	 * @param target
	 *            target of the path as Point2
	 * 
	 * @return Point2List points of the shortest path inside the polygon
	 */
	public Point2List shortestPath(
			Point2 start,
			Point2 target)
	{
		Segment2[] path = this.getShortestPath(start, target);
		Point2List shortest = new Point2List();
		for (int i = 0; i < path.length; i++)
			shortest.addPoint(path[i].source());
		if (path.length > 0)
			shortest.addPoint(path[path.length - 1].target());
		return shortest;

	} // shortestPath


	/**
	 * Liefert das (offene) Teilpolygon zwischen Start- und Endknoten
	 * zurueck. Bei den Knoten des Teilpolygons handelt es sich wahlweise
	 * um Referenzen oder Kopien.
	 * 
	 * <br>Author: Johannes Werpup
	 * 
	 * @param start
	 *            Index des Startknotens 
	 * @param end
	 *            Index des Endknotens
	 * @param returnReferences
	 *            Falls true werden Referenzen der Knoten erstellt,
	 *            ansonsten Kopien
	 * @return Das Teilpolygon
	 */
	public Polygon2 subPolygon(
			int start,
			int end,
			boolean returnReferences)
	{
		SimpleList points = subList(start, end, true).points();
		Polygon2 subPoly = new Polygon2();
		subPoly.setOpen();
		
		for (ListItem item = points.first(); item != null; item = item.next())
		{
			Point2 point = (Point2)points.value(item);
			subPoly.addPoint( returnReferences ? point : (Point2)point.clone() );
		} // for

		return subPoly;

	} // subPoly


	/**
	 * Liefert das (offene) Teilpolygon zwischen Start- und Endknoten
	 * zurueck. Bei den Knoten des Teilpolygons handelt es sich wahlweise
	 * um Referenzen oder Kopien.
	 * 
	 * <br>Author: Johannes Werpup
	 * 
	 * @param start
	 *            Startknoten
	 * @param end
	 *            Endknoten
	 * @param returnReferences
	 *            Falls true werden Referenzen der Knoten erstellt,
	 *            ansonsten Kopien
	 * @return Das Teilpolygon, oder null falls ein Punkt nicht enthalten ist
	 */
	public Polygon2 subPolygon(
			Point2 start,
			Point2 end,
			boolean returnReferences)
	{
		int start_index = indexOf(start);
		int end_index = indexOf(end);
		if (start_index == -1 || end_index == -1)
			return null;
		
		return subPolygon(start_index, end_index, returnReferences);
	}

} // class Polygon2
