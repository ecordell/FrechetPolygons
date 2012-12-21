
package anja.analysis;


import anja.util.DynamicArray;

/**
 * Allgemeine lineare Funktion der Form <i>f</i>(<i>x</i>) = <i>ax</i> +
 * <i>b</i>.
 * 
 * @version 0.8 20.02.2004
 * @author Sascha Ternes
 */

public class LinearFunction extends Polynomial
{

    // *************************************************************************
    // Constructors
    // *************************************************************************

    /**
     * Erzeugt die Identitaetsfunktion <i>f</i>(<i>x</i>) = <i>x</i>.
     */
    public LinearFunction()
    {
        this(1.0, 0.0);
    } // LinearFunction


    /**
     * Konstruktor fuer eine allgemeine lineare Funktion der Form 
     * <i>f</i>(<i>x</i>) = <i>ax</i> + <i>b</i>.
     * 
     * @param a Koeffizient <i>a</i>; darf nicht Null sein, sonst
     *            wird eine <code>IllegalArgumentException</code>
     *            ausgeworfen
     *            
     * @param b Koeffizient <i>b</i>
     * 
     * @exception IllegalArgumentException im Fall <i>a</i> = 0
     */
    public LinearFunction(double a, double b)
    {
        if(a == 0) 
        { 
            throw new IllegalArgumentException(Polynomial.ZERO_COEFFICIENT); 
        } 
        
        _coefficients = new double[2];
        _coefficients[0] = b;
        _coefficients[1] = a;

    } // LinearFunction


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

        return _coefficients[1] * x + _coefficients[0];
    } // f


    /*
     * [javadoc-Beschreibung wird aus Polynomial kopiert]
     */
    public double inverse(double y)
    {
        double x = (y - _coefficients[0]) / _coefficients[1];
        
        if(isDefinedFor(x)) 
         return x;
        
        return Double.NaN;
    } // inverse


    /*
     * [javadoc-Beschreibung wird aus Polynomial kopiert]
     */
    public DynamicArray inverseAll(double y)
    {
        return new DynamicArray(new Argument(inverse(y)));
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
        {
            throw new IllegalArgumentException(Function.ILLEGAL_ARGUMENT);
        }

        // Schnitt mit hoehergradigen Polynomen weiterreichen:
        if(p.degree() > 1) 
         return p.intersection(this);

        // Schnitt mit einer linearen Funktion:
        if(p.degree() == 1)
        {
            if(p.equals(this)) 
             return identityIntersection(this);
            
            if(p._coefficients[1] != this._coefficients[1])
            {
                double a = this._coefficients[1] - p._coefficients[1];
                double b = this._coefficients[0] - p._coefficients[0];
                
                LinearFunction fs = new LinearFunction(a, b);
                
                double s = fs.inverse(0);
                return new DynamicArray(new Argument(s));
                
                // keine Schnittmenge:
            }
            else return new DynamicArray();
        } 

        // Schnitt mit einer konstanten Funktion:
        double s = this.inverse(p._coefficients[0]);
        return new DynamicArray(new Argument(s));

    } // intersection


    /*
     * [javadoc-Beschreibung wird aus Polynomial kopiert]
     */
    public int degree()
    {
        return 1;
    } // degree

} // LinearFunction
