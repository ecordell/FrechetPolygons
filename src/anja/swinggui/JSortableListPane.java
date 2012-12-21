package anja.swinggui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

/*
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener; */

import anja.swinggui.event.SortableListActionEvent;
import anja.swinggui.event.SortableListActionListener;


/**
* Eine <code>JList</code>-Komponente mit optionalem Titel-Label. Durch
* entsprechende Buttons koennen die Eintraege der Liste nach oben und unten
* verschoben werden, einzelne Eintraege koennen entfernt werden und die ganze
* Liste kann geloescht werden; allerdings sind die Listeneintraege nicht einzeln
* editierbar.<br>
* Alle Komponenten sind in ein <code>JPanel</code> eingebaut und die
* Hilfsobjekte (Titelzeile und Buttons) koennen individuell an- oder
* abgeschaltet werden.
*
* @version 0.9 09.12.2003
* @author Sascha Ternes
*/

public class JSortableListPane
extends JPanel
implements ActionListener,
           ListDataListener,
           ListSelectionListener
{

   // *************************************************************************
   // Private constants
   // *************************************************************************

   // Beschriftungen der Buttons:
   private static final String _UP = "Up";
   private static final String _DOWN = "Down";
   private static final String _REMOVE = "Remove";
   private static final String _CLEAR = "Clear";


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // Titelzeile:
   private JLabel _title;
   // Liste
   private JList _list;
   private DefaultListModel _model;
   private DefaultListSelectionModel _sel;
   // Button-Panel:
   private JPanel _buttons;
   // Verschiebebuttons:
   private JButton _up;
   private JButton _down;
   // Entfernen-Knopf:
   private JButton _remove;
   // Loeschbutton:
   private JButton _clear;

   // zu benachrichtigende ActionListener:
   private Vector _listeners;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt eine neue leere sortierbare Liste ohne Titelzeile, jedoch allen
   * weiteren Komponenten.
   */
   public JSortableListPane() {

      this( null );

   } // JSortableListPane


   /**
   * Erzeugt eine neue leere sortierbare Liste mit allen Komponenten. Die
   * Titelzeile erhaelt den uebergebenen String.
   *
   * @param title der gewuenschte Titel der Liste
   */
   public JSortableListPane(
      String title
   ) {

      super( new BorderLayout() );

      _title = new JLabel( title );
      if ( title == null ) {
         _title.setVisible( false );
      } else { // if
         add( _title, BorderLayout.NORTH );
      } // else

      _listeners = new Vector( 1 );

      _model = new DefaultListModel();
      _model.addListDataListener( this );
      _list = new JList( _model );
      _sel = (DefaultListSelectionModel) _list.getSelectionModel();
      _list.addListSelectionListener( this );
      JScrollPane scroll = new JScrollPane( _list );
      add( scroll, BorderLayout.CENTER );

      GridBagLayout gbl = new GridBagLayout();
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.BOTH;
      gbc.weightx = 1.0;
      _buttons = new JPanel( gbl );
      _up = new JButton( _UP );
      _up.addActionListener( this );
      gbl.setConstraints( _up, gbc );
      _buttons.add( _up );
      _down = new JButton( _DOWN );
      _down.addActionListener( this );
      gbl.setConstraints( _down, gbc );
      _buttons.add( _down );
      _remove = new JButton( _REMOVE );
      _remove.addActionListener( this );
      gbl.setConstraints( _remove, gbc );
      _buttons.add( _remove );
      _clear = new JButton( _CLEAR );
      _clear.addActionListener( this );
      gbl.setConstraints( _clear, gbc );
      _buttons.add( _clear );
      add( _buttons, BorderLayout.SOUTH );
      _adjustButtons();

   } // JSortableListPane


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Fuegt ein Element an der spezifizierten Position in die Liste ein.<p>
   *
   * @param index der Index, an den das Element eingefuegt werden soll
   * @param element das einzufuegende Element
   * @exception ArrayIndexOutOfBoundsException falls <code>index</code> nicht
   *            innerhalb der Grenzen der Liste liegt <code>( index < 0 ||
   *            index > size() )</code>.
   */
   public void add(
      int index,
      Object element
   ) {

      _model.add( index, element );
      fireActionPerformed( SortableListActionEvent.ADD, element, -1, index );

   } // add


   /**
   * Fuegt ein Element am Ende der Liste an.
   *
   * @param obj die hinzuzufuegende Komponente
   */
   public void addElement(
      Object obj
   ) {

      _model.addElement( obj );
      fireActionPerformed( SortableListActionEvent.ADD, obj, -1,
                                                           _model.size() - 1 );

   } // addElement


   /**
   * Entfernt ein Element aus der Liste.
   *
   * @param index der Index des zu entfernenden Elements
   */
   public void remove(
      int index
   ) {

      Object o = _model.remove( index );
      fireActionPerformed( SortableListActionEvent.REMOVE, o, index, -1 );

   } // removeElement


   /**
   * Entfernt ein Element aus der Liste.
   *
   * @param obj das zu entfernende Element
   */
   public void removeElement(
      Object obj
   ) {

      int i = _model.indexOf( obj );
      if ( _model.removeElement( obj ) )
         fireActionPerformed( SortableListActionEvent.REMOVE, obj, i, -1 );

   } // removeElement


   /**
   * Liefert das Listenelement an der gegebenen Position.<p>
   *
   * Wenn der index ausserhalb der Grenzen der aktuellen Liste liegt
   * (<code>index < 0 || index >= size()</code>), wird eine
   * <code>ArrayIndexOutOfBoundsException</code> geworfen.
   *
   * @param index der Index des gewuenschten Elements
   * @return das Element am spezifizierten Index
   */
   public Object get(
      int index
   ) {

      return _model.get( index );

   } // get


   /**
   * Liefert die Anzahl der in der Liste enthaltenen Elemente.
   *
   * @return die Zahl der Listenelemente
   */
   public int getListSize() {

      return _model.size();

   } // size


   /**
   * Liefert eine Aufzaehlung aller Listenelemente.
   *
   * @return eine Aufzaehlung aller Elemente in der Liste
   */
   public Enumeration elements() {

      return _model.elements();

   } // elements


   /**
   * Liefert den Titel der Liste.
   *
   * @return den Titel der Liste oder <code>null</code>, wenn die Titelzeile
   *         ausgeschaltet ist
   */
   public String getTitle() {

      return _title.getText();

   } // getTitle


   /**
   * Liefert den Status der Titelzeile.
   *
   * @return <code>true</code>, wenn die Titelzeile eingeschaltet ist
   */
   public boolean isTitleShown() {

      return _title.isVisible();

   } // isTitleShown


   /**
   * Setzt den Titel der Liste neu. War die Titelzeile bisher ausgeschaltet, so
   * wird die Titelanzeige zugleich eingeschaltet.
   */
   public void setTitle(
      String title
   ) {

      _title.setText( title );
      setTitleVisible( true );

   } // setTitle


   /**
   * Fuegt den spezifizierten <code>SortableListActionListener</code> dem Panel
   * hinzu. Wenn <code>null</code> uebergeben wird, passiert nichts.
   *
   * @param l der <code>SortableListActionListener</code>
   */
   public void addSortableListActionListener(
      SortableListActionListener l
   ) {

      if ( l != null )
         _listeners.add( l );

   } // addSortableListActionListener


   /**
   * Entfernt den spezifizierten <code>SortableListActionListener</code> aus
   * dem Panel. Wenn <code>null</code> uebergeben wird, passiert nichts.
   *
   * @param l der <code>SortableListActionListener</code>
   */
   public void removeSortableListActionListener(
      SortableListActionListener l
   ) {

      if ( l != null )
         _listeners.remove( l );

   } // removeSortableListActionListener


   /**
   * Erzeugt ein <code>actionPerformed</code>-Ereignis bei allen registrierten
   * SortableListAction-Listenern.
   *
   * @param action die aufgetretene Aktion
   * @param element das beteiligte Element der Liste
   * @param old_index die alte Position des Elements in der Liste
   * @param new_index die neue Position des Elements in der Liste
   */
   protected void fireActionPerformed(
      int action,
      Object element,
      int old_index,
      int new_index
   ) {

      SortableListActionEvent e = new SortableListActionEvent( this, action,
                                               element, old_index, new_index );
      for ( int i = 0; i < _listeners.size(); i++ )
         ( (SortableListActionListener) _listeners.get( i ) ).
                                              sortableListActionPerformed( e );

   } // fireActionPerformed


   /**
   * Aktiviert oder deaktiviert die Titelzeile.
   *
   * @param status <code>true</code> schaltet die Titelanzeige ein,
   *        <code>false</code> schaltet sie aus
   */
   public void setTitleVisible(
      boolean status
   ) {

      _title.setVisible( status );

   } // setTitleVisible


   /**
   * Aktiviert oder deaktiviert die Verschiebe-Buttons.
   *
   * @param status <code>true</code> schaltet die Verschiebe-Buttons ein,
   *        <code>false</code> schaltet sie aus
   */
   public void setMoveButtonsVisible(
      boolean status
   ) {

      _up.setVisible( status );
      _down.setVisible( status );

   } // setMoveVisible


   /**
   * Aktiviert oder deaktiviert den Button zum Entfernen einzelner
   * Listenelemente.
   *
   * @param status <code>true</code> schaltet den Button ein,
   *        <code>false</code> schaltet ihn aus
   */
   public void setRemoveButtonVisible(
      boolean status
   ) {

      _remove.setVisible( status );

   } // setRemoveButtonVisible


   /**
   * Aktiviert oder deaktiviert den Button zum Loeschen der Liste.
   *
   * @param status <code>true</code> schaltet den Button ein,
   *        <code>false</code> schaltet ihn aus
   */
   public void setClearButtonVisible(
      boolean status
   ) {

      _clear.setVisible( status );

   } // setClearButtonVisible


   /**
   * Aktiviert oder deaktiviert dieses <code>JSortableListPane</code>.
   *
   * @param enabled falls <code>true</code>, wird diese Liste aktiviert,
   *        sonst deaktiviert
   */
   public void setEnabled(
      boolean enabled
   ) {

      _title.setEnabled( enabled );
      _list.setEnabled( enabled );
      _up.setEnabled( enabled );
      _down.setEnabled( enabled );
      _remove.setEnabled( enabled );
      _clear.setEnabled( enabled );
      _buttons.setEnabled( enabled );
      super.setEnabled( enabled );

   } // setEnabled


   /**
   * Setzt das Renderer-Objekt, das fuer das Zeichnen jedes Elements in der
   * Liste verwendet wird.
   *
   * @param cell_renderer der <code>ListCellRenderer</code>, der Elemente zur
   *        Anzeige vorbereitet
   */
   public void setCellRenderer(
      ListCellRenderer cell_renderer
   ) {

      _list.setCellRenderer( cell_renderer );

   } // setCellRenderer


   // *************************************************************************
   // Interface ActionListener
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus ActionListener kopiert]
   */
   public void actionPerformed(
      ActionEvent e
   ) {

      Object o = e.getSource();

      // bei Clear:
      if ( o == _clear ) {
         _model.clear();
         fireActionPerformed( SortableListActionEvent.CLEAR, null, -1, -1 );
         return;
      } // if

      // Durchlaufen des Intervalls:
      int si[] = _list.getSelectedIndices();
      Object o1 = null;
      Object o2 = null;
      // bei Up:
      if ( o == _up ) {
         for ( int i = 0; i < si.length; i++ ) {
            o1 = _model.remove( si[i] );
            o2 = _model.remove( si[i] - 1 );
            _model.insertElementAt( o1, si[i] - 1 );
            _model.insertElementAt( o2, si[i] );
            fireActionPerformed( SortableListActionEvent.UP, o1, si[i],
                                                                   si[i] - 1 );
            si[i]--;
         } // for
         _list.setSelectedIndices( si );
         return;
      } // if
      // bei Down:
      if ( o == _down ) {
         for ( int i = si.length - 1; i >= 0; i-- ) {
            o2 = _model.remove( si[i] + 1 );
            o1 = _model.remove( si[i] );
            _model.insertElementAt( o2, si[i] );
            _model.insertElementAt( o1, si[i] + 1 );
            fireActionPerformed( SortableListActionEvent.DOWN, o1, si[i],
                                                                   si[i] + 1 );
            si[i]++;
         } // for
         _list.setSelectedIndices( si );
      } // if
      // bei Remove:
      else {
         int s = _list.getMinSelectionIndex();
         for ( int i = si.length - 1; i >= 0; i-- ) {
            Object obj = _model.remove( si[i] );
            fireActionPerformed( SortableListActionEvent.REMOVE, obj, si[i],
                                                                          -1 );
         } // for
         int m = _model.size() - 1;
         if ( m < s ) {
            s = m;
         } // if
         _list.setSelectedIndex( s );

      } // else

   } // actionPerformed


   // *************************************************************************
   // Interface ListDataListener
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus ListSelectionListener kopiert]
   */
   public void intervalAdded(
      ListDataEvent e
   ) {

      _adjustButtons();

   } // intervalAdded


   /*
   * [javadoc-Beschreibung wird aus ListSelectionListener kopiert]
   */
   public void intervalRemoved(
      ListDataEvent e
   ) {

      _adjustButtons();

   } // intervalRemoved


   // folgende Methode wird nicht verwendet:
   public void contentsChanged( ListDataEvent e ) {}


   // *************************************************************************
   // Interface ListSelectionListener
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus ListSelectionListener kopiert]
   */
   public void valueChanged(
      ListSelectionEvent e
   ) {

      if ( ! e.getValueIsAdjusting() ) {
         _adjustButtons( );
      } // if

   } // valueChanged


   // *************************************************************************
   // Private methods
   // *************************************************************************

   /*
   * Passt die Buttons auf den aktuellen Zustand der Liste an.
   */
   private void _adjustButtons() {

      if ( ! isEnabled() ) return; // sicherheitshalber

      int n = _model.size();
      int i = _sel.getMinSelectionIndex();
      int j = _sel.getMaxSelectionIndex();

      // Up anpassen:
      _up.setEnabled( i > 0 );
      // Down anpassen:
      _down.setEnabled( ( i > -1 ) && ( j < n - 1 ) );
      // Remove anpassen:
      _remove.setEnabled( i > -1 );
      // Clear anpassen:
      _clear.setEnabled( n > 0 );

   } // _adjustButtons


} // JSortableListPane
