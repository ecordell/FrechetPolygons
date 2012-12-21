
package anja.util;

import java.lang.Math;


/**
* Die Klasse stellt einen ungerichtetet Winkel zur Verfuegung, dessen Wert in 
* Grad oder Bogenmass gesetzt und gelesen werden kann. Der Sinus, Cosinus und 
* Tangens der Winkels werden beim Erzeugen und Setzen berechnet und 
* gespeichert, so dass effizient mehrfach auf sie zugegriffen werden kann. <br>
* Es werden Konstanten fuer Pi halbe, zweimal Pi und Pi bereitgestellt. <br>
* Die Klassen-Methoden degToRad() und radToDeg() dienen zur Umrechnung von
* Grad nach Bogenmass und umgekehrt. <br>
*
* @version	1.1 04.09.1997
* @author	Norbert Selle
*/

public class Angle
{

   // ************************************************************************
   // Public constants
   // ************************************************************************


   /** Orientierung mathematisch, entgegen des Uhrzeigersinns	*/
   public final static int	ORIENTATION_LEFT	= 791;

   /** Orientierung im Uhrzeigersinn				*/
   public final static int	ORIENTATION_RIGHT	= 792;


   /** die Konstante PI 			*/
   public final static double		PI	 	= Math.PI;

   /** PI halbe 				*/
   public final static double		PI_DIV_2	= Math.PI / 2;

   /** zwei mal PI				*/
   public final static double		PI_MUL_2	= Math.PI * 2;


   /** Einheit Grad (Degree)			*/
   public final static int		DEG		= 5386;

   /** Einheit Bogenmass (Radiant)		*/
   public final static int		RAD		= 5387;


   // ************************************************************************
   // Private constants
   // ************************************************************************


   /* Faktor zur Umwandlung Grad nach Bogenmass	*/
   private final static double		_DEG_TO_RAD	= PI_MUL_2 / 360d;

   /* Faktor zur Umwandlung Bogenmass anch Grad	*/
   private final static double		_RAD_TO_DEG	= 360d / PI_MUL_2;


   // ************************************************************************
   // Variables
   // ************************************************************************


   /* der Winkel in Grad (Degree)			*/
   private double		_deg;

   /* der Winkel in Bogenmass (Radiant)			*/
   private double		_rad;

   /* der Sinus des Winkels				*/
   private double		_sin;

   /* der Cosinus des Winkels				*/
   private double		_cos;

   /* der Tangens des Winkels				*/
   private double		_tan;


   // ************************************************************************
   // Constructors
   // ************************************************************************


   /**
   * Erzeugt einen 0-Grad Winkel.
   */

   public Angle()
   {
      _deg 	= 0;
      _rad 	= 0;
      _sin 	= 0;
      _cos 	= 1;
      _tan 	= 0;

   } // Angle


   // ********************************              


   /**
   * Erzeugt eine Kopie des Eingabewinkels.
   */

   public Angle(
      Angle	input_angle
   )
   {
      _deg 	= input_angle._deg;
      _rad 	= input_angle._rad;
      _sin 	= input_angle._sin;
      _cos 	= input_angle._cos;
      _tan 	= input_angle._tan;

   } // Angle


   // ********************************              


   /**
   * Erzeugt einen Winkel mit dem Eingabewert in Bogenmass.
   *
   * @param input_rad		der Winkel in Bogenmass
   */

   public Angle(
      double	input_rad
   )
   {
      _alterRad( input_rad );

   } // Angle


   // ********************************              


   /**
   * Erzeugt einen Winkel mit dem Eingabewert in der Eingabeeinheit.
   * Loest eine IllegalArgumentException aus, wenn die Einheit weder
   * DEG noch RAD ist.
   *
   * @param input_value		der Eingabewert
   * @param input_unit		die Einheit DEG oder RAD
   *
   * @see #DEG
   * @see #RAD
   */

   public Angle(
      double	input_value,
      int	input_unit
   )
   {
      if ( input_unit == DEG )
      {
	 _alterDeg( input_value );
      }
      else if ( input_unit == RAD )
      {
	 _alterRad( input_value );
      }
      else
      {
         throw new IllegalArgumentException( "illegal unit" );
      } // if

   } // Angle


   // ************************************************************************
   // Class methods
   // ************************************************************************


   /**
   * Berechnet den Winkel vom Startwinkel zum Endwinkel in Richtung der
   * Orientierung, das Ergebnis liegt bei ORIENTATION_LEFT im Intervall 
   * [0, 2*Pi], bei ORIENTATION_RIGHT im Intervall [-2*Pi, 0].<br>
   * Die Winkel werden vor der Berechnung auf das Intervall [0, 2*Pi]
   * normiert.
   *
   * @param input_source	der Startwinkel
   * @param input_target	der Endwinkel
   * @param input_orientation	die Orientierung ORIENTATION_LEFT oder
   *				ORIENTATION_RIGHT
   *
   * @return den Winkel zwischen Start- und Endwinkel in Bogenmass
   *
   * @see #ORIENTATION_LEFT
   * @see #ORIENTATION_RIGHT
   */

   static public double delta(
      double	input_source,
      double	input_target,
      int	input_orientation
   )
   {
      double	source	= LimitedAngle.limit( input_source, 
      					      LimitedAngle.CIRCLE_ABS );
      double	target	= LimitedAngle.limit( input_target, 
      					      LimitedAngle.CIRCLE_ABS );
      double	output_delta	= target - source;

      if ( input_orientation == ORIENTATION_LEFT )
      {
         if ( output_delta < 0 )
         {
	    output_delta += PI_MUL_2;
         } // if
      }
      else // ORIENTATION_RIGHT
      {
         if ( output_delta > 0 )
	 {
	    output_delta -= PI_MUL_2;
	 } // if
      } // if

      return ( output_delta );

   } // delta


   // ************************************************************************


   /**
   * Umwandlung von Grad nach Bogenmass.
   *
   * @param input_deg	der Winkel in Grad
   *
   * @return	der Winkel in Bogenmass
   */

   public static double degToRad(
      double	input_deg
   )
   {
      return ( input_deg * _DEG_TO_RAD );

   } // degToRad


   // ************************************************************************


   /**
   * Umwandlung von Bogenmass nach Grad.
   *
   * @param input_rad	der Winkel in Bogenmass
   *
   * @return	der Winkel in Grad
   */

   public static double radToDeg(
      double	input_rad
   )
   {
      return ( input_rad * _RAD_TO_DEG );

   } // radToDeg


   // ************************************************************************


   /**
   * Berechnet die Orientierung des Kreisbogens vom Startwinkel ueber den
   * Innenwinkel zum Endwinkel.<br>
   * Die Winkel werden vor der Berechnung auf das Intervall von 0 bis 2 Pi 
   * normiert.<br>
   * Ist der Innenwinkel gleich dem Start- oder dem Endwinkel, so ist das 
   * Ergebnis ORIENTATION_LEFT.
   *
   * @param input_source	der Startwinkel
   * @param input_inner		der Innenwinkel
   * @param input_target	der Endwinkel
   *
   * @return ORIENTATION_LEFT oder ORIENTATION_RIGHT
   *
   * @see #ORIENTATION_LEFT
   * @see #ORIENTATION_RIGHT
   */

   public static int orientation(
      double	input_source,
      double	input_inner,
      double	input_target
   )
   {
      double	source	= LimitedAngle.limit( input_source, 
      					      LimitedAngle.CIRCLE_ABS );
      double	inner	= LimitedAngle.limit( input_inner, 
      					      LimitedAngle.CIRCLE_ABS );
      double	target	= LimitedAngle.limit( input_target, 
      					      LimitedAngle.CIRCLE_ABS );

      if (    ( inner == source )
           || ( inner == target ) )
      {
         return ( ORIENTATION_LEFT );
      } // if

      if ( source < target )
      {
         if (    ( inner > source ) 
	      && ( inner < target ) )
	 {
            return ( ORIENTATION_LEFT );
	 }
	 else
	 {
            return ( ORIENTATION_RIGHT );
	 } //  if
      }
      else // source >= target
      {
         if (    ( inner > target ) 
	      && ( inner < source ) )
	 {
            return ( ORIENTATION_RIGHT );
	 }
	 else
	 {
            return ( ORIENTATION_LEFT );
	 } //  if
      } // if

   } // orientation


   // ************************************************************************


   /**
   * Erzeugt eine textuelle Repraesentation der Orientierung.
   *
   * @return die textuelle Repraesentation der Orientierung
   */

   static public String orientationToString(
      int	input_orientation
   )
   {
      if      ( input_orientation == ORIENTATION_LEFT  )
         return ( new String ( "ORIENTATION_LEFT" ) );
      else if ( input_orientation == ORIENTATION_RIGHT )
         return ( new String ( "ORIENTATION_RIGHT" ) );
      else
         return ( new String ( "orientation unknown" ) );

   } // orientationToString


   // ************************************************************************
   // Public methods
   // ************************************************************************


   /**
   * Erzeugt eine Kopie des Winkels.
   *
   * @return die Kopie
   */

   public Object clone()
   {
      return ( ( Object ) new Angle( this ) );

   } // clone

      
   // ************************************************************************


   /**
   * Berechnet den Winkel zum Eingabewinkel in Richtung der Orientierung,
   * das Ergebnis liegt bei ORIENTATION_LEFT im Intervall [0, 2*Pi], bei 
   * ORIENTATION_RIGHT im Intervall [-2*Pi, 0].<br>
   * Die Winkel werden vor der Berechnung auf das Intervall [0, 2*Pi]
   * normiert.
   *
   * @param input_angle		der Eingabewinkel
   * @param input_orientation	die Orientierung ORIENTATION_LEFT oder
   *				ORIENTATION_RIGHT
   *
   * @return den Winkel zum Eingabewinkel in Bogenmass
   *
   * @see #ORIENTATION_LEFT
   * @see #ORIENTATION_RIGHT
   */

   public double delta(
      Angle	input_angle,
      int	input_orientation
   )
   {
      return ( delta( _rad, input_angle._rad, input_orientation ) );

   } // delta


   // ************************************************************************


   /**
   * Gibt den Winkel in Grad zurueck.
   *
   * @return der Winkel in Grad
   */

   public double deg()
   {
      return( _deg );
       
   } // deg


   // ************************************************************************


   /**
   * Gibt den Winkel in Bogenmass zurueck.
   *
   * @return der Winkel in Bogenmass
   */

   public double rad()
   {
      return( _rad );
       
   } // rad


   // ************************************************************************


   /**
   * Gibt den Sinus des Winkels zurueck.
   *
   * @return der Sinus des Winkels
   */

   public double sin()
   {
      return( _sin );
       
   } // sin


   // ************************************************************************


   /**
   * Gibt den Cosinus des Winkels zurueck.
   *
   * @return der Cosinus des Winkels
   */

   public double cos()
   {
      return( _cos );
       
   } // cos


   // ************************************************************************


   /**
   * Gibt den Tangens des Winkels zurueck.
   *
   * @return der Tangens des Winkels
   */

   public double tan()
   {
      return( _tan );
       
   } // tan


   // ************************************************************************


   /**
   * Setzt den Winkel auf den Eingabewert in Bogenmass.
   *
   * @param input_rad	der Winkel in Bogenmass
   */

   public void set(
      double	input_rad
   )
   {
      if ( _rad != input_rad )
      {
	 _alterRad( input_rad );
      } // if

   } // set


   // ************************************************************************


   /**
   * Setzt den Winkel auf den Eingabewert in der Eingabeeinheit.
   * Loest eine IllegalArgumentException aus, wenn die Einheit weder
   * DEG noch RAD ist.
   *
   * @param input_value		der Eingabewert
   * @param input_unit		die Einheit DEG oder RAD
   *
   * @see #DEG
   * @see #RAD
   */

   public void set(
      double	input_value,
      int	input_unit
   )
   {
      if ( input_unit == DEG )
      {
	 if ( _deg != input_value )
	 {
	    _alterDeg( input_value );
	 } // if 
      }
      else if ( input_unit == RAD )
      {
	 if ( _rad != input_value )
	 {
	    _alterRad( input_value );
	 } // if
      }
      else
      {
         throw new IllegalArgumentException( "illegal unit" );
      } // if

   } // set


   // ************************************************************************


   /**
   * Erzeugt eine textuelle Repraesentation.
   */

   public String toString()
   {
      return ( new String( _rad + " rad" ) );

   } // toString


   // ************************************************************************
   // Private methods
   // ************************************************************************


   /*
   * Aendert den Winkel auf den Eingabewinkel in Grad.
   */

   private void _alterDeg(
      double	input_deg
   )
   {
      _deg = input_deg;
      _rad = degToRad( input_deg );

      _sin = Math.sin( _rad );
      _cos = Math.cos( _rad );
      _tan = Math.tan( _rad );

   } // _alterDeg


   // ************************************************************************


   /*
   * Aendert den Winkel auf den Eingabewinkel in Bogenmass.
   */

   private void _alterRad(
      double	input_rad
   )
   {
      _deg = radToDeg( input_rad );
      _rad = input_rad;

      _sin = Math.sin( _rad );
      _cos = Math.cos( _rad );
      _tan = Math.tan( _rad );

   } // _alterRad


   // ************************************************************************


} // class Angle

