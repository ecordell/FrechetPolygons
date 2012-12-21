
package anja.analysis;


/**
 * Dieses Schnittstelle spezifiziert Definitionsmengen von Funktionen.
 * 
 * @version 0.2 20.03.2004
 * @author Sascha Ternes
 */

public interface Domain
{

    // ***********************************************************************
    //                               Public constants
    // ***********************************************************************

    /**
     * Konstante fuer die leere Definitionsmenge
     */
    public static final int    EMPTY_DOMAIN    = 0;

    /**
     * Exception bei den Methoden
     * {@link #intersection(Domain) intersection} und
     * {@link #union(Domain) union}, wenn die beteiligten Mengen
     * nicht die gleiche Dimensionalitaet haben
     */
    public static final String WRONG_DIMENSION = 
     "parameter set has wrong dimension!";


    // ***********************************************************************
    // Public methods
    // ***********************************************************************

    /**
     * Erzeugt eine Kopie dieser Definitionsmenge.
     * 
     * @return eine Kopie
     */
    abstract public Object clone();


    /**
     * Testet, ob das spezifizierte Argument ein Element dieser
     * Definitionsmenge ist.
     * 
     * @param x das Testargument
     * @return <code>true</code> falls das Testargument Element
     *         dieser Definitionsmenge ist, sonst <code>false</code>
     */
    abstract public boolean contains(Argument x);


    /**
     * Liefert die Dimensionalitaet dieser Definitionsmenge.
     * 
     * @return die Zahl der Dimensionen dieser Definitionsmenge
     */
    abstract public int dimension();


    /**
     * Vergleicht diese Definitionsmenge mit der spezifizierten und
     * liefert das Ergebnis.
     * 
     * @param o die zu vergleichende Definitionsmenge
     * @return <code>true</code>, falls beide Definitionsmengen
     *         gleich sind, sonst <code>false</code>
     */
    abstract public boolean equals(Object o);


    /**
     * Liefert eine neue Definitionsmenge, die die Schnittmenge aus
     * dem Schnitt dieser Definitionsmenge mit der spezifizierten
     * Definitionsmenge ist. Falls die beiden Mengen nicht die gleiche
     * Dimensionalitaet aufweisen, wird eine
     * <code>IllegalArgumentException</code> ausgeworfen.
     * 
     * @param domain die zwei Definitionsmenge fuer den Schnitt
     * @return die Schnittmenge
     * @exception IllegalArgumentException falls die Dimensionen
     *                beider Mengen unterschiedlich sind
     */
    abstract public Domain intersection(Domain domain);


    /**
     * Testet auf die leere Menge und liefert das Testergebnis.
     * 
     * @return <code>true</code>, falls diese Menge leer ist, sonst
     *         <code>false</code>
     */
    abstract public boolean isEmpty();


    /**
     * Testet, ob das spezifizierte Argument formal ein Element dieser
     * Definitionsmenge sein koennte. Das bedeutet:
     * <ul>
     * <li>Das Argument ist nicht <code>null</code>.</li>
     * <li>Das Argument ist nicht das <i>undefinierte Argument</i>
     * {@link Argument#NOT_DEFINED Argument.NOT_DEFINED}.</li>
     * <li>Die Anzahl der Komponenten des Arguments ist korrekt.</li>
     * <li>Keine der Komponenten des Arguments ist null.</li>
     * </ul>
     * <li>Die Komponenten des Arguments weisen jeweils den richtigen
     * Typ auf.</li>
     * </ul>
     * 
     * @param x das Testargument
     * @return <code>true</code> falls das Testargument Element
     *         dieser Definitionsmenge ist, sonst <code>false</code>
     */
    abstract public boolean isValidArgument(Argument x);


    /**
     * Liefert die Vereinigungsmenge aus dieser Definitionsmenge und
     * der spezifizierten Definitionsmenge. Falls die beiden Mengen
     * nicht die gleiche Dimensionalitaet aufweisen, wird eine
     * <code>IllegalArgumentException</code> ausgeworfen.
     * 
     * @param domain die zweite Definitionsmenge fuer die Vereinigung
     * @return die Vereinigungsmenge
     * @exception IllegalArgumentException falls die Dimensionen
     *                beider Mengen unterschiedlich sind
     */
    abstract public Domain union(Domain domain);

} // Domain
