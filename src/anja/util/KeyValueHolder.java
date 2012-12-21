package anja.util;

/**<p>
* Eine Klasse, die KeyValueHolder implementiert, kann einen Schluessel 
* und ein Objekt speichern. KeyValueHolder wird beispielsweise bei
* ListItems verwendet.
*
* @author Thomas Wolf
* @version 1.0
*/
public interface KeyValueHolder {

 /** Den Schluesselwert setzen. */
 public final static int SET_KEY=6;
 /** Den Objektwert setzen. */
 public final static int SET_VALUE=7;

 /**
 * Liefert den gespeicherten Schluessel des Objektes.
 * @return gespeicherter Schluessel
 */
 public Object key();
 /**
 * Setzt den zugeordneten Schluessel.
 * @param key neuer Schluessel
 */
 public boolean setKey(Object key);
 /**
 * Liefert den gespeicherten Wert zurueck.
 * @return gespeichertes Objekt
 */
 public Object value();
 /**
 * Speichert das Objekt object.
 * @param object zu speicherndes Objekt
 */
 public boolean setValue(Object object);
}
