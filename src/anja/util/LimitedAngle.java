
package anja.util;

import java.lang.Math;


/**
* Ein <tt> LimitedAngle </tt> ist ein auf einen Wertebereich normierter
* Winkel. Der Wertebereich wird beim Erzeugen festgelegt und ist konstant,
* der Winkel wird in den Konstruktoren und in den <tt> set()</tt>-Methoden
* auf den Wertebereich normiert. <br>
* Bei den folgenden Beispielen sind Input und Value der Anschaulichkeit 
* halber in Grad, Input ist der beim Erzeugen oder Setzen angegebene Winkel
* und Value das Ergebnis, das man beim Abfragen des <tt> LimitedAngle </tt>
* erhaelt:
*
* <table border cellpadding=8>
*    <tr> <th> Name            <th> Interval    <th> Input  <th> Value  </tr>
*    <tr> <td> NO_LIMIT        <td>             <td> -17000 <td> -17000 </tr>
*    <tr> <td> CIRCLE          <td> [-2Pi, 2Pi] <td>   -361 <td>     -1 </tr>
*    <tr> <td> CIRCLE_ABS      <td> [ 0  , 2Pi] <td>   -361 <td>    359 </tr>
*    <tr> <td> CIRCLE_ABS      <td> [ 0  , 2Pi] <td>    -10 <td>    350 </tr>
*    <tr> <td> OPEN_CIRCLE     <td> ]-2Pi, 2Pi[ <td>    360 <td>      0 </tr>
*    <tr> <td> OPEN_CIRCLE_ABS <td> [ 0  , 2Pi[ <td>   -360 <td>      0 </tr>
* </table>
*
* @version	1.0 04.09.1997
* @author	Norbert Selle
*/

public class LimitedAngle extends Angle
{

   // ************************************************************************
   // Public constants
   // ************************************************************************


   /** unbeschraenkter Wertebereich				*/
   public final static int		NO_LIMIT	= 301;

   /** Wertebereich in Bogenmass: -2 PI <= Winkel <= 2 PI	*/
   public final static int		CIRCLE		= 302;

   /** Wertebereich in Bogenmass: 0 <= Winkel <= 2PI		*/
   public final static int		CIRCLE_ABS	= 303;

   /** Wertebereich in Bogenmass: -2 PI < Winkel < 2 PI		*/
   public final static int		OPEN_CIRCLE	= 304;

   /** Wertebereich in Bogenmass: 0 <= Winkel < 2 PI		*/
   public final static int		OPEN_CIRCLE_ABS	= 305;


   // ************************************************************************
   // Private constants
   // ************************************************************************


   // ************************************************************************
   // Variables
   // ************************************************************************


   /* der Wertebereich; NO_LIMIT, CIRCLE etc.			*/
   private int			_interval;


   // ************************************************************************
   // Constructors
   // ************************************************************************


   /**
   * Erzeugt einen 0-Grad Winkel mit dem eingegebenen Wertebereich.
   *
   * @param input_interval	der Wertebereich NO_LIMIT, CIRCLE etc.
   *
   * @see #NO_LIMIT
   * @see #CIRCLE
   */

   public LimitedAngle(
      int	input_interval
   )
   {
      super();

      _interval = input_interval;

   } // LimitedAngle


   // ********************************              


   /**
   * Erzeugt einen auf den Wertebereich normierten Winkel aus dem Eingabewinkel.
   *
   * @param input_angle		der Eingabewinkel
   * @param input_interval	der Wertebereich NO_LIMIT, CIRCLE etc.
   *
   * @see #NO_LIMIT
   * @see #CIRCLE
   */

   public LimitedAngle(
      Angle		input_angle,
      int		input_interval
   )
   {
      super( input_angle );

      _interval = input_interval;

      set( rad() );	// Winkel auf den Wertebereich normieren

   } // LimitedAngle


   // ********************************              


   /**
   * Erzeugt eine Kopie des Eingabewinkels.
   *
   * @param input_limited	der Eingabewinkel
   */

   public LimitedAngle(
      LimitedAngle		input_limited
   )
   {
      super( ( Angle ) input_limited );

      _interval = input_limited._interval;

   } // LimitedAngle


   // ********************************              


   /**
   * Erzeugt einen auf den Wertebereich normierten Winkel aus dem
   * Eingabewinkel in Bogenmass.
   *
   * @param input_rad		der Eingabewinkel in Bogenmass
   * @param input_interval	der Wertebereich NO_LIMIT, CIRCLE etc.
   *
   * @see #NO_LIMIT
   * @see #CIRCLE
   */

   public LimitedAngle(
      double		input_rad,
      int		input_interval
   )
   {
      super( limit( input_rad, input_interval ) );

      _interval = input_interval;

   } // LimitedAngle


   // ********************************              


   /**
   * Erzeugt einen auf den Wertebereich normierten Winkel aus dem
   * Eingabewert in der Eingabeeinheit.
   * Loest eine IllegalArgumentException aus, wenn die Einheit weder
   * DEG noch RAD ist.
   *
   * @param input_value		der Eingabewert
   * @param input_unit		die Einheit DEG oder RAD
   * @param input_interval	der Wertebereich NO_LIMIT, CIRCLE etc.
   *
   * @see anja.util.Angle#DEG
   * @see anja.util.Angle#RAD
   * @see #NO_LIMIT
   * @see #CIRCLE
   */

   public LimitedAngle(
      double		input_value,
      int		input_unit,
      int		input_interval
   )
   {
      // Loest gegebenenfalls eine IllegalArgumentException aus
      super( input_value, input_unit );

      _interval = input_interval;

      set( rad() );	// Winkel auf den Wertebereich normieren

   } // LimitedAngle


   // ************************************************************************
   // Public class methods
   // ************************************************************************


   /*
   * Normiert den Winkel in Bogenmass auf den angegebenen Wertebereich.
   * Loest eine IllegalArgumentException aus, wenn der Wertebereich
   * unbekannt ist.
   *
   * @param input_rad		der zu normierende Winkel in Bogenmass
   * @param input_interval	der Wertebereich NO_LIMIT, CIRCLE etc.
   *
   * @return den normierten Winkel
   *
   * @see #NO_LIMIT
   * @see #CIRCLE
   */

   static public double limit(
      double	input_rad,
      int	input_interval
   )
   {
      double	output_limited	= input_rad;

      switch ( input_interval )
      {
      case NO_LIMIT:
         break;
      case CIRCLE:
         if ( Math.abs( input_rad ) > PI_MUL_2 )
	    output_limited = input_rad % PI_MUL_2;
	 break;
      case OPEN_CIRCLE:
         if ( Math.abs( input_rad ) >= PI_MUL_2 )
	    output_limited = input_rad % PI_MUL_2;
	 break;
      case CIRCLE_ABS:
         if ( Math.abs( input_rad ) > PI_MUL_2 )
	    output_limited = input_rad % PI_MUL_2;
         if ( output_limited < 0 )
	    output_limited = output_limited + PI_MUL_2;
         break;
      case OPEN_CIRCLE_ABS:
         if ( Math.abs( input_rad ) >= PI_MUL_2 )
	    output_limited = input_rad % PI_MUL_2;
         if ( output_limited < 0 )
	    output_limited = output_limited + PI_MUL_2;
         break;
      default:
	 throw new IllegalArgumentException( "illegal interval" );
      } // switch

      return ( output_limited );

   } // limit


   // ************************************************************************
   // Private class methods
   // ************************************************************************


   // ************************************************************************
   // Public methods
   // ************************************************************************


   /**
   * Erzeugt eine Kopie und gibt sie zurueck.
   */

   public Object clone()
   {
      return ( ( Object ) new LimitedAngle( this ) );

   } // clone

      
   // ************************************************************************


   /**
   * Setzt den Winkel auf den normierten Eingabewinkel.
   *
   * @param input_rad	der Eingabewinkel in Bogenmass
   */

   public void set(
      double	input_rad
   )
   {
      super.set( limit( input_rad, _interval ) );

   } // set


   // ************************************************************************


   /**
   * Setzt den Winkel auf den normierten Eingabewert in der Eingabeeinheit.
   * Loest eine IllegalArgumentException aus, wenn die Einheit weder
   * DEG noch RAD ist.
   *
   * @param input_value		der Eingabewert
   * @param input_unit		die Einheit des Eingabewertes DEG oder RAD
   *
   * @see anja.util.Angle#DEG
   * @see anja.util.Angle#RAD
   */

   public void set(
      double	input_value,
      int	input_unit
   )
   {
      if ( input_unit == DEG )
      {
	 super.set( limit( Angle.degToRad( input_value ), _interval ) );
      }
      else if ( input_unit == RAD )
      {
         super.set( limit( input_value, _interval ) );
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
      return ( new String( rad() + " rad " + _intervalToString() ) );

   } // toString


   // ************************************************************************
   // Private methods
   // ************************************************************************


   /**
   * Erzeugt eine textuelle Repraesentation des Wertebereichs.
   */

   private String _intervalToString()
   {
      switch( _interval )
      {
      case NO_LIMIT:		return( "NO_LIMIT"	);
      case CIRCLE:		return( "CIRCLE"	);
      case CIRCLE_ABS:		return( "CIRCLE_ABS"	);
      case OPEN_CIRCLE:		return( "OPEN_CIRCLE"	);
      case OPEN_CIRCLE_ABS:	return( "OPEN_CIRCLE_ABS" );
      default:			return( "undefinded"	);
      } // switch

   } // _intervalToString


   // ************************************************************************


} // class LimitedAngle

