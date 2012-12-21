package anja.geom;


import java.util.Vector;

import java.awt.Graphics2D;

import anja.geom.triangulation.SeidelTriangulation;
import anja.util.GraphicsContext;


/**
 * Ein Polygon, dass auch Loecher haben darf. Es ist von Polygon2 abgeleitet.
 * Die Loecher des Polygons werden in einem Vector verwaltet und sind auch
 * Objekte vom Typ Polygon2.
 * 
 * @author Marina Bachran
 */
public class HolePolygon2
		extends Polygon2
{

	/** Die Loecher des Polygons werden in diesem Vector verwaltet. */
	private Vector	holes	= new Vector();


	/**
	 * Erzeugt ein leeres HolePolygon2. (Ruft den Konstructor Polygon2() von
	 * Polygon2 auf.)
	 */
	public HolePolygon2()
	{
		super();
	}


	/**
	 * Erzeugt ein neues HolePolygon2 aus einem Polygon2. (Ruft den Konstructor
	 * Polygon2(Polygon2 poly) von Polygon2 auf.)
	 * 
	 * @param poly
	 *            Das Eingabepolygon
	 */
	public HolePolygon2(
			Polygon2 poly)
	{
		super(poly);
	}


	/**
	 * Erzeugt ein neues HolePolygon2 aus einem bestehenden. Dabei werden auch
	 * die Loecher uebernommen.
	 * 
	 * @param poly
	 *            Das Eingabepolygon
	 */
	public HolePolygon2(
			HolePolygon2 poly)
	{
		super((Polygon2) poly);
		for (int i = 0; i < poly.getHoleCount(); i++)
			addHole(poly.getHole(i));
	}


	/**
	 * Fuegt ein Loch zu einem HolePolygon2 dazu.
	 * 
	 * @param hole
	 *            Das Loch, das hinzugefuegt wird.
	 */
	public void addHole(
			Polygon2 hole)
	{
		holes.add(hole);
	}


	/**
	 * Ueberprueft, ob das Polygon2 poly ein Loch von dem HolePolygon2 ist.
	 * 
	 * @param poly
	 *            Ist dieses Loch enthalten?
	 * 
	 * @return <i>true</i> das Loch ist enthalten. <i>false</i> das Loch ist
	 *         nicht enthalten.
	 */
	public boolean existHole(
			Polygon2 poly)
	{
		for (int i = 0; i < getHoleCount(); i++)
		{
			Polygon2 hole = getHole(i);
			if (hole.equals(poly))
				return true;
		}
		return false;
	}


	/**
	 * Entfernt das Loch hole aus dem HolePolygon2.
	 * 
	 * @param hole
	 *            Loch, das entfernt wird.
	 * 
	 * @return <i>true</i> das Loch war enthalten und wurde geloescht.
	 *         <i>false</i> das Loch war nicht enthalten.
	 */
	public boolean removeHole(
			Polygon2 hole)
	{
		return holes.remove(hole);
	}


	/**
	 * Liefert die Anzahl der Loecher des HolePolygon2.
	 * 
	 * @return int - Anzahl der Loecher
	 */
	public int getHoleCount()
	{
		return holes.size();
	}


	/**
	 * Liefert das Loch an der Stelle i des Loch-Vectors zurueck.
	 * 
	 * @param i
	 *            Indexstelle i des Vectors.
	 * 
	 * @return Polygon2 - das Loch an der i-ten Stelle.
	 */
	public Polygon2 getHole(
			int i)
	{
		if (i >= getHoleCount())
			return null;
		return (Polygon2) holes.get(i);
	}


	/**
	 * Setzt die Orientierung aller Loecher des HolePolygon2 auf die
	 * Orientierung ori.
	 * 
	 * @param ori
	 *            Polygon2.ORIENTATION_LEFT fuer eine links-Orientierung,
	 *            Polygon2.ORIENTATION_RIGHT fuer eine rechts-Orientierung
	 */
	public void setHoleOrientation(
			byte ori)
	{
		for (int i = 0; i < getHoleCount(); i++)
			getHole(i).setOrientation(ori);
	}


	/**
	 * Liefert die Gesamtanzahl der Kanten des HolePolygon2, also die Anzahl der
	 * Kanten der Loecher plus der Anzahl der Kanten des aeusseren Polygons.
	 * 
	 * @return Die Gesamtanzahl der Kanten im HolePolygon
	 */
	public int getEdgeCount()
	{
		int len = length();
		for (int i = 0; i < getHoleCount(); i++)
			len += getHole(i).length();
		return len;
	}


	/**
	 * Liefert alle Kanten des HolePolygon2, also die Kanten der Loecher plus
	 * die Kanten des aeusseren Polygons.
	 * 
	 * @return Die Kanten im HolePolygon
	 */
	public Segment2[] allEdges()
	{

		int len = getEdgeCount();
		Segment2[] all = new Segment2[len];
		Segment2[] outer = edges();
		for (int i = 0; i < outer.length; i++)
			all[i] = outer[i];
		int index = outer.length;
		Polygon2 aktHole;
		Segment2[] aktEdges;
		for (int j = 0; j < holes.size(); j++)
		{
			aktHole = (Polygon2) holes.get(j);
			aktEdges = aktHole.edges();
			for (int k = 0; k < aktHole.edgeNumber(); k++)
			{
				all[index] = aktEdges[k];
				index++;
			}
		}
		return all;
	}


	/**
	 * Ueberprueft, ob der Punkt p innerhalb des HolePolygon2 liegt (dabei
	 * werden die Loecher auch beruecksichtigt.
	 * 
	 * @param p
	 *            dieser Punkt soll geprueft werden
	 * 
	 * @return <i>true</i> - der Punkt p liegt innerhalb des Polygons,
	 *         <i>false</i> - der Punkt p liegt ausserhalb des Polygons
	 */
	public boolean inside(
			Point2 p)
	{
		return (locatePoint(p) == POINT_INSIDE);
	}


	/**
	 * Liefert zurueck, ob sich der Punkt q innerhalb, ausserhalb oder auf der
	 * Kante des HolePolygon2 liegt
	 * 
	 * @param q
	 *            dieser Punkt soll geprueft werden
	 * 
	 * @return <i>POINT_INSIDE</i> - der Punkt liegt innerhalb des Polygons <br>
	 *         <i>POINT_ON_EDGE</i> - der Punkt liegt auf einer Kante des
	 *         Polygons <br> <i>POINT_OUTSIDE</i> - der Punkt liegt ausserhalb
	 *         des Polygons oder in einem Loch
	 */
	public byte locatePoint(
			Point2 q)
	{
		byte location = super.locatePoint(q);
		if (location == POINT_INSIDE)
		{
			for (int i = 0; i < getHoleCount(); i++)
			{
				byte tempLocation = getHole(i).locatePoint(q);
				if (tempLocation == POINT_OUTSIDE)
					continue;
				if (tempLocation == POINT_INSIDE)
					location = POINT_OUTSIDE;
				else if (tempLocation == POINT_ON_EDGE)
					location = POINT_ON_EDGE;
				break;
			}
		}
		return location;
	}


	/**
	 * Zeichnet das HolePolygon2.
	 * 
	 * @param g
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * @param gcPoly
	 *            Der Grafikkontext, mit dem das Polygon gezeichnet wird
	 * @param gcHole
	 *            Der Grafikkontext, mit dem die Loecher gezeichnet werden
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gcPoly,
			GraphicsContext gcHole)
	{
		draw(g, gcPoly);
		for (int i = 0; i < getHoleCount(); i++)
			getHole(i).draw(g, gcHole);
	}
	
	
	/**
	 * Zeichnet das HolePolygon2 mit verschiedenen GraphicsContext.
	 * 
	 * <br>Jedes Loch wird mit dem gc gezeichnet, der im Array
	 * seiner Nummer entspricht. Sind weniger gc angegeben als Löcher
	 * wird für die restlichen Löcher der letzte gc im Array verwendet.
	 * 
	 * @param g
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * @param gcPoly
	 *            Der Grafikkontext, mit dem das Polygon gezeichnet wird
	 * @param gcHole
	 *            Der Grafikkontext Array, mit dem die Loecher gezeichnet werden
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gcPoly,
			GraphicsContext[] gcHole)
	{
		draw(g, gcPoly);
		for (int i = 0; i < getHoleCount(); i++)
			if (getHole(i) != null)
				if (i < gcHole.length)
					getHole(i).draw(g, gcHole[i]);
				else
					getHole(i).draw(g, gcHole[gcHole.length-1]);
	}


	/**
	 * Berechnet die Vereinigung von diesem HolePolygon2 und otherPoly.
	 * 
	 * @param otherPoly
	 *            das andere Polygon
	 * 
	 * @return HolePolygon2 die Vereinigung der beiden Polygone
	 * 
	 * @see PolygonUnion
	 */
	public HolePolygon2 union(
			HolePolygon2 otherPoly)
	{
		PolygonUnion pu = new PolygonUnion(this, otherPoly);
		return pu.getUnion();
	}


	/**
	 * Berechnet die Vereinigung der Polygone, die in polys uebergeben werden.
	 * 
	 * @param polys
	 *            Array der Polygone, die vereinigt werden sollen.
	 * 
	 * @return Polygon2Scene die Polygonszene, die die Vereinigung der Polygone
	 *         enthaelt.
	 */
	public static HolePolygon2 union(
			HolePolygon2[] polys)
	{
		Vector polygons = new Vector();
		for (int i = 0; i < polys.length; i++)
		{
			polygons.add(polys[i]);
		}
		return union(polygons);
	}


	/**
	 * Berechnet die Vereinigung der Polygone, die in polys uebergeben werden.
	 * 
	 * @param polys
	 *            Array der Polygone, die vereinigt werden sollen.
	 * 
	 * @return Polygon2Scene die Polygonszene, die die Vereinigung der Polygone
	 *         enthaelt.
	 */
	public static HolePolygon2 union(
			Polygon2[] polys)
	{
		Vector polygons = new Vector();
		for (int i = 0; i < polys.length; i++)
		{
			polygons.add(new HolePolygon2(polys[i]));
		}
		return union(polygons);
	}


	/**
	 * Berechnet die Vereinigung der Polygone, die in polys uebergeben werden.
	 * 
	 * @param polys
	 *            Array der Polygone, die vereinigt werden sollen.
	 * 
	 * @return Polygon2Scene die Polygonszene, die die Vereinigung der Polygone
	 *         enthaelt.
	 */
	private static HolePolygon2 union(
			Vector polys)
	{
		while (polys.size() > 1)
		{
			HolePolygon2 p1 = (HolePolygon2) polys.get(0);
			boolean foundUnionPartner = false;
			for (int i = 1; i < polys.size(); i++)
			{
				HolePolygon2 p2 = (HolePolygon2) polys.get(i);
				HolePolygon2 result = p1.union(p2);
				if (result != null)
				{
					polys.remove(i);
					polys.remove(0);
					polys.add(result);
					foundUnionPartner = true;
					break;
				}
			}
			if (!foundUnionPartner)
				return null;

		}
		return (HolePolygon2) polys.get(0);
	}


	/**
	 * Entfernt alle Punkte aus dem Polygon, die auf einer Kante des Polygons
	 * liegen, also keine eigene Ecke bilden und damit ueberfluessig sind. Dabei
	 * werden auch die Loecher beruecksichtigt.
	 */
	public void removeRedundantPoints()
	{
		super.removeRedundantPoints();
		for (int i = 0; i < getHoleCount(); i++)
		{
			getHole(i).removeRedundantPoints();
		}
	}


	/**
	 * Berechnet die Minkowski-Summe von diesem Polygon und dem Polygon2 poly.
	 * 
	 * @param poly
	 *            das zweite Polygon
	 * 
	 * @return HolePolygon2 die Minkowski-Summe der beiden Polygone
	 */
	public HolePolygon2 minkowskiSum(
			Polygon2 poly)
	{
		return HolePolygon2.minkowskiSum(this, poly);
	}


	/**
	 * minkowskiSum berechnet die Minkowski-Summe mit Hilfe von
	 * 'minkowskiSumConvex' und 'isConvex' und fuehrt, wenn noetig eine
	 * Triangulierung der beiden Polygone durch.
	 * 
	 * @param poly1
	 *            von poly1 und poly2 soll die Minkowski-Summe berechnet werden.
	 * @param poly2
	 *            von poly1 und poly2 soll die Minkowski-Summe berechnet werden.
	 * 
	 * @return HolePolygon2 die Minkowski-Summe der beiden Polygone
	 */
	public static HolePolygon2 minkowskiSum(
			Polygon2 poly1,
			Polygon2 poly2)
	{

		boolean convex1 = poly1.isConvex();
		boolean convex2 = poly2.isConvex();

		if (convex1 && convex2)
		{
			ConvexPolygon2 cp1 = new ConvexPolygon2(poly1);
			ConvexPolygon2 cp2 = new ConvexPolygon2(poly2);
			return new HolePolygon2(cp1.minkowskiSum(cp2));
		}
		else if ((convex1) && (!convex2))
		{
			ConvexPolygon2 cp1 = new ConvexPolygon2(poly1);
			SeidelTriangulation tria = new SeidelTriangulation(poly2);
			ConvexPolygon2[] triangles = tria.getTriangles();
			HolePolygon2[] minkSums = new HolePolygon2[triangles.length];
			for (int i = 0; i < triangles.length; i++)
			{
				minkSums[i] = new HolePolygon2(cp1.minkowskiSum(triangles[i]));
			}

			return HolePolygon2.union(minkSums);

		}
		else if ((!convex1) && (convex2))
		{
			ConvexPolygon2 cp2 = new ConvexPolygon2(poly2);
			SeidelTriangulation tria = new SeidelTriangulation(poly1);
			ConvexPolygon2[] triangles = tria.getTriangles();
			HolePolygon2[] minkSums = new HolePolygon2[triangles.length];
			for (int i = 0; i < triangles.length; i++)
			{
				minkSums[i] = new HolePolygon2(cp2.minkowskiSum(triangles[i]));
			}

			return HolePolygon2.union(minkSums);

		}
		else
		{
			SeidelTriangulation tria1 = new SeidelTriangulation(poly1);
			SeidelTriangulation tria2 = new SeidelTriangulation(poly2);
			ConvexPolygon2[] triangles1 = tria1.getTriangles();
			ConvexPolygon2[] triangles2 = tria2.getTriangles();
			HolePolygon2[] minkSums = new HolePolygon2[triangles1.length
					* triangles2.length];

			for (int j = 0; j < triangles1.length; j++)
			{
				for (int i = 0; i < triangles2.length; i++)
				{
					minkSums[i * triangles1.length + j] = new HolePolygon2(
							triangles1[j].minkowskiSum(triangles2[i]));
				}
			}

			return HolePolygon2.union(minkSums);
		}
	}

}
