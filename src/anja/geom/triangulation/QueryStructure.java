package anja.geom.triangulation;


import java.util.Vector;

import anja.geom.Intersection;
import anja.geom.Line2;
import anja.geom.Point2;
import anja.geom.Segment2;


/**
 * Hilfsklasse fuer die Polygon-Triangulierung in
 * {@link anja.geom.triangulation.Polygon2Triangulation}.
 * 
 * @version 0.9 23.06.2003
 * @author Sascha Ternes
 */

public class QueryStructure
		implements java.io.Serializable
{

	// *************************************************************************
	// Private variables
	// *************************************************************************

	// Wert fuer unendliche Koordinaten:
	private float	_max	= 500.0f;

	// der Startknoten:
	private Node	_start;

	// Hilfsvariable zur Knotenrueckgabe bei der Suche:
	private Node	_node;


	// *************************************************************************
	// Inner classes
	// *************************************************************************

	/**
	 * Klasse fuer einen Suchknoten.
	 */
	class Node
			implements java.io.Serializable
	{

		// Typkonstanten:
		static final int	SINK	= 0;
		static final int	X		= 1;
		static final int	Y		= 2;

		// Verkettung:
		Node				link1;
		Node				link2;
		// Typ und Objekt:
		int					type;
		Object				object;


		/**
		 * Erzeugt einen neuen Knoten des angegebenen Typs.
		 * 
		 * @param type
		 *            Der Typ des Knotens
		 */
		Node(
				int type)
		{
			link1 = null;
			link2 = null;
			this.type = type;
			object = null;
		} // Node


		/**
		 * Testet den spezifizierten Punkt gegen das Segment und liefert den
		 * naechsten Node.
		 * 
		 * @param point
		 *            Der Punkt
		 * 
		 * @return Der nächste Knoten
		 * 
		 * @throws anja.geom.triangulation.ExistingPointException
		 */
		Node branchX(
				Point2 point)
				throws ExistingPointException
		{
			Segment2 seg = (Segment2) object;
			Point2 a = seg.source();
			Point2 b = seg.target();
			if (point.equals(a) || point.equals(b))
			{
				throw new ExistingPointException();
			} // if
			if ((a.y < b.y) || ((a.y == b.y) && (a.x < b.x)))
			{
				if (point.orientation(a, b) == Point2.ORIENTATION_LEFT)
				{
					return link1;
				}
				else // if
				if (point.orientation(a, b) == Point2.ORIENTATION_RIGHT)
				{
					return link2;
				}
				else
				{ // if
					return link1;
				} // else
			}
			else
			{ // if
				if (point.orientation(b, a) == Point2.ORIENTATION_LEFT)
				{
					return link1;
				}
				else // if
				if (point.orientation(b, a) == Point2.ORIENTATION_RIGHT)
				{
					return link2;
				}
				else
				{ // if
					return link1;
				} // else
			} // else
		} // branchX


		/**
		 * Testet den spezifizierten Punkt gegen die y-Koordinate und liefert
		 * den naechsten Node.
		 * 
		 * @param point
		 *            Der Punkt
		 * 
		 * @return Der nächste Knoten
		 * 
		 * @throws anja.geom.triangulation.ExistingPointException
		 */
		Node branchY(
				Point2 point)
				throws ExistingPointException
		{
			Point2 o = (Point2) object;
			if (point.equals(o))
			{
				throw new ExistingPointException();
			} // if
			if ((point.y > o.y) || ((point.y == o.y) && (point.x > o.x)))
			{
				return link1;
			}
			else
			{ // if
				return link2;
			} // else
		} // branchY


		/**
		 * Liefert eine textuelle Repraesentation.
		 * 
		 * @return Die textuelle Repräsentation
		 */
		public String toString()
		{
			String s = "\n";
			switch (type)
			{
				case X:
					s += "X-Node [";
					s += (Segment2) object;
					s += "]";
					s += link1;
					s += link2;
					break;
				case Y:
					s += "Y-Node [";
					s += (Point2) object;
					s += "]";
					s += link1;
					s += link2;
					break;
				default:
					s += "Sink [";
					s += (Trapezoid) object;
					s += "]";
			} // switch
			return s;
		} // toString

	} // Node


	// *************************************************************************
	// Constructors
	// *************************************************************************

	/**
	 * Erzeugt eine leere Suchstruktur. Repraesentiert wird ein einziges
	 * Trapezoid, das unendlich gross ist.
	 */
	QueryStructure()
	{

		Trapezoid t = new Trapezoid(new Point2(-_max, _max), new Point2(_max,
				_max), new Point2(-_max, -_max), new Point2(_max, -_max));
		_start = new Node(Node.SINK);
		_start.object = t;
		t.node = _start;
	} // QueryStructure


	// *************************************************************************
	// Public methods
	// *************************************************************************

	/**
	 * Liefert alle Trapezoiden.
	 * 
	 * @return Die Menge aller Trapezoiden
	 */
	public Vector getTrapezoids()
	{

		return _getTrapezoids(_start);

	} // getTrapezoids


	/**
	 * Liefert den Wert, der fuer unendliche Koordinaten bei Trapezoiden benutzt
	 * wird.
	 * 
	 * @return Wert für unendliche Koordinaten
	 */
	public float getInfinity()
	{

		return _max;

	} // getInfinity


	/**
	 * Startet eine Suche und liefert das Trapezoid, das den spezifizierten
	 * Punkt enthaelt.
	 * 
	 * @param point
	 *            der zu suchende Punkt
	 * 
	 * @return das Trapezoid, das den gesuchten Punkt enthaelt; falls ueberhaupt
	 *         keine Segmente und damit Trapezoide existieren, wird
	 *         <code>null<code> zurueckgeliefert
	 * 
	 * @throws ExistingPointException
	 *             wenn der gesuchte Punkt nicht innerhalb eines Trapezoids
	 *             liegt, sondern ein Eckpunkt eines Trapezoids selbst ist
	 */
	public Trapezoid query(
			Point2 point)
			throws ExistingPointException
	{

		_node = _start;
		return _query(point);

	} // query


	/**
	 * Liefert eine textuelle Repraesentation.
	 * 
	 * @return ein String, der die Suchstruktur repraesentiert
	 */
	public String toString()
	{

		String s = "QueryStructure:";
		s += _start;
		return s;

	} // toString


	// *************************************************************************
	// Package methods
	// *************************************************************************

	/**
	 * Fuegt das spezifizierte Segment in die Suchstruktur ein.
	 * 
	 * @param segment
	 *            Das einzufügende Segment
	 */
	void insert(
			Segment2 segment)
	{

		// System.out.println( "Fuege " + segment + " ein..." );
		// Ermittle die Endpunkte des Segments geordnet:
		Point2 a = segment.source();
		Point2 b = segment.target();
		if ((a.y < b.y) || ((a.y == b.y) && (a.x < b.x)))
		{
			a = b;
			b = segment.source();
		} // if
			// Einfuegen der Endpunkte:
		_insert(a);
		_insert(b);
		/*
		Vector _trapezoids = getTrapezoids();
		System.out.println( "*** Current trapezoidation:" );
		while ( _trapezoids.size() > 0 ) {
		Trapezoid t = (Trapezoid) _trapezoids.get( 0 );
		Point2 p = t.getTopLeft();
		for ( int j = 1; j < _trapezoids.size(); j++ ) {
		Trapezoid r = (Trapezoid) _trapezoids.get( j );
		Point2 q = r.getTopLeft();
		if ( ( q.y > p.y ) || ( ( q.y == p.y ) && ( q.x < p.x ) ) ) {
		t = r;
		p = q;
		} // if
		} // for
		_trapezoids.remove( t );
		System.out.println( t );
		Trapezoid[] tr = t.getTopNeighbors();
		System.out.println( "  --> " + tr[0] );
		System.out.println( "  --> " + tr[1] );
		tr = t.getBottomNeighbors();
		System.out.println( "  --> " + tr[0] );
		System.out.println( "  --> " + tr[1] );
		} // while
		*/
		// teile alle durchschnittenen Trapezoiden:
		_splitAllTrapezoids(a, b, segment);

	} // insert


	// *************************************************************************
	// Private methods
	// *************************************************************************

	/**
	 * Liefert das Trapezoid, das den gesuchten Punkt enthaelt. Die Suche
	 * beginnt ab _node.
	 * 
	 * @param point
	 *            Das gesuchte Punkt
	 * 
	 * @return Das Trapezoid, das den Punkt enthält
	 * 
	 * @throws anja.geom.triangulation.ExistingPointException
	 */
	private Trapezoid _query(
			Point2 point)
			throws ExistingPointException
	{

		while (true)
		{
			switch (_node.type)
			{
				case Node.X:
					_node = _node.branchX(point);
					break;
				case Node.Y:
					_node = _node.branchY(point);
					break;
				default:
					return (Trapezoid) _node.object;
			} // switch
		} // while

	} // _query


	/**
	 * Liefert alle Trapezoiden ab einem gegebenen Knoten.
	 * 
	 * @param node
	 *            Der Knoten
	 * 
	 * @return Liste aller folgenden Knoten (Vector)
	 */
	private Vector _getTrapezoids(
			Node node)
	{

		if (node.type == Node.SINK)
		{
			Vector v = new Vector();
			v.add((Trapezoid) node.object);
			return v;
		}
		else
		{ // if
			Vector v = _getTrapezoids(node.link1);
			v.addAll(_getTrapezoids(node.link2));
			return v;
		} // else

	} // _getTrapezoids


	/**
	 * Fuegt einen Punkt in die Suchstruktur ein. Splittet ein Trapez am Punkt
	 * auf.
	 * 
	 * @param p
	 *            der einzufügende Punkt
	 */
	private void _insert(
			Point2 p)
	{

		// Ermittle zugehoeriges Trapezoid und neue Eckpunkte:
		try
		{
			Trapezoid trapez = query(p); // liefert auch _node!
			// System.out.println( "Fuege " + p + " ein." );
			Point2[] t = trapez.getPoints();
			Line2 l_a = new Line2(p.x, p.y, p.x + 1, p.y);
			Line2 l_t1 = new Line2(t[0], t[2]);
			Line2 l_t2 = new Line2(t[1], t[3]);
			// Trapezoid in p teilen:
			Intersection i = new Intersection();
			Point2 l1 = l_a.intersection(l_t1, i);
			Point2 l2 = l_a.intersection(l_t2, i);
			if (l1 == null)
				System.out.println("Warning: l1 is null !!!");
			if (l2 == null)
				System.out.println("Warning: l2 is null !!!");
			Trapezoid trapez1 = new Trapezoid(t[0], t[1], l1, l2);
			Trapezoid trapez2 = new Trapezoid(l1, l2, t[2], t[3]);
			// Trapezoide untereinander verbinden:
			Trapezoid[] tops = trapez.getTopNeighbors();
			Trapezoid[] bots = trapez.getBottomNeighbors();
			trapez1.linkTopTo(tops);
			trapez1.linkBottomTo(trapez2);
			trapez2.linkTopTo(trapez1);
			trapez2.linkBottomTo(bots);

			// Verbindung von oben:
			if (trapez.topNeighborCount() > 0)
			{
				Trapezoid[] tr = tops[0].getBottomNeighbors();
				Trapezoid[] tn = new Trapezoid[2];
				if (tr[0] == trapez)
				{
					tn[0] = trapez1;
					tn[1] = tr[1];
				}
				else
				{ // if
					tn[0] = tr[0];
					tn[1] = trapez1;
				} // else
				tops[0].linkBottomTo(tn);
				if (tops[1] != null)
				{
					tr = tops[1].getBottomNeighbors();
					tn = new Trapezoid[2];
					if (tr[0] == trapez)
					{
						tn[0] = trapez1;
						tn[1] = tr[1];
					}
					else
					{ // if
						tn[0] = tr[0];
						tn[1] = trapez1;
					} // else
					tops[1].linkBottomTo(tn);
				} // if
			} // if

			// Verbindung von unten:
			if (trapez.bottomNeighborCount() > 0)
			{
				Trapezoid[] tr = bots[0].getTopNeighbors();
				Trapezoid[] tn = new Trapezoid[2];
				if (tr[0] == trapez)
				{
					tn[0] = trapez2;
					tn[1] = tr[1];
				}
				else
				{ // if
					tn[0] = tr[0];
					tn[1] = trapez2;
				} // else
				bots[0].linkTopTo(tn);
				if (bots[1] != null)
				{
					tr = bots[1].getTopNeighbors();
					tn = new Trapezoid[2];
					if (tr[0] == trapez)
					{
						tn[0] = trapez2;
						tn[1] = tr[1];
					}
					else
					{ // if
						tn[0] = tr[0];
						tn[1] = trapez2;
					} // else
					bots[1].linkTopTo(tn);
				} // if
			} // if

			// Trapezoidknoten wird Y-Knoten:
			_node.type = Node.Y;
			_node.object = p;
			// verbinde mit neuen Trapezoiden:
			Node new1 = new Node(Node.SINK);
			Node new2 = new Node(Node.SINK);
			new1.object = trapez1;
			new2.object = trapez2;
			trapez1.node = new1;
			trapez2.node = new2;
			_node.link1 = new1;
			_node.link2 = new2;
		}
		catch (ExistingPointException epe)
		{ // try
			// System.out.println( p + " ist bereits enthalten." );
		} // catch

	} // _insert


	/**
	 * Sucht die vom Segment durchschnittenen Trapezoide und teilt sie. Punkt a
	 * ist der obere Segmentpunkt.
	 * 
	 * @param a
	 *            Der obere Segmentpunkt
	 * @param b
	 *            Der untere Segmentpunkt
	 * @param segment
	 *            Das Schnittsegment
	 */
	private void _splitAllTrapezoids(
			Point2 a,
			Point2 b,
			Segment2 segment)
	{

		Trapezoid first = _findUpperTrapezoid(a, b);
		Trapezoid last = _findLowerTrapezoid(a, b);
		if (first.topDegenerated())
			System.out.println("Warning: first is topD !!!");
		if (first.bottomDegenerated())
			System.out.println("Warning: first is botD !!!");
		if (last.topDegenerated())
			System.out.println("Warning: last is topD !!!");
		if (last.bottomDegenerated())
			System.out.println("Warning: last is botD !!!");
		// System.out.println( "first: " + first );
		// System.out.println( "last: " + last );
		Trapezoid next = null;
		Trapezoid neighbor = first;
		Trapezoid middle_trapez = null;
		int middle_flag = 0;
		Trapezoid[] tn = null;
		Trapezoid[] tm = null;
		Line2 seg = new Line2(a, b);
		Intersection is = new Intersection();
		do
		{
			next = neighbor;
			if (next == null)
			{
				System.out.println("Warning: next is null !!!");
				return;
			} // if
				// System.out.println( "-->" + next );
			if (next.topDegenerated())
				System.out.println("[topD] - " + next);
			if (next.bottomDegenerated())
				System.out.println("[botD] - " + next);
			// Trapezoidpunkte:
			Point2[] t = next.getPoints();
			// Nachbarn:
			Trapezoid[] tops = next.getTopNeighbors();
			Trapezoid[] bots = next.getBottomNeighbors();
			// Schnittpunkte:
			Point2 s1 = seg.intersection(new Line2(t[0], t[1]), is);
			Point2 s2 = seg.intersection(new Line2(t[2], t[3]), is);
			if (s1 == null)
				System.out.println("Warning: s1 is null !!!");
			if (s2 == null)
				System.out.println("Warning: s2 is null !!!");
			if (s1.equals(a))
				s1 = a;
			if (s2.equals(b))
				s2 = b;
			// Trapezoid teilen:
			Trapezoid trapez1 = new Trapezoid(t[0], s1, t[2], s2);
			Trapezoid trapez2 = new Trapezoid(s1, t[1], s2, t[3]);
			// Flags fuer degenerierte Trapezoide:
			int t_deg = 0;
			if (trapez1.topDegenerated())
				t_deg = 1;
			else if (trapez2.topDegenerated())
				t_deg = 2;
			int b_deg = 0;
			if (trapez1.bottomDegenerated())
				b_deg = 1;
			else if (trapez2.bottomDegenerated())
				b_deg = 2;
			// Array der beiden Trapezoide fuer Nachbarverknuepfung:
			Trapezoid[] tr = { trapez1, trapez2 };

			// Trapezoide nach unten verbinden:
			switch (next.topNeighborCount())
			{
				case 1:
					Trapezoid trap = next.getTopNeighbor();
					if (t_deg == 0)
					{
						trap.linkBottomTo(tr);
					}
					else
					{ // if
						tn = trap.getBottomNeighbors();
						tm = new Trapezoid[2];
						if (tn[0] == next)
						{
							if (t_deg != 1)
							{
								tm[0] = trapez1;
							}
							else
							{ // if
								tm[0] = trapez2;
							} // else
							tm[1] = tn[1];
						}
						else
						{ // if
							if (t_deg != 1)
							{
								tm[1] = trapez1;
							}
							else
							{ // if
								tm[1] = trapez2;
							} // else
							tm[0] = tn[0];
						} // else
						trap.linkBottomTo(tm);
					} // else
					if (t_deg != 1)
						trapez1.linkTopTo(trap);
					if (t_deg != 2)
						trapez2.linkTopTo(trap);
					break;
				case 2:
					tm = new Trapezoid[2];
					// Einbau eines vorher gespeicherten "mittleren Trapezoids":
					if (middle_trapez != null)
					{
						tn = tops[0].getBottomNeighbors();
						if (tn[0] == next)
						{
							tops[0].linkBottomTo(trapez1);
						}
						else
						{ // if
							tm[0] = tn[0];
							tm[1] = trapez1;
							tops[0].linkBottomTo(tm);
						} // else
						tm = new Trapezoid[2];
						tn = tops[1].getBottomNeighbors();
						tm[0] = trapez2;
						tm[1] = tn[1];
						tops[1].linkBottomTo(tm);
						tm = new Trapezoid[2];
						// middle gehoert zu trapez1:
						if (middle_flag == 1)
						{
							middle_trapez.linkBottomTo(trapez1);
							tm[0] = tops[0];
							tm[1] = middle_trapez;
							trapez1.linkTopTo(tm);
							trapez2.linkTopTo(tops[1]);
							// middle gehoert zu trapez2:
						}
						else
						{ // if
							middle_trapez.linkBottomTo(trapez2);
							tm[0] = middle_trapez;
							tm[1] = tops[1];
							trapez1.linkTopTo(tops[0]);
							trapez2.linkTopTo(tm);
						} // else
							// middle-Reset:
						middle_trapez = null;
						middle_flag = 0;
					}
					else // if
					// Trapez 1 bekommt zwei Nachbarn:
					if (s1.x > tops[0].getBottomRight().x)
					{
						tn = tops[0].getBottomNeighbors();
						if (tn[0] == next)
						{
							tm[0] = trapez1;
							tm[1] = tn[1];
						}
						else
						{ // if
							tm[0] = tn[0];
							tm[1] = trapez1;
						} // else
						tops[0].linkBottomTo(tm);
						tops[1].linkBottomTo(tr);
						trapez1.linkTopTo(tops);
						trapez2.linkTopTo(tops[1]);
						// Trapez 2 bekommt zwei Nachbarn:
					}
					else if (s1.x < tops[0].getBottomRight().x)
					{ // if
						tn = tops[1].getBottomNeighbors();
						if (tn[0] == next)
						{
							tm[0] = trapez2;
							tm[1] = tn[1];
						}
						else
						{ // if
							tm[0] = tn[0];
							tm[1] = trapez2;
						} // else
						tops[0].linkBottomTo(tr);
						tops[1].linkBottomTo(tm);
						trapez1.linkTopTo(tops[0]);
						trapez2.linkTopTo(tops);
						// Aufteilung exakt 1-1:
					}
					else
					{ // if
						tn = tops[0].getBottomNeighbors();
						if (tn[0] == next)
						{
							tm[0] = trapez1;
							tm[1] = tn[1];
						}
						else
						{ // if
							tm[0] = tn[0];
							tm[1] = trapez1;
						} // else
						tops[0].linkBottomTo(tm);
						tn = tops[1].getBottomNeighbors();
						tm = new Trapezoid[2];
						if (tn[0] == next)
						{
							tm[0] = trapez2;
							tm[1] = tn[1];
						}
						else
						{ // if
							tm[0] = tn[0];
							tm[1] = trapez2;
						} // else
						tops[1].linkBottomTo(tm);
						trapez1.linkTopTo(tops[0]);
						trapez2.linkTopTo(tops[1]);
					} // else
			} // switch

			// Trapezoide nach oben verbinden:
			switch (next.bottomNeighborCount())
			{
				case 0:
					// Schleife hiernach beenden:
					neighbor = null;
					System.out.println("  noNeighbor - new next: " + neighbor);
					break;
				case 1:
					neighbor = next.getBottomNeighbor();
					tn = neighbor.getTopNeighbors();
					if (b_deg == 0)
					{
						if (neighbor.topNeighborCount() < 2)
						{
							neighbor.linkTopTo(tr);
						}
						else
						{ // if
							// ggf. "mittleres Trapezoid" zwischenspeichern:
							tm = new Trapezoid[2];
							if (tn[0] == next)
							{
								tm[0] = trapez1;
								tm[1] = tn[1];
								middle_trapez = trapez2;
								middle_flag = 2;
							}
							else
							{ // if
								tm[0] = tn[0];
								tm[1] = trapez2;
								middle_trapez = trapez1;
								middle_flag = 1;
							} // else
							neighbor.linkTopTo(tm);
						} // else
					}
					else
					{ // if
						tm = new Trapezoid[2];
						if (tn[0] == next)
						{
							if (b_deg != 1)
							{
								tm[0] = trapez1;
							}
							else
							{ // if
								tm[0] = trapez2;
							} // else
							tm[1] = tn[1];
						}
						else
						{ // if
							if (b_deg != 1)
							{
								tm[1] = trapez1;
							}
							else
							{ // if
								tm[1] = trapez2;
							} // else
							tm[0] = tn[0];
						} // else
						neighbor.linkTopTo(tm);
					} // else
					if (b_deg != 1)
						trapez1.linkBottomTo(neighbor);
					if (b_deg != 2)
						trapez2.linkBottomTo(neighbor);
					// System.out.println( "  only1 - new next: " + neighbor );
					break;
				case 2:
					// Trapez 1 bekommt zwei Nachbarn:
					if (s2.x > bots[0].getTopRight().x)
					{
						tn = bots[0].getTopNeighbors();
						tm = new Trapezoid[2];
						if (tn[0] == next)
						{
							tm[0] = trapez1;
							tm[1] = tn[1];
						}
						else
						{ // if
							tm[0] = tn[0];
							tm[1] = trapez1;
						} // else
						bots[0].linkTopTo(tm);
						bots[1].linkTopTo(tr);
						trapez1.linkBottomTo(bots);
						trapez2.linkBottomTo(bots[1]);
						// naechstes Trapezoid fuer naechste Schleife:
						neighbor = bots[1];
						// System.out.println( "  1gets2 - new next: " + neighbor );
						// Trapez 2 bekommt zwei Nachbarn:
					}
					else if (s2.x < bots[0].getTopRight().x)
					{ // if
						tn = bots[1].getTopNeighbors();
						tm = new Trapezoid[2];
						if (tn[0] == next)
						{
							tm[0] = trapez2;
							tm[1] = tn[1];
						}
						else
						{ // if
							tm[0] = tn[0];
							tm[1] = trapez2;
						} // else
						bots[0].linkTopTo(tr);
						bots[1].linkTopTo(tm);
						trapez1.linkBottomTo(bots[0]);
						trapez2.linkBottomTo(bots);
						// naechstes Trapezoid fuer naechste Schleife:
						neighbor = bots[0];
						// System.out.println( "  2gets2 - new next: " + neighbor );
						// Aufteilung exakt 1-1:
					}
					else
					{ // if
						tn = bots[0].getTopNeighbors();
						tm = new Trapezoid[2];
						if (tn[0] == next)
						{
							tm[0] = trapez1;
							tm[1] = tn[1];
						}
						else
						{ // if
							tm[0] = tn[0];
							tm[1] = trapez1;
						} // else
						bots[0].linkTopTo(tm);
						tn = bots[1].getTopNeighbors();
						tm = new Trapezoid[2];
						if (tn[0] == next)
						{
							tm[0] = trapez2;
							tm[1] = tn[1];
						}
						else
						{ // if
							tm[0] = tn[0];
							tm[1] = trapez2;
						} // else
						bots[1].linkTopTo(tm);
						trapez1.linkBottomTo(bots[0]);
						trapez2.linkBottomTo(bots[1]);
						// Schleife hiernach beenden:
						neighbor = null;
						// System.out.println( "  1-1 - new next: " + neighbor );

					} // else
			} // switch
				// Trapezoidknoten wird X-Knoten:
			Node node = (Node) next.node;
			node.type = Node.X;
			node.object = segment;
			// verbinde mit neuen Trapezoiden:
			Node new1 = new Node(Node.SINK);
			Node new2 = new Node(Node.SINK);
			new1.object = trapez1;
			new2.object = trapez2;
			trapez1.node = new1;
			trapez2.node = new2;
			node.link1 = new1;
			node.link2 = new2;
			// System.out.println( "next:" + next + " last:" + last + "  " + ( next != last ) );
		}
		while (next != last);

	} // _splitAllTrapezoids


	/**
	 * Sucht das Trapezoid, dessen Oberkante den Punkt upper enthaelt.
	 * 
	 * @param upper
	 *            Der gesuchte Punkt
	 * @param lower
	 *            Point2
	 */
	private Trapezoid _findUpperTrapezoid(
			Point2 upper,
			Point2 lower)
	{

		Trapezoid result = null;
		_node = _start;
		while (result == null)
		{
			try
			{
				result = _query(upper);
			}
			catch (ExistingPointException epe)
			{
				switch (_node.type)
				{
					case Node.X:
						Segment2 s = (Segment2) _node.object;
						Point2 a = s.source();
						Point2 b = s.target();
						if ((a.y < b.y) || ((a.y == b.y) && (a.x < b.x)))
						{
							if (lower.orientation(a, b) == Point2.ORIENTATION_LEFT)
							{
								_node = _node.link1;
							}
							else // if
							if (lower.orientation(a, b) == Point2.ORIENTATION_RIGHT)
							{
								_node = _node.link2;
							}
							else
							{ // if
								_node = _node.link1;
							} // else
						}
						else
						{ // if
							if (lower.orientation(b, a) == Point2.ORIENTATION_LEFT)
							{
								_node = _node.link1;
							}
							else // if
							if (lower.orientation(b, a) == Point2.ORIENTATION_RIGHT)
							{
								_node = _node.link2;
							}
							else
							{ // if
								_node = _node.link1;
							} // else
						} // else
						break;
					case Node.Y:
						_node = _node.link2;
				} // switch
			} // catch
		} // while
		return result;

	} // _findUpperTrapezoid


	/**
	 * Sucht das Trapezoid, dessen Unterkante den Punkt lower enthaelt.
	 * 
	 * @param upper
	 *            Point2
	 * @param lower
	 *            Der gesuchte Punkt
	 */
	private Trapezoid _findLowerTrapezoid(
			Point2 upper,
			Point2 lower)
	{

		Trapezoid result = null;
		_node = _start;
		while (result == null)
		{
			try
			{
				result = _query(lower);
			}
			catch (ExistingPointException epe)
			{
				switch (_node.type)
				{
					case Node.X:
						Segment2 s = (Segment2) _node.object;
						Point2 a = s.source();
						Point2 b = s.target();
						if ((a.y < b.y) || ((a.y == b.y) && (a.x < b.x)))
						{
							if (upper.orientation(a, b) == Point2.ORIENTATION_LEFT)
							{
								_node = _node.link1;
							}
							else // if
							if (upper.orientation(a, b) == Point2.ORIENTATION_RIGHT)
							{
								_node = _node.link2;
							}
							else
							{ // if
								_node = _node.link1;
							} // else
						}
						else
						{ // if
							if (upper.orientation(b, a) == Point2.ORIENTATION_LEFT)
							{
								_node = _node.link1;
							}
							else // if
							if (upper.orientation(b, a) == Point2.ORIENTATION_RIGHT)
							{
								_node = _node.link2;
							}
							else
							{ // if
								_node = _node.link1;
							} // else
						} // else
						break;
					case Node.Y:
						_node = _node.link1;
				} // switch
			} // catch
		} // while
		return result;

	} // _findLowerTrapezoid

} // QueryStructure
