package anja.geom;


import anja.util.ListItem;
import anja.util.SimpleList;


/**
 * Intersection dient zur Rueckgabe von Ergebnissen von Tests auf Schnitt. Die
 * Variable <em>result</em> wird von den set()-Methoden automatisch gesetzt und
 * enthaelt die Art der Schnittmenge wie EMPTY, LIST, POINT2 usw., entsprechend
 * ihres Inhalts ist eine der Variablen <em>list, point2, segment2</em> etc.
 * belegt, die anderen sind auf null gesetzt. Ist zum Beispiel <em>result</em>
 * gleich POINT2, so ist <em>point2</em> belegt. <br>Die set()-Methoden
 * speichern <b>Referenzen</b> auf ihre Eingabeparameter, im Unterschied dazu
 * erzeugt clone() <b>Kopien</b> der belegten Variablen.
 * 
 * @version 1.3 18.07.1997
 * @author Norbert Selle
 */

public class Intersection
		implements Cloneable, java.io.Serializable
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	/**
	 * Die Schnittmenge ist leer
	 */
	public final static int	EMPTY		= 1450;

	/**
	 * Die Schnittmenge ist eine Liste von Objekten der Klassen Arc2, Circle2,
	 * Point2, Line2, Ray2 und Segment2
	 */
	public final static int	LIST		= 1451;

	/**
	 * Die Schnittmenge ist ein Arc2
	 */
	public final static int	ARC2		= 1452;

	/**
	 * Die Schnittmenge ist ein Circle2
	 */
	public final static int	CIRCLE2		= 1453;

	/**
	 * Die Schnittmenge ist ein Point2
	 */
	public final static int	POINT2		= 1454;

	/**
	 * Die Schnittmenge ist eine Line2
	 */
	public final static int	LINE2		= 1455;

	/**
	 * Die Schnittmenge ist ein Ray2
	 */
	public final static int	RAY2		= 1456;

	/**
	 * Die Schnittmenge ist ein Segment2
	 */
	public final static int	SEGMENT2	= 1457;

	// ************************************************************************
	// Variables
	// ************************************************************************

	/**
	 * Ergebnisart der Schnittmengenberechnung, enthaelt eine der Konstanten
	 * EMPTY, LIST, POINT2, etc.
	 */
	public int				result;

	/**
	 * Liste von Objekten
	 */
	public SimpleList		list;

	/**
	 * Schnittpunkt in Form eines Arc2-Objektes
	 */
	public Arc2				arc2;

	/**
	 * Schnittpunkt in Form eines Circle2-Objektes (Kreis)
	 */
	public Circle2			circle2;

	/**
	 * Schnittpunkt in Form eines Point2-Objektes (Punkt)
	 */
	public Point2			point2;

	/**
	 * Schnittsegment in Form einer Segment2-Objektes (Strecke)
	 */
	public Segment2			segment2;

	/**
	 * Schnittstrahl in Form eines Ray2-Objektes (Strahl)
	 */
	public Ray2				ray2;

	/**
	 * Schnittgerade in Form eines Line2-Objektes (Gerade)
	 */
	public Line2			line2;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt ein leeres Objekt und setzt result auf EMPTY.
	 */
	public Intersection()
	{
		result = EMPTY;

	} // Intersection


	// ********************************              

	/**
	 * Erzeugt eine Kopie der Eingabeintersection mit <b>Kopien</b> - nicht
	 * Referenzen - ihrer Elemente.
	 * 
	 * @param input_intersection
	 *            Das zu kopierende Intersection-Objekt
	 */
	public Intersection(
			Intersection input_intersection)
	{
		result = input_intersection.result;

		if (input_intersection.arc2 != null)
		{
			arc2 = (Arc2) input_intersection.arc2.clone();
		} // if

		if (input_intersection.circle2 != null)
		{
			circle2 = (Circle2) input_intersection.circle2.clone();
		} // if

		if (input_intersection.point2 != null)
		{
			point2 = (Point2) input_intersection.point2.clone();
		} // if

		if (input_intersection.list != null)
		{
			list = _cloneList(input_intersection.list);
		} // if

		if (input_intersection.segment2 != null)
		{
			segment2 = (Segment2) input_intersection.segment2.clone();
		} // if

		if (input_intersection.ray2 != null)
		{
			ray2 = (Ray2) input_intersection.ray2.clone();
		} // if

		if (input_intersection.line2 != null)
		{
			line2 = (Line2) input_intersection.line2.clone();
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
	 * @return Kopie des Schnittobjekts
	 */
	public Object clone()
	{
		return (new Intersection(this));

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
	 * 
	 * @param input_line
	 *            Das zu uebertragende Line2-Objekt
	 */
	public void set(
			Line2 input_line)
	{
		_emptyObjects();
		result = LINE2;
		line2 = input_line;

	} // set


	// ********************************              

	/**
	 * Setzt result auf LIST und list auf die Eingabeliste, die nur Objekte der
	 * Klassen Arc2, Circle2, Point2, Line2, Ray2, Segment2 enthalten darf, die
	 * anderen Variablen werden geloescht.
	 * 
	 * @param input_list
	 *            Das zu uebertragende Listen-Objekt
	 */
	public void set(
			SimpleList input_list)
	{
		_emptyObjects();
		result = LIST;
		list = input_list;

	} // set


	// ********************************              

	/**
	 * Setzt result auf ARC2 und arc2 auf den Eingabekreisbogen, die anderen
	 * Variablen werden geloescht.
	 * 
	 * @param input_arc
	 *            Das zu uebertragende Arc2-Objekt
	 */
	public void set(
			Arc2 input_arc)
	{
		_emptyObjects();
		result = ARC2;
		arc2 = input_arc;

	} // set


	// ********************************              

	/**
	 * Setzt result auf CIRCLE2 und circle2 auf den Eingabekreis, die anderen
	 * Variablen werden geloescht.
	 * 
	 * @param input_circle
	 *            Das zu uebertragende Circle2-Objekt
	 */
	public void set(
			Circle2 input_circle)
	{
		_emptyObjects();
		result = CIRCLE2;
		circle2 = input_circle;

	} // set


	// ********************************              

	/**
	 * Setzt result auf POINT2 und point2 auf den Eingabepunkt, die anderen
	 * Variablen werden geloescht.
	 * 
	 * @param input_point
	 *            Das zu uebertragende Point2-Objekt
	 */
	public void set(
			Point2 input_point)
	{
		_emptyObjects();
		result = POINT2;
		point2 = input_point;

	} // set


	// ********************************              

	/**
	 * Setzt result auf RAY2 und ray2 auf den Eingabestrahl, die anderen
	 * Variablen werden geloescht.
	 * 
	 * @param input_ray
	 *            Das zu uebertragende Ray2-Objekt
	 */
	public void set(
			Ray2 input_ray)
	{
		_emptyObjects();
		result = RAY2;
		ray2 = input_ray;

	} // set


	/**
	 * Setzt result auf SEGMENT2 und segment2 auf das Eingabesegment, die
	 * anderen Variablen werden geloescht.
	 * 
	 * @param input_segment
	 *            Das zu uebertragende Segment2-Objekt
	 */
	public void set(
			Segment2 input_segment)
	{
		_emptyObjects();
		result = SEGMENT2;
		segment2 = input_segment;

	} // set


	// ********************************
	// SETTER
	// *******************************

	/**
	 * Setzt result auf EMPTY und loescht die anderen Variablen.
	 */
	public void clear()
	{
		_emptyObjects();
		result = EMPTY;

	} // set


	// ********************************              

	/**
	 * Setzt result auf LINE2 und line2 auf die Eingabegerade.
	 * 
	 * @param input_line
	 *            Das zu uebertragende Line2-Objekt
	 */
	public void setLine(
			Line2 input_line)
	{
		result = LINE2;
		line2 = input_line;

	} // set


	// ********************************              

	/**
	 * Setzt result auf LIST und list auf die Eingabeliste, die nur Objekte der
	 * Klassen Arc2, Circle2, Point2, Line2, Ray2, Segment2 enthalten darf.
	 * 
	 * @param input_list
	 *            Das zu uebertragende Listen-Objekt
	 */
	public void setList(
			SimpleList input_list)
	{
		result = LIST;
		list = input_list;

	} // set


	// ********************************              

	/**
	 * Setzt result auf ARC2 und arc2 auf den Eingabekreisbogen.
	 * 
	 * @param input_arc
	 *            Das zu uebertragende Arc2-Objekt
	 */
	public void setArc(
			Arc2 input_arc)
	{
		result = ARC2;
		arc2 = input_arc;

	} // set


	// ********************************              

	/**
	 * Setzt result auf CIRCLE2 und circle2 auf den Eingabekreis.
	 * 
	 * @param input_circle
	 *            Das zu uebertragende Circle2-Objekt
	 */
	public void setCircle(
			Circle2 input_circle)
	{
		result = CIRCLE2;
		circle2 = input_circle;

	} // set


	// ********************************              

	/**
	 * Setzt result auf POINT2 und point2 auf den Eingabepunkt.
	 * 
	 * @param input_point
	 *            Das zu uebertragende Point2-Objekt
	 */
	public void setPoint(
			Point2 input_point)
	{
		result = POINT2;
		point2 = input_point;

	} // set


	// ********************************              

	/**
	 * Setzt result auf RAY2 und ray2 auf den Eingabestrahl.
	 * 
	 * @param input_ray
	 *            Das zu uebertragende Ray2-Objekt
	 */
	public void setRay(
			Ray2 input_ray)
	{
		result = RAY2;
		ray2 = input_ray;

	} // set


	// ********************************              

	/**
	 * Setzt result auf SEGMENT2 und segment2 auf das Eingabesegment.
	 * 
	 * @param input_segment
	 *            Das zu uebertragende Segment2-Objekt
	 */
	public void setSegment(
			Segment2 input_segment)
	{
		result = SEGMENT2;
		segment2 = input_segment;

	} // set


	// ********************************
	// GETTER
	// *******************************

	/**
	 * Liefert ein Line2-Objekt.
	 * 
	 * @return Das Line2-Schnittobjekt oder NULL, falls kein Schnitt in Form
	 *         einer Gerade vorliegt
	 */
	public Line2 getLine()
	{
		return line2;

	} // get


	// ********************************              

	/**
	 * Liefert die Eingabeliste.
	 * 
	 * @return Das Listen-Schnittobjekt oder NULL, falls kein Schnitt in Form
	 *         einer Liste vorliegt
	 */
	public SimpleList getList()
	{
		return list;

	} // get


	// ********************************              

	/**
	 * Liefert das Arc2-Objekt.
	 * 
	 * @return Das Arc2-Schnittobjekt oder NULL, falls kein Schnitt in Form
	 *         eines Bogens vorliegt
	 */
	public Arc2 getArc()
	{
		return arc2;

	} // get


	// ********************************              

	/**
	 * Liefert das Kreis-Objekt.
	 * 
	 * @return Das Circle2-Schnittobjekt oder NULL, falls kein Schnitt in Form
	 *         einer Kreises vorliegt
	 */
	public Circle2 getCircle()
	{
		return circle2;

	} // get


	// ********************************              

	/**
	 * Liefert ein Point2-Objekt.
	 * 
	 * @return Das Point2-Schnittobjekt oder NULL, falls kein Schnitt in Form
	 *         eines Punktes vorliegt
	 */
	public Point2 getPoint()
	{
		return point2;

	} // get


	// ********************************              

	/**
	 * Liefert ein Ray2-Objekt.
	 * 
	 * @return Das Ray2-Schnittobjekt oder NULL, falls kein Schnitt in Form
	 *         eines Strahls vorliegt
	 */
	public Ray2 getRay()
	{
		return ray2;

	} // get


	// ********************************              

	/**
	 * Liefert ein Segment2-Objekt.
	 * 
	 * @return Das Segment2-Schnittobjekt oder NULL, falls kein Schnitt in Form
	 *         einer Strecke vorliegt
	 */
	public Segment2 getSegment()
	{
		return segment2;

	} // get


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation.
	 * 
	 * @return Die textuelle Repraesentation
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer(_resultToString());

		if (list != null)
		{
			buffer.append(" List " + list);
		} // if

		if (arc2 != null)
		{
			buffer.append(" Arc2 " + arc2);
		} // if

		if (circle2 != null)
		{
			buffer.append(" Circle2 " + circle2);
		} // if

		if (point2 != null)
		{
			buffer.append(" Point2 " + point2);
		} // if

		if (segment2 != null)
		{
			buffer.append(" Segment2 " + segment2);
		} // if

		if (ray2 != null)
		{
			buffer.append(" Ray2 " + ray2);
		} // if

		if (line2 != null)
		{
			buffer.append(" Line2 " + line2);
		} // if

		return (buffer.toString());

	} // toString


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Erzeugt eine Kopie der Eingabeliste. Ihre Elemente werden kopiert, nicht
	 * referenziert. Erlaubte Inhalte der ListItem sind: Arc2 Circle2 Point2
	 * Segment2 Ray2 Line2
	 * 
	 * @param input_list
	 *            Die Liste mit dem zu klonenden Inhalt
	 */
	private SimpleList _cloneList(
			SimpleList input_list)
	{
		SimpleList new_list = new SimpleList();
		ListItem orig_item = input_list.first();
		Object orig_object;

		// Listenelemente kopieren (nicht referenzieren)

		while (orig_item != null)
		{
			orig_object = orig_item.value();

			if (orig_object instanceof Arc2)
			{
				new_list.add(((Arc2) orig_object).clone());
			}
			else if (orig_object instanceof Circle2)
			{
				new_list.add(((Circle2) orig_object).clone());
			}
			else if (orig_object instanceof Point2)
			{
				new_list.add(((Point2) orig_object).clone());
			}
			else if (orig_object instanceof Segment2)
			{
				new_list.add(((Segment2) orig_object).clone());
			}
			else if (orig_object instanceof Ray2)
			{
				new_list.add(((Ray2) orig_object).clone());
			}
			else if (orig_object instanceof Line2)
			{
				new_list.add(((Line2) orig_object).clone());
			}
			else
			{
				System.err
						.println("Intersection._cloneList error: can't copy unkown object"
								+ " List: " + input_list);
			} // if

			orig_item = orig_item.next();
		} // while

		return (new_list);

	} // _cloneList


	// ************************************************************************

	/**
	 * Loescht die Objekt-Referenzen
	 */
	private void _emptyObjects()
	{
		list = null;
		arc2 = null;
		circle2 = null;
		point2 = null;
		segment2 = null;
		ray2 = null;
		line2 = null;

	} // _emptyObjects


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation von result.
	 * 
	 * @return Die textuelle Repraesentation
	 */
	private String _resultToString()
	{
		String output_string;

		switch (result)
		{
			case EMPTY:
				output_string = new String("EMPTY");
				break;
			case LIST:
				output_string = new String("LIST");
				break;
			case ARC2:
				output_string = new String("ARC2");
				break;
			case CIRCLE2:
				output_string = new String("CIRCLE2");
				break;
			case POINT2:
				output_string = new String("POINT2");
				break;
			case SEGMENT2:
				output_string = new String("SEGMENT2");
				break;
			case RAY2:
				output_string = new String("RAY2");
				break;
			case LINE2:
				output_string = new String("LINE2");
				break;
			default:
				output_string = new String("Unknown result");
				break;
		} // switch

		return (output_string);

	} // _resultToString

} // class Intersection

