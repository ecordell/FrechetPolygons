package anja.geom.triangulation;


import java.awt.Color;
import java.util.Enumeration;
import java.util.Vector;

import java.awt.Graphics2D;

import anja.geom.Point2;
import anja.geom.Polygon2;
import anja.geom.Segment2;

import anja.util.Comparitor;
import anja.util.GraphicsContext;
import anja.util.ListItem;
import anja.util.SimpleList;
import anja.util.StdListItem;


/**
 * Triangulation eines einfachen Polygons nach:<br> <cite>Raimund Seidel. A
 * Simple and Fast Incremental Randomized Algorithm for Computing Trapezoidal
 * Decompositions and for Triangulating Polygons. Computational Geometry: Theory
 * and Applications, 1(1):51-64, 1991.</cite>
 * 
 * <br><b>Achtung:</b> Diese Implementation ueberschreibt die <a
 * href="../Point2.html#label"><code>label</code></a>-Variable aller
 * <code>Point2</code>-Objekte!
 * 
 * @version 0.7 25.08.2003
 * @author Sascha Ternes
 */

public class Polygon2Triangulation
		extends Triangulation
{

	// *************************************************************************
	// Public constants
	// *************************************************************************

	/**
	 * Label fuer Original-Punkte des Polygons
	 */
	public static final String	ORIGINAL	= "original";

	/**
	 * Pi / der Winkel eines halben Kreises, also 180°
	 */
	private static final double	PI			= Math.PI;

	// *************************************************************************
	// Private constants
	// *************************************************************************

	// Farbe der Trapezoid-Linien:
	private static final Color	_LINE_COLOR	= Color.RED;

	// Konstante fuer Polygon-Status: besucht
	private static final int	_VISITED	= 77;
	// Konstante fuer Polygon-Status: zum loeschen vorgemerkt
	private static final int	_DELETE		= 80;

	// *************************************************************************
	// Private variables
	// *************************************************************************

	// das zu triangulierende Polygon:
	private Polygon2			_polygon;

	// berechnete trapezoidale Dekomposition:
	private Vector				_trapezoids;
	// zugehoerige QueryStructure:
	private QueryStructure		_query;

	// berechnete polygonale Dekomposition:
	private Vector				_subpolygons;

	// berechnete Triangulation:
	private Vector				_triangles;

	// GraphicsContext fuer Trapezoide:
	private GraphicsContext		_gc_trapezoids;


	// *************************************************************************
	// Inner classes
	// *************************************************************************

	/**
	 * Comparitor fuer Punkte.
	 * 
	 * @see anja.util.Comparitor
	 */
	class Point2Comparitor
			implements Comparitor
	{

		@Override
		public short compare(
				Object o1,
				Object o2)
		{
			Point2 p1 = (Point2) ((ListItem) o1).value();
			Point2 p2 = (Point2) ((ListItem) o2).value();
			if (p1.y < p2.y)
				return Comparitor.SMALLER;
			if (p1.y > p2.y)
				return Comparitor.BIGGER;
			return Comparitor.EQUAL;
		} // compare

	} // Point2Comparitor


	// *************************************************************************
	// Constructors
	// *************************************************************************

	/*
	* Verbotener Konstruktor.
	*/
	private Polygon2Triangulation()
	{}


	/**
	 * Erzeugt eine neue Triangulation des spezifizierten Polygons, das keine
	 * Punkte mit gleichen y-Koordinaten beinhalten darf.
	 * 
	 * @param polygon
	 *            das zu triangulierende Polygon
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             falls mehrere Punkte des Polygons dieselbe y-Koordinate haben
	 */
	public Polygon2Triangulation(
			Polygon2 polygon)
			throws IllegalArgumentException
	{

		if (!_isValid(polygon))
		{
			throw new IllegalArgumentException(
					"polygon must not contain points with same y coordinate!");
		} // if
		_polygon = polygon;
		_trapezoids = new Vector();
		_subpolygons = new Vector();
		_triangles = new Vector();
		_initGraphicsContexts();

	} // Polygon2Triangulation


	// *************************************************************************
	// Interface Triangulation
	// *************************************************************************

	/*
	* [javadoc-Kommentar wird aus Triangulation kopiert]
	*/
	public void draw(
			Graphics2D g2d)
	{

		_makePointsOriginal();
		_triangulate();
		Enumeration e = _triangles.elements();
		while (e.hasMoreElements())
		{
			Polygon2 tri = (Polygon2) e.nextElement();
			tri.draw(g2d, _gc_trapezoids);
		} // while

	} // draw


	// *************************************************************************
	// Private methods
	// *************************************************************************

	/**
	 * Testet das Polygon auf Vorhandensein mehrerer Punkte mit gleicher
	 * y-Koordinate.
	 * 
	 * @param polygon
	 *            Das zu untersuchende Polygon
	 * 
	 * @return true if valid, false else
	 */
	private boolean _isValid(
			Polygon2 polygon)
	{

		SimpleList list = (SimpleList) polygon.points().clone();
		list.sort(new Point2Comparitor());
		Enumeration e = list.values();
		Point2 curr = null;
		if (e.hasMoreElements())
		{
			curr = (Point2) e.nextElement();
		} // if
		while (e.hasMoreElements())
		{
			Point2 next = (Point2) e.nextElement();
			if (next.y == curr.y)
				return false;
			curr = next;
		} // while
		return true;

	} // if


	/**
	 * Markiert alle Polygonpunkte als Originalpunkte.
	 */
	private void _makePointsOriginal()
	{

		Enumeration e = _polygon.points().values();
		while (e.hasMoreElements())
		{
			Point2 p = (Point2) e.nextElement();
			p.setLabel(ORIGINAL);
		} // while

	} // _makePointsOriginal


	/**
	 * Trianguliert das aktuelle Polygon.
	 */
	private void _triangulate()
	{

		_computeTrapezoidation();
		_keepInnerTrapezoids();
		_mergeIrrelevantTrapezoids();
		_introduceDiagonals();
		_computeSubPolygons();
		_computeTriangles();

	} // _triangulate


	/**
	 * Erzeugt die trapezoidale Dekomposition.
	 */
	private void _computeTrapezoidation()
	{

		/* Schritt 1: Erzeuge zufaellige Reihenfolge der Polygonsegmente */

		Segment2[] segments = _polygon.edges();
		int n = segments.length;
		Segment2 segment = null;
		// n-faches Vertauschen zweier zufaelliger Segmente:
		for (int i = 0; i < n; i++)
		{
			int a = (int) (Math.random() * n);
			int b = (int) (Math.random() * n);
			if (a == b)
				continue;
			segment = segments[a];
			segments[a] = segments[b];
			segments[b] = segment;
		} // for

		/* Schritt 2: Erzeuge und initialisiere T und Q mit s1
		   Hier wird die Trapezoidzerlegung vereinfacht:
		   Es werden nacheinander alle Segmente eingefuegt. */

		_query = new QueryStructure();

		// alle Segmente einfuegen:
		for (int i = 0; i < n; i++)
		{
			_query.insert(segments[i]);
		} // for

		// Sammeln aller Trapezoide:
		_trapezoids = _query.getTrapezoids();

	} // _computeTrapezoidation


	/**
	 * Loeschen aller Trapezoide ausserhalb des Polygons aus der Trapezoidmenge.
	 */
	private void _keepInnerTrapezoids()
	{

		// Sammlung der inneren Trapezoide:
		Vector inner = new Vector();

		// durchlaufen aller Trapezoide:
		float max = _query.getInfinity();
		Trapezoid[] tr = null;
		Trapezoid[] tn = null;
		Enumeration e = _trapezoids.elements();
		while (e.hasMoreElements())
		{
			Trapezoid trapez = (Trapezoid) e.nextElement();
			float x1 = trapez.getTopLeft().x;
			float x2 = trapez.getTopRight().x;
			// Trapezoide innerhalb der konvexen Huelle uebernehmen:
			if ((x1 > -max) && (x2 < max))
			{
				inner.add(trapez);
				// Trapezoide ausserhalb nicht uebernehmen:
			}
			else
			{ // if
				// Verbindungen entfernen:
				tr = trapez.getTopNeighbors();
				if (tr[0] != null)
				{
					tn = tr[0].getBottomNeighbors();
					if (tn[0] == trapez)
					{
						tr[0].linkBottomTo(tn[1]);
					}
					else
					{ // if
						tr[0].linkBottomTo(tn[0]);
					} // else
					if (tr[1] != null)
					{
						tn = tr[1].getBottomNeighbors();
						if (tn[0] == trapez)
						{
							tr[1].linkBottomTo(tn[1]);
						}
						else
						{ // if
							tr[1].linkBottomTo(tn[0]);
						} // else
					} // if
				} // if
				trapez.removeTopLinks();
				tr = trapez.getBottomNeighbors();
				if (tr[0] != null)
				{
					tn = tr[0].getTopNeighbors();
					if (tn[0] == trapez)
					{
						tr[0].linkTopTo(tn[1]);
					}
					else
					{ // if
						tr[0].linkTopTo(tn[0]);
					} // else
					if (tr[1] != null)
					{
						tn = tr[1].getTopNeighbors();
						if (tn[0] == trapez)
						{
							tr[1].linkTopTo(tn[1]);
						}
						else
						{ // if
							tr[1].linkTopTo(tn[0]);
						} // else
					} // if
				} // if
				trapez.removeBottomLinks();
			} // else
		} // while

		// Entfernen aller Trapezoidketten ausserhalb des Polygons:
		_trapezoids = inner;
		inner = new Vector();
		e = _trapezoids.elements();
		while (e.hasMoreElements())
		{
			Trapezoid trapez = (Trapezoid) e.nextElement();
			boolean td = trapez.topDegenerated();
			boolean bd = trapez.bottomDegenerated();
			// eine Trapezoidkette beginnt mit einem degenerierten Trapezoid:
			if (td || bd)
			{
				Point2 p = null;
				if (td)
				{
					p = (new Segment2(trapez.getBottomLeft(), trapez
							.getBottomRight()).center());
				}
				else
				{ // if
					p = (new Segment2(trapez.getTopLeft(), trapez.getTopRight())
							.center());
				} // else
				// Test, ob Trapezoid ausserhalb des Polygons liegt:
				if (!_polygon.inside(p))
				{
					// aeussere Kette entfernen:
					if (bd)
					{
						while (trapez.topNeighborCount() > 0)
						{
							Trapezoid next = trapez.getTopNeighbor();
							trapez.removeTopLinks();
							next.removeBottomLinks();
							trapez = next;
						} // while
					}
					else
					{ // if
						while (trapez.bottomNeighborCount() > 0)
						{
							Trapezoid next = trapez.getBottomNeighbor();
							trapez.removeBottomLinks();
							next.removeTopLinks();
							trapez = next;
						} // while
					} // else
				}
				else
				{ // if
					// inneres Trapezoid behalten:
					inner.add(trapez);
				} // else
				// ansonsten Trapezoid erstmal uebernehmen:
			}
			else
			{ // if
				inner.add(trapez);
			} // else
		} // while

		// abschliessend alle Trapezoide ohne Verbindungen entfernen:
		_trapezoids = inner;
		for (int i = _trapezoids.size() - 1; i >= 0; i--)
		{
			Trapezoid trapez = (Trapezoid) _trapezoids.get(i);
			if ((trapez.topNeighborCount() == 0)
					&& (trapez.bottomNeighborCount() == 0))
			{
				_trapezoids.remove(i);
			} // if
		} // for

	} // _keepInnerTrapezoids


	/**
	 * Fasst Trapezoide zusammen, die oben oder unten keine Originalpunkte
	 * haben.
	 */
	private void _mergeIrrelevantTrapezoids()
	{

		// die jeweils aktualisierte Trapezoidmenge bis zum Ende durchlaufen:
		int i = 0;
		String s0 = null;
		String s1 = null;
		String s2 = null;
		String s3 = null;
		while (i < _trapezoids.size())
		{
			Trapezoid trapez = (Trapezoid) _trapezoids.get(i);
			// nur "unbenutzte" Trapezoide behandeln:
			if (trapez.status == 0)
			{
				Point2[] p = trapez.getPoints();
				s0 = p[0].getLabel();
				s1 = p[1].getLabel();
				s2 = p[2].getLabel();
				s3 = p[3].getLabel();
				if (trapez.topDegenerated()
						&& (trapez.bottomNeighborCount() == 1))
				{
					// Test der Unterkante:
					if (((s2 != null) && (s2.equals(ORIGINAL)))
							|| ((s3 != null) && (s3.equals(ORIGINAL))))
					{
						trapez.status = _VISITED;
						// mit unterem Nachbarn zusammenfassen:
					}
					else
					{ // if
						Trapezoid neighbor = trapez.getBottomNeighbor();
						trapez.status = _DELETE;
						neighbor.status = _DELETE;
						trapez = new Trapezoid(p[0], p[1], neighbor
								.getBottomLeft(), neighbor.getBottomRight());
						// Verknuepfungen untereinander:
						Trapezoid[] bots = neighbor.getBottomNeighbors();
						trapez.linkBottomTo(bots);
						if (bots[0] != null)
						{
							Trapezoid[] t1 = bots[0].getTopNeighbors();
							Trapezoid[] t2 = new Trapezoid[2];
							if (t1[0] == neighbor)
							{
								t2[0] = trapez;
								t2[1] = t1[1];
							}
							else
							{ // if
								t2[0] = t1[0];
								t2[1] = trapez;
							} // else
							bots[0].linkTopTo(t2);
						} // if
						if (bots[1] != null)
						{
							Trapezoid[] t1 = bots[1].getTopNeighbors();
							Trapezoid[] t2 = new Trapezoid[2];
							if (t1[0] == neighbor)
							{
								t2[0] = trapez;
								t2[1] = t1[1];
							}
							else
							{ // if
								t2[0] = t1[0];
								t2[1] = trapez;
							} // else
							bots[1].linkTopTo(t2);
						} // if
						_trapezoids.add(trapez);
					} // else
				}
				else // if
				if (trapez.bottomDegenerated()
						&& (trapez.topNeighborCount() == 1))
				{
					// Test der Oberkante:
					if (((s0 != null) && (s0.equals(ORIGINAL)))
							|| ((s1 != null) && (s1.equals(ORIGINAL))))
					{
						trapez.status = _VISITED;
						// mit oberem Nachbarn zusammenfassen:
					}
					else
					{ // if
						Trapezoid neighbor = trapez.getTopNeighbor();
						trapez.status = _DELETE;
						neighbor.status = _DELETE;
						trapez = new Trapezoid(neighbor.getTopLeft(), neighbor
								.getTopRight(), p[2], p[3]);
						// Verknuepfungen untereinander:
						Trapezoid[] tops = neighbor.getTopNeighbors();
						trapez.linkTopTo(tops);
						if (tops[0] != null)
						{
							Trapezoid[] t1 = tops[0].getBottomNeighbors();
							Trapezoid[] t2 = new Trapezoid[2];
							if (t1[0] == neighbor)
							{
								t2[0] = trapez;
								t2[1] = t1[1];
							}
							else
							{ // if
								t2[0] = t1[0];
								t2[1] = trapez;
							} // else
							tops[0].linkBottomTo(t2);
						} // if
						if (tops[1] != null)
						{
							Trapezoid[] t1 = tops[1].getBottomNeighbors();
							Trapezoid[] t2 = new Trapezoid[2];
							if (t1[0] == neighbor)
							{
								t2[0] = trapez;
								t2[1] = t1[1];
							}
							else
							{ // if
								t2[0] = t1[0];
								t2[1] = trapez;
							} // else
							tops[1].linkBottomTo(t2);
						} // if
						_trapezoids.add(trapez);
					} // else
					// "mittlere" Trapezoide:
				}
				else // if
				// mit oberem Nachbarn zusammenfassen:
				if ((trapez.topNeighborCount() == 1) && (s0 == null)
						&& (s1 == null))
				{
					Trapezoid neighbor = trapez.getTopNeighbor();
					trapez.status = _DELETE;
					neighbor.status = _DELETE;
					Trapezoid trap = new Trapezoid(neighbor.getTopLeft(),
							neighbor.getTopRight(), p[2], p[3]);
					// Verknuepfungen untereinander:
					Trapezoid[] tops = neighbor.getTopNeighbors();
					trap.linkTopTo(tops);
					if (tops[0] != null)
					{
						Trapezoid[] t1 = tops[0].getBottomNeighbors();
						Trapezoid[] t2 = new Trapezoid[2];
						if (t1[0] == neighbor)
						{
							t2[0] = trap;
							t2[1] = t1[1];
						}
						else
						{ // if
							t2[0] = t1[0];
							t2[1] = trap;
						} // else
						tops[0].linkBottomTo(t2);
					} // if
					if (tops[1] != null)
					{
						Trapezoid[] t1 = tops[1].getBottomNeighbors();
						Trapezoid[] t2 = new Trapezoid[2];
						if (t1[0] == neighbor)
						{
							t2[0] = trap;
							t2[1] = t1[1];
						}
						else
						{ // if
							t2[0] = t1[0];
							t2[1] = trap;
						} // else
						tops[1].linkBottomTo(t2);
					} // if
					// Verknuepfung nach unten:
					Trapezoid[] bots = trapez.getBottomNeighbors();
					trap.linkBottomTo(bots);
					Trapezoid[] t1 = bots[0].getTopNeighbors();
					Trapezoid[] t2 = new Trapezoid[2];
					if (t1[0] == trapez)
					{
						t2[0] = trap;
						t2[1] = t1[1];
					}
					else
					{ //if
						t2[0] = t1[0];
						t2[1] = trap;
					} // else
					bots[0].linkTopTo(t2);
					if (bots[1] != null)
					{
						t1 = bots[1].getTopNeighbors();
						t2 = new Trapezoid[2];
						if (t1[0] == trapez)
						{
							t2[0] = trap;
							t2[1] = t1[1];
						}
						else
						{ //if
							t2[0] = t1[0];
							t2[1] = trap;
						} // else
						bots[1].linkTopTo(t2);
					} // if
					_trapezoids.add(trap);
				}
				else // if
				// mit unterem Nachbarn zusammenfassen:
				if ((trapez.bottomNeighborCount() == 1) && (s2 == null)
						&& (s3 == null))
				{
					Trapezoid neighbor = trapez.getBottomNeighbor();
					trapez.status = _DELETE;
					neighbor.status = _DELETE;
					Trapezoid trap = new Trapezoid(p[0], p[1], neighbor
							.getBottomLeft(), neighbor.getBottomRight());
					// Verknuepfungen untereinander:
					Trapezoid[] bots = neighbor.getBottomNeighbors();
					trap.linkBottomTo(bots);
					if (bots[0] != null)
					{
						Trapezoid[] t1 = bots[0].getTopNeighbors();
						Trapezoid[] t2 = new Trapezoid[2];
						if (t1[0] == neighbor)
						{
							t2[0] = trap;
							t2[1] = t1[1];
						}
						else
						{ // if
							t2[0] = t1[0];
							t2[1] = trap;
						} // else
						bots[0].linkTopTo(t2);
					} // if
					if (bots[1] != null)
					{
						Trapezoid[] t1 = bots[1].getTopNeighbors();
						Trapezoid[] t2 = new Trapezoid[2];
						if (t1[0] == neighbor)
						{
							t2[0] = trap;
							t2[1] = t1[1];
						}
						else
						{ // if
							t2[0] = t1[0];
							t2[1] = trap;
						} // else
						bots[1].linkTopTo(t2);
					} // if
					// Verknuepfung nach oben:
					Trapezoid[] tops = trapez.getTopNeighbors();
					trap.linkTopTo(tops);
					Trapezoid[] t1 = tops[0].getBottomNeighbors();
					Trapezoid[] t2 = new Trapezoid[2];
					if (t1[0] == trapez)
					{
						t2[0] = trap;
						t2[1] = t1[1];
					}
					else
					{ //if
						t2[0] = t1[0];
						t2[1] = trap;
					} // else
					tops[0].linkBottomTo(t2);
					if (tops[1] != null)
					{
						t1 = tops[1].getBottomNeighbors();
						t2 = new Trapezoid[2];
						if (t1[0] == trapez)
						{
							t2[0] = trap;
							t2[1] = t1[1];
						}
						else
						{ //if
							t2[0] = t1[0];
							t2[1] = trap;
						} // else
						tops[1].linkBottomTo(t2);
					} // if
					_trapezoids.add(trap);
				}
				else
				{ // if
					// Trapezoide mit vier Nachbarn ueberspringen:
					trapez.status = _VISITED;
				} // else
			} // if
			i++;
		} // while

		// neue Trapezoidmenge erzeugen:
		Vector merged = new Vector();
		for (i = 0; i < _trapezoids.size(); i++)
		{
			Trapezoid trapez = (Trapezoid) _trapezoids.get(i);
			if (trapez.status != _DELETE)
			{
				merged.add(trapez);
			} // if
		} // for
		_trapezoids = merged;

	} // _mergeUnrelevantTrapezoids


	/**
	 * Fuegt Diagonalen in allen Trapezoiden ein, die Polygonpunkte auf
	 * verschiedenen Seiten haben. Daraus resultieren einfache Trapezoidketten,
	 * die zusammen das Polygon ergeben und jeweils von je einem degenerierten
	 * Trapezoid pro Kettenende begrenzt werden.
	 */
	private void _introduceDiagonals()
	{

		Vector traps = new Vector();
		Enumeration e = _trapezoids.elements();
		while (e.hasMoreElements())
		{
			Trapezoid trapez = (Trapezoid) e.nextElement();
			Point2[] p = trapez.getPoints();
			// degenerierte Trapezoide behandeln:
			if (trapez.topDegenerated())
			{
				// Degenerierte mit einem Nachbarn uebernehmen:
				if (trapez.bottomNeighborCount() == 1)
				{
					traps.add(trapez);
					// Degenerierte mit zwei Nachbarn teilen:
				}
				else
				{ // if
					// benutze gemeinsamen Punkt der unteren Nachbarn:
					Trapezoid[] bots = trapez.getBottomNeighbors();
					Point2 bot = bots[0].getTopRight();
					// vertikale Teilung des Trapezoids:
					Trapezoid t1 = new Trapezoid(p[0], p[0], p[2], bot);
					Trapezoid t2 = new Trapezoid(p[0], p[0], bot, p[3]);
					Connector conn = new Connector(t1, t2, bot, p[0]);
					t1.connector = conn;
					t2.connector = conn;
					t1.linkBottomTo(bots[0]);
					t2.linkBottomTo(bots[1]);
					bots[0].linkTopTo(t1);
					bots[1].linkTopTo(t2);
					traps.add(t1);
					traps.add(t2);
				} // else
			}
			else if (trapez.bottomDegenerated())
			{
				// Degenerierte mit einem Nachbarn uebernehmen:
				if (trapez.topNeighborCount() == 1)
				{
					traps.add(trapez);
					// Degenerierte mit zwei Nachbarn teilen:
				}
				else
				{ // if
					// benutze gemeinsamen Punkt der oberen Nachbarn:
					Trapezoid[] tops = trapez.getTopNeighbors();
					Point2 top = tops[0].getBottomRight();
					// vertikale Teilung des Trapezoids:
					Trapezoid t1 = new Trapezoid(p[0], top, p[2], p[2]);
					Trapezoid t2 = new Trapezoid(top, p[1], p[2], p[2]);
					Connector conn = new Connector(t1, t2, p[2], top);
					t1.connector = conn;
					t2.connector = conn;
					t1.linkTopTo(tops[0]);
					t2.linkTopTo(tops[1]);
					tops[0].linkBottomTo(t1);
					tops[1].linkBottomTo(t2);
					traps.add(t1);
					traps.add(t2);
				} // else
				// alle anderen Trapezoide:
			}
			else
			{ // if
				String s1 = p[0].getLabel();
				String s2 = p[1].getLabel();
				String s3 = p[2].getLabel();
				String s4 = p[3].getLabel();

				if (trapez.topNeighborCount() == 2)
				{
					// benutze gemeinsamen Punkt der oberen Nachbarn:
					Trapezoid[] tops = trapez.getTopNeighbors();
					Point2 top = tops[0].getBottomRight();
					if (trapez.bottomNeighborCount() == 2)
					{
						// benutze zusaetzlich gemeinsamen Punkt der unteren Nachbarn:
						Trapezoid[] bots = trapez.getBottomNeighbors();
						Point2 bot = bots[0].getTopRight();
						// Teilung durch die beiden Punkte:
						Trapezoid t1 = new Trapezoid(p[0], top, p[2], bot);
						Trapezoid t2 = new Trapezoid(top, p[1], bot, p[3]);
						Connector conn = new Connector(t1, t2, bot, top);
						t1.connector = conn;
						t2.connector = conn;
						t1.linkTopTo(tops[0]);
						t1.linkBottomTo(bots[0]);
						t2.linkTopTo(tops[1]);
						t2.linkBottomTo(bots[1]);
						tops[0].linkBottomTo(t1);
						tops[1].linkBottomTo(t2);
						bots[0].linkTopTo(t1);
						bots[1].linkTopTo(t2);
						traps.add(t1);
						traps.add(t2);
					}
					else
					{ // if
						// benutze nur oberen:
						Trapezoid bot = trapez.getBottomNeighbor();
						if ((s3 != null) && (s3.equals(ORIGINAL)))
						{
							// Diagonale von oben Mitte nach links unten:
							Trapezoid t1 = new Trapezoid(p[0], top, p[2], p[2]);
							Trapezoid t2 = new Trapezoid(top, p[1], p[2], p[3]);
							Connector conn = new Connector(t1, t2, p[2], top);
							t1.connector = conn;
							t2.connector = conn;
							t1.linkTopTo(tops[0]);
							t2.linkTopTo(tops[1]);
							t2.linkBottomTo(bot);
							tops[0].linkBottomTo(t1);
							tops[1].linkBottomTo(t2);
							Trapezoid[] tn = bot.getTopNeighbors();
							Trapezoid[] tm = new Trapezoid[2];
							if (tn[0] == trapez)
							{
								tm[0] = t2;
								tm[1] = tn[1];
							}
							else
							{ // if
								tm[0] = tn[0];
								tm[1] = t2;
							} // else
							bot.linkTopTo(tm);
							traps.add(t1);
							traps.add(t2);
						}
						else // if
						if ((s4 != null) && (s4.equals(ORIGINAL)))
						{
							// Diagonale von oben Mitte nach rechts unten:
							Trapezoid t1 = new Trapezoid(p[0], top, p[2], p[3]);
							Trapezoid t2 = new Trapezoid(top, p[1], p[3], p[3]);
							Connector conn = new Connector(t1, t2, p[3], top);
							t1.connector = conn;
							t2.connector = conn;
							t1.linkTopTo(tops[0]);
							t2.linkTopTo(tops[1]);
							t1.linkBottomTo(bot);
							tops[0].linkBottomTo(t1);
							tops[1].linkBottomTo(t2);
							Trapezoid[] tn = bot.getTopNeighbors();
							Trapezoid[] tm = new Trapezoid[2];
							if (tn[0] == trapez)
							{
								tm[0] = t1;
								tm[1] = tn[1];
							}
							else
							{ // if
								tm[0] = tn[0];
								tm[1] = t1;
							} // else
							bot.linkTopTo(tm);
							traps.add(t1);
							traps.add(t2);
						} // if
					} // else
				}
				else // if

				if (trapez.bottomNeighborCount() == 2)
				{
					// benutze gemeinsamen Punkt der unteren Nachbarn:
					Trapezoid top = trapez.getTopNeighbor();
					Trapezoid[] bots = trapez.getBottomNeighbors();
					Point2 bot = bots[0].getTopRight();
					if ((s1 != null) && (s1.equals(ORIGINAL)))
					{
						// Diagonale von unten Mitte nach links oben:
						Trapezoid t1 = new Trapezoid(p[0], p[0], p[2], bot);
						Trapezoid t2 = new Trapezoid(p[0], p[1], bot, p[3]);
						Connector conn = new Connector(t1, t2, bot, p[0]);
						t1.connector = conn;
						t2.connector = conn;
						t2.linkTopTo(top);
						t1.linkBottomTo(bots[0]);
						t2.linkBottomTo(bots[1]);
						bots[0].linkTopTo(t1);
						bots[1].linkTopTo(t2);
						Trapezoid[] tn = top.getBottomNeighbors();
						Trapezoid[] tm = new Trapezoid[2];
						if (tn[0] == trapez)
						{
							tm[0] = t2;
							tm[1] = tn[1];
						}
						else
						{ // if
							tm[0] = tn[0];
							tm[1] = t2;
						} // else
						top.linkBottomTo(tm);
						traps.add(t1);
						traps.add(t2);
					}
					else // if
					if ((s2 != null) && (s2.equals(ORIGINAL)))
					{
						// Diagonale von unten Mitte nach rechts oben:
						Trapezoid t1 = new Trapezoid(p[0], p[1], p[2], bot);
						Trapezoid t2 = new Trapezoid(p[1], p[1], bot, p[3]);
						Connector conn = new Connector(t1, t2, bot, p[1]);
						t1.connector = conn;
						t2.connector = conn;
						t1.linkTopTo(top);
						t1.linkBottomTo(bots[0]);
						t2.linkBottomTo(bots[1]);
						bots[0].linkTopTo(t1);
						bots[1].linkTopTo(t2);
						Trapezoid[] tn = top.getBottomNeighbors();
						Trapezoid[] tm = new Trapezoid[2];
						if (tn[0] == trapez)
						{
							tm[0] = t1;
							tm[1] = tn[1];
						}
						else
						{ // if
							tm[0] = tn[0];
							tm[1] = t1;
						} // else
						top.linkBottomTo(tm);
						traps.add(t1);
						traps.add(t2);
					} // if
				}
				else // if

				if ((s1 != null) && s1.equals(ORIGINAL)
						&& ((s4 != null) && s4.equals(ORIGINAL)))
				{
					// Diagonale zwischen Punkt 1 und 4:
					Trapezoid t1 = new Trapezoid(p[0], p[0], p[2], p[3]);
					Trapezoid t2 = new Trapezoid(p[0], p[1], p[3], p[3]);
					Connector conn = new Connector(t1, t2, p[3], p[0]);
					t1.connector = conn;
					t2.connector = conn;
					t1.linkBottomTo(trapez.getBottomNeighbor());
					t2.linkTopTo(trapez.getTopNeighbor());
					Trapezoid[] tn = trapez.getTopNeighbor()
							.getBottomNeighbors();
					Trapezoid[] tm = new Trapezoid[2];
					if (tn[0] == trapez)
					{
						tm[0] = t2;
						tm[1] = tn[1];
					}
					else
					{ // if
						tm[0] = tn[0];
						tm[1] = t2;
					} // else
					trapez.getTopNeighbor().linkBottomTo(tm);
					tn = trapez.getBottomNeighbor().getTopNeighbors();
					tm = new Trapezoid[2];
					if (tn[0] == trapez)
					{
						tm[0] = t1;
						tm[1] = tn[1];
					}
					else
					{ // if
						tm[0] = tn[0];
						tm[1] = t1;
					} // else
					trapez.getBottomNeighbor().linkTopTo(tm);
					traps.add(t1);
					traps.add(t2);
				}
				else // if

				if ((s2 != null) && s2.equals(ORIGINAL)
						&& ((s3 != null) && s3.equals(ORIGINAL)))
				{
					// Diagonale zwischen Punkt 2 und 3:
					Trapezoid t1 = new Trapezoid(p[0], p[1], p[2], p[2]);
					Trapezoid t2 = new Trapezoid(p[1], p[1], p[2], p[3]);
					Connector conn = new Connector(t1, t2, p[2], p[1]);
					t1.connector = conn;
					t2.connector = conn;
					t1.linkTopTo(trapez.getTopNeighbor());
					t2.linkBottomTo(trapez.getBottomNeighbor());
					Trapezoid[] tn = trapez.getTopNeighbor()
							.getBottomNeighbors();
					Trapezoid[] tm = new Trapezoid[2];
					if (tn[0] == trapez)
					{
						tm[0] = t1;
						tm[1] = tn[1];
					}
					else
					{ // if
						tm[0] = tn[0];
						tm[1] = t1;
					} // else
					trapez.getTopNeighbor().linkBottomTo(tm);
					tn = trapez.getBottomNeighbor().getTopNeighbors();
					tm = new Trapezoid[2];
					if (tn[0] == trapez)
					{
						tm[0] = t2;
						tm[1] = tn[1];
					}
					else
					{ // if
						tm[0] = tn[0];
						tm[1] = t2;
					} // else
					trapez.getBottomNeighbor().linkTopTo(tm);
					traps.add(t1);
					traps.add(t2);

				}
				else
				{ // if
					// keine Diagonale - Trapezoid uebernehmen:
					traps.add(trapez);
				} // else
			} // else
		} // while

		// neue Trapezoidzerlegung ermitteln:
		_trapezoids = traps;

	} // _introduceDiagonals()


	/**
	 * Berechnet die Dekomposition in triangulierbare Polygone.
	 */
	private void _computeSubPolygons()
	{

		_subpolygons.clear();
		// jedes Subpolygon hat ein Bottom-Trapezoid - diese durchlaufen:
		Enumeration e = _trapezoids.elements();
		while (e.hasMoreElements())
		{
			Trapezoid bottom = (Trapezoid) e.nextElement();
			// aufbauend auf dem Bottom-Trapezoid das Subpolygon erstellen:
			if (bottom.bottomDegenerated())
			{
				SubPolygon2 sub = new SubPolygon2(bottom);
				_subpolygons.add(sub); // Subpolygon hinzufuegen
			} // if
		} // while

	} // _computeSubPolygons


	/**
	 * Berechnet aus den Subpolygonen die Dreiecke.
	 */
	private void _computeTriangles()
	{

		// Initialisierungen:
		_triangles.clear();
		SubPolygon2 sub = null;
		Triangle triangle = null;
		Connector connector = null;
		SimpleList chain = null;
		boolean is_left = true;
		boolean is_foot = true;
		StdListItem foot = null;
		StdListItem item1 = null;
		StdListItem item2 = null;
		StdListItem top = null;

		// Lauf ueber alle Subpolygone:
		Enumeration e = _subpolygons.elements();
		while (e.hasMoreElements())
		{
			// Initialisierungen pro Subpolygon:
			sub = (SubPolygon2) e.nextElement();
			chain = (SimpleList) sub.getChain().clone();
			is_left = sub.isLeftChain();
			is_foot = true;
			foot = (StdListItem) chain.first();
			top = (StdListItem) chain.last();

			// sukzessives Verkleinern des Subpolygons:
			while (chain.length() >= 3)
			{
				item1 = (StdListItem) chain.at(1);
				item2 = (StdListItem) chain.at(2);
				// Art der Dreieckerzeugung abhaengig vom Winkel:
				double angle = 0.0;
				if (is_left)
				{
					angle = _angle(foot, item1, item2);
				}
				else
				{ // if
					angle = _angle(item2, item1, foot);
				} // else
				// erzeuge ein Dreieck aus den unteren drei Punkten:
				if (angle < PI)
				{
					triangle = _triangle(foot, item1, item2);
					connector = (Connector) foot.value();
					if (connector != null)
					{
						if (is_left)
						{
							connector.right_triangle = triangle;
						}
						else
						{ // if
							connector.left_triangle = triangle;
						} // else
						triangle.addNeighbor(connector);
					} // if
					connector = (Connector) item1.value();
					if (connector != null)
					{
						if (is_left)
						{
							connector.right_triangle = triangle;
						}
						else
						{ // if
							connector.left_triangle = triangle;
						} // else
						triangle.addNeighbor(connector);
					} // if
					chain.remove(item1);

					// verwende den Fusspunkt und den Dachpunkt zur Dreieckerzeugung:
				}
				else
				{ // if
					triangle = _triangle(foot, item1, top);
					connector = (Connector) foot.value();
					if (connector != null)
					{
						if (is_left)
						{
							connector.right_triangle = triangle;
						}
						else
						{ // if
							connector.left_triangle = triangle;
						} // else
						triangle.addNeighbor(connector);
					} // if
					chain.remove(foot);
					foot = (StdListItem) chain.at(0);
					is_foot = false;
				} // else
				_triangles.add(triangle);
			} // while
		} // while

	} // _computeTriangles


	/**
	 * Vergleicht zwei Punkte primaer nach y-Koordinate und sekundaer nach
	 * x-Koordinate und liefert das Ergebnis (p-q <>= 0).
	 * 
	 * @param p
	 *            Erster Vergleichspunkt
	 * @param q
	 *            Zweiter Vergleichspunkt
	 * 
	 * @return -1, 0, 1 je nach Lage der Punkte
	 */
	private int _compare(
			Point2 p,
			Point2 q)
	{

		if (p.y < q.y)
			return -1;
		if (p.y > q.y)
			return 1;
		if (p.x < q.x)
			return -1;
		if (p.x > q.x)
			return 1;
		else
			return 0;

	} // _compare


	/**
	 * Berechnet den Winkel zwischen drei Punkten einer Subpolygon-Kette.
	 * Berechnet wird der Winkel zwischen p(a)---p(b) und p(b)---p(c) entgegen
	 * dem Uhrzeigersinn.
	 * 
	 * @param a
	 *            Erster Punkt
	 * @param b
	 *            Zweiter Punkt
	 * @param c
	 *            Dritter Punkt
	 * 
	 * @return Der Winkel
	 */
	private double _angle(
			StdListItem a,
			StdListItem b,
			StdListItem c)
	{

		return ((Point2) b.key()).angle((Point2) a.key(), (Point2) c.key());

	} // _angle


	/**
	 * Erzeugt ein Dreieck aus drei Punkten einer Kette.
	 * 
	 * @param a
	 *            Erster Punkt
	 * @param b
	 *            Zweiter Punkt
	 * @param c
	 *            Dritter Punkt
	 * 
	 * @return Das Dreieck
	 */
	private Triangle _triangle(
			StdListItem a,
			StdListItem b,
			StdListItem c)
	{

		return new Triangle((Point2) a.key(), (Point2) b.key(), (Point2) c
				.key());

	} // _triangle


	/**
	 * Initialisiert die GraphicsContext-Objekte.
	 */
	private void _initGraphicsContexts()
	{

		_gc_trapezoids = new GraphicsContext();
		_gc_trapezoids.setForegroundColor(_LINE_COLOR);
		_gc_trapezoids.setFillStyle(0);

	} // _initGraphicsContexts

} // Polygon2Triangulation
