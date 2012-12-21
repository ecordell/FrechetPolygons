
package anja.ratgeom;


/**
* <p align="justify">
* InspectResult dient zur Rueckgabe der Ergebnisse von <tt> inspectBasicLine()
* </tt> der Klasse <em> BasicLine2</em>. Sind die Linien parallel, so ist
* die Variable <tt> parallel </tt> gleich true und die uebrigen Variablen sind
* undefiniert, ansonsten ist <tt> parallel </tt> gleich false und die uebrigen
* Variablen sind belegt.
* </p>
*
* @version	1.0  24.09.1997
* @author	Norbert Selle
*
* @see		BasicLine2
*/

public class InspectResult
{
   // ************************************************************************
   // Variables
   // ************************************************************************


   /** true bei parallelen Linien				*/
   public boolean	parallel;

   /**
   * die Lage des Schnittpunkts bezueglich der Linie
   *
   * @see	Point2#LIES_BEFORE
   * @see	Point2#LIES_ON
   * @see	Point2#LIES_BEHIND
   */
   public int		orderOnThis;

   /**
   * die Lage des Schnittpunkts bezueglich der Eingabelinie
   *
   * @see	Point2#LIES_BEFORE
   * @see	Point2#LIES_ON
   * @see	Point2#LIES_BEHIND
   */
   public int		orderOnParam;
   
   /** der Schnittpunkt der Linien				*/
   public Point2	intersectionPoint;

   
   // ************************************************************************
   // Constructors
   // ************************************************************************


   public InspectResult() {};


   // ************************************************************************


} // InspectResult

