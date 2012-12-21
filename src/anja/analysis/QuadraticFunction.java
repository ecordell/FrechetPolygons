
package anja.analysis;


import java.util.TreeSet;
import anja.util.DynamicArray;

/**
 * Allgemeine quadratische Funktionen. Die Funktionen koennen entweder
 * in der allgemeinen Form <i>f</i>(<i>x</i>) = <i>ax<sup>2</sup></i> +
 * <i>bx</i> + <i>c</i> oder in Nullstellenform 
 * <i>f</i>(<i>x</i>) = (<i>x</i>+<i>a</i>)(<i>x</i>+<i>b</i>)
 * spezifiziert werden.
 * 
 * @version 0.8 26.02.2004
 * @author Sascha Ternes
 */

public class QuadraticFunction extends Polynomial
{

    // *************************************************************************
    // Constructors
    // *************************************************************************

    /**
     * Erzeugt die Parabel <i>f</i>(<i>x</i>) = <i>x</i><sup>2</sup>.
     */
    public QuadraticFunction()
    {
        this(1.0, 0.0, 0.0);
    } // QuadraticFunction

    /**
     * Konstruktor fuer eine allgemeine quadratische Funktion der Form
     * <i>f</i>(<i>x</i>) = <i>ax<sup>2</sup></i> + <i>bx</i> +
     * <i>c</i>.
     * 
     * @param a Koeffizient <i>a</i>; darf nicht Null sein, sonst
     *            wird eine <code>IllegalArgumentException</code>
     *            ausgeworfen
     * @param b Koeffizient <i>b</i>
     * @param c Koeffizient <i>c</i>
     * @exception IllegalArgumentException im Fall <i>a</i> = 0
     */
    public QuadraticFunction(double a, double b, double c)
    {

        if(a == 0) { throw new IllegalArgumentException(
                Polynomial.ZERO_COEFFICIENT); } 

        _coefficients = new double[3];
        _coefficients[0] = c;
        _coefficients[1] = b;
        _coefficients[2] = a;

    } // QuadraticFunction

    /**
     * Konstruktor fuer eine quadratische Funktion in der
     * Nullstellenform <i>f</i>(<i>x</i>) = (<i>x</i>+<i>a</i>)(<i>x</i>+<i>b</i>).
     * 
     * @param a die inverse Nullstelle -<i>a</i>
     * @param b die inverse Nullstelle -<i>b</i>
     */
    public QuadraticFunction(double a, double b)
    {

        this(1.0, -(a + b), a * b);
        _roots = new TreeSet();
        
        if(a == b)
        {
            _roots.add(new Double(a));
        }
        else
        { 
            _roots.add(new Double(a));
            _roots.add(new Double(b));
        } 

    } // QuadraticFunction

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

        return _coefficients[2] * x * x + 
               _coefficients[1] * x + 
               _coefficients[0];

    } // f

    /*
     * [javadoc-Beschreibung wird aus Function kopiert]
     */
    public double inverse(double y)
    {

        double inv[] = inverseTwo(y);
        
        if(inv.length == 2) 
         return Double.NaN;
        
        else return inv[0];

    } // inverse

    /*
     * [javadoc-Beschreibung wird aus Function kopiert]
     */
    public DynamicArray inverseAll(double y)
    {

        double[] inv = inverseTwo(y);
        DynamicArray a = new DynamicArray(inv.length);
        
        a.add(new Argument(inv[0]));
        if(inv.length == 2) a.add(new Argument(inv[1]));
        
        return a;

    } // inverseAll

    /**
     * Berechnet die Umkehrfunktion fuer ein gegebenes <i>f</i>(<i>x</i>)
     * durch Anwendung der <i>p-q-Formel</i>.<br>
     * Zurueckgegeben wird ein Array, das ein oder zwei Werte
     * enthaelt:
     * <ul>
     * <li>Falls die Umkehrfunktion fuer das uebergebene <i>f</i>(<i>x</i>)
     * nicht definiert ist, enthaelt das Array nur den Wert
     * <code>Double.NaN</code>.</li>
     * <li>Falls die Umkehrfunktion eindeutig ist, enthaelt das Array
     * das einzige <i>x</i>, das die Funktion mit dem gegebenen 
     * <i>f</i>(<i>x</i>)
     * erfuellt.</li>
     * <li>Falls zwei Loesungen fuer die Umkehrfunktion existieren,
     * enthaelt das Array genau diese, mit dem kleineren Wert an Index
     * 0.</li>
     * </ul>
     * 
     * @param y das Argument fuer die Umkehrfunktion
     * @return ein Array mit der/den Loesung/en bzw.
     *         <code>Double.NaN</code>, wenn keine Loesung
     *         existiert
     */
    public double[] inverseTwo(double y)
    {

        double p_half = (_coefficients[1] / _coefficients[2]) / 2.0;
        double p_half_quad = p_half * p_half;
        
        double q = (_coefficients[0] - y) / _coefficients[2];
        double r = p_half_quad - q;

        double[] x;
        
        // Wurzel hat keine Loesung:
        if(r < 0.0)
        {
            x = new double[1];
            x[0] = Double.NaN;
        }
        else
            
        // es gibt genau eine Loesung:
        if(r == 0.0)
        {
            x = new double[1];
            x[0] = -p_half;
            if(!isDefinedFor(x[0])) x[0] = Double.NaN;
            
            // es gibt zwei Loesungen:
        }
        else
        {
            p_half = -p_half;
            
            double sqrt = Math.sqrt(r);
            double x1 = p_half - sqrt;
            double x2 = p_half + sqrt;
            
            if(isDefinedFor(x1)) if(isDefinedFor(x2))
            {
                x = new double[2];
                x[0] = x1;
                x[1] = x2;
            }
            else
            { 
                x = new double[1];
                x[0] = x1;
            } 
            else
            { 
                x = new double[1];
                if(isDefinedFor(x2)) x[0] = x2;
                else x[0] = Double.NaN;
            } 
        } 

        return x;
    } // inverse

    /*
     * [javadoc-Beschreibung wird aus Polynomial kopiert]
     */
    public int degree()
    {
        return 2;
    } // degree

} // QuadraticFunction
