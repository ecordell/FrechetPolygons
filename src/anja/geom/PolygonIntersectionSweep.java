package anja.geom;


import anja.util.BasicTreeItem;
import anja.util.ListItem;
import anja.util.SimpleList;
import anja.util.StdListItem;
import anja.util.Sweep;
import anja.util.SweepEvent;
import anja.util.SweepEventComparitor;


/**
 * PolygonIntersectionSweep realisiert den Schnitt von zwei einfachen Polygonen
 * mittels eines Plane-Sweeps in O(n*log(n)) Zeit, wobei n die Summe der
 * Eckpunkte der beiden Polygon bezeichnet. Um den Algorithmus auszufuehren,
 * muss ledeglich <b>polygonIntersection</b> aufgerufen werden.
 * 
 * @author Thomas Wolf
 * @version 1.0
 */
public class PolygonIntersectionSweep
		extends Sweep
{

	/** Ereignis-ID: Polygonpunkt Fall 1 erreicht */
	private final static byte	POINT_THROUGH		= 1;

	/** Ereignis-ID: Polygonpunkt Fall 2 erreicht */
	private final static byte	POINT_LEFT			= 2;

	/** Ereignis-ID: Polygonpunkt Fall 3 erreicht */
	private final static byte	POINT_RIGHT			= 3;

	/** Ereignis-ID: Schnittpunkt erreicht */
	private final static byte	INTERSECTION		= 4;

	/** Sub-Ereignis-ID: Neues Teilpolygon beginnen */
	private final static byte	NEW_POLYGON			= 1;

	/** Sub-Ereignis-ID: Punkt zum oberen Polygonrand hinzufuegen */
	private final static byte	ADD_UPPOINT			= 2;

	/** Sub-Ereignis-ID: Punkt zum unteren Polygonrand hinzufuegen */
	private final static byte	ADD_DOWNPOINT		= 3;

	/** Sub-Ereignis-ID: ein Schnittintervall aufteilen */
	private final static byte	SPLIT_INTERVAL		= 4;

	/** Sub-Ereignis-ID: zwei Schnittpolygone zusammenfuegen */
	private final static byte	CONCAT_INTERVALS	= 5;

	/** Sub-Ereignis-ID: ein Schnittintervall beenden */
	private final static byte	FINISH_INTERVAL		= 6;

	/** Besitz-Konstante: Objekt gehoert weder zu P noch zu Q */
	final static byte			_0					= 0;

	/** Besitz-Konstante: Objekt gehoert nur zu Q */
	final static byte			_Q					= 1;

	/** Besitz-Konstante: Objekt gehoert nur zu P */
	final static byte			_P					= 2;

	/** Besitz-Konstante: Objekt gehoert zum Schnitt von P und Q */
	final static byte			_P_Q				= 3;

	/** Die Schnittpolygone. */
	private Polygon2			_p, _q;

	/** Linienvergleicher fuer die SSS */
	private LineComparitor		_linecomparitor;

	/** X-Koordinate des letzten SweepEvents */
	private float				_lastx;

	/** Liste mit den Schnittpolygonen */
	private SimpleList			_polygons;

	/** Hilfskonstanten, da Baseline2.direction() und Konstanten nicht ex. */
	private final static byte	DIRECTION_LEFT		= 1, DIRECTION_RIGHT = 2,
			DIRECTION_UP = 3, DIRECTION_DOWN = 4;


	/**
	 * Berechnet den Schnitt der Polygone pol1 und pol2 in O(n*log(n)) Zeit,
	 * wobei n die Summe der Anzahl der Randpunkte von pol1 und pol2 bezeichnet.
	 * 
	 * <b>Vorbedingung:</b> Die Polygone muessen beide einfach sein.
	 * 
	 * @param pol1
	 *            Polygon 1
	 * @param pol2
	 *            Polygon 2
	 * 
	 * @return Liste mit allen Schnittpolygonen
	 */
	public SimpleList polygonIntersection(
			Polygon2 pol1,
			Polygon2 pol2)
	{
		createEventStructure(new SweepEventComparitor(new PointComparitor(
				PointComparitor.X_ORDER)), true);
		_linecomparitor = new LineComparitor();
		// bugfix: Toleranz von 0.001f auf 0.005f erhöht
		_linecomparitor.setTolerance(0.005f);
		createSSS(_linecomparitor);
		_p = pol1;
		_q = pol2;
		byte[] eve;
		SimpleList L = pol1.points();
		Point2 p, pl;
		Segment2 sl, s = new Segment2(pol1.lastPoint(), pol1.firstPoint());
		for (ListItem i = L.first(); i != null; i = i.next())
		{
			if (i.next() != null)
				pl = (Point2) i.next().key();
			else
				pl = (Point2) L.firstKey();
			p = (Point2) i.key();
			sl = s;
			s = new Segment2(p, pl);
			eve = getEventType(sl, s);
			if (eve[1] == 1)
				insertEvent(eve[0], p, new SweepEventContext(_P, sl, s));
			else
				insertEvent(eve[0], p, new SweepEventContext(_P, s, sl));
		}
		L = pol2.points();
		s = new Segment2(pol2.lastPoint(), pol2.firstPoint());
		for (ListItem i = L.first(); i != null; i = i.next())
		{
			if (i.next() != null)
				pl = (Point2) i.next().key();
			else
				pl = (Point2) L.firstKey();
			p = (Point2) i.key();
			sl = s;
			s = new Segment2(p, pl);
			eve = getEventType(sl, s);
			if (eve[1] == 1)
				insertEvent(eve[0], p, new SweepEventContext(_Q, sl, s));
			else
				insertEvent(eve[0], p, new SweepEventContext(_Q, s, sl));
		}
		_lastx = Float.MIN_VALUE;
		_polygons = new SimpleList();
		execute();
		for (ListItem i = _polygons.first(); i != null; i = i.next())
			i.setKey(createPolygon((SimpleList) i.key()));
		return _polygons;
	}


	/**
	 * Weils Basicline2.direction nicht gibt, nehmen wir dies als Ersatz
	 * 
	 * @param l
	 *            Basicline2, deren Richtung bestimmt wird
	 * 
	 * @return Richtungskonstante
	 */
	private byte direction(
			BasicLine2 l)
	{
		if (l.source().x < l.target().x)
			return DIRECTION_RIGHT;
		if (l.source().x > l.target().x)
			return DIRECTION_LEFT;
		if (l.source().y < l.target().y)
			return DIRECTION_UP;
		else
			return DIRECTION_DOWN;
	}


	/**
	 * Uebersicht ueber die generierten Events in Abhaengigkeit der Richtungen
	 * zweier inzidenter Kanten eines Polygons. Die Segmentreihenfolge beim
	 * Sweepereignis ist wichtig, um die Ereignisse richtig zu bearbeiten. Die
	 * Zahlen beziehen sich auf die Position der Segmente in der
	 * Richtungsspalte.
	 * 
	 * <table border="1"> <tr><td
	 * width="25%"><strong>Segment-Orientierungen</strong></td> <td
	 * width="25%"><strong>Event-Typ</strong></td> <td
	 * width="25%"><strong>Segmentreihenfolge</strong></td></tr> <tr><td
	 * width="25%">LEFT,LEFT</td> <td width="25%">POINT_THROUGH</td> <td
	 * width="25%">2,1</td></tr> <tr><td>RIGHT,RIGHT</td> <td>POINT_THROUGH</td>
	 * <td>1,2</td></tr> <tr><td>UP,UP</td> <td>POINT_THROUGH</td>
	 * <td>1,2</td></tr> <tr><td>DOWN,DOWN</td> <td>POINT_THROUGH</td>
	 * <td>2,1</td></tr> <tr><td>LEFT,UP</td> <td>POINT_RIGHT</td>
	 * <td>2,1</td></tr> <tr><td>UP,RIGHT</td> <td>POINT_THROUGH</td>
	 * <td>1,2</td></tr> <tr><td>DOWN,RIGHT</td> <td>POINT_RIGHT</td>
	 * <td>1,2</td></tr> <tr><td>LEFT,DOWN</td> <td>POINT_THROUGH</td>
	 * <td>2,1</td></tr> <tr><td>LEFT,RIGHT</td> <td>POINT_RIGHT</td> <td>1,2
	 * bzw. 2,1</td></tr> <tr><td>RIGHT,UP</td> <td>POINT_THROUGH</td>
	 * <td>1,2</td></tr> <tr><td>UP,LEFT</td> <td>POINT_LEFT</td>
	 * <td>2,1</td></tr> <tr><td>DOWN,LEFT</td> <td>POINT_THROUGH</td>
	 * <td>2,1</td></tr> <tr><td>RIGHT,DOWN</td> <td>POINT_LEFT</td>
	 * <td>1,2</td></tr> <tr><td>RIGHT,LEFT</td> <td>POINT_LEFT</td> <td>1,2
	 * bzw. 2,1</td></tr> </table>
	 * 
	 * @param s1
	 *            Strecke 1
	 * @param s2
	 *            Strecke 2
	 * 
	 * @return Werte, wie in der Tabelle beschrieben
	 */
	private byte[] getEventType(
			Segment2 s1,
			Segment2 s2)
	{
		byte[] r = new byte[3];
		byte dir1 = direction(s1), dir2 = direction(s2);
		// Wir gehen hier davon aus, das alle Richtungskonstanten >0 und <10 sind !!!
		switch (dir1 * 10 + dir2)
		{
			case DIRECTION_RIGHT * 10 + DIRECTION_RIGHT:
			case DIRECTION_UP * 10 + DIRECTION_UP:
			case DIRECTION_RIGHT * 10 + DIRECTION_UP:
			case DIRECTION_UP * 10 + DIRECTION_RIGHT:
				r[0] = POINT_THROUGH;
				r[1] = 1;
				r[2] = 2;
				break;
			case DIRECTION_LEFT * 10 + DIRECTION_LEFT:
			case DIRECTION_DOWN * 10 + DIRECTION_DOWN:
			case DIRECTION_LEFT * 10 + DIRECTION_DOWN:
			case DIRECTION_DOWN * 10 + DIRECTION_LEFT:
				r[0] = POINT_THROUGH;
				r[1] = 2;
				r[2] = 1;
				break;
			case DIRECTION_LEFT * 10 + DIRECTION_RIGHT:
			case DIRECTION_DOWN * 10 + DIRECTION_RIGHT:
			case DIRECTION_LEFT * 10 + DIRECTION_UP:
				r[0] = POINT_RIGHT;
				r[1] = (s1.orientation(s2.target()) == Point2.ORIENTATION_LEFT) ? (byte) 1
						: (byte) 2;
				r[2] = (byte) (r[1] % 2 + 1);
				break;
			case DIRECTION_RIGHT * 10 + DIRECTION_LEFT:
			case DIRECTION_UP * 10 + DIRECTION_LEFT:
			case DIRECTION_RIGHT * 10 + DIRECTION_DOWN:
				r[0] = POINT_LEFT;
				r[1] = (s1.orientation(s2.target()) == Point2.ORIENTATION_LEFT) ? (byte) 2
						: (byte) 1;
				r[2] = (byte) (r[1] % 2 + 1);
				break;
		}
		return r;
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
		if (_lastx == ((Point2) e.key()).x)
			_linecomparitor.setY(((Point2) e.key()).y);
		else
			_linecomparitor.resetY();
		_lastx = ((Point2) e.key()).x;
		SweepEventContext ec = (SweepEventContext) e.value();
		SweepItem i = null, j = null;
		switch (e.getID())
		{
			case POINT_THROUGH:
				_linecomparitor.setDelta(LineComparitor.EXACT);
				i = (SweepItem) sss().find(ec.segment1);
				i.segment = ec.segment2;
				testIntersection(i, true);
				testIntersection(i, false);
				// Subevents generieren..
				if (i.owner == _P_Q)
					polygonService(ADD_UPPOINT, i, j, e);
				j = (SweepItem) sss().next(i);
				if ((j != null) && (j.owner == _P_Q))
					polygonService(ADD_DOWNPOINT, j, i, e);
				break;
			case POINT_RIGHT:
				byte aown = locatePoint((Point2) e.key());
				byte own = calculateOwner(aown, ec.owner);
				i = new SweepItem(ec.segment1, own);
				j = new SweepItem(ec.segment2, aown);
				_linecomparitor.setDelta(LineComparitor.AFTER);
				sss().add(i);
				sss().add(j);
				testIntersection(i, true);
				testIntersection(j, false);
				// Subevents generieren..
				if (aown == _P_Q)
					polygonService(SPLIT_INTERVAL, (SweepItem) sss().next(i),
							j, e);
				if (own == _P_Q)
					polygonService(NEW_POLYGON, i, j, e);
				break;
			case POINT_LEFT:
				_linecomparitor.setDelta(LineComparitor.BEFORE);
				i = (SweepItem) sss().find(ec.segment1);
				j = (SweepItem) sss().find(ec.segment2);
				if ((i != null) && (j != null))
				{
					SweepItem k;
					k = (SweepItem) sss().next(i);
					sss().remove(i);
					sss().remove(j);
					if (k != null)
						testIntersection(k, false);
					// Subevents generieren..
					if (i.owner == _P_Q)
						polygonService(FINISH_INTERVAL, i, j, e);
					if ((k != null) && (k.owner == _P_Q))
						polygonService(CONCAT_INTERVALS, k, j, e);
				}
				break;
			case INTERSECTION:
				_linecomparitor.setDelta(LineComparitor.BEFORE);
				i = (SweepItem) sss().find(ec.segment1);
				j = (SweepItem) sss().find(ec.segment2);
				if (i == null || j == null)
				{
					System.out.println("Something propably went wrong! "
							+ "You should increase maybe the tolerance of "
							+ "the LineComparitor in this file!");
				}
				if ((i != null) && (j != null))
				{
					// Subevents generieren..
					if (i.owner == _P_Q)
						polygonService(FINISH_INTERVAL, i, j, e);
					i.segment = ec.segment2;
					j.segment = ec.segment1;
					i.owner = calculateIOwner(i.owner, j.owner);
					testIntersection(i, true);
					testIntersection(j, false);
					// Subevents generieren..
					if (i.owner == _P_Q)
						polygonService(NEW_POLYGON, i, j, e);
					if (j.owner == _P_Q)
						polygonService(ADD_UPPOINT, j, j, e);
					j = (SweepItem) sss().next(i);
					if ((j != null) && (j.owner == _P_Q))
						polygonService(ADD_DOWNPOINT, j, i, e);
				}
				break;
		}
	}


	/**
	 * Aktualisiert die Schnittpolygon-Daten. Je nach id wird eine bestimmte
	 * Aufgabe ausgefuehrt und die restlichen uebergebenen Variablen uebernehmen
	 * bestimmte Parameter: <ul><li>NEW_POLYGON: i=Intervall, mit
	 * Schnittflaeche, e Event</li><li>ADD_UPPOINT,ADD_DOWNPOINT: i=Intervall, e
	 * Event</li><li>SPLIT_INTERVAL: i=altes (oberes) Intervall, j=neues
	 * (unteres) Intervall</li><li>CONCAT_INTERVALS: i=Intervall oberes Gebiet,
	 * j=Intervall wegfallendes Gebiet, e Event</li><li>FINISH_INTERVAL:
	 * i=Intervall</li></ul>
	 * 
	 * @param id
	 *            Besitzer/Aufgabe
	 * @param i
	 *            SweepItem
	 * @param j
	 *            SweepItem
	 * @param e
	 *            SweepEvent
	 */
	private void polygonService(
			byte id,
			SweepItem i,
			SweepItem j,
			SweepEvent e)
	{
		SimpleList poly, points;
		ListItem k;
		switch (id)
		{
			case NEW_POLYGON:
				poly = new SimpleList();
				_polygons.add(poly);
				i.polygon = poly;
				// Die Polygonliste ist gleich auch mal die Punktliste
				i.setList(poly, false);
				i.setList(poly, true);
				i.addPoint((Point2) e.key(), false);
				break;
			case ADD_UPPOINT:
				// Fuege Punkt an...
				i.addPoint((Point2) e.key(), true);
				break;
			case ADD_DOWNPOINT:
				// Fuege Punkt an...
				i.addPoint((Point2) e.key(), false);
				break;
			case SPLIT_INTERVAL:
				// Listen erzeugen und aufteilen...
				points = new SimpleList();
				j.polygon = i.polygon;
				j.setList(i.getList(false), false);
				i.setList(points, false);
				j.setList(points, true);
				// Punkt anfuegen
				i.addPoint((Point2) e.key(), false);
				break;
			case CONCAT_INTERVALS:
				i.addPoint((Point2) e.key(), false);
				k = _polygons.find(j.polygon);
				// Das Polygon von j ist kein selbstaendiges Polygon mehr
				// muss aber auch nicht unbedingt noch ein globales mehr sein!!!
				if (k != null)
					_polygons.remove(k);
				// Mit der oberen Liste von j muessen wir beginnen...
				k = new StdListItem("polygon", j.getList(true));
				i.getList(false).add(k);
				i.setList(j.getList(false), false);
				break;
			case FINISH_INTERVAL:
				// Fuege Punkt an...
				i.addPoint((Point2) e.key(), false);
				if (i.getList(true) != i.getList(false))
				{
					// So werden die Listen verkettet...
					k = new StdListItem("concat", i.getList(true));
					i.getList(false).add(k);
				}
				break;
		}
	}


	/**
	 * Erzeugt aus der Liste L ein Polygon2-Objekt. Da die Liste mehrere
	 * ineinanderverschachtelte Listen enthalten kann, ist das nicht ganz mit
	 * dem Listenkonstruktor getan.
	 * 
	 * @param L
	 *            Liste mit Listen mit Punkten
	 * 
	 * @return Polygon2-Objekt
	 */
	private Polygon2 createPolygon(
			SimpleList L)
	{
		// zuerst Liste auftrudeln
		SimpleList points = new SimpleList();
		concatPolygon(L, points);
		return visPolygon.createPolygon(points);
	}


	/**
	 * Fuegt alle Punkte in der Polygonliste L an die Punktliste points an. L
	 * kann verschachtelte Listen enthalten. Bei Elementen mit dem Schluessel
	 * "polygon" ruft sich diese Methode rekursiv auf!
	 * 
	 * @param L
	 *            Polygon-Punktliste
	 * @param points
	 *            Liste fuer Punkte
	 */
	private void concatPolygon(
			SimpleList L,
			SimpleList points)
	{
		ListItem i = L.first(), j;
		String s;
		while (i != null)
		{
			if (i.key() instanceof Point2)
			{
				j = i.next();
				L.remove(i);
				points.add(i);
				i = j;
			}
			else if (i.key() instanceof String)
			{
				s = (String) i.key();
				if (s == "polygon")
				{
					// Im Unterschied zu concat muessen wir an diese Stelle wieder zurueck!
					j = i.next();
					L.remove(i);
					concatPolygon((SimpleList) i.value(), points);
					i = j;
				}
				if (s == "concat")
				{
					L.remove(i);
					L = (SimpleList) i.value();
					i = L.first();
				}
			}
		}
	}


	/**
	 * Bestimmt den Besitzer des Intervalls, in dem der Punkt p liegt.
	 * 
	 * @param p
	 *            zu lokalisierender Punkt
	 * 
	 * @return Besitzer des Intervalls, das p enthaelt
	 */
	private byte locatePoint(
			Point2 p)
	{
		SweepItem i = (SweepItem) sss().findBigger(new Double(p.y));
		if (i == null)
			return _0;
		return i.owner;
	}


	/**
	 * Berechnet den Besitzer des neu entstehenden Intervalls bei einem
	 * POINT_RIGHT-Ereignis.
	 * 
	 * @param areaowner
	 *            Besitzer des Intervalls, in dem das POINT_RIGHT-Ereignis
	 *            auftrat
	 * @param lineowner
	 *            Besitzer der Linien, die das neues Gebiet abgrenzen
	 * 
	 * @return Besitzer des neuen Intervalles
	 */
	private byte calculateOwner(
			byte areaowner,
			byte lineowner)
	{
		switch (areaowner)
		{
			case _0:
				return lineowner;
			case _P:
			case _Q:
				if (lineowner == areaowner)
					return _0;
				else
					return _P_Q;
			case _P_Q:
				if (lineowner == _P)
					return _Q;
				else
					return _P;
		}
		return _0;
	}


	/**
	 * Berechnet den Besitzer des neu entstehenden rechten Intervalls bei einem
	 * Schnitt von Kanten.
	 * 
	 * @param upowner
	 *            Besitzer des oberen Intervalles
	 * @param downowner
	 *            Besitzer des unteren Intervalles
	 * 
	 * @return Besitzer des rechten (oberen) Intervalles
	 */
	private byte calculateIOwner(
			byte upowner,
			byte downowner)
	{
		switch (upowner)
		{
			case _Q:
				return _P;
			case _P:
				return _Q;
			case _0:
				return _P_Q;
			case _P_Q:
				return _0;
		}
		return _0;
	}


	/**
	 * Ueberprueft, ob sich die Segmente des Intervalles i und des
	 * darueber/darunter liegenden schneiden und erzeugt, falls soetwas der Fall
	 * ist, ein INTERSECTION-Ereignis.
	 * 
	 * @param i
	 *            SweepItem in der SSS, das das Intervall repraesentiert
	 * @param up
	 *            falls true wird Schnitt mit dem darueberliegenden ansonsten
	 *            mit dem darunterliegenden Segment geprueft
	 */
	private void testIntersection(
			SweepItem i,
			boolean up)
	{
		if (i == null)
			return;
		SweepItem j;
		if (up)
			j = (SweepItem) sss().next(i);
		else
			j = (SweepItem) sss().prev(i);
		if (j == null)
			return;
		Intersection inter = new Intersection();
		Point2 p = i.segment.intersection(j.segment, inter);
		if (p == null)
			return;
		if ((p.equals(i.segment.target())) || (p.equals(j.segment.target())))
			return;
		if (up)
			insertEvent(INTERSECTION, p, new SweepEventContext(_0, j.segment,
					i.segment));
		else
			insertEvent(INTERSECTION, p, new SweepEventContext(_0, i.segment,
					j.segment));
	}
}

/**
 * Objekt zum Speichern von Eventinformationen. Wird als value()-Wert fuer
 * Events verwendet.
 */
class SweepEventContext
		implements java.io.Serializable
{

	/** erstes Segment */
	public Segment2	segment1;

	/** zweites Segment */
	public Segment2	segment2;

	/** Gibt an, wem das Event gehoert */
	public byte		owner;


	/**
	 * Konstruktor.
	 * 
	 * @param owner
	 *            Besitzer des Events
	 * @param seg1
	 *            erstes Segment
	 * @param seg2
	 *            zweites Segment
	 */
	public SweepEventContext(
			byte owner,
			Segment2 seg1,
			Segment2 seg2)
	{
		this.owner = owner;
		this.segment1 = seg1;
		this.segment2 = seg2;
	}


	/**
	 * Ueberschreibt Object.toString().
	 * 
	 * @return Textuelle Repräsentation dieses Objekts
	 * 
	 * @see java.lang.Object#toString
	 */
	public String toString()
	{
		String s = "unknown";
		switch (owner)
		{
			case PolygonIntersectionSweep._0:
				s = "0";
				break;
			case PolygonIntersectionSweep._P:
				s = "P";
				break;
			case PolygonIntersectionSweep._Q:
				s = "Q";
				break;
			case PolygonIntersectionSweep._P_Q:
				s = "P and Q";
				break;
		}
		return "[owner=" + s + ",segment1=" + segment1 + ",segment2="
				+ segment2 + "]";
	}
}

/**
 * Ein TreeItem fuer die SSS. Allerdings ist es kein korrekt implementiertes
 * TreeItem, da man den Schluessel ohne Nachfragen aendern kann. Entsprechend
 * kann viel damit schiefgehen, was allerdings im dafuer vorgesehenen
 * Algorithmus nicht passieren duerfte.<br>
 * 
 * Ein Element der SSS repraesentiert hier ein Intervall auf der Sweep-Line. Als
 * Schluessel dient das obere begrenzende Segment des Intervalles mittels des
 * LineComparitors. Im Intervall wird seine Zugehoerigkeit, das Polygons, zu dem
 * das Intervall gehoert sowie die obere und untere Randpunktliste, falls es ein
 * Schnittintervall ist, gespeichert.
 */
class SweepItem
		extends BasicTreeItem
{

	/** oberes begrenzendes Segment des Intervalls */
	public Segment2		segment;

	/** Liste mit Punkten (und Listen) fuer den oberen Intervallrand */
	private SimpleList	points1;

	/** Liste mit Punkten (und Listen) fuer den unteren Intervallrand */
	private SimpleList	points2;

	/** Das Polygon, zu dem das Intervall gehoert. */
	public SimpleList	polygon;

	/** Besitz-Konstante fuer das Intervall */
	public byte			owner;


	/**
	 * Konstruktor.
	 * 
	 * @param s
	 *            oberes begrenzendes Segment
	 * @param owner
	 *            Besitz-Konstante
	 */
	public SweepItem(
			Segment2 s,
			byte owner)
	{
		super(2);
		this.segment = s;
		this.owner = owner;
	}


	/**
	 * Liefert den gespeicherten Schluessel des Objektes.
	 * 
	 * @return gespeicherter Schluessel
	 */
	public Object key()
	{
		return segment;
	}


	/**
	 * Fuegt einen Randpunkt zum oberen bzw. unteren Rand hinzu.
	 * 
	 * @param p
	 *            neuer Punkt
	 * @param up
	 *            falls true, wird p zum oberen ansonsten zum unteren Rand
	 *            hinzugefuegt
	 */
	public void addPoint(
			Point2 p,
			boolean up)
	{
		if (up)
			points1.Push(p);
		else
			points2.push(p);
	}


	/**
	 * Setzt die obere bzw. untere Randpunktliste auf L.
	 * 
	 * @param L
	 *            neue Liste
	 * @param up
	 *            falls true, wird L als obere Randpunktliste gesetzt, ansonsten
	 *            als untere
	 */
	public void setList(
			SimpleList L,
			boolean up)
	{
		if (up)
			points1 = L;
		else
			points2 = L;
	}


	/**
	 * Liefert die obere bzw. untere Randpunktliste.
	 * 
	 * @param up
	 *            falls true, wird die obere ansonsten die untere Randpunktliste
	 *            zurueckgegeben.
	 */
	public SimpleList getList(
			boolean up)
	{
		if (up)
			return points1;
		else
			return points2;
	}


	/**
	 * Ueberschreibt Object.toString().
	 * 
	 * @return Textuelle Repräsentation
	 * 
	 * @see java.lang.Object#toString
	 */
	public String toString()
	{
		String s = "unknown";
		switch (owner)
		{
			case PolygonIntersectionSweep._0:
				s = "0";
				break;
			case PolygonIntersectionSweep._P:
				s = "P";
				break;
			case PolygonIntersectionSweep._Q:
				s = "Q";
				break;
			case PolygonIntersectionSweep._P_Q:
				s = "P and Q";
				break;
		}
		return "[owner=" + s + ",segment=" + segment + ",polygon=" + polygon
				+ "]";
	}
}
