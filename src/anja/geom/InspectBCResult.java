package anja.geom;


/**
 * InspectBCResult dient zur Rueckgabe der Ergebnisse von <tt>
 * inspectBasicCircle()</tt> der Klasse <em>BasicCircle2</em>.
 * 
 * 
 * @version 1.0 17.09.1997
 * @author Norbert Selle
 * 
 * @see BasicCircle2
 */
public class InspectBCResult
		implements java.io.Serializable
{

	// ************************************************************************
	// Variables
	// ************************************************************************

	/** true bei gleichem Mittelpunkt und Radius, sonst false */
	public boolean	lies_on;

	/** ein oder zwei Schnittpunkte */
	public Point2	points[];


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Default constructor
	 */
	public InspectBCResult()
	{
		lies_on = false;
	};


	// ************************************************************************

	// ************************************************************************
	// Setter und Getter
	// ************************************************************************

	/**
	 * Setter für die Variable, die den Gleichheitstest enthält.
	 * 
	 * @param lieson
	 *            Die Gleichheitsangabe
	 */
	public void setLiesOn(
			boolean lieson)
	{
		lies_on = lieson;
	}


	/**
	 * Array der Schnittpunkte
	 * 
	 * @param p
	 *            Die Schnittpunkte
	 */
	public void setPoints(
			Point2[] p)
	{
		points = p;
	}


	/**
	 * Getter für die Gleichheitsangabe.
	 * 
	 * @return true bei absoluter Gleichheit, false sonst
	 */
	public boolean isLiesOn()
	{
		return lies_on;
	}


	/**
	 * Rückgabe der Schnittpunkte
	 * 
	 * @return Die Schnittpunkte
	 */
	public Point2[] getPoints()
	{
		return points;
	}

} // InspectBCResult

