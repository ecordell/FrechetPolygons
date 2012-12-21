package anja.analysis;

import anja.util.DynamicArray;


/**
* Abstrakte reelle (eindimensionale) Funktion. Argument und Funktionswert
* bestehen aus einer reellen Zahl vom Typ <code>double</code> bzw. dem
* korrespondierenden Objekttyp <code>Double</code>.
*
* @version 0.7 20.04.2004
* @author Sascha Ternes
*/

public abstract class RealFunction
extends Function
{

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * binaere Maske fuer eine AND-Verknuepfung zur Ermittlung der linken
   * Teilkonstante
   */
   public static final int LEFT_MASK = 32 + 16 + 8; // 111 000 binaer
   /**
   * Konstante fuer Funktionsorientierung: Orientierung links vom Schnittpunkt
   * unbekannt
   */
   public static final int LEFT_UNKNOWN = 32; // 100 000 binaer
   /**
   * Konstante fuer Funktionsorientierung: Funktionwert links vom Schnittpunkt
   * ist groesser
   */
   public static final int LEFT_ABOVE = 16;   // 010 000 binaer
   /**
   * Konstante fuer Funktionsorientierung: Funktionwert links vom Schnittpunkt
   * ist kleiner
   */
   public static final int LEFT_BELOW = 8;    // 001 000 binaer
   /**
   * Konstante fuer Funktionsorientierung: Funktionwert links vom Schnittpunkt
   * ist gleich
   */
   public static final int LEFT_EQUAL = 16 + 8;   // 011 000 binaer

   /**
   * binaere Maske fuer eine AND-Verknuepfung zur Ermittlung der rechten
   * Teilkonstante
   */
   public static final int RIGHT_MASK = 4 + 2 + 1; // 000 111 binaer
   /**
   * Konstante fuer Funktionsorientierung: Orientierung rechts vom Schnittpunkt
   * unbekannt
   */
   public static final int RIGHT_UNKNOWN = 4; // 000 100 binaer
   /**
   * Konstante fuer Funktionsorientierung: Funktionswert rechts vom Schnittpunkt
   * ist groesser
   */
   public static final int RIGHT_ABOVE = 2;   // 000 010 binaer
   /**
   * Konstante fuer Funktionsorientierung: Funktionswert rechts vom Schnittpunkt
   * ist kleiner
   */
   public static final int RIGHT_BELOW = 1;   // 000 001 binaer
   /**
   * Konstante fuer Funktionsorientierung: Funktionswert rechts vom Schnittpunkt
   * ist gleich
   */
   public static final int RIGHT_EQUAL = 2 + 1;   // 000 011 binaer


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert den Funktionswert fuer das spezifizierte <i>x</i>. Falls der
   * Funktionswert fuer das angegebene <i>x</i> nicht definiert ist, soll die
   * Methode den Wert <code>Double.NaN</code> zurueckliefern.
   *
   * @param x das Argument fuer diese Funktion
   * @return den Funktionswert
   */
   abstract public double f( double x );


   /**
   * Liefert den Grenzwert <i>lim</i>[<i>x</i>-><i>c</i>](<i>f(<i>x</i>)</i>)
   * dieses Polynoms <i>f</i> fuer <i>x</i> gegen <i>c</i>.<p>
   *
   * @param c der Annaeherungsparameter fuer <i>x</i>, fuer den der Grenzwert
   *        gesucht ist
   * @return den Grenzwert dieses Polynoms fuer <i>x</i> gegen <i>c</i>
   */
   abstract public double limit( double c );


   /**
   * Berechnung der Umkehrfunktion: Fuer ein gegebenes <i>f</i>(<i>x</i>) wird
   * das noetige Argument errechnet.<p>
   *
   * Standardmaessig wird bei Aufruf eine
   * <code>UnsupportedOperationException</code> ausgeworfen, dies bedeutet,
   * dass keine Umkehrfunktion definiert ist. Diese Methode sollte
   * ueberschrieben werden, wenn das implementierte Polynom umkehrbar ist.<br>
   * Falls die Umkehrfunktion fuer das gegebene <i>f</i>(<i>x</i>) nicht
   * eindeutig ist, soll der Wert <code>Double.NaN</code> zurueckgegeben
   * werden.<br>
   *
   * @param y das Argument der Umkehrfunktion dieses Polynoms
   * @return das Argument, das an dieses Polynom uebergeben werden muesste, um
   *         den Funktionswert <code>y</code> zu erhalten
   * @exception UnsupportedOperationException falls zu diesem Polynom keine
   *            Umkehrfunktion definiert ist
   */
   public double inverse(
      double y
   ) {

      throw new UnsupportedOperationException( UNDEFINED_INVERSE );

   } // inverse


   /**
   * Berechnung der Umkehrfunktion: Fuer ein gegebenes <i>f</i>(<i>x</i>) wird
   * das noetige Argument errechnet. Diese Methode liefert im Gegensatz zu
   * {@link #inverse(double) inverse} alle Loesungen fuer die Umkehrfunktion in
   * einem dynamischen Array.<p>
   *
   * Standardmaessig wird bei Aufruf eine
   * <code>UnsupportedOperationException</code> ausgeworfen, dies bedeutet,
   * dass keine Umkehrfunktion definiert ist. Diese Methode sollte
   * ueberschrieben werden, wenn die implementierte Funktion umkehrbar ist.<br>
   * Falls die Umkehrfunktion fuer das gegebene <i>f</i>(<i>x</i>) nicht
   * definiert ist, soll das Array aus einem einzigen Argument bestehen, dem
   * undefinierten Argument {@link Argument#NOT_DEFINED Argument.NOT_DEFINED}.
   *
   * @param y das Argument der Umkehrfunktion dieses Polynoms
   * @return ein dynamisches Array aller Argumente <i>x</i>, die eingesetzt in
   *         dieses Polynom den spezifizierten Funktionswert <code>y</code>
   *         ergeben
   * @exception UnsupportedOperationException falls zu diesem Polynom keine
   *            Umkehrfunktion definiert ist
   */
   public DynamicArray inverseAll(
      double y
   ) {

      throw new UnsupportedOperationException( UNDEFINED_INVERSE );

   } // inverseAll


   /**
   * Liefert die Nullstellen dieser Funktion unter Benutzung der Methode
   * {@link #inverseAll(double) inverseAll(...)}.<p>
   *
   * @return ein dynamisches Array aller Nullstellen
   * @exception UnsupportedOperationException falls zu diesem Polynom keine
   *            Umkehrfunktion definiert ist
   */
   public DynamicArray roots() {

      return inverseAll( 0.0 );

   } // roots


   /**
   * Liefert die erste Ableitungsfunktion dieser Funktion.
   *
   * @return die Ableitung
   */
   abstract public RealFunction derivative();


   /**
   * Liefert die n-te Ableitungsfunktion <i>f</i><sup>(<i>n</i>)</sup> dieser
   * Funktion <i>f</i> durch rekursives Ableiten.
   *
   * @param n der Index der gesuchten Ableitung
   * @return die Ableitung
   */
   public RealFunction derivative(
      int n
   ) {

      if ( n <= 0 ) return this;
      if ( n == 1 ) return derivative();
      return derivative().derivative( n - 1 );

   } // derivative


   /**
   * Liefert ein aufsteigend sortiertes dynamisches Array mit den lokalen
   * Extremwerten dieser Funktion.
   *
   * @return ein Array der lokalen Extrema
   */
   public DynamicArray extrema() {

      RealFunction f_1 = this.derivative(); // f'
      RealFunction f_2 = f_1.derivative();  // f''
      DynamicArray p = f_1.inverseAll( 0.0 ); // f'(x)=0
      DynamicArray ex = new DynamicArray();
      Argument px;
      double x, t;
      // fuer alle x aus p testen, ob f''(x[i])!=0:
      for ( int i = 0; i < p.size(); i++ ) {
         px = (Argument) p.get( i );
         x = ( (Double) px.n[0] ).doubleValue();
         t = f_2.f( x );
         if ( t > 0.0 )
            ex.add( new Minimum( px, new Argument( this.f( x ) ) ) );
         else if ( t < 0.0 )
            ex.add( new Maximum( px, new Argument( this.f( x ) ) ) );
      } // for
      return ex;

   } // extrema


   /**
   * Untersucht die Lage dieser Funktion zu der spezifizierten Funktion.
   * Betrachtet werden dazu alle Schnitte der beiden Funktionen in einem
   * festen gemeinsamen Intervall. Die Schnittobjekte und das Intervall werden
   * zusaetzlich uebergeben, um einen doppelten Berechnungsaufwand zu vermeiden,
   * da normalerweise diese Informationen sowieso vorliegen bzw. berechnet
   * werden muessen; ausserdem kann so ueber das Intervall der untersuchte Bereich
   * eingeschraenkt werden.<p>
   *
   * An die uebergebenen Parameter werden die folgenden Voraussetzungen
   * gestellt: <ul>
   * <li>Das Intervall muss in den Definitionsmengen beider Funktionen
   * vollstaendig enthalten sein.</li>
   * <li>Die Menge der Schnittobjekte muss fuer das gegebene Intervall
   * vollstaendig und korrekt sein.</li>
   * <li>Beide Funktionen muessen im betrachteten Intervall stetig ohne
   * Definitionsluecken sein.</li>
   * <li>Falls Funktionsstuecke (eine Funktion zwischen zwei Grenzwerten, siehe
   * {@link #intersection(Function) intersection}) als Schnittobjekte
   * vorkommen, muessen diese einer der beiden beteiligten Funktionen
   * entstammen, es darf darin keine fremde Funktion referenziert werden, wie
   * es die Definition der <code>intersection</code>-Methode erlaubt.</li></ul>
   * Falls nicht alle Voraussetzungen erfuellt sind, kann diese Methode ein
   * falsches Ergebnis liefern.<p>
   *
   * Das Ergebnis dieser Methode ist ein Ganzzahlarray, das eine um zwei
   * groessere Laenge hat wie das uebergebene Schnitt-Array. Fuer jedes Element im
   * Schnitt-Array gibt es eine entsprechende Konstante im Ergebnis-Array, die
   * die Orientierung <b>dieser</b> Funktion zur spezifizierten, jeweils links
   * und rechts des Schnittobjekts, beschreibt; als erstes und letztes Element
   * dieses Ergebnis-Arrays dienen zwei zusaetzliche Konstanten fuer die
   * Orientierung an den Intervallgrenzen. Eine solche Konstante ist
   * zusammengesetzt aus einer linken und rechten Komponente, wozu die
   * binaeren Konstanten {@link #LEFT_UNKNOWN LEFT_...} und
   * {@link #RIGHT_UNKNOWN RIGHT_...} in Kombination verwendet werden; die
   * erste bzw. letzte Konstante des Arrays enthaelt immer die Komponente
   * <code>LEFT_UNKNOWN</code> bzw. <code>RIGHT_UNKNOWN</code>.
   * Folgendes Beispiel demonstriert die Verwendung:<br>
   * Gegeben sind die Funktionen <i>f(<i>x</i>)</i> = <i>x</i> und
   * <i>g(<i>x</i>)</i> = -<i>x</i>. Im Intervall <i>I</i> = [-1; 1] ist die
   * Schnittmenge <i>S</i> = {0}. Der Aufruf
   * <code>f.getOrientation( g, I, S )</code> liefert dann ein dreielementiges
   * Array <i>A</i> = {(<code>LEFT_UNKNOWN</code>+<code>RIGHT_BELOW</code>),
   * (<code>LEFT_BELOW</code>+<code>RIGHT_ABOVE</code>),
   * (<code>LEFT_ABOVE</code>+<code>RIGHT_UNKNOWN</code>)}.<br>
   * Die Schnittmenge kann neben <code>Argument</code>-Objekten auch von
   * <code>Bound</code> eingeschlossene <code>Function</code>-Objekte
   * enthalten. Einer
   * <code>Bound</code>-<code>Function</code>-<code>Bound</code>-Kette wird
   * immer eine Konstantenkette der Form
   * (<code>LEFT_...</code>+<code>RIGHT_EQUAL</code>) -
   * (<code>LEFT_EQUAL</code>+<code>RIGHT_EQUAL</code>) -
   * (<code>LEFT_EQUAL</code>+<code>RIGHT_...</code>) zugeordnet.<p>
   *
   * Folgende Sonderfaelle existieren:<ol>
   * <li>Das gemeinsame Intervall besteht nur aus einem Punkt. Das Ergebnis
   * besteht dann zweimal aus der Konstanten
   * (<code>LEFT_UNKNOWN</code>+<code>RIGHT_UNKNOWN</code>) (dreimal, wenn
   * dieser Punkt auch Schnittpunkt ist).</li>
   * <li>Es gibt keine Schnittmenge, dann besteht das Ergebnis aus den beiden
   * Konstanten (<code>LEFT_UNKNOWN</code>+<code>RIGHT_XXX</code>) und
   * (<code>LEFT_XXX</code>+<code>RIGHT_UNKNOWN</code>), wobei <code>XXX</code>
   * identisch ist.</li>
   * <li>Wenn ein Schnittpunkt auf einer Intervallgrenze liegt, gilt: Der
   * Intervallgrenze wird die Konstante
   * (<code>LEFT_UNKNOWN</code>+<code>RIGHT_UNKNOWN</code>) zugewiesen und dem
   * Schnittpunkt eine Konstante der Form<ul>
   * <li>(<code>LEFT_UNKNOWN</code>+<code>RIGHT_XXX</code>) (im Fall der linken
   * Intervallgrenze), oder</li>
   * <li>(<code>LEFT_XXX</code>+<code>RIGHT_UNKNOWN</code>)
   * (im Fall der rechten Intervallgrenze).</li></ul></li></ol>
   *
   * @param function die zweite Funktion, zu der die Orientierung dieser
   *        Funktion ermittelt werden soll
   * @param interval das gemeinsame Intervall, fuer das die Orientierung
   *        untersucht wird
   * @param intersection die Schnittmenge beider Funktionen im untersuchten
   *        Intervall
   * @return ein Array mit je einer Orientierungskonstanten pro Schnittpunkt
   */
   public int[] getOrientation(
      RealFunction function,
      Interval interval,
      DynamicArray intersection
   ) {

      int result[] = new int[intersection.size() + 2];
      Bound[] b = interval.getBounds();
      double x,y1,y2;

      // Intervall ist nur ein Punkt:
      if ( interval.isPoint() ) {
         result[0] = LEFT_UNKNOWN + RIGHT_UNKNOWN;
         result[1] = result[0];
         if ( ! intersection.isEmpty() )
            result[2] = result[0];
         return result;
      } // if

      // keine Schnittmenge:
      if ( intersection.isEmpty() ) {
         // nur die beiden Konstanten fuer die Intervallgrenzen erzeugen:
         x = ( (Double) b[0].center( b[1] ).n[0] ).doubleValue();
         y1 = this.f( x );
         y2 = function.f( x );
         if ( y1 < y2 ) {
            result[0] = LEFT_UNKNOWN + RIGHT_BELOW;
            result[1] = LEFT_BELOW + RIGHT_UNKNOWN;
         } else { // if
            result[0] = LEFT_UNKNOWN + RIGHT_ABOVE;
            result[1] = LEFT_ABOVE + RIGHT_UNKNOWN;
         } // else
         return result;
      } // if

      // Konstante fuer linke Intervallgrenze erzeugen:
      Argument old = b[0];
      Argument s = (Argument) intersection.first();
      int left = LEFT_UNKNOWN;
      int right;
      int k = 0;
      if ( ( (Double) s.n[0] ).equals( b[0].n[0] ) )
         right = RIGHT_UNKNOWN;
      else {
         x = ( (Double) s.center( old ).n[0] ).doubleValue();
         y1 = this.f( x );
         y2 = function.f( x );
         if ( y1 < y2 )
            right = RIGHT_BELOW;
         else if ( y1 > y2 )
            right = RIGHT_ABOVE;
         else
            right = RIGHT_EQUAL;
      } // else
      result[k++] = left + right;

      // bis zum vorletzten Schnittpunkt Konstanten erzeugen:
      Object o;
      while ( k < intersection.length() ) {
         old = s;
         left = right << 3;
         o = intersection.get( k );
         // eine Schnittfunktion abfangen:
         if ( o instanceof Function )
            right = RIGHT_EQUAL;
         else { // if
            s = (Argument) o;
            x = ( (Double) s.center( old ).n[0] ).doubleValue();
            y1 = this.f( x );
            y2 = function.f( x );
            if ( y1 < y2 )
               right = RIGHT_BELOW;
            else if ( y1 > y2 )
               right = RIGHT_ABOVE;
            else
               right = RIGHT_EQUAL;
         } // else
         result[k++] = left + right;
      } // for

      // fuer letzten Schnittpunkt und rechte Grenze Konstanten erzeugen:
      left = right << 3;
      if ( ( (Double) s.n[0] ).equals( b[1].n[0] ) ) {
         result[k++] = left + RIGHT_UNKNOWN;
         result[k] = LEFT_UNKNOWN + RIGHT_UNKNOWN;
      } else { // if
         x = ( (Double) b[1].center( s ).n[0] ).doubleValue();
         y1 = this.f( x );
         y2 = function.f( x );
         if ( y1 < y2 ) {
            result[k++] = left + RIGHT_BELOW;
            result[k] = LEFT_BELOW + RIGHT_UNKNOWN;
         } else { // if
            result[k++] = left + RIGHT_ABOVE;
            result[k] = LEFT_ABOVE + RIGHT_UNKNOWN;
         } // else
      } // else

      return result;

   } // getOrientation


   /**
   * Testet ein Argument, ob es eine einzelne reelle Zahl repraesentiert.
   *
   * @param x das Argument
   * @return <code>true</code>, falls das Argument eine reelle Zahl ist, sonst
   *         <code>false<code>
   */
   public boolean isValidArgument(
      Argument x
   ) {

      return ( x != null ) &&
             ( x.dimension() == 1 ) &&
             ( x.n[0] != null ) &&
             ( ! x.n[0].equals( Argument.NOT_DEFINED ) ) &&
             ( x.n[0] instanceof Double );

   } // isValidArgument


   /**
   * Testet, ob das spezifizierte Argument in der Definitionsmenge dieser
   * Funktion liegt.
   *
   * @param x das zu testende Argument
   * @return <code>true</code>, wenn das Argument in der Definitionsmenge
   *         liegt, sonst <code>false</code>
   */
   public boolean isDefinedFor(
      double x
   ) {

      return ( (RealDomain) domain() ).contains( x );

   } // isDefinedFor


   /*
   * [javadoc-Beschreibung wird aus Function kopiert]
   */
   public Argument f(
      Argument x
   ) {

      if ( ! isValidArgument( x ) ) {
         throw new IllegalArgumentException( Function.ILLEGAL_ARGUMENT );
      } // if

      double z = x.n[0].doubleValue();
      return new Argument( f( z ) );

   } // f


   /*
   * [javadoc-Beschreibung wird aus Function kopiert]
   */
   public Argument limit(
      Argument c
   ) {

      if ( ! isValidArgument( c ) ) {
         throw new IllegalArgumentException( Function.ILLEGAL_ARGUMENT );
      } // if

      double z = c.n[0].doubleValue();
      return new Argument( limit( z ) );

   } // limit


   /*
   * [javadoc-Beschreibung wird aus Function kopiert]
   */
   public Argument inverse(
      Argument y
   ) {

      if ( ! isValidArgument( y ) ) {
         throw new IllegalArgumentException( Function.ILLEGAL_ARGUMENT );
      } // if

      double z = y.n[0].doubleValue();
      return new Argument( inverse( z ) );

   } // inverse


   /*
   * [javadoc-Beschreibung wird aus Function kopiert]
   */
   public DynamicArray inverseAll(
      Argument y
   ) {

      if ( ! isValidArgument( y ) ) {
         throw new IllegalArgumentException( Function.ILLEGAL_ARGUMENT );
      } // if

      double z = y.n[0].doubleValue();
      return inverseAll( z );

   } // inverseAll


   /**
   * Rudimentaere Schnittpunktberechnung: liefert nur bei identischen Funktionen
   * die Schnittmenge, die dieser Funktion ueber ihrer Definitionsmenge
   * entspricht. Im Falle, dass die spezifizierte Funktion keine reelle
   * Funktion ist, wird eine <code>IllegalArgumentException</code> ausgeworfen;
   * in allen anderen Faellen wird eine
   * <code>UnsupportedOperationException</code> ausgeworfen.
   *
   * @param g die Schnittfunktion
   * @return die Schnittmenge (Konzept siehe {@link Function Function})
   * @exception IllegalArgumentException falls die spezifizierte Funktion keine
   *            reelle Funktion ist
   * @exception UnsupportedOperationException falls die spezifizierte Funktion
   *            nicht mit dieser Funktion identisch ist
   */
   public DynamicArray intersection(
      Function g
   ) {

      // Test auf reelle Funktion:
      RealFunction p = null;
      try {
         p = (RealFunction) g;
      } catch ( ClassCastException cce ) { // try
         throw new IllegalArgumentException( Function.ILLEGAL_ARGUMENT );
      } // catch

      // Test auf Identitaet:
      if ( this.equals( p ) ) return identityIntersection( this );

      // sonst Exception werfen:
      throw new UnsupportedOperationException(
                                           Function.UNAVAILABLE_INTERSECTION );

   } // intersection


   // *************************************************************************
   // Protected methods
   // *************************************************************************

   /**
   * Hilfsmethode fuer Schnittberechnung in dieser und den Unterklassen: liefert
   * ein dynamisches Array, das als Schnittobjekt die spezifizierte Funktion
   * <i>f</i> ueber ihrer Definitionsmenge enthaelt.
   *
   * @param f die Schnittfunktion
   * @return ein Array, das die spezifizierte Funktion als Schnittobjekt
   *         repraesentiert
   */
   protected DynamicArray identityIntersection(
      RealFunction f
   ) {

      DynamicArray bound = ( (RealDomain) f.domain() ).getBounds();
      DynamicArray a = new DynamicArray( ( bound.size() / 2 ) * 3 );
      for ( int i = 0; i < bound.size(); i++ ) {
         a.add( bound.get( i++ ) );
         a.add( this );
         a.add( bound.get( i ) );
      } // for
      return a;

   } // identityIntersection


} // RealFunction
