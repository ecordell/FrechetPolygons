
package anja.ratgeom;

import anja.util.BigRational;
import anja.util.Drawable;


/**
* BasicLine2 ist eine abstrakte Basisklasse fuer Line2, Ray2 und Segment2,
* das sind gerichtete Geraden, Strahlen und Segmente - zusammenfassend als
* <em> Linien </em> bezeichnet - mit <em> rationalen Koordinaten </em>
* beliebiger Genauigkeit. <br>
* Alle Linien werden ueber die Punkte <tt> _source </tt> und <tt> _target
* </tt>  definiert, und sie sind wie erwartet von <tt> _source </tt> nach 
* <tt> _target </tt> gerichtet.
*
* @version	0.1 10.11.1997
* @author	Norbert Selle
*
* @see		Line2
* @see		Ray2
* @see		Segment2
*/


abstract public class BasicLine2 implements Drawable,
						Cloneable
{

   // ************************************************************************
   // Constants
   // ************************************************************************


   // ************************************************************************
   // Variables
   // ************************************************************************


   /** der Startpunkt	*/
   protected Point2	_source;

   /** der Endpunkt	*/
   protected Point2	_target;


   // ************************************************************************
   // Constructors
   // ************************************************************************


   /**
   * Erzeugt die Linie von (0, 0) nach (0, 0).
   */

   public BasicLine2()
   {
      this( new Point2( 0, 0 ) ,
	    new Point2( 0, 0 ) );

   } // BasicLine2


   // ********************************


   /**
   * Erzeugt eine zur Eingabelinie identische Linie.
   *
   * @param input_basicline		Eingabelinie
   */

   public BasicLine2(
      BasicLine2	input_basicline
   )
   {
      this( new Point2( input_basicline._source ),
	    new Point2( input_basicline._target )
	  );

   } // BasicLine2


   // ********************************


   /**
   * Erzeugt eine Linie durch den ersten Eingabepunkt in Richtung des
   * zweiten Eingabepunktes, die Linie merkt sich <b> Referenzen </b>
   * auf die Punkte.
   *
   * @param input_source	der Startpunkt
   * @param input_target	der Zielpunkt
   */

   public BasicLine2(
      Point2	input_source,
      Point2	input_target
   )
   {
      _source = input_source;
      _target = input_target;
             
   } // BasicLine2


   // ********************************


   /**
   * Erzeugt eine Linie durch die Punkte mit den Eingabekoordinaten.
   *
   * @param input_source_x	die x-Koordinate des Startpunkts
   * @param input_source_y	die y-Koordinate des Startpunkts
   * @param input_target_x	die x-Koordinate des Zielpunkts
   * @param input_target_y	die y-Koordinate des Zielpunkts
   */

   public BasicLine2(
      double	input_source_x,
      double	input_source_y,
      double	input_target_x,
      double	input_target_y
   )
   {
      this( new Point2( input_source_x, input_source_y ) ,
	    new Point2( input_target_x, input_target_y ) );

   } // BasicLine2


   // ************************************************************************
   // Public methods
   // ************************************************************************

   
   /**
   * Berechnet den Punkt mit der X-Koordinate x auf der Linie. Wenn die Linie
   * nicht vertikal ist und der Punkt auf dem Objekt liegt (abhaengig von der 
   * abgeleiteten Klasse), dann wird er zurueckgegeben, ansonsten wird null
   * zurueckgegeben.
   *
   * @param input_x  die X-Koordinate
   *
   * @return den Punkt auf dem Objekt
   */

   public Point2 calculatePoint(
      double input_x
   )
   {
      if ( isVertical() )
	 return null;

      // (y = slope * x + y_abs) oder auf deutsch: (y = m * x + b)

      BigRational x     = BigRational.valueOf( input_x );
      BigRational slope = deltaY().divide( deltaX() );
      BigRational y_abs = _source.y.subtract( slope.multiply( _source.x ) );
      BigRational y	= slope.multiply( x ).add( y_abs );

      Point2      new_p = new Point2( x, y );

      if ( inspectCollinearPoint( new_p ) == Point2.LIES_ON )
	 return new_p;
      else
	 return null;

   } // calculatePoint


   // ************************************************************************


   /**
   * Erzeugt eine Kopie und gibt sie zurueck.
   */

   public abstract Object clone();


   // ************************************************************************


   /**
   * Gibt true zurueck bei Gleichheit, sonst false.
   */

   public boolean equals(
      Object	input_object
   )
   {
      if ( input_object == null )
         return ( false );

      if ( getClass() != input_object.getClass() )
         return ( false );

      BasicLine2	basicline = ( BasicLine2 ) input_object;

      return (    _source.equals( basicline._source )
               && _target.equals( basicline._target )
	     );

   } // equals


   // ************************************************************************


   /**
   * Untersucht die Lage des Eingabepunktes in Bezug auf die mit ihm auf
   * einer Geraden liegenden Linie.
   */

   public abstract int inspectCollinearPoint(
      Point2	input_point
   );


   // ************************************************************************


   /**
   * Berechnet die Schnittmenge mit dem Kreisumfang und gibt sie im
   * Intersection-Parameter zurueck. Die Schnittmenge kann leer sein oder 
   * einen Punkt oder eine Liste mit zwei Punkten enthalten, im Falle eines
   * Punktes wird er ausserdem als Ergebnis der Methode zurueckgegeben.
   *
   * @see Circle2#intersection
   */

   public Point2 intersection(
      Circle2		input_circle,
      Intersection	inout_set
   )
   {
      return( input_circle.intersection( this, inout_set ) );

   } // intersection


   // ********************************


   /**
   * Berechnet die Schnittmenge mit dem Eingabelinie und gibt sie im 
   * Intersection-Parameter zurueck, ist es ein Schnittpunkt wird er
   * ausserdem als Ergebnis zurueckgegeben.
   * 
   * @param  input_basic	die Eingabelinie
   * @param  inout_set		die Schnittmenge
   * 
   * @return den Schnittpunkt wenn er existiert, sonst null
   */

   abstract public Point2 intersection(
      BasicLine2	input_basic,
      Intersection	inout_set
   );


   // ************************************************************************


   /**
   * Berechnet ( target().x -  source().x ).
   */

   public BigRational deltaX()
   {
      return ( _target.x.subtract( _source.x ) );

   } // deltaX


   // ************************************************************************


   /**
   * Berechnet ( target().y - source().y ).
   */

   public BigRational deltaY()
   {
      return ( _target.y.subtract( _source.y ) );

   } // deltaY


   // ************************************************************************


   /**
   * Untersucht die Beziehung zur Eingabelinie, bei Parallelitaet ist die 
   * Flagge <tt> parallel </tt> des Ergebnisses gleich true, sonst ist sie
   * gleich false und die Komponenten <tt> intersectionPoint </tt>, 
   * <tt> orderOnThis </tt> und <tt> orderOnParam </tt> des Ergebnisses 
   * sind gesetzt.
   *
   * @return das Ergebnis der Untersuchung
   */

   public InspectResult inspectBasicLine(
      BasicLine2	input_basic
   )
   {
      // Sei this        = (x1, y1), (x2, y2)
      // und input_basic = (x3, y3), (x4, y4)

      BigRational	dx_1_2	= deltaX();
      BigRational	dy_1_2	= deltaY();
      BigRational	dx_3_4	= input_basic.deltaX();
      BigRational	dy_3_4	= input_basic.deltaY();
      BigRational 	det     =           ( dx_1_2.multiply( dy_3_4 )
                                  ).subtract( dy_1_2.multiply( dx_3_4 ) );

      InspectResult	result	= new InspectResult();

      if ( det.signum() == 0 )
      {
         result.parallel = true;
      }
      else
      {
         result.parallel = false;

	 BigRational dx_1_3 = input_basic.source().x.subtract( source().x );
	 BigRational dy_1_3 = input_basic.source().y.subtract( source().y );

	 BigRational lambda =           ( dx_1_3.multiply( dy_3_4 )
	 		      ).subtract( dy_1_3.multiply( dx_3_4 )
	 		      ).divide( det );

	 BigRational new_x  = source().x.add( lambda.multiply( dx_1_2 ) );
	 BigRational new_y  = source().y.add( lambda.multiply( dy_1_2 ) );

	 result.intersectionPoint = new Point2( new_x, new_y );

	 result.orderOnThis  = result.intersectionPoint.inspectCollinearPoint( 
				     source(),
				     target() );
	 result.orderOnParam = result.intersectionPoint.inspectCollinearPoint( 
				     input_basic.source(),
				     input_basic.target() );
      } // if

      return ( result );

   } // inspectBasicLine


   // ************************************************************************


   /**
   * Gibt true zurueck wenn der Eingabepunkt auf der Geraden durch source()
   * und target() liegt, sonst false.
   *
   * @return true wenn ja, sonst false
   *
   * @see #liesOn
   */

   public boolean isCollinear(
      Point2	input_point
   )
   {
      return ( input_point.isCollinear( _source, _target ) );

   } // isCollinear


   // ************************************************************************


   /**
   * Ist die Linie horizontal?
   */

   public boolean isHorizontal()
   {
      return( _source.y.equals( _target.y ) );

   } // isHorizontal


   // ************************************************************************


   /**
   * Ist die Linie vertikal?
   */

   public boolean isVertical()
   {
      return( _source.x.equals( _target.x ) );

   } // isVertical


   // ************************************************************************


   /** 
   * Gibt true zurueck wenn die Linie parallel zur Linie durch die beiden
   * Eingabepunkte ist, sonst false.
   */

   public boolean isParallel(
      Point2		pointP,
      Point2		pointQ
   )
   {   
      BigRational	in_delta_x = pointQ.x.subtract( pointP.x );
      BigRational	in_delta_y = pointQ.y.subtract( pointP.y );

      return (         ( deltaX().multiply( in_delta_y )
	       ).equals( deltaY().multiply( in_delta_x ) )
	     );

   } // isParallel


   // ********************************


   /** 
   * Gibt true zurueck wenn die Linien parallel sind, sonst false.
   */

   public boolean isParallel(
      BasicLine2  input_line
   )
   {   
      return (         ( deltaX().multiply( input_line.deltaY() )
	       ).equals( deltaY().multiply( input_line.deltaX() ) )
	     );

   } // isParallel


   // ************************************************************************


   /**
   * Gibt true zurueck wenn der Eingabepunkt auf der Linie liegt, sonst false.
   *
   * @see	#isCollinear
   */

   public boolean liesOn(
      Point2	input_point
   )
   {
      return (    ( isCollinear( input_point )                             )
	       && ( inspectCollinearPoint( input_point ) == Point2.LIES_ON )
	     );

   } // liesOn


   // ************************************************************************


   /**
   * Bestimmt ob der Eingabepunkt in der linken oder in der rechten Halbebene 
   * oder auf der Linie liegt. <br>
   * Ist die Linie nur ein Punkt so wird <tt> ORIENTATION_COLLINEAR </tt>
   * zurueckgegeben, wenn der Eingabepunkt auf diesem Punkt liegt, sonst
   * wird <tt> ORIENTATION_UNDEFINED </tt> zurueckgegeben.
   *
   * @param input_point		Eingabepunkt
   *
   * @return ORIENTATION_COLLINEAR, ORIENTATION_LEFT etc.
   *
   * @see anja.ratgeom.Point2#ORIENTATION_COLLINEAR
   * @see anja.ratgeom.Point2#ORIENTATION_LEFT
   * @see anja.ratgeom.Point2#ORIENTATION_RIGHT
   * @see anja.ratgeom.Point2#ORIENTATION_UNDEFINED 
   */

   public int orientation(
      Point2	input_point
   )
   {
      return ( input_point.orientation(	_source, _target ) );

   } // orientation


   // ************************************************************************


   /**
   * Berechnet eine orthogonale Gerade und gibt sie zurueck.
   */

   public Line2 orthogonal()
   {
      Point2	new_source = new Point2( _source );
      Point2	new_target = new Point2( new_source.x.add(      deltaY() ),
					 new_source.y.subtract( deltaX() )
				       );

      return ( new Line2( new_source, new_target ) );

   } // orthogonal

      
   // ********************************


   /**
   * Berechnet die orthogonale Gerade durch den Eingabepunkt und gibt sie
   * zurueck.
   */

   public Line2 orthogonal(
      Point2	input_point
   )
   {
      Point2	new_source = new Point2( input_point );
      Point2	new_target = new Point2( new_source.x.add(      deltaY() ),
				         new_source.y.subtract( deltaX() )
				       );

      return ( new Line2( new_source, new_target ) );

   } // orthogonal

      
   // ************************************************************************


   /**
   * Berechnet die Steigung und gibt sie zurueck.
   */

   public double slope()
   {
      if ( isVertical() )
         return Double.POSITIVE_INFINITY;
      else
         return ( deltaY().divide( deltaX() ) ).doubleValue();

   } // slope


   // ************************************************************************


   /**
   * Gibt eine <b> Referenz </b> auf den Startpunkt zurueck.
   */

   public Point2 source()
   {
      return( _source );

   } // source


   // ************************************************************************


   /**
   * Gibt eine <b> Referenz </b> auf den Endpunkt zurueck.
   */

   public Point2 target()
   {
      return( _target );

   } // target


   // ************************************************************************


   /**
   * Erzeugt eine textuelle Repraesentation und gibt sie zurueck.
   */

   public String toString()
   {
      return ( _source.toString() + "-->" + _target.toString() );

   } // toString


   // ************************************************************************


   /**
   * Verschiebt die Linie um den Vektor vom Nullpunkt zum Eingabepunkt.
   *
   * @param input_point		der Eingabepunkt
   */

   public void translate(
      Point2	input_point
   )
   {
      _source.translate( input_point );
      _target.translate( input_point );

   } // translate


   // ********************************


   /**
   * Verschiebt die Linie um die Eingabewerte.
   *
   * @param input_horizontal	die horizontale Verschiebung
   * @param input_vertical	die vertikale Verschiebung
   */

   public void translate(
      double	input_horizontal,
      double	input_vertical
   )
   {
      _source.translate( input_horizontal, input_vertical );
      _target.translate( input_horizontal, input_vertical );

   } // translate


   // ************************************************************************
   // Private methods
   // ************************************************************************


   // ************************************************************************


} // class BasicLine2

