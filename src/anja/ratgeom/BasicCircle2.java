
package anja.ratgeom;

import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;


import anja.util.Angle;
import anja.util.BigRational;
import anja.util.Drawable;
import anja.util.GraphicsContext;
import anja.util.List;


/**
* <p align="justify">
* BasicCircle2 ist die abstrakte Basisklasse fuer Arc2 und Circle2. Sie werden
* ueber ihren Mittelpunkt und ihren Radius definiert.
* Mit <tt> setRadius( BigRational, BigRational ) </tt> koennen der Radius und
* und sein Quadrat gesetzt werden, was sinnvoll ist, wenn der letztere Wert
* ausserhalb des Objekts bereits bekannt ist, so dass eine unnoetige mehrfache
* Berechnung gespart wird.
* </p>
*
* @version	0.3  21.11.1997
* @author	Norbert Selle
*
* @see		Arc2
* @see		Circle2
*/

abstract public class BasicCircle2 implements 	Drawable,
						Cloneable
{

   /*
   * Besonderheiten bei der Implementierung
   * --------------------------------------
   *
   * Die Variable _square_radius - die den Radius zum Quadrat beinhaltet -
   * wird nur bei Bedarf belegt, um die Zeit fuer ihre Berechnung wenn moeglich
   * zu sparen. Der Lesezugriff auf sie darf deshalb nur mit der Methode
   * squareRadius() erfolgen. Diese Methode stellt zum einen sicher, dass ein
   * gueltiger Wert geliefert wird, und sorgt zum anderen dafuer, dass der Wert
   * nicht unnoetig mehrfach berechnet wird.
   * Als Konsequenz darauf muss _square_radius in setRadius( BigRational ) auf
   * null zurueckgesetzt werden, damit sie nicht einen falschen Wert enthaelt.
   * Mit setRadius( BigRational, BigRational ) kann _square_radius explizit
   * gesetzt werden.
   */


   // ************************************************************************
   // Constants
   // ************************************************************************
  	

   // die BigRational mit dem Wert zwei
   private static final BigRational _BIG_TWO	= BigRational.valueOf( 2 );

   // die Genauigkeit beim Wurzelziehen
   private static final int	_SCALE		= 40;


   // ************************************************************************
   // Variables
   // ************************************************************************


   /** der Mittelpunkt	 	*/
   public Point2	centre;

   /* der Radius		*/
   private BigRational	_radius;

   /*
   * der Radius zum Quadrat; der Zugriff darf nur ueber squareRadius() erfolgen,
   * weil die Variable nur bei Bedarf belegt wird
   */
   private BigRational	_square_radius;


   // ************************************************************************
   // Constructors
   // ************************************************************************


   /**
   * Erzeugt ein Objekt mit dem Mittelpunkt (0, 0) und dem Radius 0.
   */

   public BasicCircle2()
   {
      this( new Point2( 0, 0 ),
            BigRational.valueOf( 0 ) );

   } // BasicCircle2


   // ********************************


   /**
   * Erzeugt eine Kopie des Eingabeobjekts.
   */

   public BasicCircle2(
      BasicCircle2	input_basic
   )
   {
      centre = new Point2( input_basic.centre );

      setRadius( input_basic._radius,
      		 input_basic._square_radius );

   } // BasicCircle2


   // ********************************


   /**
   * Erzeugt ein Objekt mit den Eingabeparametern, es merkt sich eine 
   * <b> Referenz </b> auf den Mittelpunkt. Ein negatives Vorzeichen des
   * Radius wird automatisch entfernt.
   *
   * @param input_centre	der Mittelpunkt
   * @param input_radius	der Radius
   */

   public BasicCircle2(
      Point2		input_centre,
      BigRational	input_radius
   )
   {
      centre = input_centre;
      setRadius( input_radius );

   } // BasicCircle2


   // ************************************************************************
   // Class methods
   // ************************************************************************


   // ************************************************************************
   // Public methods
   // ************************************************************************


   /**
   * Erzeugt eine Kopie und gibt sie zurueck.
   */

   abstract public Object clone();


   // ************************************************************************


   /**
   * Zeichnet das Objekt.
   */

   abstract public void draw(
       Graphics2D	g,
       GraphicsContext	gc
   );


   // ************************************************************************


   /**
   * Gibt das umschliessende Rechteck zurueck.
   */

   abstract public Rectangle2D getBoundingRect();


   // ************************************************************************


   /**
   * Untersucht die Schnittmenge mit dem eingegebenen BasicCircle. Bei leerer
   * Schnittmenge wird null zurueckgegeben. Sind die Mittelpunkte und Radien 
   * gleich, so ist lies_on gleich true und points gleich null. Ansonsten ist 
   * lies_on gleich false und points[] hat die Laenge eins oder zwei und enthaelt
   * den einen oder die zwei Schnittpunkte der Kreisumfaenge.
   *
   * @return null bei leerer Schnittmenge, sonst ein InspectBCResult
   */

   protected InspectBCResult inspectBasicCircle(
      BasicCircle2	input_basic
   )
   {
      BigRational	r_sum    = radius().add( input_basic.radius() );
      BigRational	distance = centre.bigDistance( input_basic.centre );

      if (    ( r_sum.signum()    == 0 ) // Fall 0: Punkt auf Punkt
           && ( distance.signum() == 0 )
	 )
	 return ( new InspectBCResult( true ) ); // lies_on ist true

      int d_compare_rsum = distance.compareTo( r_sum );

      if ( d_compare_rsum == 0 ) 	// Fall I: d = r1 + r2
         return ( _outerKissPoint( input_basic, distance ) );

      if ( d_compare_rsum > 0 ) 	// Fall II: d > r1 + r2
         return ( null );

      // Fall III: d < r1 + r2

      BigRational delta	= radius().subtract( input_basic.radius() ).abs();

      if ( distance.signum() == 0 )	// Fall III a: d = 0
      {
         if ( delta.signum() == 0 )	// Fall III a 1.: r1 = r2
            return ( new InspectBCResult( true ) ); // lies_on ist true
	 else				// Fall III a 2.: r1 <> r2
	    return ( null );
      } // if
      
      // Fall III b: d <> 0

      int d_compare_delta = distance.compareTo( delta );

      if ( d_compare_delta == 0 )	// Fall III b 1.: d = abs( r2 - r1 )
	 return ( _innerKissPoint( input_basic, distance ) );

      if ( d_compare_delta < 0 )	// Fall III b 2.: d < abs( r2 - r1 )
	 return ( null );

      // Fall III b 3.: d > abs( r2 - r1 )

      return ( _intersectionPoints( input_basic ) );

   } // inspectBasicCircle


   // ************************************************************************


   /**
   * Berechnet die Schnittmenge mit den Polygonkanten und gibt sie im 
   * Intersection-Parameter zurueck, sie kann leer sein oder eine eine 
   * nichtleere Liste von Punkten enthalten.
   */
/* !!NS not yet implemented
   public void intersection(
      Polygon2		input_polygon,
      Intersection	inout_intersection
   )
   {
      input_polygon.intersection( this, inout_intersection );

   } // intersection
*/

   // ********************************              


   /**
   * Berechnet die Schnittmenge des Umfangs mit der Eingabelinie und gibt sie 
   * im Intersection-Parameter zurueck. Die Schnittmenge kann leer sein oder 
   * einen Punkt oder eine Liste mit zwei Punkten enthalten, im Falle eines
   * Punktes wird er ausserdem als Ergebnis der Methode zurueckgegeben. <br>
   * Ein Segment oder ein Strahl kann den Kreis auch dann in genau einem Punkt
   * schneiden, wenn es keine Tangente ist, sondern im Kreisinneren beginnt
   * oder endet.
   *
   * <dl>
   *   <dt> <b> Author: </b>
   *   <dd> Urs Bachert
   *   <dd> Norbert Selle (Anpassung an anja/geom)
   * </dl>
   */

   public Point2 intersection(
      BasicLine2	input_line,
      Intersection	inout_intersection
   )
   {
      // equations: 
      //    I.    source.y = m * source.x + b                    (line)
      //    II.   target.y = m * target.x + b                    (line)
      //    III.  (x - centre.x)² + (y - centre.y)² = centre.r²  (circle)
   
      Point2		source		= input_line.source();
      Point2		target		= input_line.target();
      List		points		= new List();
      BigRational	radicant;

      if ( input_line.isVertical() )
      {   
	 radicant =            squareRadius()
	            .subtract( source.x.subtract( centre.x ).square() );

	 if ( radicant.signum() == 0 )
	 {
	     _addInside( input_line, new Point2( source.x, centre.y ), points );
	 } 
	 else if ( radicant.signum() > 0 )
	 {
	    BigRational radicant_sqrt = radicant.sqrt( _SCALE );
	    Point2	p1 = new Point2( source.x,
	    				 centre.y.add( radicant_sqrt ) );
	    Point2	p2 = new Point2( source.x,
	    				 centre.y.subtract( radicant_sqrt ) );
	    _addInside( input_line, p1, points );
	    _addInside( input_line, p2, points );
	 } // if
      }
      else
      {
	 BigRational	delta_x	    = input_line.deltaX();
	 BigRational	delta_y	    = input_line.deltaY();
	 BigRational	m           = delta_y.divide( delta_x );
	 BigRational	square_m    = m.square();
	 BigRational	sm_plus_one = square_m.add( BigRational.valueOf( 1 ) );

	 BigRational	b =           ( source.y.multiply( target.x )
	 		    ).subtract( source.x.multiply( target.y )
	 		    ).divide( delta_x );


	 BigRational	cx_times_m = centre.x.multiply( m );

	 BigRational	term2 =    b.multiply( centre.y.subtract( cx_times_m )
			    ).add( centre.y.multiply( cx_times_m )
			    ).multiply( BigRational.valueOf( 2 ) );

	 radicant =           ( squareRadius().multiply( sm_plus_one )
	 	    ).subtract( b.square()
	 	    ).subtract( centre.y.square()
	 	    ).subtract( centre.x.square().multiply( square_m )
	 	    ).add( term2 );

	 if ( radicant.signum() >= 0 )
	 {
	    BigRational	real_part =        centre.x.subtract( b.multiply( m )
	    			    ).add( centre.y.multiply( m )
	    			    ).divide( sm_plus_one );

	    if (radicant.signum() == 0) 
	    {   
	       Point2   p   = new Point2( real_part, 
	       				  m.multiply( real_part ).add( b ) );
	       _addInside( input_line, p, points );
	    }
	    else
	    {
	       BigRational delta = radicant.sqrt( _SCALE ).divide( sm_plus_one );

	       BigRational x1 = real_part.add(      delta );
	       BigRational x2 = real_part.subtract( delta );

	       Point2 p1 = new Point2( x1, m.multiply( x1 ).add( b ) );
	       Point2 p2 = new Point2( x2, m.multiply( x2 ).add( b ) );

	       _addInside( input_line, p1, points );
	       _addInside( input_line, p2, points );
	    }  // if
	 } // if
      }  // if

      if ( points.empty() )
         inout_intersection.set();
      else if ( points.length() == 1 )
         inout_intersection.set( ( Point2 ) points.firstValue() );
      else
         inout_intersection.set( points );

      return ( inout_intersection.point2 );

    } // intersection


   // ************************************************************************


   /**
   * Testet ob der <b> Umfang </b> beziehungsweise der Kreisbogen die Linie 
   * schneidet.
   *
   * @param  input_line	die zu testende Linie
   *
   * @return true wenn ja, sonst false
   */

   public boolean intersects(
      BasicLine2	input_line
   )

   {
      Intersection	set	= new Intersection();

      intersection( input_line, set );

      return ( set.result != Intersection.EMPTY );

   } // intersects


   // ************************************************************************


   /**
   * Gibt true zurueck wenn die <b> Kreisflaeche </b> beziehungsweise der 
   * Kreisbogen das Rechteck schneidet, sonst false.
   *
   * @param  input_box	das zu testende Rechteck
   */

   abstract public boolean intersects(
      Rectangle2D	input_box
   );


   // ************************************************************************


   /** 
   * Berechnet den Umfang beziehungsweise die Laenge des Kreisbogens und gibt
   * das Ergebnis zurueck.
   */

   abstract public double len();


   // ************************************************************************


   /**
   * Gibt true zurueck wenn der Punkt auf dem Umfang beziehungsweise auf dem
   * Kreisbogen liegt, sonst false.
   */

   abstract public boolean liesOn(
      Point2		input_point
   );


   // ************************************************************************


   /**
   * Berechnet die Anzahl Ecken, mit der ein regelmaessiges Polygon den
   * Kreis hinreichend genau fuer eine runde zeichnerische Darstellung
   * approximiert.
   */

   protected int polygonEdgeNumber(
      Graphics2D	g
   )
   {
      int		output_number	= 20;

      //anja.geom.Point2	zero	= new anja.geom.Point2( 0, 0 );
      //anja.geom.Point2	one	= new anja.geom.Point2( 1, 1 );
      Point2D.Float zero = new Point2D.Float(0,0);
      Point2D.Float one  = new Point2D.Float(1,1);

      try
      {
	 g.getTransform().inverseTransform( zero, zero );
	 g.getTransform().inverseTransform( one , one  );

	 float delta_pixel = Math.min( Math.abs( one.x - zero.x ),
	 			       Math.abs( one.y - zero.y ) );

	 // Formal nach "Computer Graphics Software Construction" von
	 // John R. Rankin, S. 73

         output_number = Math.round( ( float )
	    (   ( Angle.PI_MUL_2				    )
	      / ( Math.asin( delta_pixel / radius().doubleValue() ) )
	    ) );
      }
      catch ( NoninvertibleTransformException ex )
      {
      } // try

      return ( output_number );

   } // polygonEdgeNumber


   // ************************************************************************


   /*
   * Gibt den Radius zurueck.
   */

   public BigRational radius()
   {
      return ( _radius );

   } // radius


   // ************************************************************************


   /**
   * Setzt den Radius auf den Betrag des Eingabewertes.
   */

   public void setRadius(
      BigRational	input_radius
   )
   {
      _radius		= input_radius.abs();
      _square_radius	= null;		// _square_radius zuruecksetzen

   } // setRadius


   // ************************************************************************


   /**
   * Setzt den Radius auf den Betrag des Eingaberadius und den squareRadius
   * auf den Betrag des Eingabe-SquareRadius, falls dieser ungleich null ist,
   * sonst auf null. <br>
   * Die korrektheit der beiden Werte wird nicht geprueft, das liegt in der
   * Verantwortung der aufrufenden Methode.
   */

   public void setRadius(
      BigRational	input_radius,
      BigRational	input_square 
   )
   {
      _radius = input_radius.abs();

      if ( input_square == null )
         _square_radius	= null;		// _square_radius zuruecksetzen
      else
         _square_radius	= input_square.abs();

   } // setRadius


   // ************************************************************************


   /*
   * Gibt das Quadrat des Radius zurueck.
   */

   public BigRational squareRadius()
   {
      // _square_radius noetigenfalls erst berechnen 

      if ( _square_radius == null )
      {
         _square_radius = radius().square();
      } // if

      return ( _square_radius );

   } // squareRadius


   // ************************************************************************


   /**
   * Erzeugt eine textuelle Repraesentation und gibt sie zurueck.
   */

   abstract public String toString();


   // ************************************************************************


   /**
   * Verschiebung um die Eingabewerte.
   *
   * @param input_horizontal	die horizontale Verschiebung
   * @param input_vertical	die vertikale Verschiebung
   */

   public void translate(
      double	input_horizontal,
      double	input_vertical
   )
   {
      centre.translate( input_horizontal, input_vertical );

   } // translate


   // ********************************


   /**
   * Verschiebung um den Vektor vom Nullpunkt zum Eingabepunkt.
   *
   * @param input_point		der Eingabepunkt
   */

   public void translate(
      Point2	input_point
   )
   {
      centre.translate( input_point );

   } // translate


   // ************************************************************************
   // Private methods
   // ************************************************************************


   /*
   * Haengt den Eingabepunkt an die Liste wenn er auf der Eingabelinie liegt.
   */

   private void _addInside(
      BasicLine2	input_line,
      Point2		input_point,
      List		inout_points
   )
   {
      if ( input_line.inspectCollinearPoint( input_point ) == Point2.LIES_ON )
      {
         inout_points.add( input_point );
      } // if

   } // _addInside


   // ************************************************************************


   /*
   * Berechnet den Schnittpunkt mit dem Eingabekreis, einer der Kreise liegt
   * im inneren des anderen.
   */

   private InspectBCResult _innerKissPoint(
      BasicCircle2	input_basic,
      BigRational	input_distance
   )
   {
      BigRational	s1	= null;
      BigRational	s2	= null;
      Point2		p1	= null;
      Point2		p2	= null;

      if ( radius().compareTo( input_basic.radius() ) > 0 )
      {
          // input_basic liegt in this

          s2 = radius().divide( input_distance );
          p1 = centre;
          p2 = input_basic.centre;
      }
      else
      {
          // this liegt in input_basic 

          s2 = input_basic.radius().divide( input_distance );
          p1 = input_basic.centre;
          p2 = centre;
      } // if

      s1 = BigRational.valueOf( 1 ).subtract( s2 );

      InspectBCResult	result	= new InspectBCResult();

      result.points      = new Point2[ 1 ];
      result.points[ 0 ] = new Point2( s1, p1, s2, p2 );

      return ( result );

   } // _innerKissPoint


   // ************************************************************************


   /*
   * Berechnet den Schnittpunkt mit dem Eingabekreis, keiner der beiden
   * Kreise liegt im anderen. 
   */

   private InspectBCResult _outerKissPoint(
      BasicCircle2	input_basic,
      BigRational	input_distance
   )
   {
      // Seien (x1, y1) der Mittelpunkt und r1 der Radius des einen Kreises,
      // (x2, y2) der Mittelpunkt und r2 der Radius des anderen Kreises und
      // d = r1 + r2. Dann gilt fuer den Schnittpunkt (x, y):
      //
      //          r1
      // x = x1 + -- * ( x2 - x1 )
      //          d
      //
      //          r1
      // y = y1 + -- * ( y2 - y1 )
      //          d
      //
      // ausgedrueckt als Skalarprodukt:
      //
      //           r1          r1
      // p = ( 1 - -- ) * p1 + -- * p2
      //           d           d

      InspectBCResult	result	= new InspectBCResult();

      result.points = new Point2[ 1 ];

      if ( radius().signum() == 0 )
      {
         result.points[ 0 ] = new Point2( centre );
      }
      else if ( input_basic.radius().signum() == 0 )
      {
         result.points[ 0 ] = new Point2( input_basic.centre );
      }
      else
      {
	 BigRational   s2 = radius().divide( input_distance );
	 BigRational   s1 = BigRational.valueOf( 1 ).subtract( s2 );

	 result.points[ 0 ] = new Point2( s1, centre,
	 				  s2, input_basic.centre );
      } // if

      return ( result );

   } // _outerKissPoint


   // ************************************************************************


   /*
   * Berechnet die Schnittpunkte mit dem Eingabekreis. Sei der erste Kreis 
   * definiert durch x1, y1, r1 und der zweite durch x2, y2, r2. Dann kann 
   * man aus den Kreisgleichungen ableiten:
   *
   * y = m * x + b
   *
   *           x2 - x1
   * mit m = - -------
   *           y2 - y1
   *
   *         ( r1² - r2² ) + ( x2² - x1² ) + ( y2² - y1² )
   * und b = ---------------------------------------------
   *                      2 * ( y2 - y1 )
   *
   * x =  f ± sqrt( g + f² )
   *
   *         ( m * ( y1 - b ) + x1 )
   * mit f = -----------------------
   *                 1 + m²
   *
   *         r1² - x1² - y1² + b * ( 2 * y1 - b )
   * und g = ------------------------------------
   *                      1 + m²
   */

   private InspectBCResult _intersectionPoints(
      BasicCircle2	input_basic
   )
   {
      if ( centre.y.equals( input_basic.centre.y ) )
         return ( _intersectionHorizontal( input_basic ) );

      BigRational dx		= input_basic.centre.x.subtract( centre.x );
      BigRational dy		= input_basic.centre.y.subtract( centre.y );
      BigRational x1_square	= centre.x.square();
      BigRational y1_square	= centre.y.square();
      BigRational x2_square	= input_basic.centre.x.square();
      BigRational y2_square	= input_basic.centre.y.square();
      BigRational r1_square	=             squareRadius();
      BigRational r2_square	= input_basic.squareRadius();

      BigRational m = dx.divide( dy ).negate();

      BigRational b =      ( r1_square.subtract( r2_square )
		      ).add( x2_square.subtract( x1_square )
		      ).add( y2_square.subtract( y1_square )
		      ).divide( _BIG_TWO
		      ).divide( dy );

      BigRational denum	= m.square().add( BigRational.valueOf( 1 ) );

      BigRational f	= (      ( m.multiply( centre.y.subtract( b ) )
                            ).add( centre.x )
      			  ).divide( denum );

      BigRational g	= (           ( r1_square
			    ).subtract( x1_square
			    ).subtract( y1_square
			    ).add(           ( _BIG_TWO.multiply( centre.y )
				   ).subtract( b
				   ).multiply( b ) )
			  ).divide( denum );

      BigRational root	= g.add( f.square() ).sqrt( _SCALE );

      InspectBCResult	result	= new InspectBCResult();

      if ( root.signum() == 0 )
      {
         result.points = new Point2[ 1 ];
	 result.points[ 0 ] = new Point2( f, ( m.multiply( f ).add( b ) ) );
      }
      else
      {
         result.points = new Point2[ 2 ];
	 BigRational p1x = f.add(      root );
	 BigRational p2x = f.subtract( root );
	 result.points[ 0 ] = new Point2( p1x, m.multiply( p1x ).add( b ) );
	 result.points[ 1 ] = new Point2( p2x, m.multiply( p2x ).add( b ) );
      } // if

      return ( result );

   } // _intersectionPoints


   // ************************************************************************


   /*
   * Berechnet die Schnittpunkte mit dem Eingabekreis, dessen Mittelpunkt
   * die gleiche y-Koordinate hat. Sei der erste Kreis definiert durch 
   * x1, y1, r1 und der zweite durch x2, y2, r2. Dann folgt aus den 
   * Kreisgleichungen:
   *
   *     ( r1² - r2² ) + ( x2² - x1² )
   * x = -----------------------------
   *            2 * ( x2 - x1 )
   *
   * y = y1 ± sqrt( r1² - ( x - x1 )² )
   */

   private InspectBCResult _intersectionHorizontal(
      BasicCircle2	input_basic
   )
   {
      BigRational dx	    = input_basic.centre.x.subtract( centre.x );

      BigRational x1_square =             centre.x.square();
      BigRational x2_square = input_basic.centre.x.square();

      BigRational r1_square =             squareRadius();
      BigRational r2_square = input_basic.squareRadius();

      BigRational x = (            ( r1_square.subtract( r2_square )
      			      ).add( x2_square.subtract( x1_square ) )
      		      ).divide( _BIG_TWO
      		      ).divide( dx );

      BigRational l = x.subtract( centre.x );

      BigRational root = r1_square.subtract( l.square() ).sqrt( _SCALE );

      InspectBCResult	result	= new InspectBCResult();

      if ( root.signum() == 0 )
      {
         result.points = new Point2[ 1 ];
	 result.points[ 0 ] = new Point2( x, centre.y );
      }
      else
      {
         result.points = new Point2[ 2 ];
	 result.points[ 0 ] = new Point2( x, centre.y.add(      root ) );
	 result.points[ 1 ] = new Point2( x, centre.y.subtract( root ) );
      } // if

      return ( result );

   } // _intersectionHorizontal


   // ************************************************************************


} // class BasicCircle2

