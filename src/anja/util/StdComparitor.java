package anja.util;


/**
 * <p> Standard-Vergleicher. Kann Zahlen, Strings, Characters, Booleanwerte und
 * Items miteinander vergleichen. Eigene Vergleicher-Objekte sollten am besten
 * von diesem Vergleicher abgeleitet werden. Das ueberschreiben der eigentlichen
 * Compare-Funktion <tt>compare(Object,Object)</tt> sollte dabei am besten in
 * folgender Form geschehen:<br><pre> public short compare(Object o1,Object o2)
 * { ... // Aufruf eigener Compare-Funktionen mit return! zum Beispiel: if ((o1
 * instanceof MyObject) && (o2 instanceOf MyObject)) return
 * compare((MyObject)o1,(MyObject)o2); // ruft Routine zum Vergleichen von
 * MyObject-Objekten auf und beendet. return super.compare(o1,o2); // Restliche
 * Vergleiche probieren und falls // unvergleichbar, wird Exception erzeugt }
 * <pre>
 * 
 * @version 1.3
 * @author Thomas Wolf
 */
public class StdComparitor
		extends Object
		implements Comparitor, java.io.Serializable
{

	/**
	 * Vergleicht zwei Strings miteinander.
	 * @param s1
	 *            und
	 * @param s2
	 *            zu vergleichende Strings
	 * @return Vergleichskonstante
	 */
	public short compare(
			String s1,
			String s2)
	{
		int c = s1.compareTo(s2);
		if (c < 0)
			return SMALLER;
		if (c > 0)
			return BIGGER;
		return EQUAL;
	}


	/**
	 * Vergleicht zwei Charakter-Objekte miteinander.
	 * @param c1
	 *            und
	 * @param c2
	 *            zu vergleichende Charakterobjekte
	 * @return Vergleichskonstante
	 */
	public short compare(
			Character c1,
			Character c2)
	{
		char cc1 = c1.charValue(), cc2 = c2.charValue();
		if (cc1 < cc2)
			return SMALLER;
		if (cc1 > cc2)
			return BIGGER;
		return EQUAL;
	}


	/**
	 * Vergleicht zwei Zahlen miteinander.
	 * @param n1
	 *            und
	 * @param n2
	 *            zu vergleichende Number-Objekte
	 * @return Vergleichskonstante
	 */
	public short compare(
			Number n1,
			Number n2)
	{
		double d1 = n1.doubleValue(), d2 = n2.doubleValue();
		if (d1 < d2)
			return SMALLER;
		if (d1 > d2)
			return BIGGER;
		return EQUAL;
	}


	/**
	 * Vergleicht zwei Boolean.Werte miteinander. Hier liegt <bf>false</bf> vor
	 * <bf>true</bf>.
	 * @param b1
	 *            und
	 * @param b2
	 *            die zu vergleichenden Booleanwerte
	 * @return Vergleichskonstante
	 */
	public short compare(
			Boolean b1,
			Boolean b2)
	{
		boolean bb1 = b1.booleanValue(), bb2 = b2.booleanValue();
		if ((!bb1) && (bb2))
			return SMALLER;
		if ((!bb2) && (bb1))
			return BIGGER;
		return EQUAL;
	}


	/**
	 * Vergleicht zwei Objekte aufgrund ihres Hash-Codes miteinander.
	 * @param o1
	 *            und
	 * @param o2
	 *            die zu vergleichenden Objekte
	 * @return Vergleichskonstante
	 */
	public short compareHash(
			Object o1,
			Object o2)
	{
		// Hash-Code-Abfragen...
		int h1 = 0, h2 = 0;
		if (o1 != null)
			h1 = o1.hashCode();
		if (o2 != null)
			h2 = o2.hashCode();
		if (h1 < h2)
			return SMALLER;
		if (h1 > h2)
			return BIGGER;
		return EQUAL;
	}


	/**
	 * Ueberschreibt Comparitor.compare.
	 * @param o1
	 *            und
	 * @param o2
	 *            zu vergleichende Objekte
	 * @return Vergleichskonstanten
	 * @see Comparitor#compare
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
		// null-Abfragen...
		if ((o1 == null) && (o2 == null))
			return EQUAL;
		if (o1 == null)
			return SMALLER;
		if (o2 == null)
			return BIGGER;
		// ab hier spezieller Code
		if ((o1 instanceof String) && (o2 instanceof String))
			return compare((String) o1, (String) o2);
		if ((o1 instanceof Character) && (o2 instanceof Character))
			return compare((Character) o1, (Character) o2);
		if ((o1 instanceof Number) && (o2 instanceof Number))
			return compare((Number) o1, (Number) o2);
		if ((o1 instanceof Boolean) && (o2 instanceof Boolean))
			return compare((Boolean) o1, (Boolean) o2);
		return compareHash(o1, o2);
		//  throw new CompareException(this,o1,o2); // Fehler erzeugen, falls nicht vergleichbar
	}
}
