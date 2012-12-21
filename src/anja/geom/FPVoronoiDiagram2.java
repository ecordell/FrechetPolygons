package anja.geom;


import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import anja.util.SimpleList;
import anja.util.Sorter;


/**
 * Zweidimensionales, zeichenbares Farthest Point Voronoi-Diagramm einer
 * Punktmenge.
 * 
 * Die Vorgehensweise der Berechnung ist uebernommen aus:<br> <i>Franco P.
 * Preparata, Michael Ian Shamos. Computational Geometry. Kapitel 5: Proximity:
 * Fundamental Algorithms, Seite 215.</i>
 * 
 * Die Laufzeit dieses Algorithmus liegt in <i>O</i>(<i>n</i> log <i>n</i>).
 * 
 * @version 0.9 18.04.2003
 * @author Sascha Ternes
 */

public class FPVoronoiDiagram2
		extends VoronoiDiagram2
{

	//**************************************************************************
	// Protected variables
	//**************************************************************************

	/**
	 * Hilfsspeicher fuer nach der Berechnung zu entfernende alte Voronoi-Knoten
	 */
	protected Vector	_vertices_to_remove;


	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt ein neues leeres Farthest Point Voronoi-Diagram.
	 */
	public FPVoronoiDiagram2()
	{

		super();
		_vertices_to_remove = new Vector();

	} // FPVoronoiDiagram2


	/**
	 * Erzeugt ein neues Farthest Point Voronoi-Diagramm mit einer Punktliste
	 * als Kopie der Eingabeliste; die Punkte der neuen Liste sind Kopien -
	 * nicht etwa Referenzen - der Punkte der Eingabeliste.
	 * 
	 * @param input_points
	 *            die Eingabe-Punktliste
	 */
	public FPVoronoiDiagram2(
			Point2List input_points)
	{

		super(input_points);
		_vertices_to_remove = new Vector();

	} // FPVoronoiDiagram2


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/**
	 * Prueft, ob das Farthest Point Voronoi-Diagramm neu berechnet werden muss,
	 * und veranlasst gegebenenfalls eine Neuberechnung.
	 */
	public void recompute()
	{

		if (!_recompute)
			return;

		int n = _points.length();

		// initialisiere Datenstrukturen:
		_initDataStructures();

		// Abbruch bei maximal einem Punkt:
		if (n <= 1)
			return;

		PointComparitor comp = new PointComparitor(PointComparitor.X_ORDER);
		// Punkte falls noetig nach x-Koordinate sortieren:
		if (!_sorted)
		{
			sort(comp, Sorter.ASCENDING);
			_sorted = true;
		} // if

		// fuer die Berechnung die Voronoi-Punkte ueberschreiben:
		SimpleList old_points = _points;
		// nur die Punkte der Huelle interessieren:
		if (n > 3)
		{
			ConvexPolygon2 cp = new ConvexPolygon2((Point2List) this);
			_points = new SimpleList();
			cp.sort(comp, Sorter.ASCENDING);
			// nur Huellenpunkte verwenden:
			Enumeration e1 = old_points.values();
			Enumeration e2 = cp.points().values();
			Point2 chp = (Point2) e2.nextElement();
			while (e1.hasMoreElements())
			{
				Point2 curr = (Point2) e1.nextElement();
				// Huellenpunkte in die Punktliste stecken:
				if (curr.equals(chp))
				{
					_points.add(curr);
					if (e2.hasMoreElements())
					{
						chp = (Point2) e2.nextElement();
					} // if
				} // if
			} // while
			// Zahl der Punkte anpassen:
			n = _points.length();
		} // if

		// Spezialfall von kollinearen Punkten abfangen:
		if (!_simpleDiagram())
		{

			/* rekursive Berechnung */

			// zwei Kopien der Punkte anlegen:
			Point2List copy1 = new Point2List();
			Point2List copy2 = new Point2List();
			// die Punktliste auf die beiden Listen aufteilen:
			Point2 curr = null;
			Enumeration e = _points.values();
			// die erste Haelfte der Punkte in die erste Liste stecken:
			for (int i = 0; i < n / 2; i++)
			{
				curr = (Point2) e.nextElement();
				copy1.addPoint(curr);
			} // for
			boolean cont = true;
			boolean same_x = false;
			// falls es weitere Punkte mit gleichem x gibt, diese in Liste 1:
			while (cont && e.hasMoreElements())
			{
				Point2 next = (Point2) e.nextElement();
				if (next.x == curr.x)
				{
					copy1.addPoint(next);
				}
				else
				{ // if
					copy2.addPoint(next);
					cont = false;
				} // else
			} // while
			// den Rest in die zweite Liste stecken:
			while (e.hasMoreElements())
			{
				copy2.addPoint((Point2) e.nextElement());
			} // while

			// Vor(S1) und Vor(S2) berechnen:
			FPVoronoiDiagram2 vor1 = null;
			FPVoronoiDiagram2 vor2 = null;
			if (copy2.length() == 0)
			{
				float x = copy1.lastPoint().x;
				while (copy1.firstPoint().x != x)
				{
					copy2.addPoint(copy1.firstPoint());
					copy1.removeFirstPoint();
				} // while
				vor1 = new FPVoronoiDiagram2(copy2);
				vor2 = new FPVoronoiDiagram2(copy1);
			}
			else
			{ // if
				vor1 = new FPVoronoiDiagram2(copy1);
				vor2 = new FPVoronoiDiagram2(copy2);
			} // else
			vor1.recompute();
			vor2.recompute();
			// beide Diagramme zum Gesamtdiagramm vereinigen:
			_mergeDiagrams(vor1, vor2);
		} // if

		// alte Punktmenge wiederherstellen:
		_points = old_points;
		_recompute = false;

	} // recompute


	/**
	 * Berechnet den kleinsten umschliessenden Kreis dieses Farthest Point
	 * Voronoi-Diagramms und liefert ihn zurueck.
	 * 
	 * @return den kleinsten umschliessenden Kreis oder <code>null</code>, wenn
	 *         es keinen solchen gibt
	 */
	public MinimalEnclosingCircle2 computeMEC()
	{

		if (!_vertices.isEmpty())
		{
			return new MinimalEnclosingCircle2(this);
		} // if
		return null;

	} // computeMEC


	//**************************************************************************
	// Protected methods
	//**************************************************************************

	/**
	 * Berechnet das Farthest Point Voronoi-Diagramm einer Punktmenge, die nur
	 * aus kollinearen Punkten auf derselben Geraden besteht.
	 * 
	 * @return <code>true</code>, falls es sich um eine vollstaendig kollineare
	 *         Punktmenge handelt; dann wurde das Farthest Point
	 *         Voronoi-Diagramm erzeugt
	 */
	protected boolean _simpleDiagram()
	{

		// Test auf Kollinearitaet:
		boolean collinear = true;
		Point2 p1 = (Point2) _points.firstValue();
		Point2 p2 = (Point2) _points.lastValue();
		Segment2 seg = new Segment2(p1, p2);
		Enumeration e = _points.values();
		while (collinear && e.hasMoreElements())
		{
			collinear &= seg.isCollinear((Point2) e.nextElement());
		} // while
		// ggf. Diagramm berechnen:
		if (collinear)
		{
			VoronoiBisector2 bisec = new VoronoiBisector2(p1, p2);
			_bisectors.add(bisec);
			((Vector) _p2b.get(p1)).add(bisec);
			((Vector) _p2b.get(p2)).add(bisec);
		} // if
		return collinear;

	} // _simpleDiagram


	/**
	 * Vereinigt zwei linear separierte Farthest Point Voronoi-Diagramme zum
	 * Gesamtdiagramm.
	 * 
	 * @param vor1
	 *            die beiden
	 * @param vor2
	 *            Teildiagramme, die zusammengefasst dieses Diagramm bilden
	 */
	protected void _mergeDiagrams(
			FPVoronoiDiagram2 vor1,
			FPVoronoiDiagram2 vor2)
	{

		/* Voraussetzungen:
		   vor1 und vor2 sind bzgl. der x-Koordinate linear separiert.
		   Die Punktmengen von vor1 und vor2 sind primaer nach der x-Koordinate
		   aufsteigend geordnet, sekundaer aufsteigend nach y-Koordinate, wenn zwei
		   aufeinanderfolgende Punkte die gleiche x-Koordinate haben.
		   Die Punktmengen von vor1 und vor2 bestehen entweder aus
		   - einem einzigen Punkt,
		   - zwei Punkten, deren x- oder y-Koordinaten gleich sein koennen,
		   - oder drei oder mehr Punkten (hier ist das Flag fuer gleiche
		     x-Koordinaten gesetzt, wenn alle Punkte die gleiche x-Koordinate
		     haben). */

		// vereinige die Voronoi-Knoten:
		this._vertices.addAll(vor1._vertices);
		this._vertices.addAll(vor2._vertices);
		// vereinige vorerst alle Bisektoren:
		this._bisectors.addAll(vor1._bisectors);
		this._bisectors.addAll(vor2._bisectors);
		this._p2b.putAll(vor1._p2b);
		this._p2b.putAll(vor2._p2b);

		/* Abfangen der Spezialfaelle:
		  (0. points_n(Vor1)=1 und points_n(Vor2)=1 ist unmoeglich.)
		   1. Wenn ein Vor mit points_n(Vor)=1 beteiligt ist und das andere Vor
		      nur parallele Bisektoren hat, muss geprueft werden, ob die Punkte
		      des Gesamtdiagramms wiederum alle auf einer Geraden liegen.
		   2. Wenn beide Vor's nur parallele Bisektoren haben, muss geprueft
		      werden, ob alle Bisektoren die gleiche Steigung haben, denn dann
		      resultiert ein Gesamtdiagramm nur aus parallelen Geraden.
		  (3. Falls sonst auf den zu berechnenden unterstuetzenden Segmenten
		      weitere Punkte liegen, muessen die Segmente angepasst werden.)
		  (4. Beim spaeteren Suchen des obersten Schnittpunkts mit dem aktuellen
		      Bisektor muss der Fall beruecksichtigt werden, dass von beiden
		      Seiten je ein Schnitt an dem exakt gleichen Punkt auftritt. Dann
		      muessen beide schneidenden Bisektoren ersetzt werden, und der neue
		      Bisektor liegt zwischen zwei neuen naechsten Punkten.) */

		_vertices_to_remove.clear();
		boolean simple_case = false;
		// Spezialfall 1:
		if (((vor1.length() == 1) || (vor2.length() == 1))
				&& (vor1._all_bisectors_are_parallel || vor2._all_bisectors_are_parallel))
		{
			simple_case = _checkAndMergeSpecialCase(vor1, vor2);
		} // if
		// Spezialfall 2:
		else if (vor1._all_bisectors_are_parallel
				&& vor2._all_bisectors_are_parallel)
		{
			simple_case = _checkAndMergeSpecialCase(vor1, vor2);
		} // if
		if (simple_case)
			return;

		/* Falls keiner der Faelle 1 und 2 zutrifft, erfolgt das Mergen der
		   Teildiagramme mittels der "dividing chain" im folgenden Verfahren. */

		_all_bisectors_are_parallel = false;
		_all_points_have_same_y = false;

		/* Bestimmung der beiden unterstuetzenden Segmente, hierbei wird der
		   Spezialfall 3 beruecksichtigt: */
		float b_x = vor1.maximumX() + (vor2.minimumX() - vor1.maximumX())
				/ 2.0f;
		Segment2 support[] = _computeSupportingSegments(vor1, vor2, b_x);
		/* support[0] oben, support[1] unten */

		// Initialisierung mit unterem unterstuetzenden Segment:
		boolean first_bisector = true;
		VoronoiVertex2 vertex = null;
		VoronoiVertex2 new_vertex[] = new VoronoiVertex2[2];
		Point2 bi_p[] = new Point2[2];
		Point2 bi_n[] = new Point2[2];
		bi_p[0] = support[1].source();
		bi_p[1] = support[1].target();
		BasicLine2 bisec = support[1].orthogonal(support[1].center());
		if (bisec.source().y < bisec.target().y)
		{
			// Korrektur des Bisektors:
			Point2 sp = bisec.source();
			bisec.setSource(bisec.target());
			bisec.setTarget(sp);
		} // if
		VoronoiBisector2 next_bisec[] = new VoronoiBisector2[2];
		boolean both;
		int upper;
		Intersection dummy = new Intersection();

		/* Schleife zum Suchen des naechsten Voronoi-Knotens, hierbei wird der
		   Spezialfall 4 beruecksichtigt: */
		while (true)
		{
			new_vertex[0] = null;
			new_vertex[1] = null;
			bi_n[0] = null;
			bi_n[1] = null;
			next_bisec[0] = null;
			next_bisec[1] = null;
			VoronoiBisector2 bi[] = new VoronoiBisector2[2];
			both = false;

			// suche obersten Schnittpunkt mit Bisektoren (links, dann rechts):
			for (int i = 0; i < 2; i++)
			{
				Enumeration e = ((Vector) _p2b.get(bi_p[i])).elements();
				while (e.hasMoreElements())
				{
					VoronoiBisector2 next = (VoronoiBisector2) e.nextElement();
					Point2 s = bisec.intersection((BasicLine2) next
							.getRepresentation(), dummy);
					if ((s != null)
							&& (!s.equals(vertex))
							&& ((new_vertex[i] == null) || (s.y > new_vertex[i].y)))
					{
						bi[i] = next;
						new_vertex[i] = new VoronoiVertex2(s);
						// ermittle den naechsten Punkt:
						bi_n[i] = next.getPoints()[0];
						if (bi_n[i] == bi_p[i])
						{
							bi_n[i] = next.getPoints()[1];
						} // if
						next_bisec[i] = next;
					} // if
				} // while
			} // for

			// feststellen, welcher Bisektor (zuerst) schneidet:
			if ((new_vertex[0] != null) && (new_vertex[1] != null))
			{
				// Spezialfall 4 - beide Bisektoren schneiden:
				if (new_vertex[0].equals(new_vertex[1]))
				{
					both = true;
					upper = 1;
				}
				else
				{ // if
					// Schnittpunkt mit einem linken Bisektor kommt zuerst:
					if (new_vertex[0].y > new_vertex[1].y)
					{
						upper = 0;
						// Schnittpunkt mit einem rechten Bisektor kommt zuerst:
					}
					else
					{ // if
						upper = 1;
					} // else
				} // else
			}
			else
			{ // if
				// Schnittpunkt mit einem linken Bisektor:
				if (new_vertex[0] != null)
				{
					upper = 0;
					// Schnittpunkt mit einem rechten Bisektor:
				}
				else
				{ // if
					upper = 1;
				} // else
			} // else

			/*
			         if ( ( new_vertex[0] == null ) && ( new_vertex[1] == null ) ) {
			            System.out.println( " !! no vertex found !!" );
			         } // if
			*/

			// Sicherheitshalber:
			if ((new_vertex[0] == null) && (new_vertex[1] == null))
				break;

			// Hinzufuegen des neuen Voronoi-Knotens:
			_vertices.add(new_vertex[upper]);
			// Hinzufuegen des neuen Bisektors:
			VoronoiBisector2 new_bisec = null;
			if (first_bisector)
			{
				// neuer Bisektor ist ein Strahl:
				new_bisec = new VoronoiBisector2(new_vertex[upper],
						Point2.ORIENTATION_LEFT, support[1].source(),
						support[1].target());
			}
			else
			{ // if
				// neuer Bisektor ist ein Segment:
				new_bisec = new VoronoiBisector2(vertex, new_vertex[upper],
						bi_p[0], bi_p[1]);
				vertex.addBisector(new_bisec);
			} // else
			_bisectors.add(new_bisec);
			((Vector) _p2b.get(bi_p[0])).add(new_bisec);
			((Vector) _p2b.get(bi_p[1])).add(new_bisec);
			new_vertex[upper].addBisector(new_bisec);

			// Ersetzen des (der) schneidenden Bisektors (Bisektoren):
			int k = upper;
			if (both)
				k = 0;
			for (; k <= upper; k++)
			{
				_bisectors.remove(next_bisec[k]);
				((Vector) _p2b.get(bi_p[k])).remove(next_bisec[k]);
				((Vector) _p2b.get(bi_n[k])).remove(next_bisec[k]);

				/* ersetzenden Bisektor berechnen: */

				// alter Bisektor ist eine Gerade:
				if (next_bisec[k].isLine())
				{
					// Unterscheidung zwischen der Ursprungsseite:
					if (k == 0)
					{
						new_bisec = new VoronoiBisector2(new_vertex[upper],
								Point2.ORIENTATION_RIGHT, bi_p[k], bi_n[k]);
					}
					else
					{ // if
						new_bisec = new VoronoiBisector2(new_vertex[upper],
								Point2.ORIENTATION_LEFT, bi_p[k], bi_n[k]);
					} // else
				} // if

				// alter Bisektor ist ein Strahl:
				else if (next_bisec[k].isRay())
				{
					VoronoiVertex2 old_vertex = (VoronoiVertex2) next_bisec[k]
							.getRepresentation().source();
					old_vertex.removeBisector(next_bisec[k]);
					// Unterscheidung zwischen der Ursprungsseite:
					if (k == 0)
					{
						// Beruecksichtigung des Voronoi-Knotens:
						if (bisec.orientation(old_vertex) == Point2.ORIENTATION_LEFT)
						{
							// ersetzender Bisektor ist ein Segment:
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex, bi_p[k], bi_n[k]);
							old_vertex.addBisector(new_bisec);
							old_vertex = null;
							// Knoten wird mitsamt anliegender Bisektoren verworfen:
						}
						else
						{
							// ersetzender Bisektor ist ein Strahl:
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									Point2.ORIENTATION_RIGHT, bi_p[k], bi_n[k]);
						} // else
					}
					else
					{ // if
						// Beruecksichtigung des Voronoi-Knotens:
						if (bisec.orientation(old_vertex) == Point2.ORIENTATION_RIGHT)
						{
							// ersetzender Bisektor ist ein Segment:
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex, bi_p[k], bi_n[k]);
							old_vertex.addBisector(new_bisec);
							old_vertex = null;
							// Knoten wird mitsamt anliegender Bisektoren verworfen:
						}
						else
						{
							// ersetzender Bisektor ist ein Strahl:
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									Point2.ORIENTATION_LEFT, bi_p[k], bi_n[k]);
						} // else
					} // else
					// Entfernung des alten Bisektors und Merken des alten Knotens:
					_removeAndMark(next_bisec[k], old_vertex);
				} // if

				// alter Bisektor ist ein Segment:
				else
				{
					VoronoiVertex2 old_vertex1 = (VoronoiVertex2) next_bisec[k]
							.getRepresentation().source();
					VoronoiVertex2 old_vertex2 = (VoronoiVertex2) next_bisec[k]
							.getRepresentation().target();
					old_vertex1.removeBisector(next_bisec[k]);
					old_vertex2.removeBisector(next_bisec[k]);
					VoronoiVertex2 old_vertex = null;
					// Unterscheidung zwischen der Ursprungsseite:
					if (k == 0)
					{
						// ersetzender Bisektor ist ein Segment:
						if (bisec.orientation(old_vertex1) == Point2.ORIENTATION_LEFT)
						{
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex1, bi_p[k], bi_n[k]);
							old_vertex1.addBisector(new_bisec);
							old_vertex = old_vertex2;
						}
						else
						{
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex2, bi_p[k], bi_n[k]);
							old_vertex2.addBisector(new_bisec);
							old_vertex = old_vertex1;
						} // else
					}
					else
					{ // if
						// ersetzender Bisektor ist ein Segment:
						if (bisec.orientation(old_vertex1) == Point2.ORIENTATION_RIGHT)
						{
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex1, bi_p[k], bi_n[k]);
							old_vertex1.addBisector(new_bisec);
							old_vertex = old_vertex2;
						}
						else
						{
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex2, bi_p[k], bi_n[k]);
							old_vertex2.addBisector(new_bisec);
							old_vertex = old_vertex1;
						} // else
					} // else
					// Entfernung des alten Bisektors und Merken des alten Knotens:
					_removeAndMark(next_bisec[k], old_vertex);
				} // if

				// neuen Bisektor hinzufuegen:
				_bisectors.add(new_bisec);
				((Vector) _p2b.get(bi_p[k])).add(new_bisec);
				((Vector) _p2b.get(bi_n[k])).add(new_bisec);
				new_vertex[upper].addBisector(new_bisec);
			} // for

			// Vorbereiten des naechsten Durchlaufs:
			first_bisector = false;
			vertex = new_vertex[upper];
			bi_p[upper] = bi_n[upper];
			if (both)
			{
				bi_p[0] = bi_n[0];
			} // if
			// Test, ob oberes unterstuetzendes Segment erreicht:
			if ((bi_p[0] == support[0].source())
					&& (bi_p[1] == support[0].target()))
				break;
			// sonst naechsten Bisektorstrahl erzeugen:
			Point2 c = new Point2(bi_p[0].x + (bi_p[1].x - bi_p[0].x) / 2,
					bi_p[0].y + (bi_p[1].y - bi_p[0].y) / 2);
			Point2 l1 = new Point2(c.x - (bi_p[0].y - c.y), c.y
					- (c.x - bi_p[0].x));
			Point2 l2 = new Point2(c.x + (bi_p[0].y - c.y), c.y
					+ (c.x - bi_p[0].x));
			if (l2.y < l1.y)
			{
				l1 = l2;
			} // if
			l1.translate(vertex.x - c.x, vertex.y - c.y);
			bisec = new Ray2(vertex, l1);

		} // while

		// Bisektor des oberen unterstuetzenden Segments hinzufuegen:
		VoronoiBisector2 last_bisec = new VoronoiBisector2(vertex,
				Point2.ORIENTATION_RIGHT, support[0].source(), support[0]
						.target());
		_bisectors.add(last_bisec);
		((Vector) _p2b.get(bi_p[0])).add(last_bisec);
		((Vector) _p2b.get(bi_p[1])).add(last_bisec);
		vertex.addBisector(last_bisec);

		// alle registrierten alten Voronoi-Knoten samt Bisektoren loeschen:
		while (!_vertices_to_remove.isEmpty())
		{
			_recursiveRemove((VoronoiVertex2) _vertices_to_remove.remove(0));
		} // while

	} // _mergeDiagrams


	/**
	 * Liefert eine Kopie dieses Farthest Point Voronoi-Diagramms. Saemtliche
	 * Daten sind Kopien - nicht etwa Referenzen - der Originaldaten.
	 * 
	 * @return eine Kopie dieses Farthest Point Voronoi-Diagramms
	 */
	protected Object _clone()
	{

		FPVoronoiDiagram2 copy = new FPVoronoiDiagram2();
		// kopiere Punktmenge:
		Enumeration e = _points.values();
		while (e.hasMoreElements())
		{
			copy.addPoint((Point2) ((Point2) e.nextElement()).clone());
		} // while

		// kopiere private-Variablen:
		copy._draw_points = this._draw_points;
		copy._draw_bisectors = this._draw_bisectors;
		copy._gc_points = this._gc_points;
		copy._gc_bisectors = this._gc_bisectors;

		return copy;

	} // _clone


	/**
	 * Entfernt den alten, ersetzten Bisektor aus dem Diagramm. Wenn zusaetzlich
	 * ein ganzer Teilbaum an einem alten Voronoi-Knoten entfernt werden muss,
	 * wird der alte Knoten registriert, so dass spaeter alle registrierten
	 * Knoten und deren anhaengende Teilbaeume aus dem Diagramm entfernt werden
	 * koennen.
	 * 
	 * @param bisector
	 *            der zu entfernende alte Bisektor
	 * @param vertex
	 *            der Voronoi-Wurzelknoten des zu entfernenden Teilbaums; kann
	 *            <code>null</code> sein, wenn <code>bisector</code> ein Strahl
	 *            ist
	 */
	protected void _removeAndMark(
			VoronoiBisector2 bisector,
			VoronoiVertex2 vertex)
	{

		_bisectors.remove(bisector);
		((Vector) _p2b.get(bisector.getPoints()[0])).remove(bisector);
		((Vector) _p2b.get(bisector.getPoints()[1])).remove(bisector);
		if (vertex != null)
		{
			_vertices_to_remove.add(vertex);
			vertex.removeBisector(bisector);
		} // if

	} // _removeAndMark


	/**
	 * Entfernt einen Voronoi-Knoten samt anliegenden Bisektoren rekursiv aus
	 * dem Diagramm, d.h. der gesamte an ihm haengende Teilbaum aus Knoten und
	 * Bisektoren wird entfernt.
	 * 
	 * @param vertex
	 *            der zu entfernende Voronoi-Knoten
	 */
	protected void _recursiveRemove(
			VoronoiVertex2 vertex)
	{

		// den Knoten entfernen:
		_vertices.remove(vertex);
		// alle anliegenden Bisektoren entfernen:
		Iterator i = vertex.bisectors();
		while (i.hasNext())
		{
			VoronoiBisector2 bi = (VoronoiBisector2) i.next();
			// den Bisektor entfernen:
			_bisectors.remove(bi);
			((Vector) _p2b.get(bi.getPoints()[0])).remove(bi);
			((Vector) _p2b.get(bi.getPoints()[1])).remove(bi);
			// ggf. rekursiv einen anderen Endknoten entfernen:
			if (bi.isSegment())
			{
				VoronoiVertex2 next = bi.getVertices()[0];
				if (next == vertex)
				{
					next = bi.getVertices()[1];
				} // if
				next.removeBisector(bi);
				_recursiveRemove(next);
			} // if
		} // while

	} // _removeOldVertices

} // FPVoronoiDiagram2
