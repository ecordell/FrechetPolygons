
package anja.analysis;


/**
 * Lokales oder globales Maximum fuer Funktionen.
 * 
 * @version 0.1 12.12.03
 * @author Sascha Ternes
 */

public class Maximum extends FunctionPoint
{

    // ***********************************************************************
    // Constructors
    // ***********************************************************************

    /**
     * Erzeugt ein neues Maximum (<i>x</i>,<i>f</i>(<i>x</i>)).
     * 
     * @param x Stelle des Maximums
     * @param y Wert des Maximums
     */
    public Maximum(Argument x, Argument y)
    {
        super(x, y, MAXIMUM);
    } // Maximum

} // Maximum
