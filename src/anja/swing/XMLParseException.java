package anja.swing;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;


/**
* Exception fuer Szenen, die ihre Objekte aus einer XML-Repraesentation
* auslesen und dabei auf fehlende Elemente, fehlende Kindelemente oder
* Attribute oder fehlerhafte Attribute treffen.
*
* @version 0.9 21.08.2004
*/

public class XMLParseException
extends JDOMException
{

   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Konstruktor fuer eine <code>XMLParseException</code> mit nicht naeher
   * definierter Fehlerursache.
   */
   public XMLParseException() {

      super( "Error occurred while parsing XML Scene." );

   } // XMLParseException


   /**
   * Konstruktor fuer eine <code>XMLParseException</code>, die auf ein
   * fehlendes Element hinweist.
   *
   * @param element der Name des fehlenden Elements
   */
   public XMLParseException(
      String element
   ) {

      super( "Missing element <" + element + "/>." );

   } // XMLParseException


   /**
   * Konstruktor fuer eine <code>XMLParseException</code>, die auf ein
   * fehlendes Kindelement oder Attribut hinweist.
   *
   * @param element das Element, in dem ein Kindelement oder Attribut fehlt
   * @param missing der Name des fehlenden Kindelements oder Attributs
   * @param is_element <code>true</code>, wenn ein Kindelement fehlt,
   *        <code>false</code>, wenn ein Attribut fehlt
   */
   public XMLParseException(
      Element element,
      String missing,
      boolean is_element
   ) {

      super( "Missing " +
         ( is_element ? ( new Element( missing ) ).toString() :
                        ( new Attribute( missing, "" ) ).toString() ) +
                                                      " in " + element + "." );

   } // XMLParseException


   /**
   * Konstruktor fuer eine <code>XMLParseException</code>, die auf ein
   * fehlerhaftes Attribut hinweist.
   *
   * @param element das Element, in dem ein Attribut fehlerhaft ist
   * @param attribute der Name des fehlerhaften Attributs
   */
   public XMLParseException(
      Element element,
      Attribute attribute
   ) {

      super( "Invalid " + attribute + " in " + element + "." );

   } // XMLParseException


} // XMLParseException
