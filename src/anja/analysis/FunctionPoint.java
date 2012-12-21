
package anja.analysis;


/**
 * Allgemeines Paar aus Funktionsargument und Funktionswert 
 * (<i>x</i>,<i>f</i>(<i>x</i>)),
 * dem optional ein Typattribut zugewiesen werden kann.
 * 
 * @version 0.2 25.02.2004
 * @author Sascha Ternes
 */

public class FunctionPoint implements Cloneable
{

    // ***********************************************************************
    // Public constants
    // ***********************************************************************

    /**
     * undefinierter Typ
     */
    public static final int UNDEFINED = 0;

    /**
     * Typ fuer ein (lokales) Minimum
     */
    public static final int MINIMUM   = -1;

    /**
     * Typ fuer ein (lokales) Maximum
     */
    public static final int MAXIMUM   = 1;

    // ***********************************************************************
    // Public variables
    // ***********************************************************************

    /**
     * Funktionsargument <i>x</i>
     */
    public Argument x;

    /**
     * Funktionswert <i>f</i>(<i>x</i>)
     */
    public Argument y;

    /**
     * der Typ dieses Punkts
     */
    public int type;


    // *************************************************************************
    // Constructors
    // *************************************************************************

    /**
     * Erzeugt ein neues Paar (<i>x</i>,<i>f</i>(<i>x</i>)).
     * 
     * @param x Argument <i>x</i>
     * @param y Funktionswert <i>f</i>(<i>x</i>)
     */
    public FunctionPoint(Argument x, Argument y)
    {
        this(x, y, UNDEFINED);
    } // FunctionPoint


    /**
     * Erzeugt ein neues Paar (<i>x</i>,<i>f</i>(<i>x</i>)) mit
     * zusaetzlichem Typattribut. Als Attribute koennen die Konstanten
     * {@link #MINIMUM MINIMUM} und {@link #MAXIMUM MAXIMUM} verwendet
     * werden.
     * 
     * @param x Argument <i>x</i>
     * @param y Funktionswert <i>f</i>(<i>x</i>)
     * @param type das Typattribut
     */
    public FunctionPoint(Argument x, Argument y, int type)
    {
        this.x = x;
        this.y = y;
        this.type = type;
    } // FunctionPoint


    // *************************************************************************
    // Public methods
    // *************************************************************************

    /**
     * Erzeugt eine Kopie dieses Funktionspunkts.
     * 
     * @return die Kopie
     */
    public Object clone()
    {
        return new FunctionPoint((Argument) x.clone(), 
                                 (Argument) y.clone(),
                                 type);
    } // clone


    /**
     * Vergleicht diesen mit dem spezifizierten Funktionspunkt und
     * liefert das Testergebnis. Zwei Funktionspunkte sind genau dann
     * gleich, wenn ihre Koordinaten gleich sind; der (optionale) Typ
     * ist irrelevant.
     * 
     * @param o der zu vergleichende Funktionspunkt
     * @return <code>true</code>, wenn beide identisch sind, sonst
     *         <code>false<code>
     */
    public boolean equals(Object o)
    {
        FunctionPoint p = (FunctionPoint) o;
        return p.x.equals(this.x) && p.y.equals(this.y);
    } // equals


    /**
     * Liefert eine textuelle Repraesentation.
     * 
     * @return diesen Funktionspunkt als String
     */
    public String toString()
    {
        String s = "(" + x + "," + y;
        if(type != UNDEFINED) s += "|" + type;
        s += ")";
        return s;

    } // toString

} // FunctionPoint
