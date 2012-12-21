
package anja.analysis;


import anja.util.DynamicArray;

/**
 * Abstrakte allgemeine Funktion <i>f</i>(<i>x</i>) mit freier
 * Definitions- und Wertemenge. Als Argument <i>x</i> und
 * Funktionsergebnis <i>f</i>(<i>x</i>) dienen Objekte des Typs
 * <code>Argument</code>.
 * 
 * @version 0.7 27.04.2004
 * @author Sascha Ternes
 */

public abstract class Function implements Cloneable
{

    // ***********************************************************************
    //                          Public constants
    // ***********************************************************************

    /**
     * Exception bei ungueltigem Argument
     */
    public static final String ILLEGAL_ARGUMENT  = "illegal argument given!";
    
    /**
     * Exception bei undefinierter Umkehrfunktion
     */
    public static final String UNDEFINED_INVERSE  = "inverse not defined!";
    
    /**
     * Exception bei undefinierter Schnittberechnung
     */
    public static final String UNAVAILABLE_INTERSECTION 
      = "intersection not available!";

    // ***********************************************************************
    //                          Protected variables
    // ***********************************************************************

    /**
     * die Definitionsmenge dieser Funktion
     */
    protected Domain           _domain;

    // ***********************************************************************
    //                           Public variables
    // ***********************************************************************

    /**
     * frei verfuegbare Bezeichnung dieser Funktion, mit
     * <code>null</code> initialisiert
     */
    public String              name;


    // ***********************************************************************
    //                           Public methods
    // ***********************************************************************

    /**
     * Testet, ob das spezifizierte Argument fuer diese Funktion
     * formal korrekt ist.<br>
     * Das Argument ist formal korrekt, wenn folgende Bedingungen
     * erfuellt sind:
     * <ul>
     * <li>Das Argument ist nicht <code>null</code>.</li>
     * <li>Das Argument ist nicht das <i>undefinierte Argument</i>
     * {@link Argument#NOT_DEFINED Argument.NOT_DEFINED}.</li>
     * <li>Die Anzahl der Komponenten des Arguments ist korrekt.</li>
     * <li>Keine der Komponenten des Arguments ist <code>null</code>.</li>
     * <li>Die Komponenten des Arguments weisen jeweils den richtigen
     * Typ auf.</li>
     * </ul>
     * 
     * @param x das zu testende Argument der Funktion
     * @return <code>true</code>, falls das Argument in Ordnung
     *         ist, sonst <code>false</code>
     */
    abstract public boolean isValidArgument(Argument x);


    /**
     * Erzeugt und liefert eine Kopie dieser Funktion.
     * 
     * @return eine Kopie dieser Funktion
     */
    abstract public Object clone();


    /**
     * Testet, ob diese Funktion (genannt <i>f</i>) dem
     * spezifizierten Objekt gleicht. Dazu gilt es, folgende
     * Bedingungen zu pruefen:
     * <ol>
     * <li>Das Objekt muss vom Typ <code>Function</code> sein.</li>
     * <li>Wenn die vorstehende Bedingung zutrifft, so sei <i>g</i>
     * die Funktion, die durch das Objekt repraesentiert wird. Dann
     * muss fuer alle Argumente <i>x</i> gelten: <i>f</i>(<i>x</i>) =
     * <i>g</i>(<i>x</i>).</li>
     * <li>Die vorstehende Bedingung setzt voraus, dass die Argumente
     * beider Funktionen formal vom gleichen Typ sind. Alle in <i>f</i>
     * gueltigen Argumente muessen also auch in <i>g</i> gueltig
     * sein, und umgekehrt.</li>
     * </ol>
     * <b>Achtung: Es wird nicht ueberprueft, ob beide Funktionen
     * identische Definitionsmengen besitzen!</b> Es kann also sein,
     * dass die Funktionskurven der Funktionen in keinem einzigen
     * Punkt uebereinstimmen. Um dieses Kriterium zusaetzlich zu
     * ueberpruefen gibt es die Methode
     * {@link #isCongruent(Function) isCongruent}.
     * 
     * @param obj die zu testende Funktion
     * @return <code>true</code>, wenn alle drei genannten
     *         Bedingungen erfuellt sind, sonst <code>false</code>
     * @see #isCongruent(Function)
     */
    abstract public boolean equals(Object obj);


    /**
     * Liefert den Funktionswert <i>f</i>(<i>x</i>) der Funktion
     * fuer das Argument <i>x</i>.
     * <p>
     * 
     * Die implementierte Funktion soll eine
     * <code>IllegalArgumentException</code> auswerfen, falls das
     * uebergebene Argument eine unkorrekte Form hat (wenn
     * beispielsweise die Zahl der Komponenten falsch ist).<br>
     * Falls die Funktion allerdings fuer ein formal korrektes
     * <code>x</code> nicht definiert ist, weil <i>x</i> nicht in
     * der Definitionsmenge der Funktion vorkommt, soll
     * {@link Argument#NOT_DEFINED Argument.NOT_DEFINED}
     * zurueckgeliefert werden.
     * 
     * @param x das Argument der Funktion
     * @return den Funktionswert <i>f</i>(<i>x</i>)
     * @exception IllegalArgumentException falls <i>x</i> keine
     *                gueltige Form hat
     */
    abstract public Argument f(Argument x);


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
    abstract public Argument limit(Argument c);


    /**
     * Berechnet die Menge der Unstetigkeitsstellen dieser Funktion.
     * Zurueckgeliefert werden alle Stellen aus der Definitionsmenge,
     * an der der Funktionsgraph nicht stetig ist. Das bedeutet, dass
     * Intervallgrenzen nicht als Unstetigkeitsstellen betrachtet
     * werden, Unstetigkeiten koennen nur innerhalb eines
     * Definitionsintervalls liegen.<br>
     * Unstetigkeitsstellen sind die Argumente <i>x</i><sub>dis</sub>,
     * an denen diese Funktion unstetig ist zu allen <i>x</i> > 
     * <i>x</i><sub>dis</sub>.
     * Diese Argumente sind vom Typ {@link Bound Bound}, da neben der
     * Zahl <i>x</i><sub>dis</sub> noch ein
     * Zugehoerigkeitskriterium gebraucht wird, das festlegt, ob 
     * <i>f</i>(<i>x</i><sub>dis</sub>)
     * links oder rechts der Unstetigkeitsstelle liegt; im ersten Fall
     * ist das Argument eine <i>eingeschlossene Schranke</i>, und der
     * Funktionsgraph bis zu Stelle <i>x</i><sub>dis</sub> enthaelt
     * den Funktionswert, im zweiten Fall nicht.<br>
     * Falls es keine Unstetigkeitsstellen gibt, soll das Array leer
     * sein.
     * 
     * @return ein dynamisches Array aller Unstetigkeitsstellen
     */
    abstract public DynamicArray getDiscontinuities();


    /**
     * Liefert die Definitionsmenge <i><b>D</b></i> dieser
     * Funktion.
     * 
     * @return die Definitionsmenge
     */
    public Domain domain()
    {
        return _domain;
    } // domain


    /**
     * Testet, ob das spezifizierte Argument in der Definitionsmenge
     * dieser Funktion liegt.
     * 
     * @param x das zu testende Argument
     * @return <code>true</code>, wenn das Argument in der
     *         Definitionsmenge liegt, sonst <code>false</code>
     */
    public boolean isDefinedFor(Argument x)
    {
        return domain().contains(x);
    } // isDefinedFor


    /**
     * Prueft, ob diese Funktion der spezifizierten Funktion gleicht
     * im Sinne der Definition von {@link #equals(Object) equals}.
     * Zusaetzlich wird ueberprueft, ob die Definitionsmengen beider
     * Funktionen identisch sind; dann und nur dann liefert diese
     * Methode <code>true</code>.
     * 
     * @param function die zu testende Funktion
     * @return <code>true</code>, wenn die Funktionsgraphen der
     *         Funktionen exakt uebereinstimmen, sonst
     *         <code>false</code>
     * @see #equals(Object)
     */
    public boolean isCongruent(Function function)
    {
        return equals(function) && function.domain().equals(this.domain());
    } // isCongruent


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
     * ganz. Das Array enthaelt somit im ersten Fall die
     * <code>Argument</code>-Objekte der betreffenden Argumente
     * (nicht notwendigerweise sortiert) und im zweiten Fall das
     * <code>Function</code>-Objekt der Funktion, welches innerhalb
     * des Arrays "eingeschlossen" wird von den beiden
     * Intervallgrenzen (<code>Bound</code>-Objekte), die den Teil
     * der Funktion abgrenzen, der in der Schnittmenge liegt. Diese
     * Funktion ist im Allgemeinen eine der Schnittfunktionen; dies
     * ist aber nicht zwingend. Dazu ein paar Beispiele:
     * <p>
     * 
     * <ol>
     * <li>Eine reelle Funktion ueber <b><i>R</i></b>, geschnitten
     * mit sich selbst, liefert immer das Array 
     * {[{@link Argument#NEGATIVE_INFINITY NEGATIVE_INFINITY},
     * <i>f</i>, [{@link Argument#POSITIVE_INFINITY POSITIVE_INFINITY}}
     * mit dem formalen Aufbau {<code>Bound</code>,
     * <code>Function</code>, <code>Bound</code>}.</li>
     * <li>Die reelle Funktion <i>f</i>(<i>x</i>) = 1/<i>x</i>
     * mit einer Definitionsluecke bei <i>x</i>=0, geschnitten mit
     * sich selbst, liefert das Array 
     * {[{@link Argument#NEGATIVE_INFINITY NEGATIVE_INFINITY},
     * <i>f</i>, ]0, ]0, <i>f</i>, [
     * {@link Argument#POSITIVE_INFINITY POSITIVE_INFINITY}},
     * formal {<code>Bound</code>, <code>Function</code>,
     * <code>Bound</code>, <code>Bound</code>,
     * <code>Function</code>, <code>Bound</code>}.</li>
     * <li>Zwei reelle lineare Funktionen mit verschiedener Steigung
     * liefern genau ein Schnittobjekt: <code>{</code><i>x</i><code>}</code>;
     * hierbei ist <i>x</i> formal ein <code>Argument</code>.</li>
     * <li>Die reelle Sinusfunktion geschnitten mit der reellen
     * Kosinusfunktion liefert theoretisch eine unendliche Menge an
     * diskreten Schnittargumenten <code>{..., 
     * </code><i>x<sub>k</i>-1</sub><code>,
     * </code><i>x<sub>k</sub></i><code>, </code><i>x<sub>k</i>+1</sub><code>,
     * ...}</code>,
     * was formal dem Aufbau {..., <code>Argument</code>,
     * <code>Argument</code>, <code>Argument</code>, ...}
     * entsprechen wuerde. Diese unendliche Menge koennte jedoch mit
     * der Menge {[{@link Argument#NEGATIVE_INFINITY NEGATIVE_INFINITY},
     * <i>enum</i>, [{@link Argument#POSITIVE_INFINITY POSITIVE_INFINITY}}
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
     * Liefert den Namen dieser Funktion, falls er existiert, sonst wird
     * <code>Object.toString()</code> aufgerufen.
     *
     * @return den Namen dieser Funktion
     */
    public String toString()
    {
        if(name != null) 
         return name;
        
        return super.toString();

    } // toString

} // Function
