package anja.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Iterator;
import java.util.LinkedList;


/**
* Diese Klasse ermoeglicht die Verwendung von Konfigurationsdateien in
* Programmen ("ini-Dateien"). Es koennen neue Dateien angelegt werden sowie
* vorhandene Dateien ausgelesen, geaendert, erweitert und geloescht werden.<p>
*
* Es steht fuer jeden Java-Grunddatentyp (unterstuetzt werden <code>String,
* boolean, int, long, float, double</code>) je eine Lese- und Schreiboperation
* zur Verfuegung, so dass ein zusaetzliches Typcasting entfaellt (die
* Konfigurationsdatei besteht aus reinem Text, alle Daten werden als String
* gespeichert).<p>
*
* Gueltige Konfigurationsdateien fuer diese Klasse muessen folgende Konventionen
* beachten:<ul>
* <li>Der Inhalt der ini-Datei besteht aus <i>Kapiteln</i>, die
* <i>Schluessel</i> mit zugeordneten <i>Werten</i> beinhalten, und optionalen
* <i>Kommentarzeilen</i> <b>zu Beginn</b> der Datei. Die Datei ist
* zeilenorientiert, d.h. Kapitel und Schluessel erstrecken sich ueber je eine
* ganze Zeile.</li>
* <li>Kommentar darf <b>nur</b> vor der ersten Kapiteldeklaration stehen. Der
* Kommentar darf keine oeffnende eckige Klammer <code>[</code> beinhalten.</li>
* <li>Eine Kapiteldeklaration besteht aus einem beliebigen String,
* eingeschlossen in eckigen Klammern <code>[</code> und <code>]</code>. Die
* Klammer <code>[</code> muss das <b>erste</b> Zeichen in einer Kapitelzeile
* sein.</li>
* <li>Eine Schluesseldeklaration besteht aus einem beliebigen String als
* Schluesselbezeichner, gefolgt von einem Gleichheitszeichen <code>=</code> und
* dem zugeordneten Wert, der ein beliebiger String sein darf. Der Wert darf
* auch Gleichheitszeichen enthalten. Zur Trennung von Schluessel und Wert wird
* das erste in der Zeile vorkommende Gleichheitszeichen verwendet. Der
* Schluessel selbst darf daher <b>kein</b> Gleichheitszeichen enthalten.</li>
* <li>Alle Schluessel werden immer dem vorher deklarierten Kapitel zugeordnet.
* Sollten vor der ersten Kapiteldeklaration Schluessel definiert werden, so
* werden diese als Kommentarzeilen behandelt.</li>
* <li>Ein Kapitel darf keine Schluessel enthalten, kann also leer sein.</li>
* <li>Eine Konfigurationsdatei darf keine Kapitel und somit keine Schluessel
* enthalten, kann also (bis auf Kommentar) leer sein; Kommentar darf natuerlich
* auch fehlen.</li>
* <li>Es duerfen an beliebiger Stelle <b>Leerzeilen</b> stehen.</ul>
*
* @version      1.1  10.09.02
* @author       Sascha Ternes
*/

public class IniFile {

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * Stringersatz fuer boolean <code>true</code> in einer ini-Datei
   */
   public static final String TRUE = String.valueOf( true );
   /**
   * Stringersatz fuer boolean <code>false</code> in einer ini-Datei
   */
   public static final String FALSE = String.valueOf( false );


   // *************************************************************************
   // Public constants
   // *************************************************************************

   // Text der Exception bei fehlendem Schluessel:
   private static final String _EXC_NOT_EXISTING
                             = "Key does not exist in config file: ";
   // Text der Exception bei unkonvertierbarem boolean-Wert:
   private static final String _EXC_NO_BOOL_STRING
                             = "Key is not convertible to boolean: ";
   // Text der Exception bei unkonvertierbarem int-Wert:
   private static final String _EXC_NO_INT_STRING
                             = "Key is not convertible to int: ";
   // Text der Exception bei unkonvertierbarem long-Wert:
   private static final String _EXC_NO_LONG_STRING
                             = "Key is not convertible to long: ";
   // Text der Exception bei unkonvertierbarem float-Wert:
   private static final String _EXC_NO_FLOAT_STRING
                             = "Key is not convertible to float: ";
   // Text der Exception bei unkonvertierbarem double-Wert:
   private static final String _EXC_NO_DOUBLE_STRING
                             = "Key is not convertible to double: ";


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // Geoeffnete Konfigurationsdatei:
   private File _inifile;

   // Speicherung der Kommentare:
   private LinkedList _comments;
   // Speicherung der Kapitel:
   private LinkedList _chapters;

   // Flag, das zu schreibende Aenderungen anzeigt:
   private boolean _changed;


   // *************************************************************************
   // Inner classes
   // *************************************************************************

   /*
   * Unterklasse fuer ein Kapitel.
   */
   class Chapter {

      // Name und Inhalt des Kapitels:
      String name;
      LinkedList keys;


      /*
      * Der Konstruktor erzeugt ein neues leeres Kapitel mit festgelegtem
      * Namen.
      */
      Chapter( String name ) {

         this.name = name;
         keys = new LinkedList();

      } // Chapter


      /*
      * Fuegt einen Schluessel zu diesem Kapitel hinzu.
      */
      void add( KeyEntry entry ) {

         keys.add( entry );

      } // add


   } // Chapter


   /*
   * Unterklasse fuer einen Schluesselwert-Eintrag im Kapitel.
   */
   class KeyEntry {

      // Name und Wert des Schluessels:
      String name;
      String value;


      /*
      * Der Konstruktor erzeugt einen neuen Schluessel mit zugewiesenem Wert.
      */
      KeyEntry( String name, String value ) {

         this.name = name;
         this.value = value;

      } // KeyEntry


   } // KeyEntry


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt eine neue leere Konfigurationsdatei.
   */
   public IniFile() {

      _inifile = null;
      _comments = new LinkedList();
      _chapters = new LinkedList();
      _changed = true;

   } // IniFile


   /**
   * Oeffnet eine vorhandene Konfigurationsdatei oder erzeugt eine neue
   * Konfigurationsdatei mit festgelegtem Dateinamen.
   *
   * @param name der Dateiname der Konfigurationsdatei inkl. Pfadangabe
   */
   public IniFile(
      String name
   ) {

      _inifile = new File( name );
      if ( _inifile.exists() ) {
         _changed = false;
         _getIniFileContent();
      } else { // if
         _comments = new LinkedList();
         _chapters = new LinkedList();
         _changed = true;
      } // else

   } // IniFile


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Prueft, ob die Konfigurationsdatei einen bestimmten Schluessel enthaelt.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @return <code>true</code>, wenn der Schluessel existiert und ihm ein Wert
   *         zugeordnet ist, sonst <code>false</code>
   */
   public boolean contains(
      String chapter,
      String name
   ) {

      // Kapitel suchen:
      Chapter chap = _search( chapter );
      if ( chap == null ) {
         return false; // Kapitel nicht vorhanden -> Schluessel existiert nicht
      } // if
      // Schluessel suchen:
      KeyEntry entry = _search( chap, name );
      if ( entry == null ) {
         return false; // Schluessel existiert nicht
      } // if
      return true; // andernfalls wurde der Schluessel gefunden

   } // contains


   /**
   * Liest einen Schluesselwert aus der Konfigurationsdatei.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @return den Wert des Schluessels, <code>null</code> bei Nichtexistenz
   */
   public String read(
      String chapter,
      String name
   ) {

      // Kapitel suchen:
      Chapter chap = _search( chapter );
      if ( chap != null ) {
         // Schluessel suchen:
         KeyEntry entry = _search( chap, name );
         if ( entry != null ) {
            return entry.value; // gefundenen Wert zurueckliefern
         } // if
      } // if
      return null; // Schluessel existiert nicht

   } // read


   /**
   * Liest einen Schluessel aus der Konfigurationsdatei und interpretiert den
   * Schluesselwert als <code>String</code>-Variable.<br>
   * Diese Methode unterscheidet sich von <a HREF="#read(java.lang.String,
   * java.lang.String)"<code>read(String chapter, String name)</code></a>
   * dadurch, dass ein nicht existenter Schluessel durch eine geworfene
   * <code>IllegalArgumentException</code> angezeigt wird, und nicht durch
   * Rueckgabe von <code>null</code>. 
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @return den Wert des Schluessels als String
   * @throws IllegalArgumentException wenn der Schluessel nicht vorhanden ist
   */
   public String readString(
      String chapter,
      String name
   ) {

      String str = read( chapter, name );
      if ( str == null ) {
         throw new IllegalArgumentException(
                   _EXC_NOT_EXISTING + chapter + "." + name );
      } // if
      return str;

   } // readString


   /**
   * Liest einen Schluessel aus der Konfigurationsdatei und interpretiert den
   * Schluesselwert als <code>boolean</code>-Variable.<br>
   * Um nichtexistente oder fehlerhafte Schluessel abzufangen, kann die dann
   * geworfene <code>IllegalArgumentException</code> abgefangen werden,
   * <b>muss aber nicht</b>.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @return <code>true</code> oder <code>false</code>, je nach Schluesselwert
   * @throws IllegalArgumentException wenn der gelesene Schluesselwert weder
   *         dem String <code>true</code> noch dem String <code>false</code>
   *         entspricht, oder garnicht vorhanden ist
   */
   public boolean readBoolean(
      String chapter,
      String name
   ) {

      String bool = read( chapter, name );
      // Fehler, wenn Schluessel nicht existiert:
      if ( bool == null ) {
         throw new IllegalArgumentException(
                   _EXC_NOT_EXISTING + chapter + "." + name );
      } // if
      bool = bool.toLowerCase();
      if ( bool.equals( TRUE ) ) {
         return true;
      } // if
      if ( bool.equals( FALSE ) ) {
         return false;
      } // if
      // Fehler, wenn Schluesselwert fehlerhaft:
      throw new IllegalArgumentException(
                _EXC_NO_BOOL_STRING + chapter + "." + name + "=" + bool );

   } // readBoolean


   /**
   * Liest einen Schluessel aus der Konfigurationsdatei und interpretiert den
   * Schluesselwert als <code>int</code>-Variable.<br>
   * Um nichtexistente oder fehlerhafte Schluessel abzufangen, kann die dann
   * geworfene <code>IllegalArgumentException</code> abgefangen werden,
   * <b>muss aber nicht</b>.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @return den Schluesselwert als <code>int</code>-Variable
   * @throws IllegalArgumentException wenn der gelesene Schluesselwert nicht
   *         vorhanden ist oder nicht in eine ganze Zahl konvertiert werden
   *         kann
   */
   public int readInt(
      String chapter,
      String name
   ) {

      String number = read( chapter, name );
      // Fehler, wenn Schluessel nicht existiert:
      if ( number == null ) {
         throw new IllegalArgumentException(
                   _EXC_NOT_EXISTING + chapter + "." + name );
      } // if
      // Fehler, wenn Schluesselwert fehlerhaft:
      try {
         return Integer.parseInt( number );
      } catch ( NumberFormatException nfe ) { // try
         throw new IllegalArgumentException(
                   _EXC_NO_INT_STRING + chapter + "." + name + "=" + number );
      } // catch

   } // readInt


   /**
   * Liest einen Schluessel aus der Konfigurationsdatei und interpretiert den
   * Schluesselwert als <code>long</code>-Variable.<br>
   * Um nichtexistente oder fehlerhafte Schluessel abzufangen, kann die dann
   * geworfene <code>IllegalArgumentException</code> abgefangen werden,
   * <b>muss aber nicht</b>.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @return den Schluesselwert als <code>long</code>-Variable
   * @throws IllegalArgumentException wenn der gelesene Schluesselwert nicht
   *         vorhanden ist oder nicht in eine ganze Zahl konvertiert werden
   *         kann
   */
   public long readLong(
      String chapter,
      String name
   ) {

      String number = read( chapter, name );
      // Fehler, wenn Schluessel nicht existiert:
      if ( number == null ) {
         throw new IllegalArgumentException(
                   _EXC_NOT_EXISTING + chapter + "." + name );
      } // if
      // Fehler, wenn Schluesselwert fehlerhaft:
      try {
         return Long.parseLong( number );
      } catch ( NumberFormatException nfe ) { // try
         throw new IllegalArgumentException(
               _EXC_NO_LONG_STRING + chapter + "." + name + "=" + number );
      } // catch

   } // readLong


   /**
   * Liest einen Schluessel aus der Konfigurationsdatei und interpretiert den
   * Schluesselwert als <code>float</code>-Variable.<br>
   * Um nichtexistente oder fehlerhafte Schluessel abzufangen, kann die dann
   * geworfene <code>IllegalArgumentException</code> abgefangen werden,
   * <b>muss aber nicht</b>.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @return den Schluesselwert als <code>float</code>-Variable
   * @throws IllegalArgumentException wenn der gelesene Schluesselwert nicht
   *         vorhanden ist oder nicht in eine Dezimalzahl konvertiert werden
   *         kann
   */
   public float readFloat(
      String chapter,
      String name
   ) {

      String number = read( chapter, name );
      // Fehler, wenn Schluessel nicht existiert:
      if ( number == null ) {
         throw new IllegalArgumentException(
                   _EXC_NOT_EXISTING + chapter + "." + name );
      } // if
      // Fehler, wenn Schluesselwert fehlerhaft:
      try {
         return Float.parseFloat( number );
      } catch ( NumberFormatException nfe ) { // try
         throw new IllegalArgumentException(
               _EXC_NO_FLOAT_STRING + chapter + "." + name + "=" + number );
      } // catch

   } // readFloat


   /**
   * Liest einen Schluessel aus der Konfigurationsdatei und interpretiert den
   * Schluesselwert als <code>double</code>-Variable.<br>
   * Um nichtexistente oder fehlerhafte Schluessel abzufangen, kann die dann
   * geworfene <code>IllegalArgumentException</code> abgefangen werden,
   * <b>muss aber nicht</b>.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @return den Schluesselwert als <code>double</code>-Variable
   * @throws IllegalArgumentException wenn der gelesene Schluesselwert nicht
   *         vorhanden ist oder nicht in eine Dezimalzahl konvertiert werden
   *         kann
   */
   public double readDouble(
      String chapter,
      String name
   ) {

      String number = read( chapter, name );
      if ( number == null ) {
         throw new IllegalArgumentException(
                   _EXC_NOT_EXISTING + chapter + "." + name );
      } // if
      // Fehler, wenn Schluesselwert fehlerhaft:
      try {
         return Double.parseDouble( number );
      } catch ( NumberFormatException nfe ) { // try
         throw new IllegalArgumentException(
               _EXC_NO_DOUBLE_STRING + chapter + "." + name + "=" + number );
      } // catch

   } // readDouble


   /**
   * Schreibt einen Schluessel mit einem <code>String</code>-Wert in die
   * Konfigurationsdatei.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @param value der Wert des Schluessels
   * @return <code>true</code>, wenn der Schluessel bereits existierte und
   *         ueberschrieben wurde, sonst <code>false</code>
   */
   public boolean write(
      String chapter,
      String name,
      String value
   ) {

      _changed = true;
      // Kapitel suchen:
      Chapter chap = _search( chapter );
      if ( chap == null ) {
         // Kapitel nicht vorhanden -> Schluessel nicht vorhanden:
         chap = new Chapter( chapter );
         _chapters.add( chap ); // neues Kapitel erzeugen und anfuegen
      } // if
      // Schluessel suchen:
      KeyEntry entry = _search( chap, name );
      if ( entry == null ) {
         // Schluessel noch nicht vorhanden:
         entry = new KeyEntry( name, value );
         chap.keys.add( entry ); // neuen Schluessel erzeugen und anfuegen
         return false; // Schluessel existierte noch nicht
      } // if
      entry.value = value; // sonst existierenden Schluesselwert ueberschreiben
      return true; // Schluessel hat schon existiert

   } // write


   /**
   * Schreibt einen Schluessel mit einem <code>boolean</code>-Wert in die
   * Konfigurationsdatei.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @param value der Wert des Schluessels
   * @return <code>true</code>, wenn der Schluessel bereits existierte und
   *         ueberschrieben wurde, sonst <code>false</code>
   */
   public boolean write(
      String chapter,
      String name,
      boolean value
   ) {

      return write( chapter, name, String.valueOf( value ) );

   } // write


   /**
   * Schreibt einen Schluessel mit einem <code>int</code>-Wert in die
   * Konfigurationsdatei.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @param value der Wert des Schluessels
   * @return <code>true</code>, wenn der Schluessel bereits existierte und
   *         ueberschrieben wurde, sonst <code>false</code>
   */
   public boolean write(
      String chapter,
      String name,
      int value
   ) {

      return write( chapter, name, String.valueOf( value ) );

   } // write


   /**
   * Schreibt einen Schluessel mit einem <code>long</code>-Wert in die
   * Konfigurationsdatei.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @param value der Wert des Schluessels
   * @return <code>true</code>, wenn der Schluessel bereits existierte und
   *         ueberschrieben wurde, sonst <code>false</code>
   */
   public boolean write(
      String chapter,
      String name,
      long value
   ) {

      return write( chapter, name, String.valueOf( value ) );

   } // write


   /**
   * Schreibt einen Schluessel mit einem <code>float</code>-Wert in die
   * Konfigurationsdatei.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @param value der Wert des Schluessels
   * @return <code>true</code>, wenn der Schluessel bereits existierte und
   *         ueberschrieben wurde, sonst <code>false</code>
   */
   public boolean write(
      String chapter,
      String name,
      float value
   ) {

      return write( chapter, name, String.valueOf( value ) );

   } // write


   /**
   * Schreibt einen Schluessel mit einem <code>double</code>-Wert in die
   * Konfigurationsdatei.
   *
   * @param chapter das Kapitel des Schluessels
   * @param name der Name des Schluessels
   * @param value der Wert des Schluessels
   * @return <code>true</code>, wenn der Schluessel bereits existierte und
   *         ueberschrieben wurde, sonst <code>false</code>
   */
   public boolean write(
      String chapter,
      String name,
      double value
   ) {

      return write( chapter, name, String.valueOf( value ) );

   } // write


   /**
   * Veranlasst die Aktualisierung der Konfigurationsdatei. Alle Aenderungen
   * werden abgespeichert.
   */
   public void update() {

      if ( _changed ) {
         _changed = false;
         _updateIniFile();
      } // if

   } // update


   /**
   * Liefert eine textuelle Repraesentation der Ini-Datei (den Inhalt der
   * Datei inkl. Kommentar, jedoch keine Leerzeilen).
   *
   * @return den Inhalt der Konfigurationsdatei
   */
   public String toString() {

      String ret = new String();
      // Kommentare hinzufuegen:
      Iterator i = _comments.iterator();
      while ( i.hasNext() ) {
         ret = ret + (String) i.next() + '\n';
      } // while
      // Kapitel hinzufuegen:
      i = _chapters.iterator();
      while ( i.hasNext() ) {
         Chapter chapter = (Chapter) i.next();
         ret = ret + '[' + chapter.name + ']' + '\n';
         // Schluessel hinzufuegen:
         Iterator j = chapter.keys.iterator();
         while ( j.hasNext() ) {
            KeyEntry entry = (KeyEntry) j.next();
            ret = ret + entry.name + '=' + entry.value + '\n';
         } // while
      } // while
      return ret; // Rueckgabe

   } // toString


   // *************************************************************************
   // Private methods
   // *************************************************************************

   /*
   * Liest die aktuelle Ini-Datei in den Speicher und ueberschreibt damit alles.
   */
   private void _getIniFileContent() {

      // Initialisierungen:
      _comments = new LinkedList();
      _chapters = new LinkedList();
      
      // aktuelles Kapitel:
      Chapter chapter = null;

      // Oeffnen der Datei:
      try {
         BufferedReader br = new BufferedReader( new FileReader( _inifile ) );
         String line;
         // Auslesen des Dateiinhalts:
         do {
            line = br.readLine();
            if ( line != null ) {

               // Kommentarzeilen lesen:
               if ( line.length() == 0 ) continue;
               if ( ( chapter == null ) && ( line.charAt( 0 ) != '[' ) ) {
                  _comments.add( line );
               
               // echte Eintraege lesen:
               } else { // if
                  // neues Kapitel anfangen:
                  if ( line.charAt( 0 ) == '[' ) {
                     String name = line.substring( 1, line.length() - 1 );
                     chapter = new Chapter( name );
                     _chapters.add( chapter );
                  // neuen Schluesselwert lesen:
                  } else { // if
                     int eq = line.indexOf( '=' );
                     if ( eq > 0 && eq + 1 < line.length() ) {
                        String name = line.substring( 0, eq );
                        String value = line.substring( eq + 1 );
                        chapter.add( new KeyEntry( name, value ) );
                     } // if
                  } // else
               } // else
        
            } // if
         } while ( line != null );
         br.close(); // Schliessen der Datei
      } catch( IOException ioe ) { // try
         _comments = new LinkedList();
         _chapters = new LinkedList();
         _changed = true;
      } // catch

   } // _getIniFileContent


   /*
   * Speichert die aktuelle Konfigurationsdatei mit den Aenderungen ab.
   */
   private void _updateIniFile() {

   try {
      // Oeffnen der Datei:
      BufferedWriter bw = new BufferedWriter( new FileWriter( _inifile ) );
      
      // Speichern der Kommentarzeilen:
      Iterator i = _comments.iterator();
      while ( i.hasNext() ) {
         bw.write( (String) i.next() );
         bw.newLine();
      } // while
      
      // Speichern der Kapitel und Schluessel:
      boolean first = true; // Flag fuer erstes Kapitel (wegen Leerzeilen)
      i = _chapters.iterator();
      while ( i.hasNext() ) {
      	 // Zwischen Kapiteln eine Leerzeile einfuegen:
      	 if ( first ) {
      	    first = false;
      	 } else { // if
      	    bw.newLine();
      	 } // else
         Chapter chapter = (Chapter) i.next();
         bw.write( '[' + chapter.name + ']' );
         bw.newLine();
         Iterator j = chapter.keys.iterator();
         // in jedem Kapitel alle Schluessel speichern:
         while ( j.hasNext() ) {
            KeyEntry entry = (KeyEntry) j.next();
            bw.write( entry.name + '=' + entry.value );
            bw.newLine();
         } // while
      } // while
      bw.close(); // Schliessen der Datei
   } catch( IOException ioe ) {} // catch

   } // _updateIniFile


   /*
   * Sucht nach dem Kapitel mit dem gewuenschten Namen und liefert es zurueck.
   */
   private Chapter _search(
      String name
   ) {

      // Kapitelliste durchsuchen:
      Iterator i = _chapters.iterator();
      while ( i.hasNext() ) {
         Chapter chap = (Chapter) i.next();
         if ( chap.name.equals( name ) ) {
            return chap; // gesuchtes Kapitel gefunden
         } // if
      } // while
      return null; // Kapitel nicht gefunden

   } // _search


   /*
   * Sucht nach einem Schluessel in einem bestimmten Kapitel und liefert ihn
   * zurueck.
   */
   private KeyEntry _search(
      Chapter chapter,
      String keyname
   ) {

      // Schluesselliste durchsuchen:
      Iterator i = chapter.keys.iterator();
      while ( i.hasNext() ) {
         KeyEntry entry = (KeyEntry) i.next();
         if ( entry.name.equals( keyname ) ) {
            return entry; // gesuchten Schluessel gefunden
         } // if
      } // while
      return null; // Schluessel nicht gefunden

   } // _search


} // IniFile
