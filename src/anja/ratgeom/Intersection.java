
package anja.ratgeom;


import anja.util.SimpleList;
import anja.util.ListItem;


/**
* Intersection dient zur Rueckgabe von Ergebnissen von Tests auf Schnitt.
* Die Variable <em> result </em> wird von den set()-Methoden automatisch 
* gesetzt und enthaelt die Art der Schnittmenge wie EMPTY, LIST, POINT2 usw.,
* entsprechend ihres Inhalts ist eine der Variablen <em> list, point2, 
* segment2 </em> etc. belegt, die anderen sind auf null gesetzt. Ist zum 
* Beispiel <em> result </em> gleich POINT2, so ist <em> point2 </em> belegt.
* <br>
* Die set()-Methoden speichern <b> Referenzen </b> auf ihre Eingabeparameter,
* im Unterschied dazu erzeugt clone() <b> Kopien </b> der belegten Variablen.
*
* @version	1.0 24.09.1997
* @author	Norbert Selle
*/

public class Intersection implements Cloneable
{

   // ************************************************************************
   // Constants
   // ************************************************************************


   /** die Schnittmenge ist leer			*/
   public final static int	EMPTY		= 1450;

   /** 
   * die Schnittmenge ist eine Liste von Objekten der Klassen
   * Arc2, Circle2, Point2, Line2, Ray2 und Segment2
   */
   public final static int	LIST		= 1451;

   /** die Schnittmenge ist ein Arc2			*/
   public final static int	ARC2		= 1452;

   /** die Schnittmenge ist ein Circle2			*/
   public final static int	CIRCLE2		= 1453;

   /** die Schnittmenge ist ein Point2			*/
   public final static int	POINT2		= 1454;

   /** die Schnittmenge ist eine Line2			*/
   public final static int	LINE2		= 1455;

   /** die Schnittmenge ist ein Ray2			*/
   public final static int	RAY2		= 1456;

   /** die Schnittmenge ist ein Segment2		*/
   public final static int	SEGMENT2	= 1457;


   // ************************************************************************
   // Variables
   // ************************************************************************


   /** 
   * Ergebnisart der Schnittmengenberechnung, enthaelt eine der Konstanten 
   * EMPTY, LIST, POINT2, etc.
   */
   public int			result;

   /** Liste von Objekten 		*/
   public SimpleList		list;

   /** Schnittpunkt 			*/
   public Arc2			arc2;

   /** Schnittpunkt 			*/
   public Circle2		circle2;

   /** Schnittpunkt 			*/
   public Point2		point2;

   /** Schnittsegment 			*/
   public Segment2		segment2;

   /** Schnittstrahl 			*/
   public Ray2			ray2;

   /** Schnittgerade 			*/
   public Line2			line2;


   // ************************************************************************
   // Constructors
   // ************************************************************************


   /**
   * Setzt result auf EMPTY.
   */

   public Intersection()
   {
      result = EMPTY;

   } // Intersection


   // ********************************              


   /**
   * Erzeugt eine Kopie der Eingabeintersection mit <b>Kopien</b>
   * - nicht Referenzen - ihrer Elemente.
   */

   public Intersection(
      Intersection	input_intersection
   )
   {
      result		= input_intersection.result;

      if ( input_intersection.arc2 != null )
      {
	 arc2		= ( Arc2 ) input_intersection.arc2.clone();
      } // if

      if ( input_intersection.circle2 != null )
      {
	 circle2	= ( Circle2 ) input_intersection.circle2.clone();
      } // if

      if ( input_intersection.point2 != null )
      {
	 point2		= ( Point2 ) input_intersection.point2.clone();
      } // if

      if ( input_intersection.list != null )
      {
	 list		= _cloneList( input_intersection.list );
      } // if

      if ( input_intersection.segment2 != null )
      {
	 segment2	= ( Segment2 ) input_intersection.segment2.clone();
      } // if

      if ( input_intersection.ray2 != null )
      {
	 ray2		= ( Ray2 ) input_intersection.ray2.clone();
      } // if

      if ( input_intersection.line2 != null )
      {
	 line2		= ( Line2 ) input_intersection.line2.clone();
      } // if

   } // Intersection


   // ************************************************************************
   // Class methods
   // ************************************************************************


   // ************************************************************************
   // Public methods
   // ************************************************************************


   /**
   * Erzeugt eine Kopie.
   *
   * @return Kopie
   */

   public Object clone()
   {
      return( new Intersection ( this ) );

   } // clone


   // ************************************************************************


   /**
   * Setzt result auf EMPTY und loescht die anderen Variablen.
   */

   public void set()
   {
      _emptyObjects();
      result = EMPTY;

   } // set


   // ********************************              


   /**
   * Setzt result auf LINE2 und line2 auf die Eingabegerade, die anderen
   * Variablen werden geloescht.
   */

   public void set(
      Line2		input_line
   )
   {
      _emptyObjects();
      result	= LINE2;
      line2	= input_line;

   } // set


   // ********************************              


   /**
   * Setzt result auf LIST und list auf die Eingabeliste, die nur Objekte 
   * der Klassen Arc2, Circle2, Point2, Line2, Ray2, Segment2 enthalten
   * darf, die anderen Variablen werden geloescht.
   */

   public void set(
      SimpleList	input_list
   )
   {
      _emptyObjects();
      result	= LIST;
      list	= input_list;

   } // set


   // ********************************              

   
   /**
   * Setzt result auf ARC2 und arc2 auf den Eingabekreisbogen, die anderen
   * Variablen werden geloescht.
   */

   public void set(
      Arc2		input_arc
   )
   {
      _emptyObjects();
      result	= ARC2;
      arc2	= input_arc;

   } // set


   // ********************************              

   
   /**
   * Setzt result auf CIRCLE2 und circle2 auf den Eingabekreis, die anderen
   * Variablen werden geloescht.
   */

   public void set(
      Circle2		input_circle
   )
   {
      _emptyObjects();
      result	= CIRCLE2;
      circle2	= input_circle;

   } // set


   // ********************************              

   
   /**
   * Setzt result auf POINT2 und point2 auf den Eingabepunkt, die anderen
   * Variablen werden geloescht.
   */

   public void set(
      Point2		input_point
   )
   {
      _emptyObjects();
      result	= POINT2;
      point2	= input_point;

   } // set


   // ********************************              


   /**
   * Setzt result auf RAY2 und ray2 auf den Eingabestrahl, die anderen
   * Variablen werden geloescht.
   */

   public void set(
      Ray2		input_ray
   )
   {
      _emptyObjects();
      result	= RAY2;
      ray2	= input_ray;

   } // set


   // ********************************              


   /**
   * Setzt result auf SEGMENT2 und segment2 auf das Eingabesegment, die 
   * anderen Variablen werden geloescht.
   */

   public void set(
      Segment2		input_segment
   )
   {
      _emptyObjects();
      result	= SEGMENT2;
      segment2	= input_segment;

   } // set


   // ************************************************************************


   /**
   * Erzeugt eine textuelle Repraesentation und gibt sie zurueck.
   */

   public String toString()
   {
      StringBuffer	buffer	= new StringBuffer( _resultToString() );

      if ( list != null )
      {
         buffer.append( " List " + list );
      } // if

      if ( arc2 != null )
      {
         buffer.append( " Arc2 " + arc2 );
      } // if

      if ( circle2 != null )
      {
         buffer.append( " Circle2 " + circle2 );
      } // if

      if ( point2 != null )
      {
         buffer.append( " Point2 " + point2 );
      } // if

      if ( segment2 != null )
      {
         buffer.append( " Segment2 " + segment2 );
      } // if

      if ( ray2 != null )
      {
         buffer.append( " Ray2 " + ray2 );
      } // if

      if ( line2 != null )
      {
         buffer.append( " Line2 " + line2 );
      } // if

      return( buffer.toString() );

   } // toString


   // ************************************************************************
   // Private methods
   // ************************************************************************


   /*
   * Erzeugt eine Kopie der Eingabeliste. Ihre Elemente werden kopiert,
   * nicht referenziert. Erlaubte Inhalte der ListItem sind:
   *	Arc2
   *	Circle2
   *    Point2
   *	Segment2
   *	Ray2
   *	Line2
   */

   private SimpleList _cloneList(
      SimpleList	input_list
   )
   {
      SimpleList	new_list	= new SimpleList();
      ListItem 		orig_item	= input_list.first();
      Object		orig_object;

      // Listenelemente kopieren (nicht referenzieren)

      while ( orig_item != null )
      {
	 orig_object	= orig_item.value();

	 if ( orig_object instanceof Arc2 )
	 {
	    new_list.add( ( ( Arc2 ) orig_object ).clone() );
	 }
	 else if ( orig_object instanceof Circle2 )
	 {
	    new_list.add( ( ( Circle2 ) orig_object ).clone() );
	 }
	 else if ( orig_object instanceof Point2 )
	 {
	    new_list.add( ( ( Point2 ) orig_object ).clone() );
	 }
	 else if ( orig_object instanceof Segment2 )
	 {
	    new_list.add( ( ( Segment2 ) orig_object ).clone() );
	 }
	 else if ( orig_object instanceof Ray2 )
	 {
	    new_list.add( ( ( Ray2 ) orig_object ).clone() );
	 }
	 else if ( orig_object instanceof Line2 )
	 {
	    new_list.add( ( ( Line2 ) orig_object ).clone() );
	 }
	 else
	 {
	    System.err.println( 
	         "Intersection._cloneList error: can't copy unkown object"
	       + " List: " + input_list );
	 } // if

	 orig_item = orig_item.next();
      } // while

      return ( new_list );

   } // _cloneList


   // ************************************************************************


   /*
   * Loescht die Objekt-Referenzen.
   */

   private void _emptyObjects()
   {
      list	= null;
      arc2	= null;
      circle2	= null;
      point2	= null;
      segment2	= null;
      ray2	= null;
      line2	= null;

   } // _emptyObjects


   // ************************************************************************


   /*
   * Erzeugt eine textuelle Repraesentation von result und gibt sie zurueck.
   */

   private String _resultToString()
   {
      String	output_string;

      switch ( result ) {
      case EMPTY:
	 output_string = new String( "EMPTY" );
         break;
      case LIST:
	 output_string = new String( "LIST" );
         break;
      case ARC2:
	 output_string = new String( "ARC2" );
         break;
      case CIRCLE2:
	 output_string = new String( "CIRCLE2" );
         break;
      case POINT2:
	 output_string = new String( "POINT2" );
         break;
      case SEGMENT2:
	 output_string = new String( "SEGMENT2" );
         break;
      case RAY2:
	 output_string = new String( "RAY2" );
         break;
      case LINE2:
	 output_string = new String( "LINE2" );
         break;
      default:
	 output_string = new String( "Unknown result" );
         break;
      } // switch

      return ( output_string );

   } // _resultToString


   // ************************************************************************

} // class Intersection


