package anja.geom;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import anja.geom.Intersection;
import anja.geom.Point2;
import anja.geom.PointComparitor;
import anja.geom.Polygon2;
import anja.geom.Polygon2Ext;
import anja.geom.Segment2;
import anja.util.Angle;
import anja.util.BinarySearchTree;
import anja.util.Comparitor;
import anja.util.Drawable;
import anja.util.GraphicsContext;
import anja.util.KeyValueHolder;
import anja.util.LimitedAngle;
import anja.util.List;
import anja.util.ListItem;
import anja.util.RedBlackTree;
import anja.util.SimpleList;
import anja.util.Sweep;
import anja.util.SweepEvent;
import anja.util.SweepEventComparitor;
import anja.util.SweepTreeItem;
import anja.util.TreeItem;


/**
 * Berechnet fuer ein Polygon2 "poly" und einen Kreis mit dem Durchmesser
 * "radius", die Minkowskisumme. Die berechnete Minkowskisumme besteht aus
 * Boegen und Segmenten
 * 
 * @author Darius Geiss
 * 
 */
public class MinkSum
		implements java.io.Serializable
{

	/**
	 * MinkSum has been initialized, but no calculation has been done so far.
	 */
	public final static int	NOT_INITIALIZED						= 900;

	/**
	 * MinkSum has been initialized, but no calculation has been done so far.
	 */
	public final static int	INITIALIZED							= 901;

	/**
	 * Something is wrong with the Polygon2. Possible reasons:<br> Polygon is
	 * not closed<br> Polygon is empty<br>
	 */
	public final static int	POLYGON_IS_NOT_OK					= 902;

	/**
	 * Radius is <= 0. Minkowskisum can´t be calculated. Use set_radius(float)
	 * to change the value.
	 */
	public final static int	RADIUS_IS_TO_SMALL					= 903;

	/**
	 * During computing the Minkowskisum an problem occurred at position
	 * get_ErrorPosition(). Please rotate the Polygon or change it at this
	 * position.
	 */
	public final static int	TROUBLE_AT_ERRORPOSITION			= 904;

	/**
	 * Minkowskisum has been calculated and everything seems to be fine.
	 */
	public final static int	MINKSUM_IS_OK						= 905;

	/**
	 * Minkowskisum and the triangulation went well. Use getters for Data.
	 */
	public final static int	MINKSUM_AND_TRIANGULATION_ARE_OK	= 906;

	private PointComparitor	pcompare							= new PointComparitor(
																		PointComparitor.X_ORDER);
	private Polygon2		_poly								= null;
	private Polygon2Ext		_minkpoly							= null;
	private float			_radius								= 0;
	private SimpleList		_intersections						= null;
	private SimpleList		_objectList							= null;
	private Point2			_errorPosition						= null;
	private int				_sweepStep							= 0;
	private List			_sweepStatus						= null;
	private List			_verticalSegs						= null;
	private SimpleList		_listOfTrapezoids					= null;
	private List			_listOfTriangles					= null;
	private int				_status								= NOT_INITIALIZED;


	/**
	 * Leere MinkSum-Klasse. Folgende Sachen muessen noch gesetzt werden bevor
	 * die Berechnungen gestartet werden können.<br> - Ausgangspolygon<br> -
	 * Kreisdurchmesser
	 * 
	 * @see MinkSum#set_poly(Polygon2)
	 * @see MinkSum#set_radius(float)
	 */
	public MinkSum()
	{}


	/**
	 * Initialisiert die Klasse mit einem Polygon und einem Kreisdurchmesser,
	 * aus denen die Minkowskisumme berechnet werden soll.
	 * 
	 * @param poly
	 *            Das Eingabepolygon
	 * @param radius
	 *            Der Radius
	 */
	public MinkSum(
			Polygon2 poly,
			float radius)
	{
		_poly = poly;
		_radius = radius;
		_status = INITIALIZED;
	}


	/**
	 * Berechnet fuer ein Polygon2 "poly" und einen Kreis mit dem Durchmesser
	 * "radius", die Minkowskisumme. Die Daten stehen dann ueber die
	 * get-Funktionen zur Verfügung.
	 * 
	 * @return {@link MinkSum#POLYGON_IS_NOT_OK},
	 *         {@link MinkSum#RADIUS_IS_TO_SMALL}, ...
	 */
	public int calcMinkSum()
	{
		reset();
		if (_poly != null && _poly.isClosed() && !_poly.empty())
		{
			if (_radius >= 0)
			{
				_poly.setOrientation(Polygon2.ORIENTATION_LEFT);
				Segment2[] edgesOfPoly = _poly.edges();

				// Parallele Kanten berechnen.
				Segment2Ext edgesOfMS[] = minkSumPart1(_poly, _radius);

				SimpleList arcsOfMS = new SimpleList();
				RedBlackTree eventTree = new RedBlackTree(pcompare, true);
				eventTree.setAllowDuplicates(true);

				// Schnittpunkte der aufeinander folgenden Segmente bestimmen und diese Korregieren
				// Boegen hinzufuegen
				// Alle Schnitte im eventTree sammeln
				minkSumPart2_V2(edgesOfPoly, edgesOfMS, eventTree, arcsOfMS,
						_radius);

				// Die parallelen Kanten und die die Boegen werden in eine gemeinsame Liste
				// eingefuegt
				_objectList = new SimpleList(edgesOfMS, 0, edgesOfMS.length);
				_objectList.addAll(arcsOfMS);

				// Sweep-Algorithmus zur berechnung der uebrigen Schnittpunkte 
				SegmentToCloseSweep segSweep = new SegmentToCloseSweep();
				segSweep.setIntersectionMode(SegmentToCloseSweep.ALL_INTERSECTIONS);
				segSweep.setOutputMode(SegmentToCloseSweep.POINTS_AND_SEGMENTS);
				if (_sweepStep > 0)
					_sweepStatus = segSweep.segmentIntersection(_objectList,
							_radius, eventTree, _sweepStep);
				_intersections = segSweep.segmentIntersection(_objectList,
						_radius, eventTree, 0);

				// Aus den ermittelten Objekten und Schnittpunkten wird die Minkowski-Summe
				// errechnet. Das Resultat wird in minkObjektList gespeichert.
				// Sollte es ein Problem bei der Berechnung geben, so wird der Problematische
				// Point2 zurueck gegeben.
				Object myMinkObjectList = minkSumPart3(_intersections
						.copyList());
				if (myMinkObjectList instanceof SimpleList)
				{
					_minkpoly = new Polygon2Ext();
					_minkpoly.addList((SimpleList) myMinkObjectList);
					_status = MINKSUM_IS_OK;
					return MINKSUM_IS_OK;
				}
				else if (myMinkObjectList instanceof Point2)
				{
					_errorPosition = (Point2) myMinkObjectList;
					_status = TROUBLE_AT_ERRORPOSITION;
					return TROUBLE_AT_ERRORPOSITION;
				}
			}
			else
				return RADIUS_IS_TO_SMALL;
		}
		_status = POLYGON_IS_NOT_OK;
		return POLYGON_IS_NOT_OK;
	}


	/**
	 * Diese Funktion trianguliert die Ebene, die sich ausserhalb der
	 * Minkowski-Summe befindent.<br>- Eventtree mit den Schnittpunkten erzeugen
	 * <br>- Phase I: Bestimmung der vertikalen Segmente (Sweep) <br> - Phase
	 * II: Erzeugen der Pseudo-Vierecke (linear jedes VS wird 2x
	 * betrachtet)<br>- Phase III: Erzeugen der Pseudo-Dreiecke (ebenfalls
	 * linear)
	 * 
	 * @return {@link MinkSum#MINKSUM_AND_TRIANGULATION_ARE_OK}, ...
	 */
	public int triangulatePlane()
	{
		if (_status == MINKSUM_IS_OK)
		{
			SimpleList objects = _minkpoly.get_objectList();

			SimpleList list = new SimpleList();
			Point2 firstP = null;
			Point2 lastP = null;
			for (ListItem lItem = objects.first(); lItem != null; lItem = lItem
					.next())
			{
				if (lItem.value() instanceof Arc2Ext)
				{
					Arc2ExtModified arc = new Arc2ExtModified(
							(Arc2Ext) ((Arc2Ext) lItem.value()).clone());
					arc.setLabel(((Arc2Ext) lItem.value()).getLabel());
					if (list.empty())
					{
						lastP = arc.source();
						firstP = arc.target();
					}
					else if (arc.source().equals(firstP))
					{
						arc.setRefTraget(lastP);
						arc.setRefSource(firstP);
					}
					else
					{
						arc.setRefTraget(lastP);
						lastP = arc.source();
					}
					list.add(arc);
				}
				else if (lItem.value() instanceof Segment2)
				{
					Segment2Ext seg = new Segment2Ext(
							(Segment2) ((Segment2) lItem.value()).clone());
					if (list.empty())
					{
						firstP = seg.source();
						lastP = seg.target();
					}
					else if (seg.target().equals(firstP))
					{
						seg.setSource(lastP);
						seg.setTarget(firstP);
					}
					else
					{
						seg.setSource(lastP);
						lastP = seg.target();
					}
					list.add(seg);
				}
			}

			RedBlackTree eventTree = new RedBlackTree();
			ListItem lastItem = null;
			for (ListItem k = list.first(); k != null; k = k.next())
			{
				if (k.value() instanceof Segment2Ext)
				{
					if (lastItem != null)
						addEvent(eventTree, lastItem.value(), k.value(),
								((Segment2Ext) k.value()).source());
					lastItem = k;
				}
				if (k.value() instanceof Arc2ExtModified)
				{
					if (lastItem != null)
						addEvent(eventTree, lastItem.value(), k.value(),
								((Arc2ExtModified) k.value()).target());
					lastItem = k;
				}
			}
			if (!list.empty())
			{
				// Phase I (verticate Segmente erzeugen)
				Point2 p = (list.firstValue() instanceof Arc2ExtModified) ? ((Arc2ExtModified) list
						.firstValue()).target() : ((Segment2Ext) list
						.firstValue()).source();
				addEvent(eventTree, list.lastValue(), list.firstValue(), p);
				PhaseI phase1 = new PhaseI();
				phase1.setIntersectionMode(PhaseI.ALL_INTERSECTIONS);
				phase1.setOutputMode(PhaseI.POINTS_AND_SEGMENTS);

				// Segmente, die die vertikalen Segmente nach oben und nach unten begrenzen
				double polyMaxX = _poly.maximumX() + 2 * _radius;
				double polyMinX = _poly.minimumX() - 2 * _radius;
				float polyMaxY = _poly.maximumY() + 2 * _radius;
				float polyMinY = _poly.minimumY() - 2 * _radius;
				Segment2Ext upperBorder = new Segment2Ext(new Point2(polyMinX,
						polyMaxY), new Point2(polyMaxX, polyMaxY));
				Segment2Ext lowerBorder = new Segment2Ext(new Point2(polyMinX,
						polyMinY), new Point2(polyMaxX, polyMinY));

				list.add(upperBorder);
				list.add(lowerBorder);
				// bestimmung der vertikalen Segmente
				_verticalSegs = phase1.segmentIntersection(list, eventTree);
				// Ende Phase I

				// Phase II (Pseudo-Vierecke erzeugen)
				_listOfTrapezoids = new SimpleList();
				for (ListItem k = _verticalSegs.first(); k != null; k = _verticalSegs
						.next(k))
				{
					VerticalSegment left = (VerticalSegment) k.value();
					if (left.getTopRight() != null)
					{
						PseudoTrapezoid2 pTrap2 = new PseudoTrapezoid2();
						left.setPseudoTrapRight(pTrap2);
						SimpleList itemList = new SimpleList();
						itemList.add(left);
						VerticalSegment next = left;
						while ((next.getObjectBelow() != null)
								&& (next.getBottomRight() == null))
						{
							next = (VerticalSegment) next.getObjectBelow();
							next.setPseudoTrapRight(pTrap2);
							itemList.add(next);
						}
						Object bottom = next.getBottomRight();
						if (bottom instanceof Segment2Ext)
							((Segment2Ext) bottom).add_PseudoTrapezoid(pTrap2);
						itemList.add(bottom);
						VerticalSegment rightB = null;
						boolean stop = false;
						if (bottom instanceof Arc2ExtModified)
						{
							if (((Arc2ExtModified) bottom).get_adjacentVS()
									.length() < 2)
								stop = true;
							rightB = (((Arc2ExtModified) bottom)
									.get_adjacentVS().firstValue().equals(next)) ? (VerticalSegment) ((Arc2ExtModified) bottom)
									.get_adjacentVS().lastValue()
									: (VerticalSegment) ((Arc2ExtModified) bottom)
											.get_adjacentVS().firstValue();
						}
						else if (bottom instanceof Segment2Ext)
						{
							if (((Segment2Ext) bottom).get_adjacentVS()
									.length() < 2)
								stop = true;
							rightB = (((Segment2Ext) bottom).get_adjacentVS()
									.firstValue().equals(next)) ? (VerticalSegment) ((Segment2Ext) bottom)
									.get_adjacentVS().lastValue()
									: (VerticalSegment) ((Segment2Ext) bottom)
											.get_adjacentVS().firstValue();
						}
						if (stop == false)
						{
							next = rightB;
							next.setPseudoTrapLeft(pTrap2);
							itemList.add(next);
							while ((next.getObjectAbove() != null)
									&& (next.getTopLeft() == null))
							{
								next = (VerticalSegment) next.getObjectAbove();
								next.setPseudoTrapLeft(pTrap2);
								itemList.add(next);
							}
							itemList.add(next.getTopLeft());
							if (next.getTopLeft() instanceof Segment2Ext)
								((Segment2Ext) next.getTopLeft())
										.add_PseudoTrapezoid(pTrap2);
							pTrap2.setPseudoTrapezoid(itemList);
							_listOfTrapezoids.add(pTrap2);
						}
						else
						{
							if ((left.getTopRight() != null)
									&& (next.getBottomRight() != null)
									&& (left.source().x < _poly.maximumX()))
							{
								// Dreieck
								itemList.add(left.getTopRight());
								if (left.getTopRight() instanceof Segment2Ext)
									((Segment2Ext) left.getTopRight())
											.add_PseudoTrapezoid(pTrap2);
								pTrap2.setPseudoTrapezoid(itemList);
								_listOfTrapezoids.add(pTrap2);
							}
						}
					}
					if ((left.getTopLeft() != null)
							&& (left.getPseudoTrapLeft() == null)
							&& (_poly.minimumX() < left.source().x))
					{
						PseudoTrapezoid2 pTrap2 = new PseudoTrapezoid2();
						left.setPseudoTrapLeft(pTrap2);
						SimpleList itemList = new SimpleList();
						itemList.add(left);
						VerticalSegment next = left;
						while ((next.getObjectBelow() != null)
								&& (next.getBottomLeft() == null))
						{
							next = (VerticalSegment) next.getObjectBelow();
							next.setPseudoTrapLeft(pTrap2);
							itemList.add(next);
						}
						Object bottom = next.getBottomLeft();
						if (bottom instanceof Segment2Ext)
							((Segment2Ext) bottom).add_PseudoTrapezoid(pTrap2);
						itemList.add(bottom);
						VerticalSegment leftB = null;
						boolean stop = false;
						if (bottom instanceof Arc2ExtModified)
						{
							if (((Arc2ExtModified) bottom).get_adjacentVS()
									.length() < 2)
								stop = true;
							leftB = (((Arc2ExtModified) bottom)
									.get_adjacentVS().firstValue().equals(next)) ? (VerticalSegment) ((Arc2ExtModified) bottom)
									.get_adjacentVS().lastValue()
									: (VerticalSegment) ((Arc2ExtModified) bottom)
											.get_adjacentVS().firstValue();
						}
						else if (bottom instanceof Segment2Ext)
						{
							if (((Segment2Ext) bottom).get_adjacentVS()
									.length() < 2)
								stop = true;
							leftB = (((Segment2Ext) bottom).get_adjacentVS()
									.firstValue().equals(next)) ? (VerticalSegment) ((Segment2Ext) bottom)
									.get_adjacentVS().lastValue()
									: (VerticalSegment) ((Segment2Ext) bottom)
											.get_adjacentVS().firstValue();
						}
						if (stop == false)
						{
							next = leftB;
							itemList.add(next);
							while ((next.getObjectAbove() != null)
									&& (next.getTopRight() == null))
							{
								next = (VerticalSegment) next.getObjectAbove();
								next.setPseudoTrapRight(pTrap2);
								itemList.add(next);
							}
							itemList.reverse();
							itemList.add(next.getTopRight());
							if (next.getTopLeft() instanceof Segment2Ext)
								((Segment2Ext) next.getTopLeft())
										.add_PseudoTrapezoid(pTrap2);
							pTrap2.setPseudoTrapezoid(itemList);
							left.setPseudoTrapLeft(pTrap2);
							_listOfTrapezoids.add(pTrap2);
						}
						else
						{
							if ((left.getTopLeft() != null)
									&& (next.getBottomLeft() != null)
									&& (left.source().x > _poly.minimumX()))
							{
								// Dreieck
								itemList.reverse();
								itemList.add(left.getTopLeft());
								if (left.getTopLeft() instanceof Segment2Ext)
									((Segment2Ext) left.getTopLeft())
											.add_PseudoTrapezoid(pTrap2);
								pTrap2.setPseudoTrapezoid(itemList);
								left.setPseudoTrapLeft(pTrap2);
								_listOfTrapezoids.add(pTrap2);
							}
						}
					}
				} // Ende Phase II

				// Phase III (Triangulation der Pseudo-Vierecke)
				List vsToDevide = new List();
				_listOfTriangles = new List();
				for (ListItem k = _listOfTrapezoids.first(); k != null; k = _listOfTrapezoids
						.next(k))
				{
					PseudoTrapezoid2 pTrap = (PseudoTrapezoid2) k.value();
					SimpleList l = pTrap.triangulation(vsToDevide);
					for (ListItem m = l.first(); m != null; m = l.next(m))
					{
						PseudoTriangle2 pTria = (PseudoTriangle2) m.value();
						_listOfTriangles.add(pTria.getCentreOfMass(), pTria);
					}
				}
				vsToDevide.sort(new PointComparitor(PointComparitor.X_ORDER));
				for (ListItem k = vsToDevide.first(); k != null; k = vsToDevide
						.next(k))
				{
					VerticalSegment[] vsArray = (VerticalSegment[]) k.value();
					VerticalSegment x = vsArray[0];
					SimpleList l = new SimpleList();
					if (x.getPseudoTriaLeft() != null)
					{
						l = ((PseudoTriangle2) x.getPseudoTriaLeft())
								.divideTriangle(x, vsArray[1], vsArray[2]);
						_listOfTriangles.remove(((PseudoTriangle2) x
								.getPseudoTriaLeft()).getCentreOfMass());
					}
					else if (x.getPseudoTriaRight() != null)
					{
						l = ((PseudoTriangle2) x.getPseudoTriaRight())
								.divideTriangle(x, vsArray[1], vsArray[2]);
						_listOfTriangles.remove(((PseudoTriangle2) x
								.getPseudoTriaRight()).getCentreOfMass());
					}
					if (l != null)
					{
						for (ListItem m = l.first(); m != null; m = l.next(m))
						{
							PseudoTriangle2 pTria = (PseudoTriangle2) m.value();
							_listOfTriangles
									.add(pTria.getCentreOfMass(), pTria);
						}
					}
				} // Ende Phase III
			}
			_status = MINKSUM_AND_TRIANGULATION_ARE_OK;
		}
		return _status;
	}


	/**
	 * Erster Teil der Berechnung der Minkowskisumme. Es werden zu allen Kanten
	 * des Polygons2 poly parallele Kanten berechnet mit dem Abstand radius.
	 * 
	 * @param poly
	 *            Das Eingabepolygon
	 * @param radius
	 *            Der Radius/Die Ausdehnung
	 * 
	 * @return Die berechneten Kanten
	 */
	private Segment2Ext[] minkSumPart1(
			Polygon2 poly,
			float radius)
	{
		if (poly != null)
		{
			poly.setOrientation(Polygon2.ORIENTATION_LEFT);
			Segment2[] edges = poly.edges();
			Segment2Ext[] edgesOfMS = new Segment2Ext[edges.length];
			float deltaX;
			float deltaY;
			float multi;

			// ermittelt zu jeder Kante eine parallele Kanten mit dem Abstand _radius.
			for (int i = 0; i < edges.length; i++)
			{
				deltaX = edges[(i + 1) % edges.length].source().x
						- edges[i].source().x;
				deltaY = edges[(i + 1) % edges.length].source().y
						- edges[i].source().y;
				multi = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

				Segment2Ext seg = new Segment2Ext(edges[i].source().x
						+ (radius * deltaY) / multi, edges[i].source().y
						- (radius * deltaX) / multi, edges[i].target().x
						+ (radius * deltaY) / multi, edges[i].target().y
						- (radius * deltaX) / multi);
				edgesOfMS[i] = seg;

				float diffY1 = (float) Math.sqrt(Math.pow(radius, 2)
						- Math.pow(
								Math.min(
										Math.abs(edges[i].source().x
												- seg.source().x), radius), 2));
				float diffY2 = (float) Math.sqrt(Math.pow(radius, 2)
						- Math.pow(
								Math.min(
										Math.abs(edges[i].target().x
												- seg.target().x), radius), 2));

				edgesOfMS[i].source().y = (diffY1 == 0) ? edges[i].source().y
						: (edges[i].source().y - (deltaX / Math.abs(deltaX))
								* diffY1);
				edgesOfMS[i].target().y = (diffY2 == 0) ? edges[i].target().y
						: (edges[i].target().y - (deltaX / Math.abs(deltaX))
								* diffY2);
			}
			return edgesOfMS;
		}
		return null;
	}


	/**
	 * Zweiter Teil der Berechnung der Minkowskisumme. Es wird berechnet ob
	 * zwischen zwei nacheinander- folgenden Segmenten ein, zwei bzw. kein
	 * Bo(e)gen eingefuegt werden muessen. Es wird hierbei zwischen zwei
	 * verschiedene Bogenarten unterschieden: A) Nach oben offen und B) Nach
	 * unten offen Alle bisdahin und waehrend des Verfahrens ermittelten
	 * Schnittpunkte werden im eventTree gespeichert. Der Schuessel ist der
	 * Schnittpunkt und value ist eine Liste mit den beteiligten Objekten.
	 * 
	 * @param edges
	 *            Segment2[] (Kanten des Polygons)
	 * @param edgesOfMS
	 *            Segment2Ext[] (Ergebniss aus minkSumPart1)
	 * @param eventTree
	 *            RedBlackTree (leerer Baum für die Schnittpunkte)
	 * @param arcsOfMS
	 *            SimpleList (leere List für die Boegen)
	 * @param radius
	 *            float (Radius der Boegen)
	 */
	private void minkSumPart2_V2(
			Segment2[] edges,
			Segment2Ext[] edgesOfMS,
			RedBlackTree eventTree,
			SimpleList arcsOfMS,
			float radius)
	{
		Point2 p1, centre, p3;
		for (int i = 0; i < (edgesOfMS.length); i++)
		{
			p1 = edges[i].source();
			centre = edges[i].target();
			p3 = edges[(i + 1) % edgesOfMS.length].target();
			double angle = centre.angle(p1, p3);
			if (angle > Angle.PI)
			{
				// ein bzw. zwei Boegen muessen erstellt werden
				Segment2Ext s1 = edgesOfMS[i];
				Segment2Ext s2 = edgesOfMS[(i + 1) % edgesOfMS.length];
				double angleS1 = centre.angle(s1.target());
				double angleS2 = centre.angle(s2.source());
				if (angleS2 < Angle.PI)
				{
					// 1) Bogen endet oberhalb von centre
					if (angleS1 < Angle.PI)
					{
						// 2) Bogen startet oberhalb von centre
						// Aus 1 und 2 => ein Bogen oberhalb von centre
						Arc2ExtModified arc1 = new Arc2ExtModified(centre,
								radius, angleS1, angleS2,
								Angle.ORIENTATION_LEFT, s1.target(),
								s2.source());
						arc1.setLabel("oben");
						arcsOfMS.add(arc1);
						SimpleList list = new SimpleList();
						list.add(arc1);
						list.add(s1);
						eventTree.add(new SweepTreeItem(2, s1.target(), list)); // Schnitt Bogen1 und Segment1
						list = new SimpleList();
						list.add(arc1);
						list.add(s2);
						eventTree.add(new SweepTreeItem(2, s2.source(), list)); // Schnitt Bogen1 und Segment2
					}
					else
					{
						// 2) Bogen startet unterhalb von centre
						// Aus 1 und 2 => zwei Boegen
						Point2 iP = new Point2(centre.getX() + radius,
								centre.getY());
						Arc2ExtModified arc1 = new Arc2ExtModified(centre,
								radius, 0.0, angleS2, Angle.ORIENTATION_LEFT,
								iP, s2.source());
						Arc2ExtModified arc2 = new Arc2ExtModified(centre,
								radius, angleS1, 0.0, Angle.ORIENTATION_LEFT,
								s1.target(), iP);
						arc1.setLabel("oben");
						arc2.setLabel("unten");

						arcsOfMS.add(arc1);
						SimpleList list = new SimpleList();
						list.add(arc1);
						list.add(arc2);
						eventTree.add(new SweepTreeItem(2, iP, list)); // Schnitt Bogen1 und Bogen

						list = new SimpleList();
						list.add(arc1);
						list.add(s2);
						eventTree.add(new SweepTreeItem(2, s2.source(), list)); // Schnitt Bogen1 und Segment2

						arcsOfMS.add(arc2);
						list = new SimpleList();
						list.add(arc2);
						list.add(s1);
						eventTree.add(new SweepTreeItem(2, s1.target(), list)); // Schnitt Bogen2 und Segment1
					}
				}
				else
				{
					// 1) Bogen endet unterhalb von centre
					if (angleS1 < Angle.PI)
					{
						// 2) Bogen startet oberhalb von centre
						// Aus 1 und 2 => zwei Boegen
						Point2 iP = new Point2(centre.getX() - radius,
								centre.getY());
						Arc2ExtModified arc1 = new Arc2ExtModified(centre,
								radius, Angle.PI, angleS2,
								Angle.ORIENTATION_LEFT, iP, s2.source());
						Arc2ExtModified arc2 = new Arc2ExtModified(centre,
								radius, angleS1, Angle.PI,
								Angle.ORIENTATION_LEFT, s1.target(), iP);
						arc1.setLabel("unten");
						arc2.setLabel("oben");

						arcsOfMS.add(arc1);
						SimpleList list = new SimpleList();
						list.add(arc1);
						list.add(arc2);
						eventTree.add(new SweepTreeItem(2, iP, list)); // Schnitt Bogen1 und Bogen

						list = new SimpleList();
						list.add(arc1);
						list.add(s2);
						eventTree.add(new SweepTreeItem(2, s2.source(), list)); // Schnitt Bogen1 und Segment2

						arcsOfMS.add(arc2);
						list = new SimpleList();
						list.add(arc2);
						list.add(s1);
						eventTree.add(new SweepTreeItem(2, s1.target(), list)); // Schnitt Bogen2 und Segment1
					}
					else
					{
						// 2) Bogen startet unterhalb von centre
						// Aus 1 und 2 => ein Bogen unterhalb von centre
						Arc2ExtModified arc1 = new Arc2ExtModified(centre,
								radius, angleS1, angleS2,
								Angle.ORIENTATION_LEFT, s1.target(),
								s2.source());
						arc1.setLabel("unten");

						arcsOfMS.add(arc1);
						SimpleList list = new SimpleList();
						list.add(arc1);
						list.add(s1);
						eventTree.add(new SweepTreeItem(2, s1.target(), list)); // Schnitt Bogen1 und Segment1
						list = new SimpleList();
						list.add(arc1);
						list.add(s2);
						eventTree.add(new SweepTreeItem(2, s2.source(), list)); // Schnitt Bogen1 und Segment2
					}
				}
			}
			else
			{
				// Es gibt keinen Bogen zwischen den Segmenten, aber die Segmente koennten sich schneiden.
				Segment2Ext s1 = edgesOfMS[i];
				Segment2Ext s2 = edgesOfMS[(i + 1) % edgesOfMS.length];
				Point2 p = null;
				if ((s1.source().equals(s2.source()))
						|| (s1.source().equals(s2.target())))
					p = s1.source();
				else if ((s1.target().equals(s2.source()))
						|| (s1.target().equals(s2.target())))
					p = s1.target();
				if (p == null)
				{
					Intersection inter = new Intersection();
					p = s1.intersection(s2, inter);
				}
				if (p != null)
				{
					edgesOfMS[i].setTarget(p);
					edgesOfMS[(i + 1) % edgesOfMS.length].setSource(p);
					SimpleList list = new SimpleList();
					list.add(s1);
					list.add(s2);
					eventTree.add(new SweepTreeItem(2, p, list));
				}
			}
		}
	}


	/**
	 * Dritter und letzter Teil zur berechnung der Minkowski-Summe. In
	 * "intersections" befinden sich alle ermittelten Schnittpunkte samt der
	 * beteiligten Objekte. Die Liste ist aufsteigend nach den X-Koordinaten der
	 * Schnittpunkte sortiert. Algorithmus:<br><br>Anfang: Nehme den ersten
	 * Schnittpunkt und definieren diesen als Start und Endpunkt, da dieser auf
	 * jeden Fall zu der Minkowski-Summe gehoert. Von den an dem Schnitt
	 * beteiligten Objekten wird eines zum aktuellen Objekt bestimmt und das
	 * andere zum letzten Objekt.<br>
	 * 
	 * Schleife: <br>1) Man bestimme die Richtung in der man nach dem naehsten
	 * Schnittpunkt sucht, Anhand des aktuellen Schnittpunktes und der Anfangs-
	 * und Endpunkte des aktuellen Objekts.<br>2) Man suche nun die Liste in der
	 * vorher ermittelten Richtung nach einem Schnittpunkt ab, dessen Liste mit
	 * beteiligten Objekte das aktuelle Objekt enthaelt jedoch nicht das letzte
	 * Object.<br>3) Aus dem so ermittelten Schnittpunkt und dem aktuellen
	 * Schnittpunkt wird ein neues Objekt, des selbene Typs des aktuellen
	 * Objektes erzeugt (falls noetig). Das so erzeugte Objekt ist ein Objekt
	 * der Minkowski-Summe.<br>4) Zu letzt werden noch folgende Variablen
	 * aktualisiert:<br>- aktuelles Objekt<br>- letztes Objekt<br>- aktueller
	 * Schnittpunkt
	 * 
	 * @param intersections
	 *            (liste der Schnittpunkte)
	 * 
	 * @return java.lang.Object
	 */
	private Object minkSumPart3(
			SimpleList intersections)
	{
		if (!intersections.empty())
		{
			// Es wurden Schnitte gefunden
			Point2 firstPoint = (Point2) intersections.first().key();
			Object firstObject = ((SimpleList) intersections.first().value())
					.firstValue();
			Object lastObject = ((SimpleList) intersections.first().value())
					.lastValue();
			Rectangle2D rec1 = null, rec2 = null;
			if (firstObject instanceof Segment2Ext)
			{
				rec1 = ((Segment2Ext) firstObject).getBoundingRect();
			}
			else if (firstObject instanceof Arc2ExtModified)
			{
				rec1 = ((Arc2ExtModified) firstObject).getBoundingRect();
			}
			if (lastObject instanceof Segment2Ext)
			{
				rec2 = ((Segment2Ext) lastObject).getBoundingRect();
			}
			else if (lastObject instanceof Arc2ExtModified)
			{
				rec2 = ((Arc2ExtModified) lastObject).getBoundingRect();
			}
			if (rec1.getMaxY() == rec2.getMaxY())
			{
				if (rec1.getMinY() < rec2.getMinY())
				{
					firstObject = ((SimpleList) intersections.first().value())
							.lastValue();
					lastObject = ((SimpleList) intersections.first().value())
							.firstValue();
				}
			}
			else if (rec1.getMaxY() < rec2.getMaxY())
			{
				firstObject = ((SimpleList) intersections.first().value())
						.lastValue();
				lastObject = ((SimpleList) intersections.first().value())
						.firstValue();
			}
			Object actObject = firstObject;
			ListItem lItem = intersections.first();
			Point2 p = (Point2) lItem.key();

			lItem = intersections.first();
			p = (Point2) lItem.key();
			SimpleList list = new SimpleList();
			Point2 lastPoint = (Point2) intersections.firstKey();
			if (lastObject instanceof Arc2ExtModified)
			{
				if (lastPoint.equals(((Arc2ExtModified) lastObject).source(),
						0.0001))
					lastPoint = ((Arc2ExtModified) lastObject).target();
				else
					lastPoint = ((Arc2ExtModified) lastObject).source();
			}
			else
			{
				if (lastPoint.equals(((Segment2Ext) lastObject).source(),
						0.0001))
					lastPoint = ((Segment2Ext) lastObject).target();
				else
					lastPoint = ((Segment2Ext) lastObject).source();
			}

			ListItem lastItem = null;
			int zaehler2 = 0;
			int zaehler3 = 0;

			do
			{
				Point2 nextP = null;
				if (actObject instanceof Segment2Ext)
				{
					Segment2Ext seg = (Segment2Ext) actObject;
					if (p.equals(seg.source()))
					{
						nextP = seg.target();
					}
					else if (p.equals(seg.target()))
					{
						nextP = seg.source();
					}
					else
					{
						if (lastObject instanceof Arc2ExtModified)
						{
							LimitedAngle angle = new LimitedAngle(
									p.angle(((Arc2ExtModified) lastObject).centre),
									LimitedAngle.CIRCLE_ABS);
							double winkel = ((angle.deg() - 90.0) < 0) ? (angle
									.deg() - 90.0 + 360.0)
									: (angle.deg() - 90.0);
							nextP = findNextPoint(p, winkel, seg.source(),
									seg.target());
						}
						else
							nextP = findNextPoint(p, lastPoint, seg.source(),
									seg.target());
					}
				}
				else if (actObject instanceof Arc2ExtModified)
				{
					Arc2ExtModified arc = (Arc2ExtModified) actObject;
					if (p.equals(arc.source()))
					{
						nextP = arc.target();
					}
					else if (p.equals(arc.target()))
					{
						nextP = arc.source();
					}
					else
					{
						if (lastObject instanceof Arc2ExtModified)
						{
							LimitedAngle angle = new LimitedAngle(
									p.angle(((Arc2ExtModified) lastObject).centre),
									LimitedAngle.CIRCLE_ABS);
							double winkel = ((angle.deg() - 90.0) < 0) ? (angle
									.deg() - 90.0 + 360.0)
									: (angle.deg() - 90.0);
							nextP = findNextPoint(p, winkel, arc.source(),
									arc.target());
						}
						else
							nextP = findNextPoint(p, lastPoint, arc.source(),
									arc.target());
					}
				}

				boolean up = true;
				if (nextP.x >= p.x)
				{
					up = true;
				}
				else
				{
					up = false;
				}

				boolean found = false;
				zaehler2 = 0;
				while ((found == false)
						&& (zaehler2 < 2 * intersections.length()))
				{
					if ((up)
							&& ((lItem.next() == null) || (((Point2) lItem
									.key()).x > nextP.x)))
					{
						up = false;
					}
					else if ((!up)
							&& ((lItem.prev() == null) || (((Point2) lItem
									.key()).x < nextP.x)))
					{
						up = true;
					}
					lItem = (up) ? lItem.next() : lItem.prev();

					if (lItem == null)
					{
						lItem = null;
					}

					if ((((SimpleList) lItem.value()).firstValue()
							.equals(actObject))
							&& !(((SimpleList) lItem.value()).lastValue()
									.equals(lastObject)))
					{
						if (lastObject instanceof Segment2Ext)
						{
							Segment2Ext s = new Segment2Ext(lastPoint, p);
							list.add(s);

							lastObject = actObject;
							lastPoint = p;
							actObject = ((SimpleList) lItem.value())
									.lastValue();
							p = (Point2) lItem.key();
							if (lastItem != null)
								intersections.remove(lastItem);
							lastItem = lItem;
							found = true;
							zaehler3 += 1;
						}
						else if (lastObject instanceof Arc2ExtModified)
						{
							Arc2ExtModified a = new Arc2ExtModified(p,
									lastPoint,
									((Arc2ExtModified) lastObject).centre,
									((Arc2ExtModified) lastObject)
											.orientation());
							a.setLabel(((Arc2ExtModified) lastObject)
									.getLabel());
							a.radius = ((Arc2ExtModified) lastObject).radius;
							double winkel = Math.abs(a.delta());
							if ((winkel <= Angle.PI) && (winkel > 0.0))
							{
								list.add(a);

								lastObject = actObject;
								lastPoint = p;
								actObject = ((SimpleList) lItem.value())
										.lastValue();
								p = (Point2) lItem.key();
								if (lastItem != null)
									intersections.remove(lastItem);
								lastItem = lItem;
								found = true;
								zaehler3 += 1;
							}
						}
					}
					else if ((((SimpleList) lItem.value()).lastValue()
							.equals(actObject))
							&& !(((SimpleList) lItem.value()).firstValue()
									.equals(lastObject)))
					{
						if (lastObject instanceof Segment2Ext)
						{
							Segment2Ext s = new Segment2Ext(lastPoint, p);
							list.add(s);
							lastObject = actObject;
							lastPoint = p;
							actObject = ((SimpleList) lItem.value())
									.firstValue();
							p = (Point2) lItem.key();
							if (lastItem != null)
								intersections.remove(lastItem);
							lastItem = lItem;
							found = true;
							zaehler3 += 1;
						}
						else if (lastObject instanceof Arc2ExtModified)
						{
							Arc2ExtModified a = new Arc2ExtModified(p,
									lastPoint,
									((Arc2ExtModified) lastObject).centre,
									((Arc2ExtModified) lastObject)
											.orientation());
							a.setLabel(((Arc2ExtModified) lastObject)
									.getLabel());
							a.radius = ((Arc2ExtModified) lastObject).radius;
							double winkel = Math.abs(a.delta());
							if ((winkel <= Arc2ExtModified.PI)
									&& (winkel > 0.0))
							{
								list.add(a);
								lastObject = actObject;
								lastPoint = p;
								actObject = ((SimpleList) lItem.value())
										.firstValue();
								p = (Point2) lItem.key();
								if (lastItem != null)
									intersections.remove(lastItem);
								lastItem = lItem;
								found = true;
								zaehler3 += 1;
							}
						}
					}
					zaehler2 += 1;
				}
			}
			while (!firstPoint.equals(p)
					&& (zaehler2 < 2 * intersections.length()));
			if (lastObject instanceof Segment2Ext)
			{
				Segment2Ext s = new Segment2Ext(lastPoint, p);
				firstObject = s;
				list.add(s);
				list.remove(list.first());
			}
			else if (lastObject instanceof Arc2ExtModified)
			{
				Arc2ExtModified a = new Arc2ExtModified(p, lastPoint,
						((Arc2ExtModified) lastObject).centre,
						((Arc2ExtModified) lastObject).orientation());
				a.setLabel(((Arc2ExtModified) lastObject).getLabel());
				a.radius = ((Arc2ExtModified) lastObject).radius;
				firstObject = a;
				if (a.delta() != 0.0)
					list.add(a);
				if ((list != null) && (!list.empty()))
					list.remove(list.first());
			}
			if (p.equals(firstPoint))
				return list;
			else
				return lastPoint;
		}
		return null;
	}


	/**
	 * Naechster Punkt bzgl. einer radialen Ordnung.
	 * 
	 * @param c
	 *            Point2
	 * @param w1
	 *            double
	 * @param p2
	 *            Point2
	 * @param p3
	 *            Point2
	 * 
	 * @return Der nächste Punkt
	 */
	private Point2 findNextPoint(
			Point2 c,
			double w1,
			Point2 p2,
			Point2 p3)
	{
		LimitedAngle a2 = new LimitedAngle(c.angle(p2), LimitedAngle.CIRCLE_ABS);
		LimitedAngle a3 = new LimitedAngle(c.angle(p3), LimitedAngle.CIRCLE_ABS);

		double w2 = a2.deg();
		double w3 = a3.deg();

		w2 = (w1 < w2) ? w2 - 360 : w2;
		w3 = (w1 < w3) ? w3 - 360 : w3;

		return ((w1 - w2) < (w1 - w3)) ? p2 : p3;
	}


	/**
	 * Naechster Punkt bzgl. einer radialen Ordnung.
	 * 
	 * @param c
	 *            Point2
	 * @param p1
	 *            Point2
	 * @param p2
	 *            Point2
	 * @param p3
	 *            Point2
	 * 
	 * @return Der nächste Punkt
	 */
	private Point2 findNextPoint(
			Point2 c,
			Point2 p1,
			Point2 p2,
			Point2 p3)
	{
		Segment2Ext s1 = new Segment2Ext(c, p1);
		Segment2Ext s2 = new Segment2Ext(c, p2);
		Segment2Ext s3 = new Segment2Ext(c, p3);

		double w1 = calcAngle(s1);
		double w2 = calcAngle(s2);
		double w3 = calcAngle(s3);

		w2 = (w1 < w2) ? w2 - 360 : w2;
		w3 = (w1 < w3) ? w3 - 360 : w3;

		return ((w1 - w2) < (w1 - w3)) ? p2 : p3;
	}


	/**
	 * Fuegt ein Schnittevent in den eventTree ein.
	 * 
	 * @param eventTree
	 *            Rot-Schwarz-Baum
	 * @param o1
	 *            (Objekt eins)
	 * @param o2
	 *            (Objekt zwei)
	 * @param key
	 *            (Schnittpunkt der beiden Objekte)
	 */
	private void addEvent(
			RedBlackTree eventTree,
			Object o1,
			Object o2,
			Object key)
	{
		SimpleList list = new SimpleList();
		list.add(o1);
		list.add(o2);
		eventTree.add(new SweepTreeItem(2, key, list));
	}


	/**
	 * Berechnet den Winkel
	 * 
	 * @param s
	 *            Das Eingabesegment
	 * 
	 * @return Der Winkel
	 */
	private double calcAngle(
			Segment2Ext s)
	{
		double a = Math.toDegrees(Math.atan(s.slope()));

		if (s.target().x < s.source().x)
		{
			if (s.target().y <= s.source().y)
			{}
			else
			{
				a = a + 360;
			}
		}
		else if (s.target().x > s.source().x)
		{
			a = a + 180.0;
		}
		else if (s.target().x == s.source().x)
		{
			if (s.target().y < s.source().y)
			{
				a = 90.0;
			}
			else if (s.target().y > s.source().y)
			{
				a = 270.0;
			}
		}

		return a;
	}


	/**
	 * Gibt das Polygon zurueck fuer das die Minkowskisumme berechnet wurde.
	 * 
	 * @return Das Ergebnispolygon
	 */
	public Polygon2 get_poly()
	{
		return _poly;
	}


	/**
	 * Setzt das Ausgangspolygon.
	 * 
	 * @param _poly
	 *            Das neue Eingabepolygon
	 */
	public void set_poly(
			Polygon2 _poly)
	{
		this._poly = _poly;
	}


	/**
	 * Liefert den Radius
	 * 
	 * @return Der Radius
	 */
	public float get_radius()
	{
		return _radius;
	}


	/**
	 * Setzt den Radius fuer die Minkowskisumme
	 * 
	 * @param _radius
	 *            Der neue Radius
	 */
	public void set_radius(
			float _radius)
	{
		this._radius = _radius;
	}


	/**
	 * Liefert die zuletzt berechnete Minkowskisumme
	 * 
	 * Polygon2Ext implements Drawable
	 * 
	 * @return Die zuletzt berechnete Summe
	 * 
	 * @see Polygon2Ext
	 */
	public Polygon2Ext get_minkSum()
	{
		return _minkpoly;
	}


	/**
	 * Liefert alle bekannten Schnittpunkte, die waehrend der Berechnung der
	 * Minkowskisumme ermittelt wurden.
	 * 
	 * @return SimpleList mit Punkten vom Typ Point2
	 * 
	 * @see anja.geom.Point2
	 */
	public SimpleList get_intersections()
	{
		return _intersections;
	}


	/**
	 * Liefert alle Objekte, die als Grundlage zur Berechnung der Minkowskisumme
	 * benoetigt wurden. Die Objekte sind Drawable
	 * 
	 * @return Liste mit allen Objekten
	 */
	public SimpleList get_objectList()
	{
		return _objectList;
	}


	/**
	 * Sollte es zu einem Problem bei der Berechnung gekommen sein, so gibt
	 * diese Funktion einen Anhaltspunkt, an welcher Stelle sich das Problem
	 * befindet.
	 * 
	 * @return Position des Fehlers
	 * 
	 * @see anja.geom.Point2
	 */
	public Point2 get_ErrorPosition()
	{
		return _errorPosition;
	}


	/**
	 * Schrittnummer passend zum get_sweepStatus()
	 * 
	 * @return Die Schrittnummer
	 */
	public int get_sweepStep()
	{
		return _sweepStep;
	}


	/**
	 * Setzt die Schrittnummer fuer get_sweepStatus() Standardwert ist 0
	 * (deaktiviert)
	 * 
	 * @param step
	 *            Die neue, aktuelle Schrittnummer
	 */
	public void set_sweepStep(
			int step)
	{
		_sweepStep = step;
	}


	/**
	 * Liefert die SSS zum Zeitpunkt (Schritt) get_sweepStep()
	 * 
	 * @return Die SSS zum jetzigen Zeitpunkt
	 */
	public List get_sweepStatus()
	{
		return _sweepStatus;
	}


	/**
	 * Gibt die Ressourcen des Objekts frei
	 */
	private void reset()
	{
		_minkpoly = null;
		_intersections = null;
		_objectList = null;
		_errorPosition = null;
		_sweepStatus = null;
		_verticalSegs = null;
		_listOfTrapezoids = null;
		_listOfTriangles = null;
		_status = INITIALIZED;
	}


	/**
	 * Liefert die ermittelten vertikalen Segmente, diese sind Drawable.
	 * 
	 * @return Liste der vertikalen Segmente
	 */
	public List get_verticalSegs()
	{
		return _verticalSegs;
	}


	/**
	 * Liefert eine Liste mit Pseudo-Vierecken, die bei der Trinagulation
	 * "triangulatePlane()" berechnet wurden. PseudoTrapezoid2 implements
	 * Drawable.
	 * 
	 * @see PseudoTrapezoid2
	 * 
	 * @return SimpleList mit Pseudo-Vierecken vom Typ PseudoTrapezoid2
	 */
	public SimpleList get_listOfTrapezoids()
	{
		return _listOfTrapezoids;
	}


	/**
	 * Liefert eine Liste mit Pseudo-Vierecken, die bei der Trinagulation
	 * "triangulatePlane()" berechnet wurden.
	 * 
	 * PseudoTriangle2 implements Drawable.
	 * 
	 * @return List mit Pseudo-Dreiecken vom Typ PseudoTriangle2
	 * 
	 * @see anja.geom.MinkSum.PseudoTriangle2
	 */
	public List get_listOfTriangles()
	{
		return _listOfTriangles;
	}


	/**
	 * Liefert den derzeitigen Status der Berechnungen
	 * 
	 * @return Der Status
	 * 
	 * @see MinkSum#NOT_INITIALIZED
	 * @see MinkSum#INITIALIZED
	 * @see MinkSum#POLYGON_IS_NOT_OK
	 * @see MinkSum#RADIUS_IS_TO_SMALL
	 * @see MinkSum#TROUBLE_AT_ERRORPOSITION
	 * @see MinkSum#MINKSUM_IS_OK
	 * @see MinkSum#MINKSUM_AND_TRIANGULATION_ARE_OK
	 * 
	 */
	public int get_status()
	{
		return _status;
	}


	/**
	 * Es handelt sich hierbei um einen Algorithmus der zu einer gegebenen
	 * Minkowskisumme (bestehend aus Segment2 und Arc2 Objekten) alle vertikalen
	 * Segmente findet.<br><br>
	 * 
	 * An jedem Schnittpunkt, zweier Objekte in der Minkowskisumme, und an jedem
	 * Bogen, an dem eine vertikale Tangente angelegt werden kann, werden
	 * Segmente erzeugt. Diese Segmente werden sowohl nach oben als auch nach
	 * unten erweitert bis sie auf ein anderes Objekt aus der Minkowskisumme
	 * stoßen. Ein vertikales Segment darf die Minkowskisumme nicht schneiden.
	 * 
	 * @author Darius Geiss
	 * 
	 */
	public class PhaseI
			extends Sweep
	{

		/** Ereignis-ID: linker Endpunkt erreicht */
		public final static byte	LEFT_ENDPOINT		= 1;
		/** Ereignis-ID: Schnittpunkt erreicht */
		public final static byte	INTERSECTION		= 2;
		/** Ereignis-ID: rechter Endpunkt erreicht */
		public final static byte	RIGHT_ENDPOINT		= 3;

		byte						UP					= 1;
		byte						DOWN				= 2;
		byte						VERTICAL			= 3;
		byte						LEFT				= 4;
		byte						RIGHT				= 5;
		byte						EQUAL				= 6;
		byte						DIVERSE				= 7;

		// Ausgabemodus-Konstanten

		/** Ausgabemodus: Nur Schnittpunkte werden ausgegeben */
		public final static byte	POINTS				= 0;
		/**
		 * Ausgabemodus: Schnittpunkte und beteiligte Segmente werden ausgegeben
		 */
		public final static byte	POINTS_AND_SEGMENTS	= 1;

		// Schnittmodus-Konstanten

		/** Schnittmodus: Alle Schnittpunkte werden berechnet */
		public final static byte	ALL_INTERSECTIONS	= 0;
		/** Schnittmodus: Nur echte Schnitte werden berechnet */
		public final static byte	REAL_INTERSECTIONS	= 1;
		/**
		 * Schnittmodus: Nur Schnittpunkte, die nicht an den
		 * Segment-Target-Punkten liegen, werden ausgegeben
		 */
		public final static byte	WITHOUT_TARGETS		= 2;

		/** gesetzter Ausgabemodus */
		private byte				_output_mode		= POINTS;
		/** gesetzter Schnittmodus */
		private byte				_intersection_mode	= ALL_INTERSECTIONS;
		/** Liste mit den Ausgabepunkten. */
		private List				_points;
		/** Spezielervergleicher fuer die SSS */
		private SweepComparitor		_swingcomparitor;
		/** letztes Event */
		private SweepEvent			_lastEvent			= null;

		/** Beinhaltet alle Referenzen auf Events in denen das Objekt vorkommt */
		private BinarySearchTree	eventRefs;


		// Setzen von Modi
		// ===============

		/**
		 * Liefert den gesetzten Ausgabemodus.
		 * 
		 * @return Ausgabemodus (eine Konstante aus {POINTS,
		 *         POINTS_AND_SEGMENTS})
		 */
		public byte outputMode()
		{
			return _output_mode;
		}


		/**
		 * Setzt den Ausgabemodus auf die uebergebene Konstante.
		 * 
		 * @param mode
		 *            neuer Ausgabemodus (eine Konstante aus {POINTS,
		 *            POINTS_AND_SEGMENTS})
		 */
		public void setOutputMode(
				byte mode)
		{
			_output_mode = mode;
		}


		/**
		 * Liefert den gesetzten Schnittmodus.
		 * 
		 * @return Schnittmodus (eine Konstante aus {ALL_INTERSECTIONS,
		 *         ALL_INTERSECTIONS, WITHOUT_TARGETS}
		 */
		public byte intersectionMode()
		{
			return _intersection_mode;
		}


		/**
		 * Setzt den Schnittmodus auf die uebergebene Konstante.
		 * 
		 * @param mode
		 *            neuer Schnittmodus (eine Konstante aus {ALL_INTERSECTIONS,
		 *            ALL_INTERSECTIONS, WITHOUT_TARGETS}
		 */
		public void setIntersectionMode(
				byte mode)
		{
			_intersection_mode = mode;
		}


		// Algorithmus
		// ===========

		/**
		 * Fuehrt einen Plane-Sweep durch, um alle vetrtikalen Segmente Segmente
		 * zu bestimmen.
		 * 
		 * @param L
		 *            Segmentliste (Liste mit Segment2Ext- und
		 *            Arc2ExtModified-Objekten)
		 * @param eTree
		 *            (Binärbaum mit den bereits bekannten Schnittpukten)
		 * 
		 * @return Liste mit den vertikalen Segmenten
		 */
		public List segmentIntersection(
				SimpleList L,
				BinarySearchTree eTree)
		{
			if ((L == null) || (L.length() < 2))
				return new List();
			PointComparitor pcompare = new PointComparitor(
					PointComparitor.X_ORDER);
			createEventStructure(new SweepEventComparitor(pcompare), true);
			_swingcomparitor = new SweepComparitor();
			eventRefs = new BinarySearchTree();
			createSSS(_swingcomparitor);
			sss().setAllowDuplicates(true);
			event().setAllowDuplicates(true);

			// Start- und End-Punkte werden in den Eventbaum eingefuegt. 
			Segment2Ext s;
			Arc2ExtModified a;
			for (ListItem i = L.first(); i != null; i = i.next())
			{
				if (i.value() instanceof Segment2Ext)
				{
					s = (Segment2Ext) i.key();
					if (PointComparitor.compareX(s.source(), s.target()) == Comparitor.SMALLER)
					{
						insertEvent(LEFT_ENDPOINT, s.source(), s);
						insertEvent(RIGHT_ENDPOINT, s.target(), s);
					}
					else
					{
						insertEvent(LEFT_ENDPOINT, s.target(), s);
						insertEvent(RIGHT_ENDPOINT, s.source(), s);
					}
				}
				if (i.value() instanceof Arc2ExtModified)
				{
					a = (Arc2ExtModified) i.key();
					if (PointComparitor.compareX(a.source(), a.target()) == Comparitor.SMALLER)
					{
						insertEvent(LEFT_ENDPOINT, a.source(), a);
						insertEvent(RIGHT_ENDPOINT, a.target(), a);
					}
					else
					{
						insertEvent(LEFT_ENDPOINT, a.target(), a);
						insertEvent(RIGHT_ENDPOINT, a.source(), a);
					}
				}
			}

			_points = new List();

			// Die bereits bekannten Schnittpunkte werden ebenfalls in den Eventbaum eingefuegt.
			TreeItem tItem = eTree.first();
			while (tItem != null)
			{
				insertEvent(((SweepTreeItem) tItem).getID(),
						((SweepTreeItem) tItem).key(),
						((SweepTreeItem) tItem).value());
				tItem = eTree.next(tItem);
			}

			_points = new List();
			execute(0);

			return _points;
		}


		/**
		 * Fuegt ein Ereignis in die Ereignisstruktur ein. Die Laufzeit betraegt
		 * O(log n) mit n=Anzahl der bisherigen Ereignisse. Die Referenzen der
		 * einzelenen Objekte aus "value" werden in "eventRefs" gespeichert.
		 * 
		 * @param id
		 *            Event-ID
		 * @param key
		 *            Schluessel, nachdem die Ereignisse sortiert sind
		 * @param value
		 *            zu speichernder Wert fuer das Event
		 */
		public void insertEvent(
				int id,
				Object key,
				Object value)
		{
			if (event() == null)
				return;
			TreeItem tItem = event().add(new SweepTreeItem(id, key, value));
			if (id == 2)
			{

				Object o1 = ((SimpleList) value).firstValue();
				Object o2 = ((SimpleList) value).lastValue();

				TreeItem tI = eventRefs.find(o1);
				if (tI == null)
				{
					SimpleList l = new SimpleList();
					l.add(tItem);
					eventRefs.add(o1, l);
				}
				else
				{
					SimpleList l = (SimpleList) tI.value();
					l.add(tItem);
				}

				tI = eventRefs.find(o2);
				if (tI == null)
				{
					SimpleList l = new SimpleList();
					l.add(tItem);
					eventRefs.add(o2, l);
				}
				else
				{
					SimpleList l = (SimpleList) tI.value();
					l.add(tItem);
				}

			}
			else
			{
				TreeItem tI = eventRefs.find(value);
				if (tI == null)
				{
					SimpleList l = new SimpleList();
					l.add(tItem);
					eventRefs.add(value, l);
				}
				else
				{
					SimpleList l = (SimpleList) tI.value();
					l.add(tItem);
				}
			}

		}


		/**
		 * Verarbeitet das SweepEvent e.
		 * 
		 * @param e
		 *            Das SweepEvent
		 */
		public void processEvent(
				SweepEvent e)
		{
			if (_lastEvent != null)
			{
				if (((Point2) _lastEvent.key()).equals((Point2) e.key()) == false)
				{
					_swingcomparitor.setX(((Point2) e.key()).x);
					// In der SSS muessen alle Datensaetze mit dem Key = e.key
					// neu sortiert werden, damit die Reihenfolge der Datensaetze
					// (nach den Y-Werten) bei mehrfach Ueberschneidungen
					// am Punkt e.key.x erhalten bleibt.
					SimpleList itemList = new SimpleList();
					boolean found = true;
					TreeItem item = null;
					_swingcomparitor.setDelta(SweepComparitor.BEFORE);
					while (found == true)
					{
						item = sss().find(e.key());
						if (item != null)
						{
							itemList.add(item.value());
							sss().removeSym(item);
						}
						else
						{
							found = false;
						}
					}
					_swingcomparitor.setDelta(SweepComparitor.EXACT);
					while (itemList.empty() == false)
					{
						sss().add(itemList.Pop());
					}
				}
			}
			else
			{
				_swingcomparitor.setX(((Point2) e.key()).x);
			}
			TreeItem i;
			switch (e.getID())
			{
				case LEFT_ENDPOINT:
					// linker Endpunkt => Das Objekt wird in die SSS eingefuegt und auf Schnitt
					// getestet.
					_swingcomparitor.setDelta(LineComparitor.EXACT);
					i = sss().add(e.value());
					break;
				case RIGHT_ENDPOINT:
					// rechter Endpunkt => das Objekt wird aus der SSS entfernt, die Objekte ober- und
					// unterhalb des entfernten Objektes mussen auf Schnitt getestet werden.
					_swingcomparitor.setDelta(LineComparitor.EXACT);
					i = sss().find(e.value());
					if (i == null)
					{
						// Etwas ist schief gelaufen. Die SSS scheint nicht mehr sortiert zu sein =>
						// SSS auslesen und neu erstelen.
						SimpleList itemList = new SimpleList();
						TreeItem myItem = sss().first();
						while (myItem != null)
						{
							itemList.add(myItem.value());
							myItem = sss().next(myItem);
						}
						sss().removeTree(sss().root());
						ListItem lItem = itemList.first();
						while (lItem != null)
						{
							sss().add(lItem.value());
							lItem = lItem.next();
						}
						i = sss().find(e.value());
						if (i == null)
							return;
					}
					// Falls vertikales Segment endet, muss der Y-Wert des LineComparitors wieder zurueckgesetzt werden
					if ((e.value() instanceof Segment2Ext)
							&& ((Segment2Ext) e.value()).isVertical())
						_swingcomparitor.resetY();
					sss().removeSym(i);
					break;
				case INTERSECTION:
					// Schnittpunkt => die Lage der Objekte mit dem gemeinsamen Schnittpunkt
					// wird betrachtet. Sollte es moeglich sein einen vertikalen Strahl nach oben
					// und/oder unten zu erzeugen, der sich nicht in der Minkowskisumme befindet.
					// So muss ein entsprechendes Segment2Ext berechnet werden.
					if (((SimpleList) e.value()).firstValue() instanceof Segment2Ext)
					{
						Segment2Ext s1 = (Segment2Ext) ((SimpleList) e.value())
								.firstValue();
						byte directionS1 = (s1.source().x < s1.target().x) ? UP
								: ((s1.source().x > s1.target().x) ? DOWN
										: VERTICAL);
						if (((SimpleList) e.value()).lastValue() instanceof Segment2Ext)
						{
							Segment2Ext s2 = (Segment2Ext) ((SimpleList) e
									.value()).lastValue();
							byte directionS2 = (s2.source().x < s2.target().x) ? UP
									: ((s2.source().x > s2.target().x) ? DOWN
											: VERTICAL);
							if ((directionS1 == UP) && (directionS2 == UP))
							{
								// Eine Verlaengerung nach oben ist moeglich
								VerticalSegment result = objectAbove(
										(Point2) e.key(), s1, s2, EQUAL);
								_points.add(result);
							}
							else if ((directionS1 == DOWN)
									&& (directionS2 == DOWN))
							{
								// Eine Verlaengerung nach unten ist moeglich
								VerticalSegment result = objectBelow(
										(Point2) e.key(), s1, s2, EQUAL);
								_points.add(result);
							}
						}
						else
						{
							Arc2ExtModified a2 = (Arc2ExtModified) ((SimpleList) e
									.value()).lastValue();
							byte directionA2 = (a2.getLabel().equals("unten")) ? DOWN
									: UP;
							if ((directionA2 == UP) && (directionS1 == UP))
							{
								VerticalSegment result = objectAbove(
										(Point2) e.key(), s1, a2, EQUAL);
								_points.add(result);
							}
							else if ((directionA2 == DOWN)
									&& (directionS1 == DOWN))
							{
								VerticalSegment result = objectBelow(
										(Point2) e.key(), s1, a2, EQUAL);
								_points.add(result);
							}
							else if ((directionA2 == UP)
									&& (directionS1 == VERTICAL))
							{
								VerticalSegment result = objectAbove(
										(Point2) e.key(), s1, a2, EQUAL);
								_points.add(result);
							}
						}
					}
					else
					{
						Arc2ExtModified a1 = (Arc2ExtModified) ((SimpleList) e
								.value()).firstValue();
						byte directionA1 = (a1.getLabel().equals("unten")) ? DOWN
								: UP;
						if (((SimpleList) e.value()).lastValue() instanceof Segment2Ext)
						{
							Segment2Ext s2 = (Segment2Ext) ((SimpleList) e
									.value()).lastValue();
							byte directionS2 = (s2.source().x < s2.target().x) ? UP
									: ((s2.source().x > s2.target().x) ? DOWN
											: VERTICAL);
							if ((directionA1 == UP) && (directionS2 == UP))
							{
								VerticalSegment result = objectAbove(
										(Point2) e.key(), a1, s2, EQUAL);
								_points.add(result);
							}
							else if ((directionA1 == DOWN)
									&& (directionS2 == DOWN))
							{
								VerticalSegment result = objectBelow(
										(Point2) e.key(), a1, s2, EQUAL);
								_points.add(result);
							}
							else if ((directionA1 == DOWN)
									&& (directionS2 == VERTICAL))
							{
								VerticalSegment result = objectBelow(
										(Point2) e.key(), a1, s2, EQUAL);
								_points.add(result);
							}
						}
						else
						{
							Arc2ExtModified a2 = (Arc2ExtModified) ((SimpleList) e
									.value()).lastValue();
							byte directionA2 = (a2.getLabel().equals("unten")) ? DOWN
									: UP;
							if (directionA1 != directionA2)
							{
								if (a1.centre.equals(a2.centre))
								{
									if (!_points.empty()
											&& (_points.lastValue() != null)
											&& (((VerticalSegment) _points
													.lastValue())
													.getLowerPoint().getX() == ((Point2) e
													.key()).getX()))
									{
										if (a1.centre.x > ((Point2) e.key()).x)
										{
											// Es befinden sich mehrere VS direkt uebereinander. Sonderfall.
											VerticalSegment midVS = (VerticalSegment) _points
													.last().value();
											VerticalSegment topVS = new VerticalSegment(
													(Point2) e.key(),
													midVS.target(),
													midVS.getTopLeft(),
													midVS.getTopRight(), null,
													null);
											topVS.setObjectBelow(midVS);
											midVS.setTopLeft(null);
											midVS.setTopRight(null);
											midVS.setObjectAbove(topVS);
											midVS.setTarget((Point2) e.key());
											SimpleList adjacentVS = null;
											if (topVS.getTopLeft() instanceof Arc2ExtModified)
											{
												adjacentVS = ((Arc2ExtModified) topVS
														.getTopLeft())
														.get_adjacentVS();
											}
											else if (topVS.getTopLeft() instanceof Segment2Ext)
											{
												adjacentVS = ((Segment2Ext) topVS
														.getTopLeft())
														.get_adjacentVS();
											}
											if (adjacentVS != null)
											{
												adjacentVS.remove(midVS);
												adjacentVS.add(topVS);
											}
											if (topVS.getTopRight() instanceof Arc2ExtModified)
											{
												adjacentVS = ((Arc2ExtModified) topVS
														.getTopRight())
														.get_adjacentVS();
											}
											else if (topVS.getTopRight() instanceof Segment2Ext)
											{
												adjacentVS = ((Segment2Ext) topVS
														.getTopRight())
														.get_adjacentVS();
											}
											if (adjacentVS != null)
											{
												adjacentVS.remove(midVS);
												adjacentVS.add(topVS);
											}
											_points.add(topVS);

											if (a1.centre.x > ((Point2) e.key()).x)
											{
												topVS.setBottomRight(a2);
												a2.add_adjacentVS(topVS);
												midVS.setTopRight(a1);
												a1.add_adjacentVS(midVS);
											}
											else
											{
												topVS.setBottomLeft(a1);
												a1.add_adjacentVS(topVS);
												midVS.setTopLeft(a2);
												a2.add_adjacentVS(midVS);
											}
										}
										else
										{
											VerticalSegment midVS = (VerticalSegment) _points
													.last().value();
											VerticalSegment topVS = objectAbove(
													(Point2) e.key(), a1, a2,
													DIVERSE);
											topVS.setObjectBelow(midVS);
											midVS.setObjectAbove(topVS);
											a2.add_adjacentVS(midVS);
											_points.add(topVS);
										}
									}
									else
									{
										VerticalSegment result = objectAbove(
												(Point2) e.key(), a1, a2,
												DIVERSE);
										VerticalSegment result2 = objectBelow(
												(Point2) e.key(), a1, a2,
												DIVERSE);
										_points.add(result2);
										_points.add(result);
										result.setObjectBelow(result2);
										result2.setObjectAbove(result);
									}
								}
							}
							else if (directionA1 == UP)
							{
								VerticalSegment result = objectAbove(
										(Point2) e.key(), a1, a2, EQUAL);
								_points.add(result);
							}
							else if (directionA1 == DOWN)
							{
								VerticalSegment result = objectBelow(
										(Point2) e.key(), a1, a2, EQUAL);
								_points.add(result);
							}
						}
					}
			}
			_lastEvent = e;
		}


		/**
		 * Diese Funktion sucht nach dem naechsten Objekt in der SSS, dass sich
		 * oberhalb von den Objekten o1 und o2 befindet.
		 * 
		 * @param key
		 *            ---
		 * @param o1
		 *            Objekt 1
		 * @param o2
		 *            Objekt 2
		 * @param direction
		 *            EQUAL oder DIVERSE
		 * 
		 * @return TreeItem oder null
		 */
		private VerticalSegment objectAbove(
				Point2 key,
				Object o1,
				Object o2,
				byte direction)
		{
			boolean found = false;
			TreeItem item = sss().findBigger(key);
			String s1, s2, s3;
			while ((!found) && (item != null))
			{
				if (item.value() instanceof Arc2ExtModified)
					s1 = ((Arc2ExtModified) item.value()).toString();
				else
					s1 = ((Segment2Ext) item.value()).toString();
				if (o1 instanceof Arc2ExtModified)
					s2 = ((Arc2ExtModified) o1).toString();
				else
					s2 = ((Segment2Ext) o1).toString();
				if (o2 instanceof Arc2ExtModified)
					s3 = ((Arc2ExtModified) o2).toString();
				else
					s3 = ((Segment2Ext) o2).toString();
				if ((!s1.equals(s2)) && (!s1.equals(s3)))
					found = true;
				if (!found)
					item = sss().next(item);
			}
			VerticalSegment vs = null;
			if ((item != null) && (item.value() instanceof Segment2Ext))
			{
				Segment2Ext s = (Segment2Ext) item.value();
				// Alle Events mit denen "s" etwas zu tun hat
				// Es sollten hoechstens vier Events sein Anfang, Ende und zwei Schnitte jeweils einer an Angang und am Ende
				SimpleList l = (SimpleList) ((TreeItem) eventRefs.find(s))
						.value();
				// Rechter teil des Segmentes "s" nach dem trennen am Punkt "inter" 
				Segment2Ext rightPartOfS;
				// Schnittpunkt des Segmentes "s" mit dem vertikalen Segment "result"
				Point2 inter = calculatePoint(s, key.x);
				Object object2 = null;
				Point2 inter2 = new Point2();
				byte type = 0;
				ListItem lItem = null;
				for (lItem = l.first(); lItem != null; lItem = l.next(lItem))
				{
					SweepTreeItem tItem = (SweepTreeItem) lItem.value();
					if (((Point2) tItem.key()).x >= key.x)
					{
						if (tItem.getID() == 2)
						{
							if (s.equals(((SimpleList) tItem.value())
									.firstValue()))
							{
								object2 = ((SimpleList) tItem.value())
										.lastValue();
								type = 1;
							}
							else
							{
								object2 = ((SimpleList) tItem.value())
										.firstValue();
								type = 2;
							}
							inter2 = (Point2) tItem.key();
						}
					}
					event().removeSym(tItem);
				}
				if (s.getLeftPoint().equals(s.source()))
				{
					rightPartOfS = new Segment2Ext(new Point2(s.target()),
							new Point2(inter));
					s.setTarget(inter);
				}
				else
				{
					rightPartOfS = new Segment2Ext(new Point2(s.source()),
							new Point2(inter));
					s.setSource(inter);
				}
				SimpleList l2 = new SimpleList();
				if (type == 1)
				{
					l2.add(rightPartOfS);
					l2.add(object2);
				}
				else if (type == 2)
				{
					l2.add(object2);
					l2.add(rightPartOfS);
				}

				insertEvent(RIGHT_ENDPOINT, rightPartOfS.source(), rightPartOfS);
				if (type > 0)
					insertEvent(INTERSECTION, inter2, l2);
				// altes Segment aus der SSS entfernen
				sss().removeSym(item);
				// neues Segment in die SSS einfuegen
				sss().add(rightPartOfS);

				Segment2Ext result = new Segment2Ext(key, inter);
				vs = new VerticalSegment(key, inter, s, rightPartOfS, null,
						null);
				rightPartOfS.add_adjacentVS(vs);
				s.add_adjacentVS(vs);

				if (lItem != null)
					l.remove(lItem);

				if (result.target() == null)
				{
					System.out.println("Fehler: kein Segment");
				}
			}
			else if ((item != null)
					&& (item.value() instanceof Arc2ExtModified))
			{
				Arc2ExtModified a = (Arc2ExtModified) item.value();
				// Alle Events mit denen "s" etwas zu tun hat
				// Es sollten hoechstens vier Events sein Anfang, Ende und zwei Schnitte jeweils einer an Angang und am Ende
				SimpleList l = (SimpleList) ((TreeItem) eventRefs.find(a))
						.value();
				// Rechter teil des Bogens "a" nach dem trennen am Punkt "inter" 
				Arc2ExtModified rightPartOfA;
				Point2 inter = new Point2(key.x,
						(float) (a.centre.y - Math.sqrt(Math.pow(a.radius, 2)
								- Math.pow(Math.abs(key.x - a.centre.x), 2))));
				LimitedAngle la = new LimitedAngle(a.centre.angle(inter),
						LimitedAngle.CIRCLE_ABS);
				boolean stop = false;
				if (a.source().x < a.target().x)
				{
					if (la.rad() == a.targetAngle())
					{
						stop = true;
						vs = new VerticalSegment(key, inter, a, null, null,
								null);
					}
				}
				else
				{
					if (a.sourceAngle() == la.rad())
					{
						stop = true;
						vs = new VerticalSegment(key, inter, null, a, null,
								null);
					}
				}
				if (stop == false)
				{
					Object object2 = null;
					Point2 inter2 = new Point2();
					byte type = 0;
					ListItem lItem = null;
					for (lItem = l.first(); lItem != null; lItem = l
							.next(lItem))
					{
						SweepTreeItem tItem = (SweepTreeItem) lItem.value();
						if (((Point2) tItem.key()).x >= key.x)
						{
							if (tItem.getID() == 2)
							{
								if (a.equals(((SimpleList) tItem.value())
										.firstValue()))
								{
									object2 = ((SimpleList) tItem.value())
											.lastValue();
									type = 1;
								}
								else
								{
									object2 = ((SimpleList) tItem.value())
											.firstValue();
									type = 2;
								}
								inter2 = (Point2) tItem.key();
							}
						}
						event().removeSym(tItem);
					}

					if (a.source().x < a.target().x)
					{
						rightPartOfA = new Arc2ExtModified(a.centre, a.radius,
								la.rad(), a.targetAngle(), a.orientation(),
								inter, a.target());
						a.setRefTraget(inter);
						a.setTargetAngle(la.rad());
						inter = new Point2(a.target());
					}
					else
					{
						rightPartOfA = new Arc2ExtModified(a.centre, a.radius,
								a.sourceAngle(), la.rad(), a.orientation(),
								a.source(), inter);
						a.setRefSource(inter);
						a.setSourceAngle(la.rad());
						inter = new Point2(a.source());
					}
					rightPartOfA.setLabel(a.getLabel());
					SimpleList l2 = new SimpleList();
					if (type == 1)
					{
						l2.add(rightPartOfA);
						l2.add(object2);
					}
					else if (type == 2)
					{
						l2.add(object2);
						l2.add(rightPartOfA);
					}
					insertEvent(RIGHT_ENDPOINT, inter2, rightPartOfA);
					if (type > 0)
						insertEvent(INTERSECTION, inter2, l2);
					// altes Arc aus der SSS entfernen
					sss().removeSym(item);
					// neues Arc in die SSS einfuegen
					if (Math.abs(rightPartOfA.delta()) > 0.0)
						sss().add(rightPartOfA);

					Segment2Ext result = new Segment2Ext(key, inter);
					if (Math.abs(rightPartOfA.delta()) > 0.0)
					{
						vs = new VerticalSegment(key, inter, a, rightPartOfA,
								null, null);
						rightPartOfA.add_adjacentVS(vs);
					}
					else
					{
						vs = new VerticalSegment(key, inter, a, null, null,
								null);
					}
					a.add_adjacentVS(vs);

					if (lItem != null)
						l.remove(lItem);

					if (result.target() == null)
					{
						System.out.println("Fehler: kein Segment");
					}
				}
			}
			else
			{
				Segment2Ext s = new Segment2Ext();
				s.setSource(key);
				s.setTarget(key.x, Float.MAX_VALUE);
				vs = new VerticalSegment(key,
						new Point2(key.x, Float.MAX_VALUE), null, null, null,
						null);
			}
			if (direction == EQUAL)
			{
				vs.setBottomLeft(o1);
				vs.setBottomRight(o2);
				if (o1 instanceof Arc2ExtModified)
					((Arc2ExtModified) o1).add_adjacentVS(vs);
				if (o1 instanceof Segment2Ext)
					((Segment2Ext) o1).add_adjacentVS(vs);
				if (o2 instanceof Arc2ExtModified)
					((Arc2ExtModified) o2).add_adjacentVS(vs);
				if (o2 instanceof Segment2Ext)
					((Segment2Ext) o2).add_adjacentVS(vs);
			}
			else if (direction == DIVERSE)
			{
				if (((Arc2ExtModified) o1).centre.x > key.x)
				{
					vs.setBottomRight(o2);
					((Arc2ExtModified) o2).add_adjacentVS(vs);
				}
				else
				{
					vs.setBottomLeft(o1);
					((Arc2ExtModified) o1).add_adjacentVS(vs);
				}
			}
			return vs;
		}


		/**
		 * Diese Funktion sucht nach dem naechsten Objekt in der SSS, dass sich
		 * unterhalb von den Objekten o1 und o2 befindet.
		 * 
		 * @param key
		 *            ---
		 * @param o1
		 *            Objekt 1
		 * @param o2
		 *            Objekt 2
		 * @param direction
		 *            EQUAL oder DIVERSE
		 * 
		 * @return TreeItem oder null
		 */
		private VerticalSegment objectBelow(
				Point2 key,
				Object o1,
				Object o2,
				byte direction)
		{
			boolean found = false;
			TreeItem item = sss().findSmaller(key);
			String s1, s2, s3;
			while ((!found) && (item != null))
			{
				if (item.value() instanceof Arc2ExtModified)
					s1 = ((Arc2ExtModified) item.value()).toString();
				else
					s1 = ((Segment2Ext) item.value()).toString();
				if (o1 instanceof Arc2ExtModified)
					s2 = ((Arc2ExtModified) o1).toString();
				else
					s2 = ((Segment2Ext) o1).toString();
				if (o2 instanceof Arc2ExtModified)
					s3 = ((Arc2ExtModified) o2).toString();
				else
					s3 = ((Segment2Ext) o2).toString();
				if ((!s1.equals(s2)) && (!s1.equals(s3)))
					found = true;
				if (!found)
					item = sss().prev(item);
			}
			VerticalSegment vs;
			if ((item != null) && (item.value() instanceof Segment2Ext))
			{
				Segment2Ext s = (Segment2Ext) item.value();
				// Alle Events mit denen "s" etwas zu tun hat
				// Es sollten vier Events sein Anfang, Ende und zwei Schnitte jeweils einer an Angang und am Ende
				SimpleList l = (SimpleList) ((TreeItem) eventRefs.find(s))
						.value();
				// Rechter teil des Segmentes "s" nach dem trennen am Punkt "inter" 
				Segment2Ext rightPartOfS;
				// Schnittpunkt des Segmentes "s" mit dem vertikalen Segment "result"
				Point2 inter = calculatePoint(s, key.x);
				Object object2 = null;
				Point2 inter2 = new Point2();
				byte type = 0;
				ListItem lItem = null;
				for (lItem = l.first(); lItem != null; lItem = l.next(lItem))
				{
					SweepTreeItem tItem = (SweepTreeItem) lItem.value();
					if (((Point2) tItem.key()).x >= key.x)
					{
						if (tItem.getID() == 2)
						{
							if (s.equals(((SimpleList) tItem.value())
									.firstValue()))
							{
								object2 = ((SimpleList) tItem.value())
										.lastValue();
								type = 1;
							}
							else
							{
								object2 = ((SimpleList) tItem.value())
										.firstValue();
								type = 2;
							}
							inter2 = (Point2) tItem.key();
						}
					}
					event().removeSym(tItem);
				}
				if (s.getLeftPoint().equals(s.source()))
				{
					rightPartOfS = new Segment2Ext(inter,
							new Point2(s.target()));
					s.setTarget(inter);
				}
				else
				{
					rightPartOfS = new Segment2Ext(inter,
							new Point2(s.source()));
					s.setSource(inter);
				}
				SimpleList l2 = new SimpleList();
				if (type == 1)
				{
					l2.add(rightPartOfS);
					l2.add(object2);
				}
				else if (type == 2)
				{
					l2.add(object2);
					l2.add(rightPartOfS);
				}
				insertEvent(RIGHT_ENDPOINT, rightPartOfS.target(), rightPartOfS);
				if (type > 0)
					insertEvent(INTERSECTION, inter2, l2);
				// altes Segment aus der SSS entfernen
				sss().removeSym(item);
				// neues Segment in die SSS einfuegen
				sss().add(rightPartOfS);

				// ende des neuen Segementes in die Eventstruktur einfuegen
				// insertEvent(RIGHT_ENDPOINT, rightPartOfS.target(), rightPartOfS);
				Segment2Ext result = new Segment2Ext(key, inter);
				vs = new VerticalSegment(key, inter, null, null, s,
						rightPartOfS);
				rightPartOfS.add_adjacentVS(vs);
				s.add_adjacentVS(vs);

				if (lItem != null)
					l.remove(lItem);

				if (result.target() == null)
				{
					System.out.println("Fehler: kein Segment");
				}
			}
			else if ((item != null)
					&& (item.value() instanceof Arc2ExtModified))
			{
				Arc2ExtModified a = (Arc2ExtModified) item.value();
				// Alle Events mit denen "s" etwas zu tun hat
				// Es sollten hoechstens vier Events sein Anfang, Ende und zwei Schnitte jeweils einer an Angang und am Ende
				SimpleList l = (SimpleList) ((TreeItem) eventRefs.find(a))
						.value();
				// Rechter teil des Bogens "a" nach dem trennen am Punkt "inter" 
				Arc2ExtModified rightPartOfA;
				Point2 inter = new Point2(key.x,
						(float) (a.centre.y + Math.sqrt(Math.pow(a.radius, 2)
								- Math.pow(Math.abs(key.x - a.centre.x), 2))));
				Object object2 = null;
				Point2 inter2 = new Point2();
				byte type = 0;
				ListItem lItem = null;
				for (lItem = l.first(); lItem != null; lItem = l.next(lItem))
				{
					SweepTreeItem tItem = (SweepTreeItem) lItem.value();
					if (((Point2) tItem.key()).x >= key.x)
					{
						if (tItem.getID() == 2)
						{
							if (a.equals(((SimpleList) tItem.value())
									.firstValue()))
							{
								object2 = ((SimpleList) tItem.value())
										.lastValue();
								type = 1;
							}
							else
							{
								object2 = ((SimpleList) tItem.value())
										.firstValue();
								type = 2;
							}
							inter2 = (Point2) tItem.key();
						}
					}
					event().removeSym(tItem);
				}

				LimitedAngle la = new LimitedAngle(a.centre.angle(inter),
						LimitedAngle.CIRCLE_ABS);
				if (a.source().x < a.target().x)
				{
					rightPartOfA = new Arc2ExtModified(a.centre, a.radius,
							la.rad(), a.targetAngle(), a.orientation());
					a.setRefTraget(inter);
					a.setTargetAngle(la.rad());
					inter = new Point2(a.target());
				}
				else
				{
					rightPartOfA = new Arc2ExtModified(a.centre, a.radius,
							a.sourceAngle(), la.rad(), a.orientation());
					a.setRefSource(inter);
					a.setSourceAngle(la.rad());
					inter = new Point2(a.source());
				}
				rightPartOfA.setLabel(a.getLabel());
				SimpleList l2 = new SimpleList();
				if (type == 1)
				{
					l2.add(rightPartOfA);
					l2.add(object2);
				}
				else if (type == 2)
				{
					l2.add(object2);
					l2.add(rightPartOfA);
				}
				insertEvent(RIGHT_ENDPOINT, inter2, rightPartOfA);
				if (type > 0)
					insertEvent(INTERSECTION, inter2, l2);
				// altes Segment aus der SSS entfernen
				sss().removeSym(item);
				// neues Segment in die SSS einfuegen
				if (Math.abs(rightPartOfA.delta()) > 0.0)
					sss().add(rightPartOfA);

				Segment2Ext result = new Segment2Ext(key, inter);
				vs = new VerticalSegment(key, inter, null, null, a,
						rightPartOfA);
				rightPartOfA.add_adjacentVS(vs);
				a.add_adjacentVS(vs);

				if (lItem != null)
					l.remove(lItem);

				if (result.target() == null)
				{
					System.out.println("Fehler: kein Segment");
				}
			}
			else
			{
				Segment2Ext s = new Segment2Ext();
				s.setSource(key);
				s.setTarget(key.x, Float.MIN_VALUE);
				vs = new VerticalSegment(key,
						new Point2(key.x, Float.MIN_VALUE), null, null, null,
						null);
			}
			if (direction == EQUAL)
			{
				vs.setTopRight(o1);
				vs.setTopLeft(o2);
				if (o1 instanceof Arc2ExtModified)
					((Arc2ExtModified) o1).add_adjacentVS(vs);
				if (o1 instanceof Segment2Ext)
					((Segment2Ext) o1).add_adjacentVS(vs);
				if (o2 instanceof Arc2ExtModified)
					((Arc2ExtModified) o2).add_adjacentVS(vs);
				if (o2 instanceof Segment2Ext)
					((Segment2Ext) o2).add_adjacentVS(vs);
			}
			else if (direction == DIVERSE)
			{
				if (((Arc2ExtModified) o1).centre.x > key.x)
				{
					vs.setTopRight(o1);
					((Arc2ExtModified) o1).add_adjacentVS(vs);
				}
				else
				{
					vs.setTopLeft(o2);
					((Arc2ExtModified) o2).add_adjacentVS(vs);
				}
			}
			return vs;
		}


		/**
		 * Berechnet den Punkt mit X-Koordinate x auf der Gerade s neu.
		 * 
		 * @param s
		 *            Segment2Ext
		 * @param x
		 *            X-Koordinate
		 * 
		 * @return Punkt auf dem Objekt
		 */
		private Point2 calculatePoint(
				Segment2Ext s,
				float x)
		{
			if (s.isVertical())
			{
				if (x == s.source().x)
					return new Point2(x, Double.POSITIVE_INFINITY);
				else
					return null;
			}
			else
			{
				double slope = (double) (s.source().y - s.target().y)
						/ (double) (s.source().x - s.target().x);
				double y_abs = s.source().y - slope * s.source().x;
				Point2 p = new Point2(x, (float) (slope * x + y_abs));
				return p;
			}
		}
	}


	/**
	 * Diese Klasse basiert auf Segment2. Sie wurde um einige Funktionen
	 * erweitert, die der vereinfachung geometrichen Beziehung zu anderen
	 * Objekten dienen sollen.
	 * 
	 * @author Darius Geiss
	 * 
	 */
	public class VerticalSegment
			extends Segment2
	{

		// An das Segment grenzende Objekte
		private Object				_topLeft			= null;
		private Object				_topRight			= null;
		private Object				_bottomLeft			= null;
		private Object				_bottomRight		= null;
		private Object				_objectLeft			= null;
		private Object				_objectRight		= null;
		private Object				_objectAbove		= null;
		private Object				_objectBelow		= null;
		private PseudoTrapezoid2	_pseudoTrapLeft		= null;
		private PseudoTrapezoid2	_pseudoTrapRight	= null;
		private PseudoTriangle2		_pseudoTriaLeft		= null;
		private PseudoTriangle2		_pseudoTriaRight	= null;

		/**
		 * ID
		 */
		private static final long	serialVersionUID	= 1L;


		/**
		 * Erzeugt ein vertikales Segment.
		 * 
		 * @param input_source
		 *            Startpunkt des Segments
		 * @param input_target
		 *            Endpunkt des Segments
		 * @param TopLeft
		 *            Objekt oben links
		 * @param TopRight
		 *            Objekt oben rechts
		 * @param BottomLeft
		 *            Objekt unten links
		 * @param BottomRight
		 *            Objekt unten rechts
		 */
		public VerticalSegment(
				Point2 input_source,
				Point2 input_target,
				Object TopLeft,
				Object TopRight,
				Object BottomLeft,
				Object BottomRight)
		{
			super(input_source, input_target);
			_topLeft = TopLeft;
			_topRight = TopRight;
			_bottomLeft = BottomLeft;
			_bottomRight = BottomRight;
		}


		/**
		 * Liefert das Objekt zurück, dass sich links von dem oberen Punkt
		 * befindet.
		 * 
		 * @return Object oder null
		 */
		public Object getTopLeft()
		{
			return _topLeft;
		}


		/**
		 * Legt das Objekt fest, dass sich links von dem oberen Punkt befindet.
		 * 
		 * @param topLeft
		 *            Das Objekt
		 */
		public void setTopLeft(
				Object topLeft)
		{
			_topLeft = topLeft;
		}


		/**
		 * Liefert das Objekt zurück, dass sich rechts von dem oberen Punkt
		 * befindet.
		 * 
		 * @return Object oder null
		 */
		public Object getTopRight()
		{
			return _topRight;
		}


		/**
		 * Legt das Objekt fest, dass sich rechts von dem oberen Punkt befindet.
		 * 
		 * @param topRight
		 *            Das Objekt
		 */
		public void setTopRight(
				Object topRight)
		{
			_topRight = topRight;
		}


		/**
		 * Liefert das Objekt zurück, dass sich links von dem unteren Punkt
		 * befindet.
		 * 
		 * @return Object oder null
		 */
		public Object getBottomLeft()
		{
			return _bottomLeft;
		}


		/**
		 * Legt das Objekt fest, dass sich links von dem unteren Punkt befindet.
		 * 
		 * @param bottomLeft
		 *            Das Objekt
		 */
		public void setBottomLeft(
				Object bottomLeft)
		{
			_bottomLeft = bottomLeft;
		}


		/**
		 * Liefert das Objekt zurück, dass sich rechts von dem unteren Punkt
		 * befindet.
		 * 
		 * @return Object oder null
		 */
		public Object getBottomRight()
		{
			return _bottomRight;
		}


		/**
		 * Legt das Objekt fest, dass sich rechts von dem unteren Punkt
		 * befindet.
		 * 
		 * @param bottomRight
		 *            Das Objekt
		 */
		public void setBottomRight(
				Object bottomRight)
		{
			_bottomRight = bottomRight;
		}


		/**
		 * Liefert das Objekt links von dem vertikalen Segment.
		 * 
		 * @return Object oder null
		 */
		public Object getObjectLeft()
		{
			return _objectLeft;
		}


		/**
		 * Legt das Objekt links von dem vertikalen Segment fest.
		 * 
		 * @param objectLeft
		 *            Das Objekt
		 */
		public void setObjectLeft(
				Object objectLeft)
		{
			_objectLeft = objectLeft;
		}


		/**
		 * Liefert das Objekt rechts von dem vertikalen Segment.
		 * 
		 * @return Object oder null
		 */
		public Object getObjectRight()
		{
			return _objectRight;
		}


		/**
		 * Legt das Objekt rechts von dem vertikalen Segment fest.
		 * 
		 * @param objectRight
		 *            Das Objekt
		 */
		public void setObjectRight(
				Object objectRight)
		{
			_objectRight = objectRight;
		}


		/**
		 * Legt das Objekt fest, dass sich oberhalb des vertikalen Segmentes
		 * befindet.
		 * 
		 * @param objectAbove
		 *            Das Objekt
		 */
		public void setObjectAbove(
				Object objectAbove)
		{
			_objectAbove = objectAbove;
		}


		/**
		 * Liefert das Objekt oberhalb des vertikalen Segments.
		 * 
		 * @return Object oder null
		 */
		public Object getObjectAbove()
		{
			return _objectAbove;
		}


		/**
		 * Legt das Objekt fest, dass sich unterhalb des vertikalen Segmentes
		 * befindet.
		 * 
		 * @param objectBelow
		 *            Das Objekt
		 */
		public void setObjectBelow(
				Object objectBelow)
		{
			_objectBelow = objectBelow;
		}


		/**
		 * Liefert das Objekt unterhalb des vertikalen Segments.
		 * 
		 * @return Object oder null
		 */
		public Object getObjectBelow()
		{
			return _objectBelow;
		}


		/**
		 * Liefert das pseudo Viereck links von dem vertikalen Segment.
		 * 
		 * @return PseudoTrapezoid2 oder null
		 */
		public PseudoTrapezoid2 getPseudoTrapLeft()
		{
			return _pseudoTrapLeft;
		}


		/**
		 * Legt das pseudo Viereck links von dem vertikalen Segment fest.
		 * 
		 * @param pseudoTrapLeft
		 *            Das Pseudo-Viereck
		 */
		public void setPseudoTrapLeft(
				PseudoTrapezoid2 pseudoTrapLeft)
		{
			_pseudoTrapLeft = pseudoTrapLeft;
		}


		/**
		 * Liefert das pseudo Viereck rechts von dem vertikalen Segment.
		 * 
		 * @return PseudoTrapezoid2 oder null
		 */
		public PseudoTrapezoid2 getPseudoTrapRight()
		{
			return _pseudoTrapRight;
		}


		/**
		 * Legt das pseudo Viereck rechts von dem vertikalen Segment fest.
		 * 
		 * @param pseudoTrapRight
		 *            Das Pseudo-Viereck
		 */
		public void setPseudoTrapRight(
				PseudoTrapezoid2 pseudoTrapRight)
		{
			_pseudoTrapRight = pseudoTrapRight;
		}


		/**
		 * Liefert das pseudo Dreieck rechts von dem vertikalen Segment.
		 * 
		 * @return PseudoTriangle2 oder null
		 */
		public PseudoTriangle2 getPseudoTriaLeft()
		{
			return _pseudoTriaLeft;
		}


		/**
		 * Legt das pseudo Dreieck links von dem vertikalen Segment fest.
		 * 
		 * @param pseudoTriaLeft
		 *            Das Pseudo-Dreieck
		 */
		public void setPseudoTriaLeft(
				PseudoTriangle2 pseudoTriaLeft)
		{
			_pseudoTriaLeft = pseudoTriaLeft;
		}


		/**
		 * Liefert das pseudo Dreieck rechts von dem vertikalen Segment.
		 * 
		 * @return PseudoTriangle2 oder null
		 */
		public PseudoTriangle2 getPseudoTriaRight()
		{
			return _pseudoTriaRight;
		}


		/**
		 * Legt das pseudo Dreieck rechts von dem vertikalen Segment fest.
		 * 
		 * @param pseudoTriaRight
		 *            Das Pseudo-Dreieck
		 */
		public void setPseudoTriaRight(
				PseudoTriangle2 pseudoTriaRight)
		{
			_pseudoTriaRight = pseudoTriaRight;
		}

	}


	/**
	 * Diese Klasse basiert auf Arc2Ext. Sie wurde um einige Funktionen
	 * erweitert, die der Vereinfachung geometrischen Beziehung zu anderen
	 * Objekten dienen sollen.
	 * 
	 * @author Darius Geiss
	 * 
	 */
	public class Arc2ExtModified
			extends Arc2Ext
			implements Cloneable
	{

		/** Dreieck dessen Bestandteil dieser Bogen ist. */
		private PseudoTriangle2	_pseudoTriangle	= null;

		/**
		 * Liste von verticalen Segmenten (VerticalSegment), die an diesen Bogen
		 * grenzen.
		 */
		private SimpleList		_adjacentVS		= null;


		/**
		 * Konstruktor
		 * 
		 * @param input_centre
		 *            Das Zentrum
		 * @param input_radius
		 *            Der Radiud
		 * @param input_source_angle
		 *            Der Startwinkel (Bogenmaß)
		 * @param input_target_angle
		 *            Der Endwinkel (Bogenmaß)
		 * @param input_orientation
		 *            Die Orientierung
		 * @param source
		 *            Der Startpunkt
		 * @param target
		 *            Der Endpunkt
		 */
		public Arc2ExtModified(
				Point2 input_centre,
				double input_radius,
				double input_source_angle,
				double input_target_angle,
				int input_orientation,
				Point2 source,
				Point2 target)
		{
			super(input_centre, input_radius, input_source_angle,
					input_target_angle, input_orientation, source, target);
			_pseudoTriangle = null;
			_adjacentVS = new SimpleList();
		}


		/**
		 * Konstruktor
		 * 
		 * @param input_centre
		 *            Das Zentrum
		 * @param input_radius
		 *            Der Radiud
		 * @param input_source_angle
		 *            Der Startwinkel (Bogenmaß)
		 * @param input_target_angle
		 *            Der Endwinkel (Bogenmaß)
		 * @param input_orientation
		 *            Die Orientierung
		 */
		public Arc2ExtModified(
				Point2 input_centre,
				double input_radius,
				double input_source_angle,
				double input_target_angle,
				int input_orientation)
		{
			super(input_centre, input_radius, input_source_angle,
					input_target_angle, input_orientation);
			_pseudoTriangle = null;
			_adjacentVS = new SimpleList();
		}


		/**
		 * Konstruktor
		 * 
		 * @param input_center
		 *            Das Zentrum
		 * @param input_source
		 *            Der Startpunkt
		 * @param input_target
		 *            Der Endpunkt
		 * @param input_orientation
		 *            Die Orientierung
		 */
		public Arc2ExtModified(
				Point2 input_source,
				Point2 input_target,
				Point2 input_center,
				int input_orientation)
		{
			super(input_source, input_target, input_center, input_orientation);
			_pseudoTriangle = null;
			_adjacentVS = new SimpleList();
		}


		/**
		 * Kontruktor
		 * 
		 * @param a
		 *            Der Bogen
		 * @param source
		 *            Der Startpunkt
		 * @param target
		 *            Der Endpunkt
		 */
		public Arc2ExtModified(
				Arc2 a,
				Point2 source,
				Point2 target)
		{
			super(a, source, target);
			_pseudoTriangle = null;
			_adjacentVS = new SimpleList();
		}


		/**
		 * Kontruktor
		 * 
		 * @param a
		 *            Der Bogen
		 */
		public Arc2ExtModified(
				Arc2Ext a)
		{
			super(a);
			_pseudoTriangle = null;
			_adjacentVS = new SimpleList();
		}


		/**
		 * Liefert das Pseudo-Dreieck zurueck in dem sich der Bogen befindet.
		 * 
		 * @return Dreieck
		 */
		public PseudoTriangle2 get_pseudoTriangle()
		{
			return _pseudoTriangle;
		}


		/**
		 * Setzt das Pseudo-Dreieck in dem sich der Bogen befindet.
		 * 
		 * @param triangle
		 *            Die Pseudo-Dreiecke
		 */
		public void set_pseudoTriangle(
				PseudoTriangle2 triangle)
		{
			_pseudoTriangle = triangle;
		}


		/**
		 * Liefert alle bekannten, benachbarten, vertikale Segmente
		 * 
		 * @return SimpleList der benachbarten, vertikalen Segmente
		 */
		public SimpleList get_adjacentVS()
		{
			return _adjacentVS;
		}


		/**
		 * Setzt die benachbarten, vertikalen Segmente
		 * 
		 * @param _adjacentvs
		 *            SimpleList der benachbarten, vertikalen Segmente
		 */
		public void set_adjacentVS(
				SimpleList _adjacentvs)
		{
			_adjacentVS = _adjacentvs;
		}


		/**
		 * Fuegt ein vertikales Segment in die Nachbarschaftsliste hinzu.
		 * 
		 * @param topVS
		 *            Das vertikale Segment
		 */
		public void add_adjacentVS(
				VerticalSegment topVS)
		{
			_adjacentVS.add(topVS);
		}

	}


	/**
	 * Hilfsklasse für CagingPolygons. Sie erzeugt ein Pseudoviereck, dass auch
	 * einen oder zwei Boegen anstelle von Segmenten enthalten kann.
	 * 
	 * @author Darius Geiss
	 */
	public class PseudoTrapezoid2
			implements Drawable
	{

		private byte		DOWN	= 1;
		private byte		UP		= 2;

		/**
		 * Eckpunkte des Vierecks
		 */
		private Point2		A, B, C, D;

		/**
		 * Objekte des Vierecks
		 */
		private SimpleList	items;


		/**
		 * Erzeugt ein leeres pseudo Viereck
		 */
		public PseudoTrapezoid2()
		{
			clear();
		}


		/**
		 * Speichert eine Kopie der Liste (die Liste wird geklont).
		 * 
		 * @param objectList
		 */
		public PseudoTrapezoid2(
				SimpleList objectList)
		{
			clear();
			items = objectList.copyList();
		}


		/**
		 * Die alte Objektliste wird geloescht und durch eine Kopie der neuen
		 * Liste ersetzt.
		 * 
		 * @param objectList
		 *            Die neue Objektliste
		 */
		public void setPseudoTrapezoid(
				SimpleList objectList)
		{
			clear();
			items = objectList.copyList();
		}


		/**
		 * Liefert die Liste der Objekte, aus denen das pseudo Viereck besteht.
		 * 
		 * @return SimpleList der Objekte
		 */
		public SimpleList getPseudoTrapezoid()
		{
			return items;
		}


		/**
		 * Zeichnet alle zeichenbaren Objekte des Vierecks
		 * 
		 * @param graphics
		 *            Das Grafikobjekt, in das gezeichnet wird
		 * @param graphicsContext
		 *            Die entsprechenden Formatierungsangaben
		 */
		public void draw(
				Graphics2D graphics,
				GraphicsContext graphicsContext)
		{
			if (items != null)
			{
				GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
				byte direction = DOWN;
				SimpleList arcs = new SimpleList();
				Arc2D arc = null;
				Point2 arcS = null, arcT = null;
				Ellipse2D elip = null;
				boolean first = true;
				for (ListItem lItem = items.first(); lItem != null; lItem = lItem
						.next())
				{
					if (lItem.value() instanceof VerticalSegment)
					{
						VerticalSegment vs = (VerticalSegment) lItem.value();
						if (direction == DOWN)
						{
							if (first == true)
							{
								path.moveTo(vs.getUpperPoint().x,
										vs.getUpperPoint().y);
								first = false;
							}
							path.lineTo(vs.getLowerPoint().x,
									vs.getLowerPoint().y);
						}
						else
						{
							if (first == true)
							{
								path.moveTo(vs.getLowerPoint().x,
										vs.getLowerPoint().y);
								first = false;
							}
							path.lineTo(vs.getUpperPoint().x,
									vs.getUpperPoint().y);
						}
					}
					else if (lItem.value() instanceof Arc2ExtModified)
					{
						Arc2ExtModified a = (Arc2ExtModified) lItem.value();
						arc = new Arc2D.Double();
						arcS = a.source();
						arcT = a.target();
						if (direction == UP)
						{
							elip = new Ellipse2D.Float(a.centre.x - a.radius,
									a.centre.y - a.radius, 2 * a.radius,
									2 * a.radius);
							if (first == true)
							{
								path.moveTo(a.target().x, a.target().y);
								first = false;
							}
							path.lineTo(a.source().x, a.source().y);
						}
						else
						{
							elip = new Ellipse2D.Float(a.centre.x - a.radius,
									a.centre.y - a.radius, 2 * a.radius,
									2 * a.radius);
							if (first == true)
							{
								path.moveTo(a.source().x, a.source().y);
								first = false;
							}
							path.lineTo(a.source().x, a.source().y);
						}
						arcs.add(elip);
						if (direction == DOWN)
							direction = UP;
						else
							direction = DOWN;
					}
					else if (lItem.value() instanceof Segment2Ext)
					{
						Segment2Ext s = (Segment2Ext) lItem.value();
						if (direction == UP)
						{
							if (first == true)
							{
								path.moveTo(s.getRightPoint().x,
										s.getRightPoint().y);
								first = false;
							}
							path.lineTo(s.getLeftPoint().x, s.getLeftPoint().y);
						}
						else
						{
							if (first == true)
							{
								path.moveTo(s.getLeftPoint().x,
										s.getLeftPoint().y);
								first = false;
							}
							path.lineTo(s.getRightPoint().x,
									s.getRightPoint().y);
						}
						if (direction == DOWN)
							direction = UP;
						else
							direction = DOWN;
					}
				}
				path.closePath();
				Area area = new Area(path);
				for (ListItem lItem = arcs.first(); lItem != null; lItem = lItem
						.next())
				{
					Area area2 = new Area((Shape) lItem.value());
					area.subtract(area2);
				}
				graphics.setStroke(new BasicStroke(0.1f));
				graphics.setColor(new Color(230, 230, 230));
				graphics.fill(area);
				graphics.setColor(Color.black);
				graphics.draw(area);
			}
		}


		/**
		 * Liefert eine einfache Triangulation
		 * 
		 * @param left
		 *            Segmente links
		 * @param bottom
		 *            Objekte unten
		 * @param right
		 *            Segmente rechts
		 * @param top
		 *            Objekte oben
		 * @param toDo
		 *            Liste
		 * 
		 * @return Ergebnis
		 */
		private SimpleList simpleTriangulation(
				VerticalSegment left,
				Object bottom,
				VerticalSegment right,
				Object top,
				List toDo)
		{
			SimpleList listOfTriangles = new SimpleList();
			if ((bottom instanceof Segment2Ext) && (top instanceof Segment2Ext))
			{
				Point2 pLB = ((Segment2Ext) bottom).getLeftPoint();
				Point2 pRB = ((Segment2Ext) bottom).getRightPoint();
				Point2 pLT = ((Segment2Ext) top).getLeftPoint();
				Point2 pRT = ((Segment2Ext) top).getRightPoint();
				// Dreiecke erzeugen
				Segment2Ext newSeg;
				PseudoTriangle2 pt1;
				PseudoTriangle2 pt2;
				if (pLB.distance(pRT) < pRB.distance(pLT))
				{
					newSeg = new Segment2Ext(pLB, pRT);
					SimpleList list = new SimpleList();
					list.add(right);
					list.add(newSeg);
					list.add(bottom);
					pt1 = new PseudoTriangle2(list);
					list.clear();
					list.add(left);
					list.add(newSeg);
					list.add(top);
					pt2 = new PseudoTriangle2(list);
				}
				else
				{
					newSeg = new Segment2Ext(pLT, pRB);
					SimpleList list = new SimpleList();
					list.add(left);
					list.add(bottom);
					list.add(newSeg);
					pt2 = new PseudoTriangle2(list);
					list.clear();
					list.add(right);
					list.add(top);
					list.add(newSeg);
					pt1 = new PseudoTriangle2(list);
				}
				// Beziehungen erzeugen
				newSeg.add_PseudoTriangle(pt1);
				newSeg.add_PseudoTriangle(pt2);
				left.setPseudoTriaRight(pt2);
				right.setPseudoTriaLeft(pt1);
				listOfTriangles.add(pt1);
				listOfTriangles.add(pt2);
			}
			else
			{
				if (bottom instanceof Arc2ExtModified)
				{
					Point2 pLB = new Point2();
					Point2 pRB = new Point2();
					Angle pLB_A, pRB_A;
					if (((Point2) ((Arc2ExtModified) bottom).source())
							.isSmaller(((Arc2ExtModified) bottom).target()))
					{
						pLB = ((Arc2ExtModified) bottom).source();
						pLB_A = new Angle(
								((Arc2ExtModified) bottom).sourceAngle()
										- Angle.PI_DIV_2);
						pRB = ((Arc2ExtModified) bottom).target();
						pRB_A = new Angle(
								((Arc2ExtModified) bottom).targetAngle()
										+ Angle.PI_DIV_2);
					}
					else
					{
						pLB = ((Arc2ExtModified) bottom).target();
						pLB_A = new Angle(
								((Arc2ExtModified) bottom).targetAngle()
										- Angle.PI_DIV_2);
						pRB = ((Arc2ExtModified) bottom).source();
						pRB_A = new Angle(
								((Arc2ExtModified) bottom).sourceAngle()
										+ Angle.PI_DIV_2);
					}
					if (top instanceof Arc2ExtModified)
					{
						Arc2ExtModified _a1 = (Arc2ExtModified) bottom;
						Arc2ExtModified _a2 = (Arc2ExtModified) top;
						double distA1toA2 = _a1.centre.distance(_a2.centre);
						double R1plusR2 = _a1.radius + _a2.radius;
						double angleT = Math.acos(Math
								.min(R1plusR2, distA1toA2)
								/ Math.max(R1plusR2, distA1toA2));
						double angleA1toA2 = (new LimitedAngle(
								_a1.centre.angle(_a2.centre),
								LimitedAngle.CIRCLE_ABS)).rad();
						double angleA2toA1 = (new LimitedAngle(
								_a2.centre.angle(_a1.centre),
								LimitedAngle.CIRCLE_ABS)).rad();
						double angleT_At_A = angleA1toA2 - angleT;
						double angleT_At_D = angleA1toA2 + angleT;
						double angleT_At_C = angleA2toA1 - angleT;
						double angleT_At_B = angleA2toA1 + angleT;
						double angleB = (_a2.sourceAngle() > _a2.targetAngle()) ? angleB = _a2
								.targetAngle() + Angle.PI * 2
								: _a2.targetAngle();

						int check_A = (angleT_At_A <= _a1.sourceAngle()) ? 1
								: 2;
						int check_C = (angleT_At_C <= _a2.sourceAngle()) ? 1
								: 2;
						int check_B = (angleT_At_B >= angleB) ? 1 : 2;
						int check_D = (angleT_At_D >= _a1.targetAngle()) ? 1
								: 2;

						if ((check_A == 1) && (check_C == 2))
						{
							double distA2toA = _a2.centre
									.distance(_a1.source());
							double angleA2toA = (new LimitedAngle(
									_a2.centre.angle(_a1.source()),
									LimitedAngle.CIRCLE_ABS)).rad();
							double angleT_C = angleA2toA
									- Math.acos(_a2.radius / distA2toA);
							if (angleT_C < _a2.sourceAngle())
								check_C = 1;
						}
						if ((check_A == 2) && (check_C == 1))
						{
							double distA1toC = _a1.centre
									.distance(_a2.source());
							double angleA1toC = (new LimitedAngle(
									_a1.centre.angle(_a2.source()),
									LimitedAngle.CIRCLE_ABS)).rad();
							double angleT_A = angleA1toC
									- Math.acos(_a1.radius / distA1toC);
							if (angleT_A < _a1.sourceAngle())
								check_A = 1;
						}
						if ((check_B == 1) && (check_D == 2))
						{
							double distA1toB = _a1.centre
									.distance(_a2.target());
							double angleA1toB = (new LimitedAngle(
									_a1.centre.angle(_a2.target()),
									LimitedAngle.CIRCLE_ABS)).rad();
							double angleT_D = angleA1toB
									+ Math.acos(_a1.radius / distA1toB);
							if (angleT_D > _a1.targetAngle())
								check_D = 1;
						}
						if ((check_B == 2) && (check_D == 1))
						{
							double distA2toD = _a2.centre
									.distance(_a1.target());
							double angleA2toD = (new LimitedAngle(
									_a2.centre.angle(_a1.target()),
									LimitedAngle.CIRCLE_ABS)).rad();
							double angleT_B = angleA2toD
									+ Math.acos(_a2.radius / distA2toD);
							if (angleT_B > angleB)
								check_B = 1;
						}

						if ((check_A + check_C) <= (check_B + check_D))
						{ // Welche Eckpunktekombination ist guenstiger (Loesung mit weniger Dreiecken)
							// (A,C) guenstiger bzw. gleich (B,D). (A,C) wird benutzt.
							if ((check_A + check_C) == 2)
							{ // Loesung mit zwei Dreiecken (A,C) kann direkt verbunden werden
								// Dreiecke erzeugen
								Segment2Ext newSeg = new Segment2Ext(
										_a1.source(), _a2.source());
								SimpleList list = new SimpleList();
								list.add(right);
								list.add(_a2);
								list.add(newSeg);
								PseudoTriangle2 pt1 = new PseudoTriangle2(list);
								list.clear();
								list.add(left);
								list.add(_a1);
								list.add(newSeg);
								PseudoTriangle2 pt2 = new PseudoTriangle2(list);
								// Beziehungen erzeugen
								newSeg.add_PseudoTriangle(pt1);
								newSeg.add_PseudoTriangle(pt2);
								left.setPseudoTriaRight(pt2);
								right.setPseudoTriaLeft(pt1);
								listOfTriangles.add(pt1);
								listOfTriangles.add(pt2);
							}
							else if ((check_A + check_C) == 4)
							{ // Loesung mit vier Dreiecken
								// _a1 und _a2 muessen jeweils geteilt werden
								Arc2ExtModified split1_A1 = new Arc2ExtModified(
										_a1.centre, _a1.radius,
										_a1.sourceAngle(), angleT_At_A,
										Angle.ORIENTATION_LEFT);
								Arc2ExtModified split2_A1 = new Arc2ExtModified(
										_a1.centre, _a1.radius, angleT_At_A,
										_a1.targetAngle(),
										Angle.ORIENTATION_LEFT);
								Arc2ExtModified split1_A2 = new Arc2ExtModified(
										_a2.centre, _a2.radius,
										_a2.sourceAngle(), angleT_At_C,
										Angle.ORIENTATION_LEFT);
								Arc2ExtModified split2_A2 = new Arc2ExtModified(
										_a2.centre, _a2.radius, angleT_At_C,
										_a2.targetAngle(),
										Angle.ORIENTATION_LEFT);
								// Beruehrpunkte der Boegen mit der Tangete
								Point2 point1OfT = split1_A1.target();
								Point2 point2OfT = split1_A2.target();
								// Tangente der Kreise
								Line2 tangente = new Line2(point1OfT, point2OfT);
								// Schnittpunkte der Tangente mit (A,B) und (C,D)
								Intersection inout_set = new Intersection();
								Segment2Ext _s1 = new Segment2Ext(_a1.source(),
										_a2.target());
								Segment2Ext _s2 = new Segment2Ext(_a2.source(),
										_a1.target());
								Point2 sp_AB_T = _s1.intersection(tangente,
										inout_set);
								Point2 sp_CD_T = _s2.intersection(tangente,
										inout_set);
								// Neue Segmente erzeugen
								Segment2Ext midPartOfT = new Segment2Ext(
										point1OfT, point2OfT);
								Segment2Ext leftPartOfT = new Segment2Ext(
										sp_CD_T, point2OfT);
								Segment2Ext rightPartOfT = new Segment2Ext(
										point1OfT, sp_AB_T);
								VerticalSegment splitVSRightTop = new VerticalSegment(
										right.getUpperPoint(), sp_AB_T, null,
										null, null, null);
								VerticalSegment splitVSRightBottom = new VerticalSegment(
										sp_AB_T, right.getLowerPoint(), null,
										null, null, null);
								VerticalSegment splitVSLeftTop = new VerticalSegment(
										left.getUpperPoint(), sp_CD_T, null,
										null, null, null);
								VerticalSegment splitVSLeftBottom = new VerticalSegment(
										sp_CD_T, left.getLowerPoint(), null,
										null, null, null);
								//Setzen der Punktreferenzen fuer die Boegen
								split1_A1.setRefTraget(point1OfT);
								split1_A1.setRefSource(right.getLowerPoint());
								split2_A1.setRefSource(point1OfT);
								split2_A1.setRefTraget(left.getLowerPoint());
								split1_A2.setRefTraget(point2OfT);
								split1_A2.setRefSource(left.getUpperPoint());
								split2_A2.setRefSource(point2OfT);
								split2_A2.setRefTraget(right.getUpperPoint());
								// Dreiecke erzeugen
								SimpleList list = new SimpleList();
								list.add(splitVSRightBottom);
								list.add(rightPartOfT);
								list.add(split1_A1);
								PseudoTriangle2 pt1 = new PseudoTriangle2(list);
								list.clear();
								list.add(splitVSLeftBottom);
								list.add(split2_A1);
								list.add(midPartOfT);
								list.add(leftPartOfT);
								PseudoTriangle2 pt2 = new PseudoTriangle2(list);
								list.clear();
								list.add(splitVSLeftTop);
								list.add(leftPartOfT);
								list.add(split1_A2);
								PseudoTriangle2 pt3 = new PseudoTriangle2(list);
								list.clear();
								list.add(splitVSRightTop);
								list.add(split2_A2);
								list.add(midPartOfT);
								list.add(rightPartOfT);
								PseudoTriangle2 pt4 = new PseudoTriangle2(list);
								listOfTriangles.add(pt1);
								//listOfTriangles.add(pt2);
								listOfTriangles.add(pt3);
								//listOfTriangles.add(pt4);
								// Beziehungen erzeugen
								midPartOfT.add_PseudoTriangle(pt2);
								midPartOfT.add_PseudoTriangle(pt4);
								leftPartOfT.add_PseudoTriangle(pt2);
								leftPartOfT.add_PseudoTriangle(pt3);
								rightPartOfT.add_PseudoTriangle(pt1);
								rightPartOfT.add_PseudoTriangle(pt4);
								splitVSRightTop.setPseudoTriaLeft(pt4);
								splitVSRightBottom.setPseudoTriaLeft(pt1);
								splitVSLeftTop.setPseudoTriaRight(pt3);
								splitVSLeftBottom.setPseudoTriaRight(pt2);
								// Dreiecke mit vier Objekten muessen erneut geteilt werden
								PseudoTriangle2[] ptArray = pt2
										.triangulation(point2OfT);
								for (int i = 0; i < ptArray.length; i++)
								{
									listOfTriangles.add(ptArray[i]);
								}
								ptArray = pt4.triangulation(point1OfT);
								for (int i = 0; i < ptArray.length; i++)
								{
									listOfTriangles.add(ptArray[i]);
								}
								// Arbeit nach der Triangulation
								VerticalSegment[] vsArray = new VerticalSegment[3];
								vsArray[0] = (VerticalSegment) right;
								vsArray[1] = splitVSRightTop;
								vsArray[2] = splitVSRightBottom;
								toDo.add(sp_AB_T, vsArray);

								vsArray = new VerticalSegment[3];
								vsArray[0] = (VerticalSegment) left;
								vsArray[1] = splitVSLeftTop;
								vsArray[2] = splitVSLeftBottom;
								toDo.add(sp_CD_T, vsArray);

								//toDo.add(sp_AB_T, right);
								//toDo.add(sp_CD_T, left);
							}
							else
							{ // Loesung mit drei bzw zwei Dreiecken
								if (check_C == 1)
								{ // Tangente an _a1 geht durch C
									double distA1toC = _a1.centre.distance(_a2
											.source());
									double angleA1toC = (new LimitedAngle(
											_a1.centre.angle(_a2.source()),
											LimitedAngle.CIRCLE_ABS)).rad();
									double angleT_A = angleA1toC
											- Math.acos(_a1.radius / distA1toC);
									if (angleT_A >= _a1.sourceAngle())
									{
										// _a1 muss geteilt werden
										Arc2ExtModified split1_A1 = new Arc2ExtModified(
												_a1.centre, _a1.radius,
												_a1.sourceAngle(), angleT_A,
												Angle.ORIENTATION_LEFT);
										Arc2ExtModified split2_A1 = new Arc2ExtModified(
												_a1.centre, _a1.radius,
												angleT_A, _a1.targetAngle(),
												Angle.ORIENTATION_LEFT);
										// Beruehrpunkt der Tangente an _a1
										Point2 point1OfT = split1_A1.target();
										// Tangente an _a1 durch C
										Line2 tangente = new Line2(point1OfT,
												_a2.source());
										// Schnittpunkt der Tangente mit (A,B)
										Intersection inout_set = new Intersection();
										Segment2Ext _s1 = new Segment2Ext(
												_a1.source(), _a2.target());
										Point2 sp_AB_T = _s1.intersection(
												tangente, inout_set);
										// Neue Segmente erzeugen
										Segment2Ext leftPartOfT = new Segment2Ext(
												_a2.source(), point1OfT);
										Segment2Ext rightPartOfT = new Segment2Ext(
												point1OfT, sp_AB_T);
										VerticalSegment splitVSRightTop = new VerticalSegment(
												right.getUpperPoint(), sp_AB_T,
												null, null, null, null);
										VerticalSegment splitVSRightBottom = new VerticalSegment(
												sp_AB_T, right.getLowerPoint(),
												null, null, null, null);
										//Setzen der Punktreferenzen fuer die Boegen
										split1_A1.setRefTraget(point1OfT);
										split1_A1.setRefSource(right
												.getLowerPoint());
										split2_A1.setRefSource(point1OfT);
										split2_A1.setRefTraget(left
												.getLowerPoint());
										// Dreiecke erzeugen
										SimpleList list = new SimpleList();
										list.add(splitVSRightBottom);
										list.add(rightPartOfT);
										list.add(split1_A1);
										PseudoTriangle2 pt1 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(left);
										list.add(split2_A1);
										list.add(leftPartOfT);
										PseudoTriangle2 pt2 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(splitVSRightTop);
										list.add(_a2);
										list.add(leftPartOfT);
										list.add(rightPartOfT);
										PseudoTriangle2 pt3 = new PseudoTriangle2(
												list);
										listOfTriangles.add(pt1);
										listOfTriangles.add(pt2);
										//listOfTriangles.add(pt3);
										// Beziehungen erzeugen
										leftPartOfT.add_PseudoTriangle(pt2);
										leftPartOfT.add_PseudoTriangle(pt3);
										rightPartOfT.add_PseudoTriangle(pt1);
										rightPartOfT.add_PseudoTriangle(pt3);
										splitVSRightTop.setPseudoTriaLeft(pt3);
										splitVSRightBottom
												.setPseudoTriaLeft(pt1);
										left.setPseudoTriaRight(pt2);
										// Dreiecke mit vier Objekten muessen erneut geteilt werden
										PseudoTriangle2[] ptArray = pt3
												.triangulation(point1OfT);
										for (int i = 0; i < ptArray.length; i++)
										{
											listOfTriangles.add(ptArray[i]);
										}
										// Arbeit nach der Triangulation
										VerticalSegment[] vsArray = new VerticalSegment[3];
										vsArray[0] = right;
										vsArray[1] = splitVSRightTop;
										vsArray[2] = splitVSRightBottom;
										toDo.add(sp_AB_T, vsArray);

										//toDo.add(sp_AB_T, right);
									}
									else
									{
										// Eine Loesung mit zwei Dreiecken ist moeglich.
										Segment2Ext newSeg = new Segment2Ext(
												_a1.source(), _a2.source());
										// Dreiecke erzeugen
										SimpleList list = new SimpleList();
										list.add(right);
										list.add(_a2);
										list.add(newSeg);
										PseudoTriangle2 pt1 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(left);
										list.add(_a1);
										list.add(newSeg);
										PseudoTriangle2 pt2 = new PseudoTriangle2(
												list);
										newSeg.add_PseudoTriangle(pt1);
										newSeg.add_PseudoTriangle(pt2);
										listOfTriangles.add(pt1);
										listOfTriangles.add(pt2);
										// Beziehungen erzeugen
										newSeg.add_PseudoTriangle(pt1);
										newSeg.add_PseudoTriangle(pt2);
										left.setPseudoTriaRight(pt2);
										right.setPseudoTriaLeft(pt1);
									}
								}
								else
								{ // Tangente an _a2 geht durch A
									double distA2toA = _a2.centre.distance(_a1
											.source());
									double angleA2toA = (new LimitedAngle(
											_a2.centre.angle(_a1.source()),
											LimitedAngle.CIRCLE_ABS)).rad();
									double angleT_C = angleA2toA
											- Math.acos(_a2.radius / distA2toA);
									if (angleT_C >= _a2.sourceAngle())
									{
										// _a2 muss geteilt werden
										Arc2ExtModified split1_A2 = new Arc2ExtModified(
												_a2.centre, _a2.radius,
												_a2.sourceAngle(), angleT_C,
												Angle.ORIENTATION_LEFT);
										Arc2ExtModified split2_A2 = new Arc2ExtModified(
												_a2.centre, _a2.radius,
												angleT_C, _a2.targetAngle(),
												Angle.ORIENTATION_LEFT);
										// Beruehrpunkt der Tangente an _a2
										Point2 point1OfT = split1_A2.target();
										// Tangente an _a2 durch A
										Line2 tangente = new Line2(point1OfT,
												_a1.source());
										// Schnittpunkt der Tangente mit (C,D)
										Intersection inout_set = new Intersection();
										Segment2Ext _s2 = new Segment2Ext(
												_a1.target(), _a2.source());
										Point2 sp_CD_T = _s2.intersection(
												tangente, inout_set);
										// Neue Segmente erzeugen
										Segment2Ext leftPartOfT = new Segment2Ext(
												sp_CD_T, point1OfT);
										Segment2Ext rightPartOfT = new Segment2Ext(
												point1OfT, _a1.source());
										VerticalSegment splitVSLeftTop = new VerticalSegment(
												left.getUpperPoint(), sp_CD_T,
												null, null, null, null);
										VerticalSegment splitVSLeftBottom = new VerticalSegment(
												sp_CD_T, left.getLowerPoint(),
												null, null, null, null);
										//Setzen der Punktreferenzen fuer die Boegen
										split1_A2.setRefTraget(point1OfT);
										split1_A2.setRefSource(left
												.getUpperPoint());
										split2_A2.setRefSource(point1OfT);
										split2_A2.setRefTraget(right
												.getUpperPoint());
										// Dreiecke erzeugen
										SimpleList list = new SimpleList();
										list.add(splitVSLeftTop);
										list.add(leftPartOfT);
										list.add(split1_A2);
										PseudoTriangle2 pt1 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(right);
										list.add(split2_A2);
										list.add(rightPartOfT);
										PseudoTriangle2 pt2 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(splitVSLeftBottom);
										list.add(_a1);
										list.add(rightPartOfT);
										list.add(leftPartOfT);
										PseudoTriangle2 pt3 = new PseudoTriangle2(
												list);
										listOfTriangles.add(pt1);
										listOfTriangles.add(pt2);
										//listOfTriangles.add(pt3);
										// Beziehungen erzeugen
										leftPartOfT.add_PseudoTriangle(pt1);
										leftPartOfT.add_PseudoTriangle(pt3);
										rightPartOfT.add_PseudoTriangle(pt2);
										rightPartOfT.add_PseudoTriangle(pt3);
										splitVSLeftTop.setPseudoTriaRight(pt1);
										splitVSLeftBottom
												.setPseudoTriaRight(pt3);
										right.setPseudoTriaLeft(pt2);
										// Dreiecke mit vier Objekten muessen erneut geteilt werden
										PseudoTriangle2[] ptArray = pt3
												.triangulation(point1OfT);
										for (int i = 0; i < ptArray.length; i++)
										{
											listOfTriangles.add(ptArray[i]);
										}
										// Arbeit nach der Triangulation
										VerticalSegment[] vsArray = new VerticalSegment[3];
										vsArray[0] = left;
										vsArray[1] = splitVSLeftTop;
										vsArray[2] = splitVSLeftBottom;
										toDo.add(sp_CD_T, vsArray);

										//toDo.add(sp_CD_T, left);
									}
									else
									{
										// Eine Loesung mit zwei Dreiecken ist moeglich.
										Segment2Ext newSeg = new Segment2Ext(
												_a1.source(), _a2.source());
										// Dreiecke erzeugen
										SimpleList list = new SimpleList();
										list.add(right);
										list.add(_a2);
										list.add(newSeg);
										PseudoTriangle2 pt1 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(left);
										list.add(_a1);
										list.add(newSeg);
										PseudoTriangle2 pt2 = new PseudoTriangle2(
												list);
										listOfTriangles.add(pt1);
										listOfTriangles.add(pt2);
										// Beziehungen erzeugen
										newSeg.add_PseudoTriangle(pt1);
										newSeg.add_PseudoTriangle(pt2);
										left.setPseudoTriaRight(pt2);
										right.setPseudoTriaLeft(pt1);
									}
								}
							}
						}
						else
						{
							// (B,D) guenstiger als (A,C).
							if ((check_B + check_D) == 2)
							{ // Loesung mit zwei Dreiecken (B,D) kann direkt verbunden werden
								Segment2Ext newSeg = new Segment2Ext(
										_a1.target(), _a2.target());
								// Dreiecke erzeugen
								SimpleList list = new SimpleList();
								list.add(left);
								list.add(newSeg);
								list.add(_a2);
								PseudoTriangle2 pt1 = new PseudoTriangle2(list);
								list.clear();
								list.add(right);
								list.add(newSeg);
								list.add(_a1);
								PseudoTriangle2 pt2 = new PseudoTriangle2(list);
								listOfTriangles.add(pt1);
								listOfTriangles.add(pt2);
								// Beziehungen erzeugen
								newSeg.add_PseudoTriangle(pt1);
								newSeg.add_PseudoTriangle(pt2);
								left.setPseudoTriaRight(pt1);
								right.setPseudoTriaLeft(pt2);
							}
							else if ((check_B + check_D) == 4)
							{
								// Loesung mit vier Dreiecken. Sollte jedoch nie benutzt werden, da bei einer gleich schlechten Loesung (A,C) bevorzugt wird.
								System.out
										.println("Fehler: (B,D) mit vier Dreiecken");
							}
							else
							{ // Loesung mit drei bzw zwei Dreiecken
								if (check_B == 1)
								{ // Tangente an _a1 geht durch B
									double distA1toB = _a1.centre.distance(_a2
											.target());
									double angleA1toB = (new LimitedAngle(
											_a1.centre.angle(_a2.target()),
											LimitedAngle.CIRCLE_ABS)).rad();
									double angleT_D = angleA1toB
											+ Math.acos(_a1.radius / distA1toB);
									if (angleT_D <= _a1.targetAngle())
									{
										// _a1 muss geteilt werden
										Arc2ExtModified split1_A1 = new Arc2ExtModified(
												_a1.centre, _a1.radius,
												_a1.sourceAngle(), angleT_D,
												Angle.ORIENTATION_LEFT);
										Arc2ExtModified split2_A1 = new Arc2ExtModified(
												_a1.centre, _a1.radius,
												angleT_D, _a1.targetAngle(),
												Angle.ORIENTATION_LEFT);
										// Beruehrpunkt der Tangente an _a1
										Point2 point1OfT = split1_A1.target();
										// Tangente an _a1 durch B
										Line2 tangente = new Line2(point1OfT,
												_a2.target());
										// Schnittpunkt der Tangente mit (C,D)
										Intersection inout_set = new Intersection();
										Segment2Ext _s2 = new Segment2Ext(
												_a1.target(), _a2.source());
										Point2 sp_CD_T = _s2.intersection(
												tangente, inout_set);
										// Neue Segmente erzeugen
										Segment2Ext leftPartOfT = new Segment2Ext(
												sp_CD_T, point1OfT);
										Segment2Ext rightPartOfT = new Segment2Ext(
												point1OfT, _a2.target());
										VerticalSegment splitVSLeftTop = new VerticalSegment(
												left.getUpperPoint(), sp_CD_T,
												null, null, null, null);
										VerticalSegment splitVSLeftBottom = new VerticalSegment(
												sp_CD_T, left.getLowerPoint(),
												null, null, null, null);
										//Setzen der Punktreferenzen fuer die Boegen
										split1_A1.setRefTraget(point1OfT);
										split1_A1.setRefSource(right
												.getLowerPoint());
										split2_A1.setRefSource(point1OfT);
										split2_A1.setRefTraget(left
												.getLowerPoint());
										// Dreiecke erzeugen
										SimpleList list = new SimpleList();
										list.add(splitVSLeftBottom);
										list.add(split2_A1);
										list.add(leftPartOfT);
										PseudoTriangle2 pt1 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(right);
										list.add(rightPartOfT);
										list.add(split1_A1);
										PseudoTriangle2 pt2 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(splitVSLeftTop);
										list.add(leftPartOfT);
										list.add(rightPartOfT);
										list.add(_a2);
										PseudoTriangle2 pt3 = new PseudoTriangle2(
												list);
										listOfTriangles.add(pt1);
										listOfTriangles.add(pt2);
										//listOfTriangles.add(pt3);
										// Beziehungen erzeugen
										leftPartOfT.add_PseudoTriangle(pt1);
										leftPartOfT.add_PseudoTriangle(pt3);
										rightPartOfT.add_PseudoTriangle(pt2);
										rightPartOfT.add_PseudoTriangle(pt3);
										splitVSLeftTop.setPseudoTriaRight(pt3);
										splitVSLeftBottom
												.setPseudoTriaRight(pt1);
										right.setPseudoTriaLeft(pt2);
										// Dreiecke mit vier Objekten muessen erneut geteilt werden
										PseudoTriangle2[] ptArray = pt3
												.triangulation(point1OfT);
										for (int i = 0; i < ptArray.length; i++)
										{
											listOfTriangles.add(ptArray[i]);
										}
										// Arbeit nach der Triangulation
										VerticalSegment[] vsArray = new VerticalSegment[3];
										vsArray[0] = left;
										vsArray[1] = splitVSLeftTop;
										vsArray[2] = splitVSLeftBottom;
										toDo.add(sp_CD_T, vsArray);

										//toDo.add(sp_CD_T, left);
									}
									else
									{
										// Eine Loesung mit zwei Dreiecken ist moeglich.
										Segment2Ext newSeg = new Segment2Ext(
												_a1.target(), _a2.target());
										// Dreiecke erzeugen
										SimpleList list = new SimpleList();
										list.add(left);
										list.add(newSeg);
										list.add(_a2);
										PseudoTriangle2 pt1 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(right);
										list.add(newSeg);
										list.add(_a1);
										PseudoTriangle2 pt2 = new PseudoTriangle2(
												list);
										listOfTriangles.add(pt1);
										listOfTriangles.add(pt2);
										// Beziehungen erzeugen
										newSeg.add_PseudoTriangle(pt1);
										newSeg.add_PseudoTriangle(pt2);
										left.setPseudoTriaRight(pt1);
										right.setPseudoTriaLeft(pt2);
									}
								}
								else
								{ // Tangente an _a2 geht durch D
									double distA2toD = _a2.centre.distance(_a1
											.target());
									double angleA2toD = (new LimitedAngle(
											_a2.centre.angle(_a1.target()),
											LimitedAngle.CIRCLE_ABS)).rad();
									double angleT_B = angleA2toD
											+ Math.acos(_a2.radius / distA2toD);
									if (angleT_B <= _a2.targetAngle())
									{
										// _a2 muss geteilt werden
										Arc2ExtModified split1_A2 = new Arc2ExtModified(
												_a2.centre, _a2.radius,
												_a2.sourceAngle(), angleT_B,
												Angle.ORIENTATION_LEFT);
										Arc2ExtModified split2_A2 = new Arc2ExtModified(
												_a2.centre, _a2.radius,
												angleT_B, _a2.targetAngle(),
												Angle.ORIENTATION_LEFT);
										// Beruehrpunkt der Tangente an _a2
										Point2 point1OfT = split1_A2.target();
										// Tangente an _a2 durch D
										Line2 tangente = new Line2(point1OfT,
												_a1.target());
										// Schnittpunkt der Tangente mit (A,B)
										Intersection inout_set = new Intersection();
										Segment2Ext _s1 = new Segment2Ext(
												_a1.source(), _a2.target());
										Point2 sp_AB_T = _s1.intersection(
												tangente, inout_set);
										// Neue Segmente erzeugen
										Segment2Ext leftPartOfT = new Segment2Ext(
												_a1.target(), point1OfT);
										Segment2Ext rightPartOfT = new Segment2Ext(
												point1OfT, sp_AB_T);
										VerticalSegment splitVSRightTop = new VerticalSegment(
												right.getUpperPoint(), sp_AB_T,
												null, null, null, null);
										VerticalSegment splitVSRightBottom = new VerticalSegment(
												sp_AB_T, right.getLowerPoint(),
												null, null, null, null);
										//Setzen der Punktreferenzen fuer die Boegen
										split1_A2.setRefTraget(point1OfT);
										split1_A2.setRefSource(left
												.getUpperPoint());
										split2_A2.setRefSource(point1OfT);
										split2_A2.setRefTraget(right
												.getUpperPoint());
										// Dreiecke erzeugen
										SimpleList list = new SimpleList();
										list.add(left);
										list.add(leftPartOfT);
										list.add(split1_A2);
										PseudoTriangle2 pt1 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(splitVSRightTop);
										list.add(split2_A2);
										list.add(rightPartOfT);
										PseudoTriangle2 pt2 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(splitVSRightBottom);
										list.add(rightPartOfT);
										list.add(leftPartOfT);
										list.add(_a2);
										PseudoTriangle2 pt3 = new PseudoTriangle2(
												list);
										listOfTriangles.add(pt1);
										listOfTriangles.add(pt2);
										//listOfTriangles.add(pt3);
										// Beziehungen erzeugen
										leftPartOfT.add_PseudoTriangle(pt1);
										leftPartOfT.add_PseudoTriangle(pt3);
										rightPartOfT.add_PseudoTriangle(pt2);
										rightPartOfT.add_PseudoTriangle(pt3);
										splitVSRightTop.setPseudoTriaLeft(pt2);
										splitVSRightBottom
												.setPseudoTriaLeft(pt3);
										left.setPseudoTriaRight(pt1);
										// Dreiecke mit vier Objekten muessen erneut geteilt werden
										PseudoTriangle2[] ptArray = pt3
												.triangulation(point1OfT);
										for (int i = 0; i < ptArray.length; i++)
										{
											listOfTriangles.add(ptArray[i]);
										}
										// Arbeit nach der Triangulation
										VerticalSegment[] vsArray = new VerticalSegment[3];
										vsArray[0] = right;
										vsArray[1] = splitVSRightTop;
										vsArray[2] = splitVSRightBottom;
										toDo.add(sp_AB_T, vsArray);

										//toDo.add(sp_AB_T, right);
									}
									else
									{
										// Eine Loesung mit zwei Dreiecken ist moeglich.
										Segment2Ext newSeg = new Segment2Ext(
												_a1.target(), _a2.target());
										// Dreiecke erzeugen
										SimpleList list = new SimpleList();
										list.add(left);
										list.add(newSeg);
										list.add(_a2);
										PseudoTriangle2 pt1 = new PseudoTriangle2(
												list);
										list.clear();
										list.add(right);
										list.add(newSeg);
										list.add(_a1);
										PseudoTriangle2 pt2 = new PseudoTriangle2(
												list);
										listOfTriangles.add(pt1);
										listOfTriangles.add(pt2);
										// Beziehungen erzeugen
										newSeg.add_PseudoTriangle(pt1);
										newSeg.add_PseudoTriangle(pt2);
										left.setPseudoTriaRight(pt1);
										right.setPseudoTriaLeft(pt2);
									}
								}
							}
						}
					}
					else if (top instanceof Segment2Ext)
					{
						Arc2ExtModified _a1 = (Arc2ExtModified) bottom;
						Point2 pLT = ((Segment2Ext) top).getLeftPoint();
						Point2 pRT = ((Segment2Ext) top).getRightPoint();
						// tangenten an _a1 durch B und C
						double distA1toB = _a1.centre.distance(pRT);
						double angleA1toB = (new LimitedAngle(
								_a1.centre.angle(pRT), LimitedAngle.CIRCLE_ABS))
								.rad();
						double angleT_D = angleA1toB
								+ Math.acos(_a1.radius / distA1toB);

						double distA1toC = _a1.centre.distance(pLT);
						double angleA1toC = (new LimitedAngle(
								_a1.centre.angle(pLT), LimitedAngle.CIRCLE_ABS))
								.rad();
						double angleT_A = angleA1toC
								- Math.acos(_a1.radius / distA1toC);

						if ((_a1.targetAngle() <= angleT_D)
								&& (_a1.sourceAngle() >= angleT_A))
						{
							// es sind zwei Loesungen mit zwei Dreiecken moeglich
							if (pLB.distance(pRT) < pRB.distance(pLT))
							{
								// Eine Loesung mit zwei Dreiecken ist moeglich. (B,D)
								Segment2Ext newSeg = new Segment2Ext(pLB, pRT);
								// Dreiecke erzeugen
								SimpleList list = new SimpleList();
								list.add(right);
								list.add(newSeg);
								list.add(bottom);
								PseudoTriangle2 pt1 = new PseudoTriangle2(list);
								list.clear();
								list.add(left);
								list.add(newSeg);
								list.add(top);
								PseudoTriangle2 pt2 = new PseudoTriangle2(list);
								listOfTriangles.add(pt1);
								listOfTriangles.add(pt2);
								// Beziehungen erzeugen
								newSeg.add_PseudoTriangle(pt1);
								newSeg.add_PseudoTriangle(pt2);
								left.setPseudoTriaRight(pt2);
								right.setPseudoTriaLeft(pt1);
							}
							else
							{
								// Eine Loesung mit zwei Dreiecken ist moeglich. (A,C)
								Segment2Ext newSeg = new Segment2Ext(pRB, pLT);
								// Dreiecke erzeugen
								SimpleList list = new SimpleList();
								list.add(left);
								list.add(bottom);
								list.add(newSeg);
								PseudoTriangle2 pt1 = new PseudoTriangle2(list);
								list.clear();
								list.add(right);
								list.add(top);
								list.add(newSeg);
								PseudoTriangle2 pt2 = new PseudoTriangle2(list);
								listOfTriangles.add(pt1);
								listOfTriangles.add(pt2);
								// Beziehungen erzeugen
								newSeg.add_PseudoTriangle(pt1);
								newSeg.add_PseudoTriangle(pt2);
								left.setPseudoTriaRight(pt1);
								right.setPseudoTriaLeft(pt2);
							}
						}
						else if (_a1.targetAngle() <= angleT_D)
						{
							// Loesung mit zwei Dreiecken (B,D)
							Segment2Ext newSeg = new Segment2Ext(pLB, pRT);
							// Dreiecke erzeugen
							SimpleList list = new SimpleList();
							list.add(right);
							list.add(newSeg);
							list.add(bottom);
							PseudoTriangle2 pt1 = new PseudoTriangle2(list);
							list.clear();
							list.add(left);
							list.add(newSeg);
							list.add(top);
							PseudoTriangle2 pt2 = new PseudoTriangle2(list);
							listOfTriangles.add(pt1);
							listOfTriangles.add(pt2);
							// Beziehungen erzeugen
							newSeg.add_PseudoTriangle(pt1);
							newSeg.add_PseudoTriangle(pt2);
							left.setPseudoTriaRight(pt2);
							right.setPseudoTriaLeft(pt1);
						}
						else if (_a1.sourceAngle() >= angleT_A)
						{
							// Loesung mit zwei Dreiecken (A,C)
							Segment2Ext newSeg = new Segment2Ext(pRB, pLT);
							// Dreiecke erzeugen
							SimpleList list = new SimpleList();
							list.add(left);
							list.add(bottom);
							list.add(newSeg);
							PseudoTriangle2 pt1 = new PseudoTriangle2(list);
							list.clear();
							list.add(right);
							list.add(top);
							list.add(newSeg);
							PseudoTriangle2 pt2 = new PseudoTriangle2(list);
							listOfTriangles.add(pt1);
							listOfTriangles.add(pt2);
							// Beziehungen erzeugen
							newSeg.add_PseudoTriangle(pt1);
							newSeg.add_PseudoTriangle(pt2);
							left.setPseudoTriaRight(pt1);
							right.setPseudoTriaLeft(pt2);
						}
						else
						{
							// Eine Loesung mit drei Dreiecken ist noetig
							if (pLB.distance(pRT) < pRB.distance(pLT))
							{
								// (B,D)
								// _a1 muss geteilt werden
								Arc2ExtModified split1_A1 = new Arc2ExtModified(
										_a1.centre, _a1.radius,
										_a1.sourceAngle(), angleT_D,
										Angle.ORIENTATION_LEFT);
								Arc2ExtModified split2_A1 = new Arc2ExtModified(
										_a1.centre, _a1.radius, angleT_D,
										_a1.targetAngle(),
										Angle.ORIENTATION_LEFT);
								// Beruehrpunkt der Tangente an _a1
								Point2 point1OfT = split1_A1.target();
								// Tangente an _a1 durch B
								Line2 tangente = new Line2(point1OfT, pRT);
								// Schnittpunkt der Tangente mit (C,D)
								Intersection inout_set = new Intersection();
								Segment2Ext _s2 = new Segment2Ext(pLB, pLT);
								Point2 sp_CD_T = _s2.intersection(tangente,
										inout_set);
								// Neue Segmente erzeugen
								Segment2Ext leftPartOfT = new Segment2Ext(
										sp_CD_T, point1OfT);
								Segment2Ext rightPartOfT = new Segment2Ext(
										point1OfT, pRT);
								VerticalSegment splitVSLeftTop = new VerticalSegment(
										left.getUpperPoint(), sp_CD_T, null,
										null, null, null);
								VerticalSegment splitVSLeftBottom = new VerticalSegment(
										sp_CD_T, left.getLowerPoint(), null,
										null, null, null);
								//Setzen der Punktreferenzen fuer die Boegen
								split1_A1.setRefTraget(point1OfT);
								split1_A1.setRefSource(right.getLowerPoint());
								split2_A1.setRefSource(point1OfT);
								split2_A1.setRefTraget(left.getLowerPoint());
								// Dreiecke erzeugen
								SimpleList list = new SimpleList();
								list.add(splitVSLeftBottom);
								list.add(split2_A1);
								list.add(leftPartOfT);
								PseudoTriangle2 pt1 = new PseudoTriangle2(list);
								list.clear();
								list.add(right);
								list.add(rightPartOfT);
								list.add(split1_A1);
								PseudoTriangle2 pt2 = new PseudoTriangle2(list);
								list.clear();
								list.add(splitVSLeftTop);
								list.add(leftPartOfT);
								list.add(rightPartOfT);
								list.add(top);
								PseudoTriangle2 pt3 = new PseudoTriangle2(list);
								listOfTriangles.add(pt1);
								listOfTriangles.add(pt2);
								//listOfTriangles.add(pt3);
								// Beziehungen erzeugen
								leftPartOfT.add_PseudoTriangle(pt1);
								leftPartOfT.add_PseudoTriangle(pt3);
								rightPartOfT.add_PseudoTriangle(pt2);
								rightPartOfT.add_PseudoTriangle(pt3);
								splitVSLeftTop.setPseudoTriaRight(pt3);
								splitVSLeftBottom.setPseudoTriaRight(pt1);
								right.setPseudoTriaLeft(pt2);
								// Dreiecke mit vier Objekten muessen erneut geteilt werden
								PseudoTriangle2[] ptArray = pt3
										.triangulation(point1OfT);
								for (int i = 0; i < ptArray.length; i++)
								{
									listOfTriangles.add(ptArray[i]);
								}
								// Arbeit nach der Triangulation
								VerticalSegment[] vsArray = new VerticalSegment[3];
								vsArray[0] = left;
								vsArray[1] = splitVSLeftTop;
								vsArray[2] = splitVSLeftBottom;
								toDo.add(sp_CD_T, vsArray);

								// toDo.add(sp_CD_T, left);
							}
							else
							{
								// (A,C)
								// _a1 muss geteilt werden
								Arc2ExtModified split1_A1 = new Arc2ExtModified(
										_a1.centre, _a1.radius,
										_a1.sourceAngle(), angleT_A,
										Angle.ORIENTATION_LEFT);
								Arc2ExtModified split2_A1 = new Arc2ExtModified(
										_a1.centre, _a1.radius, angleT_A,
										_a1.targetAngle(),
										Angle.ORIENTATION_LEFT);
								// Beruehrpunkt der Tangente an _a1
								Point2 point1OfT = split1_A1.target();
								// Tangente an _a1 durch C
								Line2 tangente = new Line2(point1OfT, pLT);
								// Schnittpunkt der Tangente mit (A,B)
								Intersection inout_set = new Intersection();
								Segment2Ext _s1 = new Segment2Ext(pRB, pRT);
								Point2 sp_AB_T = _s1.intersection(tangente,
										inout_set);
								// Neue Segmente erzeugen
								Segment2Ext leftPartOfT = new Segment2Ext(pLT,
										point1OfT);
								Segment2Ext rightPartOfT = new Segment2Ext(
										point1OfT, sp_AB_T);
								VerticalSegment splitVSRightTop = new VerticalSegment(
										right.getUpperPoint(), sp_AB_T, null,
										null, null, null);
								VerticalSegment splitVSRightBottom = new VerticalSegment(
										sp_AB_T, right.getLowerPoint(), null,
										null, null, null);
								//Setzen der Punktreferenzen fuer die Boegen
								split1_A1.setRefTraget(point1OfT);
								split1_A1.setRefSource(right.getLowerPoint());
								split2_A1.setRefSource(point1OfT);
								split2_A1.setRefTraget(left.getLowerPoint());
								// Dreiecke erzeugen
								SimpleList list = new SimpleList();
								list.add(splitVSRightBottom);
								list.add(rightPartOfT);
								list.add(split1_A1);
								PseudoTriangle2 pt1 = new PseudoTriangle2(list);
								list.clear();
								list.add(left);
								list.add(split2_A1);
								list.add(leftPartOfT);
								PseudoTriangle2 pt2 = new PseudoTriangle2(list);
								list.clear();
								list.add(splitVSRightTop);
								list.add(top);
								list.add(leftPartOfT);
								list.add(rightPartOfT);
								PseudoTriangle2 pt3 = new PseudoTriangle2(list);
								listOfTriangles.add(pt1);
								listOfTriangles.add(pt2);
								//listOfTriangles.add(pt3);
								// Beziehungen erzeugen
								leftPartOfT.add_PseudoTriangle(pt2);
								leftPartOfT.add_PseudoTriangle(pt3);
								rightPartOfT.add_PseudoTriangle(pt1);
								rightPartOfT.add_PseudoTriangle(pt3);
								splitVSRightTop.setPseudoTriaLeft(pt3);
								splitVSRightBottom.setPseudoTriaLeft(pt1);
								left.setPseudoTriaRight(pt2);
								// Dreiecke mit vier Objekten muessen erneut geteilt werden
								PseudoTriangle2[] ptArray = pt3
										.triangulation(point1OfT);
								for (int i = 0; i < ptArray.length; i++)
								{
									listOfTriangles.add(ptArray[i]);
								}
								// Arbeit nach der Triangulation
								VerticalSegment[] vsArray = new VerticalSegment[3];
								vsArray[0] = right;
								vsArray[1] = splitVSRightTop;
								vsArray[2] = splitVSRightBottom;
								toDo.add(sp_AB_T, vsArray);

								//toDo.add(sp_AB_T, right);
							}
						}
					}
				}
				else if (bottom instanceof Segment2Ext)
				{
					Point2 pLB = ((Segment2Ext) bottom).getLeftPoint();
					Point2 pRB = ((Segment2Ext) bottom).getRightPoint();
					if (top instanceof Arc2ExtModified)
					{
						Arc2ExtModified _a2 = (Arc2ExtModified) top;
						Point2 pLT = new Point2();
						Point2 pRT = new Point2();
						if (((Point2) ((Arc2ExtModified) top).source())
								.isSmaller(((Arc2ExtModified) top).target()))
						{
							pLT = ((Arc2ExtModified) top).source();
							pRT = ((Arc2ExtModified) top).target();
						}
						else
						{
							pLT = ((Arc2ExtModified) top).target();
							pRT = ((Arc2ExtModified) top).source();
						}

						// tangenten an _a2 durch A und D
						double distA2toA = _a2.centre.distance(pRB);
						double angleA2toA = (new LimitedAngle(
								_a2.centre.angle(pRB), LimitedAngle.CIRCLE_ABS))
								.rad();
						double angleT_C = angleA2toA
								- Math.acos(_a2.radius / distA2toA);

						double distA2toD = _a2.centre.distance(pLB);
						double angleA2toD = (new LimitedAngle(
								_a2.centre.angle(pLB), LimitedAngle.CIRCLE_ABS))
								.rad();
						double angleT_B = angleA2toD
								+ Math.acos(_a2.radius / distA2toD);

						if (((_a2.targetAngle() <= angleT_B) && (_a2
								.targetAngle() != 0.0))
								&& (_a2.sourceAngle() >= angleT_C))
						{
							// es sind zwei Loesungen mit zwei Dreiecken moeglich
							if (pLB.distance(pRT) < pRB.distance(pLT))
							{
								// Eine Loesung mit zwei Dreiecken ist moeglich. (B,D)
								Segment2Ext newSeg = new Segment2Ext(pLB, pRT);
								// Dreiecke erzeugen
								SimpleList list = new SimpleList();
								list.add(right);
								list.add(newSeg);
								list.add(bottom);
								PseudoTriangle2 pt1 = new PseudoTriangle2(list);
								list.clear();
								list.add(left);
								list.add(newSeg);
								list.add(top);
								PseudoTriangle2 pt2 = new PseudoTriangle2(list);
								listOfTriangles.add(pt1);
								listOfTriangles.add(pt2);
								// Beziehungen erzeugen
								newSeg.add_PseudoTriangle(pt1);
								newSeg.add_PseudoTriangle(pt2);
								left.setPseudoTriaRight(pt2);
								right.setPseudoTriaLeft(pt1);
							}
							else
							{
								// Eine Loesung mit zwei Dreiecken ist moeglich. (A,C)
								Segment2Ext newSeg = new Segment2Ext(pRB, pLT);
								// Dreiecke erzeugen
								SimpleList list = new SimpleList();
								list.add(left);
								list.add(bottom);
								list.add(newSeg);
								PseudoTriangle2 pt1 = new PseudoTriangle2(list);
								list.clear();
								list.add(right);
								list.add(top);
								list.add(newSeg);
								PseudoTriangle2 pt2 = new PseudoTriangle2(list);
								listOfTriangles.add(pt1);
								listOfTriangles.add(pt2);
								// Beziehungen erzeugen
								newSeg.add_PseudoTriangle(pt1);
								newSeg.add_PseudoTriangle(pt2);
								left.setPseudoTriaRight(pt1);
								right.setPseudoTriaLeft(pt2);
							}
						}
						else if ((_a2.targetAngle() <= angleT_B)
								&& (_a2.targetAngle() != 0.0))
						{
							// Eine Loesung mit zwei Dreiecken ist moeglich. (B,D)
							Segment2Ext newSeg = new Segment2Ext(pLB, pRT);
							// Dreiecke erzeugen
							SimpleList list = new SimpleList();
							list.add(right);
							list.add(newSeg);
							list.add(bottom);
							PseudoTriangle2 pt1 = new PseudoTriangle2(list);
							list.clear();
							list.add(left);
							list.add(newSeg);
							list.add(top);
							PseudoTriangle2 pt2 = new PseudoTriangle2(list);
							listOfTriangles.add(pt1);
							listOfTriangles.add(pt2);
							// Beziehungen erzeugen
							newSeg.add_PseudoTriangle(pt1);
							newSeg.add_PseudoTriangle(pt2);
							left.setPseudoTriaRight(pt2);
							right.setPseudoTriaLeft(pt1);
						}
						else if (_a2.sourceAngle() >= angleT_C)
						{
							// Eine Loesung mit zwei Dreiecken ist moeglich. (A,C)
							Segment2Ext newSeg = new Segment2Ext(pRB, pLT);
							// Dreiecke erzeugen
							SimpleList list = new SimpleList();
							list.add(left);
							list.add(bottom);
							list.add(newSeg);
							PseudoTriangle2 pt1 = new PseudoTriangle2(list);
							list.clear();
							list.add(right);
							list.add(top);
							list.add(newSeg);
							PseudoTriangle2 pt2 = new PseudoTriangle2(list);
							listOfTriangles.add(pt1);
							listOfTriangles.add(pt2);
							// Beziehungen erzeugen
							newSeg.add_PseudoTriangle(pt1);
							newSeg.add_PseudoTriangle(pt2);
							left.setPseudoTriaRight(pt1);
							right.setPseudoTriaLeft(pt2);
						}
						else
						{
							if (pLB.distance(pRT) < pRB.distance(pLT))
							{
								// (B,D)
								// _a2 muss geteilt werden
								Arc2ExtModified split1_A2 = new Arc2ExtModified(
										_a2.centre, _a2.radius,
										_a2.sourceAngle(), angleT_B,
										Angle.ORIENTATION_LEFT);
								Arc2ExtModified split2_A2 = new Arc2ExtModified(
										_a2.centre, _a2.radius, angleT_B,
										_a2.targetAngle(),
										Angle.ORIENTATION_LEFT);
								// Beruehrpunkt der Tangente an _a2
								Point2 point1OfT = split1_A2.target();
								// Tangente an _a2 durch D
								Line2 tangente = new Line2(point1OfT, pLB);
								// Schnittpunkt der Tangente mit (A,B)
								Intersection inout_set = new Intersection();
								Segment2Ext _s1 = new Segment2Ext(pRB, pRT);
								Point2 sp_AB_T = _s1.intersection(tangente,
										inout_set);
								// Neue Segmente erzeugen
								Segment2Ext leftPartOfT = new Segment2Ext(pLB,
										point1OfT);
								Segment2Ext rightPartOfT = new Segment2Ext(
										point1OfT, sp_AB_T);
								VerticalSegment splitVSRightTop = new VerticalSegment(
										right.getUpperPoint(), sp_AB_T, null,
										null, null, null);
								VerticalSegment splitVSRightBottom = new VerticalSegment(
										sp_AB_T, right.getLowerPoint(), null,
										null, null, null);
								//Setzen der Punktreferenzen fuer die Boegen
								split1_A2.setRefTraget(point1OfT);
								split1_A2.setRefSource(left.getUpperPoint());
								split2_A2.setRefSource(point1OfT);
								split2_A2.setRefTraget(right.getUpperPoint());
								// Dreiecke erzeugen
								SimpleList list = new SimpleList();
								list.add(left);
								list.add(leftPartOfT);
								list.add(split1_A2);
								PseudoTriangle2 pt1 = new PseudoTriangle2(list);
								list.clear();
								list.add(splitVSRightTop);
								list.add(split2_A2);
								list.add(rightPartOfT);
								PseudoTriangle2 pt2 = new PseudoTriangle2(list);
								list.clear();
								list.add(splitVSRightBottom);
								list.add(rightPartOfT);
								list.add(leftPartOfT);
								list.add(bottom);
								PseudoTriangle2 pt3 = new PseudoTriangle2(list);
								listOfTriangles.add(pt1);
								listOfTriangles.add(pt2);
								//listOfTriangles.add(pt3);
								// Beziehungen erzeugen
								leftPartOfT.add_PseudoTriangle(pt1);
								leftPartOfT.add_PseudoTriangle(pt3);
								rightPartOfT.add_PseudoTriangle(pt2);
								rightPartOfT.add_PseudoTriangle(pt3);
								splitVSRightTop.setPseudoTriaLeft(pt2);
								splitVSRightBottom.setPseudoTriaLeft(pt3);
								left.setPseudoTriaRight(pt1);
								// Dreiecke mit vier Objekten muessen erneut geteilt werden
								PseudoTriangle2[] ptArray = pt3
										.triangulation(point1OfT);
								for (int i = 0; i < ptArray.length; i++)
								{
									listOfTriangles.add(ptArray[i]);
								}
								// Arbeit nach der Triangulation
								VerticalSegment[] vsArray = new VerticalSegment[3];
								vsArray[0] = right;
								vsArray[1] = splitVSRightTop;
								vsArray[2] = splitVSRightBottom;
								toDo.add(sp_AB_T, vsArray);

								//toDo.add(sp_AB_T, right);
							}
							else
							{
								// (A,C)
								// _a2 muss geteilt werden
								Arc2ExtModified split1_A2 = new Arc2ExtModified(
										_a2.centre, _a2.radius,
										_a2.sourceAngle(), angleT_C,
										Angle.ORIENTATION_LEFT);
								Arc2ExtModified split2_A2 = new Arc2ExtModified(
										_a2.centre, _a2.radius, angleT_C,
										_a2.targetAngle(),
										Angle.ORIENTATION_LEFT);
								// Beruehrpunkt der Tangente an _a2
								Point2 point1OfT = split1_A2.target();
								// Tangente an _a2 durch A
								Line2 tangente = new Line2(point1OfT, pRB);
								// Schnittpunkt der Tangente mit (C,D)
								Intersection inout_set = new Intersection();
								Segment2Ext _s2 = new Segment2Ext(pLB, pLT);
								Point2 sp_CD_T = _s2.intersection(tangente,
										inout_set);
								// Neue Segmente erzeugen
								Segment2Ext leftPartOfT = new Segment2Ext(
										sp_CD_T, point1OfT);
								Segment2Ext rightPartOfT = new Segment2Ext(
										point1OfT, pRB);
								VerticalSegment splitVSLeftTop = new VerticalSegment(
										left.getUpperPoint(), sp_CD_T, null,
										null, null, null);
								VerticalSegment splitVSLeftBottom = new VerticalSegment(
										sp_CD_T, left.getLowerPoint(), null,
										null, null, null);
								//Setzen der Punktreferenzen fuer die Boegen
								split1_A2.setRefTraget(point1OfT);
								split1_A2.setRefSource(left.getUpperPoint());
								split2_A2.setRefSource(point1OfT);
								split2_A2.setRefTraget(right.getUpperPoint());
								// Dreiecke erzeugen
								SimpleList list = new SimpleList();
								list.add(splitVSLeftTop);
								list.add(leftPartOfT);
								list.add(split1_A2);
								PseudoTriangle2 pt1 = new PseudoTriangle2(list);
								list.clear();
								list.add(right);
								list.add(split2_A2);
								list.add(rightPartOfT);
								PseudoTriangle2 pt2 = new PseudoTriangle2(list);
								list.clear();
								list.add(splitVSLeftBottom);
								list.add(bottom);
								list.add(rightPartOfT);
								list.add(leftPartOfT);
								PseudoTriangle2 pt3 = new PseudoTriangle2(list);
								listOfTriangles.add(pt1);
								listOfTriangles.add(pt2);
								//listOfTriangles.add(pt3);
								// Beziehungen erzeugen
								leftPartOfT.add_PseudoTriangle(pt1);
								leftPartOfT.add_PseudoTriangle(pt3);
								rightPartOfT.add_PseudoTriangle(pt2);
								rightPartOfT.add_PseudoTriangle(pt3);
								splitVSLeftTop.setPseudoTriaRight(pt1);
								splitVSLeftBottom.setPseudoTriaRight(pt3);
								right.setPseudoTriaLeft(pt2);
								// Dreiecke mit vier Objekten muessen erneut geteilt werden
								PseudoTriangle2[] ptArray = pt3
										.triangulation(point1OfT);
								for (int i = 0; i < ptArray.length; i++)
								{
									listOfTriangles.add(ptArray[i]);
								}
								// Arbeit nach der Triangulation
								VerticalSegment[] vsArray = new VerticalSegment[3];
								vsArray[0] = left;
								vsArray[1] = splitVSLeftTop;
								vsArray[2] = splitVSLeftBottom;
								toDo.add(sp_CD_T, vsArray);

								//toDo.add(sp_CD_T, left);
							}
						}
					}
				}
			}
			return listOfTriangles;
		}


		/**
		 * Liefert eine Triangulation
		 * 
		 * @param vsToDevide
		 *            Liste
		 * 
		 * @return Ergebnis
		 */
		public SimpleList triangulation(
				List vsToDevide)
		{
			Object bottom = null, top = null;
			SimpleList vsLeft = new SimpleList(), vsRight = new SimpleList();
			SimpleList listOfTriangles = new SimpleList();

			// Zuordnung der Objekte den Variablen: top, bottom, vsLeft und vsRight
			byte direction = DOWN;
			for (ListItem lItem = items.first(); lItem != null; lItem = lItem
					.next())
			{
				if (direction == DOWN)
				{
					if (lItem.value() instanceof VerticalSegment)
					{
						vsLeft.add(lItem.value());
					}
					else
					{
						bottom = lItem.value();
						direction = UP;
					}
				}
				else
				{
					if (lItem.value() instanceof VerticalSegment)
					{
						vsRight.add(lItem.value());
					}
					else
					{
						top = lItem.value();
						direction = DOWN;
					}
				}
			}
			vsRight.reverse();
			if ((vsLeft.length() == 1) && (vsRight.length() == 1))
			{
				// einfachere variante
				listOfTriangles
						.addAll(simpleTriangulation(
								(VerticalSegment) vsLeft.firstValue(), bottom,
								(VerticalSegment) vsRight.firstValue(), top,
								vsToDevide));
			}
			else if ((vsLeft.length() == 2) && (vsRight.length() == 1))
			{
				VerticalSegment left = new VerticalSegment(
						((VerticalSegment) vsLeft.firstValue()).getUpperPoint(),
						((VerticalSegment) vsLeft.lastValue()).getLowerPoint(),
						((VerticalSegment) vsLeft.firstValue()).getTopLeft(),
						((VerticalSegment) vsLeft.firstValue()).getTopRight(),
						((VerticalSegment) vsLeft.lastValue()).getBottomLeft(),
						((VerticalSegment) vsLeft.lastValue()).getBottomRight());
				listOfTriangles
						.addAll(simpleTriangulation(left, bottom,
								(VerticalSegment) vsRight.firstValue(), top,
								vsToDevide));
			}
			else if ((vsLeft.length() == 1) && (vsRight.length() == 2))
			{
				VerticalSegment right = new VerticalSegment(
						((VerticalSegment) vsRight.firstValue())
								.getUpperPoint(),
						((VerticalSegment) vsRight.lastValue()).getLowerPoint(),
						((VerticalSegment) vsRight.firstValue()).getTopLeft(),
						((VerticalSegment) vsRight.firstValue()).getTopRight(),
						((VerticalSegment) vsRight.lastValue()).getBottomLeft(),
						((VerticalSegment) vsRight.lastValue())
								.getBottomRight());
				listOfTriangles.addAll(simpleTriangulation(
						(VerticalSegment) vsLeft.firstValue(), bottom, right,
						top, vsToDevide));
			}
			else if ((vsLeft.length() == 2) && (vsRight.length() == 2))
			{
				VerticalSegment left = new VerticalSegment(
						((VerticalSegment) vsLeft.firstValue()).getUpperPoint(),
						((VerticalSegment) vsLeft.lastValue()).getLowerPoint(),
						((VerticalSegment) vsLeft.firstValue()).getTopLeft(),
						((VerticalSegment) vsLeft.firstValue()).getTopRight(),
						((VerticalSegment) vsLeft.lastValue()).getBottomLeft(),
						((VerticalSegment) vsLeft.lastValue()).getBottomRight());
				VerticalSegment right = new VerticalSegment(
						((VerticalSegment) vsRight.firstValue())
								.getUpperPoint(),
						((VerticalSegment) vsRight.lastValue()).getLowerPoint(),
						((VerticalSegment) vsRight.firstValue()).getTopLeft(),
						((VerticalSegment) vsRight.firstValue()).getTopRight(),
						((VerticalSegment) vsRight.lastValue()).getBottomLeft(),
						((VerticalSegment) vsRight.lastValue())
								.getBottomRight());
				listOfTriangles.addAll(simpleTriangulation(left, bottom, right,
						top, vsToDevide));
			}
			else if ((vsLeft.length() == 0) && (vsRight.length() == 1))
			{
				if ((top instanceof Arc2ExtModified)
						&& (bottom instanceof Arc2ExtModified))
				{
					Arc2ExtModified _a1 = (Arc2ExtModified) bottom;
					Arc2ExtModified _a2 = (Arc2ExtModified) top;
					Angle bottomA = new Angle(_a1.targetAngle()
							- Angle.PI_DIV_2);
					Angle topA = new Angle(_a2.sourceAngle() + Angle.PI_DIV_2);
					Line2 l = new Line2(_a1.centre, _a2.centre);
					Line2 orthToL = l.orthogonal(_a1.target());
					Point2 sp = orthToL.calculatePoint(_a1.source().x);
					// neue Segmente
					Segment2Ext newSeg = new Segment2Ext(_a1.target(), sp);
					VerticalSegment rightTop = new VerticalSegment(
							((VerticalSegment) vsRight.firstValue())
									.getUpperPoint(),
							sp, ((VerticalSegment) vsRight.firstValue())
									.getTopLeft(), ((VerticalSegment) vsRight
									.firstValue()).getTopRight(), null, null);
					VerticalSegment rightBottom = new VerticalSegment(
							((VerticalSegment) vsRight.firstValue())
									.getLowerPoint(),
							sp, null, null, ((VerticalSegment) vsRight
									.firstValue()).getBottomLeft(),
							((VerticalSegment) vsRight.firstValue())
									.getBottomRight());
					// Dreiecke erzeugen
					SimpleList list = new SimpleList();
					list.add(rightBottom);
					list.add(newSeg);
					list.add(bottom);
					PseudoTriangle2 pt1 = new PseudoTriangle2(list);
					list.clear();
					list.add(rightTop);
					list.add(top);
					list.add(newSeg);
					PseudoTriangle2 pt2 = new PseudoTriangle2(list);
					listOfTriangles.add(pt1);
					listOfTriangles.add(pt2);
					// Beziehungen erzeugen
					rightTop.setObjectBelow(rightBottom);
					rightBottom.setObjectAbove(rightTop);
					newSeg.add_adjacentVS(rightBottom);
					newSeg.add_adjacentVS(rightTop);
					rightBottom.setPseudoTriaLeft(pt1);
					rightTop.setPseudoTriaLeft(pt2);
					//
					// TODO: vsRight wird geteilt. Ereigniss erzeugen.
					// ausßerdem fehlen weitere Beziehungen
					//
					VerticalSegment[] vsArray = new VerticalSegment[3];
					vsArray[0] = (VerticalSegment) vsRight.firstValue();
					vsArray[1] = rightTop;
					vsArray[2] = rightBottom;
					vsToDevide.add(sp, vsArray);
				}
				else
				{
					SimpleList list = new SimpleList();
					list.add(vsRight.firstValue());
					list.add(top);
					list.add(bottom);
					PseudoTriangle2 pt = new PseudoTriangle2(list);
					listOfTriangles.add(pt);
					// Beziehungen erzeugen
					((VerticalSegment) vsRight.firstValue())
							.setPseudoTriaLeft(pt);
				}
			}
			else if ((vsLeft.length() == 1) && (vsRight.length() == 0))
			{
				if ((top instanceof Arc2ExtModified)
						&& (bottom instanceof Arc2ExtModified))
				{
					Arc2ExtModified _a1 = (Arc2ExtModified) bottom;
					Arc2ExtModified _a2 = (Arc2ExtModified) top;
					Line2 l = new Line2(_a1.centre, _a2.centre);
					Line2 orthToL = l.orthogonal(_a1.source());
					Point2 sp = orthToL.calculatePoint(_a1.target().x);
					Segment2Ext newSeg = new Segment2Ext(_a1.source(), sp);
					VerticalSegment leftTop = new VerticalSegment(
							((VerticalSegment) vsLeft.firstValue())
									.getUpperPoint(),
							sp, ((VerticalSegment) vsLeft.firstValue())
									.getTopLeft(), ((VerticalSegment) vsLeft
									.firstValue()).getTopRight(), null, null);
					VerticalSegment leftBottom = new VerticalSegment(
							((VerticalSegment) vsLeft.firstValue())
									.getLowerPoint(),
							sp, null, null, ((VerticalSegment) vsLeft
									.firstValue()).getBottomLeft(),
							((VerticalSegment) vsLeft.firstValue())
									.getBottomRight());
					// Dreiecke erzeugen
					SimpleList list = new SimpleList();
					list.add(leftBottom);
					list.add(bottom);
					list.add(newSeg);
					PseudoTriangle2 pt1 = new PseudoTriangle2(list);
					list.clear();
					list.add(leftTop);
					list.add(newSeg);
					list.add(top);
					PseudoTriangle2 pt2 = new PseudoTriangle2(list);
					listOfTriangles.add(pt1);
					listOfTriangles.add(pt2);
					// Beziehungen erzeugen
					leftTop.setObjectBelow(leftBottom);
					leftBottom.setObjectAbove(leftTop);
					newSeg.add_adjacentVS(leftBottom);
					newSeg.add_adjacentVS(leftTop);
					leftTop.setPseudoTriaRight(pt2);
					leftBottom.setPseudoTriaRight(pt1);
					//
					// TODO: vsLeft wird geteilt. Ereigniss erzeugen.
					// ausßerdem fehlen weitere Beziehungen
					//
					VerticalSegment[] vsArray = new VerticalSegment[3];
					vsArray[0] = (VerticalSegment) vsLeft.firstValue();
					vsArray[1] = leftTop;
					vsArray[2] = leftBottom;
					vsToDevide.add(sp, vsArray);
				}
				else
				{
					SimpleList list = new SimpleList();
					list.add(vsLeft.firstValue());
					list.add(bottom);
					list.add(top);
					PseudoTriangle2 pt = new PseudoTriangle2(list);
					listOfTriangles.add(pt);
					// Beziehungen erzeugen
					((VerticalSegment) vsLeft.firstValue())
							.setPseudoTriaRight(pt);
				}
			}
			return listOfTriangles;
		}


		/**
		 * Überprüft auf Schnitte
		 * 
		 * Nicht implementiert, gibt immer false aus.
		 * 
		 * @param box
		 *            Das Rechteck
		 * 
		 * @return false
		 */
		public boolean intersects(
				Rectangle2D box)
		{
			// TODO Auto-generated method stub
			return false;
		}


		/**
		 * Alle Referenzen werden auf <b>null</b> gesetzt.
		 */
		public void clear()
		{
			A = null;
			B = null;
			C = null;
			D = null;
			items = null;
		}

	}


	/**
	 * Hilfsklasse für CagingPolygons. Sie erzeugt ein Pseudodreieck, dass auch
	 * einen Boegen anstelle eines Segmentes enthalten kann.
	 * 
	 * @author Darius Geiss
	 */
	public class PseudoTriangle2
			implements Drawable
	{

		/**
		 * Objekte des Vierecks
		 */
		private SimpleList	items;

		private float		minX, minY, maxX, maxY;

		private Point2		centreOfMass	= null;

		private int			_identNumber	= -1;

		private Object		AB				= null, BC = null, CA = null;
		private Point2		A, B, C;


		/**
		 * Speichert eine Kopie der Liste (die Liste wird geklont). Es werden
		 * nur Listen mit einer Laenge von drei akzeptiert.<br><br> Es werden
		 * die Eckpunkte A,B und C ermittelt. Die Objekte zwischen den
		 * jeweiligen Eckpunkten werden unter AB, BC und CA abgespeichert.
		 * 
		 * @param objectList
		 *            SimpleList
		 */
		public PseudoTriangle2(
				SimpleList objectList)
		{
			items = objectList.copyList();
			if ((objectList != null) && (objectList.length() == 3))
			{
				Point2 p1, p2;
				for (ListItem lItem = items.first(); lItem != null; lItem = lItem
						.next())
				{
					p1 = null;
					p2 = null;
					if (lItem.value() instanceof Segment2Ext)
					{
						p1 = ((Segment2Ext) lItem.value()).source();
						p2 = ((Segment2Ext) lItem.value()).target();
					}
					else if (lItem.value() instanceof Arc2ExtModified)
					{
						p1 = ((Arc2ExtModified) lItem.value()).source();
						p2 = ((Arc2ExtModified) lItem.value()).target();
					}
					else if (lItem.value() instanceof VerticalSegment)
					{
						p1 = ((VerticalSegment) lItem.value()).source();
						p2 = ((VerticalSegment) lItem.value()).target();
					}

					if ((p1 != null) && (p2 != null))
					{
						minX = Math.min(minX, p1.x);
						maxX = Math.max(maxX, p1.x);
						minY = Math.min(minY, p1.y);
						maxY = Math.max(maxY, p1.y);

						minX = Math.min(minX, p2.x);
						maxX = Math.max(maxX, p2.x);
						minY = Math.min(minY, p2.y);
						maxY = Math.max(maxY, p2.y);

						if (A == null)
						{
							A = p1;
							B = p2;
							AB = lItem.value();
						}
						else
						{
							if ((!A.equals(p1)) && (!B.equals(p1))
									&& (C == null))
							{
								C = p1;
								if (p2.equals(A))
								{
									CA = lItem.value();
								}
								else if (p2.equals(B))
								{
									BC = lItem.value();
								}
							}
							else if ((!A.equals(p2)) && (!B.equals(p2))
									&& (C == null))
							{
								C = p2;
								if (p1.equals(A))
								{
									CA = lItem.value();
								}
								else if (p1.equals(B))
								{
									BC = lItem.value();
								}
							}
							else
							{
								if (CA == null)
								{
									CA = lItem.value();
								}
								else
								{
									BC = lItem.value();
								}
							}
						}
					}
				}
				// Die Eckpunkte werden gegen den Uhrzeigersinn angeordnet 
				if (A.angle(B, C) > Math.PI)
				{
					Point2 tmpP = B;
					B = A;
					A = tmpP;

					Object tmpO = CA;
					CA = BC;
					BC = tmpO;
				}
				items = new SimpleList();
				items.add(AB);
				items.add(BC);
				items.add(CA);
				centreOfMass = new Point2((A.x + B.x + C.x) / 3,
						(A.y + B.y + C.y) / 3);
			}
		}


		/**
		 * Zeichnet alle Objekte aus der Liste (jedes fuer sich und nur wenn es
		 * Drawable ist). sofern sie Drawable sind.
		 * 
		 * @param graphics
		 *            Das Grafikobjekt, in das gezeichnet wird
		 * @param graphicsContext
		 *            Die dazugehörigen Formatierungsangaben
		 */
		public void draw(
				Graphics2D graphics,
				GraphicsContext graphicsContext)
		{
			if (items != null)
			{
				for (ListItem lItem = items.first(); lItem != null; lItem = lItem
						.next())
				{
					if (lItem.value() instanceof Drawable)
						((Drawable) lItem.value()).draw(graphics,
								graphicsContext);
				}
			}
		}


		/**
		 * Eine Funktion die das Pseudo-Dreieck in zwei Dreiecke aufteilt.
		 * 
		 * SONDERFALL: Gilt nur fuer Dreiecke, die vier Kanten haben, zwei von
		 * den Kanten haben den gemeinsamen Punkt p und die selbe Steigung.
		 * 
		 * @param p
		 *            Punkt
		 * 
		 * @return Array mit Dreiecken
		 */
		public PseudoTriangle2[] triangulation(
				Point2 p)
		{
			if (items.length() == 4)
			{
				Point2 A = null, B = null, C = null, D = null;
				Object AB, BC, CD, DA;
				Point2 p1 = ((VerticalSegment) items.firstValue()).target();
				Point2 p2 = ((VerticalSegment) items.firstValue()).source();
				AB = items.firstValue();
				BC = items.first().next().value();
				CD = items.first().next().next().value();
				DA = items.lastValue();
				if (items.first().next().value() instanceof Segment2Ext)
				{
					if (((Segment2Ext) items.first().next().value()).source()
							.equals(p1))
					{
						A = p2;
						B = p1;
						C = ((Segment2Ext) items.first().next().value())
								.target();
					}
					if (((Segment2Ext) items.first().next().value()).source()
							.equals(p2))
					{
						A = p1;
						B = p2;
						C = ((Segment2Ext) items.first().next().value())
								.target();
					}
					if (((Segment2Ext) items.first().next().value()).target()
							.equals(p1))
					{
						A = p2;
						B = p1;
						C = ((Segment2Ext) items.first().next().value())
								.source();
					}
					if (((Segment2Ext) items.first().next().value()).target()
							.equals(p2))
					{
						A = p1;
						B = p2;
						C = ((Segment2Ext) items.first().next().value())
								.source();
					}
				}
				else if (items.first().next().value() instanceof Arc2ExtModified)
				{
					if (((Arc2ExtModified) items.first().next().value())
							.source().equals(p1))
					{
						A = p2;
						B = p1;
						C = ((Arc2ExtModified) items.first().next().value())
								.target();
					}
					if (((Arc2ExtModified) items.first().next().value())
							.source().equals(p2))
					{
						A = p1;
						B = p2;
						C = ((Arc2ExtModified) items.first().next().value())
								.target();
					}
					if (((Arc2ExtModified) items.first().next().value())
							.target().equals(p1))
					{
						A = p2;
						B = p1;
						C = ((Arc2ExtModified) items.first().next().value())
								.source();
					}
					if (((Arc2ExtModified) items.first().next().value())
							.target().equals(p2))
					{
						A = p1;
						B = p2;
						C = ((Arc2ExtModified) items.first().next().value())
								.source();
					}
				}
				if (items.lastValue() instanceof Segment2Ext)
				{
					if (((Segment2Ext) items.lastValue()).source().equals(A))
					{
						D = ((Segment2Ext) items.lastValue()).target();
					}
					if (((Segment2Ext) items.lastValue()).target().equals(A))
					{
						D = ((Segment2Ext) items.lastValue()).source();
					}
				}
				else if (items.lastValue() instanceof Arc2ExtModified)
				{
					if (((Arc2ExtModified) items.lastValue()).source()
							.equals(A))
					{
						D = ((Arc2ExtModified) items.lastValue()).target();
					}
					if (((Arc2ExtModified) items.lastValue()).target()
							.equals(A))
					{
						D = ((Arc2ExtModified) items.lastValue()).source();
					}
				}

				if (p.equals(C))
				{
					// Dreiecke (A,B,C) & (A,C,D)
					// Test auf direkte Verbindung zwischen A & C
					boolean zweierLoesung = false;
					if (!(DA instanceof Arc2ExtModified))
					{
						zweierLoesung = true;
					}
					else if (A.angle(C, ((Arc2ExtModified) DA).centre) <= Angle.PI_DIV_2)
					{
						zweierLoesung = true;
					}
					if (zweierLoesung == true)
					{
						// direkte Verbindung ist moeglich
						// neues Segment
						Segment2Ext newSeg = new Segment2Ext(A, C);
						// Dreiecke erzeugen
						SimpleList list = new SimpleList();
						list.add(newSeg);
						list.add(CD);
						list.add(DA);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(newSeg);
						list.add(AB);
						list.add(BC);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						PseudoTriangle2[] ptArray = new PseudoTriangle2[2];
						ptArray[0] = pt1;
						ptArray[1] = pt2;
						// Beziehungen erzeugen
						newSeg.add_PseudoTriangle(pt1);
						newSeg.add_PseudoTriangle(pt2);
						addPTtoObject(AB, pt2);
						addPTtoObject(BC, pt2);
						addPTtoObject(CD, pt1);
						addPTtoObject(DA, pt1);
						return ptArray;
					}
					else
					{
						Arc2ExtModified arc = (Arc2ExtModified) DA;
						// X ist der Mittelpunkt von arc
						double angleCXA = Math.acos(arc.radius
								/ C.distance(arc.centre));
						double angleXtoC = (new LimitedAngle(
								arc.centre.angle(C), LimitedAngle.CIRCLE_ABS))
								.rad();
						double angleOfT = angleXtoC - angleCXA;
						// teilen des Bogens
						Arc2ExtModified spiltDA_1 = new Arc2ExtModified(
								arc.centre, arc.radius, arc.sourceAngle(),
								angleOfT, arc.orientation());
						Arc2ExtModified spiltDA_2 = new Arc2ExtModified(
								arc.centre, arc.radius, angleOfT,
								arc.targetAngle(), arc.orientation());
						// T ist der Beruehrpunkt der Tangente, die durch C geht, an arc.
						Point2 T = spiltDA_1.target();
						// neue Segmente erstellen (C,T) & (B,T)
						Segment2Ext CT = new Segment2Ext(C, T);
						Segment2Ext BT = new Segment2Ext(B, T);
						// neue Dreiecke erstellen
						SimpleList list = new SimpleList();
						list.add(AB);
						list.add(BT);
						list.add(spiltDA_1);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(BT);
						list.add(BC);
						list.add(CT);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						list.clear();
						list.add(CD);
						list.add(spiltDA_2);
						list.add(CT);
						PseudoTriangle2 pt3 = new PseudoTriangle2(list);
						PseudoTriangle2[] ptArray = new PseudoTriangle2[3];
						ptArray[0] = pt1;
						ptArray[1] = pt2;
						ptArray[2] = pt3;
						// Beziehungen erzeugen
						BT.add_PseudoTriangle(pt1);
						BT.add_PseudoTriangle(pt2);
						CT.add_PseudoTriangle(pt2);
						CT.add_PseudoTriangle(pt3);
						addPTtoObject(AB, pt1);
						addPTtoObject(BC, pt2);
						addPTtoObject(CD, pt3);
						addPTtoObject(spiltDA_1, pt1);
						addPTtoObject(spiltDA_2, pt3);
						return ptArray;
					}
				}
				else if (p.equals(D))
				{
					// Dreiecke (B,C,D) & (B,D,A)
					// Test auf direkte Verbindung zwischen B & D
					boolean zweierLoesung = false;
					if (!(BC instanceof Arc2ExtModified))
					{
						zweierLoesung = true;
					}
					else if (B.angle(((Arc2ExtModified) BC).centre, D) >= Angle.PI_DIV_2)
					{
						zweierLoesung = true;
					}
					if (zweierLoesung == true)
					{
						// direkte Verbindung ist moeglich
						// neues Segment
						Segment2Ext newSeg = new Segment2Ext(B, D);
						// Dreiecke erzeugen
						SimpleList list = new SimpleList();
						list.add(newSeg);
						list.add(DA);
						list.add(AB);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(newSeg);
						list.add(BC);
						list.add(CD);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						PseudoTriangle2[] ptArray = new PseudoTriangle2[2];
						ptArray[0] = pt1;
						ptArray[1] = pt2;
						// Beziehungen erzeugen
						newSeg.add_PseudoTriangle(pt1);
						newSeg.add_PseudoTriangle(pt2);
						addPTtoObject(AB, pt1);
						addPTtoObject(BC, pt2);
						addPTtoObject(CD, pt2);
						addPTtoObject(DA, pt1);
						return ptArray;
					}
					else
					{
						Arc2ExtModified arc = (Arc2ExtModified) BC;
						// X ist der Mittelpunkt von arc
						double angleBXD = Math.acos(arc.radius
								/ D.distance(arc.centre));
						double angleXtoD = (new LimitedAngle(
								arc.centre.angle(D), LimitedAngle.CIRCLE_ABS))
								.rad();
						double angleOfT = angleXtoD + angleBXD;
						// teilen des Bogens
						Arc2ExtModified spiltBC_1 = new Arc2ExtModified(
								arc.centre, arc.radius, arc.sourceAngle(),
								angleOfT, arc.orientation());
						Arc2ExtModified spiltBC_2 = new Arc2ExtModified(
								arc.centre, arc.radius, angleOfT,
								arc.targetAngle(), arc.orientation());
						// T ist der Beruehrpunkt der Tangente, die durch C geht, an arc.
						Point2 T = spiltBC_1.target();
						// neue Segmente erstellen (C,T) & (B,T)
						Segment2Ext AT = new Segment2Ext(A, T);
						Segment2Ext DT = new Segment2Ext(D, T);
						// neue Dreiecke erstellen
						SimpleList list = new SimpleList();
						list.add(AB);
						list.add(spiltBC_2);
						list.add(AT);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(AT);
						list.add(DT);
						list.add(DA);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						list.clear();
						list.add(CD);
						list.add(DT);
						list.add(spiltBC_1);
						PseudoTriangle2 pt3 = new PseudoTriangle2(list);
						PseudoTriangle2[] ptArray = new PseudoTriangle2[3];
						ptArray[0] = pt1;
						ptArray[1] = pt2;
						ptArray[2] = pt3;
						// Beziehungen erzeugen
						AT.add_PseudoTriangle(pt1);
						AT.add_PseudoTriangle(pt2);
						DT.add_PseudoTriangle(pt2);
						DT.add_PseudoTriangle(pt3);
						addPTtoObject(AB, pt1);
						addPTtoObject(CD, pt3);
						addPTtoObject(DA, pt2);
						addPTtoObject(spiltBC_1, pt3);
						addPTtoObject(spiltBC_2, pt1);
						return ptArray;
					}
				}
			}
			return null;
		}


		/**
		 * Diese Funktion teilt ein Dreieck in mehrere Dreiecke auf.
		 * 
		 * @param mainVS
		 *            (Zu teilendes vertikales Segment)
		 * @param topVS
		 *            (Oberer Teil des geteilten vertikalen Segmentes)
		 * @param bottomVS
		 *            (Unterer Teil des geteilten vetrikalen Segmentes)
		 * 
		 * @return SimpleList (Liste mit den neuen Dreiecken)
		 */
		public SimpleList divideTriangle(
				VerticalSegment mainVS,
				VerticalSegment topVS,
				VerticalSegment bottomVS)
		{
			Point2 A = null, B = null, C = null;
			Point2 p1 = null, p2 = null;
			if (items.lastValue() instanceof Segment2Ext)
			{
				p1 = ((Segment2Ext) items.lastValue()).source();
				p2 = ((Segment2Ext) items.lastValue()).target();
			}
			else if (items.lastValue() instanceof Arc2ExtModified)
			{
				p1 = ((Arc2ExtModified) items.lastValue()).source();
				p2 = ((Arc2ExtModified) items.lastValue()).target();
			}
			boolean cIsSmaller = false;
			double distP1toVS = ((VerticalSegment) items.firstValue())
					.distance(p1);
			double distP2toVS = ((VerticalSegment) items.firstValue())
					.distance(p2);
			if (distP1toVS > distP2toVS)
			{
				if ((p1.x > ((VerticalSegment) items.firstValue()).source().x)
						&& ((p1.x > ((VerticalSegment) items.firstValue())
								.target().x)))
				{
					A = mainVS.getUpperPoint();
					B = mainVS.getLowerPoint();
					C = p1;
				}
				else if ((p1.x < ((VerticalSegment) items.firstValue())
						.source().x)
						&& ((p1.x < ((VerticalSegment) items.firstValue())
								.target().x)))
				{
					B = mainVS.getUpperPoint();
					A = mainVS.getLowerPoint();
					C = p1;
					cIsSmaller = true;
				}
			}
			else
			{
				if ((p2.x > ((VerticalSegment) items.firstValue()).source().x)
						&& (p2.x > ((VerticalSegment) items.firstValue())
								.target().x))
				{
					A = mainVS.getUpperPoint();
					B = mainVS.getLowerPoint();
					C = p2;
				}
				else if ((p2.x < ((VerticalSegment) items.firstValue())
						.source().x)
						&& (p2.x < ((VerticalSegment) items.firstValue())
								.target().x))
				{
					B = mainVS.getUpperPoint();
					A = mainVS.getLowerPoint();
					C = p2;
					cIsSmaller = true;
				}
			}
			Point2 D = topVS.getLowerPoint();
			if ((items.first().next().value() instanceof Segment2Ext)
					&& (items.lastValue() instanceof Segment2Ext))
			{
				// direkte Verbindung zwischen (C,D) ist moeglich
				Segment2Ext o1 = (Segment2Ext) items.first().next().value();
				Segment2Ext o2 = (Segment2Ext) items.lastValue();
				Segment2Ext bottom = null, top = null;
				if (o1.getUpperPoint().y > o2.getUpperPoint().y)
				{
					top = o1;
					bottom = o2;
				}
				else if (o1.getUpperPoint().y < o2.getUpperPoint().y)
				{
					top = o2;
					bottom = o1;
				}
				else
				{
					if (o1.getLowerPoint().y > o2.getLowerPoint().y)
					{
						top = o1;
						bottom = o2;
					}
					else
					{
						top = o2;
						bottom = o1;
					}
				}
				Segment2Ext segCD = new Segment2Ext(C, D);
				if (cIsSmaller)
				{
					// Dreiecke erstellen
					SimpleList list = new SimpleList();
					list.add(topVS);
					list.add(top);
					list.add(segCD);
					PseudoTriangle2 pt1 = new PseudoTriangle2(list);
					list.clear();
					list.add(bottomVS);
					list.add(segCD);
					list.add(bottom);
					PseudoTriangle2 pt2 = new PseudoTriangle2(list);
					// Beziegungen herstellen
					segCD.add_PseudoTriangle(pt1);
					segCD.add_PseudoTriangle(pt2);
					topVS.setPseudoTriaLeft(pt1);
					bottomVS.setPseudoTriaLeft(pt2);
					try
					{
						bottom.get_PseudoTriangles().remove(this);
					}
					catch (Exception e)
					{}
					bottom.add_PseudoTriangle(pt2);
					try
					{
						top.get_PseudoTriangles().remove(this);
					}
					catch (Exception e)
					{}
					top.add_PseudoTriangle(pt1);
					// Ergebniss Rueckgabe
					SimpleList listOfTri = new SimpleList();
					listOfTri.add(pt1);
					listOfTri.add(pt2);
					return listOfTri;
				}
				else
				{
					// Dreiecke erstellen
					SimpleList list = new SimpleList();
					list.add(topVS);
					list.add(segCD);
					list.add(top);
					PseudoTriangle2 pt1 = new PseudoTriangle2(list);
					list.clear();
					list.add(bottomVS);
					list.add(bottom);
					list.add(segCD);
					PseudoTriangle2 pt2 = new PseudoTriangle2(list);
					// Beziegungen herstellen
					segCD.add_PseudoTriangle(pt1);
					segCD.add_PseudoTriangle(pt2);
					topVS.setPseudoTriaLeft(pt1);
					bottomVS.setPseudoTriaLeft(pt2);
					try
					{
						bottom.get_PseudoTriangles().remove(this);
					}
					catch (Exception e)
					{}
					bottom.add_PseudoTriangle(pt2);
					try
					{
						top.get_PseudoTriangles().remove(this);
					}
					catch (Exception e)
					{}
					top.add_PseudoTriangle(pt1);
					// Ergebniss Rueckgabe
					SimpleList listOfTri = new SimpleList();
					listOfTri.add(pt1);
					listOfTri.add(pt2);
					return listOfTri;
				}
			}
			else if ((items.first().next().value() instanceof Arc2ExtModified)
					&& (items.lastValue() instanceof Segment2Ext))
			{
				if (cIsSmaller)
				{
					Arc2ExtModified top = (Arc2ExtModified) items.first()
							.next().value();
					Segment2Ext bottom = (Segment2Ext) items.lastValue();
					double angle = C.angle(D, top.centre);
					if (angle >= Angle.PI_DIV_2)
					{
						// Loesung mit zwei Dreiecken
						// Dreiecke erstellen
						Segment2Ext segCD = new Segment2Ext(C, D);
						SimpleList list = new SimpleList();
						list.add(topVS);
						list.add(top);
						list.add(segCD);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(bottomVS);
						list.add(segCD);
						list.add(bottom);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						// Beziegungen herstellen
						segCD.add_PseudoTriangle(pt1);
						segCD.add_PseudoTriangle(pt2);
						topVS.setPseudoTriaLeft(pt1);
						bottomVS.setPseudoTriaLeft(pt2);
						bottom.get_PseudoTriangles().remove(this);
						bottom.add_PseudoTriangle(pt2);
						top.set_pseudoTriangle(pt1);
						// Ergebniss Rueckgabe
						SimpleList listOfTri = new SimpleList();
						listOfTri.add(pt1);
						listOfTri.add(pt2);
						return listOfTri;
					}
					else
					{
						// Loesung mit drei Dreiecken
						// X ist der Mittelpunkt von top
						double angleTXD = Math.acos(top.radius
								/ D.distance(top.centre));
						double angleXtoD = (new LimitedAngle(
								top.centre.angle(D), LimitedAngle.CIRCLE_ABS))
								.rad();
						double angleOfT = angleXtoD - angleTXD;
						// teilen des Bogens
						Arc2ExtModified splitBC_1 = new Arc2ExtModified(
								top.centre, top.radius, top.sourceAngle(),
								angleOfT, top.orientation());
						Arc2ExtModified splitBC_2 = new Arc2ExtModified(
								top.centre, top.radius, angleOfT,
								top.targetAngle(), top.orientation());
						// T ist der Beruehrpunkt der Tangente, die durch D geht, an top.
						Point2 T = splitBC_1.target();
						// neue Segmente erstellen (D,T) & (A,T)
						Segment2Ext DT = new Segment2Ext(D, T);
						Segment2Ext AT = new Segment2Ext(A, T);
						// neue Dreiecke erstellen
						SimpleList list = new SimpleList();
						list.add(topVS);
						list.add(splitBC_2);
						list.add(DT);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(bottomVS);
						list.add(DT);
						list.add(AT);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						list.clear();
						list.add(splitBC_1);
						list.add(bottom);
						list.add(AT);
						PseudoTriangle2 pt3 = new PseudoTriangle2(list);
						// Beziehungen erzeugen
						DT.add_PseudoTriangle(pt1);
						DT.add_PseudoTriangle(pt2);
						AT.add_PseudoTriangle(pt2);
						AT.add_PseudoTriangle(pt3);
						addPTtoObject2(topVS, pt1, true);
						addPTtoObject2(bottomVS, pt2, true);
						addPTtoObject2(splitBC_1, pt3);
						addPTtoObject2(splitBC_2, pt1);
						addPTtoObject(bottom, pt3);
						// Ergebniss Rueckgabe
						SimpleList listOfTri = new SimpleList();
						listOfTri.add(pt1);
						listOfTri.add(pt2);
						listOfTri.add(pt3);
						return listOfTri;
					}
				}
				else
				{
					Arc2ExtModified bottom = (Arc2ExtModified) items.first()
							.next().value();
					Segment2Ext top = (Segment2Ext) items.lastValue();
					double angle = C.angle(D, bottom.centre);
					if (angle >= Angle.PI_DIV_2)
					{
						// Loesung mit zwei Dreiecken
						// Dreiecke erstellen
						Segment2Ext segCD = new Segment2Ext(C, D);
						SimpleList list = new SimpleList();
						list.add(topVS);
						list.add(segCD);
						list.add(top);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(bottomVS);
						list.add(bottom);
						list.add(segCD);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						// Beziegungen herstellen
						segCD.add_PseudoTriangle(pt1);
						segCD.add_PseudoTriangle(pt2);
						topVS.setPseudoTriaRight(pt1);
						bottomVS.setPseudoTriaRight(pt2);
						bottom.set_pseudoTriangle(pt2);
						top.get_PseudoTriangles().remove(this);
						top.add_PseudoTriangle(pt1);
						// Ergebniss Rueckgabe
						SimpleList listOfTri = new SimpleList();
						listOfTri.add(pt1);
						listOfTri.add(pt2);
						return listOfTri;
					}
					else
					{
						// Loesung mit drei Dreiecken
						// X ist der Mittelpunkt von bottom
						double angleTXD = Math.acos(bottom.radius
								/ D.distance(bottom.centre));
						double angleXtoD = (new LimitedAngle(
								bottom.centre.angle(D), LimitedAngle.CIRCLE_ABS))
								.rad();
						double angleOfT = angleXtoD - angleTXD;
						// teilen des Bogens
						Arc2ExtModified splitBC_1 = new Arc2ExtModified(
								bottom.centre, bottom.radius,
								bottom.sourceAngle(), angleOfT,
								bottom.orientation());
						Arc2ExtModified splitBC_2 = new Arc2ExtModified(
								bottom.centre, bottom.radius, angleOfT,
								bottom.targetAngle(), bottom.orientation());
						// T ist der Beruehrpunkt der Tangente, die durch D geht, an bottom.
						Point2 T = splitBC_1.target();
						// neue Segmente erstellen (D,T) & (A,T)
						Segment2Ext DT = new Segment2Ext(D, T);
						Segment2Ext AT = new Segment2Ext(A, T);
						// neue Dreiecke erstellen
						SimpleList list = new SimpleList();
						list.add(topVS);
						list.add(DT);
						list.add(AT);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(bottomVS);
						list.add(splitBC_2);
						list.add(DT);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						list.clear();
						list.add(splitBC_1);
						list.add(top);
						list.add(AT);
						PseudoTriangle2 pt3 = new PseudoTriangle2(list);
						// Beziehungen erzeugen
						DT.add_PseudoTriangle(pt1);
						DT.add_PseudoTriangle(pt2);
						AT.add_PseudoTriangle(pt1);
						AT.add_PseudoTriangle(pt3);
						addPTtoObject2(topVS, pt1, false);
						addPTtoObject2(bottomVS, pt2, false);
						addPTtoObject2(splitBC_1, pt3);
						addPTtoObject2(splitBC_2, pt2);
						addPTtoObject(top, pt3);
						// Ergebniss Rueckgabe
						SimpleList listOfTri = new SimpleList();
						listOfTri.add(pt1);
						listOfTri.add(pt2);
						listOfTri.add(pt3);
						return listOfTri;
					}
				}
			}
			else if ((items.first().next().value() instanceof Segment2Ext)
					&& (items.lastValue() instanceof Arc2ExtModified))
			{
				if (cIsSmaller)
				{
					Segment2Ext top = (Segment2Ext) items.first().next()
							.value();
					Arc2ExtModified bottom = (Arc2ExtModified) items
							.lastValue();
					double angle = C.angle(bottom.centre, D);
					if (angle >= Angle.PI_DIV_2)
					{
						// Loesung mit zwei Dreiecken
						// Dreiecke erstellen
						Segment2Ext segCD = new Segment2Ext(C, D);
						SimpleList list = new SimpleList();
						list.add(topVS);
						list.add(top);
						list.add(segCD);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(bottomVS);
						list.add(segCD);
						list.add(bottom);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						// Beziegungen herstellen
						segCD.add_PseudoTriangle(pt1);
						segCD.add_PseudoTriangle(pt2);
						topVS.setPseudoTriaLeft(pt1);
						bottomVS.setPseudoTriaLeft(pt2);
						bottom.set_pseudoTriangle(pt2);
						top.get_PseudoTriangles().remove(this);
						top.add_PseudoTriangle(pt1);
						// Ergebniss Rueckgabe 
						SimpleList listOfTri = new SimpleList();
						listOfTri.add(pt1);
						listOfTri.add(pt2);
						return listOfTri;
					}
					else
					{
						// Loesung mit drei Dreiecken
						// X ist der Mittelpunkt von bottom
						double angleTXD = Math.acos(bottom.radius
								/ D.distance(bottom.centre));
						double angleXtoD = (new LimitedAngle(
								bottom.centre.angle(D), LimitedAngle.CIRCLE_ABS))
								.rad();
						double angleOfT = angleXtoD + angleTXD;
						// teilen des Bogens
						Arc2ExtModified splitCA_1 = new Arc2ExtModified(
								bottom.centre, bottom.radius,
								bottom.sourceAngle(), angleOfT,
								bottom.orientation());
						Arc2ExtModified splitCA_2 = new Arc2ExtModified(
								bottom.centre, bottom.radius, angleOfT,
								bottom.targetAngle(), bottom.orientation());
						// T ist der Beruehrpunkt der Tangente, die durch D geht, an bottom.
						Point2 T = splitCA_1.target();
						// neue Segmente erstellen (D,T) & (A,T)
						Segment2Ext DT = new Segment2Ext(D, T);
						Segment2Ext BT = new Segment2Ext(B, T);
						// neue Dreiecke erstellen
						SimpleList list = new SimpleList();
						list.add(topVS);
						list.add(BT);
						list.add(DT);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(bottomVS);
						list.add(DT);
						list.add(splitCA_1);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						list.clear();
						list.add(splitCA_2);
						list.add(BT);
						list.add(top);
						PseudoTriangle2 pt3 = new PseudoTriangle2(list);
						// Beziehungen erzeugen
						DT.add_PseudoTriangle(pt1);
						DT.add_PseudoTriangle(pt2);
						BT.add_PseudoTriangle(pt1);
						BT.add_PseudoTriangle(pt3);
						addPTtoObject2(topVS, pt1, false);
						addPTtoObject2(bottomVS, pt2, false);
						addPTtoObject2(splitCA_1, pt2);
						addPTtoObject2(splitCA_2, pt3);
						addPTtoObject(top, pt3);
						// Ergebniss Rueckgabe
						SimpleList listOfTri = new SimpleList();
						listOfTri.add(pt1);
						listOfTri.add(pt2);
						listOfTri.add(pt3);
						return listOfTri;
					}
				}
				else
				{
					Segment2Ext bottom = (Segment2Ext) items.first().next()
							.value();
					Arc2ExtModified top = (Arc2ExtModified) items.lastValue();
					double angle = C.angle(top.centre, D);
					if (angle >= Angle.PI_DIV_2)
					{
						// Loesung mit zwei Dreiecken
						// Dreiecke erstellen
						Segment2Ext segCD = new Segment2Ext(C, D);
						SimpleList list = new SimpleList();
						list.add(topVS);
						list.add(segCD);
						list.add(top);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(bottomVS);
						list.add(bottom);
						list.add(segCD);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						// Beziegungen herstellen
						segCD.add_PseudoTriangle(pt1);
						segCD.add_PseudoTriangle(pt2);
						topVS.setPseudoTriaRight(pt1);
						bottomVS.setPseudoTriaRight(pt2);
						bottom.get_PseudoTriangles().remove(this);
						bottom.add_PseudoTriangle(pt2);
						top.set_pseudoTriangle(pt1);
						// Ergebniss Rueckgabe
						SimpleList listOfTri = new SimpleList();
						listOfTri.add(pt1);
						listOfTri.add(pt2);
						return listOfTri;
					}
					else
					{
						// Loesung mit drei Dreiecken
						// X ist der Mittelpunkt von top
						double angleTXD = Math.acos(top.radius
								/ D.distance(top.centre));
						double angleXtoD = (new LimitedAngle(
								top.centre.angle(D), LimitedAngle.CIRCLE_ABS))
								.rad();
						double angleOfT = angleXtoD + angleTXD;
						// teilen des Bogens
						Arc2ExtModified splitCA_1 = new Arc2ExtModified(
								top.centre, top.radius, top.sourceAngle(),
								angleOfT, top.orientation());
						Arc2ExtModified splitCA_2 = new Arc2ExtModified(
								top.centre, top.radius, angleOfT,
								top.targetAngle(), top.orientation());
						// T ist der Beruehrpunkt der Tangente, die durch D geht, an top.
						Point2 T = splitCA_1.target();
						// neue Segmente erstellen (D,T) & (A,T)
						Segment2Ext DT = new Segment2Ext(D, T);
						Segment2Ext BT = new Segment2Ext(B, T);
						// neue Dreiecke erstellen
						SimpleList list = new SimpleList();
						list.add(topVS);
						list.add(DT);
						list.add(splitCA_1);
						PseudoTriangle2 pt1 = new PseudoTriangle2(list);
						list.clear();
						list.add(bottomVS);
						list.add(BT);
						list.add(DT);
						PseudoTriangle2 pt2 = new PseudoTriangle2(list);
						list.clear();
						list.add(splitCA_2);
						list.add(BT);
						list.add(bottom);
						PseudoTriangle2 pt3 = new PseudoTriangle2(list);
						// Beziehungen erzeugen
						DT.add_PseudoTriangle(pt1);
						DT.add_PseudoTriangle(pt2);
						BT.add_PseudoTriangle(pt2);
						BT.add_PseudoTriangle(pt3);
						addPTtoObject2(topVS, pt1, true);
						addPTtoObject2(bottomVS, pt2, true);
						addPTtoObject2(splitCA_1, pt1);
						addPTtoObject2(splitCA_2, pt3);
						addPTtoObject(bottom, pt3);
						// Ergebniss Rueckgabe
						SimpleList listOfTri = new SimpleList();
						listOfTri.add(pt1);
						listOfTri.add(pt2);
						listOfTri.add(pt3);
						return listOfTri;
					}
				}
			}
			return null;
		}


		/**
		 * HILFSFUNKTION fuer triangulation(Point2) Diese Funktion fuegt
		 * entsprechend der Klasse das Dreieck pt dem Objekt o zu.
		 * 
		 * @param o
		 *            Das Objekt, dem hinzugefügt wird
		 * @param pt
		 *            Das Dreieck
		 */
		private void addPTtoObject(
				Object o,
				PseudoTriangle2 pt)
		{
			if (o instanceof Segment2Ext)
			{
				SimpleList triaList = ((Segment2Ext) o).get_PseudoTriangles();
				try
				{
					triaList.remove(this);
				}
				catch (Exception e)
				{}
				triaList.add(pt);
			}
			else if (o instanceof Arc2ExtModified)
			{
				((Arc2ExtModified) o).set_pseudoTriangle(pt);
			}
			else if (o instanceof VerticalSegment)
			{
				if (this.equals(((VerticalSegment) o).getPseudoTriaLeft()))
				{
					((VerticalSegment) o).setPseudoTriaLeft(pt);
				}
				else
				{
					((VerticalSegment) o).setPseudoTriaRight(pt);
				}
			}
		}


		/**
		 * HILFSFUNKTION fuer divideTriangle(VerticalSegment, VerticalSegment,
		 * VerticalSegment) Diese Funktion fuegt entsprechend der Klasse das
		 * Dreieck pt dem Objekt o zu.
		 * 
		 * @param o
		 *            Das Objekt
		 * @param pt
		 *            Das Dreieck
		 * @param left
		 *            Schalter, wo eingefügt wird
		 */
		private void addPTtoObject2(
				Object o,
				PseudoTriangle2 pt,
				boolean left)
		{
			if (o instanceof VerticalSegment)
			{
				if (left)
				{
					((VerticalSegment) o).setPseudoTriaLeft(pt);
				}
				else
				{
					((VerticalSegment) o).setPseudoTriaRight(pt);
				}
			}
		}


		/**
		 * HILFSFUNKTION fuer divideTriangle(VerticalSegment, VerticalSegment,
		 * VerticalSegment) Diese Funktion fuegt entsprechend der Klasse das
		 * Dreieck pt dem Objekt o zu.
		 * 
		 * @param o
		 *            Das Objekt
		 * @param pt
		 *            Das Dreieck
		 */
		private void addPTtoObject2(
				Object o,
				PseudoTriangle2 pt)
		{
			if (o instanceof Segment2Ext)
			{
				SimpleList triaList = ((Segment2Ext) o).get_PseudoTriangles();
				try
				{
					triaList.remove(this);
				}
				catch (Exception e)
				{}
				triaList.add(pt);
			}
			else if (o instanceof Arc2ExtModified)
			{
				((Arc2ExtModified) o).set_pseudoTriangle(pt);
			}
		}


		/**
		 * NICHTE IMPLEMENTIERT
		 * 
		 * @param box
		 *            Das Rechteck
		 * 
		 * @return false
		 */
		public boolean intersects(
				Rectangle2D box)
		{
			// TODO Auto-generated method stub
			return false;
		}


		/**
		 * Gibt eine Liste mit den Objekten des Dreiecks zurueck
		 * 
		 * @return SimpleList der Objekte
		 */
		public SimpleList getItems()
		{
			return items;
		}


		/**
		 * Liefert den kleinsten X-Wert des Dreiecks.
		 * 
		 * @return Der kleinste X-Wert
		 */
		public float getMinX()
		{
			return minX;
		}


		/**
		 * Liefert den kleinsten Y-Wert des Dreiecks.
		 * 
		 * @return Der kleinste Y-Wert
		 */
		public float getMinY()
		{
			return minY;
		}


		/**
		 * Liefert den hoehsten X-Wert des Dreiecks.
		 * 
		 * @return Der größte X-Wert
		 */
		public float getMaxX()
		{
			return maxX;
		}


		/**
		 * Liefert den hoehsten Y-Wert des Dreiecks.
		 * 
		 * @return Der größte Y-Wert
		 */
		public float getMaxY()
		{
			return maxY;
		}


		/**
		 * Liefert den Schwerpunkt des Dreiecks zurueck.
		 * 
		 * Achtung: Zum berechnen des Schwerpunktes werden nur die Eckpunkte
		 * benutzt.
		 * 
		 * @return Das Massenzentrum
		 */
		public Point2 getCentreOfMass()
		{
			return centreOfMass;
		}


		/**
		 * Setzen der IdentNummer
		 * 
		 * @param nr
		 *            Die Identnummer
		 */
		public void setIdentNumber(
				int nr)
		{
			_identNumber = nr;
		}


		/**
		 * Gibt die IdentNummer aus
		 * 
		 * @return Die IdentNummer
		 */
		public int getIdentNumber()
		{
			return _identNumber;
		}


		/**
		 * Es wird eine Liste mit allen bekannten Nachbardreiecken ermittelt und
		 * zurueck gegeben. Diese wird durch die Nachbarschafts- Informationen
		 * aus VerticalSegment und Segment2Ext realisiert.
		 * 
		 * @return Liste der Nachbarn
		 */
		public SimpleList getNeighbours()
		{
			SimpleList myList = new SimpleList();
			for (ListItem lItem = items.first(); lItem != null; lItem = lItem
					.next())
			{
				if (lItem.value() instanceof VerticalSegment)
				{
					if (this.equals(((VerticalSegment) lItem.value())
							.getPseudoTriaLeft()))
					{
						if (((VerticalSegment) lItem.value())
								.getPseudoTriaRight() != null)
							myList.add(((VerticalSegment) lItem.value())
									.getPseudoTriaRight());
					}
					else
					{
						if (((VerticalSegment) lItem.value())
								.getPseudoTriaLeft() != null)
							myList.add(((VerticalSegment) lItem.value())
									.getPseudoTriaLeft());
					}
				}
				else if (lItem.value() instanceof Segment2Ext)
				{
					SimpleList list = ((Segment2Ext) lItem.value())
							.get_PseudoTriangles();
					for (ListItem lItem2 = list.first(); lItem2 != null; lItem2 = lItem2
							.next())
					{
						if (!lItem2.value().equals(this))
						{
							myList.add(lItem2.value());
						}
					}
				}
			}
			return myList;
		}


		/**
		 * Liefert das Objekt, dass die Eckpunkte A und B miteinander verbindet.
		 * 
		 * @return Das Objekt
		 */
		public Object getAB()
		{
			return AB;
		}


		/**
		 * Liefert das Objekt, dass die Eckpunkte B und C miteinander verbindet.
		 * 
		 * @return Das Objekt
		 */
		public Object getBC()
		{
			return BC;
		}


		/**
		 * Liefert das Objekt, dass die Eckpunkte C und A miteinander verbindet.
		 * 
		 * @return Das Objekt
		 */
		public Object getCA()
		{
			return CA;
		}


		/**
		 * Liefert den Eckpunkt A.
		 * 
		 * @return Eckpunkt A
		 */
		public Point2 getA()
		{
			return A;
		}


		/**
		 * Liefert den Eckpunkt B.
		 * 
		 * @return Eckpunkt B
		 */
		public Point2 getB()
		{
			return B;
		}


		/**
		 * Liefert den Eckpunkt C.
		 * 
		 * @return Eckpunkt C
		 */
		public Point2 getC()
		{
			return C;
		}
	}


	/**
	 * Zu dieser Klasse gibt es keine Beschreibung.
	 */
	public class Segment2Ext
			extends Segment2
	{

		private static final long	serialVersionUID		= 1L;

		/** Liste von pseudo Vierecken, in denen sich dieses Segment befindet. */
		private SimpleList			_partOfPseudoTrapezoids	= null;

		/** Liste von pseudo Dreiecken, in denen sich dieses Segment befindet. */
		private SimpleList			_partOfPseudoTriangles	= null;

		/**
		 * Liste von verticalen Segmenten (VerticalSegment), die an dieses
		 * Segment grenzen.
		 */
		private SimpleList			_adjacentVS				= null;


		/**
		 * Standardkonstruktor
		 * 
		 */
		public Segment2Ext()
		{
			super();
			_partOfPseudoTriangles = new SimpleList();
			_partOfPseudoTrapezoids = new SimpleList();
			_adjacentVS = new SimpleList();
		}


		/**
		 * Konstruktor
		 * 
		 * @param input_source_x
		 *            X-Wert des Startpunkts
		 * @param input_source_y
		 *            Y-Wert des Startpunkts
		 * @param input_target_x
		 *            X-Wert des Endpunkts
		 * @param input_target_y
		 *            Y-Wert des Endpunkts
		 */
		public Segment2Ext(
				float input_source_x,
				float input_source_y,
				float input_target_x,
				float input_target_y)
		{
			super(input_source_x, input_source_y, input_target_x,
					input_target_y);
			_partOfPseudoTriangles = new SimpleList();
			_partOfPseudoTrapezoids = new SimpleList();
			_adjacentVS = new SimpleList();
		}


		/**
		 * Konstruktor
		 * 
		 * @param input_source
		 *            Erster Punkt
		 * @param input_target
		 *            Zweiter Punkt
		 */
		public Segment2Ext(
				Point2 input_source,
				Point2 input_target)
		{
			super(input_source, input_target);
			_partOfPseudoTriangles = new SimpleList();
			_partOfPseudoTrapezoids = new SimpleList();
			_adjacentVS = new SimpleList();
		}


		/**
		 * Konstruktor
		 * 
		 * @param seg
		 *            Das zu kopierende Objekt
		 */
		public Segment2Ext(
				Segment2 seg)
		{
			super(seg);
			_partOfPseudoTrapezoids = new SimpleList();
			_partOfPseudoTriangles = new SimpleList();
			_adjacentVS = new SimpleList();
		}


		/**
		 * Übergibt Liste von Objekten
		 * 
		 * @param ofPseudoTrapezoids
		 *            Die Objekte
		 */
		public void set_list_partOfPseudoTrapezoids(
				SimpleList ofPseudoTrapezoids)
		{
			_partOfPseudoTrapezoids = ofPseudoTrapezoids;
		}


		/**
		 * Übergibt Liste von Objekten
		 * 
		 * @param ofPseudoTriangles
		 *            Die Objekte
		 */
		public void set_list_partOfPseudoTriangles(
				SimpleList ofPseudoTriangles)
		{
			_partOfPseudoTriangles = ofPseudoTriangles;
		}


		/**
		 * Übergibt Liste von Objekten
		 * 
		 * @param _adjacentvs
		 *            Die Objekte
		 */
		public void set_listOfAdjacentVS(
				SimpleList _adjacentvs)
		{
			_adjacentVS = _adjacentvs;
		}


		/**
		 * Liefert Liste von Objekten
		 * 
		 * @return Die Objekte
		 */
		public SimpleList get_PseudoTrapezoids()
		{
			return _partOfPseudoTrapezoids;
		}


		/**
		 * Fügt ein Objekt hinzu
		 * 
		 * @param pt
		 *            Das Objekt
		 */
		public void add_PseudoTrapezoid(
				PseudoTrapezoid2 pt)
		{
			_partOfPseudoTrapezoids.add(pt);
		}


		/**
		 * Liefert Liste von Objekten
		 * 
		 * @return Die Objekte
		 */
		public SimpleList get_PseudoTriangles()
		{
			return _partOfPseudoTriangles;
		}


		/**
		 * Fügt ein Objekt hinzu
		 * 
		 * @param pt
		 *            Das Objekt
		 */
		public void add_PseudoTriangle(
				PseudoTriangle2 pt)
		{
			_partOfPseudoTriangles.add(pt);
		}


		/**
		 * Liefert Liste von Objekten
		 * 
		 * @return Die Objekte
		 */
		public SimpleList get_adjacentVS()
		{
			return _adjacentVS;
		}


		/**
		 * Fügt ein Objekt hinzu
		 * 
		 * @param vs
		 *            Das Objekt
		 */
		public void add_adjacentVS(
				VerticalSegment vs)
		{
			_adjacentVS.add(vs);
		}

	}


	/**
	 * Für diese Klasse existiert keine Beschreibung
	 */
	public class SegmentToCloseSweep
			extends Sweep
	{

		private float				_radius;
		private double				_oldX				= Double.NEGATIVE_INFINITY;

		// Ereignis-IDs

		/** Ereignis-ID: linker Endpunkt erreicht */
		public final static byte	LEFT_ENDPOINT		= 1;

		/** Ereignis-ID: Schnittpunkt erreicht */
		public final static byte	INTERSECTION		= 2;

		/** Ereignis-ID: rechter Endpunkt erreicht */
		public final static byte	RIGHT_ENDPOINT		= 3;

		// Ausgabemodus-Konstanten

		/** Ausgabemodus: Nur Schnittpunkte werden ausgegeben */
		public final static byte	POINTS				= 0;
		/**
		 * Ausgabemodus: Schnittpunkte und beteiligte Segmente werden ausgegeben
		 */
		public final static byte	POINTS_AND_SEGMENTS	= 1;

		// Schnittmodus-Konstanten

		/** Schnittmodus: Alle Schnittpunkte werden berechnet */
		public final static byte	ALL_INTERSECTIONS	= 0;

		/** Schnittmodus: Nur echte Schnitte werden berechnet */
		public final static byte	REAL_INTERSECTIONS	= 1;
		/**
		 * Schnittmodus: Nur Schnittpunkte, die nicht an den
		 * Segment-Target-Punkten liegen, werden ausgegeben
		 */
		public final static byte	WITHOUT_TARGETS		= 2;

		/** gesetzter Ausgabemodus */
		private byte				_output_mode		= POINTS;

		/** gesetzter Schnittmodus */
		private byte				_intersection_mode	= ALL_INTERSECTIONS;

		/** Liste mit den Ausgabepunkten. */
		private List				_points;

		/** Spezielervergleicher fuer die SSS */
		private SweepComparitor		_swingcomparitor;

		/** letzter erreichter Schnittpunkt */
		private Point2				_lastcut			= null;

		/** letztes Event */
		private SweepEvent			_lastEvent			= null;

		private float				_Epsilon			= 0.0f;

		/**
		 * Alle Events des Sweeps. Wird erst nach dem abarbeiten des letzten
		 * events gesetzt.
		 */
		private BinarySearchTree	eventTree;


		// Setzen von Modi
		// ===============

		/**
		 * Liefert den gesetzten Ausgabemodus.
		 * 
		 * @return Ausgabemodus (eine Konstante aus {POINTS,
		 *         POINTS_AND_SEGMENTS})
		 */
		public byte outputMode()
		{
			return _output_mode;
		}


		/**
		 * Setzt den Ausgabemodus auf die uebergebene Konstante.
		 * 
		 * @param mode
		 *            neuer Ausgabemodus (eine Konstante aus {POINTS,
		 *            POINTS_AND_SEGMENTS})
		 */
		public void setOutputMode(
				byte mode)
		{
			_output_mode = mode;
		}


		/**
		 * Liefert den gesetzten Schnittmodus.
		 * 
		 * @return Schnittmodus (eine Konstante aus {ALL_INTERSECTIONS,
		 *         ALL_INTERSECTIONS, WITHOUT_TARGETS}
		 */
		public byte intersectionMode()
		{
			return _intersection_mode;
		}


		/**
		 * Setzt den Schnittmodus auf die uebergebene Konstante.
		 * 
		 * @param mode
		 *            neuer Schnittmodus (eine Konstante aus {ALL_INTERSECTIONS,
		 *            ALL_INTERSECTIONS, WITHOUT_TARGETS}
		 */
		public void setIntersectionMode(
				byte mode)
		{
			_intersection_mode = mode;
		}


		// Algorithmus
		// ===========

		/**
		 * Fuehrt einen Plane-Sweep durch, um alle Schnittpunkte der in der
		 * Liste L uebergebenen Segmente zu bestimmen. Die Ausgabeliste enthaelt
		 * alle Schnittpunkte sortiert nach X-Koordinaten
		 * (PointComparitor.X_ORDER).<br>Uebersicht des Verhaltens bei
		 * verschiedenen Schnittmodi:
		 * 
		 * <table border="1" cellpadding="2"> <tr> <td>Konstante</td>
		 * <td>erkannte Schnitte</td> </tr> <tr> <td>ALL_INTERSECTIONS</td>
		 * <td>alle Schnitte der Segmente</td> </tr> <tr>
		 * <td>REAL_INTERSECTIONS</td> <td>Nur 'echte' Schnitte, d.h keine
		 * Schnittpunkte, die auf die Enden eines Segmentes fallen.</td> </tr>
		 * <tr> <td>WITHOUT_TARGETS</td> <td>Nur Schnittpunkte, die kein
		 * Endpunkt eines Segmentes sind.</td> </tr> </table>
		 * 
		 * Uebersicht des Verhaltens bei verschiedenen Ausgabemodi:
		 * 
		 * <table border="1" cellpadding="2"> <tr> <td>Konstante</td>
		 * <td>Ausgabeformat</td> </tr> <tr> <td>POINTS</td> <td>eine Liste mit
		 * den Schnittpunkten (keine doppelten Punkte)</td> </tr> <tr>
		 * <td>POINTS_AND_SEGMENTS</td> <td>eine Liste mit den Schnittpunkten
		 * als Schluessel und einem Segment2-Array mit den daran beteiligten
		 * Segmenten (das sind immer nur 2!). Bei Mehrfachschnittpunkten wird
		 * derselbe Schnittpunkt mehrfach berichtet (mit jeweils den zwei
		 * beteiligten Segmenten).</td> </tr> </table>
		 * 
		 * @param L
		 *            Segmentliste (Liste mit Segment2-Objekten)
		 * @param radius
		 *            Der Radius
		 * @param eTree
		 *            Der Suchbaum
		 * @param until
		 *            Abbruchbedingung
		 * 
		 * @return Liste mit den Schnittpunkten
		 * 
		 * @see PointComparitor#X_ORDER
		 */
		public List segmentIntersection(
				SimpleList L,
				float radius,
				BinarySearchTree eTree,
				int until)
		{
			_radius = radius;
			if (L == null)
				return new List();
			PointComparitor pcompare = new PointComparitor(
					PointComparitor.X_ORDER);
			createEventStructure(new SweepEventComparitor(pcompare), true);
			_swingcomparitor = new SweepComparitor();
			createSSS(_swingcomparitor);
			sss().setAllowDuplicates(true);
			event().setAllowDuplicates(true);
			Segment2Ext s;
			Arc2ExtModified a;
			for (ListItem i = L.first(); i != null; i = i.next())
			{
				if (i.value() instanceof Segment2Ext)
				{
					s = (Segment2Ext) i.key();
					if (PointComparitor.compareX(s.source(), s.target()) == Comparitor.SMALLER)
					{
						super.insertEvent(LEFT_ENDPOINT, s.source(), s);
						super.insertEvent(RIGHT_ENDPOINT, s.target(), s);
					}
					else
					{
						super.insertEvent(LEFT_ENDPOINT, s.target(), s);
						super.insertEvent(RIGHT_ENDPOINT, s.source(), s);
					}
				}
				if (i.value() instanceof Arc2ExtModified)
				{
					a = (Arc2ExtModified) i.key();
					if (PointComparitor.compareX(a.source(), a.target()) == Comparitor.SMALLER)
					{
						//Point2 s1 = a.source();
						super.insertEvent(LEFT_ENDPOINT, a.source(), a);
						super.insertEvent(RIGHT_ENDPOINT, a.target(), a);
					}
					else
					{
						//Point2 s1 = a.source();
						super.insertEvent(LEFT_ENDPOINT, a.target(), a);
						super.insertEvent(RIGHT_ENDPOINT, a.source(), a);
					}
				}
			}

			_points = new List();

			TreeItem tItem = eTree.first();
			while (tItem != null)
			{
				super.insertEvent(((SweepTreeItem) tItem).getID(),
						((SweepTreeItem) tItem).key(),
						((SweepTreeItem) tItem).value());
				//_points.add(((SweepTreeItem)tItem).key(), ((SweepTreeItem)tItem).value());
				tItem = eTree.next(tItem);
			}

			_lastcut = null;
			eventTree = new RedBlackTree(pcompare, true);
			eventTree = eTree;
			eventTree.setAllowDuplicates(true);
			execute(until);

			TreeItem treeItem = sss().first();
			for (treeItem = sss().first(); treeItem != null; treeItem = sss()
					.next(treeItem))
			{
				_points.add(treeItem.value());
			}
			Segment2Ext seg = new Segment2Ext(((Point2) _lastEvent.key()).x,
					-1000, ((Point2) _lastEvent.key()).x, 1000);
			_points.add(seg);

			if (until == 0)
			{
				_points.clear();
				tItem = eventTree.first();
				_points = new List();
				while (tItem != null)
				{
					_points.add(tItem.key(), tItem.value());
					tItem = eventTree.next(tItem);
				}
			}

			return _points;
		}


		/**
		 * Verarbeitet das SweepEvent e.
		 * 
		 * @param e
		 *            SweepEvent
		 */
		public void processEvent(
				SweepEvent e)
		{
			if (_lastEvent != null)
			{
				if (((Point2) _lastEvent.key()).equals((Point2) e.key()) == false)
				{
					_swingcomparitor.setX(((Point2) e.key()).x);
					// In der SSS muessen alle Datensaetze mit dem Key = e.key
					// neu sortiert werden, damit die Reihenfolge der Datensaetze
					// (nach den Y-Werten) bei mehrfach Ueberschneidungen
					// am Punkt e.key.x erhalten bleibt.
					SimpleList itemList = new SimpleList();
					boolean found = true;
					TreeItem item = null;
					_swingcomparitor.setDelta(SweepComparitor.BEFORE);
					_swingcomparitor.setTolerance(0.01f);
					while (found == true)
					{
						item = sss().find(e.key());
						if (item != null)
						{
							itemList.add(item.value());
							sss().removeSym(item);
						}
						else
						{
							found = false;
						}
					}
					_swingcomparitor.setDelta(SweepComparitor.AFTER);
					_swingcomparitor.setTolerance(0.02f);
					while (itemList.empty() == false)
					{
						Point2 s1_At_X = null;
						Point2 a2_At_X = null;
						Point2 c1_At_X = null;
						Intersection myInter = new Intersection();
						Point2 actPoint = (Point2) e.key();
						SimpleList myList = new SimpleList();
						if (itemList.firstValue() instanceof Segment2Ext)
						{
							s1_At_X = ((Segment2Ext) itemList.firstValue())
									.calculatePoint(actPoint.x);
						}
						else if (itemList.firstValue() instanceof Arc2ExtModified)
						{
							Segment2Ext myLine = new Segment2Ext(actPoint.x,
									actPoint.y + _radius * 2, actPoint.x,
									actPoint.y - _radius * 2);
							Arc2ExtModified arc = (Arc2ExtModified) itemList
									.firstValue();
							Circle2 cir = new Circle2(arc.centre, arc.radius);
							c1_At_X = cir.intersection(myLine, myInter);
							a2_At_X = ((Arc2ExtModified) itemList.firstValue())
									.intersection(myLine, myInter);
							float y1 = (float) (arc.centre.y + Math
									.sqrt((arc.radius * arc.radius)
											- ((arc.centre.x - actPoint.x) * (arc.centre.x - actPoint.x))));
							float y2 = (float) (arc.centre.y - Math
									.sqrt((arc.radius * arc.radius)
											- ((arc.centre.x - actPoint.x) * (arc.centre.x - actPoint.x))));
							float y3 = (float) (arc.centre.y - Math
									.sqrt((arc.radius * arc.radius)
											- ((arc.centre.getX() - actPoint
													.getX()) * (arc.centre
													.getX() - actPoint.getX()))));
							myList = getIntersections(arc, myLine);
						}

						TreeItem k = sss().add(itemList.Pop());
						testIntersection(k, true, (Point2) e.key());
						testIntersection(k, false, (Point2) e.key());

						if (true)
						{}
					}
				}
				else
				{

				}
			}
			else
			{
				_swingcomparitor.setX(((Point2) e.key()).x);
			}
			TreeItem i, j, startItem, endItem, k;
			boolean end;
			switch (e.getID())
			{
				case LEFT_ENDPOINT:
					_swingcomparitor.setDelta(LineComparitor.EXACT);

					i = sss().add(e.value());

					startItem = sss().findSmaller(e.key());
					if (startItem == null)
						startItem = i;
					endItem = sss().findBigger(e.key());
					if (endItem == null)
						endItem = i;
					k = startItem;
					end = false;
					do
					{
						if (k != i)
							testIntersection(k, i, (Point2) e.key());
						if (k.equals(endItem))
							end = true;
						else
							k = sss().next(k);
					}
					while ((!end) && (k != null));

					testIntersection(i, true, (Point2) e.key());
					testIntersection(i, false, (Point2) e.key());
					break;
				case RIGHT_ENDPOINT:
					_swingcomparitor.setDelta(LineComparitor.EXACT);

					i = sss().find(e.value());

					startItem = sss().findSmaller(e.key());
					if (startItem == null)
						startItem = i;
					endItem = sss().findBigger(e.key());
					if (endItem == null)
						endItem = i;
					k = startItem;
					end = false;
					do
					{
						if (k != i)
							testIntersection(k, i, (Point2) e.key());
						if (k.equals(endItem))
							end = true;
						else
							k = sss().next(k);
					}
					while ((!end) && (k != null));

					if (i == null)
					{
						// Etwas ist schief gelaufen. Die SSS scheint nicht mehr sortiert zu sein =>
						// SSS auslesen und neu erstelen.
						SimpleList itemList = new SimpleList();
						TreeItem myItem = sss().first();
						while (myItem != null)
						{
							itemList.add(myItem.value());
							myItem = sss().next(myItem);
						}
						sss().removeTree(sss().root());
						ListItem lItem = itemList.first();
						while (lItem != null)
						{
							sss().add(lItem.value());
							lItem = lItem.next();
						}
						i = sss().find(e.value());
						if (i == null)
							return;
					}
					if (!e.value().equals(i.value()))
					{
						sss();
					}
					j = sss().prev(i);
					// Falls vertikales Segment endet, muss der Y-Wert des LineComparitors wieder zurueckgesetzt werden
					if ((e.value() instanceof Segment2Ext)
							&& ((Segment2Ext) e.value()).isVertical())
						_swingcomparitor.resetY();
					sss().removeSym(i);
					testIntersection(j, true, (Point2) e.key());
					testIntersection(j, false, (Point2) e.key());
					break;
				case INTERSECTION:
			}
			_lastEvent = e;
		}


		/**
		 * Fügt ein Objekt hinzu
		 * 
		 * @param o1
		 *            Das Objekt
		 */
		public void add(
				Object o1)
		{
			TreeItem ti = find(o1);
			if (ti == null)
			{
				SimpleList list = new SimpleList();
				list.add(o1);
				sss().add(list);
			}
			else
			{
				SimpleList list = (SimpleList) ti.value();
				list.add(01);
				sss().add(list);
			}
		}


		public void remove(
				Object o1)
		{
			TreeItem ti = find(o1);
			if (ti == null)
			{}
			else
			{
				SimpleList list = (SimpleList) ti.value();
				if (list.length() > 1)
				{
					ListItem li = list.find(o1);
					list.remove(li);
				}
				else
				{
					sss().remove(ti);
				}
			}
		}


		/**
		 * Sucht in der SSS nach dem key. Sollte der key sich nicht in der SSS
		 * befinden, so wird der sich am naechsten befinden key zurückgegeben.
		 * 
		 * @param key
		 *            Das zu suchende Objekt
		 * 
		 * @return Das gefundene Objekt
		 */
		public TreeItem find(
				Object key)
		{
			if (sss().root() == null)
			{
				return null;
			}
			TreeItem start = sss().root();
			TreeItem j = null, i = start;
			short comp = Comparitor.SMALLER, lef = Comparitor.SMALLER;
			if (!sss().leftIsSmaller())
			{
				lef = Comparitor.BIGGER;
			}
			while ((i != null) && (comp != Comparitor.EQUAL))
			{
				comp = sss().comparitor().compare(key, i);
				j = i;
				if (comp == lef)
				{
					i = i.child(TreeItem.LEFT);
				}
				else
				{
					i = i.child(TreeItem.RIGHT);
				}
			}
			return j;
		}


		/**
		 * Diese Funktion ermittelt, ob sich ein Punkt p innerhalb des
		 * umschliessenden Vierecks des Bogens a befindet.
		 * 
		 * @param a
		 *            Der Bogen
		 * @param p
		 *            Der Punkt
		 * 
		 * @return wahr oder falsch
		 */
		private boolean contains(
				Arc2ExtModified a,
				Point2 p)
		{
			Rectangle2D bRect = calcBoundingRect(a, 0.0);
			if ((p.x >= bRect.getMinX()) && (p.x <= bRect.getMaxX())
					&& (p.y >= bRect.getMinY()) && (p.y <= bRect.getMaxY()))
				return true;
			return false;
		}


		/**
		 * Diese Funktion ermittelt den bzw. die Schnittpunkte eines Bogens a
		 * und eines Segmentes s.
		 * 
		 * @param a
		 *            Der Bogen
		 * @param s
		 *            Das Segment
		 * 
		 * @return entweder eine SimpleList oder null
		 */
		private SimpleList getIntersections(
				Arc2ExtModified a,
				Segment2Ext s)
		{
			double slope = ((s.source().y - s.target().y) / (s.source().x - s
					.target().x));
			double y_abs = s.source().y - slope * s.source().x;
			double p = (-2 * a.centre.x + 2 * slope * (y_abs - a.centre.y))
					/ (Math.pow(slope, 2) + 1);
			double q = (Math.pow(a.centre.x, 2)
					+ Math.pow(y_abs - a.centre.y, 2) - Math.pow(a.radius, 2))
					/ (Math.pow(slope, 2) + 1);
			double wurzel = Math.pow(p / 2, 2) - q;
			if (wurzel > 0)
			{
				double x1 = -(p / 2) + Math.sqrt(wurzel);
				double x2 = -(p / 2) - Math.sqrt(wurzel);
				double y1 = slope * x1 + y_abs;
				double y2 = slope * x2 + y_abs;
				SimpleList l = new SimpleList();
				l.add(new Point2(x1, y1));
				l.add(new Point2(x2, y2));
				return l;
			}
			else if (wurzel == 0)
			{
				double x1 = -(p / 2);
				double y1 = slope * x1 + y_abs;
				SimpleList l = new SimpleList();
				l.add(new Point2(x1, y1));
				return l;
			}
			return null;
		}


		/**
		 * Diese Funktion ermittelt, ob sich ein Punkt p innerhalb des
		 * umschliessenden Vierecks des Bogens a befindet. Es kann zusaetzlich
		 * eine Toleranz angegeben werden.
		 * 
		 * @param a
		 *            Der Bogen
		 * @param p
		 *            Der Punkt
		 * @param tolerance
		 *            Die Toleranz
		 * 
		 * @return wahr oder falsch
		 */
		private boolean contains(
				Arc2ExtModified a,
				Point2 p,
				double tolerance)
		{
			Rectangle2D bRect = calcBoundingRect(a, tolerance);
			if ((p.x >= bRect.getMinX()) && (p.x <= bRect.getMaxX())
					&& (p.y >= bRect.getMinY()) && (p.y <= bRect.getMaxY()))
				return true;
			return false;
		}


		/**
		 * Sucht danach, ob ein bestimmtes Ereignis vorkommt.
		 * 
		 * @param id
		 *            ID
		 * @param key
		 *            Der Schlüssel des Objekts
		 * @param value
		 *            Der Wert des Objekts
		 * 
		 * @return true falls vorhanden, false sonst
		 */
		private boolean searchForEvent(
				int id,
				Object key,
				Object value)
		{
			double toleranz = 0.001;
			BinarySearchTree allE = event();
			Point2 minKey = new Point2(((Point2) key).x - toleranz,
					((Point2) key).y);
			Point2 maxKey = new Point2(((Point2) key).x + toleranz,
					((Point2) key).y);
			TreeItem first = allE.findSmaller(minKey);
			if (first == null)
				first = allE.findSmaller(key);
			TreeItem last = allE.findBigger(maxKey);
			if (last == null)
				last = allE.findBigger(key);
			for (TreeItem i = first; (i != null)
					&& (((Point2) i.key()).x <= ((Point2) last.key()).x); i = allE
					.next(i))
			{
				if (i instanceof SweepEvent)
				{
					SweepEvent e = (SweepEvent) i;
					if ((e.getID() == id)
							&& (((Point2) e.key()).equals((Point2) key, 0.0001)))
					{
						if (((((SimpleList) value).firstValue()
								.equals(((SimpleList) e.value()).lastValue())) && ((((SimpleList) e
								.value()).firstValue()
								.equals(((SimpleList) value).lastValue()))))
								|| ((((SimpleList) value).firstValue()
										.equals(((SimpleList) e.value())
												.firstValue())) && ((((SimpleList) e
										.value()).lastValue()
										.equals(((SimpleList) value)
												.lastValue())))))
						{
							return true;
						}
					}
				}
			}
			return false;
		}


		/**
		 * Fügt ein Event hinzu
		 * 
		 * @param id
		 *            Die ID
		 * @param key
		 *            der Schlüssel
		 * @param value
		 *            Der Wert
		 */
		public void insertEvent(
				int id,
				Object key,
				Object value)
		{
			if (!searchForEvent(id, key, value))
			{
				super.insertEvent(id, key, value);
				eventTree.add(new SweepTreeItem(id, key, value));
			}
		}


		/**
		 * Berechnet das umschließende Rechteck
		 * 
		 * @param a
		 *            Der Bogen
		 * @param tolerance
		 *            Die Toleranz
		 * 
		 * @return Das Rechteck
		 */
		private Rectangle2D calcBoundingRect(
				Arc2ExtModified a,
				double tolerance)
		{
			double minX = 0, maxX = 0, minY = 0, maxY = 0;
			if (a.orientation() == Angle.ORIENTATION_LEFT)
			{ //gegen den Uhrzeigersinn
				if ((a.sourceTrig().deg() < 180.0
						&& a.targetTrig().deg() > 180.0 && a.sourceTrig().deg() < a
						.targetTrig().deg())
						|| (a.sourceTrig().deg() > 180.0
								&& a.targetTrig().deg() > 180.0 && a
								.sourceTrig().deg() > a.targetTrig().deg())
						|| (a.sourceTrig().deg() < 180.0
								&& a.targetTrig().deg() < 180.0 && a
								.sourceTrig().deg() > a.targetTrig().deg()))
				{
					minX = a.centre.x - a.radius;
				}
				else
				{
					minX = Math.min(a.source().x, a.target().x);
				}
				if (a.sourceTrig().deg() > a.targetTrig().deg())
				{
					maxX = a.centre.x + a.radius;
				}
				else
				{
					maxX = Math.max(a.source().x, a.target().x);
				}
				if ((a.sourceTrig().deg() < 90.0 && a.targetTrig().deg() > 90.0 && a
						.sourceTrig().deg() < a.targetTrig().deg())
						|| (a.sourceTrig().deg() > 90.0
								&& a.targetTrig().deg() > 90.0 && a
								.sourceTrig().deg() > a.targetTrig().deg())
						|| (a.sourceTrig().deg() < 90.0
								&& a.targetTrig().deg() < 90.0 && a
								.sourceTrig().deg() > a.targetTrig().deg()))
				{
					maxY = a.centre.y + a.radius;
				}
				else
				{
					maxY = Math.max(a.source().y, a.target().y);
				}
				if ((a.sourceTrig().deg() < 270.0
						&& a.targetTrig().deg() > 270.0 && a.sourceTrig().deg() < a
						.targetTrig().deg())
						|| (a.sourceTrig().deg() > 270.0
								&& a.targetTrig().deg() > 270.0 && a
								.sourceTrig().deg() > a.targetTrig().deg())
						|| (a.sourceTrig().deg() < 270.0
								&& a.targetTrig().deg() < 270.0 && a
								.sourceTrig().deg() > a.targetTrig().deg()))
				{
					minY = a.centre.y - a.radius;
				}
				else
				{
					minY = Math.min(a.source().y, a.target().y);
				}
			}
			else if (a.orientation() == Angle.ORIENTATION_RIGHT)
			{ //im Uhrzeigersinn
				if ((a.sourceTrig().deg() > 180.0
						&& a.targetTrig().deg() < 180.0 && a.sourceTrig().deg() > a
						.targetTrig().deg())
						|| (a.sourceTrig().deg() < 180.0
								&& a.targetTrig().deg() < 180.0 && a
								.sourceTrig().deg() < a.targetTrig().deg())
						|| (a.sourceTrig().deg() > 180.0
								&& a.targetTrig().deg() > 180.0 && a
								.sourceTrig().deg() < a.targetTrig().deg()))
				{
					minX = a.centre.x - a.radius;
				}
				else
				{
					minX = Math.min(a.source().x, a.target().x);
				}
				if (a.sourceTrig().deg() < a.targetTrig().deg())
				{
					maxX = a.centre.x + a.radius;
				}
				else
				{
					maxX = Math.max(a.source().x, a.target().x);
				}
				if ((a.sourceTrig().deg() > 90.0 && a.targetTrig().deg() < 90.0 && a
						.sourceTrig().deg() > a.targetTrig().deg())
						|| (a.sourceTrig().deg() < 90.0
								&& a.targetTrig().deg() < 90.0 && a
								.sourceTrig().deg() < a.targetTrig().deg())
						|| (a.sourceTrig().deg() > 90.0
								&& a.targetTrig().deg() > 90.0 && a
								.sourceTrig().deg() < a.targetTrig().deg()))
				{
					maxY = a.centre.y + a.radius;
				}
				else
				{
					maxY = Math.max(a.source().y, a.target().y);
				}
				if ((a.sourceTrig().deg() > 270.0
						&& a.targetTrig().deg() < 270.0 && a.sourceTrig().deg() > a
						.targetTrig().deg())
						|| (a.sourceTrig().deg() < 270.0
								&& a.targetTrig().deg() < 270.0 && a
								.sourceTrig().deg() < a.targetTrig().deg())
						|| (a.sourceTrig().deg() > 270.0
								&& a.targetTrig().deg() > 270.0 && a
								.sourceTrig().deg() < a.targetTrig().deg()))
				{
					minY = a.centre.y - a.radius;
				}
				else
				{
					minY = Math.min(a.source().y, a.target().y);
				}
			}
			minX -= tolerance;
			minY -= tolerance;
			maxX += tolerance;
			maxY += tolerance;
			return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
		}


		/**
		 * Berechnet das umschließende Rechteck
		 * 
		 * @param s
		 *            Das Segment
		 * @param tolerance
		 *            Die Toleranz
		 * 
		 * @return Das Rechteck
		 */
		private Rectangle2D calcBoundingRect(
				Segment2Ext s,
				double tolerance)
		{
			double minX, maxX, minY, maxY;
			minX = Math.min(s.source().x, s.target().x);
			maxX = Math.max(s.source().x, s.target().x);
			minY = Math.min(s.source().y, s.target().y);
			maxY = Math.max(s.source().y, s.target().y);
			minX -= tolerance;
			minY -= tolerance;
			maxX += tolerance;
			maxY += tolerance;
			return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
		}


		/**
		 * Testet den Schnitt des Segments im TreeItem t der SSS mit dem
		 * darueberliegenden - falls up==true, ansonsten mit den
		 * darunterliegenden - Segment. Falls ein Schnitt entdeckt wurde, wird
		 * ein entsprechendes Schnittevent eingefuegt
		 * 
		 * @param t
		 *            TreeItem der SSS
		 * @param up
		 *            falls true, Test mit darueberliegenden (naechsten)
		 *            Segment, ansonsten mit dem darunterliegenden (vorigen)
		 *            Segment
		 * @param key
		 *            Point2
		 */
		private void testIntersection(
				TreeItem t,
				boolean up,
				Point2 key)
		{
			TreeItem i1, i2;
			if (up)
			{
				i1 = sss().next(t);
				i2 = t;
			}
			else
			{
				i1 = t;
				i2 = sss().prev(t);
			}
			testIntersection(i1, i2, key);
		}


		/**
		 * Testet auf Schnitt
		 * 
		 * @param i1
		 *            Baumknoten
		 * @param i2
		 *            Baumknoten
		 * @param key
		 *            Point2
		 */
		private void testIntersection(
				TreeItem i1,
				TreeItem i2,
				Point2 key)
		{
			if ((i1 != null) && (i2 != null))
			{
				if (i1.equals(i2))
					return;
				if ((i1 == null) || (i2 == null))
					return;
				if (i1.key() instanceof Segment2Ext)
				{
					Segment2Ext s1 = (Segment2Ext) i1.key();
					if (i2.key() instanceof Segment2Ext)
					{
						// Fall 1 zwei Segment2 schneiden sich
						Segment2Ext s2 = (Segment2Ext) i2.key();
						Point2 p = null;
						if ((s1.source().equals(s2.source()))
								|| (s1.source().equals(s2.target())))
							p = s1.source();
						else if ((s1.target().equals(s2.source()))
								|| (s1.target().equals(s2.target())))
							p = s1.target();
						if (p == null)
						{
							Intersection inter = new Intersection();
							p = s1.intersection(s2, inter);
						}
						if (p != null)
						{
							SimpleList list = new SimpleList();
							list.add(s1);
							list.add(s2);
							switch (_intersection_mode)
							{
								case ALL_INTERSECTIONS:
									insertEvent(INTERSECTION, p, list);
									break;
								case REAL_INTERSECTIONS:
									if ((!p.equals(s1.source()))
											&& (!p.equals(s1.target()))
											&& (!p.equals(s2.source()))
											&& (!p.equals(s2.target())))
										insertEvent(INTERSECTION, p, list);
									break;
								case WITHOUT_TARGETS:
									if ((!p.equals(s1.target()))
											&& (!p.equals(s2.target())))
										insertEvent(INTERSECTION, p, list);
									break;
							}
						}
					}
					else if (i2.key() instanceof Arc2ExtModified)
					{
						// Fall 2 ein Segment2 und ein Circle2 schneiden sich
						Arc2ExtModified a2 = (Arc2ExtModified) i2.key();
						Point2 sp1 = null;
						if ((s1.source().equals(key) || s1.target().equals(key))
								&& (a2.source().equals(key) || a2.target()
										.equals(key)))
						{
							sp1 = key;
						}
						Intersection inter = new Intersection();
						Circle2 c2 = new Circle2(a2.centre, a2.radius);
						Point2 sp = c2.intersection(s1, inter);

						// Fall 2.1: Es gibt mehrere Schnittpunkte
						if (inter.result == Intersection.LIST)
						{
							// Die Schnittpunkte, die den Bogen nicht schneiden muessen aus der
							// Liste entfernt werden.
							if (!contains(a2, (Point2) inter.list.firstValue(),
									0.0))
							{
								inter.list.Pop();
							}
							if (!contains(a2, (Point2) inter.list.lastValue(),
									0.0))
							{
								inter.list.pop();
							}

							SimpleList l = inter.list;
							if ((l != null) && (l.first() != null))
							{
								ListItem lItem = l.first();
								while ((l != null) & (lItem != null))
								{
									Point2 p = (Point2) lItem.value();
									if (((sp1 != null) && !(p.equals(sp1,
											0.00001))) || (sp1 == null))
									{
										// Events, die einen Key haben dessen X-Wert kleiner ist als der derzeitige
										// X-Eventwert wurden bereits abgehandelt und werden deshalb nicht erneut
										// hinzugefuegt.
										if ((p.getX() >= _oldX)
												&& (s1.liesOn(p)))
										{
											SimpleList list = new SimpleList();
											list.add(s1);
											list.add(a2);
											switch (_intersection_mode)
											{
												case ALL_INTERSECTIONS:
													insertEvent(INTERSECTION,
															p, list);
													break;
												case REAL_INTERSECTIONS:
													if ((!p.equals(s1.source()))
															&& (!p.equals(s1
																	.target())))
														insertEvent(
																INTERSECTION,
																p, list);
													break;
												case WITHOUT_TARGETS:
													if (!p.equals(s1.target()))
														insertEvent(
																INTERSECTION,
																p, list);
													break;
											}
										}
									}
									lItem = lItem.next();
								}
							}
						}
						//Fall 2.2: Es gibt einen Schinttpunkt
						if ((inter.result == Intersection.POINT2)
								&& (contains(a2, sp)) && (sp.getX() >= _oldX))
						{
							if (((sp1 != null) && !(sp.equals(sp1, 0.00001)))
									|| (sp1 == null))
							{
								SimpleList list = new SimpleList();
								list.add(s1);
								list.add(a2);
								switch (_intersection_mode)
								{
									case ALL_INTERSECTIONS:
										insertEvent(INTERSECTION, sp, list);
										break;
									case REAL_INTERSECTIONS:
										if ((!sp.equals(s1.source()))
												&& (!sp.equals(s1.target())))
											insertEvent(INTERSECTION, sp, list);
										break;
									case WITHOUT_TARGETS:
										if (!sp.equals(s1.target()))
											insertEvent(INTERSECTION, sp, list);
										break;
								}
							}
						}
						// Fall 2.3; Es gibt keinen Schnittpunkt 
						if (inter.result == Intersection.EMPTY)
						{
							double minDist = s1.distance(a2.centre);
							if (minDist < (_radius + _Epsilon))
							{
								// Obwohl inter leer ist scheint es dennoch einen 
								// Schnittpunkt zu geben.
								SimpleList list = new SimpleList();
								list.add(s1);
								list.add(a2);
								Arc2ExtModified.set_EPSILON(_Epsilon);
								switch (_intersection_mode)
								{
									case ALL_INTERSECTIONS:
										if (a2.liesOn(s1.source()))
										{
											insertEvent(INTERSECTION,
													s1.source(), list);
											if ((sp1 != null)
													&& (s1.source().equals(sp1)))
												sp1 = null;
										}
										if (a2.liesOn(s1.target()))
										{
											insertEvent(INTERSECTION,
													s1.target(), list);
											if ((sp1 != null)
													&& (s1.target().equals(sp1)))
												sp1 = null;
										}
										break;
									case REAL_INTERSECTIONS:
										break;
									case WITHOUT_TARGETS:
										if (a2.liesOn(s1.source()))
										{
											insertEvent(INTERSECTION,
													s1.source(), list);
											if ((sp1 != null)
													&& (s1.source().equals(sp1)))
												sp1 = null;
										}
										break;
								}
							}
						}
						if (sp1 != null)
						{
							SimpleList list = new SimpleList();
							list.add(s1);
							list.add(a2);
							insertEvent(INTERSECTION, sp1, list);
						}
						if (((inter.result == Intersection.EMPTY) || ((inter.result == Intersection.LIST) && (inter.list
								.empty()))) && sp1 == null)
						{
							SimpleList ll = getIntersections(a2, s1);
							if (ll != null)
							{
								ListItem i = ll.first();
								Rectangle2D r1 = calcBoundingRect(s1, _Epsilon);
								Rectangle2D r2 = calcBoundingRect(a2, _Epsilon);
								while (i != null)
								{
									if (r1.contains((Point2) i.key())
											&& r2.contains((Point2) i.key()))
									{
										insertEvent(INTERSECTION, i.key(), ll);
									}
									i = ll.next(i);
								}
							}
						}
					}
				}
				else if (i1.key() instanceof Arc2ExtModified)
				{
					Arc2ExtModified a1 = (Arc2ExtModified) i1.key();
					if (i2.key() instanceof Segment2Ext)
					{
						// Fall 3 ein Circle2 schneidet ein Segment2 (aehnelt Fall 2)
						Segment2Ext s2 = (Segment2Ext) i2.key();
						Point2 sp1 = null;
						if ((s2.source().equals(key) || s2.target().equals(key))
								&& (a1.source().equals(key) || a1.target()
										.equals(key)))
						{
							sp1 = key;
						}
						Intersection inter = new Intersection();
						Circle2 c1 = new Circle2(a1.centre, a1.radius);
						Point2 sp = c1.intersection(s2, inter);
						SimpleList myList = getIntersections(a1, s2);
						// Fall 3.1: Es gibt mehrere Schnittpunkte
						if (inter.result == Intersection.LIST)
						{
							// Die Schnittpunkte, die den Bogen nicht schneiden muessen aus der
							// Liste entfernt werden.
							if (!contains(a1, (Point2) inter.list.firstValue(),
									0.0))
							{
								inter.list.Pop();
							}
							if (!contains(a1, (Point2) inter.list.lastValue(),
									0.0))
							{
								inter.list.pop();
							}

							SimpleList l = inter.list;
							if ((l != null) && (l.first() != null))
							{
								ListItem lItem = l.first();
								while ((l != null) & (lItem != null))
								{
									Point2 p = (Point2) lItem.value();
									if (((sp1 != null) && !(p.equals(sp1,
											0.00001))) || (sp1 == null))
									{
										// Events, die einen Key haben dessen X-Wert kleiner ist als der derzeitige
										// X-Eventwert wurden bereits abgehandelt und werden deshalb nicht erneut
										// hinzugefuegt.
										if ((p.getX() >= _oldX)
												&& (s2.liesOn(p)))
										{
											SimpleList list = new SimpleList();
											list.add(a1);
											list.add(s2);
											switch (_intersection_mode)
											{
												case ALL_INTERSECTIONS:
													insertEvent(INTERSECTION,
															p, list);
													break;
												case REAL_INTERSECTIONS:
													if ((!p.equals(s2.source()))
															&& (!p.equals(s2
																	.target())))
														insertEvent(
																INTERSECTION,
																p, list);
													break;
												case WITHOUT_TARGETS:
													if (!p.equals(s2.target()))
														insertEvent(
																INTERSECTION,
																p, list);
													break;
											}
										}
									}
									lItem = lItem.next();
								}
							}
						}
						//Fall 3.2: Es gibt einen Schinttpunkt
						if ((inter.result == Intersection.POINT2)
								&& (contains(a1, sp)) && (sp.getX() >= _oldX))
						{
							if (((sp1 != null) && !(sp.equals(sp1, 0.00001)))
									|| (sp1 == null))
							{
								SimpleList list = new SimpleList();
								list.add(a1);
								list.add(s2);
								switch (_intersection_mode)
								{
									case ALL_INTERSECTIONS:
										insertEvent(INTERSECTION, sp, list);
										break;
									case REAL_INTERSECTIONS:
										if ((!sp.equals(s2.source()))
												&& (!sp.equals(s2.target())))
											insertEvent(INTERSECTION, sp, list);
										break;
									case WITHOUT_TARGETS:
										if (!sp.equals(s2.target()))
											insertEvent(INTERSECTION, sp, list);
										break;
								}
							}
						}
						// Fall 3.3; Es gibt keinen Schnittpunkt 
						if (inter.result == Intersection.EMPTY)
						{
							double minDist = s2.distance(a1.centre);
							if (minDist < (_radius + _Epsilon))
							{
								// Obwohl inter leer ist scheint es dennoch einen 
								// Schnittpunkt zu geben.
								SimpleList list = new SimpleList();
								list.add(a1);
								list.add(s2);
								Arc2ExtModified.set_EPSILON(_Epsilon);
								switch (_intersection_mode)
								{
									case ALL_INTERSECTIONS:
										if (a1.liesOn(s2.source()))
										{
											insertEvent(INTERSECTION,
													s2.source(), list);
											if ((sp1 != null)
													&& (s2.source().equals(sp1)))
												sp1 = null;
										}
										if (a1.liesOn(s2.target()))
										{
											insertEvent(INTERSECTION,
													s2.target(), list);
											if ((sp1 != null)
													&& (s2.target().equals(sp1)))
												sp1 = null;
										}
										break;
									case REAL_INTERSECTIONS:
										break;
									case WITHOUT_TARGETS:
										if (a1.liesOn(s2.source()))
										{
											insertEvent(INTERSECTION,
													s2.source(), list);
											if ((sp1 != null)
													&& (s2.source().equals(sp1)))
												sp1 = null;
										}
										break;
								}
							}
						}
						if (sp1 != null)
						{
							SimpleList list = new SimpleList();
							list.add(a1);
							list.add(s2);
							insertEvent(INTERSECTION, sp1, list);
						}
						if (((inter.result == Intersection.EMPTY) || ((inter.result == Intersection.LIST) && (inter.list
								.empty()))) && sp1 == null)
						{
							SimpleList ll = getIntersections(a1, s2);
							if (ll != null)
							{
								ListItem i = ll.first();
								Rectangle2D r1 = calcBoundingRect(a1, _Epsilon);
								Rectangle2D r2 = calcBoundingRect(s2, _Epsilon);
								while (i != null)
								{
									if (r1.contains((Point2) i.key())
											&& r2.contains((Point2) i.key()))
									{
										insertEvent(INTERSECTION, i.key(), ll);
									}
									i = ll.next(i);
								}
							}
						}
					}
					else if (i2.key() instanceof Arc2ExtModified)
					{
						// Fall 4 zwei Circle2 schneiden sich
						Arc2ExtModified a2 = (Arc2ExtModified) i2.key();
						Point2 sp1 = null;
						if ((a1.centre.equals(a2.centre))
								&& (a1.radius == a2.radius))
						{
							SimpleList l = new SimpleList();
							if (a1.source().equals(a2.target()))
								sp1 = a1.source();
							if (a2.source().equals(a1.target()))
								sp1 = a2.source();
							if (sp1 != null)
							{
								l.add(a1);
								l.add(a2);
								insertEvent(INTERSECTION, sp1, l);
							}
						}
						else
						{
							if ((a1.source().equals(key) || a1.target().equals(
									key))
									&& (a2.source().equals(key) || a2.target()
											.equals(key)))
							{
								sp1 = key;
							}
							Intersection inter = new Intersection();
							Circle2 c1 = new Circle2(a1.centre, a1.radius);
							Circle2 c2 = new Circle2(a2.centre, a2.radius);
							Point2 sp = c1.intersection(c2, inter);
							if (inter.result == Intersection.LIST)
							{
								if ((!contains(a1,
										(Point2) inter.list.firstValue(), 0.0))
										|| (!contains(a2,
												(Point2) inter.list
														.firstValue(), 0.0)))
								{
									inter.list.Pop();
								}
								if ((!contains(a1,
										(Point2) inter.list.lastValue(), 0.0))
										|| (!contains(
												a2,
												(Point2) inter.list.lastValue(),
												0.0)))
								{
									inter.list.pop();
								}
								SimpleList l = inter.list;
								if ((l != null) && (l.first() != null))
								{
									ListItem lItem = l.first();
									while ((l != null) & (lItem != null))
									{
										Point2 p = (Point2) lItem.value();
										if (((sp1 != null) && !(p.equals(sp1,
												0.00001))) || (sp1 == null))
										{
											SimpleList list = new SimpleList();
											list.add(a1);
											list.add(a2);
											switch (_intersection_mode)
											{
												case ALL_INTERSECTIONS:
													insertEvent(INTERSECTION,
															p, list);
													break;
												case REAL_INTERSECTIONS:
													if (l.length() > 1)
														insertEvent(
																INTERSECTION,
																p, list);
													break;
												case WITHOUT_TARGETS:
													if (l.length() > 1)
														insertEvent(
																INTERSECTION,
																p, list);
													break;
											}
										}
										lItem = lItem.next();
									}
								}
							}
							else if (inter.result == Intersection.POINT2)
							{
								if (((sp1 != null) && !(sp.equals(sp1, 0.00001)))
										|| (sp1 == null))
								{
									SimpleList list = new SimpleList();
									list.add(a1);
									list.add(a2);
									switch (_intersection_mode)
									{
										case ALL_INTERSECTIONS:
											insertEvent(INTERSECTION, sp, list);
											break;
										case REAL_INTERSECTIONS:
											insertEvent(INTERSECTION, sp, list);
											break;
										case WITHOUT_TARGETS:
											insertEvent(INTERSECTION, sp, list);
											break;
									}
								}
							}
							if (sp1 != null)
							{
								SimpleList list = new SimpleList();
								list.add(a1);
								list.add(a2);
								insertEvent(INTERSECTION, sp1, list);
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Vergleicht Geraden- und Bogenobjekte, indem sie an einer bestimmten
	 * Stelle ausgewertet werden und ihre Y-Werte verglichen werden.
	 * 
	 * @author Darius Geiss
	 * @version 1.0
	 */
	public class SweepComparitor
			extends LineComparitor
	{

		/**
		 * Liefert beim Vergleich von sich schneidenden Geraden am Schnittpunkt
		 * EQUAL
		 */
		public final static byte	EXACT		= 0;

		/**
		 * Liefert beim Vergleich von sich schneidenden Geraden am Schnittpunkt
		 * die Ordnung unmittelbar vor dem Schnittpunkt
		 */
		public final static byte	BEFORE		= 1;

		/**
		 * Liefert beim Vergleich von sich schneidenden Geraden am Schnittpunkt
		 * die Ordnung unmittelbar nach dem Schnittpunkt
		 */
		public final static byte	AFTER		= 2;

		/** X-Wert, an dem die Linienabstaende gemessen werden */
		private float				_x			= 0;

		/** Y-Wert fuer das richtige Einordnen von vertikalen Segmenten */
		private float				_y			= Float.MIN_VALUE;

		/**
		 * Gibt an, welche Ordnung bei sich schneidenden Geraden verwendet
		 * werden soll (eine Konstante aus {EXACT,BEFORE,AFTER})
		 */
		private byte				_delta		= EXACT;

		/** Einstellbare Toleranz bei der Identifizierung von Punkten */
		private float				_tolerance	= (float) 0.0;


		/**
		 * Ueberschreibt Object.toString().
		 * 
		 * @return Textuelle Repräsentation
		 * 
		 * @see java.lang.Object#toString
		 */
		public String toString()
		{
			String s = ",unknown delta";
			switch (_delta)
			{
				case EXACT:
					s = ",compare exact on x";
					break;
				case BEFORE:
					s = ",compare infinitisimal before x";
					break;
				case AFTER:
					s = ",compare infinitisimal after x";
					break;
			}
			if (_y > Double.MIN_VALUE)
				s = ",y=" + _y + s;
			return getClass().getName() + "[x=" + _x + s + "]";
		}


		/**
		 * Liefert den gesetzten X-Wert fuer den Vergleich der Geraden.
		 * 
		 * @return X-Wert
		 */
		public double getX()
		{
			return _x;
		}


		/**
		 * Setzt den X-Wert fuer den Vergleich von Geraden.
		 * 
		 * @param x
		 *            neuer X-Wert
		 */
		public void setX(
				float x)
		{
			_x = x;
		}


		/**
		 * Liefert den gesetzten Y-Wert fuer den Vergleich der vertikalen
		 * Linienobjekten.
		 * 
		 * @return Y-Wert
		 */
		public double getY()
		{
			return _y;
		}


		/**
		 * Setzt den y-Wert fuer den Vergleich von vertikalen Linienobjekten.
		 * 
		 * @param y
		 *            neuer Y-Wert
		 */
		public void setY(
				float y)
		{
			_y = y;
		}


		/**
		 * Setzt den Y-Wert zum Vergleichen von vertiaklen Linienobjekten
		 * zurueck.
		 */
		public void resetY()
		{
			_y = Float.MIN_VALUE;
			super.resetY();
		}


		/**
		 * Liefert den Delta-Wert fuer den Vergleich von sich schneidenden
		 * Geraden am Schnittpunkt.
		 * 
		 * @return eine Konstante aus {EXACT,BEFORE,AFTER}
		 */
		public byte getDelta()
		{
			return _delta;
		}


		/**
		 * Setzt den Delta-Wert fuer den Vergleich von sich schneidenden Geraden
		 * am Schnittpunkt.
		 * 
		 * @param d
		 *            eine Konstante aus {EXACT,BEFORE,AFTER}
		 */
		public void setDelta(
				byte d)
		{
			if ((d != EXACT) && (d != BEFORE) && (d != AFTER))
				return;
			_delta = d;
		}


		/**
		 * Gibt der Wert für Toloeranz zurück.
		 * 
		 * @return Toleranz-Wert
		 */
		public float getTolerance()
		{
			return _tolerance;
		}


		/**
		 * Setzt den Wert fuer die Toleranz, die fuer den Vergleich von Punkten
		 * benutzt werden soll.
		 * 
		 * @param t
		 *            neuer Toleranz-Wert
		 */
		public void setTolerance(
				float t)
		{
			_tolerance = t;
		}


		/**
		 * Vergleicht zwei Punkte auf gleichheit in abhängigkeit von _toleranz.
		 * 
		 * @param p1
		 *            Punkt1
		 * @param p2
		 *            Punkt2
		 * 
		 * @return TRUE oder FALSE
		 */
		private boolean pointsEqual(
				Point2 p1,
				Point2 p2)
		{
			return (Math.abs(p1.x - p2.x) <= _tolerance)
					&& (Math.abs(p1.y - p2.y) <= _tolerance);
		}


		/**
		 * Vergleicht zwei Punkte auf gleichheit in abhängigkeit von toleranz.
		 * 
		 * @param p1
		 *            Punkt1
		 * @param p2
		 *            Punkt2
		 * 
		 * @return TRUE oder FALSE
		 */
		private boolean pointsEqual(
				Point2 p1,
				Point2 p2,
				double tolerance)
		{
			return (Math.abs(p1.x - p2.x) <= tolerance)
					&& (Math.abs(p1.y - p2.y) <= tolerance);
		}


		/**
		 * Vergleicht zwei Werte auf gleichheit in abhängigkeit von _toleranz.
		 * 
		 * @param x1
		 *            Erster Wert
		 * @param x2
		 *            Zweiter Wert
		 * 
		 * @return TRUE bei Gleichheit oder FALSE sonst
		 */
		private boolean valuesEqual(
				double x1,
				double x2)
		{
			return (Math.abs(x1 - x2) <= _tolerance);
		}


		/**
		 * Vergleicht zwei Werte auf gleichheit in abhängigkeit von _toleranz.
		 * 
		 * @param x1
		 *            Erster Wert
		 * @param x2
		 *            Zweiter Wert
		 * @param t
		 *            Toleranz (in %)
		 * 
		 * @return TRUE bei Gleichheit oder FALSE sonst
		 */
		private boolean valuesEqual(
				double x1,
				double x2,
				double t)
		{
			return (Math.abs(x1 - x2) <= t);
		}


		/**
		 * Überprüft, ob b in a enthalten ist
		 * 
		 * @param a
		 *            Der Bogen
		 * @param p
		 *            Der Punkt
		 * 
		 * @return true wenn enthalten, false sonst
		 */
		private boolean contains(
				Arc2ExtModified a,
				Point2 p)
		{
			Rectangle2D bRect = calcBoundingRect(a, 0.0);
			if ((p.x >= bRect.getMinX()) && (p.x <= bRect.getMaxX())
					&& (p.y >= bRect.getMinY()) && (p.y <= bRect.getMaxY()))
				return true;
			return false;
		}


		/**
		 * Überprüft, ob b in a enthalten ist
		 * 
		 * @param a
		 *            Der Bogen
		 * @param p
		 *            Der Punkt
		 * @param tolerance
		 *            Die Toleranz
		 * 
		 * @return true wenn enthalten, false sonst
		 */
		private boolean contains(
				Arc2ExtModified a,
				Point2 p,
				double tolerance)
		{
			Rectangle2D bRect = calcBoundingRect(a, tolerance);
			if ((p.x >= bRect.getMinX()) && (p.x <= bRect.getMaxX())
					&& (p.y >= bRect.getMinY()) && (p.y <= bRect.getMaxY()))
				return true;
			return false;
		}


		/**
		 * Berechnet das umschließende Rechteck
		 * 
		 * @param a
		 *            Der Bogen
		 * @param tolerance
		 *            Die Toleranz
		 * 
		 * @return Das Rechteck
		 */
		private Rectangle2D calcBoundingRect(
				Arc2ExtModified a,
				double tolerance)
		{
			double minX = 0, maxX = 0, minY = 0, maxY = 0;
			if (a.orientation() == Angle.ORIENTATION_LEFT)
			{ //gegen den Uhrzeigersinn
				if ((a.sourceTrig().deg() < 180.0
						&& a.targetTrig().deg() > 180.0 && a.sourceTrig().deg() < a
						.targetTrig().deg())
						|| (a.sourceTrig().deg() > 180.0
								&& a.targetTrig().deg() > 180.0 && a
								.sourceTrig().deg() > a.targetTrig().deg())
						|| (a.sourceTrig().deg() < 180.0
								&& a.targetTrig().deg() < 180.0 && a
								.sourceTrig().deg() > a.targetTrig().deg()))
				{
					minX = a.centre.x - a.radius;
				}
				else
				{
					minX = Math.min(a.source().x, a.target().x);
				}
				if (a.sourceTrig().deg() > a.targetTrig().deg())
				{
					maxX = a.centre.x + a.radius;
				}
				else
				{
					maxX = Math.max(a.source().x, a.target().x);
				}
				if ((a.sourceTrig().deg() < 90.0 && a.targetTrig().deg() > 90.0 && a
						.sourceTrig().deg() < a.targetTrig().deg())
						|| (a.sourceTrig().deg() > 90.0
								&& a.targetTrig().deg() > 90.0 && a
								.sourceTrig().deg() > a.targetTrig().deg())
						|| (a.sourceTrig().deg() < 90.0
								&& a.targetTrig().deg() < 90.0 && a
								.sourceTrig().deg() > a.targetTrig().deg()))
				{
					maxY = a.centre.y + a.radius;
				}
				else
				{
					maxY = Math.max(a.source().y, a.target().y);
				}
				if ((a.sourceTrig().deg() < 270.0
						&& a.targetTrig().deg() > 270.0 && a.sourceTrig().deg() < a
						.targetTrig().deg())
						|| (a.sourceTrig().deg() > 270.0
								&& a.targetTrig().deg() > 270.0 && a
								.sourceTrig().deg() > a.targetTrig().deg())
						|| (a.sourceTrig().deg() < 270.0
								&& a.targetTrig().deg() < 270.0 && a
								.sourceTrig().deg() > a.targetTrig().deg()))
				{
					minY = a.centre.y - a.radius;
				}
				else
				{
					minY = Math.min(a.source().y, a.target().y);
				}
			}
			else if (a.orientation() == Angle.ORIENTATION_RIGHT)
			{ //im Uhrzeigersinn
				if ((a.sourceTrig().deg() > 180.0
						&& a.targetTrig().deg() < 180.0 && a.sourceTrig().deg() > a
						.targetTrig().deg())
						|| (a.sourceTrig().deg() < 180.0
								&& a.targetTrig().deg() < 180.0 && a
								.sourceTrig().deg() < a.targetTrig().deg())
						|| (a.sourceTrig().deg() > 180.0
								&& a.targetTrig().deg() > 180.0 && a
								.sourceTrig().deg() < a.targetTrig().deg()))
				{
					minX = a.centre.x - a.radius;
				}
				else
				{
					minX = Math.min(a.source().x, a.target().x);
				}
				if (a.sourceTrig().deg() < a.targetTrig().deg())
				{
					maxX = a.centre.x + a.radius;
				}
				else
				{
					maxX = Math.max(a.source().x, a.target().x);
				}
				if ((a.sourceTrig().deg() > 90.0 && a.targetTrig().deg() < 90.0 && a
						.sourceTrig().deg() > a.targetTrig().deg())
						|| (a.sourceTrig().deg() < 90.0
								&& a.targetTrig().deg() < 90.0 && a
								.sourceTrig().deg() < a.targetTrig().deg())
						|| (a.sourceTrig().deg() > 90.0
								&& a.targetTrig().deg() > 90.0 && a
								.sourceTrig().deg() < a.targetTrig().deg()))
				{
					maxY = a.centre.y + a.radius;
				}
				else
				{
					maxY = Math.max(a.source().y, a.target().y);
				}
				if ((a.sourceTrig().deg() > 270.0
						&& a.targetTrig().deg() < 270.0 && a.sourceTrig().deg() > a
						.targetTrig().deg())
						|| (a.sourceTrig().deg() < 270.0
								&& a.targetTrig().deg() < 270.0 && a
								.sourceTrig().deg() < a.targetTrig().deg())
						|| (a.sourceTrig().deg() > 270.0
								&& a.targetTrig().deg() > 270.0 && a
								.sourceTrig().deg() < a.targetTrig().deg()))
				{
					minY = a.centre.y - a.radius;
				}
				else
				{
					minY = Math.min(a.source().y, a.target().y);
				}
			}
			minX -= tolerance;
			minY -= tolerance;
			maxX += tolerance;
			maxY += tolerance;
			return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
		}


		/**
		 * Vergleicht die Arc2-Objekte, indem der Y-Wert an der Stelle x (zu
		 * setzen ueber setX) verglichen werden. Wenn sich die zwei Boegen an
		 * dieser Stelle schneiden, entscheidet der delta-Wert (zu setzen ueber
		 * setDelta), ob EQUAL zurueckgegeben oder der Vergleich unmittelbar vor
		 * oder nach der Stelle durchgefuehrt wird. ACHTUNG: Es können nur
		 * Boegen verglichen werden, die zu jedem X-Wert lediglich einen Y-Wert
		 * haben.
		 * 
		 * @param a1
		 *            Bogen1
		 * @param a2
		 *            Bogen2
		 * 
		 * @return {@link anja.geom.MinkSum.SweepComparitor#SMALLER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#BIGGER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#EQUAL}
		 */
		public short compare(
				Arc2ExtModified a1,
				Arc2ExtModified a2)
		{
			// Falls die Objekte identisch sind wird EQUAL zurückgegeben
			if (a1.toString().compareTo((String) (a2.toString())) == 0)
			{
				return EQUAL;
			}
			// ansonsten müssen die Objekte genauer betrachtet werden

			// Als erstes muss untersucht werden, ob die Boegen sich zu einem Kries ergaenzen
			if (pointsEqual(a1.centre, a2.centre))
				return (a1.getLabel() == "oben") ? BIGGER : SMALLER;

			// sollte dies nicht der Fall sein muss eine genauere Betrachtung erfolgen
			Segment2 basicLine1 = new Segment2(new Point2(_x, a1.centre.getY()
					- a1.radius * 2), new Point2(_x, a1.centre.getY()
					+ a1.radius * 2));
			Intersection inout_set1 = new Intersection();
			Circle2 c1 = new Circle2(a1.centre, a1.radius);
			Point2 p1 = c1.intersection(basicLine1, inout_set1);
			if (inout_set1.result == 1451)
			{
				if (contains(a1, (Point2) inout_set1.list.firstValue()))
					p1 = (Point2) inout_set1.list.firstValue();
				if (contains(a1, (Point2) inout_set1.list.lastValue()))
					p1 = (Point2) inout_set1.list.lastValue();
			}
			if (p1 == null)
			{
				float diffY = (float) Math
						.sqrt(Math.pow((a1.radius), 2)
								- Math.pow(Math.min(Math.abs(a1.centre.x - _x),
										a1.radius), 2));
				p1 = new Point2();
				p1.x = _x;
				if (a1.getLabel() == "oben")
				{ // Bogen ist nach unten offen
					p1.y = a1.centre.y + diffY;
				}
				else
				{
					p1.y = a1.centre.y - diffY;
				}
			}
			Segment2 basicLine2 = new Segment2(new Point2(_x, a2.centre.getY()
					- a2.radius * 2), new Point2(_x, a2.centre.getY()
					+ a2.radius * 2));
			Intersection inout_set2 = new Intersection();
			Circle2 c2 = new Circle2(a2.centre, a2.radius);
			Point2 p2 = c2.intersection(basicLine2, inout_set2);
			if (inout_set2.result == 1451)
			{
				if (contains(a2, (Point2) inout_set2.list.firstValue()))
					p2 = (Point2) inout_set2.list.firstValue();
				if (contains(a2, (Point2) inout_set2.list.lastValue()))
					p2 = (Point2) inout_set2.list.lastValue();
			}
			if (p2 == null)
			{
				float diffY = (float) Math
						.sqrt(Math.pow((a2.radius), 2)
								- Math.pow(Math.min(Math.abs(a2.centre.x - _x),
										a2.radius), 2));
				p2 = new Point2();
				p2.x = _x;
				if (a2.getLabel() == "oben")
				{ // Bogen ist nach unten offen
					p2.y = a2.centre.y + diffY;
				}
				else
				{
					p2.y = a2.centre.y - diffY;
				}
			}
			if ((p1 == null) && (p2 == null))
				return EQUAL;
			if (p1 == null)
				return SMALLER;
			if (p2 == null)
				return BIGGER;
			if (pointsEqual(p1, p2, 0.1))
			{
				// Die Steigung der Boegen wird hier berechnet. Im Falle, dass s1 oder s2 == 0.0 ist,
				// muss zusaetzlich ermittelt werden, ob die Steigung gegen minus oder plus Unendlich
				// geht.
				double s1 = (new Segment2(a1.centre, p1)).slope();
				if (s1 == 0.0)
				{
					if (a1.getLabel() == "oben")
					{ // Bogen ist nach unten offen
						s1 = Double.POSITIVE_INFINITY;
					}
					else
					{ // Bogen ist nach oben offen
						s1 = Double.NEGATIVE_INFINITY;
					}
				}
				else
				{
					s1 = (-1) / s1;
				}

				// Das ganze fuer s2
				double s2 = (new Segment2(a2.centre, p2)).slope();
				if (s2 == 0.0)
				{
					if (a2.getLabel() == "oben")
					{ // Bogen ist nach unten offen
						s2 = Double.POSITIVE_INFINITY;
					}
					else
					{ // Bogen ist nach oben offen
						s2 = Double.NEGATIVE_INFINITY;
					}
				}
				else
				{
					s2 = (-1) / s2;
				}

				if (valuesEqual(s1, s2, 0.1))
				{ // Die Steigungen beider Boegen sind identisch (_toleranz wird miteinbezogen)
					if (s1 == Double.POSITIVE_INFINITY)
					{
						if (p1.x < c1.centre.x)
						{
							return BIGGER;
						}
						else
						{
							return SMALLER;
						}
					}
					if (s1 == Double.NEGATIVE_INFINITY)
					{
						if (p1.x < c1.centre.x)
						{
							return SMALLER;
						}
						else
						{
							return BIGGER;
						}
					}
					if (a1.getLabel() == "oben")
					{
						return SMALLER;
					}
					else
					{
						return BIGGER;
					}
				}
				if (s1 < s2)
					return (_delta == BEFORE) ? BIGGER : SMALLER;
				if (s1 > s2)
					return (_delta == BEFORE) ? SMALLER : BIGGER;

			}
			else
			{
				if (p1.y < p2.y)
					return SMALLER;
				else
					return BIGGER;
			}
			return EQUAL;
		}


		/**
		 * Vergleicht ein BasicLine2-Objekt mit einem Arc2-Objekte, indem der
		 * Y-Wert an der Stelle x (zu setzen ueber setX) verglichen werden. Wenn
		 * sich die beiden Objekte an dieser Stelle schneiden, entscheidet der
		 * delta-Wert (zu setzen ueber setDelta), ob EQUAL zurueckgegeben oder
		 * der Vergleich unmittelbar vor oder nach der Stelle durchgefuehrt
		 * wird. ACHTUNG: Es können nur Boegen verglichen werden, die zu jedem
		 * X-Wert lediglich einen Y-Wert haben.
		 * 
		 * @param l1
		 *            Bogen1
		 * @param a2
		 *            Bogen2
		 * 
		 * @return {@link anja.geom.MinkSum.SweepComparitor#SMALLER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#BIGGER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#EQUAL}
		 */
		public short compare(
				BasicLine2 l1,
				Arc2ExtModified a2)
		{
			// Die Objekte können nicht identisch sein, da sie unterschiedlichen Typs sind
			//Point2 p1=l1.calculatePoint(_x);
			Point2 p1 = calculatePoint(l1);

			Intersection inout_set2 = new Intersection();
			Circle2 c2 = new Circle2(a2.centre, a2.radius);
			Point2 p2 = c2.intersection(l1, inout_set2);
			if ((p2 != null) && (!contains(a2, p2)))
				p2 = null;
			if ((p2 != null) && (!valuesEqual(_x, p2.x)))
				p2 = null;
			if (inout_set2.result == Intersection.LIST)
			{
				if ((contains(a2, (Point2) inout_set2.list.firstValue()))
						&& (valuesEqual(_x,
								((Point2) inout_set2.list.firstValue()).x)))
				{
					p2 = (Point2) inout_set2.list.firstValue();
					p1 = p2;
				}
				if ((contains(a2, (Point2) inout_set2.list.lastValue()))
						&& (valuesEqual(_x,
								((Point2) inout_set2.list.lastValue()).x)))
				{
					p2 = (Point2) inout_set2.list.lastValue();
					p1 = p2;
				}
			}
			if (p2 == null)
			{
				Line2 basicLine2 = new Line2(new Point2(_x, a2.centre.getY()
						- a2.radius * 2), new Point2(_x, a2.centre.getY()
						+ a2.radius * 2));
				p2 = c2.intersection(basicLine2, inout_set2);
				if (inout_set2.result == Intersection.LIST)
				{
					if (contains(a2, (Point2) inout_set2.list.firstValue()))
						p2 = (Point2) inout_set2.list.firstValue();
					if (contains(a2, (Point2) inout_set2.list.lastValue()))
						p2 = (Point2) inout_set2.list.lastValue();
				}
				if (p2 == null)
				{
					float diffY = (float) Math.sqrt(Math.pow((a2.radius), 2)
							- Math.pow(Math.min(Math.abs(a2.centre.x - _x),
									a2.radius), 2));
					p2 = new Point2();
					p2.x = _x;
					if (a2.getLabel() == "oben")
					{ // Bogen ist nach unten offen
						p2.y = a2.centre.y + diffY;
					}
					else
					{
						p2.y = a2.centre.y - diffY;
					}
				}
			}

			if ((p1 == null) && (p2 == null))
				return EQUAL;
			if (p1 == null)
				return SMALLER;
			if (p2 == null)
				return BIGGER;
			if (l1.isVertical())
				p1 = calculateCorrectVerticalPoint(l1);
			if (pointsEqual(p1, p2, _tolerance))
			{

				double s1 = l1.slope();
				double s2 = (new Segment2(a2.centre, p2)).slope();
				if (s2 == 0.0)
				{
					if (a2.getLabel() == "oben")
					{ // Bogen ist nach unten offen
						s2 = Double.POSITIVE_INFINITY;
					}
					else
					{ // Bogen ist nach oben offen
						s2 = Double.NEGATIVE_INFINITY;
					}
				}
				else
				{
					s2 = (-1) / s2;
				}
				double winkel1 = Math.toDegrees(Math.atan(s1));
				double winkel2 = Math.toDegrees(Math.atan(s2));

				if (valuesEqual(winkel1, winkel2, 0.1))
				{ // Steigung und die Y-Werte zum _x-Wert sind identisch 
					if (s1 == Double.POSITIVE_INFINITY)
						return BIGGER;
					if (a2.getLabel() == "oben")
					{
						return BIGGER;
					}
					else
					{
						return SMALLER;
					}
				}

				if (s1 == Double.POSITIVE_INFINITY)
					return BIGGER;
				if (s2 == Double.POSITIVE_INFINITY)
					return SMALLER;
				if (s2 == Double.NEGATIVE_INFINITY)
					return BIGGER;
				if (s1 < s2)
					return SMALLER;
				if (s1 > s2)
					return BIGGER;
			}
			else
			{
				if (p1.y < p2.y)
					return SMALLER;
				else
					return BIGGER;
			}
			return EQUAL;
		}


		/**
		 * Vergleicht die BasicLine2-Objekte, indem die Y-Wert an der Stelle x
		 * (zu setzen ueber setX) verglichen werden. Wenn sich die zwei Geraden
		 * gerade an dieser Stelle schneiden, entscheidet der delta-Wert (zu
		 * setzen ueber setDelta), ob EQUAL zurueckgegeben oder der Vergleich
		 * unmittelbar vor oder nach der Stelle durchgefuehrt wird.
		 * 
		 * @param l1
		 *            erstes Geradenobjekt
		 * @param l2
		 *            zweites Geradenobjekt
		 * 
		 * @return {@link anja.geom.MinkSum.SweepComparitor#SMALLER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#BIGGER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#EQUAL}
		 */
		public short compare(
				BasicLine2 l1,
				BasicLine2 l2)
		{
			// Falls die Objekte identisch sind wird EQUAL zurückgegeben
			if (l1.toString().compareTo((String) (l2.toString())) == 0)
			{
				return EQUAL;
			}
			Point2 p1 = calculatePoint(l1), p2 = calculatePoint(l2);
			if ((p1 == null) && (p2 == null))
				return EQUAL;
			if (p1 == null)
				return SMALLER;
			if (p2 == null)
				return BIGGER;
			if (l1.isVertical())
				p1 = calculateCorrectVerticalPoint(l1);
			if (l2.isVertical())
				p2 = calculateCorrectVerticalPoint(l2);
			if (pointsEqual(p1, p2, _tolerance))
			{
				double s1 = l1.slope(), s2 = l2.slope();
				if (s1 < s2)
					return SMALLER;
				if (s1 > s2)
					return BIGGER;
			}
			else
			{
				if (p1.y < p2.y)
					return SMALLER;
				else
					return BIGGER;
			}
			return EQUAL;
		}


		/**
		 * Vergleicht ein BasicLine2-Objekt mit einem Arc2-Objekt, indem der
		 * Y-Wert an der Stelle x (zu setzen ueber setX) verglichen wird. Wenn
		 * sich die beiden Objekte an dieser Stelle schneiden, entscheidet der
		 * delta-Wert (zu setzen ueber setDelta), ob EQUAL zurueckgegeben oder
		 * der Vergleich unmittelbar vor oder nach der Stelle durchgefuehrt
		 * wird. ACHTUNG: Es können nur Boegen verglichen werden, die zu jedem
		 * X-Wert lediglich einen Y-Wert haben.
		 * 
		 * @param a1
		 *            Bogen1
		 * @param l2
		 *            Bogen2
		 * 
		 * @return {@link anja.geom.MinkSum.SweepComparitor#SMALLER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#BIGGER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#EQUAL}
		 */
		public short compare(
				Arc2ExtModified a1,
				BasicLine2 l2)
		{
			return inverse(compare(l2, a1));
		}


		/**
		 * Vergleicht eines Point2-Objekt mit einem BasicLine2-Objekt, indem der
		 * Y-Wert an der Stelle x (zu setzen ueber setX) verglichen wird. Diese
		 * Funktion arbeitet immer mit dem delta-Wert EQUAL. Sie wird zum finden
		 * von Objekten benutzt, die durch den Punkt p1 gehen.
		 * 
		 * @param p1
		 *            Punkt2
		 * @param l2
		 *            BasicLine2
		 * 
		 * @return {@link anja.geom.MinkSum.SweepComparitor#SMALLER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#BIGGER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#EQUAL}
		 */
		public short compare(
				Point2 p1,
				BasicLine2 l2)
		{
			Point2 p2 = calculatePoint(l2);
			if ((p1 == null) && (p2 == null))
				return EQUAL;
			if (p1 == null)
				return SMALLER;
			if (p2 == null)
				return BIGGER;
			if (l2.isVertical())
				p2 = calculateCorrectVerticalPoint(l2);
			if (pointsEqual(p1, p2, _tolerance))
			{
				return EQUAL;
			}
			if (p1.y > p2.y)
			{
				return BIGGER;
			}
			else
			{
				return SMALLER;
			}
		}


		/**
		 * Vergleicht eines Point2-Objekt mit einem Arc2-Objekt, indem der
		 * Y-Wert an der Stelle x (zu setzen ueber setX) verglichen wird. Diese
		 * Funktion arbeitet immer mit dem delta-Wert EQUAL. Sie wird zum finden
		 * von Objekten benutzt, die durch den Punkt p1 gehen.
		 * 
		 * @param p1
		 *            Punkt2
		 * @param a2
		 *            Arc2
		 * 
		 * @return {@link anja.geom.MinkSum.SweepComparitor#SMALLER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#BIGGER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#EQUAL}
		 */
		public short compare(
				Point2 p1,
				Arc2ExtModified a2)
		{
			double radiusQ = Math.pow((a2.radius), 2);
			double diffXQ = Math.pow(
					Math.min((double) (Math.abs((double) (a2.centre.x - _x))),
							a2.radius), 2);
			double diffY = (double) (Math.sqrt(radiusQ - diffXQ));
			Point2 p2 = new Point2();
			p2.x = _x;
			if (a2.getLabel() == "oben")
			{ // Bogen ist nach unten offen
				p2.y = a2.centre.y + (float) diffY;
			}
			else
			{
				p2.y = a2.centre.y - (float) diffY;
			}
			if ((p1 == null) && (p2 == null))
				return EQUAL;
			if (p1 == null)
				return SMALLER;
			if (p2 == null)
				return BIGGER;
			if (pointsEqual(p1, p2, _tolerance))
			{
				return EQUAL;
			}
			if (p1.y > p2.y)
			{
				return BIGGER;
			}
			else
			{
				return SMALLER;
			}
		}


		/**
		 * Berechnet den Punkt mit X-Koordinate x auf der Gerade neu. Nur wenn
		 * er auch auf dem Objekt liegt (abhaengig von abgeleiteter Klasse),
		 * wird er zurueckgegeben, ansonsten null.
		 * 
		 * @param l
		 *            Gerade
		 * 
		 * @return Punkt auf dem Objekt
		 */
		public Point2 calculatePoint(
				BasicLine2 l)
		{
			if (l.isVertical())
			{
				if (_x == l.source().x)
					return new Point2(_x, Double.POSITIVE_INFINITY);
				else
					return null;
			}
			else
			{
				double slope1 = (((double) l.source().y) - ((double) l.target().y));
				double slope2 = (((double) l.source().x) - ((double) l.target().x));
				double y_abs = ((double) l.source().y)
						- (slope1 * (double) l.source().x) / slope2;
				Point2 p = new Point2(_x,
						(float) ((slope1 * _x) / slope2 + y_abs));
				double maxX = Math.max(l.source().x, l.target().x) + _tolerance;
				double minX = Math.min(l.source().x, l.target().x) - _tolerance;
				double maxY = Math.max(l.source().y, l.target().y) + _tolerance;
				double minY = Math.min(l.source().y, l.target().y) - _tolerance;
				if ((p.x >= minX) && (p.x <= maxX) && (p.y >= minY)
						&& (p.y <= maxY))
					return p;
				else
					return null;
			}
		}


		/**
		 * Bei vertikalen Linienobjekten gibt calculatePoint irgendeinen Punkt
		 * auf dem Objekt zurueck. Um einen verwertbaren Punkt zu erhalten (der
		 * vom y-Wert des Vergleichers beeinflusst werden kann), wird diese
		 * Methode benoetigt.
		 * 
		 * <b>Vorbedingungen:</b> l muss vertikal sein und sich auf der
		 * X-Koordinate des Vergleichers befinden.
		 * 
		 * @param l
		 *            vertikales Linienobjekt, auf dem ein Punkt berechnet
		 *            werden soll
		 * 
		 * @return berechneter Punkt
		 */
		private Point2 calculateCorrectVerticalPoint(
				BasicLine2 l)
		{
			Point2 p = new Point2(_x, _y);
			if (l.liesOn(p))
				return p;
			if (_y > l.source().y)
				p.moveTo(_x, Math.max(l.source().y, l.target().y));
			else
				p.moveTo(_x, Math.min(l.source().y, l.target().y));
			return p;
		}


		/**
		 * Invertiert den Vergleichswert c.
		 * 
		 * @param c
		 *            Vergleichskonstante
		 * 
		 * @return inverse Vergleichskonstante
		 */
		private short inverse(
				short c)
		{
			switch (c)
			{
				case SMALLER:
					return BIGGER;
				case BIGGER:
					return SMALLER;
				default:
					return c;
			}
		}


		/**
		 * Vergleicht zwei Objekte
		 * 
		 * @param o1
		 *            Object 1
		 * @param o2
		 *            Object 2
		 * 
		 * @return {@link anja.geom.MinkSum.SweepComparitor#SMALLER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#BIGGER},
		 *         {@link anja.geom.MinkSum.SweepComparitor#EQUAL}
		 * 
		 */
		public short compare(
				Object o1,
				Object o2)
		{
			// KeyValueHolder-Objekte entpacken (nur eine Schicht!!!)...
			if (o1 instanceof KeyValueHolder)
				o1 = ((KeyValueHolder) o1).key();
			if (o2 instanceof KeyValueHolder)
				o2 = ((KeyValueHolder) o2).key();
			// Point-Objekte behandeln
			if ((o1 instanceof Arc2ExtModified)
					&& (o2 instanceof Arc2ExtModified))
				return compare((Arc2ExtModified) o1, (Arc2ExtModified) o2);
			if ((o1 instanceof Arc2ExtModified) && (o2 instanceof Segment2Ext))
				return compare((Arc2ExtModified) o1, (Segment2Ext) o2);
			if ((o1 instanceof Segment2Ext) && (o2 instanceof Arc2ExtModified))
				return compare((Segment2Ext) o1, (Arc2ExtModified) o2);
			if ((o1 instanceof Segment2Ext) && (o2 instanceof Segment2Ext))
				return compare((Segment2Ext) o1, (Segment2Ext) o2);
			if ((o1 instanceof Point2) && (o2 instanceof Segment2Ext))
				return compare((Point2) o1, (Segment2Ext) o2);
			if ((o1 instanceof Point2) && (o2 instanceof Arc2ExtModified))
				return compare((Point2) o1, (Arc2ExtModified) o2);
			return super.compare(o1, o2);
		}
	}

}
