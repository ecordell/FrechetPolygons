package anja.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;

import java.awt.event.ActionEvent;

import java.util.Enumeration;

import javax.swing.JDialog;
import javax.swing.JLabel;

import anja.swing.event.SceneEvent;
import anja.swing.event.SceneListener;


/**
* Ein Panel mit einer sortierbaren Liste, mit dem die Zeichenreihenfolge der
* Objekte einer {@link Scene Szene} in einem Anzeigepanel veraendert werden
* kann.
*
* @version 0.8 16.08.2004
* @author Sascha Ternes
*/

public class JPaintPriorityPanel
extends JSortableListPane
implements SceneListener
{

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // die Liste:
   private JSortableListPane _pane;

   // die Szene:
   private Scene _scene;


   // *************************************************************************
   // Class methods
   // *************************************************************************

   /**
   * Erzeugt ein eigenstaendiges Dialogfenster mit einem neuen Prioritaetspanel.
   *
   * @return ein <code>JDialog</code>-Fenster
   */
   public static JDialog createJDialog(
      Scene scene
   ) {

      JDialog dialog = new JDialog();
      dialog.setTitle( "Paint priority" );
      dialog.setLocation( 100, 100 );
      dialog.setSize( 130, 150 );
      dialog.setResizable( false );
      dialog.getContentPane().add( new JPaintPriorityPanel( scene ) );
      return dialog;

   } // createJFrame


   /**
   * Erzeugt ein eigenstaendiges Dialogfenster mit einem neuen Prioritaetspanel.
   *
   * @return ein <code>JDialog</code>-Fenster
   */
   public static JDialog createJDialog(
      Frame owner,
      String title,
      Scene scene
   ) {

      JDialog dialog = new JDialog( owner, title);
      if ( title == null ) dialog.setTitle( "Paint priority" );
      if ( owner == null )
         dialog.setLocation( 100, 100 );
      else {
         int x = owner.getX() + ( owner.getWidth() - 130 ) / 2;
         int y = owner.getY() + ( owner.getHeight() - 150 ) / 2;
         dialog.setLocation( x, y );
      } // else
      dialog.setSize( 130, 150 );
      dialog.setResizable( false );
      dialog.getContentPane().add( new JPaintPriorityPanel( scene ) );
      return dialog;

   } // createJFrame


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Panel mit einer sortierbaren Liste zum Aendern der
   * Zeichenprioritaet fuer die angegebene Szene.
   *
   * @param scene die Szene, fuer die Zeichenprioritaeten eingestellt werden
   *        sollen
   */
   public JPaintPriorityPanel(
      Scene scene
   ) {

      this( null, scene );

   } // JPaintPriorityPanel


   /**
   * Erzeugt ein neues Panel mit einer sortierbaren Liste zum Aendern der
   * Zeichenprioritaet fuer die angegebene Szene.
   *
   * @param scene die Szene, fuer die Zeichenprioritaeten eingestellt werden
   *        sollen
   * @param caption die Ueberschrift fuer die Liste, in der sortiert wird
   */
   public JPaintPriorityPanel(
      String caption,
      Scene scene
   ) {

      super( caption );
      _scene = scene;
      setRemoveButtonVisible( false );
      setClearButtonVisible( false );
      Enumeration e = scene.elements();
      while( e.hasMoreElements() )
         addElement( e.nextElement() );
      scene.addSceneListener( this );

   } // JPaintPriorityPanel


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus JSortableListPane kopiert]
   */
   public void actionPerformed(
      ActionEvent e
   ) {

      super.actionPerformed( e );
      _updatePriorities();
      _scene.sort();
      _scene.firePrioritiesChanged( new SceneEvent( this, _scene ) );

   } // actionPerformed


   // *************************************************************************
   // Interface SceneListener
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus SceneListener kopiert]
   */
   public void objectAdded(
      SceneEvent e
   ) {

      SceneObject object = e.getObject();
      _model.add( object.getPriority() - 1, object );

   } // objectAdded

   /*
   * [javadoc-Beschreibung wird aus SceneListener kopiert]
   */
   public void objectRemoved(
      SceneEvent e
   ) {

      _model.removeElement( e.getObject() );

   } // objectRemoved


   /*
   * [javadoc-Beschreibung wird aus SceneListener kopiert]
   */
   public void objectUpdated(
      SceneEvent e
   ) {

      if ( ( e.getField() == SceneEvent.FIELD_ANY ) ||
           ( e.getField() == SceneEvent.FIELD_NAME ) )
         repaint();

   } // objectUpdated


   /*
   * [javadoc-Beschreibung wird aus SceneListener kopiert]
   */
   public void prioritiesChanged(
      SceneEvent e
   ) {

      if ( e.getSource() != this )
         _rebuild();

   } // prioritiesChanged


   // folgende Methode wird nicht verwendet:
   public void objectSelected( SceneEvent e ) {}


   // *************************************************************************
   // Private methods
   // *************************************************************************

   /*
   * Aktualisiert die Prioritaeten aller Objekte nach einer Aenderung in der
   * Liste.
   */
   private void _updatePriorities() {

      for ( int i = 0; i < getListSize(); i++ ) {
         SceneObject object = (SceneObject) get( i );
         object.setPriority( i + 1 );
      } // for

   } // _updatePriorities


   /*
   * Baut die Liste neu auf nach einer Aenderung der Prioritaeten der Objekte.
   */
   private void _rebuild() {

      _model.clear();
      Enumeration e = _scene.elements();
      while( e.hasMoreElements() )
         _model.addElement( e.nextElement() );

   } // _rebuild


} // JPaintPriorityPanel
