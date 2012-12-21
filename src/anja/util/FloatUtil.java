package anja.util;

import java.util.Random;

/**
* Hilfsroutinen fuer Float-Zahlen, die ganz nuetzlich sind und die
* Methoden in java.lang.Float und java.lang.Math ergaenzen.
* Es gibt fast nur statische Methoden, nur fuer nextRandomFloat
* braucht man ein Objekt dieser Klasse.
*
* @version	0.3 22.10.1997
* @author	Christian Icking
*/

public class FloatUtil
{
   private Random _rand;
   /**
   * Konstruktor, wird nur gebraucht fuer nextRandomFloat
   */
   public FloatUtil()
   {
	_rand = new Random();
   }
   /**
   * Konstruktor mit Initialisierungswert fuer nextRandomFloat
   */
   public FloatUtil( int init )
   {
	_rand = new Random( init );
   }


  /** Umwandlung von float in einen String, ergibt viel genauere Werte
  * als das eingebaute toString fuer float-Werte. <BR><BR>
  *
  * Zum Beispiel ist hier garantiert, dass
  * bei der Umwandlung von float in einen String und zurueck in float
  * Float.valueOf(FloatUtil.floatToString(f)).floatValue()==f ist fuer
  * alle float-Werte (jedenfalls fuer Millionen von Werten getestet).
  * Das entsprechende fuer java.lang.Float.toString funktioniert nur
  * in den seltensten Faellen, weil die letzten Ziffern abgeschnitten werden.
  * <BR><BR>
  * 
  * Wenn man also mit FloatUtil.floatToString einen String erzeugt, kommt beim
  * Einlesen dieses Strings in eine float-Zahl wieder der urspruengliche Wert heraus.
  * <BR><BR>
  *
  * Aehnlich wie bei toString wird der Wert von f in e-Schreibweise
  * (scientific notation) ausgegeben, wenn |x| >= 10^6 oder <=10^(-6) gilt,
  * ansonsten in einer ganzzahligen oder Festkomma-Schreibweise.
  * 
  * @param	f	Eingabezahl
  * @return		Wert von f in einem String
  */
  public static String floatToString( float f )
  {
	if (    ( Float.isNaN( f )           ) 
	     || ( f==Float.NEGATIVE_INFINITY )
	     || ( f==Float.POSITIVE_INFINITY )
	   )
	   return Float.toString( f );

	// Float.toString ist nicht genau genug, besser:

	if ( f==+0.0f ) return "0";                   // 0 vorab erledigen (-0.0f auch)

	int i;
	int mlen = 9;                                 // Laenge der Dezimal-Mantisse
	int exla = 5;                                 // Exponent-Grenze fuer e-Schreib-
	                                              // weise (scientific notation)

	StringBuffer s = new StringBuffer(mlen+7);
	int len = 0;                                  // len = s.length()

	double g = Math.abs(f);                       // Vorzeichen weg
	int exponent = log10floor( Math.abs(f) );     // Exponent merken

	i = new Double(g).intValue();
	if ( g==i ) {
		s.append(i);                          // ganze Zahlen einfach erledigen
		len = s.length();
	}
	else {
		g = g/Math.pow(10,exponent);          // Exponent weg, jetzt ist 1<=g<10
		do {
			double gf = Math.floor(g);    // eine Ziffer holen
			s.append((int)gf);            // ... und anhaengen, cast nach int
						      // damit kein '.0' angehaengt wird
			len++;
			g = (g-gf)*10;                // shift
		} while ( (g!=0) && (len < mlen) );   // bis genug Ziffern da sind
	}

	while ( s.charAt(--len) == '0' );             // Nullen am Ende entfernen
	s.setLength( ++len );

	if ( Math.abs(exponent) > exla ) {            // e-Schreibweise
		s.append("e"+exponent);               // Exponent anhaengen
		if (len > 1) s.insert(1,'.');         // Dezimalpunkt dazu
	}
	else if ( exponent >= len-1 )                 // "kleine" ganze Zahl
		for ( i = 0; i <= exponent-len; i++ ) // noetige Nullen anfuegen
			s.append("0");
	else if ( exponent >= 0 )                     // nichtganze Zahl >= 1
		s.insert(exponent+1, '.');            // Dezimalpunkt dazu
	else {                                        // nichtganze Zahl < 1
		for ( i = -1; i > exponent ; i-- )    // noetige Nullen voranstellen
			s.insert(0,"0");
		s.insert(0,"0.");                     // und "0." voranstellen
	}

	if ( f<0 ) s.insert(0,"-");                   // Vorzeichen voranstellen

	return s.toString();
  }


  // IEEE 754 Kodierung:                      "±exponentmantissemantissemantiss"
  //                                          "33222222222211111111110000000000"
  //                                          "10987654321098765432109876543210"
  final static int mantmask = Integer.valueOf("00000000011111111111111111111111",2).intValue();
  final static int expomask = Integer.valueOf("01111111100000000000000000000000",2).intValue();

  // valueOf("10000000000000000000000000000000",2) wird zwar von Java 1.0 akzeptiert, fuehrt in Java 1.1
  // aber zu einem kryptischen Fehler bei der Initialisierung der Klasse
  final static int signmask = ~(Integer.valueOf("01111111111111111111111111111111",2).intValue());

  /** Umwandlung von float in einen String aus 32 Nullen und Einsen,
  * so wie die Zahl intern in 32 bit gespeichert ist.
  * @param	f	Eingabezahl
  * @return		Darstellung der bits von f in einem String von 32 Nullen und Einsen
  */
  public static String floatToBitString( float f )
  {
	String s, sign;
//	s = Integer.toBinaryString(Float.floatToIntBits(f),2); // Einzeiler in Java 1.1

	int i = Float.floatToIntBits(f);                       // Java 1.0
	sign = (i<0) ? "1" : "0";                              // Vorzeichen merken
	i &= ~signmask;                                        // Vorzeichen weg
	s = Integer.toString(i,2);                             // Bitstring erzeugen
	while ( s.length() < 31 ) s = "0"+s;                   // fuehrende Nullen ergaenzen
	return sign+s;                                         // Vorzeichen ergaenzen
  }


  /** Berechnet die Maschinengenauigkeit der float-Zahlen,
  * also die kleinste positive float-Zahl e, so dass 1+e > 1 ergibt.
  * @return		kleinste positive float-Zahl e mit 1+e > 1
  */
  public static float floatEpsilon()
  {
	float add = 1, eps;

	do {
		eps = add;
		add = add/2;
	} while ( 1+add != 1 );

	return eps;
  }


  /** Berechnet die Maschinengenauigkeit der double-Zahlen,
  * also die kleinste positive double-Zahl e, so dass 1+e > 1 ergibt.
  * @return		kleinste positive double-Zahl e mit 1+e > 1
  */
  public static double doubleEpsilon()
  {
	double add = 1, eps;

	do {
		eps = add;
		add = add/2;
	} while ( 1+add != 1 );

	return eps;
  }


  /** Zufaellige float-Zahl, ist nicht dasselbe wie
  * java.util.Random.nextFloat().<BR><BR>
  *
  * Der Unterschied zu java.util.Random.nextFloat() ist, dass nicht nur
  * Zahlen zwischen 0 und 1 geliefert werden, sondern jede moegliche
  * float-Zahl vorkommen kann,
  * ausser NaN, POSITIVE_INFINITY und NEGATIVE_INFINITY.
  */
  public float nextRandomFloat()
  {
	float f;
	do {
		f = Float.intBitsToFloat(_rand.nextInt());
	} while ( Float.isNaN(f) || Float.isInfinite(f) );
	return f;
  }

  /** Zufaellige int-Zahl, ist genau dasselbe wie java.util.Random.nextInt().
  */
  public int nextRandomInt()
  {
	return _rand.nextInt();
  }


  /** Zehnerlogarithmus von float-Zahl
  * @param	f	Eingabezahl
  * @return		Logarithmus zur Basis 10 von f.
  */
  public static double log10( float f )
  {
	return Math.log(Math.abs(f))/Math.log(10);
  }

  /** Ganzzahliger Zehnerlogarithmus von float-Zahl
  * @param	f	Eingabezahl
  * @return		groesste ganze Zahl <= Logarithmus zur Basis 10 von f.
  */
  public static int log10floor( float f )
  {
// die 1+1e-14 sind noetig (und koennen wir uns leisten), weil
// f vom Typ float ist und hier die floor-Funktion gesucht ist,
// sonst wird durch Rundungsfehler manchmal die naechstkleinere ganze Zahl
// geliefert.
	return (int) Math.floor( log10(f)*1.00000000000001 );
  }


  /** float-Wert von einem String.
  * @param	s	Eingabestring
  * @return		float-Wert
  */
  public static float floatValue( String s )
  {
	return Float.valueOf(s).floatValue();
  }

  /** double-Wert von einem String.
  * @param	s	Eingabestring
  * @return		double-Wert
  */
  public static double doubleValue( String s )
  {
	return Double.valueOf(s).doubleValue();
  }

}
