package anja.util;

/**<p>
* Ein Rot-Schwarz-Baum, also ein vollständiger balancierter Binärbaum mit 
* folgenden vier Eigenschaften: <ol>
* <li> Jeder Knoten ist entweder rot oder schwarz
* <li> Jedes Blatt ist schwarz
* <li> Jeder rote Knoten hat nur schwarze Kinder
* <li> Jeder absteigende Pfad von einem Knoten zu irgendeinem einem Blatt 
* enthält dieselbe Anzahl schwarzer Knoten. </ol>
* Um die Vollständigkeit des Baumes zu garantieren, sind in diesem Context
* die Null-Pointer an den bisherigen Blättern als Blätter anzusehen (isLeaf()
* liefert natürlich nach wie vor für Knoten mit null-Pointern true).
* Nach dieser Vereinbarung ist es also möglich, das ein 'Blatt' (unserer alten 
* Definition) rot ist.
* Die Höhe eines Rot-Schwarz-Baumes beträgt höchstens 1*log(length()+1), ist also
* in O(log(length())). Suchen, Einfügen und Löschen passieren also in 
* O(log(length())) Zeit.
*
* @author Thomas Wolf
* @version 1.0
*/
public class RedBlackTree extends BinarySearchTree {
	
	/** Konstante für rote Knoten. */
	public final static byte RED=0;
	/** Konstante für schwarze Knoten. */
	public final static byte BLACK=1;

	// Konstruktion
	// ============
	
	/**
	* Leerer Konstruktor. Zum Vergleich der Baumelemente werden die
	* Hash-Werte der Objekte benutzt.
	*/
	public RedBlackTree() { super(); }
	/**
	* Standard-Konstruktor. Uebergeben wird ein Comparitor-Objekt, das den
	* Vergleich der Baumknoten ausführt.
	*/
	public RedBlackTree(Comparitor comparitor) { super(comparitor); }
	/**
	* Konstruiert einen Binärbaum mit dem Comparitor comparitor und der Angabe
	* ob kleinere Schlüssel in linken oder rechten Teilbäumen gespeichert werden.
	* @param comparitor Comparitor-Objekt zum Vergleichen von Schlüsseln
	* @param left_is_smaller falls true, werden kleinere Schlüssel im linken
	* Teilbaum gespeichert, ansonsten im rechten
	*/
	public RedBlackTree(Comparitor comparitor,boolean left_is_smaller) { super(comparitor,left_is_smaller); }
	
 // Zugriff
 // =======
 
 /**
 * Liefert die black-height des Teilbaumes mit Wurzel root. Dies ist die
 * Anzahl schwarzer Knoten eines (und damit jedes) Pfades ohne die Wurzel
 * root selbst. Da die eigentlichen Blätter des Red-Black-Trees die null-
 * Pointer an den Enden des hier verwalteten Baumes sind (und diese immer
 * schwarz sein müssen), ist die black-height um eins größer als die so
 * zu zählende Anzahl. Für root==null gibt deshalb die Methode 0 zurück.
 * Gehört root nicht zu diesem Baum gibt blackHeight einen negativen Wert
 * zurück.
 * @param root Wurzel des Teilbaumes
 * @return black-height des Teilbaumes
 */
 public int blackHeight(TreeItem root) {
 	if (root==null) return 0; else if (!contains(root)) return -1;
 	int b=1;
 	root=root.child(TreeItem.LEFT);
 	while (root!=null) {
 		if (root.balance()==BLACK) b++;
 		root=root.child(TreeItem.LEFT);
 	}
 	return b;
 }
 /**
 * Liefert die black-height des gesamten Baumes zurück.
 * @see #blackHeight(TreeItem)
 * @return black-height des Baumes
 */
 public int blackHeight() { return blackHeight(root()); }

	// Rebalancierung
	// ==============
	
	/**
	* Liefert links falls pos rechts ist und umgekehrt.
	* @param pos zu invertierende Position
	* @return inverse Position
	*/
	private int inversePos(int pos) {
		return (pos==TreeItem.LEFT)?TreeItem.RIGHT:TreeItem.LEFT;
	}
	
	/**
	* Rebalancierung und Umfärbung des Baumes nach Einfügen des Knotens x.
	* @param x neues TreeItem
	*/
	private synchronized void rebalancing_insert(TreeItem x) {
	 allowAccess(ANY_ACCESS);
	 x.setBalance(RED);
	 TreeItem u,p,y;
	 int pos,pos1;
	 while ((x.parent()!=null) && (x.parent().parent()!=null) && (x.parent().balance()==RED)) {
	 	u=x.parent(); p=u.parent();
	 	pos=p.pos(u); pos1=u.pos(x);
	 	y=u.sibling();
	 	if ((y!=null) && (y.balance()==RED)) {
	 		u.setBalance(BLACK);
	 		y.setBalance(BLACK);
	 		p.setBalance(RED);
	 		x=p;
	 	} else {
	 		if (pos!=pos1) {
	 			x=u; rotation(x,pos1,inversePos(pos1)); allowAccess(ANY_ACCESS);
	 			u=x.parent(); p=u.parent();
	 		}
	 		u.setBalance(BLACK);
 			p.setBalance(RED);
 		 rotation(p,pos,inversePos(pos)); allowAccess(ANY_ACCESS);
	 	}
	 }
		root().setBalance(BLACK);
	 allowAccess(NO_ACCESS);
	}
	/**
	* Rebalancierung und Umfärbung nach dem Löschen eines Elementes. Beim symmetrischen
	* Entfernen wird oft der symmetrische Nachfolger für das zu entfernende TreeItem
	* eingesetzt und somit eigentlich der symmetrische Nachfolger entfernt. In dieser
	* Routine interessiert nur dieser (also das TreeItem, das wirklich entfernt wurde).
	* x ist das verbleibende Kind dieses TreeItems. Falls x null ist, sollte y, 'der 
	* Vater von x' mit übergeben werden. Das wäre also dann der Vater des zu löschenden
	* Elementes oder in einigen Fällen das verschobene TreeItem selbst (an Stelle des 
	* eigentlich gelöschten TreeItems). Zusätzlich wird noch die Position pos, an der x an
	* y hing übergeben. Hatte das entfernte TreeItem keine Kinder sollte pos die
	* Position des entfernten TreeItems am Vater speichern.
	* @param x verbleibendes Kind des eigentlich entfernten TreeItems
	* @param y Vater von x nach dem Entfernen
	* @param pos Position, an der x an seinem ursprünglichen Vater hing, bzw. Position,
	* an der das eigentlich entfernte TreeItem an seinem Vater hing (falls kein x)
	*/
	private synchronized void rebalance_delete(TreeItem x,TreeItem y,int pos) {
	 allowAccess(ANY_ACCESS);
	 int pos1;
	 TreeItem w;
		while ((x!=root()) && ((x==null) || (x.balance()==BLACK))) {
			if (x!=null) { y=x.parent(); pos=y.pos(x); }
			pos1=inversePos(pos);
			w=y.child(pos1); // w kann nicht null sein!
			if (w.balance()==RED) {
				w.setBalance(BLACK); y.setBalance(RED);
				rotation(y,pos1,pos); allowAccess(ANY_ACCESS);
				w=y.child(pos1);
			}
			if (((w.child(pos)==null) || (w.child(pos).balance()==BLACK))
				  && ((w.child(pos1)==null) || (w.child(pos1).balance()==BLACK))) {
			 w.setBalance(RED); x=y;
			} else {
				if ((w.child(pos1)==null) || (w.child(pos1).balance()==BLACK)) {
					// aus der Bedingung folgt, daß der andere Sohn existiert und rot ist...
					w.child(pos).setBalance(BLACK);
					w.setBalance(RED);
					rotation(w,pos,pos1); allowAccess(ANY_ACCESS);
					w=y.child(pos1);
				}
				w.setBalance(y.balance());
				y.setBalance(BLACK);
				if (w.child(pos1)!=null) w.child(pos1).setBalance(BLACK);
				rotation(y,pos1,pos); allowAccess(ANY_ACCESS);
				x=root();
			}
		}
		if (x!=null) x.setBalance(BLACK);
		allowAccess(NO_ACCESS);
	}
	
 // Hinzufügen und Löschen einzelner Elemente
 // =========================================

 /**
 * Fügt das TreeItem item letztendlich an. Diese Methode wird von allen
 * add-Methoden benutzt und stellt somit die Rebalancierung des 
 * Red-Black-Trees sicher.
 * @param item neues TreeItem
 * @return eingefügtes TreeItem
 */
 protected TreeItem add_item(TreeItem item) {
		beforeAdd(item);
 	item=super.add_item(item);
 	if (item!=null) rebalancing_insert(item);
		afterAdd(item);
 	return item;
 }
 /**
 * Entfernt das TreeItem item aus dem Baum und gibt eine Referenz
 * auf das entfernte TreeItem zurück (oder null, falls es nicht 
 * entfernt werden konnte).
 * Der Baum wird nach dem Entfernen wieder Rebalanciert und Recoloriert.
 * @param item zu entfernendes TreeItem
 * @return entferntes TreeItem
 */
 public synchronized TreeItem remove(TreeItem item) {
 	if (!contains(item)) return null;
		beforeRemove(item);
 	TreeItem y=getRemovableTreeItem(item);
 	if (y!=root()) {
 	 int pos=y.nextChildPos(0);
 	 if (pos<0) pos=y.parent().pos(y);
 	 TreeItem x=y.child(pos);
 	 byte bal=y.balance();
 	 if (y.parent()!=item) y=y.parent();
 	 item=removeSym(item);
 	 if (bal==BLACK) rebalance_delete(x,y,pos);
 	} else item=removeSym(item);
		afterRemove(item);
 	return item;
 }
 
 // Methoden für das Interface Owner
 // ================================

 /**
 * Das Owned-Objekt who bittet den Owner um Genehmigung einer 
 * Aktion vom Typ accesstype mit dem Argument argument.
 * Diese Methode überschreibt requestAccess von BasicTree um 
 * noch einige Restriktionen einzubringen (key() darf nicht
 * geändert werden und maxRank() ebenfalls nicht).
 * @param accesstype Typ der Aktion (des Zugriffs)
 * @param who Objekt, welches den Zugriff erbittet
 * @param argument Argument der Aktion
 * @return true, falls der Owner die Aktion erlaubt, sonst false
 */
 public boolean requestAccess(int accesstype,Object who,Object argument) {
 	boolean answer=super.requestAccess(accesstype,who,argument);
 	if (!answer) return false;
 	if (_access==ANY_ACCESS) return true;
  switch (accesstype) {
  	case TreeItem.SET_BALANCE:
  		return false;
  }
  return true;
 }
	
}