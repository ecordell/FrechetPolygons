package anja.analysis;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;

import anja.util.DynamicArray;


/**
* Allgemeines Polynom der Form <i>f</i>(<i>x</i>) =
* <i>k<sub>n</sub>x<sup>n</sup></i> +
* <i>k<sub>n</i>-1</i></sub><i>x<sup>n</i>-1</sup> + ... +
* <i>k<sub></i>1<i></sub>x</i> + <i>k</i><sub>0</sub><br>.
*
* @version 0.85 29.11.2005
* @author Sascha Ternes
*/

public class Polynomial
extends RealFunction
{

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * Exception, wenn der nte uebergebene Koeffizient im Konstruktor Null ist;
   * wird lediglich von den Subklassen verwendet
   */
   public static final String ZERO_COEFFICIENT =
                                           "nth coefficient must not be zero!";

   /**
   * Exception, wenn der Koeffizient mit dem spezifizierten Index nicht
   * existiert
   */
   public static final String INVALID_INDEX =
                                         "no coefficient existing with index ";


   // *************************************************************************
   // Private constants
   // *************************************************************************

   // Koeffizienten fuer Nullfunktion:
   private static final double[] _ZERO = { 0.0 };


   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * die Koeffizienten
   */
   protected double[] _coefficients;

   /**
   * die Nullstellen
   */
   protected TreeSet _roots;
   
   /**
    * Flag ob Nullstellen bereits berechnet
    */
   protected boolean _rootsCalculated;
   
   
   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt die konstante Nullfunktion <i>f</i>(<i>x</i>) = 0.
   */
   public Polynomial() {

      this( _ZERO );

   } // Polynomial


   /**
   * Erzeugt ein Polynom mit den angegebenen Koeffizienten. Der groesste Index
   * aller Koeffizienten, die ungleich Null sind, bestimmt den Grad des
   * Polynoms. Die Koeffizienten eines Polynoms vom Grad <i>n</i> werden in
   * einem (mindestens) <i>n</i>+1-elementigen Array "von hinten nach vorne"
   * nach folgendem Schema uebergeben:<p>
   *
   * Polynom: <i>f</i>(<i>x</i>) = <i>k<sub>n</sub>x<sup>n</sup></i> +
   * <i>k<sub>n</i>-1</i></sub><i>x<sup>n</i>-1</sup> + ... +
   * <i>k<sub></i>1<i></sub>x</i> + <i>k</i><sub>0</sub><br>
   * Koeffizientenarray: { <i>k</i><sub>0</sub>, <i>k</i><sub>1</sub>, ...
   * <i>k<sub>n</i>-1</sub>, <i>k<sub>n</sub></i> }<p>
   *
   * Somit entspricht der Index eines Koeffizienten dem Arrayindex dieses
   * Koeffizienten, und das Array hat (mindestens) die Laenge <i>n</i>+1.
   *
   * @param coefficients ein Array mit den Koeffizienten des Polynoms; falls
   *        das Array keine Elemente enthaelt oder <code>null</code> ist, wird
   *        die konstante Nullfunktion erzeugt.
   */
   public Polynomial(
      double[] coefficients
   ) {

      if ( ( coefficients == null ) || ( coefficients.length == 0 ) )
         _coefficients = _ZERO;
      else _coefficients = coefficients;
      _roots = null;
      _rootsCalculated = false;
      _domain = new RealDomain();

   } // Polynomial


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Erzeugt ein neues Polynom mit den spezifizierten Nullstellen.
   *
   * @param roots die Nullstellen
   * @return ein neues Polynom
   */
   public static Polynomial createFromZeroSpots(
      double[] roots
   ) {

      // Erzeugung der Polynom-Rohversion:
      Polynomial p = new Polynomial();
      // sortierte Menge der Nullstellen erzeugen und speichern:
      p._roots = new TreeSet();
      for ( int i = 0; i < roots.length; i++ )
         p._roots.add( new Double( roots[i] ) );
      p._rootsCalculated = true;
      // Koeffizientenberechnung:
      Iterator i = p._roots.iterator();
      int n = p._roots.size() + 1;
      double[] c = new double[n];
      double[] d = new double[n];
      double z = ( (Double) i.next() ).doubleValue();
      c[1] = 1.0; // x
      c[0] = - z; //  - z
      int k = 2; // naechster Koeffizient
      while ( k < n ) {
         // kopiere die bisherigen Nullstellen:
         for ( int j = 0; j < k; j++ ) d[j] = c[j];
         // naechste Nullstelle:
         z = ( (Double) i.next() ).doubleValue();
         // neue Koeffizienten:
         c[k] = 1.0;
         for ( int j = k - 1; j > 0; j-- )
            c[j] = d[j - 1] - ( d[j] * z );
         c[0] = - d[0] * z;
         k++; // naechster Durchlauf
      } // while
      // Vervollstaendigung des Polynoms:
      p._coefficients = c;
      return p;

   } // createFromZeroSpots


   /*
   * [javadoc-Beschreibung wird aus RealFunction kopiert]
   */
   public Object clone() {

      Polynomial poly = new Polynomial();
      poly._coefficients = new double[this._coefficients.length];
      for ( int i = 0; i < _coefficients.length; i++ )
         poly._coefficients[i] = this._coefficients[i];
      if ( this._roots != null )
         poly._roots = (TreeSet) this._roots.clone();
      poly._domain = (RealDomain) this._domain.clone();
      return poly;

   } // clone


   /*
   * [javadoc-Beschreibung wird aus RealFunction kopiert]
   */
   public boolean equals(
      Object obj
   ) {

      // nur andere Polynome koennen getestet werden:
      Polynomial g = null;
      try {
         g = (Polynomial) obj;
      } catch ( ClassCastException cce ) { // try
         return false;
      } // catch
      // nur Polynome vom gleichen Grad koennen identisch sein:
      if ( g.degree() != this.degree() ) return false;
      // nur Polynome mit gleichen Koeffizienten sind identisch:
      for ( int i = 0; i < _coefficients.length; i++ ) {
         if ( g._coefficients[i] != this._coefficients[i] ) return false;
      } // for

      return true;

   } // equals

   /**
    * Gibt Nullstellen der Funktion zurueck
    */
   public double[] getRoots()
   {
	   if (_rootsCalculated == false)
	   {
		   _calculateRoots();
	   }
	   Iterator i = _roots.iterator();
	   double[] doRo = new double[_roots.size()];
	   for (int j=0;j < _roots.size(); j++)
		   doRo[j] = ((Double)i.next()).doubleValue();
	   return doRo;
   } // getRoots
   

   /**
   * Liefert den Funktionswert des Polynoms fuer das spezifizierte <i>x</i>.
   *
   * @param x das Argument fuer diese Funktion
   * @return den Funktionswert
   */
   public double f(
      double x
   ) {

      // Definitionsmenge pruefen:
      if ( ! isDefinedFor( x ) ) return Double.NaN;

      // f(x) berechnen:
      double y = 0.0;
      for ( int i = degree(); i >= 2; i-- ) {
         y += _coefficients[i] * Math.pow( x, i );
      } // for
      if ( degree() > 0 ) {
         y += _coefficients[1] * x;
      } // if
      y += _coefficients[0];
      return y;

   } // f


   /**
   * Liefert den Grenzwert dieses Polynoms fuer <i>x</i> gegen die spezifizierte
   * Konstante <i>c</i>. Sinnvolle Argumente sind dabei lediglich
   * <code>NEGATIVE_INFINITY</code> und <code>POSITIVE_INFINITY</code>, der
   * Rueckgabewert ist dann ebenfalls eine dieser Konstanten. Bei allen anderen
   * Argumenten ist das Resultat das gleiche wie bei einem Aufruf der Methode
   * {@link #f(double)}.
   *
   * @param c der Annaeherungsparameter fuer <i>x</i>, fuer den der Grenzwert
   *        gesucht ist
   * @return den Grenzwert dieser Funktion fuer <i>x</i> gegen <i>c</i>
   * @exception IllegalArgumentException falls <i>c</i> keine gueltige Form hat
   */
   public double limit(
      double c
   ) {

      // konstante Funktionen gesondert behandeln:
      if ( degree() == 0 ) return f( c );

      // Definitionsmenge beruecksichtigen:      
      if ( ! isDefinedFor( c ) ) return Double.NaN;

      // limes[-oo]:
      if ( c == Double.NEGATIVE_INFINITY ) {
         int d = degree();
         // ungerader Polynomgrad:
         if ( d % 2 > 0 )
            if ( _coefficients[d] < 0 )
               return Double.POSITIVE_INFINITY;
            else
               return Double.NEGATIVE_INFINITY;
         // gerader Polynomgrad:
         else { // if
            if ( _coefficients[d] < 0 )
               return Double.NEGATIVE_INFINITY;
            else
               return Double.POSITIVE_INFINITY;
         } // else
      } // if

      // limes[+oo]:
      if ( c == Double.POSITIVE_INFINITY ) {
         int d = degree();
         // ungerader Polynomgrad:
         if ( d % 2 > 0 )
            if ( _coefficients[d] < 0 )
               return Double.NEGATIVE_INFINITY;
            else
               return Double.POSITIVE_INFINITY;
         // gerader Polynomgrad:
         else { // if
            if ( _coefficients[d] < 0 )
               return Double.NEGATIVE_INFINITY;
            else
               return Double.POSITIVE_INFINITY;
         } // else
      } // if

      // sonstiger Grenzwert = Funktionswert:
      return f( c );

   } // limit


   /*
   * [javadoc-Beschreibung wird aus RealFunction kopiert]
   */
   public double inverse(
      double y
   ) {

      DynamicArray a = inverseAll( y );
      if ( ( a.size() > 1 ) ||
           ( (Argument) a.first() ).equals( Argument.NOT_DEFINED ) )
         return Double.NaN;
      return ( (Argument) a.first() ).n[0].doubleValue();
      
   } // inverse


   /*
   * [javadoc-Beschreibung wird aus RealFunction kopiert]
   */
   public DynamicArray inverseAll(
      double y
   ) {

      DynamicArray result = null;
      int n = degree(); // Grad dieses Polynoms

      // Sonderfaelle abfangen:
      if ( n < 3 ) {
         if ( n == 0 )
            result = ( new ConstantFunction( _coefficients[0]
                                                           ) ).inverseAll( y );
         else if ( n == 1 )
            result = ( new LinearFunction( _coefficients[1], _coefficients[0]
                                                           ) ).inverseAll( y );
         else
            result = ( new QuadraticFunction( _coefficients[2],
                        _coefficients[1], _coefficients[0] ) ).inverseAll( y );
      } // if

      // Loesungen auf der x-Achse eingrenzen:
      else {
         double est = 0.0; // Betragssumme der Koeffizienten c_n-1..c_0
         for ( int i = 0; i <= n - 1; i++ )
            est += Math.abs( _coefficients[i] );
         /* Die Summe der Terme c_n-1*x^n-1..c_0 des Polynoms kann nun durch
            est*x dem Betrag nach nach oben abgeschaetzt werden. Durch Loesen der
            Ungleichung |c_n|x^n >= est*x^n-1 kann nun |x| ermittelt werden, ab
            dem der Term c_n*x^n dominant wird. Alle Loesungen liegen somit im
            Intervall [-|x|,|x|]. */
         est /= Math.abs( _coefficients[n] ); // umgef.: x^n >= |est/c_n|x^n-1
         // Also ist |x|=|est/c_n|. Intervall nach Loesungen absuchen:
         result = _findSolutions( y, -est, est );
      } // else

      // wenn keine Loesungen existieren, abbrechen:
      if ( ( (Argument) result.first() ).equals( Argument.NOT_DEFINED ) )
         return result;
      // sonst alle Loesungen auf Vorkommen in der Definitionsmenge pruefen:
      DynamicArray a = new DynamicArray( result.size() );
      Enumeration e = result.elements();
      while ( e.hasMoreElements() ) {
         Argument x = (Argument) e.nextElement();
         if ( isDefinedFor( x ) )
            a.add( x );
      } // while
      // Test auf leere Menge:
      if ( a.size() == 0 )
         a.add( Argument.NOT_DEFINED );
      return a;

   } // inverseAll


   /**
   * Berechnet die Schnittmenge mit der spezifizierten Funktion, die ein
   * Polynom sein muss. Falls die Funktion kein Polynom ist, wird eine
   * <code>IllegalArgumentException</code> ausgeloest.<br>
   * Zur Berechnung der Schnittpunkte bei nicht identischen Polynomen wird die
   * Methode {@link #inverseAll(Argument) inverseAll(...)} benutzt:<p>
   * <i>f</i>(<i>x</i>)=<i>g</i>(<i>x</i>) <=>
   * (<i>f</i>-<i>g</i>)(<i>x</i>)=0<p>
   * Es wird also ein neues Polynom <i>f</i>-<i>g</i> erzeugt, dessen
   * <code>inverseAll</code>-Methode mit dem Argument Null aufgerufen wird; das
   * Ergebnis ist die Menge der Schnittpunkte.<br>
   * Wenn die Methode <code>inverseAll</code> nicht ueberschrieben wird, wird
   * beim Aufruf dieser Methode also eine
   * <code>UnsupportedOperationException</code> ausgeworfen.
   *
   * @param g die Schnittfunktion
   * @return die Schnittmenge
   * @exception IllegalArgumentException falls <code>g</code> kein Polynom ist
   * @exception UnsupportedOperationException falls die Methode
   *            <code>inverseAll</code> nicht ueberschrieben wird
   */
   public DynamicArray intersection(
      Function g
   ) {

      // Test auf Polynom:
      Polynomial p = null;
      try {
         p = (Polynomial) g;
      } catch ( ClassCastException cce ) { // try
         throw new IllegalArgumentException( Function.ILLEGAL_ARGUMENT );
      } // catch

      // Schnitt mit hoehergradigen Polynomen weiterreichen:
      if ( p.degree() > this.degree() ) return p.intersection( this );

      // bei Identitaet diese Funktion zurueckliefern:
      if ( p.equals( this ) )
         if ( p._domain.equals( this._domain ) )
            return identityIntersection( this );
         else {
            Polynomial q = (Polynomial) p.clone();
            q._domain = (RealDomain) p._domain.intersection( this._domain );
            return identityIntersection( q );
         } // else

      // fuer den Schnitt ein neues Polynom f-g erzeugen:
      double[] a = new double[this._coefficients.length];
      int i = 0;
      for ( ; i <= p.degree(); i++ )
         a[i] = this._coefficients[i] - p._coefficients[i];
      for ( ; i < a.length; i++ )
         a[i] = this._coefficients[i];
      i = a.length - 1;
      while ( i > 0 ) {
         if ( a[i] != 0 ) break;
         i--;
      } // while
      Polynomial fs = null;
      if ( i == 0 ) fs = new ConstantFunction( a[0] );
      else if ( i == 1 ) fs = new LinearFunction( a[1], a[0] );
      else if ( i == 2 ) fs = new QuadraticFunction( a[2], a[1], a[0] );
      else fs = new Polynomial( a );
      // neues Polynom mit der gemeinsamen Definitionsmenge ausstatten:
      fs._domain = (RealDomain) p.domain().intersection( this.domain() );
      DynamicArray s = fs.inverseAll( 0.0 );
      if ( s.get( 0 ).equals( Argument.NOT_DEFINED ) )
         return new DynamicArray();
      else
         return s;

   } // intersection


   /**
   * Liefert die Menge der Unstetigkeitsstellen dieses Polynoms. Diese ist
   * leer, da ein allgemeines Polynom stetig ist ueber seiner Definitionsmenge.
   *
   * @return ein leeres dynamisches Array
   */
   public DynamicArray getDiscontinuities() {

      return new DynamicArray();

   } // getDiscontinuities


   /*
   * [javadoc-Beschreibung wird aus RealFunction kopiert]
   */
   public RealFunction derivative() {

      Polynomial p = null;
      int n = degree(); // Grad dieses Polynoms

      // Sonderfaelle abfangen:
      if ( n == 0 )
            p = new ConstantFunction();
      else if ( n == 1 )
            p = new ConstantFunction( _coefficients[1] );
      else if ( n == 2 )
            p = new LinearFunction( 2.0 * _coefficients[2], _coefficients[1] );
      else if ( n == 3 )
            p = new QuadraticFunction( 3.0 * _coefficients[3],
                                    2.0 * _coefficients[2], _coefficients[1] );
      // allgemeines Polynom:
      else {
         double[] c = new double[n];
         for ( int i = 1; i <= n; i++ )
            c[i - 1] = (double) i * _coefficients[i];
         p = new Polynomial( c );
      } // else

      // Definitionsmenge uebernehmen:
      p._domain = (RealDomain) _domain.clone();
      return p;

   } // derivative


   /**
   * Liefert den <i>echten</i> Grad des Polynoms, d.h. den groessten
   * Koeffizientenindex, dessen Koeffizient nicht Null ist, oder Null, falls
   * saemtliche Koeffizienten gleich Null sind.
   *
   * @return den Grad des Polynoms
   */
   public int degree() {

      int i = _coefficients.length - 1;
      while ( i > 0 ) {
         if ( _coefficients[i] != 0 ) return i;
         else i--;
      } // while
      return i;

   } // degree


   /**
   * Liefert den Koeffizienten mit dem spezifizierten Index. Falls der Index
   * einen nicht vorhandenen Koeffizienten beschreibt, wird eine
   * <code>IndexOutOfBoundsException</code> ausgeworfen.
   *
   * @param i Index des gewuenschten Koeffizienten
   * @return den Koeffizienten
   * @exception IndexOutOfBoundsException wenn ein ungueltiger Index
   *            spezifiziert wurde
   */
   public double getCoefficient(
      int i
   ) {

      try {
         return _coefficients[i];
      } catch ( ArrayIndexOutOfBoundsException aioobe ) { // try
         throw new IndexOutOfBoundsException( INVALID_INDEX + i );
      } // catch

   } // getCoefficient


   /**
   * Setzt den Koeffizienten mit dem spezifizierten Index. Falls der Index
   * einen nicht vorhandenen Koeffizienten beschreibt, wird eine
   * <code>IndexOutOfBoundsException</code> ausgeworfen.
   *
   * param i Index des gewuenschten Koeffizienten
   * param value der Wert des Koeffizienten
   * @exception IndexOutOfBoundsException wenn ein ungueltiger Index
   *            spezifiziert wurde
   */
   public void setCoefficient(
      int i,
      double value
   ) {

      try {
         _coefficients[i] = value;
      } catch ( ArrayIndexOutOfBoundsException aioobe ) { // try
         throw new IndexOutOfBoundsException( INVALID_INDEX + i );
      } // catch

   } // setCoefficient


   /**
   * Schraenkt den Definitionsbereich dieses Polynoms auf das spezifizierte
   * Intervall ein. Falls das Intervall nicht eindimensional ist oder seine
   * Grenzen keine reellen Zahlen sind, wird eine
   * <code>IllegalArgumentException</code> ausgeworfen.
   *
   * @param interval das Intervall
   * @exception IllegalArgumentException falls das Intervall nicht
   *            eindimensional reell ist
   */
   public void setDomain(
      Interval interval
   ) {

      _domain = new RealDomain( interval );

   } // setDomain


   // *************************************************************************
   // Private methods
   // *************************************************************************
   
   /**
    * Berechnet Nullstellen fuer Funktionen bis vierten Grades
    * durch pq, <i>Cardanische Formel</i> bzw nach <i>Ferrari</i> 
    */
   protected void _calculateRoots()
   {
	   switch (degree())
	   {
	   		case 1: //Berechnung Nullstellen 1. Grades
	   			_roots = new TreeSet();
	   			double x1 = -_coefficients[0]/_coefficients[1];
	   			_roots.add(new Double(x1));
	   			
	   			break;
	   			
	   		case 2: //Berechnung Nullstellen 2. Grades
	   			_roots = new TreeSet();
	   			double p = _coefficients[1]/_coefficients[2];
	   			double q = _coefficients[0]/_coefficients[2];
	   			x1 = -p/2 + Math.sqrt(p*p/4-q);
	   			if (!Double.isNaN(x1))
   					_roots.add(new Double(x1));
	   			x1 = -p/2 - Math.sqrt(p*p/4-q);
	   			if (!Double.isNaN(x1))
   					_roots.add(new Double(x1));
	   			
	   			break;
	   			
	   		case 3: //Berechnung Nullstellen 3. Grades
	   			
	   			_roots = new TreeSet();
	   			
	   			//Koeffizienten berechnen
	   			double a = _coefficients[2] / _coefficients[3];
	   			double b = _coefficients[1] / _coefficients[3];
	   			double c = _coefficients[0] / _coefficients[3];
	   			
	   			//Reduzierung auf Kubische Gleichung => p und q berechnen
	   			p = b - (a*a) / 3.0;
	   			q = c + (2*(a*a*a) / 27.0) - (a*b / 3); 
	   			
	   			//Diskriminante berechnen
	   			double D = (q/2)*(q/2) + (p/3)*(p/3)*(p/3);
	   			
	   			//System.out.println("D: "+D);
	   			
	   			//D>0 => eine reelle Nullstelle
	   			if (D>0)
	   			{
	   				double x = cubeRoot(-(q/2.0) + Math.sqrt(D)) + cubeRoot(-(q/2.0) - Math.sqrt(D)) - a/3.0;
	   				_roots.add(new Double(x));
	   			}
	   			
	   			//D=0 => zwei reelle Nullstellen
	   			else if (D==0)
	   			{
	   				double x = (cubeRoot(q/2.0)) - a/3.0;
	   				_roots.add(new Double(x));
	   				x = (-cubeRoot(4*q)) - a/3.0;
	   				_roots.add(new Double(x));
	   			}
	   			
	   			//D<0 => drei reelle Nullstellen (Casus irreducibilis)
	   			else if (D<0)
	   			{
	   				double cosphi,phi;       
	   			    double absp=Math.abs(p);

	   			    cosphi=-(q/(double)2)/Math.sqrt(absp*absp*absp/27.0);

	   			    //Numerischer Fehler kann wegen Double auftreten (zu wenig Nachkommastellen)
	   			    //aber arccos nur fuer Zahlen zwischen -1 und 1 definiert => cast auf short
	   			    if(!((cosphi>-1)&&(cosphi<1)))
	   			        cosphi=(short)cosphi;
	   			    
	   			    phi=Math.acos(cosphi);

	   			    double x = (2*Math.sqrt(absp/3.0)*Math.cos(phi/3.0))-a/3.0;
	   			    _roots.add(new Double(x));
	   			    x = (-2*Math.sqrt(absp/3.0)*Math.cos(phi/3.0-Math.PI/3.0))-a/3.0;        //60 Grad = 1/3.0*PI radiant
	   			    _roots.add(new Double(x));
	   			    x = (-2*Math.sqrt(absp/3.0)*Math.cos(phi/3.0+Math.PI/3.0))-a/3.0;
	   			    _roots.add(new Double(x));
	   			    
  			    }
	   			
	   			break;
	   		
	   			
	   		case 4: //Berechnung Nullstellen 4. Grades
	   			
	   			_roots = new TreeSet();
	   			
	   			//Koeffizienten holen
	   			a = _coefficients[4];
	   			b = _coefficients[3];
	   			c = _coefficients[2];
	   			double d = _coefficients[1];
	   			double e = _coefficients[0];
	   			
	   			//Reduzierung auf z^3 + p*z + q = 0
	   			p = (3*b*d - c*c)/(12*a*a) - e/a;
	   			q = (8*c*e - 3*d*d)/(24*a*a) - (27*b*b*e - 9*b*c*d + 2*c*c*c)/(216*a*a*a);
	   			Polynomial fz = new Polynomial(new double[] {q,p,0,1});
	   			
	   			//z berechnen
	   			//Es kann beliebige Nullstelle genommen werden; immer erste fuer uns
	   			double z = fz.getRoots()[0];
	   			
	   			double y = z + c/(6*a);
	   			D = (b * y - d)/a;
	   			
	   			//System.out.println("D: "+D);
	   			
	   			//1. Fall
	   			if (D>0)
	   			{
	   				double x = -b/(4*a) - 0.5*Math.sqrt(2*y+b*b/(4*a*a) - c/a) + Math.sqrt(b*b/(8*a*a) - 0.5*y - c/(4*a) + (b/(4*a)*Math.sqrt(2*y + b*b/(4*a*a) - c/a) - Math.sqrt(y*y - e/a)));
	   				if (!Double.isNaN(x))
	   					_roots.add(new Double(x));
	   				
	   				x = -b/(4*a) - 0.5*Math.sqrt(2*y+b*b/(4*a*a) - c/a) - Math.sqrt(b*b/(8*a*a) - 0.5*y - c/(4*a) + (b/(4*a)*Math.sqrt(2*y + b*b/(4*a*a) - c/a) - Math.sqrt(y*y - e/a)));
	   				if (!Double.isNaN(x))
	   					_roots.add(new Double(x));
	   				
	   				x = -b/(4*a) + 0.5*Math.sqrt(2*y+b*b/(4*a*a) - c/a) + Math.sqrt(b*b/(8*a*a) - 0.5*y - c/(4*a) - (b/(4*a)*Math.sqrt(2*y + b*b/(4*a*a) - c/a) - Math.sqrt(y*y - e/a)));
	   				if (!Double.isNaN(x))
	   					_roots.add(new Double(x));
	   				
	   				x = -b/(4*a) + 0.5*Math.sqrt(2*y+b*b/(4*a*a) - c/a) - Math.sqrt(b*b/(8*a*a) - 0.5*y - c/(4*a) - (b/(4*a)*Math.sqrt(2*y + b*b/(4*a*a) - c/a) - Math.sqrt(y*y - e/a)));
	   				if (!Double.isNaN(x))
	   					_roots.add(new Double(x));
	   				
	   			}
	   			
	   			//2. Fall
	   			else if (D<0)
	   			{
	   				
	   				double x = -b/(4*a) - 0.5*Math.sqrt(2*y+b*b/(4*a*a) - c/a) + Math.sqrt(b*b/(8*a*a) - 0.5*y - c/(4*a) + (b/(4*a)*Math.sqrt(2*y + b*b/(4*a*a) - c/a) + Math.sqrt(y*y - e/a)));
	   				if (!Double.isNaN(x))
	   					_roots.add(new Double(x));
	   				
	   				x = -b/(4*a) - 0.5*Math.sqrt(2*y+b*b/(4*a*a) - c/a) - Math.sqrt(b*b/(8*a*a) - 0.5*y - c/(4*a) + (b/(4*a)*Math.sqrt(2*y + b*b/(4*a*a) - c/a) + Math.sqrt(y*y - e/a)));
	   				if (!Double.isNaN(x))
	   					_roots.add(new Double(x));
	   				
	   				x = -b/(4*a) + 0.5*Math.sqrt(2*y+b*b/(4*a*a) - c/a) + Math.sqrt(b*b/(8*a*a) - 0.5*y - c/(4*a) - (b/(4*a)*Math.sqrt(2*y + b*b/(4*a*a) - c/a) + Math.sqrt(y*y - e/a)));
	   				if (!Double.isNaN(x))
	   					_roots.add(new Double(x));
	   				
	   				x = -b/(4*a) + 0.5*Math.sqrt(2*y+b*b/(4*a*a) - c/a) - Math.sqrt(b*b/(8*a*a) - 0.5*y - c/(4*a) - (b/(4*a)*Math.sqrt(2*y + b*b/(4*a*a) - c/a) + Math.sqrt(y*y - e/a)));
	   				if (!Double.isNaN(x))
	   					_roots.add(new Double(x));
	   			}
	   			
	   			break;
	   		
	   			
	   		default:
	   			throw new IllegalArgumentException("Degree too high: " + degree());
	   			//System.out.println("Keine Berechnung");
	   		
	   }
	   _rootsCalculated = true;
   } //_calculateRoots
   

   /**
   * Mehrfache Anwendung des <i>Newton-Verfahrens</i>, um in einem Intervall
   * alle Loesungen des Polynoms fuer das spezifizierte y zu finden. Dazu werden
   * die Extrempunkte des Polynoms benutzt, um zwischen Ihnen und mit den
   * aeusseren Grenzen Intervalle zu bilden, fuer die das Newton-Verfahren
   * anschliessend angewendet wird.
   *
   * @param y der Funktionswert, fuer den die Loesungen dieses Polynoms gesucht
   *        sind
   * @param left die linke Grenze des untersuchten Intervalls
   * @param right die rechte Grenze des untersuchten Intervalls
   * @return die Loesungen in einem dynamischen Array
   */
   protected DynamicArray _findSolutions(
      double y,
      double left,
      double right
   ) {

      DynamicArray solutions = new DynamicArray( this.degree() );
      Domain original = _domain;  // Definitionsmenge sichern
      _domain = RealDomain.R; // fuer Newton muss D = R sein!

      // Extrempunkte ex1,ex2,...:
      DynamicArray extrema = this.extrema();
      // keine Extrempunkte, dann gibt es nur eine Loesung:
      if ( extrema.isEmpty() ) {
         solutions.add( new Argument( new Double( _newton( y, left ) ) ) );
         return solutions;
      } // if

      // Intervall [-oo,ex1] betrachten:
      FunctionPoint ex = (FunctionPoint) extrema.first();
      double fx = ex.y.n[0].doubleValue();
      if ( ( ( ex instanceof Minimum ) && ( y >= fx ) ) ||
           ( ( ex instanceof Maximum ) && ( y <= fx ) ) )
         solutions.add( new Argument( new Double( _newton( y, left ) ) ) );

      // Intervalle [ex_i,ex_i+1] betrachten:
      double x, x1, x2;
      double y1, y2;
      for ( int i = 1; i < extrema.size(); i++ ) {
         ex = (FunctionPoint) extrema.get( i - 1 );
         x1 = ex.x.n[0].doubleValue();
         y1 = ex.y.n[0].doubleValue();
         ex = (FunctionPoint) extrema.get( i );
         x2 = ex.x.n[0].doubleValue();
         y2 = ex.y.n[0].doubleValue();
         if ( y1 < y2 ) {
            if ( ( y1 < y ) && ( y <= y2 ) ) {
               x = x1 + ( x2 - x1 ) / 2.0;
               solutions.add( new Argument( new Double( _newton( y, x ) ) ) );
            } // if
         } else { // if
            if ( ( y1 > y ) && ( y >= y2 ) ) {
               x = x1 + ( x2 - x1 ) / 2.0;
               solutions.add( new Argument( new Double( _newton( y, x ) ) ) );
            } // if
         } // else
      } // for

      // Intervall [ex_n,+oo] betrachten:
      ex = (FunctionPoint) extrema.last();
      fx = ex.y.n[0].doubleValue();
      if ( ( ( ex instanceof Minimum ) && ( y > fx ) ) ||
           ( ( ex instanceof Maximum ) && ( y < fx ) ) )
         solutions.add( new Argument( new Double( _newton( y, right ) ) ) );

      _domain = original;
      return solutions;

   } // _findSolutions


   /**
   * Sucht eine Loesung x dieses Polynoms fuer den Funktionswert y nach dem
   * <i>Newton-Verfahren</i>.
   *
   * @param y der Funktionswert, fuer den eine Loesung gesucht wird
   * @param start der Startwert der Iteration fuer x
   * @return die Loesung x
   */
   protected double _newton(
      double y,
      double start
   ) {

      double x;  // Naeherungswert fuer die Loesung initialisieren
      double fx; // Funktionswert fuer aktuelle Naeherung
      double m;  // Steigung der Tangente am Polynom in x
      double b;  // y-Achsenabschnitt der Tangente durch x
      double dy = 0.0; // absoluter Fehler |f(x)-y|
      double epsilon = 1.0e-12; // Epsilon-Umgebung fuer erlaubten Fehler
      Polynomial der = (Polynomial) this.derivative(); // Ableitung

      // Iteration, um x weiter anzunaehern:
      x = start;
      do {
         // aktuelles x ueberpruefen:
         fx = this.f( x );          // fx = f(x)
         if ( Double.isNaN( fx ) ) break; // Sicherheitsabfrage
         dy = Math.abs( fx - y );   // dy = |fx-y|
         if ( dy < epsilon ) break; // bei genuegend kleinem Fehler abbrechen
         // x weiter annaehern:
         m = der.f( x );    // m = f'(x)
         b = fx - m * x;    // b = fx-m*x  <=> fx = mx + b
         x = ( y - b ) / m; // x = (y-b)/m <=>  y = mx + b
      } while ( true );
      return x;

   } // _newton
   
   
   /**
    * Gibt reelle dritte Wurzel fuer Eingabe zurueck
    * 
    * @param input term aus der wurzel berechnet wird
    * @return die dritte Wurzel
    */
   private double cubeRoot(double input)
   {
	   double root = input / 3.0;
	   double y0 = 0;
	   while (Math.abs(y0 - root) > 1.0E-8)
	   {
		   y0 = root;
		   root = (2 * y0 + input / (y0 * y0)) / 3;
	   }
	   return root;
   } //cubeRoot


} // Polynomial
