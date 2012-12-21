
package anja.analysis;


import java.util.Enumeration;

import anja.util.DynamicArray;
import anja.util.ListItem;
import anja.util.SimpleList;
import anja.util.StdListItem;

/**
 * Konturfunktion fuer reelle Funktionen. Beim Bilden der Kontur von
 * <i>n</i> Funktionen wird durch <i>Divide&Conquer</i> eine
 * Laufzeit von <i>O</i>(<i>n</i> log <i>n</i>) erreicht.
 * 
 * @version 0.8 27.04.2004
 * @author Sascha Ternes
 */

public final class RealEnvelope extends RealFunction
{

    // *************************************************************************
    // Public constants
    // *************************************************************************

    /**
     * Konstante fuer eine untere Kontur
     */
    public static final int  LOWER_ENVELOPE = 0;

    /**
     * Konstante fuer eine obere Kontur
     */
    public static final int  UPPER_ENVELOPE = 1;

    // *************************************************************************
    // Private constants
    // *************************************************************************

    // Konstanten aus RealFunction:
    private static final int _RM            = RealFunction.RIGHT_MASK;
    private static final int _LU            = RealFunction.LEFT_UNKNOWN;
    private static final int _RU            = RealFunction.RIGHT_UNKNOWN;
    private static final int _LA            = RealFunction.LEFT_ABOVE;
    private static final int _RA            = RealFunction.RIGHT_ABOVE;
    private static final int _LB            = RealFunction.LEFT_BELOW;
    private static final int _RB            = RealFunction.RIGHT_BELOW;
    private static final int _LE            = RealFunction.LEFT_EQUAL;
    private static final int _RE            = RealFunction.RIGHT_EQUAL;

    // *************************************************************************
    // Protected variables
    // *************************************************************************

    /**
     * die Art der Kontur ({@link #LOWER_ENVELOPE LOWER_ENVELOPE})
     * oder {@link #UPPER_ENVELOPE UPPER_ENVELOPE})
     */
    protected int            _type;

    /**
     * geordnete Liste der beteiligten Funktionen mit ihren
     * Intervallen
     */
    protected SimpleList     _functions;

    // *************************************************************************
    // Inner classes
    // *************************************************************************

    /*
     * Zusammenfassung von Konturelementen und deren Komponenten fuer
     * die _getNextItem-Methode.
     */
    private class EnvItem
    {

        protected ListItem     item;
        protected Interval     i;
        protected Bound[]      b;
        protected RealFunction f;
    } // EnvItem

    // *************************************************************************
    // Constructors
    // *************************************************************************

    /**
     * Erzeugt eine leere untere Kontur.
     */
    public RealEnvelope()
    {

        this(LOWER_ENVELOPE);

    } // RealEnvelope

    /**
     * Erzeugt eine leere Kontur. Fuer den Parameter <code>type</code>
     * stehen die Konstanten {@link #LOWER_ENVELOPE LOWER_ENVELOPE}}
     * und {@link #UPPER_ENVELOPE UPPER_ENVELOPE}} zur Verfuegung, um
     * eine untere oder obere Kontur zu erzeugen.
     * 
     * @param type die Art der Kontur
     */
    public RealEnvelope(int type)
    {

        if(type == UPPER_ENVELOPE) _type = UPPER_ENVELOPE;
        else _type = LOWER_ENVELOPE;
        _functions = new SimpleList();

    } // RealEnvelope

    /**
     * Erzeugt eine Kontur des spezifizierten Typs fuer die angegebene
     * Funktion.
     * 
     * @param type die Art der Kontur ({@link #LOWER_ENVELOPE LOWER_ENVELOPE}
     *            oder {@link #UPPER_ENVELOPE UPPER_ENVELOPE})
     * @param function die erste Funktion der Kontur
     */
    public RealEnvelope(int type, RealFunction function)
    {

        this(type);
        add(function);

    } // RealEnvelope

    /**
     * Erzeugt eine Kontur des spezifizierten Typs fuer die beiden
     * angegebenen Funktionen.
     * 
     * @param type die Art der Kontur ({@link #LOWER_ENVELOPE LOWER_ENVELOPE}
     *            oder {@link #UPPER_ENVELOPE UPPER_ENVELOPE})
     * @param function1 die erste Funktion der Kontur
     * @param function2 die zweite Funktion der Kontur
     */
    public RealEnvelope(int type, RealFunction function1, RealFunction function2)
    {

        this(type);
        add(function1);
        add(function2);

    } // RealEnvelope

    /**
     * Erzeugt eine Kontur des spezifizierten Typs fuer die
     * uebergebenen Funktionen.
     * 
     * @param type die Art der Kontur ({@link #LOWER_ENVELOPE LOWER_ENVELOPE}
     *            oder {@link #UPPER_ENVELOPE UPPER_ENVELOPE})
     * @param functions die Funktionen, deren Kontur berechnet werden
     *            soll
     */
    public RealEnvelope(int type, RealFunction[] functions)
    {

        this(type);
        add(functions, 0, functions.length - 1);

    } // RealEnvelope

    /**
     * Erzeugt eine Kontur des spezifizierten Typs fuer die
     * uebergebenen Funktionen. Der Bereich im Array der Funktionen
     * wird durch die spezifizierten Indizes ausgewaehlt.
     * 
     * @param type die Art der Kontur ({@link #LOWER_ENVELOPE LOWER_ENVELOPE}
     *            oder {@link #UPPER_ENVELOPE UPPER_ENVELOPE})
     * @param functions die Funktionen, deren Kontur berechnet werden
     *            soll
     * @param first der Anfangsindex im Funktionsarray
     * @param last der Endindex im Funktionsarray
     */
    public RealEnvelope(int type, RealFunction[] functions, int first, int last)
    {

        this(type);
        add(functions, first, last);

    } // RealEnvelope

    // *************************************************************************
    // Public methods
    // *************************************************************************

    /*
     * [javadoc-Beschreibung wird aus Function kopiert]
     */
    public Object clone()
    {

        RealEnvelope env = new RealEnvelope(this._type);
        env._functions = (SimpleList) this._functions.clone();
        return env;

    } // clone

    /*
     * [javadoc-Beschreibung wird aus Function kopiert]
     */
    public boolean equals(Object obj)
    {

        // nur andere reelle Konturfunktionen koennen getestet werden:
        RealEnvelope g = null;
        try
        {
            g = (RealEnvelope) obj;
        } catch(ClassCastException cce)
        { // try
            return false;
        } // catch

        // nur Konturen gleichen Umfangs koennen gleich sein:
        if(g._functions.length() != this._functions.length()) return false;

        // einzelne Funktionen vergleichen:
        ListItem item1 = this._functions.first();
        ListItem item2 = g._functions.first();
        while(item1 != null)
        {
            if((!item1.key().equals(item2.key()))
                    || (!item1.value().equals(item2.value()))) return false;
            item1 = this._functions.next(item1);
            item2 = g._functions.next(item2);
        } // while

        return true;

    } // equals

    /**
     * Liefert den Funktionswert der Kontur fuer das spezifizierte
     * <i>x</i>.
     * 
     * @param x das Argument fuer diese Konturfunktion
     * @return den Funktionswert oder <code>Double.NaN</code>, wenn
     *         die Kontur leer ist
     */
    public double f(double x)
    {

        RealFunction function = getFunctionFor(x);
        if(function == null) return Double.NaN;
        return function.f(x);

    } // f

    /**
     * Liefert den Grenzwert dieser Kontur fuer <i>x</i> gegen die
     * spezifizierte Konstante <i>c</i>. Sinnvolle Argumente sind
     * dabei lediglich <code>NEGATIVE_INFINITY</code> und
     * <code>POSITIVE_INFINITY</code>, der Rueckgabewert ist dann
     * ebenfalls eine dieser Konstanten. Bei allen anderen Argumenten
     * ist das Resultat das gleiche wie bei einem Aufruf der Methode
     * {@link #f(double)}. Falls die Kontur leer ist, wird
     * <code>Double.NaN</code> zurueckgeliefert.
     * 
     * @param c der Annaeherungsparameter fuer <i>x</i>, fuer den der
     *            Grenzwert gesucht ist
     * @return den Grenzwert dieser Kontur fuer <i>x</i> gegen <i>c</i>
     *         oder <code>Double.NaN</code>, wenn diese Kontur leer
     *         ist
     */
    public double limit(double c)
    {

        RealFunction function = getFunctionFor(c);
        if(function == null) return Double.NaN;
        return function.limit(c);

    } // limit

    /**
     * Liefert die Schnittmenge dieser Kontur mit der spezifizierten
     * Funktion, die eine reelle Funktion oder Kontur sein muss (sonst
     * wird eine <code>IllegalArgumentException</code> ausgeworfen).
     * 
     * @param g die Schnittfunktion
     * @return die Schnittmenge (Konzept siehe
     *         {@link Function Function})
     * @exception IllegalArgumentException falls die spezifizierte
     *                Funktion weder eine reelle Funktion noch eine
     *                Kontur ist
     */
    public DynamicArray intersection(Function g)
    {

        RealFunction function = null;
        RealEnvelope env = null;
        // Test auf reelle Kontur:
        try
        {
            env = (RealEnvelope) g;
            // Test auf Identitaet:
            if(env.isCongruent(this)) return identityIntersection(this);
        } catch(ClassCastException cce)
        { // try
            // Test auf reelle Funktion:
            try
            {
                function = (RealFunction) g;
            } catch(ClassCastException cce2)
            { // try
                throw new IllegalArgumentException(Function.ILLEGAL_ARGUMENT);
            } // catch
        } // catch

        DynamicArray a = new DynamicArray();
        if(_functions.empty()) return a;

        // Schnitt mit einer Funktion:
        if(function != null)
        {
            // Funktionen der Kontur einzeln mit der spezifizierten
            // schneiden:
            ListItem item = _functions.first();
            Interval i;
            RealFunction f;
            DynamicArray b;
            while(item != null)
            {
                // echte Schnittmenge des aktuellen Abschnitts
                // ermitteln:
                i = (Interval) item.key();
                f = (RealFunction) item.value();
                b = f.intersection(function);
                i.filter(b);
                // es gibt mindestens einen Schnittpunkt im aktuellen
                // Abschnitt:
                if(!b.isEmpty())
                {
                    int k = 0;
                    // bei gleichen Werten doppelte Schnittpunkte
                    // vermeiden:
                    if(!a.isEmpty())
                    {
                        Argument arg = (Argument) a.last();
                        Argument brg = (Argument) b.first();
                        // Vorgaenger ist a oder [a:
                        if(
                        // nur bei gleichen Werten:
                        ((arg.equals(b.first())))
                                && (((!(arg instanceof Bound)) || ((arg instanceof Bound) && ((Bound) arg).inclusion[0])))) if(brg instanceof Bound)
                        // Nachfolger ist [b -> ersetzen:
                        if(((Bound) brg).inclusion[0])
                        {
                            a.add(new Bound(brg.n[0], Bound.EXCLUSIVE));
                            k++;
                        } // if
                        /* Nachfolger ist ]b -> ok */
                        // Nachfolger ist Argument -> kein add:
                        else k++;
                        /* Vorgaenger ist ]a -> ok */
                    } // if
                    // die Schnittobjekte der Gesamt-Schnittmenge
                    // hinzufuegen:
                    for(; k < b.length(); k++)
                        a.add(b.get(k));
                } // if
                item = _functions.next(item);
            } // while

            // Schnitt mit einer Kontur:
        }
        else
        { // if
            if(env.isEmpty()) return a;
            EnvItem e1 = new EnvItem();
            EnvItem e2 = new EnvItem();
            e1.item = this._functions.first();
            e2.item = env._functions.first();
            e1.i = (Interval) e1.item.key();
            e2.i = (Interval) e2.item.key();
            e1.b = e1.i.getBounds();
            e2.b = e2.i.getBounds();
            e1.f = (RealFunction) e1.item.value();
            e2.f = (RealFunction) e2.item.value();
            Interval test;
            int comp;
            while((e1.item != null) && (e2.item != null))
            {
                test = e1.i.intersection(e2.i);
                // es gibt eine gemeinsame Schnittmenge:
                if(test != null)
                {
                    // erzeuge gefilterte Schnittobjektmenge:
                    DynamicArray b = (e1.f.intersection(e2.f));
                    test.filter(b);
                    for(int i = 0; i < b.length(); i++)
                        a.add(b.get(i));
                } // if
                // die naechsten Intervalle bestimmen:
                comp = ((Double) e1.b[1].n[0]).compareTo((Double) e2.b[1].n[0]);
                // Intervall 1 liegt links von Intervall 2:
                if(comp < 0) _getNextItem(this._functions, e1);
                // Intervall 1 liegt rechts von Intervall 2:
                else if(comp > 0) _getNextItem(env._functions, e2);
                // beide Intervalle enden aufeinander und haben
                // gleiche Grenze:
                else if(e1.b[1].inclusion[0] == e2.b[1].inclusion[0])
                { // if
                    _getNextItem(this._functions, e1);
                    _getNextItem(env._functions, e2);
                    // enden aufeinander, Intervall 1 ist inklusiv:
                }
                else if(e1.b[1].inclusion[0]) // if
                _getNextItem(env._functions, e2);
                // enden aufeinander, Intervall 2 ist inklusiv:
                else _getNextItem(this._functions, e1);
            } // while
        } // else

        return a;

    } // intersection

    /*
     * [javadoc-Beschreibung wird aus RealFunction kopiert]
     */
    public DynamicArray getDiscontinuities()
    {

        DynamicArray dis = new DynamicArray();
        if(_functions.length() <= 1) return dis; // ggf. abbrechen
        ListItem item = _functions.first();
        ListItem next;
        Bound[] a, b;
        double da, db;
        RealFunction f, g;
        while(true)
        {
            next = _functions.next(item);
            if(next == null) break;
            a = ((Interval) item.key()).getBounds();
            da = ((Double) a[1].n[0]).doubleValue();
            b = ((Interval) next.key()).getBounds();
            db = ((Double) b[0].n[0]).doubleValue();
            if(da == db)
            {
                f = (RealFunction) item.value();
                g = (RealFunction) next.value();
                // Unstetigkeit:
                if(f.f(da) != g.f(db)) dis
                        .add(new Bound(da, a[1].inclusion[0]));
            } // if
            item = next;
        } // while
        return dis;

    } // getDiscontinuities

    /*
     * [javadoc-Beschreibung wird aus RealFunction kopiert]
     */
    public RealFunction derivative()
    {

        RealEnvelope env = new RealEnvelope(_type);
        ListItem item = _functions.first();
        while(item != null)
        {
            RealFunction function = ((RealFunction) item.value()).derivative();
            ListItem it = new StdListItem(item.key(), function);
            env._functions.add(it);
            item = _functions.next(item);
        } // while
        return env;

    } // derivative

    /*
     * [javadoc-Beschreibung wird aus RealFunction kopiert]
     */
    public Domain domain()
    {

        RealDomain dom = new RealDomain(0);
        ListItem item = _functions.first();
        while(item != null)
        {
            dom.add((Interval) item.key());
            item = _functions.next(item);
        } // while
        return dom;

    } // domain

    /**
     * Liefert die Art dieser Kontur.
     * 
     * @return die Art der Kontur als eine der Konstanten
     *         {@link #LOWER_ENVELOPE LOWER_ENVELOPE} oder
     *         {@link #UPPER_ENVELOPE UPPER_ENVELOPE}
     */
    public int getType()
    {

        return _type;

    } // getType

    /**
     * Liefert zurueck, ob die Kontur leer ist.
     * 
     * @return <code>true</code>, wenn der Kontur bisher keine
     *         Funktionen hinzugefuegt wurden, sonst
     *         <code>false</code>
     */
    public boolean isEmpty()
    {

        return _functions.empty();

    } // isEmpty

    /**
     * Fuegt die spezifizierte Funktion der Kontur hinzu; die Kontur
     * wird neu berechnet.
     * 
     * @param function die hinzuzufuegende Funktion
     */
    public void add(RealFunction function)
    {

        Interval[] intervals = ((RealDomain) function.domain()).getIntervals();
        // in eine leere Liste einfach die Funktion einfuegen:
        if(_functions.empty())
        {
            for(int i = 0; i < intervals.length; i++)
                _functions.add(new StdListItem(intervals[i], function));
            // System.out.println( "---Envelope: " + _functions );
            return;
        } // if
        // sonst eine neue Kontur aus der Funktion zur Kontur
        // hinzufuegen:
        this.add(new RealEnvelope(this._type, function));

    } // add

    /**
     * Fuegt die spezifizierten Funktionen der Kontur hinzu; die
     * Kontur wird neu berechnet.
     * 
     * @param functions die hinzuzufuegenden Funktionen
     */
    public void add(RealFunction[] functions)
    {

        add(functions, 0, functions.length - 1);

    } // add

    /**
     * Fuegt die spezifizierten Funktionen der Kontur hinzu; die
     * Kontur wird neu berechnet.
     * 
     * @param functions die hinzuzufuegenden Funktionen
     */
    public void add(RealFunction[] functions, int first, int last)
    {

        // Anzahl der Funktionen feststellen:
        int n = last - first + 1;

        // eine einzelne Funktion direkt hinzufuegen:
        if(n == 1)
        {
            add(functions[first]);
            return;
        } // if

        // mehrere Funktionen aufteilen und untereinander die Kontur
        // bilden:
        this.add(functions, first, first + n / 2 - 1);
        RealEnvelope e = new RealEnvelope(_type, functions, first + n / 2, last);
        // Konturen verschmelzen:
        this.add(e);

    } // add

    /**
     * Vereint diese Kontur (genannt <i>this</i>) mit der
     * spezifizierten Kontur (genannt <i>k</i>). Die resultierende
     * Kontur wird in <i>this</i> gespeichert und ist von der
     * gleichen Art wie <i>this</i>.
     * 
     * @param envelope die hinzuzufuegende Kontur
     */
    public void add(RealEnvelope envelope)
    {

        // bei leerer Kontur abbrechen:
        if((envelope == null) || envelope.isEmpty()) return;

        // wenn diese Kontur leer ist, Kontur uebernehmen:
        if(this.isEmpty())
        {
            _type = envelope._type;
            _functions.addAll(envelope._functions);
            return;
        } // if

        // System.out.println( "---Merging:" );
        // System.out.println( this._functions );
        // System.out.println( envelope._functions );
        // sonst die beiden Konturen zusammenfassen:
        SimpleList result = new SimpleList();
        SimpleList env = envelope._functions;
        EnvItem e1 = new EnvItem();
        EnvItem e2 = new EnvItem();
        _prepareNextItem(e1, _functions.first());
        _prepareNextItem(e2, env.first());
        EnvItem r = new EnvItem();
        Interval test;
        Bound[] t;
        int comp;
        boolean f1_is_env;
        while((e1.item != null) && (e2.item != null))
        {
            // System.out.println( "-- " + e1.i + ":" + e1.f + " <-> "
            // + e2.i + ":" + e2.f );
            // Test auf Schnittintervall:
            test = e1.i.intersection(e2.i);
            // System.out.println( "Common interval: " + test );

            // kein gemeinsames Intervall:
            if(test == null)
            {
                comp = _compareExt(e1.b[1], e2.b[1]);
                // Intervall 1 liegt links -> einfuegen und naechstes
                // Intervall:
                if(comp < 0)
                {
                    // System.out.println( "I1 inserted." );
                    result.add(e1.item);
                    _getNextItem(_functions, e1);
                    // Intervall 2 liegt links -> einfuegen und
                    // naechstes Intervall:
                }
                else
                { // if
                // System.out.println( "I2 inserted." );
                    result.add(e2.item);
                    _getNextItem(env, e2);
                } // else

                // die Intervalle beruehren sich:
            }
            else if(test.isPoint())
            {
                // Intervall 2 ist nur ein Punkt -> ignorieren, I1
                // einfuegen:
                if(e2.i.isPoint())
                {
                    result.add(e1.item);
                    if(_compare(e1.b[1], e2.b[0]) == 0) _getNextItem(
                            _functions, e1); // auch naechstes
                                                // Intervall 1
                    _getNextItem(env, e2); // naechstes Intervall 2
                    // Intervall 1 ist nur ein Punkt -> ignorieren, I2
                    // einfuegen:
                }
                else if(e1.i.isPoint())
                { // if
                    result.add(e2.item);
                    if(_compare(e2.b[1], e1.b[0]) == 0) _getNextItem(env, e2); // auch
                                                                                // naechstes
                                                                                // Intervall
                                                                                // 2
                    _getNextItem(_functions, e1); // naechstes
                                                    // Intervall 1
                    // Intervall 2 folgt Intervall 1 -> Intervall 1
                    // offen einfuegen:
                }
                else if(_compare(e1.b[1], e2.b[0]) == 0)
                { // if
                    if(e1.b[1].inclusion[0])
                    {
                        e1.b[1] = new Bound(e1.b[1].n[0], Bound.EXCLUSIVE);
                        e1.i = new Interval(e1.b[0], e1.b[1]);
                        e1.item.setKey(e1.i);
                    } // if
                    result.add(e1.item);
                    _getNextItem(_functions, e1); // naechstes
                                                    // Intervall 1
                    // Intervall 1 folgt Intervall 2 -> Intervall 2
                    // offen einfuegen:
                }
                else
                { // if
                    if(e2.b[1].inclusion[0])
                    {
                        e2.b[1] = new Bound(e2.b[1].n[0], Bound.EXCLUSIVE);
                        e2.i = new Interval(e2.b[0], e2.b[1]);
                        e2.item.setKey(e2.i);
                    } // if
                    result.add(e2.item);
                    _getNextItem(env, e2);
                } // else

                // die beiden Intervalle ueberdecken sich:
            }
            else
            { // if
            // System.out.println( "(step0)" );
                t = test.getBounds(); // Schnittintervall-Grenzen
                // System.out.println( "(step1)" );
                // Schnitt der beiden Funktionsstuecke im gem.
                // Intervall berechnen:
                // System.out.println( e1.f + " X " + e2.f );
                DynamicArray s = e1.f.intersection(e2.f);
                // System.out.println( "(step2)" );
                test.filter(s);
                // System.out.println( "S = " + s );
                // Orientierung der beiden Funktionsstuecke ermitteln:
                int[] ori = e1.f.getOrientation(e2.f, test, s);
                f1_is_env = false; // e2.f ist initial die Kontur
                // Position der Intervallanfaenge untereinander
                // ermitteln:
                comp = _compareExt(e1.b[0], e2.b[0]);

                // Intervall 1 beginnt links von Intervall 2 oder
                // aufeinander </>:
                if((comp < 0)
                        || ((comp == 0) && (((_type == LOWER_ENVELOPE) && (ori[0] == (_LU + _RB))) || ((_type == UPPER_ENVELOPE) && (ori[0] == (_LU + _RA))))))
                {
                    // System.out.println( "Starting with I1..." );
                    f1_is_env = true; // Funktion 1 ist die Kontur
                    _copyItem(e1, r);
                }
                else // if
                // Intervall 2 beginnt links von Intervall 1 oder
                // aufeinander </>:
                if((comp > 0)
                        || ((comp == 0) && (((_type == LOWER_ENVELOPE) && (ori[0] == (_LU + _RA))) || ((_type == UPPER_ENVELOPE) && (ori[0] == (_LU + _RB))))))
                {
                    // System.out.println( "Starting with I2..." );
                    f1_is_env = false; // Funktion 2 ist die Kontur
                    _copyItem(e2, r);
                } // if

                // das andere Intervall wird spaeter dominant:
                if((comp != 0)
                        && (((_type == LOWER_ENVELOPE) && ((f1_is_env && (ori[0] == (_LU + _RA))) || ((!f1_is_env) && (ori[0] == (_LU + _RB))))) || ((_type == UPPER_ENVELOPE) && ((f1_is_env && (ori[0] == (_LU + _RB))) || ((!f1_is_env) && (ori[0] == (_LU + _RA)))))))
                {
                    // Intervall 1 wird durch Intervall 2 abgeloest:
                    if(f1_is_env)
                    {
                        // System.out.println( "at test followed by
                        // I2..." );
                        f1_is_env = false;
                        // Konturintervall verkuerzen:
                        r.b[1] = new Bound(t[0].n[0], !t[0].inclusion[0]);
                        r.i = new Interval(r.b[0], r.b[1]);
                        r.item.setKey(r.i);
                        result.add(r.item);
                        // Funktion 2 als Kontur hinzufuegen:
                        _copyItem(e2, r);
                        // Intervall 2 wird durch Intervall 1
                        // abgeloest:
                    }
                    else
                    { // if
                    // System.out.println( "at test followed by I1..."
                    // );
                        f1_is_env = true;
                        // Konturintervall verkuerzen:
                        r.b[1] = new Bound(t[0].n[0], !t[0].inclusion[0]);
                        r.i = new Interval(r.b[0], r.b[1]);
                        r.item.setKey(r.i);
                        result.add(r.item);
                        // Funktion 1 als Kontur hinzufuegen:
                        _copyItem(e1, r);
                    } // else
                } // if

                // ohne Schnitt kann die Kontur trivial fortgesetzt
                // werden:
                if(s.isEmpty())
                {
                    comp = _compareExt(e1.b[1], e2.b[1]);
                    // Funktion 1 wird durch Funktion 2 abgeloest:
                    if(f1_is_env && (comp < 0))
                    {
                        // System.out.println( "after test followed by
                        // I2..." );
                        // Konturintervall verkuerzen:
                        r.b[1] = new Bound(t[1].n[0], t[1].inclusion[0]);
                        r.i = new Interval(r.b[0], r.b[1]);
                        r.item.setKey(r.i);
                        result.add(r.item);
                        // Funktion 2 als Kontur vorbereiten:
                        e2.b[0] = new Bound(t[1].n[0], !t[1].inclusion[0]);
                        e2.i = new Interval(e2.b[0], e2.b[1]);
                        e2.item.setKey(e2.i);
                        _getNextItem(_functions, e1);
                        // Funktion 2 wird durch Funktion 1 abgeloest:
                    }
                    else if((!f1_is_env) && (comp > 0))
                    { // if
                    // System.out.println( "after test followed by
                    // I1..." );
                        // Konturintervall verkuerzen:
                        r.b[1] = new Bound(t[1].n[0], t[1].inclusion[0]);
                        r.i = new Interval(r.b[0], r.b[1]);
                        r.item.setKey(r.i);
                        result.add(r.item);
                        // Funktion 1 als Kontur vorbereiten:
                        e1.b[0] = new Bound(t[1].n[0], !t[1].inclusion[0]);
                        e1.i = new Interval(e1.b[0], e1.b[1]);
                        e1.item.setKey(e1.i);
                        _getNextItem(env, e2);
                        // Funktion 1 bleibt erhalten:
                    }
                    else if(f1_is_env)
                    { // if
                        e1.b = r.b;
                        e1.i = r.i;
                        e1.item.setKey(e1.i);
                        _getNextItem(env, e2);
                        // Funktion 2 bleibt erhalten:
                    }
                    else
                    { // if
                        e2.b = r.b;
                        e2.i = r.i;
                        e2.item.setKey(e2.i);
                        _getNextItem(_functions, e1);
                    } // else

                    // sonst die Schnitte beruecksichtigen:
                }
                else
                { // if
                // System.out.println( "using intersection:" );

                    // Sonderfall "erster Schnitt auf Intervallgrenze"
                    // vorbereiten:
                    if((comp == 0) && (ori[0] == (_LU + _RU)))
                    {
                        if(_type == LOWER_ENVELOPE)
                        {
                            if(ori[1] == (_LU + _RA))
                            {
                                f1_is_env = false;
                                _copyItem(e2, r);
                            }
                            else
                            { // if
                                f1_is_env = true;
                                _copyItem(e1, r);
                            } // else
                        }
                        else
                        { // if
                            if(ori[1] == (_LU + _RB))
                            {
                                f1_is_env = false;
                                _copyItem(e2, r);
                            }
                            else
                            { // if
                                f1_is_env = true;
                                _copyItem(e1, r);
                            } // else
                        } // else
                    } // if

                    // die Schnitte einzeln durchgehen:
                    Object o;
                    Argument x;
                    for(int i = 0; i < s.length(); i++)
                    {
                        o = s.get(i);
                        // Schnittfunktionen ignorieren:
                        if(o instanceof Function) continue;
                        // am Schnittpunkt pruefen, ob die
                        // Konturfunktion wechselt:
                        if(((_type == LOWER_ENVELOPE) && ((f1_is_env && ((ori[i + 1] & _RM) == _RA)) || ((!f1_is_env) && ((ori[i + 1] & _RM) == _RB))))
                                || ((_type == UPPER_ENVELOPE) && ((f1_is_env && ((ori[i + 1] & _RM) == _RB)) || ((!f1_is_env) && ((ori[i + 1] & _RM) == _RA)))))
                        {
                            x = (Argument) o;
                            // altes Intervall verkuerzen:
                            r.b[1] = new Bound(x.n[0], Bound.EXCLUSIVE);
                            r.i = new Interval(r.b[0], r.b[1]);
                            r.item.setKey(r.i);
                            // letztes Segment hinzufuegen:
                            result.add(r.item);
                            // bis zum Schnittpunkt ist Funktion 1 die
                            // Kontur:
                            if(f1_is_env)
                            {
                                f1_is_env = false; // am Schnitt wird
                                                    // Funktion 2
                                                    // Kontur
                                _copyItem(e2, r); // naechstes
                                                    // Intervall mit
                                                    // F2
                                // System.out.println( "at " + x + ":
                                // followed by I2..." );
                                // bis zum Schnittpunkt ist Funktion 2
                                // die Kontur:
                            }
                            else
                            { // if
                                f1_is_env = true; // am Schnitt wird
                                                    // Funktion1
                                                    // Kontur
                                _copyItem(e1, r); // naechstes
                                                    // Intervall mit
                                                    // F1
                                // System.out.println( "at " + x + ":
                                // followed by I1..." );
                            } // else
                            // naechstes Segment vorbereiten, noch
                            // nicht hinzufuegen:
                            r.b[0] = new Bound(x.n[0], Bound.INCLUSIVE);
                            r.i = new Interval(r.b[0], r.b[1]);
                            r.item.setKey(r.i);
                        } // if
                    } // for

                    // ueberstehenden Rest verarbeiten:
                    comp = _compareExt(e1.b[1], e2.b[1]);
                    // lediglich die naechsten Intervalle ermitteln:
                    if(comp == 0)
                    {
                        // System.out.println( "Both intervalls end at
                        // the same point." );
                        result.add(r.item); // letztes Segment
                                            // hinzufuegen
                        _getNextItem(_functions, e1);
                        _getNextItem(env, e2);
                    }
                    else if(f1_is_env)
                    { // if
                        // Kontur 2 verkuerzen fuer naechsten
                        // Durchlauf:
                        if(comp < 0)
                        {
                            // System.out.println( "I2 is shortened at
                            // the beginning." );
                            result.add(r.item); // letztes Segment
                                                // hinzufuegen
                            e2.b[0] = new Bound(r.b[1].n[0],
                                    !r.b[1].inclusion[0]);
                            e2.i = new Interval(e2.b[0], e2.b[1]);
                            e2.item.setKey(e2.i);
                            _getNextItem(_functions, e1);
                            // Kontur 1 restaurieren fuer naechsten
                            // Durchlauf:
                        }
                        else
                        { // if
                        // System.out.println( "I1 is set to last
                        // segment." );
                            e1.b = r.b;
                            e1.i = r.i;
                            e1.item.setKey(e1.i);
                            _getNextItem(env, e2);
                        } // else
                    }
                    else
                    { // if
                        // Kontur 1 verkuerzen fuer naechsten
                        // Durchlauf:
                        if(comp > 0)
                        {
                            // System.out.println( "I1 is shortened at
                            // the beginning." );
                            result.add(r.item); // letztes Segment
                                                // hinzufuegen
                            e1.b[0] = new Bound(r.b[1].n[0],
                                    !r.b[1].inclusion[0]);
                            e1.i = new Interval(e1.b[0], e1.b[1]);
                            e1.item.setKey(e1.i);
                            _getNextItem(env, e2);
                            // Kontur 2 restaurieren fuer naechsten
                            // Durchlauf:
                        }
                        else
                        { // if
                        // System.out.println( "I2 is set to last
                        // segment." );
                            e2.b = r.b;
                            e2.i = r.i;
                            e2.item.setKey(e2.i);
                            _getNextItem(_functions, e1);
                        } // else
                    } // else
                } // else
            } // else
            // System.out.println( _functions );
            // System.out.println( env );
            // System.out.println( result );
        } // while

        // Verbleibende Konturteile einfuegen:
        while(e1.item != null)
        {
            result.add(e1.item);
            e1.item = _functions.next(e1.item);
        } // while
        while(e2.item != null)
        {
            result.add(e2.item);
            e2.item = env.next(e2.item);
        } // while

        // neue Kontur uebernehmen:
        this._functions = result;
        // System.out.println( "--final:" + this._functions );
        // System.out.println();

    } // add

    /**
     * Liefert zu dem spezifizierten Funktionsargument <i>x</i> die Funktion, die
     * in der Kontur an der Stelle <i>x</i> liegt.
     *
     * @param x die Stelle der Kontur, fuer die die repraesentierende Funktion
     *        gesucht wird
     * @return die Funktion in der Kontur an der Stelle <i>x</i>
     */
    public RealFunction getFunctionFor(double x)
    {

        // leere Liste abfangen:
        if(_functions.empty()) return null;
        // passendes Polynom suchen:
        ListItem item = _functions.first();
        while(item != null)
        {
            if(((Interval) item.key()).contains(new Argument(x))) return (RealFunction) item
                    .value();
            else item = _functions.next(item);
        } // while
        return null;

    } // getFunctionFor

    // *************************************************************************
    // Private methods
    // *************************************************************************

    /*
     * Hilfsmethode fuer das Setzen des naechsten Listenelements.
     */
    private void _prepareNextItem(EnvItem e, ListItem item)
    {

        e.item = item;
        if(e.item != null)
        {
            e.i = (Interval) item.key();
            e.b = e.i.getBounds();
            e.f = (RealFunction) item.value();
        } // if

    } // _prepareNextItem

    /*
     * Hilfsmethode fuer die Ermittlung des naechsten Listenelements.
     */
    private void _getNextItem(SimpleList list, EnvItem e)
    {

        _prepareNextItem(e, list.next(e.item));

    } // _getNextItem

    /*
     * Hilfsmethode zum Kopieren eines Listenelements.
     */
    private void _copyItem(EnvItem source, EnvItem target)
    {

        target.i = source.i;
        target.b = target.i.getBounds();
        target.f = source.f;
        target.item = new StdListItem(target.i, target.f);

    } // _copyItem

    /*
     * Hilfsmethode zum Vergleich zweier Grenzwerte.
     */
    private int _compare(Bound b1, Bound b2)
    {

        return ((Double) b1.n[0]).compareTo((Double) b2.n[0]);

    } // _compare

    /*
     * Hilfsmethode zum Vergleich zweier Grenzwerte; beruecksichtigt bei gleichen
     * Zahlenwerten auch die Schrankenzugehoerigkeit.
     */
    private int _compareExt(Bound b1, Bound b2)
    {

        int comp = ((Double) b1.n[0]).compareTo((Double) b2.n[0]);
        if(comp == 0)
        {
            if(b1.inclusion[0] != b2.inclusion[0])
            {
                if(b1.inclusion[0]) comp = 1;
                else comp = -1;
            } // if
        } // if
        return comp;

    } // _compareExt

} // RealEnvelope
