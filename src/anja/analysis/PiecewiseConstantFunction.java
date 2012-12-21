
package anja.analysis;


import anja.util.DynamicArray;

/**
 * Eine allgemeine stueckweise konstante Funktion.
 *
 * @version 1.0 16.03.2005
 * @author Torsten Baumgartner
 */

public class PiecewiseConstantFunction extends RealFunction
{

    // *************************************************************************
    // Private constants
    // *************************************************************************

    // Speichert die Unstetigkeitsstellen der Funktion
    // und ihre Funktionswerte.
    private Argument[]         _argument;

    // Speichert nur die Unstetigkeitsstellen.
    // Wird fuer die Methode getDiscontinuities gebraucht.
    private DynamicArray       _discontinuities;

    // *************************************************************************
    // Public constants
    // *************************************************************************

    /**
     * Exception bei ungueltigem Argument
     */
    public static final String ILLEGAL_ARGUMENT         
     = "illegal argument given!";
    
    /**
     * Exception bei undefinierter Umkehrfunktion
     */
    public static final String UNDEFINED_INVERSE        
     = "inverse not defined!";
    
    /**
     * Exception bei undefinierter Ableitungsberechnung
     */

    public static final String UNAVAILABLE_DERIVATIVE   
    = "derivative not available!";
    
    /**
     * Exception bei undefinierter Schnittberechnung
     */
    
    public static final String UNAVAILABLE_INTERSECTION 
     = "intersection not available!";
    
    /**
     * Exception bei undefinierter Grenzwertberechnung
     */
    
    public static final String UNAVAILABLE_LIMIT        
     = "limit not available!";

    // *************************************************************************
    // Constructors
    // *************************************************************************

    /**
     * Der leere Konstruktor
     */
    public PiecewiseConstantFunction()
    {}

    /**
     * Der Konstruktor
     */
    public PiecewiseConstantFunction(Argument[] args)
    {
        this._argument = args;
    }

    // *************************************************************************
    // Public methods
    // *************************************************************************

    /**
     * Testet, ob das spezifizierte Argument fuer diese Funktion formal korrekt
     * ist.<br>
     * Das Argument ist formal korrekt, wenn folgende Bedingungen erfuellt
     * sind:<ul>
     * <li>Das Argument ist nicht <code>null</code>.</li>
     * <li>Das Argument ist nicht das <i>undefinierte Argument</i> {@link
     * anja.analysis.Argument#NOT_DEFINED}.</li>
     * <li>Die Anzahl der Komponenten des Arguments ist korrekt.</li>
     * <li>Keine der Komponenten des Arguments ist <code>null</code>.</li>
     * <li>Die Komponenten des Arguments weisen jeweils den richtigen Typ
     * auf.</li></ul>
     *
     * @param x das zu testende Argument der Funktion
     * @return <code>true</code>, falls das Argument in Ordnung ist, sonst
     *         <code>false</code>
     */
    public boolean isValidArgument(Argument x)
    {
        if(x == null) return false;

        if(x.equals(Argument.NOT_DEFINED)) return false;

        if(x.n.length != 1) return false;

        if(x.n[0] == null) return false;
        else return true;
    }

    /**
     * Testet, ob der Wert des spezifierten Arguments im
     * Definitionsbereich der Funktion liegt.
     *
     * @return <code>true</code>, falls x ein formal korrektes Argument
     *         ist, dessen Wert im Definitionsbereich liegt, sonst
     *          <code>false</code>
     */
    public boolean isInDefinitionRange(double x)
    {
        // check if the value x is in the definition range
        if(x < _argument[0].n[0].doubleValue() || 
           x >= _argument[_argument.length - 1].n[1].doubleValue()) 
         return false;

        else return true;
    }

    /**
     * Erzeugt und liefert eine Kopie dieser Funktion.
     *
     * @return eine Kopie dieser Funktion
     */
    public Object clone()
    {
        return new PiecewiseConstantFunction(_argument);
    }

    /**
     * Soll die erste Ableitungsfunktion dieser Funktion liefern.
     * Diese ist konstant, aber an den Unstetigkeitsstellen nicht
     * definiert.
     *
     * @return die Ableitung
     */
    public RealFunction derivative()
    {
        throw new UnsupportedOperationException(UNAVAILABLE_DERIVATIVE);
    }

    /**
     * TODO:
     * Soll den Grenzwert <i>lim</i>[<i>x</i>-><i>c</i>](<i>f(<i>x</i>)</i>)
     * dieser Funktion <i>f</i> fuer <i>x</i> gegen <i>c</i> liefern.<p>
     *
     * @param c der Annaeherungsparameter fuer <i>x</i>, fuer den der Grenzwert
     *        gesucht ist
     * @return den Grenzwert dieser Funktion fuer <i>x</i> gegen <i>c</i>
     */
    public double limit(double c)
    {
        throw new UnsupportedOperationException(UNAVAILABLE_LIMIT);
    }

    /**
     * Testet, ob diese Funktion (genannt <i>f</i>) identisch mit dem
     * spezifizierten Objekt ist. Dazu gilt es, folgende Bedingungen
     * zu pruefen:
     * <ol>
     * <li>Das Objekt muss vom Typ
     * <code>PiecewiseConstantFunction</code> sein.</li>
     * <li>Wenn die vorstehende Bedingung zutrifft, so sei <i>g</i>
     * die Funktion, die durch das Objekt repraesentiert wird. Dann
     * muss fuer alle Argumente <i>x</i> gelten: <i>f</i>(<i>x</i>) =
     * <i>g</i>(<i>x</i>).</li>
     * <li>Die vorstehende Bedingung setzt voraus, dass die Argumente
     * beider Funktionen formal vom gleichen Typ sind. Alle in <i>f</i>
     * gueltigen Argumente muessen also auch in <i>g</i> gueltig
     * sein, und umgekehrt.</li>
     * </ol>
     * 
     * @param obj die zu testende Funktion
     * @return <code>true</code>, wenn alle drei genannten
     *         Bedingungen erfuellt sind, sonst <code>false</code>
     */
    public boolean equals(Object obj)
    {
        // nur andere stueckweise konstante Funktionen
        // koennen getestet werden:
        PiecewiseConstantFunction g = null;
        
        try
        {
            g = (PiecewiseConstantFunction) obj;
        } 
        catch(ClassCastException cce)
        { 
            return false;
        } 

        // beide Funktionen muessen gleiche Funktionswerte liefern und
        // ihre Funktionsbereiche muessen uebereinstimmen.
        // Dies ist gegeben, wenn ihre Argument-Arrays uebereinstimmen.
        g = (PiecewiseConstantFunction) obj;

        // TODO : verlgeiche beide Argument-Arrays elementweise

        return true;
    }

    /**
     * Liefert den Funktionswert fuer das spezifizierte <i>x</i>. Falls der
     * Funktionswert fuer das angegebene <i>x</i> nicht definiert ist, soll die
     * Methode den Wert <code>Double.NaN</code> zurueckliefern.
     *
     * @param x das Argument fuer diese Funktion
     * @return den Funktionswert
     */
    public double f(double x)
    {
        if(!isInDefinitionRange(x)) 
        { return Double.NaN; }

        for(int i = 0; i < _argument.length; ++i)
        {
            if(_argument[i].n[0].doubleValue() <= x && 
               _argument[i].n[1].doubleValue() > x) 
            { 
                return _argument[i].n[2].doubleValue(); 
            } 
        } 

        // kein passender Funktionswert gefunden. ( Sollte nicht passieren! )
        return Double.NaN;

    } //  f( double )

    /**
     * Liefert den Grenzwert <i>lim</i>[<i>x</i>-><i>c</i>](<i>f(<i>x</i>)</i>)
     * dieser Funktion <i>f</i> fuer <i>x</i> gegen <i>c</i>.
     * <p>
     * 
     * Die implementierte Funktion soll eine
     * <code>IllegalArgumentException</code> auswerfen, falls das
     * uebergebene Argument eine unkorrekte Form hat (wenn
     * beispielsweise die Zahl der Komponenten falsch ist).
     * 
     * @param c der Annaeherungsparameter fuer <i>x</i>, fuer den der
     *            Grenzwert gesucht ist
     * @return den Grenzwert dieser Funktion fuer <i>x</i> gegen <i>c</i>
     * @exception IllegalArgumentException falls <i>c</i> keine
     *                gueltige Form hat
     */
    public Argument limit(Argument c)
    {
        // gebe ein Argument mit 0 zurueck.
        return new Argument(0);
    }

    /**
     * Liefert die Definitionsmenge <i><b>D</b></i> dieser Funktion.
     *
     * @return die Definitionsmenge
     */
    public Domain domain()
    {
        return RealDomain.R;
    }

    /**
     * Berechnung der Umkehrfunktion: Fuer ein gegebenes <i>f</i>(<i>x</i>)
     * wird das noetige Argument errechnet.
     * <p>
     * 
     * Standardmaessig wird bei Aufruf eine
     * <code>UnsupportedOperationException</code> ausgeworfen, dies
     * bedeutet, dass keine Umkehrfunktion definiert ist. Diese
     * Methode sollte ueberschrieben werden, wenn die implementierte
     * Funktion umkehrbar ist.<br>
     * Falls die Umkehrfunktion fuer das gegebene <i>f</i>(<i>x</i>)
     * nicht eindeutig ist, soll das undefinierte Argument
     * {@link Argument#NOT_DEFINED Argument.NOT_DEFINED}
     * zurueckgegeben werden.<br>
     * Die implementierte Funktion soll eine
     * <code>IllegalArgumentException</code> auswerfen, falls das
     * uebergebene Argument eine unkorrekte Form hat (wenn
     * beispielsweise die Zahl der Komponenten falsch ist).
     * 
     * @param y das Argument der Umkehrfunktion dieser Funktion
     * @return das Argument, das an diese Funktion uebergeben werden
     *         muesste, um den Funktionswert <code>y</code> zu
     *         erhalten
     * @exception UnsupportedOperationException falls zu dieser
     *                Funktion keine Umkehrfunktion definiert ist
     * @exception IllegalArgumentException falls <i>x</i> keine
     *                gueltige Form hat
     */
    public Argument inverse(Argument y)
    {
        throw new UnsupportedOperationException(UNDEFINED_INVERSE);
    } // inverse

    /**
     * Berechnung der Umkehrfunktion: Fuer ein gegebenes <i>f</i>(<i>x</i>)
     * wird das noetige Argument errechnet. Diese Methode liefert im
     * Gegensatz zu {@link #inverse(Argument) inverse(Argument y)}
     * alle Loesungen fuer die Umkehrfunktion in einem dynamischen
     * Array.
     * <p>
     * 
     * Standardmaessig wird bei Aufruf eine
     * <code>UnsupportedOperationException</code> ausgeworfen, dies
     * bedeutet, dass keine Umkehrfunktion definiert ist. Diese
     * Methode sollte ueberschrieben werden, wenn die implementierte
     * Funktion umkehrbar ist.<br>
     * Die implementierte Funktion soll eine
     * <code>IllegalArgumentException</code> auswerfen, falls das
     * uebergebene Argument eine unkorrekte Form hat (wenn
     * beispielsweise die Zahl der Komponenten falsch ist).<br>
     * Falls die Umkehrfunktion fuer das gegebene <i>f</i>(<i>x</i>)
     * nicht definiert ist, soll das Array aus einem einzigen Argument
     * bestehen, dem undefinierten Argument
     * {@link Argument#NOT_DEFINED Argument.NOT_DEFINED}.
     * 
     * @param y das Argument der Umkehrfunktion dieser Funktion
     * @return ein dynamisches Array aller Argumente <i>x</i>, die
     *         eingesetzt in diese Funktion den spezifizierten
     *         Funktionswert <code>y</code> ergeben
     * @exception UnsupportedOperationException falls zu dieser
     *                Funktion keine Umkehrfunktion definiert ist
     * @exception IllegalArgumentException falls <i>x</i> keine
     *                gueltige Form hat
     */
    public DynamicArray inverseAll(Argument y)
    {
        throw new UnsupportedOperationException(UNDEFINED_INVERSE);
    } // inverseAll

    /**
     * Berechnet die Schnittmenge dieser Funktion mit der
     * spezifizierten Funktion. Zurueckgeliefert werden alle
     * Schnittobjekte ueber der gemeinsamen Definitionsmenge in einem
     * dynamischen Array.<br>
     * Schnittobjekte sind entweder die Argumente <i>x</i>, die bei
     * den Funktionen denselben Funktionswert <i>f</i>(<i>x</i>)
     * hervorrufen, oder der Funktionsgraph selbst, teilweise oder
     * ganz. Das Array enthaelt somit die <code>Argument</code>-Objekte
     * der betreffenden Argumente (nicht notwendigerweise sortiert)
     * und das <code>Function</code>-Objekt der Funktion, welches
     * innerhalb des Arrays "eingeschlossen" wird von den beiden
     * Argumenten, die den Teil der Funktion abgrenzen, der in der
     * Schnittmenge liegt. Diese Funktion ist im Allgemeinen eine der
     * Schnittfunktionen; dies ist aber nicht zwingend. Dazu ein paar
     * Beispiele:
     * <p>
     * 
     * <ol>
     * <li>Eine reelle Funktion, geschnitten mit sich selbst, liefert
     * immer das Array {{@link Argument#NEGATIVE_INFINITY NEGATIVE_INFINITY},
     * <i>f</i>, {@link Argument#POSITIVE_INFINITY POSITIVE_INFINITY}}.</li>
     * <li>Zwei reelle lineare Funktionen mit verschiedener Steigung
     * liefern genau ein Schnittobjekt: <code>{</code><i>x</i><code>}</code></li>
     * <li>Die reelle Sinusfunktion geschnitten mit der reellen
     * Kosinusfunktion liefert theoretisch eine unendliche Menge an
     * diskreten Schnittargumenten <code>{..., </code><i>x<sub>k</i>-1</sub><code>,
     * </code><i>x<sub>k</sub></i><code>, </code><i>x<sub>k</i>+1</sub><code>,
     * ...}</code>.
     * Diese unendliche Menge koennte jedoch mit der Menge 
     * {{@link Argument#NEGATIVE_INFINITY NEGATIVE_INFINITY},
     * <i>enum</i>,
     * {@link Argument#POSITIVE_INFINITY POSITIVE_INFINITY}}
     * nachgebildet werden, wobei <i>enum</i> eine
     * Aufzaehlungsfunktion fuer die Schnittargumente waere mit der
     * Definitionsmenge der ganzen Zahlen.</li>
     * </ol>
     * <p>
     * 
     * Standardmaessig wird bei Aufruf eine
     * <code>UnsupportedOperationException</code> ausgeworfen, dies
     * bedeutet, dass die Schnittmengenberechnung nicht implementiert
     * ist.<br>
     * Die implementierte Funktion soll eine
     * <code>IllegalArgumentException</code> auswerfen, falls die
     * uebergebene Funktion nicht auf Schnitt getestet werden kann.<br>
     * Falls es keine Schnittobjekte gibt, soll das Array leer sein.
     * 
     * @param g die Schnittfunktion
     * @return ein dynamisches Array aller Schnittobjekte
     * @exception UnsupportedOperationException falls zu dieser
     *                Funktion keine Schnittberechnung implementiert
     *                ist
     * @exception IllegalArgumentException falls <code>g</code>
     *                nicht mit dieser Funktion auf Schnitt getestet
     *                werden kann
     */
    public DynamicArray intersection(Function g)
    {
        throw new UnsupportedOperationException(UNAVAILABLE_INTERSECTION);
    } // intersection

    /**
     * Berechnet die Menge der Unstetigkeitsstellen dieser Funktion.
     * Zurueckgeliefert werden alle Stellen aus der Definitionsmenge, an der der
     * Funktionsgraph nicht stetig ist. Das bedeutet, dass Intervallgrenzen nicht
     * als Unstetigkeitsstellen betrachtet werden, Unstetigkeiten koennen nur
     * innerhalb eines Definitionsintervalls liegen.<br>
     * Falls es keine Unstetigkeitsstellen gibt, soll das Array
     * leer sein.
     *
     * @return ein dynamisches Array aller Unstetigkeitsstellen
     */
    public DynamicArray getDiscontinuities()
    {
        if(_discontinuities == null)
        {
            _discontinuities = new DynamicArray();
            Bound bound;

            for(int i = 0; i < _argument.length; i++)
            {
                bound = new Bound(_argument[i].n[0].doubleValue(), true);
                _discontinuities.add(bound);
            }
        }

        return _discontinuities;

    } // getDiscontinuities

    /**
     * Liefert das Argument-Array dieser Funktion. Dieses Array
     * speichert die Unstetigkeitsstellen sowie die Funktionswerte
     * der Funktion.
     *
     * @return das Argument-Array
     */
    public Argument[] getArgument()
    {
        return _argument;
    }

    /**
     * Setzt das Argument-Array dieser Funktion. Dieses Array
     * speichert die Unstetigkeitsstellen sowie die Funktionswerte
     * der Funktion.
     */
    public void setArgument(Argument[] args)
    {
        this._argument = args;
    }

} // Function
