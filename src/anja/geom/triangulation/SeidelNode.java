/* Anmerkung: zu diesem Projekt gehoerend die Dateien:
 * SeidelQueryStructure.java, SeidelTriangulation.java, SeidelNode.java
 */

package anja.geom.triangulation;


import java.util.Vector;

import anja.geom.Point2;
import anja.geom.Polygon2;
import anja.geom.Rectangle2;
import anja.geom.Segment2;


/**
 * Vom Typ der Klasse Node sollen spaeter die Elemente der SeidelQueryStructure
 * sein. Es gibt 3 unterschiedliche Arten von Nodes. Naehere Informationen sind
 * in 'Raimund Seidel: A Simple and Fast Incremental Randomized Algorithm for
 * Computing Trapezoidal Decompositions and for Triangulating Polygons' zu
 * finden.
 * 
 * @author Marina Bachran
 */
class SeidelNode
		implements Cloneable, java.io.Serializable
{

	/** Konstante */
	public static int		TYPE_YNODE			= 1;

	/** Konstante */
	public static int		TYPE_XNODE			= 2;

	/** Konstante */
	public static int		TYPE_TRAPEZOID_ID	= 3;

	/**
	 * Die Trapezoide werden durchnummeriert in der Reihenfolge in der sie
	 * erstellt werden. trapID enthaelt die aktuell hoechste Nummer.
	 */
	public static int		trapID				= 0;

	/** Knotentyp analog zu dem oben angegebenen Paper */
	public int				type				= 0;

	private Point2			yNode				= null;
	private Segment2		xNode				= null;
	private int				trapezoidID			= -1;

	private SeidelNode		leftSon				= null;
	private SeidelNode		rightSon			= null;

	// fuer die Begrenzung der Trapezoide
	private Segment2		leftBorder			= null;
	private Segment2		rightBorder			= null;

	// zum Speichern, welche Segmentendpunkte ein bestimmtes Trapezoid begrenzen
	private Point2			upperPoint;
	private Point2			lowerPoint;

	/* um einen Nachbarschaftsgraphen aufzubauen. */
	/** ist die obere Trapezoidgrenze aufgeteilt? */
	private boolean			twoUpperNeighbors;

	/** ist die untere Trapezoidgrenze aufgeteilt? */
	private boolean			twoLowerNeighbors;

	/** enthaelt die hoechstens 4 Nachbarn eines Trapezoids */
	private SeidelNode[]	neighbor			= new SeidelNode[4];

	/**
	 * zum Speichern bei der Graphtraversierung, welche der vier moeglichen
	 * Verbindungen schon benutzt wurde.
	 */
	private boolean[]		used				= new boolean[4];

	/**
	 * temporaere Verkettung von X-Knoten beim Einfuegen von X-Knoten in die
	 * SeidelQueryStructure
	 */
	private SeidelNode		nextNode			= null;

	/** fuer X-Nodes */
	Vector					leftUpdPartners		= new Vector();
	Vector					rightUpdPartners	= new Vector();


	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*          Konstruktoren                                                   */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	/**
	 * Ein Y-Node enthaelt einen Endpunkt eines Liniensegments. Hier ist das
	 * also ein Eckpunkt des Polygons, das trianguliert wird.
	 * 
	 * @param y
	 *            Der Knoten
	 */
	public SeidelNode(
			Point2 y)
	{
		yNode = y;
		type = TYPE_YNODE;
	}


	/**
	 * Ein X-Node enthaelt einen Liniensegment, also eine Kante des Polygons,
	 * das trianguliert wird.
	 * 
	 * @param x
	 *            Das Liniensegment
	 */
	public SeidelNode(
			Segment2 x)
	{
		xNode = x;
		type = TYPE_XNODE;
	}


	/**
	 * Das Polygon und dessen Umgebung wird in Trapezoide zerlegt. Diese
	 * Trapezoide werden mit einer ID versehen.
	 */
	public SeidelNode()
	{
		trapezoidID = trapID++;
		type = TYPE_TRAPEZOID_ID;
		// um einen Nachbarschaftsgraphen der Trapezoide aufzubauen
		twoUpperNeighbors = false;
		twoLowerNeighbors = false;
		for (int i = 0; i < 4; i++)
		{
			neighbor[i] = null;
			used[i] = false;
		}
	}


	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*          Zugriffsmethoden                                                */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	/**
	 * Liefert den Inhalt des Y-Knotens zurueck.
	 * 
	 * @return Der Knoten
	 */
	public Point2 getyNode()
	{
		return yNode;
	}


	/**
	 * Liefert den Inhalt des X-Knotens zurueck.
	 * 
	 * @return Das Liniensegment
	 */
	public Segment2 getxNode()
	{
		return xNode;
	}


	/**
	 * Liefert die TrapezoidID (sink) zurueck.
	 * 
	 * @return Die ID
	 */
	public int getTrapezoidID()
	{
		return trapezoidID;
	}


	/**
	 * Setzt den linken Sohn des Knotens node, wenn setLeft true ist, ansonsten
	 * den rechten, dabei werden auftretene Veraenderungen Graphen korrigiert.
	 * 
	 * @param setLeft
	 *            entscheidet, ob der linke oder rechte Sohn gesetzt werden
	 *            soll. SeidelNode node von diesem Knoten wird ein Sohn gesetzt.
	 * @param node
	 *            Der Sohn
	 */
	public void setSon(
			boolean setLeft,
			SeidelNode node)
	{
		if (type == TYPE_XNODE)
		{
			if (setLeft)
			{
				for (int i = 0; i < leftUpdPartners.size(); i++)
					((SeidelNode) leftUpdPartners.get(i))
							.setSonWithoutUpdatePartners(setLeft, node);
			}
			else
			{
				for (int i = 0; i < rightUpdPartners.size(); i++)
					((SeidelNode) rightUpdPartners.get(i))
							.setSonWithoutUpdatePartners(setLeft, node);
			}
		}
		if (setLeft)
			leftSon = node;
		else
			rightSon = node;
	}


	/**
	 * Setzt den linken Sohn des Knotens node, wenn setLeft true ist, ansonsten
	 * den rechten.
	 * 
	 * @param setLeft
	 *            entscheidet, ob der linke oder rechte Sohn gesetzt werden
	 *            soll. SeidelNode node von diesem Knoten wird ein Sohn gesetzt.
	 * @param node
	 *            Der Sohn
	 */
	public void setSonWithoutUpdatePartners(
			boolean setLeft,
			SeidelNode node)
	{
		if (setLeft)
			leftSon = node;
		else
			rightSon = node;
	}


	/**
	 * Liefert den linken Sohn fuer getLeft = true, sonst den rechten
	 * 
	 * @param getLeft
	 *            true für linken Sohn, rechts sonst
	 * 
	 * @return Der Sohn entsprechend des Parameters
	 */
	public SeidelNode getSon(
			boolean getLeft)
	{
		return (getLeft ? leftSon : rightSon);
	}


	/**
	 * Setzt als linke Grenze das Segment seg, wenn setLeft true ist, ansonsten
	 * als rechte Grenze.
	 * 
	 * @param setLeft
	 *            true für links, false sonst
	 * @param seg
	 *            Die Grenze
	 */
	public void setBorder(
			boolean setLeft,
			Segment2 seg)
	{
		if (setLeft)
			leftBorder = seg;
		else
			rightBorder = seg;
	}


	/**
	 * Liefert die linke Grenze zurueck, wenn getLeft = true ist, ansonsten die
	 * rechte
	 * 
	 * @param getLeft
	 *            true für die linke Grenze, false sonst
	 * 
	 * @return Die Grenze je nach Parameter
	 */
	public Segment2 getBorder(
			boolean getLeft)
	{
		return (getLeft ? leftBorder : rightBorder);
	}


	/**
	 * Setzt einen Punkt, der auf einem Grenzsegment eines Trapezoids liegt. Ist
	 * setUpperPoint true, so wird der obere Punkt gesetzt. (oben entspricht im
	 * Graphen links)
	 * 
	 * @param setUpperPoint
	 *            true für oben/links, false sonst
	 * @param p
	 *            Der Punkt
	 */
	public void setPoint(
			boolean setUpperPoint,
			Point2 p)
	{
		if (setUpperPoint)
			upperPoint = p;
		else
			lowerPoint = p;
	}


	/**
	 * Liefert einen Punkt zurueck, der gleichzeitig das Ende eines Segmentes
	 * ist und auf dem Rand eines Trapezoids liegt. Ist getUpperPoint true, so
	 * wird der obere Punkt zurueck gegeben, ansonsten der untere (ein Trapezoid
	 * besitzt immer zwei).
	 * 
	 * @param getUpperPoint
	 *            true für oben, false sonst
	 * 
	 * @return Der Punkt
	 */
	public Point2 getPoint(
			boolean getUpperPoint)
	{
		return (getUpperPoint ? upperPoint : lowerPoint);
	}


	/**
	 * liefert zurueck, ob ein Trapezoid ein oder zwei Nachbarn hat.
	 * 
	 * @param getUpperNeighbors
	 *            true für oben, false sonst
	 * 
	 * @return true für 2, false sonst
	 * 
	 * @see #twoLowerNeighbors
	 * @see #twoUpperNeighbors
	 */
	public boolean getTwoNeighbors(
			boolean getUpperNeighbors)
	{
		return (getUpperNeighbors ? twoUpperNeighbors : twoLowerNeighbors);
	}


	/**
	 * gibt an, ob zwei Nachbarn existieren, gibt es nur einen, so wird dieser
	 * zum linken Nachbarn.
	 * 
	 * @param setUpperNeighbors
	 *            true für oben, false sonst
	 * @param split
	 *            Zahl der Nachbarn, true für 2, false sonst
	 */
	public void setTwoNeighbors(
			boolean setUpperNeighbors,
			boolean split)
	{
		if (setUpperNeighbors)
			twoUpperNeighbors = split;
		else
			twoLowerNeighbors = split;
	}


	/**
	 * Liefert den Nachbarn in Abhaenigkeit von isUpper und isLeft zurueck
	 * 
	 * @param isUpper
	 *            true für oben, false sonst
	 * @param isLeft
	 *            true für links, false sonst
	 * 
	 * @return Der Nachbar als SeidelNode
	 */
	public SeidelNode getNeighbor(
			boolean isUpper,
			boolean isLeft)
	{
		// 0 => o/l, 1 => o/r, 2 => u/l, 3 => u/r
		// (u=unten, o=oben, l=links, r=rechts)
		int index = (isUpper ? 0 : 2) + (isLeft ? 0 : 1);
		return neighbor[index];
	}


	/**
	 * Setzt den Nachbarn in Abhaenigkeit von isUpper und isLeft.
	 * 
	 * @param isUpper
	 *            true für oben, false sonst
	 * @param isLeft
	 *            true für links, false sonst
	 * @param neighbour
	 *            Der Nachbar als SeidelNode
	 */
	public void setNeighbor(
			boolean isUpper,
			boolean isLeft,
			SeidelNode neighbour)
	{
		// 0 => o/l, 1 => o/r, 2 => u/l, 3 => u/r
		// (u=unten, o=oben, l=links, r=rechts)
		int index = (isUpper ? 0 : 2) + (isLeft ? 0 : 1);
		neighbor[index] = neighbour;
	}


	/**
	 * Liefert zurueck, ob der Weg ueber ein Nachbar-Trapezoid schon durchlaufen
	 * wurde. (Methode fuer die Traversierung der Trapezoide.)
	 * 
	 * @param isUpper
	 *            true für oben, false sonst
	 * @param isLeft
	 *            true für links, false sonst
	 * 
	 * @return true, falls bereits durchlaufen, false sonst
	 */
	public boolean getUsedNeighbor(
			boolean isUpper,
			boolean isLeft)
	{
		// 0 => o/l, 1 => o/r, 2 => u/l, 3 => u/r  (u=unten, o=oben, l=links, r=rechts)
		int index = (isUpper ? 0 : 2) + (isLeft ? 0 : 1);
		return used[index];
	}


	/**
	 * Setzt den Weg ueber ein Nachbar-Trapezoid als schon benutzt oder als
	 * unbenutzt.(Methode fuer die Traversierung der Trapezoide.)
	 * 
	 * @param isUpper
	 *            true für oben, false sonst
	 * @param isLeft
	 *            true für links, false sonst
	 * @param use
	 *            true für benutzt, false sonst
	 */
	public void setUsedNeighbor(
			boolean isUpper,
			boolean isLeft,
			boolean use)
	{
		// 0 => o/l, 1 => o/r, 2 => u/l, 3 => u/r  (u=unten, o=oben, l=links, r=rechts)
		int index = (isUpper ? 0 : 2) + (isLeft ? 0 : 1);
		used[index] = use;
	}


	/**
	 * Fuegt einen neuen 'Auto-Update-Partner' ein.
	 * 
	 * @param isLeft
	 *            true für links, false sonst
	 * @param otherXNode
	 *            Neuer Update-Partner
	 */
	public void insertNewUpdatePartner(
			boolean isLeft,
			SeidelNode otherXNode)
	{
		if (isLeft)
		{
			leftUpdPartners = (Vector) otherXNode.getUpdatePartners(isLeft)
					.clone();
			for (int i = 0; i < leftUpdPartners.size(); i++)
				((SeidelNode) leftUpdPartners.get(i)).updatePartnerList(isLeft,
						this);
			otherXNode.updatePartnerList(isLeft, this);
			leftUpdPartners.add(otherXNode);
		}
		else
		{
			rightUpdPartners = (Vector) otherXNode.getUpdatePartners(isLeft)
					.clone();
			for (int i = 0; i < rightUpdPartners.size(); i++)
				((SeidelNode) rightUpdPartners.get(i)).updatePartnerList(
						isLeft, this);
			otherXNode.updatePartnerList(isLeft, this);
			rightUpdPartners.add(otherXNode);
		}
	}


	/**
	 * Aktualisiert die 'Auto-Update-Partner-Liste'
	 * 
	 * @param isLeft
	 *            true für links, false sonst
	 * @param otherXNode
	 *            Der hinzuzufügende, neue Partner
	 */
	public void updatePartnerList(
			boolean isLeft,
			SeidelNode otherXNode)
	{
		if (isLeft)
			leftUpdPartners.add(otherXNode);
		else
			rightUpdPartners.add(otherXNode);
	}


	/**
	 * Liefert die 'Auto-Update-Partner-Liste'
	 * 
	 * @param isLeft
	 *            true für links, false sonst
	 * 
	 * @return Alle Update-Partner in einer Liste (Vector)
	 */
	public Vector getUpdatePartners(
			boolean isLeft)
	{
		return (isLeft ? leftUpdPartners : rightUpdPartners);
	}


	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*          Sonstige Methoden                                               */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	/**
	 * Liefert fuer ein Trapezoid ein Polygon
	 * 
	 * @return Das Trapezoid-Polygon
	 */
	public Polygon2 getPolygon()
	{
		if (type == TYPE_TRAPEZOID_ID)
		{
			Polygon2 poly = new Polygon2();
			Segment2 leftSeg = getBorder(true);
			Segment2 rightSeg = getBorder(false);
			float left = (leftSeg.source().x < leftSeg.target().x) ? leftSeg
					.source().x : leftSeg.target().x;
			float right = (rightSeg.source().x > rightSeg.target().x) ? rightSeg
					.source().x
					: rightSeg.target().x;

			//Hier immer aufrunden um Floating-Point Fehler zu vermeiden!
			float width = (int) (right - left + 1);
			//float width = right - left;

			float bottom = getPoint(false).y;
			float height = getPoint(true).y - bottom;
			Rectangle2 clipRect = new Rectangle2(left, bottom, width, height);
			leftSeg = leftSeg.clip(clipRect);
			rightSeg = rightSeg.clip(clipRect);
			Point2 lowerLeft = leftSeg.getLowerPoint();
			Point2 upperLeft = leftSeg.getUpperPoint();
			Point2 upperRight = rightSeg.getUpperPoint();
			Point2 lowerRight = rightSeg.getLowerPoint();
			poly.addPoint(lowerLeft);
			poly.addPoint(upperLeft);
			if (!upperRight.equals(upperLeft))
				poly.addPoint(upperRight);
			if (!lowerRight.equals(lowerLeft))
				poly.addPoint(lowerRight);
			return poly;
		}
		return null;
	}


	/**
	 * Liefert einen Clone des Knotens zurueck.
	 * 
	 * @return Die Kopie
	 */
	public Object clone()
	{
		SeidelNode cloned = null;
		if (type == TYPE_YNODE)
			cloned = new SeidelNode(getyNode());
		else if (type == TYPE_XNODE)
			cloned = new SeidelNode(getxNode());
		else if (type == TYPE_TRAPEZOID_ID)
		{
			cloned = new SeidelNode();
			cloned.setBorder(true, getBorder(true));
			cloned.setBorder(false, getBorder(false));
			cloned.setPoint(true, getPoint(true));
			cloned.setPoint(false, getPoint(false));
			cloned.setTwoNeighbors(true, getTwoNeighbors(true));
			cloned.setTwoNeighbors(false, getTwoNeighbors(false));
			cloned.setNeighbor(true, true, getNeighbor(true, true));
			cloned.setNeighbor(true, false, getNeighbor(true, false));
			cloned.setNeighbor(false, true, getNeighbor(false, true));
			cloned.setNeighbor(false, false, getNeighbor(false, false));
			cloned.setUsedNeighbor(true, true, getUsedNeighbor(true, true));
			cloned.setUsedNeighbor(true, false, getUsedNeighbor(true, false));
			cloned.setUsedNeighbor(false, true, getUsedNeighbor(false, true));
			cloned.setUsedNeighbor(false, false, getUsedNeighbor(false, false));
		}
		cloned.setSon(true, getSon(true));
		cloned.setSon(false, getSon(false));
		return cloned;
	}


	/**
	 * Liefert eine textuelle Repraesentation eines Knotens.
	 * 
	 * @return Die textuelle Repräsentation
	 */
	public String toString()
	{
		String s = "";
		switch (type)
		{
			case 1:
				s = "Y-Node: " + getyNode().toString();
				break;
			case 2:
				s = "X-Node: " + getxNode().toString();
				break;
			case 3:
				s = "Trapazoid-Node: ID=" + getTrapezoidID()
						+ ",  border(l,r)=(";
				if (getBorder(true) != null)
					s += getBorder(true).toString();
				s += ", ";
				if (getBorder(false) != null)
					s += getBorder(false).toString();
				s += ")";

				s += ",  points(u,l)=(";
				if (getPoint(true) != null)
					s += getPoint(true).toString();
				s += ", ";
				if (getPoint(false) != null)
					s += getPoint(false).toString();
				s += ")";

				s += ",  neighbors(ul,ur,ll,lr)=(";
				if (getNeighbor(true, true) != null)
					s += getNeighbor(true, true).getTrapezoidID();
				s += ", ";
				if (getTwoNeighbors(true))
					s += getNeighbor(true, false).getTrapezoidID();
				s += ", ";
				if (getNeighbor(false, true) != null)
					s += getNeighbor(false, true).getTrapezoidID();
				s += ", ";
				if (getTwoNeighbors(false))
					s += getNeighbor(false, false).getTrapezoidID();
				s += ")";

				break;
		}
		return s;
	}

}
