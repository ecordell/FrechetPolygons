
package anja.ratgeom;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;


import anja.util.Angle;
import anja.util.BigRational;
import anja.util.GraphicsContext;
import anja.util.List;



/**
* Zweidimensionaler zeichenbarer Kreis.
*
* @version	0.2  17.11.1997
* @author	Norbert Selle
*/

public class Circle2 extends BasicCircle2
{

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


   // ************************************************************************
   // Constructors
   // ************************************************************************


   /**
   * Erzeugt einen Kreis mit dem Mittelpunkt (0, 0) und dem Radius 0.
   */

   public Circle2()
   {
      this( new Point2( 0, 0 ), 
            BigRational.valueOf( 0 ) );

   } // Circle2


   // ********************************              


   /**
   * Erzeugt einen Kreis mit den Daten des Eingabekreises.
   */

   public Circle2(
      Circle2		input_circle
   )
   {
      super( input_circle );

   } // Circle2


   // ********************************              


   /**
   * Erzeugt einen Kreis mit den Mittelpunkt und Radius Parametern,
   * der Kreis merkt sich eine <b> Referenz </b> auf den Mittelpunkt.
   *
   * @param input_centre	Mittelpunkt
   * @param input_radius	Radius
   */

   public Circle2(
      Point2		input_centre,
      BigRational	input_radius
   )
   {
      super( input_centre, input_radius );

   } // Circle2


   // ********************************              


   /**
   * Erzeugt einen Kreis mit den Mittelpunkt und Radius Parametern,
   * der Kreis merkt sich eine <b> Referenz </b> auf den Mittelpunkt.
   *
   * @param input_centre	Mittelpunkt
   * @param input_radius	Radius
   */

   public Circle2(
      Point2		input_centre,
      double		input_radius
   )
   {
      this( input_centre,
            BigRational.valueOf( input_radius ) );

   } // Circle2


   // ********************************              


   /**
   * Erzeugt einen Kreis mit den Mittelpunkt und Radius Parametern.
   *
   * @param input_centre_x	Mittelpunkt x-Koordinate
   * @param input_centre_y	Mittelpunkt y-Koordinate
   * @param input_radius	Radius
   */

   public Circle2(
      double		input_centre_x,
      double		input_centre_y,
      double		input_radius
   )
   {
      this( new Point2( input_centre_x, input_centre_y ),
            BigRational.valueOf( input_radius ) );

   } // Circle2


   // ************************************************************************


   /**
   * Erzeugt einen Kreis aus den Endpunkten eines Durchmessers, der Kreis
   * merkt sich <b> keine Referenzen </b> auf die Endpunkte.
   *
   * @param input_source	ein Durchmesserendpunkt
   * @param input_target	der andere Durchmesserendpunkt
   */

   public Circle2(
      Point2		input_source,
      Point2		input_target
   )
   {
      centre = new Point2(
         input_source.x.add( input_target.x ).divide( _BIG_TWO ),
	 input_source.y.add( input_target.y ).divide( _BIG_TWO ) );

      BigRational square_radius = centre.bigSquareDistance( input_source );
      BigRational radius        = square_radius.sqrt( _SCALE );

      // der exakte Wert des Radius zum Quadrat ist hier bekannt und wird
      // deshalb auch gesetzt, eine spaetere Berechnung aus dem - leider
      // durch das Wurzelziehen u.U. nicht exaktem - Radius waere schoen bloed

      setRadius( radius, square_radius );

   } // Circle2


   // ************************************************************************
   // Class methods
   // ************************************************************************


   // ************************************************************************
   // Public methods
   // ************************************************************************


   /**
   * Erzeugt eine Kopie.
   *
   * @return die Kopie
   */

   public Object clone()
   {
      return ( new Circle2( this ) );
       
   } // clone



   // ************************************************************************


   /**
   * Gibt den Durchmesser zurueck.
   */

   public float diameter()
   {
      return ( radius().multiply( _BIG_TWO ).floatValue() );

   } // diameter


   // ************************************************************************


   /**
   * Zeichnet den Kreis.<br>
   * <b> !!NS Ist noch nicht fertig, die Liniendicke wird missachtet.</b>
   */

   public void draw(
       Graphics2D	g,
       GraphicsContext	gc
   )
   {
      toGeom().draw( g, gc );

   } // draw


   // ************************************************************************


   /**
   * Gibt das umschliessende Rechteck zurueck.
   */

   public Rectangle2D getBoundingRect()
   {
      float	f_radius   = radius().floatValue();
      float	f_diameter = f_radius * 2;

      return( new Rectangle2D.Float(
         centre.x.floatValue() - f_radius,	// x
	 centre.y.floatValue() - f_radius,	// y
	 f_diameter,				// width
	 f_diameter ) ); 			// height

   } // getBoundingRect


   // ************************************************************************


   /**
   * Berechnet die Schnittmenge der Kreisumfaenge. Der Rueckgabeparameter 
   * Intersection kann leer sein oder einen Punkt, eine Kopie von this 
   * (wenn die Kreise aufeinanderliegen) oder eine Liste von zwei Punkten 
   * enthalten. Ist es ein Punkt, so ist er ausserdem das Funktionsergebnis, 
   * sonst ist das Funktionsergebnis null.
   */

   public Point2 intersection(
      Circle2		input_circle,
      Intersection	inout_intersection
   )
   {
      InspectBCResult	inspect	= inspectBasicCircle( input_circle );

      if ( inspect == null )
      {
         inout_intersection.set();
      }
      else if ( inspect.lies_on )
      {
         inout_intersection.set( ( Circle2 ) this.clone() );
      }
      else if ( inspect.points.length == 1 )
      {
         inout_intersection.set( inspect.points[ 0 ]);
      }
      else if ( inspect.points.length == 2 )
      {
         List	list	= new List();
	 list.add( inspect.points[ 0 ] );
	 list.add( inspect.points[ 1 ] );
	 inout_intersection.set( list );
      } // if

      return ( inout_intersection.point2 );

   } // intersection


   // ************************************************************************


   /**
   * Testet ob der Kreis das Rechteck schneidet.
   *
   * @param  input_box	Das zu testende Rechteck
   *
   * @return true wenn ja, sonst false
   */

   public boolean intersects(
      Rectangle2D	input_box
   )
   {
      if ( input_box.contains( centre.x.floatValue(),
      			       centre.y.floatValue() ) 
	 )
         return ( true );	// Mittelpunkt in input_box

      if ( ! getBoundingRect().intersects( input_box ) )
         return( false );	// Das kreisumschliessende Rechteck
	 			// schneidet input_box nicht

      // das kreisumschliessende Rechteck schneidet input_box, deshalb
      // wird getestet ob auch der Kreis input_box schneidet,
      // Bedingung: die Distanz mind. einer input_box-Kante zu centre
      //	    ist kleiner gleich dem Radius

      float			square_radius = squareRadius().floatValue();
      anja.geom.Point2		gp  = centre.toGeom();
      anja.geom.Rectangle2	box = new anja.geom.Rectangle2( input_box );

      return (    ( box.top(   ).squareDistance( gp ) <= square_radius )
               || ( box.bottom().squareDistance( gp ) <= square_radius )
               || ( box.left(  ).squareDistance( gp ) <= square_radius )
               || ( box.right( ).squareDistance( gp ) <= square_radius )
	     );

   } // intersects


   // ************************************************************************


   /**
   * Berechnet den Umfang.
   */

   public double len()
   {
      return ( Math.abs( radius().doubleValue() * Angle.PI_MUL_2 ) );

   } // len


   // ************************************************************************


   /**
   * Testet ob der Punkt auf dem Kreisumfang liegt.
   *
   * @return true wenn ja, sonst false
   */

   public boolean liesOn(
      Point2	point
   )
   {
      return (
         centre.bigSquareDistance( point ).compareTo( squareRadius() ) == 0
      );

   } // liesOn


   // ************************************************************************


   /** Erzeugt einen anja.geom.Circle2 und gibt ihn zurueck.
   */

   public anja.geom.Circle2 toGeom()
   {
      return (
         new anja.geom.Circle2( centre.toGeom(), radius().floatValue() )
      );

   } // toGeom


   // ************************************************************************


   /**
   * Erzeugt eine textuelle Repraesentation und gibt sie zurueck.
   */

   public String toString()
   {
      return (
         new String( "(" + centre + ", " + radius().doubleValue() + ")" )
      );

   } // toString


   // ************************************************************************
   // Private methods
   // ************************************************************************


} // class Circle2

