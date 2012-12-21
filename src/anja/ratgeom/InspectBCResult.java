
package anja.ratgeom;


/**
* <p align="justify">
* InspectBCResult dient zur Rueckgabe der Ergebnisse von <tt>
* inspectBasicCircle() </tt> der Klasse <em> BasicCircle2</em>.
* </p>
*
* @version	1.1  12.11.1997
* @author	Norbert Selle
*
* @see		BasicCircle2
*/

public class InspectBCResult
{

   // ************************************************************************
   // Variables
   // ************************************************************************


   /** true bei gleichem Mittelpunkt und Radius, sonst false	*/
   public boolean	lies_on;

   /** ein oder zwei Schnittpunkte				*/
   public Point2	points[];


   // ************************************************************************
   // Constructors
   // ************************************************************************


   public InspectBCResult()
   {
      lies_on  = false;

   } // InspectBCResult


   // ********************************              


   public InspectBCResult(
      boolean	input_lies_on
   )
   {
      lies_on  = input_lies_on;

   } // InspectBCResult


   // ************************************************************************


} // InspectBCResult


