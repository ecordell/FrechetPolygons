package anja.geom.visgraph;


import java.util.Vector;

import anja.geom.Intersection;
import anja.geom.Point2;
import anja.geom.Point2Graph;
import anja.geom.PointsAccess;
import anja.geom.Polygon2;
import anja.geom.Polygon2Scene;


/**
 * Diese Klasse berechnet zu einer Polygon2Scene den Sichtbarkeitsgraphen anhand
 * des radialen Sweeps. (Quelle: Vorlesung "Bewegungsplanung fuer Roboter"
 * Wintersemester 2002/2003 von Rolf Klein, Skript von Tom Kamphans) Die
 * Reihenfolge, in der die Sichtbarkeitskanten betrachtet werden, wird mit Hilfe
 * der Sweepline bestimmt. Die Ausrichtung der Kanten des Polygons wird in dem
 * Algorithmus auf {@link anja.geom.Polygon2#ORIENTATION_LEFT} gesetzt. Falls
 * das bounding Polygon benutzt wird, werden seine Kanten die Ausrichtung
 * {@link anja.geom.Polygon2#ORIENTATION_RIGHT} haben.
 * 
 * <br>Vorraussetzung fuer das richtige Ergebniss ist, dass die Polygone
 * <b>keine Selbstschnitte</b> aufweisen.
 * 
 * @author Alexander Tiderko
 * 
 * @see anja.geom.Polygon2#ORIENTATION_LEFT
 * @see anja.geom.Polygon2#ORIENTATION_RIGHT
 * 
 * 
 **/
public class VisGraph
{

	// Knoten der Scene
	protected VisGVertice[]	_vertices	= null;
	protected Point2Graph	_visG		= null;


	//*************************************************************************

	/**
	 * Erzeugt aus einer Polygon2Scene einen Sichtbarkeitsgraphen. Die Polygone
	 * duerfen keine Selbstschnitte haben.
	 * 
	 * @param polyScene
	 *            Die Szene
	 */
	public VisGraph(
			Polygon2Scene polyScene)
	{
		init(polyScene, false);
	}


	//*************************************************************************

	/**
	 * Erzeugt aus einer Polygon2Scene einen Sichtbarkeitsgraphen. Die Polygone
	 * duerfen keine Selbstschnitte haben.
	 * 
	 * @param polyScene
	 *            Die Szene
	 * @param useBoundingPoly
	 *            <b>true</b> gibt an, dass das Bounding Polygon in die
	 *            Berechnung einbezogen werden soll.
	 */
	public VisGraph(
			Polygon2Scene polyScene,
			boolean useBoundingPoly)
	{
		init(polyScene, useBoundingPoly);
	}


	//*************************************************************************

	/**
	 * Diese Methode Berechnet den Sichtbarkeitsgraphen. Um den Graphen zu
	 * bekommen, muss die Methode {@link #getVisG(Point2, boolean)} benutzt
	 * werden.
	 * 
	 */
	public void calculate()
	{
		radSweep(_vertices);
	}


	//*************************************************************************

	/**
	 * Gibt den Sichtbarkeitsgraphen mit dem Startpunkt zurueck. Vorher muss der
	 * Sichtbarkeitsgraph der Scene mit {@link #calculate()} berechnet werden.
	 * 
	 * @param sourcePoint
	 *            Startpunkt
	 * @param addPolyEdges
	 *            <b>true</b> gibt an, dass die Polygonkanten in den
	 *            Sichtbarkeitsgraphen mit aufgenommen werden sollen.
	 * 
	 * @return Sichtbarkeitsgraph in Form eines Point2Graph
	 * 
	 * @see #calculate()
	 */
	public Point2Graph getVisG(
			Point2 sourcePoint,
			boolean addPolyEdges)
	{
		_visG = getVisG(addPolyEdges);
		Intersection intersection = new Intersection();

		if (_vertices != null)
		{
			// Pruefe zu jedem Punkt, ob dieser sichtbar ist
			for (int i = 0; i < _vertices.length; i++)
			{
				VisGEdge edge = new VisGEdge(sourcePoint, _vertices[i]);
				boolean find_intersects = false;

				for (int j = 0; (j < _vertices.length) && (!find_intersects); j++)
				{
					if (_vertices[j].get_nextPEdge() != null)
					{
						Point2 iPoint = edge.intersection(_vertices[j]
								.get_nextPEdge(), intersection);

						if ((iPoint != null)
								&& (iPoint.comparePointsLexX(edge.get_target()) != 0))
						{
							find_intersects = true;
						}
					}
				}
				// wenn keine Schnitte gefunden, fuege die Kante hinzu
				if (!find_intersects)
					_visG.add(sourcePoint, _vertices[i]);
			}
		}
		return _visG;
	}


	//*************************************************************************

	/**
	 * Gibt den Sichtbarkeitsgraphen mit dem Startpunkt und Zielpunkt zurueck.
	 * Vorher muss der Sichtbarkeitsgraph der Scene mit {@link #calculate()}
	 * berechnet werden.
	 * 
	 * @param sourcePoint
	 *            Startpunkt
	 * @param targetPoint
	 *            Zielpunkt
	 * @param addPolyEdges
	 *            <b>true</b> gibt an, dass die Polygonkanten in den
	 *            Sichtbarkeitsgraphen mit aufgenommen werden sollen.
	 * 
	 * @return Sichtbarkeitsgraph in Form eines Point2Graph
	 * 
	 * @see #calculate()
	 */
	public Point2Graph getVisG(
			Point2 sourcePoint,
			Point2 targetPoint,
			boolean addPolyEdges)
	{
		_visG = getVisG(addPolyEdges);
		Intersection intersection = new Intersection();
		if (_vertices != null)
		{
			// Pruefe zu jedem Punkt, ob dieser sichtbar ist
			for (int i = 0; i < _vertices.length; i++)
			{
				VisGEdge edgeSource = new VisGEdge(sourcePoint, _vertices[i]);

				VisGEdge edgeTarget = new VisGEdge(targetPoint, _vertices[i]);

				boolean find_intersects_source = false;
				boolean find_intersects_target = false;

				for (int j = 0; (j < _vertices.length)
						&& ((!find_intersects_source) || (!find_intersects_target)); j++)
				{
					if (_vertices[j].get_nextPEdge() != null)
					{
						Point2 iSPoint = edgeSource.intersection(_vertices[j]
								.get_nextPEdge(), intersection);

						Point2 iTPoint = edgeTarget.intersection(_vertices[j]
								.get_nextPEdge(), intersection);

						if ((!find_intersects_source)
								&& (iSPoint != null)
								&& (iSPoint.comparePointsLexY(edgeSource
										.get_target()) != 0))
						{
							find_intersects_source = true;
						}

						if ((!find_intersects_target)
								&& (iTPoint != null)
								&& (iTPoint.comparePointsLexX(edgeTarget
										.get_target()) != 0))
						{
							find_intersects_target = true;
						}
					} // end inner for() loop
				}// end outer for() loop

				// wenn keine Schnitte gefunden, fuege die Kante hinzu
				if (!find_intersects_source)
					_visG.add(sourcePoint, _vertices[i]);
				if (!find_intersects_target)
					_visG.add(targetPoint, _vertices[i]);

			} // if(vertices != null)

			// Teste, ob der Start und Zielpunkt sich sehen koennen..
			boolean find_intersection = false;
			VisGEdge edge = new VisGEdge(sourcePoint, targetPoint);

			for (int j = 0; (j < _vertices.length) && (!find_intersection); j++)
			{
				if (_vertices[j].get_nextPEdge() != null)
				{
					Point2 iPoint = edge.intersection(_vertices[j]
							.get_nextPEdge(), intersection);

					if ((iPoint != null)
							&& (iPoint.comparePointsLexX(targetPoint) != 0))
					{
						find_intersection = true;
					}
				}
			}

			if (!find_intersection)
				_visG.add(sourcePoint, targetPoint);
		}
		return _visG;
	}


	//*************************************************************************

	/**
	 * Gibt den Sichtbarkeitsgraphen zurueck. Vorher muss der Sichtbarkeitsgraph
	 * mit {@link #calculate()} berechnet werden.
	 * 
	 * @param addPolyEdges
	 *            <b>true</b> gibt an, dass die Polygonkanten in den
	 *            Sichtbarkeitsgraphen mit aufgenommen werden sollen.
	 * 
	 * @return Sichtbarkeitsgraph in Form eines Point2Graph
	 * 
	 * @see #calculate()
	 */
	public Point2Graph getVisG(
			boolean addPolyEdges)
	{
		_visG = new Point2Graph();

		if (_vertices != null)
		{
			// Fuege alle Knoten in den graph ein...   
			for (int i = 0; i < _vertices.length; i++)
			{
				_visG.add(_vertices[i]);
			}

			// Fuege alle Kanten in den Graph ein..
			for (int i = 0; i < _vertices.length; i++)
			{
				VisGVertice v = _vertices[i];

				// Fuege alle Polygonkanten in den Graph ein..
				if (addPolyEdges)
					_visG.add(_vertices[i].get_nextPEdge());

				// Fuege alle ausgehenden Sichtbarkeitskanten in den Graph ein..
				for (int j = 0; j < v.sizeOutVisEdges(); j++)
					_visG.add(v.getOutVisEdge(j));
			}
		}
		return _visG;
	}


	//*************************************************************************

	/**
	 * In dieser Methode werden die Punkte mit den Polygonkante aus der Scene
	 * ausgelesen.
	 * 
	 * Davor werden alle Polygone der Scene auf
	 * {@link anja.geom.Polygon2#ORIENTATION_LEFT} ausgerichtet. Falls das
	 * BoundigPolygon benutzt wird, wird dieses auf
	 * {@link anja.geom.Polygon2#ORIENTATION_RIGHT} ausgerichtet. Alle Punkte
	 * werden nach der X-Koordinate sortiert. Anschliessend werden alle
	 * Zustaende initialisiert (siehe {@link #initSweep(VisGVertice[])}) die
	 * Berechnung muss vom Benutzer mit {@link #calculate()} aufgerufen werden.
	 * 
	 * @param polyScene
	 *            Scene mit den Polygonen
	 * @param useBoundingPoly
	 *            <b>true</b> gibt an, dass das Bounding Polygon in die
	 *            Berechnung einbezogen werden soll.
	 * 
	 * @see #calculate()
	 * @see #initSweep(VisGVertice[])
	 * @see anja.geom.Polygon2#ORIENTATION_LEFT
	 * @see anja.geom.Polygon2#ORIENTATION_RIGHT
	 */
	protected void init(
			Polygon2Scene polyScene,
			boolean useBoundingPoly)
	{
		Polygon2 boundPoly = (useBoundingPoly) ? polyScene.getBoundingPolygon()
				: null;

		Polygon2[] poly = polyScene.getPolygons();

		/*		
		 * // Redundante Punkte in den Polygonen entfernen
				for (int i = 0; i < poly.length; i++)
					poly[i].removeRedundantPoints();
				if (boundPoly != null)
				  boundPoly.removeRedundantPoints();
		*/

		/* Gesamtanzahl der Knoten des Graphen berechnen 
		 * und Knotenarray erzeugen 
		 * 
		 */

		int n = polyScene.numOfAllVertices();
		if (boundPoly != null)
			n += boundPoly.length();

		if (n > 1)
		{
			_vertices = new VisGVertice[n];

			int pos = 0;
			// Setze die Orientierung der Polygone
			for (int i = 0; i < poly.length; i++)
			{
				poly[i].setOrientation(Polygon2.ORIENTATION_LEFT);
				pos += fillPolyArray(_vertices, pos, poly[i]);
			}

			if (boundPoly != null)
			{
				// Das Bounding-Polygon muss anders orientiert sein!
				boundPoly.setOrientation(Polygon2.ORIENTATION_RIGHT);
				pos += fillPolyArray(_vertices, pos, boundPoly);
			}

			// Sortiere Vertices
			Point2.sortPointsX(_vertices);
			initSweep(_vertices);
		}
	}


	//*************************************************************************

	/**
	 * In dieser Methode werden die Punkte eines Polygons ausgelesen. Zu diesen
	 * Punkten werden im Gegenuhrzeigersinn die Vorgaenger- und Nachfolgerkante
	 * bestimmt und hinzugefuegt. Weiterhin wird die der Punkt auf inaktiv
	 * gesetzt, wenn die Snkrechte nach unten von diesem Punkt ins Innerre des
	 * Polygons zeigt.
	 * 
	 * @param v
	 *            Array von Punkten der Scene
	 * @param pos
	 *            Position in <em>v</em> ab der die Punkte des Polygons
	 *            <em>poly</em> beginnen
	 * @param poly
	 *            Das Polygon
	 * 
	 * @return Position in dem Array <em>v</em>, nachdem alle Punkte des
	 *         Polygons <em>poly</em> ausgelesen wurden.
	 */
	protected int fillPolyArray(
			VisGVertice[] v,
			int pos,
			Polygon2 poly)
	{
		int n = poly.length();
		PointsAccess pAccess = new PointsAccess(poly);

		// Erzeuge Vertices
		for (int i = 0; i < n; i++)
		{
			Point2 act = pAccess.cyclicNextPoint();
			v[pos + i] = new VisGVertice(act.x, act.y);
		}

		// Setze Nachfolger- und Vorgaenger-Kanten
		for (int i = 0; i < n; i++)
		{
			int act = pos + i;
			int next = (i == n - 1) ? (pos) : (pos + i + 1);

			VisGEdge edge = new VisGEdge(v[act], v[next]);

			v[act].set_nextPEdge(edge);
			v[next].set_prevPEdge(edge);
		}

		// Initialisiere die Markierung fuer den radialen Sweep
		for (int i = 0; i < n; i++)
		{
			int act = pos + i;
			Point2 prevP = v[act].get_prevPEdge().source();
			Point2 nextP = v[act].get_nextPEdge().target();
			Point2 vChanged = new Point2(v[act].x, v[act].y - 1.0);

			v[act].set_active(v[act].angle(nextP, prevP) < v[act].angle(
					vChanged, prevP));
		}

		return n;
	}


	//*************************************************************************

	/**
	 * Bei der Initialisierung des Sweeps werden Senkrechten nach unten von den
	 * Punkten aus gezogen. Treffen diese auf ein Segment, so wird ein Zeiger
	 * auf dieses Segment gesetzt. Das ist das Segment, was von dem Punkt aus
	 * als naechstes sichtbar ist. Gibt es keine Segmente darunter, so wird der
	 * Zeiger auf <b>null</b> gesetzt.
	 * 
	 * <br>Ineffizient aufgrund der Verwendung der Vektorstruktur, Balansierte
	 * Baeume (z.B.AVL) waeren effizienter.
	 * 
	 * @param v
	 *            die Menge der Punkte, fuer die der Sichtbarkeitsgraph
	 *            berechnet wird
	 */
	protected void initSweep(
			VisGVertice[] v)
	{
		Vector actSegs = new Vector();

		for (int i = 0; i < v.length; i++)
		{
			VisGEdge prevEdge = v[i].get_prevPEdge();
			VisGEdge nextEdge = v[i].get_nextPEdge();

			// Pruefe, ob Segmente des Knotens anfangen oder aufhoeren
			boolean prevEdgeEnds = (v[i].comparePointsLexX(prevEdge
					.get_source()) > 0);

			boolean nextEdgeEnds = (v[i].comparePointsLexX(nextEdge
					.get_target()) > 0);

			// entferne beendete Segmente, die zu v_i gehoeren 
			if (prevEdgeEnds)
				actSegs.remove(prevEdge);
			if (nextEdgeEnds)
				actSegs.remove(nextEdge);

			/* Bestimme anhand der y-Koordinate die Position 
			 * fuer v_i innerhalb von actSegs
			 * 
			 */
			int n = actSegs.size();
			int index = n / 2;
			int min = 0;
			int max = n - 1;
			int ori = 0;
			VisGEdge actSeg = null;
			boolean not_found = (max >= min);
			while (not_found)
			{
				actSeg = (VisGEdge) actSegs.elementAt(index);
				ori = v[i].orientation(actSeg.getLeftPoint(), actSeg
						.getRightPoint());

				if (ori == Point2.ORIENTATION_LEFT)
					min = (index + 1 > max) ? max : index + 1;
				else
					max = index;

				index = min + ((max - min) / 2);
				not_found = (max > min);
			}
			if (!actSegs.isEmpty())
			{
				actSeg = (VisGEdge) actSegs.elementAt(index);

				ori = v[i].orientation(actSeg.getLeftPoint(), actSeg
						.getRightPoint());

				if (ori == Point2.ORIENTATION_LEFT)
					index++;
			}

			// Setze _radSweepPointer auf das naechste untere Segment

			v[i].set_radSweepPointer((VisGEdge) ((index <= 0) ? null : actSegs
					.elementAt(index - 1)));

			// fuege anfangende Segmente, die zu v_i gehoeren hinzu 
			if (!(prevEdgeEnds || nextEdgeEnds))
			{
				double angle = v[i].angle(prevEdge.get_source(), nextEdge
						.get_target());
				if (angle < Math.PI)
				{
					actSegs.add(index, nextEdge);
					actSegs.add(index, prevEdge);
				}
				else
				{
					actSegs.add(index, prevEdge);
					actSegs.add(index, nextEdge);
				}
			}
			else if (!prevEdgeEnds)
			{
				actSegs.add(index, prevEdge);
			}
			else if (!nextEdgeEnds)
			{
				actSegs.add(index, nextEdge);
			}
		} // end outer for() loop
	}


	//*************************************************************************

	/**
	 * In diese Methode wird der radiale Sweep durchgefuehrt. Es ist wichtig,
	 * dass die Punkte vorher mit der Methode {@link #initSweep(VisGVertice[])}
	 * initialisiert werden. Die Punkte muessen auch nach der X-Koordinate
	 * sortiert vorliegen.
	 * 
	 * Die Berechneten sichtbarkeitsgeraden werden in den Punkten als eingehende
	 * bzw. ausgehende Kante gespeichert. (siehe
	 * {@link anja.geom.visgraph.VisGVertice})
	 * 
	 * @param vertices
	 *            Sortiertes und Initalisiertes Array von Punkten
	 * 
	 * @see #initSweep(VisGVertice[])
	 * @see anja.geom.visgraph.VisGVertice
	 */
	protected void radSweep(
			VisGVertice[] vertices)
	{
		Intersection iResult = new Intersection();
		VisGVertice v = null;
		VisGVertice v_other = null;

		// Initialiesere die Sweepline
		SweepLine sl = new SweepLine(vertices);

		// Hole aus der Sweepline-Struktur die abzuarbeitenden Kanten
		VisGEdge edge = sl.getNextSegment();

		while (edge != null)
		{
			v = edge.getLeft();
			v_other = edge.getRight();

			// Punkt aktiv ?
			if (v.is_active())
			{
				VisGVertice v_next = v.get_nextPEdge().get_target();

				/* anderer Punkt der edge ist Punkt 
				 * von next edge des Ausgangspunktes
				 * 
				 */
				if (v_next.comparePointsLexX(v_other) == 0)
				{
					/* Ueberpruefe, ob ein v_next und _vprev die gleichen 
					 * Endpunkte haben, dann ist das Polygon ein Liniensegment!
					 */
					VisGVertice v_prev = v.get_prevPEdge().get_source();

					if (v_prev.comparePointsLexX(v_next) != 0)
					{
						// Punkt wird inaktiv - nichts zu Graphen hinzufuegen
						v.set_active(false);
					} // do nothing - Sichtbarkeit bleibt erhalten !!!
				}
				else
				{
					// aktives Segment ist null?
					if (v.get_radSweepPointer() == null)
					{
						// neues Segment von v_other wird sichtbar, welches?
						VisGVertice v_other_prev = v_other.get_prevPEdge()
								.get_source();

						VisGVertice v_other_next = v_other.get_nextPEdge()
								.get_target();

						if (v_other_next.orientation(v_other, v_other_prev) == Point2.ORIENTATION_LEFT)
						{
							// v_other->v_other_next liegt links von v_other->v_other_prev
							v.set_radSweepPointer(v_other.get_nextPEdge());
						}
						else
						{
							// v_other->v_other_prev liegt links von v_other->v_other_next
							v.set_radSweepPointer(v_other.get_prevPEdge());
						}

						// Fuege Sichtbarkeitskante zu Graphen hinzu
						v.addOutVisEdge(edge);
						v_other.addInVisEdge(edge);
					}
					else
					{
						// existiert ein Schnitt der Sichtbarkeitskante mit dem gerade aktiven Segment
						edge.intersection(v.get_radSweepPointer(), iResult);
						if (iResult.result == Intersection.POINT2)
						{
							// Schnittpunkte mit einem Endpunkt der Sichtbarkeitskante sind keine Schnittpunkte! 
							if (iResult.point2.comparePointsLexX(v_other) == 0)
								iResult.set();
						}
						if (iResult.result != Intersection.POINT2)
						{
							// Ueberpruefe Segmente des Zielpunktes - case?
							VisGVertice v_other_prev = v_other.get_prevPEdge()
									.get_source();
							VisGVertice v_other_next = v_other.get_nextPEdge()
									.get_target();
							boolean vo_prev_isLeft = (v_other_prev.orientation(
									v, v_other) == Point2.ORIENTATION_LEFT);
							boolean vo_next_isLeft = (v_other_next.orientation(
									v, v_other) == Point2.ORIENTATION_LEFT);

							if (vo_prev_isLeft && vo_next_isLeft)
							{
								// beide Segmente beginnen - das 'vordere' Segment wird sichtbar
								if (v_other_next.orientation(v_other,
										v_other_prev) == Point2.ORIENTATION_LEFT)
								{
									// v_other->v_other_next liegt links von v_other->v_other_prev
									v.set_radSweepPointer(v_other
											.get_nextPEdge());
								}
								else
								{
									// v_other->v_other_prev liegt links von v_other->v_other_next
									v.set_radSweepPointer(v_other
											.get_prevPEdge());
								}
							}
							else if (vo_prev_isLeft)
							{
								// && !vo_next_isLeft
								// v_other_prev beginnt, v_other_next endet
								v.set_radSweepPointer(v_other.get_prevPEdge());
							}
							else if (vo_next_isLeft)
							{
								// && !vo_prev_isLeft
								// ein Segment endet, das andere beginnt - dieser Fall sollte nicht auftreten !!!
								v.set_radSweepPointer(v_other.get_nextPEdge());
							}
							else
							{
								// beide Segmente enden - was liegt hinter v_other
								v.set_radSweepPointer(v_other
										.get_radSweepPointer());
							}

							// Fuege Sichtbarkeitskante zu Graphen hinzu
							v.addOutVisEdge(edge);
							v_other.addInVisEdge(edge);

						} // else do nothing - Kante nicht hinzufuegen
					}
				}
			}
			else
			{
				// v not active
				VisGVertice v_prev = v.get_prevPEdge().get_source();
				// anderer Punkt der edge ist Punkt von prev edge des Ausgangspunktes
				if (v_prev.comparePointsLexX(v_other) == 0)
				{
					v.set_active(true);
					// ist v_prev konvex? es sei denn, dass v_prev ein nicht konvexer Knoten ist
					if (v_prev.angle(v, v_prev.get_prevPEdge().get_source()) < Math.PI)
					{
						// worauf zeigt der andere prev-edge-Punkt - dieses Segment ist jetzt auch das aktive
						v.set_radSweepPointer(v_prev.get_radSweepPointer());
					}
					else
					{
						// jetzt wird die Vorgaengerkante sichtbar
						v.set_radSweepPointer(v_prev.get_prevPEdge());
					}
				} // else do nothing
			}

			// naechste Sichtbarkeitskante holen
			edge = sl.getNextSegment();
		}
	}

	//*************************************************************************

}
