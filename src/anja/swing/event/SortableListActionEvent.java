package anja.swing.event;

import java.util.EventObject;

import anja.swing.JSortableListPane;


/**
* Diese Klasse definiert die Ereignisse, die von einem
* <code>SortableListActionListener</code> behandelt werden.
*
* @version 0.7 19.06.2004
* @author Sascha Ternes
*/

public class SortableListActionEvent
extends EventObject
{

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * Aktion: ein Element wurde der Liste hinzugefuegt
   */
   public static final int ADD = 1;

   /**
   * Aktion: ein Element wurde aus der Liste entfernt
   */
   public static final int REMOVE = 2;

   /**
   * Aktion: ein Element wurde nach oben verschoben
   */
   public static final int UP = 3;

   /**
   * Aktion: ein Element wurde nach unten verschoben
   */
   public static final int DOWN = 4;

   /**
   * Aktion: die Liste wurde geloescht
   */
   public static final int CLEAR = 5;

   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * die Aktion, die das zugrundeliegende Listenpanel veraendert hat
   */
   protected int action;

   /**
   * das Listenelement, das von diesem Ereignis betroffen ist
   */
   protected Object element;

   /**
   * der Index des betroffenen Listenelements vor dem Ereignis
   */
   protected int old_index;

   /**
   * der Index des betroffenen Listenelements nach dem Ereignis
   */
   protected int new_index;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues SortableListAction-Ereignis.
   *
   * @param source das Listen-Panel, das dieses Ereignis ausloest
   * @param action die Aktion als eine der Aktions-Konstanten {@link #ADD ADD},
   *        {@link #CLEAR CLEAR}, {@link #DOWN DOWN}, {@link #REMOVE REMOVE},
   *        {@link #UP UP}
   * @param element das betroffene Listenelement
   * @param old_index der alte Index des Elements vor dem Ereignis
   * @param new_index der neue Index des Elements nach dem Ereignis
   */
   public SortableListActionEvent(
      JSortableListPane source,
      int action,
      Object element,
      int old_index,
      int new_index
   ) {

      super( source );
      this.action = action;
      this.element = element;
      this.old_index = old_index;
      this.new_index = new_index;

   } // SortableListActionEvent


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert die durchgefuehrte Aktion.
   *
   * @return die Aktion als eine der Aktions-Konstanten {@link #ADD ADD},
   *         {@link #CLEAR CLEAR}, {@link #DOWN DOWN}, {@link #REMOVE REMOVE},
   *         {@link #UP UP}
   */
   public int getAction() {

      return action;

   } // getAction


   /**
   * Liefert das Listenobjekt zu diesem Ereignis.
   *
   * @return das betroffene Listenobjekt oder <code>null</code>, wenn dieses
   *         Ereignis die Aktion {@link #CLEAR CLEAR} beinhaltet
   */
   public Object getElement() {

      return element;

   } // getElement


   /**
   * Liefert den Index des betroffenen Listenelements vor dem Ereignis.
   *
   * @return den alten Elementindex oder <code>-1</code>, wenn dieser nicht
   *         definiert ist
   */
   public int getOldIndex() {

      return old_index;

   } // getOldIndex


   /**
   * Liefert den Index des betroffenen Listenelements nach dem Ereignis.
   *
   * @return den neuen Elementindex oder <code>-1</code>, wenn dieser nicht
   *         definiert ist
   */
   public int getNewIndex() {

      return new_index;

   } // getNewIndex


} // SortableListActionEvent
