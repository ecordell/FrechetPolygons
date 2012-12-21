
package anja.analysis;


/**
 * Argument fuer Funktionen. Ein Argument kann mehrdimensional sein; die
 * Komponenten des Arguments sind Elemente einer beliebigen Grundmenge. Auf die
 * Komponenten des Arguments kann ueber die Arrayvariable <code>n[i]</code>
 * zugegriffen werden. Als Grundmenge ist jede Menge verwendbar, deren Elemente
 * den Typ <code>java.lang.Number</code> ableiten.<p>
 *
 * Beispiele:<ul>
 * <li>Die Geradenfunktion <i>f</i>(<i>x</i>) = <i>mx</i> + <i>b</i> hat ein
 * eindimensionales Argument mit der Komponente <code>n[0]</code>, die
 * beispielsweise eine reelle Zahl vom Typ <code>Double</code> ist.</li>
 * <li>Der Abstand zweier Punkte in der Ebene kann als eine reellwertige
 * Funktion <i>f</i>(<i>p</i><sub>1</sub>, <i>p</i><sub>2</sub>) =
 * |p<sub>1</sub> - p<sub>2</sub>| betrachtet werden, bei der
 * <i>p</i><sub>i</sub> zweidimensionale Vektoren mit den Komponenten
 * <code>n[0]</code> und <code>n[1]</code> aus der Menge der reellen Zahlen vom
 * Typ <code>Double</code> sind.</li>
 * <li>Man definiert die <i>komplexen Zahlen</i> <i><b>C</b></i> als Subklasse
 * von <code>java.lang.Number</code>, dann waeren in der Funktion
 * <i>f</i>(<i>x</i>, <i>y</i>) = <i>x</i> + <i>y</i> die Argumente
 * eindimensional mit der jeweiligen Komponente <code>n[0]</code> aus den
 * komplexen Zahlen des Subklassentyps.</li>
 *
 * @version 0.9 17.04.2004
 * @author Sascha Ternes
 */

public class Argument implements Cloneable
{

    // *************************************************************************
    // Public constants
    // *************************************************************************

    /**
     * das undefinierte Argument bzw. Funktionsergebnis, besteht aus der
     * Komponente <code>Double.NaN</code>
     */
    public static final Argument NOT_DEFINED       = new Argument(Double.NaN);

    /**
     * vordefiniertes reelles Argument fuer negative Unendlichkeit
     */
    public static final Argument NEGATIVE_INFINITY = new Argument(
                                                    Double.NEGATIVE_INFINITY);

    /**
     * vordefiniertes reelles Argument fuer positive Unendlichkeit
     */
    public static final Argument POSITIVE_INFINITY = new Argument(
                                                     Double.POSITIVE_INFINITY);

    /**
     * Exception bei Argument mit falscher Dimensionalitaet
     */
    public static final String   WRONG_DIMENSION   
     = "argument has wrong dimension!";


    // *************************************************************************
    // Public variables
    // *************************************************************************

    /**
     * die Komponenten des Arguments
     */
    public Number[]              n;


    // *************************************************************************
    // Constructors
    // *************************************************************************

    /**
     * Erzeugt das eindimensionale Null-Argument 0 der reellen Zahlen
     * <i><b>R</b></i>.
     */
    public Argument()
    {

        this(0.0);

    } // Argument


    /**
     * Erzeugt ein eindimensionales Argument aus einer ganzen Zahl.
     *
     * @param value die reelle Zahl
     */
    public Argument(int value)
    {

        n = new Number[1];
        n[0] = new Integer(value);

    } // Argument


    /**
     * Erzeugt ein mehrdimensionales Argument, dessen Komponenten ganze Zahlen
     * sind.
     *
     * @param value die reellen Komponenten
     */
    public Argument(int[] value)
    {
        n = new Number[value.length];
        
        for(int i = 0; i < value.length; i++)
        {
            n[i] = new Integer(value[i]);
        } // for

    } // Argument


    /**
     * Erzeugt ein eindimensionales Argument aus einer reellen Zahl.
     *
     * @param value die reelle Zahl
     */
    public Argument(double value)
    {
        n = new Number[1];
        n[0] = new Double(value);

    } // Argument


    /**
     * Erzeugt ein mehrdimensionales Argument, dessen Komponenten reelle Zahlen
     * sind.
     *
     * @param value die reellen Komponenten
     */
    public Argument(double[] value)
    {
        n = new Number[value.length];
        for(int i = 0; i < value.length; i++)
        {
            n[i] = new Double(value[i]);
        } // for

    } // Argument


    /**
     * Erzeugt ein eindimensionales Argument.
     *
     * @param value der Wert des Arguments
     */
    public Argument(Number value)
    {
        n = new Number[1];
        n[0] = value;

    } // Argument


    /**
     * Erzeugt ein mehrdimensionales Argument.
     *
     * @param value der Wert des Arguments.
     */
    public Argument(Number[] value)
    {
        n = value;
    } // Argument


    /**
     * Erzeugt eine Kopie des uebergebenen Arguments.
     *
     * @param argument das zu kopierende Argument
     */
    public Argument(Argument argument)
    {
        n = ((Argument) argument.clone()).n;

    } // Argument


    // *************************************************************************
    // Public methods
    // *************************************************************************

    /**
     * Erzeugt eine Kopie dieses Arguments.
     *
     * @return eine Kopie dieses Arguments.
     */
    public Object clone()
    {
        Argument arg = new Argument((Number[]) n.clone());
        return arg;

    } // clone


    /**
     * Erzeugt ein Argument, das das Zentrum dieses Arguments und des
     * spezifizierten beschreibt. Anders ausgedrueckt liefert diese Methode den
     * Mittelpunkt der Verbindungsgeraden zwischen den Punkten mit den
     * Koordinaten aus diesem und dem spezifizierten Argument. Dazu muss die
     * Dimensionalitaet beider Argumente gleich sein, sonst wird eine
     * <code>IllegalArgumentException</code> ausgeworfen.<p>
     *
     * <b>Achtung: Diese Methode nutzt zur Berechnung eine der Methoden
     * <code>Number.intValue()</code>, <code>Number.longValue()</code> oder
     * <code>Number.doubleValue()</code> fuer die einzelnen Argumentkomponenten.
     * Daher kann das Ergebnis bei entsprechenden Argumentformaten falsch
     * sein!</b><br>
     * Das Format der Komponenten des Ergebnisses richtet sich wie folgt nach
     * den Formaten der Ausgangskomponenten:<ul>
     * <li>Ist eine Komponente <code>Long</code> und die andere <code>Long</code>
     * oder <code>Integer</code>, ist die Ergebniskomponente vom Typ
     * <code>Long</code>.</li>
     * <li>Sind beide Komponenten vom Typ <code>Integer</code>, ist die
     * Ergebiskomponente ebenfalls vom Typ <code>Integer</code>.
     * <li>Andernfalls ist die Ergebniskomponente vom Typ
     * <code>Double</code>.</li></ul><br>
     * Falls es <code>Double</code>-Koordinaten im Unendlichen gibt, kann
     * natuerlich kein echter Mittelpunkt berechnet werden; dann gilt folgende
     * Regelung:<ul>
     * <li>Die Mitte zwischen negativer und positiver Unendlichkeit ist
     * Null.</li>
     * <li>Die Mitte zwischen zwei Unendlichkeiten mit gleichem Vorzeichen ist
     * gleich diesen Unendlichkeiten.</li>
     * <li>Die Mitte zwischen negativer Unendlichkeit und <i>x</i> ist<ul>
     * <li>2<i>x</i> fuer <i>x</i> < 0,</li>
     * <li>-1 fuer <i>x</i> = 0 und</li>
     * <li><i>x</i>/2 fuer <i>x</i> > 0.</li></ul></li>
     * <li>Die Mitte zwischen positiver Unendlichkeit und <i>x</i> ist<ul>
     * <li><i>x</i>/2 fuer <i>x</i> < 0,</li>
     * <li>1 fuer <i>x</i> = 0 und</li>
     * <li>2<i>x</i> fuer <i>x</i> > 0.</li></ul></li></ul>
     *
     * @param argument das zweite Argument fuer die Zentrumsbestimmung
     * @return ein Argument mit den Koordinaten des Zentrums und der gleichen
     *         Dimensionalitaet
     * @exception IllegalArgumentException falls die Dimensionalitaet dieses und
     *            des spezifizierten Arguments verschieden ist
     */
    public Argument center(Argument argument)
    {

        if(this.n.length != argument.n.length) 
            throw new IllegalArgumentException(WRONG_DIMENSION);

        Number[] r = new Number[this.n.length];
        
        for(int i = 0; i < this.n.length; i++)
        {  
            // beide Komponenten sind Integer:
            
            if((this.n[i] instanceof Integer) && 
               (argument.n[i] instanceof Integer))
            {
                int a = this.n[i].intValue();
                int b = argument.n[i].intValue();
                
                r[i] = new Integer(a + (b - a) / 2);
            }
            
            else // if
            // beide Komponenten sind ganzzahlig, mindestens eine ist Long:
                
            if(((this.n[i] instanceof Long) && 
               ((argument.n[i] instanceof Long) || 
               (argument.n[i] instanceof Integer))) || 
               ((argument.n[i] instanceof Long) && 
               ((this.n[i] instanceof Long) || 
               (this.n[i] instanceof Integer))))
            {
                long a = this.n[i].longValue();
                long b = argument.n[i].longValue();
                r[i] = new Long(a + (b - a) / 2l);
                
                // in allen anderen Faellen Double berechnen:
            }
            else
            {
                // if
                double a = this.n[i].doubleValue();
                double b = argument.n[i].doubleValue();
                
                if(Double.isInfinite(a)) 
                    if(Double.isInfinite(b)) 
                        if(a == b) 
                            r[i] = new Double(a);
                
                else 
                    r[i] = new Double(0.0);
                
                else if(a < 0) 
                    if(b < 0) 
                        r[i] = new Double(2.0 * b);
                
                else if(b > 0) 
                    r[i] = new Double(b / 2.0);
                
                else 
                    r[i] = new Double(-1.0);
                
                else 
                    if(b < 0) 
                        r[i] = new Double(b / 2.0);
                
                else if(b > 0) 
                    r[i] = new Double(2.0 * b);
                
                else 
                    r[i] = new Double(1.0);
                
                else if(Double.isInfinite(b)) 
                    if(b < 0) 
                        if(a < 0) 
                            r[i] = new Double(2.0 * a);
                
                else if(a > 0) 
                    r[i] = new Double(a / 2.0);
                
                else 
                    r[i] = new Double(-1.0);
                
                else if(a < 0) 
                    r[i] = new Double(a / 2.0);
                
                else if(a > 0) 
                    r[i] = new Double(2.0 * a);
                
                else 
                    r[i] = new Double(1.0);
                
                else 
                    r[i] = new Double(a + (b - a) / 2.0);
                
                
            } // else
            
        }
        return new Argument(r);

    } // center


    /**
     * Liefert die Anzahl der Dimensionen / der Komponenten dieses Arguments.
     *
     * @return die Dimensionalitaet dieses Arguments
     */
    public int dimension()
    {
        return n.length;
    } // dimension


    /**
     * Testet dieses Argument mit dem spezifizierten Objekt, das ein Argument
     * sein muss, auf Gleichheit.
     *
     * @param obj das zu testende Argument
     * @return <code>true</code>, falls <code>obj</code> ein gleichdimensionales
     *         Argument ist und alle Komponenten gleich sind, sonst
     *         <code>false</code>
     */
    public boolean equals(Object obj)
    {

        Argument a = null;
        
        try
        {
            a = (Argument) obj;
        } 
        catch(ClassCastException cce)
        { 
            // try
            return false;
        } // catch
        
        if(a.n.length != this.n.length) 
            return false;
        
        for(int i = 0; i < n.length; i++)
         if(!this.n[i].equals(a.n[i])) 
          return false;
        
        return true;

    } // equals


    /**
     * Liefert eine textuelle Repraesentation.
     *
     * @return das Argument als String
     */
    public String toString()
    {
        String s = "";   
        if(n.length > 1) s += "(";
        
        for(int i = 0; i < n.length; i++)
        {
            s += n[i];
            
            if(i < n.length - 1)
             s += ", ";
        } // for
        
        
        if(n.length > 1) s += ")";
         return s;

    } // toString


} // Argument
