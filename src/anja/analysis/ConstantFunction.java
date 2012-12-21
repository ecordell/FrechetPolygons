
package anja.analysis;


import anja.util.DynamicArray;

/**
 * Allgemeine konstante Funktion der Form <i>f</i>(<i>x</i>) = <i>c</i>.
 * 
 * @version 0.7 20.02.2004
 * @author Sascha Ternes
 */

public class ConstantFunction extends Polynomial
{

    // *************************************************************************
    // Constructors
    // *************************************************************************

    /**
     * Erzeugt die Nullfunktion <i>f</i>(<i>x</i>) = 0.
     */
    public ConstantFunction()
    {
        super();
    } // ConstantFunction


    /**
     * Erzeugt die konstante Funktion fuer die angegebene Konstante.
     * 
     * @param c die Konstante
     */
    public ConstantFunction(double c)
    {
        _coefficients = new double[1];
        _coefficients[0] = c;
    } // ConstantFunction


    // *************************************************************************
    // Public methods
    // *************************************************************************

    /*
     * [javadoc-Beschreibung wird aus Polynomial kopiert]
     */
    public double f(double x)
    {
        // Definitionsmenge pruefen:
        if(!isDefinedFor(x)) 
         return Double.NaN;
        
        return _coefficients[0];
    } // f


    /**
     * Liefert den Wert <code>Double.NaN</code>.
     * 
     * @param y das Argument der Umkehrfunktion dieser konstanten
     *            Funktion
     * @return <code>Double.NaN</code>
     */
    public double inverse(double y)
    {
        return Double.NaN;
    } // inverse


    /**
     * Liefert ein Array, das als einziges Element den Wert
     * {@link Argument#NOT_DEFINED Argument.NOT_DEFINED} enthaelt.
     * 
     * @param y das Argument der Umkehrfunktion dieser konstanten
     *            Funktion
     * @return ein Array mit dem Wert
     *         {@link Argument#NOT_DEFINED Argument.NOT_DEFINED}
     */
    public DynamicArray inverseAll(double y)
    {
        return new DynamicArray(Argument.NOT_DEFINED);
    } // inverseAll


    /**
     * Berechnet die Schnittmenge mit der spezifizierten Funktion, die
     * ein Polynom sein muss. Falls die Funktion kein Polynom ist,
     * wird eine <code>IllegalArgumentException</code> ausgeloest.
     * 
     * @param g die Schnittfunktion
     * @return die Schnittmenge
     * @exception IllegalArgumentException falls <code>g</code> kein
     *                Polynom ist
     */
    public DynamicArray intersection(Function g)
    {
        // Test auf Polynom:
        Polynomial p = null;
        
        try
        {
            p = (Polynomial) g;
        } 
        catch(ClassCastException cce)
        { // try
            
            throw new IllegalArgumentException(Function.ILLEGAL_ARGUMENT);
        } // catch

        // Schnitt mit hoehergradigen Polynomen weiterreichen:
        if(p.degree() > 0) 
         return p.intersection(this);

        // Schnitt mit einer konstanten Funktion sofort aufloesen:
        if(p.equals(this)) 
         return identityIntersection(this);
        
        // keine Schnittmenge:
        return new DynamicArray();

    } // intersection


    /*
     * [javadoc-Beschreibung wird aus Polynomial kopiert]
     */
    public int degree()
    {
        return 0;
    } // degree

} // ConstantFunction
