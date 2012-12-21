package anja.geom;


import java.util.Vector;

import anja.util.AVLTree;


/**
 * Diese Klasse verwaltet ein Segment und dessen Vorgaenger, Nachfolger und
 * ausserdem noch einige weitere Statusinformationen.
 */
class SegmentEntry
		implements java.io.Serializable
{

	public Segment2		value;
	public SegmentEntry	prev;						// Vorgaenger
	public SegmentEntry	next;						// Nachfolger
	public boolean		leftIsInnerArea	= true;	// gehoert die linke Seite des Segments zum Inneren des Polygons?
	public boolean		isIncoming		= false;	// temporaere Variable: ist das Segment zu einem bestimmten Punkt eingehend oder ausgehend?
	public boolean		isConnected		= false;	// Status-Wert fuer ein Update der Verbindungen an einem Intersection-Point
	public PointEntry	srcPoint		= null;	// zum Abspeichern des Source-Punkts des Segments mit den zusaetzlichen Informationen von PointEntry.
	public Vector		intersectEvents;			// speichert die Schnittereignisse auf einem Segment.


	/**
	 * Konstruktor. Erhält das eigentliche Segment als Parameter
	 * 
	 * @param val
	 *            Das Segment
	 */
	SegmentEntry(
			Segment2 val)
	{
		value = val;
		intersectEvents = new Vector();
	}


	/**
	 * Kopiert das Objekt
	 * 
	 * @return Die Kopie
	 */
	SegmentEntry getClone()
	{
		SegmentEntry clonedSeg = new SegmentEntry(new Segment2(value.source(),
				value.target()));
		clonedSeg.prev = prev;
		clonedSeg.next = next;
		clonedSeg.leftIsInnerArea = leftIsInnerArea;
		clonedSeg.srcPoint = srcPoint;
		return clonedSeg;
	}
} // end SegmentEntry

/**
 * Diese Klasse verwaltet einen Punkt zusammen mit dem vorhergehenden Segment
 * und dem nachfolgenden Segment. Ausserdem werden noch verschiedene
 * Statusinformationen gespeichert.
 */
class PointEntry
		implements Comparable<PointEntry>, java.io.Serializable
{

	public Point2		value;					// der eigentliche Punkt.
	public SegmentEntry	nextSeg;				// das vorhergehende Segment
	public SegmentEntry	prevSeg;				// das nachfolgende Segment
	public boolean		isIntersectionPoint;	// ist der Punkt ein Schnittpunkt?
	public boolean		isUsed	= false;		// temporaere Variable.


	/**
	 * Konstruktor. Erhält den eigentlichen Punkt als Parameter
	 * 
	 * @param val
	 *            Der Punkt
	 */
	PointEntry(
			Point2 val)
	{
		value = val;
		isIntersectionPoint = false;
	}


	/*
	 * Vergleichsfunktion
	 * 
	 * @param object Das Vergleichsobjekt
	 * 
	 * @return Vergleichswert 
	 */
	public int compareTo(
			PointEntry object)
	{
		return value.comparePointsLexX(object.value);
	}
} // end PointEntry

/**
 * Diese Klasse verwaltet zu einem Schnittpunkt die daran beteiligten Segmente
 * und Punkte. Der Schnitt kann aus einem einzelnen Punkt bestehen oder aus
 * einem (Teil-)Segment.
 */
class IntersectionEvent
		implements java.io.Serializable
{

	Point2			point;			// der Schnittpunkt
	Segment2		seg;			// das Schnittsegment
	SegmentEntry	actSeg;		// vorgehendes oder nachfolgendes Segment des Schnittes
	SegmentEntry	otherSeg;		// vorgehendes oder nachfolgendes Segment des Schnittes
	boolean			eventHandled;	// wurde dieses Ereignis schon behandelt?


	/**
	 * Konstruktor
	 * 
	 * @param iPoint
	 *            der Schnittpunkt
	 * @param iSeg
	 *            das Schnittsegment
	 * @param segAct
	 *            vorgehendes oder nachfolgendes Segment des Schnittes
	 * @param segOther
	 *            vorgehendes oder nachfolgendes Segment des Schnittes
	 */
	IntersectionEvent(
			Point2 iPoint,
			Segment2 iSeg,
			SegmentEntry segAct,
			SegmentEntry segOther)
	{
		point = iPoint;
		seg = iSeg;
		actSeg = segAct;
		otherSeg = segOther;
		eventHandled = false;
	}
}

/**
 * Diese Klasse berechnet die Vereinigung von 2 Polygonen, die auch Loecher
 * haben duerfen.
 * 
 * @author Marina Bachran
 * 
 * @see HolePolygon2
 */
public class PolygonUnion
		implements java.io.Serializable
{

	private HolePolygon2		p1;							// die Polygone p1 und p2 sollen vereinigt werden
	private HolePolygon2		p2;							// die Polygone p1 und p2 sollen vereinigt werden
	private HolePolygon2		result;						// die Vereinigung der Polygone p1 und p2

	private int					segmentCount		= 0;		// # Segmente, aus denen p1 und p2 bestehen
	private SegmentEntry[]		segs;							// enthaelt alle Segmente aus p1 und p2
	private PointEntry[]		points;						// enthaelt alle Punkte aus p1 und p2 und spaeter auch noch die Schnittpunkte
	private AVLTree<PointEntry>	avl;							// dient zum Aufbau der Abarbeitungsreihenfolge der Punkte der Polygone
	private Vector				activeSegs;					// die aktiven Segmente nach Abarbeitung des i-ten Punktes
	private Vector				intersectionEvents;			// Schnittereignisse
	private boolean				intersectionOccured	= false;	// ist ein Schnitt zwischen den Polygonen aufgetreten?


	/**
	 * Erstellt eine neue Vereinigung zweier Polygone
	 * 
	 * @param poly1
	 *            und
	 * @param poly2
	 *            die beiden Polygone die vereinigt werden sollen.
	 */
	public PolygonUnion(
			Polygon2 poly1,
			Polygon2 poly2)
	{
		p1 = new HolePolygon2(poly1);
		p2 = new HolePolygon2(poly2);
		makeUnion();
	}


	/**
	 * Erstellt eine neue Vereinigung zweier Polygone
	 * 
	 * @param poly1
	 *            Polygon 1
	 * @param poly2
	 *            Polygon 2
	 */
	public PolygonUnion(
			HolePolygon2 poly1,
			HolePolygon2 poly2)
	{
		p1 = new HolePolygon2(poly1);
		p2 = new HolePolygon2(poly2);
		makeUnion();
	}


	/**
	 * Liefert die Vereinigung der Polygone zurueck.
	 * 
	 * @return die Vereinigung der Polygone
	 */
	public HolePolygon2 getUnion()
	{
		return result;
	}


	/* ******************/
	/* PRIVATE METHODEN */
	/* ******************/

	/**
	 * Berechnet die Vereinigung von p1 und p2.
	 */
	private void makeUnion()
	{
		result = null;
		intersectionOccured = false;

		// Setze Orientierungen des Polygon-Randes und der Loecher
		p1.setOrientation(HolePolygon2.ORIENTATION_RIGHT);
		p1.setHoleOrientation(HolePolygon2.ORIENTATION_LEFT);
		p2.setOrientation(HolePolygon2.ORIENTATION_RIGHT);
		p2.setHoleOrientation(HolePolygon2.ORIENTATION_LEFT);

		segmentCount = p1.getEdgeCount() + p2.getEdgeCount();

		segs = new SegmentEntry[segmentCount];
		points = new PointEntry[segmentCount];
		int i;
		int segCounter = 0;

		// Durchlaufe alle Polygone und speichere diese in den zugehoerigen Arrays
		segCounter += getSegmentAndPointEntries((Polygon2) p1, segCounter);
		for (i = 0; i < p1.getHoleCount(); i++)
			segCounter += getSegmentAndPointEntries(p1.getHole(i), segCounter);
		segCounter += getSegmentAndPointEntries((Polygon2) p2, segCounter);
		for (i = 0; i < p2.getHoleCount(); i++)
			segCounter += getSegmentAndPointEntries(p2.getHole(i), segCounter);

		avl = new AVLTree<PointEntry>();
		// Die Punkte werden in einen AVL-Baum gelegt
		for (i = 0; i < segCounter; i++)
			avl.add(points[i]);
		// und sortiert ausgelesen
		PointEntry[] tempPoints = avl.getTreeEntries();
		for (i = 0; i < segCounter; i++)
			points[i] = tempPoints[i];

		// Gehe alle Punkte von links nach rechts durch und generiere Intersection-Events.
		activeSegs = new Vector();
		intersectionEvents = new Vector();
		for (i = 0; i < segCounter; i++)
		{
			checkForIntersection(points[i].nextSeg);
			checkForIntersection(points[i].prevSeg);
		}

		// Wenn kein Schnitt existiert, kann evtl. das eine Polygon in dem anderen liegen
		if (!intersectionOccured)
		{
			// Hier wird ueberprueft, ob das eine Polygon in dem anderen liegt
			HolePolygon2 outer = null;
			HolePolygon2 inner = null;
			if (p2.inside(p1.firstPoint()))
			{
				outer = p2;
				inner = p1;
			}
			else if (p1.inside(p2.firstPoint()))
			{
				outer = p1;
				inner = p2;
			}
			if (outer == null)
				return;

		}

		// Teile die Segmente mit Intersection-Points auf
		for (i = 0; i < intersectionEvents.size(); i++)
		{
			IntersectionEvent iEvent = (IntersectionEvent) intersectionEvents
					.get(i);
			if (iEvent.point != null)
				insertIntersectionPoint(iEvent);
			else if (iEvent.seg != null)
				insertIntersectionSegment(iEvent);
			iEvent.eventHandled = true;
		}

		// Aktualisiere die Verbindungen und Nachbargebiete der Segmente
		for (i = 0; i < intersectionEvents.size(); i++)
		{
			IntersectionEvent iEvent = (IntersectionEvent) intersectionEvents
					.get(i);
			if (iEvent.point != null)
				updateAtIntersectionPoint(iEvent);
			else if (iEvent.seg != null)
				updateAtIntersectionSegment(iEvent);
		}

		// Lese den AVL-Baum erneut sortiert aus - diesmal mit Intersection-Points
		tempPoints = avl.getTreeEntries();
		points = new PointEntry[tempPoints.length];
		for (i = 0; i < tempPoints.length; i++)
			points[i] = (PointEntry) tempPoints[i];

		// Lese das auessere Polygon aus
		SegmentEntry actSeg;
		// Wenn der linkeste unterste Punkt Schnittpunkt ist, so muss der richtige Eintrag gefunden werden
		Point2 firstPoint = points[0].value;

		Vector tempHoles = new Vector();
		boolean isOuterPolygon = true;
		i = 0;
		while (firstPoint.comparePointsLexX(points[i].value) == 0)
		{
			result = new HolePolygon2();
			isOuterPolygon = true;
			if (!points[i].nextSeg.leftIsInnerArea)
			{
				actSeg = points[i].nextSeg;
				while (!actSeg.srcPoint.isUsed)
				{
					result.addPoint(actSeg.srcPoint.value);
					actSeg.srcPoint.isUsed = true;
					actSeg = actSeg.next;
					if (actSeg.leftIsInnerArea)
						isOuterPolygon = false; // Bin in einen inneren Bereich gelaufen -> bearbeite Zyklus dennoch bis zum Ende
				}
				if (!isOuterPolygon)
				{
					result = null;
				}
				else if (result.getOrientation() == Polygon2.ORIENTATION_RIGHT)
				{
					break; // Aeusseres Polygon gefunden
				}
				else
				{ // vorzeitig ein Loch gefunden -> fuer spaeter merken
					tempHoles.add(result);
					result = null;
				}
			}
			i++;
		}
		if (!isOuterPolygon)
		{
			result = null;
			return;
		} // Irgendwas ist schief gelaufen -> Exception kann hier neu erstellt werden

		// vorzeitig gefundene Loecher abspeichern
		for (i = 0; i < tempHoles.size(); i++)
		{
			Polygon2 hole = (HolePolygon2) tempHoles.get(i);
			if (result == null)
			{
				System.out.println("result null");
				return;
			}
			if (!result.existHole(hole))
				result.addHole(hole);
		}

		// Lese die Loecher aus
		for (i = 0; i < points.length; i++)
		{
			if (!points[i].isUsed)
			{
				// evtl. neues Loch
				Polygon2 hole = new Polygon2();
				boolean isHole = true;
				actSeg = points[i].nextSeg;
				while (!actSeg.srcPoint.isUsed)
				{
					hole.addPoint(actSeg.srcPoint.value);
					actSeg.srcPoint.isUsed = true;
					actSeg = actSeg.next;
					if (actSeg.leftIsInnerArea)
						isHole = false; // Kein Loch!
				}
				if ((isHole && (hole.getOrientation() == Polygon2.ORIENTATION_LEFT))
						&& (!(p1.inside(hole.firstPoint()) || p2.inside(hole
								.firstPoint()))) && (!result.existHole(hole)))
				{
					result.addHole(hole);
				}
			}
		}
		result.removeRedundantPoints();
	}


	/**
	 * Prueft nach, ob der Punkt p Teil eines Schnittereignisses ist.
	 * 
	 * @param p
	 *            Punkt p
	 * 
	 * @return true Teil eines Schnittereignisses, false sonst
	 */
	private boolean existIntersectionEvent(
			Point2 p)
	{
		for (int i = 0; i < intersectionEvents.size(); i++)
		{
			IntersectionEvent event = (IntersectionEvent) intersectionEvents
					.get(i);
			if (event.point == null)
			{
				if ((p.comparePointsLexX(event.seg.source()) == 0)
						|| (p.comparePointsLexX(event.seg.target()) == 0))
					return true;
			}
			else
			{
				if (p.comparePointsLexX(event.point) == 0)
					return true;
			}
		}
		return false;
	}


	/**
	 * Berechnet die SegmentEntries und die PointEntries zu dem Polygon2 poly.
	 * Die Eintraege werden in den Arrays points[] und segs[] ab dem Index
	 * baseIndex abgespeichert.
	 * 
	 * @param poly
	 *            Das Polygon
	 * @param baseIndex
	 *            Der Startindex
	 * 
	 * @return Zahl der Ergebnisse
	 */
	private int getSegmentAndPointEntries(
			Polygon2 poly,
			int baseIndex)
	{
		Segment2[] tempSegs = poly.edges();
		for (int j = 0; j < tempSegs.length; j++)
		{
			// Erzeuge Segment-Eintraege
			SegmentEntry segEntry = new SegmentEntry(tempSegs[j]);
			segEntry.leftIsInnerArea = false; // beim Rand und bei Loechern ist links kein Polygon!
			segs[baseIndex + j] = segEntry;
		}
		for (int j = 0; j < tempSegs.length; j++)
		{
			// Erzeuge Punkt-Eintraege und erzeuge die Referenzen zu den Segment-Eintraegen
			PointEntry pointEntry = new PointEntry(tempSegs[j].source());
			pointEntry.nextSeg = segs[baseIndex + j];
			pointEntry.prevSeg = segs[baseIndex
					+ ((j < 1) ? (tempSegs.length - 1) : (j - 1))];
			points[baseIndex + j] = pointEntry;
			segs[baseIndex + j].prev = segs[baseIndex
					+ ((j < 1) ? (tempSegs.length - 1) : (j - 1))];
			segs[baseIndex + j].next = segs[baseIndex
					+ ((j + 1 < tempSegs.length) ? (j + 1) : 0)];
			segs[baseIndex + j].srcPoint = pointEntry;
		}
		return tempSegs.length;
	}


	/**
	 * Spaltet ein Segment und setzt einen Intersection-Point dazwischen.
	 * 
	 * @param actual
	 *            Das Segment
	 * @param p
	 *            Der Punkt
	 * 
	 * @return Das Ergebnissegment
	 */
	private PointEntry splitSegmentEntry(
			SegmentEntry actual,
			Point2 p)
	{
		SegmentEntry newSeg = actual.getClone();
		newSeg.value.setSource(p);
		newSeg.prev = actual;
		actual.value.setTarget(p);
		actual.next = newSeg;
		newSeg.next.prev = newSeg;
		newSeg.next.srcPoint.prevSeg = newSeg;

		// Ueberpruefe, ob intersectEreignisse vorliegen, die ggf. aktualisiert werden muessen
		for (int i = actual.intersectEvents.size() - 1; i >= 0; i--)
		{
			IntersectionEvent ie = (IntersectionEvent) actual.intersectEvents
					.get(i);
			Point2 eventPoint;
			if (ie.point == null)
				eventPoint = ie.seg.source();
			else
				eventPoint = ie.point;
			if ((p.comparePointsLexX(eventPoint) != 0)
					&& (newSeg.value.liesOn(eventPoint)))
			{
				if ((newSeg.value.target().comparePointsLexX(eventPoint) != 0)
						|| (!ie.eventHandled))
				{
					if (ie.actSeg == actual)
					{
						ie.actSeg = newSeg;
					}
					else
					{
						ie.otherSeg = newSeg;
					}
				}
				newSeg.intersectEvents.add(ie);
				actual.intersectEvents.remove(i);
			}
		}

		PointEntry iPoint = new PointEntry(p);
		iPoint.nextSeg = newSeg;
		iPoint.prevSeg = actual;
		avl.add(iPoint);
		newSeg.srcPoint = iPoint;

		return iPoint;
	}


	/**
	 * Sucht und entfernt ein Schnittereignis.
	 * 
	 * @param p
	 *            Der Schnitt
	 */
	private void findAndRemoveIntersectionEvent(
			Point2 p)
	{
		for (int i = intersectionEvents.size() - 1; i >= 0; i--)
		{
			IntersectionEvent ie = (IntersectionEvent) intersectionEvents
					.get(i);
			if ((ie.point != null) && (ie.point.comparePointsLexX(p) == 0))
			{
				intersectionEvents.remove(i);
				break;
			}
		}
	}


	/**
	 * Ueberprueft auf eine Ueberschneidung der Segmente und fuegt bei diesen
	 * ggf. den Punkt in die Liste der Schnittpunkte ein.
	 * 
	 * @param actSeg
	 *            Das Eingabesegment
	 */
	private void checkForIntersection(
			SegmentEntry actSeg)
	{
		if (activeSegs.contains(actSeg))
		{ // Segment wird inaktiv
			activeSegs.remove(actSeg);
		}
		else
		{ // Segment wird aktiv
			// Teste auf Schnitte
			for (int j = 0; j < activeSegs.size(); j++)
			{
				SegmentEntry otherSeg = (SegmentEntry) activeSegs.get(j);
				Intersection intersect = new Intersection();
				actSeg.value.intersection(otherSeg.value, intersect);
				if (intersect.result == Intersection.SEGMENT2)
				{
					// Ueberpruefe, ob schon ein Punkt-Ereignis existiert - wenn ja, so entferne es
					findAndRemoveIntersectionEvent(intersect.segment2.source());
					findAndRemoveIntersectionEvent(intersect.segment2.target());
					IntersectionEvent ie = new IntersectionEvent(null,
							intersect.segment2, actSeg, otherSeg);
					intersectionEvents.add(ie);
					actSeg.intersectEvents.add(ie);
					otherSeg.intersectEvents.add(ie);
					intersectionOccured = true;
				}
				else if (intersect.result == Intersection.POINT2)
				{
					// Ueberpruefe, ob die Segmente aufeinander folgende Segmente sind und der berechnete Schnittpunkt diese verbindet
					if (((actSeg.next != otherSeg) && (otherSeg.next != actSeg))
							&& (!existIntersectionEvent(intersect.point2)))
					{
						IntersectionEvent ie = new IntersectionEvent(
								intersect.point2, null, actSeg, otherSeg);
						intersectionEvents.add(ie);
						actSeg.intersectEvents.add(ie);
						otherSeg.intersectEvents.add(ie);
						intersectionOccured = true;
					}
				}
			}
			activeSegs.add(actSeg);
		}
	}


	/**
	 * Fuegt ein Segment-Schnittereignis ein.
	 * 
	 * @param ie
	 *            Das Schnittereignis
	 */
	private void insertIntersectionSegment(
			IntersectionEvent ie)
	{
		// Fuege evtl. Punkte in die Segmente ein
		Point2 actSrc = ie.actSeg.value.source();
		Point2 actTrg = ie.actSeg.value.target();
		Point2 otherSrc = ie.otherSeg.value.source();
		Point2 otherTrg = ie.otherSeg.value.target();

		boolean actSegDirection = (actSrc.comparePointsLexX(actTrg) > 0);
		boolean otherSegDirection = (otherSrc.comparePointsLexX(otherTrg) > 0);
		boolean iSegDirection = (ie.seg.source().comparePointsLexX(
				ie.seg.target()) > 0);

		boolean targetFirst = (actSegDirection ^ iSegDirection);
		Point2 firstPoint = (targetFirst ? ie.seg.target() : ie.seg.source());
		Point2 secondPoint = (targetFirst ? ie.seg.source() : ie.seg.target());
		if (actSrc.comparePointsLexX(firstPoint) != 0) // split actSeg with firstPoint
			ie.actSeg = splitSegmentEntry(ie.actSeg, firstPoint).nextSeg;
		if (actTrg.comparePointsLexX(secondPoint) != 0) // split actSeg with secondPoint
			ie.actSeg = splitSegmentEntry(ie.actSeg, secondPoint).prevSeg;

		targetFirst = (otherSegDirection ^ iSegDirection);
		firstPoint = (targetFirst ? ie.seg.target() : ie.seg.source());
		secondPoint = (targetFirst ? ie.seg.source() : ie.seg.target());
		if (otherSrc.comparePointsLexX(firstPoint) != 0) // split otherSeg with firstPoint
			ie.otherSeg = splitSegmentEntry(ie.otherSeg, firstPoint).nextSeg;
		if (otherTrg.comparePointsLexX(secondPoint) != 0) // split otherSeg with secondPoint
			ie.otherSeg = splitSegmentEntry(ie.otherSeg, secondPoint).prevSeg;

		ie.actSeg.srcPoint.isIntersectionPoint = true;
		ie.actSeg.next.srcPoint.isIntersectionPoint = true;
		ie.otherSeg.srcPoint.isIntersectionPoint = true;
		ie.otherSeg.next.srcPoint.isIntersectionPoint = true;
	}


	/**
	 * Aktualisiert die Nachfolger und Vorgaenger der Segmente und zusaetzlich
	 * die Markierung der benachbarten Flaechen
	 * 
	 * @param ie
	 *            Das Schnittereignis
	 */
	private void updateAtIntersectionSegment(
			IntersectionEvent ie)
	{
		SegmentEntry actSeg = ie.actSeg;
		SegmentEntry otherSeg = ie.otherSeg;

		// Berechne eine zyklische Ordnung aller beteiligten Segmente
		actSeg.prev.isIncoming = true;
		actSeg.next.isIncoming = false;
		otherSeg.prev.isIncoming = true;
		otherSeg.next.isIncoming = false;

		actSeg.isConnected = false;
		actSeg.prev.isConnected = false;
		actSeg.next.isConnected = false;
		otherSeg.isConnected = false;
		otherSeg.prev.isConnected = false;
		otherSeg.next.isConnected = false;

		boolean sameDirection = (actSeg.srcPoint.value
				.comparePointsLexX(otherSeg.srcPoint.value) == 0);
		SegmentEntry[] cyclicOrder = new SegmentEntry[4];

		Point2 anglePointPrev;
		Point2 anglePointNext;
		boolean actSegFirst;
		if (sameDirection)
		{
			// beide prev => beide inComing
			SegmentEntry tempActSeg = actSeg;
			SegmentEntry tempOtherSeg = otherSeg;
			while (tempActSeg.prev.value.equals(tempOtherSeg.prev.value))
			{
				tempActSeg = tempActSeg.prev;
				tempOtherSeg = tempOtherSeg.prev;
				if (tempActSeg == actSeg)
					return; // Beide Zyklen gleich -> mache nichts
			}
			anglePointPrev = tempActSeg.value.source();
			anglePointNext = tempActSeg.value.target();
			actSegFirst = (anglePointPrev.angle(tempActSeg.prev.value.source(),
					anglePointNext) < anglePointPrev.angle(
					tempOtherSeg.prev.value.source(), anglePointNext));
			cyclicOrder[0] = (actSegFirst ? actSeg.prev : otherSeg.prev);
			cyclicOrder[1] = (actSegFirst ? otherSeg.prev : actSeg.prev);
			// beide next => beide Outgoing
			tempActSeg = actSeg;
			tempOtherSeg = otherSeg;
			while (tempActSeg.next.value.equals(tempOtherSeg.next.value))
			{
				tempActSeg = tempActSeg.next;
				tempOtherSeg = tempOtherSeg.next;
			}
			anglePointPrev = tempActSeg.value.source();
			anglePointNext = tempActSeg.value.target();
			actSegFirst = (anglePointNext.angle(tempActSeg.next.value.target(),
					anglePointPrev) < anglePointNext.angle(
					tempOtherSeg.next.value.target(), anglePointPrev));
			cyclicOrder[2] = (actSegFirst ? actSeg.next : otherSeg.next);
			cyclicOrder[3] = (actSegFirst ? otherSeg.next : actSeg.next);
		}
		else
		{
			// prev bei actSeg, next bei otherSeg
			SegmentEntry tempActSeg = actSeg;
			SegmentEntry tempOtherSeg = otherSeg;
			while (tempActSeg.prev.value.equals(new Segment2(
					tempOtherSeg.next.value.target(), tempOtherSeg.next.value
							.source())))
			{
				tempActSeg = tempActSeg.prev;
				tempOtherSeg = tempOtherSeg.next;
				if (tempActSeg == actSeg)
				{
					// Beide Zyklen gleich - aber andere Richtung -> Ein Aussenrand und ein Loch - Loch wird ueberdeckt!
					actSeg.leftIsInnerArea = true;
					otherSeg.leftIsInnerArea = true;
					return;
				}
			}
			anglePointPrev = tempActSeg.value.source();
			anglePointNext = tempActSeg.value.target();
			actSegFirst = (anglePointPrev.angle(tempActSeg.prev.value.source(),
					anglePointNext) < anglePointPrev.angle(
					tempOtherSeg.next.value.target(), anglePointNext));
			cyclicOrder[0] = (actSegFirst ? actSeg.prev : otherSeg.next);
			cyclicOrder[1] = (actSegFirst ? otherSeg.next : actSeg.prev);
			// next bei actSeg, prev bei otherSeg
			tempActSeg = actSeg;
			tempOtherSeg = otherSeg;
			while (tempActSeg.next.value.equals(new Segment2(
					tempOtherSeg.prev.value.target(), tempOtherSeg.prev.value
							.source())))
			{
				tempActSeg = tempActSeg.next;
				tempOtherSeg = tempOtherSeg.prev;
			}
			anglePointPrev = tempActSeg.value.source();
			anglePointNext = tempActSeg.value.target();
			actSegFirst = (anglePointNext.angle(tempActSeg.next.value.target(),
					anglePointPrev) < anglePointNext.angle(
					tempOtherSeg.prev.value.source(), anglePointPrev));
			cyclicOrder[2] = (actSegFirst ? actSeg.next : otherSeg.prev);
			cyclicOrder[3] = (actSegFirst ? otherSeg.prev : actSeg.next);
		}
		// Markierung der benachbarten Flaechen aktualisieren
		SegmentEntry prev;
		SegmentEntry actual = cyclicOrder[2];
		SegmentEntry next = cyclicOrder[3];
		for (int pos = 0; pos < 4; pos++)
		{
			prev = actual;
			actual = next;
			next = cyclicOrder[pos];
			if (actual.isIncoming)
			{ // eingehend -> ueberpruefung der linken Seite mit next
				actual.leftIsInnerArea = (actual.value.equals(next.value)) ? false
						: next.isIncoming;
			}
			else
			{ // ausgehend -> ueberpruefung der linken Seite mit prev
				actual.leftIsInnerArea = (actual.value.equals(prev.value)) ? false
						: !prev.isIncoming;
			}
		}

		// Aktualisierung der Bereiche der mittleren Segmente
		actSeg.leftIsInnerArea = (cyclicOrder[1].isIncoming) ? cyclicOrder[1].leftIsInnerArea
				: true;
		if (sameDirection)
			otherSeg.leftIsInnerArea = actSeg.leftIsInnerArea;
		else
			otherSeg.leftIsInnerArea = (cyclicOrder[0].isIncoming) ? true
					: cyclicOrder[0].leftIsInnerArea;

		// Linien-Verknuepfung aktualisieren
		int connections = 0;
		SegmentEntry last = cyclicOrder[3];
		actual = cyclicOrder[0];
		int pos = 0;
		while (connections < 2)
		{
			while (last.isConnected || !(last.isIncoming))
			{ // Finde eingehendes Segment als last
				pos = (pos + 1) % 4;
				last = actual;
				actual = cyclicOrder[pos];
			}
			while (actual.isConnected || actual.isIncoming)
			{ // Finde jetzt ein paar von eingehendem Segment last und direktem Nachfolger actual als ausgehendes Segment
				pos = (pos + 1) % 4;
				if (!actual.isConnected)
					last = actual;
				actual = cyclicOrder[pos];
			}
			// Verbinde: last-->actual
			if (last.value.target().comparePointsLexX(actual.value.source()) == 0)
			{
				// Zwischensegmente werden nicht genutzt (Segmente von zwei unterschiedlichen Polygonen)
				last.next = actual;
				actual.prev = last;
				actual.srcPoint.nextSeg = actual;
				actual.srcPoint.prevSeg = last;
			}
			else
			{
				// Zwischensegmente werden genutzt
				if (!actSeg.isConnected)
				{
					last.next = actSeg;
					actSeg.prev = last;
					actSeg.next = actual;
					actual.prev = actSeg;
					actual.srcPoint.nextSeg = actual;
					actual.srcPoint.prevSeg = actSeg;
					actSeg.srcPoint.nextSeg = actSeg;
					actSeg.srcPoint.prevSeg = last;
					actSeg.isConnected = true;
				}
				else
				{
					last.next = otherSeg;
					otherSeg.prev = last;
					otherSeg.next = actual;
					actual.prev = otherSeg;
					actual.srcPoint.nextSeg = actual;
					actual.srcPoint.prevSeg = otherSeg;
					otherSeg.srcPoint.nextSeg = otherSeg;
					otherSeg.srcPoint.prevSeg = last;
					otherSeg.isConnected = true;
				}
			}
			last.isConnected = true;
			actual.isConnected = true;
			connections++;
		}
		if (!actSeg.isConnected)
		{ // Baue kleinen Zyklus fuer nicht verwendete Zwischensegmente
			actSeg.next = otherSeg;
			actSeg.prev = otherSeg;
			actSeg.srcPoint.nextSeg = otherSeg;
			actSeg.srcPoint.prevSeg = otherSeg;
			otherSeg.next = actSeg;
			otherSeg.prev = actSeg;
			otherSeg.srcPoint.nextSeg = actSeg;
			otherSeg.srcPoint.prevSeg = actSeg;
		}
	}


	/**
	 * Fuegt einen Schnittpunkt ein.
	 * 
	 * @param ie
	 *            Das Schnittereignis
	 */
	private void insertIntersectionPoint(
			IntersectionEvent ie)
	{
		Point2 p = ie.point;
		SegmentEntry actSeg = ie.actSeg;
		SegmentEntry otherSeg = ie.otherSeg;

		PointEntry[] ip = new PointEntry[2]; // Intersection-Points
		// Bestimme die Orientierung der Endpunkte zu dem jeweiligen anderen Segment
		int actSourceOrientation = otherSeg.value.orientation(actSeg.value
				.source());
		int actTargetOrientation = otherSeg.value.orientation(actSeg.value
				.target());
		int otherSourceOrientation = actSeg.value.orientation(otherSeg.value
				.source());
		int otherTargetOrientation = actSeg.value.orientation(otherSeg.value
				.target());

		if ((actTargetOrientation == Point2.ORIENTATION_COLLINEAR)
				&& (otherTargetOrientation == Point2.ORIENTATION_COLLINEAR))
		{
			// Schnittpunkt ist gemeinsamer Endpunkt beider Segmente
			ip[0] = actSeg.next.srcPoint;
			ip[1] = otherSeg.next.srcPoint;
			if (ip[0].isIntersectionPoint || ip[1].isIntersectionPoint)
				return;
		}
		else if ((actSourceOrientation == Point2.ORIENTATION_COLLINEAR)
				&& (otherSourceOrientation == Point2.ORIENTATION_COLLINEAR))
		{
			// Schnittpunkt ist gemeinsamer Anfangspunkt beider Segmente
			ip[0] = actSeg.srcPoint;
			ip[1] = otherSeg.srcPoint;
			if (ip[0].isIntersectionPoint || ip[1].isIntersectionPoint)
				return;
		}
		else if ((actSourceOrientation == Point2.ORIENTATION_COLLINEAR)
				&& (otherTargetOrientation == Point2.ORIENTATION_COLLINEAR))
		{
			// Schnittpunkt ist Anfangs-Punkt des einen Segmentes und Endpunkt des anderen
			ip[0] = actSeg.srcPoint;
			ip[1] = otherSeg.next.srcPoint;
			if (ip[0].isIntersectionPoint || ip[1].isIntersectionPoint)
				return;
		}
		else if ((actTargetOrientation == Point2.ORIENTATION_COLLINEAR)
				&& (otherSourceOrientation == Point2.ORIENTATION_COLLINEAR))
		{
			// Schnittpunkt ist Anfangs-Punkt des einen Segmentes und Endpunkt des anderen
			ip[0] = actSeg.next.srcPoint;
			ip[1] = otherSeg.srcPoint;
			if (ip[0].isIntersectionPoint || ip[1].isIntersectionPoint)
				return;
		}
		else if (actSourceOrientation == Point2.ORIENTATION_COLLINEAR)
		{ // Schnittpunkt ist Anfangspunkt von actSeg
			ip[0] = actSeg.srcPoint;
			if (ip[0].isIntersectionPoint)
				return;
			ip[1] = splitSegmentEntry(otherSeg, p);
		}
		else if (actTargetOrientation == Point2.ORIENTATION_COLLINEAR)
		{ // Schnittpunkt ist Endpunkt von actSeg
			ip[0] = actSeg.next.srcPoint;
			if (ip[0].isIntersectionPoint)
				return;
			ip[1] = splitSegmentEntry(otherSeg, p);
		}
		else if (otherSourceOrientation == Point2.ORIENTATION_COLLINEAR)
		{ // Schnittpunkt ist Anfangspunkt von otherSeg
			ip[1] = otherSeg.srcPoint;
			if (ip[1].isIntersectionPoint)
				return;
			ip[0] = splitSegmentEntry(actSeg, p);
		}
		else if (otherTargetOrientation == Point2.ORIENTATION_COLLINEAR)
		{ // Schnittpunkt ist Endpunkt von otherSeg
			ip[1] = otherSeg.next.srcPoint;
			if (ip[1].isIntersectionPoint)
				return;
			ip[0] = splitSegmentEntry(actSeg, p);
		}
		else
		{
			// Kein Segment-Endpunkt liegt dem anderen Segment
			ip[0] = splitSegmentEntry(actSeg, p);
			ip[1] = splitSegmentEntry(otherSeg, p);
		}
		ie.actSeg = ip[0].nextSeg;
		ie.otherSeg = ip[1].nextSeg;
	}


	/**
	 * Aktualisiert die Nachfolger und Vorgaenger der Segmente und zusaetzlich
	 * die Markierung der benachbarten Flaechen
	 * 
	 * @param ie
	 *            Das Schnittereignis
	 */
	private void updateAtIntersectionPoint(
			IntersectionEvent ie)
	{
		PointEntry[] p = new PointEntry[2];
		p[0] = ie.actSeg.srcPoint;
		p[1] = ie.otherSeg.srcPoint;
		int pos = 0;
		// Berechne eine zyklische Ordnung aller beteiligten Segmente
		Vector cyclicOrder = new Vector();
		Vector cyclicOrderPoints = new Vector();
		p[0].prevSeg.isIncoming = true;
		p[0].nextSeg.isIncoming = false;
		p[0].prevSeg.isConnected = false;
		p[0].nextSeg.isConnected = false;

		cyclicOrder.add(0, p[0].prevSeg);
		cyclicOrderPoints.add(0, p[0].prevSeg.value.source());
		cyclicOrder.add(0, p[0].nextSeg);
		cyclicOrderPoints.add(0, p[0].nextSeg.value.target());
		for (int i = 1; i < p.length; i++)
		{
			p[i].isIntersectionPoint = true;
			p[i].prevSeg.isIncoming = true;
			p[i].nextSeg.isIncoming = false;
			p[i].prevSeg.isConnected = false;
			p[i].nextSeg.isConnected = false;
			while (true)
			{
				double angle = p[i].value.angle(
						((Point2) cyclicOrderPoints.get(pos)),
						p[i].prevSeg.value.source());
				pos = (pos + 1) % cyclicOrder.size();
				double nextangle = p[i].value.angle(
						((Point2) cyclicOrderPoints.get(pos)),
						p[i].prevSeg.value.source());
				// Winkel zu pos ist groesser als zu pos+1 => bin zyklisch hinter pos, aber nicht hinter pos+1
				if (angle > nextangle)
					break;
			}
			cyclicOrder.add(pos, p[i].prevSeg);
			cyclicOrderPoints.add(pos, p[i].prevSeg.value.source());
			while (true)
			{
				double angle = p[i].value.angle(
						((Point2) cyclicOrderPoints.get(pos)),
						p[i].nextSeg.value.target());
				pos = (pos + 1) % cyclicOrder.size();
				double nextangle = p[i].value.angle(
						((Point2) cyclicOrderPoints.get(pos)),
						p[i].nextSeg.value.target());
				// Winkel zu pos ist groesser als zu pos+1 => bin zyklisch hinter pos, aber nicht hinter pos+1
				if (angle > nextangle)
					break;
			}
			cyclicOrder.add(pos, p[i].nextSeg);
			cyclicOrderPoints.add(pos, p[i].nextSeg.value.target());
		}
		// Linien-Verknuepfung aktualisieren
		int connections = 0;
		SegmentEntry last = (SegmentEntry) cyclicOrder.get(pos);
		pos = (pos + 1) % cyclicOrder.size();
		SegmentEntry actual = (SegmentEntry) cyclicOrder.get(pos);
		while (connections < p.length)
		{ // es muessen p.length Verknuepfungen vorgenommen werden.
			while (last.isConnected || !(last.isIncoming))
			{ // Finde eingehendes Segment als last
				pos = (pos + 1) % cyclicOrder.size();
				last = actual;
				actual = (SegmentEntry) cyclicOrder.get(pos);
			}
			while (actual.isConnected || actual.isIncoming)
			{ // Finde jetzt ein Paar von eingehendem Segment last und direktem Nachfolger actual als ausgehendes Segment
				pos = (pos + 1) % cyclicOrder.size();
				if (!actual.isConnected)
					last = actual;
				actual = (SegmentEntry) cyclicOrder.get(pos);
			}
			// Verbinde
			last.next = actual;
			actual.prev = last;
			actual.srcPoint.nextSeg = actual;
			actual.srcPoint.prevSeg = last;
			last.isConnected = true;
			actual.isConnected = true;
			connections++;
		}
		// ??? nicht ganz fehlerfrei!
		// Markierung der benachbarten Flaechen aktualisieren
		SegmentEntry prev;
		actual = (SegmentEntry) cyclicOrder.get(cyclicOrder.size() - 2);
		SegmentEntry next = (SegmentEntry) cyclicOrder
				.get(cyclicOrder.size() - 1);
		for (pos = 0; pos < cyclicOrder.size(); pos++)
		{
			prev = actual;
			actual = next;
			next = (SegmentEntry) cyclicOrder.get(pos);
			if (actual.isIncoming)
			{ // eingehend
				actual.leftIsInnerArea = next.isIncoming;
			}
			else
			{ // ausgehend
				actual.leftIsInnerArea = !prev.isIncoming;
			}
		}

	}

}
