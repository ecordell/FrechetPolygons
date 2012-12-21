
package anja.analysis;


/**
 * Universelle <i>Schranke</i>, die z.B. in Intervallen verwendet
 * wird. Die Eigenschaften einer solchen Schranke sind:
 * <ol>
 * <li>Der Schrankenwert ist ein {@link Argument} und kann somit
 * mehrdimensional sein.</li>
 * <li>Alle Komponenten dieses Arguments muessen vom Typ
 * <code>Number</code> sein (nach Definition von
 * <code>Argument</code>) <b>und zusaetzlich</b> das Interface
 * <code>Comparable</code> implementieren, also sortierbar sein
 * (z.B. ist dies fuer die Klasse <code>Double</code> der Fall).</li>
 * <li>Den Komponenten des Schrankenwerts ist jeweils eine
 * Eigenschaft zugeordnet, die bestimmt, ob die Komponente selbst in
 * der durch die Schranke definierten Menge enthalten ist oder nicht.
 * Dies ist z.B. bei Intervallen von Bedeutung, die <i>offen</i>,
 * <i>halboffen</i> oder <i>geschlossen</i> sein koennen.</li>
 * </ol>
 * 
 * @version 0.9 06.04.2004
 * @author Sascha Ternes
 */

public class Bound extends Argument implements Cloneable
{

    // ***********************************************************************
    // Public constants
    // ***********************************************************************

    /**
     * eingeschlossene Schrankenwertkomponente; die Komponente gehoert
     * zum durch die Schranke definierten Bereich
     */
    public static final boolean INCLUSIVE             = true;

    /**
     * ausgeschlossene Schrankenwertkomponente; die Komponente gehoert
     * nicht zum durch die Schranke definierten Bereich
     */
    public static final boolean EXCLUSIVE             = !INCLUSIVE;

    /**
     * vordefinierte reelle Schranke fuer negative Unendlichkeit,
     * inklusiv
     */
    public static final Bound   NEGATIVE_INFINITY     = new Bound(
                                                      Double.NEGATIVE_INFINITY,
                                                      INCLUSIVE);

    /**
     * vordefinierte reelle Schranke fuer positive Unendlichkeit,
     * inklusiv
     */
    public static final Bound   POSITIVE_INFINITY     = new Bound(
                                                      Double.POSITIVE_INFINITY,
                                                      INCLUSIVE);

    /**
     * Exception bei ungueltigen Argumenten, deren Typ nicht das
     * Interface <code>Comparable</code> implementiert
     */
    public static final String  UNCOMPARABLE_BOUND    
     = "bound value contains uncomparable components!";

    /**
     * Exception bei Konstruktoraufrufen, wenn die
     * Zugehoerigkeitsvariable falsch dimensioniert ist
     */
    public static final String  MISMATCHING_INCLUSION 
     = "inclusion value size does not match bound value size!";

    /**
     * Exception der Methode <code>findArgumentBetween</code>, wenn
     * die Dimensionaliaet der Parameter nicht uebereinstimmt
     */
    public static final String  MISMATCHING_DIMENSION 
     = "bounds' dimension does not match!";

    // *************************************************************************
    // Public variables
    // *************************************************************************

    /**
     * die Zugehoerigkeit der Schrankenwertkomponenten
     */
    public boolean[] inclusion;


    // *************************************************************************
    // Constructors
    // *************************************************************************

    /**
     * Erzeugt eine neue eindimensionale Schranke mit der reellen Zahl
     * Null, inklusiv.
     */
    public Bound()
    {

        super();
        inclusion = new boolean[1];
        inclusion[0] = INCLUSIVE;

    } // Bound


    /**
     * Erzeugt eine neue eindimensionale ganzzahlige Schranke mit dem
     * spezifizierten Wert und der Zugehoerigkeitseigenschaft.
     * 
     * @param value der Schrankenwert
     * @param inclusion die Zugehoerigkeit des Schrankenwerts zum
     *            definierten Bereich der Schranke; hierfuer stehen
     *            auch die Konstanten {@link #INCLUSIVE INCLUSIVE} und
     *            {@link #EXCLUSIVE EXCLUSIVE} zur Verfuegung
     */
    public Bound(int value, boolean inclusion)
    {

        super(value);
        this.inclusion = new boolean[1];
        this.inclusion[0] = inclusion;

    } // Bound


    /**
     * Erzeugt eine neue mehrdimensionale ganzzahlige Schranke mit dem
     * spezifizierten Wert und der Zugehoerigkeitseigenschaft. Falls
     * die Dimensionen der beiden Parameter unterschiedlich sind, wird
     * eine <code>IllegalArgumentException</code> ausgeworfen.
     * 
     * @param value der Schrankenwert
     * @param inclusion die Zugehoerigkeit der
     *            Schrankenwertkomponenten zum definierten Bereich der
     *            Schranke; hierfuer stehen auch die Konstanten
     *            {@link #INCLUSIVE INCLUSIVE} und
     *            {@link #EXCLUSIVE EXCLUSIVE} zur Verfuegung
     * @exception IllegalArgumentException falls die Dimension von
     *                <code>inclusion</code> nicht mit der von
     *                <code>value</code> uebereinstimmt
     */
    public Bound(int[] value, boolean[] inclusion)
    {

        super(value);
        if(value.length != inclusion.length) 
            throw new IllegalArgumentException(MISMATCHING_INCLUSION);
        
        this.inclusion = inclusion;

    } // Bound


    /**
     * Erzeugt eine neue eindimensionale reelle Schranke mit dem
     * spezifizierten Wert und der Zugehoerigkeitseigenschaft.
     * 
     * @param value der Schrankenwert
     * @param inclusion die Zugehoerigkeit des Schrankenwerts zum
     *            definierten Bereich der Schranke; hierfuer stehen
     *            auch die Konstanten {@link #INCLUSIVE INCLUSIVE} und
     *            {@link #EXCLUSIVE EXCLUSIVE} zur Verfuegung
     */
    public Bound(double value, boolean inclusion)
    {

        super(value);
        this.inclusion = new boolean[1];
        this.inclusion[0] = inclusion;

    } // Bound


    /**
     * Erzeugt eine neue mehrdimensionale reelle Schranke mit dem
     * spezifizierten Wert und der Zugehoerigkeitseigenschaft. Falls
     * die Dimensionen der beiden Parameter unterschiedlich sind, wird
     * eine <code>IllegalArgumentException</code> ausgeworfen.
     * 
     * @param value der Schrankenwert
     * @param inclusion die Zugehoerigkeit der
     *            Schrankenwertkomponenten zum definierten Bereich der
     *            Schranke; hierfuer stehen auch die Konstanten
     *            {@link #INCLUSIVE INCLUSIVE} und
     *            {@link #EXCLUSIVE EXCLUSIVE} zur Verfuegung
     * @exception IllegalArgumentException falls die Dimension von
     *                <code>inclusion</code> nicht mit der von
     *                <code>value</code> uebereinstimmt
     */
    public Bound(double[] value, boolean[] inclusion)
    {

        super(value);
        if(value.length != inclusion.length) 
         throw new IllegalArgumentException(MISMATCHING_INCLUSION);
        
        this.inclusion = inclusion;

    } // Bound


    /**
     * Erzeugt eine neue eindimensionale Schranke mit dem
     * spezifizierten Wert und der Zugehoerigkeitseigenschaft. Der
     * Klassentyp des Schrankenwerts muss das Interface
     * <code>Comparable</code> implementieren, sonst wird eine
     * <code>IllegalArgumentException</code> ausgeworfen.
     * 
     * @param value der Schrankenwert
     * @param inclusion die Zugehoerigkeit des Schrankenwerts zum
     *            definierten Bereich der Schranke; hierfuer stehen
     *            auch die Konstanten {@link #INCLUSIVE INCLUSIVE} und
     *            {@link #EXCLUSIVE EXCLUSIVE} zur Verfuegung
     * @exception IllegalArgumentException falls der Schrankenwert
     *                nicht vom Interfacetyp <code>Comparable</code>
     *                ist
     */
    public Bound(Number value, boolean inclusion)
    {

        super(value);
        this.inclusion = new boolean[1];
        this.inclusion[0] = inclusion;
        
        // auf Comparable pruefen:
        if(!(value instanceof Comparable))     
         throw new IllegalArgumentException(UNCOMPARABLE_BOUND);

    } // Bound


    /**
     * Erzeugt eine neue mehrdimensionale Schranke mit dem
     * spezifizierten Wert und der Zugehoerigkeitseigenschaft. Der
     * Klassentyp aller Komponenten des Schrankenwerts muss das
     * Interface <code>Comparable</code> implementieren, sonst wird
     * eine <code>IllegalArgumentException</code> ausgeworfen. Falls
     * die Dimensionen der beiden Parameter unterschiedlich sind, wird
     * ebenfalls eine <code>IllegalArgumentException</code>
     * ausgeworfen.
     * 
     * @param value der Schrankenwert
     * @param inclusion die Zugehoerigkeit der
     *            Schrankenwertkomponenten zum definierten Bereich der
     *            Schranke; hierfuer stehen auch die Konstanten
     *            {@link #INCLUSIVE INCLUSIVE} und
     *            {@link #EXCLUSIVE EXCLUSIVE} zur Verfuegung
     * @exception IllegalArgumentException falls die Komponenten des
     *                Schrankenwerts nicht vom Interfacetyp
     *                <code>Comparable</code> sind oder die
     *                Dimension von <code>inclusion</code> nicht mit
     *                der von <code>value</code> uebereinstimmt
     */
    public Bound(Number[] value, boolean[] inclusion)
    {

        super(value);
        
        if(value.length != inclusion.length) 
            throw new IllegalArgumentException(MISMATCHING_INCLUSION);
        
        this.inclusion = inclusion;
        
        // auf Comparable pruefen:
        
        for(int i = 0; i < value.length; i++)
            if(!(value[i] instanceof Comparable)) 
             throw new IllegalArgumentException(UNCOMPARABLE_BOUND);

    } // Bound


    /**
     * Erzeugt eine neue Schranke mit dem spezifizierten Wert und der
     * Zugehoerigkeitseigenschaft. Der Klassentyp aller Komponenten
     * des Schrankenwerts muss das Interface <code>Comparable</code>
     * implementieren, sonst wird eine
     * <code>IllegalArgumentException</code> ausgeworfen. Falls die
     * Dimensionen der beiden Parameter unterschiedlich sind, wird
     * ebenfalls eine <code>IllegalArgumentException</code>
     * ausgeworfen.
     * 
     * @param value der Schrankenwert
     * @param inclusion die Zugehoerigkeit der
     *            Schrankenwertkomponenten zum definierten Bereich der
     *            Schranke; hierfuer stehen auch die Konstanten
     *            {@link #INCLUSIVE INCLUSIVE} und
     *            {@link #EXCLUSIVE EXCLUSIVE} zur Verfuegung
     * @exception IllegalArgumentException falls die Komponenten des
     *                Schrankenwerts nicht vom Interfacetyp
     *                <code>Comparable</code> sind oder die
     *                Dimension von <code>inclusion</code> nicht mit
     *                der von <code>value</code> uebereinstimmt
     */
    public Bound(Argument value, boolean[] inclusion)
    {

        super(value);
        
        if(value.n.length != inclusion.length) 
         throw new IllegalArgumentException(MISMATCHING_INCLUSION);
        
        this.inclusion = inclusion;
        
        // auf Comparable pruefen:
        for(int i = 0; i < value.dimension(); i++)
         if(!(value.n[i] instanceof Comparable)) 
          throw new IllegalArgumentException(UNCOMPARABLE_BOUND);

    } // Bound


    // *************************************************************************
    // Public methods
    // *************************************************************************

    /**
     * Erzeugt eine Kopie dieser Schranke.
     * 
     * @return die Kopie
     */
    public Object clone()
    {
        Number[] n2 = (Number[]) n.clone();
        
        boolean[] i2 = new boolean[inclusion.length];
        
        for(int i = 0; i < i2.length; i++)
         i2[i] = inclusion[i];
        
        return new Bound(n2, i2);

    } // clone


    /**
     * Vergleicht diese Schranke mit der spezifizierten und liefert
     * das Testergebnis.
     * 
     * @param o die zu vergleichende Schranke
     * @return <code>true</code>, falls die Schranken identisch
     *         sind, sonst <code>false</code>
     */
    public boolean equals(Object o)
    {

        if(!super.equals(o)) 
         return false;
        
        if(this.inclusion.length != ((Bound) o).inclusion.length) 
         return false;
        
        for(int i = 0; i < inclusion.length; i++)
         if(((Bound) o).inclusion[i] != this.inclusion[i]) 
          return false;
        
        return true;

    } // equals


    /*
     * [javadoc-Beschreibung wird aus Argument kopiert]
     */
    public String toString()
    {

        String s = "";
        
        for(int i = 0; i < inclusion.length; i++)
         if(inclusion[i]) 
           s += "[";
        else 
           s += "]";
        
        if(n.length > 1) 
         s += "(";
        
        for(int i = 0; i < n.length; i++)
        {
            s += n[i];
            
            if(i < n.length - 1) 
             s += ", ";
        } 
        
        if(n.length > 1) 
         s += ")";
        
        return s;

    } // toString

} // Bound
