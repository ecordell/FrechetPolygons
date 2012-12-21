package anja.util;

import anja.geom.Point2;
import anja.geom.Segment2;

/**
* Grundlegende Klasse fuer Sweep-Algorithmen. Verwaltet wird eine Event- und eine Statusstruktur.
* Um einen konkreten Sweep zu implementieren, empfiehlt es sich, diese Klasse abzuleiten und
* die Methode <b>processEvent</b> zu ueberschreiben. Nach dem generieren einer Event- und Status-
* Struktur, koennen SweepEvents generiert werden und die Ausfuehrung des Sweeps mittels <b>execute</b>
* gestartet werden.<br>
* Alternativ zum Ueberschreiben der processEvent-Methode, kann auch mit <b>setSweepListener</b> ein
* SweepListener die EventVerarbeitung uebernehmen. Anders als beim Verarbeiten von GUI-Events, duerfen
* SweepEvents nur einmal Verarbeitet werden (es gibt naemlich nur eine Sweep-Status-Struktur), so dass
* <i>entweder</i> die ueberschriebene processEvent-Methode <i>oder</i> die processEvent-Methode
* <i>eines</i> SweepListeners aufgerufen wird.<br>
* Bevor ein Sweep-Algorithmus mit execute() gestartet werden kann, muss erst eine Ereignisstruktur mit
* <b>createEventStructure</b> und eine Sweep-Status-Struktur mit <b>createSSS</b> und den jeweils 
* passenden Vergleicher-Objekten erzeugt werden, sowie Events mit <b>insertEvent</b>eingefuegt werden, 
* mit deren Bearbeitung der Sweep beginnen kann.<br>
* Die Sweep-Status-Struktur ist ein balancierter Binaerbaum und kann als Binaerbaum direkt manipuliert
* werden.
*
* @see SweepEvent
*
* @author Thomas Wolf
* @version 1.0
* 
* Die Klasse wurde um die Funktion execute(int until) erweitert.
* execute bricht nach spaetestens n Events ab.
* 
* @author Darius Geiss
* @version 1.1
*/
public class Sweep implements SweepListener {

	/** Balancierter Binaerbaum fuer die Ereignisstruktur. */
	private RedBlackTree _events=null;
	/** Balancierter Binaerbaum fuer die Sweep-Status-Struktur. */
	private RedBlackTree _sss=null;
	/** SweepListener fuer Eventverarbeitung (entweder man leitet die Klasse ab oder man instanziiert
	* sie und schreibt den eigentlichen Code in einer anderen Klasse, die SweepListener implementiert). */
	private SweepListener _sweep=this;

	// Konstruktion
	// ============

	/**
	* Leerer Konstruktor.
	*/
	public Sweep() {
		_events=_sss=null; _sweep=this;
	}
	/**
	* Konstruiert ein Sweep-Objekt und setzt den SweepListener auf sweep.
	* @param sweep neuer SweepListener
	*/
	public Sweep(SweepListener sweep) {
		this();
		if (sweep!=null) _sweep=sweep;
	}

	// Start- und Ereignismethoden
	// ===========================
	
	/**
	* Setzt den SweepListener auf sweep. Statt bei execute die processEvent-Methode dieser
	* Klasse aufzurufen, wird die processEvent-Methode des SweepListeners aufgerufen.
	* @param sweep neuer SweepListener
	*/
	public void setSweepListener(SweepListener sweep) {
		if (sweep!=null) _sweep=sweep;
	}
	/**
	* Startet die Ausfuehrung des Sweeps. Zuvor muss die Ereignisstruktur initialisiert
	* worden sein.<br>
	* Es werden nacheinander alle Events, der Ereignisstruktur mittels processEvent
	* abgearbeitet. Waehrenddessen koennen auch neue angefuegt werden. execute endet,
	* wenn das letzte Ereignis verarbeitet wurde.
	*/
	public final synchronized void execute() {
		boolean stop = false;
		if ((_events==null) || (_sss==null) || (_sweep==null)) return;
		for (TreeItem i=_events.first();i!=null & stop == false;i=_events.next(i)) {
			_sweep.processEvent((SweepEvent)i);
		}
		_events.clear();
	}
	/**
	* Startet die Ausfuehrung des Sweeps. Zuvor muss die Ereignisstruktur initialisiert
	* worden sein.<br>
	* Es werden nacheinander alle Events, der Ereignisstruktur mittels processEvent
	* abgearbeitet. Waehrenddessen koennen auch neue angefuegt werden. execute endet,
	* wenn das letzte Ereignis verarbeitet wurde, oder der eventCouter until erreicht hat.
	*/
	public final synchronized void execute(int until) {
		boolean stop = false;
		if ((_events==null) || (_sss==null) || (_sweep==null)) return;
		int counter = 0;
		for (TreeItem i=_events.first();i!=null & stop == false;i=_events.next(i)) {
			counter++;
			_sweep.processEvent((SweepEvent)i);
			if ((counter >= until) && (until > 0)){
				break;
			}
		}
		_events.clear();
	}
	/**
	* Verarbeitet das SweepEvent e.
	* @param e SweepEvent
	*/
	public void processEvent(SweepEvent e) {
	}

	// Zugriff auf die Ereignisstruktur
	// ================================

	/**
	* Erzeugt eine neue Ereignisstruktur mit dem Comparitor comparitor.
	* @param comparitor Vergleicher um die Ordnung der Ereignisstruktur zu 
	* gewaehrleisten
	* @param smallfirst 'kleinere' Ereignisse werden zuerst abgearbeitet, falls
	* true, ansonsten zuletzt
	*/
	public void createEventStructure(Comparitor comparitor,boolean smallfirst) {
		if (comparitor==null) comparitor=new StdComparitor();
		_events=new RedBlackTree(comparitor,smallfirst);
	}
	/**
	* Erzeugt eine neue Ereignisstruktur mit dem Comparitor comparitor.
	* @param comparitor Vergleicher um die Ordnung der Ereignisstruktur zu 
	* gewaehrleisten
	*/
	public void createEventStructure(Comparitor comparitor) {
		createEventStructure(comparitor,true);
	}
	/**
	* Fuegt ein Ereignis in die Ereignisstruktur ein.
	* Die Laufzeit betraegt O(log n) mit n=Anzahl der bisherigen Ereignisse
	* @param id Event-ID
	* @param key Schluessel, nachdem die Ereignisse sortiert sind
	* @param value zu speichernder Wert fuer das Event
	*/
	public void insertEvent(int id,Object key,Object value) {
		if (_events==null) return;
		_events.add(new SweepTreeItem(id,key,value));
	}

	// Zugriff auf die die Statusstruktur
	// ==================================

	/**
	* Erzeugt eine neue Sweep-Status-Struktur mit der durch den Vergleicher
	* comparitor definierten Ordnung.
	* @param comparitor Vergleicher zur Gewaehrleistung der Ordnung in der SSS
	* @param smallfirst kleinere Objekte werden zuerst abgespeichert, falls
	* true, ansonsten zuletzt
	*/
	public void createSSS(Comparitor comparitor,boolean smallfirst) {
		if (comparitor==null) comparitor=new StdComparitor();
		_sss=new RedBlackTree(comparitor,smallfirst);
	}
	/**
	* Erzeugt eine neue Sweep-Status-Struktur mit der durch den Vergleicher
	* comparitor definierten Ordnung.
	* @param comparitor Vergleicher zur Gewaehrleistung der Ordnung in der SSS
	*/
	public void createSSS(Comparitor comparitor) {
		createSSS(comparitor,true);
	}
	/**
	* Liefert die Sweep-Status-Struktur. Hinzufuegen, Suchen und Loeschen benoetigen
	* jeweils O(log n) Laufzeit, wobei n die Anzahl der in der SSS gespeicherten 
	* Elemente bezeichnet
	* @return die Sweep-Status-Struktur als balancierter Binaerbaum
	*/
	public BinarySearchTree sss() {
		return _sss;
	}

	/**
	 * 
	 * @return die Event-Reihenfolge als balancierter Binaerbaum
	 */
	public BinarySearchTree event() 
	{
		return _events;
	}

}
