package anja.analysis;

import java.util.Enumeration;

import anja.util.DynamicArray;
import anja.util.ListItem;
import anja.util.SimpleList;


/**
* Definitionsbereich von Funktionen des Typs {@link RealFunction}, die eine
* reelle Zahl als Argument und Funktionswert haben. Diese Definitionsmenge wird
* gebildet durch eine geordnete Liste von sich nicht ueberschneidenden
* Intervallen aus der Menge <i><b>R</b></i>.
*
* @version 0.7 25.03.2004
* @author Sascha Ternes
*/

public class RealDomain
implements Cloneable,
           Domain
{

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * die leere Menge <b>{}</b>
   */
   public static final RealDomain EMPTY = new RealDomain( EMPTY_DOMAIN );

   /**
   * die Menge <i><b>R</b></i>
   */
   public static final RealDomain R = new RealDomain();

   /**
   * Exception bei ungueltigem Intervall
   */
   public static final String INVALID_INTERVAL = "illegal interval given!";


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // die Definitionsmenge:
   private SimpleList _set;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt die Menge <i><b>R</b></i> aller reellen Zahlen.
   */
   public RealDomain() {

      this( 1 );

   } // RealDomain


   /**
   * Falls der Parameter <i>Null</i> ist, erzeugt dieser Konstruktor die leere
   * Menge <b>{}</b>. Bei allen anderen Werten wird die Menge <i><b>R</b></i>
   * der reellen Zahlen erzeugt.<p>
   *
   * @param n die Art der erzeugten Definitionsmenge; bei <i>Null</i> wird die
   *        leere Menge erzeugt, sonst die Menge <i><b>R</b></i>
   */
   public RealDomain(
      int n
   ) {

      _set = new SimpleList();
      if ( n != EMPTY_DOMAIN )
         _set.add( Interval.REAL_NUMBERS );

   } // RealDomain


   /**
   * Erzeugt eine neue Definitionsmenge, die aus dem spezifizierten Intervall
   * besteht. Falls das Intervall nicht eindimensional ist oder seine Grenzen
   * keine reellen Zahlen sind, wird eine <code>IllegalArgumentException</code>
   * ausgeworfen.
   *
   * @param interval das Intervall
   * @exception IllegalArgumentException falls das Intervall nicht
   *            eindimensional reell ist
   */
   public RealDomain(
      Interval interval
   ) {

      _set = new SimpleList();
      add( interval );

   } // RealDomain


   /*
   * Privater Konstruktor fuer die Methode union(...). Erzeugt eine
   * Definitionsmenge mit der gegebenen Intervallliste.
   */
   private RealDomain(
      SimpleList list
   ) {

      _set = list;

   } // RealDomain


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Fuegt das spezifizierte Intervall der Definitionsmenge hinzu. Falls das
   * Intervall nicht eindimensional ist oder seine Grenzen keine reellen Zahlen
   * sind, wird eine <code>IllegalArgumentException</code> ausgeworfen.
   *
   * @param interval das hinzuzufuegende Intervall
   * @exception IllegalArgumentException falls das Intervall nicht
   *            eindimensional reell ist
   */
   public void add(
      Interval interval
   ) {

      // Intervall ueberpruefen:
      Bound[] bounds = interval.getBounds();
      if ( ( ! isValidArgument( bounds[0] ) ) ||
           ( ! isValidArgument( bounds[1] ) ) )
         throw new IllegalArgumentException( INVALID_INTERVAL );

      // in leere Menge Intervall einfach einfuegen:
      if ( _set.empty() ) {
         _set.add( interval );
         return;
      } // if

      // Intervall an der richtigen Stelle einfuegen:
      double bound0 = ( (Double) bounds[0].n[0] ).doubleValue();
      double bound1 = ( (Double) bounds[1].n[0] ).doubleValue();
      ListItem item = _set.first();
      Interval curr = interval;
      Interval a, b;
      double a0, a1;
      while ( item != null ) {
         a = (Interval) item.value();
         bounds = a.getBounds();
         a0 = ( (Double) bounds[0].n[0] ).doubleValue();
         a1 = ( (Double) bounds[1].n[0] ).doubleValue();
         // Intervall vor dem aktuellen einfuegen, ohne zusammenzufassen:
         if ( bound1 < a0 ) {
            _set.insert( item, curr );
            return;
         } else // if
         // aktuelles Intervall ueberspringen:
         if ( bound0 > a1 )
            item = _set.next( item );
         // Test auf Zusammenfassbarkeit:
         else {
            b = a.union( curr );
            // mit aktuellem zusammenfassen und damit naechster Durchlauf:
            if ( b != null ) {
               curr = b;
               bounds = b.getBounds();
               bound0 = ( (Double) bounds[0].n[0] ).doubleValue();
               bound1 = ( (Double) bounds[1].n[0] ).doubleValue();
               ListItem i = item;
               item = _set.next( item );
               _set.remove( i );
            } else // if
            // Intervall vor dem aktuellen einfuegen, ohne zusammenzufassen:
            if ( bound1 == a0 ) {
               _set.insert( item, curr );
               return;
            // aktuelles Intervall ueberspringen
            } else // if
               item = _set.next( item );
         } // else
      } // while

      // hier angekommen, muss das Intervall am Ende angefuegt werden:
      _set.add( curr );

   } // add


   /**
   * Testet, ob die spezifizierte Zahl ein Element dieser Definitionsmenge
   * ist.
   *
   * @param x die Zahl
   * @return <code>true</code> falls die Zahl Element dieser
   *         Definitionsmenge ist, sonst <code>false</code>
   */
   public boolean contains(
      double x
   ) {

      return contains( new Argument( x ) );

   } // contains


   /**
   * Testet, ob diese Definitionsmenge <i>zusammenhaengend</i> ist, ob sie also
   * aus maximal einem einzigen Intervall besteht.
   *
   * @return <code>true</code>, falls diese Definitionsmenge zusammenhaengend
   *         ist, sonst <code>false</code>
   */
   public boolean isContiguous() {

      if ( _set.length() <= 1 ) return true;
      return false;

   } // isContiguous


   /**
   * Liefert die Intervallgrenzen der Intervalle, die diese Definitionsmenge
   * erzeugen. Diese Intervallgrenzen sind aufsteigend sortiert und bilden
   * jeweils ein Paar (<i>a<sub>i</sub></i>,<i>a<sub>i</sub></i>) pro Intervall
   * <i>I<sub>i</sub></i>. Das Rueckggabearray enthaelt also 2<i>n</i>
   * Intervallgrenzen fuer <i>n</i> Intervalle.
   *
   * @return die geordnete Menge der Intervalle
   */
   public DynamicArray getBounds() {

      Interval[] intervals = getIntervals();
      DynamicArray bounds = new DynamicArray( 2 * intervals.length );
      Bound[] b;
      for ( int i = 0; i < intervals.length; i++ ) {
         b = intervals[i].getBounds();
         bounds.add( b[0] );
         bounds.add( b[1] );
      } // for
      return bounds;

   } // getBounds


   /**
   * Liefert die Intervalle, die diese Definitionsmenge erzeugen. Diese
   * Intervalle sind aufsteigend sortiert.
   *
   * @return die geordnete Menge der Intervalle
   */
   public Interval[] getIntervals() {

      Interval[] intervals = new Interval[_set.length()];
      ListItem item = _set.first();
      for ( int i = 0; i < intervals.length; i++ ) {
         intervals[i] = (Interval) item.value();
         item = _set.next( item );
      } // for
      return intervals;

   } // getIntervals


   /**
   * Liefert eine textuelle Repraesentation.
   *
   * @return diese Definitionsmenge als String
   */
   public String toString() {

      String s = "{";
      Enumeration e = _set.values();
      while ( e.hasMoreElements() ) {
         s += e.nextElement();
         if ( e.hasMoreElements() ) s += ", ";
      } // while
      s += "}";
      return s;

   } // toString


   // *************************************************************************
   // Interface Domain
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus Domain kopiert]
   */
   public Object clone() {

      RealDomain domain = new RealDomain( EMPTY_DOMAIN );
      Enumeration e = this._set.values();
      while ( e.hasMoreElements() )
         domain._set.add( ( (Interval) e.nextElement() ).clone() );
      return domain;

   } // clone


   /*
   * [javadoc-Beschreibung wird aus Domain kopiert]
   */
   public boolean contains(
      Argument x
   ) {

      Enumeration e = _set.values();
      while ( e.hasMoreElements() )
         if ( ( (Interval) e.nextElement() ).contains( x ) )
            return true;
      return false; // nicht enthalten

   } // contains


   /**
   * Liefert als Dimension dieser Definitionsmenge die Zahl <code>0</code>,
   * falls diese Menge leer ist, sonst die Zahl <code>1</code>.
   */
   public int dimension() {

      if ( _set.empty() ) return 0;
      else return 1;

   } // dimension


   /*
   * [javadoc-Beschreibung wird aus Domain kopiert]
   */
   public boolean equals(
      Object o
   ) {

      return ( (RealDomain) o )._set.equals( this._set );

   } // equals


   /*
   * [javadoc-Beschreibung wird aus Domain kopiert]
   */
   public Domain intersection(
      Domain domain
   ) {

      // Dimensionalitaet pruefen:
      if ( ! ( domain instanceof RealDomain ) )
         throw new IllegalArgumentException( WRONG_DIMENSION );

      RealDomain r = new RealDomain( EMPTY_DOMAIN ); // Rueckgabe
      // wenn es eine leere Menge gibt, fertig:
      if ( isEmpty() || domain.isEmpty() )
         return r;

      // die Schranken beider Definitionsmengen ermitteln:
      DynamicArray a = this.getBounds();
      DynamicArray b = ( (RealDomain) domain ).getBounds();
      int i = 0; // aktuelle linke Grenze von dieser Menge
      int j = 0; // aktuelle linke Grenze von Parameter-Menge
      Bound n = null; // aktuelle linke Grenze der neuen Menge
      // Schranken durchlaufen:
      while ( ( i < a.length() ) && ( j < b.length() ) ) {
         Bound aa1 = (Bound) a.get( i );
         Bound bb1 = (Bound) b.get( j );
         Bound aa2 = (Bound) a.get( i + 1 );
         Bound bb2 = (Bound) b.get( j + 1 );
         double a1 = ( (Double) aa1.n[0] ).doubleValue();
         double b1 = ( (Double) bb1.n[0] ).doubleValue();
         double a2 = ( (Double) aa2.n[0] ).doubleValue();
         double b2 = ( (Double) bb2.n[0] ).doubleValue();

         // a liegt links:
         if ( a1 < b1 )

            // b beginnt in a:
            if ( b1 < a2 )
               // b liegt ganz in a:
               if ( b2 < a2 ) {
                  r._set.add( new Interval( bb1, bb2 ) );
                  j += 2;
               // b geht ueber a hinaus:
               } else if ( b2 > a2 ) {
                  r._set.add( new Interval( bb1, aa2 ) );
                  i += 2;
               // a und b enden gleichzeitig:
               } else { // else
                  // exklusive Schranke beruecksichtigen:
                  if ( ! aa2.inclusion[0] )
                     r._set.add( new Interval( bb1, aa2 ) );
                  else
                     r._set.add( new Interval( bb1, bb2 ) );
                  i += 2;
                  j += 2;
               } // else

            // b liegt rechts von a:
            else if ( b1 > a2 )
               i += 2;

            // b beginnt auf dem Ende von a:
            else {
               // bilde ein Intervall nur aus der inklusiven Schranke:
               if ( aa2.inclusion[0] && bb1.inclusion[0] )
                  r._set.add( new Interval( bb1, bb1 ) );
               i += 2;
            } // else

         // b liegt links:
         else if ( a1 > b1 )

            // a beginnt in b:
            if ( a1 < b2 )
               // a liegt ganz in b:
               if ( a2 < b2 ) {
                  r._set.add( new Interval( aa1, aa2 ) );
                  i += 2;
               // a geht ueber b hinaus:
               } else if ( a2 > b2 ) {
                  r._set.add( new Interval( aa1, bb2 ) );
                  j += 2;
               // a und b enden gleichzeitig:
               } else { // else
                  // exklusive Schranke beruecksichtigen:
                  if ( ! aa2.inclusion[0] )
                     r._set.add( new Interval( aa1, aa2 ) );
                  else
                     r._set.add( new Interval( aa1, bb2 ) );
                  i += 2;
                  j += 2;
               } // else

            // a liegt rechts von b:
            else if ( a1 > b2 )
               j += 2;

            // a beginnt auf dem Ende von b:
            else {
               // bilde ein Intervall nur aus der inklusiven Schranke:
               if ( aa1.inclusion[0] && bb2.inclusion[0] )
                  r._set.add( new Interval( aa1, aa1 ) );
               j += 2;
            } // else

         // a und b sind links gleich:
         else {
            Bound begin = null;
            // exklusive Schranke beruecksichtigen:
            if ( ! aa1.inclusion[0] ) begin = aa1;
            else begin = bb1;
            // b endet in a:
            if ( b2 < a2 ) {
               r._set.add( new Interval( begin, bb2 ) );
               j += 2;
            // b geht ueber a hinaus:
            } else if ( b2 > a2 ) {
               r._set.add( new Interval( begin, aa2 ) );
               i += 2;
            // a und b enden gleichzeitig:
            } else {
               // exklusive Schranke beruecksichtigen:
               if ( ! aa2.inclusion[0] )
                  r._set.add( new Interval( begin, aa2 ) );
               else
                  r._set.add( new Interval( begin, bb2 ) );
               i += 2;
               j += 2;
            } // else
         } // else
      } // while
      return r;

   } // intersection


   /*
   * [javadoc-Beschreibung wird aus Domain kopiert]
   */
   public boolean isEmpty() {

      return _set.empty();

   } // isEmpty


   /**
   * Testet ein Argument, ob es eine einzelne reelle Zahl repraesentiert.
   *
   * @param x das Argument
   * @return <code>true</code>, falls das Argument eine reelle Zahl ist, sonst
   *         <code>false</code>
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


   /*
   * [javadoc-Beschreibung wird aus Domain kopiert]
   */
   public Domain union(
      Domain domain
   ) {

      // Dimensionalitaet pruefen:
      if ( ! ( domain instanceof RealDomain ) )
         throw new IllegalArgumentException( WRONG_DIMENSION );

      // auf Kopien der Definitionsmenge arbeiten:
      SimpleList domain1 = this._set.copyList();
      SimpleList domain2 = ( (RealDomain) domain )._set.copyList();
      // die beiden Definitionsmengen durchlaufen und in domain1 zusammenf.:
      ListItem item1 = domain1.first();
      ListItem item2 = domain2.first();
      Interval i1 = null;
      Interval i2 = null;
      Bound[] b1 = null;
      Bound[] b2 = null;
      if ( item1 != null ) {
         i1 = (Interval) item1.value();
         b1 = i1.getBounds();
      } // if
      if ( item2 != null ) {
         i2 = (Interval) item2.value();
         b2 = i2.getBounds();
      } // if
      Interval test;
      int comp;
      ListItem item;
      while ( ( item1 != null ) && ( item2 != null ) ) {
         test = i1.union( i2 );
         // die beiden aktuellen Intervalle koennen vereinigt werden:
         if ( test != null ) {
            comp = ( (Double) b1[1].n[0] ).compareTo( (Double) b2[1].n[0] );
            // Intervall 1 liegt links von oder ganz in Intervall 2:
            if ( comp < 0 ) {
               item = item1;
               item1 = domain1.next( item1 ); // naechstes Intervall 1 bestimmen
               if ( item1 != null ) {
                  i1 = (Interval) item1.value();
                  b1 = i1.getBounds();
               } // if
               domain1.remove( item ); // altes Intervall 1 entfernen
               domain2.insert( item2, test ); // vereinigtes Intervall einfuegen
               item = item2;
               item2 = domain2.prev( item2 ); // wird auch naechstes Intervall 2
               if ( item2 != null ) {
                  i2 = (Interval) item2.value();
                  b2 = i2.getBounds();
               } // if
               domain2.remove( item ); // altes Intervall 2 entfernen
            // Intervall 2 liegt links von oder ganz in Intervall 1:
            } else if ( comp > 0 ) {
               item2 = domain2.next( item2 ); // naechstes Intervall 2 bestimmen
               if ( item2 != null ) {
                  i2 = (Interval) item2.value();
                  b2 = i2.getBounds();
               } // if
               domain1.insert( item1, test ); // vereinigtes Intervall einfuegen
               item = item1;
               item1 = domain1.prev( item1 ); // wird auch naechstes Intervall 1
               if ( item1 != null ) {
                  i1 = (Interval) item1.value();
                  b1 = i1.getBounds();
               } // if
               domain1.remove( item ); // altes Intervall 1 entfernen
            // die Intervalle enden gleichzeitig:
            } else { // if
               // Ersetzen von Intervall 2 durch das vereinigte Intervall:
               if ( b2[1].inclusion[0] ) {
                  item = item1;
                  item1 = domain1.next( item1 ); // naechstes Intervall 1
                  if ( item1 != null ) {
                     i1 = (Interval) item1.value();
                     b1 = i1.getBounds();
                  } // if
                  domain1.remove( item );
                  domain2.insert( item2, test ); // vereinigtes Intervall einf.
                  item = item2;
                  item2 = domain2.prev( item2 ); // wird auch neues Intervall 2
                  if ( item2 != null ) {
                     i2 = (Interval) item2.value();
                     b2 = i2.getBounds();
                  } // if
                  domain2.remove( item ); // altes Intervall 2 entfernen
               // Ersetzen von Intervall 1 durch das vereinigte Intervall:
               } else {
                  item2 = domain2.next( item2 ); // naechstes Intervall 2
                  if ( item2 != null ) {
                     i2 = (Interval) item2.value();
                     b2 = i2.getBounds();
                  } // if
                  domain1.insert( item1, test ); // vereinigtes Intervall einf.
                  item = item1;
                  item1 = domain1.prev( item1 ); // wird auch neues Intervall 1
                  if ( item1 != null ) {
                     i1 = (Interval) item1.value();
                     b1 = i1.getBounds();
                  } // if
                  domain1.remove( item ); // altes Intervall 1 entfernen
               } // else
            } // else
         } // if

         // die beiden aktuellen Intervalle koennen nicht vereinigt werden:
         else {
            // Intervall 2 liegt rechts von Intervall 1:
            if ( ( (Double) b1[1].n[0] ).compareTo(
                                                  (Double) b2[1].n[0] ) < 0 ) {
               item1 = domain1.next( item1 ); // naechstes Intervall 1
               if ( item1 != null ) {
                  i1 = (Interval) item1.value();
                  b1 = i1.getBounds();
               } // if
            // Intervall 2 liegt links von Intervall 1:
            } else {
               item = item2;
               item2 = domain2.next( item2 ); // naechstes Intervall 2
               if ( item2 != null ) {
                  i2 = (Interval) item2.value();
                  b2 = i2.getBounds();
               } // if
               domain2.remove( item );        // Intervall 2 entfernen
               domain1.insert( item1, item ); // Intervall 2 einfuegen
            } // else
         } // else
      } // while

      // wenn es keine hinzuzufuegenden Intervalle mehr gibt, fertig:
      if ( item2 == null ) return new RealDomain( domain1 );
      // sonst die restlichen Intervalle anhaengen:
      while ( item2 != null ) {
         domain1.add( item2 );
         item2 = domain2.next( item2 );
      } // while
      return new RealDomain( domain1 ); // fertig

   } // union


} // RealDomain
