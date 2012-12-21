package anja.util;


// JDK 1.1 !!!!
/*import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;*/

/**
 * <p> Ein einfacher Baumknoten, der nur einen Wert speichert. Man
 * aendert den Wert sowohl mit setKey() als auch mit setValue(). Entsprechend
 * liefern key() und value() den selben Wert. An den Baum-Besitzer wird beim
 * Aendern des Wertes immer nur eine Anfrage nach aendern des Schluessels
 * gesendet.
 * 
 * @author Thomas Wolf
 * @version 1.0
 */
public class SimpleTreeItem
		extends BasicTreeItem
{

	/** Das gespeicherte Objekt. */
	private Object	_value	= null;


	// Konstruktoren
	// =============

	/**
	 * Standardkonstruktor. Der gespeicherte Wert wird auf null gesetzt.
	 * 
	 * @param maxrank
	 *            Der Grad des Baumes
	 */
	public SimpleTreeItem(
			int maxrank)
	{
		super(maxrank);
		_value = null;
	}


	/**
	 * Konstruktor mit Wertinitialisierung. Es wird hierbei <b>nicht</b>
	 * requestAccess aufgerufen!
	 * @param key
	 *            neuer Wert
	 */
	public SimpleTreeItem(
			int maxrank,
			Object key)
	{
		super(maxrank);
		_value = key;
	}


	// Allgemeine Routinen
	// ===================

	/**
	 * Ueberschreibt Object.toString().
	 */
	public String toString()
	{
		return getClass().getName() + "[value=" + _value + "]";
	}


	/**
	 * Klont das Objekt. Dabei sollte folgendes gelten: Das neue ListItem ist
	 * <strong>nicht</strong> verknuepft und hat <strong>keinen</strong>
	 * Besitzer. Die angehaengten Schluessel und Objekte werden ebenfalls
	 * geklont.
	 * @return Kopie des ListItems
	 */
	public Object clone()
	{
		return new SimpleTreeItem(maxRank(), _value);
	}


	/**
	 * Legt eine Kopie der im Objekt enthaltenen referenzierten Datenobjekte an.
	 * Schluesselobjekte werden nicht geklont und Verbindungen sowie Besitzer
	 * bleiben unveraendert.
	 */
	public void cloneData()
	{
		/*  if (_value==null) return;
		  // Der Zugriff auf die clone-Methode ist nicht soo einfach...
		  // (und funktioniert nur mit JDK 1.1)
		  try {
		   Method cloneMethod=_value.getClass().getDeclaredMethod("clone",new Class[0]);
		   if (Modifier.isPublic(cloneMethod.getModifiers())) {
		    _value=cloneMethod.invoke(_value,new Object[0]);
		   }
		  } catch (NoSuchMethodException e) {}
		    catch (SecurityException e) {}
		    catch (IllegalAccessException e) {}
		    catch (IllegalArgumentException e) {}
		    catch (InvocationTargetException e) {}*/
	}


	// Eigentum
	// ========

	/**
	 * Liefert den gespeicherten Schluessel des Objektes.
	 * @return gespeicherter Schluessel
	 */
	public Object key()
	{
		return _value;
	}


	/**
	 * Setzt den zugeordneten Schluessel.
	 * @param key
	 *            neuer Schluessel
	 */
	public synchronized boolean setKey(
			Object key)
	{
		if (_value == key)
			return true;
		if (!requestTreeAccess(SET_KEY, key))
			return false;
		_value = key;
		return true;
	}


	/**
	 * Liefert den gespeicherten Wert zurueck.
	 * @return gespeichertes Objekt
	 */
	public Object value()
	{
		return _value;
	}


	/**
	 * Speichert das Objekt object.
	 * @param object
	 *            zu speicherndes Objekt
	 */
	public boolean setValue(
			Object object)
	{
		return setKey(object);
	}

}
