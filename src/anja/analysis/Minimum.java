
package anja.analysis;


/**
 * Lokales oder globales Minimum fuer Funktionen.
 * 
 * @version 0.1 12.12.03
 * @author Sascha Ternes
 */

public class Minimum extends FunctionPoint
{

    // ***********************************************************************
    // Constructors
    // ***********************************************************************

    /**
     * Erzeugt ein neues Minimum (<i>x</i>,<i>f</i>(<i>x</i>)).
     * 
     * @param x Stelle des Minimums
     * @param y Wert des Minimums
     */
    public Minimum(Argument x, Argument y)
    {
        super(x, y, MINIMUM);
    } // Minimum

} // Minimum
