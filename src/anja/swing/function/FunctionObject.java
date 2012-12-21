package anja.swing.function;

import java.awt.Color;

import org.jdom.Element;

import anja.analysis.RealFunction;

import anja.swing.SceneObject;


/**
* Objekte dieser Klasse fassen Funktionen mit zusaetzlichen Daten zusammen zur
* Verwendung in einer {@link FunctionScene Funktionszene}.
*
* @version 0.8 22.08.2004
* @author Sascha Ternes
*/

public class FunctionObject
extends SceneObject
{

   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * die Funktion
   */
   protected RealFunction _function;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Funktionsobjekt, dessen Variablen noch belegt werden
   * muessen.<p>
   *
   * Instanzen, die mit diesem Konstruktor erzeugt wurden, besitzen noch kein
   * zugeordnetes <code>FunctionPlugin</code>-Objekt zum Editieren der
   * Funktionsdaten und koennen daher nicht ohne weiteres in einem
   * <code>FunctionEditor</code> verwendet werden!
   */
   public FunctionObject() {

      this( null, null, null, null );

   } // FunctionObject


   /**
   * Erzeugt ein neues Funktionsobjekt fuer die spezifizierte Funktion. Die
   * uebrigen Daten muessen noch gesetzt werden.<p>
   *
   * Instanzen, die mit diesem Konstruktor erzeugt wurden, besitzen noch kein
   * zugeordnetes <code>FunctionPlugin</code>-Objekt zum Editieren der
   * Funktionsdaten und koennen daher nicht ohne weiteres in einem
   * <code>FunctionEditor</code> verwendet werden!
   *
   * @param function die Funktion
   */
   public FunctionObject(
      RealFunction function
   ) {

      this( function, null, null, null );

   } // FunctionObject


   /**
   * Erzeugt ein neues Funktionsobjekt, dessen Variablen noch belegt werden
   * muessen.
   *
   * @param plugin die <code>FunctionPlugin</code>-Instanz, die die
   *        Funktionsdialoge abwickelt
   */
   public FunctionObject(
      FunctionPlugin plugin
   ) {

      this( null, null, null, plugin );

   } // FunctionObject


   /**
   * Erzeugt ein neues Funktionsobjekt fuer die spezifizierte Funktion. Die
   * uebrigen Daten muessen noch gesetzt werden.
   *
   * @param function die Funktion
   * @param plugin die <code>FunctionPlugin</code>-Instanz, die die
   *        Funktionsdialoge abwickelt
   */
   public FunctionObject(
      RealFunction function,
      FunctionPlugin plugin
   ) {

      this( function, null, null, plugin );

   } // FunctionObject


   /**
   * Erzeugt ein neues Funktionsobjekt.
   *
   * @param function die Funktion
   * @param name eine frei waehlbare Bezeichnung der Funktion
   * @param color die Darstellungsfarbe der Funktion
   * @param plugin die <code>FunctionPlugin</code>-Instanz, die die
   *        Funktionsdialoge abwickelt
   */
   public FunctionObject(
      RealFunction function,
      String name,
      Color color,
      FunctionPlugin plugin
   ) {

      super( name, color );
      _function = function;
      if ( _function != null )
         _function.name = name;
      _plugin = plugin;

   } // FunctionObject


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus SceneObject kopiert]
   */
   public Element createXML() {

      return null;

   } // createXML


   /**
   * Liefert die Funktion.
   *
   * @return die Funktion
   */
   public RealFunction getFunction() {

      return _function;

   } // getFunction


   /**
   * Setzt die Funktion.
   *
   * @param function die Funktion
   */
   public void setFunction(
      RealFunction function
   ) {

      _function = function;

   } // setFunction


   /**
   * Aendert die Bezeichnung der Funktion.
   *
   * @param name die Bezeichnung
   */
   public void setName(
      String name
   ) {

      super.setName( name );
      if ( _function != null )
         _function.name = name;

   } // setName


   /**
   * Testet, ob die spezifizierte Funktion mit dieser Funktion identisch ist.
   *
   * @param scene_object die Testfunktion
   * @return <code>true</code>, falls die Funktionen identisch sind
   */
   public boolean equals(
      Object scene_object
   ) {

      try {
         FunctionObject f = (FunctionObject) scene_object;
         if ( this._function.equals( f._function ) ) return true;
      } catch ( ClassCastException cce ) { // try
         try {
            RealFunction r = (RealFunction) scene_object;
            if ( _function.equals( r ) ) return true;
         } catch ( ClassCastException cce2 ) {} // try
      } // catch
      return false;

   } // equals


} // FunctionObject
