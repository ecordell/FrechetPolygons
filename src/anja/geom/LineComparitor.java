package anja.geom;


import anja.util.KeyValueHolder;
import anja.util.StdComparitor;


/**
 * Vergleicht Geradenobjekte (von BasicLine2 abgeleitet), indem sie an einer
 * bestimmten Stelle ausgewertet werden und ihre Y-Werte verglichen werden.
 * 
 * @author Thomas Wolf
 * @version 1.0
 */
public class LineComparitor
		extends StdComparitor
{

	/**
	 * Liefert beim Vergleich von sich schneidenden Geraden am Schnittpunkt
	 * EQUAL
	 */
	public final static byte	EXACT		= 0;

	/**
	 * Liefert beim Vergleich von sich schneidenden Geraden am Schnittpunkt die
	 * Ordnung unmittelbar vor dem Schnittpunkt
	 */
	public final static byte	BEFORE		= 1;

	/**
	 * Liefert beim Vergleich von sich schneidenden Geraden am Schnittpunkt die
	 * Ordnung unmittelbar nach dem Schnittpunkt
	 */
	public final static byte	AFTER		= 2;

	/** X-Wert, an dem die Linienabstaende gemessen werden */
	private float				_x			= 0;

	/** Y-Wert fuer das richtige Einordnen von vertikalen Segmenten */
	private float				_y			= Float.MIN_VALUE;

	/**
	 * Gibt an, welche Ordnung bei sich schneidenden Geraden verwendet werden
	 * soll (eine Konstante aus {EXACT,BEFORE,AFTER})
	 */
	private byte				_delta		= EXACT;

	/** Einstellbare Toleranz bei der Identifizierung von Punkten */
	private float				_tolerance	= (float) 0.001;


	/**
	 * Überschreibt {@link java.lang.Object#toString()}.
	 * 
	 * @return Textuelle Repräsentation dieses Objekts
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
	 * Setzt den Y-Wert zum Vergleichen von vertiaklen Linienobjekten zurueck.
	 */
	public void resetY()
	{
		_y = Float.MIN_VALUE;
	}


	/**
	 * Liefert den Delta-Wert fuer den Vergleich von sich schneidenden Geraden
	 * am Schnittpunkt.
	 * 
	 * @return eine Konstante aus {EXACT,BEFORE,AFTER}
	 */
	public byte getDelta()
	{
		return _delta;
	}


	/**
	 * Setzt den Delta-Wert fuer den Vergleich von sich schneidenden Geraden am
	 * Schnittpunkt.
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
	 * Liefert die Toleranz zurück
	 * 
	 * @return Toleranz
	 */
	public float getTolerance()
	{
		return _tolerance;
	}


	/**
	 * Setzt die Toleranz
	 * 
	 * @param t
	 *            Die neue Toleranz
	 */
	public void setTolerance(
			float t)
	{
		_tolerance = t;
	}


	/**
	 * Vergleicht 2 Punkte auf Gleichheit
	 * 
	 * @param p1
	 *            Punkt 1
	 * @param p2
	 *            Punkt 2
	 * 
	 * @return true bei Gleichheit bezgl. der Toleranz, false sonst
	 */
	private boolean pointsEqual(
			Point2 p1,
			Point2 p2)
	{
		return (Math.abs(p1.x - p2.x) <= _tolerance)
				&& (Math.abs(p1.y - p2.y) <= _tolerance);
	}


	/**
	 * Vergleicht die BasicLine2-Objekte, indem die Y-Wert an der Stelle x (zu
	 * setzen ueber setX) verglichen werden. Wenn sich die zwei Geraden gerade
	 * an dieser Stelle schneiden, entscheidet der delta-Wert (zu setzen ueber
	 * setDelta), ob EQUAL zurueckgegeben oder der Vergleich unmittelbar vor
	 * oder nach der Stelle durchgefuehrt wird.
	 * 
	 * @param l1
	 *            erstes Geradenobjekt
	 * @param l2
	 *            zweites Geradenobjekt
	 * 
	 * @return SMALLER, BIGGER oder EQUAL
	 */
	public short compare(
			BasicLine2 l1,
			BasicLine2 l2)
	{
		Point2 p1 = l1.calculatePoint(_x), p2 = l2.calculatePoint(_x);
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
		if (pointsEqual(p1, p2))
		{
			if (_delta == EXACT)
				return EQUAL;
			if ((l1.isVertical()) && (l2.isVertical()))
				return EQUAL;
			if (l1.isVertical())
				return (_delta == BEFORE) ? SMALLER : BIGGER;
			if (l2.isVertical())
				return (_delta == BEFORE) ? BIGGER : SMALLER;
			double s1 = l1.slope(), s2 = l2.slope();
			if (s1 < s2)
				return (_delta == BEFORE) ? BIGGER : SMALLER;
			if (s2 < s1)
				return (_delta == BEFORE) ? SMALLER : BIGGER;
			return EQUAL;
		}
		else
		{
			if (p1.y < p2.y)
				return SMALLER;
			else
				return BIGGER;
		}
	}


	/**
	 * Bei vertikalen Linienobjekten gibt calculatePoint irgendeinen Punkt auf
	 * dem Objekt zurueck. Um einen verwertbaren Punkt zu erhalten (der vom
	 * y-Wert des Vergleichers beeinflusst werden kann), wird diese Methode
	 * benoetigt. <b>Vorbedingungen:</b> l muss vertikal sein und sich auf der
	 * X-Koordinate des Vergleichers befinden.
	 * 
	 * @param l
	 *            vertikales Linienobjekt, auf dem ein Punkt berechnet werden
	 *            soll
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
	 * Vergleich das Geradenobjekt l mit der Zahl d, die eine horizontale Gerade
	 * mit Y-Wert d darstellt.
	 * 
	 * @param l
	 *            Geradenobjekt
	 * @param d
	 *            Y-Wert fuer den Vergleich
	 * 
	 * @return SMALLER, BIGGER oder EQUAL
	 */
	public short compare(
			BasicLine2 l,
			double d)
	{
		Point2 p = l.calculatePoint(_x);
		if (p == null)
			return SMALLER;
		if (pointsEqual(p, new Point2(_x, d)))
		{
			if (_delta == EXACT)
				return EQUAL;
			double s = l.slope();
			if (s < 0)
				return (_delta == BEFORE) ? BIGGER : SMALLER;
			if (s > 0)
				return (_delta == BEFORE) ? SMALLER : BIGGER;
			return EQUAL;
		}
		else
		{
			if (p.y < d)
				return SMALLER;
			else
				return BIGGER;
		}
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
	 * Ueberschreibt Comparitor.compare(). Akzeptiert werden Objekte des Types
	 * BasicLine2, Number und KeyValueHolder, wobei bei KeyValueHolder-Objekten
	 * ihre key()-Werte ausgewertet werden (die also vom Typ BasicLine2 oder
	 * Number sein sollten). BasicLine2-Objekte koennen mit Zahlen verglichen
	 * werden. Im Prinzip stellen Zahlen eine Gerade parallel zur X-Achse mit
	 * Y-Wert der Zahl dar. Da StdComparitor ueberschrieben wird, werden andere
	 * Objekte mit den Methoden in StdComparitor verglichen.
	 * 
	 * @param o1
	 *            erstes Objekt
	 * @param o2
	 *            zweites Objekt
	 * 
	 * @return SMALLER, falls o1 in der Ordnung <b>vor</b> o2 liegt; BIGGER,
	 *         falls o1 in der Ordnung <b>nach</b> o2 liegt; EQUAL, falls o1 und
	 *         o2 gleich sind
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
		if ((o1 instanceof BasicLine2) && (o2 instanceof BasicLine2))
			return compare((BasicLine2) o1, (BasicLine2) o2);
		if ((o1 instanceof BasicLine2) && (o2 instanceof Number))
			return compare((BasicLine2) o1, ((Number) o2).doubleValue());
		if ((o2 instanceof BasicLine2) && (o1 instanceof Number))
			return inverse(compare((BasicLine2) o2, ((Number) o1).doubleValue()));
		return super.compare(o1, o2);
	}

}
