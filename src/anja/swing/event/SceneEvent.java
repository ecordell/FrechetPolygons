package anja.swing.event;

import java.util.EventObject;

import anja.swing.Scene;
import anja.swing.SceneObject;


/**
* Diese Klasse definiert die Ereignisse, die von einem
* <code>SceneListener</code> behandelt werden. Ein Szenenereignis referenziert
* das betroffe Szenenobjekt und zusaetzlich beim Ereignis <i>objectUpdated</i>
* das veraenderte Datenfeld des Objekts. Lediglich beim Ereignis
* <i>prioritiesChanged</i> ist das betroffene Szenenobjekt als undefiniert zu
* betrachten.
*
* @version 0.7 18.07.2004
* @author Sascha Ternes
*/

public class SceneEvent
extends EventObject
{

   // *************************************************************************
   // Public variables
   // *************************************************************************

   /**
   * Feldkonstante fuer ein Ereignis, bei dem sich mindestens ein nicht weiter
   * bekanntes Datenfeld des Szenenobjekts veraendert hat
   */
   public static final int FIELD_ANY = 0;

   /**
   * Feldkonstante fuer das Feld {@link SceneObject#_name _name}
   */
   public static final int FIELD_NAME = 1;


   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * die Szene fuer das Szenenobjekt
   */
   protected Scene scene;

   /**
   * das Szenenobjekt, das von diesem Ereignis betroffen ist
   */
   protected SceneObject object;

   /**
   * beim Ereignis <i>objectUpdated</i> das geaenderte Datenfeld des
   * Szenenobjekts, dessen Objektdaten sich veraendert haben
   */
   protected int field;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Ereignis-Objekt fuer das Szenenereignis
   * <i>prioritiesChanged</i>.
   *
   * @param source die Quelle des Ereignisses
   * @param scene die Szene fuer das betroffene Szenenobjekt
   */
   public SceneEvent(
      Object source,
      Scene scene
   ) {

      this( source, scene, null, FIELD_ANY );

   } // SceneEvent


   /**
   * Erzeugt ein neues Ereignis-Objekt fuer die Szenenereignisse
   * <i>objectAdded</i>, <i>objectRemoved</i> und <i>objectSelected</i>.
   *
   * @param source die Quelle des Ereignisses
   * @param scene die Szene fuer das betroffene Szenenobjekt
   * @param object das betroffene Szenenobjekt
   */
   public SceneEvent(
      Object source,
      Scene scene,
      SceneObject object
   ) {

      this( source, scene, object, FIELD_ANY );

   } // SceneEvent


   /**
   * Erzeugt ein neues Ereignis-Objekt fuer das Szenenereignis
   * <i>objectUpdated</i>.
   *
   * @param source die Quelle des Ereignisses
   * @param scene die Szene fuer das betroffene Szenenobjekt
   * @param object das betroffene Szenenobjekt
   * @param field Feldkonstante fuer das veraenderte Datenfeld
   */
   public SceneEvent(
      Object source,
      Scene scene,
      SceneObject object,
      int field
   ) {

      super( source );
      this.scene = scene;
      this.object = object;
      this.field = field;

   } // SceneEvent


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert die Szene, in der dieses Ereignis aufgetreten ist.
   *
   * @return die Szene fuer das betroffene Szenenobjekt
   */
   public Scene getScene() {

      return scene;

   } // getScene


   /**
   * Liefert das Szenenobjekt zu diesem Ereignis.
   *
   * @return das betroffene Szenenobjekt
   */
   public SceneObject getObject() {

      return object;

   } // getObject


   /**
   * Liefert das geaenderte Datenfeld des Szenenobjekts zum Ereignis
   * <i>objectUpdated</i>.
   *
   * @return das veraenderte Datenfeld als Konstante
   *         {@link #FIELD_NAME FIELD_NAME} oder {@link #FIELD_ANY FIELD_ANY}
   */
   public int getField() {

      return field;

   } // getField


} // SceneEvent
