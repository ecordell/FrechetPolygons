
package anja.analysis;


import anja.util.DynamicArray;

/**
 * Offenes, halboffenes oder geschlossenes Intervall, bestehend aus
 * zwei {@link Bound}-Objekten.
 * 
 * @version 0.9 17.04.2004
 * @author Sascha Ternes
 */

public class Interval implements Cloneable
{

    // *************************************************************************
    // Public constants
    // *************************************************************************

    /**
     * vordefiniertes geschlossenes Interval ueber allen reellen
     * Zahlen
     */
    public static final Interval REAL_NUMBERS  = new Interval();

    /**
     * Exception bei ungueltigem Argument, dessen Typ nicht das
     * Interface <code>Comparable</code> implementiert
     */
    public static final String   UNCOMPARABLE_COMPONENTS 
     = "argument contains uncomparable components!";

    /**
     * Exception bei DynamicArray, das keine gueltige Schnittmenge ist
     */
    public static final String   INVALID_INTERSECTION  
     = "given DynamicArray is not a valid intersection!";

    // *************************************************************************
    // Private variables
    // *************************************************************************

    // linke und rechte Grenze:
    private Bound                _bound1;
    private Bound                _bound2;

    // *************************************************************************
    // Constructors
    // *************************************************************************

    /**
     * Erzeugt das reelle eindimensionale geschlossene unendliche
     * Intervall ueber allen reellen Zahlen.
     */
    public Interval()
    {
        this(Bound.NEGATIVE_INFINITY, Bound.POSITIVE_INFINITY);
    } // Interval


    /**
     * Erzeugt ein neues Intervall mit den spezifizierten Grenzen.<br>
     * Um vollstaendig korrekte Grenzen zu erhalten, werden die
     * Komponenten der "linken" und "rechten" Grenze ggf. vertauscht,
     * so dass fuer jedes Intervall <i>I</i> = [(<i>l</i><sub>1</sub>,
     * <i>l</i><sub>2</sub>, ..., <i>l<sub>n</sub></i>); (<i>r</i><sub>1</sub>,
     * <i>r</i><sub>2</sub>, ..., <i>r<sub>n</sub></i>)] die
     * Bedingung <i>l<sub>i</sub></i> <= <i>r<sub>i</sub></i>
     * gilt. <b>Die <code>Bound</code>-Objekte werden also unter
     * Umstaenden veraendert!</b>
     * <p>
     * 
     * Falls die uebergebenen Schranken eine unterschiedliche
     * Dimensionalitaet besitzen, wird eine
     * {@link IncompatibleBoundsException} ausgeworfen.
     * 
     * @param bound1 die "linke" Grenze
     * @param bound2 die "rechte" Grenze
     * @exception IncompatibleBoundsException falls die Dimensionen
     *                der beiden Grenzen unterschiedlich sind
     */
    public Interval(Bound bound1, Bound bound2)
    {

        // Dimensionalitaet pruefen:
        if(bound1.dimension() != bound2.dimension()) 
         throw new IncompatibleBoundsException();
        
        _bound1 = bound1;
        _bound2 = bound2;
        
        Number d;
        boolean b;
        
        // ggf. Komponenten tauschen:
        
        for(int i = 0; i < bound1.dimension(); i++)
        {
            if(((Comparable) bound2.n[i]).compareTo(bound1.n[i]) < 0)
            {
                d = _bound1.n[i];
                b = _bound1.inclusion[i];
                
                _bound1.n[i] = _bound2.n[i];
                _bound1.inclusion[i] = _bound2.inclusion[i];
                
                _bound2.n[i] = d;
                _bound2.inclusion[i] = b;
            } 
        }
    } // Interval


    // *************************************************************************
    // Public methods
    // *************************************************************************

    /**
     * Erzeugt eine Kopie dieses Intervalls.
     * 
     * @return die Kopie
     */
    public Object clone()
    {
        return new Interval((Bound) _bound1.clone(), 
                            (Bound) _bound2.clone());
    } // clone


    /**
     * Testet, ob dieses Intervall mit dem spezifizierten
     * uebereinstimmt.
     * 
     * @param o ein Intervall
     * @return <code>true</code>, wenn die Intervalle gleich sind,
     *         sonst <code>false</code>.
     */
    public boolean equals(Object o)
    {
        Interval i = (Interval) o;
        
        return i._bound1.equals(this._bound1) && 
               i._bound2.equals(this._bound2);
    } // equals


    /**
     * Testet fuer eindimensionale Intervalle, ob dieses Intervall
     * <i>geschlossen</i> ist. Wenn dieses Intervall mehrdimensional
     * ist, wird lediglich die nullte Dimension getestet.
     * 
     * @return <code>true</code>, wenn das Intervall geschlossen
     *         ist
     */
    public boolean isClosed()
    {
        return _bound1.inclusion[0] && _bound2.inclusion[0];
    } // isClosed()


    /**
     * Testet fuer eindimensionale Intervalle, ob dieses Intervall
     * <i>links offen</i> ist. Wenn dieses Intervall mehrdimensional
     * ist, wird lediglich die nullte Dimension getestet.
     * 
     * @return <code>true</code>, wenn das Intervall links offen
     *         ist
     */
    public boolean isLeftOpen()
    {
        return (!_bound1.inclusion[0]) && _bound2.inclusion[0];
    } // isLeftOpen()


    /**
     * Testet fuer eindimensionale Intervalle, ob dieses Intervall
     * <i>rechts offen</i> ist. Wenn dieses Intervall mehrdimensional
     * ist, wird lediglich die nullte Dimension getestet.
     * 
     * @return <code>true</code>, wenn das Intervall rechts offen
     *         ist
     */
    public boolean isRightOpen()
    {
        return _bound1.inclusion[0] && (!_bound2.inclusion[0]);
    } // isRightOpen()


    /**
     * Testet fuer eindimensionale Intervalle, ob dieses Intervall
     * <i>halboffen</i> ist. Wenn dieses Intervall mehrdimensional
     * ist, wird lediglich die nullte Dimension getestet.
     * 
     * @return <code>true</code>, wenn das Intervall halboffen ist
     */
    public boolean isHalfOpen()
    {
        return isLeftOpen() || isRightOpen();
    } // isHalfOpen()


    /**
     * Testet fuer eindimensionale Intervalle, ob dieses Intervall
     * <i>offen</i> ist. Wenn dieses Intervall mehrdimensional ist,
     * wird lediglich die nullte Dimension getestet.
     * 
     * @return <code>true</code>, wenn das Intervall offen ist
     */
    public boolean isOpen()
    {
        return (!_bound1.inclusion[0]) && (!_bound2.inclusion[0]);
    } // isOpen()


    /**
     * Testet, ob dieses Intervall degeneriert ist und lediglich einen
     * einzigen Punkt enthaelt. Dies ist genau dann der Fall, wenn das
     * Intervall <i>geschlossen</i> ist und fuer die Komponenten der
     * Intervallgrenzen I = [(<i>a</i><sub>1</sub>,
     * <i>a</i><sub>1</sub>,...,<i>a</i><sub>n</sub>); 
     * (<i>b</i><sub>1</sub>,<i>b</i><sub>1</sub>,...,<i>b</i><sub>n</sub>)]
     * gilt <i>a<sub>i</sub></i> = <i>b<sub>i</sub></i>.
     * 
     * @return <code>true</code>, falls das Intervall zu einem
     *         Punkt degeneriert ist, sonst <code>false</code>
     */
    public boolean isPoint()
    {
        return isClosed() && ((Argument) _bound1).equals((Argument) _bound2);
    } // isPoint


    /**
     * Prueft, ob das uebergebene Argument in diesem Intervall liegt.
     * Das Argument muss die gleiche Dimensionalitaet aufweisen wie
     * dieses Intervall, und alle seine Komponenten muessen das
     * Interface <code>Comparable</code> implementieren, sonst wird
     * eine <code>IllegalArgumentException</code> ausgeworfen.
     * 
     * @param x das zu testende Argument
     * @return <code>true</code>, wenn das Argument im Intervall
     *         liegt
     * @exception IllegalArgumentException wenn das Argument die
     *                falsche Dimensionalitaet hat oder seine
     *                Komponenten nicht vom Typ
     *                <code>Comparable</code> sind
     */
    public boolean contains(Argument x)
    {

        // Dimensionalitaet pruefen:
        if(x.dimension() != _bound1.dimension()) 
        { 
            throw new IllegalArgumentException(Argument.WRONG_DIMENSION); 
        } 

        Comparable a = null;
        int test;
        
        for(int i = 0; i < x.dimension(); i++)
        {
            // Vergleichbarkeit testen:
            try
            {
                a = (Comparable) x.n[i];
                
                // Vergleich durchfuehren:
                test = ((Comparable) _bound1.n[i]).compareTo(a);
                
                if(test > 0) 
                 return false; // Argument liegt ausserhalb
                
                else if((test == 0) && (!_bound1.inclusion[i])) 
                 return false;  // Argument
                                // liegt
                                // auf
                                // einer
                                // Exklusivgrenze
                
                test = ((Comparable) _bound2.n[i]).compareTo(a);
                
                if(test < 0)
                 return false; // Argument liegt ausserhalb
                
                else if((test == 0) && (!_bound2.inclusion[i])) 
                 return false;  // Argument
                                // liegt
                                // auf
                                // einer
                                // Exklusivgrenze
            } 
            catch(ClassCastException cce)
            { 
                // try
                throw new IllegalArgumentException(UNCOMPARABLE_COMPONENTS);
            } 
        }
        return true;
    } 


    /**
     * Prueft, ob das spezifizierte Intervall vollstaendig in diesem
     * Intervall liegt. Die Intervalle muessen die gleiche
     * Dimensionalitaet aufweisen und ihre Grenzen muessen
     * untereinander vergleichbar sein, sonst wird eine
     * <code>IllegalArgumentException</code> ausgeworfen.
     * 
     * @param interval das zu testende Intervall
     * @return <code>true</code>, wenn das Argument im Intervall
     *         liegt
     * @exception IllegalArgumentException wenn das Argument die
     *                falsche Dimensionalitaet hat oder die
     *                Komponenten der Intervallgrenzen sich nicht
     *                vergleichen lassen
     */
    public boolean contains(Interval interval)
    {

        // Dimensionalitaet pruefen:
        if(interval._bound1.dimension() != this._bound1.dimension()) 
         throw new IllegalArgumentException(Argument.WRONG_DIMENSION);
        
        Bound[] x = interval.getBounds();
        Comparable a = null;
        int test;
        
        // linke Intervallgrenze testen:
        for(int i = 0; i < x[0].dimension(); i++)
        {
            // Vergleichbarkeit testen:
            try
            {
                a = (Comparable) x[0].n[i];
                
                // Vergleich durchfuehren:
                test = ((Comparable) _bound1.n[i]).compareTo(a);
                
                if(test > 0) 
                 return false; // Grenze liegt ausserhalb
                
                else if((test == 0) && 
                        (!_bound1.inclusion[i])&& 
                        x[0].inclusion[i]) 
                    
                 return false;  // Grenze
                                // liegt
                                // auf
                                // einer
                                // Exklusivgrenze
                
                test = ((Comparable) _bound2.n[i]).compareTo(a);
                
                if(test < 0) return false; // Grenze liegt ausserhalb
                
                else if((test == 0) && 
                      ((!_bound2.inclusion[i]) || (!x[0].inclusion[i]))) 
                    
                    return false;   // Grenze
                                    // liegt
                                    // auf
                                    // Exklusivgrenze
                                    // o.
                                    // ausserhalb
            } 
            catch(ClassCastException cce)
            { 
                // try
                throw new IllegalArgumentException(UNCOMPARABLE_COMPONENTS);
            } 
        }
        
        
        // rechte Intervallgrenze testen:
        for(int i = 0; i < x[1].dimension(); i++)
        {
            // Vergleichbarkeit testen:
            try
            {
                a = (Comparable) x[1].n[i];
                test = ((Comparable) _bound2.n[i]).compareTo(a);
            } 
            catch(ClassCastException cce)
            { 
                // try
                throw new IllegalArgumentException(UNCOMPARABLE_COMPONENTS);
            } 
            
            // Vergleich durchfuehren:
            if(test < 0) return false; // Grenze liegt ausserhalb
            
            else if((test == 0) && 
                    (!_bound2.inclusion[i]) && x[1].inclusion[i]) 
                
             return false;  // Grenze
                            // liegt
                            // auf
                            // einer
                            // Exklusivgrenze
        } // for
        return true;

    } // contains


    /**
     * Testet, ob das spezifizierte Intervall mit diesem Intervall zu
     * einem einzigen Intervall vereinigt werden kann, d.h. ob sich
     * die beiden Intervalle ueberschneiden oder beruehren.
     * 
     * @param interval das zweite Intervall
     * @return <code>true</code>, wenn sich die beiden Intervalle
     *         vereinigen lassen, sonst <code>false</code>
     */
    public boolean canUniteWith(Interval interval)
    {

        try
        {
            Interval i = union(interval);
            return i != null;
        } 
        catch(IllegalArgumentException iae)
        { 
            return false;
        } 

    } // canUniteWith


    /**
     * Vereinigt dieses Intervall mit dem spezifizierten. Falls die
     * Intervalle eine unterschiedliche Dimensionalitaet besitzen oder
     * ihre Grenzen nicht verglichen werden koennen, wird eine
     * <code>IllegalArgumentException</code> ausgeworfen. Wenn die
     * Intervalle ansonsten nicht vereinbar sind, wird
     * <code>null</code> zurueckgeliefert.
     * 
     * @param interval das zweite Intervall
     * @return ein neu erzeugtes vereinigtes Intervall
     * @exception IllegalArgumentException falls die beiden Intervalle
     *                eine unterschiedliche Dimensionalitaet besitzen
     *                oder nicht vergleichbar sind
     */
    public Interval union(Interval interval)
    {

        if(_bound1.dimension() != interval._bound1.dimension()) 
         throw new IllegalArgumentException(Argument.WRONG_DIMENSION);
        
        if(this.contains(interval)) 
         return (Interval) this.clone();
        
        if(interval.contains(this)) 
         return (Interval) interval.clone();

        int n = _bound1.dimension();
        
        Bound[] bounds = interval.getBounds();
        boolean r = false;
        
        Comparable a, b;
        int test1, test2;
        
        Number[] new_l = new Number[n];
        Number[] new_r = new Number[n];
        
        boolean[] l_inclusive = new boolean[n];
        boolean[] r_inclusive = new boolean[n];
        
        for(int i = 0; i < n; i++)
        {
            // Vergleichbarkeit testen:
            try
            {
                a = (Comparable) bounds[0].n[i];
                b = (Comparable) bounds[1].n[i];
                
                test1 = ((Comparable) _bound1.n[i]).compareTo(a);
                test2 = ((Comparable) _bound2.n[i]).compareTo(b);
                
            } 
            catch(ClassCastException cce)
            { 
                throw new IllegalArgumentException(UNCOMPARABLE_COMPONENTS);
            } 
            
            
            // die n-1 gleichen Grenzkomponenten uebernehmen:
            if((test1 == 0) && (test2 == 0))
            {
                new_l[i] = (Double) _bound1.n[i];
                l_inclusive[i] = _bound1.inclusion[i];
                
                new_r[i] = (Double) _bound2.n[i];
                r_inclusive[i] = _bound2.inclusion[i];
            }
            
            // in einer Dimension gibt es verschiedene
            // Grenzkomponenten:
            
            else
            {
                if(r) return null; // es kann nur eine geben ;-)
                
                r = true; // erste verschiedene Dimension entdeckt
                
                // Suchen der kleinsten Grenzkomponente:
                if(test1 < 0)
                {
                    new_l[i] = (Double) _bound1.n[i];
                    l_inclusive[i] = _bound1.inclusion[i];
                    
                    // _bound2 darf nicht links von interval liegen:
                    int t = ((Comparable) _bound2.n[i]).compareTo(a);
                    
                    if((t < 0) || ((t == 0) && (!_bound2.inclusion[i]) && 
                       (!bounds[0].inclusion[i]))) 
                     return null;
                }
                else if(test1 > 0)
                { 
                    
                    new_l[i] = (Double) bounds[0].n[i];
                    l_inclusive[i] = bounds[0].inclusion[i];
                    
                    // bounds[1] darf nicht links von this liegen:
                    
                    int t = ((Comparable) _bound1.n[i]).compareTo(b);
                    
                    if((t > 0) || ((t == 0) && (!_bound1.inclusion[i]) && 
                      (!bounds[1].inclusion[i]))) 
                     return null;
                    
                    // die linken Grenzen liegen uebereinander:
                }
                else
                {
                    if(_bound1.inclusion[i])
                    {
                        new_l[i] = (Double) _bound1.n[i];
                        l_inclusive[i] = _bound1.inclusion[i];
                    }
                    else
                    { 
                        new_l[i] = (Double) bounds[0].n[i];
                        l_inclusive[i] = bounds[0].inclusion[i];
                    } 
                }
                
                // Suchen der groessten Grenzkomponente:
                if(test2 < 0)
                {
                    new_r[i] = (Double) bounds[1].n[i];
                    r_inclusive[i] = bounds[1].inclusion[i];
                }
                else if(test2 > 0)
                {
                    new_r[i] = (Double) _bound2.n[i];
                    r_inclusive[i] = _bound2.inclusion[i];
                }
                else 
                    
                // die rechten Grenzen liegen uebereinander:
                if(_bound2.inclusion[i])
                {
                    new_r[i] = (Double) _bound2.n[i];
                    r_inclusive[i] = _bound2.inclusion[i];
                }
                else
                { 
                    new_r[i] = (Double) bounds[1].n[i];
                    r_inclusive[i] = bounds[1].inclusion[i];
                } 
            } 
        } // for
        return new Interval(new Bound(new_l, l_inclusive), 
                            new Bound(new_r, r_inclusive));

    } // union


    /**
     * Berechnet das Schnittintervall aus diesem Intervall mit dem
     * spezifizierten Intervall. Falls die Schnittmenge leer ist, wird
     * <code>null</code> zurueckgeliefert. Falls die Intervalle eine
     * unterschiedliche Dimensionalitaet besitzen oder ihre Grenzen
     * nicht verglichen werden koennen, wird eine
     * <code>IllegalArgumentException</code> ausgeworfen.
     * 
     * @param interval das zweite Intervall
     * @return ein neu erzeugtes Schnittintervall
     * @exception IllegalArgumentException falls die beiden Intervalle
     *                eine unterschiedliche Dimensionalitaet besitzen
     *                oder nicht vergleichbar sind
     */
    public Interval intersection(Interval interval)
    {

        if(_bound1.dimension() != interval._bound1.dimension()) 
         throw new IllegalArgumentException(Argument.WRONG_DIMENSION);
        
        int n = _bound1.dimension();
        
        Number[] nb0 = new Number[n];
        Number[] nb1 = new Number[n];
        
        boolean[] bb0 = new boolean[n];
        boolean[] bb1 = new boolean[n];
        
        Comparable a0, a1, b0, b1;
        
        for(int i = 0; i < n; i++)
        {
            try
            {
                a0 = (Comparable) this._bound1.n[i];
                a1 = (Comparable) this._bound2.n[i];
                
                b0 = (Comparable) interval._bound1.n[i];
                b1 = (Comparable) interval._bound2.n[i];
                
            } 
            catch(ClassCastException cce)
            { 
                throw new IllegalArgumentException(UNCOMPARABLE_COMPONENTS);
            } 

            int comp = a0.compareTo(b0);

            // this beginnt links von interval:
            if(comp < 0)
            {
                comp = b0.compareTo(a1);
                
                // interval beginnt innerhalb von this:
                if(comp < 0)
                {
                    nb0[i] = interval._bound1.n[i];
                    bb0[i] = interval._bound1.inclusion[i];
                    
                    comp = b1.compareTo(a1);
                    
                    // interval endet innerhalb von this:
                    if(comp < 0)
                    {
                        nb1[i] = interval._bound2.n[i];
                        bb1[i] = interval._bound2.inclusion[i];
                        // interval endet rechts von this:
                    }
                    else if(comp > 0)
                    { 
                        nb1[i] = _bound2.n[i];
                        bb1[i] = _bound2.inclusion[i];
                        // interval und this enden auf der gleichen
                        // Grenze:
                    }
                    else
                    { 
                        nb1[i] = _bound2.n[i];
                        bb1[i] = _bound2.inclusion[i]
                                && interval._bound2.inclusion[i];
                    } 
                    // interval liegt rechts von this -> keine
                    // Schnittmenge:
                    
                }
                else if(comp > 0) 
                 return null;
                
                // interval beginnt mit dem Ende von this:
                
                else
                
                // keine Schnittmenge:
                if((!_bound2.inclusion[i]) || 
                   (!interval._bound1.inclusion[i])) 
                return null;
                
                else
                
                {
                    nb0[i] = _bound2.n[i];
                    bb0[i] = true;
                    nb1[i] = _bound2.n[i];
                    bb1[i] = true;
                } 
            }
            else 

            // interval beginnt links von this:
            if(comp > 0)
            {
                comp = a0.compareTo(b1);
                
                // this beginnt innerhalb von interval:
                if(comp < 0)
                {
                    nb0[i] = _bound1.n[i];
                    bb0[i] = _bound1.inclusion[i];
                    
                    comp = a1.compareTo(b1);
                    
                    // this endet innerhalb von interval:
                    if(comp < 0)
                    {
                        nb1[i] = _bound2.n[i];
                        bb1[i] = _bound2.inclusion[i];
                        // this endet rechts von interval:
                    }
                    else if(comp > 0)
                    { 
                        nb1[i] = interval._bound2.n[i];
                        bb1[i] = interval._bound2.inclusion[i];
                        // this und interval enden auf der gleichen
                        // Grenze:
                    }
                    else
                    {
                        nb1[i] = _bound2.n[i];
                        bb1[i] = _bound2.inclusion[i]
                                && interval._bound2.inclusion[i];
                    } 
                    // this liegt rechts von interval -> keine
                    // Schnittmenge:
                }
                else if(comp > 0) 
                 return null;
                
                // this beginnt mit dem Ende von interval:
                
                else
                
                // keine Schnittmenge:
                if((!interval._bound2.inclusion[i]) || 
                   (!_bound1.inclusion[i])) 
                 return null;
                
                else
                {
                    nb0[i] = interval._bound2.n[i];
                    bb0[i] = true;
                    
                    nb1[i] = interval._bound2.n[i];
                    bb1[i] = true;
                } 
            } 

            // die Intervalle beginnen mit der gleichen Grenze:
            else
            {
                nb0[i] = _bound1.n[i];
                bb0[i] = _bound1.inclusion[i] && interval._bound1.inclusion[i];
                
                comp = a1.compareTo(b1);
                
                // this endet vor interval:
                if(comp < 0)
                {
                    nb1[i] = _bound2.n[i];
                    bb1[i] = _bound2.inclusion[i];
                    // interval endet vor this:
                }
                else if(comp > 0)
                { 
                    nb1[i] = interval._bound2.n[i];
                    bb1[i] = interval._bound2.inclusion[i];
                    // die Intervalle enden mit der gleichen Grenze:
                }
                else
                { 
                    nb1[i] = _bound2.n[i];
                    bb1[i] = _bound2.inclusion[i]
                            && interval._bound2.inclusion[i];
                } 
            } 
        } // for

        return new Interval(new Bound(nb0, bb0), new Bound(nb1, bb1));

    } // intersection


    /**
     * Liefert die Dimensionalitaet dieses Intervalls.
     * 
     * @return die Zahl der Dimensionen dieses Intervalls
     */
    public int dimension()
    {
        return _bound1.dimension();
    } // dimension


    /**
     * Liefert die Intervallgrenzen.
     * 
     * @return die Grenzen des Intervalls
     */
    public Bound[] getBounds()
    {
        Bound[] bounds = new Bound[2];
        
        bounds[0] = _bound1;
        bounds[1] = _bound2;
        return bounds;

    } // getBounds


    /**
     * Prueft das spezifizierte Array, dessen Elemente vom Typ
     * {@link Argument Argument} oder {@link Function Function},
     * umschlossen von zwei Elementen des Typs {@link Bound Bound}
     * sein muessen, und entfernt daraus die Elemente, die nicht in
     * diesem Array enthalten sind. Der Sinn dieser Methode ist es,
     * das Rueckgabe-Array der
     * {@link Function#intersection(Function) intersection(...)}-Methode
     * von Funktionen zu "filtern", also diejenigen Schnittobjekte zu
     * entfernen, die nicht im Intervall vorkommen.<br>
     * Falls der Aufbau des Arrays nicht dem einer gueltigen
     * Schnittmenge entspricht, wird eine
     * <code>IllegalArgumentException</code> ausgeworfen.
     * 
     * @param array das zu filternde Array
     * @exception IllegalArgumentException falls das Array nicht wie
     *                eine gueltige Schnittmenge aufgebaut ist
     */
    public void filter(DynamicArray array)
    {

        try
        {
            int i = array.length() - 1;
            
            Object o;
            Argument a;
            Bound b0, b1;
            Function f;
            
            while(i >= 0)
            {
                o = array.get(i);
                
                if(o instanceof Bound)
                {
                    b1 = (Bound) o;
                    f = (Function) array.get(i - 1);
                    b0 = (Bound) array.get(i - 2);
                    
                    if((!contains(b1)) || (!contains(b0)))
                    {
                        Interval test = intersection(new Interval(b0, b1));
                        
                        if(test == null)
                        {
                            array.remove(i--);
                            array.remove(i--);
                            array.remove(i--);
                        }
                        else
                        { 
                            Bound[] nb = test.getBounds();
                            array.set(i, nb[1]);
                            
                            i -= 2;
                            array.set(i--, nb[0]);
                        } 
                    }
                    
                    else 
                    i -= 3;
                }
                else
                { 
                    a = (Argument) o;
                    
                    if(!contains(a)) array.remove(i);
                    i--;
                } 
            } // while
        } 
        catch(Exception e)
        { 
            throw new IllegalArgumentException(INVALID_INTERSECTION);
        } 

    } // filter


    /**
     * Liefert eine textuelle Repraesentation.
     *
     * @return dieses Intervall als String
     */
    public String toString()
    {

        String s = "";
        
        for(int i = 0; i < _bound1.inclusion.length; i++)
         if(_bound1.inclusion[i]) 
           s += "[";
         else 
           s += "]";
        
        if(_bound1.n.length > 1) 
         s += "(";
        
        for(int i = 0; i < _bound1.n.length; i++)
        {
            s += _bound1.n[i];
            
            if(i < _bound1.n.length - 1) 
             s += ", ";
        } // for
        
        if(_bound2.n.length > 1) 
         s += ")";
        
        s += "; ";
        
        if(_bound2.n.length > 1) 
         s += "(";
        
        for(int i = 0; i < _bound2.n.length; i++)
        {
            s += _bound2.n[i];
            
            if(i < _bound2.n.length - 1) 
             s += ", ";
            
        } // for
        
        if(_bound2.n.length > 1) 
         s += ")";
        
        for(int i = 0; i < _bound2.inclusion.length; i++)
         if(_bound2.inclusion[i]) 
          s += "]";
         else 
          s += "[";
        
        return s;

    } // toString

} // Interval
