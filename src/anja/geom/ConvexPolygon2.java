package anja.geom;


import anja.util.Sorter;


/**
 * Zweidimensionales konvexes Polygon.
 * 
 * @version 0.1 11.04.1997
 * @author Lihong Ma
 */

public class ConvexPolygon2
		extends Polygon2
{

	// ************************************************************************
	// constants
	// ************************************************************************
	/**
	 * Gegeben seien ein konvexes Polygon und zwei Punkte, dann wird die Ebene
	 * in 4 Teile zerlegt. Punkt liegt in der Region Gu_pq, die durch pp4 und
	 * pp3 beschraenkt ist.
	 */
	public static final int	IN_Gu_pq	= 0;

	/**
	 * Punkt liegt in der Region Gu_qp, die durch qq3 und qq4 beschraenkt ist.
	 */
	public static final int	IN_Gu_qp	= 1;

	/**
	 * Punkt liegt in der Region Gb_pq, die durch pp1, pp2 und qq2, qq1
	 * beschraenkt ist.
	 */
	public static final int	IN_Gb_pq	= 2;

	/**
	 * Punkt liegt ausserhalb von Regionen Gb_pq, Gu_pq, Gu_qp.
	 */
	public static final int	OUT			= 3;

	/**
	 * Punkt liegt auf der Gerade pp3.
	 */
	public static final int	ON_p3		= 4;

	/**
	 * Punkt liegt auf der Gerade pp4.
	 */
	public static final int	ON_p4		= 5;

	/**
	 * Punkt liegt auf der Gerade qq3.
	 */
	public static final int	ON_q3		= 6;

	/**
	 * Punkt liegt auf der Gerade qq4.
	 */
	public static final int	ON_q4		= 7;

	/**
	 * Punkt liegt auf dem Polygon.
	 */
	public static final int	ON_POLYGON	= 10;

	/**
	 * Punkt liegt innerhalb des Polygons.
	 */
	public static final int	IN_POLYGON	= 11;

	/**
	 * Punkt liegt ausserhalb des Polygons.
	 */
	public static final int	OUT_POLYGON	= 12;

	// ************************************************************************
	// Variables
	// ************************************************************************
	private Point2			_center;			// Zentrum des konvexen Polygons.


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt ein leeres convexes Polygon.
	 */
	public ConvexPolygon2()
	{
		super();
		_center = new Point2();

	} // ConvexPolygon2


	// ********************************

	/**
	 * Erzeugt ein konvexes Polygon ohne Zentrum aus der eingegebenen
	 * Punktliste, die Punkte des konvexen Polygons sind <B>Kopien</B> - nicht
	 * etwa Referenzen - der Punkte der Eingabeliste.
	 * 
	 * Eingabeliste Berechnung der konvexen Huelle mit Hilfe des Konturpolygons,
	 * siehe Kurs 1840. Zurueckgegeben wird ein Polygon2 mit der konvexen
	 * Huelle, orientiert im Gegenuhrzeigersinn, der Anfangspunkt des Polygons
	 * hat die kleinste vorkommende Y-Koordinate.
	 * 
	 * Autor: Lihong Ma, 3.4.1997
	 * 
	 * @param input_points
	 *            The point list of all polygon edges
	 * 
	 */
	public ConvexPolygon2(
			Point2List input_points)
	{
		_center = new Point2();
		_points = _convexHull(input_points)._points;

	} // ConvexPolygon2


	// ********************************

	/**
	 * Erzeugt ein konvexes Polygon mit Zentrum cen aus der eingegebenen
	 * Punktliste, die Punkte des konvexen Polygons sind <B>Kopien</B> - nicht
	 * etwa Referenzen - der Punkte der Eingabeliste.
	 * 
	 * @param input_points
	 *            Eingabeliste
	 * @param cen
	 *            Zentrum
	 */
	public ConvexPolygon2(
			Point2List input_points,
			Point2 cen)
	{
		_center = cen;
		_points = _convexHull(input_points)._points;

	} // ConvexPolygon2


	// ********************************

	/**
	 * Erzeugt ein konvexes Polygon als Kopie des eingegebenen konvexen
	 * Polygons. Die Punkte des konvexen Polygons sind <B>Kopien</B> - nicht
	 * etwa Referenzen - der Punkte des Eingabepolygons.
	 * 
	 * @param cpolygon
	 *            Eingabepolygon
	 */
	public ConvexPolygon2(
			ConvexPolygon2 cpolygon)
	{
		super((Polygon2) cpolygon);
		_center = new Point2(cpolygon._center);
	} // ConvexPolygon2


	// ********************************
	/**
	 * Erzeugt ein konvexes Polygon als skalierte Kopie des eingegebenen
	 * konvexen Polygons. Die Skalierung mit dem Faktor s erfolgt bezueglich des
	 * Zentrums des Polygons.
	 * 
	 * @param s
	 *            The scale
	 * @param cpolygon
	 *            The convex polygon
	 */
	public ConvexPolygon2(
			double s,
			ConvexPolygon2 cpolygon)
	{
		super(s, cpolygon, cpolygon._center);
		_center = new Point2(cpolygon._center);

	} // ConvexPolygon2


	// ********************************
	/**
	 * Erzeugt ein konvexes Polygon als skalierte Kopie des eingegebenen
	 * konvexen Polygons, aber mit Zentrum um den gegebenen Punkt.
	 * 
	 * @param p
	 *            The centre for the scale
	 * @param s
	 *            The scale
	 * @param cpolygon
	 *            The convex polygon
	 */
	public ConvexPolygon2(
			Point2 p,
			double s,
			ConvexPolygon2 cpolygon)
	{
		super(s, cpolygon, cpolygon._center);
		Point2 vec = new Point2(1, p, -1, cpolygon._center);
		translate(vec);
		_center = new Point2(p);

	} // ConvexPolygon2


	// ********************************
	/**
	 * Calculates the convex hull of the given polygon. The boolean variable is
	 * essential for this constructor. If the polyogn is simple, the convex hull
	 * can be calculated in O(n) time, while issimple==false results in an O(n
	 * logn) time. Information: The result of the Polygon2.isSimple()-call uses
	 * O(n^2) time.
	 * 
	 * @param poly
	 *            The (simple) polygon
	 * @param issimple
	 *            Information, if the polygon is simple
	 */
	public ConvexPolygon2(
			Polygon2 poly,
			boolean issimple)
	{
		if (issimple)
		{
			this.createConvexHullFrom(poly);
		}
		else
		{
			_center = new Point2();
			_points = _convexHull(poly)._points;
		}

	} // ConvexPolygon2


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Kopiert das konvexe Polygon, die Punkte der Kopie sind <B>Kopien</B> -
	 * nicht etwa Referenzen - der Punkte des Originals.
	 * 
	 * @return Kopie des konvexen Polygons
	 */
	public Object clone()
	{
		return (new ConvexPolygon2(this));

	} // clone


	// ************************************************************************

	/**
	 * Das Polygon erhaelt ein Zentrum.
	 * 
	 * @param cen
	 *            The center of the polygon
	 */
	public void setCenter(
			Point2 cen)
	{
		_center = cen;
	}


	// ************************************************************************

	/**
	 * Gibt das Polygon-Zentrum zurueck.
	 * 
	 * @return The center of the polygon
	 */
	public Point2 center()
	{
		return _center;
	}


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation.
	 * 
	 * @return die textuelle Repraesentation
	 */
	public String toString()
	{
		return "ConvexPolygon2[" + _points + " center:" + _center + "]";

	} // toString


	// ************************************************************************

	/**
	 * Verschiebung um die Eingabewerte.
	 * 
	 * @param input_horizontal
	 *            horizontale Verschiebung
	 * @param input_vertical
	 *            vertikale Verschiebung
	 */
	public void translate(
			float input_horizontal,
			float input_vertical)
	{
		super.translate(input_horizontal, input_vertical);
		_center.translate(input_horizontal, input_vertical);
	} // translate


	// ********************************

	/**
	 * Verschiebung um den Vektor vom Nullpunkt zum Eingabepunkt.
	 * <BR><B>Vorbedingungen:</B> Der Eingabepunkt ist nicht null
	 * 
	 * @param input_point
	 *            Eingabepunkt
	 */
	public void translate(
			Point2 input_point)
	{
		translate(input_point.x, input_point.y);

	} // translate


	// ************************************************************************

	/**
	 * Converts this object to the convex hull of the given polygon poly.
	 * 
	 * The method retreives a simple polygon. It only works with simple
	 * polygons, because the order of the points on the shape of the polygon is
	 * a additional information to caculate the convex hull of the polygon,
	 * which can so be done in O(n) time.
	 * 
	 * The content of this object is overwritten by the new points of the convex
	 * hull.
	 * 
	 * @param poly
	 *            The simple polygon, the convex hull should be calculated from
	 */
	public void createConvexHullFrom(
			Polygon2 poly)
	{
		_points = _convexHullForSimplePolygons(poly)._points;
	}


	// ********************************
	/**
	 * Liefert das symmetrische konvexe Polygon des konvexen Polygons um sein
	 * Zentrum.
	 * 
	 * @return The convex polygon around its center
	 */
	public ConvexPolygon2 symmetry()
	{
		PolygonAccess pA = new PolygonAccess(this);
		Point2 cen = _center;
		Point2 first = pA.nextPoint();
		Point2 vertex = pA.nextPoint();
		Point2List sym = new Point2List();
		sym.addPoint(first);
		sym.addPoint(2 * cen.x - first.x, 2 * cen.y - first.y);

		while (!vertex.equals(first))
		{
			sym.addPoint(vertex);
			sym.addPoint(2 * cen.x - vertex.x, 2 * cen.y - vertex.y);
			vertex = pA.nextPoint();
		}
		return new ConvexPolygon2(sym);

	} // symmetry


	// ********************************
	/**
	 * Liefert das gespiegelte konvexe Polygon des konvexen Polygons um sein
	 * Zentrum.
	 * 
	 * @return A reflection of the convex polygon
	 */
	public ConvexPolygon2 reflection()
	{
		PolygonAccess pA = new PolygonAccess(this);
		Point2 cen = _center;
		Point2 first = pA.nextPoint();
		Point2 vertex = pA.nextPoint();
		ConvexPolygon2 ref = new ConvexPolygon2();
		ref.addPoint(2 * cen.x - first.x, 2 * cen.y - first.y);

		while (!vertex.equals(first))
		{
			ref.addPoint(2 * cen.x - vertex.x, 2 * cen.y - vertex.y);
			vertex = pA.nextPoint();
		}
		return ref;

	} // reflection


	// ********************************

	/**
	 * Liefert die Ecke des konvexen Polygons mit Zentrum im Ursprung, die am
	 * weitesten und auf der linken Seite der Gerade liegt, wobei die Gerade
	 * durch den Ursprung mit Richtung pq geht. Falls es zwei Ecke gibt, dann
	 * gibt den rechten zurueck.
	 * 
	 * @param p
	 *            Point2 object
	 * @param q
	 *            Point2 object
	 * 
	 * @return PolygonAccess object
	 */
	public PolygonAccess leftMostRight(
			Point2 p,
			Point2 q)
	{
		//System.out.println("this Polygon: "+this);
		if (this.empty())
			return null;
		Point2 origin = new Point2(0, 0);

		Point2 direction = new Point2(1, q, -1, p); // Richtung pq.
		PolygonAccess pA = new PolygonAccess(this);
		Point2 first = pA.nextPoint(); // der erste Punkt von C.
		Point2 vertex;
		double maxDist = 0;
		PolygonAccess maxpA = null;
		double vertexDist;
		if (first.orientation(origin, direction) == Point2.ORIENTATION_LEFT)
		{
			maxDist = first.squareDistance(origin, direction);
			maxpA = new PolygonAccess(pA);
		}

		while (!pA.nextPoint().equals(first))
		{
			vertex = pA.currentPoint();
			if (vertex.orientation(origin, direction) == Point2.ORIENTATION_LEFT)
			{
				vertexDist = vertex.squareDistance(origin, direction);
				if (vertexDist > maxDist)
				{
					maxDist = vertexDist;
					maxpA = new PolygonAccess(pA);
				}
			}
		}
		Line2 pq = new Line2(p, q);
		Point2 t = maxpA.currentPoint();
		Point2 tPrev = maxpA.prevPoint();
		if (pq.isParallel(t, tPrev))
			return maxpA;
		maxpA.nextPoint();
		return maxpA;
	} // leftMostRight


	// ********************************

	/**
	 * Liefert die Ecke des konvexen Polygons mit Zentrum im Ursprung, die am
	 * weitesten und auf der linken Seite der Gerade liegt, wobei die Gerade
	 * durch den Ursprung mit Richtung pq geht. Falls es zwei gibt, dann gibt
	 * den linken zurueck.
	 * 
	 * @param p
	 *            Point2 object
	 * @param q
	 *            Point2 object
	 * 
	 * @return PolygonAccess object
	 */
	public PolygonAccess leftMostLeft(
			Point2 p,
			Point2 q)
	{
		if (this.empty())
			return null;
		Point2 origin = new Point2(0, 0);

		Point2 direction = new Point2(1, q, -1, p); // Richtung pq.
		PolygonAccess pA = new PolygonAccess(this);
		Point2 first = pA.nextPoint(); // der erste Punkt von C.
		Point2 vertex;
		double maxDist = 0;
		PolygonAccess maxpA = null;
		double vertexDist;
		if (first.orientation(origin, direction) == Point2.ORIENTATION_LEFT)
		{
			maxDist = first.squareDistance(origin, direction);
			maxpA = new PolygonAccess(pA);
		}

		while (!pA.nextPoint().equals(first))
		{
			vertex = pA.currentPoint();
			if (vertex.orientation(origin, direction) == Point2.ORIENTATION_LEFT)
			{
				vertexDist = vertex.squareDistance(origin, direction);
				if (vertexDist > maxDist)
				{
					maxDist = vertexDist;
					maxpA = new PolygonAccess(pA);
				}
			}
		}
		Line2 pq = new Line2(p, q);
		Point2 t = maxpA.currentPoint();
		Point2 tNext = maxpA.nextPoint();
		if (pq.isParallel(t, tNext))
			return maxpA;
		maxpA.prevPoint();
		return maxpA;
	} // leftMostLeft


	// ************************************************************************

	/**
	 * Liefert die Ecke des konvexen Polygons mit Zentrum im Ursprung, die am
	 * weitesten und auf der rechten Seite der Gerade liegt, wobei die Gerade
	 * durch den Ursprung mit Richtung pq geht. Falls es zwei gibt, gibt den
	 * linken zurueck.
	 * 
	 * @param p
	 *            Point2 object
	 * @param q
	 *            Point2 object
	 * 
	 * @return PolygonAccess object
	 */
	public PolygonAccess rightMostLeft(
			Point2 p,
			Point2 q)
	{
		if (this.empty())
			return null;
		Point2 origin = new Point2(0, 0);

		Point2 direction = new Point2(1, q, -1, p);
		PolygonAccess pA = new PolygonAccess(this);
		Point2 first = pA.nextPoint(); // der erste Punkt von C.
		Point2 vertex;
		double maxDist = 0;
		PolygonAccess maxpA = null;
		double vertexDist;
		if (first.orientation(origin, direction) == Point2.ORIENTATION_RIGHT)
		{
			maxDist = first.squareDistance(origin, direction);
			maxpA = new PolygonAccess(pA);
		}
		while (!pA.nextPoint().equals(first))
		{
			vertex = pA.currentPoint();
			if (vertex.orientation(origin, direction) == Point2.ORIENTATION_RIGHT)
			{
				vertexDist = vertex.squareDistance(origin, direction);
				if (vertexDist > maxDist)
				{
					maxDist = vertexDist;
					maxpA = new PolygonAccess(pA);
				}
			}
		}
		Line2 pq = new Line2(p, q);
		Point2 d = maxpA.currentPoint();
		Point2 dPrev = maxpA.prevPoint();
		if (pq.isParallel(d, dPrev))
			return maxpA;
		maxpA.nextPoint();
		return maxpA;
	} // rightMost


	// ************************************************************************

	/**
	 * Liefert die Ecke des konvexen Polygons mit Zentrum im Ursprung, die am
	 * weitesten und auf der rechten Seite der Gerade liegt, wobei die Gerade
	 * durch den Ursprung mit Richtung pq geht. Falls es zwei gibt, gibt den
	 * rechten zurueck.
	 * 
	 * @param p
	 *            Point2 object
	 * @param q
	 *            Point2 object
	 * 
	 * @return PolygonAccess object
	 */
	public PolygonAccess rightMostRight(
			Point2 p,
			Point2 q)
	{
		if (this.empty())
			return null;
		Point2 origin = new Point2(0, 0);
		Point2 direction = new Point2(1, q, -1, p);

		PolygonAccess pA = new PolygonAccess(this);
		Point2 first = pA.nextPoint(); // der erste Punkt von C.
		Point2 vertex;
		double maxDist = 0;
		PolygonAccess maxpA = null;
		double vertexDist;
		if (first.orientation(origin, direction) == Point2.ORIENTATION_RIGHT)
		{
			maxDist = first.squareDistance(origin, direction);
			maxpA = new PolygonAccess(pA);
		}
		while (!pA.nextPoint().equals(first))
		{
			vertex = pA.currentPoint();
			if (vertex.orientation(origin, direction) == Point2.ORIENTATION_RIGHT)
			{
				vertexDist = vertex.squareDistance(origin, direction);
				if (vertexDist > maxDist)
				{
					maxDist = vertexDist;
					maxpA = new PolygonAccess(pA);
				}
			}
		}
		Line2 pq = new Line2(p, q);
		Point2 d = maxpA.currentPoint();
		Point2 dNext = maxpA.nextPoint();
		if (pq.isParallel(d, dNext))
			return maxpA;
		maxpA.prevPoint();
		return maxpA;
	} // rightMost


	// ************************************************************************

	/**
	 * Teste, in welcher Regionen Gb_pq, Gu_pq, Gu_qp der Punkt r liegt. Gegeben
	 * sind drei Punkte und das konvexe Polygon.
	 * 
	 * 
	 * @param p
	 *            Point2 object
	 * @param q
	 *            Point2 object
	 * @param r
	 *            Point2 object
	 * 
	 * @return The region
	 */
	public int whichRegion(
			Point2 p,
			Point2 q,
			Point2 r)
	{
		//System.out.println("whichRegion");
		ConvexPolygon2 result = this.planeDivide(p, q);
		return region(p, q, r, result);

	} //whichRegion


	// ************************************************************************

	/**
	 * Gegeben sind das konvexe Polygon und zwei Punkte p und q, die Ebene wird
	 * in Gb_pq, Gu_pq, Gu_qp und OUT zerlegt. Es sollt die Grenzpunkt berechnet
	 * werden, die in eine PointsListe gespeichert werden sollen.
	 * 
	 * @param p
	 *            Point2 object
	 * @param q
	 *            Point2 object
	 * 
	 * @return The convex polygon
	 */
	public ConvexPolygon2 planeDivide(
			Point2 p,
			Point2 q)
	{
		//System.out.println("planeDivide "+this);
		ConvexPolygon2 result = new ConvexPolygon2();
		Point2 origin = new Point2(0, 0);
		Point2 cen = center();
		ConvexPolygon2 copyC = (ConvexPolygon2) clone();
		if (!cen.equals(origin))
		{
			copyC.translate(-cen.x, -cen.y);
		}
		copyC.setCenter(origin);

		Line2 pq = new Line2(p, q);
		PolygonAccess pATopLeft = copyC.leftMostLeft(p, q);
		PolygonAccess pABottomLeft = copyC.rightMostLeft(p, q);

		Point2 p1, q1, p2, q2, p3, q3, p4, q4, pt, nt, t, pd, nd, t1, d, d1, dir;
		nt = pATopLeft.nextPoint();
		t1 = pATopLeft.prevPoint();
		t = pATopLeft.prevPoint();
		pt = pATopLeft.prevPoint();
		if (!pq.isParallel(t, t1))
		{
			pt = new Point2(t);
			t = new Point2(t1);
		}
		if (!t.equals(t1))
		{
			dir = new Point2(1, t, -1, t1);
			p2 = new Point2(p.x + dir.x, p.y + dir.y);
			q2 = new Point2(q.x - dir.x, q.y - dir.y);

			p3 = new Point2(p.x - dir.x, p.y - dir.y);
			q3 = new Point2(q.x + dir.x, q.y + dir.y);
			if (p.isSmaller(q))
			{ // p<q
				p3 = new Point2(1, t, -1, pt);
				p3.translate(p);
			}

			if (q.isSmaller(p))
			{ // p>q
				q3 = new Point2(1, t1, -1, nt);
				q3.translate(q);
			}

		}
		else
		{
			dir = new Point2(pt.x - t.x, pt.y - t.y);
			p2 = new Point2(p.x + dir.x, p.y + dir.y);
			p3 = new Point2(p.x - dir.x, p.y - dir.y);

			dir = new Point2(nt.x - t.x, nt.y - t.y);
			q2 = new Point2(q.x + dir.x, q.y + dir.y);
			q3 = new Point2(q.x - dir.x, q.y - dir.y);

		}
		pd = pABottomLeft.prevPoint();
		d1 = pABottomLeft.nextPoint();
		d = pABottomLeft.nextPoint();
		nd = pABottomLeft.nextPoint();
		if (!pq.isParallel(d, d1))
		{
			nd = new Point2(d);
			d = new Point2(d1);
		}
		if (!d.equals(d1))
		{
			dir = new Point2(1, d, -1, d1);
			p1 = new Point2(p.x + dir.x, p.y + dir.y);
			q1 = new Point2(q.x - dir.x, q.y - dir.y);

			p4 = new Point2(p.x - dir.x, p.y - dir.y);
			q4 = new Point2(q.x + dir.x, q.y + dir.y);
			if (p.isSmaller(q))
			{ // p< q
				p4 = new Point2(1, d, -1, nd);
				p4.translate(p);
			}
			if (q.isSmaller(p))
			{ // p>q
				q4 = new Point2(1, d1, -1, pd);
				q4.translate(q);
			}
		}
		else
		{
			dir = new Point2(nd.x - d.x, nd.y - d.y);
			p1 = new Point2(p.x + dir.x, p.y + dir.y);

			p4 = new Point2(p.x - dir.x, p.y - dir.y);

			dir = new Point2(pd.x - d.x, pd.y - d.y);
			q1 = new Point2(q.x + dir.x, q.y + dir.y);
			q4 = new Point2(q.x - dir.x, q.y - dir.y);
		}
		result.addPoint(p1);
		result.addPoint(p2);
		result.addPoint(p3);
		result.addPoint(p4);
		result.addPoint(q1);
		result.addPoint(q2);
		result.addPoint(q3);
		result.addPoint(q4);
		//System.out.println(p+" "+q);
		//System.out.println(p1+" "+p2+" "+p3+" "+p4+" "+q1+" "+q2+" "+q3+" "+q4);
		return result;
	} //planeDivide


	// ************************************************************************

	/**
	 * Teste, ob es fuer die drei Eingabepunkte einen Voronoiknoten gibt.
	 * 
	 * @param p
	 *            Punkt 1
	 * @param q
	 *            Punkt 2
	 * @param r
	 *            Punkt 3
	 * 
	 * @return true, wenn es einen Voronoiknoten gibt, false sonst
	 */
	public boolean existVoronoiVertex(
			Point2 p,
			Point2 q,
			Point2 r)
	{
		if (empty())
			return false;
		return (whichRegion(p, q, r) == OUT);
	} //existVoronoiVertex


	// ************************************************************************

	/**
	 * Teste, in welcher Regionen Gb_pq, Gu_pq, Gu_qp der Punkt r liegt. Gegeben
	 * sind drei Punkte und eine Point2Liste.
	 * 
	 * @param p
	 *            Point 1
	 * @param q
	 *            Point 2
	 * @param r
	 *            Point to look for
	 * @param pL
	 *            The point list
	 * 
	 * @return the region
	 */
	public static int region(
			Point2 p,
			Point2 q,
			Point2 r,
			ConvexPolygon2 pL)
	{
		//System.out.println("region");
		Point2 p1, p2, p3, p4, q1, q2, q3, q4;
		PolygonAccess pA = new PolygonAccess(pL);
		p1 = new Point2(pA.nextPoint());
		p2 = new Point2(pA.nextPoint());
		p3 = new Point2(pA.nextPoint());
		p4 = new Point2(pA.nextPoint());
		q1 = new Point2(pA.nextPoint());
		q2 = new Point2(pA.nextPoint());
		q3 = new Point2(pA.nextPoint());
		q4 = new Point2(pA.nextPoint());

		//System.out.println("p: "+p+" q: "+q+" r: "+r);
		//System.out.println("p1: "+p1+" p2: "+p2+" p3: "+p3+" p4: "+p4);
		//System.out.println("q1: "+q1+" q2: "+q2+" q3: "+q3+" q4: "+q4);
		//System.out.println("r.isInAngle(p1,p,p2): "+r.isInAngle(p1,p,p2));
		//System.out.println("r.isInAngle(q2,q,q1): "+r.isInAngle(q2,q,q1));
		if (r.isInAngle(p1, p, p2) && r.isInAngle(q2, q, q1))
		{
			//System.out.println("1. IN_Gb_pq");
			return IN_Gb_pq;
		}

		if (p.isSmaller(q))
		{
			if (r.isCollinear(p, p1) && r.isCollinear(q, q1))
			{
				if (p.isCollinear(p1, p2))
				{
					if (r.isSmaller(p))
						return OUT;
					return IN_Gu_pq;
				}
				else
				{
					return OUT;
				}
			}
			if (r.isCollinear(p, p2) && r.isCollinear(q, q2))
			{
				if (p.isCollinear(p1, p2))
				{
					if (r.isSmaller(p))
						return OUT;
					return IN_Gu_pq;
				}
				else
				{
					return OUT;
				}
			}
		}
		if (q.isSmaller(p))
		{
			if (r.isCollinear(p, p1) && r.isCollinear(q, q1))
			{
				if (q.isCollinear(q1, q2))
				{
					if (r.isSmaller(q))
						return OUT;
					return IN_Gu_qp;
				}
				else
				{
					return OUT;
				}
			}
			if (r.isCollinear(p, p2) && r.isCollinear(q, q2))
			{
				if (q.isCollinear(q1, q2))
				{
					if (r.isSmaller(q))
						return OUT;
					return IN_Gu_qp;
				}
				else
				{
					return OUT;
				}
			}
		}

		if (r.isInAngle(q2, q, q1))
		{
			if (r.isCollinear(p, p1))
			{
				if (r.inspectCollinearPoint(p, p1) != Point2.LIES_BEFORE)
				{
					if (p1.equals(p3))
						return IN_Gu_pq; // p,p1,r collinar.
					if (p.isSmaller(q))
					{
						if (p.isSmaller(r))
							return OUT;
						return IN_Gb_pq;
					} // p>q
					if (r.isSmaller(p))
						return IN_Gb_pq;
					return OUT; // r>p
				}
			}
			else if (r.isCollinear(p, p2))
			{
				if (r.inspectCollinearPoint(p, p2) != Point2.LIES_BEFORE)
				{
					if (p2.equals(p4))
						return IN_Gu_pq;
					if (p.isSmaller(q))
					{
						if (p.isSmaller(r))
							return OUT;
						return IN_Gb_pq;
					}
					else
					{ // p>q
						if (r.isSmaller(p))
							return IN_Gb_pq;
						return OUT;
					}
				}
			}
		}

		if (r.isInAngle(p1, p, p2))
		{
			if (r.isCollinear(q, q1))
			{
				if (r.inspectCollinearPoint(q, q1) != Point2.LIES_BEFORE)
				{
					if (q1.equals(q3))
						return IN_Gu_qp;
					if (q.isSmaller(p))
					{
						if (q.isSmaller(r))
							return OUT;
						return IN_Gb_pq; // q>r
					}
					else
					{ // q>p
						if (r.isSmaller(q))
							return IN_Gb_pq;
						return OUT; // r>q
					}
				}
			}
			else if (r.isCollinear(q, q2))
			{
				if (r.inspectCollinearPoint(q, q2) != Point2.LIES_BEFORE)
				{
					if (q2.equals(q4))
						return IN_Gu_qp;
					if (q.isSmaller(p))
					{
						if (q.isSmaller(r))
							return OUT;
						return IN_Gb_pq;
					}
					if (r.isSmaller(q))
						return IN_Gb_pq;
					return OUT;
				}
			}
		}

		if (r.isInAngle(p4, p, p3))
		{
			//System.out.println("1. IN_Gu_pq");
			return IN_Gu_pq;
		}
		//System.out.println("r.orientation(p,p3): "+r.orientation(p,p3));
		//System.out.println("r.isInAngle(q2,q,q1): "+r.isInAngle(q2,q,q1));
		//System.out.println("r.orientation(p,p4): "+r.orientation(p,p4));
		//System.out.println("r.orientation(q,q3): "+r.orientation(q,q3));
		//System.out.println("r.orientation(q,q4): "+r.orientation(q,q4));
		if (r.isCollinear(p, p3))
		{
			if (r.inspectCollinearPoint(p, p3) != Point2.LIES_BEFORE)
			{
				if (!p.isCollinear(p2, p3))
					return OUT;
				if (r.isSmaller(p))
					return OUT;
				return IN_Gu_pq;
			}
		}

		if (r.isCollinear(p, p4))
		{
			if (r.inspectCollinearPoint(p, p4) != Point2.LIES_BEFORE)
			{
				if (!p.isCollinear(p1, p4))
					return OUT;
				if (r.isSmaller(p))
					return OUT;
				return IN_Gu_pq;
			}
		}
		if (r.isInAngle(q3, q, q4))
		{
			//System.out.println("1. IN_Gu_qp");
			return IN_Gu_qp;
		}

		if (r.isCollinear(q, q3))
		{
			if (r.inspectCollinearPoint(q, q3) != Point2.LIES_BEFORE)
			{
				if (!q.isCollinear(q2, q3))
					return OUT;
				if (r.isSmaller(q))
					return OUT;
				return IN_Gu_qp;
			}
		}

		if (r.isCollinear(q, q4))
		{
			if (r.inspectCollinearPoint(q, q4) != Point2.LIES_BEFORE)
			{
				if (!q.isCollinear(q1, q4))
					return OUT;
				if (r.isSmaller(q))
					return OUT;
				return IN_Gu_qp;
			}
		}
		return OUT;

	} //whichRegion


	// ************************************************************************

	/**
	 * Erzeugt ein offenes konvexes Polygon mit 8 Punkten.
	 * 
	 * @param p
	 *            The first point
	 * @param q
	 *            The second point
	 * 
	 * @return A convex polygon with 8 edges.
	 */
	public ConvexPolygon2 openConvPolygon(
			Point2 p,
			Point2 q)
	{
		Point2 origin = new Point2(0, 0);
		//System.out.println("openConvPolygon: ");
		//System.out.println(this);
		Point2 cen = center();
		ConvexPolygon2 copyC = (ConvexPolygon2) clone();
		if (!cen.equals(origin))
		{
			copyC.translate(-cen.x, -cen.y);
		}
		copyC.setCenter(origin);

		ConvexPolygon2 result = new ConvexPolygon2(copyC.planeDivide(p, q));
		setOpen();
		//System.out.println("openConvPolygon: "+result);
		return result;
	} //openConvPolygon


	// ************************************************************************

	/**
	 * Finde die Ecke, so dass der Eingabepunkt auf der Kante zwischen ihr und
	 * ihrem Vorgaenger liegt. Falls der Eingabepunkt eine Ecke ist, dann gibt
	 * sie sie zurueck.
	 * 
	 * @param p
	 *            The point to check
	 * 
	 * @return A PolygonAccess object (pointing at the answer)
	 */
	public PolygonAccess findPoint(
			Point2 p)
	{
		PolygonAccess pA = new PolygonAccess(this);
		Point2 first = pA.nextPoint();
		if (p.equals(first))
			return new PolygonAccess(pA);
		Point2 second = new Point2(pA.nextPoint());
		if (p.equals(second))
			return new PolygonAccess(pA);

		Point2 q1 = pA.currentPoint();
		Point2 p1 = pA.nextPoint();
		if (p.equals(p1))
			return new PolygonAccess(pA);

		while (!p1.equals(second))
		{
			if (p.orientation(q1, p1) == Point2.ORIENTATION_COLLINEAR)
			{
				if (p.inspectCollinearPoint(q1, p1) == Point2.LIES_ON)
					return new PolygonAccess(pA);
			}
			q1 = p1;
			p1 = pA.nextPoint();
		}
		if (p.orientation(q1, p1) == Point2.ORIENTATION_COLLINEAR)
		{
			if (p.inspectCollinearPoint(q1, p1) == Point2.LIES_ON)
				return new PolygonAccess(pA);
		}
		if (p1.equals(second))
		{
			System.out.println(p + " liegt leider nicht auf dem Polygon");
			System.out.println(this);

		}
		return null;
	} // findPoint


	// ************************************************************************

	/**
	 * Finde den Schnittpunkt zwischen dem Strahl aus Zentrum durch den
	 * Eingabepunkt und einer Kante des konvexen Polygons. Die Ausgabe ist einen
	 * Zeiger, der die Ecke zeigt, so da"s der SchnittPunkt zwischen ihr und
	 * ihrem Vorg"anger liegt.
	 * 
	 * @param v
	 *            The point to check
	 * 
	 * @return PolygonAccess object
	 */
	public PolygonAccess findCutPoint(
			Point2 v)
	{
		//System.out.println("findCutPoint");
		if (this.center() == null)
		{
			System.out.println(this + " hat noch kein Zentrum");
			return null;
		}
		PolygonAccess pA = new PolygonAccess(this);
		Point2 first = new Point2(pA.nextPoint());
		Point2 second = new Point2(pA.nextPoint());

		Ray2 rpv = new Ray2(this.center(), v);
		//System.out.println("this.center() "+this.center()+" v: "+v);
		//System.out.println("rpv: "+rpv);
		Segment2 spq;
		Intersection cut = new Intersection();
		Point2 q1 = pA.currentPoint();
		Point2 p1 = pA.nextPoint();
		while (!p1.equals(second))
		{
			int or1, or2;
			or1 = v.orientation(this.center(), q1);
			or2 = v.orientation(this.center(), p1);
			//System.out.println("or1: "+or1+" or2: "+or2+" q1: "+q1+" p1: "+p1);
			if (or2 == Point2.ORIENTATION_COLLINEAR)
			{
				if (!(v.inspectCollinearPoint(this.center(), p1) == Point2.LIES_BEFORE))
					return new PolygonAccess(pA);
			}
			if (or1 == Point2.ORIENTATION_LEFT
					&& or2 == Point2.ORIENTATION_RIGHT)
			{
				spq = new Segment2(p1, q1);
				rpv.intersection(spq, cut);
				if (cut.result == Intersection.POINT2)
				{
					//System.out.println("rpv: "+rpv+" spq: "+spq+" "+cut.point2);
					return new PolygonAccess(pA);
				}
			}
			q1 = p1;
			p1 = pA.nextPoint();
		}
		spq = new Segment2(p1, q1);
		rpv.intersection(spq, cut);
		if (cut.result == Intersection.POINT2)
			return new PolygonAccess(pA);

		System.out.println("Fehler in findcutPoint");
		return null;

	} // findCutPoint


	/**
	 * NICHT FERTIG! Berechnet den Schnitt von 2 konvexen Polygonen in O(n+m)
	 * 
	 * @param b
	 *            Das zweite konvexe Polygon
	 * 
	 * @return Das Ergebnispolygon, welches nicht zwangsweise konvex sein muss
	 *         (z.B. Linie)
	 */
	public Polygon2 intersection(
			ConvexPolygon2 b)
	{
		ConvexPolygon2 a = this;
		Polygon2 result = new Polygon2();

		//Orientierung testen

		if (a.edgeNumber() < 2 || b.edgeNumber() < 2)
		{
			return null;
		}

		//Orientierung von a
		boolean or_a_ccw;
		if (a.edges()[0].target().angle(a.edges()[1].target(),
				a.edges()[0].source()) < Math.PI)
			or_a_ccw = true;
		else
			or_a_ccw = false;

		//Orientierung von b
		boolean or_b_ccw;
		if (b.edges()[0].target().angle(b.edges()[1].target(),
				b.edges()[0].source()) < Math.PI)
			or_b_ccw = true;
		else
			or_b_ccw = false;

		//wird true, sobald ein Schnittpunkt entsteht
		boolean cut = false;

		//inside ist flag für 0=unknown, 1=a inside, 2=b inside
		//erst einmal reicht unknown, alles Weitere ergibt sich
		int inside = 0;

		Intersection inter = new Intersection();

		Segment2[] s_a = a.edges();
		Segment2[] s_b = b.edges();

		int m = a.edgeNumber();
		int n = b.edgeNumber();

		int i_a = or_a_ccw ? 0 : m - 1;
		int i_b = or_b_ccw ? 0 : n - 1;

		int schritte = 0;
		while (schritte++ <= 2 * (m + n))
		{
			if ((i_a == m - ((or_a_ccw ? 0 : 1) * m) || i_b == n
					- ((or_b_ccw ? 0 : 1) * n))
					&& inside == 0)
			{
				break;
			}

			//Aktive Kanten als Vektoren interpretiert, bHA = b in Halbebene links von a
			Point2 vec_a = null;
			Point2 vec_b = null;
			boolean bHA = false;
			boolean aHB = false;
			if (or_a_ccw)
			{
				vec_a = new Point2(s_a[i_a].target().x - s_a[i_a].source().x,
						s_a[i_a].target().y - s_a[i_a].source().y);
				if (or_b_ccw)
				{
					bHA = s_b[i_b].target().orientation(s_a[i_a]) != Point2.ORIENTATION_RIGHT;

					vec_b = new Point2(s_b[i_b].target().x
							- s_b[i_b].source().x, s_b[i_b].target().y
							- s_b[i_b].source().y);
				}
				else
				{
					bHA = s_b[i_b].source().orientation(s_a[i_a]) != Point2.ORIENTATION_RIGHT;

					vec_b = new Point2(s_b[i_b].source().x
							- s_b[i_b].target().x, s_b[i_b].source().y
							- s_b[i_b].target().y);
				}
			}
			else
			{
				vec_a = new Point2(s_a[i_a].source().x - s_a[i_a].target().x,
						s_a[i_a].source().y - s_a[i_a].target().y);

				if (or_b_ccw)
				{
					bHA = s_b[i_b].target().orientation(s_a[i_a].target(),
							s_a[i_a].source()) != Point2.ORIENTATION_RIGHT;

					vec_b = new Point2(s_b[i_b].target().x
							- s_b[i_b].source().x, s_b[i_b].target().y
							- s_b[i_b].source().y);
				}
				else
				{
					bHA = s_b[i_b].source().orientation(s_a[i_a].target(),
							s_a[i_a].source()) != Point2.ORIENTATION_RIGHT;

					vec_b = new Point2(s_b[i_b].source().x
							- s_b[i_b].target().x, s_b[i_b].source().y
							- s_b[i_b].target().y);
				}
			}

			//Schnitt testen
			s_a[i_a].intersection(s_b[i_b], inter);
			if (inter.getPoint() != null)
			{
				//if ()
			}
			else
			{
				if (inter.getSegment() != null)
				{
					//TODO beide liegen aufeinander, 2 Punkte einfügen
				}
			}

			//Kreuzprodukt
			double cross = vec_a.x * vec_b.y - vec_a.y * vec_b.x;

			//Abfrage stellen, nach O'Rourke
			if (cross > 0)
			{
				if (bHA)
					i_a = or_a_ccw ? i_a + 1 : (i_a - 1 + m) % m;
				else
					i_b = or_b_ccw ? i_b + 1 : (i_b - 1 + n) % n;
			}
			else
			{
				if (aHB)
					i_b = or_b_ccw ? i_b + 1 : (i_b - 1 + n) % n;
				else
					i_a = or_a_ccw ? i_a + 1 : (i_a - 1 + m) % m;
			}
		}

		return result;
	}


	// ************************************************************************
	/**
	 * Finde den Schnittpunkt zwischen dem Strahl aus Zentrum durch den
	 * Eingabepunkt und einer Kante des konvexen Polygons.
	 * 
	 * @param v
	 *            The input point to check
	 * 
	 * @return The intersection
	 */
	public Point2 projection(
			Point2 v)
	{
		ConvexPolygon2 copyC = new ConvexPolygon2(this);
		PolygonAccess pA = copyC.findCutPoint(v);
		Point2 pnn = pA.nextPoint();
		Point2 pn = pA.prevPoint();
		Point2 pv = pA.prevPoint();
		int or = v.orientation(copyC.center(), pn);
		if (or == Point2.ORIENTATION_COLLINEAR)
			return pn;
		Segment2 le;
		Ray2 av = new Ray2(copyC.center(), v);
		if (or == Point2.ORIENTATION_RIGHT)
		{
			le = new Segment2(pv, pn);
		}
		else
		{
			le = new Segment2(pnn, pn);
		}
		Intersection intersect = new Intersection();
		le.intersection(av, intersect);
		if (intersect.result == Intersection.POINT2)
		{
			return intersect.point2;
		}

		System.out.println("Fehler in preojection");
		return null;

	} // projection


	// ************************************************************************

	/**
	 * Teste, ob Punkt p in dem konvexen Polygon liegt.
	 * 
	 * @param p
	 *            The input point to check
	 * 
	 * @return true, if p is inside the polygon, false else
	 */
	public boolean contains(
			Point2 p)
	{
		PolygonAccess pA = new PolygonAccess(this);
		Point2 first = new Point2(pA.nextPoint());
		Point2 second = new Point2(pA.nextPoint());

		//System.out.println("this Polygon: "+this);

		Point2 q1 = pA.currentPoint();
		Point2 p1 = pA.nextPoint();
		while (!p1.equals(second))
		{
			if (p.orientation(q1, p1) == Point2.ORIENTATION_COLLINEAR)
			{
				if (p.inspectCollinearPoint(q1, p1) == Point2.LIES_ON)
					return false;
			}
			if (p.orientation(q1, p1) == Point2.ORIENTATION_RIGHT)
			{
				return false;
			}

			q1 = p1;
			p1 = pA.nextPoint();
		}
		if (p.orientation(q1, p1) == Point2.ORIENTATION_COLLINEAR)
		{
			if (p.inspectCollinearPoint(q1, p1) == Point2.LIES_ON)
				return false;
		}
		if (p.orientation(q1, p1) == Point2.ORIENTATION_RIGHT)
		{
			return false;
		}

		return true;
	} // contain


	// ************************************************************************

	/**
	 * Calculates, if a convex polygon is inside (or equal to) the polygon
	 * represented by this object.
	 * 
	 * <br>The algorithm takes O(n) time. The calculation uses the algorithm
	 * to calculate the tangents for two convex polygons. If one point is inside
	 * of the polygon and there are no tangents, then true is returned
	 * 
	 * @param convp
	 *            The polygon which is tested to be inside.
	 * 
	 * @return true if it's inside or equal, false if there is at least one
	 *         point outside of this object.
	 */
	public boolean contains(
			ConvexPolygon2 convp)
	{
		if (convp == null)
			return false;
		
		return 
			this.containsOrOn(convp.firstPoint()) != ConvexPolygon2.OUT_POLYGON && 
			Tangent2.getTangents(this, convp) == null;
	} //end: contains(ConvexPolygon2)


	// ************************************************************************

	/**
	 * Teste, ob Punkt p in dem offenen konvexen Polygon liegt.
	 * 
	 * @param p
	 *            The point to check
	 * 
	 * @return true, if the point is inside the open polygon, false else
	 */
	public boolean containsOpen(
			Point2 p)
	{
		PolygonAccess pA = new PolygonAccess(this);
		Point2 q1 = pA.currentPoint();
		Point2 p1 = pA.nextPoint();
		Point2 last = this.lastPoint();
		while (!p1.equals(last))
		{
			if (p.orientation(q1, p1) == Point2.ORIENTATION_RIGHT)
			{
				return false;
			}
			q1 = p1;
			p1 = pA.nextPoint();
		}
		if (p.orientation(q1, p1) == Point2.ORIENTATION_RIGHT)
		{
			return false;
		}
		return true;

	} //containOpen


	// ************************************************************************

	/**
	 * Teste, ob Punkt p in oder auf dem konvexen Polygon liegt.
	 * 
	 * @param p
	 *            The point to check
	 * 
	 * @return {@link anja.geom.Point2#ORIENTATION_COLLINEAR}
	 */
	public int containsOrOn(
			Point2 p)
	{
		double epsilon = 0.09f;
		double dc, dp, d;
		PolygonAccess pA = new PolygonAccess(this);
		Point2 first = new Point2(pA.nextPoint());
		Point2 second = new Point2(pA.nextPoint());
		Point2 center = this.getCenterOfMass();
		dp = p.squareDistance(center);

		//frueher .center(), aber da das immer 0,0 ist, umgestellt.
		Ray2 rpv = new Ray2(center, p);
		if (center.equals(p))
			return IN_POLYGON;	
		
		Segment2 spq;
		Intersection cut = new Intersection();

		Point2 q1 = pA.currentPoint();
		Point2 p1 = pA.nextPoint();
		while (!p1.equals(second))
		{
			if (p.orientation(p1, q1) == Point2.ORIENTATION_COLLINEAR)
			{
				if (p.inspectCollinearPoint(p1, q1) == Point2.LIES_ON)
					return ON_POLYGON;
			}
			spq = new Segment2(p1, q1);
			spq.intersection(rpv, cut);
			if (cut.result == Intersection.POINT2)
			{
				dc = cut.point2.squareDistance(center);
				d = dc - dp;
				//System.out.println("dc: "+dc+" dp: "+dp+" d: "+d);
				if (Math.abs(d) <= epsilon)
					return ON_POLYGON;
				if (d < -epsilon)
					return OUT_POLYGON;
				if (d > epsilon)
					return IN_POLYGON;
			}
			q1 = p1;
			p1 = pA.nextPoint();
		}
		spq = new Segment2(p1, q1);
		if (p.orientation(p1, q1) == Point2.ORIENTATION_COLLINEAR)
		{
			if (p.inspectCollinearPoint(p1, q1) == Point2.LIES_ON)
				return ON_POLYGON;
		}
		spq.intersection(rpv, cut);
		if (cut.result == Intersection.POINT2)
		{
			dc = cut.point2.squareDistance(center);
			d = dc - dp;
			//System.out.println("d: "+d);
			if (d <= epsilon && d >= -epsilon)
				return ON_POLYGON;
			if (d < -epsilon)
				return OUT_POLYGON;
			if (d > epsilon)
				return IN_POLYGON;
		}

		return IN_POLYGON;
	} // containsOrOn


	// ************************************************************************

	/**
	 * Teste, ob Punkt p in oder auf dem konvexen Polygon liegt, wobei das
	 * Polygon eine Vergroesserung oder Verkleinerung von this Polygon mit dem
	 * Faktor Wurzel von (a/b) ist.
	 * 
	 * @param p
	 *            The point to check
	 * @param a
	 *            The numerator
	 * @param b
	 *            The denominator
	 * 
	 * @return {@link anja.geom.ConvexPolygon2#IN_POLYGON}...
	 */
	public int containsOrOn(
			Point2 p,
			double a,
			double b)
	{
		double epsilon = 0.09f;
		double dc, dp, d;
		PolygonAccess pA = new PolygonAccess(this);
		Point2 first = new Point2(pA.nextPoint());
		Point2 second = new Point2(pA.nextPoint());
		dp = p.squareDistance(this.center());

		Ray2 rpv = new Ray2(this.center(), p);
		Segment2 spq;
		Intersection cut = new Intersection();

		Point2 q1 = pA.currentPoint();
		Point2 p1 = pA.nextPoint();
		while (!p1.equals(second))
		{
			spq = new Segment2(p1, q1);
			spq.intersection(rpv, cut);
			if (cut.result == Intersection.POINT2)
			{
				dc = cut.point2.squareDistance(this.center());
				d = dp / dc - a / b;
				//System.out.println("d: "+d);
				if (d <= epsilon && d >= -epsilon)
					return ON_POLYGON;
				if (d < -epsilon || d > epsilon)
					return OUT_POLYGON;
			}
			q1 = p1;
			p1 = pA.nextPoint();
		}
		spq = new Segment2(p1, q1);
		spq.intersection(rpv, cut);
		if (cut.result == Intersection.POINT2)
		{
			dc = cut.point2.squareDistance(this.center());
			d = dp / dc - a / b;
			//System.out.println("d: "+d);
			if (d <= epsilon && d >= -epsilon)
				return ON_POLYGON;
			if (d < -epsilon || d > epsilon)
				return OUT_POLYGON;

		}

		return IN_POLYGON;

	} // containsOrOn


	/**
	 * Liefert ein Points-Access-Objekt zum Zugriff auf das Polygon. Der Pointer
	 * des Points-Access zeigt je nach dem Parameter mode auf einen bestimmten
	 * Punkt des Polygons.
	 * 
	 * <br>Author: Marina Bachran
	 * 
	 * @param mode
	 *            - 0: Punkt ganz rechts - bei mehreren mit gleicher
	 *            X-Koordinate den unteren 1: Punkt ganz rechts - bei mehreren
	 *            mit gleicher X-Koordinate den oberen 2: Punkt ganz links - bei
	 *            mehreren mit gleicher X-Koordinate den unteren 3: Punkt ganz
	 *            links - bei mehreren mit gleicher X-Koordinate den oberen 4:
	 *            Punkt ganz oben - bei mehreren mit gleicher X-Koordinate den
	 *            rechten 5: Punkt ganz oben - bei mehreren mit gleicher
	 *            X-Koordinate den linken 6: Punkt ganz unten - bei mehreren mit
	 *            gleicher X-Koordinate den rechten 7: Punkt ganz unten - bei
	 *            mehreren mit gleicher X-Koordinate den linken
	 * 
	 * @return das Objekt zum Zugriff auf das Polygon
	 */
	public PointsAccess getPointsAccess(
			int mode)
	{
		Point2 actual, next;
		boolean direction = true;
		boolean wrongDirection = false;
		PointsAccess access = new PointsAccess(this);

		// suche den rechtesten Punkt mit einer minimalen y-Koordinate!!!
		actual = access.cyclicNextPoint();
		next = access.cyclicNextPoint();
		switch (mode)
		{
			case 0:
				wrongDirection = (actual.x > next.x)
						|| ((actual.x == next.x) && (actual.y < next.y));
				break;
			case 1:
				wrongDirection = (actual.x > next.x)
						|| ((actual.x == next.x) && (actual.y > next.y));
				break;
			case 2:
				wrongDirection = (actual.x < next.x)
						|| ((actual.x == next.x) && (actual.y < next.y));
				break;
			case 3:
				wrongDirection = (actual.x < next.x)
						|| ((actual.x == next.x) && (actual.y > next.y));
				break;
			case 4:
				wrongDirection = (actual.y > next.y)
						|| ((actual.y == next.y) && (actual.x > next.x));
				break;
			case 5:
				wrongDirection = (actual.y > next.y)
						|| ((actual.y == next.y) && (actual.x < next.x));
				break;
			case 6:
				wrongDirection = (actual.y < next.y)
						|| ((actual.y == next.y) && (actual.x > next.x));
				break;
			case 7:
				wrongDirection = (actual.y < next.y)
						|| ((actual.y == next.y) && (actual.x < next.x));
				break;
			default:
				return null;
		}
		if (wrongDirection)
		{
			direction = false;
			access.cyclicPrevPoint();
			next = access.cyclicPrevPoint();
		} // else: actual.x > next.x
		// -> Richtung wie angenommen, also rechts durch die Liste wandern

		while (true)
		{
			switch (mode)
			{
				case 0:
					wrongDirection = (actual.x > next.x)
							|| ((actual.x == next.x) && (actual.y < next.y));
					break;
				case 1:
					wrongDirection = (actual.x > next.x)
							|| ((actual.x == next.x) && (actual.y > next.y));
					break;
				case 2:
					wrongDirection = (actual.x < next.x)
							|| ((actual.x == next.x) && (actual.y < next.y));
					break;
				case 3:
					wrongDirection = (actual.x < next.x)
							|| ((actual.x == next.x) && (actual.y > next.y));
					break;
				case 4:
					wrongDirection = (actual.y > next.y)
							|| ((actual.y == next.y) && (actual.x > next.x));
					break;
				case 5:
					wrongDirection = (actual.y > next.y)
							|| ((actual.y == next.y) && (actual.x < next.x));
					break;
				case 6:
					wrongDirection = (actual.y < next.y)
							|| ((actual.y == next.y) && (actual.x > next.x));
					break;
				case 7:
					wrongDirection = (actual.y < next.y)
							|| ((actual.y == next.y) && (actual.x < next.x));
					break;
				default:
					return null;
			}
			if (wrongDirection)
				break;
			actual = next;
			next = direction ? access.cyclicNextPoint() : access
					.cyclicPrevPoint();
		}

		// gehe wieder an die richtige Stelle, wo der gesuchte Punkt ist...
		actual = (!direction) ? access.cyclicNextPoint() : access
				.cyclicPrevPoint();

		return access;
	}


	/**
	 * In dieser Methode wird die Minkowskisumme von diesem Polygon und poly
	 * berechnet. Beide Polygone sind konvex.
	 * 
	 * <br>Author: Marina Bachran
	 * 
	 * @param poly
	 *            Das zweite Polygon, mit dem die Minkowski-Summe berechnet
	 *            werden soll.
	 * 
	 * @return das Ergebnis-Polygon
	 */
	public ConvexPolygon2 minkowskiSum(
			ConvexPolygon2 poly)
	{
		/* Der Algorithmus Minkowski-Summe besteht aus in dieser 4 Phasen. In
		 * jeder Phase werden deltaX und deltaY berechnet, wobei deltaX die
		 * Differenz zwischen den X Koordinaten zweier aufeinander folgenden
		 * Punkten eines Polygons ist. deltaY wird analog berechnet. Die
		 * Berechnung der beiden deltas erfolgt fuer beide Polygone. Ausserdem
		 * werden die Steigungen zwischen den Punkten berechnet. Anhand der
		 * resultierenden Werte wird entschieden, welcher Punkt als naechstes
		 * in die Berechnung einfliesst. */
		ConvexPolygon2 polygon[] = new ConvexPolygon2[2];
		polygon[0] = new ConvexPolygon2(this);
		polygon[0].setOrientation(Polygon2.ORIENTATION_LEFT);
		polygon[1] = new ConvexPolygon2(poly);
		polygon[1].setOrientation(Polygon2.ORIENTATION_LEFT);
		ConvexPolygon2 minkSum = new ConvexPolygon2();
		PointsAccess pAccess[] = new PointsAccess[2];
		pAccess[0] = polygon[0].getPointsAccess(0);
		pAccess[1] = polygon[1].getPointsAccess(0);
		Point2 actPoint[] = new Point2[2];
		Point2 newPoint[] = new Point2[2];
		double deltaX[] = new double[2];
		double deltaY[] = new double[2];
		boolean firstPoly = true;
		int polyNum, polyOther;

		// Initialisiere die Werte fuer die Schleife
		actPoint[0] = pAccess[0].currentPoint();
		actPoint[1] = pAccess[1].currentPoint();
		newPoint[0] = pAccess[0].cyclicNextPoint();
		newPoint[1] = pAccess[1].cyclicNextPoint();
		deltaX[0] = newPoint[0].x - actPoint[0].x;
		deltaY[0] = newPoint[0].y - actPoint[0].y;
		deltaX[1] = newPoint[1].x - actPoint[1].x;
		deltaY[1] = newPoint[1].y - actPoint[1].y;

		// Durchlaufe alle 4 Phasen
		int phase = 1;
		while (phase < 5)
		{
			switch (phase)
			{
				case 1:
					if ((deltaY[0] <= 0) && (deltaY[1] <= 0))
					{
						phase++;
						continue;
					}
					firstPoly = ((deltaY[0] > 0) && (deltaY[1] > 0)) ? ((deltaX[0] / deltaY[0]) > (deltaX[1] / deltaY[1]))
							: (deltaY[0] > 0);
					break;
				case 2:
					if ((deltaX[0] >= 0) && (deltaX[1] >= 0))
					{
						phase++;
						continue;
					}
					firstPoly = ((deltaX[0] < 0) && (deltaX[1] < 0)) ? ((deltaY[0] / deltaX[0]) < (deltaY[1] / deltaX[1]))
							: (deltaX[0] < 0);
					break;
				case 3:
					if ((deltaY[0] >= 0) && (deltaY[1] >= 0))
					{
						phase++;
						continue;
					}
					firstPoly = ((deltaY[0] < 0) && (deltaY[1] < 0)) ? ((deltaY[0] / deltaX[0]) < (deltaY[1] / deltaX[1]))
							: (deltaY[0] < 0);
					break;
				case 4:
					if ((deltaX[0] <= 0) && (deltaX[1] <= 0))
					{
						phase++;
						continue;
					}
					firstPoly = ((deltaX[0] > 0) && (deltaX[1] > 0)) ? ((deltaY[0] / deltaX[0]) < (deltaY[1] / deltaX[1]))
							: (deltaX[0] > 0);
					break;
			}
			polyNum = firstPoly ? 0 : 1;
			polyOther = firstPoly ? 1 : 0;
			minkSum.addPoint(newPoint[polyNum].x + actPoint[polyOther].x,
					newPoint[polyNum].y + actPoint[polyOther].y);
			actPoint[polyNum] = newPoint[polyNum];
			newPoint[polyNum] = pAccess[polyNum].cyclicNextPoint();
			deltaX[polyNum] = newPoint[polyNum].x - actPoint[polyNum].x;
			deltaY[polyNum] = newPoint[polyNum].y - actPoint[polyNum].y;
		}
		minkSum.removeRedundantPoints();
		return minkSum;
	}


	/**
	 * Epsilon
	 */
	private double	_TRI_EPSILON	= -4e-4;


	/**
	 * Berechnet, ob ein Punkt P innerhalb oder auf dem Rand des Dreiecks, das
	 * aus den ersten drei Punkten des Polygons besteht, liegt. Diese Methode
	 * ist deutlich schneller, als die sonst verfügbaren generischen
	 * Point-In-Polygon-Tests. Dabei wird das Dreieck als Ebene aufgefaßt mit
	 * den zwei Seiten AC und AB des Dreiecks als Basisvektoren, und dann die
	 * Zerlegung von P in diese Vektoren gesucht (P = A + u*(C-A) + v*(B-A)).
	 * Sind u und v >= 0 und (u+v)<=1, liegt P auf dem Dreieck. Am einfachsten
	 * nachzulesen unter http://www.blackpawn.com/texts/pointinpoly
	 * 
	 * <br>Author: Jörg Wegener
	 * 
	 * @param P
	 *            Punkt, der überprüft werden soll
	 * @param border
	 *            auf true setzen, wenn der Rand mitüberprüft werden soll
	 * 
	 * @return true, wenn der Punkt innerhalb oder auf dem Rand des Polygons
	 *         liegt
	 */
	public boolean inTriangle(
			Point2 P,
			boolean border)
	{
		if (_points.length() == 0)
			return false;
		if (_points.length() == 1)
			return (P.equals((Point2) _points.firstValue()));
		if (_points.length() == 2)
			return (new Segment2((Point2) _points.firstValue(),
					(Point2) _points.getValueAt(2)).liesOn(P));

		Point2 A = (Point2) _points.firstValue();
		Point2 B = (Point2) _points.getValueAt(1);
		Point2 C = (Point2) _points.getValueAt(2);
		Point2 v0 = new Point2(C.getX() - A.getX(), C.getY() - A.getY());
		Point2 v1 = new Point2(B.getX() - A.getX(), B.getY() - A.getY());
		Point2 v2 = new Point2(P.getX() - A.getX(), P.getY() - A.getY());

		double dot00 = v0.getX() * v0.getX() + v0.getY() * v0.getY();
		double dot01 = v0.getX() * v1.getX() + v0.getY() * v1.getY();
		double dot02 = v0.getX() * v2.getX() + v0.getY() * v2.getY();
		double dot11 = v1.getX() * v1.getX() + v1.getY() * v1.getY();
		double dot12 = v1.getX() * v2.getX() + v1.getY() * v2.getY();

		double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
		double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		double v = (dot00 * dot12 - dot01 * dot02) * invDenom;

		// Check if point is in triangle
		if (border)
			return (u >= _TRI_EPSILON) && (v >= _TRI_EPSILON)
					&& (u + v <= 1 - _TRI_EPSILON);
		return (u > 0) && (v > 0) && (u + v < 1);
	}


	/**
	 * wie inTriangle(P, false)
	 * 
	 * @param P
	 *            The point to check
	 * 
	 * @return true, if inside the triangle, false else
	 * 
	 * @see anja.geom.ConvexPolygon2#inTriangle(Point2, boolean)
	 */
	public boolean inTriangle(
			Point2 P)
	{
		return inTriangle(P, false);
	}


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Convex Hull
	 * 
	 * @param plist
	 *            The point list
	 * 
	 * @return The (convex hull) point list
	 * 
	 */
	private static Point2List _convexHull(
			Point2List plist)
	{
		if (plist.empty())
			return new ConvexPolygon2();

		/* Punkte lexikographisch sortieren */
		PointComparitor lexico = new PointComparitor();
		lexico.setOrder(PointComparitor.X_ORDER);
		Point2List sortPoints = new Point2List(plist);
		sortPoints._points.sort(lexico, Sorter.ASCENDING);

		/* Teilhuellen bilden */
		Point2List leftHull = _leftHull(sortPoints);
		Point2List rightHull = _rightHull(sortPoints);

		/* fuege leftHull und rightHull zusammen */

		if (rightHull.lastPoint().equals(leftHull.firstPoint()))
			leftHull.removeFirstPoint();
		if (rightHull.firstPoint().equals(leftHull.lastPoint()))
			leftHull.removeLastPoint();

		rightHull.appendCopy(leftHull);

		return rightHull;

	} // _convexHull


	// ************************************************************************

	/**
	 * Berechnung der konvexen Hülle eines einfachen Polygons in O(n)
	 * 
	 * <br>Entfernt nach der Berechnung gleiche, aufeinanderfolgende Punkte, sodass
	 * kein Punkt mehrfach hintereinander kommt. Wenn dies im Originalpolygon so war,
	 * so wird dennoch dieser Schritt vorgenommen.
	 * 
	 * @param poly
	 *            The input polygon (simple)
	 * 
	 * @return The (convex) point list
	 */
	private Point2List _convexHullForSimplePolygons(
			Polygon2 poly)
	{
		//No polygon given
		if (poly == null || poly.length() == 0)
		{
			return null;
		}

		//Only 1, 2 or 3 points given.
		if (poly.length() <= 3)
		{
			return poly;
		}

		//From now on, we at least have a more complicated structure than a triangle
		byte orientation_save = poly.getOrientation();
		poly.setOrientation(ORIENTATION_LEFT);
		
		Point2[] points = poly.toArray();
		int n = points.length;
		Point2[] results = new Point2[2 * n + 1];
		
		int bot = n - 2;
		int top = bot + 3;

		results[bot] = points[2];
		results[top] = points[2];
		
		if ((new Segment2(points[0], points[1])).orientation(points[2]) == Point2.ORIENTATION_LEFT)
		{
			results[bot+1] = points[0];
			results[bot+2] = points[1];			
		}
		else
		{
			results[bot+1] = points[1];
			results[bot+2] = points[0];
		}
		
		
		for (int i=3; i < n; ++i)
		{
			if ((new Segment2(results[bot], results[bot+1])).orientation(points[i]) == Point2.ORIENTATION_LEFT &&
				(new Segment2(results[top-1], results[top])).orientation(points[i]) == Point2.ORIENTATION_LEFT
			)
			{
				continue;
			}
			
			while ((new Segment2(results[bot], results[bot+1])).orientation(points[i]) != Point2.ORIENTATION_LEFT)
			{
				++bot;
			}
			
			results[--bot] = points[i];
			
			
			while ((new Segment2(results[top-1], results[top])).orientation(points[i]) != Point2.ORIENTATION_LEFT)
			{
				--top;
			}
			
			results[++top] = points[i];
		}
		
		int h;
		Point2List pol = new Point2List();
		
		for (h = 0; h <= (top-bot); h++)
		{
			if (!results[bot+h].equals(pol.lastPoint()))
			{
				pol.addPoint(results[bot+h]);
			}
		}
		
		if (pol.firstPoint().equals(pol.lastPoint()))
		{
			pol.removeLastPoint();
		}
		
		poly.setOrientation(orientation_save);
		
		return pol;		
	} // _convexHullForSimplePolygons


	// ************************************************************************

	/**
	 * linke Huelle bilden
	 * 
	 * @param plist
	 *            The input point list
	 * 
	 * @return The left convex hull point list
	 * 
	 */
	private static Point2List _leftHull(
			Point2List plist)
	{

		float MinY, MaxY, iy;
		Point2List result = new Point2List();

		PointsAccess Access = new PointsAccess(plist);
		Point2 p = Access.nextPoint();
		MinY = p.y;
		MaxY = MinY;
		result.addPoint(p);

		while (Access.hasNextPoint())
		{
			p = Access.nextPoint();
			iy = p.y;
			if (iy < MinY)
			{
				result.addPoint(p); /* hinten einfuegen*/
				MinY = iy;
			}
			else if (iy > MaxY)
			{
				result.insertFront(p); /* vorne einfuegen*/
				MaxY = iy;
			}
		}
		_removeReflex(result);

		return result;
	} // _leftHull


	// ************************************************************************

	/**
	 * rechte Huelle bilden
	 * 
	 * @param plist
	 *            The input point list
	 * 
	 * @return The left convex hull point list
	 * 
	 */
	private static Point2List _rightHull(
			Point2List plist)
	{

		float MinY, MaxY, iy;
		Point2List result = new Point2List();

		PointsAccess Access = new PointsAccess(plist);
		Point2 p = Access.prevPoint();
		MinY = p.y;
		MaxY = MinY;
		result.addPoint(p);

		while (Access.hasPrevPoint())
		{
			p = Access.prevPoint();
			iy = p.y;
			if (iy < MinY)
			{
				result.insertFront(p); /* vorne einfuegen*/
				MinY = iy;
			}
			else if (iy > MaxY)
			{
				result.addPoint(p); /* hinten einfuegen*/
				MaxY = iy;
			}
		}
		_removeReflex(result);

		return result;
	} // _rightHull


	// ************************************************************************

	/**
	 * reflexe Ecken entfernen, die Eingabe wird verändert!
	 * 
	 * @param plist
	 *            The input list
	 */
	private static void _removeReflex(
			Point2List plist)
	{

		if (plist.length() <= 2)
			return;
		/* mehr als zwei Punkte sind da */
		PointsAccess pA = new PointsAccess(plist);
		Point2 first = pA.nextPoint();
		Point2 p = first;

		PointsAccess qA = new PointsAccess(pA);
		Point2 q = qA.nextPoint();

		PointsAccess rA = new PointsAccess(qA);
		Point2 r = rA.nextPoint();

		while (r != null)
		{
			if (r.orientation(p, q) == Point2.ORIENTATION_LEFT)
			{
				p = pA.nextPoint();
				q = qA.nextPoint();
				r = rA.nextPoint();
			}
			else
			{
				qA.removePoint();
				if (p == first)
				{
					q = qA.nextPoint();
					r = rA.nextPoint();
				}
				else
				{
					q = qA.prevPoint();
					p = pA.prevPoint();
				}
			}
		}
	} // _removeReflex

	// ************************************************************************

} // class convexPolygon2

