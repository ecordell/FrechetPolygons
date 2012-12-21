package anja.swinggui.event;


/**
* Ein Listener zur Ueberwachung von <code>JSortableListPane</code>-Objekten.<br>
* Ueberwacht wird das Hinzufuegen oder Entfernen von Elementen aus der Liste,
* das Verschieben von Elementen innerhalb der Liste und das Loeschen der
* gesamten Liste.
*
* @version 0.1 09.12.03
* @author Sascha Ternes
*/

public interface SortableListActionListener {

/**
* Wird aufgerufen, wenn sich der Inhalt der Liste durch eine Aktion geaendert
* hat.
*
* @param e das <code>SortableListActionEvent</code>-Objekt
*/
public void sortableListActionPerformed( SortableListActionEvent e );


} // SortableListActionListener
