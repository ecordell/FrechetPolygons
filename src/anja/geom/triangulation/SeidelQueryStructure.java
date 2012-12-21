/* Anmerkung: zu diesem Projekt gehoerend die Dateien:
 * SeidelQueryStructure.java, SeidelTriangulation.java, SeidelNode.java
 */

package anja.geom.triangulation;


import java.util.Stack;
import java.util.Vector;

import java.awt.geom.Rectangle2D;

import anja.geom.Point2;
import anja.geom.Segment2;


/**
 * Die Klasse SeidelQueryStructure implementiert die 'Point Location Query
 * Structure' wie sie in 'Raimund Seidel: A Simple and Fast Incremental
 * Randomized Algorithm for Computing Trapezoidal Decompositions and for
 * Triangulating Polygons' vorgestellt wird. Die SeidelQueryStructure verwaltet
 * intern einen (s,t)-Digraphen.
 * 
 * @author Marina Bachran
 */
public class SeidelQueryStructure
		implements java.io.Serializable
{

	private Segment2[]	segments;
	private SeidelNode	root			= null;
	private SeidelNode	lowestTrapezoid	= null;

	/* temporaere Variablen */
	private SeidelNode	parent;
	private boolean		movedLeft;


	/* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*          Konstruktor                                                     */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	/**
	 * Konstruktor der Klasse SeidelQueryStructure. Das Polygon, das
	 * trianguliert werden soll, wird in einzelne Segmente zerlegt, diese werden
	 * dem Konstruktor uebergeben.
	 * 
	 * @param seg
	 *            die Einzelsegmente des Polygons
	 */
	public SeidelQueryStructure(
			Segment2[] seg)
	{
		// Segmente in einer zufaelligen Reihenfolge anordnen.
		segments = random(seg);
		// zu Beginn existiert nur ein Knoten im Baum, ein grosses Trapezoid.
		SeidelNode.trapID = 0;
		lowestTrapezoid = new SeidelNode();
		root = lowestTrapezoid;
		// Segmente in den Baum einfuegen.
		int segCount = segments.length;

		for (int i = 0; i < segCount; i++)
			insert(segments[i]);
	} // Konstructor von SeidelQueryStructure


	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*          Hilfsmethoden                                                   */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	/**
	 * Die Segmente werden in einer zufaelligen Reihenfolge angeordnet. Dabei
	 * wird das orginale Array nicht veraendert.
	 * 
	 * @param seg
	 * 
	 * @return Segment2[], die Segmente in zufaelliger Reihenfolge.
	 */
	private Segment2[] random(
			Segment2[] seg)
	{
		Segment2[] newSeg = copySegArray(seg);
		int n = newSeg.length;
		Segment2 segment = null;
		for (int i = 0; i < n; i++)
		{
			int r = (int) (Math.random() * n);
			if (r == i)
				continue;
			segment = newSeg[i];
			newSeg[i] = newSeg[r];
			newSeg[r] = segment;
		}
		return newSeg;
	} // random


	/**
	 * copySegArray kopiert ein Array von Segmenten in ein anderes Array von
	 * Segmenten.
	 * 
	 * @param oldSeg
	 *            das Segment-Array, das kopiert werden soll.
	 * 
	 * @return Segment2[], die Kopie von oldSeg
	 */
	private Segment2[] copySegArray(
			Segment2[] oldSeg)
	{
		int n = oldSeg.length;
		Segment2[] newSeg = new Segment2[n];
		for (int i = 0; i < n; i++)
			newSeg[i] = oldSeg[i];
		return newSeg;
	} // copySegArray


	/**
	 * Ueberprueft die Lage des Segmentes seg in Bezug auf das Segment boundary.
	 * Dabei wird nur der Bereich von seg betrachtet, das in dem Y-Intervall von
	 * boundary liegt. Es muss also ein Teil des Segmentes seg in dem
	 * Y-Intervall von boundary liegen, ein anderer Fall kann bei der
	 * Baumerstellung nicht vorkommen.
	 * 
	 * @param seg
	 *            Das Segment, dessen Lage ermittelt werden soll.
	 * @param boundary
	 *            Das Vergleichssegment
	 * 
	 * @return int ORIENTATION_LEFT - seg ist links von boundary
	 *         ORIENTATION_RIGHT - seg ist rechts von boundary
	 */
	private int getSegmentOrientation(
			Segment2 seg,
			Segment2 boundary)
	{
		// Schneide seg so zurecht, dass es im Y-Bereich von boundary liegt
		float x = seg.getLeftPoint().x;
		float width = seg.getRightPoint().x - x;
		float y = boundary.getLowerPoint().y;
		float height = boundary.getUpperPoint().y - y;
		if (height == 0)
		{ // boundary ist ein waagerechtes Segment
			// nehme einen Endpunkt von boundary der nicht auch ein Endpunkt von seg ist
			Point2 boundaryPoint = boundary.getUpperPoint();
			if ((boundaryPoint.comparePointsLexY(seg.getUpperPoint()) == 0)
					|| (boundaryPoint.comparePointsLexY(seg.getLowerPoint()) == 0))
				boundaryPoint = boundary.getLowerPoint();
			int temp = getPointOrientation(boundaryPoint, seg);
			if (temp == Point2.ORIENTATION_LEFT)
			{
				return Point2.ORIENTATION_RIGHT;
			}
			else if (temp == Point2.ORIENTATION_RIGHT)
			{
				return Point2.ORIENTATION_LEFT;
			}
			else
				return temp;
		}
		Rectangle2D clipRect = new Rectangle2D.Float(x, y, width, height);
		Segment2 clippedSeg = seg.clip(clipRect);
		// Bestimme Mittelpunkt von clippedSeg
		Point2 centerPoint = clippedSeg.center();
		// Ueberpruefe die lage des Punktes und gebe diese als Ergebnis zurueck.
		return getPointOrientation(centerPoint, boundary);
	} // getSegmentOrientation


	/**
	 * liefert die Orientierung des Punktes p in Bezug auf das Segment seg
	 * 
	 * @param p
	 *            Der Punkt
	 * @param seg
	 *            Das Segment
	 * 
	 * @return int ORIENTATION_LEFT - seg ist links von boundary,
	 *         ORIENTATION_RIGHT - seg ist rechts von boundary
	 */
	private int getPointOrientation(
			Point2 p,
			Segment2 seg)
	{
		Point2 src = seg.getLowerPoint();
		Point2 dest = seg.getUpperPoint();
		return p.orientation(src, dest);
	}


	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*     Methoden zum Einfuegen oder Lokalisieren von Punkten und Segmenten    */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	/**
	 * Fuegt ein Segment in die SeidelQueryStructure ein und benutzt dafuer
	 * insertYNode und insertXNode.
	 * 
	 * @param seg
	 *            Das einzufuegende Segment
	 */
	private void insert(
			Segment2 seg)
	{
		insertYNode(seg.getUpperPoint());
		insertYNode(seg.getLowerPoint());
		insertXNode(seg);
	} // insert


	/**
	 * Fuegt einen Y-Knoten in den Baum der SeidelQueryStructure ein.
	 * 
	 * @param point
	 *            Der einzufuegende Punkt
	 */
	private void insertYNode(
			Point2 point)
	{
		SeidelNode actual = locatePoint(point);
		if (actual == null)
			return;

		// habe jetzt das Trapezoid gefunden, das point enthaelt, dieses wird aufgeteilt.
		SeidelNode newYNode = new SeidelNode(point);
		SeidelNode clone = (SeidelNode) (actual.clone());
		newYNode.setSon(true, actual); // links
		newYNode.setSon(false, clone); // rechts
		actual.setPoint(true, point);
		clone.setPoint(false, point);

		// es gibt nur einen Nachbarn - dieser eine Nachbar wird gesetzt.
		actual.setTwoNeighbors(true, false);
		actual.setNeighbor(true, true, clone);
		actual.setNeighbor(true, false, null);
		// es gibt nur einen Nachbarn - dieser eine Nachbar wird gesetzt.
		clone.setTwoNeighbors(false, false);
		clone.setNeighbor(false, true, actual);
		clone.setNeighbor(false, false, null);

		// aktualisiere beim clone nach oben...
		SeidelNode leftUpperNeighbor = clone.getNeighbor(true, true);
		SeidelNode rightUpperNeighbor = clone.getNeighbor(true, false);
		boolean twoUpperNeighbors = clone.getTwoNeighbors(true);
		if (twoUpperNeighbors)
		{
			rightUpperNeighbor.setNeighbor(false, true, clone);
			leftUpperNeighbor.setNeighbor(false, true, clone);
		}
		else
		{
			// Ueberpruefe, ob der obere Nachbar - wenn existent - zwei Soehne hat
			if (leftUpperNeighbor != null)
			{
				boolean isLeft = true;
				// Welcher war vorher der actual ?
				if (leftUpperNeighbor.getTwoNeighbors(false))
					isLeft = (leftUpperNeighbor.getNeighbor(false, true) == actual);
				leftUpperNeighbor.setNeighbor(false, isLeft, clone);
			}
		}

		if (parent == null)
			root = newYNode;
		else
			parent.setSon(movedLeft, newYNode);
	} // insertYNode


	/**
	 * Fuegt einen X-Knoten die SeidelQueryStructure ein.
	 * 
	 * @param seg
	 *            Das einzufuengende Segment
	 */
	private void insertXNode(
			Segment2 seg)
	{
		Stack nodeStack = new Stack();
		Vector xNodeList = new Vector();
		Point2 upperSegPoint = seg.getUpperPoint();
		Point2 lowerSegPoint = seg.getLowerPoint();

		SeidelNode actual;

		nodeStack.push(root); // actual ist root
		nodeStack.push(null); // kein parent vorhanden
		do
		{
			parent = (SeidelNode) (nodeStack.pop());
			actual = (SeidelNode) (nodeStack.pop());
			movedLeft = false; // actual ist immer der rechter Sohn von parent oder parent ist null (bei root)
			while (actual.type != SeidelNode.TYPE_TRAPEZOID_ID)
			{
				parent = actual;
				if (actual.type == SeidelNode.TYPE_YNODE)
				{
					// Gehe links, wenn das Segment nicht oberhalb der YNode liegt
					movedLeft = (lowerSegPoint.comparePointsLexY(actual
							.getyNode()) < 0);
					if (movedLeft
							&& (upperSegPoint.comparePointsLexY(actual
									.getyNode()) > 0))
					{
						// Y-Wert des Punktes liegt zwischen den Y-Werten des Segmentes => auch rechts gehen!
						nodeStack.push(actual.getSon(!movedLeft)); // spaeter hiermit fortfahren
						nodeStack.push(actual); // als Vater des rechten Sohnes auf den Stack
					}
				}
				else
				{ // actual.type == SeidelNode.TYPE_XNODE
					// liegt neues Segment links von dem aktuellen xNode-Segment ?
					movedLeft = (getSegmentOrientation(seg, actual.getxNode()) != Point2.ORIENTATION_RIGHT);
				}
				actual = actual.getSon(movedLeft);
			} // while-Ende => (actual.type == Node.TYPE_TRAPEZOID_ID)

			// Ueberpruefe, ob actual schon einmal Sohn in xNodeList ist
			boolean listContainsActual = false;
			for (int i = 0; i < xNodeList.size(); i++)
			{
				if (actual == ((SeidelNode) xNodeList.get(i)).getSon(true))
					listContainsActual = true;
			}
			if (!listContainsActual)
			{
				SeidelNode newXNode = new SeidelNode(seg);
				newXNode.setSon(true, actual); // links
				newXNode.setSon(false, actual); // rechts
				parent.setSon(movedLeft, newXNode);
				xNodeList.add(newXNode);
			}

		}
		while (!(nodeStack.empty()));

		// am ersten Knoten werden die Soehne dupliziert...
		actual = (SeidelNode) (xNodeList.get(0));
		SeidelNode actualSon = actual.getSon(true); // beide Soehne sind gleich - nehme linken
		SeidelNode newRightSon = (SeidelNode) (actualSon.clone());
		actual.setSon(false, newRightSon); // rechten Sohn setzen

		Point2 upPoint = actualSon.getPoint(true);
		if (!((upPoint == null) || (seg.liesOn(upPoint))))
		{
			int ori = getPointOrientation(upPoint, seg);
			boolean isLeft = (ori != Point2.ORIENTATION_RIGHT);
			actual.getSon(!isLeft).setPoint(true, null);
		}
		Point2 lowPoint = actualSon.getPoint(false);
		if (!((lowPoint == null) || (seg.liesOn(lowPoint))))
		{
			int ori = getPointOrientation(lowPoint, seg);
			boolean isLeft = (ori != Point2.ORIENTATION_RIGHT);
			actual.getSon(!isLeft).setPoint(false, null);
		}

		actualSon.setBorder(false, actual.getxNode());
		newRightSon.setBorder(true, actual.getxNode());

		// Nachbarschaftsbeziehung neu verknuepfen...
		SeidelNode rightLowerNeigh;
		SeidelNode leftLowerNeigh = actualSon.getNeighbor(false, true);
		boolean hasTwoLowerNeigh = actualSon.getTwoNeighbors(false);
		if (hasTwoLowerNeigh)
			rightLowerNeigh = actualSon.getNeighbor(false, false);
		else
			rightLowerNeigh = actualSon.getNeighbor(false, true); // es existiert nur ein Nachbar!

		// linke oder rechte Seite wird von zwei Segmenten begrenzt, die einen gemeinsamen unteren Punkt haben?
		Segment2 leftBorder = actualSon.getBorder(true);
		Segment2 rightBorder = newRightSon.getBorder(false);
		boolean leftCompareLowerPoints = (leftBorder != null)
				&& (seg.getLowerPoint().comparePointsLexY(
						leftBorder.getLowerPoint()) == 0);
		boolean rightCompareLowerPoints = (rightBorder != null)
				&& (seg.getLowerPoint().comparePointsLexY(
						rightBorder.getLowerPoint()) == 0);

		actualSon.setTwoNeighbors(false, false);
		actualSon.setNeighbor(false, true, (leftCompareLowerPoints ? null
				: leftLowerNeigh));
		actualSon.setNeighbor(false, false, null);
		newRightSon.setTwoNeighbors(false, false);
		newRightSon.setNeighbor(false, true, (rightCompareLowerPoints ? null
				: rightLowerNeigh));
		newRightSon.setNeighbor(false, false, null);

		if (hasTwoLowerNeigh)
		{
			leftLowerNeigh.setTwoNeighbors(true, false);
			leftLowerNeigh.setNeighbor(true, true, actualSon);
			leftLowerNeigh.setNeighbor(true, false, null);
			rightLowerNeigh.setTwoNeighbors(true, false);
			rightLowerNeigh.setNeighbor(true, true, newRightSon);
		}
		else
		{
			leftLowerNeigh.setTwoNeighbors(true, true);
			if (!leftCompareLowerPoints)
				leftLowerNeigh.setNeighbor(true, true, actualSon);
			if (!rightCompareLowerPoints)
				leftLowerNeigh.setNeighbor(true, false, newRightSon);
		}

		// Beide Seiten nach oben korrigieren
		correctNeighborhoodAfterXSplit(actual);

		// Trapezoide werden aufgeteilt und zusammengefasst
		for (int i = 1; i < xNodeList.size(); i++)
		{
			actual = (SeidelNode) (xNodeList.get(i));
			actualSon = actual.getSon(true); // beide Soehne sind gleich - nehme linken
			SeidelNode last = (SeidelNode) (xNodeList.get(i - 1));

			// Vergleiche angrenzende Segmente der linken Trapezoide - sind
			//   diese nicht gleich, so ist dies bei der rechten Seite der Fall!
			boolean mergeLeft = (actualSon.getBorder(true) == last.getSon(true)
					.getBorder(true));
			actual.insertNewUpdatePartner(mergeLeft, last); // Setze auf Merge-Seite bei der X-Node die Information, dass noch die anderen XNodes aktualisiert werden muessen.
			actual.setSon(mergeLeft, last.getSon(mergeLeft)); // vereine Trapezoide
			actualSon.setBorder(mergeLeft, actual.getxNode()); // Setze angrenzendes Segment des urspruenglichen Sohnes

			if (seg.liesOn(actualSon.getPoint(true)))
			{
				// der Punkt ist auf dem neu eingefuegten Segment
				last.getSon(mergeLeft).setPoint(true, actualSon.getPoint(true));
			}
			else if ((getPointOrientation(actualSon.getPoint(true), seg) != Point2.ORIENTATION_RIGHT) == mergeLeft)
			{
				// der obere Punkt befindet sich auf der Seite, wo Trapezoide zusammengefuegt werden.
				last.getSon(mergeLeft).setPoint(true, actualSon.getPoint(true));
				actualSon.setPoint(true, null);
			}

			// Durch Zusammenlegen bekommt Merge-Seite die Einstellungen nach oben von anderer Seite kopiert
			actual.getSon(mergeLeft).setTwoNeighbors(true,
					actual.getSon(!mergeLeft).getTwoNeighbors(true));
			actual.getSon(mergeLeft).setNeighbor(true, true,
					actual.getSon(!mergeLeft).getNeighbor(true, true));
			actual.getSon(mergeLeft).setNeighbor(true, false,
					actual.getSon(!mergeLeft).getNeighbor(true, false));

			// Nicht Merge-Seite muss nach unten korrigiert werden -> nur noch einen Nachbarn! Vom unteren aus gesehen war er richtig gesetzt.
			boolean mergeSideWasNeighbor = (actual.getSon(!mergeLeft)
					.getNeighbor(false, mergeLeft) == actual.getSon(mergeLeft));
			if (mergeSideWasNeighbor)
			{
				actual.getSon(!mergeLeft).setTwoNeighbors(false, false);
				actual.getSon(!mergeLeft).setNeighbor(
						false,
						true,
						actual.getSon(!mergeLeft)
								.getNeighbor(false, !mergeLeft));
				actual.getSon(!mergeLeft).setNeighbor(false, false, null);
			} // else: lasse die Nachbarn so wie sie sind
			// Beide Seiten nach oben korrigieren
			correctNeighborhoodAfterXSplit(actual);
		}
	} // insertXNode


	/**
	 * Lokalisiert den Trapezoid-Knoten in der Query-Structure, der point
	 * enthaelt, und gibt diesen zurueck.
	 * 
	 * @param point
	 *            Der Punkt, der lokalisiert wird.
	 * 
	 * @return Der Trapezoid-Knoten
	 */
	public SeidelNode locatePoint(
			Point2 point)
	{
		parent = null;
		SeidelNode actual = root;
		movedLeft = false;
		// Suche nach dem Trapezoid-Knoten der point enthaelt.
		while (actual.type != SeidelNode.TYPE_TRAPEZOID_ID)
		{
			// der aktuelle Knoten ist ein Y-Knoten...
			if (actual.type == SeidelNode.TYPE_YNODE)
			{
				int pointComp = point.comparePointsLexY(actual.getyNode());
				if (pointComp == 0)
					return null; // gleicher Punkt => wird nicht eingefuegt
				parent = actual;
				movedLeft = (pointComp == -1); // kleiner => gehe links weiter
				actual = actual.getSon(movedLeft);
				// der aktuelle Knoten ist ein X-Knoten...
			}
			else
			{ // actual.type == Node.TYPE_XNODE
				int pointOri = getPointOrientation(point, actual.getxNode());
				parent = actual;
				movedLeft = (pointOri != Point2.ORIENTATION_RIGHT); // Punkt links?
				actual = actual.getSon(movedLeft);
			}
		}
		return actual;
	} // locatePoint


	/**
	 * Liefert das tiefste innere Trapezoid zurueck.
	 * 
	 * @return Das unterste Trapezoid des Polygons
	 */
	public SeidelNode locateLowestPolyTrap()
	{
		Point2 lowestPoint = lowestTrapezoid.getPoint(true);
		// Segmente suchen, die lowestPoint enthalten...
		int index = 0;
		Point2 center = null;

		float y = lowestPoint.y;
		float height = Float.MAX_VALUE;

		Segment2[] segs = new Segment2[2];
		for (int i = 0; i < segments.length; i++)
		{
			if (segments[i].getLowerPoint().comparePointsLexY(lowestPoint) == 0)
			{
				segs[index++] = segments[i];
				if ((height > (segments[i].getUpperPoint().y - lowestPoint.y))
						&& (segments[i].getUpperPoint().y != lowestPoint.y))
					height = segments[i].getUpperPoint().y - lowestPoint.y;
			}
			else if (height > (segments[i].getLowerPoint().y - lowestPoint.y))
			{
				if (segments[i].getLowerPoint().y != lowestPoint.y)
					height = segments[i].getLowerPoint().y - lowestPoint.y;
			}
		}
		if (segs[0].getUpperPoint().y == lowestPoint.y)
		{
			float xMiddle = (lowestPoint.x + segs[0].getUpperPoint().x) / 2;
			center = new Point2(xMiddle, lowestPoint.y);
		}
		else if (segs[1].getUpperPoint().y == lowestPoint.y)
		{
			float xMiddle = (lowestPoint.x + segs[1].getUpperPoint().x) / 2;
			center = new Point2(xMiddle, lowestPoint.y);
		}
		else
		{
			// jetzt ein Dreieck aus den Segmenten basteln...
			Point2 p1 = segs[0].getUpperPoint();
			Point2 p2 = segs[1].getUpperPoint();
			int compare = p1.comparePointsLexY(p2);
			float x = ((p1.x < p2.x) ? p1.x : p2.x);
			float x_big = ((p1.x > p2.x) ? p1.x : p2.x);
			if (lowestPoint.x < x)
				x = lowestPoint.x;
			if (lowestPoint.x > x_big)
				x_big = lowestPoint.x;
			float width = Math.abs(x_big - x);
			Rectangle2D clipRect = new Rectangle2D.Float(x, y, width, height);
			segs[0] = segs[0].clip(clipRect);
			segs[1] = segs[1].clip(clipRect);
			p1 = segs[0].getUpperPoint();
			p2 = segs[1].getUpperPoint();
			// center wird im gesuchten Trapezoid liegen...
			center = new Point2((p1.x + p2.x + lowestPoint.x) / 3,
					(p1.y + p2.y + lowestPoint.y) / 3);
		}
		return locatePoint(center);
	} // locateLowestPolyTrap


	/**
	 * Die Nachbarschaftsbeziehungen actXNode werden korrigiert.
	 * 
	 * @param actXNode
	 *            Der Knoten
	 */
	private void correctNeighborhoodAfterXSplit(
			SeidelNode actXNode)
	{
		SeidelNode leftTrap = actXNode.getSon(true);
		SeidelNode rightTrap = actXNode.getSon(false);
		SeidelNode upperTrap = null;
		boolean leftHasUpperPoint = (leftTrap.getPoint(true) != null);
		boolean rightHasUpperPoint = (rightTrap.getPoint(true) != null);
		boolean existOnlyOneUpperNeighbour = !leftTrap.getTwoNeighbors(true);

		if (leftHasUpperPoint && rightHasUpperPoint)
		{
			// Segment-Ende als Upper-Point -> zwei Faelle:
			//     (a) vorher entweder zwei obere Nachbarn oder einer und der
			//         upperPoint gehoert nicht zu einem Grenzsegment
			//     (b) vorher ein oberer Nachbar und (a) trifft nicht zu
			Point2 upperPoint = leftTrap.getPoint(true);
			Segment2 boundingSegmentLeft = leftTrap.getBorder(true);
			Segment2 boundingSegmentRight = rightTrap.getBorder(false);
			boolean isBoundingPointLeft = (boundingSegmentLeft == null) ? false
					: boundingSegmentLeft.liesOn(upperPoint);
			boolean isBoundingPointRight = (boundingSegmentRight == null) ? false
					: boundingSegmentRight.liesOn(upperPoint);

			if ((!existOnlyOneUpperNeighbour)
					|| ((!isBoundingPointLeft) && (!isBoundingPointRight)))
			{ // Fall (a)
				// linke Seite
				upperTrap = leftTrap.getNeighbor(true, true);
				leftTrap.setTwoNeighbors(true, false); // nur ein oberer Nachbar
				leftTrap.setNeighbor(true, true, upperTrap);
				leftTrap.setNeighbor(true, false, null);
				if (rightTrap.getTwoNeighbors(true))
				{
					// Der oberste Teil des eingefuegten Segmentes ist erreicht und der Segment-Endpunkt ist Endpunkt eines anderen Segmentes
					upperTrap.setTwoNeighbors(false, false);
					upperTrap.setNeighbor(false, true, leftTrap);
					upperTrap.setNeighbor(false, false, null);
					// rechte Seite
					upperTrap = rightTrap.getNeighbor(true, false);
					upperTrap.setTwoNeighbors(false, false);
					upperTrap.setNeighbor(false, true, rightTrap);
					upperTrap.setNeighbor(false, false, null);
				}
				else
				{
					// Das obere evtl. noch nicht geteilte Trapezoid hat zwei untere Nachbarn
					upperTrap.setTwoNeighbors(false, true);
					upperTrap.setNeighbor(false, true, leftTrap);
					upperTrap.setNeighbor(false, false, rightTrap);
				}
				rightTrap.setTwoNeighbors(true, false); // nur ein oberer Nachbar
				rightTrap.setNeighbor(true, true, upperTrap);
				rightTrap.setNeighbor(true, false, null);
			}
			else
			{ // Korrektur falls Fall (b)
				upperTrap = leftTrap.getNeighbor(true, true);
				leftTrap.setTwoNeighbors(true, false); // nur ein oberer Nachbar
				leftTrap.setNeighbor(true, false, null);
				rightTrap.setTwoNeighbors(true, false); // nur ein oberer Nachbar
				rightTrap.setNeighbor(true, false, null);
				if (isBoundingPointLeft)
				{
					rightTrap.setNeighbor(true, true, upperTrap);
					upperTrap.setNeighbor(false, false, rightTrap);
					leftTrap.setNeighbor(true, true, null);
				}
				if (isBoundingPointRight)
				{
					leftTrap.setNeighbor(true, true, upperTrap);
					upperTrap.setNeighbor(false, true, leftTrap);
					rightTrap.setNeighbor(true, true, null);
				}
			}
		}
		else if (!leftHasUpperPoint)
		{
			// Das linke Trapezoid hat keinen oberen Punkt => es hat nur einen oberen Nachbarn und ist davon der linke untere Nachbar
			upperTrap = leftTrap.getNeighbor(true, true);
			leftTrap.setTwoNeighbors(true, false);
			leftTrap.setNeighbor(true, true, upperTrap);
			leftTrap.setNeighbor(true, false, null);
			// Das obere noch nicht geteilte Trapezoid hat immer zwei untere Nachbarn
			Segment2 rightBorder = rightTrap.getBorder(false);
			if ((rightBorder != null)
					&& (rightTrap.getPoint(true) != null)
					&& (upperTrap.getTwoNeighbors(false))
					&& (rightBorder.getUpperPoint().comparePointsLexY(
							rightTrap.getPoint(true)) == 0))
			{
				// kurzzeitig drei -> linke Seite wird im naechsten Schritt zusammengelegt!
				//                 -> rechte Border hat upper- und lowerPoint des rechten Trapezes
				upperTrap.setNeighbor(false, true, rightTrap);
			}
			else
			{
				upperTrap.setTwoNeighbors(false, true);
				upperTrap.setNeighbor(false, true, leftTrap);
				upperTrap.setNeighbor(false, false, rightTrap);
			}
			if (!existOnlyOneUpperNeighbour)
			{
				upperTrap = rightTrap.getNeighbor(true, false);
				upperTrap.setNeighbor(false, true, rightTrap);
			}
		}
		else if (!rightHasUpperPoint)
		{
			// Das rechte Trapezoid hat keinen oberen Punkt => es hat nur einen oberen Nachbarn und ist davon der rechte untere Nachbar
			if (rightTrap.getTwoNeighbors(true)) // wenn zwei obere Nachbarn existierten, dann nehme den rechten
				upperTrap = rightTrap.getNeighbor(true, false);
			else
				upperTrap = rightTrap.getNeighbor(true, true);
			rightTrap.setTwoNeighbors(true, false);
			rightTrap.setNeighbor(true, true, upperTrap);
			rightTrap.setNeighbor(true, false, null);
			// Das obere noch nicht geteilte Trapezoid hat immer zwei untere Nachbarn
			Segment2 leftBorder = leftTrap.getBorder(true);

			if ((leftBorder != null)
					&& (leftTrap.getPoint(true) != null)
					&& (upperTrap.getTwoNeighbors(false))
					&& ((leftBorder.getUpperPoint().comparePointsLexY(
							leftTrap.getPoint(true)) == 0)))
			{
				// kurzzeitig drei -> rechte Seite wird im naechsten Schritt zusammengelegt!
				//                 -> linke Border hat upper- und lowerPoint des linken Trapezes
				upperTrap.setNeighbor(false, false, leftTrap);
			}
			else
			{
				upperTrap.setTwoNeighbors(false, true);
				upperTrap.setNeighbor(false, true, leftTrap);
				upperTrap.setNeighbor(false, false, rightTrap);
			}
			if (!existOnlyOneUpperNeighbour)
			{
				upperTrap = leftTrap.getNeighbor(true, true);
				if (upperTrap.getTwoNeighbors(false))
					upperTrap.setNeighbor(false, false, leftTrap);
				else
					upperTrap.setNeighbor(false, true, leftTrap);
			}
		} // else: kann nicht passieren

	} // correctNeighborhoodAfterXSplit

}
