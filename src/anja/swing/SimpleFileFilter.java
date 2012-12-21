package anja.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
* Diese Hilfsklasse definiert einen einfachen Dateifilter fuer Dateien. Dieser
* Filter akzeptiert Dateinamen der Form <code>Name.xxx</code>, wobei
* <code>Name</code> beliebig waehlbar ist und <code>xxx</code> eine dreistellige
* Endung ist.
*
* @version 0.9 19.08.2004
* @author Sascha Ternes
*/

public final class SimpleFileFilter
   extends FileFilter
{

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // die Beschreibung:
   private String _description;

   // die Erweiterung:
   private String _extension;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /*
   * Verbotener Konstruktor.
   */
   public SimpleFileFilter() {}


   /**
   * Erzeugt einen neuen Dateifilter. Die <i>Beschreibung</i> darf nicht
   * <code>null</code> oder leer sein und die <i>Erweiterung</i> muss exakt
   * drei Zeichen lang sein, sonst wird eine
   * <code>IllegalArgumentException</code> ausgeworfen.
   *
   * @param description die Beschreibung des Dateiformats; wird in den
   *        Dateiauswahl-Dialogen des Editors verwendet
   * @param extension die dreistellige Erweiterung fuer die Dateinamen
   * @exception IllegalArgumentException wenn die Regeln fuer die Parameter
   *            verletzt werden
   */
   public SimpleFileFilter(
      String description,
      String extension
   ) {

      super();
      if ( ( description == null ) || description.equals( "" ) ||
           ( extension == null ) || ( extension.length() != 3 ) )
         throw new IllegalArgumentException();
      _description = description;
      _extension = extension;

   } // SimpleFileFilter


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Testet, ob eine Datei einen im Sinne der Definition dieser Klasse gueltigen
   * Dateinamen (<code>[name].[xxx]</code>) hat.
   *
   * @param file die zu testende Datei
   * @return <code>true</code>, wenn der Dateiname gueltig ist
   */
   public boolean accept(
      File file
   ) {

      // Directories immer akzeptieren:
      if ( file.isDirectory() ) {
         return true;
      } // if

      // Dateiendung ermitteln:
      String ext = file.getName();
      int i = ext.lastIndexOf( '.' );
      // Test, ob Dateiendung genau 3 Zeichen lang ist:
      if ( ( i > 0 ) && ( i < ext.length() - 3 ) ) {
         // Test, ob Dateiendung richtig ist:
         if ( ext.substring( ++i ).toLowerCase().equals( _extension ) ) {
            return true;
         } // if
      } // if

      // Sonst keine gueltige Datei:
      return false;

   } // accept


   /**
   * Liefert die Beschreibung des Dateiformats.
   *
   * @return eine kurze Beschreibung des Dateiformats als String
   */
   public String getDescription() {

      return _description;

   } // getDescription


   /**
   * Liefert die Dateinamen-Erweiterung der durch diesen Filter akzeptierten
   * Dateien.
   *
   * @return die Erweiterung
   */
   public String getExtension() {

      return _extension;

   } // getExtension


} // SimpleFileFilter
