/* Anmerkung: zu diesem Projekt gehoerend die Dateien:
 * SeidelQueryStructure.java, SeidelTriangulation.java, SeidelNode.java
 */

package anja.geom.triangulation;


import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Stack;
import java.util.Vector;

import anja.geom.ConvexPolygon2;
import anja.geom.Point2;
import anja.geom.PointsAccess;
import anja.geom.Polygon2;
import anja.geom.Segment2;
import anja.util.GraphicsContext;


/**
 * Triangulierung eines Polygons nach 'Raimund Seidel: A Simple and Fast
 * Incremental Randomized Algorithm for Computing Trapezoidal Decompositions and
 * for Triangulating Polygons'.
 * 
 * @author Marina Bachran
 */
public class SeidelTriangulation
	extends Triangulation
{

	private SeidelQueryStructure	qs;
	private SeidelNode				startNode;
	private int						segCount;
	private Polygon2[]				innerTraps;
	private Segment2[]				diagonals;
	private Segment2[]				seidelDiagonals;
	private Polygon2[]				monotonePolys;
	private ConvexPolygon2[]		triangles;
	
	private GraphicsContext 		_gc_trapezoids;
	private static final Color		_LINE_COLOR	= Color.RED;


	/**
	 * Erzeugt ein neues Triangulationsobjekt mit dem dazugehörigen Polygon.
	 * 
	 * <br>Die Triangulation wird implizit berechnet
	 * 
	 * @param poly
	 *            Das zu triangulierende Polygon
	 */
	public SeidelTriangulation(
			Polygon2 poly)
	{
		Segment2[] segs = poly.edges();
		segCount = segs.length;
		qs = new SeidelQueryStructure(segs);
		startNode = qs.locateLowestPolyTrap();
		_initGraphicsContexts();
		traverseInnerTrapezoids();
		calculateTriangulation();
	} // SeidelTriangulation


	/**
	 * Berechnet eine Triangulierung von Y-monotonen Polygonen mit einem
	 * einfachen Greedy-Algorithmus.
	 */
	private void calculateTriangulation()
	{
		triangles = new ConvexPolygon2[segCount - 2]; // hier sollen alle Dreiecke nachher rein...
		diagonals = new Segment2[segCount - 3];
		int diagIndex = 0;
		while (diagIndex < seidelDiagonals.length)
		{
			diagonals[diagIndex] = seidelDiagonals[diagIndex];
			diagIndex++;
		}
		int triIndex = 0;
		Polygon2 monotone;
		ConvexPolygon2 newTriangle;
		PointsAccess pointsAccess;
		Point2[] pointArray = new Point2[3];

		for (int i = 0; i < monotonePolys.length; i++)
		{
			monotone = new Polygon2(monotonePolys[i]);
			int orientation = (monotone.getOrientation() == Polygon2.ORIENTATION_RIGHT) ? 1
					: -1;
			int polyLength = (monotone.edges()).length;
			pointsAccess = new PointsAccess(monotone);

			Point2 lowestPoint = pointsAccess.cyclicNextPoint();
			pointArray[0] = lowestPoint;
			pointArray[1] = pointsAccess.cyclicNextPoint();
			pointArray[2] = pointsAccess.cyclicNextPoint();
			int j = 0;
			while (j < polyLength - 3)
			{
				// von dem Polygon die Ohren abschneiden, bis es nur noch 3 Ecken hat, also ein Dreieck ist.
				if ((pointArray[1].angle(pointArray[0], pointArray[2]) * orientation) < (Math.PI * orientation))
				{
					// neues Dreieck erstellen...
					newTriangle = new ConvexPolygon2();
					newTriangle.addPoint(pointArray[0]);
					newTriangle.addPoint(pointArray[1]);
					newTriangle.addPoint(pointArray[2]);
					triangles[triIndex++] = newTriangle; // neues Dreieck in das Array tun.
					diagonals[diagIndex++] = new Segment2(pointArray[0],
							pointArray[2]);

					//Dreieck von Polygon abschneiden.
					pointsAccess.cyclicPrevPoint();
					pointsAccess.removePoint();
					if (pointArray[0].comparePointsLexY(lowestPoint) == 0)
					{
						pointArray[1] = pointsAccess.cyclicNextPoint();
					}
					else
					{
						pointsAccess.cyclicPrevPoint();
						pointArray[0] = pointsAccess.cyclicPrevPoint();
						pointArray[1] = pointsAccess.cyclicNextPoint();
					}
					pointArray[2] = pointsAccess.cyclicNextPoint();
					j++; // habe eine convexe Ecke abgeschnitten
				}
				else
				{ // war keine convexe Ecke, gehe zur naechsten Ecke...
					pointArray[0] = pointArray[1];
					pointArray[1] = pointArray[2];
					pointArray[2] = pointsAccess.cyclicNextPoint();
				}
			}
			triangles[triIndex++] = monotone.getConvexPolygon();
		}
	} // calculateTriangulation


	/**
	 * Durchlaeuft die inneren Trapezoide des Polygons und speichert sie als
	 * einzelne Polygone.
	 */
	private void traverseInnerTrapezoids()
	{
		int innerTrapsIndex = 0;
		Stack nodeStack = new Stack();
		Vector tempMonoPolys = new Vector();
		Vector tempDiags = new Vector();
		innerTraps = new Polygon2[segCount - 1];
		Polygon2 monotonePoly;
		SeidelNode actual;
		SeidelNode last;
		boolean moveLeft = false;
		boolean onlyUp = false;
		boolean onlyDown = false;

		// erstes Trapezoid-Polygon berechnen...
		innerTraps[innerTrapsIndex++] = startNode.getPolygon();
		nodeStack.push(startNode);

		while (!nodeStack.empty())
		{
			last = (SeidelNode) nodeStack.pop();
			actual = last;
			Polygon2 poly = new Polygon2();

			// schauen, ob ich nach rechts oder links unten gehen muss.
			Point2 upper = actual.getPoint(true);
			Point2 lower = actual.getPoint(false);
			Segment2 leftBorder = actual.getBorder(true);
			Segment2 rightBorder = actual.getBorder(false);
			boolean upperOnBorder = (leftBorder.liesOn(upper) || rightBorder
					.liesOn(upper));
			boolean lowerOnBorder = (leftBorder.liesOn(lower) || rightBorder
					.liesOn(lower));

			if (!(upperOnBorder || lowerOnBorder))
			{ // Fall 1: beide Punkte liegen nicht auf einem begrenzenden Segment...
				moveLeft = (!actual.getUsedNeighbor(true, true));
				onlyUp = onlyDown = false;
			}
			else if (!upperOnBorder && lowerOnBorder)
			{ // Fall 2a: der untere Punkt liegt auf einem begrenzenden Segment
				if ((leftBorder.liesOn(lower) && rightBorder.liesOn(lower)))
				{
					moveLeft = (!actual.getUsedNeighbor(true, true));
					onlyUp = true;
					onlyDown = false;
					if (moveLeft)
					{
						nodeStack.push(actual);
						Segment2 diagonal = new Segment2(upper, lower);
						tempDiags.add(diagonal);
					}
				}
				else
				{
					moveLeft = actual.getUsedNeighbor(true, false);
					onlyDown = false;
					onlyUp = actual.getBorder(moveLeft).liesOn(lower);
				}
			}
			else if (upperOnBorder && !lowerOnBorder)
			{ // Fall 2b: der obere Punkt liegt auf einem begrenzenden Segment
				moveLeft = actual.getUsedNeighbor(false, false);
				onlyDown = actual.getBorder(moveLeft).liesOn(upper);
				onlyUp = false;
			}
			else if ((leftBorder.liesOn(upper) && leftBorder.liesOn(lower))
					|| (rightBorder.liesOn(upper) && rightBorder.liesOn(lower)))
			{ // Fall 4: beide Punkte liegen auf ein und demselben begrenzenden Segment
				onlyDown = (actual.getNeighbor(true, true) == null);
				onlyUp = !onlyDown;
				moveLeft = onlyDown ? rightBorder.liesOn(lower) : rightBorder
						.liesOn(upper);
			}
			else
			{// (upperOnBorder && lowerOnBorder) // Fall 3: beide Punkte liegen auf einem begrenzenden Segment
				onlyDown = actual.getUsedNeighbor(true, true);
				onlyUp = !onlyDown;
				moveLeft = onlyDown ? (leftBorder.liesOn(upper)) : (rightBorder
						.liesOn(upper));
			}

			poly.insertFront(actual.getPoint(false));
			// zuerst nach unten laufen, solange ein Weg nach unten existiert.
			while (!onlyUp)
			{
				//  gehe nach unten und markiere den Weg als genutzt
				boolean temp = !actual.getTwoNeighbors(false) || moveLeft;
				actual.setUsedNeighbor(false, temp, true);
				actual = actual.getNeighbor(false, temp);
				temp = !actual.getTwoNeighbors(true) || moveLeft; // beeinhaltet den tatsaechlich gegangenen Weg
				actual.setUsedNeighbor(true, temp, true);

				innerTraps[innerTrapsIndex++] = actual.getPolygon();
				poly.insertFront(actual.getPoint(false));

				//  Ueberpruefe auf Diagonalen = true
				upper = actual.getPoint(true);
				lower = actual.getPoint(false);
				leftBorder = actual.getBorder(true);
				rightBorder = actual.getBorder(false);
				if (!((leftBorder.liesOn(upper) && leftBorder.liesOn(lower)) || (rightBorder
						.liesOn(upper) && rightBorder.liesOn(lower))))
				{ // Diagonale einfuegen
					Segment2 diagonal = new Segment2(upper, lower);
					tempDiags.add(diagonal);
					nodeStack.push(actual);
					// Ueberpruefung, ob die Diagonale den Weg nach unten blockiert...
					if ((!actual.getTwoNeighbors(false)) && // nach unten nur ein Weg => vielleicht Engpass
							((!actual.getTwoNeighbors(true)) || // oben auch nur ein Weg!
							(actual.getBorder(moveLeft).liesOn(lower)))) // unten zwei Wege, aber Diagonale blockiert dennoch
						break;
				}
				// weg nach unten existiert?
				if (actual.getNeighbor(false, true) == null)
					break;
			}

			actual = last;
			poly.addPoint(actual.getPoint(true));
			// jetzt nach oben laufen, solange ein Weg existiert.
			while (!onlyDown)
			{
				//  gehe nach oben und markiere den Weg als genutzt
				boolean temp = !actual.getTwoNeighbors(true) || moveLeft;
				actual.setUsedNeighbor(true, temp, true);
				actual = actual.getNeighbor(true, temp);
				temp = !actual.getTwoNeighbors(false) || moveLeft; // beeinhaltet den tatsaechlich gegangenen Weg
				actual.setUsedNeighbor(false, temp, true);

				innerTraps[innerTrapsIndex++] = actual.getPolygon();
				poly.addPoint(actual.getPoint(true));

				//  Ueberpruefe auf Diagonalen = true
				upper = actual.getPoint(true);
				lower = actual.getPoint(false);
				leftBorder = actual.getBorder(true);
				rightBorder = actual.getBorder(false);
				if (!((leftBorder.liesOn(upper) && leftBorder.liesOn(lower)) || (rightBorder
						.liesOn(upper) && rightBorder.liesOn(lower))))
				{ // Diagonale einfuegen
					Segment2 diagonal = new Segment2(upper, lower);
					tempDiags.add(diagonal);
					nodeStack.push(actual);
					// Ueberpruefung, ob die Diagonale den Weg nach oben blockiert...
					if ((!actual.getTwoNeighbors(true)) && // nach oben nur ein Weg => vielleicht Engpass
							((!actual.getTwoNeighbors(false)) || // unten auch nur ein Weg!
							(actual.getBorder(moveLeft).liesOn(upper)))) // oben zwei Wege, aber Diagonale blockiert dennoch
						break;
				}

				// Weg nach oben existiert?
				if (actual.getNeighbor(true, true) == null)
					break;
			}

			tempMonoPolys.add(poly);
		}
		// monotone Polygone aus Vector in Array...
		int tempSize = tempMonoPolys.size();
		monotonePolys = new Polygon2[tempSize];
		for (int i = 0; i < tempSize; i++)
			monotonePolys[i] = (Polygon2) tempMonoPolys.get(i);

		// Diagonalen aus Vector in Array...
		tempSize = tempDiags.size();
		seidelDiagonals = new Segment2[tempSize];
		for (int i = 0; i < tempSize; i++)
			seidelDiagonals[i] = (Segment2) tempDiags.get(i);
	} // traverseInnerTrapezoids


	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*          Zugriffsmethoden                                                */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	/**
	 * Liefert die inneren Trapezoide des Polygons in einem Polygon2-Array
	 * zurueck.
	 * 
	 * @return Polygon2[] enthaelt die Trapezoide
	 */
	public Polygon2[] getInnerTrapezoids()
	{
		return innerTraps;
	}


	/**
	 * Liefert die Diagonalen, die waehrend der Berechnung der Triangulierung
	 * nach Seidel in das Polygon eingefuegt werden, zurueck.
	 * 
	 * @return Polygon2[] enthaelt die Diagonalen
	 */
	public Segment2[] getSeidelDiagonals()
	{
		return seidelDiagonals;
	}


	/**
	 * Liefert die Diagonalen zurueck, die das Polygon in Dreiecke unterteilen.
	 * 
	 * @return Segment2[] enthaelt die Diagonalen
	 */

	public Segment2[] getDiagonals()
	{
		return diagonals;
	}


	/**
	 * Liefert die y-monotonen Teilpolygone des Polygons (Zwischenschritt
	 * waehrend der Triangulierung nach Seidel.
	 * 
	 * @return Polygon2[] enthaelt die y monotonen Teilpolygone
	 */
	public Polygon2[] getMonotonePolygons()
	{
		return monotonePolys;
	}


	/**
	 * Liefert die Dreiecke zurueck in die das Polygon zerlegt wurde.
	 * 
	 * @return ConvexPolygon2[] enthaelt die Dreiecke
	 */
	public ConvexPolygon2[] getTriangles()
	{
		return triangles;
	}


	@Override
	public void draw(
			Graphics2D g2d)
	{
		for (Segment2 s : diagonals)
			s.draw(g2d, _gc_trapezoids);		
	}
	
	
	
	/**
	 * Initialisiert die GraphicsContext-Objekte.
	 */
	private void _initGraphicsContexts()
	{

		_gc_trapezoids = new GraphicsContext();
		_gc_trapezoids.setForegroundColor(_LINE_COLOR);
		_gc_trapezoids.setFillStyle(0);

	} // _initGraphicsContexts

}
