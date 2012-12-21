package anja.ratgeom;

import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.util.NoSuchElementException;

import anja.util.*;

/**
* Point2List ist eine Liste von zweidimensionalen sortierbaren Punkten. 
* Mit PointsAccess-Objekten kann ohne Castings auf die Punkte zugegriffen 
* werden.
* Die Point2List ist <b> nicht </b> zeichenbar, zum Zeichnen dient 
* gui.Point2Module.
*
* @version	0.3 19.12.1997
* @author	Norbert Selle
* 
* @see		anja.ratgeom.PointsAccess
*/


public class Point2List implements Cloneable
{

   // ************************************************************************
   // Constants
   // ************************************************************************


   // ************************************************************************
   // Variables
   // ************************************************************************


   /** Standard- GraphicsContext fuer die Punkte */
   protected GraphicsContext 	_point_context;

   /** die Punkte */
   protected SimpleList		_points;


   // ************************************************************************
   // Constructors
   // ************************************************************************


   /*
   * Erzeugt _points und initialisiert sie mit Kopien der Punkte 
   * von input_points, ist input_points leer oder gleich null, so
   * bleibt _points leer.
   */
   private void _init(
      SimpleList	input_points
   )
   {
      _points = new SimpleList();

      if ( input_points != null )
      {
	 PointsAccess	origs = new PointsAccess( input_points );

	 while( origs.hasNextPoint() )
	 {
	    Point2	orig_point	= origs.nextPoint();
	    Point2	new_point	= ( Point2 ) orig_point.clone();

	    addPoint( new_point );
	 } // while
      } // if

   } // _init


   // ********************************


   /**
   * Erzeugt eine leere Punktliste.
   */
   public Point2List()
   {
      _init( null );

   } // Point2List


   // ********************************


   /**
   * Erzeugt eine neue Punktliste als Kopie der Eingabeliste, die Punkte
   * der neuen Liste sind <B>Kopien</B> - nicht etwa Referenzen - der 
   * Punkte der Eingabeliste.
   *
   * @param input_points	Eingabeliste
   */
   public Point2List(
      Point2List	input_points
   )
   {
      _init( input_points._points );

   } // Point2List


   // ********************************


   /**
   * Erzeugt eine neue Punktliste als skalierte Kopie der Eingabeliste.
   * Die Skalierung mit dem Faktor s erfolgt bezueglich des eingegebenen
   * Punktes p.
   *
   * @param input_points	Eingabeliste
   */
   public Point2List(
      double s,
      Point2List	input_points,
      Point2 p
   )
   {
      _points = new SimpleList();

      PointsAccess origs = new PointsAccess( input_points );

      while( origs.hasNextPoint() )
	 addPoint( new Point2( 1-s, p, s, origs.nextPoint() ) );
      
   } // Point2List


   // ************************************************************************
   // Class methods
   // ************************************************************************


   /**
   * Erzeugt String mit Punktliste.
   */

   static public String listPoints(
      SimpleList	input_list
   )
   {
      StringBuffer ret = new StringBuffer();
      PointsAccess pt = new PointsAccess(input_list);
      while(pt.hasNextPoint()) {
         Point2      actual_point = pt.nextPoint();

         ret.append(actual_point.toString());
         if(pt.hasNextPoint())
            ret.append(" | ");

      } // while
      return ret.toString();

   } // listPoints


   // ************************************************************************
   // Public methods
   // ************************************************************************


   /**
   * Setzt den Standard Graphics-Context fuer Punkte
   * @param context Der GraphicsContext
   * @see anja.util.GraphicsContext
   */

   public synchronized void setPointContext(GraphicsContext context) {
      _point_context = context;
   }


   // ************************************************************************


   /**
   * liefert den Standard-GraphicsContext fuer Punkte
   */

   public GraphicsContext getPointContext() {
      return(_point_context);
   }


   // ************************************************************************


   /**
   * Haengt eine <B>Referenz</B> auf den Eingabepunkt an das Listenende.
   * <BR><B>Vorbedingungen:</B>
   * <BR>der Eingabepunkt ist nicht null
   *
   * @param input_point		Eingabepunkt
   */
   public synchronized void addPoint(
      Point2		input_point
   )
   {
      _points.add( input_point );

   } // addPoint


   // ********************************


   /**
   * Haengt einen neuen Punkt mit den Eingabekoordinaten an das Listenende.
   *
   * @param input_x	x-Koordinate
   * @param input_y	y-Koordinate
   */
   public synchronized void addPoint(
      double		input_x,
      double		input_y
   )
   {
      Point2		new_point	= new Point2( input_x, input_y );

      _points.add( new_point );

   } // addPoint


   // ************************************************************************


   /**
   * Haengt <B>Referenzen</B> auf die Punkte der Eingabeliste sukzessive 
   * an das Listenende.
   *
   * @param input_list	Eingabeliste der anzuhaengenden Point2-Objekte
   */
   public synchronized void appendCopy(
      SimpleList	input_list
   )
   {
      SimpleList	new_points = ( SimpleList ) input_list.clone();

      concat( new_points );

   } // appendCopy

 
   // ********************************


   /**
   * Haengt <B>Referenzen</B> auf die Punkte der Eingabepunktliste sukzessive 
   * an das Listenende.
   *
   * @param input_points	Eingabepunktliste der anzuhaengenden Punkte
   */
   public synchronized void appendCopy(
      Point2List	input_points
   )
   {
      appendCopy( input_points.points() );

   } // appendCopy

 
   // ************************************************************************


   /**
   * Kopiert die Punktliste, die Punkte der Kopie sind <B>Kopien</B> - nicht 
   * etwa Referenzen - der Punkte des Originals.
   *
   * @return Kopie der Punktliste
   */
   public synchronized Object clone()
   {
      Point2List	output_points	= new Point2List( this );

      return( output_points );

   } // clone


   // ************************************************************************


   /**
   * Haengt die Elemente der Eingabeliste - die von der Klasse Point2 sein
   * muessen - sukzessive um an das Listenende, die Eingabeliste ist danach
   * leer.
   *
   * @param input_list	Eingabeliste der umzuhaengenden Point2-Objekte
   */
   public synchronized void concat(
      SimpleList	input_list
   )
   {
      _points.concat( input_list );

   } // concat


   // ********************************


   /**
   * Haengt die Punkte der Eingabepunktliste sukzessive um an das Listenende,
   * die Eingabepunktliste ist danach leer.
   *
   * @param input_points	Eingabepunktliste der umzuhaengenden Punkte
   */
   public synchronized void concat(
      Point2List	input_points
   )
   {
      concat( input_points.points() );

   } // concat


   // ************************************************************************


   /**
   * Zeichnet die Punkte.
   */
   public void draw(
      Graphics2D 		g, 
      GraphicsContext 	gc
   )
   { //Bloss keine Punkte malen !!! Das wird bereits in Point2Module erledigt.
   } // draw


   // ************************************************************************


   /**
   * Testet ob die Punktliste leer ist.
   *
   * @return true wenn die Punktliste leer ist, sonst false
   */
   public boolean empty()
   {
      return( _points.empty() );

   } // empty


   // ************************************************************************


   /**
   * Gibt den ersten Punkt zurueck, null wenn die Liste leer ist.
   */
   public Point2 firstPoint()
   {
      if ( length() > 0 )
      {
         return( ( Point2 ) _points.firstValue() );
      }
      else
      {
         return( null );
      } // if

   } // firstPoint


   // ************************************************************************


   /**
   * Gibt das umschliessende Rechteck zurueck, null wenn die Punktliste leer
   * ist.
   *
   * @return das umschliessende Rechteck oder null
   */

   public Rectangle2D getBoundingRect()
   {
      Rectangle2D	output_rectangle	= null;

      if ( ! _points.empty() )
      {
	 float	x	= ( float ) minimumX();
	 float	y	= ( float ) minimumY();
	 float	width	= ( float ) maximumX() - x;
	 float	height	= ( float ) maximumY() - y;

	 output_rectangle = new Rectangle2D.Float( x, y, width, height );
      } // if

      return ( output_rectangle );

   } // getBoundingRect


   // ************************************************************************


   /**
   * Fuegt eine <B> Referenz </B> auf den dritten Eingabepunkt zwischen die 
   * ersten beiden Eingabepunkte ein, letztere muessen <em> Kopien </em> oder 
   * <em> Referenzen </em> von aufeinanderfolgenden Punkten oder dem ersten
   * und dem letzten Punkt der Punktliste in beliebiger Reihenfolge enthalten,
   * und die Punktliste muss mindestens zwei Punkte enthalten, ansonsten ist
   * das Resultat der Funktion undefiniert.
   *
   * @param input_first	  einer der Punkte zwischen die eingefuegt wird
   * @param input_second  der andere der Punkte zwischen die eingefuegt wird
   * @param input_insert  der einzufuegende Punkt
   */
   public synchronized void insert(
      Point2		input_first,
      Point2		input_second,
      Point2		input_insert
   )
   {
      if ( _points.length() >= 2 )
      {
	 ListItem	current_item	= _points.first();
	 ListItem	next_item;
	 Point2		current_point;
	 Point2		next_point;

	 for ( int count = 0;
	           count <  _points.length();
		   count++ )
	 {
	    next_item = _points.cyclicRelative( current_item, 1 );

	    current_point = ( Point2 ) current_item.value();

	    if ( current_point.equals( input_first ) )
	    {
	       next_point = ( Point2 ) next_item.value();
	       if ( next_point.equals( input_second ) )
	       {
	          _points.insert( next_item, input_insert );
	       }
	       else
	       {
	          _points.insert( current_item, input_insert );
	       } // if
	       
	       return;
	    } // if

	    current_item = next_item;

	 } // while
      } // if

   } // insert

   // ************************************************************************


   /**
   * Fuegt eine <B> Referenz </B> auf den Eingabepunkt am Listenanfang ein.
   * <BR> <B> Vorbedingungen: </B>
   * <BR> der Eingabepunkt ist nicht null
   *
   * @param input_point		der einzufuegende Punkt
   */
   public void insertFront(
      Point2		input_point
   )
   {
      _points.Push( input_point );

   } // insertFront


   // ********************************


   /**
   * Fuegt einen neuen Punkt mit den Eingabekoordinaten am Listenanfang ein.
   *
   * @param input_x	x-Koordinate
   * @param input_y	y-Koordinate
   */
   public void insertFront(
      double		input_x,
      double		input_y
   )
   {
      Point2		new_point	= new Point2( input_x, input_y );

      _points.Push( new_point );

   } // insertFront


   // ************************************************************************


   /**
   * Gibt den letzten Punkt zurueck, null wenn die Liste leer ist.
   */
   public Point2 lastPoint()
   {
      if ( length() > 0 )
      {
         return( ( Point2 ) _points.lastValue() );
      }
      else
      {
         return( null );
      } // if

   } // lastPoint


   // ************************************************************************


   /**
   * Gibt die Anzahl der Punkte zurueck.
   *
   * @return Punkt-Anzahl
   */
   public int length()
   {
      return( _points.length() );

   } // length


   // ************************************************************************


   /**
   * Gibt die groesste X-Koordinate der Punkte zurueck.
   */

   public double maximumX() throws NoSuchElementException
   {
      if ( _points.empty() )
      {
         throw new NoSuchElementException();
      } // if

      PointComparitor	compare	= new PointComparitor();

      compare.setOrder( PointComparitor.X_ORDER );

      return ( ( ( Point2 ) _points.max( compare ).value() ).x.doubleValue() );

   } // maximumX()


   // ************************************************************************


   /**
   * Gibt die kleinste X-Koordinate der Punkte zurueck.
   */

   public double minimumX() throws NoSuchElementException
   {
      if ( _points.empty() )
      {
         throw new NoSuchElementException();
      } // if

      PointComparitor	compare	= new PointComparitor();

      compare.setOrder( PointComparitor.X_ORDER );

      return ( ( ( Point2 ) _points.min( compare ).value() ).x.doubleValue() );

   } // minimumX()


   // ************************************************************************


   /**
   * Gibt die groesste Y-Koordinate der Punkte zurueck.
   */

   public double maximumY() throws NoSuchElementException
   {
      if ( _points.empty() )
      {
         throw new NoSuchElementException();
      } // if

      PointComparitor	compare	= new PointComparitor();

      compare.setOrder( PointComparitor.Y_ORDER );

      return ( ( ( Point2 ) _points.max( compare ).value() ).y.doubleValue() );

   } // maximumY()


   // ************************************************************************


   /**
   * Gibt die kleinste Y-Koordinate der Punkte zurueck.
   */

   public double minimumY() throws NoSuchElementException
   {
      if ( _points.empty() )
      {
         throw new NoSuchElementException();
      } // if

      PointComparitor	compare	= new PointComparitor();

      compare.setOrder( PointComparitor.Y_ORDER );

      return ( ( ( Point2 ) _points.min( compare ).value() ).y.doubleValue() );

   } // minimumY()


   // ************************************************************************


   /**
   * Gibt eine <B>Referenz</B> auf die Liste der Punkte zurueck.
   */
   public SimpleList points()
   {
      return( _points );

   } // points


   // ************************************************************************


   /**
   * Loescht den ersten Punkt.
   * <BR><B>Vorbedingungen:</B>
   * <BR>die Punktliste ist nicht leer
   */
   public void removeFirstPoint()
   {
      if ( _points.empty() )
      {
         throw new NoSuchElementException();
      }
      else
      {
         _points.remove( _points.first() );
      } // if

   } // removeFirstPoint


   // ************************************************************************


   /**
   * Loescht den letzten Punkt.
   * <BR><B>Vorbedingungen:</B>
   * <BR>die Punktliste ist nicht leer
   */
   public void removeLastPoint()
   {
      if ( _points.empty() )
      {
         throw new NoSuchElementException();
      }
      else
      {
         _points.remove( _points.last() );
      } // if

   } // removeLastPoint


   // ************************************************************************


   /**
   * Sortiert die Punkte der Liste entsprechend des eingegebenen 
   * Vergleichskriteriums. Die Reihenfolge ist entweder Sorter.ASCENDING
   * fuer aufsteigend oder Sorter.DESCENDING fuer absteigend.
   *
   * @param	input_comparitor	Vergleichskriterium
   * @param	input_order		Reihenfolge
   *
   * @see anja.geom.PointComparitor
   * @see anja.util.Sorter#ASCENDING
   * @see anja.util.Sorter#DESCENDING
   */
   public void sort(
      PointComparitor	input_comparitor,
      byte		input_order
   )
   {
      _points.sort( input_comparitor, input_order );

   } // sort


   // ************************************************************************


   /**
   * Testet ob ein oder mehrere Punkte im Rechteck enthalten sind.
   *
   * @param  box	Das zu testende Rechteck
   * @return true wenn ja, sonst false
   */
   public boolean intersects(
      Rectangle2D	box
   )
   {
      PointsAccess	access	= new PointsAccess( _points );

      while ( access.hasNextPoint() )
      {
	 if ( access.nextPoint().intersects( box ) )
	 {
	    return ( true );
	 } // if
      } // while

      return ( false );

   } // intersects


   // ************************************************************************


   /**
   * Erzeugt eine anja.geom.Point2List aus der Punktliste und gibt sie nach
   * Object gecastet zurueck.
   */

   public Object toGeom()
   {
      anja.geom.Point2List	geom_list = new anja.geom.Point2List();
      PointsAccess		access = new PointsAccess( this );

      while ( access.hasNextPoint() )
      {
         geom_list.addPoint( access.nextPoint().toGeom() );
      } // while

      return ( geom_list );

   } // toGeom


   // ************************************************************************


   /**
   * Erzeugt eine textuelle Repraesentation.
   *
   * @return die textuelle Repraesentation
   */
   public String toString()
   {
      return (   "Point2List <" + _points.length()
               + "> [" + listPoints()
               + "]"
	     );

   } // toString


   // ************************************************************************


   /**
   * Erzeugt String mit Punktliste.
   */
   public String listPoints()
   {
      return ( listPoints( _points ) );

   } // listPoints


   // ************************************************************************


   /**
   * Verschiebung um die Eingabewerte.
   *
   * @param input_horizontal	horizontale Verschiebung
   * @param input_vertical	vertikale Verschiebung
   */
   public synchronized void translate(
      double	input_horizontal,
      double	input_vertical
   )
   {
      translate( new Point2( input_horizontal, input_vertical ) );

   } // translate


   // ********************************


   /**
   * Verschiebung um den Vektor vom Nullpunkt zum Eingabepunkt.
   * <BR><B>Vorbedingungen:</B>
   * <BR>der Eingabepunkt ist nicht null
   *
   * @param input_vector	Eingabepunkt
   */
   public synchronized void translate(
      Point2		input_vector
   )
   {
      PointsAccess	the_points = new PointsAccess( _points );

      while( the_points.hasNextPoint() )
      {
         the_points.nextPoint().translate( input_vector );
      } // while

   } // translate

   // ************************************************************************

   /**
   * Berechne den Punkt aus der Point2List, der dem Argument p am naechsten ist.
   */  
   public Point2 closestPoint(Point2 p)
   {
      return closestPointAccess(p).currentPoint();
   } // closestPoint

   // ************************************************************************

   /**
   * Berechne den Punkt aus der Point2List, der dem Argument p am naechsten ist,
   * aber nur, wenn es ueberhaupt einen gibt, der naeher als maxdist dran ist.
   */  
   public Point2 closestPoint(Point2 p, double maxdist)
   {
      return closestPointAccess(p,maxdist).currentPoint();
   } // closestPoint

   // ************************************************************************

   /**
   * Berechne den Punkt aus der Point2List, der dem Argument p am naechsten ist,
   * abgesehen von dem Punkt ex,
   * aber nur, wenn es ueberhaupt einen gibt, der naeher als maxdist dran ist.
   */  
   public Point2 closestPointExcept(Point2 p, Point2 ex, double maxdist)
   {
      return closestPointExceptAccess(p,ex,maxdist).currentPoint();
   } // closestPointExcept

   // ************************************************************************

   /**
   * wie closestPoint(Point2 p), aber liefert einen PointsAccess zurueck.
   */  
   public PointsAccess closestPointAccess(Point2 p)
   {
      PointsAccess Access = new PointsAccess(this);
      if ( empty() ) return Access;

      Point2 q = Access.nextPoint();
      PointsAccess rA = new PointsAccess(Access);
      double dist2, distcomp = p.squareDistance(q);

      while ( Access.hasNextPoint() ) {
         q = Access.nextPoint();
         dist2 = p.squareDistance(q);
         if ( dist2 < distcomp ) 
         {  
            rA = new PointsAccess(Access);
            distcomp = dist2;
         }
      }    
      return rA;  
   } // closestPointAccess

   // ************************************************************************

   /**
   * wie closestPoint(Point2 p, double maxdist),
   * aber liefert einen PointsAccess zurueck.
   */
   public PointsAccess closestPointAccess(Point2 p, double maxdist)
   {
      PointsAccess Access = new PointsAccess(this);
      if ( empty() ) return Access;

      Point2 q = null;
      PointsAccess rA = new PointsAccess(Access);
      double dist2, distcomp = maxdist*maxdist;

      while ( Access.hasNextPoint() ) {
         q = Access.nextPoint();
         dist2 = p.squareDistance(q);
         if ( dist2 < distcomp ) 
         {  
            rA = new PointsAccess(Access);
            distcomp = dist2;
         }
      }    
      return rA;  
   } // closestPointAccess

   // ************************************************************************

   /**
   * wie closestPointExcept(Point2 p, Point2 ex, double maxdist),
   * aber liefert einen PointsAccess zurueck.
   */  
   public PointsAccess closestPointExceptAccess(Point2 p, Point2 ex, double maxdist)
   {
      PointsAccess Access = new PointsAccess(this);
      if ( empty() ) return Access;

      Point2 q = null;
      PointsAccess rA = new PointsAccess(Access);
      double dist2, distcomp = maxdist*maxdist;

      while ( Access.hasNextPoint() ) {
         q = Access.nextPoint();
         dist2 = p.squareDistance(q);
         if ( (dist2 < distcomp) && (q != ex) ) 
         {  
            rA = new PointsAccess(Access);
            distcomp = dist2;
         }
      }    
      return rA;  
   } // closestPointExceptAccess

   // ************************************************************************

} // class Point2List

