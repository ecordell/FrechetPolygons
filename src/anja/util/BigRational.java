

package anja.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
* <p align="justify">
* <b> BigRational </b> sind konstante rationale Zahlen mit beliebiger 
* Genauigkeit. Konstant bedeutet in diesem Zusammenhang, dass sich der 
* Wert einer einmal erzeugten BigRational nicht mehr aendern laesst.
* Zaehler und Nenner sind vom Typ BigInteger, an welcher Klasse sich die 
* Implementierung der BigRational orientiert.
*
* <p align="justify">
* Die Methode <tt> valueOf() </tt> erzeugt eine BigRational aus einer
* double. Dieses wird einem entsprechendem Konstruktor vorgezogen, um
* haeufig verwendete Konstanten wie 0 und 1 wiederverwenden zu koennen,
* ohne sie exportieren zu muessen.
*
* <p align="justify">
* Vergleichsoperationen bezueglich 0 lassen sich effizient mit <tt> signum()
* </tt> realisieren.
*
* <p> Erzeugung:
* <ul type=circle>
*    <li> BigRational( String )	</li>
*    <li> valueOf( double )	</li>
* </ul>
*
* Rechenoperationen:
* <ul type=circle>
*    <li> add			</li>
*    <li> subtract		</li>
*    <li> multiply		</li>
*    <li> divide		</li>
*    <li> square		</li>
*    <li> pow			</li>
*    <li> sqrt			</li>
* </ul>
*
* Vorzeichenoperationen:
* <ul type=circle>
*    <li> abs			</li>
*    <li> negate		</li>
*    <li> signum		</li>
* </ul>
*
* Vergleichsoperationen:
* <ul type=circle>
*    <li> equals		</li>
*    <li> compareTo		</li>
*    <li> max			</li>
*    <li> min			</li>
* </ul>
*
* Konvertierungsfunktionen:
* <ul type=circle>
*    <li> intValue		</li>
*    <li> longValue		</li>
*    <li> floatValue		</li>
*    <li> doubleValue		</li>
*    <li> toString		</li>
* </ul>
*
* </p>
*
* @version	1.3  16.09.1997
* @author	Norbert Selle
* @see		java.math.BigInteger
*/

public class BigRational extends Number 
{

   // ************************************************************************
   //
   // Bemerkungen zur Implementierung:
   // - Der Nenner ist immer positiv, d.h. wenn er im Laufe einer
   //   Rechenoperation ein negatives Vorzeichen erhaelt, so wird
   //   es in den Zaehler hochgezogen
   // - Rueckgabewerte vom Typ BigRational sind stets gekuerzt
   //
   // ************************************************************************


   // ************************************************************************
   // Constants
   // ************************************************************************


   /* Flagge fuer den Konstruktor: Kuerzen ist notwendig		*/
   private static boolean	_REDUCE		= true;

   /* Flagge fuer den Konstruktor: Kuerzen ist nicht notwendig	*/
   private static boolean	_DONT_REDUCE	= false;

   /* Diverse BigRational 					*/

   private static BigRational	_ZERO	= new BigRational(
					     BigInteger.valueOf(  0 ),
					     BigInteger.valueOf(  1 ),
					     _DONT_REDUCE );

   private static BigRational	_ONE	= new BigRational(
					     BigInteger.valueOf(  1 ),
					     BigInteger.valueOf(  1 ),
					     _DONT_REDUCE );

   private static BigRational	_TEN	= new BigRational(
					     BigInteger.valueOf( 10 ),
					     BigInteger.valueOf(  1 ),
					     _DONT_REDUCE );

   /* Diverse BigInteger 					*/

   private static BigInteger 	_INT_ONE = BigInteger.valueOf(  1 );

   private static BigInteger 	_INT_TEN = BigInteger.valueOf( 10 );


   // ************************************************************************
   // Variables
   // ************************************************************************


   /* der Zaehler			*/
   private BigInteger	_numerator;

   /* der Nenner			*/
   private BigInteger	_denominator;


   // ************************************************************************
   // Constructors
   // ************************************************************************


   /**
   * Erzeugt eine BigRational aus einem String mit zwei durch einen Slash
   * getrennten BigInteger. Um den Slash herum sind whitespaces erlaubt.
   * Bei Formatfehlern erfolgt eine <tt> NumberFormatException</tt>,
   * bei einem Nenner von 0 eine <tt> ArithmeticException</tt>.
   */

   public BigRational(
      String	input_str
   )
   throws
      ArithmeticException,
      NumberFormatException
   {
      // String in Zaehler und Nenner zerlegen

      int	slash_index	= input_str.indexOf( '/' );

      if ( slash_index < 0 )
         throw new NumberFormatException( "missing slash" );

      String	num_str	  = input_str.substring( 0, slash_index  );
      String	denom_str = input_str.substring( slash_index + 1 );

      try
      {
	 _numerator   = new BigInteger( num_str.trim() );
	 _denominator = new BigInteger( denom_str.trim() );
      }
      catch ( NumberFormatException exception )
      {
         throw exception;
      } // try

      if ( _denominator.signum() == -1 )
      {
	 // Negatives Vorzeichen des Nenners in den Zaehler hochziehen

	 _numerator   = _numerator.negate();
	 _denominator = _denominator.negate();
      }
      else if ( _denominator.signum() == 0 )
      {
         // Fehlerfall Nenner == 0

         throw new ArithmeticException( "denominator == 0" );
      } // if

      _reduce();

   } // BigRational


   // ********************************


   /*
   * Erzeugt eine BigRational aus dem Zaehler und dem Nenner und kuerzt
   * sie, falls <tt> input_reduce </tt> gleich <tt> _REDUCE </tt> ist.
   * Ein negatives Vorzeichen des Nenners wird in den Zaehler hochgezogen.
   * <br>
   * Bei einem Nenner von 0 erfolgt eine <tt> ArithmeticException</tt>.
   *
   * @param input_numerator	der Zaehler
   * @param input_denominator	der Nenner
   * @param input_reduce	_REDUCE oder _DONT_REDUCE
   *
   * @see   _REDUCE
   * @see   _DONT_REDUCE
   */

   private BigRational(
      BigInteger	input_numerator,
      BigInteger	input_denominator,
      boolean		input_reduce
   )
   {
      switch ( input_denominator.signum() )
      {
      case -1:
	 _numerator   = input_numerator.negate();
	 _denominator = input_denominator.negate();
         break;
      case  0:
         throw new ArithmeticException( "denominator == 0" );
      case  1:
	 _numerator   = input_numerator;
	 _denominator = input_denominator;
         break;
      default:
         break;
      } // case

      if ( input_reduce == _REDUCE )
         _reduce();

   } // BigRational


   // ************************************************************************
   // Public class methods
   // ************************************************************************


   /**
   * Gibt eine BigRational mit dem Eingabewert zurueck.
   */

   public static BigRational valueOf(
      double	input_val
   )
   {
      if (      input_val ==  0 )
         return _ZERO;
      else if ( input_val ==  1 )
         return _ONE;
      else if ( input_val == 10 )
         return _TEN;

      BigDecimal	decimal_val	= new BigDecimal( input_val );
      int		scale		= decimal_val.scale();
      BigInteger	numerator	= null;
      BigInteger	denominator	= null;

      numerator	= decimal_val.movePointRight( scale ).toBigInteger();

      if ( scale == 0 )
         denominator = BigInteger.valueOf( 1 );
      else
         denominator = BigInteger.valueOf( 10 ).pow( scale );

      return ( new BigRational( numerator, denominator, _REDUCE ) );

   } // valueOf


   // ************************************************************************
   // Private class methods
   // ************************************************************************


   /*
   * Berechnet die Quadratwurzel einer BigInteger. Nachkommastellen entfallen.
   * Erzeugt eine ArithmeticException bei einer negativen Zahl.
   */

   private static BigInteger _sqrt(
      BigInteger	value
   )
   throws
      ArithmeticException
   {
      // Der Algorithmus stammt im Original aus der folgenden Quelle und
      // wurde auf unseren Zweck angepasst.
      //
      //    Subject:      Re: Square Root of BigDecimal
      //    From:         hughesm@cs.unc.edu (Merlin Hughes)
      //    Date:         1997/06/25
      //    Message-Id:   <5orbq2$bpr@hopper.cs.unc.edu>
      //    Newsgroups:   comp.lang.java.programmer
      //
      // (c) 1997 merlin & conrad hughes, use at your leisure

      if ( value.signum() < 0)
	 throw new ArithmeticException( "square root of negative number");

      if ( value.signum() == 0 )
	 return ( value );

      int		bits	= ( value.bitLength() - 1 ) >> 1;
      BigInteger	root	= _INT_ONE.shiftLeft( bits);

      value = value.subtract( root.shiftLeft( bits ) );

      while ( bits-- > 0 )
      {
	 BigInteger	tmp	=
	    value.subtract( _INT_ONE.shiftLeft( bits + bits )
			  ).subtract( root.shiftLeft( bits + 1 ) );

	 if ( tmp.signum() >= 0 )
	 {
	    root  = root.add( _INT_ONE.shiftLeft( bits ) );
	    value = tmp;
	 } // if
      } // while

      return ( root );

   } // _sqrt


   // ************************************************************************
   // Public methods
   // ************************************************************************


   /**
   * Addiert die BigRational und den Eingabewert.
   *
   * @return die Summe
   */

   public BigRational add(
      BigRational	input_val
   )
   {
      BigInteger	num1  = _numerator.multiply( input_val._denominator );
      BigInteger	num2  = input_val._numerator.multiply( _denominator );

      return ( new BigRational( num1.add( num2 ),
				_commonDenominator( this, input_val ),
				_REDUCE )
             );

   } // add


   // ************************************************************************


   /**
   * Subtrahiert den Eingabewert von der BigRational.
   *
   * @return die Differenz
   */

   public BigRational subtract(
      BigRational	input_val
   )
   {
      BigInteger	num1  = _numerator.multiply( input_val._denominator );
      BigInteger	num2  = input_val._numerator.multiply( _denominator );

      return ( new BigRational( num1.subtract( num2 ),
				_commonDenominator( this, input_val ),
				_REDUCE )
             );

   } // subtract


   // ************************************************************************


   /**
   * Multipliziert die BigRational mit dem Eingabewert.
   *
   * @return das Produkt
   */

   public BigRational multiply(
      BigRational	input_val
   )
   {
      return ( 
         new BigRational( _numerator.multiply(   input_val._numerator   ),
			  _denominator.multiply( input_val._denominator ),
			  _REDUCE ) 
      );

   } // multiply


   // ************************************************************************


   /**
   * Dividiert die BigRational durch den Eingabewert. Erzeugt eine
   * <tt> ArithmeticException </tt> bei Division durch 0.
   *
   * @return den Quotienten
   */

   public BigRational divide(
      BigRational	input_val
   )
   throws
      ArithmeticException
   {
      if ( input_val._numerator.signum() == 0 )
      {
         throw new ArithmeticException( "BigRational divide by zero" );
      } // if

      if ( _isZero() || input_val._isZero() )
         return ( _ZERO );

      return ( 
         new BigRational( _numerator.multiply(   input_val._denominator ),
			  _denominator.multiply( input_val._numerator   ),
			  _REDUCE ) 
      );

   } // divide


   // ************************************************************************


   /**
   * Quadriert die Zahl.
   */

   public BigRational square()
   {
      return ( pow( 2 ) );

   } // square


   // ************************************************************************


   /**
   * Berechnet die Potenz. Der Exponent muss eine natuerliche Zahl groesser gleich
   * null sein, sonst wird eine <tt> ArithmeticException </tt> erzeugt.
   *
   * @return die Potenz
   */

   public BigRational pow(
      int	input_exponent
   )
   throws
      ArithmeticException
   {
      if ( input_exponent < 0 )
      {
         throw new ArithmeticException( "Negative exponent" );
      } // if

      try
      {
	 return ( new BigRational( _numerator.pow(   input_exponent ),
				   _denominator.pow( input_exponent ),
				   _DONT_REDUCE )
		);
      }
      catch ( ArithmeticException exception )
      {
         throw exception;
      } // try

   } // pow


   // ************************************************************************


   /**
   * Berechnet die Quadratwurzel. Die Genauigkeit entspricht der Anzahl zu
   * berechnender Nachkommastellen, dazu werden Zaehler und Nenner vor der
   * Berechnung geeignet erweitert.
   * Erzeugt eine ArithmeticException bei einer negativen Zahl oder bei einer
   * negativen Genauigkeit.
   *
   * @param input_scale		die Genauigkeit
   *
   * @return die Quadratwurzel
   */

   public BigRational sqrt(
      int	input_scale
   )
   throws 
      ArithmeticException
   {
      if ( input_scale < 0 )
         throw new ArithmeticException( "Negative scale" );

      if ( signum() == 0 )
         return ( this );

      // Zaehler und Nenner erweitern, damit nach dem Wurzelziehen die 
      // gewuenschte Genauigkeit vorliegt

      BigInteger   factor   = _INT_TEN.pow( input_scale );
      BigInteger   a        = _numerator.multiply(   factor );
      BigInteger   b        = _denominator.multiply( factor );

      // Um den Nenner erweitern, damit nur eine Wurzel zu berechnen ist:
      //
      //       a           a * b     sqrt( a * b)   z
      // sqrt( - ) = sqrt( ----- ) = ------------ = -
      //       b           b * b          b         b

      BigInteger   z        = BigRational._sqrt( a.multiply( b ) );

      return ( new BigRational( z, b, _REDUCE ) );

   } // sqrt


   // ************************************************************************


   /**
   * Gibt den Absolutwert zurueck.
   */

   public BigRational abs()
   {
      if ( _numerator.signum() < 0 )
         return ( negate() );
      else
         return ( this );

   } // abs


   // ************************************************************************


   /**
   * Gibt den negierten Wert zurueck.
   */

   public BigRational negate()
   {
      return ( new BigRational( _numerator.negate(),
				_denominator,
				_DONT_REDUCE ) 
	     );

   } // negate


   // ************************************************************************


   /**
   * Gibt den Wert der Signum-Funktion fuer die BigRational zurueck, dass heisst
   * -1, 0 oder 1 wenn sie negativ, null oder positiv ist.
   */

   public int signum()
   {
      return ( _numerator.signum() );

   } // signum


   // ************************************************************************


   /**
   * Gibt true zurueck bei Gleichheit, sonst false.
   */

   public boolean equals(
      Object		input_val
   )
   {
      if ( ! ( input_val instanceof BigRational ) )
         return ( false );

      BigRational	big_value	= ( BigRational ) input_val;

      if ( this == big_value )
         return ( true );

      return (    _numerator.equals(   big_value._numerator   )
               && _denominator.equals( big_value._denominator )
	     );

   } // equals


   // ************************************************************************


   /**
   * Gibt -1, 0 oder 1 zurueck wenn die BigRational kleiner, gleich oder
   * groesser als der Eingabewert ist. Der Aufruf mit einer der sechs
   * Vergleichsoperationen an der Stelle von OP ist so gedacht:
   * <pre>
   *    ( x.compareTo( y ) OP 0 ) 
   * </pre>
   */

   public int compareTo(
      BigRational	input_val
   )
   {
      if ( signum() < input_val.signum() )
         return ( -1 );
      if ( signum() > input_val.signum() )
         return (  1 );

      if ( signum() == 0 )
         return ( -input_val.signum() );

      if ( input_val.signum() == 0 )
         return ( signum() );

      if ( this == input_val )
         return ( 0 );

      return ( _numerator.multiply( input_val._denominator ).compareTo(
                  input_val._numerator.multiply( _denominator ) )
	     );

   } // compareTo


   // ************************************************************************


   /**
   * Gibt das Maximum der beiden Werte zurueck.
   */

   public BigRational max(
      BigRational	input_val
   )
   {
      if ( compareTo( input_val ) > 0 )
         return ( this );
      else
         return ( input_val );

   } // max


   // ************************************************************************


   /**
   * Gibt das Minimum der beiden Werte zurueck.
   */

   public BigRational min(
      BigRational	input_val
   )
   {
      if ( compareTo( input_val ) < 0 )
         return ( this );
      else
         return ( input_val );

   } // min


   // ************************************************************************


   /**
   * Konvertierung nach int.
   */

   public int intValue()
   {
      return ( ( int ) longValue() );

   } // intValue


   // ************************************************************************


   /**
   * Konvertierung nach long.
   */

   public long longValue()
   {
      if ( signum() == 0 )
         return ( 0 );

      return ( _numerator.divide( _denominator ).longValue() );

   } // longValue


   // ************************************************************************


   /**
   * Konvertierung nach float.
   */

   public float floatValue()
   {
      return ( ( float ) doubleValue() );

   } // floatValue


   // ************************************************************************


   /**
   * Konvertierung nach double.
   */

   public double doubleValue()
   {
      if ( signum() == 0 )
         return ( 0 );

      return ( _numerator.doubleValue() / _denominator.doubleValue() );

   } // doubleValue


   // ************************************************************************


   /**
   * Konvertierung nach String.
   */

   public String toString()
   {
      return (   _numerator.toString()
      	       + " / "
      	       + _denominator.toString()
	     );

   } // toString


   // ************************************************************************
   // Private class methods
   // ************************************************************************


   // ************************************************************************
   // Private methods
   // ************************************************************************


   /*
   * Berechnet den Hauptnenner.
   *
   * @return den Hauptnenner
   */

   private BigInteger _commonDenominator(
      BigRational	input_val1,
      BigRational	input_val2
   )
   {
      return ( input_val1._denominator.multiply( input_val2._denominator ) );

   } // _commonDenominator


   // ************************************************************************


   /*
   * Test auf Gleichheit mit 0.
   */

   private boolean _isZero()
   {
      return ( _numerator.signum() == 0 );

   } // _isZero


   // ************************************************************************


   /*
   * Kuerzt die BigRational. Wird ausschliesslich von den Konstruktoren
   * verwendet.
   */

   private void _reduce()
   {
      BigInteger	gcd = _numerator.gcd( _denominator );

      if ( ! gcd.equals( BigInteger.valueOf( 1 ) ) )
      {
	 _numerator   = _numerator.divide(   gcd );
	 _denominator = _denominator.divide( gcd );
      } // if

   } // _reduce


   // ************************************************************************


} // class BigRational

