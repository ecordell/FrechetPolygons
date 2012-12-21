package anja.geom;


import anja.util.Comparitor;
import anja.util.List;
import anja.util.ListItem;
import anja.util.SimpleList;
import anja.util.Sweep;
import anja.util.SweepEvent;
import anja.util.SweepEventComparitor;
import anja.util.TreeItem;


/**
 * SegmentIntersectionSweep realisiert einen Plane-Sweep-Algorithmus zum
 * Bestimmen von Schnittpunkten von n Segmenten in O(n*log(n)) Zeit mit Hilfe
 * der Sweep-Basisklasse.<br>Mit <b>segmentIntersection</b> wird der Algorithmus
 * auf die uebergebene Segmentliste angewandt und gibt die Punktliste
 * zurueck.<br>Mittels <b>setOutputMode</b> kann das Ausgabeformat beeinflusst
 * werden, mit <b>setIntersectionMode</b> kann festgelegt werden, welche
 * Schnittpunkte erkannt werden sollen. Das genaue Verhalten ist den folgenden
 * Tabellen zu entnehmen:
 * 
 * Uebersicht des Verhaltens bei verschiedenen Schnittmodi:
 * 
 * <table border="1" cellpadding="2"> <tr> <td>Konstante</td> <td>erkannte
 * Schnitte</td> </tr> <tr> <td>ALL_INTERSECTIONS</td> <td>alle Schnitte der
 * Segmente</td> </tr> <tr> <td>REAL_INTERSECTIONS</td> <td>Nur 'echte'
 * Schnitte, d.h keine Schnittpunkte, die auf die Enden eines Segmentes
 * fallen.</td> </tr> <tr> <td>WITHOUT_TARGETS</td> <td>Nur Schnittpunkte, die
 * kein Endpunkt eines Segmentes sind.</td> </tr> </table>
 * 
 * Uebersicht des Verhaltens bei verschiedenen Ausgabemodi:
 * 
 * <table border="1" cellpadding="2"> <tr> <td>Konstante</td>
 * <td>Ausgabeformat</td> </tr> <tr> <td>POINTS</td> <td>eine Liste mit den
 * Schnittpunkten (keine doppelten Punkte)</td> </tr> <tr>
 * <td>POINTS_AND_SEGMENTS</td> <td>eine Liste mit den Schnittpunkten als
 * Schluessel und einem Segment2-Array mit den daran beteiligten Segmenten (das
 * sind immer nur 2!). Bei Mehrfachschnittpunkten wird derselbe Schnittpunkt
 * mehrfach berichtet (mit jeweils den zwei beteiligten Segmenten).</td> </tr>
 * </table>
 * 
 * @author Thomas Wolf
 * @version 1.0
 */
public class SegmentIntersectionSweep
		extends Sweep
{

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

	/** Ausgabemodus: Schnittpunkte und beteiligte Segmente werden ausgegeben */
	public final static byte	POINTS_AND_SEGMENTS	= 1;

	// Schnittmodus-Konstanten

	/** Schnittmodus: Alle Schnittpunkte werden berechnet */
	public final static byte	ALL_INTERSECTIONS	= 0;

	/** Schnittmodus: Nur echte Schnitte werden berechnet */
	public final static byte	REAL_INTERSECTIONS	= 1;

	/**
	 * Schnittmodus: Nur Schnittpunkte, die nicht an den Segment-Target-Punkten
	 * liegen, werden ausgegeben
	 */
	public final static byte	WITHOUT_TARGETS		= 2;

	/** gesetzter Ausgabemodus */
	private byte				_output_mode		= POINTS;

	/** gesetzter Schnittmodus */
	private byte				_intersection_mode	= ALL_INTERSECTIONS;

	/** Liste mit den Ausgabepunkten. */
	private List				_points;

	/** Linienvergleicher fuer die SSS */
	private LineComparitor		_linecomparitor;

	/** letzter erreichter Schnittpunkt */
	private Point2				_lastcut			= null;


	// Setzen von Modi
	// ===============

	/**
	 * Liefert den gesetzten Ausgabemodus.
	 * 
	 * @return Ausgabemodus (eine Konstante aus {POINTS, POINTS_AND_SEGMENTS})
	 * 
	 * @see #POINTS
	 * @see #POINTS_AND_SEGMENTS
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
	 * 
	 * @see #POINTS
	 * @see #POINTS_AND_SEGMENTS
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
	 *         WITHOUT_TARGETS}
	 * 
	 * @see #ALL_INTERSECTIONS
	 * @see #WITHOUT_TARGETS
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
	 *            WITHOUT_TARGETS}
	 * 
	 * @see #ALL_INTERSECTIONS
	 * @see #WITHOUT_TARGETS
	 */
	public void setIntersectionMode(
			byte mode)
	{
		_intersection_mode = mode;
	}


	// Algorithmus
	// ===========

	/**
	 * Fuehrt einen Plane-Sweep durch, um alle Schnittpunkte der in der Liste L
	 * uebergebenen Segmente zu bestimmen. Die Ausgabeliste enthaelt alle
	 * Schnittpunkte sortiert nach X-Koordinaten (PointComparitor.X_ORDER).<br>
	 * 
	 * <br>Uebersicht des Verhaltens bei verschiedenen Schnittmodi:
	 * 
	 * <table border="1" cellpadding="2"> <tr> <td>Konstante</td> <td>erkannte
	 * Schnitte</td> </tr> <tr> <td>ALL_INTERSECTIONS</td> <td>alle Schnitte der
	 * Segmente</td> </tr> <tr> <td>REAL_INTERSECTIONS</td> <td>Nur 'echte'
	 * Schnitte, d.h keine Schnittpunkte, die auf die Enden eines Segmentes
	 * fallen.</td> </tr> <tr> <td>WITHOUT_TARGETS</td> <td>Nur Schnittpunkte,
	 * die kein Endpunkt eines Segmentes sind.</td> </tr> </table>
	 * 
	 * <br>Uebersicht des Verhaltens bei verschiedenen Ausgabemodi:
	 * 
	 * <table border="1" cellpadding="2"> <tr> <td>Konstante</td>
	 * <td>Ausgabeformat</td> </tr> <tr> <td>POINTS</td> <td>eine Liste mit den
	 * Schnittpunkten (keine doppelten Punkte)</td> </tr> <tr>
	 * <td>POINTS_AND_SEGMENTS</td> <td>eine Liste mit den Schnittpunkten als
	 * Schluessel und einem Segment2-Array mit den daran beteiligten Segmenten
	 * (das sind immer nur 2!). Bei Mehrfachschnittpunkten wird derselbe
	 * Schnittpunkt mehrfach berichtet (mit jeweils den zwei beteiligten
	 * Segmenten).</td> </tr> </table>
	 * 
	 * @param L
	 *            Segmentliste (Liste mit Segment2-Objekten)
	 * 
	 * @return Liste mit den Schnittpunkten
	 * 
	 * @see PointComparitor#X_ORDER
	 */
	public List segmentIntersection(
			SimpleList L)
	{
		if ((L == null) || (L.length() < 2))
			return new List();
		PointComparitor pcompare = new PointComparitor(PointComparitor.X_ORDER);
		createEventStructure(new SweepEventComparitor(pcompare), true);
		_linecomparitor = new LineComparitor();
		createSSS(_linecomparitor);
		Segment2 s;
		for (ListItem i = L.first(); i != null; i = i.next())
		{
			if (i.value() instanceof Segment2)
			{
				s = (Segment2) i.key();
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
		}
		_points = new List();
		_lastcut = null;
		execute();
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
		_linecomparitor.setX(((Point2) e.key()).x);
		TreeItem i, j, k;
		switch (e.getID())
		{
			case LEFT_ENDPOINT:
				i = sss().add(e.value());
				testIntersection(i, true);
				testIntersection(i, false);
				break;
			case RIGHT_ENDPOINT:
				i = sss().find(e.value());
				j = sss().prev(i);
				// Falls vertikales Segment endet, muss der Y-Wert des LineComparitors wieder zurueckgesetzt werden
				if (((Segment2) e.value()).isVertical())
					_linecomparitor.resetY();
				sss().remove(i);
				testIntersection(j, true);
				break;
			case INTERSECTION:
				Segment2[] seg = (Segment2[]) e.value();
				_linecomparitor.setDelta(LineComparitor.BEFORE);
				// um vertikale Segmente richtig zu verwalten, muessen wir den Y-Wert mitsetzen...
				if ((seg[0].isVertical()) || (seg[1].isVertical()))
					_linecomparitor.setY(((Point2) e.key()).y);
				k = sss().find(seg[0]);
				j = sss().find(seg[1]);
				// ==null: nur, wenn derselbe Schnitt schoneinmal berechnet wurde
				if ((k != null) && (j != null))
				{
					// Vertausche oberes und unteres Segment...
					sss().remove(j);
					sss().remove(k);
					_linecomparitor.setDelta(LineComparitor.AFTER);
					k = sss().add(k);
					j = sss().add(j);
					testIntersection(k, false);
					testIntersection(j, true);
					Point2 p = (Point2) e.key();
					switch (_output_mode)
					{
						case POINTS:
							if ((_lastcut == null) || (!_lastcut.equals(p)))
								_points.add(p);
							break;
						case POINTS_AND_SEGMENTS:
							_points.add(p, e.value());
							break;
					}
					_lastcut = p;
				}
				break;
		}
	}


	/**
	 * Testet den Schnitt des Segments im TreeItem t der SSS mit dem
	 * darueberliegenden - falls up==true, ansonsten mit den darunterliegenden -
	 * Segment. Falls ein Schnitt entdeckt wurde, wird ein entsprechendes
	 * Schnittevent eingefuegt
	 * 
	 * @param t
	 *            TreeItem der SSS
	 * @param up
	 *            falls true, Test mit darueberliegenden (naechsten) Segment,
	 *            ansonsten mit dem darunterliegenden (vorigen) Segment
	 */
	private void testIntersection(
			TreeItem t,
			boolean up)
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
		if ((i1 == null) || (i2 == null))
			return;
		Segment2 s1 = (Segment2) i1.key(), s2 = (Segment2) i2.key();
		Intersection inter = new Intersection();
		Point2 p = s1.intersection(s2, inter);
		if (p == null)
			return;
		Segment2[] sl = { s1, s2 };
		switch (_intersection_mode)
		{
			case ALL_INTERSECTIONS:
				insertEvent(INTERSECTION, p, sl);
				break;
			case REAL_INTERSECTIONS:
				if ((!p.equals(s1.source())) && (!p.equals(s1.target()))
						&& (!p.equals(s2.source())) && (!p.equals(s2.target())))
					insertEvent(INTERSECTION, p, sl);
				break;
			case WITHOUT_TARGETS:
				if ((!p.equals(s1.target())) && (!p.equals(s2.target())))
					insertEvent(INTERSECTION, p, sl);
				break;
		}
	}

}
