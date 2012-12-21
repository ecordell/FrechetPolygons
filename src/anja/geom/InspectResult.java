package anja.geom;


/**
 * InspectResult dient zur Rueckgabe der Ergebnisse der Methode
 * inspectBasicLine().
 * 
 * @version 1.0 22.09.1997
 * @author Norbert Selle
 * 
 * @see anja.geom.BasicLine2
 */
public class InspectResult
		implements java.io.Serializable
{

	// ************************************************************************
	// Variables
	// ************************************************************************

	/** true bei parallelen Linien */
	public boolean	parallel;

	/**
	 * Lage des Schnittpunkts bezueglich der Linie.
	 * 
	 * @see anja.geom.Point2#LIES_BEFORE
	 * @see anja.geom.Point2#LIES_ON
	 * @see anja.geom.Point2#LIES_BEHIND
	 */
	public int		orderOnThis;

	/**
	 * Lage des Schnittpunkts bezueglich der Eingabelinie.
	 * 
	 * @see anja.geom.Point2#LIES_BEFORE
	 * @see anja.geom.Point2#LIES_ON
	 * @see anja.geom.Point2#LIES_BEHIND
	 */
	public int		orderOnParam;

	/** Schnittpunkt bei nicht parallelen Linien */
	public Point2	intersectionPoint;

	/** Schnittpunktparameter */
	public double	lambda;
	public double	mue;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Default constructor
	 */
	public InspectResult()
	{};

	// ************************************************************************

} // InspectResult

